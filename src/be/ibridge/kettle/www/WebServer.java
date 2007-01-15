package be.ibridge.kettle.www;

import interbase.interclient.UnknownHostException;

import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.bio.SocketConnector;
import org.mortbay.jetty.handler.ContextHandler;
import org.mortbay.jetty.handler.HandlerCollection;
import org.mortbay.jetty.security.Constraint;
import org.mortbay.jetty.security.ConstraintMapping;
import org.mortbay.jetty.security.HashUserRealm;
import org.mortbay.jetty.security.SecurityHandler;
import org.mortbay.jetty.servlet.ServletHandler;
import org.mortbay.jetty.servlet.ServletHolder;

import be.ibridge.kettle.core.LogWriter;

public class WebServer
{
    private static LogWriter log = LogWriter.getInstance();
    
    public  static final int PORT = 80;

    private Server             server;
    private HashUserRealm      userRealm;
    
    private TransformationMap  transformationMap;

    private String hostname;
    private int port;

    public WebServer(TransformationMap transformationMap, String hostname, int port) throws Exception
    {
        this.transformationMap = transformationMap;
        this.hostname = hostname;
        this.port = port;
        
        userRealm = new HashUserRealm("Kettle", "pwd/kettle.pwd");
        
        startServer();
    }

    public Server getServer()
    {
        return server;
    }

    public void startServer() throws Exception
    {
        server = new Server();

        server.addUserRealm(userRealm);

        Constraint constraint = new Constraint();
        constraint.setName(Constraint.__BASIC_AUTH);;
        constraint.setRoles( new String[] { Constraint.ANY_ROLE } );
        constraint.setAuthenticate(true);
        
        ConstraintMapping constraintMapping = new ConstraintMapping();
        constraintMapping.setConstraint(constraint);
        constraintMapping.setPathSpec("/*");

        SecurityHandler securityHandler = new SecurityHandler();
        securityHandler.setUserRealm(userRealm);
        securityHandler.setConstraintMappings(new ConstraintMapping[]{constraintMapping});
        
        HandlerCollection handlers = new HandlerCollection();

        // Add trans
        ServletHandler addTrans = new ServletHandler();
        addTrans.addServletWithMapping(new ServletHolder(new AddTransServlet(transformationMap)), AddTransServlet.CONTEXT_PATH);
        
        // Root
        ContextHandler rootHandler = new ContextHandler(GetRootHandler.CONTEXT_PATH);
        rootHandler.setHandler(new GetRootHandler());
        
        // Get status
        ContextHandler getStatus = new ContextHandler(GetStatusHandler.CONTEXT_PATH);
        getStatus.addHandler(new GetStatusHandler(transformationMap));
        
        // Get trans status
        ContextHandler getTransStatus = new ContextHandler(GetTransStatusHandler.CONTEXT_PATH);
        getTransStatus.addHandler(new GetTransStatusHandler(transformationMap));
        
        // Start transformation
        ContextHandler startTrans = new ContextHandler(StartTransHandler.CONTEXT_PATH);
        startTrans.addHandler(new StartTransHandler(transformationMap));
        
        // Stop transformation
        ContextHandler stopTrans = new ContextHandler(StopTransHandler.CONTEXT_PATH);
        stopTrans.addHandler(new StopTransHandler(transformationMap));
        
        // Prepare execution
        ContextHandler prepareExecution = new ContextHandler(PrepareExecutionTransHandler.CONTEXT_PATH);
        prepareExecution.addHandler(new PrepareExecutionTransHandler(transformationMap));
        
        // Start execution
        ContextHandler startExecution = new ContextHandler(StartExecutionTransHandler.CONTEXT_PATH);
        startExecution.addHandler(new StartExecutionTransHandler(transformationMap));
        
        // Set the handler collection
        // 
        handlers.setHandlers(
                new Handler[] 
                    { 
                        securityHandler,
                        rootHandler, 
                        getStatus, 
                        getTransStatus, 
                        startTrans, 
                        stopTrans, 
                        prepareExecution, 
                        startExecution, 
                     } 
                );
        
        HandlerCollection servlets = new HandlerCollection();
        servlets.setHandlers(new Handler[] { securityHandler, addTrans, });
        
        HandlerCollection allHandlers = new HandlerCollection();
        allHandlers.setHandlers(new Handler[] { handlers, servlets, });
        
        server.setHandler(allHandlers);       

        createListeners();
        
        server.start();
        server.join();
    }

    private void createListeners() throws UnknownHostException 
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

