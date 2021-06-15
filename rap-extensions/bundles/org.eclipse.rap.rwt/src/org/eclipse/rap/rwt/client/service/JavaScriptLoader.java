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
 * The JavaScriptLoader service allows loading additional JavaScript files at runtime.
 *
 * @since 2.0
 * @noimplement This interface is not intended to be implemented by clients.
 * @deprecated Use {@link ClientFileLoader} instead.
 */
@Deprecated
public interface JavaScriptLoader extends ClientService  {

  /**
   * Instructs the client to immediately load and evaluate a JavaScript file from the given URL.
   *
   * If the file has already been loaded by the client, nothing happens.
   * The {@link org.eclipse.rap.rwt.RWT#getResourceManager() ResourceManager} can be used to
   * register a file that can be loaded by the JavaScriptLoader. Since 2.2 the file can also loaded
   * from any other server the client can connect to.
   *
   * @param url the URL from which to load the JavaScript file
   */
  void require( String url );

}
