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

import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * This interface describes the plugin itself, the IDs it listens too, what libraries (jar files) it uses, the names,
 * the i18n detailes, etc.
 *
 * @author matt
 *
 */
public interface PluginInterface {

  /**
   * @return All the possible IDs that this plugin corresponds with.<br>
   *         Multiple IDs are typically used when you migrate 2 different plugins into a single one with the same
   *         functionality.<br>
   *         It can also happen if you deprecate an older plugin and you want to have a new one provide compatibility
   *         for it.<br>
   */
  public String[] getIds();

  /**
   * @return The type of plugin
   */
  public Class<? extends PluginTypeInterface> getPluginType();

  /**
   * @return The main class assigned to this Plugin.
   */
  public Class<?> getMainType();

  /**
   * @return The libraries (jar file names) that are used by this plugin
   */
  public List<String> getLibraries();

  /**
   * @return The name of the plugin
   */
  public String getName();

  /**
   * @return The description of the plugin
   */
  public String getDescription();

  /**
   * @return The location of the image (icon) file for this plugin
   */
  public String getImageFile();

  /**
   * @return The category of this plugin or null if this is not applicable
   */
  public String getCategory();

  /**
   * @return True if a separate class loader is needed every time this class is instantiated
   */
  public boolean isSeparateClassLoaderNeeded();

  /**
   * @return true if this is considered to be a standard native plugin.
   */
  public boolean isNativePlugin();

  /**
   * @return All the possible class names that can be loaded with this plugin, split up by type.
   */
  public Map<Class<?>, String> getClassMap();

  /**
   * @param id
   *          the plugin id to match
   * @return true if one of the ids matches the given argument. Return false if it doesn't.
   */
  public boolean matches( String id );

  /**
   * @return An optional location to a help file that the plugin can refer to in case there is a loading problem. This
   *         usually happens if a jar file is not installed correctly (class not found exceptions) etc.
   */
  public String getErrorHelpFile();

  public URL getPluginDirectory();

  /**
   * @return the documentationUrl
   */
  public String getDocumentationUrl();

  /**
   * @param documentationUrl
   *          the documentationUrl to set
   */
  public void setDocumentationUrl( String documentationUrl );

  /**
   * @return The cases URL of the plugin
   */
  public String getCasesUrl();

  /**
   * @param casesUrl
   *          the cases URL to set for this plugin
   */
  public void setCasesUrl( String casesUrl );

  /**
   * @return the forum URL
   */
  public String getForumUrl();

  /**
   * @param forumUrl
   *          the forum URL to set
   */
  public void setForumUrl( String forumUrl );

  /**
   * @return The group to which this class loader belongs.  
   * Returns null if the plugin does not belong to a group (the default)
   */
  public String getClassLoaderGroup();

  /**
   * @param group The group to which this class loader belongs.  
   * Set to null if the plugin does not belong to a group (the default)
   */
  public void setClassLoaderGroup( String group );

}
