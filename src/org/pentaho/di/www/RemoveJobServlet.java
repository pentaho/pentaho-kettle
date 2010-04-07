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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.logging.CentralLogStore;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.Job;

public class RemoveJobServlet extends BaseHttpServlet implements CarteServletInterface {

  private static Class<?> PKG = RemoveJobServlet.class; // for i18n purposes, needed by Translator2!! $NON-NLS-1$
  private static final long	serialVersionUID	= -2051906998698124039L;

  public static final String CONTEXT_PATH = "/kettle/removeJob";

  public RemoveJobServlet() {
  }
  
  public RemoveJobServlet(JobMap jobMap) {
    super(jobMap);
  }

  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    if (isJettyMode() && !request.getContextPath().startsWith(CONTEXT_PATH)) {
      return;
    }

    if (log.isDebug())
      logDebug(BaseMessages.getString(PKG, "RemoveJobServlet.Log.RemoveJobRequested"));

    String jobName = request.getParameter("name");
    String id = request.getParameter("id");
    boolean useXML = "Y".equalsIgnoreCase(request.getParameter("xml"));

    response.setStatus(HttpServletResponse.SC_OK);

    if (useXML) {
      response.setContentType("text/xml");
      response.setCharacterEncoding(Const.XML_ENCODING);
    } else {
      response.setContentType("text/html");
    }

    PrintWriter out = response.getWriter();

    // ID is optional...
    //
    Job job;
    CarteObjectEntry entry;
    if (Const.isEmpty(id)) {
    	// get the first transformation that matches...
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
      
      CentralLogStore.discardLines(job.getLogChannelId(), true);
      getJobMap().removeJob(entry);
      
      if (useXML) {
        response.setContentType("text/xml");
        response.setCharacterEncoding(Const.XML_ENCODING);
        out.print(XMLHandler.getXMLHeader(Const.XML_ENCODING));
        out.print(WebResult.OK.getXML());
      } else {
        response.setContentType("text/html");

        out.println("<HTML>");
        out.println("<HEAD>");
        out.println("<TITLE>" + BaseMessages.getString(PKG, "RemoveJobServlet.JobRemoved") + "</TITLE>");
        out.println("</HEAD>");
        out.println("<BODY>");
        out.println("<H3>" + BaseMessages.getString(PKG, "RemoveJobServlet.TheJobWasRemoved", jobName, id) + "</H3>");
        out.print("<a href=\"" + convertContextPath(GetStatusServlet.CONTEXT_PATH) + "\">" + BaseMessages.getString(PKG, "TransStatusServlet.BackToStatusPage") + "</a><br>");
        out.println("<p>");
        out.println("</BODY>");
        out.println("</HTML>");
      }
    } else {
      if (useXML) {
        out.println(new WebResult(WebResult.STRING_ERROR, BaseMessages.getString(PKG, "RemoveJobServlet.Log.CoundNotFindSpecJob", jobName)));
      } else {
        out.println("<H1>" + BaseMessages.getString(PKG, "RemoveJobServlet.JobRemoved.Log.CoundNotFindJob", jobName, id) + "</H1>");
        out.println("<a href=\"" + convertContextPath(GetStatusServlet.CONTEXT_PATH) + "\">" + BaseMessages.getString(PKG, "TransStatusServlet.BackToStatusPage") + "</a><p>");
      }
    }
  }

  public String toString() {
    return "Remove job servlet";
  }

  public String getService() {
    return CONTEXT_PATH + " (" + toString() + ")";
  }
}
