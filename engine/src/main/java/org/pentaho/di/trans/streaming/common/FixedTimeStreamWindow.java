/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.SubtransExecutor;
import org.pentaho.di.trans.streaming.api.StreamWindow;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * A StreamWindow implementation which buffers rows of I by a fixed amount of time and size, executing each batch in a
 * subtransformation.
 */
public class FixedTimeStreamWindow<I extends List> implements StreamWindow<I, Result> {

  private final RowMetaInterface rowMeta;
  private final long millis;
  private final int batchSize;
  private SubtransExecutor subtransExecutor;
  private final Consumer<Map.Entry<List<I>, Result>> postProcessor;

  public FixedTimeStreamWindow( SubtransExecutor subtransExecutor, RowMetaInterface rowMeta, long millis,
                                int batchSize ) {
    this( subtransExecutor, rowMeta, millis, batchSize, (p) -> { } );
  }

  public FixedTimeStreamWindow( SubtransExecutor subtransExecutor, RowMetaInterface rowMeta, long millis,
                                int batchSize, Consumer<Map.Entry<List<I>, Result>> postProcessor ) {
    this.subtransExecutor = subtransExecutor;
    this.rowMeta = rowMeta;
    this.millis = millis;
    this.batchSize = batchSize;
    this.postProcessor = postProcessor;
  }

  @Override public Iterable<Result> buffer( Observable<I> observable ) {
    Observable<List<I>> buffer = millis > 0
      ? batchSize > 0 ? observable.buffer( millis, MILLISECONDS, Schedulers.io(), batchSize, ArrayList::new, true )
      : observable.buffer( millis, MILLISECONDS )
      : observable.buffer( batchSize );
    return buffer
      .observeOn( Schedulers.io() )
      .filter( list -> !list.isEmpty() )
      .map( this::sendBufferToSubtrans )
      .takeWhile( pair -> pair.getValue().getNrErrors() == 0 )
      .doOnNext( postProcessor )
      .map( Map.Entry::getValue )
      .blockingIterable();
  }

  private Map.Entry<List<I>, Result> sendBufferToSubtrans( List<I> input ) throws KettleException {
    final List<RowMetaAndData> rows = input.stream()
      .map( row -> row.toArray( new Object[ 0 ] ) )
      .map( objects -> new RowMetaAndData( rowMeta, objects ) )
      .collect( Collectors.toList() );
    Optional<Result> optionalRes = subtransExecutor.execute( rows );
    return optionalRes.map( result -> new AbstractMap.SimpleImmutableEntry<>( input, result ) )
      .orElse( new AbstractMap.SimpleImmutableEntry<>( input, new Result() ) );
  }

}
