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

package org.pentaho.di.trans.steps.simplemapping;

import org.apache.commons.lang.StringUtils;
import org.json.simple.JSONObject;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.util.MappingUtil;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.StepWithMappingMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepHelper;
import org.pentaho.di.trans.step.StepMeta;

import java.util.Arrays;
import java.util.Map;

public class SimpleMappingHelper extends BaseStepHelper {
  protected static final String SIMPLE_MAPPING_REFERENCE_PATH = "referencePath";
  protected static final String GET_MAPPING_STEPS = "getMappingSteps";
  protected static final String ERROR_MESSAGE = "errorMessage";
  protected static final String SOURCE_FIELDS= "sourceFields";
  protected static final String TARGET_FIELDS = "targetFields";
  protected static final String GET_SIMPLE_MAPPING_FIELDS = "getSimpleMappingFields";
  private static final Class<?> PKG = SimpleMappingHelper.class;
  protected static final String MAPPING_STEPS = "mappingSteps";
  private final SimpleMappingMeta simpleMappingMeta;

  public SimpleMappingHelper( SimpleMappingMeta simpleMappingMeta) {
    this.simpleMappingMeta = simpleMappingMeta;
  }

  /**
   * Handles step-specific actions for Simple Mapping step.
   */
  @Override
  protected JSONObject handleStepAction( String method, TransMeta transMeta,
                                         Map<String, String> queryParams ) {
    JSONObject response = new JSONObject();
    if ( StringUtils.isBlank( method  ) ) {
      response.put( ACTION_STATUS, FAILURE_METHOD_NOT_FOUND_RESPONSE );
      return response;
    }

    try {
      switch ( method )  {
        case SIMPLE_MAPPING_REFERENCE_PATH:
          response = getReferencePath( transMeta );
          break;
        case GET_MAPPING_STEPS:
          response = getMappingSteps( transMeta );
          break;
        case GET_SIMPLE_MAPPING_FIELDS:
          response = getSimpleMappingFields( transMeta, queryParams );
          break;
        default :
          response.put( ACTION_STATUS, FAILURE_METHOD_NOT_FOUND_RESPONSE );
      }
    } catch ( Exception ex ) {
      response.put( ACTION_STATUS, FAILURE_RESPONSE );
    }
    return response;
  }

  /**
   * Fetches the reference path of the sub transformation used in the Simple Mapping Step.
   *
   * @return  A JSON object containing:
   * - "referencePath": The full path to the referenced transformation, with environment variables substituted.
   * - "isValidReference": A boolean indicating whether the referenced transformation is valid.
   *
   */
  private JSONObject getReferencePath( TransMeta transMeta ) {
    JSONObject response = new JSONObject();
    response.put( SIMPLE_MAPPING_REFERENCE_PATH, getReferencePath( transMeta, simpleMappingMeta.getDirectoryPath(), simpleMappingMeta.getTransName(),
        simpleMappingMeta.getSpecificationMethod(), simpleMappingMeta.getFileName() ) );
    return validateAndPutReferenceStatus( transMeta, response, simpleMappingMeta );
  }

  /**
   * Fetches the output steps of the sub-transformation used in the Simple Mapping Step.
   * @param transMeta The parent transformation metadata.
   *
   * @return A JSON object containing:
   * - "mappingSteps": A list of step names that are output steps in the referenced transformation.
   * - "actionStatus": Indicates success or failure of the operation.
   * - "errorMessage": Contains error details if the operation fails.
   */
  private JSONObject getMappingSteps( TransMeta transMeta ) {
    JSONObject response = new JSONObject();
    try {
      boolean isValidRepositoryPath = MappingUtil.validRepositoryPath( transMeta, simpleMappingMeta.getFileName() );
      if ( !isValidRepositoryPath ) {
        response.put( ACTION_STATUS, FAILURE_RESPONSE );
        response.put( ERROR_MESSAGE,
            BaseMessages.getString(
                PKG, "SimpleMappingHelper.Exception.NoValidMappingDetailsFound" ) );
        return response;
      }

      TransMeta mappingTransMeta = loadSimpleMappingMeta( transMeta, simpleMappingMeta );
      mappingTransMeta.clearChanged();
      StepMeta mappingOutputStepMeta = mappingTransMeta.findMappingOutputStep( null );
      RowMetaInterface rowMetaInterface = mappingTransMeta.getStepFields( mappingOutputStepMeta );
      String[] mappingSteps = rowMetaInterface.getFieldNames();
      response.put( ACTION_STATUS, SUCCESS_RESPONSE );
      response.put( MAPPING_STEPS, Arrays.stream( mappingSteps ).toList() );
    } catch ( Exception ex ) {
      log.logError( ex.getMessage() );
      response.put( ACTION_STATUS, FAILURE_RESPONSE );
      response.put( ERROR_MESSAGE, BaseMessages.getString( PKG, ex.getMessage() ) );
      return response;
    }

    return response;
  }

