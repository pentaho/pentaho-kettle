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


package org.pentaho.di.trans.steps.transexecutor;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.trans.StepWithMappingMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepHelper;

import java.util.Map;

public class TransExecutorHelper extends BaseStepHelper {

  private static final String TRANS_PRESENT = "transPresent";
  private static final String PARAMETERS = "parameters";
  private static final String IS_TRANS_VALID = "isTransValid";

  private final TransExecutorMeta transExecutorMeta;

  public TransExecutorHelper( TransExecutorMeta transExecutorMeta ) {
    this.transExecutorMeta = transExecutorMeta;
  }

  /**
   * Handles step-specific actions for TransExecutor.
   */
  @Override
  protected JSONObject handleStepAction( String method, TransMeta transMeta,
                                         Map<String, String> queryParams ) {
    transExecutorMeta.setRepository( transMeta.getRepository() );
    JSONObject response = new JSONObject();
    switch ( method ) {
      case PARAMETERS:
        response = getParametersFromTrans( transMeta, transExecutorMeta );
        break;
      case IS_TRANS_VALID:
        response = isTransValid( transMeta, transExecutorMeta );
        break;
      case REFERENCE_PATH:
        response = getReferencePath( transMeta, transExecutorMeta );
        break;
      default:
        response.put( ACTION_STATUS, FAILURE_METHOD_NOT_FOUND_RESPONSE );
        break;
    }
    return response;
  }

  /**
   * Retrieves the parameters of the transformation and returns them as a JSON object.
   *
   * @param transMeta         The metadata of the current transformation.
   * @param transExecutorMeta The metadata of the transformation executor step.
   * @return A JSON object containing:
   * - "parameters": A JSON array where each element is a JSON object representing a parameter with:
   * - "variable": The name of the parameter.
   * - "field": An empty string (reserved for future use).
   * - "input": The default value or description of the parameter.
   * @throws KettleException If an error occurs while loading the transformation metadata.
   */
  private JSONObject getParametersFromTrans( TransMeta transMeta, TransExecutorMeta transExecutorMeta ) {
    JSONObject response = new JSONObject();
    JSONArray parameterArray = new JSONArray();
    try {
      TransMeta inputTransMeta = loadExecutorTransMeta( transMeta, transExecutorMeta );
      if ( inputTransMeta != null ) {
        String[] parameters = inputTransMeta.listParameters();
        for ( int i = 0; i < parameters.length; i++ ) {
          JSONObject parameter = new JSONObject();
          String name = parameters[ i ];
          String desc = inputTransMeta.getParameterDescription( name );
          String str = inputTransMeta.getParameterDefault( name );
          parameter.put( "variable", Const.NVL( name, "" ) );
          parameter.put( "field", "" );
          parameter.put( "input", Const.NVL( str, Const.NVL( desc, "" ) ) );
          parameterArray.add( parameter );
        }
      }
      response.put( PARAMETERS, parameterArray );
    } catch ( Exception ex ) {
      log.logError( ex.getMessage() );
      response.put( ACTION_STATUS, FAILURE_RESPONSE );
    }
    return response;
  }

  /**
   * Validates the presence of a transformation by attempting to load its metadata.
   *
   * @return A JSON object containing:
   * - "transPresent": A string ("true" or "false") indicating whether the transformation metadata was successfully
   * loaded.
   * - "errorMessage": An error message if the transformation metadata could not be loaded.
   * @throws KettleException If an error occurs while loading the transformation metadata.
   */
  private JSONObject isTransValid( TransMeta transMeta, TransExecutorMeta transExecutorMeta ) {
    JSONObject response = new JSONObject();
    try {
      loadExecutorTransMeta( transMeta, transExecutorMeta );
      response.put( TRANS_PRESENT, true );
    } catch ( Exception e ) {
      response.put( TRANS_PRESENT, false );
      response.put( "errorMessage", ExceptionUtils.getRootCauseMessage( e ) );
    }
    return response;
  }

  /**
   * Fetches the reference path of the transformation used in the transformation executor step.
   *
   * @return  A JSON object containing:
   * - "referencePath": The full path to the referenced transformation, with environment variables substituted.
   * - "isValidReference": A boolean indicating whether the referenced transformation could be successfully loaded.
   *
   */
  private JSONObject getReferencePath( TransMeta transMeta, TransExecutorMeta transExecutorMeta ) {
    JSONObject response = new JSONObject();
    try {
      response.put( REFERENCE_PATH, getReferencePath( transMeta, transExecutorMeta.getDirectoryPath(), transExecutorMeta.getTransName() ) );
      loadExecutorTransMeta( transMeta, transExecutorMeta );
      response.put( IS_VALID_REFERENCE, true );
      response.put( IS_TRANS_REFERENCE, true );
    } catch ( Exception exception ) {
      response.put( IS_VALID_REFERENCE, false );
      response.put( IS_TRANS_REFERENCE, true );
    }
    return response;
  }

  /**
   * Loads the metadata of the transformation associated with the TransExecutor step.
   *
   * @param transMeta         The metadata of the current transformation.
   * @param transExecutorMeta The metadata of the transformation executor step.
   * @return The metadata of the loaded transformation.
   * @throws KettleException If an error occurs while loading the transformation metadata.
   */
  TransMeta loadExecutorTransMeta( TransMeta transMeta, TransExecutorMeta transExecutorMeta ) throws KettleException {
    return StepWithMappingMeta.loadMappingMeta( transMeta.getBowl(), transExecutorMeta,
      transExecutorMeta.getRepository(),
      transExecutorMeta.getMetaStore(), transMeta,
      transExecutorMeta.getParameters().isInheritingAllVariables() );
  }
}
