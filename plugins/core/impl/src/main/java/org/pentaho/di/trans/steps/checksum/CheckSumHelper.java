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

package org.pentaho.di.trans.steps.checksum;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepHelper;
import org.pentaho.di.trans.step.StepInterface;

import java.util.Map;

public class CheckSumHelper extends BaseStepHelper {
  public static final String CHECKSUM_TYPE_RESPONSE_KEY = "checkSumType";
  public static final String RESULT_TYPE_RESPONSE_KEY = "resultType";
  public static final String EVALUATION_METHOD_RESPONSE_KEY = "evaluationMethod";
  public static final String DEFAULT_VALUES_ACTION = "getDefaultValues";
  public static final String DEFAULT_VALUES = "defaultValues";
  private static final String CHECKSUM_TYPES = "getCheckSumTypes";
  private static final String EVALUATION_METHODS = "getEvaluationMethods";
  private static final String RESULT_TYPES = "getResultTypes";
  private final CheckSumMeta checkSumMeta;

  public CheckSumHelper( CheckSumMeta checkSumMeta ) {
    this.checkSumMeta = checkSumMeta;
  }

  /**
   * Handles step-specific actions for CheckSum.
   */
  @Override
  protected JSONObject handleStepAction( String method, TransMeta transMeta,
                                         Map<String, String> queryParams ) {
    JSONObject response = new JSONObject();
    switch ( method ) {
      case CHECKSUM_TYPES:
        response = checkSumTypes();
        break;
      case EVALUATION_METHODS:
        response = evaluationMethods();
        break;
      case RESULT_TYPES:
        response = resultTypes();
        break;
      case DEFAULT_VALUES_ACTION:
        response = getDefaultValues();
        break;
      default:
        response.put( ACTION_STATUS, FAILURE_METHOD_NOT_FOUND_RESPONSE );
        break;
    }
    return response;
  }

  /**
   * Retrieves the list of available checksum types.
   */
  public JSONObject checkSumTypes() {
    JSONObject response = new JSONObject();
    JSONArray checkSumTypes = new JSONArray();
    for ( int i = 0; i < CheckSumMeta.checksumtypeDescs.length; i++ ) {
      JSONObject checkSumType = new JSONObject();
      checkSumType.put( "id", CheckSumMeta.checksumtypeCodes[ i ] );
      checkSumType.put( "name", CheckSumMeta.checksumtypeDescs[ i ] );
      checkSumTypes.add( checkSumType );
    }

    response.put( "checkSumTypes", checkSumTypes );
    response.put( StepInterface.ACTION_STATUS, StepInterface.SUCCESS_RESPONSE );
    return response;
  }

  /**
   * Retrieves the list of available evaluation methods.
   */
  public JSONObject evaluationMethods() {
    JSONObject response = new JSONObject();
    JSONArray evaluationMethods = new JSONArray();
    for ( int i = 0; i < CheckSumMeta.EVALUATION_METHOD_DESCS.length; i++ ) {
      JSONObject evaluationMethod = new JSONObject();
      evaluationMethod.put( "id", CheckSumMeta.EVALUATION_METHOD_CODES[ i ] );
      evaluationMethod.put( "name", CheckSumMeta.EVALUATION_METHOD_DESCS[ i ] );
      evaluationMethods.add( evaluationMethod );
    }

    response.put( "evaluationMethods", evaluationMethods );
    response.put( StepInterface.ACTION_STATUS, StepInterface.SUCCESS_RESPONSE );
    return response;
  }

  /**
   * Retrieves the list of available result types.
   */
  public JSONObject resultTypes() {
    JSONObject response = new JSONObject();
    JSONArray resultTypes = new JSONArray();
    for ( int i = 0; i < CheckSumMeta.resultTypeCode.length; i++ ) {
      JSONObject resultType = new JSONObject();
      resultType.put( "id", CheckSumMeta.resultTypeCode[ i ] );
      resultType.put( "name", CheckSumMeta.resultTypeCode[ i ] );
      resultTypes.add( resultType );
    }

    response.put( "resultTypes", resultTypes );
    response.put( StepInterface.ACTION_STATUS, StepInterface.SUCCESS_RESPONSE );
    return response;
  }

  /**
   * Retrieves the default values for the CheckSum step.
   */
  public JSONObject getDefaultValues() {
    JSONObject defaultValues = new JSONObject();
    JSONObject response = new JSONObject();
    checkSumMeta.setDefault();
    String checkSumType = checkSumMeta.getCheckSumType();
    String resultType = CheckSumMeta.resultTypeCode[ checkSumMeta.getResultType() ];
    String evaluationMethod = CheckSumMeta.EVALUATION_METHOD_CODES[ checkSumMeta.getEvaluationMethod() ];
    defaultValues.put( CHECKSUM_TYPE_RESPONSE_KEY, checkSumType );
    defaultValues.put( RESULT_TYPE_RESPONSE_KEY, resultType );
    defaultValues.put( EVALUATION_METHOD_RESPONSE_KEY, evaluationMethod );
    response.put( DEFAULT_VALUES, defaultValues );
    response.put( StepInterface.ACTION_STATUS, StepInterface.SUCCESS_RESPONSE );
    return response;
  }
}
