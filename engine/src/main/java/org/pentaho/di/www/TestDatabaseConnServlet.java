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

import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.pentaho.di.core.database.DatabaseFactory;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.database.DatabaseTestResults;
import org.pentaho.di.core.exception.KettleException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Objects;
import java.util.stream.Collectors;

public class TestDatabaseConnServlet extends BaseHttpServlet implements CartePluginInterface {

  public static final String CONTEXT_PATH = "/kettle/database/test-connection";

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
      if ( StringUtils.isBlank( dbConnectionRequest ) ) {
        throw new KettleException( "Invalid request for test connection" );
      }

      JSONParser parser = new JSONParser();
      JSONObject dbConnectionJson = (JSONObject) parser.parse( dbConnectionRequest );

      String connectionName = (String) dbConnectionJson.get( "connectionName" );
      String databaseName = (String) dbConnectionJson.get( "databaseName" );
      if ( StringUtils.isBlank( connectionName ) ) {
        throw new KettleException( "Please give this database connection a name" );
      }

      if ( StringUtils.isBlank( databaseName ) ) {
        throw new KettleException( "Please specify the name of the database" );
      }

      DatabaseMeta databaseMeta = DatabaseRequestMapping.fetchDatabaseMeta( dbConnectionJson );

      DatabaseFactory databaseFactory = new DatabaseFactory();
      DatabaseTestResults databaseTestResults = databaseFactory.getConnectionTestResults( databaseMeta );

      JSONObject jsonOutput = new JSONObject();
      jsonOutput.put( "message", databaseTestResults.getMessage() );
      jsonOutput.put( "success", databaseTestResults.isSuccess() );
      if ( Objects.nonNull( databaseTestResults.getException() ) ) {
        jsonOutput.put( "exception", databaseTestResults.getException().getMessage() );
      }

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
}
