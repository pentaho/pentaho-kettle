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

package org.pentaho.di.trans.step;

import java.lang.annotation.Annotation;
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
  @Override
  protected void registerNatives() throws KettlePluginException {
    // None at this moment
  }

  @Override
  protected void registerXmlPlugins() throws KettlePluginException {
    // None at this moment
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
  protected String extractSuggestion( Annotation annotation ) {
    return null;
  }

  @Override
  protected String extractClassLoaderGroup( Annotation annotation ) {
    return ( (RowDistributionPlugin) annotation ).classLoaderGroup();
  }
}
