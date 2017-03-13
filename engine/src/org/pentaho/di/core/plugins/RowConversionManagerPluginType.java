/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Pentaho : http://www.pentaho.com
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

import org.pentaho.di.core.annotations.RowConversionManagerPlugin;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.engine.api.converter.RowConversionManager;

import java.lang.annotation.Annotation;
import java.util.Map;

/**
 * Plugin for interacting with {@link RowConversionManager} implementations.
 */
@PluginMainClassType( RowConversionManager.class )
@PluginAnnotationType( RowConversionManagerPlugin.class )
public class RowConversionManagerPluginType extends BasePluginType implements PluginTypeInterface {

  private static RowConversionManagerPluginType rowConversionPluginType;

  private RowConversionManagerPluginType() {
    super( RowConversionManagerPlugin.class, "ROW_CONVERSION_MANAGER_PLUGIN", "Row Conversion Manager Plugin" );
  }

  public static RowConversionManagerPluginType getInstance() {
    if ( rowConversionPluginType == null ) {
      rowConversionPluginType = new RowConversionManagerPluginType();
    }
    return rowConversionPluginType;
  }

  @Override
  protected void registerNatives() throws KettlePluginException {
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
    return ( (RowConversionManagerPlugin) annotation ).description();
  }

  @Override
  protected String extractID( Annotation annotation ) {
    return ( (RowConversionManagerPlugin) annotation ).id();
  }

  @Override
  protected String extractName( Annotation annotation ) {
    return ( (RowConversionManagerPlugin) annotation ).name();
  }

  @Override
  protected String extractImageFile( Annotation annotation ) {
    return null;
  }

  @Override
  protected boolean extractSeparateClassLoader( Annotation annotation ) {
    return ( (RowConversionManagerPlugin) annotation ).isSeparateClassLoaderNeeded();
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
    return ( (RowConversionManagerPlugin) annotation ).documentationUrl();
  }

  @Override
  protected String extractCasesUrl( Annotation annotation ) {
    return ( (RowConversionManagerPlugin) annotation ).casesUrl();
  }

  @Override
  protected String extractForumUrl( Annotation annotation ) {
    return ( (RowConversionManagerPlugin) annotation ).forumUrl();
  }

  @Override
  protected String extractClassLoaderGroup( Annotation annotation ) {
    return ( (RowConversionManagerPlugin) annotation ).classLoaderGroup();
  }
}
