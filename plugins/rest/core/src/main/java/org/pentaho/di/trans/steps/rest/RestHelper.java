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

package org.pentaho.di.trans.steps.rest;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepHelper;

import java.util.Arrays;
import java.util.Map;

public class RestHelper extends BaseStepHelper {

  private static final String APPLICATION_TYPES = "applicationTypes";
  private static final String HTTP_METHODS = "httpMethods";

  public RestHelper() {
    super();
  }

  /**
   * Handles step-specific actions for REST.
   */
  @Override
  protected JSONObject handleStepAction( String method, TransMeta transMeta, Map<String, String> queryParams ) {
    JSONObject response = new JSONObject();
    if ( APPLICATION_TYPES.equals( method ) ) {
      response = applicationTypesAction( queryParams );
    } else if ( HTTP_METHODS.equals( method ) ) {
      response = httpMethodsAction( queryParams );
    } else {
      response.put( ACTION_STATUS, FAILURE_METHOD_NOT_FOUND_RESPONSE );
    }
    return response;
  }

  /**
   * Retrieves the list of application types and returns them as a JSON object.
   *
   * @param queryParams A map of query parameters for the action.
   * @return A JSON object containing the list of application types.
   */
  public JSONObject applicationTypesAction( Map<String, String> queryParams ) {
    JSONObject response = new JSONObject();
    JSONArray applicationTypes = new JSONArray();
    applicationTypes.addAll( Arrays.asList( RestMeta.APPLICATION_TYPES ) );
    response.put( APPLICATION_TYPES, applicationTypes );
    return response;
  }

  /**
   * Retrieves the list of HTTP methods and returns them as a JSON object.
   *
   * @param queryParams A map of query parameters for the action.
   * @return A JSON object containing the list of HTTP methods.
   */
  public JSONObject httpMethodsAction( Map<String, String> queryParams ) {
    JSONObject response = new JSONObject();
    JSONArray httpMethods = new JSONArray();
    httpMethods.addAll( Arrays.asList( RestMeta.HTTP_METHODS ) );
    response.put( HTTP_METHODS, httpMethods );
    return response;
  }
}
