/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2015 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.trans.step;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.pentaho.di.core.BlockingRowSet;
import org.pentaho.di.core.ResultFile;
import org.pentaho.di.core.RowSet;
import org.pentaho.di.core.SingleRowRowSet;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.fileinput.NonAccessibleFileObject;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaBase;
import org.pentaho.di.core.row.value.ValueMetaInteger;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.trans.BasePartitioner;
import org.pentaho.di.trans.steps.mock.StepMockHelper;

public class BaseStepTest {
  private StepMockHelper<StepMetaInterface, StepDataInterface> mockHelper;

  @Before
  public void setup() {
    mockHelper =
        new StepMockHelper<StepMetaInterface, StepDataInterface>( "BASE STEP", StepMetaInterface.class,
            StepDataInterface.class );
  }

  @After
  public void tearDown() {
    mockHelper.cleanUp();
  }

  /**
   * This test checks that data from one non-partitioned step copies to 2 partitioned steps right.
   *
   * @see {@link <a href="http://jira.pentaho.com/browse/PDI-12211">http://jira.pentaho.com/browse/PDI-12211<a>}
   * @throws KettleException
   */
  @Test
  public void testBaseStepPutRowLocalSpecialPartitioning() throws KettleException {
    List<StepMeta> stepMetas = new ArrayList<StepMeta>();
    stepMetas.add( mockHelper.stepMeta );
    stepMetas.add( mockHelper.stepMeta );
    StepPartitioningMeta stepPartitioningMeta = spy( new StepPartitioningMeta() );
    BasePartitioner partitioner = mock( BasePartitioner.class );

    when( mockHelper.logChannelInterfaceFactory.create( any(), any( LoggingObjectInterface.class ) ) ).thenAnswer(
        new Answer<LogChannelInterface>() {

          @Override
          public LogChannelInterface answer( InvocationOnMock invocation ) throws Throwable {
            ( (BaseStep) invocation.getArguments()[0] ).getLogLevel();
            return mockHelper.logChannelInterface;
          }
        } );
    when( mockHelper.trans.isRunning() ).thenReturn( true );
    when( mockHelper.transMeta.findNextSteps( any( StepMeta.class ) ) ).thenReturn( stepMetas );
    when( mockHelper.stepMeta.getStepPartitioningMeta() ).thenReturn( stepPartitioningMeta );
    when( stepPartitioningMeta.getPartitioner() ).thenReturn( partitioner );
    when( partitioner.getNrPartitions() ).thenReturn( 2 );

    Object object0 = "name0";
    ValueMetaInterface meta0 = new ValueMetaBase( object0.toString() );

    Object object1 = "name1";
    ValueMetaInterface meta2 = new ValueMetaBase( object1.toString() );

    RowMetaInterface rowMeta0 = new RowMeta();
    rowMeta0.addValueMeta( meta0 );

    Object[] objects0 = { object0 };

    RowMetaInterface rowMeta1 = new RowMeta();
    rowMeta1.addValueMeta( meta2 );

    Object[] objects1 = { object1 };

    when( stepPartitioningMeta.getPartition( rowMeta0, objects0 ) ).thenReturn( 0 );
    when( stepPartitioningMeta.getPartition( rowMeta1, objects1 ) ).thenReturn( 1 );

    BlockingRowSet[] rowSet = { new BlockingRowSet( 2 ), new BlockingRowSet( 2 ), new BlockingRowSet( 2 ), new BlockingRowSet( 2 ) };
    List<RowSet> outputRowSets = new ArrayList<RowSet>();
    outputRowSets.addAll( Arrays.asList( rowSet ) );

    BaseStep baseStep =
        new BaseStep( mockHelper.stepMeta, mockHelper.stepDataInterface, 0, mockHelper.transMeta, mockHelper.trans );
    baseStep.setStopped( false );
    baseStep.setRepartitioning( StepPartitioningMeta.PARTITIONING_METHOD_SPECIAL );
    baseStep.setOutputRowSets( outputRowSets );
    baseStep.putRow( rowMeta0, objects0 );
    baseStep.putRow( rowMeta1, objects1 );

    assertEquals( object0, baseStep.getOutputRowSets().get( 0 ).getRow()[0] );
    assertEquals( object1, baseStep.getOutputRowSets().get( 1 ).getRow()[0] );
    assertEquals( object0, baseStep.getOutputRowSets().get( 2 ).getRow()[0] );
    assertEquals( object1, baseStep.getOutputRowSets().get( 3 ).getRow()[0] );
  }

