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
import org.pentaho.di.trans.step.StepDialogInterface;

import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.List;

/**
 * This class represents the step dialog fragment type.
 *
 */
@PluginMainClassType( StepDialogInterface.class )
@PluginAnnotationType( PluginDialog.class )
public class StepDialogFragmentType extends BaseFragmentType implements PluginTypeInterface {

  private static StepDialogFragmentType stepDialogFragmentType;

  protected StepDialogFragmentType() {
    super( PluginDialog.class, "STEPDIALOG", "Plugin Step Dialog", StepPluginType.class );
  }

  public static StepDialogFragmentType getInstance() {
    if ( stepDialogFragmentType == null ) {
      stepDialogFragmentType = new StepDialogFragmentType();
    }
    return stepDialogFragmentType;
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
    if ( ( (PluginDialog) annotation ).pluginType() == PluginDialog.PluginType.STEP ) {
      super.handlePluginAnnotation( clazz, annotation, libraries, nativePluginType, pluginFolder );
    }
  }
}
