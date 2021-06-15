/*******************************************************************************
 * Copyright (c) 2013 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.rap.rwt.service;

import java.util.EventObject;


/**
 * Events of this type signal a state change of an application context.
 *
 * @since 2.2
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class ApplicationContextEvent extends EventObject {
  private static final long serialVersionUID = 1L;

  /**
   * Creates a new instance of this class.
   *
   * @param context the application context for this event
   */
  public ApplicationContextEvent( ApplicationContext context ) {
    super( context );
  }

  /**
   * Returns the application context that this event is related to.
   *
   * @return the application context
   */
  public ApplicationContext getApplicationContext() {
    return ( ApplicationContext )getSource();
  }

}
