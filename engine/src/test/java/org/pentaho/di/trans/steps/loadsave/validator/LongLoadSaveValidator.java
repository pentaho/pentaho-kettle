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
