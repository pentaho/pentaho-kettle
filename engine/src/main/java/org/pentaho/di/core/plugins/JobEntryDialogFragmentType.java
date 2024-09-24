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

import org.pentaho.di.core.Const;
import org.pentaho.di.core.annotations.PluginDialog;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.job.entry.JobEntryDialogInterface;

import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.List;

/**
 * This class represents the job entry dialog fragment type.
 *
 */
@PluginMainClassType( JobEntryDialogInterface.class )
@PluginAnnotationType( PluginDialog.class )
public class JobEntryDialogFragmentType extends BaseFragmentType implements PluginTypeInterface {

  private static JobEntryDialogFragmentType jobEntryDialogFragmentType;

  protected JobEntryDialogFragmentType() {
    super( PluginDialog.class, "JOBENTRYDIALOG", "Plugin Job Entry Dialog", JobEntryPluginType.class );
  }

  public static JobEntryDialogFragmentType getInstance() {
    if ( jobEntryDialogFragmentType == null ) {
      jobEntryDialogFragmentType = new JobEntryDialogFragmentType();
    }
    return jobEntryDialogFragmentType;
  }

  @Override
  protected String extractID( Annotation annotation ) {
    return ( (PluginDialog) annotation ).id();
  }

  @Override
  protected String extractImageFile( Annotation annotation ) {
    return ( (PluginDialog) annotation ).image();
  }

  @Override
  protected String extractDocumentationUrl( Annotation annotation ) {
    return Const.getDocUrl( ( (PluginDialog) annotation ).documentationUrl() );
  }

  @Override
  protected String extractCasesUrl( Annotation annotation ) {
    return ( (PluginDialog) annotation ).casesUrl();
  }

  @Override
  protected String extractForumUrl( Annotation annotation ) {
    return ( (PluginDialog) annotation ).forumUrl();
  }

  @Override
  protected String extractSuggestion( Annotation annotation ) {
    return null;
  }

  @Override
  public void handlePluginAnnotation( Class<?> clazz, java.lang.annotation.Annotation annotation,
    List<String> libraries, boolean nativePluginType, URL pluginFolder ) throws KettlePluginException {
    if ( ( (PluginDialog) annotation ).pluginType() == PluginDialog.PluginType.JOBENTRY ) {
      super.handlePluginAnnotation( clazz, annotation, libraries, nativePluginType, pluginFolder );
    }
  }
}
