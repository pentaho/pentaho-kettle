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

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.Job;
import org.pentaho.di.trans.Trans;

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
      out.println( "</HEAD>" );
      out.println( "<BODY>" );
      out.println( "<H1>" + BaseMessages.getString( PKG, "GetStatusServlet.TopStatus" ) + "</H1>" );

      try {
        out.println( "<table border=\"1\">" );
        out.print( "<tr> <th>"
          + BaseMessages.getString( PKG, "GetStatusServlet.TransName" ) + "</th> <th>"
          + BaseMessages.getString( PKG, "GetStatusServlet.CarteId" ) + "</th> <th>"
          + BaseMessages.getString( PKG, "GetStatusServlet.Status" ) + "</th> <th>"
          + BaseMessages.getString( PKG, "GetStatusServlet.LastLogDate" ) + "</th> <th>"
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

        for ( CarteObjectEntry entry : transEntries ) {
          String name = entry.getName();
          String id = entry.getId();
          Trans trans = getTransformationMap().getTransformation( entry );
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

          out.print( "<tr>" );
          out.print( "<td><a href=\""
            + convertContextPath( GetTransStatusServlet.CONTEXT_PATH ) + "?name="
            + URLEncoder.encode( name, "UTF-8" ) + "&id=" + id + "\">" + name + "</a></td>" );
          out.print( "<td>" + id + "</td>" );
          out.print( "<td>" + status + "</td>" );
          out.print( "<td>"
            + ( trans.getLogDate() == null ? "-" : XMLHandler.date2string( trans.getLogDate() ) ) + "</td>" );
          out.print( "<td>" + removeText + "</td>" );
          out.print( "</tr>" );
        }
        out.print( "</table><p>" );

        out.println( "<table border=\"1\">" );
        out.print( "<tr> <th>"
          + BaseMessages.getString( PKG, "GetStatusServlet.JobName" ) + "</th> <th>"
          + BaseMessages.getString( PKG, "GetStatusServlet.CarteId" ) + "</th> <th>"
          + BaseMessages.getString( PKG, "GetStatusServlet.Status" ) + "</th> <th>"
          + BaseMessages.getString( PKG, "GetStatusServlet.LastLogDate" ) + "</th> <th>"
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

        for ( CarteObjectEntry entry : jobEntries ) {
          String name = entry.getName();
          String id = entry.getId();
          Job job = getJobMap().getJob( entry );
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

          out.print( "<tr>" );
          out.print( "<td><a href=\""
            + convertContextPath( GetJobStatusServlet.CONTEXT_PATH ) + "?name="
            + URLEncoder.encode( name, "UTF-8" ) + "&id=" + id + "\">" + name + "</a></td>" );
          out.print( "<td>" + id + "</td>" );
          out.print( "<td>" + status + "</td>" );
          out.print( "<td>"
            + ( job.getLogDate() == null ? "-" : XMLHandler.date2string( job.getLogDate() ) ) + "</td>" );
          out.print( "<td>" + removeText + "</td>" );
          out.print( "</tr>" );
        }
        out.print( "</table>" );

      } catch ( Exception ex ) {
        out.println( "<p>" );
        out.println( "<pre>" );
        ex.printStackTrace( out );
        out.println( "</pre>" );
      }

      out.println( "<p>" );
      out.println( "<H1>"
        + BaseMessages.getString( PKG, "GetStatusServlet.ConfigurationDetails.Title" ) + "</H1><p>" );
      out.println( "<table border=\"1\">" );
      out.print( "<tr> <th>"
        + BaseMessages.getString( PKG, "GetStatusServlet.Parameter.Title" ) + "</th> <th>"
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
        out.print( "<tr> <td>"
          + BaseMessages.getString( PKG, "GetStatusServlet.Parameter.MaxLogLines" ) + "</td> <td>" + maxLines
          + "</td> </tr>" );

        // The max age of log lines
        //
        String maxAge = "";
        if ( serverConfig.getMaxLogTimeoutMinutes() == 0 ) {
          maxAge = BaseMessages.getString( PKG, "GetStatusServlet.NoLimit" );
        } else {
          maxAge = serverConfig.getMaxLogTimeoutMinutes() + BaseMessages.getString( PKG, "GetStatusServlet.Minutes" );
        }
        out.print( "<tr> <td>"
          + BaseMessages.getString( PKG, "GetStatusServlet.Parameter.MaxLogLinesAge" ) + "</td> <td>" + maxAge
          + "</td> </tr>" );

        // The max age of stale objects
        //
        String maxObjAge = "";
        if ( serverConfig.getObjectTimeoutMinutes() == 0 ) {
          maxObjAge = BaseMessages.getString( PKG, "GetStatusServlet.NoLimit" );
        } else {
          maxObjAge = serverConfig.getObjectTimeoutMinutes() + BaseMessages.getString( PKG, "GetStatusServlet.Minutes" );
        }
        out.print( "<tr> <td>"
          + BaseMessages.getString( PKG, "GetStatusServlet.Parameter.MaxObjectsAge" ) + "</td> <td>" + maxObjAge
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
        out.print( "<tr> <td>"
          + BaseMessages.getString( PKG, "GetStatusServlet.Parameter.RepositoryName" ) + "</td> <td>"
          + repositoryName + "</td> </tr>" );

        out.print( "</table>" );

        String filename = serverConfig.getFilename();
        if ( filename == null ) {
          filename = BaseMessages.getString( PKG, "GetStatusServlet.ConfigurationDetails.UsingDefaults" );
        }
        out
          .println( "<i>"
            + BaseMessages.getString( PKG, "GetStatusServlet.ConfigurationDetails.Advice", filename )
            + "</i><br>" );
      }
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
