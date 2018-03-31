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
import java.net.URLEncoder;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.annotations.VisibleForTesting;
import org.owasp.encoder.Encode;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.logging.LoggingObjectType;
import org.pentaho.di.core.logging.SimpleLoggingObject;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobConfiguration;
import org.pentaho.di.www.cache.CarteStatusCache;


public class StartJobServlet extends BaseHttpServlet implements CartePluginInterface {
  private static Class<?> PKG = StartJobServlet.class; // for i18n purposes,
  // needed by
  // Translator2!!

  private static final long serialVersionUID = -8487225953910464032L;

  public static final String CONTEXT_PATH = "/kettle/startJob";

  @VisibleForTesting
  CarteStatusCache cache = CarteStatusCache.getInstance();

  public StartJobServlet() {
  }

  public StartJobServlet( JobMap jobMap ) {
    super( jobMap );
  }

  /**
  <div id="mindtouch">
      <h1>/kettle/startJob</h1>
      <a name="GET"></a>
      <h2>GET</h2>
      <p>Starts the job. If the job cannot be started, an error is returned.</p>
      
      <p><b>Example Request:</b><br />
      <pre function="syntax.xml">
      GET /kettle/startJob/?name=dummy_job&xml=Y
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
      <td>Name of the job to be executed.</td>
      <td>query</td>
      </tr>
      <tr>
      <td>xml</td>
      <td>Boolean flag which sets the output format required. Use <code>Y</code> to receive XML response.</td>
      <td>boolean, optional</td>
      </tr>
      <tr>
      <td>id</td>
      <td>Carte job ID of the job to be executed. This parameter is optional when xml=Y is used.</td>
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
      <message>Job &#x5b;dummy_job&#x5d; was started.</message>
      <id>abd61143-8174-4f27-9037-6b22fbd3e229</id>
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
      logDebug( BaseMessages.getString( PKG, "StartJobServlet.Log.StartJobRequested" ) );
    }

    String jobName = request.getParameter( "name" );
    String id = request.getParameter( "id" );
    boolean useXML = "Y".equalsIgnoreCase( request.getParameter( "xml" ) );

    response.setStatus( HttpServletResponse.SC_OK );

    PrintWriter out = response.getWriter();
    if ( useXML ) {
      response.setContentType( "text/xml" );
      response.setCharacterEncoding( Const.XML_ENCODING );
      out.print( XMLHandler.getXMLHeader( Const.XML_ENCODING ) );
    } else {
      response.setContentType( "text/html;charset=UTF-8" );
      out.println( "<HTML>" );
      out.println( "<HEAD>" );
      out.println( "<TITLE>Start job</TITLE>" );
      out.println( "<META http-equiv=\"Refresh\" content=\"2;url="
        + convertContextPath( GetStatusServlet.CONTEXT_PATH ) + "?name=" + URLEncoder.encode( jobName, "UTF-8" )
        + "\">" );
      out.println( "<META http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">" );
      out.println( "</HEAD>" );
      out.println( "<BODY>" );
    }

    try {
      // ID is optional...
      //
      Job job;
      CarteObjectEntry entry;
      if ( Utils.isEmpty( id ) ) {
        // get the first job that matches...
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
        // First see if this job already ran to completion.
        // If so, we get an exception is we try to start() the job thread
        //
        if ( job.isInitialized() && !job.isActive() ) {
          // Re-create the job from the jobMeta
          //
          // We might need to re-connect to the repository
          //
          if ( job.getRep() != null && !job.getRep().isConnected() ) {
            if ( job.getRep().getUserInfo() != null ) {
              job.getRep().connect(
                job.getRep().getUserInfo().getLogin(), job.getRep().getUserInfo().getPassword() );
            } else {
              job.getRep().connect( null, null );
            }
          }

          cache.remove( job.getLogChannelId() );

          // Create a new job object to start from a sane state. Then replace
          // the new job in the job map
          //
          synchronized ( this ) {
            JobConfiguration jobConfiguration = getJobMap().getConfiguration( jobName );

            String carteObjectId = UUID.randomUUID().toString();
            SimpleLoggingObject servletLoggingObject =
              new SimpleLoggingObject( CONTEXT_PATH, LoggingObjectType.CARTE, null );
            servletLoggingObject.setContainerObjectId( carteObjectId );

            Job newJob = new Job( job.getRep(), job.getJobMeta(), servletLoggingObject );
            newJob.setLogLevel( job.getLogLevel() );

            // Discard old log lines from the old job
            //
            KettleLogStore.discardLines( job.getLogChannelId(), true );

            getJobMap().replaceJob( entry, newJob, jobConfiguration );
            job = newJob;
          }
        }

        runJob( job );

        String message = BaseMessages.getString( PKG, "StartJobServlet.Log.JobStarted", jobName );
        if ( useXML ) {
          out.println( new WebResult( WebResult.STRING_OK, message, id ).getXML() );
        } else {

          out.println( "<H1>" + Encode.forHtml( message ) + "</H1>" );
          out.println( "<a href=\""
            + convertContextPath( GetJobStatusServlet.CONTEXT_PATH ) + "?name="
            + URLEncoder.encode( jobName, "UTF-8" ) + "&id=" + URLEncoder.encode( id, "UTF-8" ) + "\">"
            + BaseMessages.getString( PKG, "JobStatusServlet.BackToJobStatusPage" ) + "</a><p>" );
        }
      } else {
        String message = BaseMessages.getString( PKG, "StartJobServlet.Log.SpecifiedJobNotFound", jobName );
        if ( useXML ) {
          out.println( new WebResult( WebResult.STRING_ERROR, message ) );
        } else {
          out.println( "<H1>" + Encode.forHtml( message ) + "</H1>" );
          out.println( "<a href=\""
            + convertContextPath( GetStatusServlet.CONTEXT_PATH ) + "\">"
            + BaseMessages.getString( PKG, "TransStatusServlet.BackToStatusPage" ) + "</a><p>" );
        }
      }
    } catch ( Exception ex ) {
      if ( useXML ) {
        out.println( new WebResult( WebResult.STRING_ERROR, BaseMessages.getString(
          PKG, "StartJobServlet.Error.UnexpectedError", Const.CR + Const.getStackTracker( ex ) ) ) );
      } else {
        out.println( "<p>" );
        out.println( "<pre>" );
        out.println( Encode.forHtml( Const.getStackTracker( ex ) ) );
        out.println( "</pre>" );
      }
    }

    if ( !useXML ) {
      out.println( "<p>" );
      out.println( "</BODY>" );
      out.println( "</HTML>" );
    }
  }

  public String toString() {
    return "Start job";
  }

  public String getService() {
    return CONTEXT_PATH + " (" + toString() + ")";
  }

  protected void runJob( Job job ) throws KettleException {
    job.start(); // runs the thread in the background...
  }

  public String getContextPath() {
    return CONTEXT_PATH;
  }

}
