/*
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2013 by Pentaho : http://www.pentaho.com
 *
 * **************************************************************************
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
 */

package org.pentaho.di.core.logging;


import junit.framework.Assert;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

public class LoggingBufferTest {

  @Test
  public void testRaceCondition() throws Exception {

    final int eventCount = 100;

    final LoggingBuffer buf = new LoggingBuffer( 200 );

    final AtomicBoolean done = new AtomicBoolean( false );

    final KettleLoggingEventListener lsnr = new KettleLoggingEventListener() {
      @Override public void eventAdded( KettleLoggingEvent event ) {
        //stub
      }
    };

    final KettleLoggingEvent event = new KettleLoggingEvent();

    final CountDownLatch latch = new CountDownLatch( 1 );

    Thread.UncaughtExceptionHandler errorHandler = new Thread.UncaughtExceptionHandler() {
      @Override public void uncaughtException( Thread t, Throwable e ) {
        e.printStackTrace();
      }
    };

    Thread addListeners = new Thread( new Runnable() {
      @Override public void run() {
        try {
          while ( !done.get() ) {
            buf.addLoggingEventListener( lsnr );
          }
        } finally {
          latch.countDown();
        }
      }
    }, "Add Listeners Thread" ) {

    };

    Thread addEvents = new Thread( new Runnable() {
      @Override public void run() {
        try {
          for ( int i = 0; i < eventCount; i++ ) {
            buf.addLogggingEvent( event );
          }
          done.set( true );
        } finally {
          latch.countDown();
        }
      }
    }, "Add Events Thread" ) {

    };

    // add error handlers to pass exceptions outside the thread
    addListeners.setUncaughtExceptionHandler( errorHandler );
    addEvents.setUncaughtExceptionHandler( errorHandler );

    // start
    addListeners.start();
    addEvents.start();

    // wait both
    latch.await();

    // check
    Assert.assertEquals( "Failed", true, done.get() );

  }
}
