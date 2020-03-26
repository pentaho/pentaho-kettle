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
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransAdapter;
import org.pentaho.di.trans.TransConfiguration;
import org.pentaho.di.trans.TransExecutionConfiguration;
import org.pentaho.di.trans.TransMeta;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class ExecuteTransServlet extends BaseHttpServlet implements CartePluginInterface {

  public static final String KETTLE_DEFAULT_SERVLET_ENCODING = "KETTLE_DEFAULT_SERVLET_ENCODING";

  private static Class<?> PKG = ExecuteTransServlet.class; // i18n

  private static final long serialVersionUID = -5879219287669847357L;

  public static final String CONTEXT_PATH = "/kettle/executeTrans";

  public ExecuteTransServlet() {
  }

  public ExecuteTransServlet( TransformationMap transformationMap ) {
    super( transformationMap );
  }


  /**
 <div id="mindtouch">
    <h1>/kettle/executeTrans</h1>
    <a name="GET"></a>
    <h2>GET</h2>
    <p>Executes transformation from the specified repository.
  Connects to the repository provided as a parameter, loads the transformation from it and executes it.
  Empty response is returned or response contains output of an error happened during the transformation execution.
  Response contains <code>ERROR</code> result if error happened during transformation execution.</p>

    <p><b>Example Request:</b><br />
    <pre function="syntax.xml">
    GET /kettle/executeTrans/?rep=my_repository&user=my_user&pass=my_password&trans=my_trans&level=INFO
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
    <td>trans</td>
    <td>Transfromation name to be loaded and executed.</td>
    <td>query</td>
    </tr>
    <tr>
    <td>level</td>
    <td>Logging level to be used for transformation execution (i.e. Debug).</td>
    <td>query</td>
    </tr>
    <tr>
    <td>*any name*</td>
    <td>All the other parameters will be sent to the transformation for using as variables.
  When necessary you can add custom parameters to the request.
  They will be used to set the transformation variables values..</td>
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
    <p>Response contains error output of the transformation executed or nothing
  if the execution was successful.</p>

    <p><b>Example Error Response:</b></p>
    <pre function="syntax.xml">
  <webresult>
    <result>ERROR</result>
    <message>Unexpected error executing the transformation&#x3a;
    &#xd;&#xa;org.pentaho.di.core.exception.KettleException&#x3a;
    &#xd;&#xa;Unable to find transformation &#x27;dummy-trans.ktr&#x27; in directory
    &#x3a;&#x2f;home&#x2f;admin&#xd;&#xa;&#xd;&#xa; at
    org.pentaho.di.www.ExecuteTransServlet.loadTransformation&#x28;ExecuteTransServlet.java&#x3a;214&#x29;&#xd;&#xa;
    at org.pentaho.di.www.ExecuteTransServlet.doGet&#x28;ExecuteTransServlet.java&#x3a;104&#x29;&#xd;&#xa;
    at javax.servlet.http.HttpServlet.service&#x28;HttpServlet.java&#x3a;707&#x29;&#xd;&#xa;
    at javax.servlet.http.HttpServlet.service&#x28;HttpServlet.java&#x3a;820&#x29;&#xd;&#xa;
    at org.mortbay.jetty.servlet.ServletHolder.handle&#x28;ServletHolder.java&#x3a;511&#x29;&#xd;&#xa;
    at org.mortbay.jetty.servlet.ServletHandler.handle&#x28;ServletHandler.java&#x3a;390&#x29;&#xd;&#xa;
    at org.mortbay.jetty.servlet.SessionHandler.handle&#x28;SessionHandler.java&#x3a;182&#x29;&#xd;&#xa;
    at org.mortbay.jetty.handler.ContextHandler.handle&#x28;ContextHandler.java&#x3a;765&#x29;&#xd;&#xa;
    at org.mortbay.jetty.handler.ContextHandlerCollection.handle&#x28;ContextHandlerCollection.java&#x3a;230&#x29;&#xd;&#xa;
    at org.mortbay.jetty.handler.HandlerCollection.handle&#x28;HandlerCollection.java&#x3a;114&#x29;&#xd;&#xa;
    at org.mortbay.jetty.handler.HandlerWrapper.handle&#x28;HandlerWrapper.java&#x3a;152&#x29;&#xd;&#xa;
    at org.mortbay.jetty.Server.handle&#x28;Server.java&#x3a;326&#x29;&#xd;&#xa;
    at org.mortbay.jetty.HttpConnection.handleRequest&#x28;HttpConnection.java&#x3a;536&#x29;&#xd;&#xa;
    at org.mortbay.jetty.HttpConnection&#x24;RequestHandler.headerComplete&#x28;HttpConnection.java&#x3a;915&#x29;&#xd;&#xa;
    at org.mortbay.jetty.HttpParser.parseNext&#x28;HttpParser.java&#x3a;539&#x29;&#xd;&#xa;
    at org.mortbay.jetty.HttpParser.parseAvailable&#x28;HttpParser.java&#x3a;212&#x29;&#xd;&#xa;
    at org.mortbay.jetty.HttpConnection.handle&#x28;HttpConnection.java&#x3a;405&#x29;&#xd;&#xa;
    at org.mortbay.jetty.bio.SocketConnector&#x24;Connection.run&#x28;SocketConnector.java&#x3a;228&#x29;&#xd;&#xa;
    at org.mortbay.thread.QueuedThreadPool&#x24;PoolThread.run&#x28;QueuedThreadPool.java&#x3a;582&#x29;&#xd;&#xa;
    </message>
    <id/>
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
      logDebug( BaseMessages.getString( PKG, "ExecuteTransServlet.Log.ExecuteTransRequested" ) );
    }

    String encoding = System.getProperty( KETTLE_DEFAULT_SERVLET_ENCODING, null );
    if ( encoding != null && !Utils.isEmpty( encoding.trim() ) ) {
      response.setCharacterEncoding( encoding );
      response.setContentType( "text/html; charset=" + encoding );
    }

    // Options taken from PAN
    //
    String[] knownOptions = new String[] { "rep", "user", "pass", "trans", "level", };

    String repOption = request.getParameter( "rep" );
    String userOption = request.getParameter( "user" );
    String passOption = Encr.decryptPasswordOptionallyEncrypted( request.getParameter( "pass" ) );
    String transOption = request.getParameter( "trans" );
    String levelOption = request.getParameter( "level" );

    PrintWriter out = response.getWriter();

    Repository repository;
    try {
      repository = openRepository( repOption, userOption, passOption );
    } catch ( KettleRepositoryNotFoundException krnfe ) {
      // Repository not found.
      response.setStatus( HttpServletResponse.SC_NOT_FOUND );
      String message = BaseMessages.getString( PKG, "ExecuteTransServlet.Error.UnableToFindRepository", repOption );
      out.println( new WebResult( WebResult.STRING_ERROR, message ) );
      return;
    } catch ( KettleException ke ) {
      // Authentication Error.
      if ( ke.getCause() instanceof ExecutionException ) {
        ExecutionException ee = (ExecutionException) ke.getCause();
        if ( ee.getCause() instanceof KettleAuthenticationException ) {
          response.setStatus( HttpServletResponse.SC_UNAUTHORIZED );
          String message = BaseMessages.getString( PKG, "ExecuteTransServlet.Error.Authentication", repOption );
          out.println( new WebResult( WebResult.STRING_ERROR, message ) );
          return;
        }
      }

      // Something unexpected occurred.
      response.setStatus( HttpServletResponse.SC_INTERNAL_SERVER_ERROR );
      String message = BaseMessages.getString(
        PKG, "ExecuteTransServlet.Error.UnexpectedError", Const.CR + Const.getStackTracker( ke ) );
      out.println( new WebResult( WebResult.STRING_ERROR, message ) );
      return;
    }

    // Let's see if the user has the required Execute Permission
    if ( !checkExecutePermission( repository ) ) {
      response.setStatus( HttpServletResponse.SC_UNAUTHORIZED );
      String message = BaseMessages.getString( PKG, "ExecuteTransServlet.Error.ExecutePermissionRequired" );
      out.println( new WebResult( WebResult.STRING_ERROR, message ) );
      return;
    }

    TransMeta transMeta;
    try {
      transMeta = loadTransformation( repository, transOption );
    } catch ( KettleException ke ) {
      // Job not found in repository.
      response.setStatus( HttpServletResponse.SC_NOT_FOUND );
      out.println( new WebResult( WebResult.STRING_ERROR, ke.getMessage() ) );
      return;
    }

    Trans trans;
    String carteObjectId;
    try {
      // Set the servlet parameters as variables in the transformation
      setServletParametersAsVariables( request, knownOptions, transMeta );

      TransExecutionConfiguration transExecutionConfiguration = new TransExecutionConfiguration();
      LogLevel logLevel = LogLevel.getLogLevelForCode( levelOption );
      transExecutionConfiguration.setLogLevel( logLevel );
      TransConfiguration transConfiguration = new TransConfiguration( transMeta, transExecutionConfiguration );

      carteObjectId = UUID.randomUUID().toString();
      SimpleLoggingObject servletLoggingObject =
        new SimpleLoggingObject( CONTEXT_PATH, LoggingObjectType.CARTE, null );
      servletLoggingObject.setContainerObjectId( carteObjectId );
      servletLoggingObject.setLogLevel( logLevel );

      // Create the transformation and store in the list...
      //
      trans = new Trans( transMeta, servletLoggingObject );

      trans.setRepository( repository );
      trans.setSocketRepository( getSocketRepository() );

      getTransformationMap().addTransformation( transMeta.getName(), carteObjectId, trans, transConfiguration );
      trans.setContainerObjectId( carteObjectId );

      // Pass the servlet print writer to the transformation...
      //
      trans.setServletPrintWriter( out );
      trans.setServletReponse( response );
      trans.setServletRequest( request );

      if ( repository != null ) {
        // The repository connection is open: make sure we disconnect from the repository once we
        // are done with this transformation.
        //
        trans.addTransListener( new TransAdapter() {
          @Override public void transFinished( Trans trans ) {
            repository.disconnect();
          }
        } );
      }
    } catch ( Exception ex ) {
      // Something unexpected occurred.
      response.setStatus( HttpServletResponse.SC_INTERNAL_SERVER_ERROR );
      String message = BaseMessages.getString(
        PKG, "ExecuteTransServlet.Error.UnexpectedError", Const.CR + Const.getStackTracker( ex ) );
      out.println( new WebResult( WebResult.STRING_ERROR, message ) );
      return;
    }

    try {
      // Execute the transformation...
      //
      executeTrans( trans );
      out.flush();

      // Everything went well till the end.
      response.setStatus( HttpServletResponse.SC_OK );

    } catch ( Exception executionException ) {
      // Something went wrong while running the job.
      response.setStatus( HttpServletResponse.SC_INTERNAL_SERVER_ERROR );

      String logging = KettleLogStore.getAppender().getBuffer( trans.getLogChannelId(), false ).toString();
      String message =
        BaseMessages.getString( PKG, "ExecuteTransServlet.Error.WhileExecutingTrans", transOption, logging );
      out.println( new WebResult( WebResult.STRING_ERROR, message ) );
    }
  }

  /**
   * <p>Takes all Servlet's parameters and, if they haven't been handled, add them either as variables or as parameters
   * to the given transformation.</p>
   *
   * @param request      the Servlet request
   * @param knownOptions the options already handled
   * @param transMeta    the Transformation Meta
   * @throws UnknownParamException
   */
  protected void setServletParametersAsVariables( HttpServletRequest request, String[] knownOptions,
                                                  TransMeta transMeta ) throws UnknownParamException {
    String[] parameters = transMeta.listParameters();
    Enumeration<?> parameterNames = request.getParameterNames();
    while ( parameterNames.hasMoreElements() ) {
      String parameter = (String) parameterNames.nextElement();
      String[] values = request.getParameterValues( parameter );

      // Ignore the known options. set the rest as variables
      //
      if ( Const.indexOfString( parameter, knownOptions ) < 0 ) {
        // If it's a trans parameter, set it, otherwise simply set the variable
        //
        if ( Const.indexOfString( parameter, parameters ) < 0 ) {
          transMeta.setVariable( parameter, values[ 0 ] );
        } else {
          transMeta.setParameterValue( parameter, values[ 0 ] );
        }
      }
    }
  }

  private TransMeta loadTransformation( Repository repository, String trans ) throws KettleException {

    if ( repository == null ) {

      // Without a repository it's a filename --> file:///foo/bar/trans.ktr
      //
      return new TransMeta( trans );

    } else {
      // With a repository we need to load it from /foo/bar/Transformation
      // We need to extract the folder name from the path in front of the name...
      //
      int lastSlash = trans.lastIndexOf( RepositoryDirectory.DIRECTORY_SEPARATOR );
      String dirPath = ( lastSlash > 0 ) ? trans.substring( 0, lastSlash ) : RepositoryDirectory.DIRECTORY_SEPARATOR;
      String transName = ( lastSlash < 0 ) ? trans : trans.substring( lastSlash + 1 );

      RepositoryDirectoryInterface directory = repository.loadRepositoryDirectoryTree().findDirectory( dirPath );
      if ( directory == null ) {
        String message = BaseMessages.getString( PKG, "ExecuteTransServlet.Error.DirectoryPathNotFoundInRepository", dirPath );
        throw new KettleException( message );
      }

      ObjectId transformationID = repository.getTransformationID( transName, directory );
      if ( transformationID == null ) {
        String message = BaseMessages.getString( PKG, "ExecuteTransServlet.Error.TransformationNotFoundInDirectory", transName, dirPath );
        throw new KettleException( message );
      }

      return repository.loadTransformation( transformationID, null );
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
      String message = BaseMessages.getString( PKG, "ExecuteTransServlet.Error.UnableToFindRepository", repositoryName );
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
    return "Start transformation";
  }

  @Override
  public String getService() {
    return CONTEXT_PATH + " (" + toString() + ")";
  }

  protected void executeTrans( Trans trans ) throws KettleException {
    trans.prepareExecution( null );
    trans.startThreads();
    trans.waitUntilFinished();
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
