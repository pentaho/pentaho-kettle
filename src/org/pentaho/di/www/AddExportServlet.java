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
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.vfs.FileObject;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobConfiguration;
import org.pentaho.di.job.JobExecutionConfiguration;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransConfiguration;
import org.pentaho.di.trans.TransExecutionConfiguration;
import org.pentaho.di.trans.TransMeta;
import org.w3c.dom.Document;

/**
 * This servlet allows you to transport an exported job or transformation over to the carte server as a zip file. It ends up in a temporary file.
 * 
 * The servlet returns the name of the file stored.
 * 
 * @author matt
 * 
 */
public class AddExportServlet extends BaseHttpServlet implements CarteServletInterface {
  public static final String PARAMETER_LOAD = "load";
  public static final String PARAMETER_TYPE = "type";

  public static final String TYPE_JOB = "job";
  public static final String TYPE_TRANS = "trans";

  private static final long serialVersionUID = -6850701762586992604L;
  public static final String CONTEXT_PATH = "/kettle/addExport";

  public AddExportServlet() {
  }
  
  public AddExportServlet(JobMap jobMap, TransformationMap transformationMap) {
    super(transformationMap, jobMap);
  }

  protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    doGet(request, response);
  }

  public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    if (isJettyMode() && !request.getRequestURI().startsWith(CONTEXT_PATH))
      return;

    if (log.isDebug())
      logDebug("Addition of export requested");

    PrintWriter out = response.getWriter();
    BufferedReader in = request.getReader(); // read from the client
    if (log.isDetailed())
      logDetailed("Encoding: " + request.getCharacterEncoding());

    boolean isJob = TYPE_JOB.equalsIgnoreCase(request.getParameter(PARAMETER_TYPE));
    String load = request.getParameter(PARAMETER_LOAD); // the resource to load

    response.setContentType("text/xml");
    out.print(XMLHandler.getXMLHeader());

    response.setStatus(HttpServletResponse.SC_OK);

    OutputStream outputStream = null;

    try {
      FileObject tempFile = KettleVFS.createTempFile("export", ".zip", System.getProperty("java.io.tmpdir"));
      outputStream = KettleVFS.getOutputStream(tempFile, false);

      // Pass the input directly to a temporary file
      //
      int size = 0;
      int c;
      while ((c = in.read()) != -1) {
        outputStream.write(c);
        size++;
      }

      outputStream.flush();
      outputStream.close();
      outputStream = null; // don't close it twice

      String archiveUrl = tempFile.getName().toString();
      String fileUrl = null;

      // Now open the top level resource...
      //
      if (!Const.isEmpty(load)) {

        fileUrl = "zip:" + archiveUrl + "!" + load;

        if (isJob) {
          // Open the job from inside the ZIP archive
          //
          KettleVFS.getFileObject(fileUrl);

          JobMeta jobMeta = new JobMeta(fileUrl, null); // never with a repository
          Job job = new Job(null, jobMeta);

          // Also read the execution configuration information
          //
          String configUrl = "zip:" + archiveUrl + "!" + Job.CONFIGURATION_IN_EXPORT_FILENAME;
          Document configDoc = XMLHandler.loadXMLFile(configUrl);
          JobExecutionConfiguration jobExecutionConfiguration = new JobExecutionConfiguration(XMLHandler.getSubNode(configDoc,
              JobExecutionConfiguration.XML_TAG));

          // store it all in the map...
          //
          getJobMap().addJob(job.getJobname(), job, new JobConfiguration(jobMeta, jobExecutionConfiguration));

          // Apply the execution configuration...
          //
          log.setLogLevel(jobExecutionConfiguration.getLogLevel());
          jobMeta.setArguments(jobExecutionConfiguration.getArgumentStrings());
          jobMeta.injectVariables(jobExecutionConfiguration.getVariables());

          // Also copy the parameters over...
          //
          Map<String, String> params = jobExecutionConfiguration.getParams();
          for (String param : params.keySet()) {
            String value = params.get(param);
            jobMeta.setParameterValue(param, value);
          }

        } else {
          // Open the transformation from inside the ZIP archive
          //
          TransMeta transMeta = new TransMeta(fileUrl);
          Trans trans = new Trans(transMeta);

          // Also read the execution configuration information
          //
          String configUrl = "zip:" + archiveUrl + "!" + Trans.CONFIGURATION_IN_EXPORT_FILENAME;
          Document configDoc = XMLHandler.loadXMLFile(configUrl);
          TransExecutionConfiguration executionConfiguration = new TransExecutionConfiguration(XMLHandler.getSubNode(configDoc,
              TransExecutionConfiguration.XML_TAG));

          // store it all in the map...
          //
          getTransformationMap().addTransformation(trans.getName(), trans, new TransConfiguration(transMeta, executionConfiguration));
        }
      } else {
        fileUrl = archiveUrl;
      }

      out.println(new WebResult(WebResult.STRING_OK, fileUrl));
    } catch (Exception ex) {
      out.println(new WebResult(WebResult.STRING_ERROR, Const.getStackTracker(ex)));
    } finally {
      if (outputStream != null)
        outputStream.close();
    }
  }

  public String toString() {
    return "Add export";
  }

  public String getService() {
    return CONTEXT_PATH + " (" + toString() + ")";
  }

}
