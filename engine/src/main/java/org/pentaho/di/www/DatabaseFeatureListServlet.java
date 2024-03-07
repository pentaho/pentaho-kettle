/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2024 by Hitachi Vantara : http://www.pentaho.com
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

import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.database.BaseDatabaseMeta;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.database.OracleDatabaseMeta;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Servlet for the feature list button in the database connections
 */
public class DatabaseFeatureListServlet extends BaseHttpServlet implements CartePluginInterface {

  public static final String CONTEXT_PATH = "/kettle/database/featureList";

  @Override
  protected void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException {
    doGet( request, response );
  }

  @Override
  public void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException {

    if ( isJettyMode() && !request.getContextPath().startsWith( CONTEXT_PATH ) ) {
      return;
    }

    response.setCharacterEncoding( "UTF-8" );
    response.setContentType( "application/json;charset=UTF-8" );
    PrintWriter out = response.getWriter();
    try {
      String dbConnectionRequest = request.getReader().lines().collect( Collectors.joining( System.lineSeparator() ) );
      DatabaseMeta databaseMeta = fetchDatabaseMeta( dbConnectionRequest );

      List<Object[]> featureList = new LinkedList<>();
      for ( RowMetaAndData rowMetaAndData : databaseMeta.getFeatureSummary() ) {
        featureList.add( rowMetaAndData.getData() );
      }

      JSONArray featureListJson = new JSONArray();
      for ( Object[] feature : featureList ) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put( feature[0] , feature[1] );
        featureListJson.add( jsonObject );
      }

      JSONObject jsonOutput = new JSONObject();
      jsonOutput.put( "featureList", featureListJson );

      response.setStatus( HttpServletResponse.SC_OK );
      out.print( jsonOutput );
    } catch ( Exception ex ) {
      response.setStatus( HttpServletResponse.SC_BAD_REQUEST );
      out.print( ex.getMessage() );
    }
  }

  @Override
  public String getContextPath() {
    return CONTEXT_PATH;
  }

  @Override
  public String getService() {
    return CONTEXT_PATH + " (" + this + ")";
  }

  private DatabaseMeta fetchDatabaseMeta( String dbConnectionRequest ) throws Exception {
    JSONParser parser = new JSONParser();
    try {
      if( StringUtils.isBlank( dbConnectionRequest ) ) {
        throw new Exception( "Invalid request for feature list" );
      }

      JSONObject dbConnectionJson = ( JSONObject ) parser.parse( dbConnectionRequest );

      String name = ( String ) dbConnectionJson.get( "connectionName" );
      String connectionType = ( String ) dbConnectionJson.get( "type" );
      String accessType = ( String ) dbConnectionJson.get( "access" );
      String hostName = ( String ) dbConnectionJson.get( "serverName" );
      String database = ( String ) dbConnectionJson.get( "database" );
      String port = ( String ) dbConnectionJson.get( "port" );
      String username = ( String ) dbConnectionJson.get( "username" );
      String password = ( String ) dbConnectionJson.get( "password" );

      DatabaseMeta databaseMeta = new DatabaseMeta( name, connectionType, accessType, hostName,
          database, port, username, password );

      JSONArray attributesJson = ( JSONArray ) dbConnectionJson.get( "attributes" );
      if( !Objects.isNull( attributesJson ) ) {
        for ( Object object : attributesJson ) {
          JSONObject jsonObject = ( JSONObject ) object;
          String attribute = ( String ) jsonObject.get( "attribute" );
          String value = ( String ) jsonObject.get( "code" );
          validateAndAddAttribute( attribute, value, databaseMeta );
        }
      }

      JSONArray parametersArray = ( JSONArray ) dbConnectionJson.get( "parameters" );
      if( !Objects.isNull( parametersArray ) ) {
        for ( Object object : parametersArray ) {
          JSONObject jsonObject = ( JSONObject ) object;
          String parameter = ( String ) jsonObject.get("parameter");
          String value = ( String ) jsonObject.get("value");
          databaseMeta.addExtraOption( connectionType, parameter, value );
        }
      }

      return databaseMeta;
    } catch ( ParseException | NumberFormatException ex ) {
      throw new Exception( ex.getMessage() );
    }
  }

  private void validateAndAddAttribute( String attribute, String value, DatabaseMeta databaseMeta ) {
    boolean result = BaseDatabaseMeta.ATTRIBUTE_KEYLIST.stream().anyMatch( x -> x.equalsIgnoreCase( attribute ) );
    if ( databaseMeta.getDatabaseInterface() instanceof OracleDatabaseMeta  &&
        OracleDatabaseMeta.STRICT_BIGNUMBER_INTERPRETATION.equalsIgnoreCase( attribute ) ) {
      result = true;
    }

    if ( result ) {
      databaseMeta.getDatabaseInterface().addAttribute( attribute, value );
    }
  }
}
