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

package org.pentaho.di.trans.steps.mapping;

import org.apache.commons.lang.StringUtils;
import org.json.simple.JSONObject;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.util.MappingUtil;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.StepWithMappingMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepHelper;
import org.pentaho.di.trans.step.StepMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class MappingHelper extends BaseStepHelper {
  private static final Class<?> PKG = MappingHelper.class;
  protected static final String MAPPING_REFERENCE_PATH = "referencePath";
  protected static final String MAPPING_STEPS = "mappingSteps";
  protected static final String SOURCE_FIELDS = "sourceFields";
  protected static final String TARGET_FIELDS = "targetFields";
  protected static final String GET_MAPPING_FIELDS = "getMappingFields";
  protected static final String GET_MAPPING_STEPS = "getMappingSteps";
  protected static final String ERROR_MESSAGE = "errorMessage";
  private final MappingMeta mappingMeta;

  public MappingHelper( MappingMeta mappingMeta ) {
    this.mappingMeta = mappingMeta;
  }

  /**
   * Handles step-specific actions for Mapping step.
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
        case MAPPING_REFERENCE_PATH:
          response = getReferencePath( transMeta );
          break;
        case GET_MAPPING_STEPS:
          response = getMappingSteps( transMeta, queryParams );
          break;
        case GET_MAPPING_FIELDS:
          response = getMappingFields( transMeta, queryParams );
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
   * Fetches the reference path of the sub transformation used in the Mapping Step.
   *
   * @return  A JSON object containing:
   * - "referencePath": The full path to the referenced transformation, with environment variables substituted.
   * - "isValidReference": A boolean indicating whether the referenced transformation is valid.
   *
   */
  private JSONObject getReferencePath( TransMeta transMeta ) {
    JSONObject response = new JSONObject();
    response.put( MAPPING_REFERENCE_PATH, getReferencePath( transMeta, mappingMeta.getDirectoryPath(), mappingMeta.getTransName(),
        mappingMeta.getSpecificationMethod(), mappingMeta.getFileName() ) );
    return validateAndPutReferenceStatus( transMeta, response, mappingMeta );
  }

  /**
   * Fetches the mapping input or output steps from the referenced transformation.
   * @param transMeta The parent transformation metadata.
   * @param queryParams The query parameters containing "isMappingInput" to specify input or output steps.
   * @return A JSON object containing:
   * - "mappingSteps": An array of mapping input or output step names.
   * - "actionStatus": Indicates success or failure of the operation.
   * - "errorMessage": Contains error details if the operation fails.
   */
  private JSONObject getMappingSteps( TransMeta transMeta, Map<String, String> queryParams ) {
    JSONObject response = new JSONObject();
    try {
      boolean isValidRepositoryPath = MappingUtil.validRepositoryPath( transMeta, mappingMeta.getFileName() );
      if ( !isValidRepositoryPath ) {
        response.put( ACTION_STATUS, FAILURE_RESPONSE );
        response.put( ERROR_MESSAGE,
            BaseMessages.getString(
                PKG, "MappingHelper.Exception.NoValidMappingDetailsFound" ) );
        return response;
      }

      TransMeta mappingTransMeta = loadMappingMeta( transMeta, mappingMeta );
      mappingTransMeta.clearChanged();
      String[] mappingSteps = getMappingSteps( mappingTransMeta, queryParams.getOrDefault( "isMappingInput", "false" ).equals( "true" ) );
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

    /**
    * Fetches the fields from the mapping input or output step in the referenced transformation and the corresponding source or target steps in the parent transformation.
    * @param transMeta The parent transformation metadata.
    * @param queryParams The query parameters containing "isMappingInput", "inputStepName", and "outputStepName".
    * @return A JSON object containing:
    * - "sourceFields": An array of field names from the source step (previous step of the Mapping step or specified input step).
    * - "targetFields": An array of field names from the target step (mapping input/output step in the referenced transformation).
    * - "actionStatus": Indicates success or failure of the operation.
    * - "errorMessage": Contains error details if the operation fails.
    */
  private JSONObject getMappingFields( TransMeta transMeta, Map<String, String> queryParams ) {
    JSONObject response = new JSONObject();
    try {
      boolean isValidRepositoryPath = MappingUtil.validRepositoryPath( transMeta, mappingMeta.getFileName() );
      if ( !isValidRepositoryPath ) {
        response.put( ERROR_MESSAGE, BaseMessages.getString( PKG, "MappingHelper.Exception.NoValidMappingDetailsFound" ) );
        response.put( ACTION_STATUS, FAILURE_RESPONSE );
        return response;
      }

      TransMeta mappingTransMeta = loadMappingMeta( transMeta, mappingMeta );
      mappingTransMeta.clearChanged();
      boolean isSourceMapping = queryParams.getOrDefault( "isMappingInput", "false" ).equals( "true" );
      String inputStepName = queryParams.getOrDefault( "inputStepName", "" );
      String outputStepName = queryParams.getOrDefault( "outputStepName", "" );
      RowMetaInterface sourceRowMeta = getFieldsFromStep( transMeta, mappingTransMeta, mappingMeta.getParentStepMeta().getName(), inputStepName, true, isSourceMapping );
      String[] sourceFields = sourceRowMeta.getFieldNames();
      response.put( SOURCE_FIELDS, Arrays.stream( sourceFields ).toList() );

      RowMetaInterface targetRowMeta = getFieldsFromStep( transMeta, mappingTransMeta, mappingMeta.getParentStepMeta().getName(), outputStepName, false, isSourceMapping );
      String[] targetFields = targetRowMeta.getFieldNames();
      response.put( TARGET_FIELDS, Arrays.stream( targetFields ).toList() );

      response.put( ACTION_STATUS, SUCCESS_RESPONSE );
    } catch ( Exception ex ) {
      log.logError( ex.getMessage() );
      response.put( ERROR_MESSAGE, BaseMessages.getString( PKG, ex.getMessage() ) );
      response.put( ACTION_STATUS, FAILURE_RESPONSE );
      return response;
    }

    return response;
  }

  public static String[] getMappingSteps( TransMeta mappingTransMeta, boolean mappingInput ) {
    List<StepMeta> steps = new ArrayList<>();
    for ( StepMeta stepMeta : mappingTransMeta.getSteps() ) {
      if ( !mappingInput && stepMeta.getStepID().equals( "MappingOutput" ) ) {
        steps.add( stepMeta );
      }

      if ( mappingInput && stepMeta.getStepID().equals( "MappingInput" ) ) {
        steps.add( stepMeta );
      }
    }
    String[] stepNames = new String[ steps.size() ];
    for ( int i = 0; i < stepNames.length; i++ ) {
      stepNames[ i ] = steps.get( i ).getName();
    }

    return stepNames;
  }

  TransMeta loadMappingMeta( TransMeta transMeta, MappingMeta mappingMeta ) throws KettleException {
    return StepWithMappingMeta.loadMappingMeta( transMeta.getBowl(), mappingMeta,
        transMeta.getRepository(),
        transMeta.getMetaStore(), transMeta,
        true );
  }

  public RowMetaInterface getFieldsFromStep( TransMeta transMeta, TransMeta mappingTransMeta, String stepName,
                                             String inputStepName, boolean getTransformationStep,
                                             boolean mappingInput ) throws KettleException {
    if ( mappingInput == getTransformationStep ) {
      return getFieldsFromParentTrans( inputStepName, transMeta, stepName );
    } else {
      return getFieldsFromMappingTrans( inputStepName, mappingTransMeta, mappingInput );
    }
  }

  private RowMetaInterface getFieldsFromParentTrans( String inputStepName, TransMeta transMeta, String stepName ) throws KettleException {
    if ( Utils.isEmpty( inputStepName ) ) {
      return transMeta.getPrevStepFields( stepName );
    } else {
      StepMeta stepMeta = transMeta.findStep( inputStepName );
      if ( stepMeta == null ) {
        throw new KettleException( BaseMessages.getString(
            PKG, "MappingDialog.Exception.SpecifiedStepWasNotFound", inputStepName ) );
      }
      return transMeta.getStepFields( stepMeta );
    }
  }

  private RowMetaInterface getFieldsFromMappingTrans( String inputStepName, TransMeta mappingTransMeta, boolean mappingInput ) throws KettleException {
    if ( mappingTransMeta == null ) {
      throw new KettleException( BaseMessages.getString( PKG, "MappingDialog.Exception.NoMappingSpecified" ) );
    }

    if ( Utils.isEmpty( inputStepName ) ) {
      String[] stepNames = MappingHelper.getMappingSteps( mappingTransMeta, mappingInput );
      if ( stepNames.length > 1 ) {
        throw new KettleException( BaseMessages.getString(
            PKG, "MappingDialog.Exception.OnlyOneMappingInputStepAllowed", "" + stepNames.length ) );
      }
      if ( stepNames.length == 0 ) {
        throw new KettleException( BaseMessages.getString(
            PKG, "MappingDialog.Exception.OneMappingInputStepRequired", "" + stepNames.length ) );
      }
      return mappingTransMeta.getStepFields( stepNames[ 0 ] );
    } else {
      StepMeta stepMeta = mappingTransMeta.findStep( inputStepName );
      if ( stepMeta == null ) {
        throw new KettleException( BaseMessages.getString(
            PKG, "MappingDialog.Exception.SpecifiedStepWasNotFound", inputStepName ) );
      }
      return mappingTransMeta.getStepFields( stepMeta );
    }
  }
}
