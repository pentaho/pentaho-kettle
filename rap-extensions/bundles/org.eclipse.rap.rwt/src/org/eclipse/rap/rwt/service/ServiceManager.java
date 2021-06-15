/*******************************************************************************
 * Copyright (c) 2007, 2014 Innoopract Informationssysteme GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/
package org.eclipse.rap.rwt.service;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.application.ApplicationConfiguration;


/**
 * The service manager is used to register service handlers with the framework. An instance can be
 * obtained from {@link RWT#getServiceManager()}. Service handlers can also be registered in an
 * {@link ApplicationConfiguration}.
 *
 * @see ServiceHandler
 * @since 2.0
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ServiceManager {

  /**
   * Registers a new service handler with the given unique id. The id is used to address the service
   * handler in a request. The URL to address a registered service handler can be obtained from
   * {@link #getServiceHandlerUrl(String)}.
   *
   * @param id the id to register this service handler with
   * @param serviceHandler the service handler to register
   * @throws IllegalArgumentException if a service handler is already registered with the given id
   * @see ServiceHandler
   */
  void registerServiceHandler( String id, ServiceHandler serviceHandler );

  /**
   * Unregisters the service handler with the given id. If no service hander has been registered
   * with the given id, nothing happens.
   *
   * @param id the id of the service handler to remove
   * @see ServiceHandler
   */
  void unregisterServiceHandler( String id );

  /**
   * Returns the URL to access a service handler. The URL contains a parameter, it is legal to
   * append more parameters by appending <code>"&key=value..."</code>.
   *
   * @param id the id that the service handler has been registered with
   * @return the URL to address the service handler
   */
  String getServiceHandlerUrl( String id );

}
