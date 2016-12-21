package org.pentaho.di.engine.api;

import java.util.Optional;

public interface IExecutableOperationFactory {

  Optional<IExecutableOperation> create( ITransformation transformation, IOperation operation, IExecutionContext context );
}
