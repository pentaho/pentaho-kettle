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
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.pentaho.di.i18n.BaseMessages;

public class GetRootServlet extends BaseHttpServlet implements CartePluginInterface {
  private static Class<?> PKG = GetRootServlet.class; // for i18n purposes, needed by Translator2!!

  private static final long serialVersionUID = 3634806745372015720L;
  public static final String CONTEXT_PATH = "/";

  public GetRootServlet() {
  }
  /**
<div id="mindtouch">
    <h1>/</h1>
    <a name="GET"></a>
    <h2>GET</h2>
    <p>Display initial Carte page.</p>
    
    <p><b>Example Request:</b><br />
    <pre function="syntax.xml">
    GET /
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
        <td>text/html</td>
      </tr>
    </tbody>
  </table>
    <p>HTML response containing content of initial page is returned.</p>

    <p><b>Example Response:</b></p>
    <pre function="syntax.xml">
    <HTML>
      <HEAD><TITLE>Kettle slave server</TITLE>
        <META http-equiv="Content-Type" content="text/html; charset=UTF-8">
      </HEAD>
      <BODY>
        <H2>Slave server menu</H2>
        <p>
          <a href="/kettle/status">Show status</a><br>
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
    if ( isJettyMode() && !request.getRequestURI().equals( CONTEXT_PATH ) ) {
      response.setStatus( HttpServletResponse.SC_NOT_FOUND );
      return;
    }

    if ( log.isDebug() ) {
      logDebug( BaseMessages.getString( PKG, "GetRootServlet.RootRequested" ) );
    }

    response.setContentType( "text/html;charset=UTF-8" );
    response.setStatus( HttpServletResponse.SC_OK );

    PrintWriter out = response.getWriter();

    out.println( "<HTML>" );
    out.println( "<HEAD><TITLE>"
      + BaseMessages.getString( PKG, "GetRootServlet.KettleSlaveServer.Title" ) + "</TITLE>" );
    out.println( "<META http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">" );
    out.println( "</HEAD>" );
    out.println( "<BODY>" );
    out.println( "<H2>" + BaseMessages.getString( PKG, "GetRootServlet.SlaveServerMenu" ) + "</H2>" );

    out.println( "<p>" );
    out.println( "<a href=\""
      + convertContextPath( GetStatusServlet.CONTEXT_PATH ) + "\">"
      + BaseMessages.getString( PKG, "GetRootServlet.ShowStatus" ) + "</a><br>" );

    out.println( "<p>" );
    out.println( "</BODY>" );
    out.println( "</HTML>" );
  }

  public String toString() {
    return "Root Handler";
  }

  public String getService() {
    return CONTEXT_PATH + " (" + toString() + ")";
  }

  public String getContextPath() {
    return CONTEXT_PATH;
  }

}
