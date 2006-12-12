package be.ibridge.kettle.www;

import java.io.IOException;
import java.io.PrintStream;
import java.util.zip.GZIPOutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.jetty.HttpConnection;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.handler.AbstractHandler;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.KettleVariables;
import be.ibridge.kettle.core.LocalVariables;
import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.XMLHandler;
import be.ibridge.kettle.core.logging.Log4jStringAppender;
import be.ibridge.kettle.trans.Trans;
import be.ibridge.kettle.trans.TransConfiguration;
import be.ibridge.kettle.trans.TransExecutionConfiguration;

public class PrepareExecutionTransHandler extends AbstractHandler
{
    private static final long serialVersionUID = 3634806745372015720L;
    public static final String CONTEXT_PATH = "/kettle/prepareExec";
    private static LogWriter log = LogWriter.getInstance();
    private TransformationMap transformationMap;
    
    public PrepareExecutionTransHandler(TransformationMap transformationMap)
    {
        this.transformationMap = transformationMap;
    }

    public void handle(String target, HttpServletRequest request, HttpServletResponse response, int dispatch) throws IOException, ServletException
    {
        if (!request.getContextPath().equals(CONTEXT_PATH)) return;
        if (!isStarted()) return;

        if (log.isDebug()) log.logDebug(toString(), "Prepare execution of transformation requested");

        String transName = request.getParameter("name");
        boolean useXML = "Y".equalsIgnoreCase( request.getParameter("xml") );

        PrintStream out;
        if (useXML) out = new PrintStream(new GZIPOutputStream(response.getOutputStream()));
        else out = new PrintStream(response.getOutputStream());

        if (useXML)
        {
            response.setContentType("text/xml");
            out.print(XMLHandler.getXMLHeader(Const.XML_ENCODING));
        }
        else
        {
            response.setContentType("text/html");
            out.println("<HTML>");
            out.println("<HEAD><TITLE>Prepare execution of transformation</TITLE></HEAD>");
            out.println("<BODY>");
        }
    
        try
        {
            // Create a variables space to work in, separate from the other transformations running.
            KettleVariables kettleVariables = LocalVariables.getInstance().createKettleVariables(Thread.currentThread().getName(), transformationMap.getParentThreadName(), false);
            
            Trans trans = transformationMap.getTransformation(transName);
            TransConfiguration transConfiguration = transformationMap.getConfiguration(transName);
            if (trans!=null && transConfiguration!=null)
            {
                TransExecutionConfiguration executionConfiguration = transConfiguration.getTransExecutionConfiguration();
                // Set the appropriate logging, variables, arguments, replaydate, ...
                // etc.
                log.setLogLevel(executionConfiguration.getLogLevel());
                kettleVariables.setVariables(executionConfiguration.getVariables());
                trans.getTransMeta().setArguments(executionConfiguration.getArgumentStrings());
                trans.setReplayDate(executionConfiguration.getReplayDate());
                trans.setSafeModeEnabled(executionConfiguration.isSafeModeEnabled());
                
                // Log to a String
                Log4jStringAppender appender = LogWriter.createStringAppender();
                log.addAppender(appender);
                transformationMap.addAppender(transName, appender);
                
                if (trans.prepareExecution(null))
                {
                    if (useXML)
                    {
                        out.println(WebResult.OK.getXML());
                    }
                    else
                    {
                        out.println("<H1>Transformation '"+transName+"' was started.</H1>");
                        out.println("<a href=\"/kettle/transStatus?name="+transName+"\">Back to the transformation status page</a><p>");
                    }
                }
                else
                {
                    if (useXML)
                    {
                        out.println(new WebResult(WebResult.STRING_ERROR, "Initialisation of transformation failed: "+Const.CR+appender.getBuffer().toString()));
                    }
                    else
                    {
                        out.println("<H1>Transformation '"+transName+"' was not initialised correctly.</H1>");
                        out.println("<pre>");
                        out.println(appender.getBuffer().toString());
                        out.println("</pre>");
                        out.println("<a href=\"/kettle/transStatus?name="+transName+"\">Back to the transformation status page</a><p>");
                    }
                }
            }
            else
            {
                if (useXML)
                {
                    out.println(new WebResult(WebResult.STRING_ERROR, "The specified transformation ["+transName+"] could not be found"));
                }
                else
                {
                    out.println("<H1>Transformation '"+transName+"' could not be found.</H1>");
                    out.println("<a href=\"/kettle/status\">Back to the status page</a><p>");
                }
            }
        }
        catch (Exception ex)
        {
            if (useXML)
            {
                out.println(new WebResult(WebResult.STRING_ERROR, "Unexpected error during transformation execution preparation:"+Const.CR+Const.getStackTracker(ex)));
            }
            else
            {
                out.println("<p>");
                out.println("<pre>");
                ex.printStackTrace(out);
                out.println("</pre>");
            }
        }

        if (!useXML)
        {
            out.println("<p>");
            out.println("</BODY>");
            out.println("</HTML>");
        }

        out.flush();

        Request baseRequest = (request instanceof Request) ? (Request)request:HttpConnection.getCurrentConnection().getRequest();
        baseRequest.setHandled(true);
    }

    public String toString()
    {
        return "Start transformation";
    }
}
