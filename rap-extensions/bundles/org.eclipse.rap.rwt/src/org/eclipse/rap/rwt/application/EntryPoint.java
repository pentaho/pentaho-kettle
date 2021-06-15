/*******************************************************************************
 * Copyright (c) 2002, 2013 Innoopract Informationssysteme GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/
package org.eclipse.rap.rwt.application;


/**
 * An entrypoint creates the main UI of a RAP application. It is registered with the framework and
 * mapped to a URL path. The framework will create a separate instance of an entrypoint for every UI
 * session.
 * <p>
 * It is recommended to extend {@link AbstractEntryPoint} rather than to implement this interface.
 * </p>
 *
 * @see AbstractEntryPoint
 * @since 2.0
 */
public interface EntryPoint {

  /**
   * This method is called to initialize the UI. Implementations will create a display, one or more
   * shells, and add content. If and only if the application is supposed to run in SWT_COMPATIBILITY
   * mode, this methods must run the SWT main loop if.
   *
   * @return exit status, reserved for future use
   */
  int createUI();

}
