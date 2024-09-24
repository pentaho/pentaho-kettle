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

package org.pentaho.di.trans.streaming.common;

import io.reactivex.Flowable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
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
  private final Function<List<I>, List<I>> bufferFilter;
  private int sharedStreamingBatchPoolSize = 0;
  private static ThreadPoolExecutor sharedStreamingBatchPool;
  private final int rxBatchCount;

  public FixedTimeStreamWindow( SubtransExecutor subtransExecutor, RowMetaInterface rowMeta, long millis,
                                int batchSize, int parallelism ) {
    this( subtransExecutor, rowMeta, millis, batchSize, parallelism, ( p ) -> { } );
  }

  public FixedTimeStreamWindow( SubtransExecutor subtransExecutor, RowMetaInterface rowMeta, long millis,
                                int batchSize, int parallelism, Consumer<Map.Entry<List<I>, Result>> postProcessor ) {
    this( subtransExecutor, rowMeta, millis, batchSize, parallelism, postProcessor, ( p ) -> p );
  }

  public FixedTimeStreamWindow( SubtransExecutor subtransExecutor, RowMetaInterface rowMeta, long millis,
                                int batchSize, int parallelism, Consumer<Map.Entry<List<I>, Result>> postProcessor, Function<List<I>, List<I>> bufferFilter ) {
    this.subtransExecutor = subtransExecutor;
    this.rowMeta = rowMeta;
    this.millis = millis;
    this.batchSize = batchSize;
    this.parallelism = parallelism;
    this.postProcessor = postProcessor;
    this.bufferFilter = bufferFilter;

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
      .map( this.bufferFilter ) // apply any filtering for data that should no longer be processed
      .filter( list -> !list.isEmpty() ) // ensure at least one record is left before sending to subtrans
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
