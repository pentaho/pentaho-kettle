/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2019 - 2024 by Hitachi Vantara : http://www.pentaho.com
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

  /**
   * A transformer for encrypting and decrypting, which can throw a checked exception.
   * <p>
   * The ability to throw a checked exception is important to be able to support other existing encoders/decoders.
   * See, for example, {@code org.pentaho.platform.api.util.IPasswordService}.
   */
  @FunctionalInterface
  public interface Transformer<E extends Exception> {
    /**
     * Transforms a given string.
     *
     * @param value The value to transform.
     * @return The transformed value.
     */
    String transform( String value ) throws E;
  }

  private EncryptUtils() {

  }

  public static void encryptFields( Object object ) {
    transformFields( object, Encr::encryptPasswordIfNotUsingVariables );
  }

  public static void decryptFields( Object connectionDetails ) {
    transformFields( connectionDetails, Encr::decryptPasswordOptionallyEncrypted );
  }

  /**
   * Transforms the values of the fields of an object which are marked with the
   * {@link Encrypted} annotation.
   *
   * @param object      The object whose fields to transform.
   * @param transformer The transformer function.
   */
  public static <E extends Exception> void transformFields( Object object, Transformer<E> transformer ) throws E {
    List<Field> fields = getFields( object );
    for ( Field field : fields ) {
      String value = getValue( object, field );
      if ( value != null ) {
        String transformed = transformer.transform( value );
        setValue( object, field, transformed );
      }
    }
  }

  private static List<Field> getFields( Object connectionDetails ) {
    List<Field> fields = new ArrayList<>();
    Class<?> clazz = connectionDetails.getClass();
    while ( clazz != Object.class ) {
      for ( Field field : clazz.getDeclaredFields() ) {
        Annotation annotation = Arrays.stream( field.getAnnotations() ).filter(
                Encrypted.class::isInstance ).findAny().orElse( null );
        if ( annotation != null ) {
          fields.add( field );
        }
      }
      clazz = clazz.getSuperclass();
    }
    return fields;
  }

  public static void setValue( Object object, Field field, String value ) {
    try {
      Method setMethod = getDeclaredMethod( object.getClass(),
        SET_PREFIX + StringUtils.capitalize( field.getName() ), String.class );
      setMethod.invoke( object, value );
    } catch ( NoSuchMethodException | InvocationTargetException | IllegalAccessException ignore ) {
      // ignore
    }
  }

  public static String getValue( Object object, Field field ) {
    try {
      Method getMethod = getDeclaredMethod( object.getClass(),
        GET_PREFIX + StringUtils.capitalize( field.getName() ) );
      return (String) getMethod.invoke( object );
    } catch ( NoSuchMethodException | IllegalAccessException | InvocationTargetException e ) {
      return null;
    }
  }

  /**
   * Unlike Class#getDeclaredMethod which returns all declared
   * private, protected and public methods declared in the specific Class
   *
   * EncryptUtils#getDeclaredMethod returns all declared
   * private, protected and public methods declared in the specific Class and its Parent Classes
   *
   * @param parentClass The class whose declared methods are needed.
   * @param name The field/method name.
   * @param parameterTypes Return Type of the field/method
   */
  private static Method getDeclaredMethod( Class<?> parentClass, String name, Class<?>... parameterTypes ) throws NoSuchMethodException {
    if ( parentClass == Object.class ) {
      throw new NoSuchMethodException();
    }
    try {
      return parentClass.getDeclaredMethod( name, parameterTypes );
    } catch ( NoSuchMethodException | SecurityException e ) {
      parentClass = parentClass.getSuperclass();
      return getDeclaredMethod( parentClass, name, parameterTypes );
    }
  }
}
