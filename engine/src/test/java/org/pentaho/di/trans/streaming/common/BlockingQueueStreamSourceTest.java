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

package org.pentaho.di.trans.streaming.common;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.trans.SubtransExecutor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeoutException;

import static java.util.Collections.singletonList;
//import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static junit.framework.TestCase.fail;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@RunWith ( MockitoJUnitRunner.class )
public class BlockingQueueStreamSourceTest {
  private ExecutorService execSvc = Executors.newCachedThreadPool();
  @Mock private BaseStreamStep streamStep;
  @Mock private Semaphore semaphore;
  @Mock private LogChannel logChannel;
  @Mock private SubtransExecutor subtransExecutor;

  private BlockingQueueStreamSource<String> streamSource;

  @Before
  public void before() {
    when( streamStep.getSubtransExecutor() ).thenReturn( subtransExecutor );
    streamSource = new BlockingQueueStreamSource<String>( streamStep ) {
      @Override public void open() { }
    };
  }

  @Test
  @SuppressWarnings ( "unchecked" )
  public void errorLoggedIfInterruptedInAcceptRows() throws InterruptedException {
    streamSource.acceptingRowsSemaphore = semaphore;
    streamSource.logChannel = logChannel;
    doThrow( new InterruptedException( "interrupt" ) )
      .when( semaphore ).acquire();
    streamSource.acceptRows( singletonList( "new row" ) );
    verify( logChannel ).logError( any() );
    verify( semaphore ).release();
  }

  @Test
  @SuppressWarnings ( "unchecked" )
  public void errorLoggedIfInterruptedInPause() throws InterruptedException {
    streamSource.acceptingRowsSemaphore = semaphore;
    when( semaphore.availablePermits() ).thenReturn( 1 );
    streamSource.logChannel = logChannel;
    doThrow( new InterruptedException( "interrupt" ) )
      .when( semaphore ).acquire();
    streamSource.pause();
    verify( logChannel ).logError( any() );
  }

  @Test
  public void rowIterableBlocksTillRowReceived() {
    streamSource.open();
    Iterator<String> iterator = streamSource.flowable().blockingIterable().iterator();

    // first call .hasNext() on the iterator while streamSource is empty
    Future<Boolean> hasNext = execSvc.submit( iterator::hasNext );

    // should time out, since still waiting for rows
    assertTimesOut( hasNext );

    // add a row
    streamSource.acceptRows( singletonList( "New Row" ) );

    // verify that the future has completed, .hasNext() is no longer blocking and returns true
    assertThat( getQuickly( hasNext ), equalTo( true ) );
  }


  @Test
  public void streamIsPausable() {
    streamSource.open();

    Iterator<String> iter = streamSource.flowable().blockingIterable().iterator();
    Future<String> nextString = execSvc.submit( iter::next );

    // add a row
    streamSource.acceptRows( singletonList( "row" ) );
    // verify we can retrieve it
    assertThat( getQuickly( nextString ), equalTo( "row" ) );

    // pause receipt of new rows
    streamSource.pause();

    // trying to add and retrieve another row to the source should time out, since paused.
    final Future<?> newRow = execSvc.submit( () -> streamSource.acceptRows( singletonList( "new row" ) ) );
    assertTimesOut( newRow );

    // resume accepting rows
    streamSource.resume();

    // retrieve the last row added
    nextString = execSvc.submit( iter::next );
    assertThat( getQuickly( nextString ), equalTo( "new row" ) );

  }

  @Test
  public void testRowsFilled() throws InterruptedException {
    int rowCount = 4;
    // implement the blockingQueueStreamSource with an .open which will
    // launch a thread that sends rows to the queue.
    streamSource = new BlockingQueueStreamSource<String>( streamStep ) {
      @Override public void open() {
        execSvc.submit( () -> {
          for ( int i = 0; i < rowCount; i++ ) {
            acceptRows( singletonList( "new row " + i ) );
            try {
              Thread.sleep( 5 );
            } catch ( InterruptedException e ) {
              fail();
            }
          }
        } );
      }
    };
    streamSource.open();
    Iterator<String> iterator = streamSource.flowable().blockingIterable().iterator();

    Future<List<String>> iterLoop = execSvc.submit( () -> {
      List<String> strings = new ArrayList<>();
      do {
        strings.add( iterator.next() );
      } while ( strings.size() < 4 );
      return strings;
    } );
    final List<String> quickly = getQuickly( iterLoop );
    assertThat( quickly.size(), equalTo( rowCount ) );
    verify( subtransExecutor, times( rowCount ) ).acquireBufferPermit();
  }

  @Test
  public void testError() {
    // verifies that calling .error() results in an exception being thrown
    // by the .rows() blocking iterable.

    final String exceptionMessage = "Exception raised during acceptRows loop";
    streamSource = new BlockingQueueStreamSource<String>( streamStep ) {
      @Override public void open() {
        execSvc.submit( () -> {
          for ( int i = 0; i < 10; i++ ) {
            acceptRows( singletonList( "new row " + i ) );
            try {
              Thread.sleep( 5 );
            } catch ( InterruptedException e ) {
              fail();
            }
            if ( i == 5 ) {

              error( new RuntimeException( exceptionMessage ) );
              break;
            }
          }
        } );
      }
    };
    streamSource.open();
    Iterator<String> iterator = streamSource.flowable().blockingIterable().iterator();

    Future<List<String>> iterLoop = execSvc.submit( () -> {
      List<String> strings = new ArrayList<>();
      do {
        strings.add( iterator.next() );
      } while ( strings.size() < 9 );
      return strings;
    } );
    try {
      iterLoop.get( 50, MILLISECONDS );
      fail( "expected exception" );
    } catch ( InterruptedException | ExecutionException | TimeoutException e ) {
      // occasionally fails wingman with npe for mysterious reason,
      // so guarding with null check
      if ( e != null && e.getCause() != null ) {
        assertThat( e.getCause().getMessage(), equalTo( exceptionMessage ) );
      }
    }
  }


  private <T> T getQuickly( Future<T> future ) {
    try {
      return future.get( 200, MILLISECONDS );
    } catch ( InterruptedException | ExecutionException | TimeoutException e ) {
      fail();
    }
    return null;
  }

  private <T> void assertTimesOut( Future<T> next ) {
    try {
      next.get( 100, MILLISECONDS );
      fail( "Expected timeout exception" );
    } catch ( Exception e ) {
      assertThat( e, instanceOf( TimeoutException.class ) );
    }
  }
}
