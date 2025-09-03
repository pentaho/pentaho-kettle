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


package org.pentaho.di.trans.steps.transexecutor;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.logging.LogChannelInterfaceFactory;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.parameters.UnknownParamException;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.TransMeta;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.pentaho.di.trans.step.StepHelperInterface.ACTION_STATUS;
import static org.pentaho.di.trans.step.StepHelperInterface.FAILURE_METHOD_NOT_FOUND_RESPONSE;
import static org.pentaho.di.trans.step.StepHelperInterface.FAILURE_RESPONSE;

public class TransExecutorHelperTest {

  TransExecutorMeta transExecutorMeta;
  TransMeta transMeta;

  TransExecutorHelper underTest;

  @Before
  public void setUp() throws Exception {
    transMeta = mock( TransMeta.class );
    transExecutorMeta = mock( TransExecutorMeta.class );
    underTest = spy( new TransExecutorHelper( transExecutorMeta ) );

    LogChannelInterfaceFactory logChannelInterfaceFactory = mock( LogChannelInterfaceFactory.class );
    LogChannelInterface logChannelInterface = mock( LogChannelInterface.class );
    KettleLogStore.setLogChannelInterfaceFactory( logChannelInterfaceFactory );
    when( logChannelInterfaceFactory.create( any(), any( LoggingObjectInterface.class ) ) ).thenReturn(
      logChannelInterface );
    when( logChannelInterfaceFactory.create( any() ) ).thenReturn( logChannelInterface );

    Repository mockRepository = mock( Repository.class );
    TransExecutorParameters parameters = mock( TransExecutorParameters.class );

    when( transExecutorMeta.getParameters() ).thenReturn( parameters );
    when( transMeta.getRepository() ).thenReturn( mockRepository );
    when( transMeta.listVariables() ).thenReturn( new String[] { "var1", "var2" } );
    when( transMeta.getVariable( "var1" ) ).thenReturn( "value1" );
    when( transMeta.getVariable( "var2" ) ).thenReturn( "value2" );
    when( transMeta.listParameters() ).thenReturn( new String[] { "param1", "param2" } );
    when( transMeta.getParameterDescription( "param1" ) ).thenReturn( "desc1" );
    when( transMeta.getParameterDescription( "param2" ) ).thenReturn( "desc2" );
    when( transMeta.getParameterDefault( "param1" ) ).thenReturn( "default1" );
    when( transMeta.getParameterDefault( "param2" ) ).thenReturn( "default2" );

    doReturn( transMeta ).when( underTest ).loadExecutorTransMeta( transMeta, transExecutorMeta );
  }

  @Test
  public void testGetParametersFromTrans_returnsParameters() {
    JSONObject response = underTest.stepAction( "parameters", transMeta, null );

    JSONArray parameters = (JSONArray) response.get( "parameters" );
    assertNotNull( response );
    assertTrue( response.containsKey( "parameters" ) );
    assertTrue( response.get( "parameters" ) instanceof JSONArray );
    assertEquals( 2, parameters.size() );
    JSONObject param1 = (JSONObject) parameters.get( 0 );
    assertEquals( "param1", param1.get( "variable" ) );
    assertEquals( "", param1.get( "field" ) );
    assertEquals( "default1", param1.get( "input" ) );
  }

  @Test
  public void testGetParametersFromTrans_throwsException() throws KettleException {
    when( transMeta.getParameterDescription( anyString() ) ).thenThrow( new UnknownParamException() );
    JSONObject response = underTest.stepAction( "parameters", transMeta, null );

    assertNotNull( response );
    assertEquals( FAILURE_RESPONSE, response.get( ACTION_STATUS ) );
  }

  @Test
  public void testIsTranValid_withValidTransformation() {
    JSONObject response = underTest.stepAction( "isTransValid", transMeta, null );

    assertNotNull( response ); assertTrue( response.containsKey( "transPresent" ) );
    assertTrue( response.get( "transPresent" ) instanceof Boolean );
  }

  @Test
  public void testIsTranValid_withInValidTransformation() throws Exception {
    doThrow( new KettleException( "Invalid transformation" ) ).when( underTest )
      .loadExecutorTransMeta( transMeta, transExecutorMeta );

    JSONObject response = underTest.stepAction( "isTransValid", transMeta, null );

    assertNotNull( response );
    assertFalse( (Boolean) response.get( "transPresent" ) );
    assertTrue( response.containsKey( "errorMessage" ) );
  }

  @Test public void testHandleStepAction_whenMethodNameIsInvalid() {
    JSONObject response = underTest.stepAction( "invalidMethod", transMeta, null );

    assertNotNull( response ); assertEquals( FAILURE_METHOD_NOT_FOUND_RESPONSE, response.get( ACTION_STATUS ) );
  }
}
