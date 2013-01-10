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
public class ListServerSocketServlet extends BaseHttpServlet implements CartePluginInterface {
  private static final long serialVersionUID = 3634806745372015720L;

  public static final String CONTEXT_PATH = "/kettle/listSocket";

  public static final String PARAM_HOSTNAME = "host";
  public static final String PARAM_ONLY_OPEN= "onlyOpen";

  public ListServerSocketServlet() {
  }

  public ListServerSocketServlet(TransformationMap transformationMap) {
    super(transformationMap);
  }

  public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
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
  
  public String getContextPath() {
    return CONTEXT_PATH;
  }

}
