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

package org.pentaho.di.job.entries.job;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.BaseJobEntryHelper;

import java.util.Arrays;
import java.util.Map;

public class JobEntryJobHelper extends BaseJobEntryHelper {

  protected static final String PARAMETERS = "parameters";
  protected static final String JOB_ENTRY_JOB_REFERENCE_PATH = "referencePath";
  private final JobEntryJob jobEntryJob;

  public JobEntryJobHelper( JobEntryJob jobEntryJob ) {
    this.jobEntryJob = jobEntryJob;
  }

  /**
   * Handles job entry-specific actions for JobEntryJob.
   */
  @Override
  protected JSONObject handleJobEntryAction( String method, JobMeta jobMeta, Map<String, String> queryParams ) {
    JSONObject response = new JSONObject();
    if ( method.equals( JOB_ENTRY_JOB_REFERENCE_PATH ) ) {
      response = getReferencePath( jobMeta );
    } else if ( method.equals( PARAMETERS ) ) {
      response = getParametersFromJob( jobMeta );
    } else {
      response.put( ACTION_STATUS, FAILURE_METHOD_NOT_FOUND_RESPONSE );
    }

    return response;
  }

  /**
   * Retrieves the list of parameters from the given job metadata and returns them as a JSON object.
   * This method is called from JobEntryInterface#doAction dynamically using reflection.
   *
   * @param jobMeta The metadata of the current job.
   * @return A JSON object containing the list of parameters under the key "parameters".
   */
  public JSONObject getParametersFromJob( JobMeta jobMeta ) {
    JSONObject response = new JSONObject();
    try {
      JobMeta inputJobMeta = jobEntryJob.getJobMeta( jobMeta.getRepository(), jobEntryJob.getMetaStore(), jobEntryJob.getParentJobMeta() );
      String[] parametersList = inputJobMeta.listParameters();

      JSONArray parametersData = new JSONArray();
      parametersData.addAll( Arrays.asList( parametersList ) );
      response.put( PARAMETERS, parametersData );
    } catch ( KettleException kettleException ) {
      response.put( ACTION_STATUS, FAILURE_RESPONSE );
      response.put( "error", kettleException.getMessage() );
    }

    return response;
  }

  /**
   * Retrieves the reference path of the job associated with the job entry.
   * @param jobMeta The job metadata containing the job entry.
   * @return A JSON object containing:
   * - "referencePath": The reference path of the job.
   * - "isValidReference": A boolean indicating if the reference is valid.
   * - "isTransReference": A boolean indicating if the reference is a job.
   */
  public JSONObject getReferencePath( JobMeta jobMeta ) {
    JSONObject response = new JSONObject();
    try {
      response.put( JOB_ENTRY_JOB_REFERENCE_PATH, getReferencePath( jobMeta, jobEntryJob.getDirectory(), jobEntryJob.getJobName(),
          jobEntryJob.getSpecificationMethod(), jobEntryJob.getFilename() ) );
      jobEntryJob.getJobMeta( jobMeta.getRepository(), null, jobMeta );
      response.put( IS_VALID_REFERENCE, true );
      response.put( IS_TRANS_REFERENCE, false );
    } catch ( Exception exception ) {
      response.put( IS_VALID_REFERENCE, false );
      response.put( IS_TRANS_REFERENCE, false );
    }
    return response;
  }
}
