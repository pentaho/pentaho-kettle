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
  @Override
  protected void registerNatives() throws KettlePluginException {
    // No native
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
    return ( (LogTablePlugin) annotation ).description();
  }

  @Override
  protected String extractID( Annotation annotation ) {
    String id = ( (LogTablePlugin) annotation ).id();

    LogChannel.GENERAL.logBasic( "Logging plugin type found with ID: " + id );

    return id;
  }

  @Override
  protected String extractName( Annotation annotation ) {
    return ( (LogTablePlugin) annotation ).name();
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
    return ( (LogTablePlugin) annotation ).classLoaderGroup();
  }
}
