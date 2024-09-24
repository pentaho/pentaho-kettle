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
    if ( original == null && actual == null ) {
      return true;
    } else if ( original != null && actual == null ) {
      return false;
    } else if ( original == null && actual != null ) {
      return false;
    }
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
