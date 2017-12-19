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

package org.pentaho.di.trans.step;

import org.junit.Test;
import org.pentaho.di.trans.step.errorhandling.Stream;
import org.pentaho.di.trans.step.errorhandling.StreamInterface;

import static org.junit.Assert.assertNull;

/**
 * @author Andrey Khayrutdinov
 */
public class StepIOMetaIT {

  @Test
  public void streamsListIsThreadsafe_NewObject() throws Exception {
    StepIOMeta meta = new StepIOMeta( true, false, false, false, false, false );
    checkStreamsListIsThreadSafe( meta );
  }

  @Test
  public void streamsListIsThreadsafe_ClonedObject() throws Exception {
    StepIOMeta meta = new StepIOMeta( true, false, false, false, false, false );
    checkStreamsListIsThreadSafe( meta.clone() );
  }

  private void checkStreamsListIsThreadSafe( final StepIOMeta meta ) throws Exception {
    final int cycles = 1000;
    StreamAdder adder1 = new StreamAdder( meta, cycles );
    StreamAdder adder2 = new StreamAdder( meta, cycles );

    Thread t1 = new Thread( adder1, "StepIOMetaIT_thread1" );
    Thread t2 = new Thread( adder2, "StepIOMetaIT_thread2" );

    t1.start();
    t2.start();

    t1.join();
    t2.join();

    assertNull( adder1.getException() );
    assertNull( adder2.getException() );
  }

  private static class StreamAdder implements Runnable {

    private final StepIOMeta meta;
    private final int cycles;
    private Exception exception;

    public StreamAdder( StepIOMeta meta, int cycles ) {
      this.meta = meta;
      this.cycles = cycles;
      this.exception = null;
    }

    @Override
    public void run() {
      try {
        for ( int i = 0; i < cycles; i++ ) {
          Stream stream = new Stream( StreamInterface.StreamType.INPUT, null, null, null, null );
          meta.addStream( stream );
        }
      } catch ( Exception e ) {
        this.exception = e;
      }
    }

    public Exception getException() {
      return exception;
    }
  }
}
