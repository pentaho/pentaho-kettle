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
package org.pentaho.di.job.entries.trans;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.BaseJobEntryHelper;
import org.pentaho.di.trans.TransMeta;

import java.util.Arrays;
import java.util.Map;

public class JobEntryTransHelper extends BaseJobEntryHelper {

  protected static final String PARAMETERS = "parameters";
  private final JobEntryTrans jobEntryTrans;

  public JobEntryTransHelper( JobEntryTrans jobEntryTrans ) {
    this.jobEntryTrans = jobEntryTrans;
  }

  /**
   * Handles job entry-specific actions for JobEntryTrans.
   *
   * @param method The action method to execute.
   * @param jobMeta The metadata of the current job.
   * @param queryParams A map of query parameters.
   * @return A JSON object containing the result of the action.
   */
  @Override
  protected JSONObject handleJobEntryAction( String method, JobMeta jobMeta, Map<String, String> queryParams ) {
    JSONObject response = new JSONObject();
    if ( method.equals( PARAMETERS ) ) {
      response = getParametersFromTrans( jobMeta );
    } else {
      response.put( ACTION_STATUS, FAILURE_METHOD_NOT_FOUND_RESPONSE );
    }
    return response;
  }

  /**
   * Retrieves the list of parameters from the given transformation metadata and returns them as a JSON object.
   * This method is called from JobEntryInterface#doAction dynamically using reflection.
   *
   * @return A JSON object containing the list of parameters under the key "parameters".
   */
  public JSONObject getParametersFromTrans( JobMeta jobMeta ) {
    JSONObject response = new JSONObject();
    try {
      TransMeta inputTransMeta = jobEntryTrans.getTransMeta( jobMeta.getRepository(), jobEntryTrans.getMetaStore(), jobEntryTrans.getParentJobMeta() );
      String[] parametersList = inputTransMeta.listParameters();

      JSONArray parametersData = new JSONArray();
      parametersData.addAll( Arrays.asList( parametersList ) );
      response.put( PARAMETERS, parametersData );
    } catch ( KettleException kettleException ) {
      response.put( ACTION_STATUS, FAILURE_RESPONSE );
      response.put( "error", kettleException.getMessage() );
    }

    return response;
  }
}
