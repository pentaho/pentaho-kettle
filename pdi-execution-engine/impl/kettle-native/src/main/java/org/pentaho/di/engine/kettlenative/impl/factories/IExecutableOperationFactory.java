package org.pentaho.di.engine.kettlenative.impl.factories;

import org.pentaho.di.engine.api.IExecutionContext;
import org.pentaho.di.engine.api.model.IOperation;
import org.pentaho.di.engine.kettlenative.impl.IExecutableOperation;

import java.util.Optional;

public interface IExecutableOperationFactory {

  Optional<IExecutableOperation> create( IOperation operation, IExecutionContext context );
}
