/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2022 by Hitachi Vantara : http://www.pentaho.com
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
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.Job;
import org.pentaho.di.trans.Trans;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings( {"squid:S1192", "squid:S1075" } ) // suppress warnings related to dup'd strings & non-config path
public class GetStatusServlet extends BaseHttpServlet implements CartePluginInterface {
  private static final Class<?> PKG = GetStatusServlet.class; // for i18n purposes, needed by Translator2!!

  private static final long serialVersionUID = 3634806745372015720L;

  public static final String CONTEXT_PATH = "/kettle/status";

  public GetStatusServlet() {
  }

  public GetStatusServlet( TransformationMap transformationMap, JobMap jobMap ) {
    super( transformationMap, jobMap );
  }

  /**
   <div id="mindtouch">
   <h1>/kettle/status</h1>
   <a name="GET"></a>
   <h2>GET</h2>
   <p>Retrieve server status. The status contains information about the server itself (OS, memory, etc)
   and information about jobs and transformations present on the server.</p>

   <p><b>Example Request:</b><br />
   <pre function="syntax.xml">
   GET /kettle/status/?xml=Y
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
   <td>Boolean flag which defines output format <code>Y</code> forces XML output to be generated.
   HTML is returned otherwise.</td>
   <td>boolean, optional</td>
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
   <p>Response XML or HTML response containing details about the transformation specified.
   If an error occurs during method invocation <code>result</code> field of the response
   will contain <code>ERROR</code> status.</p>

   <p><b>Example Response:</b></p>
   <pre function="syntax.xml">
   <?xml version="1.0" encoding="UTF-8"?>
   <serverstatus>
   <statusdesc>Online</statusdesc>
   <memory_free>229093440</memory_free>
   <memory_total>285736960</memory_total>
   <cpu_cores>4</cpu_cores>
   <cpu_process_time>7534848300</cpu_process_time>
   <uptime>68818403</uptime>
   <thread_count>45</thread_count>
   <load_avg>-1.0</load_avg>
   <os_name>Windows 7</os_name>
   <os_version>6.1</os_version>
   <os_arch>amd64</os_arch>
   <transstatuslist>
   <transstatus>
   <transname>Row generator test</transname>
   <id>56c93d4e-96c1-4fae-92d9-d864b0779845</id>
   <status_desc>Waiting</status_desc>
   <error_desc/>
   <paused>N</paused>
   <stepstatuslist>
   </stepstatuslist>
   <first_log_line_nr>0</first_log_line_nr>
   <last_log_line_nr>0</last_log_line_nr>
   <logging_string>&#x3c;&#x21;&#x5b;CDATA&#x5b;&#x5d;&#x5d;&#x3e;</logging_string>
   </transstatus>
   <transstatus>
   <transname>dummy-trans</transname>
   <id>c56961b2-c848-49b8-abde-76c8015e29b0</id>
   <status_desc>Stopped</status_desc>
   <error_desc/>
   <paused>N</paused>
   <stepstatuslist>
   </stepstatuslist>
   <first_log_line_nr>0</first_log_line_nr>
   <last_log_line_nr>0</last_log_line_nr>
   <logging_string>&#x3c;&#x21;&#x5b;CDATA&#x5b;&#x5d;&#x5d;&#x3e;</logging_string>
   </transstatus>
   </transstatuslist>
   <jobstatuslist>
   <jobstatus>
   <jobname>dummy_job</jobname>
   <id>abd61143-8174-4f27-9037-6b22fbd3e229</id>
   <status_desc>Stopped</status_desc>
   <error_desc/>
   <logging_string>&#x3c;&#x21;&#x5b;CDATA&#x5b;&#x5d;&#x5d;&#x3e;</logging_string>
   <first_log_line_nr>0</first_log_line_nr>
   <last_log_line_nr>0</last_log_line_nr>
   </jobstatus>
   </jobstatuslist>
   </serverstatus>
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
    String root = request.getRequestURI() == null ? StatusServletUtils.PENTAHO_ROOT
      : request.getRequestURI().substring( 0, request.getRequestURI().indexOf( CONTEXT_PATH ) );
    String prefix = isJettyMode() ? StatusServletUtils.STATIC_PATH : root + StatusServletUtils.RESOURCES_PATH;
    prefix = encodeUriComponents( prefix );
    root = encodeUriComponents( root );
    boolean useXML = "Y".equalsIgnoreCase( request.getParameter( "xml" ) );
    boolean useLightTheme = "Y".equalsIgnoreCase( request.getParameter( "useLightTheme" ) );

    if ( useXML ) {
      response.setContentType( "text/xml" );
      response.setCharacterEncoding( Const.XML_ENCODING );
    } else {
      response.setContentType( "text/html;charset=UTF-8" );
    }

    PrintWriter out = response.getWriter();

    List<CarteObjectEntry> transEntries = getTransformationMap().getTransformationObjects();
    List<CarteObjectEntry> jobEntries = getJobMap().getJobObjects();

    if ( useXML ) {
      out.print( XMLHandler.getXMLHeader( Const.XML_ENCODING ) );
      SlaveServerStatus serverStatus = new SlaveServerStatus();
      serverStatus.setStatusDescription( "Online" );

      getSystemInfo( serverStatus );

      for ( CarteObjectEntry entry : transEntries ) {
        Trans trans = getTransformationMap().getTransformation( entry );
        if ( trans != null ) {
          String status = trans.getStatus();
          SlaveServerTransStatus sstatus = new SlaveServerTransStatus( entry.getName(), entry.getId(), status );
          sstatus.setLogDate( trans.getLogDate() );
          sstatus.setPaused( trans.isPaused() );
          serverStatus.getTransStatusList().add( sstatus );
        }
      }

      for ( CarteObjectEntry entry : jobEntries ) {
        Job job = getJobMap().getJob( entry );
        if ( job != null ) {
          String status = job.getStatus();
          SlaveServerJobStatus jobStatus = new SlaveServerJobStatus( entry.getName(), entry.getId(), status );
          jobStatus.setLogDate( job.getLogDate() );
          serverStatus.getJobStatusList().add( jobStatus );
        }
      }

      try {
        out.println( serverStatus.getXML() );
      } catch ( KettleException e ) {
        throw new ServletException( "Unable to get the server status in XML format", e );
      }
    } else {
      out.println( "<HTML>" );
      out.println( "<HEAD><TITLE>"
        + BaseMessages.getString( PKG, "GetStatusServlet.KettleSlaveServerStatus" ) + "</TITLE>" );
      out.println( "<META http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">" );

      int tableBorder = 1;
      if ( !useLightTheme ) {
        if ( isJettyMode() ) {
          out.println( "<link rel=\"stylesheet\" type=\"text/css\" href=\"/static/css/carte.css\" />" );
        } else {
          out.print( StatusServletUtils.getPentahoStyles( request.getSession().getServletContext(),  root ) );
          out.println( "<style>" );
          out.println(
            ".pentaho-table td, tr.cellTableRow, td.gwt-MenuItem, .toolbar-button:not(.toolbar-button-disabled) {" );
          out.println( "  cursor: pointer;" );
          out.println( "}" );
          out.println( ".toolbar-button-disabled {" );
          out.println( "  opacity: 0.4;" );
          out.println( "}" );
          out.println( "div#messageDialogBody:first-letter {" );
          out.println( "  text-transform: capitalize;" );
          out.println( "}" );
          out.println( "</style>" );
        }
        tableBorder = 0;
      }

      out.println( "</HEAD>" );
      out.println(
        "<BODY class=\"pentaho-page-background dragdrop-dropTarget dragdrop-boundary\" style=\"overflow: auto;\">" );

      // Empty div for containing currently selected item
      out.println( "<div id=\"selectedTableItem\">" );
      out.println( "<value></value>" ); //initialize to none
      out.println( "</div>" );

      out.println( "<div class=\"row\" id=\"pucHeader\">" );

      String htmlClass = useLightTheme ? "h1" : "div";
      out.println( "<" + htmlClass + " class=\"workspaceHeading\" style=\"padding: 0px 0px 0px 10px;\">" + BaseMessages
        .getString( PKG, "GetStatusServlet.TopStatus" ) + "</" + htmlClass + ">" );
      out.println( "</div>" );

      // Tooltips
      String run = BaseMessages.getString( PKG, "CarteStatusServlet.Run" );
      String stop = BaseMessages.getString( PKG, "CarteStatusServlet.StopTrans" );
      String view = BaseMessages.getString( PKG, "CarteStatusServlet.ViewTransDetails" );
      String remove = BaseMessages.getString( PKG, "CarteStatusServlet.RemoveTrans" );
      String runJ = BaseMessages.getString( PKG, "CarteStatusServlet.Run" );
      String stopJ = BaseMessages.getString( PKG, "CarteStatusServlet.StopJob" );
      String viewJ = BaseMessages.getString( PKG, "CarteStatusServlet.ViewJobDetails" );
      String removeJ = BaseMessages.getString( PKG, "CarteStatusServlet.RemoveJob" );

      try {
        out.println( "<div class=\"row\" style=\"padding: 0px 0px 0px 30px\">" );
        htmlClass = useLightTheme ? "h2" : "div";
        out.println( "<div class=\"row\" style=\"padding: 25px 30px 75px 0px;\">" );
        out.println(
          "<" + htmlClass + " class=\"workspaceHeading\" style=\"padding: 0px 0px 0px 0px;\">Transformations</"
            + htmlClass + ">" );
        out.println(
          "<table id=\"trans-table\" cellspacing=\"0\" cellpadding=\"0\"><tbody><tr><td align=\"left\" width=\"100%\""
            + " style=\"vertical-align:middle;\">" );
        out.println(
          "<table cellspacing=\"0\" cellpadding=\"0\" class=\"toolbar\" style=\"width: 100%; height: 26px; "
            + "margin-bottom: 2px; border: 0;\">" );
        out.println( "<tbody><tr>" );
        out.println( "<td align=\"left\" style=\"vertical-align: middle; width: 100%\" id=\"trans-align\"></td>" );
        out.println( "<td " + setupIconEnterLeaveJavascript( "run-pause" )
          + " align=\"left\" style=\"vertical-align: middle;\"><div style=\"padding: 2px;\" "
          + "onClick=\"runPauseFunction( this )\" class=\"toolbar-button toolbar-button-disabled\" "
          + "id=\"run-pause\"><img style=\"width: 22px; height: 22px\" src=\""
          + prefix + "/images/run.svg\" title=\"" + run + "\"/></div></td>" );
        out.println( "<td " + setupIconEnterLeaveJavascript( "stop" )
          + " align=\"left\" style=\"vertical-align: middle;\"><div style=\"padding: 2px;\" onClick=\"stopFunction( "
          + "this )\" class=\"toolbar-button toolbar-button-disabled\" id=\"stop\"><img style=\"width: 22px; height: "
          + "22px\"src=\""
          + prefix + "/images/stop.svg\" title=\"" + stop + "\"/></div></td>" );
        out.println( "<td " + setupIconEnterLeaveJavascript( "view" )
          + " align=\"left\" style=\"vertical-align: middle;\"><div style=\"padding: 2px;\" onClick=\"viewFunction( "
          + "this )\" class=\"toolbar-button toolbar-button-disabled\" id=\"view\"><img style=\"width: 22px; height: "
          + "22px\" src=\""
          + prefix + "/images/view.svg\" title=\"" + view + "\"/></div></td>" );
        out.println( "<td " + setupIconEnterLeaveJavascript( "close" )
          + " align=\"left\" style=\"vertical-align: middle;\"><div style=\"padding: 2px; margin-right: 10px;\" "
          + "onClick=\"removeFunction( this )\" class=\"toolbar-button toolbar-button-disabled\" id=\"close\"><img "
          + "style=\"width: 22px; height: 22px\" src=\""
          + prefix + "/images/close.svg\" title=\"" + remove + "\"/></div></td>" );
        out.println( "</tr></tbody></table>" );
        out.println(
          "<div id=\"stopActions\" class=\"custom-dropdown-popup\" style=\"visibility: hidden; overflow: visible; "
            + "position: fixed;\" onLoad=\"repositionActions( this, document.getElementById( 'stop' ) )\" "
            + "onMouseLeave=\"this.style='visibility: hidden; overflow: visible; position: fixed;'\"><div "
            + "class=\"popupContent\"><div style=\"padding: 0;\" class=\"gwt-MenuBar "
            + "gwt-MenuBar-vertical\"><table><tbody><tr><td class=\"gwt-MenuItem\" onClick=\"stopTransSelector( this "
            + ")\" onMouseEnter=\"this.className='gwt-MenuItem gwt-MenuItem-selected'\" onMouseLeave=\"this"
            + ".className='gwt-MenuItem'\">Stop transformation</td></tr><tr><td class=\"gwt-MenuItem\" "
            + "onClick=\"stopTransSelector( this )\" onMouseEnter=\"this.className='gwt-MenuItem "
            + "gwt-MenuItem-selected'\" onMouseLeave=\"this.className='gwt-MenuItem'\">Stop input "
            + "processing</td></tr></tbody></table></div></div></div>" );
        out.println( messageDialog() );
        out.println( "<table class=\"pentaho-table\" border=\"" + tableBorder + "\">" );
        out.print( "<tr> <th class=\"cellTableHeader\">"
          + BaseMessages.getString( PKG, "GetStatusServlet.TransName" ) + "</th> <th class=\"cellTableHeader\">"
          + BaseMessages.getString( PKG, "GetStatusServlet.CarteId" ) + "</th> <th class=\"cellTableHeader\">"
          + BaseMessages.getString( PKG, "GetStatusServlet.Status" ) + "</th> <th class=\"cellTableHeader\">"
          + BaseMessages.getString( PKG, "GetStatusServlet.LastLogDate" ) + "</th> <th class=\"cellTableHeader\">"
          + BaseMessages.getString( PKG, "GetStatusServlet.LastLogTime" ) + "</th> </tr>" );

        Comparator<CarteObjectEntry> transComparator = ( o1, o2 ) -> {
          Trans t1 = getTransformationMap().getTransformation( o1 );
          Trans t2 = getTransformationMap().getTransformation( o2 );
          // If transformations are null because they were removed from the map, sort them to end of list
          if ( t1 == null && t2 == null ) {
            return 0;
          }
          if ( t1 == null ) {
            return 1;
          }
          if ( t2 == null ) {
            return -1;
          }
          Date d1 = t1.getLogDate();
          Date d2 = t2.getLogDate();
          // if both transformations have last log date, desc sort by log date
          if ( d1 != null && d2 != null ) {
            int logDateCompare = d2.compareTo( d1 );
            if ( logDateCompare != 0 ) {
              return logDateCompare;
            }
          }
          return o1.compareTo( o2 );
        };

        Collections.sort( transEntries, transComparator );

        boolean evenRow = true;
        for ( int i = 0; i < transEntries.size(); i++ ) {
          String name = Encode.forHtml( transEntries.get( i ).getName() );
          String id = Encode.forHtml( transEntries.get( i ).getId() );
          Trans trans = getTransformationMap().getTransformation( transEntries.get( i ) );
          if ( trans != null ) {
            String status = Encode.forHtml( trans.getStatus() );
            String trClass = evenRow ? "cellTableEvenRow" : "cellTableOddRow"; // alternating row color
            String tdClass = evenRow ? "cellTableEvenRowCell" : "cellTableOddRowCell";
            evenRow = !evenRow; // flip
            out.print( "<tr onMouseEnter=\"mouseEnterFunction( this, '" + trClass + "' )\" "
              + "onMouseLeave=\"mouseLeaveFunction( this, '" + trClass + "' )\" "
              + "onClick=\"clickFunction( this, '" + trClass + "' )\" "
              + "id=\"cellTableRow_" + i + "\" class=\"" + trClass + "\">" );
            out.print( "<td onMouseEnter=\"mouseEnterFunction( this, '" + tdClass + "' )\" "
              + "onMouseLeave=\"mouseLeaveFunction( this, '" + tdClass + "' )\" "
              + "onClick=\"clickFunction( this, '" + tdClass + "' )\" "
              + "id=\"cellTableFirstCell_" + i + "\" class=\"cellTableCell cellTableFirstColumn " + tdClass + "\">"
              + name
              + "</td>" );
            out.print( "<td onMouseEnter=\"mouseEnterFunction( this, '" + tdClass + "' )\" "
              + "onMouseLeave=\"mouseLeaveFunction( this, '" + tdClass + "' )\" "
              + "onClick=\"clickFunction( this, '" + tdClass + "' )\" "
              + "id=\"cellTableCell_" + i + "\" class=\"cellTableCell " + tdClass + "\">" + id + "</td>" );
            out.print( "<td onMouseEnter=\"mouseEnterFunction( this, '" + tdClass + "' )\" "
              + "onMouseLeave=\"mouseLeaveFunction( this, '" + tdClass + "' )\" "
              + "onClick=\"clickFunction( this, '" + tdClass + "' )\" "
              + "id=\"cellTableCellStatus_" + i + "\" class=\"cellTableCell " + tdClass + "\">" + status + "</td>" );
            String dateStr = XMLHandler.date2string( trans.getLogDate() );
            out.print( "<td onMouseEnter=\"mouseEnterFunction( this, '" + tdClass + "' )\" "
              + "onMouseLeave=\"mouseLeaveFunction( this, '" + tdClass + "' )\" "
              + "onClick=\"clickFunction( this, '" + tdClass + "' )\" "
              + "id=\"cellTableCell_" + i + "\" class=\"cellTableCell " + tdClass + "\">"
              + ( trans.getLogDate() == null ? "-" : dateStr.substring( 0, dateStr.indexOf( ' ' ) ) ) + "</td>" );
            out.print( "<td onMouseEnter=\"mouseEnterFunction( this, '" + tdClass + "' )\" "
              + "onMouseLeave=\"mouseLeaveFunction( this, '" + tdClass + "' )\" "
              + "onClick=\"clickFunction( this, '" + tdClass + "' )\" "
              + "id=\"cellTableLastCell_" + i + "\" class=\"cellTableCell cellTableLastColumn " + tdClass + "\">"
              + ( trans.getLogDate() == null ? "-" : dateStr.substring( dateStr.indexOf( ' ' ), dateStr.length() ) )
              + "</td>" );
            out.print( "</tr>" );
          }
        }
        out.print( "</table></table>" );
        out.print( "</div>" ); // end div

        out.println( "<div class=\"row\" style=\"padding: 0px 30px 75px 0px;\">" );
        out.println(
          "<" + htmlClass + " class=\"workspaceHeading\" style=\"padding: 0px 0px 0px 0px;\">Jobs</" + htmlClass
            + ">" );
        out.println(
          "<table cellspacing=\"0\" cellpadding=\"0\"><tbody><tr><td align=\"left\" width=\"100%\" "
            + "style=\"vertical-align:middle;\">" );
        out.println(
          "<table cellspacing=\"0\" cellpadding=\"0\" class=\"toolbar\" style=\"width: 100%; height: 26px; "
            + "margin-bottom: 2px; border: 0;\">" );
        out.println( "<tbody><tr>" );
        out.println( "<td align=\"left\" style=\"vertical-align: middle; width: 100%\"></td>" );
        out.println( "<td " + setupIconEnterLeaveJavascript( "j-run-pause" )
          + " align=\"left\" style=\"vertical-align: middle;\"><div style=\"padding: 2px;\" "
          + "onClick=\"runPauseFunction( this )\" class=\"toolbar-button toolbar-button-disabled\" "
          + "id=\"j-run-pause\"><img style=\"width: 22px; height: 22px\" src=\""
          + prefix + "/images/run.svg\" title=\"" + runJ + "\"/></div></td>" );
        out.println( "<td " + setupIconEnterLeaveJavascript( "j-stop" )
          + " align=\"left\" style=\"vertical-align: middle;\"><div style=\"padding: 2px;\" onClick=\"stopFunction( "
          + "this )\" class=\"toolbar-button toolbar-button-disabled\" id=\"j-stop\"><img style=\"width: 22px; "
          + "height: 22px\"src=\""
          + prefix + "/images/stop.svg\" title=\"" + stopJ + "\"/></div></td>" );
        out.println( "<td " + setupIconEnterLeaveJavascript( "j-view" )
          + " align=\"left\" style=\"vertical-align: middle;\"><div style=\"padding: 2px;\" onClick=\"viewFunction( "
          + "this )\" class=\"toolbar-button toolbar-button-disabled\" id=\"j-view\"><img style=\"width: 22px; "
          + "height: 22px\" src=\""
          + prefix + "/images/view.svg\" title=\"" + viewJ + "\"/></div></td>" );
        out.println( "<td " + setupIconEnterLeaveJavascript( "j-close" )
          + " align=\"left\" style=\"vertical-align: middle;\"><div style=\"padding: 2px; margin-right: 10px;\" "
          + "onClick=\"removeFunction( this )\" class=\"toolbar-button toolbar-button-disabled\" id=\"j-close\"><img "
          + "style=\"width: 22px; height: 22px\" src=\""
          + prefix + "/images/close.svg\" title=\"" + removeJ + "\"/></div></td>" );
        out.println( "</tr></tbody></table>" );
        out.println( "<table class=\"pentaho-table\" border=\"" + tableBorder + "\">" );
        out.print( "<tr> <th class=\"cellTableHeader\">"
          + BaseMessages.getString( PKG, "GetStatusServlet.JobName" ) + "</th> <th class=\"cellTableHeader\">"
          + BaseMessages.getString( PKG, "GetStatusServlet.CarteId" ) + "</th> <th class=\"cellTableHeader\">"
          + BaseMessages.getString( PKG, "GetStatusServlet.Status" ) + "</th> <th class=\"cellTableHeader\">"
          + BaseMessages.getString( PKG, "GetStatusServlet.LastLogDate" ) + "</th> <th class=\"cellTableHeader\">"
          + BaseMessages.getString( PKG, "GetStatusServlet.LastLogTime" ) + "</th> </tr>" );

        Comparator<CarteObjectEntry> jobComparator = ( o1, o2 ) -> {
          Job t1 = getJobMap().getJob( o1 );
          Job t2 = getJobMap().getJob( o2 );
          // If jobs are null because they were removed from the map, sort them to end of list
          if ( t1 == null && t2 == null ) {
            return 0;
          }
          if ( t1 == null ) {
            return 1;
          }
          if ( t2 == null ) {
            return -1;
          }
          Date d1 = t1.getLogDate();
          Date d2 = t2.getLogDate();
          // if both jobs have last log date, desc sort by log date
          if ( d1 != null && d2 != null ) {
            int logDateCompare = d2.compareTo( d1 );
            if ( logDateCompare != 0 ) {
              return logDateCompare;
            }
          }
          return o1.compareTo( o2 );
        };

        Collections.sort( jobEntries, jobComparator );

        evenRow = true;
        for ( int i = 0; i < jobEntries.size(); i++ ) {
          String name = Encode.forHtml( jobEntries.get( i ).getName() );
          String id = Encode.forHtml( jobEntries.get( i ).getId() );
          Job job = getJobMap().getJob( jobEntries.get( i ) );
          if ( job != null ) {
            String status = Encode.forHtml( job.getStatus() );
            String trClass = evenRow ? "cellTableEvenRow" : "cellTableOddRow"; // alternating row color
            String tdClass = evenRow ? "cellTableEvenRowCell" : "cellTableOddRowCell";
            evenRow = !evenRow; // flip
            out.print( "<tr onMouseEnter=\"mouseEnterFunction( this, '" + trClass + "' )\" "
              + "onMouseLeave=\"mouseLeaveFunction( this, '" + trClass + "' )\" "
              + "onClick=\"clickFunction( this, '" + trClass + "' )\" "
              + "id=\"j-cellTableRow_" + i + "\" class=\"cellTableCell " + trClass + "\">" );
            out.print( "<td onMouseEnter=\"mouseEnterFunction( this, '" + tdClass + "' )\" "
              + "onMouseLeave=\"mouseLeaveFunction( this, '" + tdClass + "' )\" "
              + "onClick=\"clickFunction( this, '" + tdClass + "' )\" "
              + "id=\"j-cellTableFirstCell_" + i + "\" class=\"cellTableCell cellTableFirstColumn " + tdClass + "\">"
              + name + "</a></td>" );
            out.print( "<td onMouseEnter=\"mouseEnterFunction( this, '" + tdClass + "' )\" "
              + "onMouseLeave=\"mouseLeaveFunction( this, '" + tdClass + "' )\" "
              + "onClick=\"clickFunction( this, '" + tdClass + "' )\" "
              + "id=\"j-cellTableCell_" + i + "\" class=\"cellTableCell " + tdClass + "\">" + id + "</td>" );
            out.print( "<td onMouseEnter=\"mouseEnterFunction( this, '" + tdClass + "' )\" "
              + "onMouseLeave=\"mouseLeaveFunction( this, '" + tdClass + "' )\" "
              + "onClick=\"clickFunction( this, '" + tdClass + "' )\" "
              + "id=\"j-cellTableCell_" + i + "\" class=\"cellTableCell " + tdClass + "\">" + status + "</td>" );
            String dateStr = XMLHandler.date2string( job.getLogDate() );
            out.print( "<td onMouseEnter=\"mouseEnterFunction( this, '" + tdClass + "' )\" "
              + "onMouseLeave=\"mouseLeaveFunction( this, '" + tdClass + "' )\" "
              + "onClick=\"clickFunction( this, '" + tdClass + "' )\" "
              + "id=\"j-cellTableCell_" + i + "\" class=\"cellTableCell " + tdClass + "\">"
              + ( job.getLogDate() == null ? "-" : dateStr.substring( 0, dateStr.indexOf( ' ' ) ) ) + "</td>" );
            out.print( "<td onMouseEnter=\"mouseEnterFunction( this, '" + tdClass + "' )\" "
              + "onMouseLeave=\"mouseLeaveFunction( this, '" + tdClass + "' )\" "
              + "onClick=\"clickFunction( this, '" + tdClass + "' )\" "
              + "id=\"j-cellTableLastCell_" + i + "\" class=\"cellTableCell cellTableLastColumn " + tdClass + "\">"
              + ( job.getLogDate() == null ? "-" : dateStr.substring( dateStr.indexOf( ' ' ), dateStr.length() ) )
              + "</td>" );
            out.print( "</tr>" );
          }
        }
        out.print( "</table></table>" );
        out.print( "</div>" ); // end div

      } catch ( Exception ex ) {
        out.println( "<pre>" );
        ex.printStackTrace( out );
        out.println( "</pre>" );
      }

      out.println( "<div class=\"row\" style=\"padding: 0px 0px 30px 0px;\">" );
      htmlClass = useLightTheme ? "h3" : "div";
      out.println( "<div><" + htmlClass + " class=\"workspaceHeading\">"
        + BaseMessages.getString( PKG, "GetStatusServlet.ConfigurationDetails.Title" ) + "</" + htmlClass + "></div>" );
      out.println( "<table border=\"" + tableBorder + "\">" );

      // The max number of log lines in the back-end
      //
      SlaveServerConfig serverConfig = getTransformationMap().getSlaveServerConfig();
      if ( serverConfig != null ) {
        String maxLines = "";
        if ( serverConfig.getMaxLogLines() == 0 ) {
          maxLines = BaseMessages.getString( PKG, "GetStatusServlet.NoLimit" );
        } else {
          maxLines = serverConfig.getMaxLogLines() + BaseMessages.getString( PKG, "GetStatusServlet.Lines" );
        }
        out.print(
          "<tr style=\"font-size: 12;\"> <td style=\"padding: 2px 10px 2px 10px\" class=\"cellTableCell "
            + "cellTableEvenRowCell cellTableFirstColumn\">"
            + BaseMessages.getString( PKG, "GetStatusServlet.Parameter.MaxLogLines" )
            + "</td> <td style=\"padding: 2px 10px 2px 10px\" class=\"cellTableCell cellTableEvenRowCell "
            + "cellTableLastColumn\">"
            + maxLines
            + "</td> </tr>" );

        // The max age of log lines
        //
        String maxAge = "";
        if ( serverConfig.getMaxLogTimeoutMinutes() == 0 ) {
          maxAge = BaseMessages.getString( PKG, "GetStatusServlet.NoLimit" );
        } else {
          maxAge = serverConfig.getMaxLogTimeoutMinutes() + BaseMessages.getString( PKG, "GetStatusServlet.Minutes" );
        }
        out.print(
          "<tr style=\"font-size: 12;\"> <td style=\"padding: 2px 10px 2px 10px\" class=\"cellTableCell "
            + "cellTableEvenRowCell cellTableFirstColumn\">"
            + BaseMessages.getString( PKG, "GetStatusServlet.Parameter.MaxLogLinesAge" )
            + "</td> <td style=\"padding: 2px 10px 2px 10px\" class=\"cellTableCell cellTableEvenRowCell "
            + "cellTableLastColumn\">"
            + maxAge
            + "</td> </tr>" );

        // The max age of stale objects
        //
        String maxObjAge = "";
        if ( serverConfig.getObjectTimeoutMinutes() == 0 ) {
          maxObjAge = BaseMessages.getString( PKG, "GetStatusServlet.NoLimit" );
        } else {
          maxObjAge =
            serverConfig.getObjectTimeoutMinutes() + BaseMessages.getString( PKG, "GetStatusServlet.Minutes" );
        }
        out.print(
          "<tr style=\"font-size: 12;\"> <td style=\"padding: 2px 10px 2px 10px\" class=\"cellTableCell "
            + "cellTableEvenRowCell cellTableFirstColumn\">"
            + BaseMessages.getString( PKG, "GetStatusServlet.Parameter.MaxObjectsAge" )
            + "</td> <td style=\"padding: 2px 10px 2px 10px\" class=\"cellTableCell cellTableEvenRowCell "
            + "cellTableLastColumn\">"
            + maxObjAge
            + "</td> </tr>" );

        // The name of the specified repository
        //
        String repositoryName;
        try {
          repositoryName = serverConfig.getRepository() != null ? serverConfig.getRepository().getName() : "";
        } catch ( Exception e ) {
          logError( BaseMessages.getString( PKG, "GetStatusServlet.Parameter.RepositoryName.UnableToConnect",
            serverConfig.getRepositoryId() ), e );
          repositoryName = BaseMessages.getString( PKG, "GetStatusServlet.Parameter.RepositoryName.UnableToConnect",
            serverConfig.getRepositoryId() );
        }
        out.print(
          "<tr style=\"font-size: 12;\"> <td style=\"padding: 2px 10px 2px 10px\" class=\"cellTableCell "
            + "cellTableEvenRowCell cellTableFirstColumn\">"
            + BaseMessages.getString( PKG, "GetStatusServlet.Parameter.RepositoryName" )
            + "</td> <td style=\"padding: 2px 10px 2px 10px\" class=\"cellTableCell cellTableEvenRowCell "
            + "cellTableLastColumn\">"
            + repositoryName + "</td> </tr>" );

        out.print( "</table>" );

        String filename = serverConfig.getFilename();
        if ( filename == null ) {
          filename = BaseMessages.getString( PKG, "GetStatusServlet.ConfigurationDetails.UsingDefaults" );
        }
        out.println( "</div>" ); // end div
        out.print( "<div class=\"row\">" );
        out
          .println( "<i>"
            + BaseMessages.getString( PKG, "GetStatusServlet.ConfigurationDetails.Advice", filename )
            + "</i>" );
        out.print( "</div>" );
        out.print( "</div>" );
        out.print( "</div>" );
      }

      out.println( "<script type=\"text/javascript\">" );
      out.println( "if (!String.prototype.endsWith) {" );
      out.println( "  String.prototype.endsWith = function(suffix) {" );
      out.println( "    return this.indexOf(suffix, this.length - suffix.length) !== -1;" );
      out.println( "  };" );
      out.println( "}" );
      out.println( "if (!String.prototype.startsWith) {" );
      out.println( "  String.prototype.startsWith = function(searchString, position) {" );
      out.println( "    position = position || 0;" );
      out.println( "    return this.indexOf(searchString, position) === position;" );
      out.println( "  };" );
      out.println( "}" );
      out.println( "var selectedTransRowIndex = -1;" ); // currently selected table item
      out.println( "var selectedJobRowIndex = -1;" ); // currently selected table item
      out.println( "var removeElement = null;" ); // element of remove button clicked
      out.println( "var selectedTransName = \"\";" );
      out.println( "var selectedJobName = \"\";" );

      // Click function for stop button
      out.println( "function repositionActions( element, elementFrom ) {" );
      out.println( "element.style.left = ( 10 + elementFrom.getBoundingClientRect().left ) + 'px';" );
      out.println( "}" );

      // Click function for Run, Pause, or Resume button
      out.println( "function runPauseFunction( element ) {" );
      out.println( "if( !element.classList.contains('toolbar-button-disabled') ) {" );
      out.println( "if( element.id.startsWith( 'j-' ) && selectedJobRowIndex != -1 ) {" );
      out.println( setupAjaxCall( setupJobURI( convertContextPath( StartJobServlet.CONTEXT_PATH ) ),
        BaseMessages.getString( PKG, "GetStatusServlet.StartJob.Title" ),
        "'" + BaseMessages.getString( PKG, "GetStatusServlet.TheJob.Label" ) + " ' + selectedJobName + ' "
          + BaseMessages.getString( PKG, "GetStatusServlet.StartJob.Success.Body" ) + "'",
        "'" + BaseMessages.getString( PKG, "GetStatusServlet.TheJob.Label" ) + " ' + selectedJobName + ' "
          + BaseMessages.getString( PKG, "GetStatusServlet.StartJob.Failure.Body" ) + "'" ) );
      out.println(
        "} else if ( !element.id.startsWith( 'j-' ) && selectedTransRowIndex != -1 && document.getElementById( "
          + "'cellTableCellStatus_' + selectedTransRowIndex ).innerHTML == 'Running') {" );
      out.println( setupAjaxCall( setupTransURI( convertContextPath( PauseTransServlet.CONTEXT_PATH ) ),
        BaseMessages.getString( PKG, "GetStatusServlet.PauseTrans.Title" ),
        "'" + BaseMessages.getString( PKG, "GetStatusServlet.TheTransformation.Label" ) + " ' + selectedTransName + ' "
          + BaseMessages.getString( PKG, "GetStatusServlet.PauseTrans.Success.Body" ) + "'",
        "'" + BaseMessages.getString( PKG, "GetStatusServlet.TheTransformation.Label" ) + " ' + selectedTransName + ' "
          + BaseMessages.getString( PKG, "GetStatusServlet.PauseTrans.Failure.Body" ) + "'" ) );
      out.println(
        "} else if( !element.id.startsWith( 'j-' ) && selectedTransRowIndex != -1 && document.getElementById( "
          + "'cellTableCellStatus_' + selectedTransRowIndex ).innerHTML == 'Paused') {" );
      out.println( setupAjaxCall( setupTransURI( convertContextPath( PauseTransServlet.CONTEXT_PATH ) ),
        BaseMessages.getString( PKG, "GetStatusServlet.ResumeTrans.Title" ),
        "'" + BaseMessages.getString( PKG, "GetStatusServlet.TheTransformation.Label" ) + " ' + selectedTransName + ' "
          + BaseMessages.getString( PKG, "GetStatusServlet.ResumeTrans.Success.Body" ) + "'",
        "'" + BaseMessages.getString( PKG, "GetStatusServlet.TheTransformation.Label" ) + " ' + selectedTransName + ' "
          + BaseMessages.getString( PKG, "GetStatusServlet.ResumeTrans.Failure.Body" ) + "'" ) );
      out.println( "} else if( !element.id.startsWith( 'j-' ) && selectedTransRowIndex != -1 ){" );
      out.println( setupAjaxCall( setupTransURI( convertContextPath( StartTransServlet.CONTEXT_PATH ) ),
        BaseMessages.getString( PKG, "GetStatusServlet.StartTrans.Title" ),
        "'" + BaseMessages.getString( PKG, "GetStatusServlet.TheTransformation.Label" ) + " ' + selectedTransName + ' "
          + BaseMessages.getString( PKG, "GetStatusServlet.StartTrans.Success.Body" ) + "'",
        "'" + BaseMessages.getString( PKG, "GetStatusServlet.TheTransformation.Label" ) + " ' + selectedTransName + ' "
          + BaseMessages.getString( PKG, "GetStatusServlet.StartTrans.Failure.Body" ) + "'" ) );
      out.println( "}" );
      out.println( "}" );
      out.println( "}" );

      // Click function for stop button
      out.println( "function stopFunction( element ) {" );
      out.println( "if( !element.classList.contains('toolbar-button-disabled') ) {" );
      out.println( "if( element.id.startsWith( 'j-' ) && selectedJobRowIndex != -1 ) {" );
      out.println( setupAjaxCall( setupJobURI( convertContextPath( StopJobServlet.CONTEXT_PATH ) ),
        BaseMessages.getString( PKG, "GetStatusServlet.StopJob.Title" ),
        "'" + BaseMessages.getString( PKG, "GetStatusServlet.StopJob.Success.Body1" ) + " " + BaseMessages
          .getString( PKG, "GetStatusServlet.TheJob.Label" ) + " ' + selectedJobName + ' " + BaseMessages
          .getString( PKG, "GetStatusServlet.StopJob.Success.Body2" ) + "'",
        "'" + BaseMessages.getString( PKG, "GetStatusServlet.TheJob.Label" ) + " ' + selectedJobName + ' "
          + BaseMessages.getString( PKG, "GetStatusServlet.StopJob.Failure.Body" ) + "'" ) );
      out.println( "} else if ( !element.id.startsWith( 'j-' ) && selectedTransRowIndex != -1 ) {" );
      out.println( "repositionActions( document.getElementById( 'stopActions' ), element );" );
      out.println( "document.getElementById( 'stopActions' ).style.visibility = 'visible';" );
      out.println( "}" );
      out.println( "}" );
      out.println( "}" );

      // Click function for stop button
      out.println( "function stopTransSelector( element ) {" );
      out.println( "if( element.innerHTML == 'Stop transformation' ) {" );
      out.println( setupAjaxCall( setupTransURI( convertContextPath( StopTransServlet.CONTEXT_PATH ) ),
        BaseMessages.getString( PKG, "GetStatusServlet.StopTrans.Title" ),
        "'" + BaseMessages.getString( PKG, "GetStatusServlet.StopTrans.Success.Body1" ) + " " + BaseMessages
          .getString( PKG, "GetStatusServlet.TheTransformation.Label" ) + " ' + selectedTransName + ' " + BaseMessages
          .getString( PKG, "GetStatusServlet.StopTrans.Success.Body2" ) + "'",
        "'" + BaseMessages.getString( PKG, "GetStatusServlet.TheTransformation.Label" ) + " ' + selectedTransName + ' "
          + BaseMessages.getString( PKG, "GetStatusServlet.StopTrans.Failure.Body" ) + "'" ) );
      out.println( "} else if( element.innerHTML == 'Stop input processing' ) {" );
      out.println(
        setupAjaxCall( setupTransURI( convertContextPath( StopTransServlet.CONTEXT_PATH ) ) + " + '&inputOnly=Y'",
          BaseMessages.getString( PKG, "GetStatusServlet.StopInputTrans.Title" ),
          "'" + BaseMessages.getString( PKG, "GetStatusServlet.StopInputTrans.Success.Body1" ) + " " + BaseMessages
            .getString( PKG, "GetStatusServlet.TheTransformation.Label" ) + " ' + selectedTransName + ' " + BaseMessages
            .getString( PKG, "GetStatusServlet.StopInputTrans.Success.Body2" ) + "'",
          "'" + BaseMessages.getString( PKG, "GetStatusServlet.TheTransformation.Label" )
            + " ' + selectedTransName + ' " + BaseMessages
            .getString( PKG, "GetStatusServlet.StopInputTrans.Failure.Body" ) + "'" ) );
      out.println( "}" );
      out.println( "document.getElementById( 'stopActions' ).style.visibility = 'hidden';" );
      out.println( "}" );

      // Click function for view button
      out.println( "function viewFunction( element ) {" );
      out.println( "if( !element.classList.contains('toolbar-button-disabled') ) {" );
      out.println( "if( element.id.startsWith( 'j-' ) && selectedJobRowIndex != -1 ) {" );
      out.println( "window.location.replace( '"
        + convertContextPath( GetJobStatusServlet.CONTEXT_PATH ) + "'"
        + " + '?name=' + encodeURIComponent(document.getElementById( 'j-cellTableFirstCell_' + selectedJobRowIndex )"
        + ".innerText)"
        + " + '&id=' + document.getElementById( 'j-cellTableCell_' + selectedJobRowIndex ).innerHTML + '&from=0' );" );
      out.println( "} else if ( selectedTransRowIndex != -1 ) {" );
      out.println( "window.location.replace( '"
        + convertContextPath( GetTransStatusServlet.CONTEXT_PATH ) + "'"
        + " + '?name=' + encodeURIComponent(document.getElementById( 'cellTableFirstCell_' + selectedTransRowIndex )"
        + ".innerText)"
        + " + '&id=' + document.getElementById( 'cellTableCell_' + selectedTransRowIndex ).innerText + '&from=0' );" );
      out.println( "}" );
      out.println( "}" );
      out.println( "}" );

      // Click function for remove button
      out.println( "function removeFunction( element ) {" );
      out.println( "if( !element.classList.contains('toolbar-button-disabled') ) {" );
      out.println( "removeElement = element;" );
      out.println( "if( element.id.startsWith( 'j-' ) && selectedJobRowIndex != -1 ) {" );
      out.println( "openMessageDialog( '" + BaseMessages.getString( PKG, "GetStatusServlet.RemoveJob.Title" ) + "',"
        + "'" + BaseMessages.getString( PKG, "GetStatusServlet.RemoveJob.Confirm.Body" ) + " " + BaseMessages
        .getString( PKG, "GetStatusServlet.TheJob.Label" ) + " ' + selectedJobName + '?" + "'" + ", false );" );
      out.println( "} else if ( selectedTransRowIndex != -1 ) {" );
      out.println( "openMessageDialog( '" + BaseMessages.getString( PKG, "GetStatusServlet.RemoveTrans.Title" ) + "',"
        + "'" + BaseMessages.getString( PKG, "GetStatusServlet.RemoveTrans.Confirm.Body" ) + " " + BaseMessages
        .getString( PKG, "GetStatusServlet.TheTransformation.Label" ) + " ' + selectedTransName + '?" + "'"
        + ", false );" );
      out.println( "}" );
      out.println( "}" );
      out.println( "}" );

      // OnClick function for table element
      out.println( "function clickFunction( element, tableClass ) {" );
      out.println( "var prefix = element.id.startsWith( 'j-' ) ? 'j-' : '';" );
      out.println( "var rowNum = getRowNum( element.id );" );
      out.println( "if( tableClass.endsWith( 'Row' ) ) {" );
      out.println( "element.className='cellTableRow ' + tableClass + ' cellTableSelectedRow';" );
      out.println( "} else {" );
      out.println(
        "document.getElementById( prefix + 'cellTableFirstCell_' + rowNum ).className='cellTableCell "
          + "cellTableFirstColumn ' + tableClass + ' cellTableSelectedRowCell';" );
      out.println( "element.className='cellTableCell ' + tableClass + ' cellTableSelectedRowCell';" );
      out.println( "}" );
      out.println( "if( element.id.startsWith( 'j-' ) ) {" );
      out.println( "document.getElementById( \"j-run-pause\" ).classList.remove( \"toolbar-button-disabled\" )" );
      out.println( "document.getElementById( \"j-stop\" ).classList.remove( \"toolbar-button-disabled\" )" );
      out.println( "document.getElementById( \"j-view\" ).classList.remove( \"toolbar-button-disabled\" )" );
      out.println( "document.getElementById( \"j-close\" ).classList.remove( \"toolbar-button-disabled\" )" );
      out.println( "if( selectedJobRowIndex != -1 && rowNum != selectedJobRowIndex ) {" );
      out.println(
        "document.getElementById( prefix + 'cellTableRow_' + selectedJobRowIndex ).className='cellTableRow ' + "
          + "tableClass;" );
      out.println(
        "document.getElementById( prefix + 'cellTableFirstCell_' + selectedJobRowIndex ).className='cellTableCell "
          + "cellTableFirstColumn ' + tableClass;" );
      out.println(
        "document.getElementById( prefix + 'cellTableCell_' + selectedJobRowIndex ).className='cellTableCell ' + "
          + "tableClass;" );
      out.println(
        "document.getElementById( prefix + 'cellTableLastCell_' + selectedJobRowIndex ).className='cellTableCell "
          + "cellTableLastColumn ' + tableClass;" );
      out.println( "}" );
      out.println( "selectedJobRowIndex = rowNum;" );
      out.println( "} else {" );
      out.println( "document.getElementById( \"run-pause\" ).classList.remove( \"toolbar-button-disabled\" )" );
      out.println( "document.getElementById( \"stop\" ).classList.remove( \"toolbar-button-disabled\" )" );
      out.println( "document.getElementById( \"view\" ).classList.remove( \"toolbar-button-disabled\" )" );
      out.println( "document.getElementById( \"close\" ).classList.remove( \"toolbar-button-disabled\" )" );
      out.println( "if( selectedTransRowIndex != -1 && rowNum != selectedTransRowIndex ) {" );
      out.println(
        "document.getElementById( prefix + 'cellTableRow_' + selectedTransRowIndex ).className='cellTableRow ' + "
          + "tableClass;" );
      out.println(
        "document.getElementById( prefix + 'cellTableFirstCell_' + selectedTransRowIndex ).className='cellTableCell "
          + "cellTableFirstColumn ' + tableClass;" );
      out.println(
        "document.getElementById( prefix + 'cellTableCell_' + selectedTransRowIndex ).className='cellTableCell ' + "
          + "tableClass;" );
      out.println(
        "document.getElementById( prefix + 'cellTableLastCell_' + selectedTransRowIndex ).className='cellTableCell "
          + "cellTableLastColumn ' + tableClass;" );
      out.println( "}" );
      out.println( "selectedTransRowIndex = rowNum;" );
      out.println(
        "if( document.getElementById( 'cellTableCellStatus_' + selectedTransRowIndex ).innerHTML == 'Running' ) {" );
      out.println( "document.getElementById( 'run-pause' ).innerHTML = '<img style=\"width: 22px; height: 22px\" src=\""
        + prefix + "/images/pause.svg\"/ title=\"" + BaseMessages.getString( PKG, "GetStatusServlet.PauseTrans" )
        + "\">';" );
      out.println(
        "} else if( document.getElementById( 'cellTableCellStatus_' + selectedTransRowIndex ).innerHTML == 'Paused' )"
          + " {" );
      out.println( "document.getElementById( 'run-pause' ).innerHTML = '<img style=\"width: 22px; height: 22px\" src=\""
        + prefix + "/images/pause.svg\" title=\"" + BaseMessages.getString( PKG, "GetStatusServlet.ResumeTrans" )
        + "\"/>';" );
      out.println( "} else {" );
      out.println( "document.getElementById( 'run-pause' ).innerHTML = '<img style=\"width: 22px; height: 22px\" src=\""
        + prefix + "/images/run.svg\" title=\"" + run + "\"/>';" );
      out.println( "}" );
      out.println( "}" );
      out.println( "setSelectedNames();" );
      out.println( "}" );

      // Function to set the trans or job name of the selected trans or job
      out.println( "function setSelectedNames() {" );
      out.println( "  selectedJobName = selectedTransName = \"\";" );
      out.println(
        "  var selectedElementNames = document.getElementsByClassName( \"cellTableFirstColumn "
          + "cellTableSelectedRowCell\" );" );
      out.println( "  if( selectedElementNames ) {" );
      out.println( "    for(var i = 0; i < selectedElementNames.length; i++) {" );
      out.println( "      if(selectedElementNames[i].id.startsWith(\"j-\")) {" );
      out.println( "        selectedJobName = selectedElementNames[i].innerHTML;" );
      out.println( "      } else {" );
      out.println( "        selectedTransName = selectedElementNames[i].innerHTML;" );
      out.println( "      }" );
      out.println( "    }" );
      out.println( "  }" );
      out.println( "}" );

      // OnMouseEnter function
      out.println( "function mouseEnterFunction( element, tableClass ) {" );
      out.println( "var prefix = '';" );
      out.println( "var rowNum = getRowNum( element.id );" );
      out.println( "var selectedIndex = selectedTransRowIndex;" );
      out.println( "if( element.id.startsWith( 'j-' ) ) {" );
      out.println( "prefix = 'j-';" );
      out.println( "selectedIndex = selectedJobRowIndex;" );
      out.println( "}" );
      out.println( "if( rowNum != selectedIndex ) {" );
      out.println( "if( tableClass.endsWith( 'Row' ) ) {" );
      out.println( "element.className='cellTableRow ' + tableClass + ' cellTableHoveredRow';" );
      out.println( "} else {" );
      out.println(
        "document.getElementById( prefix + 'cellTableFirstCell_' + element.id.charAt( element.id.length - 1 ) )"
          + ".className='cellTableCell cellTableFirstColumn ' + tableClass + ' cellTableHoveredRowCell';" );
      out.println(
        "document.getElementById( prefix + 'cellTableCell_' + element.id.charAt( element.id.length - 1 ) )"
          + ".className='cellTableCell ' + tableClass + ' cellTableHoveredRowCell';" );
      out.println(
        "document.getElementById( prefix + 'cellTableLastCell_' + element.id.charAt( element.id.length - 1 ) )"
          + ".className='cellTableCell cellTableLastColumn ' + tableClass + ' cellTableHoveredRowCell';" );
      out.println( "}" );
      out.println( "}" );
      out.println( "}" );

      // OnMouseLeave function
      out.println( "function mouseLeaveFunction( element, tableClass ) {" );
      out.println( "var prefix = '';" );
      out.println( "var rowNum = getRowNum( element.id );" );
      out.println( "var selectedIndex = selectedTransRowIndex;" );
      out.println( "if( element.id.startsWith( 'j-' ) ) {" );
      out.println( "prefix = 'j-';" );
      out.println( "selectedIndex = selectedJobRowIndex;" );
      out.println( "}" );
      out.println( "if( rowNum != selectedIndex ) {" );
      out.println( "if( tableClass.endsWith( 'Row' ) ) {" );
      out.println( "element.className='cellTableRow ' + tableClass;" );
      out.println( "} else {" );
      out.println(
        "document.getElementById( prefix + 'cellTableFirstCell_' + element.id.charAt( element.id.length - 1 ) )"
          + ".className='cellTableCell cellTableFirstColumn ' + tableClass;" );
      out.println(
        "document.getElementById( prefix + 'cellTableCell_' + element.id.charAt( element.id.length - 1 ) )"
          + ".className='cellTableCell ' + tableClass;" );
      out.println(
        "document.getElementById( prefix + 'cellTableLastCell_' + element.id.charAt( element.id.length - 1 ) )"
          + ".className='cellTableCell cellTableLastColumn ' + tableClass;" );
      out.println( "}" );
      out.println( "}" );
      out.println( "}" );

      // Onclick function for closing message dialog-->make it hidden
      out.println( "function closeMessageDialog( refresh ) {" );
      out.println( "  document.getElementById( \"messageDialogBackdrop\" ).style.visibility = 'hidden';" );
      out.println( "  document.getElementById( \"messageDialog\" ).style.visibility = 'hidden';" );
      out.println( "  if( refresh ) {" );
      out.println( "    window.location.reload();" );
      out.println( "  }" );
      out.println( "}" );

      // Function to open the message dialog--> make it visible
      out.println( "function openMessageDialog( title, body, single ) {" );
      out.println( "  document.getElementById( \"messageDialogBackdrop\" ).style.visibility = 'visible';" );
      out.println( "  document.getElementById( \"messageDialog\" ).style.visibility = 'visible';" );
      out.println( "  document.getElementById( \"messageDialogTitle\" ).innerHTML = title;" );
      out.println( "  document.getElementById( \"messageDialogBody\" ).innerHTML = body;" );
      out.println( "  if( single ) {" );
      out.println( "    document.getElementById( \"singleButton\" ).style.display = 'block';" );
      out.println( "    document.getElementById( \"doubleButton\" ).style.display = 'none';" );
      out.println( "  } else {" );
      out.println( "    document.getElementById( \"singleButton\" ).style.display = 'none';" );
      out.println( "    document.getElementById( \"doubleButton\" ).style.display = 'block';" );
      out.println( "  }" );
      out.println( "}" );

      // Function to remove selected trans/job after user confirms
      out.println( "function removeSelection() {" );
      out.println( "  if( removeElement !== null ) {" );
      out.println( "    if( removeElement.id.startsWith( 'j-' ) && selectedJobRowIndex != -1 ) {" );
      out.println( setupAjaxCall( setupJobURI( convertContextPath( RemoveJobServlet.CONTEXT_PATH ) ),
        BaseMessages.getString( PKG, "GetStatusServlet.RemoveJob.Title" ),
        "'" + BaseMessages.getString( PKG, "GetStatusServlet.TheJob.Label" ) + " ' + selectedJobName + ' "
          + BaseMessages.getString( PKG, "GetStatusServlet.RemoveJob.Success.Body" ) + "'",
        "'" + BaseMessages.getString( PKG, "GetStatusServlet.TheJob.Label" ) + " ' + selectedJobName + ' "
          + BaseMessages.getString( PKG, "GetStatusServlet.RemoveJob.Failure.Body" ) + "'" ) );
      out.println( "} else if ( selectedTransRowIndex != -1 ) {" );
      out.println( setupAjaxCall( setupTransURI( convertContextPath( RemoveTransServlet.CONTEXT_PATH ) ),
        BaseMessages.getString( PKG, "GetStatusServlet.RemoveTrans.Title" ),
        "'" + BaseMessages.getString( PKG, "GetStatusServlet.TheTransformation.Label" ) + " ' + selectedTransName + ' "
          + BaseMessages.getString( PKG, "GetStatusServlet.RemoveTrans.Success.Body" ) + "'",
        "'" + BaseMessages.getString( PKG, "GetStatusServlet.TheTransformation.Label" ) + " ' + selectedTransName + ' "
          + BaseMessages.getString( PKG, "GetStatusServlet.RemoveTrans.Failure.Body" ) + "'" ) );
      out.println( "    }" );
      out.println( "  }" );
      out.println( "}" );

      out.println( "function getRowNum( id ) {" );
      out.println( "  return id.substring( id.indexOf('_') + 1, id.length);" );
      out.println( "}" );

      out.println( "</script>" );

      out.println( "</BODY>" );
      out.println( "</HTML>" );
    }
  }

  private String encodeUriComponents( String path ) {
    return Arrays.stream( path.split( "/" ) )
      .map( Encode::forUriComponent )
      .collect( Collectors.joining( "/" ) );
  }

  private static void getSystemInfo( SlaveServerStatus serverStatus ) {
    OperatingSystemMXBean operatingSystemMXBean =
      java.lang.management.ManagementFactory.getOperatingSystemMXBean();
    ThreadMXBean threadMXBean = java.lang.management.ManagementFactory.getThreadMXBean();
    RuntimeMXBean runtimeMXBean = java.lang.management.ManagementFactory.getRuntimeMXBean();

    int cores = Runtime.getRuntime().availableProcessors();

    long freeMemory = Runtime.getRuntime().freeMemory();
    long totalMemory = Runtime.getRuntime().totalMemory();
    String osArch = operatingSystemMXBean.getArch();
    String osName = operatingSystemMXBean.getName();
    String osVersion = operatingSystemMXBean.getVersion();
    double loadAvg = operatingSystemMXBean.getSystemLoadAverage();

    int threadCount = threadMXBean.getThreadCount();
    long allThreadsCpuTime = 0L;

    long[] threadIds = threadMXBean.getAllThreadIds();
    for ( int i = 0; i < threadIds.length; i++ ) {
      allThreadsCpuTime += threadMXBean.getThreadCpuTime( threadIds[ i ] );
    }

    long uptime = runtimeMXBean.getUptime();

    serverStatus.setCpuCores( cores );
    serverStatus.setCpuProcessTime( allThreadsCpuTime );
    serverStatus.setUptime( uptime );
    serverStatus.setThreadCount( threadCount );
    serverStatus.setLoadAvg( loadAvg );
    serverStatus.setOsName( osName );
    serverStatus.setOsVersion( osVersion );
    serverStatus.setOsArchitecture( osArch );
    serverStatus.setMemoryFree( freeMemory );
    serverStatus.setMemoryTotal( totalMemory );
  }

  public String toString() {
    return "Status Handler";
  }

  public String getService() {
    return CONTEXT_PATH + " (" + toString() + ")";
  }

  public String getContextPath() {
    return CONTEXT_PATH;
  }

  private String setupIconEnterLeaveJavascript( String id ) {
    return "onMouseEnter=\"if( !document.getElementById('"
      + id + "').classList.contains('toolbar-button-disabled') ) { document.getElementById('"
      + id + "').classList.add('toolbar-button-hovering') }\" onMouseLeave=\"document.getElementById('"
      + id + "').classList.remove('toolbar-button-hovering')\"";
  }

  private String messageDialog() {
    String retVal =
      "<div id=\"messageDialogBackdrop\" style=\"visibility: hidden; position: absolute; top: 0; right: 0; bottom: 0;"
        + " left: 0; opacity: 0.5; background-color: #000; z-index: 1000;\"></div>\n";
    retVal +=
      "<div class=\"pentaho-dialog\" id=\"messageDialog\" style=\"visibility: hidden; margin: 0; position: absolute; "
        + "top: 50%; left: 50%; transform: translate(-50%, -50%); -ms-transform: translate(-50%, -50%);"
        + "-webkit-transform: translate(-50%, -50%); padding: 30px; height: auto; width: 423px; border: 1px solid "
        + "#CCC; -webkit-box-shadow: none; -moz-box-shadow: none;"
        + "box-shadow: none; -webkit-box-sizing: border-box; -moz-box-sizing: border-box; box-sizing: border-box; "
        + "overflow: hidden; line-height: 20px; background-color: #FFF; z-index: 10000;\">\n";
    retVal += "<div id=\"messageDialogTitle\" class=\"Caption\"></div>\n";
    retVal += "<div id=\"messageDialogBody\" class=\"dialog-content\"></div>\n";
    retVal +=
      "<div id=\"singleButton\" style=\"margin-top: 30px;\">\n<button class=\"pentaho-button\" style=\"float: right;"
        + "\" onclick=\"closeMessageDialog( true );\">\n<span>"
        + BaseMessages.getString( PKG, "GetStatusServlet.Button.OK" ) + "</span>\n</button>\n</div>\n";
    retVal +=
      "<div id=\"doubleButton\" style=\"margin-top: 30px;\">\n<button class=\"pentaho-button\" style=\"float: right; "
        + "margin-left: 10px;\" onclick=\"closeMessageDialog( false );\">\n<span>"
        + BaseMessages.getString( PKG, "GetStatusServlet.Button.No" )
        + "</span>\n</button>\n<button class=\"pentaho-button\" style=\"float: right;\" onclick=\"closeMessageDialog("
        + " false ); removeSelection();\">\n<span>"
        + BaseMessages.getString( PKG, "GetStatusServlet.Button.YesRemove" ) + "</span>\n</button>\n</div>\n";
    retVal += "</div>\n";
    return retVal;
  }

  private String setupAjaxCall( String uri, String title, String success, String failure ) {
    String retVal = "";
    retVal += "var xhttp = new XMLHttpRequest();\n";
    retVal += "xhttp.onreadystatechange = function() {\n";
    retVal += " if ( this.readyState === 4 ) {\n";
    retVal += "   if ( this.status === 200 ) {\n";
    retVal += "     openMessageDialog( '" + title + "', " + success + ", true );\n";
    retVal += "   } else {\n";
    retVal += "     openMessageDialog( '" + BaseMessages.getString( PKG, "GetStatusServlet.UnableTo.Label" )
      + " " + title + "', " + failure + ", true );\n";
    retVal += "   }\n";
    retVal += " }\n";
    retVal += "};\n";
    retVal += "xhttp.open( \"GET\", " + uri + ", true );\n";
    retVal += "xhttp.send();\n";
    return retVal;
  }

  private String setupTransURI( String context ) {
    return "'" + context + "'"
      + " + '?name=' + encodeURIComponent(document.getElementById( 'cellTableFirstCell_' + selectedTransRowIndex )"
      + ".innerText)"
      + " + '&id=' + document.getElementById( 'cellTableCell_' + selectedTransRowIndex ).innerText";
  }

  private String setupJobURI( String context ) {
    return "'" + context + "'"
      + " + '?name=' + encodeURIComponent(document.getElementById( 'j-cellTableFirstCell_' + selectedJobRowIndex )"
      + ".innerText)"
      + " + '&id=' + document.getElementById( 'j-cellTableCell_' + selectedJobRowIndex ).innerText";
  }
}
