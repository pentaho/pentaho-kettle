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
