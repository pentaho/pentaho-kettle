package org.pentaho.di.engine.api;

import org.pentaho.di.engine.api.model.ITransformation;
import org.pentaho.di.engine.api.reporting.ISubscriptionManager;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Created by nbaker on 5/31/16.
 */
public interface IExecutionContext extends ISubscriptionManager {
  Map<String, Object> getParameters();

  Map<String, Object> getEnvironment();

  ITransformation getTransformation();

  CompletableFuture<IExecutionResult> execute();
}
