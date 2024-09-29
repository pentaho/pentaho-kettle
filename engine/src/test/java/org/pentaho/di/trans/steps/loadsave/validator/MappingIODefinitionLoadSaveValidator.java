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
package org.pentaho.di.trans.steps.loadsave.validator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.pentaho.di.trans.steps.mapping.MappingIODefinition;
import org.pentaho.di.trans.steps.mapping.MappingValueRename;

public class MappingIODefinitionLoadSaveValidator implements FieldLoadSaveValidator<MappingIODefinition> {
  final Random rand = new Random();
  @Override
  public MappingIODefinition getTestObject() {
    MappingIODefinition rtn = new MappingIODefinition();
    rtn.setDescription( UUID.randomUUID().toString() );
    rtn.setInputStepname( UUID.randomUUID().toString() );
    rtn.setMainDataPath( rand.nextBoolean() );
    rtn.setOutputStepname( UUID.randomUUID().toString() );
    rtn.setRenamingOnOutput( rand.nextBoolean() );
    List<MappingValueRename> renames = new ArrayList<MappingValueRename>() {
      {
        add( new MappingValueRename( UUID.randomUUID().toString(), UUID.randomUUID().toString() ) );
        add( new MappingValueRename( UUID.randomUUID().toString(), UUID.randomUUID().toString() ) );
        add( new MappingValueRename( UUID.randomUUID().toString(), UUID.randomUUID().toString() ) );
      }
    };
    rtn.setValueRenames( renames );
    return rtn;
  }

  @Override
  public boolean validateTestObject( MappingIODefinition testObject, Object actual ) {
    if ( !( actual instanceof MappingIODefinition ) ) {
      return false;
    }
    MappingIODefinition actualInput = (MappingIODefinition) actual;
    return ( testObject.getXML().equals( actualInput.getXML() ) );
  }
}
