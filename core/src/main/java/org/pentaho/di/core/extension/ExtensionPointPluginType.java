/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.di.core.extension;

import java.lang.annotation.Annotation;
import java.util.Map;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.plugins.BasePluginType;
import org.pentaho.di.core.plugins.PluginAnnotationType;
import org.pentaho.di.core.plugins.PluginMainClassType;
import org.pentaho.di.core.plugins.PluginTypeInterface;

/**
 * This class represents the extension point plugin type.
 *
 * @author matt
 *
 */
@PluginMainClassType( ExtensionPointInterface.class )
@PluginAnnotationType( ExtensionPoint.class )
public class ExtensionPointPluginType extends BasePluginType implements PluginTypeInterface {
  private static ExtensionPointPluginType pluginType;

  private ExtensionPointPluginType() {
    super( ExtensionPoint.class, "EXTENSION_POINT", "Extension point" );
    populateFolders( "extension_points" );
  }

  public static ExtensionPointPluginType getInstance() {
    if ( pluginType == null ) {
      pluginType = new ExtensionPointPluginType();
    }
    return pluginType;
  }

  @Override
  protected String getXmlPluginFile() {
    return Const.XML_FILE_KETTLE_EXTENSION_POINTS;
  }

  @Override
  protected String getMainTag() {
    return "extension-points";
  }

  @Override
  protected String getSubTag() {
    return "extension-point";
  }

  @Override
  protected String getPath() {
    return "./";
  }

  @Override
  protected void registerXmlPlugins() throws KettlePluginException {
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
    return ( (ExtensionPoint) annotation ).description();
  }

  @Override
  protected String extractID( Annotation annotation ) {
    return ( (ExtensionPoint) annotation ).id();
  }

  @Override
  protected String extractName( Annotation annotation ) {
    return ( (ExtensionPoint) annotation ).extensionPointId();
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
    return ( (ExtensionPoint) annotation ).classLoaderGroup();
  }

  @Override
  protected String extractSuggestion( Annotation annotation ) {
    return null;
  }
}
