package org.pentaho.di.engine.kettlenative.impl.factories;

import org.pentaho.di.engine.api.ExecutionContext;
import org.pentaho.di.engine.api.model.Operation;
import org.pentaho.di.engine.kettlenative.impl.IExecutableOperation;

import java.util.Optional;

public interface IExecutableOperationFactory {

  Optional<IExecutableOperation> create( Operation operation, ExecutionContext context );
}
