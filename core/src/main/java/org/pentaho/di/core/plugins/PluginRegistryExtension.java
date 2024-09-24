/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.di.core.plugins;

/**
 * Implementations of this class extend the built-in functionality of the plugin registry.
 *
 * User: nbaker Date: 3/14/11
 */
public interface PluginRegistryExtension {
  void init( PluginRegistry registry );

  void searchForType( PluginTypeInterface pluginType );

  String getPluginId( Class<? extends PluginTypeInterface> pluginType, Object pluginClass );
}
