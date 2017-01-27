package org.pentaho.di.engine.kettlenative.impl.factories;

import org.pentaho.di.engine.api.ExecutionContext;
import org.pentaho.di.engine.api.model.Operation;
import org.pentaho.di.engine.api.model.Transformation;
import org.pentaho.di.engine.kettlenative.impl.IExecutableOperation;
import org.pentaho.di.engine.kettlenative.impl.KettleExecOperation;

import java.util.Optional;
import java.util.concurrent.ExecutorService;

public class KettleExecOperationFactory implements IExecutableOperationFactory {

  @Override public Optional<IExecutableOperation> create( Operation operation, ExecutionContext context ) {
    Transformation transformation = context.getTransformation();
    ExecutorService executorService = Optional.ofNullable((ExecutorService) context.getEnvironment().get( "executor" ))
      .orElseThrow( () -> new RuntimeException( "no executor" ));
    return Optional.of( KettleExecOperation.compile( operation, transformation, executorService ) );
  }
}
