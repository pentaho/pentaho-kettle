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

package org.pentaho.di.trans.steps.systemdata;

import com.google.common.annotations.VisibleForTesting;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepHelper;
import org.pentaho.di.trans.step.StepInterface;

import java.util.Arrays;
import java.util.Map;

public class SystemDataStepHelper extends BaseStepHelper {

  @VisibleForTesting
  protected static final String RESPONSE_KEY = "types";
  protected static final String METHOD_NAME = "type";

  /**
   * Generates a JSON response containing all available system data types in {@link SystemDataTypes} except for
   * TYPE_SYSTEM_INFO_NONE.
   * Each system data type is represented as a JSON object with its code and description.
   * The response includes a status indicating success.
   *
   * @return a {@link JSONObject} containing the list of system data types and the action status.
   */
  @SuppressWarnings( "unchecked" )
  protected JSONObject type() {
    JSONArray systemDataTypes = new JSONArray();

    Arrays.stream( SystemDataTypes.values() )
      .filter( type -> type != SystemDataTypes.TYPE_SYSTEM_INFO_NONE )
      .forEach( type -> {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put( "id", type.getCode() );
        jsonObject.put( "name", type.getDescription() );
        systemDataTypes.add( jsonObject );
      } );

    JSONObject response = new JSONObject();
    response.put( RESPONSE_KEY, systemDataTypes );
    response.put( StepInterface.ACTION_STATUS, StepInterface.SUCCESS_RESPONSE );
    return response;
  }

  /**
   * Handles a step action based on the provided method name.
   * <p>
   * If the method name matches the expected {@code METHOD_NAME}, this method delegates to the {@code type()} method
   * and returns its result as a {@link JSONObject}. Otherwise, it returns a failure response indicating that the
   * requested method was not found.
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
    if ( METHOD_NAME.equals( method ) ) {
      return type();
    }

    JSONObject response = new JSONObject();
    response.put( ACTION_STATUS, FAILURE_METHOD_NOT_FOUND_RESPONSE );
    return response;
  }
}
