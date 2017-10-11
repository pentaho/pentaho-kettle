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

package org.pentaho.di.core.exception;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.PluginTypeInterface;

/**
 * This Exception is throws when an error occurs loading plugins.
 *
 * @author Matt
 * @since 9-12-2004
 *
 */
public class KettleMissingPluginsException extends KettleException {
  private static final long serialVersionUID = -3008319146447259788L;

  public class PluginDetails {
    public Class<? extends PluginTypeInterface> pluginTypeClass;
    public String pluginId;

    public PluginDetails( Class<? extends PluginTypeInterface> pluginTypeClass, String pluginId ) {
      super();
      this.pluginTypeClass = pluginTypeClass;
      this.pluginId = pluginId;
    }
  }

  private List<PluginDetails> missingPluginDetailsList;

  /**
   * Constructs a new throwable with the specified detail message.
   *
   * @param message
   *          - the detail message. The detail message is saved for later retrieval by the getMessage() method.
   */
  public KettleMissingPluginsException( String message ) {
    super( message );
    this.missingPluginDetailsList = new ArrayList<PluginDetails>();
  }

  /**
   * Add a missing plugin id for a given plugin type.
   *
   * @param pluginTypeClass
   *          The class of the plugin type (ex. StepPluginType.class)
   * @param pluginId
   *          The id of the missing plugin
   */
  public void addMissingPluginDetails( Class<? extends PluginTypeInterface> pluginTypeClass, String pluginId ) {
    missingPluginDetailsList.add( new PluginDetails( pluginTypeClass, pluginId ) );
  }

  public List<PluginDetails> getMissingPluginDetailsList() {
    return missingPluginDetailsList;
  }

  @Override
  public String getMessage() {
    StringBuilder message = new StringBuilder( super.getMessage() );
    message.append( getPluginsMessage() );
    return message.toString();
  }

  public String getPluginsMessage() {
    StringBuilder message = new StringBuilder();
    for ( PluginDetails details : missingPluginDetailsList ) {
      message.append( Const.CR );
      try {
        PluginTypeInterface pluginType = PluginRegistry.getInstance().getPluginType( details.pluginTypeClass );
        message.append( pluginType.getName() );
      } catch ( Exception e ) {
        message.append( "UnknownPluginType-" ).append( details.pluginTypeClass.getName() );
      }
      message.append( " : " ).append( details.pluginId );
    }
    return message.toString();
  }

}
