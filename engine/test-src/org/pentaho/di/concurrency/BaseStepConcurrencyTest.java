/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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
import java.util.concurrent.atomic.AtomicBoolean;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BaseStepConcurrencyTest {
  private static final String STEP_META = "StepMeta";

  private BaseStep baseStep;

  /**
   * Row listeners collection modifiers are exposed out of BaseStep class,
   * whereas the collection traversal is happening on every row being processed.
   *
   * We should be sure that modification of the collection will not throw a concurrent modification exception.
   */
  @Test
  public void testRowListeners() throws Exception {
    int modifiersAmount = 100;
    int traversersAmount = 100;

    StepMeta stepMeta = mock( StepMeta.class );
    TransMeta transMeta = mock( TransMeta.class );
    when( stepMeta.getName() ).thenReturn( STEP_META );
    when( transMeta.findStep( STEP_META ) ).thenReturn( stepMeta );
    when( stepMeta.getTargetStepPartitioningMeta() ).thenReturn( mock( StepPartitioningMeta.class ) );

    baseStep = new BaseStep( stepMeta, null, 0, transMeta, mock( Trans.class ) );

    AtomicBoolean condition = new AtomicBoolean( true );

    List<Modifier> modifiers = new ArrayList<>();
    for ( int i = 0; i < modifiersAmount; i++ ) {
      modifiers.add( new Modifier( condition ) );
    }
    List<Traverser> traversers = new ArrayList<>();
    for ( int i = 0; i < traversersAmount; i++ ) {
      traversers.add( new Traverser( condition ) );
    }

    ConcurrencyTestRunner<?, ?> runner =
      new ConcurrencyTestRunner<Object, Object>( modifiers, traversers, condition );
    runner.runConcurrentTest();

    runner.checkNoExceptionRaised();
  }

  private class Modifier extends StopOnErrorCallable<BaseStep> {
    Modifier( AtomicBoolean condition ) {
      super( condition );
    }

    @Override
    BaseStep doCall() throws Exception {
      baseStep.addRowListener( mock( RowListener.class ) );
      return null;
    }
  }

  private class Traverser extends StopOnErrorCallable<BaseStep> {
    Traverser( AtomicBoolean condition ) {
      super( condition );
    }

    @Override
    BaseStep doCall() throws Exception {
      for ( RowListener rowListener : baseStep.getRowListeners() ) {
        rowListener.rowWrittenEvent( mock( RowMetaInterface.class ), new Object[]{} );
      }
      return null;
    }
  }
}
