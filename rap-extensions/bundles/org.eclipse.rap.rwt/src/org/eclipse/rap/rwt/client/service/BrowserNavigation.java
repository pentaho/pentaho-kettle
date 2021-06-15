/*******************************************************************************
 * Copyright (c) 2009, 2012 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 *    Ralf Zahn (ARS) - browser history support (Bug 283291)
 ******************************************************************************/
package org.eclipse.rap.rwt.client.service;


/**
 * The browser navigation provides methods to access a web browser's history for navigating within
 * the application. It is possible to create a history entry at the top of the history stack and to
 * handle a navigation change event.
 *
 * @since 2.0
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface BrowserNavigation extends ClientService {

  /**
   * Creates an entry in the browser history.
   *
   * @param state an unique id to identify the history entry, used in the fragment part of the
   *        URL. Must neither be <code>null</code> nor empty
   * @param title a human-readable text to identify the history entry in the browser's UI or
   *        <code>null</code>
   */
  void pushState( String state, String title );

  /**
   * Adds a listener to the browser navigation. This listener is notified whenever the URL fragment
   * in the browser changes. If the listener has already been added, this method does nothing.
   *
   * @param listener the listener to add, must not be <code>null</code>
   */
  void addBrowserNavigationListener( BrowserNavigationListener listener );

  /**
   * Removes a listener from the navigation support. If the listener has not been added before, this
   * method does nothing.
   *
   * @param listener the listener to remove, must not be <code>null</code>
   */
  void removeBrowserNavigationListener( BrowserNavigationListener listener );

}
