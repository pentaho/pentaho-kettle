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


package org.pentaho.di.trans.step;

import org.json.simple.JSONObject;
import org.pentaho.di.trans.TransMeta;

import java.util.Map;

/**
 * Defines helper methods for executing "step actions" used by a step user interface (UI).
 * <p>
 * Step actions are step-specific, non-runtime operations invoked from a step dialog
 * (e.g. fetching metadata, testing connections) whose results are displayed in the dialog.
 * <p>
 * They are not part of transformation or step execution logic.
 * <p>
 * This interface declares common response field constants and a default implementation that
 * returns a "method not found" status when no action is implemented.
 */
public interface StepHelperInterface {

  //Common response fields
  String STATUS = "Status";
  String ACTION_STATUS = "actionStatus";
  String SUCCESS_RESPONSE = "Action successful";
  String FAILURE_RESPONSE = "Action failed";
  String FAILURE_METHOD_NOT_FOUND_RESPONSE = "Action failed with method not found";

  /**
   * Default implementation for performing a step action.
   *
   * @param method      The name of the method to execute.
   * @param transMeta   The transformation metadata associated with the step.
   * @param queryParams A map of query parameters for the action.
   * @return A JSON object containing the action status, defaulting to a failure
   * response if the method is not found.
   */
  default JSONObject stepAction( String method, TransMeta transMeta,
                                 Map<String, String> queryParams ) {
    JSONObject response = new JSONObject();
    response.put( ACTION_STATUS, FAILURE_METHOD_NOT_FOUND_RESPONSE );
    return response;
  }

}
