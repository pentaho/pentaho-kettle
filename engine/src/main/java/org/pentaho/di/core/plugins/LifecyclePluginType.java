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

import java.lang.annotation.Annotation;
import java.util.Map;

import org.pentaho.di.core.annotations.LifecyclePlugin;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.gui.GUIOption;
import org.pentaho.di.core.lifecycle.LifecycleListener;

/**
 * This class represents the repository plugin type.
 *
 * @author matt
 *
 */
@PluginMainClassType( LifecycleListener.class )
@PluginExtraClassTypes( classTypes = { GUIOption.class } )
@PluginAnnotationType( LifecyclePlugin.class )
public class LifecyclePluginType extends BasePluginType implements PluginTypeInterface {

  private static LifecyclePluginType pluginType;

  private LifecyclePluginType() {
    super( LifecyclePlugin.class, "LIFECYCLE LISTENERS", "Lifecycle listener plugin type" );
    populateFolders( "repositories" );
  }

  public static LifecyclePluginType getInstance() {
    if ( pluginType == null ) {
      pluginType = new LifecyclePluginType();
    }
    return pluginType;
  }

  /**
   * Let's put in code here to search for the step plugins..
   */
  public void searchPlugins() throws KettlePluginException {
    registerNatives(); // none
    registerAnnotations(); // no longer performed
    registerPluginJars();
    registerXmlPlugins();
  }

  /**
   * Scan & register internal step plugins
   */
  protected void registerNatives() throws KettlePluginException {
    // Up until now, we have no natives.
  }

  /**
   * Scan & register internal repository type plugins
   */
  protected void registerAnnotations() throws KettlePluginException {
    // This is no longer done because it was deemed too slow. Only jar files in
    // the plugins/ folders are scanned for annotations.
  }

  protected void registerXmlPlugins() throws KettlePluginException {
    // Not supported yet.
  }

  @Override
  protected String extractCategory( Annotation annotation ) {
    return "";
  }

  @Override
  protected String extractDesc( Annotation annotation ) {
    return "";
  }

  @Override
  protected String extractID( Annotation annotation ) {
    return ( (LifecyclePlugin) annotation ).id();
  }

  @Override
  protected String extractName( Annotation annotation ) {
    return ( (LifecyclePlugin) annotation ).name();
  }

  @Override
  protected String extractImageFile( Annotation annotation ) {
    return null;
  }

  @Override
  protected boolean extractSeparateClassLoader( Annotation annotation ) {
    return false;
  }

  @Override
  protected String extractI18nPackageName( Annotation annotation ) {
    return null;
  }

  /**
   * Extract extra classes information from a plugin annotation.
   *
   * @param classMap
   * @param annotation
   */
  public void addExtraClasses( Map<Class<?>, String> classMap, Class<?> clazz, Annotation annotation ) {
    // LifecyclePlugin plugin = (LifecyclePlugin) annotation;
    classMap.put( GUIOption.class, clazz.getName() );
    classMap.put( LifecycleListener.class, clazz.getName() );
  }

  @Override
  protected String extractDocumentationUrl( Annotation annotation ) {
    return null;
  }

  @Override
  protected String extractCasesUrl( Annotation annotation ) {
    return null;
  }

  @Override
  protected String extractForumUrl( Annotation annotation ) {
    return null;
  }

  @Override
  protected String extractClassLoaderGroup( Annotation annotation ) {
    return ( (LifecyclePlugin) annotation ).classLoaderGroup();
  }
}
