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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.pentaho.di.trans.steps.loadsave.getter.Getter;
import org.pentaho.di.trans.steps.loadsave.setter.Setter;
import org.pentaho.test.util.JavaBeanManipulator;

public class ObjectValidator<T> implements FieldLoadSaveValidator<T> {
  private final FieldLoadSaveValidatorFactory fieldLoadSaveValidatorFactory;
  private final JavaBeanManipulator<T> manipulator;
  private final Class<T> clazz;
  private final List<String> fieldNames;

  public ObjectValidator( FieldLoadSaveValidatorFactory fieldLoadSaveValidatorFactory, Class<T> clazz,
      List<String> fieldNames, Map<String, String> getterMap, Map<String, String> setterMap ) {
    this.fieldLoadSaveValidatorFactory = fieldLoadSaveValidatorFactory;
    manipulator = new JavaBeanManipulator<T>( clazz, fieldNames, getterMap, setterMap );
    this.clazz = clazz;
    this.fieldNames = new ArrayList<String>( fieldNames );
  }

  public ObjectValidator( FieldLoadSaveValidatorFactory fieldLoadSaveValidatorFactory, Class<T> clazz,
      List<String> fieldNames ) {
    this( fieldLoadSaveValidatorFactory, clazz, fieldNames, new HashMap<String, String>(),
        new HashMap<String, String>() );
  }

  @SuppressWarnings( { "rawtypes", "unchecked" } )
  @Override
  public T getTestObject() {
    try {
      T object = clazz.newInstance();
      for ( String attribute : fieldNames ) {
        Setter setter = manipulator.getSetter( attribute );
        setter.set( object, fieldLoadSaveValidatorFactory.createValidator( manipulator.getGetter( attribute ) )
            .getTestObject() );
      }
      return object;
    } catch ( Exception e ) {
      throw new RuntimeException( "Unable to instantiate " + clazz, e );
    }
  }

  @Override
  public boolean validateTestObject( T testObject, Object actual ) {
    if ( actual == null || !( clazz.isAssignableFrom( actual.getClass() ) ) ) {
      return false;
    }
    try {
      for ( String attribute : fieldNames ) {
        Getter<?> getter = manipulator.getGetter( attribute );
        FieldLoadSaveValidator<?> validator = fieldLoadSaveValidatorFactory.createValidator( getter );
        Method validatorMethod = null;
        for ( Method method : validator.getClass().getMethods() ) {
          if ( "validateTestObject".equals( method.getName() ) ) {
            Class<?>[] types = method.getParameterTypes();
            if ( types.length == 2 ) {
              validatorMethod = method;
              break;
            }
          }
        }
        if ( validatorMethod == null ) {
          throw new RuntimeException( "Unable to find validator for " + attribute + " " + getter.getGenericType() );
        }
        if ( !(Boolean) validatorMethod.invoke( validator, getter.get( testObject ), getter.get( actual ) ) ) {
          return false;
        }
      }
      return true;
    } catch ( Exception e ) {
      throw new RuntimeException( "Unable to instantiate " + clazz, e );
    }
  }
}
