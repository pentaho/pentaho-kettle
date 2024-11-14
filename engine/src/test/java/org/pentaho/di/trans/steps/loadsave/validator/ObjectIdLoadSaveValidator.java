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

package org.pentaho.di.trans.steps.loadsave.validator;

import java.util.Random;
import java.util.UUID;

import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.StringObjectId;

public class ObjectIdLoadSaveValidator implements FieldLoadSaveValidator<ObjectId> {
  final Random rand = new Random();
  @Override
  public ObjectId getTestObject() {
    return new StringObjectId( UUID.randomUUID().toString() );
  }

  @Override
  public boolean validateTestObject( ObjectId testObject, Object actual ) {
    if ( !( actual instanceof ObjectId ) ) {
      return false;
    }
    ObjectId actualInput = (ObjectId) actual;
    return ( testObject.equals( actualInput ) );
  }
}
