package org.pentaho.di.trans.steps.loadsave.validator;

import java.util.Random;

public class PrimitiveIntArrayLoadSaveValidator implements FieldLoadSaveValidator<int[]> {
  private final FieldLoadSaveValidator<Integer> validator;
  private final Integer elements;

  public PrimitiveIntArrayLoadSaveValidator( FieldLoadSaveValidator<Integer> fieldValidator ) {
    this( fieldValidator, null );
  }

  public PrimitiveIntArrayLoadSaveValidator( FieldLoadSaveValidator<Integer> fieldValidator, Integer elements ) {
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
