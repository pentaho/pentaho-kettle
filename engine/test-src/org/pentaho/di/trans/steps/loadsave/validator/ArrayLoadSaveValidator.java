package org.pentaho.di.trans.steps.loadsave.validator;

import java.lang.reflect.Array;
import java.util.Random;

public class ArrayLoadSaveValidator<ObjectType> implements FieldLoadSaveValidator<ObjectType[]> {
  private final FieldLoadSaveValidator<ObjectType> validator;
  private final Integer elements;

  public ArrayLoadSaveValidator( FieldLoadSaveValidator<ObjectType> fieldValidator ) {
    this( fieldValidator, null );
  }

  public ArrayLoadSaveValidator( FieldLoadSaveValidator<ObjectType> fieldValidator, Integer elements ) {
    validator = fieldValidator;
    this.elements = elements;
  }

  @SuppressWarnings( "unchecked" )
  @Override
  public ObjectType[] getTestObject() {
    int max = elements == null ? new Random().nextInt( 100 ) + 50 : elements;
    ObjectType[] result = null;
    for ( int i = 0; i < max; i++ ) {
      ObjectType element = validator.getTestObject();
      if ( result == null ) {
        result = (ObjectType[]) Array.newInstance( element.getClass(), max );
      }
      result[i] = element;
    }
    return result;
  }

  @Override
  public boolean validateTestObject( ObjectType[] original, Object actual ) {
    if ( original.getClass().isAssignableFrom( actual.getClass() ) ) {
      @SuppressWarnings( "unchecked" )
      ObjectType[] otherList = (ObjectType[]) actual;
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
