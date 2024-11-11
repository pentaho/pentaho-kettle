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

/**
 * @author Andrey Khayrutdinov
 */
public class LongLoadSaveValidator implements FieldLoadSaveValidator<Long> {
  private final Long sample = new Random().nextLong();

  @Override
  public Long getTestObject() {
    return sample;
  }

  @Override
  public boolean validateTestObject( Long original, Object actual ) {
    return original.equals( actual );
  }
}
