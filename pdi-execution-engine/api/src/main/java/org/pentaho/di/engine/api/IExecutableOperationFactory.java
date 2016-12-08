package org.pentaho.di.engine.api;

import java.util.List;

public interface IExecutableOperationFactory {

  IExecutableOperation create( IOperation operation );
}
