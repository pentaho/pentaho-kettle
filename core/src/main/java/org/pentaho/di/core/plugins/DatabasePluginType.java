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

import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.DatabaseInterface;
import org.pentaho.di.core.exception.KettlePluginException;

/**
 * This class represents the step plugin type.
 *
 * @author matt
 *
 */
@PluginMainClassType( DatabaseInterface.class )
@PluginAnnotationType( DatabaseMetaPlugin.class )
public class DatabasePluginType extends BasePluginType implements PluginTypeInterface {
  private static DatabasePluginType pluginType;

  private DatabasePluginType() {
    super( DatabaseMetaPlugin.class, "DATABASE", "Database" );
    populateFolders( "databases" );
  }

  public static DatabasePluginType getInstance() {
    if ( pluginType == null ) {
      pluginType = new DatabasePluginType();
    }
    return pluginType;
  }

  @Override
  protected String getXmlPluginFile() {
    return Const.XML_FILE_KETTLE_DATABASE_TYPES;
  }

  @Override
  protected String getMainTag() {
    return "database-types";
  }

  @Override
  protected String getSubTag() {
    return "database-type";
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
    return ( (DatabaseMetaPlugin) annotation ).typeDescription();
  }

  @Override
  protected String extractID( Annotation annotation ) {
    return ( (DatabaseMetaPlugin) annotation ).type();
  }

  @Override
  protected String extractName( Annotation annotation ) {
    return ( (DatabaseMetaPlugin) annotation ).typeDescription();
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
    return ( (DatabaseMetaPlugin) annotation ).classLoaderGroup();
  }
}
