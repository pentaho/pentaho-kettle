/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2017 by Hitachi Vantara : http://www.pentaho.com
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

import org.pentaho.di.core.Const;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.core.annotations.StepDialog;

import java.lang.annotation.Annotation;

/**
 * This class represents the step dialog fragment type.
 *
 */
@PluginMainClassType( StepDialogInterface.class )
@PluginAnnotationType( StepDialog.class )
public class StepDialogFragmentType extends BaseFragmentType implements PluginTypeInterface {

  private static StepDialogFragmentType stepDialogFragmentType;

  protected StepDialogFragmentType() {
    super( StepDialog.class, "STEPDIALOG", "Step Dialog", StepPluginType.class );
  }

  public static StepDialogFragmentType getInstance() {
    if ( stepDialogFragmentType == null ) {
      stepDialogFragmentType = new StepDialogFragmentType();
    }
    return stepDialogFragmentType;
  }

  @Override
  protected String extractID( Annotation annotation ) {
    return ( (StepDialog) annotation ).id();
  }

  @Override
  protected String extractImageFile( Annotation annotation ) {
    return ( (StepDialog) annotation ).image();
  }

  @Override
  protected String extractDocumentationUrl( Annotation annotation ) {
    return Const.getDocUrl( ( (StepDialog) annotation ).documentationUrl() );
  }

  @Override
  protected String extractCasesUrl( Annotation annotation ) {
    return ( (StepDialog) annotation ).casesUrl();
  }

  @Override
  protected String extractForumUrl( Annotation annotation ) {
    return ( (StepDialog) annotation ).forumUrl();
  }
}
