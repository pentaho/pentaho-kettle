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
import java.util.Enumeration;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.pentaho.di.core.Const;
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
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.RepositoriesMeta;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectory;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.repository.RepositoryMeta;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransAdapter;
import org.pentaho.di.trans.TransConfiguration;
import org.pentaho.di.trans.TransExecutionConfiguration;
import org.pentaho.di.trans.TransMeta;

public class ExecuteTransServlet extends BaseHttpServlet implements CartePluginInterface {

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

    // Options taken from PAN
    //
    String[] knownOptions = new String[] { "rep", "user", "pass", "trans", "level", };

    String repOption = request.getParameter( "rep" );
    String userOption = request.getParameter( "user" );
    String passOption = Encr.decryptPasswordOptionallyEncrypted( request.getParameter( "pass" ) );
    String transOption = request.getParameter( "trans" );
    String levelOption = request.getParameter( "level" );

    response.setStatus( HttpServletResponse.SC_OK );

    String encoding = System.getProperty( "KETTLE_DEFAULT_SERVLET_ENCODING", null );
    if ( encoding != null && !Utils.isEmpty( encoding.trim() ) ) {
      response.setCharacterEncoding( encoding );
      response.setContentType( "text/html; charset=" + encoding );
    }

    PrintWriter out = response.getWriter();

    try {

      final Repository repository = openRepository( repOption, userOption, passOption );
      final TransMeta transMeta = loadTransformation( repository, transOption );

      // Set the servlet parameters as variables in the transformation
      //
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
            transMeta.setVariable( parameter, values[0] );
          } else {
            transMeta.setParameterValue( parameter, values[0] );
          }
        }
      }

      TransExecutionConfiguration transExecutionConfiguration = new TransExecutionConfiguration();
      LogLevel logLevel = LogLevel.getLogLevelForCode( levelOption );
      transExecutionConfiguration.setLogLevel( logLevel );
      TransConfiguration transConfiguration = new TransConfiguration( transMeta, transExecutionConfiguration );

      String carteObjectId = UUID.randomUUID().toString();
      SimpleLoggingObject servletLoggingObject =
        new SimpleLoggingObject( CONTEXT_PATH, LoggingObjectType.CARTE, null );
      servletLoggingObject.setContainerObjectId( carteObjectId );
      servletLoggingObject.setLogLevel( logLevel );

      // Create the transformation and store in the list...
      //
      final Trans trans = new Trans( transMeta, servletLoggingObject );

      trans.setRepository( repository );
      trans.setSocketRepository( getSocketRepository() );

      getTransformationMap().addTransformation( transMeta.getName(), carteObjectId, trans, transConfiguration );
      trans.setContainerObjectId( carteObjectId );

      if ( repository != null ) {
        // The repository connection is open: make sure we disconnect from the repository once we
        // are done with this transformation.
        //
        trans.addTransListener( new TransAdapter() {
          public void transFinished( Trans trans ) {
            repository.disconnect();
          }
        } );
      }

      // Pass the servlet print writer to the transformation...
      //
      trans.setServletPrintWriter( out );
      trans.setServletReponse( response );
      trans.setServletRequest( request );

      try {
        // Execute the transformation...
        //
        executeTrans( trans );
        out.flush();

      } catch ( Exception executionException ) {
        String logging = KettleLogStore.getAppender().getBuffer( trans.getLogChannelId(), false ).toString();
        throw new KettleException( "Error executing transformation: " + logging, executionException );
      }
    } catch ( Exception ex ) {

      out.println( new WebResult( WebResult.STRING_ERROR, BaseMessages.getString(
        PKG, "ExecuteTransServlet.Error.UnexpectedError", Const.CR + Const.getStackTracker( ex ) ) ) );
    }
  }

  private TransMeta loadTransformation( Repository repository, String trans ) throws KettleException {

    if ( repository == null ) {

      // Without a repository it's a filename --> file:///foo/bar/trans.ktr
      //
      TransMeta transMeta = new TransMeta( trans );
      return transMeta;

    } else {

      // With a repository we need to load it from /foo/bar/Transformation
      // We need to extract the folder name from the path in front of the name...
      //
      String directoryPath;
      String name;
      int lastSlash = trans.lastIndexOf( RepositoryDirectory.DIRECTORY_SEPARATOR );
      if ( lastSlash < 0 ) {
        directoryPath = "/";
        name = trans;
      } else {
        directoryPath = trans.substring( 0, lastSlash );
        name = trans.substring( lastSlash + 1 );
      }
      RepositoryDirectoryInterface directory =
        repository.loadRepositoryDirectoryTree().findDirectory( directoryPath );
      if ( directory == null ) {
        throw new KettleException( "Unable to find directory path '" + directoryPath + "' in the repository" );
      }

      ObjectId transformationID = repository.getTransformationID( name, directory );
      if ( transformationID == null ) {
        throw new KettleException( "Unable to find transformation '" + name + "' in directory :" + directory );
      }
      TransMeta transMeta = repository.loadTransformation( transformationID, null );
      return transMeta;
    }
  }

  private Repository openRepository( String repositoryName, String user, String pass ) throws KettleException {

    if ( Utils.isEmpty( repositoryName ) ) {
      return null;
    }

    RepositoriesMeta repositoriesMeta = new RepositoriesMeta();
    repositoriesMeta.readData();
    RepositoryMeta repositoryMeta = repositoriesMeta.findRepository( repositoryName );
    if ( repositoryMeta == null ) {
      throw new KettleException( "Unable to find repository: " + repositoryName );
    }
    PluginRegistry registry = PluginRegistry.getInstance();
    Repository repository = registry.loadClass( RepositoryPluginType.class, repositoryMeta, Repository.class );
    repository.init( repositoryMeta );
    repository.connect( user, pass );
    return repository;
  }

  public String toString() {
    return "Start transformation";
  }

  public String getService() {
    return CONTEXT_PATH + " (" + toString() + ")";
  }

  protected void executeTrans( Trans trans ) throws KettleException {
    trans.prepareExecution( null );
    trans.startThreads();
    trans.waitUntilFinished();
  }

  public String getContextPath() {
    return CONTEXT_PATH;
  }

}
