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

import io.reactivex.Flowable;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.trans.SubtransExecutor;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.pentaho.di.i18n.BaseMessages.getString;

@RunWith( MockitoJUnitRunner.class )
public class FixedTimeStreamWindowTest {
  @Mock private SubtransExecutor subtransExecutor;

  @Test
  public void emptyResultShouldNotThrowException() throws KettleException {
    when( subtransExecutor.execute( any()  ) ).thenReturn( Optional.empty() );
    when( subtransExecutor.getPrefetchCount() ).thenReturn( 10 );
    RowMetaInterface rowMeta = new RowMeta();
    rowMeta.addValueMeta( new ValueMetaString( "field" ) );
    FixedTimeStreamWindow<List> window =
      new FixedTimeStreamWindow<>( subtransExecutor, rowMeta, 0, 2, 1 );
    window.buffer( Flowable.fromIterable( singletonList( asList( "v1", "v2" ) ) ) ).forEach( result -> { } );
  }

  @Test
  public void resultsComeBackToParent() throws KettleException {
    RowMetaInterface rowMeta = new RowMeta();
    rowMeta.addValueMeta( new ValueMetaString( "field" ) );
    Result mockResult = new Result();
    mockResult.setRows( Arrays.asList( new RowMetaAndData( rowMeta, "queen" ), new RowMetaAndData( rowMeta, "king" ) ) );
    when( subtransExecutor.execute( any()  ) ).thenReturn( Optional.of( mockResult ) );
    when( subtransExecutor.getPrefetchCount() ).thenReturn( 10 );
    FixedTimeStreamWindow<List> window =
      new FixedTimeStreamWindow<>( subtransExecutor, rowMeta, 0, 2, 1 );
    window.buffer( Flowable.fromIterable( singletonList( asList( "v1", "v2" ) ) ) )
      .forEach( result -> assertEquals( mockResult, result ) );
  }

  @Test
  public void abortedSubtransThrowsAnError() throws KettleException {
    Result result1 = new Result();
    result1.setNrErrors( 1 );
    when( subtransExecutor.execute( any()  ) ).thenReturn( Optional.of( result1 ) );
    when( subtransExecutor.getPrefetchCount() ).thenReturn( 10 );
    RowMetaInterface rowMeta = new RowMeta();
    rowMeta.addValueMeta( new ValueMetaString( "field" ) );
    FixedTimeStreamWindow<List> window =
      new FixedTimeStreamWindow<>( subtransExecutor, rowMeta, 0, 2, 1 );
    try {
      window.buffer( Flowable.fromIterable( singletonList( asList( "v1", "v2" ) ) ) ).forEach( result -> { } );
    } catch ( Exception e ) {
      assertEquals(
        getString( BaseStreamStep.class, "FixedTimeStreamWindow.SubtransFailed" ), e.getCause().getMessage().trim() );
    }
  }

  @Test
  public void testSharedStreamingBatchPoolInternalState() throws Exception {
    /*
    * Tests that the sharedStreamingBatchPool adjusts its core pool size based on the value being set
    * via the SHARED_STREAMING_BATCH_POOL_SIZE system property and that the sharedStreamingBatchPool
    * remains a singleton across FixedTimeStreamWindow instances.
    * */

    System.setProperty( Const.SHARED_STREAMING_BATCH_POOL_SIZE, "5" );
    FixedTimeStreamWindow<List> window1 =
        new FixedTimeStreamWindow<>( subtransExecutor, new RowMeta(), 0, 2, 1 );
    Field field1 = window1.getClass().getDeclaredField( "sharedStreamingBatchPool" );
    field1.setAccessible( true );
    ThreadPoolExecutor sharedStreamingBatchPool1 = (ThreadPoolExecutor) field1.get( window1 );
    assertEquals( 5, sharedStreamingBatchPool1.getCorePoolSize() );

    System.setProperty( Const.SHARED_STREAMING_BATCH_POOL_SIZE, "10" );
    FixedTimeStreamWindow<List> window2 =
        new FixedTimeStreamWindow<>( subtransExecutor, new RowMeta(), 0, 2, 1 );
    Field field2 = window2.getClass().getDeclaredField( "sharedStreamingBatchPool" );
    field2.setAccessible( true );
    ThreadPoolExecutor sharedStreamingBatchPool2 = (ThreadPoolExecutor) field2.get( window2 );
    assertEquals( 10, sharedStreamingBatchPool2.getCorePoolSize() );

    assertEquals( sharedStreamingBatchPool1, sharedStreamingBatchPool2 );
  }

