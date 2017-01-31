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

import org.pentaho.di.engine.api.model.LogicalModelElement;
import org.pentaho.di.engine.api.model.Transformation;
import org.pentaho.di.engine.api.reporting.ReportingEvent;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

/**
 * A request for execution by a remote Engine. All method parameters and return types should be Serializable.
 * <p>
 * Created by hudak on 1/25/17.
 */
public interface ExecutionRequest {
  Map<String, Object> getParameters();

  Map<String, Object> getEnvironment();

  Transformation getTransformation();

  Map<String, Set<Class<? extends Serializable>>> getReportingTopics();

  /**
   * Update this Execution request's state. Usually this is to either claim or close a request. Notifications will be
   * mirrored onto the Transformation's event stream so subscribers may track the request.
   * <p>
   * This notification will be ignored if the request as already been claimed by another service.
   *
   * @param notification a state-change update from the request-processing service
   * @return true if the notification was accepted.
   */
  boolean update( Notification notification );

  /**
   * Update a {@link LogicalModelElement} from this request.
   * This will be routed to the proper event stream on the client.
   * <p>
   * The update will be rejected and method will return false if the request has not yet been claimed or if the request
   * has been canceled
   *
   * @param sourceId {@link LogicalModelElement#getId()}
   * @param value    {@link ReportingEvent#getData()}
   * @return true if update was accepted
   */
  boolean update( String sourceId, Serializable value );
}
