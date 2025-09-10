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


package org.pentaho.di.trans.steps.randomvalue;

import com.google.common.annotations.VisibleForTesting;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepHelper;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

/**
 * Currently, provides action handler for exposing available
 * random function types, with potential for additional step-level utilities in the future.
 *
 * @see BaseStepHelper
 * @see RandomValueMeta
 */
public class RandomValueStepHelper extends BaseStepHelper {

  @VisibleForTesting
  protected static final String RESPONSE_KEY = "randomFunctionTypes";
  protected static final String METHOD_GET_RANDOM_FUNCTION_TYPES = "getRandomFunctionTypes";

  /**
   * Generates a JSON response containing the available random function types.
   * <p>
   * Iterates through the list of random value functions defined in {@link RandomValueMeta#functions},
   * and constructs a JSON array where each element represents a function type with its code and description.
   * The resulting JSON object contains this array under the key "randomFunctionTypes".
   * </p>
   *
   * @return a {@link JSONObject} containing the available random function types
   */
  @VisibleForTesting
  @SuppressWarnings( "unchecked" )
  protected JSONObject getRandomFunctionTypes() {
    JSONArray functionTypes = new JSONArray();

    Arrays.stream( RandomValueMeta.functions )
      .filter( Objects::nonNull )
      .forEach( func -> {
        JSONObject type = new JSONObject();
        type.put( "id", func.getCode() );
        type.put( "name", func.getDescription() );
        functionTypes.add( type );
      } );

    JSONObject response = new JSONObject();
    response.put( RESPONSE_KEY, functionTypes );
    return response;
  }

   /**
   * Handles step-specific actions for the RandomValue step.
   * <p>
   * This method overrides {@link BaseStepHelper#handleStepAction(String, TransMeta, Map)}
   * to provide RandomValue-specific functionality. Currently, supports:
   * <ul>
   *   <li>{@value #METHOD_GET_RANDOM_FUNCTION_TYPES} - returns available random function types</li>
   * </ul>
   * </p>
   *
   * @param method      the name of the method to execute (case-sensitive)
   * @param transMeta   the transformation metadata (currently unused for this step)
   * @param queryParams query parameters for the action (currently unused for this step)
   * @return a {@link JSONObject} containing either the action result or an error response
   * @see BaseStepHelper#handleStepAction(String, TransMeta, Map)
   */
  @Override
  @SuppressWarnings( "unchecked" )
  protected JSONObject handleStepAction( String method, TransMeta transMeta, Map<String, String> queryParams ) {
    if ( METHOD_GET_RANDOM_FUNCTION_TYPES.equals( method ) ) {
      return getRandomFunctionTypes();
    }

    JSONObject response = new JSONObject();
    response.put( ACTION_STATUS, FAILURE_METHOD_NOT_FOUND_RESPONSE );
    return response;
  }
}
