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

import org.pentaho.di.core.Const;
import org.pentaho.di.core.xml.XMLHandler;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class RegisterSlaveServlet extends BaseHttpServlet implements CartePluginInterface {
  private static final long serialVersionUID = 8513820270964866132L;

  public static final String CONTEXT_PATH = "/kettle/registerSlave";

  public RegisterSlaveServlet() {
  }

  public RegisterSlaveServlet( List<SlaveServerDetection> detections ) {
    super( detections );
  }

  public RegisterSlaveServlet( List<SlaveServerDetection> detections, boolean isJetty ) {
    super( detections, isJetty );
  }

  /**

  <div id="mindtouch">
  <h1>/kettle/registerSlave/</h1>
  <a name="POST"></a>
  <h2>POST</h2>
  <p>Registers slave server in the master.
  The method is used to add or update information of slave server.</p>

  <p><b>Example Request:</b><br />
  <pre function="syntax.xml">
  POST /kettle/registerSlave/
  </pre>
  Request body should contain xml containing slave server description.
  </p>

<h3>Response Body</h3>

<table class="pentaho-table">
  <tbody>
    <tr>
      <td align="right">element:</td>
      <td>(custom)</td>
    </tr>
    <tr>
      <td align="right">media types:</td>
      <td>text/xml</td>
    </tr>
  </tbody>
</table>
  <p>Response contains slave server name or error stack trace
if an error occurred. Response has <code>result</code> OK if there were no errors. Otherwise it returns ERROR.</p>

  <p><b>Example Response:</b></p>
  <pre function="syntax.xml">
  <?xml version="1.0" encoding="UTF-8"?>
  <webresult>
    <result>OK</result>
    <message>Slave server detection &#x27;Dynamic slave &#x5b;localhost&#x3a;901&#x5d;&#x27; was replaced in the list.</message>
    <id/>
  </webresult>
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
    <td>Request was processed and XML response is returned.</td>
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
    if ( isJettyMode() && !request.getRequestURI().startsWith( CONTEXT_PATH ) ) {
      return;
    }

    if ( log.isDebug() ) {
      logDebug( "Slave Server registration requested" );
    }

    PrintWriter out = response.getWriter();
    if ( log.isDetailed() ) {
      logDetailed( "Encoding: " + request.getCharacterEncoding() );
    }

    // We always use XML to reply here...
    //
    response.setContentType( "text/xml" );
    out.print( XMLHandler.getXMLHeader() );
    response.setStatus( HttpServletResponse.SC_OK );

    try {
      // First read the slave server information in memory from the request
      // Parse the XML, create a transformation configuration
      //
      Document document = XMLHandler.loadXMLFile( request.getInputStream() );
      Node node = XMLHandler.getSubNode( document, SlaveServerDetection.XML_TAG );
      SlaveServerDetection slaveServerDetection = new SlaveServerDetection( node );

      // See if this slave server is already in our list...
      //
      String message;
      int index = getDetections().indexOf( slaveServerDetection );
      if ( index < 0 ) {
        getDetections().add( slaveServerDetection );
        message =
          "Slave server detection '"
            + slaveServerDetection.getSlaveServer().getName() + "' was replaced in the list.";
      } else {
        // replace the data in the old one...
        //
        SlaveServerDetection old = getDetections().get( index );
        old.setSlaveServer( slaveServerDetection.getSlaveServer() );
        old.setActive( slaveServerDetection.isActive() );

        // Note: in case it's not the slave server itself doing the sending, it
        // might be possible for it to be inactive...
        //
        if ( old.isActive() ) {
          old.setLastActiveDate( slaveServerDetection.getLastActiveDate() );
        } else {
          old.setLastInactiveDate( slaveServerDetection.getLastInactiveDate() );
        }
        message =
          "Slave server detection '"
            + slaveServerDetection.getSlaveServer().getName() + "' was added to the list.";
      }

      out.println( new WebResult( WebResult.STRING_OK, message ) );
    } catch ( Exception ex ) {
      out.println( new WebResult( WebResult.STRING_ERROR, Const.getStackTracker( ex ) ) );
    }

  }

  public String toString() {
    return "Register slave server";
  }

  public String getService() {
    return CONTEXT_PATH + " (" + toString() + ")";
  }

  public String getContextPath() {
    return CONTEXT_PATH;
  }

}
