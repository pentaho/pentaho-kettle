/*
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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


import org.junit.Assert;

import org.junit.Test;
import org.pentaho.di.core.Const;

import java.util.Iterator;
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

  @Test
  public void testBufferSizeRestrictions() {
    final LoggingBuffer buff = new LoggingBuffer( 10 );

    Assert.assertEquals( 10, buff.getMaxNrLines() );
    Assert.assertEquals( 0, buff.getLastBufferLineNr() );
    Assert.assertEquals( 0, buff.getNrLines() );

    // Load 20 records.  Only last 10 should be kept
    LogMessage message = null;
    for ( int i = 1; i <= 20; i++ ) {
      message = new LogMessage( "Test #" + i + Const.CR + "Hello World!", String.valueOf( i ), LogLevel.DETAILED );
      buff.addLogggingEvent(
        new KettleLoggingEvent( message, Long.valueOf( i ), LogLevel.DETAILED ) );
    }
    Assert.assertEquals( 10, buff.getNrLines() );

    // Check remaining records, confirm that they are the proper records
    int i = 11;
    Iterator<BufferLine> it = buff.getBufferIterator();
    Assert.assertNotNull( it );
    while ( it.hasNext() ) {
      BufferLine bl = it.next();
      Assert.assertNotNull( bl.getEvent() );
      Assert.assertEquals( "Test #" + i + Const.CR + "Hello World!", ( (LogMessage) bl.getEvent().getMessage() ).getMessage() );
      Assert.assertEquals( Long.valueOf( i ).longValue(), bl.getEvent().getTimeStamp() );
      Assert.assertEquals( LogLevel.DETAILED, bl.getEvent().getLevel() );
      i++;
    }
    Assert.assertEquals( i, 21 ); // Confirm that only 10 lines were iterated over

    Assert.assertEquals( 0, buff.getBufferLinesBefore( 10L ).size() );
    Assert.assertEquals( 5, buff.getBufferLinesBefore( 16L ).size() );
    Assert.assertEquals( 10, buff.getBufferLinesBefore( System.currentTimeMillis() ).size() );

    buff.clear();
    Assert.assertEquals( 0, buff.getNrLines() );
    it = buff.getBufferIterator();
    Assert.assertNotNull( it );
    while ( it.hasNext() ) {
      Assert.fail( "This should never be reached, as the LogBuffer is empty" );
    }
  }

  @Test
  public void testRemoveBufferLinesBefore() {
    LoggingBuffer loggingBuffer = new LoggingBuffer( 100 );
    for ( int i = 0; i < 40; i++ ) {
      KettleLoggingEvent event = new KettleLoggingEvent();
      event.setMessage( new LogMessage( "test", LogLevel.BASIC ) );
      event.setTimeStamp( i );
      loggingBuffer.addLogggingEvent( event );
    }
    loggingBuffer.removeBufferLinesBefore( 20 );
    Assert.assertEquals( 20, loggingBuffer.size() );
  }

}
