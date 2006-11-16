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

public class StopTransHandler extends AbstractHandler
{
    private static final long serialVersionUID = 3634806745372015720L;
    public static final String CONTEXT_PATH = "/kettle/stopTrans";
    private static LogWriter log = LogWriter.getInstance();
    private TransformationMap transformationMap;
    
    public StopTransHandler(TransformationMap transformationMap)
    {
        this.transformationMap = transformationMap;
    }

    public void handle(String target, HttpServletRequest request, HttpServletResponse response, int dispatch) throws IOException, ServletException
    {
        if (!request.getContextPath().equals(CONTEXT_PATH)) return;
        if (!isStarted()) return;

        if (log.isDebug()) log.logDebug(toString(), "Stop of transformation requested");

        response.setContentType("text/html");

        OutputStream os = response.getOutputStream();
        PrintStream out = new PrintStream(os);

        String transName = request.getParameter("name");

        out.println("<HTML>");
        out.println("<HEAD><TITLE>Stop transformation</TITLE></HEAD>");
        out.println("<BODY>");

        try
        {
            Trans trans = transformationMap.getTransformation(transName);

            if (trans!=null)
            {
                trans.stopAll();
                
                out.println("<H1>Transformation '"+transName+"' stop requested.</H1>");
                out.println("<a href=\"/kettle/transStatus?name="+transName+"\">Back to the transformation status page</a><p>");
            }
            else
            {
                out.println("<H1>Transformation '"+transName+"' could not be found.</H1>");
                out.println("<a href=\"/kettle/status\">Back to the status page</a><p>");
            }
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
        return "Stop transformation";
    }
}
