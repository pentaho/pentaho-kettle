/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/

package org.pentaho.di.trans.steps.tableinput;

import org.json.simple.JSONObject;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.logging.LoggingObjectType;
import org.pentaho.di.core.logging.SimpleLoggingObject;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepHelper;
import org.pentaho.di.trans.step.StepInterface;

import java.util.Map;

public class TableInputHelper extends BaseStepHelper {

  private static final String GET_COLUMNS = "getColumns";

  @Override
  protected JSONObject handleStepAction( String method, TransMeta transMeta, Map<String, String> queryParams ) {
    JSONObject response = new JSONObject();
    try {
      if ( GET_COLUMNS.equals( method ) ) {
        response = getColumnsAction( transMeta, queryParams );
      } else {
        response.put( ACTION_STATUS, FAILURE_METHOD_NOT_FOUND_RESPONSE );
      }
    } catch ( Exception ex ) {
      response.put( ACTION_STATUS, FAILURE_RESPONSE );
    }
    return response;
  }

  public JSONObject getColumnsAction( TransMeta transMeta, Map<String, String> queryParams ) throws KettleException {
    JSONObject response = new JSONObject();
    response.put( StepInterface.ACTION_STATUS, StepInterface.FAILURE_RESPONSE );
    String sql =
      String.valueOf(
        getColumnsSQL( transMeta, queryParams.get( "connection" ), queryParams.get( "schema" ),
          queryParams.get( "table" ) ) );
    response.put( "sql", sql );
    response.put( "actionStatus", StepInterface.SUCCESS_RESPONSE );
    return response;
  }

  private StringBuilder getColumnsSQL( TransMeta transMeta, String connection, String schema, String table )
    throws KettleException {
    DatabaseMeta databaseMeta = transMeta.findDatabase( connection );
    LoggingObjectInterface loggingObject = new SimpleLoggingObject(
      "Table Output Step", LoggingObjectType.STEP, null );
    Database db = new Database( loggingObject, databaseMeta );
    StringBuilder sql =
      new StringBuilder( "SELECT *"
        + Const.CR + "FROM "
        + databaseMeta.getQuotedSchemaTableCombination( schema, table ) + Const.CR );
    try {
      db.connect();

      RowMetaInterface fields = db.getQueryFields( String.valueOf( sql ), false );
      if ( fields != null ) {
        sql = new StringBuilder( "SELECT" + Const.CR );
        for ( int i = 0; i < fields.size(); i++ ) {
          ValueMetaInterface field = fields.getValueMeta( i );
          if ( i == 0 ) {
            sql.append( "  " );
          } else {
            sql.append( ", " );
          }
          sql.append( databaseMeta.quoteField( field.getName() ) ).append( Const.CR );
        }
        sql.append( "FROM " ).append( databaseMeta.getQuotedSchemaTableCombination( schema, table ) )
          .append( Const.CR );
      }
    } catch ( KettleDatabaseException e ) {
      throw new KettleException( e );
    } finally {
      db.close();
    }
    return sql;
  }

}
