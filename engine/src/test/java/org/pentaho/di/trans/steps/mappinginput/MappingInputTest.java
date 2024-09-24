/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2024 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.trans.steps.mappinginput;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.trans.step.StepErrorMeta;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.steps.mock.StepMockHelper;
import org.pentaho.di.trans.steps.validator.Validator;
import org.pentaho.di.trans.steps.validator.ValidatorData;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * User: Dzmitry Stsiapanau Date: 12/24/13 Time: 12:45 PM
 */
public class MappingInputTest {
  private final String stepName = "MAPPING INPUT";
  private StepMockHelper<MappingInputMeta, MappingInputData> stepMockHelper;
  private volatile boolean processRowEnded;

  @Before
  public void setUp() throws Exception {
    stepMockHelper =
      new StepMockHelper<>( stepName, MappingInputMeta.class,
        MappingInputData.class );
    when( stepMockHelper.logChannelInterfaceFactory.create( any(), any( LoggingObjectInterface.class ) ) ).thenReturn(
      stepMockHelper.logChannelInterface );
    // when( stepMockHelper.trans.isRunning() ).thenReturn( true );
    setProcessRowEnded( false );
  }

  @After
  public void tearDown() throws Exception {
    stepMockHelper.cleanUp();
  }

  @Test
  public void testSetConnectorSteps() {

    when( stepMockHelper.transMeta.getSizeRowset() ).thenReturn( 1 );
    MappingInputData mappingInputData = new MappingInputData();
    MappingInput mappingInput =
      new MappingInput( stepMockHelper.stepMeta, mappingInputData,
        0, stepMockHelper.transMeta, stepMockHelper.trans );
    mappingInput.init( stepMockHelper.initStepMetaInterface, mappingInputData );
    ValidatorData validatorData = new ValidatorData();
    Validator previousStep =
      new Validator( stepMockHelper.stepMeta, validatorData, 0, stepMockHelper.transMeta, stepMockHelper.trans );
    when( stepMockHelper.stepMeta.isDoingErrorHandling() ).thenReturn( true );
    StepErrorMeta stepErrorMeta = mock( StepErrorMeta.class );
    when( stepErrorMeta.getTargetStep() ).thenReturn( stepMockHelper.stepMeta );
    when( stepMockHelper.stepMeta.getName() ).thenReturn( stepName );
    when( stepMockHelper.stepMeta.getStepErrorMeta() ).thenReturn( stepErrorMeta );

    StepInterface[] si = new StepInterface[] { previousStep };
    mappingInput.setConnectorSteps( si, Collections.emptyList(), stepName );
    assertEquals( previousStep.getOutputRowSets().size(), 0 );

  }

  @Test
  public void testSetConnectorStepsWithNullArguments() throws Exception {
    try {
      final MappingInputData mappingInputData = new MappingInputData();
      final MappingInput mappingInput =
        new MappingInput( stepMockHelper.stepMeta, mappingInputData, 0, stepMockHelper.transMeta,
          stepMockHelper.trans );
      mappingInput.init( stepMockHelper.initStepMetaInterface, mappingInputData );
      int timeOut = 1000;
      final int junitMaxTimeOut = 40000;
      mappingInput.setTimeOut( timeOut );
      final MappingInputTest mit = this;
      final Thread processRow = new Thread( () -> {
        try {
          mappingInput.processRow( stepMockHelper.initStepMetaInterface, mappingInputData );
          mit.setProcessRowEnded( true );
        } catch ( KettleException e ) {
          mit.setProcessRowEnded( true );
        }
      } );
      processRow.start();
      boolean exception = false;
      try {
        mappingInput.setConnectorSteps( null, Collections.emptyList(), "" );
      } catch ( IllegalArgumentException ex1 ) {
        try {
          mappingInput.setConnectorSteps( new StepInterface[ 0 ], null, "" );
        } catch ( IllegalArgumentException ex3 ) {
          try {
            mappingInput.setConnectorSteps( new StepInterface[] { mock( StepInterface.class ) },
              Collections.emptyList(), null );
          } catch ( IllegalArgumentException ignored ) {
            exception = true;
          }
        }

      }
      processRow.join( junitMaxTimeOut );
      assertTrue( "not enough IllegalArgumentExceptions", exception );
      assertTrue( "Process wasn`t stopped at null", isProcessRowEnded() );
    } catch ( NullPointerException npe ) {
      fail( "Null values are not suitable" );
    }
  }

  public void setProcessRowEnded( boolean processRowEnded ) {
    this.processRowEnded = processRowEnded;
  }

  public boolean isProcessRowEnded() {
    return processRowEnded;
  }
}
