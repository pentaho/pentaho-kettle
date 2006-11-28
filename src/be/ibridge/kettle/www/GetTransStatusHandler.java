package be.ibridge.kettle.www;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.jetty.handler.AbstractHandler;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.XMLHandler;
import be.ibridge.kettle.core.logging.Log4jStringAppender;
import be.ibridge.kettle.trans.Trans;
import be.ibridge.kettle.trans.step.BaseStep;
import be.ibridge.kettle.trans.step.StepDataInterface;
import be.ibridge.kettle.trans.step.StepStatus;

public class GetTransStatusHandler extends AbstractHandler
{
    private static final long serialVersionUID = 3634806745372015720L;
    public static final String CONTEXT_PATH = "/kettle/transStatus";
    
    private static final String XML_TAG = "transstatus";
    
    private static LogWriter log = LogWriter.getInstance();
    private TransformationMap transformationMap;
    
    public GetTransStatusHandler(TransformationMap transformationMap)
    {
        this.transformationMap = transformationMap;
    }

    public void handle(String target, HttpServletRequest request, HttpServletResponse response, int dispatch) throws IOException, ServletException
    {
        if (!request.getContextPath().equals(CONTEXT_PATH)) return;
        if (!isStarted()) return;

        if (log.isDebug()) log.logDebug(toString(), "Transformation status requested");

        response.setContentType("text/html");

        OutputStream os = response.getOutputStream();
        PrintStream out = new PrintStream(os);

        String transName = request.getParameter("name");
        boolean useXML = "Y".equalsIgnoreCase( request.getParameter("xml") );
        
        if (useXML)
        {
            response.setContentType("text/xml");
            response.setCharacterEncoding(Const.XML_ENCODING);
            out.print(XMLHandler.getXMLHeader(Const.XML_ENCODING));
        }
        
        Trans  trans  = transformationMap.getTransformation(transName);
        
        if (trans!=null)
        {
            String status = trans.getStatus();
    
            if (useXML)
            {
                out.println("<"+XML_TAG+">");
    
                out.print(XMLHandler.addTagValue("trans", transName, false));                
                out.print(XMLHandler.addTagValue("status", status));                
    
                out.println("<stepstatuses>");
                for (int i = 0; i < trans.nrSteps(); i++)
                {
                    BaseStep baseStep = trans.getRunThread(i);
                    if ( (baseStep.isAlive()) || baseStep.getStatus()!=StepDataInterface.STATUS_EMPTY)
                    {
                        StepStatus stepStatus = new StepStatus(baseStep);
                        out.print(stepStatus.getXML());
                    }
                }
                out.println("</stepstatuses>");
    
                out.println("</"+XML_TAG+">");
            }
            else
            {
                out.println("<HTML>");
                out.println("<HEAD><TITLE>Kettle transformation status</TITLE></HEAD>");
                out.println("<BODY>");
                out.println("<H1>Transformation status</H1>");
                
        
                try
                {
                    out.println("<table border=\"1\">");
                    out.print("<tr> <th>Transformation name</th> <th>Status</th> </tr>");
        
                    out.print("<tr>");
                    out.print("<td>"+transName+"</td>");
                    out.print("<td>"+status+"</td>");
                    out.print("</tr>");
                    out.print("</table>");
                    
                    out.print("<p>");
                    
                    if ( (trans.isFinished() && trans.isRunning()) || ( !trans.isRunning() && !trans.isPreparing() && !trans.isInitializing() ))
                    {
                        out.print("<a href=\"/kettle/startTrans?name="+transName+"\">Start this transformation</a>");
                        out.print("<p>");
                        out.print("<a href=\"/kettle/prepareExec?name="+transName+"\">Prepare the execution</a><br>");
                        out.print("<a href=\"/kettle/startExec?name="+transName+"\">Start the execution</a><p>");
                    }
                    else
                    if (trans.isRunning())
                    {
                        out.print("<a href=\"/kettle/stopTrans?name="+transName+"\">Stop this transformation</a>");
                        out.print("<p>");
                    }
                    
                    out.println("<table border=\"1\">");
                    out.print("<tr> <th>Step name</th> <th>Copy Nr</th> <th>Read</th> <th>Written</th> <th>Input</th> <th>Output</th> <th>Updated</th> " +
                            "<th>Errors</th> <th>Active</th> <th>Time</th> <th>Speed</th> <th>pr/in/out</th> <th>Sleeps</th> </tr>");
        
                    for (int i = 0; i < trans.nrSteps(); i++)
                    {
                        BaseStep baseStep = trans.getRunThread(i);
                        if ( (baseStep.isAlive()) || baseStep.getStatus()!=StepDataInterface.STATUS_EMPTY)
                        {
                            StepStatus stepStatus = new StepStatus(baseStep);
                            out.print(stepStatus.getHTMLTableRow());
                        }
                    }
                    out.println("</table>");
                    out.println("<p>");
                    
                    out.print("<a href=\"/kettle/transStatus?name="+transName+"&xml=y\">show as XML</a><p>");
                    out.print("<a href=\"/kettle/transStatus?name="+transName+"\">Refresh</a>");
                    
                    
                    // Put the logging below that.
                    Log4jStringAppender appender = (Log4jStringAppender) transformationMap.getAppender(transName);
                    if (appender!=null)
                    {
                        out.println("<p>");
                        out.println("<pre>");
                        out.println(appender.getBuffer().toString());
                        out.println("</pre>");
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

        out.flush();
    }

    public String toString()
    {
        return "Trans Status Handler";
    }
}
