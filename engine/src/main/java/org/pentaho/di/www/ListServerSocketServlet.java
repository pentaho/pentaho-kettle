/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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

import org.owasp.encoder.Encode;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class ListServerSocketServlet extends BaseHttpServlet implements CartePluginInterface {
  private static final long serialVersionUID = 3634806745372015720L;

  public static final String CONTEXT_PATH = "/kettle/listSocket";

  public static final String PARAM_HOSTNAME = "host";
  public static final String PARAM_ONLY_OPEN = "onlyOpen";

  public ListServerSocketServlet() {
  }

  public ListServerSocketServlet( TransformationMap transformationMap ) {
    super( transformationMap );
  }

  /**
<div id="mindtouch">
    <h1>/kettle/listSocket</h1>
    <a name="GET"></a>
    <h2>GET</h2>
    <p>Gets list of ports for specified host.
  Method is used for listing all or just open ports for specified host. Response contains port number,
  which transformation it is (was) used for, current status of the port and last date time used.</p>
    
    <p><b>Example Request:</b><br />
    <pre function="syntax.xml">
    GET /kettle/listSocket/?host=127.0.0.1
    </pre>
    
    </p>
    <h3>Parameters</h3>
    <table class="pentaho-table">
    <tbody>
    <tr>
      <th>name</th>
      <th>description</th>
      <th>type</th>
    </tr>
    <tr>
    <td>host</td>
    <td>Host to get ports for.</td>
    <td>query</td>
    </tr>
    <tr>
    <td>onlyOpen</td>
    <td>Boolean flag that indicates whether all or only open ports should be returned.
  Set it to <code>Y</code> to get the list of only currently open ports.</td>
    <td>boolean, optional</td>
    </tr>
    </tbody>
    </table>
  
  <h3>Response Body</h3>

  <table class="pentaho-table">
    <tbody>
      <tr>
        <td align="right">text:</td>
        <td>HTML</td>
      </tr>
      <tr>
        <td align="right">media types:</td>
        <td>text/html</td>
      </tr>
    </tbody>
  </table>
    <p>Response is HTML document listing the ports requested.</p>
    
    <p><b>Example Response:</b></p>
  <pre function="syntax.xml">
  <HTML>
  <HEAD><TITLE>List of server sockets on server '127.0.0.1'</TITLE></HEAD>
  <BODY>
  <H1>Ports for host '127.0.0.1'</H1>
  Found 5 ports for host '127.0.0.1'<p>
  8088 : Transformation=dummy-trans, crt/Dummy (do nothing) 2.0
   --> sll/Dummy (do nothing).0
   id=b20bcd03-9682-4327-8c42-b129faabbfe1, allocated=false
   time=Mon Nov 17 09:31:15 BRT 2014
  <br>
  8089 : Transformation=dummy-trans, crt/Dummy (do nothing) 2.0
   --> sll/Dummy (do nothing).1
   id=b20bcd03-9682-4327-8c42-b129faabbfe1, allocated=false
   time=Mon Nov 17 09:31:15 BRT 2014
  <br>
  8090 : Transformation=dummy-trans, crt/Dummy (do nothing) 2.0
   --> sll/Dummy (do nothing).2
   id=b20bcd03-9682-4327-8c42-b129faabbfe1, allocated=false
   time=Mon Nov 17 09:31:15 BRT 2014
  <br>
  8091 : Transformation=dummy-trans, crt/Dummy (do nothing) 2.0
   --> sll/Dummy (do nothing).3
   id=b20bcd03-9682-4327-8c42-b129faabbfe1, allocated=false
   time=Mon Nov 17 09:31:15 BRT 2014
  <br>
  8092 : Transformation=dummy-trans, crt/Dummy (do nothing) 2.0
   --> sll/Dummy (do nothing).4
   id=b20bcd03-9682-4327-8c42-b129faabbfe1, allocated=false
   time=Mon Nov 17 09:31:15 BRT 2014
  <br>
  <p>
  </BODY>
  </HTML>
  </pre>

    <h3>Status Codes</h3>
    <table class="pentaho-table">
  <tbody>
    <tr>
      <th>code</th>
      <th>description</th>
    </tr>
    <tr>
      <td>200</td>
      <td>Request was processed.</td>
    </tr>
    <tr>
      <td>500</td>
      <td>Internal server error occurs during request processing.</td>
    </tr>
  </tbody>
</table>
</div>
  */
  public void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException,
    IOException {
    if ( isJettyMode() && !request.getContextPath().startsWith( CONTEXT_PATH ) ) {
      return;
    }

    if ( log.isDebug() ) {
      logDebug( "List of ports for a server requested" );
    }
    response.setStatus( HttpServletResponse.SC_OK );

    String hostname = request.getParameter( PARAM_HOSTNAME );
    boolean onlyOpen = "Y".equalsIgnoreCase( request.getParameter( PARAM_ONLY_OPEN ) );

    response.setContentType( "text/html" );
    PrintStream out = new PrintStream( response.getOutputStream() );

    out.println( "<HTML>" );
    out.println( "<HEAD><TITLE>List of server sockets on server "
      + Encode.forHtml( "\'" + hostname + "\'" ) + "</TITLE></HEAD>" );
    out.println( "<BODY>" );
    out.println( "<H1>Ports for host " + Encode.forHtml( "\'" + hostname + "\'" ) + "</H1>" );

    List<SocketPortAllocation> allocations = getTransformationMap().getHostServerSocketPorts( hostname );

    if ( allocations == null ) {
      out.println( "No port allocations found for host " + Encode.forHtml( "\'" + hostname + "\'" ) );
      return;
    }

    out.println( "Found " + allocations.size() + " ports for host " + Encode.forHtml( "\'" + hostname + "\'" ) + "<p>" );

    Iterator<SocketPortAllocation> iterator = allocations.iterator();
    while ( iterator.hasNext() ) {
      SocketPortAllocation allocation = iterator.next();

      if ( !onlyOpen || ( onlyOpen && allocation.isAllocated() ) ) {

        out.println( allocation.getPort()
          + " : Transformation=" + allocation.getTransformationName() + ", " + allocation.getSourceSlaveName()
          + "/" + allocation.getSourceStepName() + "." + allocation.getSourceStepCopy() );
        out.println( " --> "
          + allocation.getTargetSlaveName() + "/" + allocation.getTargetStepName() + "."
          + allocation.getTargetStepCopy() );
        out.println( " id=" + allocation.getClusterRunId() + ", allocated=" + allocation.isAllocated() );
        out.println( " time=" + allocation.getLastRequested() );

        out.println( "<br>" );
      }
    }

    out.println( "<p>" );
    out.println( "</BODY>" );
    out.println( "</HTML>" );
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
