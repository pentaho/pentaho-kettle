/*******************************************************************************
 * Copyright (c) 2002, 2014 Innoopract Informationssysteme GmbH and others.
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

import java.io.InputStream;


/**
 * The resource manager is used to register static resources like images, CSS files etc. in order to
 * make them available at a URL.
 *
 * @since 2.0
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ResourceManager {

  /**
   * Registers a given resource and makes it available for download. The URL for the registered
   * resource can be obtained from {@link #getLocation(String)}.
   *
   * @param name a name that represents the resource
   * @param inputStream a stream to read the content from
   */
  void register( String name, InputStream inputStream );

  /**
   * Unregisters the resource with the given name.
   *
   * @param name the name that represents the resource. Must not be <code>null</code>.
   * @return <code>true</code> if unregistering the resource was successful, <code>false</code>
   *         otherwise.
   */
  boolean unregister( String name );

  /**
   * Determines whether the resource with the given name has been registered.
   *
   * @param name filename which identifies the registered resource. The filename must be relative to
   *          a classpath root, e.g. a gif 'my.gif' located within the package 'org.eclipse.rap' is
   *          identified as 'org/eclipse/rap/my.gif'. Must not be <code>null</code>.
   * @return if the resource is already registered
   */
  boolean isRegistered( String name );

  /**
   * Returns the location within the web-applications context where the resource will be available
   * for download.
   *
   * @param name the name which identifies the registered resource
   * @return the location where the resource will be available for download
   */
  String getLocation( String name );

  /**
   * Returns the content of the registered resource with the given name.
   *
   * @param name the name of the resource, must not be <code>null</code>
   * @return an input stream to the contents of the resource, or <code>null</code> if no such
   *         resource exists
   */
  InputStream getRegisteredContent( String name );

}
