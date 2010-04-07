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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.logging.CentralLogStore;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobConfiguration;
import org.pentaho.di.job.JobExecutionConfiguration;
import org.pentaho.di.job.JobListener;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.repository.Repository;

public class AddJobServlet extends BaseHttpServlet implements CarteServletInterface {
  private static final long serialVersionUID = -6850701762586992604L;

  public static final String CONTEXT_PATH = "/kettle/addJob";

  public AddJobServlet() {
  }

  public AddJobServlet(JobMap jobMap, SocketRepository socketRepository) {
    super(jobMap, socketRepository);
  }

  public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    if (isJettyMode() && !request.getRequestURI().startsWith(CONTEXT_PATH)) {
      return;
    }

    if (log.isDebug())
      logDebug("Addition of job requested");

    boolean useXML = "Y".equalsIgnoreCase(request.getParameter("xml"));

    PrintWriter out = response.getWriter();
    BufferedReader in = request.getReader(); // read from the client
    if (log.isDetailed())
      logDetailed("Encoding: " + request.getCharacterEncoding());

    if (useXML) {
      response.setContentType("text/xml");
      out.print(XMLHandler.getXMLHeader());
    } else {
      response.setContentType("text/html");
      out.println("<HTML>");
      out.println("<HEAD><TITLE>Add job</TITLE></HEAD>");
      out.println("<BODY>");
    }

    response.setStatus(HttpServletResponse.SC_OK);

    try {
      // First read the complete transformation in memory from the request
      int c;
      StringBuffer xml = new StringBuffer();
      while ((c = in.read()) != -1) {
        xml.append((char) c);
      }

      // Parse the XML, create a job configuration
      //
      // System.out.println(xml);
      //
      JobConfiguration jobConfiguration = JobConfiguration.fromXML(xml.toString());
      JobMeta jobMeta = jobConfiguration.getJobMeta();
      JobExecutionConfiguration jobExecutionConfiguration = jobConfiguration.getJobExecutionConfiguration();
      jobMeta.setLogLevel(jobExecutionConfiguration.getLogLevel());
      jobMeta.setArguments(jobExecutionConfiguration.getArgumentStrings());
      jobMeta.injectVariables(jobExecutionConfiguration.getVariables());

      // Also copy the parameters over...
      //
      Map<String, String> params = jobExecutionConfiguration.getParams();
      for (String param : params.keySet()) {
        String value = params.get(param);
        jobMeta.setParameterValue(param, value);
      }

      // If there was a repository, we know about it at this point in time.
      //
      final Repository repository = jobConfiguration.getJobExecutionConfiguration().getRepository();

      // Create the transformation and store in the list...
      //
      final Job job = new Job(repository, jobMeta);
      job.setLogLevel(jobExecutionConfiguration.getLogLevel());

      job.setSocketRepository(getSocketRepository());

      Job oldOne = getJobMap().getJob(job.getJobname());
      if (oldOne != null) {
        if (oldOne.isStopped() || oldOne.isFinished()) {
        	CentralLogStore.discardLines(oldOne.getLogChannelId(), true);
        }
      }
      
      String id = getJobMap().addJob(job.getJobname(), job, jobConfiguration);

      // Setting variables
      //
      job.initializeVariablesFrom(null);
      job.getJobMeta().setInternalKettleVariables(job);
      job.injectVariables(jobConfiguration.getJobExecutionConfiguration().getVariables());

      // Make sure to disconnect from the repository when the job finishes.
      // 
      if (repository != null) {
        job.addJobListener(new JobListener() {
          public void jobFinished(Job job) {
            repository.disconnect();
          }
        });
      }

      String message = "Job '" + job.getJobname() + "' was added to the list with id "+id;

      if (useXML) {
        out.println(new WebResult(WebResult.STRING_OK, message, id));
      } else {
        out.println("<H1>" + message + "</H1>");
        out.println("<p><a href=\"" + convertContextPath(GetJobStatusServlet.CONTEXT_PATH) + "?name=" + job.getJobname() + "&id="+id+"\">Go to the job status page</a><p>");
      }
    } catch (Exception ex) {
      if (useXML) {
        out.println(new WebResult(WebResult.STRING_ERROR, Const.getStackTracker(ex)));
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

  protected String[] getAllArgumentStrings(Map<String, String> arguments) {
    if (arguments == null || arguments.size() == 0)
      return null;

    String[] argNames = arguments.keySet().toArray(new String[arguments.size()]);
    Arrays.sort(argNames);

    String[] values = new String[argNames.length];
    for (int i = 0; i < argNames.length; i++) {
      values[i] = arguments.get(argNames[i]);
    }

    return values;
  }

  public String toString() {
    return "Add Job";
  }

  public String getService() {
    return CONTEXT_PATH + " (" + toString() + ")";
  }
}
