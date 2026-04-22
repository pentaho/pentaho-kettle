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

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.job.JobMeta;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.pentaho.di.job.entry.JobEntryHelperInterface.ACTION_STATUS;
import static org.pentaho.di.job.entry.JobEntryHelperInterface.FAILURE_METHOD_NOT_FOUND_RESPONSE;
import static org.pentaho.di.job.entry.JobEntryHelperInterface.SUCCESS_RESPONSE;

public class JobEntrySimpleEvalHelperTest {

  private JobEntrySimpleEvalHelper helper;
  private JobMeta jobMeta;

  @Before
  public void setUp() {
    jobMeta = mock( JobMeta.class );
    helper = new JobEntrySimpleEvalHelper();
  }

  @Test
  public void testGetSuccessConditions_String() {
    Map<String, String> params = new HashMap<>();
    params.put( "fieldType", "string" );

    JSONObject result = helper.getSuccessConditions( params );

    assertNotNull( result );
    assertEquals( SUCCESS_RESPONSE, result.get( ACTION_STATUS ) );
    JSONArray conditions = (JSONArray) result.get( JobEntrySimpleEvalHelper.RESPONSE_KEY_SUCCESS_CONDITIONS );
    assertNotNull( conditions );
    assertEquals( JobEntrySimpleEval.successConditionCode.length, conditions.size() );

    for ( int i = 0; i < conditions.size(); i++ ) {
      JSONObject item = (JSONObject) conditions.get( i );
      assertEquals( JobEntrySimpleEval.successConditionCode[i], item.get( "id" ) );
      assertEquals( JobEntrySimpleEval.successConditionDesc[i], item.get( "name" ) );
    }
  }

  @Test
  public void testGetSuccessConditions_DefaultIsString() {
    JSONObject result = helper.getSuccessConditions( Collections.emptyMap() );

    assertNotNull( result );
    JSONArray conditions = (JSONArray) result.get( JobEntrySimpleEvalHelper.RESPONSE_KEY_SUCCESS_CONDITIONS );
    assertEquals( JobEntrySimpleEval.successConditionCode.length, conditions.size() );
  }

  @Test
  public void testGetSuccessConditions_Number() {
    Map<String, String> params = new HashMap<>();
    params.put( "fieldType", "number" );

    JSONObject result = helper.getSuccessConditions( params );

    assertNotNull( result );
    assertEquals( SUCCESS_RESPONSE, result.get( ACTION_STATUS ) );
    JSONArray conditions = (JSONArray) result.get( JobEntrySimpleEvalHelper.RESPONSE_KEY_SUCCESS_CONDITIONS );
    assertNotNull( conditions );
    assertEquals( JobEntrySimpleEval.successNumberConditionCode.length, conditions.size() );

    for ( int i = 0; i < conditions.size(); i++ ) {
      JSONObject item = (JSONObject) conditions.get( i );
      assertEquals( JobEntrySimpleEval.successNumberConditionCode[i], item.get( "id" ) );
      assertEquals( JobEntrySimpleEval.successNumberConditionDesc[i], item.get( "name" ) );
    }
  }

  @Test
  public void testGetSuccessConditions_DateTime() {
    Map<String, String> params = new HashMap<>();
    params.put( "fieldType", "datetime" );

    JSONObject result = helper.getSuccessConditions( params );

    assertNotNull( result );
    JSONArray conditions = (JSONArray) result.get( JobEntrySimpleEvalHelper.RESPONSE_KEY_SUCCESS_CONDITIONS );
    // datetime uses the same conditions as number
    assertEquals( JobEntrySimpleEval.successNumberConditionCode.length, conditions.size() );
  }

  @Test
  public void testGetSuccessConditions_Boolean() {
    Map<String, String> params = new HashMap<>();
    params.put( "fieldType", "boolean" );

    JSONObject result = helper.getSuccessConditions( params );

    assertNotNull( result );
    assertEquals( SUCCESS_RESPONSE, result.get( ACTION_STATUS ) );
    JSONArray conditions = (JSONArray) result.get( JobEntrySimpleEvalHelper.RESPONSE_KEY_SUCCESS_CONDITIONS );
    assertNotNull( conditions );
    assertEquals( JobEntrySimpleEval.successBooleanConditionCode.length, conditions.size() );

    for ( int i = 0; i < conditions.size(); i++ ) {
      JSONObject item = (JSONObject) conditions.get( i );
      assertEquals( JobEntrySimpleEval.successBooleanConditionCode[i], item.get( "id" ) );
      assertEquals( JobEntrySimpleEval.successBooleanConditionDesc[i], item.get( "name" ) );
    }
  }

