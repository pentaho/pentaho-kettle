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
