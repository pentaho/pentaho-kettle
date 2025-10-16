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


package org.pentaho.di.trans.steps.streamlookup;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepHelper;
import org.pentaho.di.trans.step.StepIOMetaInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.errorhandling.StreamInterface;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;

import java.util.Map;
import java.util.Optional;

public class StreamLookupStepHelper extends BaseStepHelper {
  private static final String ERROR_MESSAGE = "errorMessage";
  private static final String LOOKUP_FIELDS_METHOD = "lookupFields";
  private static final String LOOKUP_FIELDS_RESPONSE_KEY = "lookupFields";

  private static final String FIELDS_RESPONSE_KEY = "fields";
  private static final String GET_FIELDS_METHOD = "getFields";
  private static final Class<?> PKG = StreamLookupMeta.class;

  private final StreamLookupMeta streamLookupMeta;

  public StreamLookupStepHelper( StreamLookupMeta streamLookupMeta ) {
    this.streamLookupMeta = streamLookupMeta;
  }

  /**
   * Logs an error message.
   *
   * @param message   the message
   * @param arguments the arguments
   */
  private void logError( String message, Object... arguments ) {
    if ( log != null ) {
      log.logError( message, arguments );
    }
  }

  /**
   * Helper to get the first info stream step name, or return null if not available.
   */
  private String getFirstInfoStreamStepName( StreamLookupMeta streamLookupMeta ) {
    return Optional.ofNullable( streamLookupMeta.getStepIOMeta() )
      .map( StepIOMetaInterface::getInfoStreams )
      .filter( list -> !list.isEmpty() )
      .map( list -> list.get( 0 ) )
      .map( StreamInterface::getStepname )
      .orElse( null );
  }

  /**
   * Retrieves the list of fields from the specified lookup step and returns them as a JSON object.
   * This method is invoked dynamically using reflection from StepInterface#doAction method.
   *
   * @param transMeta        the transformation metadata
   * @param streamLookupMeta the stream lookup metadata
   * @return A JSON object containing the list of lookup fields under the key "lookupFields" or an error message if
   * retrieval fails.
   */
  @SuppressWarnings( "unchecked" )
  private JSONObject lookupFields( TransMeta transMeta, StreamLookupMeta streamLookupMeta ) {
    JSONObject response = new JSONObject();
    String stepName = getFirstInfoStreamStepName( streamLookupMeta );

    if ( Utils.isEmpty( stepName ) ) {
      response.put( ERROR_MESSAGE, BaseMessages.getString( PKG, "StreamLookup.StepNameRequired.ErrorMessage" ) );
      response.put( StepInterface.ACTION_STATUS, StepInterface.FAILURE_RESPONSE );
      return response;
    }

    StepMeta lookupStepMeta = transMeta.findStep( stepName );
    JSONArray lookupFields = new JSONArray();

    if ( lookupStepMeta != null ) {
      try {
        RowMetaInterface row = transMeta.getStepFields( lookupStepMeta );
        for ( ValueMetaInterface valueMeta : row.getValueMetaList() ) {
          JSONObject lookupField = new JSONObject();
          lookupField.put( "name", valueMeta.getName() );
          lookupField.put( "id", valueMeta.getName() );
          lookupField.put( "type", valueMeta.getTypeDesc() );
          lookupFields.add( lookupField );
        }
      } catch ( KettleException e ) {
        logError( "It was not possible to retrieve the list of fields for step [{}]!", stepName );
        response.put( StepInterface.ACTION_STATUS, StepInterface.FAILURE_RESPONSE );
        return response;
      }
    }

    response.put( LOOKUP_FIELDS_RESPONSE_KEY, lookupFields );
    return response;
  }

