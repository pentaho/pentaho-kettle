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
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
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
    Repository repository = transMeta.getRepository();
    RepositoryDirectoryInterface repositoryDirectory;
    try {
      String fileName = mappingMeta.getFileName();
      if ( fileName.endsWith( ".ktr" ) ) {
        fileName = fileName.replace( ".ktr", "" );
      }

      String transPath = transMeta.environmentSubstitute( fileName );
      String realTransname = transPath;
      String realDirectory = "";
      int index = transPath.lastIndexOf( "/" );
      if ( index != -1 ) {
        realTransname = transPath.substring( index + 1 );
        realDirectory = transPath.substring( 0, index );
      }

      if ( StringUtils.isBlank( realDirectory ) || StringUtils.isBlank( realTransname ) ) {
        response.put( ACTION_STATUS, FAILURE_RESPONSE );
        response.put( ERROR_MESSAGE,
            BaseMessages.getString(
                PKG, "MappingHelper.Exception.NoValidMappingDetailsFound" ) );
        return response;
      }

      repositoryDirectory = repository.findDirectory( realDirectory );
      if ( repositoryDirectory == null ) {
        response.put( ACTION_STATUS, FAILURE_RESPONSE );
        response.put( ERROR_MESSAGE,
            BaseMessages.getString(
                PKG, "MappingHelper.Exception.UnableToFindRepositoryDirectory" ) );
        return response;
      }

      TransMeta mappingTransMeta =
            repository.loadTransformation( transMeta.environmentSubstitute( realTransname ), repositoryDirectory, null, true, null );
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
}
