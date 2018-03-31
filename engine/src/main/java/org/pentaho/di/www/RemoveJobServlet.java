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

import com.google.common.annotations.VisibleForTesting;
import org.owasp.encoder.Encode;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.Job;
import org.pentaho.di.www.cache.CarteStatusCache;


public class RemoveJobServlet extends BaseHttpServlet implements CartePluginInterface {

  private static Class<?> PKG = RemoveJobServlet.class; // for i18n purposes, needed by Translator2!!
  private static final long serialVersionUID = -2051906998698124039L;

  public static final String CONTEXT_PATH = "/kettle/removeJob";

  @VisibleForTesting
  private CarteStatusCache cache = CarteStatusCache.getInstance();

  public RemoveJobServlet() {
  }

  public RemoveJobServlet( JobMap jobMap ) {
    super( jobMap );
  }
  /**
  <div id="mindtouch">
      <h1>/kettle/removeJob</h1>
      <a name="GET"></a>
      <h2>GET</h2>
      <p>Remove specified job from Carte server.</p>

      <p><b>Example Request:</b><br />
      <pre function="syntax.xml">
      GET /kettle/removeJob/?name=dummy_job&xml=Y
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
      <td>name</td>
      <td>Name of the job to be removed.</td>
      <td>query</td>
      </tr>
      <tr>
      <td>xml</td>
      <td>Boolean flag which sets the output format required. Use <code>Y</code> to receive XML response.</td>
      <td>boolean, optional</td>
      </tr>
      <tr>
      <td>id</td>
      <td>Carte job ID of the job to be removed. This parameter is optional when xml=Y is used.</td>
      <td>query, optional</td>
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
          <td>text/xml, text/html</td>
        </tr>
      </tbody>
    </table>
    <p>Response XML or HTML containing operation result. When using xml=Y <code>result</code> field indicates whether
    operation was successful (<code>OK</code>) or not (<code>ERROR</code>).</p>

      <p><b>Example Response:</b></p>
    <pre function="syntax.xml">
    <?xml version="1.0" encoding="UTF-8"?>
    <webresult>
      <result>OK</result>
      <message/>
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
      logDebug( BaseMessages.getString( PKG, "RemoveJobServlet.Log.RemoveJobRequested" ) );
    }

    String jobName = request.getParameter( "name" );
    String id = request.getParameter( "id" );
    boolean useXML = "Y".equalsIgnoreCase( request.getParameter( "xml" ) );

    response.setStatus( HttpServletResponse.SC_OK );

    if ( useXML ) {
      response.setContentType( "text/xml" );
      response.setCharacterEncoding( Const.XML_ENCODING );
    } else {
      response.setContentType( "text/html;charset=UTF-8" );
    }

    PrintWriter out = response.getWriter();

    // ID is optional...
    //
    Job job;
    CarteObjectEntry entry;
    if ( Utils.isEmpty( id ) ) {
      // get the first transformation that matches...
      //
      entry = getJobMap().getFirstCarteObjectEntry( jobName );
      if ( entry == null ) {
        job = null;
      } else {
        id = entry.getId();
        job = getJobMap().getJob( entry );
      }
    } else {
      // Take the ID into account!
      //
      entry = new CarteObjectEntry( jobName, id );
      job = getJobMap().getJob( entry );
    }

    if ( job != null ) {

      cache.remove( job.getLogChannelId() );
      KettleLogStore.discardLines( job.getLogChannelId(), true );
      getJobMap().removeJob( entry );

      if ( useXML ) {
        response.setContentType( "text/xml" );
        response.setCharacterEncoding( Const.XML_ENCODING );
        out.print( XMLHandler.getXMLHeader( Const.XML_ENCODING ) );
        out.print( WebResult.OK.getXML() );
      } else {
        response.setContentType( "text/html;charset=UTF-8" );

        out.println( "<HTML>" );
        out.println( "<HEAD>" );
        out.println( "<TITLE>" + BaseMessages.getString( PKG, "RemoveJobServlet.JobRemoved" ) + "</TITLE>" );
        out.println( "<META http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">" );
        out.println( "</HEAD>" );
        out.println( "<BODY>" );
        out.println( "<H3>"
          + Encode.forHtml( BaseMessages
            .getString( PKG, "RemoveJobServlet.TheJobWasRemoved", jobName, id ) ) + "</H3>" );
        out.print( "<a href=\""
          + convertContextPath( GetStatusServlet.CONTEXT_PATH ) + "\">"
          + BaseMessages.getString( PKG, "TransStatusServlet.BackToStatusPage" ) + "</a><br>" );
        out.println( "<p>" );
        out.println( "</BODY>" );
        out.println( "</HTML>" );
      }
    } else {
      if ( useXML ) {
        out.println( new WebResult( WebResult.STRING_ERROR, BaseMessages.getString(
          PKG, "RemoveJobServlet.Log.CoundNotFindSpecJob", jobName ) ) );
      } else {
        out.println( "<H1>"
          + Encode.forHtml( BaseMessages.getString(
            PKG, "RemoveJobServlet.JobRemoved.Log.CoundNotFindJob", jobName, id ) ) + "</H1>" );
        out.println( "<a href=\""
          + convertContextPath( GetStatusServlet.CONTEXT_PATH ) + "\">"
          + BaseMessages.getString( PKG, "TransStatusServlet.BackToStatusPage" ) + "</a><p>" );
      }
    }
  }

  public String toString() {
    return "Remove job servlet";
  }

  public String getService() {
    return CONTEXT_PATH + " (" + toString() + ")";
  }

  public String getContextPath() {
    return CONTEXT_PATH;
  }

}
