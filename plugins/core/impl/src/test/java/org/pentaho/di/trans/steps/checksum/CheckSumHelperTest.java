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

package org.pentaho.di.trans.steps.checksum;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepInterface;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.pentaho.di.trans.step.StepHelperInterface.ACTION_STATUS;
import static org.pentaho.di.trans.step.StepHelperInterface.FAILURE_METHOD_NOT_FOUND_RESPONSE;
import static org.pentaho.di.trans.step.StepHelperInterface.SUCCESS_RESPONSE;
import static org.pentaho.di.trans.steps.checksum.CheckSumHelper.CHECKSUM_TYPE_RESPONSE_KEY;
import static org.pentaho.di.trans.steps.checksum.CheckSumHelper.DEFAULT_VALUES;
import static org.pentaho.di.trans.steps.checksum.CheckSumHelper.DEFAULT_VALUES_ACTION;
import static org.pentaho.di.trans.steps.checksum.CheckSumHelper.EVALUATION_METHOD_RESPONSE_KEY;
import static org.pentaho.di.trans.steps.checksum.CheckSumHelper.RESULT_TYPE_RESPONSE_KEY;

public class CheckSumHelperTest {

  TransMeta transMeta;
  CheckSumHelper underTest;
  CheckSumMeta checkSumMeta;

  @Before
  public void setUp() {
    checkSumMeta = spy( CheckSumMeta.class );
    underTest = new CheckSumHelper( checkSumMeta );
    transMeta = mock( TransMeta.class );
  }

  @Test
  public void testHandleStepAction_whenMethodNameIsCheckSumTypes() {
    testFieldDataWithActionMethods( "getCheckSumTypes", "checkSumTypes" );
  }

  @Test
  public void testHandleStepAction_whenMethodNameIsEvaluationMethods() {
    testFieldDataWithActionMethods( "getEvaluationMethods", "evaluationMethods" );
  }

  @Test
  public void testHandleStepAction_whenMethodNameIsResultTypes() {
    testFieldDataWithActionMethods( "getResultTypes", "resultTypes" );
  }

  @Test
  public void testHandleStepAction_whenMethodNameIsInvalid() {
    JSONObject response = underTest.stepAction( "invalidMethod", transMeta, null );

    assertNotNull( response );
    assertEquals( FAILURE_METHOD_NOT_FOUND_RESPONSE, response.get( ACTION_STATUS ) );
  }

  @Test
  public void testGetCheckSumTypes_returnsData() {
    verifyFieldData( underTest.checkSumTypes(), "checkSumTypes" );
  }

  @Test
  public void testGetEvaluationMethods_returnsData() {
    verifyFieldData( underTest.evaluationMethods(), "evaluationMethods" );
  }

  @Test
  public void testGetResultTypes_returnsData() {
    verifyFieldData( underTest.resultTypes(), "resultTypes" );
  }

  @Test
  public void getDefaultValues_ReturnsPopulatedDefaultValues() {
    JSONObject response = underTest.stepAction( DEFAULT_VALUES_ACTION, transMeta, null );

    assertNotNull( response );
    assertEquals( StepInterface.SUCCESS_RESPONSE, response.get( ACTION_STATUS ) );
    JSONObject defaultValues = (JSONObject) response.get( DEFAULT_VALUES );
    assertNotNull( defaultValues );
    assertEquals( "CRC32", defaultValues.get( CHECKSUM_TYPE_RESPONSE_KEY ) );
    assertEquals( "hexadecimal", defaultValues.get( RESULT_TYPE_RESPONSE_KEY ) );
    assertEquals( "BYTES", defaultValues.get( EVALUATION_METHOD_RESPONSE_KEY ) );
  }

  private void testFieldDataWithActionMethods( String method, String expectedKey ) {
    JSONObject response = underTest.stepAction( method, transMeta, null );
    assertNotNull( response );
    assertEquals( SUCCESS_RESPONSE, response.get( ACTION_STATUS ) );
    assertTrue( response.containsKey( expectedKey ) );
    assertTrue( response.get( expectedKey ) instanceof JSONArray );
  }

  private void verifyFieldData( JSONObject response, String method ) {
    assertNotNull( response );
    assertTrue( response.containsKey( method ) );
    assertTrue( response.get( method ) instanceof JSONArray );
    assertFalse( ( (JSONArray) response.get( method ) ).isEmpty() );
  }
}
