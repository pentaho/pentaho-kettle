/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.di.engine.api.reporting;

import org.pentaho.di.engine.api.events.PDIEvent;
import org.pentaho.di.engine.api.model.LogicalModelElement;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.io.Serializable;
import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Created by hudak on 1/11/17.
 */
public interface SubscriptionManager {
  <S extends LogicalModelElement, D extends Serializable>
    Publisher<PDIEvent<S, D>> eventStream( S source, Class<D> type );

  Collection<LogicalModelElement> getReportingSources();

  // Ease-of-use functions
  default <S extends LogicalModelElement, D extends Serializable>
    void subscribe( S source, Class<D> type, Subscriber<? super PDIEvent<S, D>> subscriber ) {
    eventStream( source, type ).subscribe( subscriber );
  }

  default <S extends LogicalModelElement, D extends Serializable>
    void subscribe( S source, Class<D> type, Consumer<D> onNext, Consumer<Throwable> onError, Runnable onComplete ) {
    subscribe( source, type, new Subscriber<PDIEvent<S, D>>() {
      @Override public void onSubscribe( Subscription s ) {
        // Start subscription immediately, get everything
        s.request( Long.MAX_VALUE );
      }

      @Override public void onNext( PDIEvent<S, D> event ) {
        if ( onNext != null ) {
          onNext.accept( event.getData() );
        }
      }

      @Override public void onError( Throwable t ) {
        if ( onError != null ) {
          onError.accept( t );
        }
      }

      @Override public void onComplete() {
        if ( onComplete != null ) {
          onComplete.run();
        }
      }
    } );
  }

  default <S extends LogicalModelElement, D extends Serializable>
    void subscribe( S source, Class<D> type, Consumer<D> onNext, Runnable onComplete ) {
    subscribe( source, type, onNext, null, onComplete );
  }

  default <S extends LogicalModelElement, D extends Serializable>
    void subscribe( S source, Class<D> type, Consumer<D> onNext, Consumer<Throwable> onError ) {
    subscribe( source, type, onNext, onError, null );
  }

  default <S extends LogicalModelElement, D extends Serializable>
    void subscribe( S source, Class<D> type, Consumer<D> onNext ) {
    subscribe( source, type, onNext, null, null );
  }

  default <S extends LogicalModelElement, D extends Serializable>
    void subscribeAll( Class<S> sourceType, Class<D> type,
                     BiConsumer<S, D> onNext, BiConsumer<S, Throwable> onError, Consumer<S> onComplete ) {
    getReportingSources().stream()
      .filter( sourceType::isInstance )
      .map( sourceType::cast )
      .forEach( s -> {
        Consumer<D> next = onNext == null ? null : ( d ) -> onNext.accept( s, d );
        Consumer<Throwable> error = onError == null ? null : ( t ) -> onError.accept( s, t );
        Runnable complete = onComplete == null ? null : () -> onComplete.accept( s );
        subscribe( s, type, next, error, complete );
      } );
  }

  default <S extends LogicalModelElement, D extends Serializable>
    void subscribeAll( Class<S> sourceType, Class<D> type, BiConsumer<S, D> onNext, Consumer<S> onComplete ) {
    subscribeAll( sourceType, type, onNext, null, onComplete );
  }

  default <S extends LogicalModelElement, D extends Serializable>
    void subscribeAll( Class<S> sourceType, Class<D> type, BiConsumer<S, D> onNext, BiConsumer<S, Throwable> onError ) {
    subscribeAll( sourceType, type, onNext, onError, null );
  }

  default <S extends LogicalModelElement, D extends Serializable>
    void subscribeAll( Class<S> sourceType, Class<D> type, BiConsumer<S, D> onNext ) {
    subscribeAll( sourceType, type, onNext, null, null );
  }
}
