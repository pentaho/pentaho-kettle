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

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.junit.Test;

import static org.junit.Assert.*;

public class NullWritableConverterTest {
  @Test
  public void canConvert() throws Exception {
    NullWritableConverter c = new NullWritableConverter();

    assertTrue(c.canConvert(Object.class, NullWritable.class));
    assertTrue(c.canConvert(null, NullWritable.class));
    assertFalse(c.canConvert(null, null));
    assertFalse(c.canConvert(LongWritable.class, Object.class));
    assertFalse(c.canConvert(Object.class, Text.class));
  }

  @Test
  public void convert() throws Exception {
    NullWritableConverter c = new NullWritableConverter();
    Long expected = 10L;

    assertEquals(NullWritable.get(), c.convert(null, new LongWritable(expected)));
    assertEquals(NullWritable.get(), c.convert(null, null));
  }
}
