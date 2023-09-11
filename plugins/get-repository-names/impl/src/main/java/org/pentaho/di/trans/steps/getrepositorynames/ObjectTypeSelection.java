/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2023 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.getrepositorynames;

import org.pentaho.di.i18n.BaseMessages;

public enum ObjectTypeSelection {

  Transformations( BaseMessages.getString( "System.ObjectTypeSelection.Description.Transformations" ) ), Jobs(
    BaseMessages.getString( "System.ObjectTypeSelection.Description.Jobs" ) ), All( BaseMessages
    .getString( "System.ObjectTypeSelection.Description.All" ) );

  private String description;

  private ObjectTypeSelection( String description ) {
    this.description = description;
  }

  public String getDescription() {
    return description;
  }

  public boolean areTransformationsSelected() {
    return this == Transformations || this == All;
  }

  public boolean areJobsSelected() {
    return this == Jobs || this == All;
  }

  public static ObjectTypeSelection getObjectTypeSelectionByDescription( String description ) {
    for ( ObjectTypeSelection selection : values() ) {
      if ( selection.getDescription().equalsIgnoreCase( description ) ) {
        return selection;
      }
    }
    return All;
  }
}
