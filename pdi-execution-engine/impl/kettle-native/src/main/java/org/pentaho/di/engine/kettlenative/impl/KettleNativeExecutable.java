package org.pentaho.di.engine.kettlenative.impl;

import org.pentaho.di.engine.api.IExecutableOperation;
import org.pentaho.di.engine.api.IOperation;
import org.pentaho.di.engine.api.IOperationVisitor;
import org.pentaho.di.engine.api.ITuple;
import org.pentaho.di.engine.kettlenative.impl.wrappers.StepExecWrapper;
import rx.Subscriber;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class KettleNativeExecutable implements IExecutableOperation {

  private final IOperation operation;
  private final StepExecWrapper stepWrapper;
  private List<Subscriber<? super ITuple> > subscribers = new ArrayList<>();
  private AtomicBoolean done = new AtomicBoolean( false );

  private KettleNativeExecutable( IOperation op ) {
    this.operation = op;
    stepWrapper = new StepExecWrapper( this, getFrom() );
  }


  public static IExecutableOperation compile( IOperation operation ) {
    return new KettleNativeExecutable( operation );
  }

  @Override public void next( ITuple tuple ) {
    subscribers.stream()
      .forEach( sub -> sub.onNext( tuple ) );
  }


  @Override public void subscribe( Subscriber<? super ITuple> subscriber ) {
    subscribers.add( subscriber );
  }

  @Override public void start() {
    stepWrapper.exec();
  }

  @Override public void done() {
    done.set( true );
    subscribers.stream()
      .forEach( sub -> sub.onCompleted() );
  }

  @Override public boolean isRunning() {
    return !done.get();
  }

  @Override public String getId() {
    return operation.getId();
  }

  @Override public List<IOperation> getFrom() {
    return operation.getFrom();
  }

  @Override public List<IOperation> getTo() {
    return operation.getTo();
  }


  @Override public String getConfig() {
    return operation.getConfig();
  }

  @Override public <T> T accept( IOperationVisitor<T> visitor ) {
    return operation.accept( visitor );
  }

  @Override public void onCompleted() {
    stepWrapper.finished();
  }

  @Override public void onError( Throwable throwable ) {

  }

  @Override public void onNext( ITuple tuple ) {
    // push to injector
    stepWrapper.next( tuple );
  }
}
