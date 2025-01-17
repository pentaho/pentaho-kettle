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


package org.pentaho.di.core.plugins;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;

import org.apache.commons.vfs2.FileObject;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.annotations.JobEntry;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * This plugin type handles the job entries.
 *
 * @author matt
 *
 */

@PluginTypeCategoriesOrder(
  getNaturalCategoriesOrder = {
    "JobCategory.Category.General", "JobCategory.Category.Mail", "JobCategory.Category.FileManagement",
    "JobCategory.Category.Conditions", "JobCategory.Category.Scripting", "JobCategory.Category.BulkLoading",
    "JobCategory.Category.BigData", "JobCategory.Category.Modeling", "JobCategory.Category.DataQuality",
    "JobCategory.Category.XML", "JobCategory.Category.Utility", "JobCategory.Category.Repository",
    "JobCategory.Category.FileTransfer", "JobCategory.Category.FileEncryption", "JobCategory.Category.Palo",
    "JobCategory.Category.ServiceManagement", "JobCategory.Category.Experimental", "JobCategory.Category.Deprecated" },
  i18nPackageClass = JobMeta.class )
@PluginMainClassType( JobEntryInterface.class )
@PluginAnnotationType( JobEntry.class )
public class JobEntryPluginType extends BasePluginType implements PluginTypeInterface {
  private static final Class<?> PKG = JobMeta.class; // for i18n purposes, needed by Translator2!!

  public static final String GENERAL_CATEGORY = BaseMessages.getString( PKG, "JobCategory.Category.General" );

  private static JobEntryPluginType pluginType;

  private JobEntryPluginType() {
    super( JobEntry.class, "JOBENTRY", "Job entry" );
    populateFolders( "jobentries" );
  }

  protected JobEntryPluginType( Class<? extends Annotation> pluginType, String id, String name ) {
    super( pluginType, id, name );
  }

  public static JobEntryPluginType getInstance() {
    if ( pluginType == null ) {
      pluginType = new JobEntryPluginType();
    }
    return pluginType;
  }

  @Override
  protected String getXmlPluginFile() {
    return Const.XML_FILE_KETTLE_JOB_ENTRIES;
  }

  @Override
  protected String getAlternativePluginFile() {
    return Const.KETTLE_CORE_JOBENTRIES_FILE;
  }

  @Override
  protected String getMainTag() {
    return "job-entries";
  }

  @Override
  protected String getSubTag() {
    return "job-entry";
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
            log.logError( "Error found while reading job entry plugin.xml file: " + file.getName().toString(), e );
          }
        }
      }
    }
  }

  @Override
  protected String extractCategory( Annotation annotation ) {
    return ( (JobEntry) annotation ).categoryDescription();
  }

  @Override
  protected String extractDesc( Annotation annotation ) {
    return ( (JobEntry) annotation ).description();
  }

  @Override
  protected String extractID( Annotation annotation ) {
    return ( (JobEntry) annotation ).id();
  }

  @Override
  protected String extractName( Annotation annotation ) {
    return ( (JobEntry) annotation ).name();
  }

  @Override
  protected String extractImageFile( Annotation annotation ) {
    return ( (JobEntry) annotation ).image();
  }

  @Override
  protected boolean extractSeparateClassLoader( Annotation annotation ) {
    return false;
  }

  @Override
  protected String extractI18nPackageName( Annotation annotation ) {
    return ( (JobEntry) annotation ).i18nPackageName();
  }

  @Override
  protected void addExtraClasses( Map<Class<?>, String> classMap, Class<?> clazz, Annotation annotation ) {
    //there are no extra classes to add to the map
  }

  @Override
  protected String extractDocumentationUrl( Annotation annotation ) {
    return Const.getDocUrl( ( (JobEntry) annotation ).documentationUrl() );
  }

  @Override
  protected String extractCasesUrl( Annotation annotation ) {
    return ( (JobEntry) annotation ).casesUrl();
  }

  @Override
  protected String extractForumUrl( Annotation annotation ) {
    return ( (JobEntry) annotation ).forumUrl();
  }

  @Override
  protected String extractSuggestion( Annotation annotation ) {
    return ( (JobEntry) annotation ).suggestion();
  }

  @Override
  protected String extractClassLoaderGroup( Annotation annotation ) {
    return ( (JobEntry) annotation ).classLoaderGroup();
  }
}
