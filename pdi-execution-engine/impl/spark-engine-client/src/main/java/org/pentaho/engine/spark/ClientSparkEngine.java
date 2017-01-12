package org.pentaho.engine.spark;

import org.pentaho.di.core.plugins.EnginePlugin;
import org.pentaho.di.engine.api.IEngine;
import org.pentaho.di.engine.api.IExecutionContext;
import org.pentaho.di.engine.api.IExecutionResultFuture;
import org.pentaho.di.engine.api.ITransformation;

@EnginePlugin( id = "ClientSparkEngine", name = "Spark Engine (Client)" )
public class ClientSparkEngine implements IEngine {

  @Override
  public IExecutionContext prepare( ITransformation trans ) {
    System.out.println("ClientSparkEngine.prepare()");
    return null;
  }

  @Override
  public IExecutionResultFuture execute( IExecutionContext context ) {
    System.out.println("ClientSparkEngine.execute()");
    return null;
  }
}
