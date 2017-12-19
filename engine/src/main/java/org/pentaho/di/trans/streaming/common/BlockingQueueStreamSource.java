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


package org.pentaho.di.trans.streaming.common;

import io.reactivex.Observable;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.ReplayProcessor;
import org.pentaho.di.trans.streaming.api.StreamSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class BlockingQueueStreamSource<T> implements StreamSource<T> {

  private final Logger logger = LoggerFactory.getLogger( getClass() );

  private final AtomicBoolean paused = new AtomicBoolean( false );

  private final FlowableProcessor<T> publishProcessor = ReplayProcessor.create();

  private final Semaphore acceptingRowsSemaphore = new Semaphore( 1 );
  private final ExecutorService execService = Executors.newSingleThreadExecutor();
  private Future<?> future;


  @Override public Iterable<T> rows() {
    return Observable
      .fromPublisher( publishProcessor )
      .blockingIterable();
  }

  @Override public void close() {
    if ( !publishProcessor.hasComplete() ) {
      publishProcessor.onComplete();
    }
  }

  @Override public void pause() {
    paused.set( true );
    if ( !acceptingRowsSemaphore.tryAcquire() ) {
      logger.info( "already paused" );
    }
  }

  @Override public void resume() {
    paused.set( false );
    acceptingRowsSemaphore.release();
  }


  /**
   * Accept rows, blocking if currently paused.
   *
   * Implementations should implement the open() function
   * to pass external row events to the acceptRows method.
   */
  protected void acceptRows( List<T> rows ) {
    try {
      acceptingRowsSemaphore.acquire();
      rows.stream()
        .forEach( ( row ) -> publishProcessor.onNext( row ) );
    } catch ( InterruptedException e ) {
      logger.error( "Interrupted while adding row  " + rows, e );
    } finally {
      acceptingRowsSemaphore.release();
    }
  }

  public void error( Throwable throwable ) {
    publishProcessor.onError( throwable );
  }

  protected boolean isPaused() {
    return paused.get();
  }
}
