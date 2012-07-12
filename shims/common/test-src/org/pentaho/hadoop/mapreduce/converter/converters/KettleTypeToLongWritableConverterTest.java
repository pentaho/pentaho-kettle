/*******************************************************************************
 *
 * Pentaho Big Data
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
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

package org.pentaho.hadoop.mapreduce.converter.converters;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.junit.Test;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.hadoop.mapreduce.converter.TypeConversionException;

import static org.junit.Assert.*;

public class KettleTypeToLongWritableConverterTest {
  @Test
  public void canConvert() throws Exception {
    KettleTypeToLongWritableConverter c = new KettleTypeToLongWritableConverter();

    assertTrue(c.canConvert(Long.class, LongWritable.class));
    assertTrue(c.canConvert(String.class, LongWritable.class));
    assertFalse(c.canConvert(Object.class, LongWritable.class));
    assertFalse(c.canConvert(IntWritable.class, LongWritable.class));
    assertFalse(c.canConvert(null, null));
    assertFalse(c.canConvert(IntWritable.class, Object.class));
    assertFalse(c.canConvert(Object.class, Long.class));
  }

  @Test
  public void convert() throws Exception {
    KettleTypeToLongWritableConverter c = new KettleTypeToLongWritableConverter();
    LongWritable expected = new LongWritable(100);
    String value = "100";

    // Convert from a normal String
    ValueMeta normalMeta = new ValueMeta("test", ValueMetaInterface.TYPE_STRING, ValueMetaInterface.STORAGE_TYPE_NORMAL);
    assertEquals(expected, c.convert(normalMeta, value));

    // Convert from a byte array
    ValueMeta binaryMeta = new ValueMeta("test", ValueMetaInterface.TYPE_STRING, ValueMetaInterface.STORAGE_TYPE_BINARY_STRING);
    ValueMeta storageMeta = new ValueMeta("test", ValueMetaInterface.TYPE_STRING, ValueMetaInterface.STORAGE_TYPE_NORMAL);
    binaryMeta.setStorageMetadata(storageMeta);
    byte[] rawValue = value.getBytes("UTF-8");
    assertEquals(expected, c.convert(binaryMeta, rawValue));

    // Convert from an Integer
    ValueMeta integerMeta = new ValueMeta("test", ValueMetaInterface.TYPE_INTEGER, ValueMetaInterface.STORAGE_TYPE_NORMAL);
    assertEquals(expected, c.convert(integerMeta, Long.valueOf(100)));

    try {
      c.convert(null, null);
      fail();
    } catch (NullPointerException ex) {
      // Expected
    }

    try {
      c.convert(integerMeta, "not an integer");
      fail();
    } catch (TypeConversionException ex) {
      assertTrue(ex.getMessage().contains("Error converting to"));
    }

  }
}
