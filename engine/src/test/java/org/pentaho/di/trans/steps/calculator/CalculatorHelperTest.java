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

package org.pentaho.di.trans.steps.calculator;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepHelper;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CalculatorHelperTest {

  @Mock
  private TransMeta transMeta;
  @Mock
  private StepMeta stepMeta;
  @Mock
  private CalculatorMeta calculatorMeta;
  @Mock
  private CalculatorMetaFunction calculatorMetaFunction;
  @Mock
  private RowMetaInterface rowMetaInterface;
  @Mock
  private ValueMetaInterface valueMeta;
  @Mock
  private StepMetaInterface nonCalculatorMeta;

  private CalculatorHelper calculatorHelper;
  private Map<String, String> queryParams;

  @Before
  public void setUp() {
    MockitoAnnotations.openMocks( this );
    calculatorHelper = new CalculatorHelper();
    queryParams = new HashMap<>();
  }

  @Test
  public void testHandleStepActionAllCases() throws KettleStepException {
    JSONObject result1 = calculatorHelper.handleStepAction( "getCalcTypes", transMeta, queryParams );
    assertNotNull( result1 );
    assertTrue( result1.containsKey( "calculationTypes" ) );
    JSONArray calcTypes = (JSONArray) result1.get( "calculationTypes" );
    assertTrue( calcTypes.size() > 0 );

    queryParams.put( "stepName", "testStep" );
    when( transMeta.findStep( "testStep" ) ).thenReturn( stepMeta );
    when( stepMeta.getStepMetaInterface() ).thenReturn( calculatorMeta );
    when( calculatorMeta.getCalculation() ).thenReturn( new CalculatorMetaFunction[] { calculatorMetaFunction } );
    when( calculatorMetaFunction.getFieldName() ).thenReturn( "calcField1" );
    when( transMeta.getPrevStepFields( "testStep" ) ).thenReturn( rowMetaInterface );
    when( rowMetaInterface.size() ).thenReturn( 1 );
    when( rowMetaInterface.getValueMeta( 0 ) ).thenReturn( valueMeta );
    when( valueMeta.getName() ).thenReturn( "prevField1" );

    JSONObject result2 = calculatorHelper.handleStepAction( "getCalcFields", transMeta, queryParams );
    assertNotNull( result2 );
    assertTrue( result2.containsKey( "fields" ) );
    JSONArray fields = (JSONArray) result2.get( "fields" );
    assertEquals( 2, fields.size() );

    JSONObject result3 = calculatorHelper.handleStepAction( "unknownMethod", transMeta, queryParams );
    assertNotNull( result3 );
    assertEquals( BaseStepHelper.FAILURE_METHOD_NOT_FOUND_RESPONSE, result3.get( BaseStepHelper.ACTION_STATUS ) );

    when( transMeta.getPrevStepFields( "testStep" ) ).thenThrow( new KettleStepException( "Test exception" ) );
    JSONObject result4 = calculatorHelper.handleStepAction( "getCalcFields", transMeta, queryParams );
    assertNotNull( result4 );
    assertEquals( BaseStepHelper.FAILURE_RESPONSE, result4.get( BaseStepHelper.ACTION_STATUS ) );
  }

  @Test
  public void testGetCalcTypes() {

    JSONObject result = calculatorHelper.getCalcTypes();

    assertNotNull( result );
    assertTrue( result.containsKey( "calculationTypes" ) );
    assertEquals( BaseStepHelper.SUCCESS_RESPONSE, result.get( BaseStepHelper.ACTION_STATUS ) );

    JSONArray calculationTypes = (JSONArray) result.get( "calculationTypes" );
    assertTrue( "Should have calculation types", calculationTypes.size() > 0 );

    JSONObject firstCalc = (JSONObject) calculationTypes.get( 0 );
    assertNotNull( "First calc should have id", firstCalc.get( "id" ) );
    assertNotNull( "First calc should have name", firstCalc.get( "name" ) );
  }

  @Test
  public void testGetCalcFieldsAllScenarios() throws KettleStepException {

    queryParams.put( "stepName", null );
    when( transMeta.getPrevStepFields( "null" ) ).thenReturn( null );
    JSONObject result1 = calculatorHelper.getCalcFields( transMeta, queryParams );
    assertFieldsResult( result1, 0 );
    queryParams.put( "stepName", "" );
    when( transMeta.getPrevStepFields( "" ) ).thenReturn( null );
    JSONObject result2 = calculatorHelper.getCalcFields( transMeta, queryParams );
    assertFieldsResult( result2, 0 );
    queryParams.put( "stepName", "nonExistentStep" );
    when( transMeta.findStep( "nonExistentStep" ) ).thenReturn( null );
    when( transMeta.getPrevStepFields( "nonExistentStep" ) ).thenReturn( null );
    JSONObject result3 = calculatorHelper.getCalcFields( transMeta, queryParams );
    assertFieldsResult( result3, 0 );
    queryParams.put( "stepName", "nonCalculatorStep" );
    when( transMeta.findStep( "nonCalculatorStep" ) ).thenReturn( stepMeta );
    when( stepMeta.getStepMetaInterface() ).thenReturn( nonCalculatorMeta );
    when( transMeta.getPrevStepFields( "nonCalculatorStep" ) ).thenReturn( rowMetaInterface );
    when( rowMetaInterface.size() ).thenReturn( 1 );
    when( rowMetaInterface.getValueMeta( 0 ) ).thenReturn( valueMeta );
    when( valueMeta.getName() ).thenReturn( "prevField1" );
    JSONObject result4 = calculatorHelper.getCalcFields( transMeta, queryParams );
    assertFieldsResult( result4, 1 );
    CalculatorMetaFunction nullFieldFunction = mock( CalculatorMetaFunction.class );
    when( nullFieldFunction.getFieldName() ).thenReturn( null );
    queryParams.put( "stepName", "calculatorStepNullField" );
    when( transMeta.findStep( "calculatorStepNullField" ) ).thenReturn( stepMeta );
    when( stepMeta.getStepMetaInterface() ).thenReturn( calculatorMeta );
    when( calculatorMeta.getCalculation() ).thenReturn( new CalculatorMetaFunction[] { nullFieldFunction } );
    queryParams.put( "stepName", "fullTestStep" );
    when( transMeta.findStep( "fullTestStep" ) ).thenReturn( stepMeta );
    when( stepMeta.getStepMetaInterface() ).thenReturn( calculatorMeta );
    when( calculatorMetaFunction.getFieldName() ).thenReturn( "calcField1" );
    when( calculatorMeta.getCalculation() ).thenReturn( new CalculatorMetaFunction[] { calculatorMetaFunction } );
    when( transMeta.getPrevStepFields( "fullTestStep" ) ).thenReturn( rowMetaInterface );
    when( rowMetaInterface.size() ).thenReturn( 2 );
    ValueMetaInterface valueMeta2 = mock( ValueMetaInterface.class );
    when( rowMetaInterface.getValueMeta( 0 ) ).thenReturn( valueMeta );
    when( rowMetaInterface.getValueMeta( 1 ) ).thenReturn( valueMeta2 );
    when( valueMeta.getName() ).thenReturn( "prevField1" );
    when( valueMeta2.getName() ).thenReturn( "prevField2" );
    JSONObject result7 = calculatorHelper.getCalcFields( transMeta, queryParams );
    assertFieldsResult( result7, 3 );
  }

  private void assertFieldsResult( JSONObject result, int expectedFieldCount ) {
    assertNotNull( result );
    assertTrue( result.containsKey( "fields" ) );
    assertEquals( BaseStepHelper.SUCCESS_RESPONSE, result.get( BaseStepHelper.ACTION_STATUS ) );
    JSONArray fields = (JSONArray) result.get( "fields" );
    assertEquals( expectedFieldCount, fields.size() );
  }
}
