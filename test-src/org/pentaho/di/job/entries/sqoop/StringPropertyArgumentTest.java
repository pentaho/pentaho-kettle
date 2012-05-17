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

package org.pentaho.di.job.entries.sqoop;

import org.junit.BeforeClass;
import org.junit.Test;

import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class StringPropertyArgumentTest {
  private static Method SETTER;
  private static Method GETTER;

  @BeforeClass
  public static void init() throws NoSuchMethodException {
    SETTER = SqoopImportConfig.class.getMethod("setJobEntryName", String.class);
    GETTER = SqoopImportConfig.class.getMethod("getJobEntryName");
  }

  @Test(expected = NullPointerException.class)
  public void instantiation_null_name() {
    new StringPropertyArgument(null, "display", new SqoopImportConfig(), SETTER, GETTER);
  }

  @Test(expected = NullPointerException.class)
  public void instantiation_null_display_name() {
    new StringPropertyArgument("name", null, new SqoopImportConfig(), SETTER, GETTER);
  }

  @Test(expected = NullPointerException.class)
  public void instantiation_null_object() {
    new StringPropertyArgument("name", "display", null, SETTER, GETTER);
  }

  @Test(expected = NullPointerException.class)
  public void instantiation_null_setter() {
    new StringPropertyArgument("name", "display", new SqoopExportConfig(), null, GETTER);
  }

  @Test(expected = NullPointerException.class)
  public void instantiation_null_getter() throws Exception {
    new StringPropertyArgument("name", "display", new SqoopExportConfig(), SETTER, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void instantiation_wrong_setter_method_parameters() throws NoSuchMethodException {
    Method m = getClass().getMethod("instantiation_wrong_setter_method_parameters");
    new StringPropertyArgument("name", "display", new SqoopExportConfig(), m, GETTER);
  }

  @Test(expected = IllegalArgumentException.class)
  public void instantiation_wrong_getter_method_return_type() throws NoSuchMethodException {
    Method m = getClass().getMethod("instantiation_wrong_getter_method_return_type");
    new StringPropertyArgument("name", "display", new SqoopExportConfig(), SETTER, m);
  }

  @Test
  public void instantiation() throws NoSuchMethodException {
    SqoopImportConfig testObj = new SqoopImportConfig();
    StringPropertyArgument arg = new StringPropertyArgument("name", "display", testObj, SETTER, GETTER);
    assertNotNull(arg);
  }

  @Test
  public void setValue() throws NoSuchMethodException {
    SqoopImportConfig testObj = new SqoopImportConfig();
    StringPropertyArgument arg = new StringPropertyArgument("name", "display", testObj, SETTER, GETTER);
    assertNull(testObj.getJobEntryName());
    arg.setValue("testing");
    assertEquals("testing", testObj.getJobEntryName());
  }

  @Test
  public void getValue() throws NoSuchMethodException {
    SqoopImportConfig testObj = new SqoopImportConfig();
    testObj.setJobEntryName("testing");
    StringPropertyArgument arg = new StringPropertyArgument("name", "display", testObj, SETTER, GETTER);
    assertEquals("testing", arg.getValue());
  }
}
