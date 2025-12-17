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

import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONObject;
import org.pentaho.di.core.ObjectLocationSpecificationMethod;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.trans.StepWithMappingMeta;
import org.pentaho.di.trans.TransMeta;

import java.util.Map;

/**
 * Base implementation of {@link StepHelperInterface} providing shared infrastructure for executing
 * UI-triggered "step actions" and other common methods.
 * <p>
 * Step actions are step-specific, non-runtime operations initiated from a step dialog (e.g. fetching
 * external metadata, validating configuration, testing connections). They do <strong>not</strong>
 * participate in transformation execution.
 * </p>
 * <p>
 * This class centralizes:
 * <ul>
 *   <li>Acquisition of a {@link org.pentaho.di.core.logging.LogChannelInterface} for consistent logging</li>
 *   <li>Template handling in {@link #stepAction(String, TransMeta, java.util.Map)} including:
 *     <ul>
 *       <li>Delegation to {@link #handleStepAction(String, TransMeta, java.util.Map)}</li>
 *       <li>Automatic population of standard response status fields defined in the interface</li>
 *       <li>Error trapping and failure status assignment</li>
 *     </ul>
 *   </li>
 *   <li>Utility to evaluate failure state via {@link #isFailedResponse(org.json.simple.JSONObject)}</li>
 * </ul>
 * Subclasses implement only the step-specific logic in {@link #handleStepAction(String, TransMeta, java.util.Map)}.
 * </p>
 *
 * @see StepHelperInterface
 */
public abstract class BaseStepHelper implements StepHelperInterface {

  protected final LogChannelInterface log;
  public static final String IS_TRANS_REFERENCE = "isTransReference";
  public static final String IS_VALID_REFERENCE = "isValidReference";
  public static final String REFERENCE_PATH = "referencePath";
  public static final String SEPARATOR = "/";

  protected BaseStepHelper() {
    this.log = KettleLogStore.getLogChannelInterfaceFactory().create( this );
  }

  /**
   * Template method for executing a step action method. Handles response status and error logging.
   * Subclasses should implement the `handleStepAction` method to provide step-specific logic.
   *
   * @param method      The name of the method to execute.
   * @param transMeta   The transformation metadata associated with the step.
   * @param queryParams A map of query parameters for the action.
   * @return A JSON object containing the action status and any additional response data.
   */
  @Override
  @SuppressWarnings( "unchecked" )
  public JSONObject stepAction( String method, TransMeta transMeta, Map<String, String> queryParams ) {
    JSONObject response;
    try {
      response = handleStepAction( method, transMeta, queryParams );

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
   * Abstract method to be implemented by subclasses for handling step-specific actions.
   *
   * @param method      The name of the method to execute.
   * @param transMeta   The transformation metadata associated with the step.
   * @param queryParams A map of query parameters for the action.
   * @return A JSON object containing the action status and any additional response data.
   */
  protected abstract JSONObject handleStepAction( String method, TransMeta transMeta, Map<String, String> queryParams );

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

  /**
   * Constructs the reference path for a transformation based on the provided directory path and name.
   * @param transMeta The transformation metadata containing the step.
   * @param directoryPath The directory path where the transformation or job is located.
   * @param specificationMethod The specification method used (e.g. REPOSITORY_BY_NAME, FILENAME).
   * @param fileName The filename of the transformation or job (used if the specification method is FILENAME).
   * @param objectName The name of the transformation or job.
   * @return The constructed reference path, or an empty string if neither directoryPath nor objectName is provided.
   */
  protected String getReferencePath( TransMeta transMeta, String directoryPath, String objectName, ObjectLocationSpecificationMethod specificationMethod, String fileName ) {
    if ( specificationMethod == null ) {
      return StringUtils.EMPTY;
    }

    if ( specificationMethod.getCode().equalsIgnoreCase( ObjectLocationSpecificationMethod.FILENAME.getCode() ) ) {
      return transMeta.environmentSubstitute( fileName );
    }

    if ( StringUtils.isNotBlank( directoryPath ) && StringUtils.isNotBlank( objectName ) ) {
      return transMeta.environmentSubstitute( directoryPath ) + SEPARATOR +  transMeta.environmentSubstitute( objectName );
    } else if ( StringUtils.isNotBlank( objectName ) ) {
      return transMeta.environmentSubstitute( objectName );
    }

    return StringUtils.EMPTY;
  }

  /**
   * Validates the reference path of a StepWithMappingMeta instance and updates the response JSON object
   * @param transMeta The transformation metadata containing the step.
   * @param stepWithMappingMeta The StepWithMappingMeta instance containing the directory path and transformation name.
   * @return A JSON object containing:
   * - "referencePath": The full path to the referenced transformation, with environment variables substituted.
   * - "isValidReference": A boolean indicating whether the referenced transformation is valid.
   * - "isTransReference": A boolean indicating whether the reference is a transformation (always true for StepWithMapping).
   */
  protected JSONObject validateAndPutReferenceStatus( TransMeta transMeta, JSONObject response, StepWithMappingMeta stepWithMappingMeta ) {
    try {
      StepWithMappingMeta.loadMappingMeta( transMeta.getBowl(), stepWithMappingMeta, transMeta.getRepository(),
          transMeta.getMetaStore(), transMeta, false );
      response.put( IS_VALID_REFERENCE, true );
      response.put( IS_TRANS_REFERENCE, true );
    } catch ( Exception exception ) {
      response.put( IS_VALID_REFERENCE, false );
      response.put( IS_TRANS_REFERENCE, true );
    }

    return response;
  }
}

