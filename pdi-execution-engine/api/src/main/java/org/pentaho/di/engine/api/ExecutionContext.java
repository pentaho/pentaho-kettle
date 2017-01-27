package org.pentaho.di.engine.api;

import org.pentaho.di.engine.api.model.Transformation;
import org.pentaho.di.engine.api.reporting.SubscriptionManager;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Created by nbaker on 5/31/16.
 */
public interface ExecutionContext extends SubscriptionManager {
  Map<String, Object> getParameters();

  Map<String, Object> getEnvironment();

  Transformation getTransformation();

  CompletableFuture<ExecutionResult> execute();
}
