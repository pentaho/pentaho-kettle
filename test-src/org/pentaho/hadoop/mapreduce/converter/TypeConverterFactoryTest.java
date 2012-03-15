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
import org.junit.Test;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.hadoop.mapreduce.converter.converters.ObjectToStringConverter;
import org.pentaho.hadoop.mapreduce.converter.spi.ITypeConverter;

import java.math.BigDecimal;
import java.sql.Date;

import static org.junit.Assert.*;

/**
 * Tests for {@link TypeConverterFactory}
 */
public class TypeConverterFactoryTest {

  @Test
  public void isKettleType() {
    assertTrue(TypeConverterFactory.isKettleType(String.class));
    assertTrue(TypeConverterFactory.isKettleType(Date.class));
    assertTrue(TypeConverterFactory.isKettleType(Integer.class));
    assertTrue(TypeConverterFactory.isKettleType(Long.class));
    assertTrue(TypeConverterFactory.isKettleType(Double.class));
    assertTrue(TypeConverterFactory.isKettleType(BigDecimal.class));
    assertTrue(TypeConverterFactory.isKettleType(Boolean.class));
    assertTrue(TypeConverterFactory.isKettleType(byte[].class));
    
    assertFalse(TypeConverterFactory.isKettleType(null));
    assertFalse(TypeConverterFactory.isKettleType(Object.class));
    assertFalse(TypeConverterFactory.isKettleType(IntWritable.class));
    assertFalse(TypeConverterFactory.isKettleType(LongWritable.class));
    assertFalse(TypeConverterFactory.isKettleType(Text.class));
    assertFalse(TypeConverterFactory.isKettleType(BytesWritable.class));
  }
  
  @Test
  public void getWritableForKettleType() {
    ValueMeta meta = new ValueMeta("test");
    
    meta.setType(ValueMetaInterface.TYPE_STRING);
    assertEquals(Text.class, TypeConverterFactory.getWritableForKettleType(meta));

    meta.setType(ValueMetaInterface.TYPE_BIGNUMBER);
    assertEquals(Text.class, TypeConverterFactory.getWritableForKettleType(meta));

    meta.setType(ValueMetaInterface.TYPE_DATE);
    assertEquals(Text.class, TypeConverterFactory.getWritableForKettleType(meta));

    meta.setType(ValueMetaInterface.TYPE_INTEGER);
    assertEquals(LongWritable.class, TypeConverterFactory.getWritableForKettleType(meta));

    meta.setType(ValueMetaInterface.TYPE_NUMBER);
    assertEquals(DoubleWritable.class, TypeConverterFactory.getWritableForKettleType(meta));

    meta.setType(ValueMetaInterface.TYPE_BOOLEAN);
    assertEquals(BooleanWritable.class, TypeConverterFactory.getWritableForKettleType(meta));

    meta.setType(ValueMetaInterface.TYPE_BINARY);
    assertEquals(BytesWritable.class, TypeConverterFactory.getWritableForKettleType(meta));

    // Default is Text
    meta.setType(ValueMetaInterface.TYPE_SERIALIZABLE);
    assertEquals(Text.class, TypeConverterFactory.getWritableForKettleType(meta));
    meta.setType(ValueMetaInterface.TYPE_NONE);
    assertEquals(Text.class, TypeConverterFactory.getWritableForKettleType(meta));
  }
  
  @Test
  public void getConverter() throws TypeConversionException {
    TypeConverterFactory factory = new TypeConverterFactory();
    ITypeConverter converter = new ObjectToStringConverter();

    try {
      factory.getConverter(Object.class, Long.class);
      fail("Expected exception when looking up converter for class combination that doesn't exist");
    } catch (TypeConversionException ex) {
      assertTrue(ex.getMessage().contains("No converter found to convert"));
    }

    factory.registerConverter(Object.class, String.class, converter);
    assertEquals(converter, factory.getConverter(Object.class, String.class));
  }
  
  @Test
  public void getJavaClass() {
    TypeConverterFactory factory = new TypeConverterFactory();
    ValueMeta valueMeta = new ValueMeta("test");
    
    valueMeta.setType(ValueMetaInterface.TYPE_BIGNUMBER);
    assertEquals(BigDecimal.class, factory.getJavaClass(valueMeta));

    valueMeta.setType(ValueMetaInterface.TYPE_BINARY);
    assertEquals(byte[].class, factory.getJavaClass(valueMeta));

    valueMeta.setType(ValueMetaInterface.TYPE_BOOLEAN);
    assertEquals(Boolean.class, factory.getJavaClass(valueMeta));

    valueMeta.setType(ValueMetaInterface.TYPE_DATE);
    assertEquals(Date.class, factory.getJavaClass(valueMeta));

    valueMeta.setType(ValueMetaInterface.TYPE_INTEGER);
    assertEquals(Long.class, factory.getJavaClass(valueMeta));

    valueMeta.setType(ValueMetaInterface.TYPE_NONE);
    assertEquals(null, factory.getJavaClass(valueMeta));

    valueMeta.setType(ValueMetaInterface.TYPE_NUMBER);
    assertEquals(Double.class, factory.getJavaClass(valueMeta));

    valueMeta.setType(ValueMetaInterface.TYPE_SERIALIZABLE);
    assertNull(factory.getJavaClass(valueMeta));
    
    valueMeta.setType(ValueMetaInterface.TYPE_STRING);
    assertEquals(String.class, factory.getJavaClass(valueMeta));
  }

  @Test
  public void getConverter_with_ValueMetaInterface() throws TypeConversionException {
    TypeConverterFactory factory = new TypeConverterFactory();
    ValueMeta valueMeta = new ValueMeta("test");

    // A converter that says it will convert everything but does nothing
    ITypeConverter mockConverter = new ITypeConverter<Object, Object>() {
      @Override
      public boolean canConvert(Class from, Class to) {
        return true;
      }

      @Override
      public Object convert(ValueMetaInterface meta, Object obj) throws TypeConversionException {
        return obj;
      }
    };
    
    factory.registerConverter(Object.class, Object.class, mockConverter);
    factory.registerConverter(Object.class, BigDecimal.class, mockConverter);
    
    // We should get a converter for types with a valid Java Class mapping
    valueMeta.setType(ValueMetaInterface.TYPE_BIGNUMBER);
    assertNotNull(factory.getConverter(Object.class, valueMeta));

    // We shouldn't get a converter for types without a valid Java Class mapping
    valueMeta.setType(ValueMetaInterface.TYPE_NONE);
    assertNull(factory.getConverter(Object.class, valueMeta));
  }
}
