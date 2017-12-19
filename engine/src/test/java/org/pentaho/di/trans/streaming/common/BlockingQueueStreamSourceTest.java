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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

import static java.util.Collections.singletonList;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static junit.framework.TestCase.fail;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith ( MockitoJUnitRunner.class )
public class BlockingQueueStreamSourceTest {
  private ExecutorService execSvc = Executors.newCachedThreadPool();
  private BlockingQueueStreamSource<String> streamSource = new BlockingQueueStreamSource<String>() {
    @Override public void open() {

    }
  };

  @Test
  public void rowIterableBlocksTillRowReceived() throws Exception {
    streamSource.open();
    Iterator<String> iterator = streamSource.rows().iterator();

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
  public void streamIsPausable() throws InterruptedException, ExecutionException, TimeoutException {
    streamSource.open();

    Iterator<String> iter = streamSource.rows().iterator();
    Future<String> nextString = execSvc.submit( iter::next );

    // add a row
    streamSource.acceptRows( singletonList( "row" ) );
    // verify we can retrieve it
    assertThat( getQuickly( nextString ), equalTo( "row" ) );

    // pause receipt of new rows
    streamSource.pause();

    // trying to add another row to the source should time out, since paused.
    assertTimesOut( execSvc.submit( () -> streamSource.acceptRows( singletonList( "new row" ) ) ) );

    // resume accepting rows
    streamSource.resume();

    // add a new row
    streamSource.acceptRows( singletonList( "new row after resume" ) );
    // try to retrieve it
    nextString = execSvc.submit( iter::next );
    assertThat( getQuickly( nextString ), equalTo( "new row after resume" ) );

  }

  @Test
  public void testRowsFilled() throws ExecutionException, InterruptedException {

    // implement the blockingQueueStreamSource with an .open which will
    // launch a thread that sends rows to the queue.
    streamSource = new BlockingQueueStreamSource<String>() {
      @Override public void open() {
        execSvc.submit( () -> {
          for ( int i = 0; i < 4; i++ ) {
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
    Iterator<String> iterator = streamSource.rows().iterator();

    Future<List<String>> iterLoop = execSvc.submit( () -> {
      List<String> strings = new ArrayList<>();
      do {
        strings.add( iterator.next() );
      } while ( strings.size() < 4 );
      return strings;
    } );
    final List<String> quickly = getQuickly( iterLoop );
    assertThat( quickly.size(), equalTo( 4 ) );
  }


  private <T> T getQuickly( Future<T> future ) {
    try {
      return future.get( 50, MILLISECONDS );
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
