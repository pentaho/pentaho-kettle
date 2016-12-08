package org.pentaho.di.engine.kettlenative.impl;

import org.pentaho.di.engine.api.AbstractExecutableOperation;
import org.pentaho.di.engine.api.IExecutableOperation;
import org.pentaho.di.engine.api.IOperation;
import org.pentaho.di.engine.api.IOperationVisitor;
import org.pentaho.di.engine.api.ITuple;
import org.pentaho.di.engine.kettlenative.impl.wrappers.IStepWrapper;
import org.pentaho.di.engine.kettlenative.impl.wrappers.NativeStepWrapper;
import rx.Subscriber;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class KettleExecOperation extends AbstractExecutableOperation implements IExecutableOperation {

  private final IOperation operation;
  private final IStepWrapper stepWrapper;
  private List<Subscriber<? super ITuple> > subscribers = new ArrayList<>();
  private AtomicBoolean done = new AtomicBoolean( false );

  protected KettleExecOperation( IOperation op ) {
    this.operation = op;
    stepWrapper = new NativeStepWrapper( this, getFrom() );
  }


  public static IExecutableOperation compile( IOperation operation ) {
    return new KettleExecOperation( operation );
  }

  @Override public void next( ITuple tuple ) {
    subscribers.stream()
      .forEach( sub -> sub.onNext( tuple ) );
  }


  @Override public void subscribe( Subscriber<? super ITuple> subscriber ) {
    subscribers.add( subscriber );
  }

  @Override public void start() {
    stepWrapper.start();
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
