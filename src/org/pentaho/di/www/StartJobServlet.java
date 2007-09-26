package org.pentaho.di.www;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.logging.Log4jStringAppender;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.job.Job;
import org.pentaho.di.trans.StepLoader;


public class StartJobServlet extends HttpServlet
{
	private static final long serialVersionUID = -8487225953910464032L;

	public static final String CONTEXT_PATH = "/kettle/startJob";
    private static LogWriter log = LogWriter.getInstance();
    private JobMap jobMap;
    
    public StartJobServlet(JobMap jobMap)
    {
        this.jobMap = jobMap;
    }
    
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        doGet(request, response);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        if (!request.getContextPath().equals(CONTEXT_PATH)) return;
        
        if (log.isDebug()) log.logDebug(toString(), "Start of job requested");

        String jobName = request.getParameter("name");
        boolean useXML = "Y".equalsIgnoreCase( request.getParameter("xml") );

        response.setStatus(HttpServletResponse.SC_OK);
        
        PrintWriter out = response.getWriter();
        if (useXML)
        {
            response.setContentType("text/xml");
            response.setCharacterEncoding(Const.XML_ENCODING);
            out.print(XMLHandler.getXMLHeader(Const.XML_ENCODING));
        }
        else
        {
            response.setContentType("text/html");
            out.println("<HTML>");
            out.println("<HEAD>");
            out.println("<TITLE>Start job</TITLE>");
            out.println("<META http-equiv=\"Refresh\" content=\"2;url=/kettle/jobStatus?name="+jobName+"\">");
            out.println("</HEAD>");
            out.println("<BODY>");
        }
    
        try
        {
            Job job = jobMap.getJob(jobName);
            if (job!=null)
            {
            	// First see if this job already ran to completion.
            	// If so, we get an exception is we try to start() the job thread
            	//
            	if (job.isInitialized() && !job.isActive())
            	{
            		// Re-create the job from the jobMeta
            		//
            		job = new Job(LogWriter.getInstance(), StepLoader.getInstance(), null, job.getJobMeta());
            	}
            	
                // Log to a String & save appender for re-use later.
                Log4jStringAppender appender = LogWriter.createStringAppender();
                log.addAppender(appender);
                jobMap.addAppender(jobName, appender);
                
                job.start(); // runs the thread in the background...

                String message = "Transformation '"+jobName+"' was started.";
                if (useXML)
                {
                    out.println(new WebResult(WebResult.STRING_OK, message).getXML());
                }
                else
                {
                    
                    out.println("<H1>"+message+"</H1>");
                    out.println("<a href=\"/kettle/jobStatus?name="+jobName+"\">Back to the job status page</a><p>");
                }
            }
            else
            {
                String message = "The specified job ["+jobName+"] could not be found";
                if (useXML)
                {
                    out.println(new WebResult(WebResult.STRING_ERROR, message));
                }
                else
                {
                    out.println("<H1>"+message+"</H1>");
                    out.println("<a href=\"/kettle/status\">Back to the status page</a><p>");
                }
            }
        }
        catch (Exception ex)
        {
            if (useXML)
            {
                out.println(new WebResult(WebResult.STRING_ERROR, "Unexpected error during job start:"+Const.CR+Const.getStackTracker(ex)));
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
    }

    public String toString()
    {
        return "Start job";
    }
}
