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

package org.pentaho.di.trans.steps.tableoutput;

import org.json.simple.JSONObject;
import org.pentaho.di.core.SQLStatement;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaInteger;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepHelper;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;

import java.util.Map;
import java.util.Objects;

public class TableOutputHelper extends BaseStepHelper {

  public static final String STEP_NAME = "stepName";
  public static final String DETAILS = "details";

  public static final String CONNECTION = "connection";

  private static final String GET_SQL = "getSQL";

  private static final Class<?> PKG = TableOutputMeta.class;

  public TableOutputHelper() {
    super();
  }

  @Override
  protected JSONObject handleStepAction( String method, TransMeta transMeta, Map<String, String> queryParams ) {
    JSONObject response = new JSONObject();
    try {
      if ( GET_SQL.equals( method ) ) {
        response = getSQLAction( transMeta, queryParams );
      } else {
        response.put( ACTION_STATUS, FAILURE_METHOD_NOT_FOUND_RESPONSE );
      }
    } catch ( Exception ex ) {
      response.put( ACTION_STATUS, FAILURE_RESPONSE );
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
            response.put( DETAILS, BaseMessages.getString( PKG, "TableOutput.NoSQL.DialogMessage" ) );
          }
        } else {
          response.put( DETAILS, sql.getError() );
        }
      } else {
        response.put( DETAILS, BaseMessages.getString( PKG, "TableOutput.NoSQL.EmptyCSVFields" ) );
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
      if ( stepMeta != null && stepMeta.getStepMetaInterface() instanceof TableOutputMeta info ) {
        info.setDatabaseMeta( transMeta.findDatabase( connection ) );

        RowMetaInterface prev = transMeta.getPrevStepFields( stepName );
        removeTableNameFieldIfNeeded( info, prev );

        processSpecifiedFieldsIfNeeded( info, prev );

        boolean autoInc = false;
        String pk = null;

        // Add the auto-increment field too if any is present.
        AutoIncrementData autoData = addAutoIncrementFieldIfNeeded( info, prev );
        autoInc = autoData.autoInc;
        pk = autoData.pk;

        if ( isValidRowMeta( prev ) ) {
          return info.getSQLStatements( transMeta, stepMeta, prev, pk, autoInc, pk );
        } else {
          return null;
        }

      }
    }
    return null;
  }

  private RowMetaInterface removeTableNameFieldIfNeeded( TableOutputMeta info, RowMetaInterface prev ) {
    if ( info.isTableNameInField() && !info.isTableNameInTable() && info.getTableNameField().length() > 0 ) {
      int idx = prev.indexOfValue( info.getTableNameField() );
      if ( idx >= 0 ) {
        prev.removeValueMeta( idx );
      }
    }
    return prev;
  }

  private RowMetaInterface processSpecifiedFieldsIfNeeded( TableOutputMeta info, RowMetaInterface prev )
    throws KettleStepException {
    if ( info.specifyFields() ) {
      // Only use the fields that were specified.
      RowMetaInterface prevNew = new RowMeta();

      for ( int i = 0; i < info.getFieldDatabase().length; i++ ) {
        ValueMetaInterface insValue = prev.searchValueMeta( info.getFieldStream()[ i ] );
        if ( insValue != null ) {
          ValueMetaInterface insertValue = insValue.clone();
          insertValue.setName( info.getFieldDatabase()[ i ] );
          prevNew.addValueMeta( insertValue );
        } else {
          throw new KettleStepException( BaseMessages.getString(
            PKG, "TableOutputDialog.FailedToFindField.Message", info.getFieldStream()[ i ] ) );
        }
      }
      prev = prevNew;
    }
    return prev;
  }

  private AutoIncrementData addAutoIncrementFieldIfNeeded( TableOutputMeta info, RowMetaInterface prev ) {
    boolean autoInc = false;
    String pk = null;

    if ( info.isReturningGeneratedKeys() && !Utils.isEmpty( info.getGeneratedKeyField() ) ) {
      ValueMetaInterface valueMeta = new ValueMetaInteger( info.getGeneratedKeyField() );
      valueMeta.setLength( 15 );
      prev.addValueMeta( 0, valueMeta );
      autoInc = true;
      pk = info.getGeneratedKeyField();
    }

    return new AutoIncrementData( autoInc, pk );
  }

  private static class AutoIncrementData {
    final boolean autoInc;
    final String pk;

    AutoIncrementData( boolean autoInc, String pk ) {
      this.autoInc = autoInc;
      this.pk = pk;
    }
  }

  private static boolean isValidRowMeta( RowMetaInterface rowMeta ) {
    if ( rowMeta == null ) {
      return false;
    }
    for ( ValueMetaInterface value : rowMeta.getValueMetaList() ) {
      String name = value.getName();
      if ( name == null || name.isEmpty() ) {
        return false;
      }
    }
    return true;
  }
}
