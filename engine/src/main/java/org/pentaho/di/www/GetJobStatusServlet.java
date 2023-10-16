/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2023 by Hitachi Vantara : http://www.pentaho.com
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
import org.pentaho.di.job.Job;
import org.pentaho.di.www.cache.CarteStatusCache;
import org.pentaho.di.www.exception.DuplicateKeyException;


public class GetJobStatusServlet extends BaseHttpServlet implements CartePluginInterface {
  private static final String TEXT_XML = "text/xml";
  private static final String HREF = "<a href=\"";
  private static Class<?> PKG = GetJobStatusServlet.class; // for i18n purposes, needed by Translator2!!

  private static final long serialVersionUID = 3634806745372015720L;

  public static final String CONTEXT_PATH = "/kettle/jobStatus";

  private static final byte[] XML_HEADER =
    XMLHandler.getXMLHeader( Const.XML_ENCODING ).getBytes( Charset.forName( Const.XML_ENCODING ) );

  @VisibleForTesting
  CarteStatusCache cache = CarteStatusCache.getInstance();

  public GetJobStatusServlet() {
  }

  public GetJobStatusServlet( JobMap jobMap ) {
    super( jobMap );
  }

  /**
   <div id="mindtouch">
   <h1>/kettle/jobStatus</h1>
   <a name="GET"></a>
   <h2>GET</h2>
   <p>Retrieves status of the specified job.
   Status is returned as HTML or XML output depending on the input parameters.
   Status contains information about last execution of the job.</p>

   <p><b>Example Request:</b><br />
   <pre function="syntax.xml">
   GET /kettle/jobStatus/?name=dummy_job&xml=Y
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
   <td>Name of the job to be used for status generation.</td>
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
   <td>Carte id of the job to be used for status generation.</td>
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
   <p>Response XML or HTML response containing details about the job specified.
   If an error occurs during method invocation <code>result</code> field of the response
   will contain <code>ERROR</code> status.</p>

   <p><b>Example Response:</b></p>
   <pre function="syntax.xml">
   <?xml version="1.0" encoding="UTF-8"?>
   <jobstatus>
   <jobname>dummy_job</jobname>
   <id>a4d54106-25db-41c5-b9f8-73afd42766a6</id>
   <status_desc>Finished</status_desc>
   <error_desc/>
   <logging_string>&#x3c;&#x21;&#x5b;CDATA&#x5b;H4sIAAAAAAAAADMyMDTRNzTUNzRXMDC3MjS2MjJQ0FVIKc3NrYzPyk8CsoNLEotKFPLTFEDc1IrU5NKSzPw8Xi4j4nRm5qUrpOaVFFUqRLuE&#x2b;vpGxhKj0y0zL7M4IzUFYieybgWNotTi0pwS2&#x2b;iSotLUWE1iTPNCdrhCGtRsXi4AOMIbLPwAAAA&#x3d;&#x5d;&#x5d;&#x3e;</logging_string>
   <first_log_line_nr>0</first_log_line_nr>
   <last_log_line_nr>20</last_log_line_nr>
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
   <is_stopped>N</is_stopped>
   <log_channel_id/>
   <log_text>null</log_text>
   <result-file></result-file>
   <result-rows></result-rows>
   </result>
   </jobstatus>
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
   <td>400</td>
   <td>Bad Request: Mandatory parameter name missing</td>
   </tr>
   <tr>
   <td>404</td>
   <td>Not found: Job not found</td>
   </tr>
   <tr>
   <td>409</td>
   <td>Conflicting: multiple jobs with the same name. must provide id</td>
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
      logDebug( BaseMessages.getString( PKG, "GetJobStatusServlet.Log.JobStatusRequested" ) );
    }

    String jobName = request.getParameter( "name" );
    String id = request.getParameter( "id" );
    String root = request.getRequestURI() == null ? StatusServletUtils.PENTAHO_ROOT
      : request.getRequestURI().substring( 0, request.getRequestURI().indexOf( CONTEXT_PATH ) );
    String prefix = isJettyMode() ? StatusServletUtils.STATIC_PATH : root + StatusServletUtils.RESOURCES_PATH;
    boolean useXML = "Y".equalsIgnoreCase( request.getParameter( "xml" ) );
    int numberOfTailLines = Const.toInt( request.getParameter( "tail" ), 0 );
    int startLineNr = Const.toInt( request.getParameter( "from" ), 0 );

    response.setStatus( HttpServletResponse.SC_OK );

    if ( useXML ) {
      response.setContentType( TEXT_XML );
      response.setCharacterEncoding( Const.XML_ENCODING );
    } else {
      response.setContentType( "text/html;charset=UTF-8" );
    }

    //Name is mandatory

    if ( jobName == null ) {
      PrintWriter out = response.getWriter();
      response.setStatus( HttpServletResponse.SC_BAD_REQUEST );
      String message = BaseMessages.getString( PKG, "GetJobStatusServlet.Error.JobNameIsMandatory" );
      printResponse( response, useXML, out, message );
      return;
    }

    // ID is optional...
    //
    Job job;
    CarteObjectEntry entry;
    if ( Utils.isEmpty( id ) ) {
      if ( isConflictingName( jobName ) ) {
        PrintWriter out = response.getWriter();
        response.setStatus( HttpServletResponse.SC_CONFLICT );
        String message = BaseMessages.getString( PKG, "GetJobStatusServlet.Error.ConflictingJobName" );
        printResponse( response, useXML, out, message );
        return;
      }
      // get the first job that matches...
      entry = getJobMap().getFirstCarteObjectEntry( jobName );
      if ( entry == null ) {
        job = null;
      } else {
        id = entry.getId();
        job = getJobMap().getJob( entry );
      }
    } else {
      // Actually, just providing the ID should be enough to identify the job
      //
      if ( Utils.isEmpty( jobName ) ) {
        // Take the ID into account!
        //
        job = getJobMap().findJob( id );
      } else {
        entry = new CarteObjectEntry( jobName, id );
        job = getJobMap().getJob( entry );
        if ( job != null ) {
          jobName = job.getJobname();
        }
      }
    }

    if ( job != null ) {

      if ( useXML ) {
        try {
          OutputStream out = null;
          byte[] data = null;
          String logId = job.getLogChannelId();
          boolean finishedOrStopped = job.isFinished() || job.isStopped();
          if ( finishedOrStopped && ( data = cache.get( logId, startLineNr ) ) != null ) {
            response.setContentLength( XML_HEADER.length + data.length );
            out = response.getOutputStream();
            out.write( XML_HEADER );
            out.write( data );
            out.flush();
          } else {
            int lastLineNr = KettleLogStore.getLastBufferLineNr();
            String logText = getLogText( job, startLineNr, lastLineNr, numberOfTailLines );
/*            if ( numberOfTailLines > 0 ) {
              //Only asking for last numberOfTailLines log lines
              logText = logText.substring( StringUtils.lastOrdinalIndexOf(  logText, "\n", numberOfTailLines + 1 ) + 1 );
            }*/