  @Test
  public void testBaseStepGetLogLevelWontThrowNPEWithNullLog() {
    when( mockHelper.logChannelInterfaceFactory.create( any(), any( LoggingObjectInterface.class ) ) ).thenAnswer(
        new Answer<LogChannelInterface>() {

          @Override
          public LogChannelInterface answer( InvocationOnMock invocation ) throws Throwable {
            ( (BaseStep) invocation.getArguments()[0] ).getLogLevel();
            return mockHelper.logChannelInterface;
          }
        } );
    new BaseStep( mockHelper.stepMeta, mockHelper.stepDataInterface, 0, mockHelper.transMeta, mockHelper.trans )
        .getLogLevel();
  }

  @Test
  public void testStepListenersConcurrentModification() throws InterruptedException {
    // Create a base step
    when( mockHelper.logChannelInterfaceFactory.create( any(), any( LoggingObjectInterface.class ) ) ).thenReturn(
        mockHelper.logChannelInterface );
    final BaseStep baseStep =
        new BaseStep( mockHelper.stepMeta, mockHelper.stepDataInterface, 0, mockHelper.transMeta, mockHelper.trans );

    // Create thread to dynamically add listeners
    final AtomicBoolean done = new AtomicBoolean( false );
    Thread addListeners = new Thread() {
      @Override
      public void run() {
        while ( !done.get() ) {
          baseStep.addStepListener( mock( StepListener.class ) );
          synchronized ( done ) {
            done.notify();
          }
        }
      }
    };

    // Mark start and stop while listeners are being added
    try {
      addListeners.start();

      // Allow a few listeners to be added
      synchronized ( done ) {
        while ( baseStep.getStepListeners().size() < 20 ) {
          done.wait();
        }
      }

      baseStep.markStart();

      // Allow more listeners to be added
      synchronized ( done ) {
        while ( baseStep.getStepListeners().size() < 100 ) {
          done.wait();
        }
      }

      baseStep.markStop();

    } finally {
      // Close addListeners thread
      done.set( true );
      addListeners.join();
    }
  }

  @Test
  public void resultFilesMapIsSafeForConcurrentModification() throws Exception {
    when( mockHelper.logChannelInterfaceFactory.create( any(), any( LoggingObjectInterface.class ) ) ).thenReturn(
      mockHelper.logChannelInterface );

    final BaseStep step =
      new BaseStep( mockHelper.stepMeta, mockHelper.stepDataInterface, 0, mockHelper.transMeta, mockHelper.trans );

    final AtomicBoolean complete = new AtomicBoolean( false );

    final int FILES_AMOUNT = 10 * 1000;
    Thread filesProducer = new Thread( new Runnable() {
      @Override public void run() {
        try {
          for ( int i = 0; i < FILES_AMOUNT; i++ ) {
            step.addResultFile( new ResultFile( 0, new NonAccessibleFileObject( Integer.toString( i ) ), null, null ) );
            try {
              Thread.sleep( 1 );
            } catch ( Exception e ) {
              fail( e.getMessage() );
            }
          }
        } finally {
          complete.set( true );
        }
      }
    } );

    filesProducer.start();
    try {
      while ( !complete.get() ) {
        for ( Map.Entry<String, ResultFile> entry : step.getResultFiles().entrySet() ) {
          entry.getKey();
        }
      }
    } finally {
      filesProducer.join();
    }
  }

  @Test
  public void outputRowMetasAreNotSharedAmongSeveralStreams() throws Exception {
    RowSet rs1 = new SingleRowRowSet();
    RowSet rs2 = new SingleRowRowSet();

    when( mockHelper.logChannelInterfaceFactory.create( any(), any( LoggingObjectInterface.class ) ) ).thenReturn(
      mockHelper.logChannelInterface );
    when( mockHelper.trans.isRunning() ).thenReturn( true );
    BaseStep baseStep =
      new BaseStep( mockHelper.stepMeta, mockHelper.stepDataInterface, 0, mockHelper.transMeta, mockHelper.trans );
    baseStep.setStopped( false );
    baseStep.setRepartitioning( StepPartitioningMeta.PARTITIONING_METHOD_NONE );
    baseStep.setOutputRowSets( Arrays.asList( rs1, rs2 ) );

    for ( RowSet rowSet : baseStep.getOutputRowSets() ) {
      assertNull( "RowMeta should be null, since no calls were done", rowSet.getRowMeta() );
    }

    RowMetaInterface rowMeta = new RowMeta();
    rowMeta.addValueMeta( new ValueMetaString( "string" ) );
    rowMeta.addValueMeta( new ValueMetaInteger( "integer" ) );

    baseStep.putRow( rowMeta, new Object[] { "a", 1 } );

    RowMetaInterface meta1 = rs1.getRowMeta();
    RowMetaInterface meta2 = rs2.getRowMeta();
    assertNotNull( meta1 );
    assertNotNull( meta2 );
    // content is same
    for ( ValueMetaInterface meta : meta1.getValueMetaList() ) {
      assertTrue( meta.getName(), meta2.exists( meta ) );
    }
    // whereas instances differ
    assertFalse( meta1 == meta2 );
  }
}
