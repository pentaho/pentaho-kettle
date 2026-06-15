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

package org.pentaho.di.trans.steps.regexeval;

import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.trans.TransMeta;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.pentaho.di.trans.step.StepHelperInterface.ACTION_STATUS;
import static org.pentaho.di.trans.step.StepHelperInterface.FAILURE_METHOD_NOT_FOUND_RESPONSE;
import static org.pentaho.di.trans.step.StepInterface.FAILURE_RESPONSE;
import static org.pentaho.di.trans.step.StepInterface.SUCCESS_RESPONSE;

/**
 * Full-coverage unit tests for {@link RegexEvalHelper}.
 *
 * Covers:
 * <ul>
 *   <li>{@code handleStepAction} routing (testRegex, unknown method)</li>
 *   <li>{@code stepAction} public API (delegates to handleStepAction)</li>
 *   <li>{@code testRegexAction}: valid script, null script, empty/blank script,
 *       invalid regex, variable interpolation, canonical equality flag, regex options prefix</li>
 * </ul>
 */
@RunWith( MockitoJUnitRunner.class )
public class RegexEvalHelperTest {

  @ClassRule
  public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @Mock
  private RegexEvalMeta regexEvalMeta;

  @Mock
  private TransMeta transMeta;

  private RegexEvalHelper helper;

  @BeforeClass
  public static void setUpClass() throws KettleException {
    KettleEnvironment.init();
  }

  @Before
  public void setUp() {
    helper = new RegexEvalHelper( regexEvalMeta );
  }

  // ─────────────────────────────────────────────────────────────────────────
  // handleStepAction – routing
  // ─────────────────────────────────────────────────────────────────────────

  @Test
  public void testHandleStepAction_testRegexMethod_delegatesToTestRegexAction() {
    when( regexEvalMeta.getScript() ).thenReturn( "[a-z]+" );
    when( regexEvalMeta.getRegexOptions() ).thenReturn( "" );
    when( regexEvalMeta.isUseVariableInterpolationFlagSet() ).thenReturn( false );
    when( regexEvalMeta.isCanonicalEqualityFlagSet() ).thenReturn( false );

    JSONObject response = helper.handleStepAction( "testRegex", transMeta, null );

    assertNotNull( response );
    assertEquals( SUCCESS_RESPONSE, response.get( ACTION_STATUS ) );
  }

  @Test
  public void testHandleStepAction_unknownMethod_returnsMethodNotFound() {
    JSONObject response = helper.handleStepAction( "unknownMethod", transMeta, null );

    assertNotNull( response );
    assertEquals( FAILURE_METHOD_NOT_FOUND_RESPONSE, response.get( ACTION_STATUS ) );
  }

  // ─────────────────────────────────────────────────────────────────────────
  // stepAction – public API (BaseStepHelper template)
  // ─────────────────────────────────────────────────────────────────────────

  @Test
  public void testStepAction_testRegexMethod_returnsSuccess() {
    when( regexEvalMeta.getScript() ).thenReturn( "\\d{3}-\\d{4}" );
    when( regexEvalMeta.getRegexOptions() ).thenReturn( "" );
    when( regexEvalMeta.isUseVariableInterpolationFlagSet() ).thenReturn( false );
    when( regexEvalMeta.isCanonicalEqualityFlagSet() ).thenReturn( false );

    JSONObject response = helper.stepAction( "testRegex", transMeta, null );

    assertNotNull( response );
    assertEquals( SUCCESS_RESPONSE, response.get( ACTION_STATUS ) );
  }

  @Test
  public void testStepAction_unknownMethod_returnsMethodNotFound() {
    JSONObject response = helper.stepAction( "invalidMethod", transMeta, null );

    assertNotNull( response );
    assertEquals( FAILURE_METHOD_NOT_FOUND_RESPONSE, response.get( ACTION_STATUS ) );
  }

  // ─────────────────────────────────────────────────────────────────────────
  // testRegexAction – success paths
  // ─────────────────────────────────────────────────────────────────────────

  @Test
  public void testTestRegexAction_validScript_returnsSuccessWithMessage() {
    when( regexEvalMeta.getScript() ).thenReturn( "[a-zA-Z]+" );
    when( regexEvalMeta.getRegexOptions() ).thenReturn( "" );
    when( regexEvalMeta.isUseVariableInterpolationFlagSet() ).thenReturn( false );
    when( regexEvalMeta.isCanonicalEqualityFlagSet() ).thenReturn( false );

    JSONObject response = helper.testRegexAction( transMeta );

    assertNotNull( response );
    assertEquals( SUCCESS_RESPONSE, response.get( ACTION_STATUS ) );
    assertNotNull( response.get( "message" ) );
  }

