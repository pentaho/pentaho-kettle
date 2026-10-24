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

package org.pentaho.di.job.entries.columnsexist;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.BaseJobEntryHelper;

import java.util.Collections;
import java.util.Map;

/**
 * Provides action handlers for the Columns Exist job entry dialog.
 * Exposes {@code getTableColumns} to fetch column names from the specified
 * database table, enabling the "Get columns" button in the UI.
 *
 * @see BaseJobEntryHelper
 * @see JobEntryColumnsExist
 */
public class JobEntryColumnsExistHelper extends BaseJobEntryHelper {

  private static final Class<?> PKG = JobEntryColumnsExist.class;

  private static final String METHOD_GET_TABLE_COLUMNS = "getTableColumns";
  private static final String RESPONSE_KEY_COLUMNS = "columns";
  private static final String RESPONSE_KEY_MESSAGE = "message";

  public JobEntryColumnsExistHelper() {
    super();
  }

  @Override
  @SuppressWarnings( "unchecked" )
  protected JSONObject handleJobEntryAction( String method, JobMeta jobMeta, Map<String, String> queryParams ) {
    JSONObject response = new JSONObject();
    try {
      if ( METHOD_GET_TABLE_COLUMNS.equals( method ) ) {
        response = getTableColumns( jobMeta, queryParams );
      } else {
        response.put( ACTION_STATUS, FAILURE_METHOD_NOT_FOUND_RESPONSE );
      }
    } catch ( Exception ex ) {
      response.put( ACTION_STATUS, FAILURE_RESPONSE );
      log.logError( "Error executing job entry action '" + method + "'", ex );
    }
    return response;
  }

  /**
   * Fetches the column names for the specified table from the database.
   *
   * @param jobMeta     the job metadata used for variable substitution and database lookup
   * @param queryParams expects {@code connection}, {@code schemaname}, {@code tablename}
   * @return a {@link JSONObject} with {@code columns} (array of column name strings) on success,
   *         or an error status on failure
   */
  @SuppressWarnings( "unchecked" )
  public JSONObject getTableColumns( JobMeta jobMeta, Map<String, String> queryParams ) {
    JSONObject response = new JSONObject();
    if ( jobMeta == null ) {
      response.put( ACTION_STATUS, FAILURE_RESPONSE );
      response.put( RESPONSE_KEY_MESSAGE, BaseMessages.getString( PKG, "JobEntryColumnsExist.Error.NoDbConnection" ) );
      return response;
    }
    JSONArray columns = new JSONArray();

    Map<String, String> safeParams = queryParams == null ? Collections.emptyMap() : queryParams;
    String connectionName = Const.NVL( safeParams.get( "connection" ), "" );
    String schemaName = Const.NVL( safeParams.get( "schemaname" ), "" );
    String tableName = Const.NVL( safeParams.get( "tablename" ), "" );

    DatabaseMeta databaseMeta = jobMeta.findDatabase( connectionName );
    if ( databaseMeta == null ) {
      response.put( ACTION_STATUS, FAILURE_RESPONSE );
      response.put( RESPONSE_KEY_MESSAGE, BaseMessages.getString( PKG, "JobEntryColumnsExist.Error.NoDbConnection" ) );
      return response;
    }

    Database database = createDatabase( jobMeta, databaseMeta );
    database.shareVariablesWith( jobMeta );
    try {
      database.connect();

      String resolvedSchema = jobMeta.environmentSubstitute( schemaName );
      String resolvedTable = jobMeta.environmentSubstitute( tableName );

      RowMetaInterface row = database.getTableFieldsMeta( resolvedSchema, resolvedTable );
      if ( row != null ) {
        String[] fieldNames = row.getFieldNames();
        Collections.addAll( columns, fieldNames );
        response.put( RESPONSE_KEY_COLUMNS, columns );
        response.put( ACTION_STATUS, SUCCESS_RESPONSE );
      } else {
        response.put( ACTION_STATUS, FAILURE_RESPONSE );
        response.put( RESPONSE_KEY_MESSAGE, BaseMessages.getString( PKG, "JobEntryColumnsExist.GetListColumsNoRow.DialogMessage" ) );
      }
    } catch ( Exception e ) {
      log.logError( BaseMessages.getString( PKG, "JobEntryColumnsExist.ConnectionError2.DialogMessage", tableName ), e );
      response.put( ACTION_STATUS, FAILURE_RESPONSE );
      response.put( RESPONSE_KEY_MESSAGE, BaseMessages.getString( PKG, "JobEntryColumnsExist.ConnectionError2.DialogMessage", tableName ) );
    } finally {
      database.disconnect();
    }

    return response;
  }

  protected Database createDatabase( JobMeta jobMeta, DatabaseMeta databaseMeta ) {
    return new Database( jobMeta, databaseMeta );
  }
}
