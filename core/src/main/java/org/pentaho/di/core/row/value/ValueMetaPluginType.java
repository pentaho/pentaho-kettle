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

package org.pentaho.di.core.row.value;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;

import org.apache.commons.vfs2.FileObject;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.plugins.BasePluginType;
import org.pentaho.di.core.plugins.PluginAnnotationType;
import org.pentaho.di.core.plugins.PluginFolderInterface;
import org.pentaho.di.core.plugins.PluginMainClassType;
import org.pentaho.di.core.plugins.PluginTypeInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.core.xml.XMLHandler;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * This class represents the value meta plugin type.
 *
 * @author matt
 *
 */

@PluginMainClassType( ValueMetaInterface.class )
@PluginAnnotationType( ValueMetaPlugin.class )
public class ValueMetaPluginType extends BasePluginType implements PluginTypeInterface {

  private static ValueMetaPluginType valueMetaPluginType;

  private ValueMetaPluginType() {
    super( ValueMetaPlugin.class, "VALUEMETA", "ValueMeta" );
    populateFolders( "valuemeta" );
  }

  public static ValueMetaPluginType getInstance() {
    if ( valueMetaPluginType == null ) {
      valueMetaPluginType = new ValueMetaPluginType();
    }
    return valueMetaPluginType;
  }

  @Override
  protected String getXmlPluginFile() {
    return Const.XML_FILE_KETTLE_VALUEMETA_PLUGINS;
  }

  @Override
  protected String getAlternativePluginFile() {
    return Const.KETTLE_VALUEMETA_PLUGINS_FILE;
  }

  @Override
  protected String getMainTag() {
    return "valuemeta-plugins";
  }

  @Override
  protected String getSubTag() {
    return "valuemeta-plugin";
  }

  @Override
  protected void registerXmlPlugins() throws KettlePluginException {
    for ( PluginFolderInterface folder : pluginFolders ) {

      if ( folder.isPluginXmlFolder() ) {
        List<FileObject> pluginXmlFiles = findPluginXmlFiles( folder.getFolder() );
        for ( FileObject file : pluginXmlFiles ) {

          try {
            Document document = XMLHandler.loadXMLFile( file );
            Node pluginNode = XMLHandler.getSubNode( document, "plugin" );
            if ( pluginNode != null ) {
              registerPluginFromXmlResource( pluginNode, KettleVFS.getFilename( file.getParent() ), this
                .getClass(), false, file.getParent().getURL() );
            }
          } catch ( Exception e ) {
            // We want to report this plugin.xml error, perhaps an XML typo or something like that...
            //
            log.logError( "Error found while reading step plugin.xml file: " + file.getName().toString(), e );
          }
        }
      }
    }
  }

  @Override
  protected String extractCategory( Annotation annotation ) {
    return null;
  }

  @Override
  protected String extractDesc( Annotation annotation ) {
    return ( (ValueMetaPlugin) annotation ).description();
  }

  @Override
  protected String extractID( Annotation annotation ) {
    return ( (ValueMetaPlugin) annotation ).id();
  }

  @Override
  protected String extractName( Annotation annotation ) {
    return ( (ValueMetaPlugin) annotation ).name();
  }

  @Override
  protected String extractImageFile( Annotation annotation ) {
    return null;
  }

  @Override
  protected boolean extractSeparateClassLoader( Annotation annotation ) {
    return ( (ValueMetaPlugin) annotation ).isSeparateClassLoaderNeeded();
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
    return ( (ValueMetaPlugin) annotation ).documentationUrl();
  }

  @Override
  protected String extractCasesUrl( Annotation annotation ) {
    return ( (ValueMetaPlugin) annotation ).casesUrl();
  }

  @Override
  protected String extractForumUrl( Annotation annotation ) {
    return ( (ValueMetaPlugin) annotation ).forumUrl();
  }

  @Override
  protected String extractClassLoaderGroup( Annotation annotation ) {
    return ( (ValueMetaPlugin) annotation ).classLoaderGroup();
  }
}
