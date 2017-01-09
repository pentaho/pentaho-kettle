package org.pentaho.di.engine.kettlenative.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.engine.api.IDataEvent;
import org.pentaho.di.engine.api.IEngine;
import org.pentaho.di.engine.api.IExecutableOperation;
import org.pentaho.di.engine.api.IExecutableOperationFactory;
import org.pentaho.di.engine.api.IExecutionContext;
import org.pentaho.di.engine.api.IExecutionResult;
import org.pentaho.di.engine.api.IExecutionResultFuture;
import org.pentaho.di.engine.api.IOperation;
import org.pentaho.di.engine.api.IProgressReporting;
import org.pentaho.di.engine.api.ITransformation;
import org.pentaho.di.engine.kettlenative.impl.factories.KettleExecOperationFactory;
import org.pentaho.di.engine.kettlenative.impl.factories.SparkExecOperationFactory;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Engine implements IEngine {

  private final ExecutorService executorService = Executors.newFixedThreadPool( 10 );

  private final JavaSparkContext javaSparkContext = new JavaSparkContext(
    new SparkConf()
      .setAppName( "AEL" )
      .setMaster( "local[2]" ) );

  private final List<IExecutableOperationFactory> factories = ImmutableList.of(
    new SparkExecOperationFactory(), new KettleExecOperationFactory() );  // TODO: injectable, rankable

  @Override public IExecutionContext prepare( ITransformation trans ) {
    return new ExecutionContext( trans,Collections.emptyMap(),
      ImmutableMap.of( "sparkcontext", javaSparkContext,
        "executor", executorService )  );
  }

  @Override public IExecutionResultFuture execute( IExecutionContext context ) {
    ITransformation trans = context.getTransformation();
    initKettle();
    // convert ops to executable ops
    List<IExecutableOperation> execOps = getExecutableOperations( trans,
      context );
    // wire up the execution graph
    wireExecution( execOps );
    // submit for execution
    return new ExecutionResultFuture( context, executorService.submit( () -> getResult( trans, execOps ) ) );
  }

  private List<IExecutableOperation> getExecutableOperations( ITransformation trans, IExecutionContext context ) {
    return trans.getOperations()
      .stream()
      .map( op -> getExecOp( trans, op, context ) )
      .collect( Collectors.toList() );
  }

  private IExecutableOperation getExecOp( ITransformation trans, IOperation op, IExecutionContext context ) {
    return factories.stream()
      .map( factory -> factory.create( trans, op, context ) )
      .filter( o -> o.isPresent() )
      .findFirst()
      .get()
      .orElseThrow( () -> new RuntimeException( "Couldn't create an executable op for " + op.getId() ) );
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


  private class CountdownSubscriber implements Serializable, Subscriber<IDataEvent> {

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
