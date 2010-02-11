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
import org.pentaho.di.trans.Trans;

public class PauseTransServlet extends BaseHttpServlet implements CarteServletInterface {
  private static Class<?> PKG = PauseTransServlet.class; // for i18n purposes, needed by Translator2!! $NON-NLS-1$

  private static final long serialVersionUID = -2598233582435767691L;
  public static final String CONTEXT_PATH = "/kettle/pauseTrans";

  public PauseTransServlet() {
  }

  public PauseTransServlet(TransformationMap transformationMap) {
    super(transformationMap);
  }

  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    if (isJettyMode() && !request.getContextPath().startsWith(CONTEXT_PATH)) {
      return;
    }

    if (log.isDebug())
      logDebug(BaseMessages.getString(PKG, "PauseTransServlet.PauseOfTransRequested"));

    String transName = request.getParameter("name");
    boolean useXML = "Y".equalsIgnoreCase(request.getParameter("xml"));

    PrintWriter out = response.getWriter();
    try {
      if (useXML) {
        response.setContentType("text/xml");
        response.setCharacterEncoding(Const.XML_ENCODING);
        out.print(XMLHandler.getXMLHeader(Const.XML_ENCODING));
      } else {
        response.setContentType("text/html");
        out.println("<HTML>");
        out.println("<HEAD>");
        out.println("<TITLE>" + BaseMessages.getString(PKG, "PauseTransServlet.PauseTrans") + "</TITLE>");
        out.println("<META http-equiv=\"Refresh\" content=\"2;url=" + convertContextPath(GetTransStatusServlet.CONTEXT_PATH) + "?name="
            + URLEncoder.encode(transName, "UTF-8") + "\">");
        out.println("</HEAD>");
        out.println("<BODY>");
      }

      Trans trans = getTransformationMap().getTransformation(transName);

      if (trans != null) {
        String message;
        if (trans.isPaused()) {
          trans.resumeRunning();
          message = BaseMessages.getString(PKG, "PauseTransServlet.TransResumeRequested", transName);
        } else {
          trans.pauseRunning();
          message = BaseMessages.getString(PKG, "PauseTransServlet.TransPauseRequested", transName);
        }

        if (useXML) {
          out.println(new WebResult(WebResult.STRING_OK, message).getXML());
        } else {
          out.println("<H1>" + message + "</H1>");
          out.println("<a href=\"" + convertContextPath(GetTransStatusServlet.CONTEXT_PATH) + "?name=" + URLEncoder.encode(transName, "UTF-8") + "\">"
              + BaseMessages.getString(PKG, "TransStatusServlet.BackToTransStatusPage") + "</a><p>");
        }
      } else {
        String message = BaseMessages.getString(PKG, "PauseTransServlet.CanNotFindTrans", transName);

        if (useXML) {
          out.println(new WebResult(WebResult.STRING_ERROR, message).getXML());
        } else {
          out.println("<H1>" + message + "</H1>");
          out.println("<a href=\"" + convertContextPath(GetStatusServlet.CONTEXT_PATH) + "\">"
              + BaseMessages.getString(PKG, "TransStatusServlet.BackToStatusPage") + "</a><p>");
        }
      }
    } catch (Exception ex) {
      if (useXML) {
        out.println(new WebResult(WebResult.STRING_ERROR, Const.getStackTracker(ex)).getXML());
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
    return "Pause transformation";
  }

  public String getService() {
    return CONTEXT_PATH + " (" + toString() + ")";
  }
}
