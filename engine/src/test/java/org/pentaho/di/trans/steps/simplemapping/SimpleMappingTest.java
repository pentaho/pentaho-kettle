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
package org.pentaho.di.trans.steps.simplemapping;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedConstruction;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.RowProducer;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.steps.mapping.MappingIODefinition;
import org.pentaho.di.trans.steps.mappinginput.MappingInput;
import org.pentaho.di.trans.steps.mappingoutput.MappingOutput;
import org.pentaho.di.trans.steps.mock.StepMockHelper;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Tatsiana_Kasiankova
 * 
 */
public class SimpleMappingTest {

  private static final String MAPPING_INPUT_STEP_NAME = "MAPPING_INPUT_STEP_NAME";

  private static final String MAPPING_OUTPUT_STEP_NAME = "MAPPING_OUTPUT_STEP_NAME";

  private StepMockHelper<SimpleMappingMeta, SimpleMappingData> stepMockHelper;

  // Using real SimpleMappingData object
  private SimpleMappingData simpleMpData = new SimpleMappingData();

  private SimpleMapping smp;

  @Before
  public void setup() throws Exception {
    stepMockHelper =
        new StepMockHelper<SimpleMappingMeta, SimpleMappingData>( "SIMPLE_MAPPING_TEST", SimpleMappingMeta.class,
            SimpleMappingData.class );
    when( stepMockHelper.logChannelInterfaceFactory.create( any(), any( LoggingObjectInterface.class ) ) ).thenReturn(
        stepMockHelper.logChannelInterface );
    when( stepMockHelper.trans.isRunning() ).thenReturn( true );

    // Mock for MappingInput
    MappingInput mpInputMock = mock( MappingInput.class );
    when( mpInputMock.getStepname() ).thenReturn( MAPPING_INPUT_STEP_NAME );

    // Mock for MappingOutput
    MappingOutput mpOutputMock = mock( MappingOutput.class );
    when( mpOutputMock.getStepname() ).thenReturn( MAPPING_OUTPUT_STEP_NAME );

    // Mock for RowDataInputMapper
    RowDataInputMapper rdInputMpMock = mock( RowDataInputMapper.class );
    RowMetaInterface rwMetaInMock = mock( RowMeta.class );
    doReturn( Boolean.TRUE ).when( rdInputMpMock ).putRow( rwMetaInMock, new Object[] { } );

    // Mock for RowProducer
    RowProducer rProducerMock = mock( RowProducer.class );
    when( rProducerMock.putRow( any( RowMetaInterface.class ), any( Object[].class ), anyBoolean() ) )
      .thenReturn( true );

    // Mock for MappingIODefinition
    MappingIODefinition mpIODefMock = mock( MappingIODefinition.class );

    // Set up real SimpleMappingData with some mocked elements
    simpleMpData.mappingInput = mpInputMock;
    simpleMpData.mappingOutput = mpOutputMock;
    simpleMpData.rowDataInputMapper = rdInputMpMock;
    simpleMpData.mappingTrans = stepMockHelper.trans;

    when( stepMockHelper.trans.findStepInterface( MAPPING_OUTPUT_STEP_NAME, 0 ) ).thenReturn( mpOutputMock );
    when( stepMockHelper.trans.addRowProducer( MAPPING_INPUT_STEP_NAME, 0 ) ).thenReturn( rProducerMock );
    when( stepMockHelper.processRowsStepMetaInterface.getInputMapping() ).thenReturn( mpIODefMock );
  }

  @After
  public void cleanUp() {
    stepMockHelper.cleanUp();
  }

  @Test
  public void testStepSetUpAsWasStarted_AtProcessingFirstRow() throws KettleException {
    try ( MockedConstruction<RowDataInputMapper> rowDataInputMapperMockedConstruction = mockConstruction(
      RowDataInputMapper.class, ( mock, context ) -> when( mock.putRow( any(), any() ) ).thenReturn( true ) ) ) {
      // RowDataInputMapper called rowMeta.clone, which dies when it gets run on a mock
      smp =
        new SimpleMapping( stepMockHelper.stepMeta, stepMockHelper.stepDataInterface, 0, stepMockHelper.transMeta,
          stepMockHelper.trans );
      smp.init( stepMockHelper.initStepMetaInterface, stepMockHelper.initStepDataInterface );
      smp.addRowSetToInputRowSets( stepMockHelper.getMockInputRowSet( new Object[] {} ) );
      assertTrue( "The step is processing in first", smp.first );
      assertTrue( smp.processRow( stepMockHelper.processRowsStepMetaInterface, simpleMpData ) );
      assertFalse( "The step is processing not in first", smp.first );
      assertTrue( "The step was started", smp.getData().wasStarted );
    }
  }

