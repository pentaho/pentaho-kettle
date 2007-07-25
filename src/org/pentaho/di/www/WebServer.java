package org.pentaho.di.www;

import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.bio.SocketConnector;
import org.mortbay.jetty.handler.ContextHandlerCollection;
import org.mortbay.jetty.security.Constraint;
import org.mortbay.jetty.security.ConstraintMapping;
import org.mortbay.jetty.security.HashUserRealm;
import org.mortbay.jetty.security.SecurityHandler;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;
import org.pentaho.di.core.logging.LogWriter;



public class WebServer
{
    private static LogWriter log = LogWriter.getInstance();
    
    public  static final int PORT = 80;

    private Server             server;
    
    private TransformationMap  transformationMap;

    private String hostname;
    private int port;

    public WebServer(TransformationMap transformationMap, String hostname, int port) throws Exception
    {
        this.transformationMap = transformationMap;
        this.hostname = hostname;
        this.port = port;
        
        startServer();
    }

    public Server getServer()
    {
        return server;
    }

    public void startServer() throws Exception
    {
        server = new Server();

        Constraint constraint = new Constraint();
        constraint.setName(Constraint.__BASIC_AUTH);;
        constraint.setRoles( new String[] { Constraint.ANY_ROLE } );
        constraint.setAuthenticate(true);
        
        ConstraintMapping constraintMapping = new ConstraintMapping();
        constraintMapping.setConstraint(constraint);
        constraintMapping.setPathSpec("/*");

        SecurityHandler securityHandler = new SecurityHandler();
        securityHandler.setUserRealm(new HashUserRealm("Kettle", "pwd/kettle.pwd"));
        securityHandler.setConstraintMappings(new ConstraintMapping[]{constraintMapping});
               
        // Add all the servlets...
        //
        ContextHandlerCollection contexts = new ContextHandlerCollection();
        
        // Root
        Context root = new Context(contexts, GetRootServlet.CONTEXT_PATH, Context.SESSIONS);
        root.addServlet(new ServletHolder(new GetRootServlet()), "/*");
        
        // Carte Status
        Context status = new Context(contexts, GetStatusServlet.CONTEXT_PATH, Context.SESSIONS);
        status.addServlet(new ServletHolder(new GetStatusServlet(transformationMap)), "/*");

        // Trans status
        Context transStatus = new Context(contexts, GetTransStatusServlet.CONTEXT_PATH, Context.SESSIONS);
        transStatus.addServlet(new ServletHolder(new GetTransStatusServlet(transformationMap)), "/*");

        // Prepare execution
        Context prepareExecution = new Context(contexts, PrepareExecutionTransServlet.CONTEXT_PATH, Context.SESSIONS);
        prepareExecution.addServlet(new ServletHolder(new PrepareExecutionTransServlet(transformationMap)), "/*");
        
        // Start execution
        Context startExecution = new Context(contexts, StartExecutionTransServlet.CONTEXT_PATH, Context.SESSIONS);
        startExecution.addServlet(new ServletHolder(new StartExecutionTransServlet(transformationMap)), "/*");

        // Start transformation
        Context startTrans = new Context(contexts, StartTransServlet.CONTEXT_PATH, Context.SESSIONS);
        startTrans.addServlet(new ServletHolder(new StartTransServlet(transformationMap)), "/*");

        // Stop transformation
        Context stopTrans = new Context(contexts, StopTransServlet.CONTEXT_PATH, Context.SESSIONS);
        stopTrans.addServlet(new ServletHolder(new StopTransServlet(transformationMap)), "/*");
        
        // Add trans
        Context addTrans = new Context(contexts, AddTransServlet.CONTEXT_PATH, Context.SESSIONS);
        addTrans.addServlet(new ServletHolder(new AddTransServlet(transformationMap)), "/*");

        // Step port reservation
        Context getPort = new Context(contexts, GetStepSocketServlet.CONTEXT_PATH, Context.SESSIONS);
        getPort.addServlet(new ServletHolder(new GetStepSocketServlet(transformationMap)), "/*");

        server.setHandlers(new Handler[] { securityHandler, contexts });

        // Start execution
        createListeners();
        
        server.start();
        server.join();
    }

    public void stopServer() {
      try {
        if (server != null) {
          server.stop();
        }
      } catch (Exception e) {
        log.logError("web server error", "failed to stop webserver", e);
      }
    }
    
    private void createListeners() 
    {
        SocketConnector connector = new SocketConnector();
        connector.setPort(port);
        connector.setHost(hostname);
        connector.setName("Kettle HTTP listener for ["+hostname+"]");
        log.logBasic(toString(), "Created listener for webserver @ address : " + hostname+":"+port);

        server.setConnectors( new Connector[] { connector });
    }

    /**
     * @return the hostname
     */
    public String getHostname()
    {
        return hostname;
    }

    /**
     * @param hostname the hostname to set
     */
    public void setHostname(String hostname)
    {
        this.hostname = hostname;
    }

}

