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
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.xml.XMLHandler;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class RegisterSlaveServlet extends BaseHttpServlet implements
    CarteServletInterface {
  private static final long serialVersionUID = 8513820270964866132L;

  public static final String CONTEXT_PATH = "/kettle/registerSlave";

  public RegisterSlaveServlet() {
  }

  public RegisterSlaveServlet(List<SlaveServerDetection> detections) {
    super(detections);
  }
  
  public RegisterSlaveServlet(List<SlaveServerDetection> detections, boolean isJetty) {
    super(detections, isJetty);
  }

  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    if (isJettyMode() && !request.getRequestURI().startsWith(CONTEXT_PATH)) {
      return;
    }

    if (log.isDebug())
      logDebug("Slave Server registration requested");

    PrintWriter out = response.getWriter();
    BufferedReader in = request.getReader();
    if (log.isDetailed())
      logDetailed("Encoding: " + request.getCharacterEncoding());

    // We always use XML to reply here...
    //
    response.setContentType("text/xml");
    out.print(XMLHandler.getXMLHeader());
    response.setStatus(HttpServletResponse.SC_OK);

    try {
      // First read the slave server information in memory from the request
      //
      StringBuilder xml = new StringBuilder(request.getContentLength());
      int c;
      while ((c = in.read()) != -1) {
        xml.append((char) c);
      }

      // Parse the XML, create a transformation configuration
      //
      Document document = XMLHandler.loadXMLString(xml.toString());
      Node node = XMLHandler.getSubNode(document, SlaveServerDetection.XML_TAG);
      SlaveServerDetection slaveServerDetection = new SlaveServerDetection(node);

      // See if this slave server is already in our list...
      //
      String message;
      int index = getDetections().indexOf(slaveServerDetection);
      if (index < 0) {
        getDetections().add(slaveServerDetection);
        message = "Slave server detection '"
            + slaveServerDetection.getSlaveServer().getName()
            + "' was replaced in the list.";
      } else {
        // replace the data in the old one...
        //
        SlaveServerDetection old = getDetections().get(index);
        old.setSlaveServer(slaveServerDetection.getSlaveServer());
        old.setActive(slaveServerDetection.isActive());

        // Note: in case it's not the slave server itself doing the sending, it
        // might be possible for it to be inactive...
        //
        if (old.isActive()) {
          old.setLastActiveDate(slaveServerDetection.getLastActiveDate());
        } else {
          old.setLastInactiveDate(slaveServerDetection.getLastInactiveDate());
        }
        message = "Slave server detection '"
            + slaveServerDetection.getSlaveServer().getName()
            + "' was added to the list.";
      }

      out.println(new WebResult(WebResult.STRING_OK, message));
    } catch (Exception ex) {
      out.println(new WebResult(WebResult.STRING_ERROR, Const
          .getStackTracker(ex)));
    }

  }

  public String toString() {
    return "Register slave server";
  }

  public String getService() {
    return CONTEXT_PATH + " (" + toString() + ")";
  }
}