  @Test
  public void testTestRegexAction_withRegexOptions_prependsOptionsAndSucceeds() {
    // getRegexOptions() returns "(?i)" for case-insensitive flag
    when( regexEvalMeta.getScript() ).thenReturn( "[a-z]+" );
    when( regexEvalMeta.getRegexOptions() ).thenReturn( "(?i)" );
    when( regexEvalMeta.isUseVariableInterpolationFlagSet() ).thenReturn( false );
    when( regexEvalMeta.isCanonicalEqualityFlagSet() ).thenReturn( false );

    JSONObject response = helper.testRegexAction( transMeta );

    assertNotNull( response );
    assertEquals( SUCCESS_RESPONSE, response.get( ACTION_STATUS ) );
  }

  // ─────────────────────────────────────────────────────────────────────────
  // testRegexAction – variable interpolation
  // ─────────────────────────────────────────────────────────────────────────

  @Test
  public void testTestRegexAction_withVariableInterpolation_callsEnvironmentSubstitute() {
    when( regexEvalMeta.getScript() ).thenReturn( "${REGEX_VAR}" );
    when( regexEvalMeta.isUseVariableInterpolationFlagSet() ).thenReturn( true );
    when( transMeta.environmentSubstitute( "${REGEX_VAR}" ) ).thenReturn( "[0-9]+" );
    when( regexEvalMeta.getRegexOptions() ).thenReturn( "" );
    when( regexEvalMeta.isCanonicalEqualityFlagSet() ).thenReturn( false );

    JSONObject response = helper.testRegexAction( transMeta );

    assertNotNull( response );
    assertEquals( SUCCESS_RESPONSE, response.get( ACTION_STATUS ) );
    verify( transMeta ).environmentSubstitute( "${REGEX_VAR}" );
  }

  @Test
  public void testTestRegexAction_variableInterpolationDisabled_doesNotCallEnvironmentSubstitute() {
    when( regexEvalMeta.getScript() ).thenReturn( "[0-9]+" );
    when( regexEvalMeta.isUseVariableInterpolationFlagSet() ).thenReturn( false );
    when( regexEvalMeta.getRegexOptions() ).thenReturn( "" );
    when( regexEvalMeta.isCanonicalEqualityFlagSet() ).thenReturn( false );

    JSONObject response = helper.testRegexAction( transMeta );

    assertNotNull( response );
    assertEquals( SUCCESS_RESPONSE, response.get( ACTION_STATUS ) );
    verify( transMeta, never() ).environmentSubstitute( "[0-9]+" );
  }

  @Test
  public void testTestRegexAction_variableInterpolationEnabled_nullTransMeta_usesRawScript() {
    when( regexEvalMeta.getScript() ).thenReturn( "[a-z]+" );
    when( regexEvalMeta.isUseVariableInterpolationFlagSet() ).thenReturn( true );
    when( regexEvalMeta.getRegexOptions() ).thenReturn( "" );
    when( regexEvalMeta.isCanonicalEqualityFlagSet() ).thenReturn( false );

    // transMeta is null – environmentSubstitute must NOT be called
    JSONObject response = helper.testRegexAction( null );

    assertNotNull( response );
    assertEquals( SUCCESS_RESPONSE, response.get( ACTION_STATUS ) );
  }

  @Test
  public void testTestRegexAction_interpolatedScriptBecomesEmpty_returnsFailure() {
    when( regexEvalMeta.getScript() ).thenReturn( "${EMPTY_VAR}" );
    when( regexEvalMeta.isUseVariableInterpolationFlagSet() ).thenReturn( true );
    when( transMeta.environmentSubstitute( "${EMPTY_VAR}" ) ).thenReturn( "" );

    JSONObject response = helper.testRegexAction( transMeta );

    assertNotNull( response );
    assertEquals( FAILURE_RESPONSE, response.get( ACTION_STATUS ) );
    assertNotNull( response.get( "errorMessage" ) );
  }

  @Test
  public void testTestRegexAction_interpolatedScriptBecomesBlank_returnsFailure() {
    when( regexEvalMeta.getScript() ).thenReturn( "${BLANK_VAR}" );
    when( regexEvalMeta.isUseVariableInterpolationFlagSet() ).thenReturn( true );
    when( transMeta.environmentSubstitute( "${BLANK_VAR}" ) ).thenReturn( "   " );

    JSONObject response = helper.testRegexAction( transMeta );

    assertNotNull( response );
    assertEquals( FAILURE_RESPONSE, response.get( ACTION_STATUS ) );
    assertNotNull( response.get( "errorMessage" ) );
  }

  // ─────────────────────────────────────────────────────────────────────────
  // testRegexAction – failure paths (null / empty / invalid)
  // ─────────────────────────────────────────────────────────────────────────

  @Test
  public void testTestRegexAction_nullScript_returnsFailureWithErrorMessage() {
    when( regexEvalMeta.getScript() ).thenReturn( null );
    // isUseVariableInterpolationFlagSet() is never reached: short-circuit on `script != null`

    JSONObject response = helper.testRegexAction( transMeta );

    assertNotNull( response );
    assertEquals( FAILURE_RESPONSE, response.get( ACTION_STATUS ) );
    assertNotNull( response.get( "errorMessage" ) );
  }

