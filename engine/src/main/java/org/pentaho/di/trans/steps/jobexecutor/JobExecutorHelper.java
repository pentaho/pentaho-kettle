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
package org.pentaho.di.trans.steps.jobexecutor;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.pentaho.di.core.Const;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepHelper;

import java.util.Map;

public class JobExecutorHelper extends BaseStepHelper {

  protected static final String PARAMETERS = "parameters";
  protected static final String IS_JOB_VALID = "isJobValid";
  protected static final String JOB_PRESENT = "jobPresent";
  private final JobExecutorMeta jobExecutorMeta;

  public JobExecutorHelper( JobExecutorMeta jobExecutorMeta ) {
    this.jobExecutorMeta = jobExecutorMeta;
  }

  /**
   * Handles step-specific actions for JobExecutor.
   *
   * @param method The action method to execute.
   * @param transMeta The metadata of the current transformation.
   * @param queryParams A map of query parameters.
   * @return A JSON object containing the result of the action.
   */
  @Override
  protected JSONObject handleStepAction( String method, TransMeta transMeta, Map<String, String> queryParams ) {
    jobExecutorMeta.setRepository( transMeta.getRepository() );
    JSONObject response = new JSONObject();
    switch ( method ) {
      case PARAMETERS:
        response = getParametersFromJob( transMeta, jobExecutorMeta );
        break;
      case IS_JOB_VALID:
        response = isJobValid( transMeta, jobExecutorMeta );
        break;
      case REFERENCE_PATH:
        response = getReferencePath( transMeta, jobExecutorMeta );
        break;
      default:
        response.put( ACTION_STATUS, FAILURE_METHOD_NOT_FOUND_RESPONSE );
        break;
    }
    return response;
  }

  /**
   * Retrieves the parameters of the job and returns them as a JSON object.
   *
   * @param transMeta The metadata of the current transformation.
   * @param jobExecutorMeta The metadata of the job executor step.
   * @return A JSON object containing the job parameters.
   */
  private JSONObject getParametersFromJob( TransMeta transMeta, JobExecutorMeta jobExecutorMeta ) {
    JSONObject response = new JSONObject();
    JSONArray parameterArray = new JSONArray();
    try {
      JobMeta inputJobMeta = JobExecutorMeta.loadJobMeta(
          transMeta.getBowl(), jobExecutorMeta, jobExecutorMeta.getRepository(), transMeta
      );
      String[] parameters = inputJobMeta.listParameters();
      for ( String name : parameters ) {
        JSONObject parameter = new JSONObject();
        String desc = inputJobMeta.getParameterDescription( name );
        String str = inputJobMeta.getParameterDefault( name );
        parameter.put( "field", "" );
        parameter.put( "variable", Const.NVL( name, "" ) );
        parameter.put( "input", Const.NVL( str, Const.NVL( desc, "" ) ) );
        parameterArray.add( parameter );
      }

      response.put( PARAMETERS, parameterArray );
    } catch ( Exception ex ) {
      log.logError( ex.getMessage() );
      response.put( ACTION_STATUS, FAILURE_RESPONSE );
    }
    return response;
  }

  /**
   * Validates the presence of a job by attempting to load its metadata.
   *
   * @param transMeta The metadata of the current transformation.
   * @param jobExecutorMeta The metadata of the job executor step.
   * @return A JSON object indicating whether the job is valid.
   */
  private JSONObject isJobValid( TransMeta transMeta, JobExecutorMeta jobExecutorMeta ) {
    JSONObject response = new JSONObject();
    try {
      JobExecutorMeta.loadJobMeta( transMeta.getBowl(), jobExecutorMeta, jobExecutorMeta.getRepository(), transMeta );
      response.put( JOB_PRESENT, true );
    } catch ( Exception e ) {
      log.logError( e.getMessage() );
      response.put( JOB_PRESENT, false );
      response.put( "errorMessage", ExceptionUtils.getRootCauseMessage( e ) );
    }
    return response;
  }

  /**
   * Fetches the reference path of the transformation used in the transformation executor step.
   *
   * @return  A JSON object containing:
   * - "referencePath": The full path to the referenced transformation, with environment variables substituted.
   * - "isValidReference": A boolean indicating whether the reference is valid.
   *
   */
  private JSONObject getReferencePath( TransMeta transMeta, JobExecutorMeta jobExecutorMeta ) {
    JSONObject response = new JSONObject();
    try {
      response.put( REFERENCE_PATH, getReferencePath( transMeta, jobExecutorMeta.getDirectoryPath(), jobExecutorMeta.getJobName() ) );
      JobExecutorMeta.loadJobMeta( transMeta.getBowl(), jobExecutorMeta, jobExecutorMeta.getRepository(), transMeta );
      response.put( IS_VALID_REFERENCE, true );
      response.put( IS_TRANS_REFERENCE, false );
    } catch ( Exception exception ) {
      response.put( IS_VALID_REFERENCE, false );
      response.put( IS_TRANS_REFERENCE, false );
    }
    return response;
  }
}