  @Test
  public void testStepShouldProcessError_WhenMappingTransHasError() throws KettleException {

    // Set Up TransMock to return the error
    int errorCount = 1;
    when( stepMockHelper.trans.getErrors() ).thenReturn( errorCount );

    // The step has been already finished
    when( stepMockHelper.trans.isFinished() ).thenReturn( Boolean.TRUE );
    // The step was started
    simpleMpData.wasStarted = true;

    smp =
        new SimpleMapping( stepMockHelper.stepMeta, stepMockHelper.stepDataInterface, 0, stepMockHelper.transMeta,
            stepMockHelper.trans );
    smp.init( stepMockHelper.initStepMetaInterface, simpleMpData );

    smp.dispose( stepMockHelper.processRowsStepMetaInterface, simpleMpData );
    verify( stepMockHelper.trans, times( 1 ) ).isFinished();
    verify( stepMockHelper.trans, never() ).waitUntilFinished();
    verify( stepMockHelper.trans, never() ).addActiveSubTransformation( anyString(), any( Trans.class ) );
    verify( stepMockHelper.trans, times( 1 ) ).removeActiveSubTransformation( anyString() );
    verify( stepMockHelper.trans, never() ).getActiveSubTransformation( anyString() );
    verify( stepMockHelper.trans, times( 1 ) ).getErrors();
    assertTrue( "The step contains the errors", smp.getErrors() == errorCount );

  }

  @Test
  public void testStepShouldStopProcessingInput_IfUnderlyingTransitionIsStopped() throws Exception {

    try ( MockedConstruction<RowDataInputMapper> rowDataInputMapperMockedConstruction = mockConstruction(
      RowDataInputMapper.class, ( mock, context ) -> when( mock.putRow( any(), any() ) ).thenReturn( true ) ) ) {
      // RowDataInputMapper called rowMeta.clone, which dies when it gets run on a mock
      MappingInput mappingInput = mock( MappingInput.class );
      when( mappingInput.getStepname() ).thenReturn( MAPPING_INPUT_STEP_NAME );
      stepMockHelper.processRowsStepDataInterface.mappingInput = mappingInput;

      RowProducer rowProducer = mock( RowProducer.class );
      when( rowProducer.putRow( any( RowMetaInterface.class ), any( Object[].class ), anyBoolean() ) )
        .thenReturn( true );

      StepInterface stepInterface = mock( StepInterface.class );

      Trans mappingTrans = mock( Trans.class );
      when( mappingTrans.addRowProducer( anyString(), anyInt() ) ).thenReturn( rowProducer );
      when( mappingTrans.findStepInterface( anyString(), anyInt() ) ).thenReturn( stepInterface );
      when( mappingTrans.isFinishedOrStopped() ).thenReturn( Boolean.FALSE ).thenReturn( Boolean.TRUE );
      stepMockHelper.processRowsStepDataInterface.mappingTrans = mappingTrans;

      MappingOutput mappingOutput = mock( MappingOutput.class );
      when( mappingOutput.getStepname() ).thenReturn( MAPPING_OUTPUT_STEP_NAME );
      stepMockHelper.processRowsStepDataInterface.mappingOutput = mappingOutput;


      smp = new SimpleMapping( stepMockHelper.stepMeta, stepMockHelper.stepDataInterface, 0, stepMockHelper.transMeta,
        stepMockHelper.trans );
      smp.init( stepMockHelper.initStepMetaInterface, simpleMpData );
      smp.addRowSetToInputRowSets( stepMockHelper.getMockInputRowSet( new Object[] {} ) );
      smp.addRowSetToInputRowSets( stepMockHelper.getMockInputRowSet( new Object[] {} ) );

      assertTrue(
        smp.processRow( stepMockHelper.processRowsStepMetaInterface, stepMockHelper.processRowsStepDataInterface ) );
      assertFalse(
        smp.processRow( stepMockHelper.processRowsStepMetaInterface, stepMockHelper.processRowsStepDataInterface ) );
    }
  }

  @After
  public void tearDown() {
    stepMockHelper.cleanUp();
  }

  @Test
  public void testDispose() throws KettleException {

    // Set Up TransMock to return the error
    when( stepMockHelper.trans.getErrors() ).thenReturn( 0 );

    // The step has been already finished
    when( stepMockHelper.trans.isFinished() ).thenReturn( Boolean.FALSE );
    // The step was started
    simpleMpData.wasStarted = true;

    smp =
            new SimpleMapping( stepMockHelper.stepMeta, stepMockHelper.stepDataInterface, 0, stepMockHelper.transMeta,
                    stepMockHelper.trans );
    smp.init( stepMockHelper.initStepMetaInterface, simpleMpData );

    smp.dispose( stepMockHelper.processRowsStepMetaInterface, simpleMpData );
    verify( stepMockHelper.trans, times( 1 ) ).isFinished();
    verify( stepMockHelper.trans, times( 1 ) ).waitUntilFinished();
    verify( stepMockHelper.trans, times( 1 ) ).removeActiveSubTransformation( anyString() );

  }

}
