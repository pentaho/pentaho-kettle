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

import com.google.common.annotations.VisibleForTesting;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.core.logging.LoggingObjectType;
import org.pentaho.di.core.logging.SimpleLoggingObject;
import org.pentaho.di.core.parameters.UnknownParamException;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.RepositoryPluginType;
import org.pentaho.di.core.util.Utils;
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
import org.pentaho.di.repository.RepositoryOperation;
import org.pentaho.di.repository.RepositorySecurityProvider;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class ExecuteJobServlet extends BaseHttpServlet implements CartePluginInterface {

  public static final String KETTLE_DEFAULT_SERVLET_ENCODING = "KETTLE_DEFAULT_SERVLET_ENCODING";

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

    String encoding = System.getProperty( KETTLE_DEFAULT_SERVLET_ENCODING, null );
    if ( encoding != null && !Utils.isEmpty( encoding.trim() ) ) {
      response.setCharacterEncoding( encoding );
      response.setContentType( "text/html; charset=" + encoding );
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
      if ( ke.getCause() instanceof ExecutionException ) {
        ExecutionException ee = (ExecutionException) ke.getCause();
        if ( ee.getCause() instanceof KettleAuthenticationException ) {
          response.setStatus( HttpServletResponse.SC_UNAUTHORIZED );
          String message = BaseMessages.getString( PKG, "ExecuteJobServlet.Error.Authentication", repOption );
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

    // Let's see if the user has the required Execute Permission
    if ( !checkExecutePermission( repository ) ) {
      response.setStatus( HttpServletResponse.SC_UNAUTHORIZED );
      String message = BaseMessages.getString( PKG, "ExecuteJobServlet.Error.ExecutePermissionRequired" );
      out.println( new WebResult( WebResult.STRING_ERROR, message ) );
      return;
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

    Job job;
    String carteObjectId;
    try {
      // Set the servlet parameters as variables in the job
      setServletParametersAsVariables( request, knownOptions, jobMeta );

      JobExecutionConfiguration jobExecutionConfiguration = new JobExecutionConfiguration();
      LogLevel logLevel = LogLevel.getLogLevelForCode( levelOption );
      jobExecutionConfiguration.setLogLevel( logLevel );
      JobConfiguration jobConfiguration = new JobConfiguration( jobMeta, jobExecutionConfiguration );

      carteObjectId = UUID.randomUUID().toString();
      SimpleLoggingObject servletLoggingObject =
        new SimpleLoggingObject( CONTEXT_PATH, LoggingObjectType.CARTE, null );
      servletLoggingObject.setContainerObjectId( carteObjectId );
      servletLoggingObject.setLogLevel( logLevel );

      // Create the job and store in the list...
      //
      job = new Job( repository, jobMeta, servletLoggingObject );

      job.setRepository( repository );
      job.setSocketRepository( getSocketRepository() );

      getJobMap().addJob( jobMeta.getName(), carteObjectId, job, jobConfiguration );
      job.setContainerObjectId( carteObjectId );

      if ( repository != null ) {
        // The repository connection is open: make sure we disconnect from the repository once we
        // are done with this job.
        //
        job.addJobListener( new JobAdapter() {
          @Override public void jobFinished( Job job ) {
            repository.disconnect();
          }
        } );
      }
    } catch ( Exception ex ) {
      // Something unexpected occurred.
      response.setStatus( HttpServletResponse.SC_INTERNAL_SERVER_ERROR );
      String message = BaseMessages.getString(
        PKG, "ExecuteJobServlet.Error.UnexpectedError", Const.CR + Const.getStackTracker( ex ) );
      out.println( new WebResult( WebResult.STRING_ERROR, message ) );
      return;
    }

    try {
      // Execute the job...
      //
      runJob( job );
      WebResult webResult = new WebResult( WebResult.STRING_OK, "Job started", carteObjectId );
      out.println( webResult.getXML() );
      out.flush();

      // Everything went well till the end.
      response.setStatus( HttpServletResponse.SC_OK );

    } catch ( Exception executionException ) {
      // Something went wrong while running the job.
      response.setStatus( HttpServletResponse.SC_INTERNAL_SERVER_ERROR );

      String logging = KettleLogStore.getAppender().getBuffer( job.getLogChannelId(), false ).toString();
      String message = BaseMessages.getString( PKG, "ExecuteJobServlet.Error.WhileExecutingJob", jobOption, logging );
      out.println( new WebResult( WebResult.STRING_ERROR, message ) );
    }
  }

  /**
   * <p>Takes all Servlet's parameters and, if they haven't been handled, add them either as variables or as parameters
   * to the given job.</p>
   *
   * @param request      the Servlet request
   * @param knownOptions the options already handled
   * @param jobMeta      the Job Meta
   * @throws UnknownParamException
   */
  protected void setServletParametersAsVariables( HttpServletRequest request, String[] knownOptions, JobMeta jobMeta )
    throws UnknownParamException {
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
          jobMeta.setVariable( parameter, values[ 0 ] );
        } else {
          jobMeta.setParameterValue( parameter, values[ 0 ] );
        }
      }
    }
  }

  private JobMeta loadJob( Repository repository, String job ) throws KettleException {

    if ( repository == null ) {

      // Without a repository it's a filename --> file:///foo/bar/job.kjb
      //
      return new JobMeta( job, repository );

    } else {
      // With a repository we need to load it from /foo/bar/Job
      // We need to extract the folder name from the path in front of the name...
      //
      int lastSlash = job.lastIndexOf( RepositoryDirectory.DIRECTORY_SEPARATOR );
      String dirPath = ( lastSlash > 0 ) ? job.substring( 0, lastSlash ) : RepositoryDirectory.DIRECTORY_SEPARATOR;
      String jobName = ( lastSlash < 0 ) ? job : job.substring( lastSlash + 1 );

      RepositoryDirectoryInterface directory = repository.loadRepositoryDirectoryTree().findDirectory( dirPath );
      if ( directory == null ) {
        String message = BaseMessages.getString( PKG, "ExecuteJobServlet.Error.DirectoryPathNotFoundInRepository", dirPath );
        throw new KettleException( message );
      }

      ObjectId jobID = repository.getJobId( jobName, directory );
      if ( jobID == null ) {
        String message = BaseMessages.getString( PKG, "ExecuteJobServlet.Error.JobNotFoundInDirectory", jobName, dirPath );
        throw new KettleException( message );
      }

      return repository.loadJob( jobID, null );
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
    repository.connect( user, pass, false );
    return repository;
  }

  @Override
  public String toString() {
    return "Start job";
  }

  @Override
  public String getService() {
    return CONTEXT_PATH + " (" + toString() + ")";
  }

  protected void runJob( Job job ) {
    // Execute the job...
    //
    job.start();
  }

  protected boolean checkExecutePermission( Repository repository ) {
    boolean ret = false;
    if ( null != repository ) {
      try {
        RepositorySecurityProvider repositorySecurityProvider =
          (RepositorySecurityProvider) repository.getService( RepositorySecurityProvider.class );
        if ( repositorySecurityProvider != null ) {
          repositorySecurityProvider.validateAction( RepositoryOperation.EXECUTE_TRANSFORMATION );
          ret = true;
        }
      } catch ( Exception ex ) {
        ret = false;
      }
    } else {
      ret = true;
    }

    return ret;
  }

  public String getContextPath() {
    return CONTEXT_PATH;
  }
}
