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
import java.net.URLEncoder;
import java.util.zip.GZIPOutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.CentralLogStore;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.Job;

public class GetJobStatusServlet extends BaseHttpServlet implements CarteServletInterface {
  private static Class<?> PKG = GetJobStatusServlet.class; // for i18n purposes, needed by Translator2!! $NON-NLS-1$

  private static final long serialVersionUID = 3634806745372015720L;
  public static final String CONTEXT_PATH = "/kettle/jobStatus";

  public GetJobStatusServlet() {
  }

  public GetJobStatusServlet(JobMap jobMap) {
    super(jobMap);
  }

  protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    doGet(request, response);
  }

  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    if (isJettyMode() && !request.getContextPath().startsWith(CONTEXT_PATH)) {
      return;
    }

    if (log.isDebug())
      logDebug(BaseMessages.getString(PKG, "GetJobStatusServlet.Log.JobStatusRequested"));

    String jobName = request.getParameter("name");
    boolean useXML = "Y".equalsIgnoreCase(request.getParameter("xml"));
    int startLineNr = Const.toInt(request.getParameter("from"), 0);

    response.setStatus(HttpServletResponse.SC_OK);

    if (useXML) {
      response.setContentType("text/xml");
      response.setCharacterEncoding(Const.XML_ENCODING);
    } else {
      response.setContentType("text/html");
    }

    PrintWriter out = response.getWriter();

    Job job = getJobMap().getJob(jobName);
    if (job != null) {
      String status = job.getStatus();
      int lastLineNr = CentralLogStore.getLastBufferLineNr();
      String logText = CentralLogStore.getAppender().getBuffer(job.getLogChannel().getLogChannelId(), false, startLineNr, lastLineNr).toString();

      if (useXML) {
        response.setContentType("text/xml");
        response.setCharacterEncoding(Const.XML_ENCODING);
        out.print(XMLHandler.getXMLHeader(Const.XML_ENCODING));

        SlaveServerJobStatus jobStatus = new SlaveServerJobStatus(jobName, status);
        jobStatus.setFirstLoggingLineNr(startLineNr);
        jobStatus.setLastLoggingLineNr(lastLineNr);

        // The log can be quite large at times, we are going to put a base64 encoding around a compressed stream
        // of bytes to handle this one.

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        GZIPOutputStream gzos = new GZIPOutputStream(baos);
        gzos.write(logText.getBytes());
        gzos.close();

        String loggingString = new String(Base64.encodeBase64(baos.toByteArray()));
        jobStatus.setLoggingString(loggingString);

        // Also set the result object...
        //
        jobStatus.setResult(job.getResult()); // might be null

        try {
          out.println(jobStatus.getXML());
        } catch (KettleException e) {
          throw new ServletException("Unable to get the job status in XML format", e);
        }
      } else {
        response.setContentType("text/html");

        out.println("<HTML>");
        out.println("<HEAD>");
        out.println("<TITLE>" + BaseMessages.getString(PKG, "GetJobStatusServlet.KettleJobStatus") + "</TITLE>");
        out.println("<META http-equiv=\"Refresh\" content=\"10;url=" + convertContextPath(GetJobStatusServlet.CONTEXT_PATH) + "?name="
            + URLEncoder.encode(jobName, "UTF-8") + "\">");
        out.println("</HEAD>");
        out.println("<BODY>");
        out.println("<H1>" + BaseMessages.getString(PKG, "GetJobStatusServlet.JobStatus") + "</H1>");

        try {
          out.println("<table border=\"1\">");
          out.print("<tr> <th>" + BaseMessages.getString(PKG, "GetJobStatusServlet.Jobname") + "</th> <th>"
              + BaseMessages.getString(PKG, "TransStatusServlet.TransStatus") + "</th> </tr>");

          out.print("<tr>");
          out.print("<td>" + jobName + "</td>");
          out.print("<td>" + status + "</td>");
          out.print("</tr>");
          out.print("</table>");

          out.print("<p>");

          if (job.isFinished()) {
            out.print("<a href=\"" + convertContextPath(StartJobServlet.CONTEXT_PATH) + "?name=" + URLEncoder.encode(jobName, "UTF-8") + "\">"
                + BaseMessages.getString(PKG, "GetJobStatusServlet.StartJob") + "</a>");
            out.print("<p>");
          } else {
            out.print("<a href=\"" + convertContextPath(StopJobServlet.CONTEXT_PATH) + "?name=" + URLEncoder.encode(jobName, "UTF-8") + "\">"
                + BaseMessages.getString(PKG, "GetJobStatusServlet.StopJob") + "</a>");
            out.print("<p>");
          }

          out.println("<p>");

          out.print("<a href=\"" + convertContextPath(GetJobStatusServlet.CONTEXT_PATH) + "?name=" + URLEncoder.encode(jobName, "UTF-8") + "&xml=y\">"
              + BaseMessages.getString(PKG, "TransStatusServlet.ShowAsXml") + "</a><br>");
          out.print("<a href=\"" + convertContextPath(GetStatusServlet.CONTEXT_PATH) + "\">"
              + BaseMessages.getString(PKG, "TransStatusServlet.BackToStatusPage") + "</a><br>");
          out.print("<p><a href=\"" + convertContextPath(GetJobStatusServlet.CONTEXT_PATH) + "?name=" + URLEncoder.encode(jobName, "UTF-8") + "\">"
              + BaseMessages.getString(PKG, "TransStatusServlet.Refresh") + "</a>");

          // Put the logging below that.

          out.println("<p>");
          out.println("<textarea id=\"joblog\" cols=\"120\" rows=\"20\" wrap=\"off\" name=\"Job log\" readonly=\"readonly\">" + logText + "</textarea>");

          out.println("<script type=\"text/javascript\"> ");
          out.println("  joblog.scrollTop=joblog.scrollHeight; ");
          out.println("</script> ");
          out.println("<p>");
        } catch (Exception ex) {
          out.println("<p>");
          out.println("<pre>");
          ex.printStackTrace(out);
          out.println("</pre>");
        }

        out.println("<p>");
        out.println("</BODY>");
        out.println("</HTML>");
      }
    } else {
      if (useXML) {
        out.println(new WebResult(WebResult.STRING_ERROR, BaseMessages.getString(PKG, "StartJobServlet.Log.SpecifiedJobNotFound", jobName)));
      } else {
        out.println("<H1>Job '" + jobName + "' could not be found.</H1>");
        out.println("<a href=\"" + convertContextPath(GetStatusServlet.CONTEXT_PATH) + "\">"
            + BaseMessages.getString(PKG, "TransStatusServlet.BackToStatusPage") + "</a><p>");
      }
    }
  }

  public String toString() {
    return "Job Status Handler";
  }

  public String getService() {
    return CONTEXT_PATH + " (" + toString() + ")";
  }
}
