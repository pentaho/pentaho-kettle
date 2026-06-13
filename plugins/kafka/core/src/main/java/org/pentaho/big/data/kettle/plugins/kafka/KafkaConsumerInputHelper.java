/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2025 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/

package org.pentaho.big.data.kettle.plugins.kafka;

import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONObject;
import org.pentaho.di.trans.StepWithMappingMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepHelper;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.metastore.api.exceptions.MetaStoreException;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class KafkaConsumerInputHelper extends BaseStepHelper {

  protected static final String GET_SUB_STEP_LIST = "getSubStepList";
  public static final String SUB_STEP_LIST = "subStepList";
  protected static final String GET_CLUSTER_LIST = "getClusterList";
  public static final String CLUSTER_LIST = "clusterList";

  private final KafkaConsumerInputMeta kafkaConsumerInputMeta;

  public KafkaConsumerInputHelper( KafkaConsumerInputMeta kafkaConsumerInputMeta ) {
    // Defensive null check - if meta is null, this helper should gracefully handle it
    this.kafkaConsumerInputMeta = kafkaConsumerInputMeta;
  }

  @Override
  protected JSONObject handleStepAction( String method, TransMeta transMeta, Map<String, String> queryParams ) {
    JSONObject response = new JSONObject();
    if ( StringUtils.isBlank( method ) ) {
      response.put( ACTION_STATUS, FAILURE_METHOD_NOT_FOUND_RESPONSE );
      return response;
    }

    // Return early if meta is not available
    if ( kafkaConsumerInputMeta == null ) {
      response.put( ACTION_STATUS, FAILURE_RESPONSE );
      return response;
    }

    try {
      if ( GET_SUB_STEP_LIST.equals( method ) ) {
        response = getSubStepList( transMeta );
      } else if ( GET_CLUSTER_LIST.equals( method ) ) {
        response = getClusterList( transMeta );
      } else {
        response.put( ACTION_STATUS, FAILURE_METHOD_NOT_FOUND_RESPONSE );
      }
    } catch ( Exception ex ) {
      log.logError( ex.getMessage(), ex );
      response.put( ACTION_STATUS, FAILURE_RESPONSE );
    }
    return response;
  }

  private JSONObject getSubStepList( TransMeta transMeta ) {
    JSONObject response = new JSONObject();
    try {
      // Validate required inputs
      if ( transMeta == null || kafkaConsumerInputMeta == null ) {
        response.put( ACTION_STATUS, FAILURE_RESPONSE );
        return response;
      }

      // Check if transformation path is configured
      if ( StringUtils.isBlank( kafkaConsumerInputMeta.getTransformationPath() ) ) {
        response.put( ACTION_STATUS, FAILURE_RESPONSE );
        return response;
      }

      // Check if Bowl is available
      if ( transMeta.getBowl() == null ) {
        response.put( ACTION_STATUS, FAILURE_RESPONSE );
        return response;
      }

      // Synchronize fileName from transformationPath to ensure loadMappingMeta can locate the file
      // loadMappingMeta uses specification method + fileName/repo fields, not transformationPath
      if ( StringUtils.isNotBlank( kafkaConsumerInputMeta.getTransformationPath() ) ) {
        // Extract filename from path and set it in the meta
        String fileName = kafkaConsumerInputMeta.getTransformationPath();
        kafkaConsumerInputMeta.replaceFileName( fileName );
      }

      TransMeta mappingTransMeta = StepWithMappingMeta.loadMappingMeta(
          transMeta.getBowl(), kafkaConsumerInputMeta,
          transMeta.getRepository(),
          transMeta.getMetaStore(), transMeta,
          true );

      if ( mappingTransMeta != null ) {
        List<String> mappingSteps = mappingTransMeta.getSteps()
            .stream()
            .map( StepMeta::getName )
            .sorted()
            .collect( Collectors.toList() );

        response.put( ACTION_STATUS, SUCCESS_RESPONSE );
        response.put( SUB_STEP_LIST, mappingSteps );
      } else {
        response.put( ACTION_STATUS, FAILURE_RESPONSE );
      }
    } catch ( Exception ex ) {
      log.logError( ex.getMessage(), ex );
      response.put( ACTION_STATUS, FAILURE_RESPONSE );
    }
    return response;
  }

  private JSONObject getClusterList( TransMeta transMeta ) {
    JSONObject response = new JSONObject();
    try {
      // Validate required inputs
      if ( transMeta == null || kafkaConsumerInputMeta == null ) {
        response.put( ACTION_STATUS, FAILURE_RESPONSE );
        return response;
      }

      // Check if NamedClusterService is available
      if ( kafkaConsumerInputMeta.getNamedClusterService() == null ) {
        response.put( ACTION_STATUS, FAILURE_RESPONSE );
        return response;
      }

      // Query MetaStore for available cluster names
      List<String> clusterNames = kafkaConsumerInputMeta.getNamedClusterService()
          .listNames( transMeta.getMetaStore() );

      response.put( ACTION_STATUS, SUCCESS_RESPONSE );
      response.put( CLUSTER_LIST, clusterNames );
    } catch ( MetaStoreException ex ) {
      log.logError( "Failed to get defined named clusters", ex );
      response.put( ACTION_STATUS, FAILURE_RESPONSE );
    } catch ( Exception ex ) {
      log.logError( ex.getMessage(), ex );
      response.put( ACTION_STATUS, FAILURE_RESPONSE );
    }
    return response;
  }

}