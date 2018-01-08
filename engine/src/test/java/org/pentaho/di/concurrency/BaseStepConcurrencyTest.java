/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.concurrency;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.RowListener;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepPartitioningMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BaseStepConcurrencyTest {
  private static final String STEP_META = "StepMeta";

  private BaseStep baseStep;

  @Before public void setup() throws Exception {
    StepMeta stepMeta = mock( StepMeta.class );
    TransMeta transMeta = mock( TransMeta.class );
    when( stepMeta.getName() ).thenReturn( STEP_META );
    when( transMeta.findStep( STEP_META ) ).thenReturn( stepMeta );
    when( stepMeta.getTargetStepPartitioningMeta() ).thenReturn( mock( StepPartitioningMeta.class ) );

    baseStep = new BaseStep( stepMeta, null, 0, transMeta, mock( Trans.class ) );
  }

  /**
   * Row listeners collection modifiers are exposed out of BaseStep class,
   * whereas the collection traversal is happening on every row being processed.
   * <p>
   * We should be sure that modification of the collection will not throw a concurrent modification exception.
   */
  @Test public void testRowListeners() throws Exception {
    int modifiersAmount = 100;
    int traversersAmount = 100;

    AtomicBoolean condition = new AtomicBoolean( true );

    List<Modifier> modifiers = new ArrayList<>();
    for ( int i = 0; i < modifiersAmount; i++ ) {
      modifiers.add( new Modifier( condition ) );
    }
    List<Traverser> traversers = new ArrayList<>();
    for ( int i = 0; i < traversersAmount; i++ ) {
      traversers.add( new Traverser( condition ) );
    }

    ConcurrencyTestRunner<?, ?> runner = new ConcurrencyTestRunner<Object, Object>( modifiers, traversers, condition );
    runner.runConcurrentTest();

    runner.checkNoExceptionRaised();
  }

  private class Modifier extends StopOnErrorCallable<BaseStep> {
    Modifier( AtomicBoolean condition ) {
      super( condition );
    }

    @Override BaseStep doCall() throws Exception {
      baseStep.addRowListener( mock( RowListener.class ) );
      return null;
    }
  }

  @Test public void testAtomicStatistics() throws Exception {
    int modifiersAmount = 100;
    int readerAmount = 100;
    AtomicBoolean condition = new AtomicBoolean( true );
    List<StatusCounterModify> modifiers = new ArrayList( modifiersAmount );
    List<StatusCounterReader> readers = new ArrayList( readerAmount );
    for ( int i = 0; i < modifiersAmount; i++ ) {
      modifiers.add( new StatusCounterModify( condition ) );
    }
    for ( int i = 0; i < readerAmount; i++ ) {
      readers.add( new StatusCounterReader( condition ) );
    }

    ConcurrencyTestRunner<?, ?> runner = new ConcurrencyTestRunner<Object, Object>( modifiers, readers, condition );
    runner.runConcurrentTest();
    runner.checkNoExceptionRaised();

    // Lines read should be ( 100 x 100 ) or 10,000
    Assert.assertEquals( 10000, baseStep.getLinesRead() );
    Assert.assertEquals( 10000, baseStep.getLinesOutput() );
  }

  private class Traverser extends StopOnErrorCallable<BaseStep> {
    Traverser( AtomicBoolean condition ) {
      super( condition );
    }

    @Override BaseStep doCall() throws Exception {
      for ( RowListener rowListener : baseStep.getRowListeners() ) {
        rowListener.rowWrittenEvent( mock( RowMetaInterface.class ), new Object[] {} );
      }
      return null;
    }
  }

  private class StatusCounterModify extends StopOnErrorCallable<BaseStep> {
    StatusCounterModify( AtomicBoolean condition ) {
      super( condition );
    }

    @Override BaseStep doCall() throws Exception {
      int cycles = 0;
      Random rand = new Random();
      while ( ( cycles < 100 ) && ( condition.get() ) ) {
        cycles++;
        baseStep.incrementLinesRead();
        baseStep.incrementLinesOutput();
        Thread.sleep( rand.nextInt( 200 ) );
      }
      return null;
    }
  }

  private class StatusCounterReader extends StopOnErrorCallable<BaseStep> {
    StatusCounterReader( AtomicBoolean condition ) {
      super( condition );
    }

    @Override BaseStep doCall() throws Exception {
      int cycles = 0;
      Random rand = new Random();
      while ( ( cycles < 100 ) && ( condition.get() ) ) {
        cycles++;
        baseStep.getLinesRead();
        baseStep.getLinesOutput();
        Thread.sleep( rand.nextInt( 200 ) );
      }
      return null;
    }
  }

}
