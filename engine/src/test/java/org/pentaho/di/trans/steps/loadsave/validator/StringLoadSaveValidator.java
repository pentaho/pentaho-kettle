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

import java.util.UUID;

public class StringLoadSaveValidator implements FieldLoadSaveValidator<String> {

  @Override
  public String getTestObject() {
    return UUID.randomUUID().toString();
  }

  @Override
  public boolean validateTestObject( String test, Object actual ) {
    return test.equals( actual );
  }
}
