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
package org.pentaho.di.core.service;

import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.PluginRegistryExtension;
import org.pentaho.di.core.plugins.PluginTypeInterface;

/**
 * The PluginServiceLoaderRegistryExtension just provides a hook to register the new
 * PluginType called "ServiceProviderPluginType". This has to be added to 
 * data-integration/classes/kettle-registry-extensions.xml as a way to insert
 * the new data type without editing the plugins that get added in ClientEnvironment.init()
 * 
 * @author jjarvis
 *
 */
public class PluginServiceLoaderRegistryExtension implements PluginRegistryExtension {

  public void init( PluginRegistry registry ) {
    PluginRegistry.addPluginType( ServiceProviderPluginType.getInstance() );
  }

  public void searchForType( PluginTypeInterface pluginType ) {

  }

  public String getPluginId( Class<? extends PluginTypeInterface> pluginType, Object pluginClass ) {
    return null;
  }

}
