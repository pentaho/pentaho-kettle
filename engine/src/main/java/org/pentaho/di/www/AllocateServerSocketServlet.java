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

import java.io.IOException;
import java.io.PrintStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.owasp.encoder.Encode;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.xml.XMLHandler;


/**
 * This servlet allows a client (TransSplitter in our case) to ask for a port number.<br>
 * This port number will be allocated in such a way that the port number is unique for a given hostname.<br>
 * This in turn will ensure that all the slaves will use valid port numbers, even if multiple slaves run on the same
 * host.
 *
 * @author matt
 *
 */
public class AllocateServerSocketServlet extends BaseHttpServlet implements CartePluginInterface {
  private static final long serialVersionUID = 3634806745372015720L;

  public static final String CONTEXT_PATH = "/kettle/allocateSocket";

  public static final String PARAM_RANGE_START = "rangeStart";
  public static final String PARAM_HOSTNAME = "host";
  public static final String PARAM_ID = "id";
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

  public AllocateServerSocketServlet( TransformationMap transformationMap ) {
    super( transformationMap );
  }

  /**

    <div id="mindtouch">
    <h1>/kettle/allocateSocket</h1>
    <a name="GET"></a>
    <h2>GET</h2>
    <p>Allocates port to use by client.
  Allows any client to ask for a port number to use. This is necessary several slaves can be run on the same host.
  The method ensures the port number is unique for host name provided and makes sure the slaves are using
  valid port numbers. Data communication across a cluster of Carte servers happens through TCP/IP sockets.
  Slave transformations sometimes open (or listen to) tens to hundreds of sockets.  When you want to allocate
  the port numbers for data communication between slave transformation in a kettle clustering run, you need
  unique combinations of all the parameters below.

  <code>port number</code> will be returned in the Response object. If an error occurred you'll receive html output
  describing the problem. HTTP status code of such response is 500.</p>

    <p><b>Example Request:</b><br />
    <pre function="syntax.xml">
    GET /kettle/allocateSocket/?xml=Y&rangeStart=100&host=locahost&id=clust&trans=my_trans&sourceSlave=slave_1
  &sourceStep=200&sourceCopy=1&targetSlave=slave_2&targetStep=50&targetCopy=1
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
    <td>xml</td>
    <td>Boolean flag set to either <code>Y</code> or <code>N</code> describing if xml or html reply
  should be produced.</td>
    <td>boolean, optional</td>
    </tr>
    <tr>
    <td>rangeStart</td>
    <td>Port number to start looking from.</td>
    <td>integer</td>
    </tr>
    <tr>
    <td>host</td>
    <td>Port's host.</td>
    <td>query</td>
    </tr>
    <tr>
    <td>id</td>
    <td>Carte container id.</td>
    <td>query</td>
    </tr>
    <tr>
    <td>trans</td>
    <td>Running transformation id.</td>
    <td>query</td>
    </tr>
    <tr>
    <td>sourceSlave</td>
    <td>Name of the source slave server.</td>
    <td>query</td>
    </tr>
    <tr>
    <td>sourceStep</td>
    <td>Port number step used on source slave server.</td>
    <td>integer</td>
    </tr>
    <tr>
    <td>sourceCopy</td>
    <td>Number of copies of the step on source server.</td>
    <td>integer</td>
    </tr>
    <tr>
    <td>targetSlave</td>
    <td>Name of the target slave server.</td>
    <td>query</td>
    </tr>
    <tr>
    <td>targetStep</td>
    <td>Port number step used on target slave server.</td>
    <td>integer</td>
    </tr>
    <tr>
    <td>targetCopy</td>
    <td>Number of copies of the step on target server.</td>
    <td>integer</td>
    </tr>
    </tbody>
    </table>

  <h3>Response Body</h3>

  <table class="pentaho-table">
    <tbody>
      <tr>
        <td align="right">element:</td>
        <td>(custom)</td>
      </tr>
      <tr>
        <td align="right">media types:</td>
        <td>text/xml, text/html</td>
      </tr>
    </tbody>
  </table>
    <p>Response wraps port number that was allocated or error stack trace
  if an error occurred. Response HTTP code is 200 if there were no errors. Otherwise it is 500.</p>

    <p><b>Example Response:</b></p>
    <pre function="syntax.xml">
    <?xml version="1.0" encoding="UTF-8"?>
    <port>100</port>
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
      <td>Internal server error occurs during request processing.
      This might also be caused by missing request parameter.</td>
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
      logDebug( "Reservation of port number of step requested" );
    }
    response.setStatus( HttpServletResponse.SC_OK );

    boolean useXML = "Y".equalsIgnoreCase( request.getParameter( "xml" ) );

    String rangeStart = request.getParameter( PARAM_RANGE_START );
    String hostname = request.getParameter( PARAM_HOSTNAME );
    String clusteredRunId = request.getParameter( PARAM_ID );
    String transName = request.getParameter( PARAM_TRANSFORMATION_NAME );
    String sourceSlaveName = request.getParameter( PARAM_SOURCE_SLAVE );
    String sourceStepName = request.getParameter( PARAM_SOURCE_STEPNAME );
    String sourceStepCopy = request.getParameter( PARAM_SOURCE_STEPCOPY );
    String targetSlaveName = request.getParameter( PARAM_TARGET_SLAVE );
    String targetStepName = request.getParameter( PARAM_TARGET_STEPNAME );
    String targetStepCopy = request.getParameter( PARAM_TARGET_STEPCOPY );

    if ( useXML ) {
      response.setContentType( "text/xml" );
      response.setCharacterEncoding( Const.XML_ENCODING );
    } else {
      response.setContentType( "text/html" );
    }

    SocketPortAllocation port =
      getTransformationMap().allocateServerSocketPort(
        Const.toInt( rangeStart, 40000 ), hostname, clusteredRunId, transName, sourceSlaveName,
        sourceStepName, sourceStepCopy, targetSlaveName, targetStepName, targetStepCopy );

    PrintStream out = new PrintStream( response.getOutputStream() );
    if ( useXML ) {
      out.print( XMLHandler.getXMLHeader( Const.XML_ENCODING ) );
      out.print( XMLHandler.addTagValue( XML_TAG_PORT, port.getPort() ) );
    } else {
      out.println( "<HTML>" );
      out.println( "<HEAD><TITLE>Allocation of a server socket port number</TITLE></HEAD>" );
      out.println( "<BODY>" );
      out.println( "<H1>Status</H1>" );

      out.println( "<p>" );
      out.println( "Run ID : " + Encode.forHtml( clusteredRunId ) + "<br>" );
      out.println( "Host name : " + Encode.forHtml( hostname ) + "<br>" );
      out.println( "Transformation name : " + Encode.forHtml( transName ) + "<br>" );
      out.println( "Source step : "
        + Encode.forHtml( sourceStepName ) + "." + Encode.forHtml( sourceStepCopy ) + "<br>" );
      out.println( "Target step : "
        + Encode.forHtml( targetStepName ) + "." + Encode.forHtml( targetStepCopy ) + "<br>" );
      out.println( "Step copy: " + Encode.forHtml( sourceStepCopy ) + "<br>" );
      out.println( "<p>" );
      out.println( "--> port : " + Encode.forHtml( port.toString() ) + "<br>" );

      out.println( "<p>" );
      out.println( "</BODY>" );
      out.println( "</HTML>" );
    }
  }

  public String toString() {
    return "Servet socket port number reservation request";
  }

  public String getService() {
    return CONTEXT_PATH + " (" + toString() + ")";
  }

  public String getContextPath() {
    return CONTEXT_PATH;
  }

}