  @Test
  public void testSharedStreamingBatchPoolExecution() throws Exception {
    /*
    * Tests that there is only 1 thread running inside the pool at all times.
    * */

    final List<String> errors = new ArrayList<>();

    // Only 1 thread should be present in the pool at a given time.
    System.setProperty( Const.SHARED_STREAMING_BATCH_POOL_SIZE, "1" );

    RowMetaInterface rowMeta = new RowMeta();
    rowMeta.addValueMeta( new ValueMetaString( "field" ) );
    Result mockResult = new Result();
    mockResult.setRows( Arrays.asList( new RowMetaAndData( rowMeta, "queen" ), new RowMetaAndData( rowMeta, "king" ) ) );

    FixedTimeStreamWindow<List> window1 = new FixedTimeStreamWindow<>( subtransExecutor, rowMeta, 0, 10, 10 );
    FixedTimeStreamWindow<List> window2 = new FixedTimeStreamWindow<>( subtransExecutor, rowMeta, 0, 10, 10 );
    Flowable flowable = Flowable.fromIterable( singletonList( asList( "v1", "v2" ) ) );

    Field field = window1.getClass().getDeclaredField( "sharedStreamingBatchPool" );
    field.setAccessible( true );
    ThreadPoolExecutor sharedStreamingBatchPool = (ThreadPoolExecutor) field.get( window1 );

    Thread bufferThread1 = new Thread( new BufferThread( window1, flowable, mockResult ) );
    bufferThread1.start();

    Thread bufferThread2 = new Thread( new BufferThread( window2, flowable, mockResult ) );
    bufferThread2.start();

    Thread.sleep( 10000 );
    assertEquals( 0, errors.size() );
  }

  private static class BufferThread implements Runnable {

    private final FixedTimeStreamWindow window;
    private final Flowable flowable;
    private final Result mockResult;

    public BufferThread( FixedTimeStreamWindow window, Flowable flowable, Result mockResult ) {
      this.window = window;
      this.flowable = flowable;
      this.mockResult = mockResult;
    }

    public void run() {
      window.buffer( flowable ).forEach( result -> assertEquals( mockResult, result ) );
    }
  }

  @Test
  public void supportsPostProcessing() throws KettleException {
    RowMetaInterface rowMeta = new RowMeta();
    rowMeta.addValueMeta( new ValueMetaString( "field" ) );
    Result mockResult = new Result();
    mockResult.setRows( Arrays.asList( new RowMetaAndData( rowMeta, "queen" ), new RowMetaAndData( rowMeta, "king" ) ) );
    when( subtransExecutor.execute( any()  ) ).thenReturn( Optional.of( mockResult ) );
    when( subtransExecutor.getPrefetchCount() ).thenReturn( 10 );
    AtomicInteger count = new AtomicInteger();
    FixedTimeStreamWindow<List> window =
      new FixedTimeStreamWindow<>( subtransExecutor, rowMeta, 0, 2, 1, ( p ) -> count.set( p.getKey().get( 0 ).size() ) );
    window.buffer( Flowable.fromIterable( singletonList( asList( "v1", "v2" ) ) ) )
      .forEach( result -> assertEquals( mockResult, result ) );
    assertEquals( 2, count.get() );
  }

  @Test
  public void emptyResultsNotPostProcessed() throws KettleException {
    RowMetaInterface rowMeta = new RowMeta();
    rowMeta.addValueMeta( new ValueMetaString( "field" ) );
    Result mockResult = new Result();
    mockResult.setRows( Arrays.asList( new RowMetaAndData( rowMeta, "queen" ), new RowMetaAndData( rowMeta, "king" ) ) );
    when( subtransExecutor.execute( any()  ) ).thenReturn( Optional.empty() );
    when( subtransExecutor.getPrefetchCount() ).thenReturn( 10 );
    AtomicInteger count = new AtomicInteger();
    FixedTimeStreamWindow<List> window =
      new FixedTimeStreamWindow<>( subtransExecutor, rowMeta, 0, 2, 1, ( p ) -> count.set( p.getKey().get( 0 ).size() ) );
    window.buffer( Flowable.fromIterable( singletonList( asList( "v1", "v2" ) ) ) )
      .forEach( result -> assertEquals( mockResult, result ) );
    assertEquals( 0, count.get() );
  }
}
