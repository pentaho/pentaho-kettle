/*******************************************************************************
 * Copyright (c) 2002, 2012 Innoopract Informationssysteme GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/
package org.eclipse.rap.rwt.service;

import org.eclipse.rap.rwt.application.Application;
import org.eclipse.rap.rwt.application.ApplicationConfiguration;


/**
 * A setting store factory is responsible for creating and initializing a specific kind of setting
 * store. To contribute a custom setting store implementation, an implementation of this interface
 * must be provided in an {@link ApplicationConfiguration} (see
 * {@link Application#setSettingStoreFactory(SettingStoreFactory)}).
 * <p>
 * At runtime, the framework will use a single factory implementation to create new setting store
 * instances. If no custom factory is specified, the {@link FileSettingStoreFactory} will be used
 * as default.
 * </p>
 *
 * @since 2.0
 * @see Application#setSettingStoreFactory(SettingStoreFactory)
 */
public interface SettingStoreFactory {

  /**
   * Creates and initializes a new specific setting store instance.
   *
   * @param id the ID for the setting store to create, must not be <code>null</code> or empty
   * @return the created setting store, never <code>null</code>
   * @throws IllegalArgumentException if the given id is empty
   */
  SettingStore createSettingStore( String id );

}
