/*
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2016 by Pentaho : http://www.pentaho.com
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


import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.KettleClientEnvironment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

public class LoggingBufferTest {

  @After
  public void runAfterTestMethod() {
    KettleClientEnvironment.getInstance().setClient( null );
  }

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
    for ( int i = 1; i <= 20; i++ ) {
      buff.addLogggingEvent(
        new KettleLoggingEvent( "Test #" + i + Const.CR + "Hello World!", Long.valueOf( i ), LogLevel.DETAILED ) );
    }
    Assert.assertEquals( 10, buff.getNrLines() );

    // Check remaining records, confirm that they are the proper records
    int i = 11;
    Iterator<BufferLine> it = buff.getBufferIterator();
    Assert.assertNotNull( it );
    while ( it.hasNext() ) {
      BufferLine bl = it.next();
      Assert.assertNotNull( bl.getEvent() );
      Assert.assertEquals( "Test #" + i + Const.CR + "Hello World!", bl.getEvent().getMessage() );
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
  public void testBufferTreadManagementForSpoon() {
    // set kettle client env to SPOON, means that code runs from Spoon
    KettleClientEnvironment.getInstance().setClient( KettleClientEnvironment.ClientType.SPOON );

    final LoggingBuffer loggingBuffer = new LoggingBuffer( 200 );
    Assert.assertNotNull( loggingBuffer );

    // wrote log messages for verification
    Map<String, LogMessage> wroteLogMessagesForVerification = new HashMap<>();
    // log channel ids for reading and removing
    List<String> logChannelIds = new ArrayList<>();
    // add logs to buffer
    final int logsCount = 100;
    for ( int i = 0; i < logsCount; i++ ) {
      // create log
      String logChannelId = "logId" + "_" + i;
      logChannelIds.add( logChannelId );
      String message = "test message" + "_" + i;
      LogMessage logMessage = new LogMessage( message, logChannelId, LogLevel.DETAILED );
      wroteLogMessagesForVerification.put( logMessage.getLogChannelId(), logMessage );
      KettleLoggingEvent loggingEvent =
        new KettleLoggingEvent( logMessage, System.currentTimeMillis(), LogLevel.DETAILED );
      // add log to buffer
      loggingBuffer.addLogggingEvent( loggingEvent );
    }

    // job/trans discard lines emulation
    List<Thread> discardLinesTreads = new ArrayList<>();
    for ( String logChannelId : logChannelIds ) {
      final String logChannelIdToRemove = logChannelId;
      // names the thread to prioritize
      Thread tread = new Thread( Const.JOB_EXECUTOR_DISCARD_LINES_THREAD ) {
        @Override public void run() {
          // remove log from buffer (in fact, will have to wait until the log is read by sniffer )
          loggingBuffer.removeChannelFromBuffer( logChannelIdToRemove );
        }
      };
      tread.start();
      discardLinesTreads.add( tread );
    }

    // read log messages for verification
    Map<String, LogMessage> readLogMessagesForVerification = new HashMap<>();
    // spoon log tab refresh thread emulation
    // names the thread to prioritize
    Thread snifferTread = new Thread( Const.KETTLE_LOG_TAB_REFRESH_THREAD ) {
      public void run() {
        // read buffer
        int lastNr = loggingBuffer.getLastBufferLineNr();
        List<KettleLoggingEvent> logLines = loggingBuffer.getLogBufferFromTo( logChannelIds, true, -1, lastNr );
        for ( KettleLoggingEvent logLine : logLines ) {
          LogMessage logMessage = (LogMessage) logLine.getMessage();
          readLogMessagesForVerification.put( logMessage.getLogChannelId(), logMessage );
        }
      }
    };
    snifferTread.start();

    try {
      // wait until sniffer tread finishes working
      snifferTread.join();
      // wait until discard lines treads finish working
      for ( Thread tread : discardLinesTreads ) {
        tread.join();
      }
    } catch ( InterruptedException e ) {
      e.printStackTrace();
    }

    // verification
    // buffer is empty (discard lines logic is working)
    Assert.assertEquals( 0, loggingBuffer.getNrLines() );
    // compare wrote and read logs (have to be equal)
    Assert.assertEquals( true, isLogMessagesEqual( wroteLogMessagesForVerification, readLogMessagesForVerification ) );
  }

  private boolean isLogMessagesEqual( Map<String, LogMessage> wroteLogMessagesForVerification,
                                      Map<String, LogMessage> readLogMessagesForVerification ) {
    if ( wroteLogMessagesForVerification.size() != readLogMessagesForVerification.size() ) {
      return false;
    }
    for ( Map.Entry<String, LogMessage> entry : wroteLogMessagesForVerification.entrySet() ) {
      String logChannelId = entry.getKey();
      if ( !readLogMessagesForVerification.containsKey( logChannelId ) ) {
        return false;
      }
      LogMessage wroteLogMessage = wroteLogMessagesForVerification.get( logChannelId );
      LogMessage readLogMessage = readLogMessagesForVerification.get( logChannelId );

      if ( !wroteLogMessage.getMessage().equals( readLogMessage.getMessage() ) ) {
        return false;
      }
    }
    return true;
  }
}
