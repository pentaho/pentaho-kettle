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


package org.pentaho.di.trans.steps.denormaliser;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepHelper;

import java.util.Map;

public class DenormaliserStepHelper extends BaseStepHelper {

  private static final String METHOD_GET_LOOKUP_FIELDS = "getLookupFields";
  private static final String METHOD_GET_AGG_TYPES = "getAggregationTypes";
  private static final String RESPONSE_KEY_AGG_TYPES = "aggregationTypes";
  private static final String RESPONSE_KEY_FIELDS = "denormaliserFields";
  private static final String ERROR_MESSAGE = "message";

  private final DenormaliserMeta deNormalizerMeta;

  public DenormaliserStepHelper( DenormaliserMeta deNormalizerMeta ) {
    this.deNormalizerMeta = deNormalizerMeta;
  }

  /**
   * Returns available aggregation types as a JSON object.
   */
  @SuppressWarnings( "unchecked" )
  private JSONObject getAggregationTypes() {
    JSONArray aggregationTypes = new JSONArray();

    for ( int i = 0; i < DenormaliserTargetField.typeAggrDesc.length; i++ ) {
      JSONObject aggregationType = new JSONObject();
      aggregationType.put( "id", DenormaliserTargetField.typeAggrDesc[ i ] );
      aggregationType.put( "name", DenormaliserTargetField.typeAggrLongDesc[ i ] );
      aggregationTypes.add( aggregationType );
    }

    JSONObject response = new JSONObject();
    response.put( RESPONSE_KEY_AGG_TYPES, aggregationTypes );
    return response;
  }

  /**
   * Returns available lookup fields as a JSON object.
   */
  @SuppressWarnings( "unchecked" )
  private JSONObject getLookupFields( TransMeta transMeta, DenormaliserMeta meta ) {
    JSONObject response = new JSONObject();
    JSONArray fields = new JSONArray();

    try {
      if ( meta.getParentStepMeta() == null ) {
        response.put( ERROR_MESSAGE, "No info stream step found." );
        response.put( ACTION_STATUS, FAILURE_RESPONSE );
        return response;
      }
      String stepName = meta.getParentStepMeta().getName();
      if ( stepName == null ) {
        response.put( ERROR_MESSAGE, "No info stream step found." );
        response.put( ACTION_STATUS, FAILURE_RESPONSE );
        return response;
      }

      RowMetaInterface previousFields = transMeta.getPrevStepFields( stepName );
      if ( previousFields == null || previousFields.isEmpty() ) {
        response.put( ERROR_MESSAGE, "No previous fields found for step: " + stepName );
        response.put( ACTION_STATUS, FAILURE_RESPONSE );
        return response;
      }

      String[] groupFields = meta.getGroupField();
      String keyField = meta.getKeyField();

      for ( ValueMetaInterface v : previousFields.getValueMetaList() ) {
        String fieldName = v.getName();

        // Exclude group fields and the key field
        if ( Const.indexOfString( fieldName, groupFields ) < 0 && !fieldName.equalsIgnoreCase( keyField ) ) {

          JSONObject fieldJson = new JSONObject();
          fieldJson.put( "fieldName", fieldName );
          fieldJson.put( "keyValue", "" );
          fieldJson.put( "targetName", fieldName );
          fieldJson.put( "targetType", v.getTypeDesc() );
          fieldJson.put( "targetLength", v.getLength() );
          fieldJson.put( "aggregationType",
            DenormaliserTargetField.getAggregationTypeDesc( DenormaliserTargetField.TYPE_AGGR_NONE ) );
          fields.add( fieldJson );
        }
      }

      response.put( RESPONSE_KEY_FIELDS, fields );
    } catch ( Exception e ) {
      response.put( ERROR_MESSAGE, "Failed to retrieve lookup fields: " + e.getMessage() );
      response.put( ACTION_STATUS, FAILURE_RESPONSE );
    }
    return response;
  }

  /**
   * Handles step actions based on the provided method name.
   * <p>
   * Depending on the value of {@code method}, this function delegates to the appropriate handler:
   * <ul>
   *   <li>If {@code method} equals {@code LOOKUP_FIELDS_METHOD}, it calls {@code lookupFields}.</li>
   *   <li>If {@code method} equals {@code GET_AGG_TYPES}, it calls {@code getFields}.</li>
   *   <li>For any other value, it returns a JSON response indicating the method was not found.</li>
   * </ul>
   *
   * @param method      the action method to perform
   * @param transMeta   the transformation metadata context
   * @param queryParams additional query parameters for the action
   * @return a {@link JSONObject} containing the result of the action or an error response if the method is not found
   * @see BaseStepHelper#handleStepAction(String, TransMeta, Map)
   */
  @Override
  @SuppressWarnings( "unchecked" )
  protected JSONObject handleStepAction( String method, TransMeta transMeta, Map<String, String> queryParams ) {
    return switch ( method ) {
      case METHOD_GET_AGG_TYPES -> getAggregationTypes();
      case METHOD_GET_LOOKUP_FIELDS -> getLookupFields( transMeta, deNormalizerMeta );
      default -> {
        JSONObject response = new JSONObject();
        response.put( ACTION_STATUS, FAILURE_METHOD_NOT_FOUND_RESPONSE );
        yield response;
      }
    };
  }
}
