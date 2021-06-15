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

import org.eclipse.swt.internal.SerializableCompatibility;


/**
 * A listener that can be attached to an <code>ApplicationContext</code> to receive a notification
 * before the application context is destroyed.
 *
 * @since 2.2
 */
public interface ApplicationContextListener extends SerializableCompatibility {

  /**
   * Called <em>before</em> the related application context is destroyed.
   *
   * @param event an event that provides access to the application context
   */
  void beforeDestroy( ApplicationContextEvent event );

}
