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
            if ( finishedOrStopped && logId != null && !dontUseCache ) {
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

        response.setContentType( "text/html;charset=UTF-8" );

        out.println( "<HTML>" );
        out.println( "<HEAD>" );
        out.println( "<TITLE>"
          + BaseMessages.getString( PKG, "TransStatusServlet.KettleTransStatus" ) + "</TITLE>" );
        out.println( "<META http-equiv=\"Refresh\" content=\"10;url="
          + convertContextPath( CONTEXT_PATH ) + "?name=" + URLEncoder.encode( transName, "UTF-8" ) + "&id="
          + URLEncoder.encode( id, "UTF-8" ) + "\">" );
        out.println( "<META http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">" );
        out.println( "</HEAD>" );
        out.println( "<BODY>" );
        out.println( "<H1>"
          + Encode.forHtml( BaseMessages.getString( PKG, "TransStatusServlet.TopTransStatus", transName ) )
          + "</H1>" );

        try {
          out.println( "<table border=\"1\">" );
          out.print( "<tr> <th>"
            + BaseMessages.getString( PKG, "TransStatusServlet.TransName" ) + "</th> <th>"
            + BaseMessages.getString( PKG, "TransStatusServlet.CarteObjectId" ) + "</th> <th>"
            + BaseMessages.getString( PKG, "TransStatusServlet.TransStatus" ) + "</th> </tr>" );

          out.print( "<tr>" );
          out.print( "<td>" + Encode.forHtml( transName ) + "</td>" );
          out.print( "<td>" + Encode.forHtml( id ) + "</td>" );
          out.print( "<td>" + Encode.forHtml( trans.getStatus() ) + "</td>" );
          out.print( "</tr>" );
          out.print( "</table>" );

          out.print( "<p>" );

          // Get the transformation image
          //
          // out.print("<a href=\"" + convertContextPath(GetTransImageServlet.CONTEXT_PATH) + "?name=" +
          // URLEncoder.encode(transName, "UTF-8") + "&id="+id+"\">"
          // + BaseMessages.getString(PKG, "TransStatusServlet.GetTransImage") + "</a>");
          Point max = trans.getTransMeta().getMaximum();
          max.x += 20;
          max.y += 20;
          out.print( "<iframe height=\""
            + max.y + "\" width=\"" + max.x + "\" seamless src=\""
            + convertContextPath( GetTransImageServlet.CONTEXT_PATH ) + "?name="
            + URLEncoder.encode( transName, "UTF-8" ) + "&id=" + URLEncoder.encode( id, "UTF-8" )
            + "\"></iframe>" );
          out.print( "<p>" );

          if ( ( trans.isFinished() && trans.isRunning() )
            || ( !trans.isRunning() && !trans.isPreparing() && !trans.isInitializing() ) ) {
            out.print( "<a href=\""
              + convertContextPath( StartTransServlet.CONTEXT_PATH ) + "?name="
              + URLEncoder.encode( transName, "UTF-8" ) + "&id=" + URLEncoder.encode( id, "UTF-8" ) + "\">"
              + BaseMessages.getString( PKG, "TransStatusServlet.StartTrans" ) + "</a>" );
            out.print( "<p>" );
            out.print( "<a href=\""
              + convertContextPath( PrepareExecutionTransServlet.CONTEXT_PATH ) + "?name="
              + URLEncoder.encode( transName, "UTF-8" ) + "&id=" + URLEncoder.encode( id, "UTF-8" ) + "\">"
              + BaseMessages.getString( PKG, "TransStatusServlet.PrepareTrans" ) + "</a><br>" );
          } else if ( trans.isRunning() ) {
            out.print( "<a href=\""
              + convertContextPath( PauseTransServlet.CONTEXT_PATH ) + "?name="
              + URLEncoder.encode( transName, "UTF-8" ) + "&id=" + URLEncoder.encode( id, "UTF-8" ) + "\">"
              + BaseMessages.getString( PKG, "PauseStatusServlet.PauseResumeTrans" ) + "</a><br>" );
            out.print( "<a href=\""
              + convertContextPath( StopTransServlet.CONTEXT_PATH ) + "?name="
              + URLEncoder.encode( transName, "UTF-8" ) + "&id=" + URLEncoder.encode( id, "UTF-8" ) + "\">"
              + BaseMessages.getString( PKG, "TransStatusServlet.StopTrans" ) + "</a>" );
            out.print( "<p>" );
          }
          out.print( "<a href=\""
            + convertContextPath( CleanupTransServlet.CONTEXT_PATH ) + "?name="
            + URLEncoder.encode( transName, "UTF-8" ) + "&id=" + URLEncoder.encode( id, "UTF-8" ) + "\">"
            + BaseMessages.getString( PKG, "TransStatusServlet.CleanupTrans" ) + "</a>" );
          out.print( "<p>" );

          out.println( "<table border=\"1\">" );
          out.print( "<tr> <th>"
            + BaseMessages.getString( PKG, "TransStatusServlet.Stepname" ) + "</th> <th>"
            + BaseMessages.getString( PKG, "TransStatusServlet.CopyNr" ) + "</th> <th>"
            + BaseMessages.getString( PKG, "TransStatusServlet.Read" ) + "</th> <th>"
            + BaseMessages.getString( PKG, "TransStatusServlet.Written" ) + "</th> <th>"
            + BaseMessages.getString( PKG, "TransStatusServlet.Input" ) + "</th> <th>"
            + BaseMessages.getString( PKG, "TransStatusServlet.Output" ) + "</th> " + "<th>"
            + BaseMessages.getString( PKG, "TransStatusServlet.Updated" ) + "</th> <th>"
            + BaseMessages.getString( PKG, "TransStatusServlet.Rejected" ) + "</th> <th>"
            + BaseMessages.getString( PKG, "TransStatusServlet.Errors" ) + "</th> <th>"
            + BaseMessages.getString( PKG, "TransStatusServlet.Active" ) + "</th> <th>"
            + BaseMessages.getString( PKG, "TransStatusServlet.Time" ) + "</th> " + "<th>"
            + BaseMessages.getString( PKG, "TransStatusServlet.Speed" ) + "</th> <th>"
            + BaseMessages.getString( PKG, "TransStatusServlet.prinout" ) + "</th> </tr>" );

          for ( int i = 0; i < trans.nrSteps(); i++ ) {
            StepInterface step = trans.getRunThread( i );
            if ( ( step.isRunning() ) || step.getStatus() != StepExecutionStatus.STATUS_EMPTY ) {
              StepStatus stepStatus = new StepStatus( step );
              boolean snif = false;
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

              out.print( stepStatus.getHTMLTableRow( snif ) );
            }
          }
          out.println( "</table>" );
          out.println( "<p>" );

          out.print( "<a href=\""
            + convertContextPath( GetTransStatusServlet.CONTEXT_PATH ) + "?name="
            + URLEncoder.encode( transName, "UTF-8" ) + "&id=" + URLEncoder.encode( id, "UTF-8" ) + "&xml=y\">"
            + BaseMessages.getString( PKG, "TransStatusServlet.ShowAsXml" ) + "</a><br>" );
          out.print( "<a href=\""
            + convertContextPath( GetStatusServlet.CONTEXT_PATH ) + "\">"
            + BaseMessages.getString( PKG, "TransStatusServlet.BackToStatusPage" ) + "</a><br>" );
          out.print( "<p><a href=\""
            + convertContextPath( GetTransStatusServlet.CONTEXT_PATH ) + "?name="
            + URLEncoder.encode( transName, "UTF-8" ) + "&id=" + URLEncoder.encode( id, "UTF-8" ) + "\">"
            + BaseMessages.getString( PKG, "TransStatusServlet.Refresh" ) + "</a>" );

          // Put the logging below that.

          out.println( "<p>" );
          out
            .println( "<textarea id=\"translog\" cols=\"120\" rows=\"20\" "
              + "wrap=\"off\" name=\"Transformation log\" readonly=\"readonly\">"
              + Encode.forHtml( getLogText( trans, startLineNr, lastLineNr ) ) + "</textarea>" );

          out.println( "<script type=\"text/javascript\"> " );
          out.println( "  translog.scrollTop=translog.scrollHeight; " );
          out.println( "</script> " );
          out.println( "<p>" );
        } catch ( Exception ex ) {
          out.println( "<p>" );
          out.println( "<pre>" );
          out.println( Encode.forHtml( Const.getStackTracker( ex ) ) );
          out.println( "</pre>" );
        }

        out.println( "<p>" );
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
