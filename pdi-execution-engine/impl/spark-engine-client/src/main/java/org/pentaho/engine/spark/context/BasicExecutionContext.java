package org.pentaho.engine.spark.context;

import org.pentaho.di.engine.api.IExecutionContext;
import org.pentaho.di.engine.api.ITransformation;
import org.pentaho.di.engine.api.reporting.IReportingEvent;
import org.pentaho.di.engine.api.reporting.IReportingEventSource;
import org.pentaho.engine.spark.ClientSparkEngine;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Abstract ExecutionContext
 * <p>
 * Contains the basic fields with getters and setters
 * <p>
 * // TODO Move this to a common area maybe?
 * <p>
 * Created by ccaspanello on 1/16/2017.
 */
public abstract class BasicExecutionContext implements IExecutionContext {

  private static final Logger LOG = LoggerFactory.getLogger( BasicExecutionContext.class );

  private ITransformation transformation;
  private Map<String, Object> parameters;
  private Map<String, Object> environment;
  private String[] arguments;

  public BasicExecutionContext( ITransformation transformation ) {
    LOG.trace( "BasicExecutionContext(transformation: {}", transformation );
    this.transformation = transformation;
    parameters = new HashMap<>();
    environment = new HashMap<>();
    arguments = new String[] {};
  }

  //<editor-fold desc="Getters & Setters">
  @Override
  public ITransformation getTransformation() {
    return transformation;
  }

  @Override
  public Map<String, Object> getParameters() {
    return parameters;
  }

  @Override
  public Map<String, Object> getEnvironment() {
    return environment;
  }

  @Override
  public String[] getArguments() {
    return arguments;
  }

  public void setTransformation( ITransformation transformation ) {
    this.transformation = transformation;
  }

  public void setParameters( Map<String, Object> parameters ) {
    this.parameters = parameters;
  }

  public void setEnvironment( Map<String, Object> environment ) {
    this.environment = environment;
  }

  public void setArguments( String[] arguments ) {
    this.arguments = arguments;
  }
  //</editor-fold>
}
