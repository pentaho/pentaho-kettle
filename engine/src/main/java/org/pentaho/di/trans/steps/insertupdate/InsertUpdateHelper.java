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

package org.pentaho.di.trans.steps.insertupdate;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.pentaho.di.core.SQLStatement;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepHelper;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.metastore.api.IMetaStore;

import java.util.Map;

public class InsertUpdateHelper extends BaseStepHelper {


  private Repository repository;
  private IMetaStore metaStore;
  private StepMeta stepMeta;
  private static final String GETSQL = "getSQL";
  private static final String GETCOMPARATORS = "getComparators";
  private static final String ERROR_KEY = "error";

  public InsertUpdateHelper() {
    super();
  }

  @Override
  protected JSONObject handleStepAction( String method, TransMeta transMeta,
                                         Map<String, String> queryParams ) {
    JSONObject response = new JSONObject();
    switch ( method ) {
      case GETSQL:
        response = getSQLAction( transMeta, stepMeta );
        break;
      case GETCOMPARATORS:
        response = getComparatorsAction();
        break;
      default:
        response.put( ACTION_STATUS, FAILURE_METHOD_NOT_FOUND_RESPONSE );
        break;
    }
    return response;
  }

  public JSONObject getComparatorsAction() {
    JSONObject response = new JSONObject();
    JSONArray comparators = new JSONArray();

    for ( String comparator : InsertUpdateMeta.COMPARATORS ) {
      JSONObject comparatorJson = new JSONObject();
      comparatorJson.put( "id", comparator );
      comparatorJson.put( "name", comparator );
      comparators.add( comparatorJson );
    }
    response.put( "comparators", comparators );
    return response;
  }

  public JSONObject getSQLAction( TransMeta transMeta, StepMeta stepMeta ) {
    JSONObject response = new JSONObject();
    if ( stepMeta == null ) {
      response.put( ERROR_KEY, "There is no connection defined in this step." );
      return response;
    }
    try {
      RowMetaInterface prev = transMeta.getPrevStepFields( stepMeta.getName() );
      InsertUpdateMeta insertUpdateMeta = (InsertUpdateMeta) stepMeta.getStepMetaInterface();
      SQLStatement sql = insertUpdateMeta.getSQLStatements(
        transMeta, stepMeta, prev, repository, metaStore );
      if ( !sql.hasError() ) {
        response.put( "sql", sql.getSQL() );
      } else {
        response.put( ERROR_KEY, sql.getError() );
      }
    } catch ( Exception e ) {
      response.put( ERROR_KEY, "Error generating SQL: " + e.getMessage() );
    }
    return response;
  }
}
