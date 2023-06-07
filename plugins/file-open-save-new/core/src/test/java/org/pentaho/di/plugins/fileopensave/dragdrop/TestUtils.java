/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2023 by Hitachi Vantara : http://www.pentaho.com
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
package org.pentaho.di.plugins.fileopensave.dragdrop;

import java.lang.reflect.Field;

/**
 * This util is designed to allow instance variable injection into mockito mocks
 */
public class TestUtils {


  // get a static class value.  private values are fair game
  public static Object reflectValue( Class<?> classToReflect, String fieldNameValueToFetch ) {
    try {
      Field reflectField = reflectField( classToReflect, fieldNameValueToFetch );
      reflectField.setAccessible( true );
      Object reflectValue = reflectField.get( classToReflect );
      return reflectValue;
    } catch ( Exception e ) {
      fail( "Failed to reflect " + fieldNameValueToFetch );
    }
    return null;
  }

  // get an instance value
  public static Object reflectValue( Object objToReflect, String fieldNameValueToFetch ) {
    try {
      Field reflectField = reflectField( objToReflect.getClass(), fieldNameValueToFetch );
      Object reflectValue = reflectField.get( objToReflect );
      return reflectValue;
    } catch ( Exception e ) {
      fail( "Failed to reflect " + fieldNameValueToFetch );
    }
    return null;
  }

  // find a field in the class tree
  public static Field reflectField( Class<?> classToReflect, String fieldNameValueToFetch ) {
    try {
      Field reflectField = null;
      Class<?> classForReflect = classToReflect;
      do {
        try {
          reflectField = classForReflect.getDeclaredField( fieldNameValueToFetch );
        } catch ( NoSuchFieldException e ) {
          classForReflect = classForReflect.getSuperclass();
        }
      } while ( reflectField == null || classForReflect == null );
      reflectField.setAccessible( true );
      return reflectField;
    } catch ( Exception e ) {
      fail( "Failed to reflect " + fieldNameValueToFetch + " from " + classToReflect );
    }
    return null;
  }

  // set a value with no setter.  Use to set an instance variable in a mock, private or otherwise.
  public static void reflectSetValue( Object objToReflect, String fieldNameToSet, Object valueToSet ) {
    try {
      Field reflectField = reflectField( objToReflect.getClass(), fieldNameToSet );
      reflectField.set( objToReflect, valueToSet );
    } catch ( Exception e ) {
      fail( "Failed to reflectively set " + fieldNameToSet + "=" + valueToSet );
    }
  }

  private static void fail( String message ) {
    throw new RuntimeException( message );
  }

}

