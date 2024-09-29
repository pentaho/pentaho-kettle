/*
 * ! ******************************************************************************
 *
 *  Pentaho Data Integration
 *
 *  Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

/**
 * Service representing a single transformation execution on a remote engine.
 * <p>
 * This service serves two purposes. First publishes the original request, to make the execution context available for
 * workers. Second, it functions as a tunnel for events to flow from workers back to the client.
 * <p>
 * This class is parametrized to remove compiler dependencies on event tunneling implementation.
 * `pentaho-object-tunnel` is recommended.
 * <p>
 * Created by hudak on 2/7/17.
 *
 * @param <T> Serialized event type
 */
public interface Execution<T extends Serializable> extends Serializable, AutoCloseable {
  /**
   * @return Retrieves the request associated with
   * this Execution, null if it has already been claimed.
   */
  ExecutionRequest getRequest();


  /**
   * Releases a request for execution by another handler.
   */
  void releaseRequest();

  /**
   * Send an event back to the client. Events may be wrapped if additional serialization logic is needed.
   * Usually, these events will be TunneledPayload objects from the pentaho-object-tunnel bundle.
   * <p>
   * This event will be added to the queue to send to the client
   *
   * @param event Serialized event data
   */
  void update( T event );

  /**
   * Communicate a failure.
   *
   * @param throwable
   */
  void closeExceptionally( ExecutionException throwable );

  /**
   * Open a stream for nosy clients to get live feedback.
   * <p>
   * Create a serialized stream of events sent to this execution via {@link #update(T)}
   * If using the pentaho-object-tunnel, wrap with a TunnelInput
   * <p>
   * Behavior may be non-deterministic if more than one stream is opened
   *
   * @return Serialized stream of events
   */
  InputStream eventStream() throws IOException;

  void stop();
}
