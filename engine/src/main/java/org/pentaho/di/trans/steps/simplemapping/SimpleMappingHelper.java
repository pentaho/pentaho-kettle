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

package org.pentaho.di.trans.steps.simplemapping;

import org.json.simple.JSONObject;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepHelper;

import java.util.Map;

public class SimpleMappingHelper extends BaseStepHelper {
  protected static final String SIMPLE_MAPPING_REFERENCE_PATH = "referencePath";

  private final SimpleMappingMeta simpleMappingMeta;

  public SimpleMappingHelper( SimpleMappingMeta simpleMappingMeta) {
    this.simpleMappingMeta = simpleMappingMeta;
  }

  /**
   * Handles step-specific actions for Simple Mapping step.
   */
  @Override
  protected JSONObject handleStepAction( String method, TransMeta transMeta,
                                        Map<String, String> queryParams ) {
    JSONObject response = new JSONObject();
    if ( method.equalsIgnoreCase( SIMPLE_MAPPING_REFERENCE_PATH ) ) {
      response = getReferencePath( transMeta );
    } else {
      response.put( ACTION_STATUS, FAILURE_METHOD_NOT_FOUND_RESPONSE );
    }

    return response;
  }

  /**
   * Fetches the reference path of the sub transformation used in the Simple Mapping Step.
   *
   * @return  A JSON object containing:
   * - "referencePath": The full path to the referenced transformation, with environment variables substituted.
   * - "isValidReference": A boolean indicating whether the referenced transformation is valid.
   *
   */
  private JSONObject getReferencePath( TransMeta transMeta ) {
    JSONObject response = new JSONObject();
    response.put( SIMPLE_MAPPING_REFERENCE_PATH, getReferencePath( transMeta, simpleMappingMeta.getDirectoryPath(), simpleMappingMeta.getTransName() ) );
    return validateAndPutReferenceStatus( transMeta, response, simpleMappingMeta );
  }
}
