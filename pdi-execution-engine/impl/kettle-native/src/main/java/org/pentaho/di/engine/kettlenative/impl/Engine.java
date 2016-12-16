package org.pentaho.di.engine.kettlenative.impl;

import com.google.common.collect.ImmutableList;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.engine.api.IDataEvent;
import org.pentaho.di.engine.api.IEngine;
import org.pentaho.di.engine.api.IExecutableOperation;
import org.pentaho.di.engine.api.IExecutionResult;
import org.pentaho.di.engine.api.IExecutionResultFuture;
import org.pentaho.di.engine.api.IOperation;
import org.pentaho.di.engine.api.IProgressReporting;
import org.pentaho.di.engine.api.ITransformation;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Engine implements IEngine {

  private static final ExecutorService executorService = Executors.newFixedThreadPool( 10 );

  @Override public Future<IExecutionResult> execute( ITransformation trans ) {
    initKettle();
    // convert ops to executable ops
    List<IExecutableOperation> execOps = getExecutableOperations( trans );
    // wire up the execution graph
    wireExecution( execOps );
    // submit for execution
    return executorService.submit( () -> getResult( trans, execOps ) );
  }

  private List<IExecutableOperation> getExecutableOperations( ITransformation trans ) {
    return trans.getOperations()
      .stream()
      .map( op -> KettleExecOperation.compile( op, trans, executorService ) )
      .collect( Collectors.toList() );
  }

  private void wireExecution( List<IExecutableOperation> execOps ) {
    // for each operation, subscribe to the set of "from" ops.
    execOps.stream()
      .forEach( op ->
        op.getFrom().stream()
          .map( fromOp -> getExecOp( fromOp, execOps ) )
          .forEach( fromExecOp -> fromExecOp.subscribe( op ) )
      );
  }

  private IExecutableOperation getExecOp( IOperation op, List<IExecutableOperation> execOps ) {
    return execOps.stream()
      .filter( execOp -> execOp.getId().equals( op.getId() ) )
      .findFirst()
      .orElseThrow( () -> new RuntimeException( "no matching exec op" ) );
  }

  private Stream<IExecutableOperation> sourceExecOpsStream( ITransformation trans,
                                                            List<IExecutableOperation> execOps ) {
    return trans.getSourceOperations().stream()
      .map( op -> getExecOp( op, execOps ) );
  }

  public IExecutionResult getResult( ITransformation trans, List<IExecutableOperation> execOps )
    throws InterruptedException, ExecutionException {
    CountDownLatch countdown = new CountDownLatch( execOps.size() );
    CountdownSubscriber subscriber =
      new CountdownSubscriber( countdown );

    // Subscribe to each operation so we can hook into completion
    execOps.stream()
      .forEach( op -> op.subscribe( subscriber ) );

    // invoke each source operation
    sourceExecOpsStream( trans, execOps )
      .forEach(
        execOp -> execOp.onNext( KettleDataEvent.empty() ) );

    // wait for all operations to complete
    countdown.await();

    //return results
    return () -> ImmutableList.<IProgressReporting<IDataEvent>>builder()
      .addAll( execOps )
      .build();
  }

  private class CountdownSubscriber implements Subscriber<IDataEvent> {

    private final CountDownLatch countDownLatch;

    CountdownSubscriber( CountDownLatch countDownLatch ) {
      this.countDownLatch = countDownLatch;
    }

    @Override public void onSubscribe( Subscription subscription ) {

    }

    @Override public void onNext( IDataEvent iDataEvent ) {

    }

    @Override public void onError( Throwable throwable ) {

    }

    @Override public void onComplete() {
      countDownLatch.countDown();
    }
  }

  private void initKettle() {
    try {
      if ( !KettleEnvironment.isInitialized() ) {
        KettleEnvironment.init();
      }
    } catch ( KettleException e ) {
      e.printStackTrace();
    }
  }

}
