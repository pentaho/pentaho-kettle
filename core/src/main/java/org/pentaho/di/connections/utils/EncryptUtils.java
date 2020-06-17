/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2019 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.connections.utils;

import org.apache.commons.lang.StringUtils;
import org.pentaho.di.connections.annotations.Encrypted;
import org.pentaho.di.core.encryption.Encr;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EncryptUtils {

  private static final String SET_PREFIX = "set";
  private static final String GET_PREFIX = "get";

  private EncryptUtils() {

  }

  public static void encryptFields( Object object ) {
    List<Field> fields = getFields( object );
    fields.forEach( field -> {
      String value = getValue( object, field );
      if ( value != null ) {
        String encrypted = Encr.encryptPasswordIfNotUsingVariables( value );
        setValue( object, field, encrypted );
      }
    } );
  }

  public static void decryptFields( Object connectionDetails ) {
    List<Field> fields = getFields( connectionDetails );
    fields.forEach( field -> {
      String value = getValue( connectionDetails, field );
      if ( value != null ) {
        String decrypted = Encr.decryptPasswordOptionallyEncrypted( value );
        setValue( connectionDetails, field, decrypted );
      }
    } );
  }

  private static List<Field> getFields( Object connectionDetails ) {
    List<Field> fields = new ArrayList<>();
    for ( Field field : connectionDetails.getClass().getDeclaredFields() ) {
      Annotation annotation = Arrays.stream( field.getAnnotations() ).filter(
        annotation1 -> annotation1 instanceof Encrypted ).findAny().orElse( null );
      if ( annotation != null ) {
        fields.add( field );
      }
    }
    return fields;
  }

  public static void setValue( Object object, Field field, String value ) {
    try {
      Method setMethod =
        object.getClass().getMethod( SET_PREFIX + StringUtils.capitalize( field.getName() ), String.class );
      setMethod.invoke( object, value );
    } catch ( NoSuchMethodException | InvocationTargetException | IllegalAccessException ignore ) {
      // ignore
    }
  }

  public static String getValue( Object object, Field field ) {
    try {
      Method getMethod =
        object.getClass().getMethod( GET_PREFIX + StringUtils.capitalize( field.getName() ) );
      if ( getMethod != null ) {
        return (String) getMethod.invoke( object );
      } else {
        return null;
      }
    } catch ( NoSuchMethodException | IllegalAccessException | InvocationTargetException e ) {
      return null;
    }
  }

}
