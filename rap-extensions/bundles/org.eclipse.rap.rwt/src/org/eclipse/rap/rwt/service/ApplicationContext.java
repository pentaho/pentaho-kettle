/*******************************************************************************
 * Copyright (c) 2011, 2014 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.rap.rwt.service;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.application.ApplicationRunner;


/**
 * An application context represents a running instance of a RAP application. This context is shared
 * by all users who access this application. The current application context can be acquired by
 * {@link RWT#getApplicationContext()}. It can be used to store any data that is shared between all
 * UI sessions of an application, and to acquire application-scoped instances of framework services
 * such as the resource manager.
 * <p>
 * The application context is bound to the servlet context of the hosting web application. It is
 * destroyed when the web application ends (i.e. the servlet context is destroyed) or when the
 * application is explicitly stopped by calling {@link ApplicationRunner#stop()}.
 * </p>
 * <p>
 * The application context is <em>thread safe</em>, it can be accessed concurrently from different
 * threads.
 * </p>
 *
 * @see org.eclipse.rap.rwt.RWT
 * @since 2.0
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ApplicationContext {

  /**
   * Stores the given value in this application context, associated with the given name. If another
   * value has already been stored with the given name, the old value is overwritten.
   *
   * @param name the name to associate the value with
   * @param value the object to be stored
   */
  void setAttribute( String name, Object value );

  /**
   * Returns the value which is stored under the given name in this application context.
   *
   * @param name the name whose associated value is requested
   * @return the object that is stored, or <code>null</code> if no object has been stored by that
   *         name
   */
  Object getAttribute( String name );

  /**
   * Removes the object which is stored under the given name in this application context. If no
   * value object was stored under the given name, this method does nothing.
   */
  void removeAttribute( String name );

  /**
   * Adds a <code>UIThreadListener</code> to this application context. UIThreadListeners are used to
   * receive a notification before the UIThread is entered and after it is left. If the given
   * listener was already added the method has no effect.
   *
   * @param listener the listener to be added
   * @since 3.1
   */
  void addUIThreadListener( UIThreadListener listener );

  /**
   * Removes a <code>UIThreadListener</code> from this application context. UIThreadListeners are
   * used to receive a notification before the UIThread is entered and after it is left. If the
   * given listener was not added to this application context before, this method has no effect.
   *
   * @param listener the listener to be removed
   * @since 3.1
   */
  void removeUIThreadListener( UIThreadListener listener );

  /**
   * Adds an <code>ApplicationContextListener</code> to this application context.
   * ApplicationContextListeners are used to receive a notification before the application context
   * is destroyed. If the given listener was already added the method has no effect.
   * <p>
   * If the ApplicationContext is already deactivated or is about to be deactivated, the listener
   * will not be added and this method will return <code>false</code>. A return value of
   * <code>true</code> asserts that this listener is registered and will be called on destroy.
   * </p>
   *
   * @param listener the listener to be added
   * @return <code>true</code> if the listener is registered, <code>false</code> if not
   * @since 2.2
   */
  boolean addApplicationContextListener( ApplicationContextListener listener );

  /**
   * Removes an <code>ApplicationContextListener</code> from this application context.
   * ApplicationContextListeners are used to receive a notification before the application context
   * is destroyed. If the given listener was not added to this application context before, this
   * method has no effect.
   * <p>
   * If the ApplicationContext is already deactivated or is about to be deactivated, the listener
   * will not be removed and this method will return <code>false</code>. A return value of
   * <code>true</code> asserts that this listener is not registered and will not be called anymore.
   * </p>
   *
   * @param listener the listener to be removed
   * @return<code>true</code> if the listener was removed, <code>false</code> if not
   * @since 2.2
   */
  boolean removeApplicationContextListener( ApplicationContextListener listener );

  /**
   * Returns the instance of the resource manager for this application context. The resource manager
   * is used to register static resources such as images of JavaScript files.
   *
   * @return the resource manager for this application context
   * @see ResourceManager
   */
  ResourceManager getResourceManager();

  /**
   * Returns the instance of the service manager for this application context. The service manager
   * is used to register and unregister service handlers.
   *
   * @return the service manager instance for this application context
   * @see ServiceManager
   * @see ServiceHandler
   */
  ServiceManager getServiceManager();

}
