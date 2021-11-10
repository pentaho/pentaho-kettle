/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2021 by Hitachi Vantara : http://www.pentaho.com
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
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.SubtransExecutor;
import org.pentaho.di.trans.streaming.api.StreamWindow;
import org.pentaho.di.core.Const;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * A StreamWindow implementation which buffers rows of I by a fixed amount of time and size, executing each batch in a
 * subtransformation.
 */
public class FixedTimeStreamWindow<I extends List> implements StreamWindow<I, Result> {

  private static final Class<?> PKG = BaseStreamStep.class;

  private final RowMetaInterface rowMeta;
  private final long millis;
  private final int batchSize;
  private SubtransExecutor subtransExecutor;
  private int parallelism;
  private final Consumer<Map.Entry<List<I>, Result>> postProcessor;
  private int sharedStreamingBatchPoolSize = 0;
  private static ThreadPoolExecutor sharedStreamingBatchPool;
  private final int rxBatchCount;

  public FixedTimeStreamWindow( SubtransExecutor subtransExecutor, RowMetaInterface rowMeta, long millis,
                                int batchSize, int parallelism ) {
    this( subtransExecutor, rowMeta, millis, batchSize, parallelism, ( p ) -> { } );
  }

  public FixedTimeStreamWindow( SubtransExecutor subtransExecutor, RowMetaInterface rowMeta, long millis,
                                int batchSize, int parallelism, Consumer<Map.Entry<List<I>, Result>> postProcessor ) {
    this.subtransExecutor = subtransExecutor;
    this.rowMeta = rowMeta;
    this.millis = millis;
    this.batchSize = batchSize;
    this.parallelism = parallelism;
    this.postProcessor = postProcessor;

    //When only batchSize is provided and it is greater than 0 and less than the prefetchCount we can exactly
    //calculate how many batches rx will have to handle. When a time value is provided handle the full prefetchCount
    // because the batchSize split by RxJava may be smaller and require more batches.
    this.rxBatchCount = ( millis == 0 && batchSize > 0 && batchSize < subtransExecutor.getPrefetchCount() )
      ? subtransExecutor.getPrefetchCount() / batchSize : this.subtransExecutor.getPrefetchCount();

    try {
      sharedStreamingBatchPoolSize = Integer.parseInt( System.getProperties().getProperty( Const.SHARED_STREAMING_BATCH_POOL_SIZE, "0" ) );
      if ( sharedStreamingBatchPoolSize > 0 ) {
        if ( sharedStreamingBatchPool == null ) {
          sharedStreamingBatchPool = (ThreadPoolExecutor) Executors.newFixedThreadPool( sharedStreamingBatchPoolSize );
        } else {
          if ( sharedStreamingBatchPool.getCorePoolSize() != sharedStreamingBatchPoolSize ) {
            sharedStreamingBatchPool.setMaximumPoolSize( sharedStreamingBatchPoolSize );
            sharedStreamingBatchPool.setCorePoolSize( sharedStreamingBatchPoolSize );
          }
        }
      }
    } catch ( NumberFormatException e ) {
      sharedStreamingBatchPoolSize = 0;
    }
  }

  @Override public Iterable<Result> buffer( Flowable<I> flowable ) {
    Flowable<List<I>> buffer = millis > 0
      ? batchSize > 0 ? flowable.buffer( millis, MILLISECONDS, Schedulers.io(), batchSize, ArrayList::new, true )
      : flowable.buffer( millis, MILLISECONDS )
      : flowable.buffer( batchSize );
    return buffer
      .parallel( parallelism, rxBatchCount )
      .runOn( sharedStreamingBatchPoolSize > 0 ? Schedulers.from( sharedStreamingBatchPool ) : Schedulers.io(),
        rxBatchCount )
      .filter( list -> !list.isEmpty() )
      .map( this::sendBufferToSubtrans )
      .filter( Optional::isPresent )
      .map( Optional::get )
      .sequential()
      .doOnNext( this::failOnError )
      .doOnNext( postProcessor )
      .map( Map.Entry::getValue )
      .blockingIterable();
  }

  private void failOnError( Map.Entry<List<I>, Result> pair ) throws KettleException {
    if ( pair.getValue().getNrErrors() > 0 ) {
      throw new KettleException( BaseMessages.getString( PKG, "FixedTimeStreamWindow.SubtransFailed"  ) );
    }
  }

  private Optional<Map.Entry<List<I>, Result>> sendBufferToSubtrans( List<I> input ) throws KettleException {
    final List<RowMetaAndData> rows = input.stream()
      .map( row -> row.toArray( new Object[ 0 ] ) )
      .map( objects -> new RowMetaAndData( rowMeta, objects ) )
      .collect( Collectors.toList() );
    Optional<Result> optionalRes = subtransExecutor.execute( rows );
    return optionalRes.map( result -> new AbstractMap.SimpleImmutableEntry<>( input, result ) );
  }

}