  @Test
  public void testGetDropdownData() {
    JSONObject result = helper.getDropdownData();

    assertNotNull( result );
    assertEquals( SUCCESS_RESPONSE, result.get( ACTION_STATUS ) );

    JSONArray valueTypes = (JSONArray) result.get( JobEntrySimpleEvalHelper.RESPONSE_KEY_VALUE_TYPES );
    assertNotNull( valueTypes );
    assertEquals( JobEntrySimpleEval.valueTypeCode.length, valueTypes.size() );

    for ( int i = 0; i < valueTypes.size(); i++ ) {
      JSONObject item = (JSONObject) valueTypes.get( i );
      assertEquals( JobEntrySimpleEval.valueTypeCode[i], item.get( "id" ) );
      assertEquals( JobEntrySimpleEval.valueTypeDesc[i], item.get( "name" ) );
    }

    JSONArray fieldTypes = (JSONArray) result.get( JobEntrySimpleEvalHelper.RESPONSE_KEY_FIELD_TYPES );
    assertNotNull( fieldTypes );
    assertEquals( JobEntrySimpleEval.fieldTypeCode.length, fieldTypes.size() );

    for ( int i = 0; i < fieldTypes.size(); i++ ) {
      JSONObject item = (JSONObject) fieldTypes.get( i );
      assertEquals( JobEntrySimpleEval.fieldTypeCode[i], item.get( "id" ) );
      assertEquals( JobEntrySimpleEval.fieldTypeDesc[i], item.get( "name" ) );
    }
  }

  @Test
  public void testHandleJobEntryAction_GetSuccessConditions() {
    Map<String, String> params = new HashMap<>();
    params.put( "fieldType", "string" );

    JSONObject result = helper.handleJobEntryAction(
      JobEntrySimpleEvalHelper.METHOD_GET_SUCCESS_CONDITIONS, jobMeta, params );

    assertNotNull( result );
    assertTrue( result.containsKey( JobEntrySimpleEvalHelper.RESPONSE_KEY_SUCCESS_CONDITIONS ) );
  }

  @Test
  public void testHandleJobEntryAction_GetDropdownData() {
    JSONObject result = helper.handleJobEntryAction(
      JobEntrySimpleEvalHelper.METHOD_GET_DROPDOWN_DATA, jobMeta, Collections.emptyMap() );

    assertNotNull( result );
    assertTrue( result.containsKey( JobEntrySimpleEvalHelper.RESPONSE_KEY_VALUE_TYPES ) );
    assertTrue( result.containsKey( JobEntrySimpleEvalHelper.RESPONSE_KEY_FIELD_TYPES ) );
  }

  @Test
  public void testHandleJobEntryAction_InvalidMethod() {
    JSONObject result = helper.handleJobEntryAction( "invalidMethod", jobMeta, Collections.emptyMap() );

    assertNotNull( result );
    assertEquals( FAILURE_METHOD_NOT_FOUND_RESPONSE, result.get( ACTION_STATUS ) );
  }

  @Test
  public void testHandleJobEntryAction_NumberAndDateTimeSameConditions() {
    Map<String, String> numberParams = new HashMap<>();
    numberParams.put( "fieldType", "number" );
    JSONObject numberResult = helper.getSuccessConditions( numberParams );

    Map<String, String> dateTimeParams = new HashMap<>();
    dateTimeParams.put( "fieldType", "datetime" );
    JSONObject dateTimeResult = helper.getSuccessConditions( dateTimeParams );

    JSONArray numberConditions =
      (JSONArray) numberResult.get( JobEntrySimpleEvalHelper.RESPONSE_KEY_SUCCESS_CONDITIONS );
    JSONArray dateTimeConditions =
      (JSONArray) dateTimeResult.get( JobEntrySimpleEvalHelper.RESPONSE_KEY_SUCCESS_CONDITIONS );

    assertEquals( numberConditions.size(), dateTimeConditions.size() );
    for ( int i = 0; i < numberConditions.size(); i++ ) {
      JSONObject numItem = (JSONObject) numberConditions.get( i );
      JSONObject dtItem = (JSONObject) dateTimeConditions.get( i );
      assertEquals( numItem.get( "id" ), dtItem.get( "id" ) );
      assertEquals( numItem.get( "name" ), dtItem.get( "name" ) );
    }
  }
}
