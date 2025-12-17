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

package org.pentaho.di.trans.steps.metainject;

import org.json.simple.JSONObject;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepHelper;

import java.util.Map;

public class MetaInjectHelper extends BaseStepHelper {

  protected static final String META_INJECT_REFERENCE_PATH = "referencePath";

  private final MetaInjectMeta metaInjectMeta;

  public MetaInjectHelper( MetaInjectMeta metaInjectMeta ) {
    this.metaInjectMeta = metaInjectMeta;
  }

  /**
   * Handles step-specific actions for ETL Metadata Injection step.
   */
  @Override
  protected JSONObject handleStepAction( String method, TransMeta transMeta,
                                        Map<String, String> queryParams ) {
    JSONObject response = new JSONObject();
    if ( method.equalsIgnoreCase( META_INJECT_REFERENCE_PATH ) ) {
      response = getReferencePath( transMeta );
    } else {
      response.put( ACTION_STATUS, FAILURE_METHOD_NOT_FOUND_RESPONSE );
    }

    return response;
  }

  /**
   * Fetches the reference path of the transformation used in the ETL metadata injection step.
   *
   * @return  A JSON object containing:
   * - "referencePath": The full path to the referenced transformation, with environment variables substituted.
   * - "isValidReference": A boolean indicating whether the referenced transformation is valid.
   *
   */
  private JSONObject getReferencePath( TransMeta transMeta ) {
    JSONObject response = new JSONObject();
    response.put( REFERENCE_PATH, getReferencePath( transMeta, metaInjectMeta.getDirectoryPath(), metaInjectMeta.getTransName(),
        metaInjectMeta.getSpecificationMethod(), metaInjectMeta.getFileName() ) );
    try {
      MetaInjectMeta.loadTransformationMeta( transMeta.getBowl(), metaInjectMeta, transMeta.getRepository(),
          null, transMeta );
      response.put( IS_VALID_REFERENCE, true );
      response.put( IS_TRANS_REFERENCE, true );
    } catch( Exception exception ) {
      response.put( IS_VALID_REFERENCE, false );
      response.put( IS_TRANS_REFERENCE, true );
    }
    return response;
  }
}
