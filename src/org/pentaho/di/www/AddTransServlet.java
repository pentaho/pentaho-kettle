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
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.logging.CentralLogStore;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransConfiguration;
import org.pentaho.di.trans.TransExecutionConfiguration;
import org.pentaho.di.trans.TransListener;
import org.pentaho.di.trans.TransMeta;

public class AddTransServlet extends BaseHttpServlet implements CarteServletInterface {
  private static final long serialVersionUID = -6850701762586992604L;

  public static final String CONTEXT_PATH = "/kettle/addTrans";

  public AddTransServlet() {
  }

  public AddTransServlet(TransformationMap transformationMap, SocketRepository socketRepository) {
    super(transformationMap, socketRepository);
  }

  public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    if (isJettyMode() && !request.getRequestURI().startsWith(CONTEXT_PATH))
      return;

    if (log.isDebug())
      logDebug("Addition of transformation requested");

    boolean useXML = "Y".equalsIgnoreCase(request.getParameter("xml"));

    PrintWriter out = response.getWriter();
    BufferedReader in = request.getReader();
    if (log.isDetailed())
      logDetailed("Encoding: " + request.getCharacterEncoding());

    if (useXML) {
      response.setContentType("text/xml");
      out.print(XMLHandler.getXMLHeader());
    } else {
      response.setContentType("text/html");
      out.println("<HTML>");
      out.println("<HEAD><TITLE>Add transformation</TITLE></HEAD>");
      out.println("<BODY>");
    }

    response.setStatus(HttpServletResponse.SC_OK);

    try {
      // First read the complete transformation in memory from the request
      StringBuilder xml = new StringBuilder(request.getContentLength());
      int c;
      while ((c = in.read()) != -1) {
        xml.append((char) c);
      }

      // Parse the XML, create a transformation configuration
      //
      TransConfiguration transConfiguration = TransConfiguration.fromXML(xml.toString());
      TransMeta transMeta = transConfiguration.getTransMeta();
      TransExecutionConfiguration transExecutionConfiguration = transConfiguration.getTransExecutionConfiguration();
      log.setLogLevel(transExecutionConfiguration.getLogLevel());
      if (log.getLogLevel() >= LogWriter.LOG_LEVEL_DETAILED) {
        logDetailed("Logging level set to " + log.getLogLevelDesc());
      }
      transMeta.injectVariables(transExecutionConfiguration.getVariables());

      // Also copy the parameters over...
      //
      Map<String, String> params = transExecutionConfiguration.getParams();
      for (String param : params.keySet()) {
        String value = params.get(param);
        transMeta.setParameterValue(param, value);
      }

      // If there was a repository, we know about it at this point in time.
      //
      final Repository repository = transConfiguration.getTransExecutionConfiguration().getRepository();

      Trans oldTrans = getTransformationMap().getTransformation(transMeta.getName());
      if (oldTrans != null) {
        // To prevent serious memory leaks in the logging sub-system, we clear out the rows from the previous execution...
        //
        CentralLogStore.discardLines(oldTrans.getLogChannelId(), true);
      }

      // Create the transformation and store in the list...
      //
      final Trans trans = new Trans(transMeta);
      trans.setRepository(repository);
      trans.setSocketRepository(getSocketRepository());

      Trans oldOne = getTransformationMap().getTransformation(trans.getName());
      if (oldOne != null) {
        if (!oldOne.isStopped() && !oldOne.isFinished()) {
          if (oldOne.isRunning() || oldOne.isPreparing() || oldOne.isInitializing()) {
            throw new Exception("A transformation with the same name exists and is not idle." + Const.CR + "Please stop the transformation first.");
          }
        }
      }

      getTransformationMap().addTransformation(transMeta.getName(), trans, transConfiguration);

      if (repository != null) {
        // The repository connection is open: make sure we disconnect from the repository once we
        // are done with this transformation.
        //
        trans.addTransListener(new TransListener() {
          public void transFinished(Trans trans) {
            repository.disconnect();
          }
        });
      }

      String message;
      if (oldOne != null) {
        message = "Transformation '" + trans.getName() + "' was replaced in the list.";
      } else {
        message = "Transformation '" + trans.getName() + "' was added to the list.";
      }
      // message+=" (session id = "+request.getSession(true).getId()+")";

      if (useXML) {
        // Return the log channel id as well
        //
        out.println(new WebResult(WebResult.STRING_OK, message, trans.getLogChannelId()));
      } else {
        out.println("<H1>" + message + "</H1>");
        out.println("<p><a href=\"" + convertContextPath(GetStatusServlet.CONTEXT_PATH) + "?name=" + trans.getName()
            + "\">Go to the transformation status page</a><p>");
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

  public String toString() {
    return "Add Transformation";
  }

  public String getService() {
    return CONTEXT_PATH + " (" + toString() + ")";
  }
}