            response.setContentType( TEXT_XML );
            response.setCharacterEncoding( Const.XML_ENCODING );

            SlaveServerJobStatus jobStatus = new SlaveServerJobStatus( jobName, id, job.getStatus() );
            jobStatus.setFirstLoggingLineNr( startLineNr );
            jobStatus.setLastLoggingLineNr( lastLineNr );
            jobStatus.setLogDate( job.getLogDate() );

            // The log can be quite large at times, we are going to putIfAbsent a base64 encoding around a compressed
            // stream


            // of bytes to handle this one.
            String loggingString = HttpUtil.encodeBase64ZippedString( logText );
            jobStatus.setLoggingString( loggingString );

            // Also set the result object...
            //
            jobStatus.setResult( job.getResult() ); // might be null


            String xml = jobStatus.getXML();
            data = xml.getBytes( Charset.forName( Const.XML_ENCODING ) );
            out = response.getOutputStream();
            response.setContentLength( XML_HEADER.length + data.length );
            out.write( XML_HEADER );
            out.write( data );
            out.flush();
            if ( finishedOrStopped && ( jobStatus.isFinished() || jobStatus.isStopped() ) && logId != null ) {
              cache.put( logId, xml, startLineNr );
            }
          }
          response.flushBuffer();
        } catch ( KettleException e ) {
          response.setStatus( HttpServletResponse.SC_INTERNAL_SERVER_ERROR );
          throw new ServletException( BaseMessages.getString( PKG, "GetJobStatusServlet.Error.UnableToGetJobStatusInXML" ), e );
        }
      } else {

        PrintWriter out = response.getWriter();

        int lastLineNr = KettleLogStore.getLastBufferLineNr();
        int tableBorder = 0;

        response.setContentType( "text/html" );

        out.println( "<HTML>" );
        out.println( "<HEAD>" );
        out
          .println( "<TITLE>"
            + BaseMessages.getString( PKG, "GetJobStatusServlet.KettleJobStatus" ) + "</TITLE>" );
        if ( EnvUtil.getSystemProperty( Const.KETTLE_CARTE_REFRESH_STATUS, "N" ).equalsIgnoreCase( "Y" ) ) {
          out.println( "<META http-equiv=\"Refresh\" content=\"10;url="
            + convertContextPath( GetJobStatusServlet.CONTEXT_PATH ) + "?name="
            + URLEncoder.encode( Const.NVL( jobName, "" ), "UTF-8" ) + "&id=" + URLEncoder.encode( id, "UTF-8" )
            + "\">" );
        }
        out.println( "<META http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">" );
        if ( isJettyMode() ) {
          out.println( "<link rel=\"stylesheet\" type=\"text/css\" href=\"/static/css/carte.css\" />" );
        } else {
          out.print( StatusServletUtils.getPentahoStyles( request.getSession().getServletContext(),  root ) );
        }
        out.println( "</HEAD>" );
        out.println( "<BODY style=\"overflow: auto;\">" );
        out.println( "<div class=\"row\" id=\"pucHeader\">" );
        out.println( "<div class=\"workspaceHeading\" style=\"padding: 0px 0px 0px 10px;\">" + Encode.forHtml( BaseMessages.getString( PKG, "GetJobStatusServlet.JobStatus", jobName ) ) + "</div>" );
        out.println( "</div>" );

        try {
          out.println( "<div class=\"row\" style=\"padding: 0px 0px 0px 30px\">" );
          out.println( "<div class=\"row\" style=\"padding-top: 30px;\">" );
          out.print( HREF + convertContextPath( GetStatusServlet.CONTEXT_PATH ) + "\">" );
          out.print( "<img src=\"" + prefix + "/images/back.svg\" style=\"margin-right: 5px; width: 16px; height: 16px; vertical-align: middle;\">" );
          out.print( BaseMessages.getString( PKG, "CarteStatusServlet.BackToCarteStatus" ) + "</a>" );
          out.println( "</div>" );
          out.println( "<div class=\"row\" style=\"padding: 30px 0px 75px 0px; display: table;\">" );
          out.println( "<div style=\"display: table-row;\">" );
          out.println( "<div style=\"padding: 0px 30px 0px 0px; width: 60px; display: table-cell; vertical-align: top;\">" );
          out.println( "<img src=\"" + prefix + "/images/job.svg\" style=\"width: 60px; height: 60px;\"></img>" );
          out.println( "</div>" );
          out.println( "<div style=\"vertical-align: top; display: table-cell;\">" );
          out.println( "<table style=\"border-collapse: collapse;\" border=\"" + tableBorder + "\">" );
          out.print( "<tr class=\"cellTableRow\" style=\"border: solid; border-width: 1px 0; border-top: none; border-color: #E3E3E3; font-size: 12; text-align: left;\"> <th style=\"font-weight: normal; padding: 8px 10px 10px 10px\" class=\"cellTableHeader\">"
            + BaseMessages.getString( PKG, "TransStatusServlet.CarteObjectId" ) + "</th> <th style=\"font-weight: normal; padding: 8px 10px 10px 10px\" class=\"cellTableHeader\">"
            + BaseMessages.getString( PKG, "TransStatusServlet.TransStatus" ) + "</th> <th style=\"font-weight: normal; padding: 8px 10px 10px 10px\" class=\"cellTableHeader\">"
            + BaseMessages.getString( PKG, "TransStatusServlet.LastLogDate" ) + "</th> </tr>" );
          out.print( "<tr class=\"cellTableRow\" style=\"border: solid; border-width: 1px 0; border-bottom: none; font-size: 12; text-align:left\">" );
          out.print( "<td style=\"padding: 8px 10px 10px 10px\" class=\"cellTableCell cellTableFirstColumn\">" + Const.NVL( Encode.forHtml( id ), "" ) + "</td>" );
          out.print( "<td style=\"padding: 8px 10px 10px 10px\" class=\"cellTableCell\" id=\"statusColor\" style=\"font-weight: bold;\">" + job.getStatus() + "</td>" );
          String dateStr = XMLHandler.date2string( job.getLogDate() );
          dateStr = dateStr == null ? "-" : dateStr.substring( 0, dateStr.indexOf( ' ' ) );
          out.print( "<td style=\"padding: 8px 10px 10px 10px\" class=\"cellTableCell cellTableLastColumn\">" + dateStr + "</td>" );
          out.print( "</tr>" );
          out.print( "</table>" );
          out.print( "</div>" );
          out.println( "<div style=\"padding: 0px 0px 0px 20px; width: 90px; display: table-cell; vertical-align: top;\">" );
          out.print( "<div style=\"display: block; margin-left: auto; margin-right: auto; padding: 5px 0px;\">" );
          out.print( "<a target=\"_blank\" href=\""
            + convertContextPath( GetJobStatusServlet.CONTEXT_PATH ) + "?name="
            + URLEncoder.encode( jobName, "UTF-8" ) + "&id=" + URLEncoder.encode( id, "UTF-8" ) + "&xml=y\">"
            + "<img src=\"" + prefix + "/images/view-as-xml.svg\" style=\"display: block; margin: auto; width: 22px; height: 22px;\"></a>" );
          out.print( "</div>" );
          out.println( "<div style=\"text-align: center; padding-top: 12px; font-size: 12px;\">" );
          out.print( "<a target=\"_blank\" href=\""
              + convertContextPath( GetJobStatusServlet.CONTEXT_PATH ) + "?name="
              + URLEncoder.encode( jobName, "UTF-8" ) + "&id=" + URLEncoder.encode( id, "UTF-8" ) + "&xml=y\">"
              + BaseMessages.getString( PKG, "TransStatusServlet.ShowAsXml" ) + "</a>" );
          out.print( "</div>" );
          out.print( "</div>" );
          out.print( "</div>" );
          out.print( "</div>" );

          out.print( "<div class=\"row\" style=\"padding: 0px 0px 75px 0px;\">" );
          out.print( "<div class=\"workspaceHeading\">Canvas preview</div>" );
          // Show job image?
          //
          Point max = job.getJobMeta().getMaximum();
          //max.x += 20;
          max.y += 20;
          out
            .print( "<iframe height=\""
              + max.y + "\" width=\"" + 875 + "\" seamless src=\""
              + convertContextPath( GetJobImageServlet.CONTEXT_PATH ) + "?name="
              + URLEncoder.encode( jobName, "UTF-8" ) + "&id=" + URLEncoder.encode( id, "UTF-8" )
              + "\"></iframe>" );
          out.print( "</div>" );

          // Put the logging below that.

          out.print( "<div class=\"row\" style=\"padding: 0px 0px 30px 0px;\">" );
          out.print( "<div class=\"workspaceHeading\">Job log</div>" );
          out.println( "<textarea id=\"joblog\" cols=\"120\" rows=\"20\" wrap=\"off\" "
              + "name=\"Job log\" readonly=\"readonly\" style=\"height: auto;\">"
              + Encode.forHtml( getLogText( job, startLineNr, lastLineNr, numberOfTailLines ) ) + "</textarea>" );
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
          out.println( "  joblog.scrollTop=joblog.scrollHeight; " );
          out.println( "</script> " );
        } catch ( Exception ex ) {
          response.setStatus( HttpServletResponse.SC_INTERNAL_SERVER_ERROR );
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
      response.setStatus( HttpServletResponse.SC_NOT_FOUND );
      String message = BaseMessages.getString( PKG, "StartJobServlet.Log.SpecifiedJobNotFound", jobName, id );
      if ( useXML ) {
        out.println( new WebResult( WebResult.STRING_ERROR, message ) );
      } else {
        out.println( "<H1>" + Encode.forHtml( message ) + "</H1>" );
        out.println( HREF
          + convertContextPath( GetStatusServlet.CONTEXT_PATH ) + "\">"
          + BaseMessages.getString( PKG, "JobStatusServlet.BackToStatusPage" ) + "</a><p>" );
      }
    }
  }

  private void printResponse( HttpServletResponse response, boolean useXML, PrintWriter out, String message ) {
    if ( useXML ) {
      response.setContentType( TEXT_XML );
      response.setCharacterEncoding( Const.XML_ENCODING );
      out.print( XMLHandler.getXMLHeader( Const.XML_ENCODING ) );
      out.println( new WebResult( WebResult.STRING_ERROR, message ) );
    } else {
      String h1End = "</H1>";
      String h1 = "<H1>";
      String hrefEnd = "</a><p>";
      response.setContentType( "text/html;charset=UTF-8" );
      out.println( "<HTML>" );
      out.println( "<HEAD>" );
      out.println( "<TITLE>Start job</TITLE>" );
      out.println( "<META http-equiv=\"Refresh\" content=\"2;url="
        + convertContextPath( GetStatusServlet.CONTEXT_PATH ) + "\">" );
      out.println( "<META http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">" );
      out.println( "</HEAD>" );
      out.println( "<BODY>" );
      out.println( h1 + Encode.forHtml( message ) + h1End );
      out.println( HREF
        + convertContextPath( GetStatusServlet.CONTEXT_PATH ) + "\">"
        + BaseMessages.getString( PKG, "JobStatusServlet.BackToStatusPage" ) + hrefEnd );
      out.println( "</BODY>" );
      out.println( "</HTML>" );
    }
  }

  public String toString() {
    return "Job Status Handler";
  }

  public String getService() {
    return CONTEXT_PATH + " (" + toString() + ")";
  }

  public String getContextPath() {
    return CONTEXT_PATH;
  }

  private String getLogText( Job job, int startLineNr, int lastLineNr, int numberOfTailLines ) throws KettleException {
    try {
      return KettleLogStore.getAppender().getBuffer(
        job.getLogChannel().getLogChannelId(), false, startLineNr, lastLineNr, numberOfTailLines ).toString();
    } catch ( OutOfMemoryError error ) {
      throw new KettleException( BaseMessages.getString( PKG, "GetJobStatusServlet.Error.LogStringIsTooLong" ) );
    }
  }

  private boolean isConflictingName( String jobName ) {
    try {
      getJobMap().getUniqueCarteObjectEntry( jobName );
    } catch ( DuplicateKeyException ex ) {
      return true;
    }
    return false;
  }
}
