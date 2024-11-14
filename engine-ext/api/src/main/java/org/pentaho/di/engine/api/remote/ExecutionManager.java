/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


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
