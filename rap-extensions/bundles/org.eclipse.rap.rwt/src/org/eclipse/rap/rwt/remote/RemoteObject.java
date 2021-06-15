/*******************************************************************************
 * Copyright (c) 2011, 2013 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.rap.rwt.remote;

import org.eclipse.rap.json.JsonObject;
import org.eclipse.rap.json.JsonValue;


/**
 * Instances of this interface represent an object on the client that is synchronized with an object
 * on the server. A RemoteObject is used to transfer changes to the remote object. To create a
 * remote object, use {@link Connection#createRemoteObject(String)}.
 *
 * @noimplement This interface is not intended to be implemented by clients.
 * @see Connection
 * @since 2.0
 */
public interface RemoteObject {

  /**
   * Returns the id that is used to synchronize this object with the client. This id is created by
   * the framework and does not change over the lifetime of the object.
   *
   * @since 2.1
   */
  String getId();

  /**
   * Sets the specified property of the remote object to the given value.
   *
   * @param name the name of the property to set
   * @param value the value to set
   */
  void set( String name, int value );

  /**
   * Sets the specified property of the remote object to the given value.
   *
   * @param name the name of the property to set
   * @param value the value to set
   */
  void set( String name, double value );

  /**
   * Sets the specified property of the remote object to the given value.
   *
   * @param name the name of the property to set
   * @param value the value to set
   */
  void set( String name, boolean value );

  /**
   * Sets the specified property of the remote object to the given value.
   *
   * @param name the name of the property to set
   * @param value the value to set
   */
  void set( String name, String value );

  /**
   * Sets the specified property of the remote object to the given value.
   *
   * @param name the name of the property to set
   * @param value the value to set, must not be <code>null</code>
   * @since 2.1
   */
  void set( String name, JsonValue value );

  /**
   * Instructs the remote object to listen or to stop listening on the given type
   * of events. When the client is listening, it must notify the server when an
   * event of the given type occurs.
   *
   * @param eventType the name of event type to listen
   * @param listen true to listen to this type of events, false otherwise
   */
  void listen( String eventType, boolean listen );

  /**
   * Calls the method with the given name on the remote object.
   *
   * @param method the name of the method to call
   * @param parameters the named parameters to pass to the method call
   * @since 2.1
   */
  void call( String method, JsonObject parameters );

  /**
   * Instructs the client to destroy the remote object.
   */
  void destroy();

  /**
   * Set a handler to process incoming operations from the remote object.
   *
   * @param handler the handler that processes incoming operation
   */
  void setHandler( OperationHandler handler );

}
