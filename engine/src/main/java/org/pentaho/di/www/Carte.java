/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.di.www;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.vfs2.FileObject;
import org.pentaho.di.cluster.HttpUtil;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.util.EnvUtil;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;

public class Carte {
  private static Class<?> PKG = Carte.class; // for i18n purposes, needed by Translator2!!

  private WebServer webServer;
  private SlaveServerConfig config;
  private boolean allOK;
  private static Options options;
  private static final String NO_SERVER_FOUND_ERROR = "Carte.Error.NoServerFound";

  public Carte( final SlaveServerConfig config ) throws Exception {
    this( config, null );
  }

  public Carte( final SlaveServerConfig config, Boolean joinOverride ) throws Exception {
    this.config = config;

    allOK = true;

    CarteSingleton.setSlaveServerConfig( config );
    LogChannelInterface log = CarteSingleton.getInstance().getLog();

    final TransformationMap transformationMap = CarteSingleton.getInstance().getTransformationMap();
    transformationMap.setSlaveServerConfig( config );
    final JobMap jobMap = CarteSingleton.getInstance().getJobMap();
    jobMap.setSlaveServerConfig( config );
    List<SlaveServerDetection> detections = new CopyOnWriteArrayList<SlaveServerDetection>();
    SocketRepository socketRepository = CarteSingleton.getInstance().getSocketRepository();

    SlaveServer slaveServer = config.getSlaveServer();

    String hostname = slaveServer.getHostname();
    int port = WebServer.PORT;
    if ( !Utils.isEmpty( slaveServer.getPort() ) ) {
      try {
        port = Integer.parseInt( slaveServer.getPort() );
      } catch ( Exception e ) {
        log.logError( BaseMessages.getString( PKG, "Carte.Error.CanNotPartPort", slaveServer.getHostname(), "" + port ),
            e );
        allOK = false;
      }
    }

    // TODO: see if we need to keep doing this on a periodic basis.
    // The master might be dead or not alive yet at the time we send this message.
    // Repeating the registration over and over every few minutes might harden this sort of problems.
    //
    Properties masterProperties = null;
    if ( config.isReportingToMasters() ) {
      String propertiesMaster = slaveServer.getPropertiesMasterName();
      for ( final SlaveServer master : config.getMasters() ) {
        // Here we use the username/password specified in the slave server section of the configuration.
        // This doesn't have to be the same pair as the one used on the master!
        //
        try {
          SlaveServerDetection slaveServerDetection = new SlaveServerDetection( slaveServer.getClient() );
          master.sendXML( slaveServerDetection.getXML(), RegisterSlaveServlet.CONTEXT_PATH + "/" );
          log.logBasic( "Registered this slave server to master slave server [" + master.toString() + "] on address ["
              + master.getServerAndPort() + "]" );
        } catch ( Exception e ) {
          log.logError( "Unable to register to master slave server [" + master.toString() + "] on address [" + master
              .getServerAndPort() + "]" );
          allOK = false;
        }
        try {
          if ( !StringUtils.isBlank( propertiesMaster ) && propertiesMaster.equalsIgnoreCase( master.getName() ) ) {
            if ( masterProperties != null ) {
              log.logError( "More than one primary master server. Master name is " + propertiesMaster );
            } else {
              masterProperties = master.getKettleProperties();
              log.logBasic( "Got properties from master server [" + master.toString() + "], address [" + master
                  .getServerAndPort() + "]" );
            }
          }
        } catch ( Exception e ) {
          log.logError( "Unable to get properties from master server [" + master.toString() + "], address [" + master
              .getServerAndPort() + "]" );
          allOK = false;
        }
      }
    }
    if ( masterProperties != null ) {
      EnvUtil.applyKettleProperties( masterProperties, slaveServer.isOverrideExistingProperties() );
    }

    // If we need to time out finished or idle objects, we should create a timer in the background to clean
    // this is done automatically now
    // CarteSingleton.installPurgeTimer(config, log, transformationMap, jobMap);

    if ( allOK ) {
      boolean shouldJoin = config.isJoining();
      if ( joinOverride != null ) {
        shouldJoin = joinOverride;
      }

      this.webServer =
          new WebServer( log, transformationMap, jobMap, socketRepository, detections, hostname, port, shouldJoin,
              config.getPasswordFile(), slaveServer.getSslConfig() );
    }
  }

  public static void main( String[] args ) {
    try {
      parseAndRunCommand( args );
    } catch ( Exception e ) {
      e.printStackTrace();
    }
  }

