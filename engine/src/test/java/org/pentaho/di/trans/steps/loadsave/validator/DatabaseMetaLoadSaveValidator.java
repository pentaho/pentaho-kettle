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

import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.repository.LongObjectId;

public class DatabaseMetaLoadSaveValidator implements FieldLoadSaveValidator<DatabaseMeta> {

  private static final Random rand = new Random();

  @Override
  public DatabaseMeta getTestObject() {
    DatabaseMeta db = new DatabaseMeta();
    db.setObjectId( new LongObjectId( rand.nextInt( Integer.MAX_VALUE ) ) );
    db.setName( UUID.randomUUID().toString() );
    db.setHostname( UUID.randomUUID().toString() );
    db.setUsername( UUID.randomUUID().toString() );
    db.setPassword( UUID.randomUUID().toString() );
    return db;
  }

  @Override
  public boolean validateTestObject( DatabaseMeta testObject, Object actual ) {
    if ( actual instanceof DatabaseMeta ) {
      return testObject.equals( actual );
    }
    return false;
  }

}
