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
   * Scan & register internal step plugins
   */
  @Override
  protected void registerNatives() throws KettlePluginException {
    // Up until now, we have no natives.
  }

  @Override
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
  protected String extractSuggestion( Annotation annotation ) {
    return null;
  }

  @Override
  protected String extractClassLoaderGroup( Annotation annotation ) {
    return ( (LifecyclePlugin) annotation ).classLoaderGroup();
  }
}
