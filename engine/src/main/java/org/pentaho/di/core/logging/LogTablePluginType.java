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

package org.pentaho.di.core.logging;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;

import org.apache.commons.vfs2.FileObject;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.plugins.BasePluginType;
import org.pentaho.di.core.plugins.PluginAnnotationType;
import org.pentaho.di.core.plugins.PluginFolderInterface;
import org.pentaho.di.core.plugins.PluginMainClassType;
import org.pentaho.di.core.plugins.PluginTypeInterface;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.core.xml.XMLHandler;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * This class represents the logging plugin type.
 *
 * @author matt
 *
 */
@PluginMainClassType( LogTablePluginInterface.class )
@PluginAnnotationType( LogTablePlugin.class )
public class LogTablePluginType extends BasePluginType implements PluginTypeInterface {

  private static LogTablePluginType logTablePluginType;

  private LogTablePluginType() {
    super( LogTablePlugin.class, "LOGTABLE", "Log table plugin" );
    populateFolders( "logtable" );
  }

  public static LogTablePluginType getInstance() {
    if ( logTablePluginType == null ) {
      logTablePluginType = new LogTablePluginType();
    }
    return logTablePluginType;
  }

  /**
   * Scan & register internal logging plugins
   */
  protected void registerNatives() throws KettlePluginException {
    // No native
  }

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
            log.logError( "Error found while reading logging plugin.xml file: " + file.getName().toString(), e );
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
    return null;
  }

  @Override
  protected String extractID( Annotation annotation ) {
    String id = ( (LogTablePlugin) annotation ).id();

    LogChannel.GENERAL.logBasic( "Logging plugin type found with ID: " + id );

    return id;
  }

  @Override
  protected String extractName( Annotation annotation ) {
    return null;
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
    return ( (LogTablePlugin) annotation ).classLoaderGroup();
  }
}
