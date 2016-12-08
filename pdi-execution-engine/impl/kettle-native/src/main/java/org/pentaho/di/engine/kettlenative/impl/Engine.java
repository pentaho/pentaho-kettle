package org.pentaho.di.engine.kettlenative.impl;

import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.engine.api.IExecutableOperation;
import org.pentaho.di.engine.api.IEngine;
import org.pentaho.di.engine.api.IOperation;
import org.pentaho.di.engine.api.ITransformation;
import org.pentaho.di.engine.api.ITuple;
import rx.Observable;
import rx.Subscriber;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Engine implements IEngine {

  @Override public void execute( ITransformation trans ) {
    try {
      KettleEnvironment.init();
    } catch ( KettleException e ) {
      e.printStackTrace();
    }
    List<IExecutableOperation> execOps = getExecutableOperations( trans );

    wireExecution( execOps );

    // start execution
    execOps.stream()
      .forEach( IExecutableOperation::start );

    // wait till done
    while( running( execOps ) ) {
      try {
        Thread.sleep( 50 );
      } catch ( InterruptedException e ) {
        e.printStackTrace();
      }
    }

  }

  private List<IExecutableOperation> getExecutableOperations( ITransformation trans ) {
    return trans.getOperations()
        .stream()
        .map( KettleExecOperation::compile )
        .collect( Collectors.toList() );
  }

  private boolean running( List<IExecutableOperation> execOps ) {
    return execOps.stream()
      .filter( IExecutableOperation::isRunning )
      .findFirst()
      .map( (op) -> new Boolean( true ) )
      .orElse( false );
  }

  private void wireExecution( List<IExecutableOperation> execOps ) {
    Map<IExecutableOperation, Observable<? super ITuple>> observables =
      execOps.stream()
        .collect( Collectors.toMap(
          execOp -> execOp, execOp -> Observable.create( sub -> execOp.subscribe( sub ) ) ) );

    observables.keySet().stream()
      .flatMap( execOp -> execOp.getFrom().stream()
        .map( op -> new AbstractMap.SimpleImmutableEntry<>( execOp, op ) ) )
      .forEach( e -> getExecOp( e.getValue(), execOps ).subscribe( new Subscriber<ITuple>() {
        @Override public void onCompleted() {
          e.getKey().onCompleted();
        }

        @Override public void onError( Throwable throwable ) {
          e.getKey().onError( throwable );
        }

        @Override public void onNext( ITuple tuple ) {
          e.getKey().onNext( tuple );
        }
      } ) );
  }


  private IExecutableOperation getExecOp( IOperation op, List<IExecutableOperation> execOps ) {
    return execOps.stream()
      .filter( execOp -> execOp.getId().equals( op.getId() ) )
      .findFirst()
      .orElseThrow( () -> new RuntimeException( "no matching exec op" ) );
  }
}
