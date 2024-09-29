/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2022 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
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
