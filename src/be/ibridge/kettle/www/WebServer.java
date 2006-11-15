package be.ibridge.kettle.www;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

import org.mortbay.http.HashUserRealm;
import org.mortbay.http.HttpContext;
import org.mortbay.http.HttpListener;
import org.mortbay.http.SecurityConstraint;
import org.mortbay.http.SocketListener;
import org.mortbay.http.handler.SecurityHandler;
import org.mortbay.jetty.Server;
import org.mortbay.util.InetAddrPort;

import be.ibridge.kettle.core.LogWriter;

public class WebServer
{
    private static LogWriter log = LogWriter.getInstance();
    public  static final int PORT = 80;

    private Server             server;
    private SecurityConstraint securityConstraint;
    private HashUserRealm      userRealm;
    
    private TransformationMap  transformationMap;

    public WebServer(TransformationMap transformationMap) throws Exception
    {
        this.transformationMap = transformationMap;
        this.securityConstraint = new SecurityConstraint();
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
        createListeners();

        addRootContext();
        addStatusContext();
        addTransStatusContext();
        addStartTransContext();
        addStopTransContext();
        
        server.start();
    }

    private void addRootContext()
    {
        HttpContext rootContext = new HttpContext();
        rootContext.setContextPath("/");
        rootContext.addHandler(new RootHandler());
        rootContext.setResourceBase("./www/");
        server.addContext(rootContext);
    }

    private void addStatusContext()
    {
        HttpContext getStatusContext = createContext("/kettle/status");
        getStatusContext.addHandler(new GetStatusHandler(transformationMap));
        server.addContext(getStatusContext);
    }

    private void addTransStatusContext()
    {
        HttpContext getTransStatusContext = createContext("/kettle/transStatus");
        getTransStatusContext.addHandler(new GetTransStatusHandler(transformationMap));
        server.addContext(getTransStatusContext);
    }
    
    private void addStartTransContext()
    {
        HttpContext startTransContext = createContext("/kettle/startTrans");
        startTransContext.addHandler(new StartTransHandler(transformationMap));
        server.addContext(startTransContext);
    }
    
    private void addStopTransContext()
    {
        HttpContext stopTransContext = createContext("/kettle/stopTrans");
        stopTransContext.addHandler(new StopTransHandler(transformationMap));
        server.addContext(stopTransContext);
    }

    private HttpContext createContext(final String contextPath)
    {
        final HttpContext httpContext = new HttpContext(server, contextPath);
        httpContext.setRealm(userRealm);
        httpContext.addHandler(new SecurityHandler());
        addSecurityConstraint(httpContext);
        return httpContext;
    }

    private void addSecurityConstraint(final HttpContext httpContext)
    {
        securityConstraint.setAuthenticate(true);
        securityConstraint.addRole(SecurityConstraint.ANY_ROLE);
        httpContext.addSecurityConstraint("*", securityConstraint);
    }
    
    private void createListeners() throws UnknownHostException {
        try {
            Enumeration e = NetworkInterface.getNetworkInterfaces();
            while (e.hasMoreElements()) 
            {
                NetworkInterface nwi = (NetworkInterface) e.nextElement();
                String nwiName = nwi.getDisplayName();

                Enumeration ip = nwi.getInetAddresses();
                while (ip.hasMoreElements())
                {
                    InetAddress inetAddress = (InetAddress) ip.nextElement();
                    server.addListener(createListener(nwiName, inetAddress));
                }
            }
        } 
        catch (SocketException e) 
        {
            throw new RuntimeException("Unable to determine IP address of ppp0 network interface", e);
        }
    }
    
    private HttpListener createListener(String nwiName, InetAddress in) throws UnknownHostException {
        SocketListener httpListener = new SocketListener(new InetAddrPort(in, PORT));
        httpListener.setMinThreads(2);
        httpListener.setMaxThreads(5);
        httpListener.setMaxIdleTimeMs(0);
        httpListener.setName("Kettle HTTP listeners");
        log.logBasic(toString(), "Created listener for webserver @ address : " + in + " on " + nwiName);

        return httpListener;
    }

}
