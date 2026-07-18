/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/


package org.pentaho.di.trans.steps.loadsave.validator;

import java.util.Random;

import org.pentaho.di.core.ObjectLocationSpecificationMethod;

public class ObjectLocationSpecificationMethodLoadSaveValidator implements FieldLoadSaveValidator<ObjectLocationSpecificationMethod> {
  final Random rand = new Random();
  @Override
  public ObjectLocationSpecificationMethod getTestObject() {
    ObjectLocationSpecificationMethod[] methods = ObjectLocationSpecificationMethod.values();
    ObjectLocationSpecificationMethod rtn = methods[ rand.nextInt( methods.length ) ];
    return rtn;
  }

  @Override
  public boolean validateTestObject( ObjectLocationSpecificationMethod testObject, Object actual ) {
    if ( !( actual instanceof ObjectLocationSpecificationMethod ) ) {
      return false;
    }
    ObjectLocationSpecificationMethod actualInput = (ObjectLocationSpecificationMethod) actual;
    return ( testObject.equals( actualInput ) );
  }
}
