/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.www;

import com.sun.jersey.spi.container.servlet.ServletContainer;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.jaas.JAASLoginService;
import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.DefaultIdentityService;
import org.eclipse.jetty.security.HashLoginService;
import org.eclipse.jetty.security.UserStore;
import org.eclipse.jetty.security.authentication.BasicAuthenticator;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.LowResourceMonitor;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.security.Constraint;
import org.eclipse.jetty.util.security.Password;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.extension.ExtensionPointHandler;
import org.pentaho.di.core.extension.KettleExtensionPoint;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.plugins.CartePluginType;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.i18n.BaseMessages;

import javax.servlet.Servlet;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class WebServer {

  private static final int DEFAULT_DETECTION_TIMER = 20000;
  public static final String SERVICE_NAME = "Kettle";
  private static Class<?> PKG = WebServer.class; // for i18n purposes, needed by Translator2!!

  private LogChannelInterface log;

  public static final int PORT = 80;

  private Server server;

  private TransformationMap transformationMap;
  private JobMap jobMap;
  private List<SlaveServerDetection> detections;
  private SocketRepository socketRepository;

  private String hostname;
  private int port;

  private Timer slaveMonitoringTimer;

  private String passwordFile;
  private WebServerShutdownHook webServerShutdownHook;
  private IWebServerShutdownHandler webServerShutdownHandler = new DefaultWebServerShutdownHandler();

  private SslConfiguration sslConfig;

  public WebServer( LogChannelInterface log, TransformationMap transformationMap, JobMap jobMap,
      SocketRepository socketRepository, List<SlaveServerDetection> detections, String hostname, int port, boolean join,
      String passwordFile ) throws Exception {
    this( log, transformationMap, jobMap, socketRepository, detections, hostname, port, join, passwordFile, null );
  }

  public WebServer( LogChannelInterface log, TransformationMap transformationMap, JobMap jobMap,
      SocketRepository socketRepository, List<SlaveServerDetection> detections, String hostname, int port, boolean join,
      String passwordFile, SslConfiguration sslConfig ) throws Exception {
    this.log = log;
    this.transformationMap = transformationMap;
    this.jobMap = jobMap;
    this.socketRepository = socketRepository;
    this.detections = detections;
    this.hostname = hostname;
    this.port = port;
    this.passwordFile = passwordFile;
    this.sslConfig = sslConfig;

    startServer();

    // Start the monitoring of the registered slave servers...
    //
    startSlaveMonitoring();

    webServerShutdownHook = new WebServerShutdownHook( this );
    Runtime.getRuntime().addShutdownHook( webServerShutdownHook );

    try {
      ExtensionPointHandler.callExtensionPoint( log, KettleExtensionPoint.CarteStartup.id, this );
    } catch ( KettleException e ) {
      // Log error but continue regular operations to make sure Carte continues to run properly
      //
      log.logError( "Error calling extension point CarteStartup", e );
    }

    if ( join ) {
      server.join();
    }
  }

  public WebServer( LogChannelInterface log, TransformationMap transformationMap, JobMap jobMap,
      SocketRepository socketRepository, List<SlaveServerDetection> slaveServers, String hostname, int port )
      throws Exception {
    this( log, transformationMap, jobMap, socketRepository, slaveServers, hostname, port, true );
  }

  public WebServer( LogChannelInterface log, TransformationMap transformationMap, JobMap jobMap,
      SocketRepository socketRepository, List<SlaveServerDetection> detections, String hostname, int port,
      boolean join ) throws Exception {
    this( log, transformationMap, jobMap, socketRepository, detections, hostname, port, join, null, null );
  }

  public Server getServer() {
    return server;
  }

  public void startServer() throws Exception {
    server = new Server();

    List<String> roles = new ArrayList<>();
    roles.add( Constraint.ANY_AUTH );

    // Set up the security handler, optionally with JAAS
    //
    ConstraintSecurityHandler securityHandler = new ConstraintSecurityHandler();
    
    if ( System.getProperty( "loginmodulename" ) != null
        && System.getProperty( "java.security.auth.login.config" ) != null ) {
      JAASLoginService jaasLoginService = new JAASLoginService( SERVICE_NAME );
      jaasLoginService.setLoginModuleName( System.getProperty( "loginmodulename" ) );
      if( jaasLoginService.getIdentityService() == null ) {
        jaasLoginService.setIdentityService( new DefaultIdentityService() );
      }
      securityHandler.setLoginService( jaasLoginService );
    } else {
      HashLoginService hashLoginService;
      SlaveServer slaveServer = transformationMap.getSlaveServerConfig().getSlaveServer();
      if ( !Utils.isEmpty( slaveServer.getPassword() ) ) {
        hashLoginService = new HashLoginService( SERVICE_NAME );
        UserStore userStore = new UserStore();
        userStore.addUser( slaveServer.getUsername(), new Password( slaveServer.getPassword() ),
          new String[] { } );
        hashLoginService.setUserStore( userStore );
      } else {
        // See if there is a kettle.pwd file in the KETTLE_HOME directory:
        if ( Utils.isEmpty( passwordFile ) ) {
          File homePwdFile = new File( Const.getKettleCartePasswordFile() );
          if ( homePwdFile.exists() ) {
            passwordFile = Const.getKettleCartePasswordFile();
          } else {
            passwordFile = Const.getKettleLocalCartePasswordFile();
          }
        }
        hashLoginService = new HashLoginService( SERVICE_NAME, passwordFile ) {
          @Override
          protected String[] loadRoleInfo( UserPrincipal user ) {
            List<String> newRoles = new ArrayList<>();
            String[] roles = super.loadRoleInfo( user );
            if ( null != roles ) {
              Collections.addAll( newRoles, roles );
            }
            return newRoles.toArray( new String[ newRoles.size() ] );
          }
        };
      }
      securityHandler.setLoginService( hashLoginService );
    }

    Constraint constraint = new Constraint();
    constraint.setName( Constraint.__BASIC_AUTH );
    constraint.setRoles( roles.toArray( new String[ roles.size() ] ) );
    constraint.setAuthenticate( true );

    ConstraintMapping constraintMapping = new ConstraintMapping();
    constraintMapping.setConstraint( constraint );
    constraintMapping.setPathSpec( "/*" );

    securityHandler.setConstraintMappings( new ConstraintMapping[] { constraintMapping } );
    securityHandler.setAuthenticator( new BasicAuthenticator() );

    
    // Add all the servlets defined in kettle-servlets.xml ...
    //
    ContextHandlerCollection contexts = new ContextHandlerCollection();

    // Root
    //
    ServletContextHandler
        root =
        new ServletContextHandler( contexts, GetRootServlet.CONTEXT_PATH, ServletContextHandler.SESSIONS );
    GetRootServlet rootServlet = new GetRootServlet();
    rootServlet.setJettyMode( true );
    root.addServlet( new ServletHolder( rootServlet ), "/*" );

    PluginRegistry pluginRegistry = PluginRegistry.getInstance();
    List<PluginInterface> plugins = pluginRegistry.getPlugins( CartePluginType.class );
    for ( PluginInterface plugin : plugins ) {

      CartePluginInterface servlet = pluginRegistry.loadClass( plugin, CartePluginInterface.class );
      servlet.setup( transformationMap, jobMap, socketRepository, detections );
      servlet.setJettyMode( true );

      ServletContextHandler servletContext =
        new ServletContextHandler( contexts, getContextPath( servlet ), ServletContextHandler.SESSIONS );
      ServletHolder servletHolder = new ServletHolder( (Servlet) servlet );
      servletContext.addServlet( servletHolder, "/*" );
    }

    // setup jersey (REST)
    ServletHolder jerseyServletHolder = new ServletHolder( ServletContainer.class );
    jerseyServletHolder.setInitParameter( "com.sun.jersey.config.property.resourceConfigClass",
        "com.sun.jersey.api.core.PackagesResourceConfig" );
    jerseyServletHolder.setInitParameter( "com.sun.jersey.config.property.packages", "org.pentaho.di.www.jaxrs" );
    root.addServlet( jerseyServletHolder, "/api/*" );

    // setup static resource serving
    // ResourceHandler mobileResourceHandler = new ResourceHandler();
    // mobileResourceHandler.setWelcomeFiles(new String[]{"index.html"});
    // mobileResourceHandler.setResourceBase(getClass().getClassLoader().
    // getResource("org/pentaho/di/www/mobile").toExternalForm());
    // Context mobileContext = new Context(contexts, "/mobile", Context.SESSIONS);
    // mobileContext.setHandler(mobileResourceHandler);

    // Allow png files to be shown for transformations and jobs...
    //
    ResourceHandler resourceHandler = new ResourceHandler();
    resourceHandler.setResourceBase( "temp" );
    // add all handlers/contexts to server

    // set up static servlet
    ServletHolder staticHolder = new ServletHolder( "static", DefaultServlet.class );
    // resourceBase maps to the path relative to where carte is started
    staticHolder.setInitParameter( "resourceBase", "./static/" );
    staticHolder.setInitParameter( "dirAllowed", "true" );
    staticHolder.setInitParameter( "pathInfoOnly", "true" );
    root.addServlet( staticHolder, "/static/*" );

    HandlerList handlers = new HandlerList();
    handlers.setHandlers( new Handler[] { resourceHandler, contexts } );
    securityHandler.setHandler( handlers );

    server.setHandler( securityHandler );
    
    ExtensionPointHandler.callExtensionPoint( log, KettleExtensionPoint.BeforeCarteStartup.id, server );
    
    // Start execution
    createListeners();

    server.start();
  }

  public String getContextPath( CartePluginInterface servlet ) {
    String contextPath = servlet.getContextPath();
    if ( !contextPath.startsWith( "/kettle" ) ) {
      contextPath = "/kettle" + contextPath;
    }
    return contextPath;
  }

  public void join() throws InterruptedException {
    server.join();
  }

  public void stopServer() {

    webServerShutdownHook.setShuttingDown( true );

    try {
      ExtensionPointHandler.callExtensionPoint( log, KettleExtensionPoint.CarteShutdown.id, this );
    } catch ( KettleException e ) {
      // Log error but continue regular operations to make sure Carte can be shut down properly.
      //
      log.logError( "Error calling extension point CarteShutdown", e );
    }

    try {
      if ( server != null ) {

        // Stop the monitoring timer
        //
        if ( slaveMonitoringTimer != null ) {
          slaveMonitoringTimer.cancel();
          slaveMonitoringTimer = null;
        }

        // Clean up all the server sockets...
        //
        socketRepository.closeAll();

        // Stop the server...
        //
        server.stop();
        KettleEnvironment.shutdown();
        if ( webServerShutdownHandler != null ) {
          webServerShutdownHandler.shutdownWebServer();
        }
      }
    } catch ( Exception e ) {
      log.logError( BaseMessages.getString( PKG, "WebServer.Error.FailedToStop.Title" ),
          BaseMessages.getString( PKG, "WebServer.Error.FailedToStop.Msg", "" + e ) );
    }
  }

  private void createListeners() {

    ServerConnector serverConnector = getServerConnector();
    serverConnector.setPort( port );
    serverConnector.setHost( hostname );
    serverConnector.setName( BaseMessages.getString( PKG, "WebServer.Log.KettleHTTPListener", hostname ) );
    log.logBasic( BaseMessages.getString( PKG, "WebServer.Log.CreateListener", hostname, "" + port ) );

    server.setConnectors( new Connector[] { serverConnector } );
  }

  private ServerConnector getServerConnector() {
    ServerConnector serverConnector = null;
    int jettyAcceptors = -1;

    // Check if there's configuration for the number of acceptors to use
    if ( validProperty( Const.KETTLE_CARTE_JETTY_ACCEPTORS ) ) {
      jettyAcceptors = Integer.parseInt( System.getProperty( Const.KETTLE_CARTE_JETTY_ACCEPTORS ) );

      log.logBasic(
        BaseMessages.getString( PKG, "WebServer.Log.ConfigOptions", "acceptors", jettyAcceptors ) );
    }

    // Create the server with the configurated number of acceptors
    if ( sslConfig != null ) {
      log.logBasic( BaseMessages.getString( PKG, "WebServer.Log.SslModeUsing" ) );
      SslContextFactory sslContextFactory = new SslContextFactory();
      sslContextFactory.setKeyStorePath( sslConfig.getKeyStore() );
      sslContextFactory.setKeyStorePassword( sslConfig.getKeyStorePassword() );
      sslContextFactory.setKeyManagerPassword( sslConfig.getKeyPassword() );
      sslContextFactory.setKeyStoreType( sslConfig.getKeyStoreType() );

      HttpConfiguration https = new HttpConfiguration();
      https.addCustomizer( new SecureRequestCustomizer() );
      serverConnector = new ServerConnector( server, jettyAcceptors, -1,
        new SslConnectionFactory( sslContextFactory, HttpVersion.HTTP_1_1.asString() ),
        new HttpConnectionFactory( https ) );
    } else {
      serverConnector = new ServerConnector( server, jettyAcceptors, -1 );
    }

    // Low resources options
    if ( validProperty( Const.KETTLE_CARTE_JETTY_ACCEPT_QUEUE_SIZE ) ) {
      serverConnector
        .setAcceptQueueSize( Integer.parseInt( System.getProperty( Const.KETTLE_CARTE_JETTY_ACCEPT_QUEUE_SIZE ) ) );
      log.logBasic( BaseMessages
        .getString( PKG, "WebServer.Log.ConfigOptions", "acceptQueueSize", serverConnector.getAcceptQueueSize() ) );
    }

    if ( validProperty( Const.KETTLE_CARTE_JETTY_RES_MAX_IDLE_TIME ) ) {
      LowResourceMonitor lowResourceMonitor = new LowResourceMonitor( server );
      lowResourceMonitor.setLowResourcesIdleTimeout(
        Integer.parseInt( System.getProperty( Const.KETTLE_CARTE_JETTY_RES_MAX_IDLE_TIME ) ) );
      server.addBean( lowResourceMonitor );
      log.logBasic( BaseMessages.getString( PKG, "WebServer.Log.ConfigOptions", "lowResourcesMaxIdleTime",
        lowResourceMonitor.getLowResourcesIdleTimeout() ) );
    }

    return serverConnector;
  }

  /**
   * Checks if the property is not null or not empty String that can be parseable as int and returns true if it is,
   * otherwise false
   *
   * @param property the property to check
   * @return true if the property is not null or not empty String that can be parseable as int, false otherwise
   */
  private boolean validProperty( String property ) {
    boolean isValid = false;
    if ( System.getProperty( property ) != null && System.getProperty( property ).length() > 0 ) {
      try {
        Integer.parseInt( System.getProperty( property ) );
        isValid = true;
      } catch ( NumberFormatException nmbfExc ) {
        log.logBasic( BaseMessages
            .getString( PKG, "WebServer.Log.ConfigOptionsInvalid", property, System.getProperty( property ) ) );
      }
    }
    return isValid;
  }

  /**
   * @return the hostname
   */
  public String getHostname() {
    return hostname;
  }

  /**
   * @param hostname the hostname to set
   */
  public void setHostname( String hostname ) {
    this.hostname = hostname;
  }

  /**
   * @return the slave server detections
   */
  public List<SlaveServerDetection> getDetections() {
    return detections;
  }

  /**
   * This method registers a timer to check up on all the registered slave servers every X seconds.<br>
   */
  private void startSlaveMonitoring() {
    slaveMonitoringTimer = new Timer( "WebServer Timer" );
    TimerTask timerTask = new TimerTask() {

      public void run() {
        for ( SlaveServerDetection slaveServerDetection : detections ) {
          SlaveServer slaveServer = slaveServerDetection.getSlaveServer();

          // See if we can get a status...
          //
          try {
            // TODO: consider making this lighter or retaining more information...
            slaveServer.getStatus(); // throws the exception
            slaveServerDetection.setActive( true );
            slaveServerDetection.setLastActiveDate( new Date() );
          } catch ( Exception e ) {
            slaveServerDetection.setActive( false );
            slaveServerDetection.setLastInactiveDate( new Date() );

            // TODO: kick it out after a configurable period of time...
          }
        }
      }
    };
    int detectionTime = defaultDetectionTimer();
    slaveMonitoringTimer.schedule( timerTask, detectionTime, detectionTime );
  }

  /**
   * @return the socketRepository
   */
  public SocketRepository getSocketRepository() {
    return socketRepository;
  }

  /**
   * @param socketRepository the socketRepository to set
   */
  public void setSocketRepository( SocketRepository socketRepository ) {
    this.socketRepository = socketRepository;
  }

  public String getPasswordFile() {
    return passwordFile;
  }

  public void setPasswordFile( String passwordFile ) {
    this.passwordFile = passwordFile;
  }

  public LogChannelInterface getLog() {
    return log;
  }

  public void setLog( LogChannelInterface log ) {
    this.log = log;
  }

  public TransformationMap getTransformationMap() {
    return transformationMap;
  }

  public void setTransformationMap( TransformationMap transformationMap ) {
    this.transformationMap = transformationMap;
  }

  public JobMap getJobMap() {
    return jobMap;
  }

  public void setJobMap( JobMap jobMap ) {
    this.jobMap = jobMap;
  }

  public int getPort() {
    return port;
  }

  public void setPort( int port ) {
    this.port = port;
  }

  public Timer getSlaveMonitoringTimer() {
    return slaveMonitoringTimer;
  }

  public void setSlaveMonitoringTimer( Timer slaveMonitoringTimer ) {
    this.slaveMonitoringTimer = slaveMonitoringTimer;
  }

  public void setServer( Server server ) {
    this.server = server;
  }

  public void setDetections( List<SlaveServerDetection> detections ) {
    this.detections = detections;
  }

  /**
   * Can be used to override the default shutdown behavior of performing a System.exit
   *
   * @param webServerShutdownHandler
   */
  public void setWebServerShutdownHandler( IWebServerShutdownHandler webServerShutdownHandler ) {
    this.webServerShutdownHandler = webServerShutdownHandler;
  }

  public int defaultDetectionTimer() {
    String sDetectionTimer = System.getProperty( Const.KETTLE_SLAVE_DETECTION_TIMER );

    if ( sDetectionTimer != null ) {
      return Integer.parseInt( sDetectionTimer );
    } else {
      return DEFAULT_DETECTION_TIMER;
    }
  }
}
