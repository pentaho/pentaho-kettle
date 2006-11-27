package be.ibridge.kettle.www;

import interbase.interclient.UnknownHostException;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.bio.SocketConnector;
import org.mortbay.jetty.handler.ContextHandler;
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

    private int port;

    public WebServer(TransformationMap transformationMap, int port) throws Exception
    {
        this.transformationMap = transformationMap;
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
        constraint.setRoles(new String[] { Constraint.ANY_ROLE });
        constraint.setAuthenticate(true);
        
        ConstraintMapping constraintMapping = new ConstraintMapping();
        constraintMapping.setConstraint(constraint);
        constraintMapping.setPathSpec("/*");

        SecurityHandler securityHandler = new SecurityHandler();
        securityHandler.setUserRealm(userRealm);
        securityHandler.setConstraintMappings(new ConstraintMapping[]{constraintMapping});
        

        ServletHandler servletHandler = new ServletHandler();
        servletHandler.addServletWithMapping(new ServletHolder(new AddTransServlet(transformationMap)), AddTransServlet.CONTEXT_PATH);
        server.setHandlers(
                new Handler[] 
                { 
                        getRootContext(securityHandler),
                        getStatusContext(securityHandler),
                        getTransStatusContext(securityHandler),
                        getStartTransContext(securityHandler),
                        getPrepareExecutionTransContext(securityHandler),
                        getStartExecutionTransContext(securityHandler),
                        getStopTransContext(securityHandler),
                        getUploadTransPageContext(securityHandler),
                        getUploadTransContext(securityHandler),
                        
                        getAddTransContext(securityHandler), 
                }
        );
        
        createListeners();
        
        server.start();
        server.join();
    }

    private Handler getRootContext(SecurityHandler securityHandler)
    {
        ContextHandler handler = createContext(GetRootHandler.CONTEXT_PATH, securityHandler);
        handler.setContextPath("/");
        handler.addHandler(new GetRootHandler());
        return handler;
    }

    private Handler getStatusContext(SecurityHandler securityHandler)
    {
        ContextHandler handler = createContext(GetStatusHandler.CONTEXT_PATH, securityHandler);
        handler.addHandler(new GetStatusHandler(transformationMap));
        return handler;
    }

    private Handler getTransStatusContext(SecurityHandler securityHandler)
    {
        ContextHandler handler = createContext(GetTransStatusHandler.CONTEXT_PATH, securityHandler);
        handler.addHandler(new GetTransStatusHandler(transformationMap));
        return handler;
    }
    
    private Handler getStartTransContext(SecurityHandler securityHandler)
    {
        ContextHandler handler = createContext(StartTransHandler.CONTEXT_PATH, securityHandler);
        handler.addHandler(new StartTransHandler(transformationMap));
        return handler;
    }

    private Handler getPrepareExecutionTransContext(SecurityHandler securityHandler)
    {
        ContextHandler handler = createContext(PrepareExecutionTransHandler.CONTEXT_PATH, securityHandler);
        handler.addHandler(new PrepareExecutionTransHandler(transformationMap));
        return handler;
    }

    private Handler getStartExecutionTransContext(SecurityHandler securityHandler)
    {
        ContextHandler handler = createContext(StartExecutionTransHandler.CONTEXT_PATH, securityHandler);
        handler.addHandler(new StartExecutionTransHandler(transformationMap));
        return handler;
    }

    private Handler getStopTransContext(SecurityHandler securityHandler)
    {
        ContextHandler handler = createContext(StopTransHandler.CONTEXT_PATH, securityHandler);
        handler.addHandler(new StopTransHandler(transformationMap));
        return handler;
    }
    
    private Handler getAddTransContext(SecurityHandler securityHandler)
    {
        ServletHandler handler=new ServletHandler();
        handler.addServletWithMapping(new ServletHolder(new AddTransServlet(transformationMap)), AddTransServlet.CONTEXT_PATH);
        return handler;
    }
    
    private Handler getUploadTransPageContext(SecurityHandler securityHandler) throws Exception
    {
        ContextHandler handler = createContext(UploadTransPageHandler.CONTEXT_PATH, securityHandler);
        handler.addHandler(new UploadTransPageHandler(UploadTransHandler.CONTEXT_PATH));
        return handler;
    }
        
    private Handler getUploadTransContext(SecurityHandler securityHandler) throws Exception
    {
        ContextHandler handler = createContext(UploadTransHandler.CONTEXT_PATH, securityHandler);
        handler.addHandler(new UploadTransHandler(transformationMap));
        return handler;
    }


    private ContextHandler createContext(final String contextPath, SecurityHandler securityHandler)
    {
        ContextHandler contextHandler = new ContextHandler(server, contextPath);
        // contextHandler.addHandler(securityHandler);
        return contextHandler;
    }
    
    private void createListeners() throws UnknownHostException 
    {
        try 
        {
            List connectors = new ArrayList();
            
            Enumeration e = NetworkInterface.getNetworkInterfaces();
            while (e.hasMoreElements()) 
            {
                NetworkInterface nwi = (NetworkInterface) e.nextElement();
                String nwiName = nwi.getDisplayName();

                Enumeration ip = nwi.getInetAddresses();
                while (ip.hasMoreElements())
                {
                    InetAddress inetAddress = (InetAddress) ip.nextElement();
            
                    SocketConnector connector = new SocketConnector();
                    connector.setPort(port);
                    connector.setHost(inetAddress.getHostAddress());
                    connector.setName("Kettle HTTP listener for ["+inetAddress.getHostAddress()+"]");
                    log.logBasic(toString(), "Created listener for webserver @ address : " + inetAddress.getHostAddress() + " on " + nwiName);

                    connectors.add(connector);
                }
            }
            
            server.setConnectors( (Connector[])connectors.toArray(new Connector[connectors.size()]) );
        } 
        catch (SocketException e) 
        {
            throw new RuntimeException("Unable to determine IP address of network interface", e);
        }
    }

}

