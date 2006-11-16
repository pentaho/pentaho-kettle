package be.ibridge.kettle.www;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.jetty.HttpConnection;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.handler.AbstractHandler;

import be.ibridge.kettle.core.LogWriter;

public class UploadTransPageHandler extends AbstractHandler
{
    private static final long serialVersionUID = -6562468540402269688L;

    public static final String CONTEXT_PATH = "/kettle/uploadTransPage";

    private LogWriter         log              = LogWriter.getInstance();

    private final String      redirectPath;

    protected UploadTransPageHandler(String redirectPath)
    {
        this.redirectPath = redirectPath;
    }

    public void handle(String target, HttpServletRequest request, HttpServletResponse response, int dispatch) throws IOException, ServletException
    {
        if (!request.getContextPath().equals(CONTEXT_PATH)) return;
        if (!isStarted()) { return; }
        if (log.isDebug())
        {
            log.logDebug(toString(), "Upload of Kettle transformation requested");
        }

        ByteArrayOutputStream buf = new ByteArrayOutputStream(4096);
        Writer writer = new OutputStreamWriter(buf);
        OutputStream out = response.getOutputStream();
        response.setContentType("text/html");
        writer.write("<html><title>Upload a Kettle transformation</title>");
        writer.write("<body>");
        writer.write("<h1>Upload a Kettle transformation</h1>");
        writer.write("<form enctype=\"multipart/form-data\" ");
        writer.write("method=\"POST\" ");
        String hostPort = request.getServerName();
        hostPort += ":";
        hostPort += WebServer.PORT;
        writer.write("action=\"http://" + hostPort + redirectPath + "\">");
        writer.write("<table colspan=1 valign=\"top\" border=0><tr><td>");
        writer.write("<table border=0><tr><td align=\"right\">");
        writer.write("<b>file:</b></td><td><input name=\"file\" type=\"file\"></td></tr></table></td></tr>");
        writer.write("<tr><td><input value=\"Upload\" type=\"submit\"></td></tr></table></form>");
        writer.write("</body>");
        writer.write("</html>");
        writer.flush();
        buf.writeTo(out);

        Request baseRequest = (request instanceof Request) ? (Request)request:HttpConnection.getCurrentConnection().getRequest();
        baseRequest.setHandled(true);
   }
}
