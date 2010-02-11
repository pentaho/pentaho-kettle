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

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobConfiguration;

public class StartJobServlet extends BaseHttpServlet implements CarteServletInterface {
  private static Class<?> PKG = StartJobServlet.class; // for i18n purposes,
  // needed by
  // Translator2!!

  private static final long serialVersionUID = -8487225953910464032L;

  public static final String CONTEXT_PATH = "/kettle/startJob";

  public StartJobServlet() {
  }

  public StartJobServlet(JobMap jobMap) {
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
      logDebug(BaseMessages.getString(PKG, "StartJobServlet.Log.StartJobRequested"));

    String jobName = request.getParameter("name");
    boolean useXML = "Y".equalsIgnoreCase(request.getParameter("xml"));

    response.setStatus(HttpServletResponse.SC_OK);

    PrintWriter out = response.getWriter();
    if (useXML) {
      response.setContentType("text/xml");
      response.setCharacterEncoding(Const.XML_ENCODING);
      out.print(XMLHandler.getXMLHeader(Const.XML_ENCODING));
    } else {
      response.setContentType("text/html");
      out.println("<HTML>");
      out.println("<HEAD>");
      out.println("<TITLE>Start job</TITLE>");
      out.println("<META http-equiv=\"Refresh\" content=\"2;url=" + convertContextPath(GetStatusServlet.CONTEXT_PATH) + "?name="
          + URLEncoder.encode(jobName, "UTF-8") + "\">");
      out.println("</HEAD>");
      out.println("<BODY>");
    }

    try {
      Job job = getJobMap().getJob(jobName);
      if (job != null) {
        // First see if this job already ran to completion.
        // If so, we get an exception is we try to start() the job thread
        //
        if (job.isInitialized() && !job.isActive()) {
          // Re-create the job from the jobMeta
          //
          // We might need to re-connect to the database
          //
          if (job.getRep() != null && !job.getRep().isConnected()) {
            job.getRep().connect();
          }

          // Create a new job object to start from a sane state. Then replace
          // the new job in the job map
          //
          synchronized (getJobMap()) {
            JobConfiguration jobConfiguration = getJobMap().getConfiguration(jobName);
            Job newJob = new Job(job.getRep(), job.getJobMeta());

            getJobMap().removeJob(jobName);

            getJobMap().addJob(jobName, newJob, jobConfiguration);
            job = newJob;
          }
        }

        job.start(); // runs the thread in the background...

        String message = BaseMessages.getString(PKG, "StartJobServlet.Log.JobStarted", jobName);
        if (useXML) {
          out.println(new WebResult(WebResult.STRING_OK, message).getXML());
        } else {

          out.println("<H1>" + message + "</H1>");
          out.println("<a href=\"" + convertContextPath(GetJobStatusServlet.CONTEXT_PATH) + "?name=" + URLEncoder.encode(jobName, "UTF-8") + "\">"
              + BaseMessages.getString(PKG, "JobStatusServlet.BackToJobStatusPage") + "</a><p>");
        }
      } else {
        String message = BaseMessages.getString(PKG, "StartJobServlet.Log.SpecifiedJobNotFound", jobName);
        if (useXML) {
          out.println(new WebResult(WebResult.STRING_ERROR, message));
        } else {
          out.println("<H1>" + message + "</H1>");
          out.println("<a href=\"" + convertContextPath(GetStatusServlet.CONTEXT_PATH) + "\">"
              + BaseMessages.getString(PKG, "TransStatusServlet.BackToStatusPage") + "</a><p>");
        }
      }
    } catch (Exception ex) {
      if (useXML) {
        out.println(new WebResult(WebResult.STRING_ERROR, BaseMessages.getString(PKG, "StartJobServlet.Error.UnexpectedError", Const.CR
            + Const.getStackTracker(ex))));
      } else {
        out.println("<p>");
        out.println("<pre>");
        ex.printStackTrace(out);
        out.println("</pre>");
      }
    }

    if (!useXML) {
      out.println("<p>");
      out.println("</BODY>");
      out.println("</HTML>");
    }
  }

  public String toString() {
    return "Start job";
  }

  public String getService() {
    return CONTEXT_PATH + " (" + toString() + ")";
  }
}
