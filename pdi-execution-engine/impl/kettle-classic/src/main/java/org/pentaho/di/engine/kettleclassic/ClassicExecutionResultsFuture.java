package org.pentaho.di.engine.kettleclassic;

import org.pentaho.di.core.Result;
import org.pentaho.di.engine.api.IExecutionContext;
import org.pentaho.di.engine.api.IExecutionResult;
import org.pentaho.di.engine.api.IExecutionResultFuture;
import org.pentaho.di.engine.api.ITransformation;
import org.pentaho.di.engine.api.ITransformationEvent;
import org.pentaho.di.trans.Trans;
import org.reactivestreams.Subscriber;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by nbaker on 1/5/17.
 */
public class ClassicExecutionResultsFuture implements IExecutionResultFuture {
  private IExecutionContext executionContext;
  private ITransformation transformation;

  private CompletableFuture<IExecutionResult> future = new CompletableFuture<>();
  private Trans trans;

  public ClassicExecutionResultsFuture(
    Trans trans ) {
    this.trans = trans;
    Thread t = new Thread( new Runnable() {
      @Override public void run() {

        trans.waitUntilFinished();
        Result result = trans.getResult();
        future.complete( new ClassicExecutionResult() );
      }
    } );
    t.setDaemon( true );
    t.start();
  }

  @Override public IExecutionContext getExecutionContext() {
    return executionContext;
  }

  @Override public ITransformation getTransformation() {
    return null;
  }

  @Override public boolean cancel( boolean mayInterruptIfRunning ) {
    return future.cancel( mayInterruptIfRunning );
  }

  @Override public boolean isCancelled() {
    return future.isCancelled();
  }

  @Override public boolean isDone() {
    return future.isDone();
  }

  @Override public IExecutionResult get() throws InterruptedException, ExecutionException {
    return future.get();
  }

  @Override public IExecutionResult get( long timeout, TimeUnit unit )
    throws InterruptedException, ExecutionException, TimeoutException {
    return future.get( timeout, unit );
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

  @Override public void subscribe( Subscriber<? super ITransformationEvent> s ) {

  }

  @Override public String getId() {
    return null;
  }
}
