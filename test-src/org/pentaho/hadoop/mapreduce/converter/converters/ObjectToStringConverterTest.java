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

public class ObjectToStringConverterTest {
  @Test
  public void canConvert() throws Exception {
    ObjectToStringConverter c = new ObjectToStringConverter();

    assertTrue(c.canConvert(Object.class, String.class));
    assertTrue(c.canConvert(null, String.class));
    assertFalse(c.canConvert(null, null));
    assertFalse(c.canConvert(String.class, Object.class));
  }

  @Test
  public void convert() throws Exception {
    ObjectToStringConverter c = new ObjectToStringConverter();
    String expected = "10";

    assertEquals(expected, c.convert(null, new LongWritable(10L)));
    assertEquals(expected, c.convert(null, new LongWritable(10L)));
    assertEquals(expected, c.convert(null, 10L));

    try {
      c.convert(null, null);
      fail();
    } catch (NullPointerException ex) {
      // Expected
    }
  }
}
