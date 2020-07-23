/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2020 by Hitachi Vantara : http://www.pentaho.com
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
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
  private static Class<?> PKG = StartJobServlet.class; // for i18n purposes, needed by Translator2!!

  private static final long serialVersionUID = -8487225953910464032L;

  public static final String CONTEXT_PATH = "/kettle/startJob";
  // Used when we need to refer to other servlets
  private static final String CONTEXT_PATH_2_ROOT = "..";
  // Value to be used on the http-equiv/refresh
  private static final int REFRESH_PERIOD_VALUE = 10;

  public static final String KETTLE_DEFAULT_SERVLET_ENCODING = "KETTLE_DEFAULT_SERVLET_ENCODING";

  // Servlet parameters
  public static final String PARM_JOB_NAME = "name";
  public static final String PARM_JOB_ID = "id";
  public static final String PARM_USE_XML_OUTPUT = "xml";

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
  @Override
  public void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException {
    if ( isJettyMode() && !request.getContextPath().startsWith( CONTEXT_PATH ) ) {
      return;
    }

    if ( log.isDebug() ) {
      logDebug( BaseMessages.getString( PKG, "StartJobServlet.Log.StartJobRequested" ) );
    }

    // Set the response encoding
    String encoding = setResponseEncoding( response );

    // Collect parameters
    String jobName = getParameter( request, PARM_JOB_NAME );
    String jobId = getParameter( request, PARM_JOB_ID );
    boolean useXML = "Y".equalsIgnoreCase( getParameter( request, PARM_USE_XML_OUTPUT ) );

    PrintWriter out = response.getWriter();

    // jobName is mandatory
    if ( null == jobName ) {
      response.setStatus( HttpServletResponse.SC_BAD_REQUEST );

      String message = BaseMessages.getString( PKG, "StartJobServlet.Error.MissingMandatoryParameter", PARM_JOB_NAME );

      if ( useXML ) {
        out.print( XMLHandler.getXMLHeader( encoding ) );
        out.println( new WebResult( WebResult.STRING_ERROR, message ) );
      } else {
        printHtmlHeader( out, null, encoding );
        printHtmlMessage( out, message );
        printHtmlFooter( out );
      }

      return;
    }

    // Get the CarteObjectEntry that corresponds to the given job name/id and, then, the job itself
    CarteObjectEntry entry = null;
    Job job = null;

    entry = getCarteObjectEntry( jobName, jobId );
    if ( null != entry ) {
      // Guarantee that both name and id are correct
      jobName = entry.getName();
      jobId = entry.getId();

      // Get the job
      job = getJobMap().getJob( entry );
    }

    try {
      if ( job != null ) {
        // Has this job already ran to completion?
        if ( job.isInitialized() && !job.isActive() ) {
          // An exception will be thrown if we try to start() the job thread
          job = recreateJob( entry, job );
        }

        // Start the job
        runJob( job );

        // All went well
        response.setStatus( HttpServletResponse.SC_OK );

        String message = BaseMessages.getString( PKG, "StartJobServlet.Log.JobStarted", jobName );

        if ( useXML ) {
          out.print( XMLHandler.getXMLHeader( encoding ) );
          out.println( new WebResult( WebResult.STRING_OK, message, jobId ).getXML() );
        } else {
          printHtmlHeader( out, entry, encoding );
          printHtmlMessage( out, message );
          out.println( "<a href=\"" + convertContextPath2local( GetJobStatusServlet.CONTEXT_PATH )
            + '?' + GetJobStatusServlet.PARM_JOB_NAME
            + '=' + URLEncoder.encode( jobName, encoding )
            + '&' + GetJobStatusServlet.PARM_JOB_ID
            + '=' + URLEncoder.encode( jobId, encoding ) + "\">"
            + BaseMessages.getString( PKG, "JobStatusServlet.BackToJobStatusPage" ) + "</a><p/>" );
          printHtmlFooter( out );
        }
      } else {
        String message = BaseMessages.getString( PKG, "StartJobServlet.Log.SpecifiedJobNotFound", jobName, jobId );
        response.setStatus( HttpServletResponse.SC_NOT_FOUND );

        if ( useXML ) {
          out.print( XMLHandler.getXMLHeader( encoding ) );
          out.println( new WebResult( WebResult.STRING_ERROR, message ) );
        } else {
          printHtmlHeader( out, null, encoding );
          printHtmlMessage( out, message );
          out.println( "<a href=\"" + convertContextPath2local( GetStatusServlet.CONTEXT_PATH ) + "\">"
            + BaseMessages.getString( PKG, "TransStatusServlet.BackToStatusPage" ) + "</a><p/>" );
          printHtmlFooter( out );
        }
      }
    } catch ( Exception ex ) {
      response.setStatus( HttpServletResponse.SC_INTERNAL_SERVER_ERROR );

      if ( useXML ) {
        out.print( XMLHandler.getXMLHeader( encoding ) );
        out.println( new WebResult( WebResult.STRING_ERROR, BaseMessages.getString(
          PKG, "StartJobServlet.Error.UnexpectedError", Const.CR + Const.getStackTracker( ex ) ) ) );
      } else {
        printHtmlHeader( out, null, encoding );
        out.println( "<p/><pre>" );
        out.println( Encode.forHtml( Const.getStackTracker( ex ) ) );
        out.println( "</pre>" );
        printHtmlFooter( out );
      }
    }
  }

  /**
   * <p>Recreates a given job from its jobMeta so that it starts from a sane state.</p>
   * <p>The old job will be replaced in the job map.</p>
   *
   * @param entry the CarteObjectEntry corresponding to the job to be recreated
   * @param job   the job to be recreated
   * @return the recreated job
   * @throws KettleException
   */
  protected Job recreateJob( CarteObjectEntry entry, Job job ) throws KettleException {
    Job newJob = null;

    // We might need to re-connect to the repository
    if ( job.getRep() != null && !job.getRep().isConnected() ) {
      if ( job.getRep().getUserInfo() != null ) {
        job.getRep().connect(
          job.getRep().getUserInfo().getLogin(), job.getRep().getUserInfo().getPassword() );
      } else {
        job.getRep().connect( null, null );
      }
    }

    CarteStatusCache.getInstance().remove( job.getLogChannelId() );

    synchronized ( this ) {
      JobConfiguration jobConfiguration = getJobMap().getConfiguration( entry.getName() );

      String carteObjectId = UUID.randomUUID().toString();
      SimpleLoggingObject servletLoggingObject =
        new SimpleLoggingObject( CONTEXT_PATH, LoggingObjectType.CARTE, null );
      servletLoggingObject.setContainerObjectId( carteObjectId );

      // Create the new job
      newJob = new Job( job.getRep(), job.getJobMeta(), servletLoggingObject );
      newJob.setLogLevel( job.getLogLevel() );

      // Discard old log lines from the old job
      KettleLogStore.discardLines( job.getLogChannelId(), true );

      // Replace the new job in the job map
      getJobMap().replaceJob( entry, newJob, jobConfiguration );
    }

    return newJob;
  }

  /**
   * <p>Prints the header section of the HTML response page.</p>
   * <p>If information on the job being handled is given, a refresh instruction pointing to the job status page will be generated.</p>
   *
   * @param printWriter the {@link PrintWriter} instance to write to
   * @param entry       information on the job being handled
   * @throws UnsupportedEncodingException
   */
  private void printHtmlHeader( PrintWriter printWriter, CarteObjectEntry entry, String encoding ) throws UnsupportedEncodingException {
    printWriter.println( "<html>" );
    printWriter.println( "<head>" );
    printWriter.println( "<title>Start job</title>" );
    if ( null != entry && null != entry.getName() && null != entry.getId() ) {
      printWriter.println( "<meta http-equiv=\"refresh\" content=\"" + REFRESH_PERIOD_VALUE
              + ";url=" + convertContextPath2local( GetJobStatusServlet.CONTEXT_PATH )
              + '?' + GetJobStatusServlet.PARM_JOB_NAME
              + '=' + URLEncoder.encode( entry.getName(), encoding )
              + '&' + GetJobStatusServlet.PARM_JOB_ID
              + '=' + URLEncoder.encode( entry.getId(), encoding )
              + "\">" );
    }
    printWriter.println( "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=" + encoding + "\">" );
    printWriter.println( "</head>" );
    printWriter.println( "<body>" );
  }

  private void printHtmlMessage( PrintWriter printWriter, String message ) {
    printWriter.println( "<h1>" + Encode.forHtml( message ) + "</h1>" );
  }

  /**
   * <p>Prints the bottom section of the HTML response page.</p>
   *
   * @param printWriter the {@link PrintWriter} instance to write to
   */
  private void printHtmlFooter( PrintWriter printWriter ) {
    printWriter.println( "<p/>" );
    printWriter.println( "</body>" );
    printWriter.println( "</html>" );
  }

  @Override
  public String toString() {
    return "Start job";
  }

  @Override
  public String getService() {
    return getContextPath() + " (" + toString() + ')';
  }

  protected void runJob( Job job ) throws KettleException {
    job.start(); // runs the thread in the background...
  }

  @Override
  public String getContextPath() {
    return CONTEXT_PATH;
  }

  /**
   * <p>Sets the response encoding.</p>
   * <p>By default {@value Const#XML_ENCODING} is used, but it can be overrided via a system property ({@value #KETTLE_DEFAULT_SERVLET_ENCODING}). </p>
   *
   * @param response the response object
   * @return the encoding used
   */
  protected String setResponseEncoding( HttpServletResponse response ) {
    String encoding = System.getProperty( KETTLE_DEFAULT_SERVLET_ENCODING, Const.XML_ENCODING );

    if ( Utils.isEmpty( encoding ) || Utils.isEmpty( encoding.trim() ) ) {
      encoding = Const.XML_ENCODING;
    }

    response.setCharacterEncoding( encoding );
    response.setContentType( "text/html; charset=" + encoding );

    return encoding;
  }

  /**
   * <p>Returns the value of the specified parameter.</p>
   * <p>The value is trimmed and, if empty, <code>null</code> is returned.</p>
   *
   * @param request       the request instance
   * @param parameterName the parameter name
   * @return the trimmed parameter value or null if empty
   */
  protected String getParameter( HttpServletRequest request, String parameterName ) {
    String paramValue = request.getParameter( parameterName );

    if ( !Utils.isEmpty( paramValue ) ) {
      paramValue = paramValue.trim();

      if ( Utils.isEmpty( paramValue ) ) {
        paramValue = null;
      }
    } else {
      paramValue = null;
    }

    return paramValue;
  }

  /**
   * <p>Returns the Carte Object that corresponds to a given job name and/or job id.</p>
   *
   * @param jobName the name of the job to find
   * @param jobId the id of the job to find
   * @return the entry that corresponds to the given name/id
   */
  protected CarteObjectEntry getCarteObjectEntry( String jobName, String jobId ) {
    CarteObjectEntry carteObjectEntry = null;

    if ( Utils.isEmpty( jobId ) ) {
      // No id, get the first job that matches the name...
      carteObjectEntry = getJobMap().getFirstCarteObjectEntry( jobName );
    } else {
      // Use the id to get the job!
      carteObjectEntry = new CarteObjectEntry( null, jobId );

      Job job = getJobMap().getJob( carteObjectEntry );

      if ( null != job ) {
        // Set the job name
        carteObjectEntry.setName( job.getJobname() );
      } else {
        // Not found
        carteObjectEntry = null;
      }
    }

    return carteObjectEntry;
  }

  /**
   * <p>Converts the context path for other servlets of this API, so
   * that it can be used on links on HTML responses.</p>
   *
   * @param contextPath the other servlet' context path
   * @return the converted context
   */
  private String convertContextPath2local( String contextPath ) {
    if ( '/' == contextPath.charAt( 0 ) ) {
      contextPath = CONTEXT_PATH_2_ROOT + contextPath;
    }
    return contextPath;
  }
}
