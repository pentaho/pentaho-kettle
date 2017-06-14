/*
 * ! ******************************************************************************
 *
 *  Pentaho Data Integration
 *
 *  Copyright (C) 2002-2017 by Pentaho : http://www.pentaho.com
 *
 * ******************************************************************************
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 * *****************************************************************************
 */

package org.pentaho.di.engine.api.remote;

import org.pentaho.di.engine.api.model.Transformation;
import org.pentaho.di.engine.api.reporting.LogEntry;
import org.pentaho.di.engine.api.reporting.LogLevel;

import java.io.Serializable;
import java.security.Principal;
import java.util.Map;
import java.util.Set;

/**
 * A request for execution by a remote Engine. All fields should be Serializable.
 * <p>
 * Created by hudak on 1/25/17.
 */
public final class ExecutionRequest implements Serializable {
  private static final long serialVersionUID = -7835121168360407191L;
  private final Map<String, Object> parameters;
  private final Map<String, Object> environment;
  private final Transformation transformation;
  private final Map<String, Set<Class<? extends Serializable>>> reportingTopics;


  private final Principal actingPrincipal;
  private LogLevel loggingLogLevel;

  public ExecutionRequest( Map<String, Object> parameters, Map<String, Object> environment,
                           Transformation transformation,
                           Map<String, Set<Class<? extends Serializable>>> reportingTopics,
                           LogLevel loggingLogLevel,
                           Principal actingPrincipal ) {
    this.parameters = parameters;
    this.environment = environment;
    this.transformation = transformation;
    this.reportingTopics = reportingTopics;
    this.loggingLogLevel = loggingLogLevel;
    this.actingPrincipal = actingPrincipal;
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
}
