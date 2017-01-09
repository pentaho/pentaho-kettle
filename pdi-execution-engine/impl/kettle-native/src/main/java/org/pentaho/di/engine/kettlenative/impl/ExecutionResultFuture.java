package org.pentaho.di.engine.kettlenative.impl;

import org.pentaho.di.engine.api.IExecutionContext;
import org.pentaho.di.engine.api.IExecutionResult;
import org.pentaho.di.engine.api.IExecutionResultFuture;
import org.pentaho.di.engine.api.IProgressReporting;
import org.pentaho.di.engine.api.ITransformation;
import org.pentaho.di.engine.api.ITransformationEvent;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by nbaker on 1/6/17.
 */
public class ExecutionResultFuture implements IExecutionResultFuture {
  private final IExecutionContext context;
  private final Future<IExecutionResult> rawFuture;

  public ExecutionResultFuture( IExecutionContext context, Future<IExecutionResult> rawFuture ) {
    this.context = context;
    this.rawFuture = rawFuture;
  }

  @Override public IExecutionContext getExecutionContext() {
    return context;
  }

  @Override public ITransformation getTransformation() {
    return context.getTransformation();
  }

  @Override public boolean cancel( boolean mayInterruptIfRunning ) {
    return rawFuture.cancel( mayInterruptIfRunning );
  }

  @Override public boolean isCancelled() {
    return rawFuture.isCancelled();
  }

  @Override public boolean isDone() {
    return rawFuture.isDone();
  }

  @Override public IExecutionResult get() throws InterruptedException, ExecutionException {
    return rawFuture.get();
  }

  @Override public IExecutionResult get( long timeout, TimeUnit unit )
    throws InterruptedException, ExecutionException, TimeoutException {
    return rawFuture.get( timeout, unit );
  }

  @Override public long getIn() {
    return 0;
  }

  @Override public long getOut() {
    return 0;
  }

  @Override public long getDropped() {
    return 0;
  }

  @Override public int getInFlight() {
    return 0;
  }

  @Override public Status getStatus() {
    return null;
  }

  @Override public String getId() {
    return null;
  }

  @Override public void subscribe( Subscriber<? super ITransformationEvent> s ) {

  }
}
