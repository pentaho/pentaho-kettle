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

import java.util.Date;
import java.util.Random;

public class DateLoadSaveValidator implements FieldLoadSaveValidator<Date> {

  @Override
  public Date getTestObject() {
    Long time = System.currentTimeMillis();
    // forward or back less than a month
    time += new Random().nextInt();
    return new Date( time );
  }

  @Override
  public boolean validateTestObject( Date testObject, Object actual ) {
    if ( !( actual instanceof Date ) ) {
      return false;
    }
    return testObject.equals( actual );
  }

}
