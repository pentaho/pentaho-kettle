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
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepHelper;
import org.pentaho.di.trans.step.StepMeta;

import java.util.Arrays;
import java.util.Map;

public class SimpleMappingHelper extends BaseStepHelper {
  protected static final String SIMPLE_MAPPING_REFERENCE_PATH = "referencePath";
  protected static final String GET_MAPPING_STEPS = "getMappingSteps";
  protected static final String ERROR_MESSAGE = "errorMessage";
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
    Repository repository = transMeta.getRepository();
    RepositoryDirectoryInterface repositoryDirectory;
    try {
      String fileName = simpleMappingMeta.getFileName();
      if ( fileName.endsWith( ".ktr" ) ) {
        fileName = fileName.replace( ".ktr", "" );
      }

      String transPath = transMeta.environmentSubstitute( fileName );
      String realDirectory = "";
      String realTransname = transPath;
      int index = transPath.lastIndexOf( "/" );
      if ( index != -1 ) {
        realDirectory = transPath.substring( 0, index );
        realTransname = transPath.substring( index + 1 );
      }

      if ( StringUtils.isBlank( realDirectory ) || StringUtils.isBlank( realTransname ) ) {
        response.put( ACTION_STATUS, FAILURE_RESPONSE );
        response.put( ERROR_MESSAGE,
            BaseMessages.getString(
                PKG, "SimpleMappingHelper.Exception.NoValidMappingDetailsFound" ) );
        return response;
      }

      repositoryDirectory = repository.findDirectory( realDirectory );
      if ( repositoryDirectory == null ) {
        response.put( ERROR_MESSAGE,
            BaseMessages.getString(
                PKG, "SimpleMappingHelper.Exception.UnableToFindRepositoryDirectory" ) );
        response.put( ACTION_STATUS, FAILURE_RESPONSE );
        return response;
      }

      TransMeta mappingTransMeta =
          repository.loadTransformation( transMeta.environmentSubstitute( realTransname ), repositoryDirectory, null, true, null );
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
}
