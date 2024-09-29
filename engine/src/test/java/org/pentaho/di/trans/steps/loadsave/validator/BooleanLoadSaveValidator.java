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

public class BooleanLoadSaveValidator implements FieldLoadSaveValidator<Boolean> {
  private final Boolean value = new Random().nextBoolean();

  @Override
  public Boolean getTestObject() {
    return value;
  }

  @Override
  public boolean validateTestObject( Boolean original, Object actual ) {
    return original.equals( actual );
  }
}
