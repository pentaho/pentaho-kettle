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
import java.util.List;
import java.util.Map;

import org.apache.commons.vfs2.FileObject;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.annotations.CarteServlet;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.www.CartePluginInterface;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * This class represents the carte plugin type.
 *
 * @author matt
 *
 */
@PluginMainClassType( CartePluginInterface.class )
@PluginAnnotationType( CarteServlet.class )
public class CartePluginType extends BasePluginType implements PluginTypeInterface {

  private static CartePluginType cartePluginType;

  private CartePluginType() {
    super( CarteServlet.class, "CARTE_SERVLET", "Carte Servlet" );
    populateFolders( "servlets" );
  }

  public static CartePluginType getInstance() {
    if ( cartePluginType == null ) {
      cartePluginType = new CartePluginType();
    }
    return cartePluginType;
  }

  @Override
  protected String getXmlPluginFile() {
    return Const.XML_FILE_KETTLE_SERVLETS;
  }

  @Override
  protected String getAlternativePluginFile() {
    return Const.KETTLE_CORE_SERVLETS_FILE;
  }

  @Override
  protected String getMainTag() {
    return "servlets";
  }

  @Override
  protected String getSubTag() {
    return "servlet";
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
            // We want to report this plugin.xml error, perhaps an XML typo or
            // something like that...
            //
            log.logError( "Error found while reading step plugin.xml file: " + file.getName().toString(), e );
          }
        }
      }
    }
  }

  @Override
  protected String extractCategory( Annotation annotation ) {
    return "";
  }

  @Override
  protected String extractDesc( Annotation annotation ) {
    return ( (CarteServlet) annotation ).description();
  }

  @Override
  protected String extractID( Annotation annotation ) {
    return ( (CarteServlet) annotation ).id();
  }

  @Override
  protected String extractName( Annotation annotation ) {
    return ( (CarteServlet) annotation ).name();
  }

  @Override
  protected String extractImageFile( Annotation annotation ) {
    return "";
  }

  @Override
  protected boolean extractSeparateClassLoader( Annotation annotation ) {
    return ( (CarteServlet) annotation ).isSeparateClassLoaderNeeded();
  }

  @Override
  protected String extractI18nPackageName( Annotation annotation ) {
    return ( (CarteServlet) annotation ).i18nPackageName();
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
    return ( (CarteServlet) annotation ).classLoaderGroup();
  }
}
