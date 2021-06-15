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
 * The exit confirmation service allows applications to show a confirmation dialog before the user
 * leaves the application, e.g. by closing the browser tab or navigating away from the page. This
 * feature is supported by all relevant web browsers and may also be supported by other clients.
 * Please note that some browsers ignore the message and only display a generic confirmation dialog
 * if a message is set.
 *
 * @since 2.0
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ExitConfirmation extends ClientService {

  /**
   * Enables the exit confirmation with the given message if the message is not null. Setting the
   * message to <code>null</code> disables the exit confirmation.
   *
   * @param message the message to display when the user is about to leave the application
   */
  void setMessage( String message );

  /**
   * Returns the current exit confirmation message.
   *
   * @return the current exit confirmation message or null, it the exit confirmation is disabled
   */
  String getMessage();

}
