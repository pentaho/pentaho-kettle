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

import java.net.URL;
import java.util.List;

import org.pentaho.di.core.exception.KettlePluginException;

/**
 * This interface describes a plugin type.<br>
 * It expresses the ID and the name of the plugin type.<br>
 * Then it also explains what the plugin meta class is called and classes the plugin interface itself.<br>
 * It also explains us where to load plugins of this type.<br>
 *
 * @author matt
 *
 */
public interface PluginTypeInterface {

  /**
   * Register an additional class type to be managed by the plugin system.
   *
   * @param clz
   *          category class, usually an interface
   * @param xmlNodeName
   *          xml node to search for a class name
   */
  void addObjectType( Class<?> clz, String xmlNodeName );

  /**
   * @return The ID of this plugin type
   */
  String getId();

  /**
   * @return The name of this plugin
   */
  String getName();

  /**
   * @return The places where we should look for plugins, both as plugin.xml and as
   */
  List<PluginFolderInterface> getPluginFolders();

  /**
   *
   * @throws KettlePluginException
   */
  void searchPlugins() throws KettlePluginException;

  /**
   * Handle an annotated plugin
   *
   * @param clazz
   *          The class to use
   * @param annotation
   *          The annotation to get information from
   * @param libraries
   *          The libraries to add
   * @param nativePluginType
   *          Is this a native plugin?
   * @param pluginFolder
   *          The plugin folder to use
   * @throws KettlePluginException
   */
  void handlePluginAnnotation( Class<?> clazz, java.lang.annotation.Annotation annotation,
    List<String> libraries, boolean nativePluginType, URL pluginFolder ) throws KettlePluginException;

  default boolean isFragment() {
    return false;
  }
}
