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

public class YNLoadSaveValidator implements FieldLoadSaveValidator<String> {
  Random r = new Random();

  @Override
  public String getTestObject() {
    boolean ltr = r.nextBoolean();
    String letter = ltr ? "Y" : "N";
    return letter;
  }

  @Override
  public boolean validateTestObject( String test, Object actual ) {
    return test.equals( actual );
  }
}
