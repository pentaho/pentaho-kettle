package be.ibridge.kettle.www;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.jetty.HttpConnection;
import org.mortbay.jetty.Request;

import be.ibridge.kettle.core.LogWriter;

public class GetRootHandler extends HttpServlet
{
    private static final long serialVersionUID = 3634806745372015720L;
    public static final String CONTEXT_PATH = "/";
    
    private static LogWriter log = LogWriter.getInstance();
    
    public GetRootHandler()
    {
    }
    
    public void init(ServletConfig servletConfig) throws ServletException
    {
        super.init(servletConfig);
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        doPost(request, response);
    }
    
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        if (!request.getServletPath().equals(CONTEXT_PATH)) return;

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
