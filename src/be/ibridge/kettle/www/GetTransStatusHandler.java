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
import be.ibridge.kettle.trans.step.BaseStep;
import be.ibridge.kettle.trans.step.StepDataInterface;

public class GetTransStatusHandler extends AbstractHandler
{
    private static final long serialVersionUID = 3634806745372015720L;
    public static final String CONTEXT_PATH = "/kettle/transStatus";
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

        out.println("<HTML>");
        out.println("<HEAD><TITLE>Kettle transformation status</TITLE></HEAD>");
        out.println("<BODY>");
        out.println("<H1>Transformation status</H1>");
        
        String transName = request.getParameter("name");

        try
        {
            out.println("<table border=\"1\">");
            out.print("<tr> <th>Transformation name</th> <th>Status</th> </tr>");
            Trans  trans  = transformationMap.getTransformation(transName);
            String status = trans.getStatus();

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
                    // Proc: nr of lines processed: input + output!
                    long in_proc = baseStep.linesInput + baseStep.linesRead;
                    long out_proc = baseStep.linesOutput + baseStep.linesWritten + baseStep.linesUpdated;

                    float lapsed = ((float) baseStep.getRuntime()) / 1000;
                    double in_speed = 0;
                    double out_speed = 0;

                    if (lapsed != 0)
                    {
                        in_speed = Math.floor(10 * (in_proc / lapsed)) / 10;
                        out_speed = Math.floor(10 * (out_proc / lapsed)) / 10;
                    }

                    String stepname = baseStep.getStepname();
                    int copy = baseStep.getCopy();
                    long linesRead = baseStep.getLinesRead(); //$NON-NLS-1$
                    long linesWritten = baseStep.getLinesWritten(); //$NON-NLS-1$
                    long linesInput = baseStep.getLinesInput(); //$NON-NLS-1$
                    long linesOutput = baseStep.getLinesOutput(); //$NON-NLS-1$
                    long linesUpdated = baseStep.getLinesUpdated(); //$NON-NLS-1$
                    long errors = baseStep.getErrors(); //$NON-NLS-1$
                    String statusDescription = baseStep.getStatusDescription(); //$NON-NLS-1$
                    double seconds = Math.floor((lapsed * 10) + 0.5) / 10; //$NON-NLS-1$
                    String speed = lapsed == 0 ? "-" : "" + (in_speed > out_speed ? in_speed : out_speed); //$NON-NLS-1$ //$NON-NLS-2$
                    String priority = baseStep.isAlive() ? "" + baseStep.getPriority() + "/" + baseStep.rowsetInputSize() + "/" + baseStep.rowsetOutputSize() : "-"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                    String sleeps = "" + baseStep.getNrGetSleeps() + "/" + baseStep.getNrPutSleeps();
                    
                    out.print("<tr> <th>"+stepname+"</th> <th>"+copy+"</th> <th>"+linesRead+"</th> <th>"+linesWritten+"</th> <th>"+linesInput+"</th> <th>"+linesOutput+"</th> <th>"+linesUpdated+"</th> " +
                    "<th>"+errors+"</th> <th>"+statusDescription+"</th> <th>"+seconds+"</th> <th>"+speed+"</th> <th>"+priority+"</th> <th>"+sleeps+"</th> </tr>");
                }
            }
            out.println("</table>");
            out.println("<p>");
            
            out.print("<a href=\"/kettle/transStatus?name="+transName+"\">Refresh</a>");
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
    }

    public String toString()
    {
        return "Trans Status Handler";
    }
}
