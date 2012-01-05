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
  
  public GetSlavesServlet(List<SlaveServerDetection> slaveServers, boolean isJetty) {
    super(slaveServers, isJetty);
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

    if(getDetections() != null) {
      for (SlaveServerDetection slaveServer : getDetections()) {
        out.println(slaveServer.getXML());
      }
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
