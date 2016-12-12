package org.pentaho.di.engine.kettlenative.impl;

import org.pentaho.di.engine.api.IData;
import org.pentaho.di.engine.api.IDataEvent;
import org.pentaho.di.engine.api.IExecutableOperation;
import org.pentaho.di.engine.api.IExecutionContext;
import org.pentaho.di.engine.api.IExecutionResult;
import org.pentaho.di.engine.api.IExecutionResultFuture;
import org.pentaho.di.engine.api.IOperation;
import org.pentaho.di.engine.api.IPDIEventSource;
import com.google.common.collect.ImmutableList;
import org.pentaho.di.engine.api.IProgressReporting;
import org.pentaho.di.engine.api.ITransformation;
import org.pentaho.di.engine.api.ITransformationEvent;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class ExecutionResultFuture implements IExecutionResultFuture {

  private final ITransformation trans;
  private final List<IExecutableOperation> execOps;

  protected ExecutionResultFuture( ITransformation trans, List<IExecutableOperation> execOps ) {
    this.trans = trans;
    this.execOps = execOps;
  }

  @Override public IExecutionContext getExecutionContext() {
    return null;
  }

  @Override public ITransformation getTransformation() {
    return trans;
  }

  @Override public boolean cancel( boolean mayInterruptIfRunning ) {
    return false;
  }

  @Override public boolean isCancelled() {
    return false;
  }

  @Override public boolean isDone() {
    return false;
  }

  @Override public IExecutionResult get() throws InterruptedException, ExecutionException {
    sourceExecOpsStream( trans, execOps )
      .forEach(
        execOp -> {
          while ( execOp.isRunning() ) {
            execOp.onNext( null );
          }
        }
      );
    return () -> {
      // gotta be a prettier way to do this
      List<IProgressReporting<IDataEvent>> reportList = new ArrayList<>();
      execOps.stream()
        .forEach( o -> reportList.add( o ) );
      return reportList;
    };
  }

  @Override public IExecutionResult get( long timeout, TimeUnit unit )
    throws InterruptedException, ExecutionException, TimeoutException {
    return null;
  }

  @Override public int getIn() {
    return 0;
  }

  @Override public int getOut() {
    return 0;
  }

  @Override public int getDropped() {
    return 0;
  }

  @Override public int getInFlight() {
    return 0;
  }

  @Override public Status getStatus() {
    return null;
  }

  @Override public void subscribe( Subscriber<? super ITransformationEvent> subscriber ) {

  }

  @Override public void onSubscribe( Subscription subscription ) {

  }

  @Override public void onNext( ITransformationEvent iTransformationEvent ) {

  }

  @Override public void onError( Throwable throwable ) {

  }

  @Override public void onComplete() {

  }

  private Stream<IExecutableOperation> sourceExecOpsStream( ITransformation trans,
                                                            List<IExecutableOperation> execOps ) {
    return trans.getSourceOperations().stream()
      .map( op -> getExecOp( op, execOps  ) );
  }

  private Stream<IExecutableOperation> sinkExecOpsStream( ITransformation trans,
                                                          List<IExecutableOperation> execOps ) {
    return trans.getSourceOperations().stream()
      .map( op -> getExecOp( op, execOps  ) );
  }

  private IExecutableOperation getExecOp( IOperation op, List<IExecutableOperation> execOps ) {
    return execOps.stream()
      .filter( execOp -> execOp.getId().equals( op.getId() ) )
      .findFirst()
      .orElseThrow( () -> new RuntimeException( "no matching exec op" ) );
  }


}
