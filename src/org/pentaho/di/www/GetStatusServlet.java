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
import java.util.Collections;
import java.util.List;

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

    List<CarteObjectEntry> transEntries = getTransformationMap().getTransformationObjects();
    List<CarteObjectEntry> jobEntries = getJobMap().getJobObjects();

    if (useXML) {
      out.print(XMLHandler.getXMLHeader(Const.XML_ENCODING));
      SlaveServerStatus serverStatus = new SlaveServerStatus();
      serverStatus.setStatusDescription("Online");

      for (CarteObjectEntry entry : transEntries) {
        String name = entry.getName();
        String id = entry.getId();
        Trans trans = getTransformationMap().getTransformation(entry);
        String status = trans.getStatus();

        SlaveServerTransStatus sstatus = new SlaveServerTransStatus(name, id, status);
        sstatus.setPaused(trans.isPaused());
        serverStatus.getTransStatusList().add(sstatus);
      }

      for (CarteObjectEntry entry : jobEntries) {
        String name = entry.getName();
        String id = entry.getId();
        Job job = getJobMap().getJob(name);
        String status = job.getStatus();

        serverStatus.getJobStatusList().add(new SlaveServerJobStatus(name, id, status));
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
        out.print("<tr> <th>" + 
        		BaseMessages.getString(PKG, "GetStatusServlet.TransName") + "</th> <th>" + 
        		BaseMessages.getString(PKG, "GetStatusServlet.CarteId") + "</th> <th>" + 
        		BaseMessages.getString(PKG, "GetStatusServlet.Status") + "</th> <th>" + 
        		BaseMessages.getString(PKG, "GetStatusServlet.LastLogDate") + "</th> <th>" +
        		BaseMessages.getString(PKG, "GetStatusServlet.Remove") + "</th> </tr>"
        	);

        Collections.sort(transEntries);
        
        for (CarteObjectEntry entry : transEntries) {
          String name = entry.getName();
          String id = entry.getId();
          Trans trans = getTransformationMap().getTransformation(entry);
          String status = trans.getStatus();
          String removeText = "";
          // Finished, Stopped, Waiting : allow the user to remove the transformation
          //
          if (trans.isFinished() || trans.isStopped() || (!trans.isInitializing() && !trans.isRunning())) {
        	  removeText = "<a href=\"" + convertContextPath(RemoveTransServlet.CONTEXT_PATH) + "?name=" + URLEncoder.encode(name, "UTF-8") + "&id="+id+"\"> Remove </a>"; 
          }
          
          out.print("<tr>");
          out.print("<td><a href=\"" + convertContextPath(GetTransStatusServlet.CONTEXT_PATH) + "?name=" + URLEncoder.encode(name, "UTF-8") + "&id="+id+"\">" + name + "</a></td>");
          out.print("<td>" + id + "</td>");
          out.print("<td>" + status + "</td>");
          out.print("<td>"+( trans.getLogDate()==null ? "-" : XMLHandler.date2string( trans.getLogDate() ))+"</td>");
          out.print("<td>"+removeText+"</td>");
          out.print("</tr>");
        }
        out.print("</table><p>");

        out.println("<table border=\"1\">");
        out.print("<tr> <th>" + BaseMessages.getString(PKG, "GetStatusServlet.JobName") + 
        		"</th> <th>" + BaseMessages.getString(PKG, "GetStatusServlet.CarteId")+ 
        		"</th> <th>" + BaseMessages.getString(PKG, "GetStatusServlet.Status")+ 
        		"</th> <th>" + BaseMessages.getString(PKG, "GetStatusServlet.LastLogDate") + 
        		"</th> <th>" + BaseMessages.getString(PKG, "GetStatusServlet.Remove")+ 
        		"</th> </tr>");

        Collections.sort(jobEntries);

        for (CarteObjectEntry entry : jobEntries) {
          String name = entry.getName();
          String id = entry.getId();
          Job job = getJobMap().getJob(name);
          String status = job.getStatus();

          String removeText;
          if (job.isFinished() || job.isStopped()) {
        	  removeText = "<a href=\"" + convertContextPath(RemoveJobServlet.CONTEXT_PATH) + "?name=" + URLEncoder.encode(name, "UTF-8") + "&id="+id+"\"> Remove </a>"; 
          } else {
        	  removeText = "";
          }

          out.print("<tr>");
          out.print("<td><a href=\"" + convertContextPath(GetJobStatusServlet.CONTEXT_PATH) + "?name=" + URLEncoder.encode(name, "UTF-8") + "&id="+id+"\">" + name + "</a></td>");
          out.print("<td>" + id + "</td>");
          out.print("<td>" + status + "</td>");
          out.print("<td>"+( job.getLogDate()==null ? "-" : XMLHandler.date2string( job.getLogDate() ))+"</td>");
          out.print("<td>"+removeText+"</td>");
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
      out.println("<H1>"+BaseMessages.getString(PKG, "GetStatusServlet.ConfigurationDetails.Title")+"</H1><p>");
      out.println("<table border=\"1\">");
      out.print("<tr> <th>" + BaseMessages.getString(PKG, "GetStatusServlet.Parameter.Title") + 
          "</th> <th>" + BaseMessages.getString(PKG, "GetStatusServlet.Value.Title")+ 
          "</th> </tr>");
      
      // The max number of log lines in the back-end
      //
      SlaveServerConfig serverConfig = getTransformationMap().getSlaveServerConfig();
      if (serverConfig!=null) {
        String maxLines = serverConfig.getMaxLogLines()+BaseMessages.getString(PKG, "GetStatusServlet.Lines");
        if (serverConfig.getMaxLogLines()==0) {
          maxLines += BaseMessages.getString(PKG, "GetStatusServlet.NoLimit");
        }
        out.print("<tr> <td>" + BaseMessages.getString(PKG, "GetStatusServlet.Parameter.MaxLogLines") + 
            "</td> <td>" + maxLines + "</td> </tr>");
        
        // The max age of log lines 
        //
        String maxAge = serverConfig.getMaxLogTimeoutMinutes()+BaseMessages.getString(PKG, "GetStatusServlet.Minutes");
        if (serverConfig.getMaxLogTimeoutMinutes()==0) {
          maxAge += BaseMessages.getString(PKG, "GetStatusServlet.NoLimit");
        }
        out.print("<tr> <td>" + BaseMessages.getString(PKG, "GetStatusServlet.Parameter.MaxLogLinesAge") + 
            "</td> <td>" + maxAge + "</td> </tr>");
        
        // The max age of stale objects
        //
        String maxObjAge = serverConfig.getObjectTimeoutMinutes()+BaseMessages.getString(PKG, "GetStatusServlet.Minutes");
        if (serverConfig.getObjectTimeoutMinutes()==0) {
          maxObjAge += BaseMessages.getString(PKG, "GetStatusServlet.NoLimit");
        }
        out.print("<tr> <td>" + BaseMessages.getString(PKG, "GetStatusServlet.Parameter.MaxObjectsAge") + 
            "</td> <td>" + maxObjAge + "</td> </tr>");
        
        out.print("</table>");
        
        String filename = serverConfig.getFilename();
        if (filename==null) {
          filename = BaseMessages.getString(PKG, "GetStatusServlet.ConfigurationDetails.UsingDefaults");
        }
        out.println("<i>"+BaseMessages.getString(PKG, "GetStatusServlet.ConfigurationDetails.Advice", filename)+"</i><br>");
      }
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
