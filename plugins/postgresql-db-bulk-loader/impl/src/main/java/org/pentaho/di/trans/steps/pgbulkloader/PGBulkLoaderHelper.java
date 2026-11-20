/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/


package org.pentaho.di.trans.steps.pgbulkloader;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.pentaho.di.core.SQLStatement;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.logging.LoggingObjectType;
import org.pentaho.di.core.logging.SimpleLoggingObject;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepHelper;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class PGBulkLoaderHelper extends BaseStepHelper {

  public static final String STEP_NAME = "stepName";
  public static final String DETAILS = "details";
  public static final String CONNECTION = "connection";
  public static final String SCHEMA = "schema";
  public static final String TABLE = "table";

  private static final String GET_SQL = "getSQL";
  private static final String GET_TABLE_FIELD_AND_TYPE = "getTableFieldAndType";
  private static final Class<?> PKG = PGBulkLoaderMeta.class;

  public PGBulkLoaderHelper() {
    super();
  }

  @Override
  protected JSONObject handleStepAction( String method, TransMeta transMeta, Map<String, String> queryParams ) {
    JSONObject response = new JSONObject();
    try {
      if ( GET_SQL.equals( method ) ) {
        response = getSQLAction( transMeta, queryParams );
      } else if ( GET_TABLE_FIELD_AND_TYPE.equals( method ) ) {
        response = getTableFieldAndType( transMeta, queryParams );
      } else {
        response.put( ACTION_STATUS, FAILURE_METHOD_NOT_FOUND_RESPONSE );
      }
    } catch ( Exception ex ) {
      response.put( ACTION_STATUS, FAILURE_RESPONSE );
      response.put( DETAILS, ex.getMessage() );
    }
    return response;
  }

  public JSONObject getTableFieldAndType( TransMeta transMeta, Map<String, String> queryParams ) {
    JSONObject response = new JSONObject();
    response.put( StepInterface.ACTION_STATUS, StepInterface.FAILURE_RESPONSE );

    String connectionName = queryParams.get( CONNECTION );
    String schema = transMeta.environmentSubstitute( queryParams.get( SCHEMA ) );
    String table = transMeta.environmentSubstitute( queryParams.get( TABLE ) );
    String normalizedSchema = ( schema == null || schema.isBlank() ) ? null : schema;

    if ( connectionName == null || connectionName.isBlank() || table == null || table.isBlank() ) {
      response.put( "error", "Missing or invalid parameters: connection or table." );
      return response;
    }
    try {
      DatabaseMeta databaseMeta = Optional.ofNullable( transMeta.findDatabase( connectionName ) )
          .orElseThrow( () -> new IllegalArgumentException( "Database connection not found: " + connectionName ) );

      LoggingObjectInterface loggingObject = new SimpleLoggingObject(
          "PGBulkLoader Step", LoggingObjectType.STEP, null );

      try ( Database db = new Database( loggingObject, databaseMeta ) ) {
        db.connect();
        String schemaTable = databaseMeta.getQuotedSchemaTableCombination( normalizedSchema, table );
        RowMetaInterface rowMeta = db.getTableFields( schemaTable );

        JSONArray columnsList = new JSONArray();
        if ( rowMeta != null ) {
          rowMeta.getValueMetaList()
              .stream()
              .map( valueMeta -> {
                JSONObject col = new JSONObject();
                col.put( "columnName", valueMeta.getName() );
                col.put( "columnType", valueMeta.getTypeDesc() );
                return col;
              } )
              .forEach( columnsList::add );
        }
        response.put( "columns", columnsList );
        response.put( StepInterface.ACTION_STATUS, StepInterface.SUCCESS_RESPONSE );
      }
    } catch ( Exception e ) {
      log.logError( "Error fetching table fields and types: " + e.getMessage(), e );
      response.put( "error", "An unexpected error occurred." );
    }
    return response;
  }

  public JSONObject getSQLAction( TransMeta transMeta, Map<String, String> queryParams ) {
    JSONObject response = new JSONObject();
    response.put( StepInterface.ACTION_STATUS, StepInterface.FAILURE_RESPONSE );
    
    try {
      SQLStatement sql = sql( transMeta, queryParams.get( STEP_NAME ), queryParams.get( CONNECTION ) );
      
      if ( Objects.nonNull( sql ) ) {
        if ( !sql.hasError() ) {
          if ( sql.hasSQL() ) {
            response.put( StepInterface.ACTION_STATUS, StepInterface.SUCCESS_RESPONSE );
            response.put( "sqlString", sql.getSQL() );
          } else {
            response.put( StepInterface.ACTION_STATUS, StepInterface.SUCCESS_RESPONSE );
            response.put( DETAILS, BaseMessages.getString( PKG, "PGBulkLoaderDialog.NoSQLNeeds.DialogMessage" ) );
          }
        } else {
          response.put( DETAILS, sql.getError() );
        }
      } else {
        response.put( DETAILS, BaseMessages.getString( PKG, "PGBulkLoaderMeta.GetSQL.NotReceivingAnyFields" ) );
      }

    } catch ( KettleStepException e ) {
      log.logError( e.getMessage() );
      response.put( StepInterface.ACTION_STATUS, StepInterface.FAILURE_METHOD_NOT_RESPONSE );
      response.put( DETAILS, e.getMessage() );
    }
    
    return response;
  }

  public SQLStatement sql( TransMeta transMeta, String stepName, String connection ) throws KettleStepException {
    if ( stepName != null && !stepName.isEmpty() ) {
      StepMeta stepMeta = transMeta.findStep( stepName );
      
      if ( stepMeta != null && stepMeta.getStepMetaInterface() instanceof PGBulkLoaderMeta meta ) {
        meta.setDatabaseMeta( transMeta.findDatabase( connection ) );

        RowMetaInterface prev = transMeta.getPrevStepFields( stepName );

        if ( isValidRowMeta( prev ) && meta.getFieldStream() != null && meta.getFieldStream().length > 0 ) {
          return meta.getSQLStatements( transMeta, stepMeta, prev, null, null );
        } else {
          return null;
        }
      }
    }
    return null;
  }

  private static boolean isValidRowMeta( RowMetaInterface rowMeta ) {
    if ( rowMeta == null ) {
      return false;
    }
    return rowMeta.size() > 0;
  }
}
