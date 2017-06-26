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

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * A service (usually running on a remote daemon) that can accept Execution Requests
 * <p>
 * Created by hudak on 2/7/17.
 */
public interface ExecutionManager extends Serializable {
  /**
   * @return common name this service's backing engine, e.g. "Spark"
   */
  String getEngineType();

  /**
   * @return Name for this cluster, can be shared by other daemons. Assume "default" if unknown
   */
  String getClusterName();

  /**
   * @return environment of backing engine
   */
  Map<String, Object> getEnvironment();

  /**
   * Submit a transformation to this execution manager.
   * <p>
   * If submission was successful, a unique service property will be returned, which can then be used to locate
   * the {@link Execution} created from this request.
   *
   * @param request {@link ExecutionRequest}
   * @return endpointId used to locate an {@link Execution}
   */
  CompletableFuture<String> submit( ExecutionRequest request );
}
