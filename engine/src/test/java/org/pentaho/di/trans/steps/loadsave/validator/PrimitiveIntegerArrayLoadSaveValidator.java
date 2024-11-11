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

public class PrimitiveIntegerArrayLoadSaveValidator implements FieldLoadSaveValidator<int[]> {

  private final FieldLoadSaveValidator<Integer> validator;
  private final Integer elements;

  public PrimitiveIntegerArrayLoadSaveValidator( FieldLoadSaveValidator<Integer> fieldValidator ) {
    this( fieldValidator, null );
  }

  public PrimitiveIntegerArrayLoadSaveValidator( FieldLoadSaveValidator<Integer> fieldValidator, Integer elements ) {
    validator = fieldValidator;
    this.elements = elements;
  }

  @Override
  public int[] getTestObject() {
    int max = elements == null ? new Random().nextInt( 100 ) + 50 : elements;
    int[] result = new int[max];
    for ( int i = 0; i < max; i++ ) {
      result[i] = validator.getTestObject();
    }
    return result;
  }

  @Override
  public boolean validateTestObject( int[] original, Object actual ) {
    if ( original.getClass().isAssignableFrom( actual.getClass() ) ) {
      int[] otherList = (int[]) actual;
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
