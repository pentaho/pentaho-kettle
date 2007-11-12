/*
 * Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
*/
package org.pentaho.di.www;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.zip.GZIPOutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.logging.Log4jStringAppender;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.job.Job;



public class GetJobStatusServlet extends HttpServlet
{
    private static final long serialVersionUID = 3634806745372015720L;
    public static final String CONTEXT_PATH = "/kettle/jobStatus";
    
    private static LogWriter log = LogWriter.getInstance();
    private JobMap jobMap;
    
    public GetJobStatusServlet(JobMap jobMap)
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
        
        if (log.isDebug()) log.logDebug(toString(), "Job status requested");

        String jobName = request.getParameter("name");
        boolean useXML = "Y".equalsIgnoreCase( request.getParameter("xml") );

        response.setStatus(HttpServletResponse.SC_OK);

        if (useXML)
        {
            response.setContentType("text/xml");
            response.setCharacterEncoding(Const.XML_ENCODING);
        }
        else
        {
            response.setContentType("text/html");
        }
        
        PrintWriter out = response.getWriter();

        Job job  = jobMap.getJob(jobName);
        if (job!=null)
        {
            String status = job.getStatus();
    
            if (useXML)
            {
                response.setContentType("text/xml");
                response.setCharacterEncoding(Const.XML_ENCODING);
                out.print(XMLHandler.getXMLHeader(Const.XML_ENCODING));
                
                SlaveServerJobStatus jobStatus = new SlaveServerJobStatus(jobName, status);
    
                Log4jStringAppender appender = (Log4jStringAppender) jobMap.getAppender(jobName);
                if (appender!=null)
                {
                    // The log can be quite large at times, we are going to put a base64 encoding around a compressed stream
                    // of bytes to handle this one.
                    
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    GZIPOutputStream gzos = new GZIPOutputStream(baos);
                    gzos.write( appender.getBuffer().toString().getBytes() );
                    gzos.close();
                    
                    String loggingString = new String(Base64.encodeBase64(baos.toByteArray()));
                    jobStatus.setLoggingString( loggingString );
                }
                
                // Also set the result object...
                //
                jobStatus.setResult( job.getResult() ); // might be null
                
                out.println(jobStatus.getXML());
            }
            else
            {
                response.setContentType("text/html");

                out.println("<HTML>");
                out.println("<HEAD>");
                out.println("<TITLE>Kettle job status</TITLE>");
                out.println("<META http-equiv=\"Refresh\" content=\"10;url=/kettle/jobStatus?name="+jobName+"\">");
                out.println("</HEAD>");
                out.println("<BODY>");
                out.println("<H1>Job status</H1>");
                
        
                try
                {
                    out.println("<table border=\"1\">");
                    out.print("<tr> <th>Job name</th> <th>Status</th> </tr>");
        
                    out.print("<tr>");
                    out.print("<td>"+jobName+"</td>");
                    out.print("<td>"+status+"</td>");
                    out.print("</tr>");
                    out.print("</table>");
                    
                    out.print("<p>");
                    
                    if (job.isActive())
                    {
                        out.print("<a href=\"/kettle/stopJob?name="+jobName+"\">Stop this job</a>");
                        out.print("<p>");
                    }
                    else
                    {
                        out.print("<a href=\"/kettle/startJob?name="+jobName+"\">Start this job</a>");
                        out.print("<p>");
                    }
                    
                    out.println("<p>");
                                        
                    out.print("<a href=\"/kettle/jobStatus/?name="+jobName+"&xml=y\">show as XML</a><br>");
                    out.print("<a href=\"/kettle/status\">Back to the status page</a><br>");
                    out.print("<p><a href=\"/kettle/jobStatus?name="+jobName+"\">Refresh</a>");
                    
                    // Put the logging below that.
                    Log4jStringAppender appender = (Log4jStringAppender) jobMap.getAppender(jobName);
                    if (appender!=null)
                    {
                        out.println("<p>");
                        /*
                        out.println("<pre>");
                        out.println(appender.getBuffer().toString());
                        out.println("</pre>");
                        */
                        out.println("<textarea id=\"joblog\" cols=\"120\" rows=\"20\" wrap=\"off\" name=\"Job log\" readonly=\"readonly\">"+appender.getBuffer().toString()+"</textarea>");
                        
                        out.println("<script type=\"text/javascript\"> ");
                        out.println("  joblog.scrollTop=joblog.scrollHeight; ");
                        out.println("</script> ");
                        out.println("<p>");
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
                out.println(new WebResult(WebResult.STRING_ERROR, "The specified job ["+jobName+"] could not be found"));
            }
            else
            {
                out.println("<H1>Job '"+jobName+"' could not be found.</H1>");
                out.println("<a href=\"/kettle/status\">Back to the status page</a><p>");
            }
        }
    }

    public String toString()
    {
        return "Job Status Handler";
    }
}
