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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.www.cache.CarteStatusCache;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;


public class GetRegisteredStepsServlet extends BaseHttpServlet implements CartePluginInterface {

  private static Class<?> PKG = GetRegisteredStepsServlet.class; // for i18n purposes, needed by Translator2!!

  private static final long serialVersionUID = 3634806745372015720L;

  public static final String CONTEXT_PATH = "/kettle/registeredSteps";

  private static final String PLUGIN_TYPE = "pluginType";

  @VisibleForTesting
  CarteStatusCache cache = CarteStatusCache.getInstance();

  public GetRegisteredStepsServlet() {
  }

  public GetRegisteredStepsServlet( TransformationMap transformationMap ) {
    super( transformationMap );
  }

  public void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException,
          IOException {

    if ( isJettyMode() && !request.getContextPath().startsWith( CONTEXT_PATH ) ) {
      return;
    }
    response.setStatus( HttpServletResponse.SC_OK );

    response.setCharacterEncoding( "UTF-8" );
    response.setContentType( "application/json;charset=UTF-8" );
    String pluginType = request.getParameter( PLUGIN_TYPE );

    PrintWriter out = response.getWriter();
    PluginRegistry registry = PluginRegistry.getInstance();
    if ( request.getParameter( "stepId" ) == null ) {
      writeJson( pluginType, out, registry );
    } else {
      String stepId = request.getParameter( "stepId" );
      PluginInterface pluginInterface = registry.getPlugin( StepPluginType.class, stepId );
      try {
        StepMetaInterface meta = (StepMetaInterface) registry.loadClass( pluginInterface );
        out.print( new ObjectMapper().writeValueAsString( meta ) );
      } catch ( KettlePluginException e ) {
        e.printStackTrace();
      }

    }
  }

  private void writeJson( String pluginType, PrintWriter out, PluginRegistry registry ) throws JsonProcessingException {

    JSONArray previewJson = new JSONArray();
    List<PluginInterface> stepPlugins = pluginType == null ? registry.getPlugins( StepPluginType.class )
            : registry.getPlugins( registry.getPluginTypes().stream()
            .filter( x -> x.getName().contains( pluginType ) )
            .findAny().get() );
    for ( PluginInterface pluginInterface : stepPlugins ) {
      if ( pluginInterface.getIds().length > 0 ) {
        JSONObject stepJson = new JSONObject();
        stepJson.put( "id", pluginInterface.getIds()[0] );
        stepJson.put( "name", pluginInterface.getName() );
        stepJson.put( "category", pluginInterface.getCategory() );
        previewJson.add( stepJson );
      }
    }
    JSONObject finalJsonOutput = new JSONObject();
    finalJsonOutput.put( "plugins", previewJson );
    out.print( finalJsonOutput );
  }

  public String toString() {
    return "Registered Plugins Handler";
  }

  public String getService() {
    return CONTEXT_PATH + " (" + toString() + ")";
  }

  public String getContextPath() {
    return CONTEXT_PATH;
  }
}
