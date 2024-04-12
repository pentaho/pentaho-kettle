/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2024 by Hitachi Vantara : http://www.pentaho.com
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

import org.json.simple.JSONObject;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.core.logging.LoggingObjectType;
import org.pentaho.di.core.logging.SimpleLoggingObject;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransExecutionConfiguration;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Objects;
import java.util.UUID;

/**
 * @author schintalapati
 */
public class StepActionServlet extends BaseHttpServlet implements CartePluginInterface {


  private static final String XML_REQUEST_BODY = "Xml request body";

  private static Class<?> PKG = StepActionServlet.class; // i18n

  private static final long serialVersionUID = -5879219287669847357L;
  private static final String STEP_NAME = "stepType";
  private static final String FIELD_NAME = "fieldName";
  private static final String LEVEL = "level";

  public static final String CONTEXT_PATH = "/kettle/stepAction";

  @Override
  public void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException {

    if ( isJettyMode() && !request.getContextPath().startsWith( CONTEXT_PATH ) ) {
      return;
    }

    if ( log.isDebug() ) {
      logDebug( BaseMessages.getString( PKG, "StepActionServlet.Log.StepActionRequested" ) );
    }

    String stepName = request.getParameter( STEP_NAME );
    String fieldName = request.getParameter( FIELD_NAME );
    String levelOption = request.getParameter( LEVEL );
    String[] knownOptions = new String[] { STEP_NAME, FIELD_NAME, LEVEL };
    InputStream requestInputStream = null;
    PrintWriter out = null;
    try {
      requestInputStream = request.getInputStream();
      out = response.getWriter();
    } catch ( IOException e ) {
      logError( e.getMessage() );
    }
    if ( requestInputStream == null ) {
      response.setStatus( HttpServletResponse.SC_BAD_REQUEST );
      out.println( new WebResult( WebResult.STRING_ERROR,
        BaseMessages.getString( PKG, "StepActionServlet.Error.MissingMandatoryParameter", XML_REQUEST_BODY ) ) );
      return;
    }
    if ( out == null ) {
      response.setStatus( HttpServletResponse.SC_BAD_REQUEST );
      return;
    }
    TransMeta transMeta = null;
    try {
      transMeta = new TransMeta( requestInputStream, null, true, null, null );
      String[] parameters = transMeta.listParameters();
      Enumeration<?> parameterNames = request.getParameterNames();
      while ( parameterNames.hasMoreElements() ) {
        String parameter = (String) parameterNames.nextElement();
        String[] values = request.getParameterValues( parameter );
        if ( Const.indexOfString( parameter, knownOptions ) < 0 ) {
          if ( Const.indexOfString( parameter, parameters ) < 0 ) {
            transMeta.setVariable( parameter, values[ 0 ] );
          } else {
            transMeta.setParameterValue( parameter, values[ 0 ] );
          }
        }
      }

      TransExecutionConfiguration transExecutionConfiguration = new TransExecutionConfiguration();
      LogLevel logLevel = LogLevel.getLogLevelForCode( levelOption );
      transExecutionConfiguration.setLogLevel( logLevel );

      String carteObjectId = UUID.randomUUID().toString();
      SimpleLoggingObject servletLoggingObject =
        new SimpleLoggingObject( CONTEXT_PATH, LoggingObjectType.CARTE, null );
      servletLoggingObject.setContainerObjectId( carteObjectId );
      servletLoggingObject.setLogLevel( logLevel );

      final Trans trans = new Trans( transMeta, servletLoggingObject );
      StepInterface step = null;
      for ( int i = 0; i < transMeta.getSteps().size(); i++ ) {
        if ( transMeta.getStep( i ).getName().equals( stepName ) ) {
          StepMeta stepMeta = transMeta.getStep( i );
          step = stepMeta.getStepMetaInterface().getStep( stepMeta, null, 0, transMeta, trans );
          break;
        }
      }
      if ( Objects.nonNull( step ) ) {
        JSONObject actionResponse =
          step.doAction( fieldName, step.getStepMeta().getStepMetaInterface(), transMeta, trans );
        response.setContentType( "application/json;charset=UTF-8" );
        out.println( actionResponse );
        response.setStatus( HttpServletResponse.SC_OK );
      } else {
        response.setStatus( HttpServletResponse.SC_BAD_REQUEST );
        out.println( new WebResult( WebResult.STRING_ERROR,
          BaseMessages.getString( PKG, "StepActionServlet.Error.StepNotFound", stepName ) ) );
      }

    } catch ( Exception ex ) {
      logError( ex.getMessage() );
    }
  }

  public String toString() {
    return "Step action";
  }

  public String getService() {
    return CONTEXT_PATH + " (" + toString() + ")";
  }

  @Override
  public void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException {
    super.doGet( request, response );
  }

  public String getContextPath() {
    return CONTEXT_PATH;
  }

}
