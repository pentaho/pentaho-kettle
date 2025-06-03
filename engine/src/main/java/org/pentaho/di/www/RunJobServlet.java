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
import java.util.Enumeration;
import java.util.UUID;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.IdNotFoundException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.core.logging.LoggingObjectType;
import org.pentaho.di.core.logging.SimpleLoggingObject;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobAdapter;
import org.pentaho.di.job.JobConfiguration;
import org.pentaho.di.job.JobExecutionConfiguration;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectory;
import org.pentaho.di.repository.RepositoryDirectoryInterface;

public class RunJobServlet extends BaseHttpServlet implements CartePluginInterface {

  private static final long serialVersionUID = 1192413943669836775L;

  private static Class<?> PKG = RunJobServlet.class; // i18n

  public static final String CONTEXT_PATH = "/kettle/runJob";

  private static final String UNAUTHORIZED_ACCESS_TO_REPOSITORY = "The server sent HTTP status code 401";
  private static final String UNABLE_TO_LOAD_JOB = "Unable to load job";

  public RunJobServlet() {
  }

  public RunJobServlet( JobMap jobMap ) {
    super( jobMap );
  }

  /**
 <div id="mindtouch">
    <h1>/kettle/runJob</h1>
    <a name="GET"></a>
    <h2>GET</h2>
    <p>Execute job from enterprise repository. Repository should be configured in Carte xml file.
  Response contains <code>ERROR</code> result if error happened during job execution.</p>

    <p><b>Example Request:</b><br />
    <pre function="syntax.xml">
    GET /kettle/runJob?job=home%2Fadmin%2Fdummy_job&level=Debug
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
    <td>job</td>
    <td>Full path to the job in repository.</td>
    <td>query</td>
    </tr>
    <tr>
    <td>level</td>
    <td>Logging level to be used for job execution (i.e. Debug).</td>
    <td>query</td>
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
        <td>text/xml</td>
      </tr>
    </tbody>
  </table>
    <p>Response contains result of the operation. It is either <code>OK</code> or <code>ERROR</code>.
     If an error occurred during job execution, response also contains information about the error.</p>

    <p><b>Example Response:</b></p>
    <pre function="syntax.xml">
    <webresult>
      <result>OK</result>
      <message>Job started</message>
      <id>05d919b0-74a3-48d6-84d8-afce359d0449</id>
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
      <td>400</td>
      <td>Bad Request: Mandatory parameter job missing</td>
    </tr>
    <tr>
      <td>401</td>
      <td>Unauthorized access to the repository</td>
    </tr>
    <tr>
      <td>404</td>
      <td>Not found: Job not found</td>
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
      logDebug( BaseMessages.getString( PKG, "RunJobServlet.Log.RunJobRequested" ) );
    }

    // Options taken from PAN
    //
    String[] knownOptions = new String[] { "job", "level", };

    String transOption = request.getParameter( "job" );
    String levelOption = request.getParameter( "level" );

    response.setStatus( HttpServletResponse.SC_OK );

    PrintWriter out = response.getWriter();
    SlaveServerConfig serverConfig = transformationMap.getSlaveServerConfig();
    try {
      Repository slaveServerRepository = serverConfig.getRepository();
      if ( slaveServerRepository == null || !slaveServerRepository.isConnected() ) {
        response.setStatus( HttpServletResponse.SC_UNAUTHORIZED );
        out.println( new WebResult( WebResult.STRING_ERROR, BaseMessages.getString(
          PKG, "RunJobServlet.Error.UnableToConnectToRepository", serverConfig.getRepositoryId() ) ) );
        return;
      }

      if ( transOption == null ) {
        response.setStatus( HttpServletResponse.SC_BAD_REQUEST );
        out.println( new WebResult( WebResult.STRING_ERROR, BaseMessages.getString(
          PKG, "RunJobServlet.Error.MissingMandatoryParameterJob" ) ) );
        return;
      }

      final JobMeta jobMeta = loadJob( slaveServerRepository, transOption );

      // Set the servlet parameters as variables in the transformation
      //
      String[] parameters = jobMeta.listParameters();
      Enumeration<?> parameterNames = request.getParameterNames();
      while ( parameterNames.hasMoreElements() ) {
        String parameter = (String) parameterNames.nextElement();
        String[] values = request.getParameterValues( parameter );

        // Ignore the known options. set the rest as variables
        //
        if ( Const.indexOfString( parameter, knownOptions ) < 0 ) {
          // If it's a trans parameter, set it, otherwise simply set the
          // variable
          //
          if ( Const.indexOfString( parameter, parameters ) < 0 ) {
            jobMeta.setVariable( parameter, values[0] );
          } else {
            jobMeta.setParameterValue( parameter, values[0] );
          }
        }
      }

      JobExecutionConfiguration jobExecutionConfiguration = new JobExecutionConfiguration();
      if ( levelOption != null && !isValidLogLevel( levelOption ) ) {
        response.setStatus( HttpServletResponse.SC_BAD_REQUEST );
        out.println( new WebResult( WebResult.STRING_ERROR, BaseMessages.getString(
          PKG, "RunJobServlet.Error.InvalidLogLevel" ) ) );
        return;
      }
      LogLevel logLevel = LogLevel.getLogLevelForCode( levelOption );
      jobExecutionConfiguration.setLogLevel( logLevel );

      // Create new repository connection for this job
      //
      final Repository repository = jobExecutionConfiguration.connectRepository(
          serverConfig.getRepositoryId(), serverConfig.getRepositoryUsername(), serverConfig.getRepositoryPassword() );

      JobConfiguration jobConfiguration = new JobConfiguration( jobMeta, jobExecutionConfiguration );

      String carteObjectId = UUID.randomUUID().toString();
      SimpleLoggingObject servletLoggingObject =
        new SimpleLoggingObject( CONTEXT_PATH, LoggingObjectType.CARTE, null );
      servletLoggingObject.setContainerObjectId( carteObjectId );
      servletLoggingObject.setLogLevel( logLevel );

      // Create the transformation and store in the list...
      //
      final Job job = new Job( repository, jobMeta, servletLoggingObject );

      // Setting variables
      //
      job.initializeVariablesFrom( null );
      job.getJobMeta().setInternalKettleVariables( job );
      job.injectVariables( jobConfiguration.getJobExecutionConfiguration().getVariables() );

      // Also copy the parameters over...
      //
      job.copyParametersFrom( jobMeta );
      job.clearParameters();
      /*
       * String[] parameterNames = job.listParameters(); for (int idx = 0; idx < parameterNames.length; idx++) { // Grab
       * the parameter value set in the job entry // String thisValue =
       * jobExecutionConfiguration.getParams().get(parameterNames[idx]); if (!Utils.isEmpty(thisValue)) { // Set the
       * value as specified by the user in the job entry // jobMeta.setParameterValue(parameterNames[idx], thisValue); }
       * }
       */
      jobMeta.activateParameters();

