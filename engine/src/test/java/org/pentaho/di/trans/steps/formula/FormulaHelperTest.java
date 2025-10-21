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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.steps.mock.StepMockHelper;
import org.pentaho.libformula.editor.FormulaEvaluator;
import org.pentaho.libformula.editor.FormulaMessage;
import org.pentaho.libformula.editor.function.FunctionDescription;
import org.pentaho.libformula.editor.function.FunctionLib;
import org.pentaho.reporting.libraries.formula.lvalues.ParsePosition;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class FormulaHelperTest {

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

    FormulaHelper helper = new FormulaHelper();
    Map<String, String> queryMap = new HashMap<>();

    JSONObject response = helper.stepAction(
      "evaluateFormula",
      stepMockHelper.transMeta,
      queryMap );
    assertEquals( "No formula provided.", response.get( "error" ) );
    assertEquals( StepInterface.FAILURE_RESPONSE, response.get( StepInterface.ACTION_STATUS ) );
  }

  @Test
  public void testEvaluateFormulaAction_validFormula() {
    // Stub the meta so that getFormula() returns an empty array to avoid NPE in init()
    FormulaMeta meta = stepMockHelper.initStepMetaInterface;
    when( meta.getFormula() ).thenReturn( new FormulaMetaFunction[ 0 ] );

    FormulaHelper helper = new FormulaHelper();
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

      JSONObject response = helper.stepAction(
        "evaluateFormula",
        stepMockHelper.transMeta,
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
  public void testEvaluateFormulaAction_NoInputFields() {
    // Stub the meta so that getFormula() returns an empty array to avoid NPE in init()
    FormulaMeta meta = stepMockHelper.initStepMetaInterface;
    when( meta.getFormula() ).thenReturn( new FormulaMetaFunction[ 0 ] );

    FormulaHelper helper = new FormulaHelper();
    Map<String, String> queryMap = new HashMap<>();
    queryMap.put( "formulaSyntax", "dummy formula" );

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

      JSONObject response = helper.stepAction(
        "evaluateFormula",
        stepMockHelper.transMeta,
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

    FormulaHelper helper = new FormulaHelper();
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
      JSONObject response = helper.stepAction(
        "formulaTreeData",
        stepMockHelper.transMeta,
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

    FormulaHelper helper = new FormulaHelper();
    Map<String, String> queryMap = new HashMap<>();

    try ( MockedConstruction<FunctionLib> mockedFunctionLib =
            Mockito.mockConstruction( FunctionLib.class, ( mock, context ) -> {
              when( mock.getFunctionCategories() ).thenThrow( new RuntimeException( "Simulated exception" ) );
            } ) ) {
      JSONObject response = helper.stepAction(
        "formulaTreeData",
        stepMockHelper.transMeta,
        queryMap );
      assertEquals( "Simulated exception", response.get( "error" ) );
      assertEquals( StepInterface.FAILURE_RESPONSE, response.get( StepInterface.ACTION_STATUS ) );
    }
  }

  @Test
  public void testUnknownMethodReturnsNotFound() {
    FormulaHelper helper = new FormulaHelper();
    JSONObject resp = helper.stepAction( "unknownMethod", stepMockHelper.transMeta, new HashMap<>() );
    assertEquals( org.pentaho.di.trans.step.StepHelperInterface.FAILURE_METHOD_NOT_FOUND_RESPONSE,
      resp.get( org.pentaho.di.trans.step.StepHelperInterface.ACTION_STATUS ) );
  }

  @Test
  public void testFunctionLibLoadFailure() {
    try ( MockedConstruction<FunctionLib> mocked = Mockito.mockConstruction( FunctionLib.class,
      ( mock, context ) -> { throw new RuntimeException("load fail"); } ) ) {
      FormulaHelper helper = new FormulaHelper();
      JSONObject resp = helper.stepAction( "formulaTreeData", stepMockHelper.transMeta, new HashMap<>() );
      assertEquals( org.pentaho.di.trans.step.StepHelperInterface.FAILURE_RESPONSE,
        resp.get( org.pentaho.di.trans.step.StepHelperInterface.ACTION_STATUS ) );
    }
  }

  @Test
  public void testEvaluateMessagePositionIncluded() {
    Map<String, String> query = new HashMap<>();
    query.put( "formulaSyntax", "dummy" );

    try ( MockedConstruction<FormulaEvaluator> mockedEval = Mockito.mockConstruction( FormulaEvaluator.class,
      ( mock, context ) -> {
        FormulaMessage msg = new FormulaMessage( FormulaMessage.TYPE_MESSAGE,
          new DummyParsePosition( 1,1,1,1 ), "sub", "detail" );
        Map<String, FormulaMessage> map = new HashMap<>();
        map.put( "k", msg );
        when( mock.evaluateFormula( any() ) ).thenReturn( map );
      } ) ) {
      FormulaHelper helper = new FormulaHelper();
      JSONObject resp = helper.stepAction( "evaluateFormula", stepMockHelper.transMeta, query );
      assertEquals( StepInterface.SUCCESS_RESPONSE,
        resp.get( StepInterface.ACTION_STATUS ) );
      JSONArray msgs = (JSONArray) resp.get( "messages" );
      JSONObject m = (JSONObject) msgs.get(0);
      assertTrue( m.containsKey( "position" ) );
    }
  }

  @Test
  public void testEvaluateUsesPrevStepFieldsWhenStepNameProvided() throws KettleStepException {
    // prepare prev step fields
    org.pentaho.di.core.row.RowMeta prevMeta = new org.pentaho.di.core.row.RowMeta();
    prevMeta.addValueMeta( new org.pentaho.di.core.row.value.ValueMetaString( "prevA" ) );
    prevMeta.addValueMeta( new org.pentaho.di.core.row.value.ValueMetaString( "prevB" ) );

    // stub transMeta.getPrevStepFields
    when( stepMockHelper.transMeta.getPrevStepFields( "prevStep" ) ).thenReturn( prevMeta );

    Map<String, String> query = new HashMap<>();
    query.put( "formulaSyntax", "dummy" );
    query.put( "stepName", "prevStep" );

    try ( MockedConstruction<FormulaEvaluator> mockedEval = Mockito.mockConstruction( FormulaEvaluator.class,
      ( mock, context ) -> {
        FormulaMessage dummyMsg = new FormulaMessage( FormulaMessage.TYPE_MESSAGE,
          new DummyParsePosition( 1, 1, 1, 1 ), "s", "d" );
        Map<String, FormulaMessage> dummyMessages = new HashMap<>();
        dummyMessages.put( "k", dummyMsg );
        when( mock.evaluateFormula( any() ) ).thenReturn( dummyMessages );
      } ) ) {
      FormulaHelper helper = new FormulaHelper();
      JSONObject resp = helper.stepAction( "evaluateFormula", stepMockHelper.transMeta, query );
      assertEquals( StepInterface.SUCCESS_RESPONSE, resp.get( StepInterface.ACTION_STATUS ) );
    }
  }

  @Test
  public void testEvaluateHandlesPrevStepFieldsException() throws KettleStepException {
    // make getPrevStepFields throw
    when( stepMockHelper.transMeta.getPrevStepFields( "badStep" ) ).thenThrow( new RuntimeException( "boom" ) );

    Map<String, String> query = new HashMap<>();
    query.put( "formulaSyntax", "dummy" );
    query.put( "stepName", "badStep" );

    try ( MockedConstruction<FormulaEvaluator> mockedEval = Mockito.mockConstruction( FormulaEvaluator.class,
      ( mock, context ) -> {
        FormulaMessage dummyMsg = new FormulaMessage( FormulaMessage.TYPE_MESSAGE,
          new DummyParsePosition( 1, 1, 1, 1 ), "s", "d" );
        Map<String, FormulaMessage> dummyMessages = new HashMap<>();
        dummyMessages.put( "k", dummyMsg );
        when( mock.evaluateFormula( any() ) ).thenReturn( dummyMessages );
      } ) ) {
      FormulaHelper helper = new FormulaHelper();
      JSONObject resp = helper.stepAction( "evaluateFormula", stepMockHelper.transMeta, query );
      // should still succeed and not propagate the exception
      assertEquals( StepInterface.SUCCESS_RESPONSE, resp.get( StepInterface.ACTION_STATUS ) );
    }
  }

  @Test
  public void testEvaluateFormulaAction_getKeyWordsFailure() {
    // mock FunctionLib constructor to throw to trigger getKeyWords failure
    Map<String, String> query = new HashMap<>();
    query.put( "formulaSyntax", "dummy" );

    try ( MockedConstruction<FunctionLib> mocked = Mockito.mockConstruction( FunctionLib.class,
      ( mock, context ) -> { throw new RuntimeException( "fnlib fail" ); } ) ) {
      FormulaHelper helper = new FormulaHelper();
      JSONObject resp = helper.stepAction( "evaluateFormula", stepMockHelper.transMeta, query );
  assertEquals( StepInterface.FAILURE_RESPONSE, resp.get( StepInterface.ACTION_STATUS ) );
  // The FunctionLib constructor mock may wrap/alter the original exception message; assert the
  // stable error prefix produced by getKeyWords instead of depending on the raw exception text.
  assertTrue( resp.get( "error" ).toString().contains( "Error loading function keywords" ) );
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
