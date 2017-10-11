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

package org.pentaho.di.trans.step;

import java.util.List;

import org.pentaho.di.core.row.ValueMetaInterface;

public class BaseStepMetaInjection {

  protected StepInjectionMetaEntry createStepMetaInjectionEntry( StepMetaInjectionEnumEntry entry ) {
    StepInjectionMetaEntry stepInjectionMetaEntry =
      new StepInjectionMetaEntry( entry.name(), entry.getValueType(), entry.getDescription() );
    return stepInjectionMetaEntry;
  }

  protected void addNestedStepMetaInjectionEntries( List<StepInjectionMetaEntry> list,
    StepMetaInjectionEnumEntry[] allEntries, StepMetaInjectionEnumEntry itemsEntry,
    StepMetaInjectionEnumEntry itemEntry ) {

    StepInjectionMetaEntry fieldsEntry = createStepMetaInjectionEntry( itemsEntry );
    list.add( fieldsEntry );
    StepInjectionMetaEntry fieldEntry = createStepMetaInjectionEntry( itemEntry );
    fieldsEntry.getDetails().add( fieldEntry );

    for ( StepMetaInjectionEnumEntry entry : allEntries ) {
      if ( entry.getParent() == itemEntry ) {
        fieldEntry.getDetails().add( createStepMetaInjectionEntry( entry ) );
      }
    }
  }

  protected void addTopLevelStepMetaInjectionEntries( List<StepInjectionMetaEntry> list,
    StepMetaInjectionEnumEntry[] allEntries ) {
    for ( StepMetaInjectionEnumEntry entry : allEntries ) {
      if ( entry.getParent() == null && entry.getValueType() != ValueMetaInterface.TYPE_NONE ) {
        list.add( createStepMetaInjectionEntry( entry ) );
      }
    }
  }

}
