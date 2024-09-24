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

package org.pentaho.di.engine.api.remote;

import org.pentaho.di.engine.api.model.Transformation;
import org.pentaho.di.engine.api.reporting.LogLevel;

import java.io.Serializable;
import java.security.Principal;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * A request for execution by a remote Engine. All fields should be Serializable.
 * <p>
 * Created by hudak on 1/25/17.
 */
public final class ExecutionRequest implements Message {
  private static final long serialVersionUID = -7835121168360407191L;

  // Unique Request ID (used in discovery / retry logic)
  private final String requestId;
  private final Map<String, Object> parameters;
  private final Map<String, Object> environment;
  private final Transformation transformation;
  private final Map<String, Set<Class<? extends Serializable>>> reportingTopics;
  private boolean reuseSparkContext = false;

  private final Principal actingPrincipal;
  private LogLevel loggingLogLevel;

  public ExecutionRequest( Map<String, Object> parameters, Map<String, Object> environment,
                           Transformation transformation,
                           Map<String, Set<Class<? extends Serializable>>> reportingTopics,
                           LogLevel loggingLogLevel,
                           Principal actingPrincipal ) {
    this.requestId = UUID.randomUUID().toString();
    this.parameters = parameters;
    this.environment = environment;
    this.transformation = transformation;
    this.reportingTopics = reportingTopics;
    this.loggingLogLevel = loggingLogLevel;
    this.actingPrincipal = actingPrincipal;
  }

  public void setReuseSparkContext( boolean reuseSparkContext ) {
    this.reuseSparkContext = reuseSparkContext;
  }

  public String getRequestId() {
    return requestId;
  }

  public Map<String, Object> getParameters() {
    return parameters;
  }

  public Map<String, Object> getEnvironment() {
    return environment;
  }

  public Transformation getTransformation() {
    return transformation;
  }

  public Map<String, Set<Class<? extends Serializable>>> getReportingTopics() {
    return reportingTopics;
  }

  public Principal getActingPrincipal() {
    return actingPrincipal;
  }

  public LogLevel getLoggingLogLevel() {
    return loggingLogLevel;
  }

  public boolean isToReuseSparkContext() {
    return reuseSparkContext;
  }
}
