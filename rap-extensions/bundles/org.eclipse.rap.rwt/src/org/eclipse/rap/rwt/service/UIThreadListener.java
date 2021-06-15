/*******************************************************************************
 * Copyright (c) 2015 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.rap.rwt.service;

import org.eclipse.swt.internal.SerializableCompatibility;


/**
 * A listener that is notified whenever a session enters the UI thread to process events and when it
 * leaves the UI thread again. Since SWT widgets can only be accessed from the UI thread, it is
 * guaranteed that all UI code will be executed between <code>enterUIThread</code> and
 * <code>leaveUIThread</code>. This listener will always be called in the UIThread.
 * <p>
 * In a RAP application, the UI thread is entered when a client request hits the server, e.g. after
 * some user interaction. When all pending events are processed, the UI thread is left and the
 * response goes back to the client. This listener can be used to track activity or to set up and
 * tear down a context such as a database connection.
 * </p>
 *
 * @since 3.1
 */
public interface UIThreadListener extends SerializableCompatibility {

  /**
   * Called when a UISession has entered the UI thread to process events.
   *
   * @param event an event that provides access to the UI session
   */
  void enterUIThread( UISessionEvent event );

  /**
   * Called when a UISession has processed all pending events and is about to leave the UI thread.
   *
   * @param event an event that provides access to the UI session
   */
  void leaveUIThread( UISessionEvent event );

}
