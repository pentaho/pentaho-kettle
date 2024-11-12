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
