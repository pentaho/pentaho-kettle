package org.pentaho.di.engine.kettlenative.impl;

import org.pentaho.di.engine.api.IExecutionContext;

import java.util.Map;

public class ExecutionContext implements IExecutionContext {

  private final Map<String, Object> params;
  private final Map<String, Object> env;

  ExecutionContext( Map<String, Object> params, Map<String, Object> env ) {
    this.params = params;
    this.env = env;
  }

  @Override public Map<String, Object> getParameters() {
    return params;
  }

  @Override public Map<String, Object> getEnvironment() {
    return env;
  }
}
