/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2025 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 ******************************************************************************/

package org.pentaho.di.job.entry;

import org.json.simple.JSONObject;
import org.pentaho.di.job.JobMeta;

import java.util.Map;

/**
 * Defines helper methods for executing "job entry actions" used by a job entry user interface (UI).
 * <p>
 * Job entry actions are job entry-specific, non-runtime operations invoked from a job entry dialog
 * (e.g. fetching metadata, testing connections) whose results are displayed in the dialog.
 * <p>
 * They are not part of job or job entry execution logic.
 * <p>
 * This interface declares common response field constants and a default implementation that
 * returns a "method not found" status when no action is implemented.
 */
public interface JobEntryHelperInterface {

  //Common response fields
  String STATUS = "Status";
  String ACTION_STATUS = "actionStatus";
  String SUCCESS_RESPONSE = "Action successful";
  String FAILURE_RESPONSE = "Action failed";
  String FAILURE_METHOD_NOT_FOUND_RESPONSE = "Action failed with method not found";
  int SUCCESS_STATUS = 1;
  int FAILURE_STATUS = -1;
  int NOT_EXECUTED_STATUS = 0;

  /**
   * Default implementation for performing a job entry action.
   *
   * @param method      The name of the method to execute.
   * @param jobMeta     The job metadata associated with the job entry.
   * @param queryParams A map of query parameters for the action.
   * @return A JSON object containing the action status, defaulting to a failure
   * response if the method is not found.
   */
  default JSONObject jobEntryAction( String method, JobMeta jobMeta, Map<String, String> queryParams ) {
    JSONObject response = new JSONObject();
    response.put( ACTION_STATUS, FAILURE_METHOD_NOT_FOUND_RESPONSE );
    return response;
  }
}
