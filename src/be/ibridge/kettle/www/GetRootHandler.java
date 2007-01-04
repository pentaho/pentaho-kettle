package be.ibridge.kettle.www;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.jetty.HttpConnection;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.handler.AbstractHandler;

import be.ibridge.kettle.core.LogWriter;

public class GetRootHandler extends AbstractHandler
{
    private static final long serialVersionUID = 3634806745372015720L;
    public static final String CONTEXT_PATH = "/";
    
    private static LogWriter log = LogWriter.getInstance();
    
    public GetRootHandler()
    {
    }

    public void handle(String target, HttpServletRequest request, HttpServletResponse response, int dispatch) throws IOException, ServletException
    {
        if (!request.getPathInfo().equals(CONTEXT_PATH)) return;
        if (!isStarted()) return;

        if (log.isDebug()) log.logDebug(toString(), "Root requested");

        response.setContentType("text/html");
        response.setStatus(HttpServletResponse.SC_OK);
        
        PrintWriter out = response.getWriter();
        
        out.println("<HTML>");
        out.println("<HEAD><TITLE>Kettle slave server</TITLE></HEAD>");
        out.println("<BODY>");
        out.println("<H2>Slave server menu</H2>");

        out.println("<p>");
        out.println("<a href=\"/kettle/status\">Show status</a><br>");

        out.println("<p>");
        out.println("</BODY>");
        out.println("</HTML>");
        
        response.flushBuffer();
        
        Request baseRequest = (request instanceof Request) ? (Request)request:HttpConnection.getCurrentConnection().getRequest();
        baseRequest.setHandled(true);
    }

    public String toString()
    {
        return "Root Handler";
    }
}
