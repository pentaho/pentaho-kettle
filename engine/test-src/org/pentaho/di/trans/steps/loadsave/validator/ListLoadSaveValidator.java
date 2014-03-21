package org.pentaho.di.trans.steps.loadsave.validator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ListLoadSaveValidator<ObjectType> implements
    FieldLoadSaveValidator<List<ObjectType>> {
  private final FieldLoadSaveValidator<ObjectType> validator;

  public ListLoadSaveValidator( FieldLoadSaveValidator<ObjectType> fieldValidator ) {
    validator = fieldValidator;
  }

  @Override
  public List<ObjectType> getTestObject() {
    int max = new Random().nextInt( 100 ) + 50;
    List<ObjectType> result = new ArrayList<ObjectType>( max );
    for ( int i = 0; i < max; i++ ) {
      result.add( validator.getTestObject() );
    }
    return result;
  }

  @Override
  public boolean validateTestObject( List<ObjectType> original, Object actual ) {
    if ( actual instanceof List ) {
      List<?> otherList = (List<?>) actual;
      if ( original.size() != otherList.size() ) {
        return false;
      }
      for ( int i = 0; i < original.size(); i++ ) {
        if ( !this.validator.validateTestObject( original.get( i ), otherList.get( i ) ) ) {
          return false;
        }
      }
      return true;
    }
    return false;
  }

}
