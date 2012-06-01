/*
 * ******************************************************************************
 *  *
 *  * Pentaho Big Data
 *  *
 *  * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
 *  *
 *  *******************************************************************************
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with
 *  * the License. You may obtain a copy of the License at
 *  *
 *  *    http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *  *
 *  *****************************************************************************
 */

package org.pentaho.hadoop.mapreduce.converter;

import org.apache.hadoop.io.*;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.hadoop.mapreduce.converter.spi.ITypeConverter;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

public class TypeConverterFactory {

  /**
   * Map key to represent a converter capable of converting an object from the given type to another type.
   */
  private static class Key {
    private Class from;
    private Class to;

    private Key(Class from, Class to) {
      this.from = from;
      this.to = to;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      Key key = (Key) o;

      if (from != null ? !from.equals(key.from) : key.from != null) return false;
      if (to != null ? !to.equals(key.to) : key.to != null) return false;

      return true;
    }

    @Override
    public int hashCode() {
      int result = from != null ? from.hashCode() : 0;
      result = 31 * result + (to != null ? to.hashCode() : 0);
      return result;
    }
  }

  /**
   * Determines if a class can be converted by {@code ValueMetaInterface}.
   *
   * @param type Type to check
   * @return True if this type maps to a {@code ValueMetaInterface.TYPE_*}
   */
  public static boolean isKettleType(Class<?> type) {
    return type != null && (CharSequence.class.isAssignableFrom(type) ||
        Number.class.isAssignableFrom(type) ||
        byte[].class.equals(type) ||
        Boolean.class.equals(type) ||
        java.sql.Date.class.equals(type));

  }

  /**
   * Attempt to determine the Java {@link Class} for the {@link ValueMetaInterface} provided
   *
   * @param vmi Value Meta with type information to look up
   * @return the class that represents the type {@link ValueMetaInterface} represents; {@code null} if no type can be matched.
   */
  public Class<?> getJavaClass(ValueMetaInterface vmi) {
    Class<?> metaClass = null;

    switch (vmi.getType()) {
      case ValueMeta.TYPE_BIGNUMBER:
        metaClass = BigDecimal.class;
        break;
      case ValueMeta.TYPE_BINARY:
        metaClass = byte[].class;
        break;
      case ValueMeta.TYPE_BOOLEAN:
        metaClass = Boolean.class;
        break;
      case ValueMeta.TYPE_DATE:
        metaClass = Date.class;
        break;
      case ValueMeta.TYPE_INTEGER:
        metaClass = Long.class;
        break;
      case ValueMeta.TYPE_NUMBER:
        metaClass = Double.class;
        break;
      case ValueMeta.TYPE_STRING:
        metaClass = String.class;
        break;
      case ValueMeta.TYPE_SERIALIZABLE:
        metaClass = Object.class;
        break;
    }

    return metaClass;
  }

  /**
   * Determine the Hadoop writable type to pass Kettle type back to Hadoop as.
   *
   * @param kettleType
   * @return Java type to convert {@code kettleType} to when sending data back to Hadoop.
   */
  public static Class getWritableForKettleType(ValueMetaInterface kettleType) {
    switch (kettleType.getType()) {
      case ValueMetaInterface.TYPE_STRING:
      case ValueMetaInterface.TYPE_BIGNUMBER:
      case ValueMetaInterface.TYPE_DATE:
        return Text.class;
      case ValueMetaInterface.TYPE_INTEGER:
        return LongWritable.class;
      case ValueMetaInterface.TYPE_NUMBER:
        return DoubleWritable.class;
      case ValueMetaInterface.TYPE_BOOLEAN:
        return BooleanWritable.class;
      case ValueMetaInterface.TYPE_BINARY:
        return BytesWritable.class;
      default:
        return Text.class;
    }
  }

  /**
   * Local cache of type converters
   */
  private Map<Key, ITypeConverter<?, ?>> cache;

  public TypeConverterFactory() {
    cache = new HashMap<Key, ITypeConverter<?, ?>>();
  }

  /**
   * Find a converter by dynamically loading SPI implementations of {@link ITypeConverter} and returning the first one
   * that returns {@code true} from {@link ITypeConverter#canConvert(Class, Class) canConvert(from, to)}.
   *
   * @param from Type to convert from
   * @param to Type to convert to
   * @return A type converter that can handle converting between {@code from} and {@code to}
   * @throws TypeConversionException Error instantiating a converter while traversing the list of registered type converters
   */
  protected <F, T> ITypeConverter<F, T> findConverter(Class<F> from, Class<T> to) throws TypeConversionException {
    ServiceLoader<ITypeConverter> loader = ServiceLoader.load(ITypeConverter.class);
    try {
      for (ITypeConverter tc : loader) {
        if (tc.canConvert(from, to)) {
          return tc;
        }
      }
    } catch (ServiceConfigurationError ex) {
      throw new TypeConversionException("Error instantiating type converter", ex);
    }
    return null;
  }

  /**
   * Registers a converter that is capable of converting from type {@code from} to type {@code to}.
   *
   * @param from Type this converter can convert from
   * @param to Type this converter can convert to
   * @param converter The converter to handle the conversion between {@code from} and {@code to}
   */
  public <F, T> void registerConverter(Class<F> from, Class<T> to, ITypeConverter<F, T> converter) {
    cache.put(new Key(from, to), converter);
  }

  /**
   * Obtain a {@link ITypeConverter} that can handle converting from {@code from} and to {@code to}.
   *
   * @param from Type to convert from
   * @param to   Type to convert to
   * @return Converter that is capable of converting from {@code from} and to {@code to}.
   * @throws TypeConversionException No converter available for these types
   */
  public <F, T> ITypeConverter<F, T> getConverter(Class<F> from, Class<T> to) throws TypeConversionException {
    ITypeConverter converter = cache.get(new Key(from, to));
    if (converter == null) {
      converter = findConverter(from, to);
      registerConverter(from, to, converter);
    }
    if (converter == null) {
      throw new TypeConversionException(BaseMessages.getString(getClass(), "ConverterNotFound", from, to));
    }
    return converter;
  }

  /**
   * Obtain a {@link ITypeConverter} that can handle converting from {@code from} to the value meta type provided. The
   * {@link Class} to convert to is obtained through {@link #getJavaClass(org.pentaho.di.core.row.ValueMetaInterface) getJavaClass(vmi)}.
   * If no destination type can be determined from {@code vmi} no converter will be returned.
   *
   * @param from Type to convert from
   * @param vmi Value meta with type information to convert to
   * @return Converter that is capable of converting from {@code from} to the type for {@code vmi} or {@code null}
   *         if no Java {@link Class} could be determined from {@code vmi}.
   * @throws TypeConversionException No converter available for these types
   */
  public <F> ITypeConverter<F, ?> getConverter(Class<F> from, ValueMetaInterface vmi) throws TypeConversionException {
    Class<?> to = getJavaClass(vmi);
    return to == null ? null : getConverter(from, to);
  }
}
