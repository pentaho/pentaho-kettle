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


package org.pentaho.di.trans.steps.addsequence;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.logging.LoggingObjectType;
import org.pentaho.di.core.logging.SimpleLoggingObject;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepHelper;
import org.pentaho.di.trans.step.StepInterface;

import java.util.Arrays;
import java.util.Map;

public class AddSequenceHelper extends BaseStepHelper {

  private static final String GET_SEQUENCE = "getSequence";

  public AddSequenceHelper() {
    super();
  }

  /**
   * Handles step-specific actions for Calculator.
   */
  @Override
  protected JSONObject handleStepAction( String method, TransMeta transMeta, Map<String, String> queryParams ) {
    JSONObject response = new JSONObject();
    try {
      switch ( method ) {
        case GET_SEQUENCE:
          response = getSequenceAction( transMeta, queryParams );
          break;
        default:
          response.put( ACTION_STATUS, FAILURE_METHOD_NOT_FOUND_RESPONSE );
          break;
      }
    } catch ( Exception ex ) {
      response.put( ACTION_STATUS, FAILURE_RESPONSE );
    }
    return response;
  }

  @SuppressWarnings( "java:S1144" ) // Using reflection this method is being invoked
  public JSONObject getSequenceAction( TransMeta transMeta, Map<String, String> queryParams ) {
    JSONObject response = new JSONObject();
    response.put( StepInterface.ACTION_STATUS, StepInterface.FAILURE_RESPONSE );

    DatabaseMeta databaseMeta = transMeta.findDatabase( queryParams.get( "connection" ) );
    LoggingObjectInterface loggingObject = new SimpleLoggingObject(
        "Add Sequence Step", LoggingObjectType.STEP, null );

    if ( databaseMeta != null ) {
      try ( Database database = new Database( loggingObject, databaseMeta ) ) {
        database.connect();
        String[] sequences = database.getSequences();
        if ( null != sequences && sequences.length > 0 ) {
          sequences = Const.sortStrings( sequences );
          JSONArray sequenceNames = new JSONArray();
          sequenceNames.addAll( Arrays.asList( sequences ) );
          response.put( "sequences", sequenceNames );
          response.put( StepInterface.ACTION_STATUS, StepInterface.SUCCESS_RESPONSE );
        }
      } catch ( KettleDatabaseException e ) {
        log.logError( e.getMessage() );
      }
    }
    return response;
  }

}
