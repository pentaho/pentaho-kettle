/*!
 *
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 *
 * Copyright (c) 2024 Hitachi Vantara. All rights reserved.
 *
 */
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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class DatabaseFeatureListServlet extends BaseHttpServlet implements CartePluginInterface {

  public static final String CONTEXT_PATH = "/kettle/database/feature-list";

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
    response.setContentType( "application/json; charset=UTF-8" );
    PrintWriter out = response.getWriter();
    try {
      String dbConnectionRequest = request.getReader().lines().collect( Collectors.joining( System.lineSeparator() ) );
      DatabaseMeta databaseMeta = fetchDatabaseMeta( dbConnectionRequest );

      List<Object[]> featureList = new ArrayList<>();
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

      String name = Objects.toString( dbConnectionJson.get( "connectionName" ) );
      String connectionType = Objects.toString( dbConnectionJson.get( "type" ) );
      String accessType = Objects.toString( dbConnectionJson.get( "access" ) );
      String hostName = Objects.toString( dbConnectionJson.get( "serverName" ) );
      String database = Objects.toString( dbConnectionJson.get( "database" ) );
      String port = Objects.toString( dbConnectionJson.get( "port" ) );
      String username = Objects.toString( dbConnectionJson.get( "username" ) );
      String password = Objects.toString( dbConnectionJson.get( "password" ) );

      DatabaseMeta databaseMeta = new DatabaseMeta( name, connectionType, accessType, hostName,
          database, port, username, password );

      JSONArray attributesJson = ( JSONArray ) dbConnectionJson.get( "attributes" );
      if( !Objects.isNull( attributesJson ) ) {
        for ( Object object : attributesJson ) {
          JSONObject jsonObject = ( JSONObject ) object;
          String attribute = Objects.toString( jsonObject.get( "attribute" ) );
          String value = Objects.toString( jsonObject.get( "code" ) );
          validateAndAddAttribute( attribute, value, databaseMeta );
        }
      }

      JSONArray parametersArray = ( JSONArray ) dbConnectionJson.get( "parameters" );
      if( !Objects.isNull( parametersArray ) ) {
        for ( Object object : parametersArray ) {
          JSONObject jsonObject = ( JSONObject ) object;
          String parameter = Objects.toString( jsonObject.get( "parameter" ) );
          String value = Objects.toString( jsonObject.get( "value" ) );
          databaseMeta.addExtraOption( connectionType, parameter, value );
        }
      }

      return databaseMeta;
    } catch ( ParseException | NumberFormatException ex ) {
      throw new Exception( ex.getMessage() );
    }
  }

  private void validateAndAddAttribute( String attribute, String value, DatabaseMeta databaseMeta ) {
    boolean result = BaseDatabaseMeta.ATTRIBUTE_KEYLIST.contains( attribute );
    if ( databaseMeta.getDatabaseInterface() instanceof OracleDatabaseMeta  &&
        OracleDatabaseMeta.STRICT_BIGNUMBER_INTERPRETATION.equalsIgnoreCase( attribute ) ) {
      result = true;
    }

    if ( result ) {
      databaseMeta.getDatabaseInterface().addAttribute( attribute, value );
    }
  }
}
