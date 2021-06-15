/*******************************************************************************
 * Copyright (c) 2013 Rüdiger Herrmann and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Rüdiger Herrmann - initial API and implementation
 ******************************************************************************/
package org.eclipse.rap.rwt.application;


/**
 * This interface allows application code to be informed of exceptions that occur while
 * running the event loop.
 *
 * @see Application#setExceptionHandler(ExceptionHandler)
 * @since 2.1
 */
public interface ExceptionHandler {

  /**
   * Called if an exception occurred.
   *
   * @param throwable the exception that occurred, never <code>null</code>
   */
  void handleException( Throwable throwable );

}
