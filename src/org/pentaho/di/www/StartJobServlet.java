/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.www;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.CentralLogStore;
import org.pentaho.di.core.logging.LoggingObjectType;
import org.pentaho.di.core.logging.SimpleLoggingObject;
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

  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    if (isJettyMode() && !request.getContextPath().startsWith(CONTEXT_PATH)) {
      return;
    }

    if (log.isDebug())
      logDebug(BaseMessages.getString(PKG, "StartJobServlet.Log.StartJobRequested"));

    String jobName = request.getParameter("name");
    String id = request.getParameter("id");
    boolean useXML = "Y".equalsIgnoreCase(request.getParameter("xml"));

    response.setStatus(HttpServletResponse.SC_OK);

    PrintWriter out = response.getWriter();
    if (useXML) {
      response.setContentType("text/xml");
      response.setCharacterEncoding(Const.XML_ENCODING);
      out.print(XMLHandler.getXMLHeader(Const.XML_ENCODING));
    } else {
      response.setContentType("text/html;charset=UTF-8");
      out.println("<HTML>");
      out.println("<HEAD>");
      out.println("<TITLE>Start job</TITLE>");
      out.println("<META http-equiv=\"Refresh\" content=\"2;url=" + convertContextPath(GetStatusServlet.CONTEXT_PATH) + "?name="
          + URLEncoder.encode(jobName, "UTF-8") + "\">");
      out.println("<META http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">");
      out.println("</HEAD>");
      out.println("<BODY>");
    }

    try {
      // ID is optional...
      //
      Job job;
      CarteObjectEntry entry;
      if (Const.isEmpty(id)) {
        	// get the first job that matches...
        	//
        	entry = getJobMap().getFirstCarteObjectEntry(jobName);
        	if (entry==null) {
        		job = null;
        	} else {
        		id = entry.getId();
        		job = getJobMap().getJob(entry);
        	}
      } else {
        	// Take the ID into account!
        	//
        	entry = new CarteObjectEntry(jobName, id);
        	job = getJobMap().getJob(entry);
      }

      if (job != null) {
        // First see if this job already ran to completion.
        // If so, we get an exception is we try to start() the job thread
        //
        if (job.isInitialized() && !job.isActive()) {
          // Re-create the job from the jobMeta
          //
          // We might need to re-connect to the repository
          //
          if (job.getRep() != null && !job.getRep().isConnected()) {
            job.getRep().connect(job.getRep().getUserInfo().getLogin(), job.getRep().getUserInfo().getPassword());
          }

          // Create a new job object to start from a sane state. Then replace
          // the new job in the job map
          //
          synchronized (getJobMap()) {
            JobConfiguration jobConfiguration = getJobMap().getConfiguration(jobName);
            
            String carteObjectId = UUID.randomUUID().toString();
            SimpleLoggingObject servletLoggingObject = new SimpleLoggingObject(CONTEXT_PATH, LoggingObjectType.CARTE, null);
            servletLoggingObject.setContainerObjectId(carteObjectId);
            
            Job newJob = new Job(job.getRep(), job.getJobMeta(), servletLoggingObject);
            newJob.setLogLevel(job.getLogLevel());

            // Discard old log lines from the old job
            //
            CentralLogStore.discardLines(job.getLogChannelId(), true);
            
            getJobMap().replaceJob(entry, newJob, jobConfiguration);
            job = newJob;
          }
        }

        runJob(job);

        String message = BaseMessages.getString(PKG, "StartJobServlet.Log.JobStarted", jobName);
        if (useXML) {
          out.println(new WebResult(WebResult.STRING_OK, message, id).getXML());
        } else {

          out.println("<H1>" + message + "</H1>");
          out.println("<a href=\"" + convertContextPath(GetJobStatusServlet.CONTEXT_PATH) + "?name=" + URLEncoder.encode(jobName, "UTF-8") + "&id="+id+"\">"
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
  
  protected void runJob(Job job) throws KettleException { 
     job.start(); // runs the thread in the background... 
   } 
}
