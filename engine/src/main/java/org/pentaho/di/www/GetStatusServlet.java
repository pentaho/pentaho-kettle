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

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.Job;
import org.pentaho.di.trans.Trans;
import org.w3c.dom.Document;

public class GetStatusServlet extends BaseHttpServlet implements CartePluginInterface {
  private static Class<?> PKG = GetStatusServlet.class; // for i18n purposes, needed by Translator2!!

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
        String status = trans.getStatus();

        SlaveServerTransStatus sstatus = new SlaveServerTransStatus( entry.getName(), entry.getId(), status );
        sstatus.setLogDate( trans.getLogDate() );
        sstatus.setPaused( trans.isPaused() );
        serverStatus.getTransStatusList().add( sstatus );
      }

      for ( CarteObjectEntry entry : jobEntries ) {
        Job job = getJobMap().getJob( entry );
        String status = job.getStatus();
        SlaveServerJobStatus jobStatus = new SlaveServerJobStatus( entry.getName(), entry.getId(), status );
        jobStatus.setLogDate( job.getLogDate() );
        serverStatus.getJobStatusList().add( jobStatus );
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
        try {
          // Read in currently set theme from pentaho.xml file
          String themeSetting = ".." + File.separator + ".." + File.separator
              + "pentaho-solutions" + File.separator + "system" + File.separator + "pentaho.xml";
          File f = new File( themeSetting );
          DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
          DocumentBuilder db = dbFactory.newDocumentBuilder();
          Document doc = db.parse( f );
          String themeName = doc.getElementsByTagName( "default-theme" ).item( 0 ).getTextContent();

          // Get theme CSS file
          String themeCss = ".." + File.separator + ".." + File.separator
              + "pentaho-solutions" + File.separator + "system" + File.separator
              + "common-ui" + File.separator + "resources" + File.separator
              + "themes" + File.separator + themeName + File.separator;
          File themeDir = new File( themeCss );
          for ( File fName : themeDir.listFiles() ) {
            if ( fName.getName().contains( ".css" ) ) {
              themeCss = fName.getName();
              break;
            }
          }

          // Get mantle theme CSS file
          String mantleThemeCss = ".." + File.separator + "webapps" + File.separator
              + "pentaho" + File.separator + "mantle" + File.separator
              + "themes" + File.separator + themeName + File.separator;
          File mantleThemeDir = new File( mantleThemeCss );
          for ( File fName : mantleThemeDir.listFiles() ) {
            if ( fName.getName().contains( ".css" ) ) {
              mantleThemeCss = fName.getName();
              break;
            }
          }

          out.println( "<link rel=\"stylesheet\" type=\"text/css\" href=\"/pentaho/content/common-ui/resources/themes/" + themeName + "/" + themeCss + "\"/>" );
          out.println( "<link rel=\"stylesheet\" type=\"text/css\" href=\"/pentaho/mantle/themes/" + themeName + "/" + mantleThemeCss + "\"/>" );
          out.println( "<link rel=\"stylesheet\" type=\"text/css\" href=\"/pentaho/mantle/MantleStyle.css\"/>" );
          tableBorder = 0;
        } catch ( Exception ex ) {
          useLightTheme = true;
          // log here
        }
      }

      out.println( "</HEAD>" );
      out.println( "<BODY class=\"pentaho-page-background dragdrop-dropTarget dragdrop-boundary\">" );

      // Empty div for containing currently selected item
      out.println( "<div id=\"selectedTableItem\">" );
      out.println( "<value></value>" ); //initialize to none
      out.println( "</div>" );

      out.println( "<div class=\"row\" id=\"pucHeader\">" );

      String htmlClass = useLightTheme ? "h1" : "div";
      out.println( "<" + htmlClass + " class=\"workspaceHeading\">" + BaseMessages.getString( PKG, "GetStatusServlet.TopStatus" ) + "</" + htmlClass + ">" );
      out.println( "</div>" );

      try {
        out.println( "<br>" );
        out.println( "<div class=\"row\" style=\"padding: 0px 0px 0px 20px\">" );
        htmlClass = useLightTheme ? "h2" : "div";
        out.println( "<div class=\"row\">" );
        out.println( "<" + htmlClass + " class=\"workspaceHeading\" style=\"padding: 0px 0px 0px 0px;\">Transformations</" + htmlClass + ">" );
        out.println( "<table cellspacing=\"0\" cellpadding=\"0\"><tbody><tr><td align=\"left\" width=\"100%\" style=\"vertical-align:middle;\">" );
        out.println( "<table cellspacing=\"0\" cellpadding=\"0\" class=\"toolbar\" style=\"width: 100%; height: 26px; margin-bottom: 5px; border: 0;\">" );
        out.println( "<tbody><tr>" );
        out.println( "<td align=\"left\" style=\"vertical-align: middle; width: 100%\"></td>" );
        out.println( "<td onMouseEnter=\"document.getElementById( 'pause' ).className='toolbar-button toolbar-button-hovering'\" onMouseLeave=\"document.getElementById( 'pause' ).className='toolbar-button'\" align=\"left\" style=\"vertical-align: middle;\"><div class=\"toolbar-button\" id=\"pause\"><a href=\"#\" style=\"\"><img style=\"width: 22px; height: 22px\" src=\"/pentaho/content/common-ui/resources/themes/images/run.svg\"/></a></div></td>" );
        out.println( "<td onMouseEnter=\"document.getElementById( 'stop' ).className='toolbar-button toolbar-button-hovering'\" onMouseLeave=\"document.getElementById( 'stop' ).className='toolbar-button'\" align=\"left\" style=\"vertical-align: middle;\"><div class=\"toolbar-button\" id=\"stop\"><a href=\"#\" style=\"\"><img style=\"width: 22px; height: 22px\"src=\"/pentaho/content/common-ui/resources/themes/images/stop.svg\"/></a></div></td>" );
        out.println( "<td onMouseEnter=\"document.getElementById( 'view' ).className='toolbar-button toolbar-button-hovering'\" onMouseLeave=\"document.getElementById( 'view' ).className='toolbar-button'\" align=\"left\" style=\"vertical-align: middle;\"><div class=\"toolbar-button\" id=\"view\"><a href=\"#\" style=\"\"><img style=\"width: 22px; height: 22px\" src=\"/pentaho/content/common-ui/resources/themes/images/view.svg\"/></a></div></td>" );
        out.println( "<td onMouseEnter=\"document.getElementById( 'close' ).className='toolbar-button toolbar-button-hovering'\" onMouseLeave=\"document.getElementById( 'close' ).className='toolbar-button'\" align=\"left\" style=\"vertical-align: middle;\"><div class=\"toolbar-button\" id=\"close\"><a href=\"#\" style=\"\"><img style=\"width: 22px; height: 22px\" src=\"/pentaho/content/common-ui/resources/themes/images/close.svg\"/></a></div></td>" );
        out.println( "</tr></tbody></table>" );
        out.println( "<table class=\"pentaho-table\" border=\"" + tableBorder + "\">" );
        out.print( "<tr> <th class=\"cellTableHeader\">"
            + BaseMessages.getString( PKG, "GetStatusServlet.TransName" ) + "</th> <th class=\"cellTableHeader\">"
            + BaseMessages.getString( PKG, "GetStatusServlet.CarteId" ) + "</th> <th class=\"cellTableHeader\">"
            + BaseMessages.getString( PKG, "GetStatusServlet.Status" ) + "</th> <th class=\"cellTableHeader\">"
            + BaseMessages.getString( PKG, "GetStatusServlet.LastLogDate" ) + "</th> <th class=\"cellTableHeader\">"
            + BaseMessages.getString( PKG, "GetStatusServlet.Remove" ) + "</th> </tr>" );

        Comparator<CarteObjectEntry> transComparator = new Comparator<CarteObjectEntry>() {
          @Override
          public int compare( CarteObjectEntry o1, CarteObjectEntry o2 ) {
            Trans t1 = getTransformationMap().getTransformation( o1 );
            Trans t2 = getTransformationMap().getTransformation( o2 );
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
          }
        };

        Collections.sort( transEntries, transComparator );

        boolean evenRow = true;
        for ( int i = 0; i < transEntries.size(); i++ ) {
          String name = transEntries.get( i ).getName();
          String id = transEntries.get( i ).getId();
          Trans trans = getTransformationMap().getTransformation( transEntries.get( i ) );
          String status = trans.getStatus();
          String removeText = "";
          // Finished, Stopped, Waiting : allow the user to remove the transformation
          //
          if ( trans.isFinished() || trans.isStopped() || ( !trans.isInitializing() && !trans.isRunning() ) ) {
            removeText =
                "<a href=\""
                    + convertContextPath( RemoveTransServlet.CONTEXT_PATH ) + "?name="
                    + URLEncoder.encode( name, "UTF-8" ) + "&id=" + id + "\"> Remove </a>";
          }

          String trClass = evenRow ? "cellTableEvenRow" : "cellTableOddRow"; // alternating row color
          String tdClass = evenRow ? "cellTableEvenRowCell" : "cellTableOddRowCell";
          evenRow = !evenRow; // flip
          String firstColumn = i == 0 ? "cellTableFirstColumn" : "";
          String lastColumn = i == transEntries.size() - 1 ? "cellTableLastColumn" : "";
          out.print( "<tr onMouseEnter=\"mouseEnterFunction( this, '" + trClass + "' )\" "
              + "onMouseLeave=\"mouseLeaveFunction( this, '" + trClass + "' )\" "
              + "onClick=\"clickFunction( this, '" + trClass + "' )\" "
              + "id=\"cellTableRow" + i + "\" class=\"" + trClass + "\">" );
          out.print( "<td onMouseEnter=\"mouseEnterFunction( this, '" + tdClass + "' )\" "
              + "onMouseLeave=\"mouseLeaveFunction( this, '" + tdClass + "' )\" "
              + "onClick=\"clickFunction( this, '" + tdClass + "' )\" "
              + "id=\"cellTableFirstCell" + i + "\" class=\"cellTableCell cellTableFirstColumn " + tdClass + "\"><a href=\""
              + convertContextPath( GetTransStatusServlet.CONTEXT_PATH ) + "?name="
              + URLEncoder.encode( name, "UTF-8" ) + "&id=" + id + "\">" + name + "</a></td>" );
          out.print( "<td onMouseEnter=\"mouseEnterFunction( this, '" + tdClass + "' )\" "
              + "onMouseLeave=\"mouseLeaveFunction( this, '" + tdClass + "' )\" "
              + "onClick=\"clickFunction( this, '" + tdClass + "' )\" "
              + "id=\"cellTableCell" + i + "\" class=\"cellTableCell " + tdClass + "\">" + id + "</td>" );
          out.print( "<td onMouseEnter=\"mouseEnterFunction( this, '" + tdClass + "' )\" "
              + "onMouseLeave=\"mouseLeaveFunction( this, '" + tdClass + "' )\" "
              + "onClick=\"clickFunction( this, '" + tdClass + "' )\" "
              + "id=\"cellTableCell" + i + "\" class=\"cellTableCell " + tdClass + "\">" + status + "</td>" );
          out.print( "<td onMouseEnter=\"mouseEnterFunction( this, '" + tdClass + "' )\" "
              + "onMouseLeave=\"mouseLeaveFunction( this, '" + tdClass + "' )\" "
              + "onClick=\"clickFunction( this, '" + tdClass + "' )\" "
              + "id=\"cellTableCell" + i + "\" class=\"cellTableCell " + tdClass + "\">"
              + ( trans.getLogDate() == null ? "-" : XMLHandler.date2string( trans.getLogDate() ) ) + "</td>" );
          out.print( "<td onMouseEnter=\"mouseEnterFunction( this, '" + tdClass + "' )\" "
              + "onMouseLeave=\"mouseLeaveFunction( this, '" + tdClass + "' )\" "
              + "onClick=\"clickFunction( this, '" + tdClass + "' )\" "
              + "id=\"cellTableLastCell" + i + "\" class=\"cellTableCell cellTableLastColumn " + tdClass + "\">" + removeText + "</td>" );
          out.print( "</tr>" );
        }
        out.print( "</table></table>" );
        out.print( "</div>" ); // end div
        out.print( "<br>" );

        out.println( "<div class=\"row\">" );
        out.println( "<" + htmlClass + " class=\"workspaceHeading\" style=\"padding: 0px 0px 0px 0px;\">Jobs</" + htmlClass + ">" );
        out.println( "<table cellspacing=\"0\" cellpadding=\"0\"><tbody><tr><td align=\"left\" width=\"100%\" style=\"vertical-align:middle;\">" );
        out.println( "<table cellspacing=\"0\" cellpadding=\"0\" class=\"toolbar\" style=\"width: 100%; height: 26px; margin-bottom: 5px; border: 0;\">" );
        out.println( "<tbody><tr>" );
        out.println( "<td align=\"left\" style=\"vertical-align: middle; width: 100%\"></td>" );
        out.println( "<td onMouseEnter=\"document.getElementById( 'j-pause' ).className='toolbar-button toolbar-button-hovering'\" onMouseLeave=\"document.getElementById( 'j-pause' ).className='toolbar-button'\" align=\"left\" style=\"vertical-align: middle;\"><div class=\"toolbar-button\" id=\"j-pause\"><a href=\"#\" style=\"\"><img style=\"width: 22px; height: 22px\" src=\"/pentaho/content/common-ui/resources/themes/" + "ruby" + "/images/run.svg\"/></a></div></td>" );
        out.println( "<td onMouseEnter=\"document.getElementById( 'j-stop' ).className='toolbar-button toolbar-button-hovering'\" onMouseLeave=\"document.getElementById( 'j-stop' ).className='toolbar-button'\" align=\"left\" style=\"vertical-align: middle;\"><div class=\"toolbar-button\" id=\"j-stop\"><a href=\"#\" style=\"\"><img style=\"width: 22px; height: 22px\"src=\"/pentaho/content/common-ui/resources/themes/" + "ruby" + "/images/stop.svg\"/></a></div></td>" );
        out.println( "<td onMouseEnter=\"document.getElementById( 'j-view' ).className='toolbar-button toolbar-button-hovering'\" onMouseLeave=\"document.getElementById( 'j-view' ).className='toolbar-button'\" align=\"left\" style=\"vertical-align: middle;\"><div class=\"toolbar-button\" id=\"j-view\"><a href=\"#\" style=\"\"><img style=\"width: 22px; height: 22px\" src=\"/pentaho/content/common-ui/resources/themes/" + "ruby" + "/images/view.svg\"/></a></div></td>" );
        out.println( "<td onMouseEnter=\"document.getElementById( 'j-close' ).className='toolbar-button toolbar-button-hovering'\" onMouseLeave=\"document.getElementById( 'j-close' ).className='toolbar-button'\" align=\"left\" style=\"vertical-align: middle;\"><div class=\"toolbar-button\" id=\"j-close\"><a href=\"#\" style=\"\"><img style=\"width: 22px; height: 22px\" src=\"/pentaho/content/common-ui/resources/themes/" + "ruby" + "/images/close.svg\"/></a></div></td>" );
        out.println( "</tr></tbody></table>" );
        out.println( "<table class=\"pentaho-table\" border=\"" + tableBorder + "\">" );
        out.print( "<tr> <th class=\"cellTableHeader\">"
            + BaseMessages.getString( PKG, "GetStatusServlet.JobName" ) + "</th> <th class=\"cellTableHeader\">"
            + BaseMessages.getString( PKG, "GetStatusServlet.CarteId" ) + "</th> <th class=\"cellTableHeader\">"
            + BaseMessages.getString( PKG, "GetStatusServlet.Status" ) + "</th> <th class=\"cellTableHeader\">"
            + BaseMessages.getString( PKG, "GetStatusServlet.LastLogDate" ) + "</th> <th class=\"cellTableHeader\">"
            + BaseMessages.getString( PKG, "GetStatusServlet.Remove" ) + "</th> </tr>" );

        Comparator<CarteObjectEntry> jobComparator = new Comparator<CarteObjectEntry>() {
          @Override
          public int compare( CarteObjectEntry o1, CarteObjectEntry o2 ) {
            Job t1 = getJobMap().getJob( o1 );
            Job t2 = getJobMap().getJob( o2 );
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
          }
        };

        Collections.sort( jobEntries, jobComparator );

        evenRow = true;
        for ( int i = 0; i < jobEntries.size(); i++ ) {
          String name = jobEntries.get( i ).getName();
          String id = jobEntries.get( i ).getId();
          Job job = getJobMap().getJob( jobEntries.get( i ) );
          String status = job.getStatus();

          String removeText;
          if ( job.isFinished() || job.isStopped() ) {
            removeText =
                "<a href=\""
                    + convertContextPath( RemoveJobServlet.CONTEXT_PATH ) + "?name="
                    + URLEncoder.encode( name, "UTF-8" ) + "&id=" + id + "\"> Remove </a>";
          } else {
            removeText = "";
          }

          String trClass = evenRow ? "cellTableEvenRow" : "cellTableOddRow"; // alternating row color
          String tdClass = evenRow ? "cellTableEvenRowCell" : "cellTableOddRowCell";
          evenRow = !evenRow; // flip
          out.print( "<tr onMouseEnter=\"mouseEnterFunction( this, '" + trClass + "' )\" "
              + "onMouseLeave=\"mouseLeaveFunction( this, '" + trClass + "' )\" "
              + "onClick=\"clickFunction( this, '" + trClass + "' )\" "
              + "id=\"j-cellTableRow" + i + "\" class=\"cellTableCell " + trClass + "\">" );
          out.print( "<td onMouseEnter=\"mouseEnterFunction( this, '" + tdClass + "' )\" "
              + "onMouseLeave=\"mouseLeaveFunction( this, '" + tdClass + "' )\" "
              + "onClick=\"clickFunction( this, '" + tdClass + "' )\" "
              + "id=\"j-cellTableFirstCell" + i + "\" class=\"cellTableCell cellTableFirstColumn " + tdClass + "\"><a href=\""
              + convertContextPath( GetJobStatusServlet.CONTEXT_PATH ) + "?name="
              + URLEncoder.encode( name, "UTF-8" ) + "&id=" + id + "\">" + name + "</a></td>" );
          out.print( "<td onMouseEnter=\"mouseEnterFunction( this, '" + tdClass + "' )\" "
              + "onMouseLeave=\"mouseLeaveFunction( this, '" + tdClass + "' )\" "
              + "onClick=\"clickFunction( this, '" + tdClass + "' )\" "
              + "id=\"j-cellTableCell" + i + "\" class=\"cellTableCell " + tdClass +  "\">" + id + "</td>" );
          out.print( "<td onMouseEnter=\"mouseEnterFunction( this, '" + tdClass + "' )\" "
              + "onMouseLeave=\"mouseLeaveFunction( this, '" + tdClass + "' )\" "
              + "onClick=\"clickFunction( this, '" + tdClass + "' )\" "
              + "id=\"j-cellTableCell" + i + "\" class=\"cellTableCell " + tdClass + "\">" + status + "</td>" );
          out.print( "<td onMouseEnter=\"mouseEnterFunction( this, '" + tdClass + "' )\" "
              + "onMouseLeave=\"mouseLeaveFunction( this, '" + tdClass + "' )\" "
              + "onClick=\"clickFunction( this, '" + tdClass + "' )\" "
              + "id=\"j-cellTableCell" + i + "\" class=\"cellTableCell " + tdClass + "\">"
              + ( job.getLogDate() == null ? "-" : XMLHandler.date2string( job.getLogDate() ) ) + "</td>" );
          out.print( "<td onMouseEnter=\"mouseEnterFunction( this, '" + tdClass + "' )\" "
              + "onMouseLeave=\"mouseLeaveFunction( this, '" + tdClass + "' )\" "
              + "onClick=\"clickFunction( this, '" + tdClass + "' )\" "
              + "id=\"j-cellTableLastCell" + i + "\" class=\"cellTableCell cellTableLastColumn " + tdClass + "\">" + removeText + "</td>" );
          out.print( "</tr>" );
        }
        out.print( "</table></table>" );
        out.print( "</div>" ); // end div

      } catch ( Exception ex ) {
        out.println( "<br>" );
        out.println( "<pre>" );
        ex.printStackTrace( out );
        out.println( "</pre>" );
      }

      out.println( "<br>" );
      out.println( "<div class=\"row\">" );
      htmlClass = useLightTheme ? "h3" : "div";
      out.println( "<div><" + htmlClass + " class=\"workspaceHeading\">"
          + BaseMessages.getString( PKG, "GetStatusServlet.ConfigurationDetails.Title" ) + "</" + htmlClass + "></div>" );
      out.println( "<table class=\"pentaho-table\" border=\"" + tableBorder + "\">" );
      out.print( "<tr> <th class=\"cellTableHeader\">"
          + BaseMessages.getString( PKG, "GetStatusServlet.Parameter.Title" ) + "</th> <th class=\"cellTableHeader\">"
          + BaseMessages.getString( PKG, "GetStatusServlet.Value.Title" ) + "</th> </tr>" );

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
        out.print( "<tr> <td class=\"cellTableCell cellTableEvenRowCell cellTableFirstColumn\">"
            + BaseMessages.getString( PKG, "GetStatusServlet.Parameter.MaxLogLines" ) + "</td> <td class=\"cellTableCell cellTableEvenRowCell cellTableLastColumn\">" + maxLines
            + "</td> </tr>" );

        // The max age of log lines
        //
        String maxAge = "";
        if ( serverConfig.getMaxLogTimeoutMinutes() == 0 ) {
          maxAge = BaseMessages.getString( PKG, "GetStatusServlet.NoLimit" );
        } else {
          maxAge = serverConfig.getMaxLogTimeoutMinutes() + BaseMessages.getString( PKG, "GetStatusServlet.Minutes" );
        }
        out.print( "<tr> <td class=\"cellTableCell cellTableEvenRowCell cellTableFirstColumn\">"
            + BaseMessages.getString( PKG, "GetStatusServlet.Parameter.MaxLogLinesAge" ) + "</td> <td class=\"cellTableCell cellTableEvenRowCell cellTableLastColumn\">" + maxAge
            + "</td> </tr>" );

        // The max age of stale objects
        //
        String maxObjAge = "";
        if ( serverConfig.getObjectTimeoutMinutes() == 0 ) {
          maxObjAge = BaseMessages.getString( PKG, "GetStatusServlet.NoLimit" );
        } else {
          maxObjAge = serverConfig.getObjectTimeoutMinutes() + BaseMessages.getString( PKG, "GetStatusServlet.Minutes" );
        }
        out.print( "<tr> <td class=\"cellTableCell cellTableEvenRowCell cellTableFirstColumn\">"
            + BaseMessages.getString( PKG, "GetStatusServlet.Parameter.MaxObjectsAge" ) + "</td> <td class=\"cellTableCell cellTableEvenRowCell cellTableLastColumn\">" + maxObjAge
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
        out.print( "<tr> <td class=\"cellTableCell cellTableEvenRowCell cellTableFirstColumn\">"
            + BaseMessages.getString( PKG, "GetStatusServlet.Parameter.RepositoryName" ) + "</td> <td class=\"cellTableCell cellTableEvenRowCell cellTableLastColumn\">"
            + repositoryName + "</td> </tr>" );

        out.print( "</table>" );

        String filename = serverConfig.getFilename();
        if ( filename == null ) {
          filename = BaseMessages.getString( PKG, "GetStatusServlet.ConfigurationDetails.UsingDefaults" );
        }
        out.println( "</div>" ); // end div
        out.print( "<br>" );
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
      out.println( "var selectedTransRowIndex = -1;" ); // currently selected table item
      out.println( "var selectedJobRowIndex = -1;" ); // currently selected table item

      // OnClick function
      out.println( "function clickFunction( element, tableClass ) {" );
      out.println( "var prefix = element.id.startsWith( 'j-' ) ? 'j-' : '';" );
      out.println( "if( tableClass.endsWith( 'Row' ) ) {" );
      out.println( "element.className='cellTableRow ' + tableClass + ' cellTableSelectedRow';" );
      out.println( "} else {" );
      out.println( "document.getElementById( prefix + 'cellTableFirstCell' + element.id.charAt( element.id.length - 1 ) ).className='cellTableCell cellTableFirstColumn ' + tableClass + ' cellTableSelectedRowCell';" );
      out.println( "element.className='cellTableCell ' + tableClass + ' cellTableSelectedRowCell';" );
      out.println( "}" );
      out.println( "if( element.id.startsWith( 'j-' ) ) {" );
      out.println( "if( selectedJobRowIndex != -1 && element.id.charAt( element.id.length - 1 ) != selectedJobRowIndex ) {" );
      out.println( "document.getElementById( prefix + 'cellTableRow' + selectedJobRowIndex ).className='cellTableRow ' + tableClass;" );
      out.println( "document.getElementById( prefix + 'cellTableFirstCell' + selectedJobRowIndex ).className='cellTableCell cellTableFirstColumn ' + tableClass;" );
      out.println( "document.getElementById( prefix + 'cellTableCell' + selectedJobRowIndex ).className='cellTableCell ' + tableClass;" );
      out.println( "document.getElementById( prefix + 'cellTableLastCell' + selectedJobRowIndex ).className='cellTableCell cellTableLastColumn ' + tableClass;" );
      out.println( "}" );
      out.println( "selectedJobRowIndex = element.id.charAt( element.id.length - 1 );" );
      out.println( "} else {" );
      out.println( "if( selectedTransRowIndex != -1 && element.id.charAt( element.id.length - 1 ) != selectedTransRowIndex ) {" );
      out.println( "document.getElementById( prefix + 'cellTableRow' + selectedTransRowIndex ).className='cellTableRow ' + tableClass;" );
      out.println( "document.getElementById( prefix + 'cellTableFirstCell' + selectedTransRowIndex ).className='cellTableCell cellTableFirstColumn ' + tableClass;" );
      out.println( "document.getElementById( prefix + 'cellTableCell' + selectedTransRowIndex ).className='cellTableCell ' + tableClass;" );
      out.println( "document.getElementById( prefix + 'cellTableLastCell' + selectedTransRowIndex ).className='cellTableCell cellTableLastColumn ' + tableClass;" );
      out.println( "}" );
      out.println( "selectedTransRowIndex = element.id.charAt( element.id.length - 1 );" );
      out.println( "}" );
      out.println( "}" );

      // OnMouseEnter function
      out.println( "function mouseEnterFunction( element, tableClass ) {" );
      out.println( "var prefix = '';" );
      out.println( "var selectedIndex = selectedTransRowIndex;" );
      out.println( "if( element.id.startsWith( 'j-' ) ) {" );
      out.println( "prefix = 'j-';" );
      out.println( "selectedIndex = selectedJobRowIndex;" );
      out.println( "}" );
      out.println( "if( element.id.charAt( element.id.length - 1 ) != selectedIndex ) {" );
      out.println( "if( tableClass.endsWith( 'Row' ) ) {" );
      out.println( "element.className='cellTableRow ' + tableClass + ' cellTableHoveredRow';" );
      out.println( "} else {" );
      out.println( "document.getElementById( prefix + 'cellTableFirstCell' + element.id.charAt( element.id.length - 1 ) ).className='cellTableCell cellTableFirstColumn ' + tableClass + ' cellTableHoveredRowCell';" );
      out.println( "document.getElementById( prefix + 'cellTableCell' + element.id.charAt( element.id.length - 1 ) ).className='cellTableCell ' + tableClass + ' cellTableHoveredRowCell';" );
      out.println( "document.getElementById( prefix + 'cellTableLastCell' + element.id.charAt( element.id.length - 1 ) ).className='cellTableCell cellTableLastColumn ' + tableClass + ' cellTableHoveredRowCell';" );
      out.println( "}" );
      out.println( "}" );
      out.println( "}" );

      // OnMouseLeave function
      out.println( "function mouseLeaveFunction( element, tableClass ) {" );
      out.println( "var prefix = '';" );
      out.println( "var selectedIndex = selectedTransRowIndex;" );
      out.println( "if( element.id.startsWith( 'j-' ) ) {" );
      out.println( "prefix = 'j-';" );
      out.println( "selectedIndex = selectedJobRowIndex;" );
      out.println( "}" );
      out.println( "if( element.id.charAt( element.id.length - 1 ) != selectedIndex ) {" );
      out.println( "if( tableClass.endsWith( 'Row' ) ) {" );
      out.println( "element.className='cellTableRow ' + tableClass;" );
      out.println( "} else {" );
      out.println( "document.getElementById( prefix + 'cellTableFirstCell' + element.id.charAt( element.id.length - 1 ) ).className='cellTableCell cellTableFirstColumn ' + tableClass;" );
      out.println( "document.getElementById( prefix + 'cellTableCell' + element.id.charAt( element.id.length - 1 ) ).className='cellTableCell ' + tableClass;" );
      out.println( "document.getElementById( prefix + 'cellTableLastCell' + element.id.charAt( element.id.length - 1 ) ).className='cellTableCell cellTableLastColumn ' + tableClass;" );
      out.println( "}" );
      out.println( "}" );
      out.println( "}" );

      out.println( "</script>" );

      out.println( "</BODY>" );
      out.println( "</HTML>" );
    }
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
      allThreadsCpuTime += threadMXBean.getThreadCpuTime( threadIds[i] );
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

}