  private JSONObject getSimpleMappingFields( TransMeta transMeta, Map<String, String> queryParams ) {
    JSONObject response = new JSONObject();
    try {
      boolean isValidRepositoryPath = MappingUtil.validRepositoryPath( transMeta, simpleMappingMeta.getFileName() );
      if ( !isValidRepositoryPath ) {
        response.put( ERROR_MESSAGE,
            BaseMessages.getString(
                PKG, "SimpleMappingHelper.Exception.NoValidMappingDetailsFound" ) );
        response.put( ACTION_STATUS, FAILURE_RESPONSE );
        return response;
      }

      TransMeta mappingTransMeta = loadSimpleMappingMeta( transMeta, simpleMappingMeta );
      mappingTransMeta.clearChanged();
      boolean isInputMapping = queryParams.getOrDefault( "isMappingInput", "false" ).equals( "true" );
      StepMeta stepMeta = transMeta.findStep( simpleMappingMeta.getParentStepMeta().getName() );
      RowMetaInterface sourceRowMeta = getFieldsFromStep( transMeta, mappingTransMeta,  stepMeta, true, isInputMapping );
      String[] sourceFields = sourceRowMeta.getFieldNames();

      RowMetaInterface targetRowMeta = getFieldsFromStep( transMeta, mappingTransMeta,  stepMeta, false, isInputMapping );
      String[] targetFields = targetRowMeta.getFieldNames();
      response.put( ACTION_STATUS, SUCCESS_RESPONSE );
      response.put( SOURCE_FIELDS, Arrays.stream( sourceFields ).toList() );
      response.put( TARGET_FIELDS, Arrays.stream( targetFields ).toList() );
    } catch ( Exception ex ) {
      log.logError( ex.getMessage() );
      response.put( ACTION_STATUS, FAILURE_RESPONSE );
      response.put( ERROR_MESSAGE, BaseMessages.getString( PKG, ex.getMessage() ) );
      return response;
    }

    return response;
  }

  TransMeta loadSimpleMappingMeta( TransMeta transMeta, SimpleMappingMeta simpleMappingMeta ) throws KettleException {
    return StepWithMappingMeta.loadMappingMeta( transMeta.getBowl(), simpleMappingMeta,
        transMeta.getRepository(),
        transMeta.getMetaStore(), transMeta,
        true );
  }

  public RowMetaInterface getFieldsFromStep( TransMeta transMeta, TransMeta mappingTransMeta, StepMeta stepMeta, boolean parent, boolean input ) throws KettleException {
    if ( input ) {
      if ( parent ) {
        return transMeta.getPrevStepFields( stepMeta );
      } else {
        if ( mappingTransMeta == null ) {
          throw new KettleException( BaseMessages.getString(
              PKG, "SimpleMappingDialog.Exception.NoMappingSpecified" ) );
        }
        StepMeta mappingInputStepMeta = mappingTransMeta.findMappingInputStep( null );
        return mappingTransMeta.getStepFields( mappingInputStepMeta );
      }
    } else {
      StepMeta mappingOutputStepMeta = mappingTransMeta.findMappingOutputStep( null );
      return mappingTransMeta.getStepFields( mappingOutputStepMeta );
    }
  }
}
