package org.pentaho.di.engine.api;

/**
 * An IEngine is responsible for executing an ITransformation.
 *
 * In order to do so, it needs to inspect the structure of that
 * trans, rewrite or modify if necessary (leveraging IOperationVisitors),
 * and "resolve" the trans IOperations to concrete functions (ICallableOperations).
 */
public interface IEngine {

   void execute( ITransformation trans );
}

