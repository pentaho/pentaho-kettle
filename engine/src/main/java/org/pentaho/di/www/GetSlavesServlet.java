/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;

public class GetSlavesServlet extends BaseHttpServlet implements CartePluginInterface {
  private static Class<?> PKG = GetSlavesServlet.class; // for i18n purposes,
  // needed by
  // Translator2!!

  public static final String XML_TAG_SLAVESERVER_DETECTIONS = "SlaveServerDetections";

  private static final long serialVersionUID = -5472184538138241050L;
  public static final String CONTEXT_PATH = "/kettle/getSlaves";

  public GetSlavesServlet() {
  }

  public GetSlavesServlet( List<SlaveServerDetection> slaveServers ) {
    super( slaveServers );
  }

  public GetSlavesServlet( List<SlaveServerDetection> slaveServers, boolean isJetty ) {
    super( slaveServers, isJetty );
  }

  /**
<div id="mindtouch">
    <h1>/kettle/getSlaves</h1>
    <a name="GET"></a>
    <h2>GET</h2>
    <p>Gets list of slave servers.
  Retrieves list of slave servers which are known to specific server.</p>

    <p><b>Example Request:</b><br />
    <pre function="syntax.xml">
    GET /kettle/getSlaves
    </pre>
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
    <p>Response contains list of slave servers.</p>

    <p><b>Example Response:</b></p>
    <pre function="syntax.xml">
    <?xml version="1.0" encoding="UTF-8"?>
    <SlaveServerDetections>
    <SlaveServerDetection>
      <slaveserver>
        <name>Dynamic slave &#x5b;localhost&#x3a;909&#x5d;</name><hostname>localhost</hostname><port>909</port>
        <webAppName/><username>cluster</username><password>Encrypted 2be98afc86aa7f2e4cb1aa265cd86aac8</password>
        <proxy_hostname/><proxy_port/><non_proxy_hosts/><master>N</master>
      </slaveserver>
      <active>Y</active>
      <last_active_date>2014&#x2f;11&#x2f;17 06&#x3a;42&#x3a;28.043</last_active_date>
      <last_inactive_date>2014&#x2f;11&#x2f;17 06&#x3a;42&#x3a;27.372</last_inactive_date>
    </SlaveServerDetection>

  </SlaveServerDetections>
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
      logDebug( BaseMessages.getString( PKG, "GetStatusServlet.StatusRequested" ) );
    }
    response.setStatus( HttpServletResponse.SC_OK );

    // We always reply in XML...
    //
    response.setContentType( "text/xml" );
    response.setCharacterEncoding( Const.XML_ENCODING );
    PrintStream out = new PrintStream( response.getOutputStream() );

    out.print( XMLHandler.getXMLHeader( Const.XML_ENCODING ) );
    out.println( XMLHandler.openTag( XML_TAG_SLAVESERVER_DETECTIONS ) );

    if ( getDetections() != null ) {
      for ( SlaveServerDetection slaveServer : getDetections() ) {
        try {
          slaveServer.getSlaveServer().getStatus();
        } catch ( Exception e ) {
          slaveServer.setActive( false );
          slaveServer.setLastInactiveDate( new Date() );
        }
        out.println( slaveServer.getXML() );
      }
    }

    out.println( XMLHandler.closeTag( XML_TAG_SLAVESERVER_DETECTIONS ) );

  }

  public String toString() {
    return "Get list of slave servers";
  }

  public String getService() {
    return CONTEXT_PATH + " (" + toString() + ")";
  }

  public String getContextPath() {
    return CONTEXT_PATH;
  }

}
