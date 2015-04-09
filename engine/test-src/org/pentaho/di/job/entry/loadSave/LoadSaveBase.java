/* ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2015 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.job.entry.loadSave;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.trans.steps.loadsave.getter.Getter;
import org.pentaho.di.trans.steps.loadsave.setter.Setter;
import org.pentaho.di.trans.steps.loadsave.validator.DefaultFieldLoadSaveValidatorFactory;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidatorFactory;
import org.pentaho.test.util.JavaBeanManipulator;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Andrey Khayrutdinov
 */
abstract class LoadSaveBase<T> {

  final Class<T> clazz;
  final List<String> xmlAttributes;
  final List<String> repoAttributes;
  final JavaBeanManipulator<T> manipulator;
  final FieldLoadSaveValidatorFactory fieldLoadSaveValidatorFactory;

  public LoadSaveBase( Class<T> clazz,
                       List<String> commonAttributes, List<String> xmlAttributes, List<String> repoAttributes,
                       Map<String, String> getterMap, Map<String, String> setterMap,
                       Map<String, FieldLoadSaveValidator<?>> fieldLoadSaveValidatorAttributeMap,
                       Map<String, FieldLoadSaveValidator<?>> fieldLoadSaveValidatorTypeMap ) {
    this.clazz = clazz;
    this.xmlAttributes = concat( commonAttributes, xmlAttributes );
    this.repoAttributes = concat( commonAttributes, repoAttributes );
    this.manipulator =
      new JavaBeanManipulator<T>( clazz, concat( this.xmlAttributes, repoAttributes ), getterMap, setterMap );

    Map<Getter<?>, FieldLoadSaveValidator<?>> fieldLoadSaveValidatorMethodMap =
      new HashMap<Getter<?>, FieldLoadSaveValidator<?>>( fieldLoadSaveValidatorAttributeMap.size() );
    for ( Map.Entry<String, FieldLoadSaveValidator<?>> entry : fieldLoadSaveValidatorAttributeMap.entrySet() ) {
      fieldLoadSaveValidatorMethodMap.put( manipulator.getGetter( entry.getKey() ), entry.getValue() );
    }
    this.fieldLoadSaveValidatorFactory =
      new DefaultFieldLoadSaveValidatorFactory( fieldLoadSaveValidatorMethodMap, fieldLoadSaveValidatorTypeMap );
  }

  T createMeta() {
    try {
      return clazz.newInstance();
    } catch ( Exception e ) {
      throw new RuntimeException( "Unable to create meta of class " + clazz.getCanonicalName(), e );
    }
  }

  Map<String, FieldLoadSaveValidator<?>> createValidatorMapAndInvokeSetters( List<String> attributes,
                                                                             T metaToSave ) {
    Map<String, FieldLoadSaveValidator<?>> validatorMap = new HashMap<String, FieldLoadSaveValidator<?>>();
    for ( String attribute : attributes ) {
      Getter<?> getter = manipulator.getGetter( attribute );
      @SuppressWarnings( "rawtypes" )
      Setter setter = manipulator.getSetter( attribute );
      FieldLoadSaveValidator<?> validator = fieldLoadSaveValidatorFactory.createValidator( getter );
      try {
        //noinspection unchecked
        setter.set( metaToSave, validator.getTestObject() );
      } catch ( Exception e ) {
        throw new RuntimeException( "Unable to invoke setter for " + attribute, e );
      }
      validatorMap.put( attribute, validator );
    }
    return validatorMap;
  }

  void validateLoadedMeta( List<String> attributes, Map<String, FieldLoadSaveValidator<?>> validatorMap,
                           T metaSaved, T metaLoaded ) {
    for ( String attribute : attributes ) {
      try {
        Getter<?> getterMethod = manipulator.getGetter( attribute );
        Object originalValue = getterMethod.get( metaSaved );
        Object value = getterMethod.get( metaLoaded );
        FieldLoadSaveValidator<?> validator = validatorMap.get( attribute );
        Method[] validatorMethods = validator.getClass().getMethods();
        Method validatorMethod = null;
        for ( Method method : validatorMethods ) {
          if ( "validateTestObject".equals( method.getName() ) ) {
            Class<?>[] types = method.getParameterTypes();
            if ( types.length == 2 ) {
              if ( types[ 1 ] == Object.class
                && ( originalValue == null || types[ 0 ].isAssignableFrom( originalValue.getClass() ) ) ) {
                validatorMethod = method;
                break;
              }
            }
          }
        }
        if ( validatorMethod == null ) {
          throw new RuntimeException( "Couldn't find proper validateTestObject method on "
            + validator.getClass().getCanonicalName() );
        }
        if ( !( (Boolean) validatorMethod.invoke( validator, originalValue, value ) ) ) {
          throw new KettleException( "Attribute " + attribute + " started with value "
            + originalValue + " ended with value " + value );
        }
      } catch ( Exception e ) {
        throw new RuntimeException( "Error validating " + attribute, e );
      }
    }
  }


  private static <E> List<E> concat( List<E> list1, List<E> list2 ) {
    List<E> result = new ArrayList<E>( list1.size() + list2.size() );
    result.addAll( list1 );
    result.addAll( list2 );
    return result;
  }
}
