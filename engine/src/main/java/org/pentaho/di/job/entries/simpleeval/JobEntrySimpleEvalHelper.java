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

package org.pentaho.di.job.entries.simpleeval;

import com.google.common.annotations.VisibleForTesting;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.pentaho.di.core.Const;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.BaseJobEntryHelper;

import java.util.Map;

/**
 * Provides action handlers for the Simple Evaluation job entry dialog,
 * exposing dropdown data for value types, field types, and field-type-specific
 * success conditions.
 *
 * @see BaseJobEntryHelper
 * @see JobEntrySimpleEval
 */
public class JobEntrySimpleEvalHelper extends BaseJobEntryHelper {

  @VisibleForTesting
  protected static final String METHOD_GET_SUCCESS_CONDITIONS = "getSuccessConditions";
  @VisibleForTesting
  protected static final String METHOD_GET_DROPDOWN_DATA = "getDropdownData";

  @VisibleForTesting
  protected static final String RESPONSE_KEY_SUCCESS_CONDITIONS = "successConditions";
  @VisibleForTesting
  protected static final String RESPONSE_KEY_VALUE_TYPES = "valueTypes";
  @VisibleForTesting
  protected static final String RESPONSE_KEY_FIELD_TYPES = "fieldTypes";

  public JobEntrySimpleEvalHelper() {
    super();
  }

  @Override
  @SuppressWarnings( "unchecked" )
  protected JSONObject handleJobEntryAction( String method, JobMeta jobMeta, Map<String, String> queryParams ) {
    JSONObject response = new JSONObject();
    if ( METHOD_GET_SUCCESS_CONDITIONS.equals( method ) ) {
      response = getSuccessConditions( queryParams );
    } else if ( METHOD_GET_DROPDOWN_DATA.equals( method ) ) {
      response = getDropdownData();
    } else {
      response.put( ACTION_STATUS, FAILURE_METHOD_NOT_FOUND_RESPONSE );
    }
    return response;
  }

  /**
   * Returns the success condition options matching the requested field type.
   * <p>
   * Reads the {@code fieldType} query parameter and returns the corresponding
   * set of success conditions:
   * <ul>
   *   <li>{@code "string"} (default) &ndash; string-based conditions (equal, different, contains, etc.)</li>
   *   <li>{@code "number"} or {@code "datetime"} &ndash; numeric/date conditions (equal, different, smaller, etc.)</li>
   *   <li>{@code "boolean"} &ndash; boolean conditions (true, false)</li>
   * </ul>
   *
   * @param queryParams query parameters; expects {@code fieldType}
   * @return a {@link JSONObject} containing the matching conditions under {@value #RESPONSE_KEY_SUCCESS_CONDITIONS}
   */
  @VisibleForTesting
  @SuppressWarnings( "unchecked" )
  protected JSONObject getSuccessConditions( Map<String, String> queryParams ) {
    String fieldType = Const.NVL( queryParams == null ? null : queryParams.get( "fieldType" ), "string" );
    JSONArray conditions = new JSONArray();

    switch ( fieldType ) {
      case "number":
      case "datetime":
        for ( int i = 0; i < JobEntrySimpleEval.successNumberConditionCode.length; i++ ) {
          JSONObject item = new JSONObject();
          item.put( "id", JobEntrySimpleEval.successNumberConditionCode[i] );
          item.put( "name", JobEntrySimpleEval.successNumberConditionDesc[i] );
          conditions.add( item );
        }
        break;
      case "boolean":
        for ( int i = 0; i < JobEntrySimpleEval.successBooleanConditionCode.length; i++ ) {
          JSONObject item = new JSONObject();
          item.put( "id", JobEntrySimpleEval.successBooleanConditionCode[i] );
          item.put( "name", JobEntrySimpleEval.successBooleanConditionDesc[i] );
          conditions.add( item );
        }
        break;
      case "string":
      default:
        for ( int i = 0; i < JobEntrySimpleEval.successConditionCode.length; i++ ) {
          JSONObject item = new JSONObject();
          item.put( "id", JobEntrySimpleEval.successConditionCode[i] );
          item.put( "name", JobEntrySimpleEval.successConditionDesc[i] );
          conditions.add( item );
        }
        break;
    }

    JSONObject response = new JSONObject();
    response.put( RESPONSE_KEY_SUCCESS_CONDITIONS, conditions );
    response.put( ACTION_STATUS, SUCCESS_RESPONSE );
    return response;
  }

  /**
   * Returns the static dropdown data for value types and field types.
   *
   * @return a {@link JSONObject} containing {@value #RESPONSE_KEY_VALUE_TYPES}
   *         and {@value #RESPONSE_KEY_FIELD_TYPES} arrays
   */
  @VisibleForTesting
  @SuppressWarnings( "unchecked" )
  protected JSONObject getDropdownData() {
    JSONArray valueTypes = new JSONArray();
    for ( int i = 0; i < JobEntrySimpleEval.valueTypeCode.length; i++ ) {
      JSONObject item = new JSONObject();
      item.put( "id", JobEntrySimpleEval.valueTypeCode[i] );
      item.put( "name", JobEntrySimpleEval.valueTypeDesc[i] );
      valueTypes.add( item );
    }

    JSONArray fieldTypes = new JSONArray();
    for ( int i = 0; i < JobEntrySimpleEval.fieldTypeCode.length; i++ ) {
      JSONObject item = new JSONObject();
      item.put( "id", JobEntrySimpleEval.fieldTypeCode[i] );
      item.put( "name", JobEntrySimpleEval.fieldTypeDesc[i] );
      fieldTypes.add( item );
    }

    JSONObject response = new JSONObject();
    response.put( RESPONSE_KEY_VALUE_TYPES, valueTypes );
    response.put( RESPONSE_KEY_FIELD_TYPES, fieldTypes );
    response.put( ACTION_STATUS, SUCCESS_RESPONSE );
    return response;
  }
}
