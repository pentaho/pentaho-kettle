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

import org.junit.Test;

import static org.junit.Assert.*;

public class NullConverterTest {
  @Test
  public void canConvert() throws Exception {
    NullConverter c = new NullConverter();

    assertTrue(c.canConvert(null, null));
    assertTrue(c.canConvert(null, Object.class));
    assertTrue(c.canConvert(Object.class, null));
    assertFalse(c.canConvert(String.class, Long.class));
    assertFalse(c.canConvert(Long.class, Object.class));
    assertFalse(c.canConvert(Object.class, Long.class));
  }

  @Test
  public void convert() throws Exception {
    NullConverter c = new NullConverter();

    assertNull(c.convert(null, new Object()));
    assertNull(c.convert(null, null));
  }
}