  @SuppressWarnings( "static-access" )
  private static void parseAndRunCommand( String[] args ) throws Exception {
    options = new Options();
    options.addOption( OptionBuilder.withLongOpt( "stop" ).withDescription( BaseMessages.getString( PKG,
        "Carte.ParamDescription.stop" ) ).hasArg( false ).isRequired( false ).create( 's' ) );
    options.addOption( OptionBuilder.withLongOpt( "userName" ).withDescription( BaseMessages.getString( PKG,
        "Carte.ParamDescription.userName" ) ).hasArg( true ).isRequired( false ).create( 'u' ) );
    options.addOption( OptionBuilder.withLongOpt( "password" ).withDescription( BaseMessages.getString( PKG,
        "Carte.ParamDescription.password" ) ).hasArg( true ).isRequired( false ).create( 'p' ) );
    options.addOption( OptionBuilder.withLongOpt( "help" ).withDescription( BaseMessages.getString( PKG,
        "Carte.ParamDescription.help" ) ).create( 'h' ) );

    CommandLineParser parser = new BasicParser();
    CommandLine cmd = parser.parse( options, args );

    if ( cmd.hasOption( 'h' ) ) {
      displayHelpAndAbort();
    }

    String[] arguments = cmd.getArgs();
    boolean usingConfigFile = false;

    // Load from an xml file that describes the complete configuration...
    //
    SlaveServerConfig config = null;
    if ( arguments.length == 1 && !Utils.isEmpty( arguments[0] ) ) {
      usingConfigFile = true;
      FileObject file = KettleVFS.getFileObject( arguments[0] );
      Document document = XMLHandler.loadXMLFile( file );
      setKettleEnvironment(); // Must stand up server now to allow decryption of password
      Node configNode = XMLHandler.getSubNode( document, SlaveServerConfig.XML_TAG );
      config = new SlaveServerConfig( new LogChannel( "Slave server config" ), configNode );
      if ( config.getAutoSequence() != null ) {
        config.readAutoSequences();
      }
      config.setFilename( arguments[0] );

      if ( cmd.hasOption( 's' ) ) {
        String user = cmd.getOptionValue( 'u' );
        String password = cmd.getOptionValue( 'p' );
        SlaveServer slaveServer = config.getSlaveServer();
        shutdown( slaveServer.getHostname(), slaveServer.getPort(), user, password, slaveServer.isSslMode(), slaveServer.getSslConfig() );
        System.exit( 0 );
      }
    }
    if ( arguments.length == 2 && !Utils.isEmpty( arguments[0] ) && !Utils.isEmpty( arguments[1] ) ) {
      String hostname = arguments[0];
      String port = arguments[1];

      if ( cmd.hasOption( 's' ) ) {
        String user = cmd.getOptionValue( 'u' );
        String password = cmd.getOptionValue( 'p' );
        shutdown( hostname, port, user, password, false, null );
        System.exit( 0 );
      }

      SlaveServer slaveServer = new SlaveServer( hostname + ":" + port, hostname, port, null, null );

      config = new SlaveServerConfig();
      config.setSlaveServer( slaveServer );
    }

    // Nothing configured: show the usage
    //
    if ( config == null ) {
      displayHelpAndAbort();
    }

    if ( !usingConfigFile ) {
      setKettleEnvironment();
    }
    runCarte( config );
  }

  private static void setKettleEnvironment() throws Exception {
    KettleClientEnvironment.getInstance().setClient( KettleClientEnvironment.ClientType.CARTE );
    KettleEnvironment.init();
  }

  public static void runCarte( SlaveServerConfig config ) throws Exception {
    KettleLogStore.init( config.getMaxLogLines(), config.getMaxLogTimeoutMinutes() );

    config.setJoining( true );

    Carte carte = new Carte( config, false );
    CarteSingleton.setCarte( carte );

    carte.getWebServer().join();
  }

  /**
   * @return the webServer
   */
  public WebServer getWebServer() {
    return webServer;
  }

  /**
   * @param webServer
   *          the webServer to set
   */
  public void setWebServer( WebServer webServer ) {
    this.webServer = webServer;
  }

  /**
   * @return the slave server (Carte) configuration
   */
  public SlaveServerConfig getConfig() {
    return config;
  }

  /**
   * @param config
   *          the slave server (Carte) configuration
   */
  public void setConfig( SlaveServerConfig config ) {
    this.config = config;
  }

  private static void displayHelpAndAbort() {
    HelpFormatter formatter = new HelpFormatter();
    String optionsHelp = getOptionsHelpForUsage();
    String header =
        BaseMessages.getString( PKG, "Carte.Usage.Text" ) + optionsHelp + "\nor\n" + BaseMessages.getString( PKG,
            "Carte.Usage.Text2" ) + "\n\n" + BaseMessages.getString( PKG, "Carte.MainDescription" );

    StringWriter stringWriter = new StringWriter();
    PrintWriter printWriter = new PrintWriter( stringWriter );
    formatter.printHelp( printWriter, 80, "CarteDummy", header, options, 5, 5, "", false );
    System.err.println( stripOff( stringWriter.toString(), "usage: CarteDummy" ) );

    System.err.println( BaseMessages.getString( PKG, "Carte.Usage.Example" ) + ": Carte 127.0.0.1 8080" );
    System.err.println( BaseMessages.getString( PKG, "Carte.Usage.Example" ) + ": Carte 192.168.1.221 8081" );
    System.err.println();
    System.err.println( BaseMessages.getString( PKG, "Carte.Usage.Example" ) + ": Carte /foo/bar/carte-config.xml" );
    System.err.println( BaseMessages.getString( PKG, "Carte.Usage.Example" )
        + ": Carte http://www.example.com/carte-config.xml" );
    System.err.println( BaseMessages.getString( PKG, "Carte.Usage.Example" )
        + ": Carte 127.0.0.1 8080 -s -u cluster -p cluster" );

    System.exit( 1 );
  }

