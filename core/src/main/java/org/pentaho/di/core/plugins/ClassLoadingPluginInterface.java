/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/



package org.pentaho.di.core.plugins;

/**
 *
 * Plugins implementing this type will be asked to load classes instead of having it handled by the PluginRegistry.
 *
 * User: nbaker Date: 12/12/10
 */
public interface ClassLoadingPluginInterface {
  <T> T loadClass( Class<T> pluginClass );

  ClassLoader getClassLoader();
}
