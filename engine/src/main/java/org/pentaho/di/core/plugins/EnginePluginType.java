/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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

import org.pentaho.di.core.Const;
import org.pentaho.di.core.annotations.EnginePlugin;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.engine.api.Engine;

import java.lang.annotation.Annotation;
import java.util.Map;

/**
 * Plugin for interacting with {@link Engine} implementations.
 *
 */
@PluginMainClassType( Engine.class )
@PluginAnnotationType( EnginePlugin.class )
public class EnginePluginType extends BasePluginType implements PluginTypeInterface {

  private static EnginePluginType enginePluginType;

  private EnginePluginType() {
    super( EnginePlugin.class, "ENGINE_PLUGIN", "Engine Plugin" );
  }

  public static EnginePluginType getInstance() {
    if ( enginePluginType == null ) {
      enginePluginType = new EnginePluginType();
    }
    return enginePluginType;
  }

  @Override
  protected String getXmlPluginFile() {
    return Const.XML_FILE_KETTLE_ENGINES;
  }

  @Override
  protected String getMainTag() {
    return "engines";
  }

  @Override
  protected String getSubTag() {
    return "engine";
  }

  @Override
  protected String getPath() {
    return "./";
  }

  @Override
  protected boolean isReturn() {
    return true;
  }

  @Override
  protected void registerXmlPlugins() throws KettlePluginException {
  }

  @Override
  protected String extractCategory( Annotation annotation ) {
    return null;
  }

  @Override
  protected String extractDesc( Annotation annotation ) {
    return ( (EnginePlugin) annotation ).description();
  }

  @Override
  protected String extractID( Annotation annotation ) {
    return ( (EnginePlugin) annotation ).id();
  }

  @Override
  protected String extractName( Annotation annotation ) {
    return ( (EnginePlugin) annotation ).name();
  }

  @Override
  protected String extractImageFile( Annotation annotation ) {
    return null;
  }

  @Override
  protected boolean extractSeparateClassLoader( Annotation annotation ) {
    return ( (EnginePlugin) annotation ).isSeparateClassLoaderNeeded();
  }

  @Override
  protected String extractI18nPackageName( Annotation annotation ) {
    return null;
  }

  @Override
  protected void addExtraClasses( Map<Class<?>, String> classMap, Class<?> clazz, Annotation annotation ) {
  }

  @Override
  protected String extractDocumentationUrl( Annotation annotation ) {
    return ( (EnginePlugin) annotation ).documentationUrl();
  }

  @Override
  protected String extractCasesUrl( Annotation annotation ) {
    return ( (EnginePlugin) annotation ).casesUrl();
  }

  @Override
  protected String extractForumUrl( Annotation annotation ) {
    return ( (EnginePlugin) annotation ).forumUrl();
  }

  @Override
  protected String extractClassLoaderGroup( Annotation annotation ) {
    return ( (EnginePlugin) annotation ).classLoaderGroup();
  }
}
