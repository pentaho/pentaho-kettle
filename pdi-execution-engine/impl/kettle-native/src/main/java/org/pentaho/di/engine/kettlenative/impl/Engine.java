package org.pentaho.di.engine.kettlenative.impl;

import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.engine.api.IEngine;
import org.pentaho.di.engine.api.IExecutableOperation;
import org.pentaho.di.engine.api.IExecutionResultFuture;
import org.pentaho.di.engine.api.IOperation;
import org.pentaho.di.engine.api.ITransformation;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Engine implements IEngine {

  @Override public IExecutionResultFuture execute( ITransformation trans ) {
    try {
      KettleEnvironment.init();
    } catch ( KettleException e ) {
      e.printStackTrace();
    }
    List<IExecutableOperation> execOps = getExecutableOperations( trans );

    wireExecution( execOps );

//    sourceExecOpsStream( trans, execOps )
//      .forEach(
//        execOp -> {
//          while ( execOp.isRunning() ) {
//            execOp.onNext( null );
//          }
//        }
//      );

    return getResultFuture( trans, execOps );
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

  private List<IExecutableOperation> getExecutableOperations( ITransformation trans ) {
    return trans.getOperations()
      .stream()
      .map( KettleExecOperation::compile )
      .collect( Collectors.toList() );
  }

  private void wireExecution( List<IExecutableOperation> execOps ) {
    // for each operation, subscribe to the set of "from" ops.
    execOps.stream()
      .forEach( op ->
        op.getFrom().stream()
        .map( fromOp -> getExecOp( fromOp, execOps  ) )
        .forEach( fromExecOp -> fromExecOp.subscribe( op ) )
      );
  }


  private IExecutableOperation getExecOp( IOperation op, List<IExecutableOperation> execOps ) {
    return execOps.stream()
      .filter( execOp -> execOp.getId().equals( op.getId() ) )
      .findFirst()
      .orElseThrow( () -> new RuntimeException( "no matching exec op" ) );
  }

  public IExecutionResultFuture getResultFuture( ITransformation trans, List<IExecutableOperation> execOps ) {
    return new ExecutionResultFuture( trans, execOps );
  }

}
