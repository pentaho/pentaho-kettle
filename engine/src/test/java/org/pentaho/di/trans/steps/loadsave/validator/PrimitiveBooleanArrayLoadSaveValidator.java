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

public class PrimitiveBooleanArrayLoadSaveValidator implements FieldLoadSaveValidator<boolean[]> {
  private final FieldLoadSaveValidator<Boolean> validator;
  private final Integer elements;

  public PrimitiveBooleanArrayLoadSaveValidator( FieldLoadSaveValidator<Boolean> fieldValidator ) {
    this( fieldValidator, null );
  }

  public PrimitiveBooleanArrayLoadSaveValidator( FieldLoadSaveValidator<Boolean> fieldValidator, Integer elements ) {
    validator = fieldValidator;
    this.elements = elements;
  }

  @Override
  public boolean[] getTestObject() {
    int max = elements == null ? new Random().nextInt( 100 ) + 50 : elements;
    boolean[] result = new boolean[max];
    for ( int i = 0; i < max; i++ ) {
      result[i] = validator.getTestObject();
    }
    return result;
  }

  @Override
  public boolean validateTestObject( boolean[] original, Object actual ) {
    if ( original.getClass().isAssignableFrom( actual.getClass() ) ) {
      boolean[] otherList = (boolean[]) actual;
      if ( original.length != otherList.length ) {
        return false;
      }
      for ( int i = 0; i < original.length; i++ ) {
        if ( !this.validator.validateTestObject( original[i], otherList[i] ) ) {
          return false;
        }
      }
      return true;
    }
    return false;
  }
}
