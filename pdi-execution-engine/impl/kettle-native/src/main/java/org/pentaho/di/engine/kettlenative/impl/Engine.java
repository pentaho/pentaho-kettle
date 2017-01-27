package org.pentaho.di.engine.kettlenative.impl;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.engine.api.events.DataEvent;
import org.pentaho.di.engine.kettlenative.impl.factories.IExecutableOperationFactory;
import org.pentaho.di.engine.api.ExecutionContext;
import org.pentaho.di.engine.api.ExecutionResult;
import org.pentaho.di.engine.api.model.Operation;
import org.pentaho.di.engine.api.model.Transformation;
import org.pentaho.di.engine.api.reporting.Metrics;
import org.pentaho.di.engine.kettlenative.impl.factories.KettleExecOperationFactory;
import org.pentaho.di.engine.kettlenative.impl.factories.SparkExecOperationFactory;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Engine implements org.pentaho.di.engine.api.Engine {

  private final ExecutorService executorService = Executors.newFixedThreadPool( 10 );

  private final JavaSparkContext javaSparkContext = new JavaSparkContext(
    new SparkConf()
      .setAppName( "AEL" )
      .setMaster( "local[2]" ) );

  private final List<IExecutableOperationFactory> factories = ImmutableList.of(
    new SparkExecOperationFactory(), new KettleExecOperationFactory() );  // TODO: injectable, rankable

  @Override public ExecutionContext prepare( Transformation trans ) {
    return new org.pentaho.di.engine.kettlenative.impl.ExecutionContext( this, trans, Collections.emptyMap(),
      ImmutableMap.of( "sparkcontext", javaSparkContext,
        "executor", executorService ) );
  }

  CompletableFuture<ExecutionResult> execute( ExecutionContext context ) {
    Transformation trans = context.getTransformation();
    initKettle();
    // convert ops to executable ops
    List<IExecutableOperation> execOps = getExecutableOperations( trans,
      context );
    // wire up the execution graph
    wireExecution( execOps );
    // submit for execution
    return getResult( trans, execOps );
  }

  private List<IExecutableOperation> getExecutableOperations( Transformation trans, ExecutionContext context ) {
    return trans.getOperations()
      .stream()
      .map( op -> getExecOp( op, context ) )
      .collect( Collectors.toList() );
  }

  private IExecutableOperation getExecOp( Operation op, ExecutionContext context ) {
    return factories.stream()
      .map( factory -> factory.create( op, context ) )
      .filter( o -> o.isPresent() )
      .findFirst()
      .get()
      .orElseThrow( () -> new RuntimeException( "Couldn't create an executable op for " + op.getId() ) );
  }

  private void wireExecution( List<IExecutableOperation> execOps ) {
    // for each operation, subscribe to the set of "from" ops.
    execOps.forEach( op ->
      op.getParent().getFrom().stream()
        .map( fromOp -> getExecOp( fromOp, execOps ) )
        .forEach( op::subscribeTo )
    );
  }

  private IExecutableOperation getExecOp( Operation op, List<IExecutableOperation> execOps ) {
    return execOps.stream()
      .filter( execOp -> execOp.getId().equals( op.getId() ) )
      .findFirst()
      .orElseThrow( () -> new RuntimeException( "no matching exec op" ) );
  }

  private Stream<IExecutableOperation> sourceExecOpsStream( Transformation trans,
                                                            List<IExecutableOperation> execOps ) {
    return ( (org.pentaho.di.engine.kettlenative.impl.Transformation) trans ).getSourceOperations().stream()
      .map( op -> getExecOp( op, execOps ) );
  }

  private CompletableFuture<ExecutionResult> getResult( Transformation trans, List<IExecutableOperation> execOps ) {
    CountDownLatch countdown = new CountDownLatch( execOps.size() );
    CountdownSubscriber subscriber =
      new CountdownSubscriber( countdown );

    // Subscribe to each operation so we can hook into completion
    execOps.forEach( op -> op.subscribe( subscriber ) );

    // invoke each source operation
    sourceExecOpsStream( trans, execOps ).forEach( IExecutableOperation::start );

    // wait for all operations to complete
    return CompletableFuture
      .runAsync( () -> {
        try {
          countdown.await();
        } catch ( InterruptedException e ) {
          Throwables.propagate( e );
        }
      } )
      .thenApply( done -> {
          Map<Operation, Metrics> report = execOps.stream()
            .collect( Collectors.toMap( IExecutableOperation::getParent, IExecutableOperation::getMetrics ) );
          return (ExecutionResult) () -> report;
          }
      );
  }


  private class CountdownSubscriber implements Serializable, Subscriber<DataEvent> {

    private final CountDownLatch countDownLatch;

    CountdownSubscriber( CountDownLatch countDownLatch ) {
      this.countDownLatch = countDownLatch;
    }

    @Override public void onSubscribe( Subscription subscription ) {

    }

    @Override public void onNext( DataEvent dataEvent ) {
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
