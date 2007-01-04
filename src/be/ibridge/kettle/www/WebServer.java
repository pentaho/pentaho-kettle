package be.ibridge.kettle.www;

import interbase.interclient.UnknownHostException;

import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.bio.SocketConnector;
import org.mortbay.jetty.handler.ContextHandler;
import org.mortbay.jetty.handler.ContextHandlerCollection;
import org.mortbay.jetty.security.Constraint;
import org.mortbay.jetty.security.ConstraintMapping;
import org.mortbay.jetty.security.HashUserRealm;
import org.mortbay.jetty.security.SecurityHandler;
import org.mortbay.jetty.servlet.Context;
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
        
        ContextHandlerCollection contexts = new ContextHandlerCollection();
        server.setHandler(contexts);
        new ContextHandler(contexts, "/*").addHandler(securityHandler);
        new Context(contexts, AddTransServlet.CONTEXT_PATH, Context.SESSIONS).addServlet(new ServletHolder(new AddTransServlet(transformationMap)), "/*");
        new ContextHandler(contexts, GetRootHandler.CONTEXT_PATH).addHandler(new GetRootHandler());
        new ContextHandler(contexts, GetStatusHandler.CONTEXT_PATH).addHandler(new GetStatusHandler(transformationMap));
        new ContextHandler(contexts, GetTransStatusHandler.CONTEXT_PATH).addHandler(new GetTransStatusHandler(transformationMap));
        new ContextHandler(contexts, StartTransHandler.CONTEXT_PATH).addHandler(new StartTransHandler(transformationMap));
        new ContextHandler(contexts, StopTransHandler.CONTEXT_PATH).addHandler(new StopTransHandler(transformationMap));
        new ContextHandler(contexts, PrepareExecutionTransHandler.CONTEXT_PATH).addHandler(new PrepareExecutionTransHandler(transformationMap));
        new ContextHandler(contexts, StartExecutionTransHandler.CONTEXT_PATH).addHandler(new StartExecutionTransHandler(transformationMap));

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

