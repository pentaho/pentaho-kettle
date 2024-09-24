/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.di.concurrency;

import org.junit.Test;
import org.pentaho.di.core.logging.KettleLoggingEvent;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.core.logging.LogMessage;
import org.pentaho.di.core.logging.LoggingBuffer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class LoggingBufferConcurrencyTest {

  private LoggingBuffer buffer;

  @Test
  public void shouldNotFailProcessingEventsUnderHighContention() throws Exception {
    int modifiersAmount = 100;
    int readersAmount = 100;

    buffer = new LoggingBuffer( 5000 );

    AtomicBoolean condition = new AtomicBoolean( true );

    List<StopOnErrorCallable<?>> modifiers = new ArrayList<>();
    for ( int i = 0; i < modifiersAmount; i++ ) {
      modifiers.add( new Appender( condition ) );
    }
    List<StopOnErrorCallable<?>> readers = new ArrayList<>();
    for ( int i = 0; i < readersAmount; i++ ) {
      readers.add( new Reader( condition ) );
    }

    ConcurrencyTestRunner<?, ?> runner =
      new ConcurrencyTestRunner<>( modifiers, readers, condition, 5000 );
    runner.runConcurrentTest();
    runner.checkNoExceptionRaised();
  }

  private class Appender extends StopOnErrorCallable<Void> {
    Appender( AtomicBoolean condition ) {
      super( condition );
    }

    @Override
    Void doCall() {
      for ( int i = 0; i < 5000; i++ ) {
        buffer.addLogggingEvent( new KettleLoggingEvent(
          new LogMessage( "subject", LogLevel.DEBUG ), System.currentTimeMillis(), LogLevel.DEBUG ) );
      }
      return null;
    }
  }

  private class Reader extends StopOnErrorCallable<StringBuffer> {
    Reader( AtomicBoolean condition ) {
      super( condition );
    }

    @Override
    StringBuffer doCall() {
      return buffer.getBuffer();
    }
  }
}
