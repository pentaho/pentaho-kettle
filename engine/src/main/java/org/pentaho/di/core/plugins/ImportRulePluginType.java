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

import java.lang.annotation.Annotation;
import java.util.Map;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.annotations.ImportRulePlugin;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.imp.rule.ImportRuleInterface;

/**
 * This is the import rule plugin type.
 *
 * @author matt
 *
 */
@PluginMainClassType( ImportRuleInterface.class )
@PluginAnnotationType( ImportRulePlugin.class )
public class ImportRulePluginType extends BasePluginType implements PluginTypeInterface {

  private static ImportRulePluginType pluginType;

  private ImportRulePluginType() {
    super( ImportRulePlugin.class, "IMPORT_RULE", "Import rule" );
    populateFolders( "rules" );
  }

  public static ImportRulePluginType getInstance() {
    if ( pluginType == null ) {
      pluginType = new ImportRulePluginType();
    }
    return pluginType;
  }

  @Override
  protected String getXmlPluginFile() {
    return Const.XML_FILE_KETTLE_IMPORT_RULES;
  }

  @Override
  protected String getMainTag() {
    return "rules";
  }

  @Override
  protected String getSubTag() {
    return "rule";
  }

  /**
   * Scan & register internal step plugins
   */
  protected void registerAnnotations() throws KettlePluginException {
    // This is no longer done because it was deemed too slow. Only jar files in the plugins/ folders are scanned for
    // annotations.
  }

  protected void registerXmlPlugins() throws KettlePluginException {
    // Not supported, ignored
  }

  @Override
  protected String extractCategory( Annotation annotation ) {
    return "";
  }

  @Override
  protected String extractDesc( Annotation annotation ) {
    return ( (ImportRulePlugin) annotation ).description();
  }

  @Override
  protected String extractID( Annotation annotation ) {
    return ( (ImportRulePlugin) annotation ).id();
  }

  @Override
  protected String extractName( Annotation annotation ) {
    return ( (ImportRulePlugin) annotation ).name();
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
    return ( (ImportRulePlugin) annotation ).i18nPackageName();
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
    return ( (ImportRulePlugin) annotation ).classLoaderGroup();
  }
}
