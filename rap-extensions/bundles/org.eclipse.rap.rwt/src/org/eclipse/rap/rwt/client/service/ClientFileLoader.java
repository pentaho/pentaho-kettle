/*******************************************************************************
 * Copyright (c) 2012, 2016 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.rap.rwt.client.service;


/**
 * The ClientFileLoader service allows loading additional JavaScript and CSS files at runtime.
 *
 * @since 3.1
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ClientFileLoader extends ClientService  {

  /**
   * Instructs the client to immediately load and execute a JavaScript file from the given URL.
   * If the file has already been loaded by the client, it won't be loaded again.
   * <p>
   * Files can be registered with the
   * {@link org.eclipse.rap.rwt.RWT#getResourceManager() ResourceManager}.
   * </p>
   *
   * @param url the URL from which to load the JavaScript file
   */
  void requireJs( String url );

  /**
   * Instructs the client to immediately load and include CSS file from the given URL.
   * If the file has already been loaded by the client, it won't be loaded again.
   * <p>
   * Files can be registered with the
   * {@link org.eclipse.rap.rwt.RWT#getResourceManager() ResourceManager}.
   * </p>
   *
   * @param url the URL from which to load the CSS file
   */
  void requireCss( String url );

}
