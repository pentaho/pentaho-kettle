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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ListLoadSaveValidator<ObjectType> implements
    FieldLoadSaveValidator<List<ObjectType>> {
  private final FieldLoadSaveValidator<ObjectType> validator;
  private final Integer elements;

  public ListLoadSaveValidator( FieldLoadSaveValidator<ObjectType> fieldValidator ) {
    validator = fieldValidator;
    elements = null;
  }

  public ListLoadSaveValidator( FieldLoadSaveValidator<ObjectType> fieldValidator, Integer elements ) {
    validator = fieldValidator;
    this.elements = elements;
  }

  @Override
  public List<ObjectType> getTestObject() {
    int max = elements == null ? new Random().nextInt( 100 ) + 50 : elements;
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
