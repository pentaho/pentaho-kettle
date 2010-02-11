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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.xml.XMLHandler;

/**
 * This servlet allows a client (TransSplitter in our case) to ask for a port number.<br>
 * This port number will be allocated in such a way that the port number is unique for a given hostname.<br>
 * This in turn will ensure that all the slaves will use valid port numbers, even if multiple slaves run on the same host.
 * 
 * @author matt
 * 
 */
public class AllocateServerSocketServlet extends BaseHttpServlet implements CarteServletInterface {
  private static final long serialVersionUID = 3634806745372015720L;

  public static final String CONTEXT_PATH = "/kettle/allocateSocket";

  public static final String PARAM_RANGE_START = "rangeStart";
  public static final String PARAM_HOSTNAME = "host";
  public static final String PARAM_TRANSFORMATION_NAME = "trans";
  public static final String PARAM_SOURCE_SLAVE = "sourceSlave";
  public static final String PARAM_SOURCE_STEPNAME = "sourceStep";
  public static final String PARAM_SOURCE_STEPCOPY = "sourceCopy";
  public static final String PARAM_TARGET_SLAVE = "targetSlave";
  public static final String PARAM_TARGET_STEPNAME = "targetStep";
  public static final String PARAM_TARGET_STEPCOPY = "targetCopy";

  public static final String XML_TAG_PORT = "port";

  public AllocateServerSocketServlet() {
  }

  public AllocateServerSocketServlet(TransformationMap transformationMap) {
    super(transformationMap);
  }

  protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    doGet(request, response);
  }

  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    if (isJettyMode() && !request.getContextPath().startsWith(CONTEXT_PATH))
      return;

    if (log.isDebug())
      logDebug("Reservation of port number of step requested");
    response.setStatus(HttpServletResponse.SC_OK);

    boolean useXML = "Y".equalsIgnoreCase(request.getParameter("xml"));

    String rangeStart = request.getParameter(PARAM_RANGE_START);
    String hostname = request.getParameter(PARAM_HOSTNAME);
    String transName = request.getParameter(PARAM_TRANSFORMATION_NAME);
    String sourceSlaveName = request.getParameter(PARAM_SOURCE_SLAVE);
    String sourceStepName = request.getParameter(PARAM_SOURCE_STEPNAME);
    String sourceStepCopy = request.getParameter(PARAM_SOURCE_STEPCOPY);
    String targetSlaveName = request.getParameter(PARAM_TARGET_SLAVE);
    String targetStepName = request.getParameter(PARAM_TARGET_STEPNAME);
    String targetStepCopy = request.getParameter(PARAM_TARGET_STEPCOPY);

    if (useXML) {
      response.setContentType("text/xml");
      response.setCharacterEncoding(Const.XML_ENCODING);
    } else {
      response.setContentType("text/html");
    }

    SocketPortAllocation port = getTransformationMap().allocateServerSocketPort(Const.toInt(rangeStart, 40000), hostname, transName, sourceSlaveName,
        sourceStepName, sourceStepCopy, targetSlaveName, targetStepName, targetStepCopy);

    PrintStream out = new PrintStream(response.getOutputStream());
    if (useXML) {
      out.print(XMLHandler.getXMLHeader(Const.XML_ENCODING));
      out.print(XMLHandler.addTagValue(XML_TAG_PORT, port.getPort()));
    } else {
      out.println("<HTML>");
      out.println("<HEAD><TITLE>Allocation of a server socket port number</TITLE></HEAD>");
      out.println("<BODY>");
      out.println("<H1>Status</H1>");

      out.println("<p>");
      out.println("Host name : " + hostname + "<br>");
      out.println("Transformation name : " + transName + "<br>");
      out.println("Source step : " + sourceStepName + "." + sourceStepCopy + "<br>");
      out.println("Target step : " + targetStepName + "." + targetStepCopy + "<br>");
      out.println("Step copy: " + sourceStepCopy + "<br>");
      out.println("<p>");
      out.println("--> port : " + port + "<br>");

      out.println("<p>");
      out.println("</BODY>");
      out.println("</HTML>");
    }
  }

  public String toString() {
    return "Servet socket port number reservation request";
  }

  public String getService() {
    return CONTEXT_PATH + " (" + toString() + ")";
  }
}
