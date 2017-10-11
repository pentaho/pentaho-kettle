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

import com.google.common.annotations.VisibleForTesting;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.core.logging.LoggingObjectType;
import org.pentaho.di.core.logging.SimpleLoggingObject;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectory;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransConfiguration;
import org.pentaho.di.trans.TransExecutionConfiguration;
import org.pentaho.di.trans.TransMeta;

public class RunTransServlet extends BaseHttpServlet implements CartePluginInterface {

  private static final long serialVersionUID = 1192413943669836776L;

  private static Class<?> PKG = RunTransServlet.class; // i18n

  public static final String CONTEXT_PATH = "/kettle/runTrans";

  public RunTransServlet() {
  }

  public RunTransServlet( TransformationMap transformationMap ) {
    super( transformationMap );
  }

  /**
 <div id="mindtouch">
    <h1>/kettle/runTrans</h1>
    <a name="GET"></a>
    <h2>GET</h2>
    <p>Execute transformation from enterprise repository. Repository should be configured in Carte xml file.
  Response contains <code>ERROR</code> result if error happened during transformation execution.</p>

    <p><b>Example Request:</b><br />
    <pre function="syntax.xml">
    GET /kettle/runTrans?trans=home%2Fadmin%2Fdummy-trans&level=Debug
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
    <td>trans</td>
    <td>Full path to the transformation in repository.</td>
    <td>query</td>
    </tr>
    <tr>
    <td>level</td>
    <td>Logging level to be used for transformation execution (i.e. Debug).</td>
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
     If an error occurred during transformation execution, response also contains information about the error.</p>

    <p><b>Example Response:</b></p>
    <pre function="syntax.xml">
    <webresult>
      <result>OK</result>
      <message>Transformation started</message>
      <id>7c082e8f-b4fe-40bc-b424-e0f881a61874</id>
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
      logDebug( BaseMessages.getString( PKG, "RunTransServlet.Log.RunTransRequested" ) );
    }

    // Options taken from PAN
    //
    String[] knownOptions = new String[] { "trans", "level", };

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

      final Repository repository = transformationMap.getSlaveServerConfig().getRepository();
      final TransMeta transMeta = loadTrans( repository, transOption );

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
          // If it's a trans parameter, set it, otherwise simply set the
          // variable
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
      final Trans trans = createTrans( transMeta, servletLoggingObject );

      // Pass information
      //
      trans.setRepository( repository );
      trans.setServletPrintWriter( out );
      trans.setServletReponse( response );
      trans.setServletRequest( request );

      // Setting variables
      //
      trans.initializeVariablesFrom( null );
      trans.getTransMeta().setInternalKettleVariables( trans );
      trans.injectVariables( transConfiguration.getTransExecutionConfiguration().getVariables() );

      // Also copy the parameters over...
      //
      trans.copyParametersFrom( transMeta );

      /*
       * String[] parameterNames = job.listParameters(); for (int idx = 0; idx < parameterNames.length; idx++) { // Grab
       * the parameter value set in the job entry // String thisValue =
       * jobExecutionConfiguration.getParams().get(parameterNames[idx]); if (!Utils.isEmpty(thisValue)) { // Set the
       * value as specified by the user in the job entry // jobMeta.setParameterValue(parameterNames[idx], thisValue); }
       * }
       */
      transMeta.activateParameters();

      trans.setSocketRepository( getSocketRepository() );

      getTransformationMap().addTransformation( trans.getName(), carteObjectId, trans, transConfiguration );

      // DO NOT disconnect from the shared repository connection when the job finishes.
      //
      String message = "Transformation '" + trans.getName() + "' was added to the list with id " + carteObjectId;
      logBasic( message );

      try {
        // Execute the transformation...
        //
        trans.execute( null );

        finishProcessing( trans, out );

      } catch ( Exception executionException ) {
        String logging = KettleLogStore.getAppender().getBuffer( trans.getLogChannelId(), false ).toString();
        throw new KettleException( "Error executing Transformation: " + logging, executionException );
      }
    } catch ( Exception ex ) {
      out.println( new WebResult( WebResult.STRING_ERROR, BaseMessages.getString(
        PKG, "RunTransServlet.Error.UnexpectedError", Const.CR + Const.getStackTracker( ex ) ) ) );
    }
  }

  //need for unit test
  Trans createTrans( TransMeta transMeta, SimpleLoggingObject servletLoggingObject ) {
    return new Trans( transMeta, servletLoggingObject );
  }

  private TransMeta loadTrans( Repository repository, String transformationName ) throws KettleException {

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
        int lastSlash = transformationName.lastIndexOf( RepositoryDirectory.DIRECTORY_SEPARATOR );
        if ( lastSlash < 0 ) {
          directoryPath = "/";
          name = transformationName;
        } else {
          directoryPath = transformationName.substring( 0, lastSlash );
          name = transformationName.substring( lastSlash + 1 );
        }
        RepositoryDirectoryInterface directory =
          repository.loadRepositoryDirectoryTree().findDirectory( directoryPath );

        ObjectId transformationId = repository.getTransformationID( name, directory );

        TransMeta transMeta = repository.loadTransformation( transformationId, null );
        return transMeta;
      }
    }
  }


  /**
   If the transformation has at least one step in a transformation,
   which writes it's data straight to a servlet output
   we should wait transformation's termination.
   Otherwise the servlet's response lifecycle may come to an end and
   the response will be closed by container while
   the transformation will be still trying writing data into it.
   */
  @VisibleForTesting
  void finishProcessing( Trans trans, PrintWriter out ) {
    if ( trans.getSteps().stream().anyMatch( step -> step.meta.passDataToServletOutput() ) ) {
      trans.waitUntilFinished();
    } else {
      WebResult webResult = new WebResult( WebResult.STRING_OK, "Transformation started", trans.getContainerObjectId() );
      out.println( webResult.getXML() );
      out.flush();
    }
  }

  public String toString() {
    return "Run Transformation";
  }

  public String getService() {
    return CONTEXT_PATH + " (" + toString() + ")";
  }

  @Override
  public String getContextPath() {
    return CONTEXT_PATH;
  }

}
