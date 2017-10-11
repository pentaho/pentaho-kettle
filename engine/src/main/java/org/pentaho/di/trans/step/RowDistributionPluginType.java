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

package org.pentaho.di.trans.step;

import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.List;
import java.util.Map;

import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.plugins.BasePluginType;
import org.pentaho.di.core.plugins.PluginAnnotationType;
import org.pentaho.di.core.plugins.PluginMainClassType;
import org.pentaho.di.core.plugins.PluginTypeInterface;

/**
 * This class represents the row distribution plugin type.
 *
 * @author matt
 *
 */
@PluginMainClassType( RowDistributionInterface.class )
@PluginAnnotationType( RowDistributionPlugin.class )
public class RowDistributionPluginType extends BasePluginType implements PluginTypeInterface {
  private static RowDistributionPluginType pluginType;

  private RowDistributionPluginType() {
    super( RowDistributionPlugin.class, "ROW_DISTRIBUTION", "Row Distribution" );
    populateFolders( "rowdistribution" );
  }

  public static RowDistributionPluginType getInstance() {
    if ( pluginType == null ) {
      pluginType = new RowDistributionPluginType();
    }
    return pluginType;
  }

  /**
   * Scan & register internal row distribution plugins
   */
  protected void registerNatives() throws KettlePluginException {
    // None at this moment
  }

  protected void registerXmlPlugins() throws KettlePluginException {
    // None at this moment
  }

  @Override
  public void handlePluginAnnotation( Class<?> clazz, Annotation annotation, List<String> libraries,
    boolean nativePluginType, URL pluginFolder ) throws KettlePluginException {
    super.handlePluginAnnotation( clazz, annotation, libraries, nativePluginType, pluginFolder );
  }

  public String[] getNaturalCategoriesOrder() {
    return new String[0];
  }

  @Override
  protected String extractCategory( Annotation annotation ) {
    return "";
  }

  @Override
  protected String extractDesc( Annotation annotation ) {
    return ( (RowDistributionPlugin) annotation ).description();
  }

  @Override
  protected String extractID( Annotation annotation ) {
    return ( (RowDistributionPlugin) annotation ).code();
  }

  @Override
  protected String extractName( Annotation annotation ) {
    return ( (RowDistributionPlugin) annotation ).name();
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

  @Override
  protected void addExtraClasses( Map<Class<?>, String> classMap, Class<?> clazz, Annotation annotation ) {
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
    return ( (RowDistributionPlugin) annotation ).classLoaderGroup();
  }
}