  private static String getOptionsHelpForUsage() {
    HelpFormatter formatter = new HelpFormatter();
    StringWriter stringWriter = new StringWriter();
    PrintWriter printWriter = new PrintWriter( stringWriter );
    formatter.printUsage( printWriter, 999, "", options );
    return stripOff( stringWriter.toString(), "usage: " ); // Strip off the "usage:" so it can be localized
  }

  private static String stripOff( String target, String strip ) {
    return target.substring( target.indexOf( strip ) + strip.length() );
  }

  private static void shutdown( String hostname, String port, String username, String password, boolean sslMode, SslConfiguration sslConfig ) {
    try {
      callStopCarteRestService( hostname, port, username, password, sslMode, sslConfig );
    } catch ( Exception e ) {
      e.printStackTrace();
    }
  }

  /**
   * Checks that Carte is running and if so, shuts down the Carte server
   *
   * @param hostname
   * @param port
   * @param username
   * @param password
   * @throws ParseException
   * @throws CarteCommandException
   */
  @VisibleForTesting
  static void callStopCarteRestService( String hostname, String port, String username, String password, boolean sslMode, SslConfiguration sslConfig )
    throws ParseException, CarteCommandException {
    // get information about the remote connection
    try {
      KettleClientEnvironment.init();

      Client client = null;
      if ( sslMode ) {
        client = createClientWithSSLConfig( sslConfig, hostname, port );
      } else {
        client = ClientBuilder.newClient();
      }

      client.register( HttpAuthenticationFeature.basic( username, Encr.decryptPasswordOptionallyEncrypted( password ) ) );

      // check if the user can access the carte server. Don't really need this call but may want to check it's output at
      // some point
      String contextURL = HttpUtil.constructUrl( new Variables(), hostname, port, "kettle", "", sslMode );
      WebTarget target = client.target( contextURL + "/status/?xml=Y" );
      String response = target.request().get( String.class );
      if ( response == null || !response.contains( "<serverstatus>" ) ) {
        throw new Carte.CarteCommandException( BaseMessages.getString( PKG, NO_SERVER_FOUND_ERROR, hostname, ""
            + port ) );
      }

      // This is the call that matters
      target = client.target( contextURL + "/stopCarte" );
      response = target.request().get( String.class );
      if ( response == null || !response.contains( "Shutting Down" ) ) {
        throw new Carte.CarteCommandException( BaseMessages.getString( PKG, "Carte.Error.NoShutdown", hostname, ""
            + port ) );
      }
    } catch ( Exception e ) {
      throw new Carte.CarteCommandException( BaseMessages.getString( PKG, NO_SERVER_FOUND_ERROR, hostname, ""
          + port ), e );
    }
  }

  private static Client createClientWithSSLConfig( SslConfiguration sslConfig, String hostname, String port  )
    throws CarteCommandException {
    HostnameVerifier hostnameVerifier = HttpsURLConnection.getDefaultHostnameVerifier();
    SSLContext sslContext;
    try {
      sslContext = getSSLContext( sslConfig.getKeyStore(), sslConfig.getKeyStorePassword() );
    } catch ( Exception e ) {
      CarteSingleton.getInstance().getLog().logError( "Unable to create SSL context. Please check SSL configuration." );
      throw new Carte.CarteCommandException( BaseMessages.getString( PKG, NO_SERVER_FOUND_ERROR, hostname, "" + port ), e );
    }

    return ClientBuilder.newBuilder().sslContext( sslContext ).hostnameVerifier( hostnameVerifier ).build();
  }

  private static SSLContext getSSLContext( String keyStore, String keyStorePassword ) throws IOException, GeneralSecurityException {
    KeyStore ks = KeyStore.getInstance( "JKS" );

    // Using a try-with-resources block to guarantee that the stream is always automatically closed
    try ( FileInputStream keyStoreStream = new FileInputStream( keyStore ) ) {
      ks.load( keyStoreStream, Encr.decryptPasswordOptionallyEncrypted( keyStorePassword ).toCharArray() );
    }

    KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance( KeyManagerFactory.getDefaultAlgorithm() );
    keyManagerFactory.init( ks, Encr.decryptPasswordOptionallyEncrypted(keyStorePassword).toCharArray());

    TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance( TrustManagerFactory.getDefaultAlgorithm() );
    trustManagerFactory.init( ks );

    SSLContext sslContext = SSLContext.getInstance( "TLS" );
    sslContext.init( keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null );

    return sslContext;
  }

  /**
   * Exception generated when command line fails
   */
  public static class CarteCommandException extends Exception {
    private static final long serialVersionUID = 1L;

    public CarteCommandException() {
    }

    public CarteCommandException( final String message ) {
      super( message );
    }

    public CarteCommandException( final String message, final Throwable cause ) {
      super( message, cause );
    }

    public CarteCommandException( final Throwable cause ) {
      super( cause );
    }
  }
}
