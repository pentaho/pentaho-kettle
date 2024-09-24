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

package org.pentaho.di.ui.trans.dialog;

import java.lang.annotation.Annotation;
import java.util.Map;

import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.plugins.BasePluginType;
import org.pentaho.di.core.plugins.PluginAnnotationType;
import org.pentaho.di.core.plugins.PluginMainClassType;
import org.pentaho.di.core.plugins.PluginTypeInterface;

/**
 * This plugin allows you to capture additional information concerning transformations
 *
 * @author matt
 *
 */
@PluginMainClassType( TransDialogPluginInterface.class )
@PluginAnnotationType( TransDialogPlugin.class )
public class TransDialogPluginType extends BasePluginType implements PluginTypeInterface {

  private static TransDialogPluginType pluginType;

  private TransDialogPluginType() {
    super( TransDialogPlugin.class, "TRANSDIALOG", "Transformation dialog" );
    populateFolders( "transdialog" );
  }

  public static TransDialogPluginType getInstance() {
    if ( pluginType == null ) {
      pluginType = new TransDialogPluginType();
    }
    return pluginType;
  }

  /**
   * Scan & register internal transformation dialog plugins
   */
  @Override
  protected void registerNatives() throws KettlePluginException {
    // No native plugins
  }

  @Override
  protected void registerXmlPlugins() throws KettlePluginException {
    // No longer performed.
  }

  @Override
  protected String extractCategory( Annotation annotation ) {
    return null;
  }

  @Override
  protected String extractDesc( Annotation annotation ) {
    return ( (TransDialogPlugin) annotation ).description();
  }

  @Override
  protected String extractID( Annotation annotation ) {
    return ( (TransDialogPlugin) annotation ).id();
  }

  @Override
  protected String extractName( Annotation annotation ) {
    return ( (TransDialogPlugin) annotation ).name();
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
    return ( (TransDialogPlugin) annotation ).i18nPackageName();
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
    return ( (TransDialogPlugin) annotation ).classLoaderGroup();
  }
}
