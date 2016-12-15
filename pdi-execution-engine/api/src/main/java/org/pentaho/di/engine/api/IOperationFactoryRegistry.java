package org.pentaho.di.engine.api;

public interface IOperationFactoryRegistry {

  IExecutableOperationFactory getFactory( String operationId );

}
