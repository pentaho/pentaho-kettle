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
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.annotations.VisibleForTesting;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.parameters.UnknownParamException;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.core.logging.LoggingObjectType;
import org.pentaho.di.core.logging.SimpleLoggingObject;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.RepositoryPluginType;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobAdapter;
import org.pentaho.di.job.JobConfiguration;
import org.pentaho.di.job.JobExecutionConfiguration;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.repository.KettleAuthenticationException;
import org.pentaho.di.repository.KettleRepositoryNotFoundException;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.RepositoriesMeta;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectory;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.repository.RepositoryMeta;

public class ExecuteJobServlet extends BaseHttpServlet implements CartePluginInterface {

  private static Class<?> PKG = ExecuteJobServlet.class; // i18n

  private static final long serialVersionUID = -5879219287669847357L;

  public static final String CONTEXT_PATH = "/kettle/executeJob";

  public ExecuteJobServlet() {
  }

  public ExecuteJobServlet( JobMap jobMap ) {
    super( jobMap );
  }


  /**
 <div id="mindtouch">
    <h1>/kettle/executeJob</h1>
    <a name="GET"></a>
    <h2>GET</h2>
    <p>Executes job from the specified repository.
  Connects to the repository provided as a parameter, loads the job from it and executes it.
  Empty response is returned or response contains output of an error happened during the job execution.
  Response contains <code>ERROR</code> result if error happened during job execution.</p>

    <p><b>Example Request:</b><br />
    <pre function="syntax.xml">
    GET /kettle/executeJob/?rep=my_repository&user=my_user&pass=my_password&job=my_job&level=INFO
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
    <td>rep</td>
    <td>Repository id to connect to.</td>
    <td>query</td>
    </tr>
    <tr>
    <td>user</td>
    <td>User name to be used to connect to repository.</td>
    <td>query</td>
    </tr>
    <tr>
    <td>pass</td>
    <td>User password to be used to connect to repository.</td>
    <td>query</td>
    </tr>
    <tr>
    <td>job</td>
    <td>Job name to be loaded and executed.</td>
    <td>query</td>
    </tr>
    <tr>
    <td>level</td>
    <td>Logging level to be used for job execution (i.e. Debug).</td>
    <td>query</td>
    </tr>
    <tr>
    <td>*any name*</td>
    <td>All the other parameters will be sent to the job for using as variables.
  When necessary you can add custom parameters to the request.
  They will be used to set the job variables values.</td>
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
        <td>application/xml</td>
      </tr>
    </tbody>
  </table>
    <p>Response contains error output of the job executed or Carte object Id
  if the execution was successful.</p>

    <p><b>Example Error Response:</b></p>
    <pre function="syntax.xml">
  <webresult>
    <result>OK</result>
    <message>Job started</message>
    <id>74d96aa6-f29a-4bac-a26a-06a8c8f107e5</id>
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
      logDebug( BaseMessages.getString( PKG, "ExecuteJobServlet.Log.ExecuteJobRequested" ) );
    }

    // Options taken from PAN
    //
    String[] knownOptions = new String[] { "rep", "user", "pass", "job", "level", };

    String repOption = request.getParameter( "rep" );
    String userOption = request.getParameter( "user" );
    String passOption = Encr.decryptPasswordOptionallyEncrypted( request.getParameter( "pass" ) );
    String jobOption = request.getParameter( "job" );
    String levelOption = request.getParameter( "level" );

    PrintWriter out = response.getWriter();

    Repository repository;
    try {
      repository = openRepository( repOption, userOption, passOption );
    } catch ( KettleRepositoryNotFoundException krnfe ) {
      // Repository not found.
      response.setStatus( HttpServletResponse.SC_NOT_FOUND );
      String message = BaseMessages.getString( PKG, "ExecuteJobServlet.Error.UnableToFindRepository", repOption );
      out.println( new WebResult( WebResult.STRING_ERROR, message ) );
      return;
    } catch ( KettleException ke ) {
      // Authentication Error.
      if (  ke.getCause() instanceof ExecutionException ) {
        ExecutionException ee = (ExecutionException) ke.getCause();
        if (  ee.getCause() instanceof KettleAuthenticationException ) {
          response.setStatus( HttpServletResponse.SC_UNAUTHORIZED );
          String message = BaseMessages.getString( PKG, "ExecuteJobServlet.Error.Authentication", getContextPath() );
          out.println( new WebResult( WebResult.STRING_ERROR, message ) );
          return;
        }
      }

      // Something unexpected occurred.
      response.setStatus( HttpServletResponse.SC_INTERNAL_SERVER_ERROR );
      String message = BaseMessages.getString(
        PKG, "ExecuteJobServlet.Error.UnexpectedError", Const.CR + Const.getStackTracker( ke ) );
      out.println( new WebResult( WebResult.STRING_ERROR, message ) );
      return;
    }

    String encoding = System.getProperty( "KETTLE_DEFAULT_SERVLET_ENCODING", null );
    if ( encoding != null && !Utils.isEmpty( encoding.trim() ) ) {
      response.setCharacterEncoding( encoding );
      response.setContentType( "text/html; charset=" + encoding );
    }

    JobMeta jobMeta;
    try {
      jobMeta = loadJob( repository, jobOption );
    } catch ( KettleException ke ) {
      // Job not found in repository.
      response.setStatus( HttpServletResponse.SC_NOT_FOUND );
      out.println( new WebResult( WebResult.STRING_ERROR, ke.getMessage() ) );
      return;
    }

    // Set the servlet parameters as variables in the job
    //
    String[] parameters = jobMeta.listParameters();
    Enumeration<?> parameterNames = request.getParameterNames();
    while ( parameterNames.hasMoreElements() ) {
      String parameter = (String) parameterNames.nextElement();
      String[] values = request.getParameterValues( parameter );

      // Ignore the known options. set the rest as variables
      //
      if ( Const.indexOfString( parameter, knownOptions ) < 0 ) {
        // If it's a job parameter, set it, otherwise simply set the variable
        //
        if ( Const.indexOfString( parameter, parameters ) < 0 ) {
          jobMeta.setVariable( parameter, values[0] );
        } else {
          try {
            jobMeta.setParameterValue( parameter, values[0] );
          } catch ( UnknownParamException upe ) {
            // Unknown parameter is unexpected.
            response.setStatus( HttpServletResponse.SC_INTERNAL_SERVER_ERROR );
            String message = BaseMessages.getString(
              PKG, "ExecuteJobServlet.Error.UnexpectedError", Const.CR + Const.getStackTracker( upe ) );
            out.println( new WebResult( WebResult.STRING_ERROR, message ) );
          }
        }
      }
    }

    JobExecutionConfiguration jobExecutionConfiguration = new JobExecutionConfiguration();
    LogLevel logLevel = LogLevel.getLogLevelForCode( levelOption );
    jobExecutionConfiguration.setLogLevel( logLevel );
    JobConfiguration jobConfiguration = new JobConfiguration( jobMeta, jobExecutionConfiguration );

    String carteObjectId = UUID.randomUUID().toString();
    SimpleLoggingObject servletLoggingObject =
      new SimpleLoggingObject( CONTEXT_PATH, LoggingObjectType.CARTE, null );
    servletLoggingObject.setContainerObjectId( carteObjectId );
    servletLoggingObject.setLogLevel( logLevel );

    // Create the job and store in the list...
    //
    final Job job = new Job( repository, jobMeta, servletLoggingObject );

    job.setRepository( repository );
    job.setSocketRepository( getSocketRepository() );

    getJobMap().addJob( jobMeta.getName(), carteObjectId, job, jobConfiguration );
    job.setContainerObjectId( carteObjectId );

    if ( repository != null ) {
      // The repository connection is open: make sure we disconnect from the repository once we
      // are done with this job.
      //
      Repository finalRepository = repository;
      job.addJobListener( new JobAdapter() {
        @Override public void jobFinished( Job job ) {
          finalRepository.disconnect();
        }
      } );
    }

    try {
      runJob( job );
      WebResult webResult = new WebResult( WebResult.STRING_OK, "Job started", carteObjectId );
      out.println( webResult.getXML() );
      out.flush();

    } catch ( Exception executionException ) {
      // Something went wrong while running the job.
      response.setStatus( HttpServletResponse.SC_INTERNAL_SERVER_ERROR );

      String logging = KettleLogStore.getAppender().getBuffer( job.getLogChannelId(), false ).toString();
      String message = BaseMessages.getString( PKG, "ExecuteJobServlet.Error.WhileExecutingJob", jobOption, logging );
      out.println( new WebResult( WebResult.STRING_ERROR, message ) );

      return;
    }

    // Everything went well till the end.
    response.setStatus( HttpServletResponse.SC_OK );
  }

  private JobMeta loadJob( Repository repository, String job ) throws KettleException {

    if ( repository == null ) {

      // Without a repository it's a filename --> file:///foo/bar/job.kjb
      //
      JobMeta jobMeta = new JobMeta( job, repository );
      return jobMeta;

    } else {

      // With a repository we need to load it from /foo/bar/Job
      // We need to extract the folder name from the path in front of the name...
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
      if ( directory == null ) {
        String message = BaseMessages.getString( PKG, "ExecuteJobServlet.Error.DirectoryPathNotFoundInRepository", directoryPath );
        throw new KettleException( message );
      }

      ObjectId jobID = repository.getJobId( name, directory );
      if ( jobID == null ) {
        String message = BaseMessages.getString( PKG, "ExecuteJobServlet.Error.JobNotFoundInDirectory", name, directoryPath );
        throw new KettleException( message );
      }
      JobMeta jobMeta = repository.loadJob( jobID, null );
      return jobMeta;
    }
  }

  @VisibleForTesting
  Repository openRepository( String repositoryName, String user, String pass ) throws KettleException {

    if ( Utils.isEmpty( repositoryName ) ) {
      return null;
    }

    RepositoriesMeta repositoriesMeta = new RepositoriesMeta();
    repositoriesMeta.readData();
    RepositoryMeta repositoryMeta = repositoriesMeta.findRepository( repositoryName );
    if ( repositoryMeta == null ) {
      String message = BaseMessages.getString( PKG, "ExecuteJobServlet.Error.UnableToFindRepository", repositoryName );
      throw new KettleRepositoryNotFoundException( message );
    }
    PluginRegistry registry = PluginRegistry.getInstance();
    Repository repository = registry.loadClass( RepositoryPluginType.class, repositoryMeta, Repository.class );
    repository.init( repositoryMeta );
    repository.connect( user, pass );
    return repository;
  }

  public String toString() {
    return "Start job";
  }

  public String getService() {
    return CONTEXT_PATH + " (" + toString() + ")";
  }

  protected void runJob( Job job ) {
    // Execute the job...
    //
    job.start();
  }

  public String getContextPath() {
    return CONTEXT_PATH;
  }

}
