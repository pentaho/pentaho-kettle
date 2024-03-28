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

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.pentaho.di.core.database.DatabaseInterface;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.plugins.DatabasePluginType;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 *
 *This class contains api's to fetch registered databases.
 *
 * @since 2024
 * @author sVarma1992
 *
 */
public class GetDatabaseDetailsServlet extends BaseHttpServlet implements CartePluginInterface {

  private static Class<?> PKG = GetDatabaseDetailsServlet.class; // for i18n purposes, needed by Translator2!!

  private static final long serialVersionUID = 3634806745372015720L;

  public static final String CONTEXT_PATH = "/kettle/registeredDatabasePlugins";

  /**
   * Adding this constructor as Carte needs default constructor for registering the servlet
   */
  public GetDatabaseDetailsServlet() {
  }
  public void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException,
          IOException {

    if ( isJettyMode() && !request.getContextPath().startsWith( CONTEXT_PATH ) ) {
      return;
    }

    response.setStatus( HttpServletResponse.SC_OK );
    response.setCharacterEncoding( "UTF-8" );
    response.setContentType( "application/json; charset=UTF-8" );
    JSONArray previewJson = new JSONArray();

    PrintWriter out = response.getWriter();
    PluginRegistry registry = PluginRegistry.getInstance();
    List<PluginInterface> stepPlugins = registry.getPlugins( DatabasePluginType.class );

    for ( PluginInterface pluginInterface : stepPlugins ) {
      if ( pluginInterface.getIds().length > 0 ) {
        JSONObject dbJson = new JSONObject();
        dbJson.put( "id", pluginInterface.getIds()[0] );
        dbJson.put( "name", pluginInterface.getName() );
        try {
          DatabaseInterface db = (DatabaseInterface) registry.loadClass( pluginInterface );
          JSONArray accessTypes = new JSONArray();
          for ( int id : db.getAccessTypeList() ) {
            accessTypes.add( DatabaseMeta.getAccessTypeDesc( id ) );
          }
          dbJson.put( "accessTypes", accessTypes );
        } catch ( KettlePluginException e ) {
          throw new ServletException( e.getMessage() );
        }
        previewJson.add( dbJson );
      }
    }

    JSONObject finalJsonOutput = new JSONObject();
    finalJsonOutput.put( "plugins", previewJson );
    out.print( finalJsonOutput );
  }

  public String toString() {
    return "Registered Database Plugins Handler";
  }

  public String getService() {
    return CONTEXT_PATH + " (" + toString() + ")";
  }

  public String getContextPath() {
    return CONTEXT_PATH;
  }
}
