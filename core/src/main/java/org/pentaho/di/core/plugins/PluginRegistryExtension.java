/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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
