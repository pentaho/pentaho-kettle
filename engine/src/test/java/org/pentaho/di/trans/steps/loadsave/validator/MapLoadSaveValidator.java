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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

public class MapLoadSaveValidator<KeyObjectType, ValueObjectType> implements
    FieldLoadSaveValidator<Map<KeyObjectType, ValueObjectType>> {
  private final FieldLoadSaveValidator<KeyObjectType> keyValidator;
  private final FieldLoadSaveValidator<ValueObjectType> valueValidator;
  private final Integer elements;

  public MapLoadSaveValidator( FieldLoadSaveValidator<KeyObjectType> keyFieldValidator,
    FieldLoadSaveValidator<ValueObjectType> valueFieldValidator ) {
    keyValidator = keyFieldValidator;
    valueValidator = valueFieldValidator;
    elements = null;
  }

  public MapLoadSaveValidator( FieldLoadSaveValidator<KeyObjectType> keyFieldValidator,
      FieldLoadSaveValidator<ValueObjectType> valueFieldValidator, Integer elements ) {
    keyValidator = keyFieldValidator;
    valueValidator = valueFieldValidator;
    this.elements = elements;
  }

  @Override
  public Map<KeyObjectType, ValueObjectType> getTestObject() {
    int max = elements == null ? new Random().nextInt( 100 ) + 50 : elements;
    Map<KeyObjectType, ValueObjectType> result = new LinkedHashMap<KeyObjectType, ValueObjectType>();
    for ( int i = 0; i < max; i++ ) {
      result.put( keyValidator.getTestObject(), valueValidator.getTestObject() );
    }
    return result;
  }

  @Override
  public boolean validateTestObject( Map<KeyObjectType, ValueObjectType> original, Object actual ) {
    if ( actual instanceof Map ) {
      @SuppressWarnings( "unchecked" )
      Map<KeyObjectType, ValueObjectType> actualMap = (Map<KeyObjectType, ValueObjectType>) actual;
      if ( original.size() != actualMap.size() ) {
        return false;
      }
      for ( KeyObjectType originalKey : original.keySet() ) {
        if ( !actualMap.containsKey( originalKey ) ) {
          return false;
        }
        if ( !this.valueValidator.validateTestObject( original.get( originalKey ), actualMap.get( originalKey ) ) ) {
          return false;
        }
      }
      return true;
    }
    return false;
  }
}
