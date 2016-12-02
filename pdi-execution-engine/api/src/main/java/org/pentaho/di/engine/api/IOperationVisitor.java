package org.pentaho.di.engine.api;

public interface IOperationVisitor<T> {
  T visit( IOperation operation );
}
