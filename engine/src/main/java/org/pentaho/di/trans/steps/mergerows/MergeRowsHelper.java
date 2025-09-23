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

package org.pentaho.di.trans.steps.mergerows;

import org.apache.commons.collections.CollectionUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepHelper;
import org.pentaho.di.trans.step.StepHelperInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.errorhandling.StreamInterface;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Helper class for the Merge Rows step
 * <p>
 * Provides step-specific actions and utilities for the Merge Rows step, such as retrieving reference step fields.
 * </p>
 */
public class MergeRowsHelper extends BaseStepHelper implements StepHelperInterface {

  private static final Class<?> PKG = MergeRowsMeta.class;

  private static final String REFERENCE_STEP_FIELDS = "referenceStepFields";

  private final MergeRowsMeta mergeRowsMeta;

  public MergeRowsHelper( MergeRowsMeta mergeRowsMeta ) {
    this.mergeRowsMeta = mergeRowsMeta;
  }

  /**
   * Handles step-specific actions for MergeRows.
   *
   * @param method      the action method name
   * @param transMeta   the transformation metadata
   * @param queryParams additional query parameters
   * @return a JSON object containing the response
   */
  @Override
  @SuppressWarnings( "unchecked" )
  protected JSONObject handleStepAction( String method, TransMeta transMeta, Map<String, String> queryParams ) {
    JSONObject response = new JSONObject();
    if ( REFERENCE_STEP_FIELDS.equals( method ) ) {
      try {
        response = getReferenceStepsFields( transMeta );
      } catch ( KettleException e ) {
        response.put( ACTION_STATUS, FAILURE_RESPONSE );
      }
    } else {
      response.put( ACTION_STATUS, FAILURE_METHOD_NOT_FOUND_RESPONSE );
    }
    return response;
  }

  /**
   * Retrieves the field names from the reference step (first info stream) for MergeRows.
   *
   * @param transMeta the transformation metadata
   * @return a JSON object containing the field names under the key "stepFieldsNames"
   * @throws KettleException if an error occurs while retrieving the fields
   */
  @SuppressWarnings( "unchecked" )
  public JSONObject getReferenceStepsFields( TransMeta transMeta ) throws KettleException {
    JSONObject response = new JSONObject();
    JSONArray stepFieldsNames = new JSONArray();
    try {
      List<StreamInterface> infoStreams = mergeRowsMeta.getStepIOMeta().getInfoStreams();
      if ( CollectionUtils.isNotEmpty( infoStreams ) ) {
        String stepName = infoStreams.get( 0 ).getStepname();
        StepMeta stepMeta = transMeta.findStep( stepName );
        String[] fields = transMeta.getStepFields( stepMeta ).getFieldNames();

        stepFieldsNames.addAll( Arrays.asList( fields ) );
      }
      response.put( "stepFieldsNames", stepFieldsNames );
    } catch ( KettleException e ) {
      log.logError( e.getMessage() );
      throw new KettleException(
        BaseMessages.getString( PKG, "MergeRowsMeta.CheckResult.ErrorGettingPrevStepFields" ) );
    } catch ( Exception e ) {
      log.logError( e.getMessage() );
      response.put( StepInterface.ACTION_STATUS, StepInterface.FAILURE_RESPONSE );
    }
    return response;
  }
}
