/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2019 by Hitachi Vantara : http://www.pentaho.com
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
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.nio.charset.Charset;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.annotations.VisibleForTesting;
import org.owasp.encoder.Encode;
import org.pentaho.di.cluster.HttpUtil;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.EnvUtil;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.gui.Point;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.step.BaseStepData.StepExecutionStatus;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepStatus;
import org.pentaho.di.www.cache.CarteStatusCache;


public class GetTransStatusServlet extends BaseHttpServlet implements CartePluginInterface {

  private static Class<?> PKG = GetTransStatusServlet.class; // for i18n purposes, needed by Translator2!!

  private static final long serialVersionUID = 3634806745372015720L;

  public static final String CONTEXT_PATH = "/kettle/transStatus";

  public static final String SEND_RESULT = "sendResult";

  private static final byte[] XML_HEADER =
    XMLHandler.getXMLHeader( Const.XML_ENCODING ).getBytes( Charset.forName( Const.XML_ENCODING ) );

  @VisibleForTesting
  CarteStatusCache cache = CarteStatusCache.getInstance();

  public GetTransStatusServlet() {
  }

  public GetTransStatusServlet( TransformationMap transformationMap ) {
    super( transformationMap );
  }

  /**
   <div id="mindtouch">
   <h1>/kettle/transStatus</h1>
   <a name="GET"></a>
   <h2>GET</h2>
   <p>Retrieves status of the specified transformation. Status is returned as HTML or XML output
   depending on the input parameters. Status contains information about last execution of the transformation.</p>
   <p><b>Example Request:</b><br />
   <pre function="syntax.xml">
   GET /kettle/transStatus/?name=dummy-trans&xml=Y
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
   <td>Name of the transformation to be used for status generation.</td>
   <td>query</td>
   </tr>
   <tr>
   <td>xml</td>
   <td>Boolean flag which defines output format <code>Y</code> forces XML output to be generated.
   HTML is returned otherwise.</td>
   <td>boolean, optional</td>
   </tr>
   <tr>
   <td>id</td>
   <td>Carte id of the transformation to be used for status generation.</td>
   <td>query, optional</td>
   </tr>
   <tr>
   <td>from</td>
   <td>Start line number of the execution log to be included into response.</td>
   <td>integer, optional</td>
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
   <p> Response XML or HTML response containing details about the transformation specified.
   If an error occurs during method invocation <code>result</code> field of the response
   will contain <code>ERROR</code> status.</p>

   <p><b>Example Response:</b></p>
   <pre function="syntax.xml">
   <?xml version="1.0" encoding="UTF-8"?>
   <transstatus>
   <transname>dummy-trans</transname>
   <id>c56961b2-c848-49b8-abde-76c8015e29b0</id>
   <status_desc>Stopped</status_desc>
   <error_desc/>
   <paused>N</paused>
   <stepstatuslist>
   <stepstatus><stepname>Dummy &#x28;do nothing&#x29;</stepname>
   <copy>0</copy><linesRead>0</linesRead>
   <linesWritten>0</linesWritten><linesInput>0</linesInput>
   <linesOutput>0</linesOutput><linesUpdated>0</linesUpdated>
   <linesRejected>0</linesRejected><errors>0</errors>
   <statusDescription>Stopped</statusDescription><seconds>0.0</seconds>
   <speed>-</speed><priority>-</priority><stopped>Y</stopped>
   <paused>N</paused>
   </stepstatus>
   </stepstatuslist>
   <first_log_line_nr>0</first_log_line_nr>
   <last_log_line_nr>37</last_log_line_nr>
   <result>
   <lines_input>0</lines_input>
   <lines_output>0</lines_output>
   <lines_read>0</lines_read>
   <lines_written>0</lines_written>
   <lines_updated>0</lines_updated>
   <lines_rejected>0</lines_rejected>
   <lines_deleted>0</lines_deleted>
   <nr_errors>0</nr_errors>
   <nr_files_retrieved>0</nr_files_retrieved>
   <entry_nr>0</entry_nr>
   <result>Y</result>
   <exit_status>0</exit_status>
   <is_stopped>Y</is_stopped>
   <log_channel_id>10e2c832-07da-409a-a5ba-4b90a234e957</log_channel_id>
   <log_text/>
   <result-file></result-file>
   <result-rows></result-rows>
   </result>
   <logging_string>&#x3c;&#x21;&#x5b;CDATA&#x5b;H4sIAAAAAAAAADMyMDTRNzTUNzJRMDSyMrC0MjFV0FVIKc3NrdQtKUrMKwbyXDKLCxJLkjMy89IViksSi0pSUxTS8osUwPJARm5iSWZ&#x2b;nkI0kq5YXi4AQVH5bFoAAAA&#x3d;&#x5d;&#x5d;&#x3e;</logging_string>
   </transstatus>
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
      logDebug( BaseMessages.getString( PKG, "TransStatusServlet.Log.TransStatusRequested" ) );
    }

    String transName = request.getParameter( "name" );
    String id = request.getParameter( "id" );
    String root = request.getRequestURI() == null ? StatusServletUtils.PENTAHO_ROOT
      : request.getRequestURI().substring( 0, request.getRequestURI().indexOf( CONTEXT_PATH ) );
    String prefix = isJettyMode() ? StatusServletUtils.STATIC_PATH : root + StatusServletUtils.RESOURCES_PATH;
    boolean useXML = "Y".equalsIgnoreCase( request.getParameter( "xml" ) );
    int startLineNr = Const.toInt( request.getParameter( "from" ), 0 );

    response.setStatus( HttpServletResponse.SC_OK );

    if ( useXML ) {
      response.setContentType( "text/xml" );
      response.setCharacterEncoding( Const.XML_ENCODING );
    } else {
      response.setCharacterEncoding( "UTF-8" );
      response.setContentType( "text/html;charset=UTF-8" );

    }

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

    if ( trans != null ) {
      if ( useXML ) {
        try {
          OutputStream out = null;
          byte[] data = null;
          String logId = trans.getLogChannelId();
          boolean finishedOrStopped = trans.isFinishedOrStopped();
          boolean sendResultXmlWithStatus = "Y".equalsIgnoreCase( request.getParameter( SEND_RESULT ) );
          boolean dontUseCache = sendResultXmlWithStatus;
          if ( finishedOrStopped && ( data = cache.get( logId, startLineNr ) ) != null && !dontUseCache ) {
            response.setContentLength( XML_HEADER.length + data.length );
            out = response.getOutputStream();
            out.write( XML_HEADER );
            out.write( data );
            out.flush();
          } else {
            int lastLineNr = KettleLogStore.getLastBufferLineNr();

            String logText = getLogText( trans, startLineNr, lastLineNr );

            response.setContentType( "text/xml" );
            response.setCharacterEncoding( Const.XML_ENCODING );

            SlaveServerTransStatus transStatus =
              new SlaveServerTransStatus( transName, entry.getId(), trans.getStatus() );
            transStatus.setFirstLoggingLineNr( startLineNr );
            transStatus.setLastLoggingLineNr( lastLineNr );
            transStatus.setLogDate( trans.getLogDate() );

            for ( int i = 0; i < trans.nrSteps(); i++ ) {
              StepInterface baseStep = trans.getRunThread( i );
              if ( ( baseStep.isRunning() ) || baseStep.getStatus() != StepExecutionStatus.STATUS_EMPTY ) {
                StepStatus stepStatus = new StepStatus( baseStep );
                transStatus.getStepStatusList().add( stepStatus );
              }
            }

            // The log can be quite large at times, we are going to putIfAbsent a base64 encoding around a compressed
            // stream
            // of bytes to handle this one.
            String loggingString = HttpUtil.encodeBase64ZippedString( logText );
            transStatus.setLoggingString( loggingString );
            //        transStatus.setLoggingUncompressedSize( logText.length() );

            // Also set the result object...
            //
            transStatus.setResult( trans.getResult() );

            // Is the transformation paused?
            //
            transStatus.setPaused( trans.isPaused() );

            // Send the result back as XML
            //
            String xml = transStatus.getXML( sendResultXmlWithStatus );
            data = xml.getBytes( Charset.forName( Const.XML_ENCODING ) );
            out = response.getOutputStream();
            response.setContentLength( XML_HEADER.length + data.length );
            out.write( XML_HEADER );
            out.write( data );
            out.flush();
            if ( finishedOrStopped && ( transStatus.isFinished() || transStatus.isStopped() ) && logId != null && !dontUseCache ) {
              cache.put( logId, xml, startLineNr );
            }
          }
          response.flushBuffer();
        } catch ( KettleException e ) {
          throw new ServletException( "Unable to get the transformation status in XML format", e );
        }

      } else {
        PrintWriter out = response.getWriter();

        int lastLineNr = KettleLogStore.getLastBufferLineNr();
        int tableBorder = 0;

        response.setContentType( "text/html;charset=UTF-8" );

        out.println( "<HTML>" );
        out.println( "<HEAD>" );
        out.println( "<TITLE>"
          + BaseMessages.getString( PKG, "TransStatusServlet.KettleTransStatus" ) + "</TITLE>" );
        if ( EnvUtil.getSystemProperty( Const.KETTLE_CARTE_REFRESH_STATUS, "N" ).equalsIgnoreCase( "Y" ) ) {
          out.println( "<META http-equiv=\"Refresh\" content=\"10;url="
            + convertContextPath( CONTEXT_PATH ) + "?name=" + URLEncoder.encode( transName, "UTF-8" ) + "&id="
            + URLEncoder.encode( id, "UTF-8" ) + "\">" );
        }
        out.println( "<META http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">" );

        if ( isJettyMode() ) {
          out.println( "<link rel=\"stylesheet\" type=\"text/css\" href=\"/static/css/carte.css\" />" );
        } else {
          out.print( StatusServletUtils.getPentahoStyles( root ) );
        }

        out.println( "</HEAD>" );
        out.println( "<BODY style=\"overflow: auto;\">" );
        out.println( "<div class=\"row\" id=\"pucHeader\">" );
        out.println( "<div class=\"workspaceHeading\" style=\"padding: 0px 0px 0px 10px;\">"
          + Encode.forHtml( BaseMessages.getString( PKG, "TransStatusServlet.TopTransStatus", transName ) )
          + "</div>" );
        out.println( "</div>" );

        try {
          out.println( "<div class=\"row\" style=\"padding: 0px 0px 0px 30px\">" );
          out.println( "<div class=\"row\" style=\"padding-top: 30px;\">" );
          out.print( "<a href=\"" + convertContextPath( GetStatusServlet.CONTEXT_PATH ) + "\">" );
          out.print( "<img src=\"" + prefix + "/images/back.svg\" style=\"margin-right: 5px; width: 16px; height: 16px; vertical-align: middle;\">" );
          out.print( BaseMessages.getString( PKG, "CarteStatusServlet.BackToCarteStatus" ) + "</a>" );
          out.println( "</div>" );
          out.println( "<div class=\"row\" style=\"padding: 30px 0px 75px 0px; display: table;\">" );
          out.println( "<div style=\"display: table-row;\">" );
          out.println( "<div style=\"padding: 0px 30px 0px 0px; width: 60px; display: table-cell; vertical-align: top;\">" );
          out.println( "<img src=\"" + prefix + "/images/trans.svg\" style=\"width: 60px; height: 60px;\"></img>" );
          out.println( "</div>" );
          out.println( "<div style=\"vertical-align: top; display: table-cell;\">" );
          out.println( "<table style=\"border-collapse: collapse;\" border=\"" + tableBorder + "\">" );
          out.print( "<tr class=\"cellTableRow\" style=\"border: solid; border-width: 1px 0; border-top: none; border-color: #E3E3E3; font-size: 12; text-align: left;\"> <th style=\"font-weight: normal; padding: 8px 10px 10px 10px\" class=\"cellTableHeader\">"
            + BaseMessages.getString( PKG, "TransStatusServlet.CarteObjectId" ) + "</th> <th style=\"font-weight: normal; padding: 8px 10px 10px 10px\" class=\"cellTableHeader\">"
            + BaseMessages.getString( PKG, "TransStatusServlet.TransStatus" ) + "</th> <th style=\"font-weight: normal; padding: 8px 10px 10px 10px\" class=\"cellTableHeader\">"
            + BaseMessages.getString( PKG, "TransStatusServlet.LastLogDate" ) + "</th> </tr>" );
          out.print( "<tr class=\"cellTableRow\" style=\"border: solid; border-width: 1px 0; border-bottom: none; font-size: 12; text-align: left;\">" );
          out.print( "<td style=\"padding: 8px 10px 10px 10px\" class=\"cellTableCell cellTableFirstColumn\">" + Encode.forHtml( id ) + "</td>" );
          out.print( "<td style=\"padding: 8px 10px 10px 10px\" class=\"cellTableCell\" id=\"statusColor\" style=\"font-weight: bold;\">" + Encode.forHtml( trans.getStatus() ) + "</td>" );
          String dateStr = XMLHandler.date2string( trans.getLogDate() );
          out.print( "<td style=\"padding: 8px 10px 10px 10px\" class=\"cellTableCell cellTableLastColumn\">" + dateStr.substring( 0, dateStr.indexOf( ' ' ) ) + "</td>" );
          out.print( "</tr>" );
          out.print( "</table>" );
          out.print( "</div>" );
          out.println( "<div style=\"padding: 0px 0px 0px 20px; width: 90px; display: table-cell; vertical-align: top;\">" );
          out.print( "<div style=\"display: block; margin-left: auto; margin-right: auto; padding: 5px 0px;\">" );
          out.print( "<a target=\"_blank\" href=\""
            + convertContextPath( GetTransStatusServlet.CONTEXT_PATH ) + "?name="
            + URLEncoder.encode( transName, "UTF-8" ) + "&id=" + URLEncoder.encode( id, "UTF-8" ) + "&xml=y\">"
            + "<img src=\"" + prefix + "/images/view-as-xml.svg\" style=\"display: block; margin: auto; width: 22px; height: 22px;\"></a>" );
          out.print( "</div>" );
          out.println( "<div style=\"text-align: center; padding-top: 12px; font-size: 12px;\">" );
          out.print( "<a target=\"_blank\" href=\""
              + convertContextPath( GetTransStatusServlet.CONTEXT_PATH ) + "?name="
              + URLEncoder.encode( transName, "UTF-8" ) + "&id=" + URLEncoder.encode( id, "UTF-8" ) + "&xml=y\">"
              + BaseMessages.getString( PKG, "TransStatusServlet.ShowAsXml" ) + "</a>" );
          out.print( "</div>" );
          out.print( "</div>" );
          out.print( "</div>" );
          out.print( "</div>" );

          out.print( "<div class=\"row\" style=\"padding: 0px 0px 75px 0px;\">" );
          out.print( "<div class=\"workspaceHeading\" style=\"padding: 0px 0px 30px 0px;\">Step detail</div>" );
          out.println( "<table class=\"pentaho-table\" border=\"" + tableBorder + "\">" );
          out.print( "<tr class=\"cellTableRow\"> <th class=\"cellTableHeader\">"
              + BaseMessages.getString( PKG, "TransStatusServlet.Stepname" ) + "</th> <th class=\"cellTableHeader\">"
              + BaseMessages.getString( PKG, "TransStatusServlet.CopyNr" ) + "</th> <th class=\"cellTableHeader\">"
              + BaseMessages.getString( PKG, "TransStatusServlet.Read" ) + "</th> <th class=\"cellTableHeader\">"
              + BaseMessages.getString( PKG, "TransStatusServlet.Written" ) + "</th> <th class=\"cellTableHeader\">"
              + BaseMessages.getString( PKG, "TransStatusServlet.Input" ) + "</th> <th class=\"cellTableHeader\">"
              + BaseMessages.getString( PKG, "TransStatusServlet.Output" ) + "</th> <th class=\"cellTableHeader\">"
              + BaseMessages.getString( PKG, "TransStatusServlet.Updated" ) + "</th> <th class=\"cellTableHeader\">"
              + BaseMessages.getString( PKG, "TransStatusServlet.Rejected" ) + "</th> <th class=\"cellTableHeader\">"
              + BaseMessages.getString( PKG, "TransStatusServlet.Errors" ) + "</th> <th class=\"cellTableHeader\">"
              + BaseMessages.getString( PKG, "TransStatusServlet.Active" ) + "</th> <th class=\"cellTableHeader\">"
              + BaseMessages.getString( PKG, "TransStatusServlet.Time" ) + "</th> <th class=\"cellTableHeader\">"
              + BaseMessages.getString( PKG, "TransStatusServlet.Speed" ) + "</th> <th class=\"cellTableHeader\">"
              + BaseMessages.getString( PKG, "TransStatusServlet.prinout" ) + "</th> </tr>" );

          boolean evenRow = true;
          for ( int i = 0; i < trans.nrSteps(); i++ ) {
            StepInterface step = trans.getRunThread( i );
            if ( ( step.isRunning() ) || step.getStatus() != StepExecutionStatus.STATUS_EMPTY ) {
              StepStatus stepStatus = new StepStatus( step );
              boolean snif = false;
              String htmlString = "";
              if ( step.isRunning() && !step.isStopped() && !step.isPaused() ) {
                snif = true;
                String sniffLink =
                    " <a href=\""
                        + convertContextPath( SniffStepServlet.CONTEXT_PATH ) + "?trans="
                        + URLEncoder.encode( transName, "UTF-8" ) + "&id=" + URLEncoder.encode( id, "UTF-8" )
                        + "&lines=50" + "&copynr=" + step.getCopy() + "&type=" + SniffStepServlet.TYPE_OUTPUT
                        + "&step=" + URLEncoder.encode( step.getStepname(), "UTF-8" ) + "\">"
                        + Encode.forHtml( stepStatus.getStepname() ) + "</a>";
                stepStatus.setStepname( sniffLink );
              }

              String rowClass = evenRow ? "cellTableEvenRow" : "cellTableOddRow";
              String cellClass = evenRow ? "cellTableEvenRowCell" : "cellTableOddRowCell";
              htmlString = "<tr class=\"" + rowClass + "\"><td class=\"cellTableCell cellTableFirstColumn " + cellClass + "\">" + stepStatus.getStepname() + "</td>"
                  + "<td class=\"cellTableCell " + cellClass + "\">" + stepStatus.getCopy() + "</td>"
                  + "<td class=\"cellTableCell " + cellClass + "\">" + stepStatus.getLinesRead() + "</td>"
                  + "<td class=\"cellTableCell " + cellClass + "\">" + stepStatus.getLinesWritten() + "</td>"
                  + "<td class=\"cellTableCell " + cellClass + "\">" + stepStatus.getLinesInput() + "</td>"
                  + "<td class=\"cellTableCell " + cellClass + "\">" + stepStatus.getLinesOutput() + "</td>"
                  + "<td class=\"cellTableCell " + cellClass + "\">" + stepStatus.getLinesUpdated() + "</td>"
                  + "<td class=\"cellTableCell " + cellClass + "\">" + stepStatus.getLinesRejected() + "</td>"
                  + "<td class=\"cellTableCell " + cellClass + "\">" + stepStatus.getErrors() + "</td>"
                  + "<td class=\"cellTableCell " + cellClass + "\">" + stepStatus.getStatusDescription() + "</td>"
                  + "<td class=\"cellTableCell " + cellClass + "\">" + stepStatus.getSeconds() + "</td>"
                  + "<td class=\"cellTableCell " + cellClass + "\">" + stepStatus.getSpeed() + "</td>"
                  + "<td class=\"cellTableCell cellTableLastColumn " + cellClass + "\">" + stepStatus.getPriority() + "</td></tr>";
              evenRow = !evenRow;
              out.print( htmlString );
            }
          }
          out.println( "</table>" );
          out.println( "</div>" );

          out.print( "<div class=\"row\" style=\"padding: 0px 0px 75px 0px;\">" );
          out.print( "<div class=\"workspaceHeading\" style=\"padding: 0px 0px 30px 0px;\">Canvas preview</div>" );
          // Get the transformation image
          //
          // out.print("<a href=\"" + convertContextPath(GetTransImageServlet.CONTEXT_PATH) + "?name=" +
          // URLEncoder.encode(transName, "UTF-8") + "&id="+id+"\">"
          // + BaseMessages.getString(PKG, "TransStatusServlet.GetTransImage") + "</a>");
          Point max = trans.getTransMeta().getMaximum();
          max.x += 20;
          max.y += 20;
          out.print( "<iframe height=\""
            + max.y + "\" width=\"" + 875 + "\" seamless src=\""
            + convertContextPath( GetTransImageServlet.CONTEXT_PATH ) + "?name="
            + URLEncoder.encode( transName, "UTF-8" ) + "&id=" + URLEncoder.encode( id, "UTF-8" )
            + "\"></iframe>" );
          out.print( "</div>" );

          // Put the logging below that.
          out.print( "<div class=\"row\" style=\"padding: 0px 0px 30px 0px;\">" );
          out.print( "<div class=\"workspaceHeading\" style=\"padding: 0px 0px 30px 0px;\">Transformation log</div>" );
          out
            .println( "<textarea id=\"translog\" cols=\"120\" rows=\"20\" "
              + "wrap=\"off\" name=\"Transformation log\" readonly=\"readonly\" style=\"height: auto;\">"
              + Encode.forHtml( getLogText( trans, startLineNr, lastLineNr ) ) + "</textarea>" );
          out.print( "</div>" );

          out.println( "<script type=\"text/javascript\">" );
          out.println( "element = document.getElementById( 'statusColor' );" );
          out.println( "if( element.innerHTML == 'Running' || element.innerHTML == 'Finished' ){" );
          out.println( "element.style.color = '#009900';" );
          out.println( "} else if( element.innerHTML == 'Stopped' ) {" );
          out.println( "element.style.color = '#7C0B2B';" );
          out.println( "} else {" );
          out.println( "element.style.color = '#F1C40F';" );
          out.println( "}" );
          out.println( "</script>" );
          out.println( "<script type=\"text/javascript\"> " );
          out.println( "  translog.scrollTop=translog.scrollHeight; " );
          out.println( "</script> " );
        } catch ( Exception ex ) {
          out.println( "<pre>" );
          out.println( Encode.forHtml( Const.getStackTracker( ex ) ) );
          out.println( "</pre>" );
        }

        out.println( "</div>" );
        out.println( "</BODY>" );
        out.println( "</HTML>" );
      }
    } else {
      PrintWriter out = response.getWriter();
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
      }
    }
  }

  public String toString() {
    return "Trans Status Handler";
  }

  public String getService() {
    return CONTEXT_PATH + " (" + toString() + ")";
  }

  public String getContextPath() {
    return CONTEXT_PATH;
  }

  private String getLogText( Trans trans, int startLineNr, int lastLineNr ) throws KettleException {
    try {
      return KettleLogStore.getAppender().getBuffer(
        trans.getLogChannel().getLogChannelId(), false, startLineNr, lastLineNr ).toString();
    } catch ( OutOfMemoryError error ) {
      throw new KettleException( "Log string is too long", error );
    }
  }

}
