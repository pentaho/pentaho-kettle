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

package org.pentaho.di.trans.steps.update;

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

public class UpdateHelper extends BaseStepHelper {

  private static final String GET_SQL = "getSQL";
  private static final String ERROR_MESSAGE = "errorMessage";

  private static Class<?> PKG = UpdateMeta.class; // for i18n purposes, needed by Translator2!!

  UpdateMeta updateMeta ;
  public UpdateHelper( UpdateMeta updateMeta ) {
    super();
    this.updateMeta = updateMeta;
  }

  /**
   * Handles step-specific actions for Formula.
   */
  @Override
  protected JSONObject handleStepAction( String method, TransMeta transMeta, Map<String, String> queryParams ) {
    JSONObject response = new JSONObject();
    try {
      switch ( method ) {
        case GET_SQL:
          response = getSQLAction( transMeta, queryParams );
          break;
        default:
          response.put( ACTION_STATUS, FAILURE_METHOD_NOT_FOUND_RESPONSE );
          break;
      }
    } catch ( Exception ex ) {
      response.put( ACTION_STATUS, FAILURE_RESPONSE );
      response.put( ERROR_MESSAGE, ex.getMessage() );
    }
    return response;
  }
  @SuppressWarnings( "java:S1144" ) // Using reflection this method is being invoked
  public JSONObject getSQLAction( TransMeta transMeta, Map<String, String> queryParams ) {
    JSONObject response = new JSONObject();
    try {

      StepMeta stepInfo = new StepMeta( BaseMessages.getString( PKG, "Update.StepMeta.Title" ), updateMeta.getParentStepMeta().getName(), updateMeta );
      RowMetaInterface prev = transMeta.getPrevStepFields( updateMeta.getParentStepMeta().getName() );

      SQLStatement sql = updateMeta.getSQLStatements( transMeta, stepInfo, prev, null, null );
      if ( sql.hasError() ) {
        response.put( StepInterface.ACTION_STATUS, StepInterface.FAILURE_RESPONSE );
        response.put( ERROR_MESSAGE, sql.getError() );
        return response;
      }

      if ( sql.hasSQL() ) {
        response.put( "sql", sql.getSQL() );
        response.put( StepInterface.ACTION_STATUS, StepInterface.SUCCESS_RESPONSE );
      } else {
        response.put( StepInterface.ACTION_STATUS, StepInterface.FAILURE_RESPONSE );
        response.put( ERROR_MESSAGE, BaseMessages.getString( PKG, "Update.NoSQLNeeds.DialogMessage" ) );
        return response;
      }
    } catch ( KettleStepException e ) {
      response.put( StepInterface.ACTION_STATUS, StepInterface.FAILURE_RESPONSE );
      response.put( ERROR_MESSAGE, e.getMessage() );
    }

    return response;
  }

}
