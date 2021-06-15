/*******************************************************************************
 * Copyright (c) 2013, 2015 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.rap.rwt.scripting;

import org.eclipse.rap.rwt.internal.scripting.ClientFunction;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;


/**
 * A special SWT event listener that is implemented in JavaScript and will be executed on a RAP
 * client. The <code>handleEvent()</code> method of this type will never be called.
 *
 * @since 2.2
 */
public class ClientListener extends ClientFunction implements Listener {

  /**
   * Creates a ClientListener instance with the specified JavaScript code. The JavaScript code
   * must contain a method named <code>handleEvent</code>. This method will be called with a
   * single argument, <code>event</event>.
   *
   * @param scriptCode the JavaScript code of the event handler
   */
 public ClientListener( String scriptCode ) {
    super( scriptCode );
  }

  /**
   * This method will NOT be called on a ClientListener.
   */
  @Override
  public void handleEvent( Event event ) {
  }

}
