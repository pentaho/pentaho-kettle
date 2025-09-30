/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2025 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 *
 ******************************************************************************/

package org.pentaho.di.trans.step.jms;

import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.trans.StepWithMappingMeta;
import org.pentaho.di.trans.TransMeta;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.pentaho.di.trans.step.BaseStepHelper.IS_VALID_REFERENCE;
import static org.pentaho.di.trans.step.StepHelperInterface.ACTION_STATUS;
import static org.pentaho.di.trans.step.StepHelperInterface.FAILURE_METHOD_NOT_FOUND_RESPONSE;
import static org.pentaho.di.trans.step.StepHelperInterface.SUCCESS_RESPONSE;
import static org.pentaho.di.trans.step.jms.JmsConsumerHelper.JMS_REFERENCE_PATH;

public class JmsConsumerHelperTest {

  JmsConsumerMeta jmsConsumerMeta;
  JmsConsumerHelper jmsConsumerHelper;
  TransMeta transMeta;

  @Before
  public void setUp() {
    transMeta = mock( TransMeta.class );
    jmsConsumerMeta = mock( JmsConsumerMeta.class );
    jmsConsumerHelper = new JmsConsumerHelper( jmsConsumerMeta );

    when( transMeta.environmentSubstitute( anyString() ) ).thenAnswer( invocation -> invocation.getArgument( 0 ) );
    when( jmsConsumerMeta.getTransformationPath() ).thenReturn( "/path/transFile.ktr" );
  }
  @Test
  public void testReferencePath() {
    try ( MockedStatic<StepWithMappingMeta> mappingMetaMockedStatic = mockStatic( StepWithMappingMeta.class ) ) {
      mappingMetaMockedStatic.when( () -> StepWithMappingMeta.loadMappingMeta( any(), any(), any(), any(), any(), anyBoolean() ) )
          .thenReturn( mock( TransMeta.class ) );
      JSONObject response = jmsConsumerHelper.stepAction( JMS_REFERENCE_PATH, transMeta, null );

      assertEquals( SUCCESS_RESPONSE, response.get( ACTION_STATUS ) );
      assertNotNull( response );
      assertNotNull( response.get( JMS_REFERENCE_PATH ) );
      assertEquals( "/path/transFile.ktr", response.get( JMS_REFERENCE_PATH ) );
      assertEquals( true, response.get( IS_VALID_REFERENCE ) );
    }
  }

  @Test
  public void testReferencePath_throwsException() {
    try ( MockedStatic<StepWithMappingMeta> mappingMetaMockedStatic = mockStatic( StepWithMappingMeta.class ) ) {
      mappingMetaMockedStatic.when( () -> StepWithMappingMeta.loadMappingMeta( any(), any(), any(), any(), any(), anyBoolean() )  )
          .thenThrow( new KettleException( "invalid Trans" ) );
      JSONObject response = jmsConsumerHelper.stepAction( JMS_REFERENCE_PATH, transMeta, null );

      assertEquals( SUCCESS_RESPONSE, response.get( ACTION_STATUS ) );
      assertNotNull( response );
      assertNotNull( response.get( IS_VALID_REFERENCE ) );
      assertEquals( false, response.get( IS_VALID_REFERENCE ) );
    }
  }

  @Test
  public void testHandleStepAction_whenMethodNameIsInvalid() {
    JSONObject response = jmsConsumerHelper.stepAction( "invalidMethod", transMeta, null );

    assertNotNull( response );
    assertEquals( FAILURE_METHOD_NOT_FOUND_RESPONSE, response.get( ACTION_STATUS ) );
  }
}
