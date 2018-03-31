/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
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