  @Test
  public void testTestRegexAction_emptyScript_returnsFailureWithErrorMessage() {
    when( regexEvalMeta.getScript() ).thenReturn( "" );
    when( regexEvalMeta.isUseVariableInterpolationFlagSet() ).thenReturn( false );

    JSONObject response = helper.testRegexAction( transMeta );

    assertNotNull( response );
    assertEquals( FAILURE_RESPONSE, response.get( ACTION_STATUS ) );
    assertNotNull( response.get( "errorMessage" ) );
  }

  @Test
  public void testTestRegexAction_blankScript_returnsFailureWithErrorMessage() {
    when( regexEvalMeta.getScript() ).thenReturn( "   " );
    when( regexEvalMeta.isUseVariableInterpolationFlagSet() ).thenReturn( false );

    JSONObject response = helper.testRegexAction( transMeta );

    assertNotNull( response );
    assertEquals( FAILURE_RESPONSE, response.get( ACTION_STATUS ) );
    assertNotNull( response.get( "errorMessage" ) );
  }

  @Test
  public void testTestRegexAction_invalidRegex_returnsFailureWithErrorMessage() {
    // "[invalid(" is a syntactically broken regex → PatternSyntaxException
    when( regexEvalMeta.getScript() ).thenReturn( "[invalid(" );
    when( regexEvalMeta.getRegexOptions() ).thenReturn( "" );
    when( regexEvalMeta.isUseVariableInterpolationFlagSet() ).thenReturn( false );
    when( regexEvalMeta.isCanonicalEqualityFlagSet() ).thenReturn( false );

    JSONObject response = helper.testRegexAction( transMeta );

    assertNotNull( response );
    assertEquals( FAILURE_RESPONSE, response.get( ACTION_STATUS ) );
    assertNotNull( response.get( "errorMessage" ) );
  }

  @Test
  public void testTestRegexAction_invalidRegexWithOptions_returnsFailure() {
    when( regexEvalMeta.getScript() ).thenReturn( "(((" );
    when( regexEvalMeta.getRegexOptions() ).thenReturn( "(?i)" );
    when( regexEvalMeta.isUseVariableInterpolationFlagSet() ).thenReturn( false );
    when( regexEvalMeta.isCanonicalEqualityFlagSet() ).thenReturn( false );

    JSONObject response = helper.testRegexAction( transMeta );

    assertNotNull( response );
    assertEquals( FAILURE_RESPONSE, response.get( ACTION_STATUS ) );
    assertNotNull( response.get( "errorMessage" ) );
  }

  // ─────────────────────────────────────────────────────────────────────────
  // Verify SUCCESS_RESPONSE is not overridden by BaseStepHelper.stepAction
  // when ACTION_STATUS is already set by testRegexAction
  // ─────────────────────────────────────────────────────────────────────────

  @Test
  public void testStepAction_failingRegex_returnsFailureNotMethodNotFound() {
    when( regexEvalMeta.getScript() ).thenReturn( "((broken" );
    when( regexEvalMeta.getRegexOptions() ).thenReturn( "" );
    when( regexEvalMeta.isUseVariableInterpolationFlagSet() ).thenReturn( false );
    when( regexEvalMeta.isCanonicalEqualityFlagSet() ).thenReturn( false );

    JSONObject response = helper.stepAction( "testRegex", transMeta, null );

    assertNotNull( response );
    // Must be FAILURE_RESPONSE ("Action failed"), NOT FAILURE_METHOD_NOT_FOUND_RESPONSE
    assertEquals( FAILURE_RESPONSE, response.get( ACTION_STATUS ) );
  }

  @Test
  public void testStepAction_nullScript_returnsActionFailed() {
    when( regexEvalMeta.getScript() ).thenReturn( null );
    // isUseVariableInterpolationFlagSet() is never reached: short-circuit on `script != null`

    JSONObject response = helper.stepAction( "testRegex", transMeta, null );

    assertNotNull( response );
    assertEquals( FAILURE_RESPONSE, response.get( ACTION_STATUS ) );
  }

  // ─────────────────────────────────────────────────────────────────────────
  // Verify BaseStepHelper.isFailedResponse utility
  // ─────────────────────────────────────────────────────────────────────────

  @Test
  public void testIsFailedResponse_failureResponse_returnsTrue() {
    JSONObject failureResponse = new JSONObject();
    failureResponse.put( ACTION_STATUS, FAILURE_RESPONSE );

    assertEquals( true, helper.isFailedResponse( failureResponse ) );
  }

  @Test
  public void testIsFailedResponse_successResponse_returnsFalse() {
    JSONObject successResponse = new JSONObject();
    successResponse.put( ACTION_STATUS, SUCCESS_RESPONSE );

    assertEquals( false, helper.isFailedResponse( successResponse ) );
  }

  @Test
  public void testIsFailedResponse_nullResponse_returnsTrue() {
    assertEquals( true, helper.isFailedResponse( null ) );
  }

  @Test
  public void testIsFailedResponse_noStatusKey_returnsFalse() {
    JSONObject emptyResponse = new JSONObject();

    assertEquals( false, helper.isFailedResponse( emptyResponse ) );
  }
}

