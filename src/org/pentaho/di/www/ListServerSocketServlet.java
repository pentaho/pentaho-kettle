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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This servlet allows a client (TransSplitter in our case) to ask for a port number.<br>
 * This port number will be allocated in such a way that the port number is unique for a given hostname.<br>
 * This in turn will ensure that all the slaves will use valid port numbers, even if multiple slaves run on the same host.
 * 
 * @author matt
 * 
 */
public class ListServerSocketServlet extends BaseHttpServlet implements CarteServletInterface {
  private static final long serialVersionUID = 3634806745372015720L;

  public static final String CONTEXT_PATH = "/kettle/listSocket";

  public static final String PARAM_HOSTNAME = "host";
  public static final String PARAM_ONLY_OPEN= "onlyOpen";

  public ListServerSocketServlet() {
  }

  public ListServerSocketServlet(TransformationMap transformationMap) {
    super(transformationMap);
  }

  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    if (isJettyMode() && !request.getContextPath().startsWith(CONTEXT_PATH))
      return;

    if (log.isDebug()) {
      logDebug("List of ports for a server requested");
    }
    response.setStatus(HttpServletResponse.SC_OK);

    String hostname = request.getParameter(PARAM_HOSTNAME);
    boolean onlyOpen = "Y".equalsIgnoreCase( request.getParameter(PARAM_ONLY_OPEN) );
    
    response.setContentType("text/html");
    PrintStream out = new PrintStream(response.getOutputStream());

    out.println("<HTML>");
    out.println("<HEAD><TITLE>List of server sockets on server '"+hostname+"'</TITLE></HEAD>");
    out.println("<BODY>");
    out.println("<H1>Ports for host '"+hostname+"'</H1>");

    Map<String, List<SocketPortAllocation>> portsMap = getTransformationMap().getHostServerSocketPortsMap();
    List<SocketPortAllocation> allocations = portsMap.get(hostname);
    if (allocations==null) {
      out.println("No port allocations found for host '"+hostname+"'");
      return;
    }

    out.println("Found "+allocations.size()+" ports for host '"+hostname+"'<p>");
    
    Iterator<SocketPortAllocation> iterator = allocations.iterator();
    while (iterator.hasNext()) {
      SocketPortAllocation allocation = iterator.next();
      
      if (!onlyOpen || (onlyOpen && allocation.isAllocated())) {
        
        out.println(allocation.getPort()+" : Transformation="+allocation.getTransformationName()+", "+allocation.getSourceSlaveName()+"/"+allocation.getSourceStepName()+"."+allocation.getSourceStepCopy());
        out.println(" --> "+allocation.getTargetSlaveName()+"/"+allocation.getTargetStepName()+"."+allocation.getTargetStepCopy());
        out.println(" id="+allocation.getClusterRunId()+", allocated="+allocation.isAllocated());
        out.println(" time="+allocation.getLastRequested());
        
        out.println("<br>");
      }
    }

    out.println("<p>");
    out.println("</BODY>");
    out.println("</HTML>");
  }

  public String toString() {
    return "Server socket port information request";
  }

  public String getService() {
    return CONTEXT_PATH + " (" + toString() + ")";
  }
}
