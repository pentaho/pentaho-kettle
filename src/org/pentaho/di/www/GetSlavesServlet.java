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
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;

public class GetSlavesServlet extends BaseHttpServlet implements
    CarteServletInterface {
  private static Class<?> PKG = GetSlavesServlet.class; // for i18n purposes,
  // needed by
  // Translator2!!
  // $NON-NLS-1$

  public static final String XML_TAG_SLAVESERVER_DETECTIONS = "SlaveServerDetections";

  private static final long serialVersionUID = -5472184538138241050L;
  public static final String CONTEXT_PATH = "/kettle/getSlaves";

  public GetSlavesServlet() {
  }

  public GetSlavesServlet(List<SlaveServerDetection> slaveServers) {
    super(slaveServers);
  }

  protected void doPut(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    doGet(request, response);
  }

  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    if (isJettyMode() && !request.getContextPath().startsWith(CONTEXT_PATH))
      return;

    if (log.isDebug())
      logDebug(BaseMessages.getString(PKG, "GetStatusServlet.StatusRequested"));
    response.setStatus(HttpServletResponse.SC_OK);

    // We always reply in XML...
    //
    response.setContentType("text/xml");
    response.setCharacterEncoding(Const.XML_ENCODING);
    PrintStream out = new PrintStream(response.getOutputStream());

    out.print(XMLHandler.getXMLHeader(Const.XML_ENCODING));
    out.println(XMLHandler.openTag(XML_TAG_SLAVESERVER_DETECTIONS));

    for (SlaveServerDetection slaveServer : getDetections()) {
      out.println(slaveServer.getXML());
    }

    out.println(XMLHandler.closeTag(XML_TAG_SLAVESERVER_DETECTIONS));

  }

  public String toString() {
    return "Get list of slave servers";
  }

  public String getService() {
    return CONTEXT_PATH + " (" + toString() + ")";
  }
}
