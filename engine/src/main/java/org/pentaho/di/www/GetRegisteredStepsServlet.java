/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2022 by Hitachi Vantara : http://www.pentaho.com
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.collections.CollectionUtils;
import org.json.simple.JSONObject;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.www.cache.CarteStatusCache;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


public class GetRegisteredStepsServlet extends BaseHttpServlet implements CartePluginInterface {

  private static Class<?> PKG = GetRegisteredStepsServlet.class; // for i18n purposes, needed by Translator2!!

  private static final long serialVersionUID = 3634806745372015720L;

  public static final String CONTEXT_PATH = "/kettle/registeredSteps";

  public static final String SEND_RESULT = "sendResult";

  @VisibleForTesting
  CarteStatusCache cache = CarteStatusCache.getInstance();

  public GetRegisteredStepsServlet() {
  }

  public class StepPluginDetails implements Serializable {
    public static final long serialVersionUID = 3634806743372015720L;
    public String id;
    public String name;
    public String category;
    public StepPluginDetails(String id,String name,String category ){
      this.id=id;
      this.name= name;
      this.category =category;
    }
    public StepPluginDetails(){

    }
  }

  public GetRegisteredStepsServlet(TransformationMap transformationMap ) {
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
    PrintWriter out = response.getWriter();
    PluginRegistry registry = PluginRegistry.getInstance();

    if(request.getParameter("stepId" ) == null) {
      List<StepPluginDetails> plugins = new ArrayList<>();
      List<PluginInterface> plugins1 = registry.getPlugins( StepPluginType.class );
      for (PluginInterface pluginInterface : plugins1) {
        if( pluginInterface.getIds().length>0)
          plugins.add(new StepPluginDetails(pluginInterface.getIds()[0],pluginInterface.getName(),pluginInterface.getCategory()));
      }
      out.print(new ObjectMapper().writeValueAsString(plugins));
    }else{
      String stepId= request.getParameter("stepId" );
      PluginInterface pluginInterface = registry.getPlugin( StepPluginType.class, stepId );
      try {
        StepMetaInterface meta = (StepMetaInterface) PluginRegistry.getInstance().loadClass( pluginInterface );
        out.print(new ObjectMapper().writeValueAsString( meta ));
      } catch (KettlePluginException e) {
        e.printStackTrace();
      }

    }
  }

  public String toString() {
    return "Trans Status Handler";
  }

  public String getService() {
    return CONTEXT_PATH + " (" + toString() + ")";
  }

  public String getContextPath() {
    return CONTEXT_PATH;
  }
}
