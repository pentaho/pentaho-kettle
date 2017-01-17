package org.pentaho.engine.spark;

import org.pentaho.di.core.plugins.EnginePlugin;
import org.pentaho.di.engine.api.IEngine;
import org.pentaho.di.engine.api.IExecutionContext;
import org.pentaho.di.engine.api.ITransformation;
import org.pentaho.engine.spark.context.SparkExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main Entry Point for Client Spark Plugin
 */
@EnginePlugin( id = "ClientSparkEngine", name = "Spark Engine (Client)" )
public class ClientSparkEngine implements IEngine {

  private static final Logger LOG = LoggerFactory.getLogger( ClientSparkEngine.class );

  @Override
  public IExecutionContext prepare( ITransformation trans ) {
    LOG.trace( "prepare(trans: {}", trans );
    return new SparkExecutionContext( trans );
  }

}
