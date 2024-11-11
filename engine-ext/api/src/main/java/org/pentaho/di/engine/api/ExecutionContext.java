/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


package org.pentaho.di.engine.api;

import org.pentaho.di.engine.api.model.Transformation;
import org.pentaho.di.engine.api.reporting.LogLevel;
import org.pentaho.di.engine.api.reporting.SubscriptionManager;

import java.security.Principal;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Created by nbaker on 5/31/16.
 */
public interface ExecutionContext extends SubscriptionManager {
  Map<String, Object> getParameters();

  Map<String, Object> getEnvironment();

  void setParameters( Map<String, Object> parameters );

  void setEnvironment( Map<String, Object> environment );

  void setParameter( String key, Object value );

  void setEnvironment( String key, Object value );

  Transformation getTransformation();

  CompletableFuture<ExecutionResult> execute();

  Principal getActingPrincipal();

  void setActingPrincipal( Principal actingPrincipal );

  void setLoggingLogLevel( LogLevel logLevel );

  LogLevel getLoggingLogLevel();

  void stopTransformation();

  /**
   * Corresponds to Trans.safeStop()
   */
  default void safeStopTransformation() {
    stopTransformation();
  }

}
