package org.pentaho.di.www;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.pentaho.di.core.logging.LogWriter;


public class GetRootServlet extends HttpServlet
{
    private static final long serialVersionUID = 3634806745372015720L;
    public static final String CONTEXT_PATH = "/";
    
    private static LogWriter log = LogWriter.getInstance();
    
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        doGet(request, response);
    }
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        if (!request.getRequestURI().equals(CONTEXT_PATH)) return;
        
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
    }

    public String toString()
    {
        return "Root Handler";
    }
}