      job.setSocketRepository( getSocketRepository() );

      JobMap jobMap = getJobMap();

      jobMap.addJob( job.getJobname(), carteObjectId, job, jobConfiguration );


      // Disconnect from the job's repository when the job finishes.
      //
      job.addJobListener( new JobAdapter() {
        public void jobFinished( Job job ) {
          repository.disconnect();
        }
      } );

      String message = "Job '" + job.getJobname() + "' was added to the list with id " + carteObjectId;
      logBasic( message );

      try {
        runJob( job );

        WebResult webResult = new WebResult( WebResult.STRING_OK, "Job started", carteObjectId );
        out.println( webResult.getXML() );
        out.flush();

      } catch ( Exception executionException ) {
        response.setStatus( HttpServletResponse.SC_INTERNAL_SERVER_ERROR );
        String logging = KettleLogStore.getAppender().getBuffer( job.getLogChannelId(), false ).toString();
        out.println( new WebResult( WebResult.STRING_ERROR, BaseMessages.getString(
          PKG, "RunJobServlet.Error.ErrorExecutingJob", serverConfig.getRepositoryId(), logging ) ) );
      }
    } catch ( IdNotFoundException idEx ) {
      response.setStatus( HttpServletResponse.SC_UNAUTHORIZED );
      out.println( new WebResult( WebResult.STRING_ERROR, BaseMessages.getString(
        PKG, "RunJobServlet.Error.UnableToRunJob", serverConfig.getRepositoryId() ) ) );
    } catch ( Exception ex ) {
      if ( ex.getMessage().contains( UNAUTHORIZED_ACCESS_TO_REPOSITORY ) ) {
        response.setStatus( HttpServletResponse.SC_UNAUTHORIZED );
        out.println( new WebResult( WebResult.STRING_ERROR, BaseMessages.getString(
          PKG, "RunJobServlet.Error.UnableToConnectToRepository", serverConfig.getRepositoryId() ) ) );
        return;
      } else if ( ex.getMessage().contains( UNABLE_TO_LOAD_JOB ) ) {
        response.setStatus( HttpServletResponse.SC_NOT_FOUND );
        out.println( new WebResult( WebResult.STRING_ERROR, BaseMessages.getString(
          PKG, "RunJobServlet.Error.UnableToFindJob", serverConfig.getRepositoryId() ) ) );
        return;
      }
      response.setStatus( HttpServletResponse.SC_INTERNAL_SERVER_ERROR );
      out.println( new WebResult( WebResult.STRING_ERROR, BaseMessages.getString(
        PKG, "RunJobServlet.Error.UnexpectedError", Const.CR + Const.getStackTracker( ex ) ) ) );
    }
  }

  protected void runJob( Job job ) {
    // Execute the transformation...
    //
    job.start();
  }

  private JobMeta loadJob( Repository repository, String job ) throws KettleException {

    if ( repository == null ) {
      throw new KettleException( "Repository required." );
    } else {

      synchronized ( repository ) {
        // With a repository we need to load it from /foo/bar/Transformation
        // We need to extract the folder name from the path in front of the
        // name...
        //
        String directoryPath;
        String name;
        int lastSlash = job.lastIndexOf( RepositoryDirectory.DIRECTORY_SEPARATOR );
        if ( lastSlash < 0 ) {
          directoryPath = "/";
          name = job;
        } else {
          directoryPath = job.substring( 0, lastSlash );
          name = job.substring( lastSlash + 1 );
        }
        RepositoryDirectoryInterface directory =
          repository.loadRepositoryDirectoryTree().findDirectory( directoryPath );

        ObjectId jobID = repository.getJobId( name, directory );

        JobMeta transJob = repository.loadJob( jobID, null );
        return transJob;
      }
    }
  }

  private boolean isValidLogLevel( String levelOption ) {
    for ( LogLevel level : LogLevel.values() ) {
      if ( level.getCode().equals( levelOption ) ) {
        return true;
      }
    }
    return false;
  }

  public String toString() {
    return "Run Job";
  }

  public String getService() {
    return CONTEXT_PATH + " (" + toString() + ")";
  }

  @Override
  public String getContextPath() {
    return CONTEXT_PATH;
  }

}
