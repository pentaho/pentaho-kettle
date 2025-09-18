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


package org.pentaho.di.trans.steps.selectvalues;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.trans.TransMeta;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.pentaho.di.trans.step.StepHelperInterface.ACTION_STATUS;
import static org.pentaho.di.trans.step.StepHelperInterface.FAILURE_METHOD_NOT_FOUND_RESPONSE;
import static org.pentaho.di.trans.step.StepHelperInterface.SUCCESS_RESPONSE;


public class SelectValuesHelperTest {

  TransMeta transMeta;
  SelectValuesHelper underTest;

  @Before
  public void setUp() {
    underTest = new SelectValuesHelper();
    transMeta = mock( TransMeta.class );
  }

  @Test
  public void testHandleStepAction_whenMethodNameIsLocales_thenReturnsLocalesArray() {
    testFieldDataWithActionMethods( "locales" );
  }

  @Test
  public void testHandleStepAction_whenMethodNameIsTimezones_thenReturnsTimezonesArray() {
    testFieldDataWithActionMethods( "timezones" );
  }

  @Test
  public void testHandleStepAction_whenMethodNameIsEncodings_thenReturnsEncodingsArray() {
    testFieldDataWithActionMethods( "encodings" );
  }

  @Test
  public void testHandleStepAction_whenMethodNameIsInvalid_thenReturnsFailureMethodNotFound() {
    JSONObject response = underTest.stepAction( "invalidMethod", transMeta, null );

    assertNotNull( response );
    assertEquals( FAILURE_METHOD_NOT_FOUND_RESPONSE, response.get( ACTION_STATUS ) );
  }

  @Test
  public void testGetLocales_whenCalled_thenReturnsLocalesArray() {
    verifyFieldData( underTest.getLocales(), "locales" );
  }

  @Test
  public void testGetTimezones_whenCalled_thenReturnsTimezonesArray() {
    verifyFieldData( underTest.getTimezones(), "timezones" );
  }

  @Test
  public void testGetEncodings_whenCalled_thenReturnsEncodingsArray() {
    verifyFieldData( underTest.getEncodings(), "encodings" );
  }

  private void testFieldDataWithActionMethods( String method ) {
    JSONObject response = underTest.stepAction( method, transMeta, null );

    assertNotNull( response );
    assertEquals( SUCCESS_RESPONSE, response.get( ACTION_STATUS ) );
    assertTrue( response.containsKey( method ) );
    assertTrue( response.get( method ) instanceof JSONArray );
  }

  private void verifyFieldData( JSONObject response, String method ) {
    assertNotNull( response );
    assertTrue( response.containsKey( method ) );
    assertTrue( response.get( method ) instanceof JSONArray );
    assertFalse( ( (JSONArray) response.get( method ) ).isEmpty() );
  }
}
