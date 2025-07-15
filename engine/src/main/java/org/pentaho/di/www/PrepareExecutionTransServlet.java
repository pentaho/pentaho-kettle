/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.di.www;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.owasp.encoder.Encode;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransConfiguration;
import org.pentaho.di.trans.TransExecutionConfiguration;
import org.pentaho.di.www.cache.CarteStatusCache;


public class PrepareExecutionTransServlet extends BaseHttpServlet implements CartePluginInterface {
  private static Class<?> PKG = PrepareExecutionTransServlet.class; // for i18n purposes, needed by Translator2!!

  private static final long serialVersionUID = 3634806745372015720L;
  public static final String CONTEXT_PATH = "/kettle/prepareExec";
  private CarteStatusCache cache = CarteStatusCache.getInstance();

  public PrepareExecutionTransServlet() {
  }

  public PrepareExecutionTransServlet( TransformationMap transformationMap ) {
    super( transformationMap );
  }

  /**
<div id="mindtouch">
    <h1>/kettle/prepareExec</h1>
    <a name="GET"></a>
    <h2>GET</h2>
    <p>Prepares previously loaded transformation for execution.
  Method is used for preparing previously uploaded transformation for execution by its name.</p>

    <p><b>Example Request:</b><br />
    <pre function="syntax.xml">
    GET /kettle/prepareExec/?name=dummy-trans2&xml=Y
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
    <td>Name of the transformation to be prepared for execution.</td>
    <td>query</td>
    </tr>
    <tr>
    <td>xml</td>
    <td>Boolean flag which sets the output format required. Use <code>Y</code> to receive XML response.</td>
    <td>boolean</td>
    </tr>
    <tr>
    <td>id</td>
    <td>Carte transformation ID of the transformation to be prepared for execution.</td>
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
      logDebug( BaseMessages.getString( PKG, "PrepareExecutionTransServlet.TransPrepareExecutionRequested" ) );
    }

    String transName = request.getParameter( "name" );
    String id = request.getParameter( "id" );
    boolean useXML = "Y".equalsIgnoreCase( request.getParameter( "xml" ) );

    response.setStatus( HttpServletResponse.SC_OK );

    PrintWriter out = response.getWriter();
    if ( useXML ) {
      response.setContentType( "text/xml" );
      out.print( XMLHandler.getXMLHeader( Const.XML_ENCODING ) );
    } else {

      response.setCharacterEncoding( "UTF-8" );
      response.setContentType( "text/html;charset=UTF-8" );

      out.println( "<HTML>" );
      out.println( "<HEAD>" );
      out.println( "<TITLE>"
        + BaseMessages.getString( PKG, "PrepareExecutionTransServlet.TransPrepareExecution" ) + "</TITLE>" );
      out.println( "<META http-equiv=\"Refresh\" content=\"2;url="
        + convertContextPath( GetTransStatusServlet.CONTEXT_PATH ) + "?name="
        + URLEncoder.encode( transName, "UTF-8" ) + "\">" );
      out.println( "<META http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">" );
      out.println( "</HEAD>" );
      out.println( "<BODY>" );
    }

    try {
      // ID is optional...
      //
      Trans trans;
      CarteObjectEntry entry;
      if ( Utils.isEmpty( id ) ) {
        // get the first transformation that matches...
        //
        entry = getTransformationMap().getFirstCarteObjectEntry( transName );
        if ( entry == null ) {
          trans = null;
        } else {
          id = entry.getId();
          trans = getTransformationMap().getTransformation( entry );
        }
      } else {
        // Take the ID into account!
        //
        entry = new CarteObjectEntry( transName, id );
        trans = getTransformationMap().getTransformation( entry );
      }

      TransConfiguration transConfiguration = getTransformationMap().getConfiguration( entry );

      if ( trans != null && transConfiguration != null ) {
        TransExecutionConfiguration executionConfiguration = transConfiguration.getTransExecutionConfiguration();
        // Set the appropriate logging, variables, arguments, replay date, ...
        // etc.
        trans.setArguments( executionConfiguration.getArgumentStrings() );
        trans.setReplayDate( executionConfiguration.getReplayDate() );
        trans.setSafeModeEnabled( executionConfiguration.isSafeModeEnabled() );
        trans.setGatheringMetrics( executionConfiguration.isGatheringMetrics() );
        trans.injectVariables( executionConfiguration.getVariables() );
        trans.setPreviousResult( executionConfiguration.getPreviousResult() );
        trans.setExecutingClustered( executionConfiguration.isExecutingClustered() );

        try {
          trans.prepareExecution( null );
          cache.remove( trans.getLogChannelId() );

          if ( useXML ) {
            out.println( WebResult.OK.getXML() );
          } else {

            out.println( "<H1>"
              + Encode.forHtml( BaseMessages.getString(
                PKG, "PrepareExecutionTransServlet.TransPrepared", transName ) ) + "</H1>" );
            out.println( "<a href=\""
              + convertContextPath( GetTransStatusServlet.CONTEXT_PATH ) + "?name="
              + URLEncoder.encode( transName, "UTF-8" ) + "&id=" + URLEncoder.encode( id, "UTF-8" ) + "\">"
              + BaseMessages.getString( PKG, "TransStatusServlet.BackToTransStatusPage" ) + "</a><p>" );
          }
        } catch ( Exception e ) {

          String logText =
            KettleLogStore.getAppender().getBuffer( trans.getLogChannel().getLogChannelId(), true ).toString();
          if ( useXML ) {
            out.println( new WebResult( WebResult.STRING_ERROR, BaseMessages.getString(
              PKG, "PrepareExecutionTransServlet.Error.TransInitFailed", Const.CR
                + logText + Const.CR + Const.getStackTracker( e ) ) ) );
          } else {
            out.println( "<H1>"
              + Encode.forHtml( BaseMessages.getString(
                PKG, "PrepareExecutionTransServlet.Log.TransNotInit", transName ) ) + "</H1>" );

            out.println( "<pre>" );
            out.println( Encode.forHtml( logText ) );
            out.println( Encode.forHtml( Const.getStackTracker( e ) ) );
            out.println( "</pre>" );
            out.println( "<a href=\""
              + convertContextPath( GetTransStatusServlet.CONTEXT_PATH ) + "?name="
              + URLEncoder.encode( transName, "UTF-8" ) + "&id=" + id + "\">"
              + BaseMessages.getString( PKG, "TransStatusServlet.BackToTransStatusPage" ) + "</a><p>" );
            response.setStatus( HttpServletResponse.SC_BAD_REQUEST );
          }
        }
      } else {
        if ( useXML ) {
          out.println( new WebResult( WebResult.STRING_ERROR, BaseMessages.getString(
            PKG, "TransStatusServlet.Log.CoundNotFindSpecTrans", transName ) ) );
        } else {
          out.println( "<H1>"
            + Encode.forHtml( BaseMessages.getString(
              PKG, "TransStatusServlet.Log.CoundNotFindTrans", transName ) ) + "</H1>" );
          out.println( "<a href=\""
            + convertContextPath( GetStatusServlet.CONTEXT_PATH ) + "\">"
            + BaseMessages.getString( PKG, "TransStatusServlet.BackToStatusPage" ) + "</a><p>" );
          response.setStatus( HttpServletResponse.SC_BAD_REQUEST );
        }
      }
    } catch ( Exception ex ) {
      if ( useXML ) {
        out.println( new WebResult( WebResult.STRING_ERROR, BaseMessages.getString(
          PKG, "PrepareExecutionTransServlet.Error.UnexpectedError", Const.CR + Const.getStackTracker( ex ) ) ) );

      } else {
        out.println( "<p>" );
        out.println( "<pre>" );
        out.println( Encode.forHtml( Const.getStackTracker( ex ) ) );
        out.println( "</pre>" );
        response.setStatus( HttpServletResponse.SC_BAD_REQUEST );
      }
    }

    if ( !useXML ) {
      out.println( "<p>" );
      out.println( "</BODY>" );
      out.println( "</HTML>" );
    }
  }

  public String toString() {
    return "Start transformation";
  }

  public String getService() {
    return CONTEXT_PATH + " (" + toString() + ")";
  }

  public String getContextPath() {
    return CONTEXT_PATH;
  }

}
