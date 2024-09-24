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

import java.util.Random;
import java.util.UUID;

import org.pentaho.di.trans.steps.mapping.MappingParameters;

public class MappingParametersLoadSaveValidator implements FieldLoadSaveValidator<MappingParameters> {
  final Random rand = new Random();
  @Override
  public MappingParameters getTestObject() {
    MappingParameters rtn = new MappingParameters();
    rtn.setVariable( new String[] {
      UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString()
    } );
    rtn.setInputField( new String[] {
      UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString()
    } );
    rtn.setInheritingAllVariables( rand.nextBoolean() );
    return rtn;
  }

  @Override
  public boolean validateTestObject( MappingParameters testObject, Object actual ) {
    if ( !( actual instanceof MappingParameters ) ) {
      return false;
    }
    MappingParameters actualInput = (MappingParameters) actual;
    return ( testObject.getXML().equals( actualInput.getXML() ) );
  }
}
