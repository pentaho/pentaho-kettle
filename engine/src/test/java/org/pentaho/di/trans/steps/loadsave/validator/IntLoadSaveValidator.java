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
public class IntLoadSaveValidator implements FieldLoadSaveValidator<Integer> {
  private final Integer maxValue;

  /**
   * An IntLoadSaveValidator that returns test values between Integer.MIN_VALUE and Integer.MAX_VALUE.
   */
  public IntLoadSaveValidator() {
    maxValue = null;
  }

  /**
   * An IntLoadSaveValidator that only returns test values between 0 and maxValue, inclusive.
   * 
   * @param maxValue The maximum Int value that should be returned
   */
  public IntLoadSaveValidator( Integer maxValue ) {
    this.maxValue = maxValue;
  }

  @Override
  public Integer getTestObject() {
    if ( maxValue == null ) {
      return new Random().nextInt();
    }
    return new Random().nextInt( maxValue );
  }

  @Override
  public boolean validateTestObject( Integer original, Object actual ) {
    return original.equals( actual );
  }
}
