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

public class CleanupTransServlet extends BaseHttpServlet implements CarteServletInterface {
  private static Class<?> PKG = CleanupTransServlet.class; // for i18n purposes, needed by Translator2!! $NON-NLS-1$

  private static final long serialVersionUID = -5879200987669847357L;

  public static final String CONTEXT_PATH = "/kettle/cleanupTrans";

  public CleanupTransServlet() {
  }
  
  public CleanupTransServlet(TransformationMap transformationMap) {
    super(transformationMap);
  }

  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    if (isJettyMode() && !request.getContextPath().startsWith(CONTEXT_PATH)) {
      return;
    }

    if (log.isDebug())
      logDebug(BaseMessages.getString(PKG, "TransStatusServlet.Log.TransCleanupRequested"));

    String transName = request.getParameter("name");
    String sockets = request.getParameter("sockets");

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
      out.println("<TITLE>Transformation cleanup</TITLE>");
      out.println("<META http-equiv=\"Refresh\" content=\"2;url=" + convertContextPath(GetTransStatusServlet.CONTEXT_PATH) + "?name="
          + URLEncoder.encode(transName, "UTF-8") + "\">");
      out.println("</HEAD>");
      out.println("<BODY>");
    }

    try {
      String message;

      if ("Y".equalsIgnoreCase(sockets)) {
        getTransformationMap().deallocateServerSocketPorts(transName);
        message = BaseMessages.getString(PKG, "TransStatusServlet.Log.TransServerSocketPortsReleased", transName);
      } else {
        Trans trans = getTransformationMap().getTransformation(transName);
        if (trans != null) {
          trans.cleanup();

          // Also release the server sockets
          message = BaseMessages.getString(PKG, "TransStatusServlet.Log.TransCleanednup", transName);
        } else {
          message = "The specified transformation [" + transName + "] could not be found";
          if (useXML) {
            out.println(new WebResult(WebResult.STRING_ERROR, message));
          } else {
            out.println("<H1>" + message + "</H1>");
            out.println("<a href=\"" + convertContextPath(GetStatusServlet.CONTEXT_PATH) + "\">"
                + BaseMessages.getString(PKG, "TransStatusServlet.BackToStatusPage") + "</a><p>");
          }
        }
      }

      if (useXML) {
        out.println(new WebResult(WebResult.STRING_OK, message).getXML());
      } else {
        out.println("<H1>" + message + "</H1>");
        out.println("<a href=\"" + convertContextPath(GetTransStatusServlet.CONTEXT_PATH) + "?name=" + URLEncoder.encode(transName, "UTF-8") + "\">"
            + BaseMessages.getString(PKG, "TransStatusServlet.BackToStatusPage") + "</a><p>");
      }
    } catch (Exception ex) {
      if (useXML) {
        out.println(new WebResult(WebResult.STRING_ERROR, "Unexpected error during transformations cleanup:" + Const.CR + Const.getStackTracker(ex)));
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
    return "Transformation cleanup";
  }

  public String getService() {
    return CONTEXT_PATH + " (" + toString() + ")";
  }
}
