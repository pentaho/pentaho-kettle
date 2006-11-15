package be.ibridge.kettle.www;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import org.mortbay.http.HttpException;
import org.mortbay.http.HttpRequest;
import org.mortbay.http.HttpResponse;
import org.mortbay.http.handler.AbstractHttpHandler;

import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.trans.Trans;

public class GetStatusHandler extends AbstractHttpHandler
{
    private static final long serialVersionUID = 3634806745372015720L;
    private static LogWriter log = LogWriter.getInstance();
    private TransformationMap transformationMap;
    
    public GetStatusHandler(TransformationMap transformationMap)
    {
        this.transformationMap = transformationMap;
    }

    public void handle(String pathInContext, String pathParams, HttpRequest request, HttpResponse response) throws HttpException, IOException
    {
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

        request.setHandled(true);
    }

    public String toString()
    {
        return "Status Handler";
    }
}
