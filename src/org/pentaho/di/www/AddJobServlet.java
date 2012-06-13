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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.logging.LoggingObjectType;
import org.pentaho.di.core.logging.SimpleLoggingObject;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobConfiguration;
import org.pentaho.di.job.JobExecutionConfiguration;
import org.pentaho.di.job.JobListener;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.repository.Repository;

public class AddJobServlet extends BaseHttpServlet implements CartePluginInterface {
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

      // If there was a repository, we know about it at this point in time.
      //
      final Repository repository = jobConfiguration.getJobExecutionConfiguration().getRepository();

      String carteObjectId = UUID.randomUUID().toString();
      SimpleLoggingObject servletLoggingObject = new SimpleLoggingObject(CONTEXT_PATH, LoggingObjectType.CARTE, null);
      servletLoggingObject.setContainerObjectId(carteObjectId);
      servletLoggingObject.setLogLevel(jobExecutionConfiguration.getLogLevel());

      // Create the transformation and store in the list...
      //
      final Job job = new Job(repository, jobMeta, servletLoggingObject);
      
      // Setting variables
      //
      job.initializeVariablesFrom(null);
      job.getJobMeta().setInternalKettleVariables(job);
      job.injectVariables(jobConfiguration.getJobExecutionConfiguration().getVariables());
      
      // Also copy the parameters over...
      //
      job.copyParametersFrom(jobMeta);
      job.clearParameters();
      String[] parameterNames = job.listParameters();
      for (int idx = 0; idx < parameterNames.length; idx++) {
        // Grab the parameter value set in the job entry
        //
        String thisValue = jobExecutionConfiguration.getParams().get(parameterNames[idx]);
        if (!Const.isEmpty(thisValue)) {
          // Set the value as specified by the user in the job entry
          //
          jobMeta.setParameterValue(parameterNames[idx], thisValue);
        }      
      }
      jobMeta.activateParameters();

      job.setSocketRepository(getSocketRepository());
      
      getJobMap().addJob(job.getJobname(), carteObjectId, job, jobConfiguration);

      // Make sure to disconnect from the repository when the job finishes.
      // 
      if (repository != null) {
        job.addJobListener(new JobListener() {
          public void jobFinished(Job job) {
            repository.disconnect();
          }
        });
      }

      String message = "Job '" + job.getJobname() + "' was added to the list with id "+carteObjectId;

      if (useXML) {
        out.println(new WebResult(WebResult.STRING_OK, message, carteObjectId));
      } else {
        out.println("<H1>" + message + "</H1>");
        out.println("<p><a href=\"" + convertContextPath(GetJobStatusServlet.CONTEXT_PATH) + "?name=" + job.getJobname() + "&id="+carteObjectId+"\">Go to the job status page</a><p>");
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
  
  public String getContextPath() {
    return CONTEXT_PATH;
  }
}
