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
import java.io.PrintStream;
import java.net.URLEncoder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.Job;
import org.pentaho.di.trans.Trans;

public class GetStatusServlet extends BaseHttpServlet implements CarteServletInterface {
  private static Class<?> PKG = GetStatusServlet.class; // for i18n purposes, needed by Translator2!! $NON-NLS-1$

  private static final long serialVersionUID = 3634806745372015720L;

  public static final String CONTEXT_PATH = "/kettle/status";

  public GetStatusServlet() {
  }
  
  public GetStatusServlet(TransformationMap transformationMap, JobMap jobMap) {
    super(transformationMap, jobMap);
  }

  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    if (isJettyMode() && !request.getContextPath().startsWith(CONTEXT_PATH)) {
      return;
    }

    if (log.isDebug())
      logDebug(BaseMessages.getString(PKG, "GetStatusServlet.StatusRequested"));
    response.setStatus(HttpServletResponse.SC_OK);

    boolean useXML = "Y".equalsIgnoreCase(request.getParameter("xml"));

    if (useXML) {
      response.setContentType("text/xml");
      response.setCharacterEncoding(Const.XML_ENCODING);
    } else {
      response.setContentType("text/html");
    }

    PrintStream out = new PrintStream(response.getOutputStream());

    String[] transNames = getTransformationMap().getTransformationNames();
    String[] jobNames = getJobMap().getJobNames();

    if (useXML) {
      out.print(XMLHandler.getXMLHeader(Const.XML_ENCODING));
      SlaveServerStatus serverStatus = new SlaveServerStatus();
      serverStatus.setStatusDescription("Online");

      for (int i = 0; i < transNames.length; i++) {
        String name = transNames[i];
        Trans trans = getTransformationMap().getTransformation(name);
        String status = trans.getStatus();

        SlaveServerTransStatus sstatus = new SlaveServerTransStatus(name, status);
        sstatus.setPaused(trans.isPaused());
        serverStatus.getTransStatusList().add(sstatus);
      }

      for (int i = 0; i < jobNames.length; i++) {
        String name = jobNames[i];
        Job job = getJobMap().getJob(name);
        String status = job.getStatus();

        serverStatus.getJobStatusList().add(new SlaveServerJobStatus(name, status));
      }

      try {
        out.println(serverStatus.getXML());
      } catch (KettleException e) {
        throw new ServletException("Unable to get the server status in XML format", e);
      }
    } else {
      out.println("<HTML>");
      out.println("<HEAD><TITLE>" + BaseMessages.getString(PKG, "GetStatusServlet.KettleSlaveServerStatus") + "</TITLE></HEAD>");
      out.println("<BODY>");
      out.println("<H1>" + BaseMessages.getString(PKG, "GetStatusServlet.TopStatus") + "</H1>");

      try {
        out.println("<table border=\"1\">");
        out.print("<tr> <th>" + BaseMessages.getString(PKG, "GetStatusServlet.TransName") + "</th> <th>"
            + BaseMessages.getString(PKG, "GetStatusServlet.Status") + "</th> </tr>");

        for (int i = 0; i < transNames.length; i++) {
          String name = transNames[i];
          Trans trans = getTransformationMap().getTransformation(name);
          String status = trans.getStatus();

          out.print("<tr>");
          out.print("<td><a href=\"" + convertContextPath(GetTransStatusServlet.CONTEXT_PATH) + "?name=" + URLEncoder.encode(name, "UTF-8") + "\">" + name + "</a></td>");
          out.print("<td>" + status + "</td>");
          out.print("</tr>");
        }
        out.print("</table><p>");

        out.println("<table border=\"1\">");
        out.print("<tr> <th>" + BaseMessages.getString(PKG, "GetStatusServlet.JobName") + "</th> <th>" + BaseMessages.getString(PKG, "GetStatusServlet.Status")
            + "</th> </tr>");

        for (int i = 0; i < jobNames.length; i++) {
          String name = jobNames[i];
          Job job = getJobMap().getJob(name);
          String status = job.getStatus();

          out.print("<tr>");
          out.print("<td><a href=\"" + convertContextPath(GetJobStatusServlet.CONTEXT_PATH) + "?name=" + URLEncoder.encode(name, "UTF-8") + "\">" + name + "</a></td>");
          out.print("<td>" + status + "</td>");
          out.print("</tr>");
        }
        out.print("</table>");

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
  }

  public String toString() {
    return "Status Handler";
  }

  public String getService() {
    return CONTEXT_PATH + " (" + toString() + ")";
  }
}