  /**
   * Retrieves the fields from the previous step or the specified lookup step and returns them as a JSON object.
   * This method is invoked dynamically using reflection from StepInterface#doAction method.
   *
   * @param transMeta        the transformation metadata
   * @param streamLookupMeta the stream lookup metadata
   * @return A JSON object containing the list of fields under the key "fields" or an error message if retrieval fails.
   */
  @SuppressWarnings( "unchecked" )
  private JSONObject getFields( TransMeta transMeta, StreamLookupMeta streamLookupMeta ) {
    JSONObject response = new JSONObject();
    JSONArray fields = new JSONArray();

    try {
      StepMeta parentStepMeta = streamLookupMeta.getParentStepMeta();
      RowMetaInterface previousFieldsRowMeta = parentStepMeta != null
        ? transMeta.getPrevStepFields( parentStepMeta.getName() )
        : null;

      if ( previousFieldsRowMeta != null && !previousFieldsRowMeta.isEmpty() ) {
        for ( ValueMetaInterface valueMeta : previousFieldsRowMeta.getValueMetaList() ) {
          JSONObject field = new JSONObject();
          field.put( "name", valueMeta.getName() );
          fields.add( field );
        }
      } else {
        String stepName = getFirstInfoStreamStepName( streamLookupMeta );

        if ( Utils.isEmpty( stepName ) ) {
          response.put( ERROR_MESSAGE, BaseMessages.getString( PKG, "StreamLookup.StepNameRequired.ErrorMessage" ) );
          response.put( StepInterface.ACTION_STATUS, StepInterface.FAILURE_RESPONSE );
          return response;
        }

        RowMetaInterface stepFields = transMeta.getStepFields( stepName );
        if ( stepFields == null || stepFields.isEmpty() ) {
          response.put( ERROR_MESSAGE, BaseMessages.getString( PKG, "StreamLookup.CouldNotFindFields.ErrorMessage" ) );
          response.put( StepInterface.ACTION_STATUS, StepInterface.FAILURE_RESPONSE );
          return response;
        }

        for ( ValueMetaInterface valueMeta : stepFields.getValueMetaList() ) {
          JSONObject field = new JSONObject();
          field.put( "name", valueMeta.getName() );
          fields.add( field );
        }
      }
    } catch ( KettleException ke ) {
      logError( "Error while retrieving the fields from the step: {}", ke.getMessage() );
      response.put( ERROR_MESSAGE, ke.getMessage() );
      response.put( StepInterface.ACTION_STATUS, StepInterface.FAILURE_RESPONSE );
      return response;
    }

    response.put( FIELDS_RESPONSE_KEY, fields );
    return response;
  }

  /**
   * Handles step actions based on the provided method name.
   * <p>
   * Depending on the value of {@code method}, this function delegates to the appropriate handler:
   * <ul>
   *   <li>If {@code method} equals {@code LOOKUP_FIELDS_METHOD}, it calls {@code lookupFields}.</li>
   *   <li>If {@code method} equals {@code GET_FIELDS_METHOD}, it calls {@code getFields}.</li>
   *   <li>For any other value, it returns a JSON response indicating the method was not found.</li>
   * </ul>
   *
   * @param method      the action method to perform (e.g., lookup fields, get fields)
   * @param transMeta   the transformation metadata context
   * @param queryParams additional query parameters for the action
   * @return a {@link JSONObject} containing the result of the action or an error response if the method is not found
   * @see BaseStepHelper#handleStepAction(String, TransMeta, Map)
   */
  @Override
  @SuppressWarnings( "unchecked" )
  protected JSONObject handleStepAction( String method, TransMeta transMeta, Map<String, String> queryParams ) {
    return switch ( method ) {
      case LOOKUP_FIELDS_METHOD -> lookupFields( transMeta, streamLookupMeta );
      case GET_FIELDS_METHOD -> getFields( transMeta, streamLookupMeta );
      default -> {
        var response = new JSONObject();
        response.put( ACTION_STATUS, FAILURE_METHOD_NOT_FOUND_RESPONSE );
        yield response;
      }
    };
  }
}
