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
