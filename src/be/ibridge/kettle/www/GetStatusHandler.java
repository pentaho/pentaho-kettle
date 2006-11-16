package be.ibridge.kettle.www;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.jetty.handler.AbstractHandler;

import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.trans.Trans;

public class GetStatusHandler extends AbstractHandler
{
    private static final long serialVersionUID = 3634806745372015720L;
    public static final String CONTEXT_PATH = "/kettle/status";
    private static LogWriter log = LogWriter.getInstance();
    private TransformationMap transformationMap;
    
    public GetStatusHandler(TransformationMap transformationMap)
    {
        this.transformationMap = transformationMap;
    }

    public void handle(String target, HttpServletRequest request, HttpServletResponse response, int dispatch) throws IOException, ServletException
    {
        if (!request.getContextPath().equals(CONTEXT_PATH)) return;
        if (!isStarted()) return;

        if (log.isDebug()) log.logDebug(toString(), "Status requested");

        response.setContentType("text/html");

        OutputStream os = response.getOutputStream();
        PrintStream out = new PrintStream(os);

        out.println("<HTML>");
        out.println("<HEAD><TITLE>Kettle slave server status</TITLE></HEAD>");
        out.println("<BODY>");
        out.println("<H1>Status</H1>");

        out.println("<table border=\"1\">");
        out.print("<tr> <th>Transformation name</th> <th>Status</th> </tr>");

        try
        {
            String[] transNames = transformationMap.getTransformationNames();
            for (int i=0;i<transNames.length;i++)
            {
                String name   = transNames[i]; 
                Trans  trans  = transformationMap.getTransformation(name);
                String status = trans.getStatus();
                
                out.print("<tr>");
                out.print("<td><a href=\"/kettle/transStatus?name="+name+"\">"+name+"</a></td>");
                out.print("<td>"+status+"</td>");
                out.print("</tr>");
            }
            out.print("</table>");
        }
        catch (Exception ex)
        {
            out.println("<p>");
            out.println("<pre>");
            ex.printStackTrace(out);
            out.println("</pre>");
        }

        out.println("<p>");
        out.println("</BODY>");
        out.println("</HTML>");

        out.flush();

        // Request baseRequest = (request instanceof Request) ? (Request)request:HttpConnection.getCurrentConnection().getRequest();
        // baseRequest.setHandled(true);
    }

    public String toString()
    {
        return "Status Handler";
    }
}
