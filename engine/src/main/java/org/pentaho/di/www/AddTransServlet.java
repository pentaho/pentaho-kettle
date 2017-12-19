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

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannelFileWriter;
import org.pentaho.di.core.logging.LoggingObjectType;
import org.pentaho.di.core.logging.SimpleLoggingObject;
import org.pentaho.di.core.util.FileUtil;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransAdapter;
import org.pentaho.di.trans.TransConfiguration;
import org.pentaho.di.trans.TransExecutionConfiguration;
import org.pentaho.di.trans.TransMeta;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

//has been replaced by RegisterTransServlet
@Deprecated
public class AddTransServlet extends BaseHttpServlet implements CartePluginInterface {
  private static final long serialVersionUID = -6850701762586992604L;

  public static final String CONTEXT_PATH = "/kettle/addTrans";

  public AddTransServlet() {
  }

  public AddTransServlet( TransformationMap transformationMap, SocketRepository socketRepository ) {
    super( transformationMap, socketRepository );
  }
  /**

    <div id="mindtouch">
    <h1>/kettle/addTrans</h1>
    <a name="POST"></a>
    <h2>POST</h2>
    <p>Uploads and executes transformation configuration XML file.
  Uploads xml file containing transformation and transformation_execution_configuration
  (wrapped in transformation_configuration tag) to be executed and executes it. Method relies
  on the input parameter to determine if xml or html reply should be produced. The transformation_configuration xml is
  transferred within request body.

  <code>transformation name of the executed transformation </code> will be returned in the Response object
  or <code>message</code> describing error occurred. To determine if the call successful or not you should
  rely on <code>result</code> parameter in response.</p>

    <p><b>Example Request:</b><br />
    <pre function="syntax.xml">
    POST /kettle/addTrans/?xml=Y
    </pre>
    <p>Request body should contain xml containing transformation_configuration (transformation and
  transformation_execution_configuration wrapped in transformation_configuration tag).</p>
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
    <td>Boolean flag set to either <code>Y</code> or <code>N</code> describing if xml or html reply
  should be produced.</td>
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
    <p>Response wraps transformation name that was executed or error stack trace
  if an error occurred. Response has <code>result</code> OK if there were no errors. Otherwise it returns ERROR.</p>

    <p><b>Example Response:</b></p>
    <pre function="syntax.xml">
    <?xml version="1.0" encoding="UTF-8"?>
    <webresult>
      <result>OK</result>
      <message>Transformation &#x27;dummy-trans&#x27; was added to Carte with id eb4a92ff-6852-4307-9f74-3c74bd61f829</message>
      <id>eb4a92ff-6852-4307-9f74-3c74bd61f829</id>
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
      <td>Request was processed and XML response is returned.</td>
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
    if ( isJettyMode() && !request.getRequestURI().startsWith( CONTEXT_PATH ) ) {
      return;
    }

    if ( log.isDebug() ) {
      logDebug( "Addition of transformation requested" );
    }

    boolean useXML = "Y".equalsIgnoreCase( request.getParameter( "xml" ) );

    PrintWriter out = response.getWriter();
    BufferedReader in = request.getReader();
    if ( log.isDetailed() ) {
      logDetailed( "Encoding: " + request.getCharacterEncoding() );
    }

    if ( useXML ) {
      response.setContentType( "text/xml" );
      out.print( XMLHandler.getXMLHeader() );
    } else {
      response.setContentType( "text/html" );
      out.println( "<HTML>" );
      out.println( "<HEAD><TITLE>Add transformation</TITLE></HEAD>" );
      out.println( "<BODY>" );
    }

    response.setStatus( HttpServletResponse.SC_OK );

    String realLogFilename = null;
    TransExecutionConfiguration transExecutionConfiguration = null;

    try {
      // First read the complete transformation in memory from the request
      //
      StringBuilder xml = new StringBuilder( request.getContentLength() );
      int c;
      while ( ( c = in.read() ) != -1 ) {
        xml.append( (char) c );
      }

      // Parse the XML, create a transformation configuration
      //
      TransConfiguration transConfiguration = TransConfiguration.fromXML( xml.toString() );
      TransMeta transMeta = transConfiguration.getTransMeta();
      transExecutionConfiguration = transConfiguration.getTransExecutionConfiguration();
      transMeta.setLogLevel( transExecutionConfiguration.getLogLevel() );
      if ( log.isDetailed() ) {
        logDetailed( "Logging level set to " + log.getLogLevel().getDescription() );
      }
      transMeta.injectVariables( transExecutionConfiguration.getVariables() );

      // Also copy the parameters over...
      //
      Map<String, String> params = transExecutionConfiguration.getParams();
      for ( String param : params.keySet() ) {
        String value = params.get( param );
        transMeta.setParameterValue( param, value );
      }

      // If there was a repository, we know about it at this point in time.
      //
      final Repository repository = transExecutionConfiguration.getRepository();

      String carteObjectId = UUID.randomUUID().toString();
      SimpleLoggingObject servletLoggingObject =
        new SimpleLoggingObject( CONTEXT_PATH, LoggingObjectType.CARTE, null );
      servletLoggingObject.setContainerObjectId( carteObjectId );
      servletLoggingObject.setLogLevel( transExecutionConfiguration.getLogLevel() );

      // Create the transformation and store in the list...
      //
      final Trans trans = new Trans( transMeta, servletLoggingObject );

      if ( transExecutionConfiguration.isSetLogfile() ) {
        realLogFilename = transExecutionConfiguration.getLogFileName();
        final LogChannelFileWriter logChannelFileWriter;
        try {
          FileUtil.createParentFolder( AddTransServlet.class, realLogFilename, transExecutionConfiguration
            .isCreateParentFolder(), trans.getLogChannel(), trans );
          logChannelFileWriter =
            new LogChannelFileWriter( servletLoggingObject.getLogChannelId(), KettleVFS
              .getFileObject( realLogFilename ), transExecutionConfiguration.isSetAppendLogfile() );
          logChannelFileWriter.startLogging();

          trans.addTransListener( new TransAdapter() {
            @Override
            public void transFinished( Trans trans ) throws KettleException {
              if ( logChannelFileWriter != null ) {
                logChannelFileWriter.stopLogging();
              }
            }
          } );

        } catch ( KettleException e ) {
          logError( Const.getStackTracker( e ) );
        }

      }

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

      String message = "Transformation '" + trans.getName() + "' was added to Carte with id " + carteObjectId;

      if ( useXML ) {
        // Return the log channel id as well
        //
        out.println( new WebResult( WebResult.STRING_OK, message, carteObjectId ) );
      } else {
        out.println( "<H1>" + message + "</H1>" );
        out.println( "<p><a href=\""
          + convertContextPath( GetTransStatusServlet.CONTEXT_PATH ) + "?name=" + trans.getName() + "&id="
          + carteObjectId + "\">Go to the transformation status page</a><p>" );
      }
    } catch ( Exception ex ) {
      if ( useXML ) {
        out.println( new WebResult( WebResult.STRING_ERROR, Const.getStackTracker( ex ) ) );
      } else {
        out.println( "<p>" );
        out.println( "<pre>" );
        ex.printStackTrace( out );
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
    return "Add Transformation";
  }

  public String getService() {
    return CONTEXT_PATH + " (" + toString() + ")";
  }

  public String getContextPath() {
    return CONTEXT_PATH;
  }
}
