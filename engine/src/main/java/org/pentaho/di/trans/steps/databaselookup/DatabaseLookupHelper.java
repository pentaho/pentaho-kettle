/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2025 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 *
 ******************************************************************************/

package org.pentaho.di.trans.steps.databaselookup;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.logging.LoggingObjectType;
import org.pentaho.di.core.logging.SimpleLoggingObject;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepHelper;
import org.pentaho.di.trans.step.StepInterface;

import java.util.Map;
import java.util.Optional;

public class DatabaseLookupHelper extends BaseStepHelper {

  public static final String GET_TABLE_FIELD_AND_TYPE = "getTableFieldAndType";
  public static final String CONNECTION = "connection";
  public static final String SCHEMA = "schema";
  public static final String TABLE = "table";
  public static final String COLUMNS = "columns";

  /**
   * Handles step-specific actions for Database lookup.
   */
  @Override
  protected JSONObject handleStepAction( String method, TransMeta transMeta, Map<String, String> queryParams ) {
    JSONObject response = new JSONObject();
    if ( method.equalsIgnoreCase( GET_TABLE_FIELD_AND_TYPE ) ) {
      response = getTableFieldAndType( transMeta, queryParams );
    } else {
      response.put( ACTION_STATUS, FAILURE_METHOD_NOT_FOUND_RESPONSE );
    }

    return response;
  }

  /**
   * Get table fields and their types.
   * @param transMeta contains the metadata of transformation.
   * @param queryParams Query parameters containing connection, schema, and table.
   * @return JSON object containing the table fields and types or error message.
   */
  public JSONObject getTableFieldAndType( TransMeta transMeta, Map<String, String> queryParams ) {
    JSONObject response = new JSONObject();
    response.put( StepInterface.ACTION_STATUS, StepInterface.FAILURE_RESPONSE );

    String connectionName = queryParams.get( CONNECTION );
    String schema = transMeta.environmentSubstitute( queryParams.get( SCHEMA ) );
    String table = transMeta.environmentSubstitute( queryParams.get( TABLE ) );

    if ( connectionName == null || connectionName.isBlank() || schema == null || schema.isBlank()
        || table == null || table.isBlank() ) {
      response.put( "error", "Missing or invalid parameters: connection, schema, or table." );
      return response;
    }
    try {
      JSONArray columnsList = getTableFieldsAndType( transMeta, connectionName, schema, table );
      response.put( COLUMNS, columnsList );
      response.put( StepInterface.ACTION_STATUS, StepInterface.SUCCESS_RESPONSE );
    } catch ( Exception e ) {
      log.logError( "Error fetching table fields and types: " + e.getMessage(), e );
      response.put( "error", "An unexpected error occurred." );
    }
    return response;
  }

  private JSONArray getTableFieldsAndType( TransMeta transMeta, String connection, String schema, String table ) {
    DatabaseMeta databaseMeta = Optional.ofNullable( transMeta.findDatabase( connection ) )
        .orElseThrow( () -> new IllegalArgumentException( "Database connection not found: " + connection ) );

    LoggingObjectInterface loggingObject = new SimpleLoggingObject(
        "DB Lookup Step", LoggingObjectType.STEP, null );

    try ( Database db = new Database( loggingObject, databaseMeta ) ) {
      db.connect();
      RowMetaInterface rowMeta = db.getTableFieldsMeta( schema, table );

      if ( rowMeta == null ) {
        log.logDebug( "No metadata found for schema: " + schema + ", table: " + table );
        return new JSONArray();
      }

      return rowMeta.getValueMetaList()
          .stream()
          .map( valueMeta -> {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put( "columnName", valueMeta.getName() );
            jsonObject.put( "columnType", valueMeta.getTypeDesc() );
            return jsonObject;
          } )
          .collect( JSONArray::new, JSONArray::add, JSONArray::addAll );
    } catch ( Exception e ) {
      log.logError( "Error fetching fields and types for table: " + table, e );
      return new JSONArray();
    }
  }
}
