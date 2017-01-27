package org.pentaho.di.engine.kettlenative.impl;

import com.google.common.collect.ImmutableList;
import org.pentaho.di.engine.api.ExecutionResult;
import org.pentaho.di.engine.api.model.Transformation;
import org.pentaho.di.engine.api.reporting.ReportingEvent;
import org.pentaho.di.engine.api.model.LogicalModelElement;
import org.pentaho.di.trans.TransExecutionConfiguration;
import org.pentaho.di.trans.TransMeta;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class ExecutionContext implements org.pentaho.di.engine.api.ExecutionContext {

  private final Engine engine;
  private Map<String, Object> parameters = new HashMap<String, Object>();
  private Map<String, Object> environment = new HashMap<String, Object>();
  private Transformation transformation;
  private TransMeta transMeta;
  private TransExecutionConfiguration executionConfiguration;
  private String[] arguments;

  public ExecutionContext( Engine engine, Transformation transformation, Map<String, Object> parameters,
                           Map<String, Object> environment ) {
    this.engine = engine;
    this.parameters = parameters;
    this.environment = environment;
    this.transformation = transformation;
  }

  @Override public Map<String, Object> getParameters() {
    return parameters;
  }

  @Override public Map<String, Object> getEnvironment() {
    return environment;
  }

  public void setParameters( Map<String, Object> parameters ) {
    this.parameters = parameters;
  }

  public void setEnvironment( Map<String, Object> environment ) {
    this.environment = environment;
  }

  @Override public Transformation getTransformation() {
    return transformation;
  }

  public void setTransformation( Transformation transformation ) {
    this.transformation = transformation;
  }

  public TransMeta getTransMeta() {
    return transMeta;
  }

  public void setTransMeta( TransMeta transMeta ) {
    this.transMeta = transMeta;
  }

  public void setExecutionConfiguration( TransExecutionConfiguration executionConfiguration ) {
    this.executionConfiguration = executionConfiguration;
  }

  public TransExecutionConfiguration getExecutionConfiguration() {
    return executionConfiguration;
  }


  public void setArguments( String[] arguments ) {
    this.arguments = arguments;
  }

  @Override public CompletableFuture<ExecutionResult> execute() {
    return engine.execute( this );
  }

  @Override
  public <S extends LogicalModelElement, D extends Serializable>
  Publisher<ReportingEvent<S, D>> eventStream( S source, Class<D> type ) {
    return Subscriber::onComplete;
  }

  @Override public Collection<LogicalModelElement> getReportingSources() {
    return ImmutableList.of();
  }
}