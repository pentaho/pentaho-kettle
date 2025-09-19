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
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.job.JobMeta;

import java.util.Map;

/**
 * Base implementation of {@link JobEntryHelperInterface} providing shared infrastructure for executing
 * UI-triggered "job entry actions".
 * <p>
 * Job entry actions are job entry-specific, non-runtime operations initiated from a job entry dialog (e.g. fetching
 * external metadata, validating configuration, testing connections). They do <strong>not</strong>
 * participate in job execution.
 * </p>
 * Subclasses implement only the job entry-specific logic in {@link #handleJobEntryAction(String, JobMeta, java.util.Map)}.
 *
 * @see JobEntryHelperInterface
 */
public abstract class BaseJobEntryHelper implements JobEntryHelperInterface {

  protected LogChannelInterface log;

  protected BaseJobEntryHelper() {
    this.log = KettleLogStore.getLogChannelInterfaceFactory().create( this );
  }

  /**
   * Default implementation for performing a job entry action.
   * <p>
   * This method provides shared infrastructure for executing job entry-specific, non-runtime operations
   * initiated from a job entry dialog (e.g. fetching external metadata, validating configuration,
   * testing connections).
   * Subclasses implement only the job entry-specific logic in {@link #handleJobEntryAction(String, JobMeta, java.util.Map)}.
   *
   * @param method      The name of the method to execute.
   * @param jobMeta     The job metadata associated with the job entry.
   * @param queryParams A map of query parameters for the action.
   * @return A JSON object containing the action status and any additional response data.
   */
  @Override
  public JSONObject jobEntryAction(String method, JobMeta jobMeta, Map<String, String> queryParams ) {
    JSONObject response;
    try {
      response = handleJobEntryAction( method, jobMeta, queryParams );

      if ( !response.containsKey( ACTION_STATUS ) ) {
        response.put( ACTION_STATUS, isFailedResponse( response ) ? FAILURE_RESPONSE : SUCCESS_RESPONSE );
      }
    } catch ( Exception ex ) {
      response = new JSONObject();
      response.put( ACTION_STATUS, FAILURE_RESPONSE );
      log.logError( "Error executing step action '" + method + "' : ", ex.getMessage() );
    }
    return response;
  }

  /**
   * Abstract method for handling a specific job entry action.
   * Subclasses must implement this method to provide job entry-specific logic.
   *
   * @param method      The name of the method to execute.
   * @param jobMeta     The job metadata associated with the job entry.
   * @param queryParams A map of query parameters for the action.
   * @return A JSON object containing the action status and any additional response data.
   */
  protected abstract JSONObject handleJobEntryAction( String method, JobMeta jobMeta, Map<String, String> queryParams );

  /**
   * Utility method to check if a response indicates a failure.
   *
   * @param response The JSON object containing the response data.
   * @return True if the response indicates a failure, false otherwise.
   */
  public boolean isFailedResponse( JSONObject response ) {
    if ( response == null ) {
      return true;
    }
    Object status = response.get( ACTION_STATUS );
    if ( status == null ) {
      return false;
    }
    return FAILURE_RESPONSE.equalsIgnoreCase( status.toString() );
  }
}

