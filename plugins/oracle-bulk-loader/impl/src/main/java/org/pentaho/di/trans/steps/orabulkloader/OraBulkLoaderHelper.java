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

package org.pentaho.di.trans.steps.orabulkloader;

import org.json.simple.JSONObject;
import org.pentaho.di.core.SQLStatement;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepHelper;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;

import java.util.Map;
import java.util.Objects;

@SuppressWarnings( "unchecked" )
public class OraBulkLoaderHelper extends BaseStepHelper {

  public static final String STEP_NAME = "stepName";
  public static final String DETAILS = "details";
  public static final String CONNECTION = "connection";

  private static final String GET_SQL = "getSQL";
  private static final Class<?> PKG = OraBulkLoaderHelper.class;

  public OraBulkLoaderHelper() {
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
      response.put( DETAILS, ex.getMessage() );
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
            response.put( DETAILS, BaseMessages.getString( PKG, "OraBulkLoaderHelper.NoSQLNeeds.DialogMessage" ) );
          }
        } else {
          response.put( DETAILS, sql.getError() );
        }
      } else {
        response.put( DETAILS, BaseMessages.getString( PKG, "OraBulkLoaderHelper.GetSQL.NotReceivingAnyFields" ) );
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
      
      if ( stepMeta != null && stepMeta.getStepMetaInterface() instanceof OraBulkLoaderMeta meta ) {
        meta.setDatabaseMeta( transMeta.findDatabase( connection ) );

        RowMetaInterface prev = transMeta.getPrevStepFields( stepName );

        String[] fieldStream = meta.getFieldStream();
        if ( prev != null && prev.size() > 0 && fieldStream != null && fieldStream.length > 0 ) {
          return meta.getSQLStatements( transMeta, stepMeta, prev, null, null );
        } else {
          return null;
        }
      }
    }
    return null;
  }
}
