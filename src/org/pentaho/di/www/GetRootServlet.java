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

import org.pentaho.di.i18n.BaseMessages;

public class GetRootServlet extends BaseHttpServlet implements CarteServletInterface {
  private static Class<?> PKG = GetRootServlet.class; // for i18n purposes, needed by Translator2!! $NON-NLS-1$

  private static final long serialVersionUID = 3634806745372015720L;
  public static final String CONTEXT_PATH = "/";

  public GetRootServlet() {
  }

  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    if (isJettyMode() && !request.getRequestURI().equals(CONTEXT_PATH)) {
      return;
    }

    if (log.isDebug())
      logDebug(BaseMessages.getString(PKG, "GetRootServlet.RootRequested"));

    response.setContentType("text/html;charset=UTF-8");
    response.setStatus(HttpServletResponse.SC_OK);

    PrintWriter out = response.getWriter();

    out.println("<HTML>");
    out.println("<HEAD><TITLE>" + BaseMessages.getString(PKG, "GetRootServlet.KettleSlaveServer.Title") + "</TITLE>");
    out.println("<META http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">");
    out.println("</HEAD>");
    out.println("<BODY>");
    out.println("<H2>" + BaseMessages.getString(PKG, "GetRootServlet.SlaveServerMenu") + "</H2>");

    out.println("<p>");
    out.println("<a href=\"" + convertContextPath(GetStatusServlet.CONTEXT_PATH) + "\">" + BaseMessages.getString(PKG, "GetRootServlet.ShowStatus")
        + "</a><br>");

    out.println("<p>");
    out.println("</BODY>");
    out.println("</HTML>");
  }

  public String toString() {
    return "Root Handler";
  }

  public String getService() {
    return CONTEXT_PATH + " (" + toString() + ")";
  }
}
