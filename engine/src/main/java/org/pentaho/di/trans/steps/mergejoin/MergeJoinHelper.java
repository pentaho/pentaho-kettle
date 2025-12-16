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

package org.pentaho.di.trans.steps.mergejoin;

import org.json.simple.JSONObject;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepHelper;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.errorhandling.StreamInterface;
import org.pentaho.di.trans.steps.util.StepKeyUtils;
import java.util.List;
import java.util.Map;

public class MergeJoinHelper extends BaseStepHelper {

  private static final String PREVIOUS_KEYS = "previousKeys";

  public MergeJoinHelper() {
    super();
  }

  /**
   * Handles step-specific actions for MergeJoin.
   */
  @Override
  protected JSONObject handleStepAction( String method, TransMeta transMeta, Map<String, String> queryParams ) {
    JSONObject response = new JSONObject();
    try {
      if ( PREVIOUS_KEYS.equals( method ) ) {
        response = previousKeysAction( transMeta, queryParams );
      } else {
        response.put( ACTION_STATUS, FAILURE_METHOD_NOT_FOUND_RESPONSE );
      }
    } catch ( Exception ex ) {
      response.put( ACTION_STATUS, FAILURE_RESPONSE );
    }
    return response;
  }

  /**
   * Retrieves the keys from the previous steps connected to the specified MergeJoin step.
   *
   * @param transMeta   The transformation metadata.
   * @param queryParams A map of query parameters for the action, including "stepName".
   * @return A JSON object containing the previous step keys and their fields.
   * @throws KettleException if there's an error retrieving step information.
   */
  public JSONObject previousKeysAction( TransMeta transMeta, Map<String, String> queryParams ) throws KettleException {
    JSONObject response = new JSONObject();
    JSONObject stepKeys = new JSONObject();

    String stepName = queryParams.get( "stepName" );

    if ( !StepKeyUtils.isValidStepName( stepName ) ) {
      return StepKeyUtils.buildSuccessResponse( response, stepKeys );
    }

    // Find and validate step
    StepMeta stepMeta = StepKeyUtils.findAndValidateStep( transMeta, stepName, MergeJoinMeta.class );
    if ( stepMeta == null ) {
      return StepKeyUtils.buildSuccessResponse( response, new JSONObject() );
    }

    // Get info streams and build step keys
    MergeJoinMeta mergeJoinMeta = (MergeJoinMeta) stepMeta.getStepMetaInterface();
    List<StreamInterface> infoStreams = mergeJoinMeta.getStepIOMeta().getInfoStreams();
    stepKeys = StepKeyUtils.buildStepKeysFromStreams( transMeta, infoStreams );

    return StepKeyUtils.buildSuccessResponse( response, stepKeys );
  }
}
