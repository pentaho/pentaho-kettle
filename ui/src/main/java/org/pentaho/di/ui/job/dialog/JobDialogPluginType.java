/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2019 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.ui.job.dialog;

import java.lang.annotation.Annotation;
import java.util.Map;

import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.plugins.BasePluginType;
import org.pentaho.di.core.plugins.PluginAnnotationType;
import org.pentaho.di.core.plugins.PluginMainClassType;
import org.pentaho.di.core.plugins.PluginTypeInterface;

/**
 * This plugin allows you to capture additional information concerning job entries.
 *
 * @author matt
 *
 */
@PluginMainClassType( JobDialogPluginInterface.class )
@PluginAnnotationType( JobDialogPlugin.class )
public class JobDialogPluginType extends BasePluginType implements PluginTypeInterface {
  // private static Class<?> PKG = JobDialogPluginType.class; // for i18n purposes, needed by Translator2!!

  private static JobDialogPluginType pluginType;

  private JobDialogPluginType() {
    super( JobDialogPlugin.class, "JOBDIALOG", "Job dialog" );
    populateFolders( "jobdialog" );
  }

  public static JobDialogPluginType getInstance() {
    if ( pluginType == null ) {
      pluginType = new JobDialogPluginType();
    }
    return pluginType;
  }

  /**
   * Scan & register internal step plugins
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
    return ( (JobDialogPlugin) annotation ).description();
  }

  @Override
  protected String extractID( Annotation annotation ) {
    return ( (JobDialogPlugin) annotation ).id();
  }

  @Override
  protected String extractName( Annotation annotation ) {
    return ( (JobDialogPlugin) annotation ).name();
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
    return ( (JobDialogPlugin) annotation ).i18nPackageName();
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
    return ( (JobDialogPlugin) annotation ).classLoaderGroup();
  }
}
