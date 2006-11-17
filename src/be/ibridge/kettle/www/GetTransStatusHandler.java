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

        String name = request.getParameter("name");
        boolean useXML = "Y".equalsIgnoreCase( request.getParameter("xml") );
        Trans  trans  = transformationMap.getTransformation(name);
        String status = trans.getStatus();

        if (useXML)
        {
            response.setContentType("text/xml");
            response.setCharacterEncoding(Const.XML_ENCODING);
            
            out.print(XMLHandler.getXMLHeader(Const.XML_ENCODING));
            out.println("<"+XML_TAG+">");

            out.print(XMLHandler.addTagValue("trans", name, false));                
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
                out.print("<td>"+name+"</td>");
                out.print("<td>"+status+"</td>");
                out.print("</tr>");
                out.print("</table>");
                
                out.print("<p>");
                
                if ( (trans.isFinished() && trans.isRunning()) || ( !trans.isRunning() && !trans.isPreparing() && !trans.isInitializing() ))
                {
                    out.print("<a href=\"/kettle/startTrans?name="+name+"\">Start this transformation</a>");
                    out.print("<p>");
                }
                else
                if (trans.isRunning())
                {
                    out.print("<a href=\"/kettle/stopTrans?name="+name+"\">Stop this transformation</a>");
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
                
                out.print("<a href=\"/kettle/transStatus?name="+name+"\">Refresh</a>");
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

        out.flush();
    }

    public String toString()
    {
        return "Trans Status Handler";
    }
}
