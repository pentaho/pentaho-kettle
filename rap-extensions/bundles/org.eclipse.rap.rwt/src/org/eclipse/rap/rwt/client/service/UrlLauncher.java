/*******************************************************************************
 * Copyright (c) 2012 EclipseSource and others.
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
 * The UrlLauncher service allows loading an URL in an external window, application or save dialog.
 *
 * @since 2.0
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface UrlLauncher extends ClientService {

  /**
   * Opens the given URL.
   *
   * Any HTTP URL or relative URL will be opened in a new window.
   * Modern browser may block any attempt to open new windows, but will usually prompt the user to
   * accept or ignore. Even if accepted, the decision may be applied to only this attempt, or only
   * to future attempts. It could also trigger a document reload, causing a session restart.
   *
   * Non-HTTP URLs like "mailto" will not create a new browser window, but require the client
   * to have a matching protocol handler registered.
   *
   * @param url the URL to open
   */
  void openURL( String url );

}
