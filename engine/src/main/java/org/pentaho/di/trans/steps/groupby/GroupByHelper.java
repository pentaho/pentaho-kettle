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

package org.pentaho.di.trans.steps.groupby;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepHelper;

import java.util.Arrays;
import java.util.Map;

public class GroupByHelper extends BaseStepHelper {

  private static final String TYPE_GROUP_CODE = "typeGroupCode";

  public GroupByHelper() {
    super();
  }

  /**
   * Handles step-specific actions for GroupBy.
   */
  @Override
  protected JSONObject handleStepAction( String method, TransMeta transMeta, Map<String, String> queryParams ) {
    JSONObject response = new JSONObject();
    if ( TYPE_GROUP_CODE.equals( method ) ) {
      response = getTypeGroupCode();
    } else {
      response.put( ACTION_STATUS, FAILURE_METHOD_NOT_FOUND_RESPONSE );
    }
    return response;
  }

  /**
   * Retrieves the list of type group codes and returns them as a JSON object.
   *
   * @return A JSON object containing the list of type group codes.
   */
  public JSONObject getTypeGroupCode() {
    JSONObject response = new JSONObject();
    JSONArray typeValues = new JSONArray();
    typeValues.addAll( Arrays.asList( GroupByMeta.typeGroupCode ) );
    response.put( TYPE_GROUP_CODE, typeValues );
    return response;
  }
}
