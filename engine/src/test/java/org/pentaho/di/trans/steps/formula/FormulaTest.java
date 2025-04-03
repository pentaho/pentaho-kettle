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

package org.pentaho.di.trans.steps.formula;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.steps.mock.StepMockHelper;
import org.pentaho.libformula.editor.FormulaEvaluator;
import org.pentaho.libformula.editor.FormulaMessage;
import org.pentaho.libformula.editor.function.FunctionDescription;
import org.pentaho.libformula.editor.function.FunctionLib;
import org.pentaho.reporting.libraries.formula.lvalues.ParsePosition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class FormulaTest {

  private StepMockHelper<FormulaMeta, FormulaData> stepMockHelper;

  @Before
  public void setup() {
    stepMockHelper = new StepMockHelper<>( "Test Formula", FormulaMeta.class, FormulaData.class );

    when( stepMockHelper.logChannelInterfaceFactory.create( any(), any() ) )
      .thenReturn( stepMockHelper.logChannelInterface );
    when( stepMockHelper.trans.isRunning() ).thenReturn( true );
  }

  @After
  public void tearDown() {
    stepMockHelper.cleanUp();
  }

  @Test
  public void testEvaluateFormulaAction_noFormula() {
    // Ensure that getFormula() returns an empty array to avoid NPE in init()
    FormulaMeta meta = stepMockHelper.initStepMetaInterface;
    when( meta.getFormula() ).thenReturn( new FormulaMetaFunction[ 0 ] );

    Formula formulaStep = new Formula(
      stepMockHelper.stepMeta,
      stepMockHelper.stepDataInterface,
      0,
      stepMockHelper.transMeta,
      stepMockHelper.trans );
    formulaStep.init( stepMockHelper.initStepMetaInterface, stepMockHelper.initStepDataInterface );
    Map<String, String> queryMap = new HashMap<>();

    JSONObject response = formulaStep.doAction(
      "evaluateFormula",
      stepMockHelper.initStepMetaInterface,
      stepMockHelper.transMeta,
      stepMockHelper.trans,
      queryMap );
    assertEquals( "No formula provided.", response.get( "error" ) );
    assertEquals( StepInterface.FAILURE_RESPONSE, response.get( StepInterface.ACTION_STATUS ) );
  }

  @Test
  public void testEvaluateFormulaAction_validFormula() {
    // Stub the meta so that getFormula() returns an empty array to avoid NPE in init()
    FormulaMeta meta = stepMockHelper.initStepMetaInterface;
    when( meta.getFormula() ).thenReturn( new FormulaMetaFunction[ 0 ] );

    Formula formulaStep = new Formula(
      stepMockHelper.stepMeta,
      stepMockHelper.stepDataInterface,
      0,
      stepMockHelper.transMeta,
      stepMockHelper.trans );
    formulaStep.init( stepMockHelper.initStepMetaInterface, stepMockHelper.initStepDataInterface );

    Map<String, String> queryMap = new HashMap<>();
    queryMap.put( "formulaSyntax", "dummy formula" );
    queryMap.put( "inputFields", "fieldA, fieldB" );

    try ( MockedConstruction<FormulaEvaluator> mockedEvaluator =
            Mockito.mockConstruction( FormulaEvaluator.class, ( mock, context ) -> {
              FormulaMessage dummyMsg = new FormulaMessage(
                FormulaMessage.TYPE_MESSAGE,
                new DummyParsePosition( 1, 1, 1, 1 ),
                "Test subject",
                "Detailed message"
              );
              Map<String, FormulaMessage> dummyMessages = new HashMap<>();
              dummyMessages.put( "dummy", dummyMsg );
              when( mock.evaluateFormula( any() ) ).thenReturn( dummyMessages );
            } ) ) {

      JSONObject response = formulaStep.doAction(
        "evaluateFormula",
        stepMockHelper.initStepMetaInterface,
        stepMockHelper.transMeta,
        stepMockHelper.trans,
        queryMap );
      assertEquals( StepInterface.SUCCESS_RESPONSE, response.get( StepInterface.ACTION_STATUS ) );
      JSONArray messagesArray = (JSONArray) response.get( "messages" );
      assertNotNull( messagesArray );
      assertFalse( messagesArray.isEmpty() );
      JSONObject msgObj = (JSONObject) messagesArray.get( 0 );
      assertTrue( msgObj.get( "message" ).toString().contains( "Test subject" ) );
      assertTrue( msgObj.get( "message" ).toString().contains( "Detailed message" ) );
    }
  }

  @Test
  public void testFormulaTreeDataAction_success() {
    // Ensure that the meta returns a non-null array for getFormula()
    FormulaMeta meta = stepMockHelper.initStepMetaInterface;
    when( meta.getFormula() ).thenReturn( new FormulaMetaFunction[ 0 ] );

    Formula formulaStep = new Formula(
      stepMockHelper.stepMeta,
      stepMockHelper.stepDataInterface,
      0,
      stepMockHelper.transMeta,
      stepMockHelper.trans );
    formulaStep.init( stepMockHelper.initStepMetaInterface, stepMockHelper.initStepDataInterface );
    Map<String, String> queryMap = new HashMap<>();
    queryMap.put( "someData", "dummy" );

    try ( MockedConstruction<FunctionLib> mockedFunctionLib =
            Mockito.mockConstruction( FunctionLib.class, ( mock, context ) -> {
              when( mock.getFunctionCategories() ).thenReturn( new String[] { "TestCategory" } );
              when( mock.getFunctionsForACategory( "TestCategory" ) ).thenReturn( new String[] { "TestFunction" } );
              when( mock.getFunctionDescription( "TestFunction" ) ).thenReturn(
                new FunctionDescription( "TestCategory", "TestFunction", "A test function.", "TestSyntax",
                  "TestReturn", "TestConstraints", "TestSemantics", new ArrayList<>() )
              );
            } ) ) {
      JSONObject response = formulaStep.doAction(
        "formulaTreeData",
        stepMockHelper.initStepMetaInterface,
        stepMockHelper.transMeta,
        stepMockHelper.trans,
        queryMap );
      assertEquals( StepInterface.SUCCESS_RESPONSE, response.get( StepInterface.ACTION_STATUS ) );
      JSONArray functionTree = (JSONArray) response.get( "functionTree" );
      assertNotNull( functionTree );
      assertFalse( functionTree.isEmpty() );
      JSONObject categoryObj = (JSONObject) functionTree.get( 0 );
      assertEquals( "TestCategory", categoryObj.get( "category" ) );
      JSONArray functionsArray = (JSONArray) categoryObj.get( "functions" );
      assertNotNull( functionsArray );
      assertFalse( functionsArray.isEmpty() );
      JSONObject functionObj = (JSONObject) functionsArray.get( 0 );
      assertEquals( "TestFunction", functionObj.get( "name" ) );
      assertEquals( "A test function.", functionObj.get( "description" ) );
    }
  }

  @Test
  public void testFormulaTreeDataAction_exception() {
    // Stub meta to return an empty array to avoid NPE in init()
    FormulaMeta meta = stepMockHelper.initStepMetaInterface;
    when( meta.getFormula() ).thenReturn( new FormulaMetaFunction[ 0 ] );

    Formula formulaStep = new Formula(
      stepMockHelper.stepMeta,
      stepMockHelper.stepDataInterface,
      0,
      stepMockHelper.transMeta,
      stepMockHelper.trans );
    formulaStep.init( stepMockHelper.initStepMetaInterface, stepMockHelper.initStepDataInterface );
    Map<String, String> queryMap = new HashMap<>();

    try ( MockedConstruction<FunctionLib> mockedFunctionLib =
            Mockito.mockConstruction( FunctionLib.class, ( mock, context ) -> {
              when( mock.getFunctionCategories() ).thenThrow( new RuntimeException( "Simulated exception" ) );
            } ) ) {
      JSONObject response = formulaStep.doAction(
        "formulaTreeData",
        stepMockHelper.initStepMetaInterface,
        stepMockHelper.transMeta,
        stepMockHelper.trans,
        queryMap );
      assertEquals( "Simulated exception", response.get( "error" ) );
      assertEquals( StepInterface.FAILURE_RESPONSE, response.get( StepInterface.ACTION_STATUS ) );
    }
  }

  /**
   * Dummy ParsePosition class for testing.
   */
  private static class DummyParsePosition extends ParsePosition {
    public DummyParsePosition( int startLine, int startColumn, int endLine, int endColumn ) {
      super( startLine, startColumn, endLine, endColumn );
    }
  }
}
