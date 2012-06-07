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
import org.pentaho.di.job.entries.helper.PersistentPropertyChangeListener;
import org.pentaho.ui.xul.XulEventSource;

import java.lang.reflect.Method;

import static org.junit.Assert.*;

public class ArgumentWrapperTest {
  private static final String FIELD_NAME = "jobEntryName";
  private static Method SETTER;
  private static Method GETTER;

  private interface TestInterface {
    void testSetter(Boolean b);
  }

  @BeforeClass
  public static void init() throws NoSuchMethodException {
    SETTER = SqoopImportConfig.class.getMethod("setJobEntryName", String.class);
    GETTER = SqoopImportConfig.class.getMethod("getJobEntryName");
  }

  @Test
  public void isXulEventSource() {
    assertTrue(XulEventSource.class.isAssignableFrom(ArgumentWrapper.class));
  }

  @Test
  public void testEquals() {
    ArgumentWrapper a = new ArgumentWrapper("arg", null, false, new SqoopImportConfig(), GETTER, SETTER);
    ArgumentWrapper b = new ArgumentWrapper("arg", null, true, new SqoopExportConfig(), GETTER, SETTER);
    ArgumentWrapper c = new ArgumentWrapper("arg2", null, true, new SqoopExportConfig(), GETTER, SETTER);

    assertTrue(a.equals(a));
    assertTrue(a.equals(b));
    assertFalse(a.equals(c));
    assertFalse(a.equals(null));
    assertFalse(a.equals(""));
  }

  @Test
  public void testHashCode() {
    ArgumentWrapper a = new ArgumentWrapper("arg", null, false, new SqoopImportConfig(), GETTER, SETTER);
    ArgumentWrapper b = new ArgumentWrapper("arg", null, true, new SqoopExportConfig(), GETTER, SETTER);
    ArgumentWrapper c = new ArgumentWrapper("arg2", null, true, new SqoopExportConfig(), GETTER, SETTER);

    assertEquals(a.hashCode(), b.hashCode());
    assertFalse(a.hashCode() == c.hashCode());
  }

  @Test(expected = NullPointerException.class)
  public void instantiation_null_name() {
    new ArgumentWrapper(null, "display", false, new SqoopImportConfig(), GETTER, SETTER);
  }

  @Test(expected = NullPointerException.class)
  public void instantiation_null_object() {
    new ArgumentWrapper("name", "display", false, null, GETTER, SETTER);
  }

  @Test(expected = NullPointerException.class)
  public void instantiation_null_setter() {
    new ArgumentWrapper("name", "display", false, new SqoopExportConfig(), GETTER, null);
  }

  @Test(expected = NullPointerException.class)
  public void instantiation_null_getter() throws Exception {
    new ArgumentWrapper("name", "display", false, new SqoopExportConfig(), null, SETTER);
  }

  @Test(expected = IllegalArgumentException.class)
  public void instantiation_wrong_setter_method_parameters() throws NoSuchMethodException {
    Method m = getClass().getMethod("instantiation_wrong_setter_method_parameters");
    assertNotNull(m);
    new ArgumentWrapper("name", "display", false, new SqoopExportConfig(), GETTER, m);
  }

  @Test(expected = IllegalArgumentException.class)
  public void instantiation_wrong_setter_method_parameters2() throws NoSuchMethodException {
    Method m = TestInterface.class.getMethod("testSetter", Boolean.class);
    assertNotNull(m);
    new ArgumentWrapper("name", "display", false, new SqoopExportConfig(), GETTER, m);
  }

  @Test(expected = IllegalArgumentException.class)
  public void instantiation_wrong_getter_method_return_type() throws NoSuchMethodException {
    Method m = getClass().getMethod("instantiation_wrong_getter_method_return_type");
    assertNotNull(m);
    new ArgumentWrapper("name", "display", false, new SqoopExportConfig(), m, SETTER);
  }

  @Test
  public void instantiation() {
    SqoopImportConfig testObj = new SqoopImportConfig();
    ArgumentWrapper arg1 = new ArgumentWrapper("name1", "display1", false, testObj, GETTER, SETTER);
    ArgumentWrapper arg2 = new ArgumentWrapper("name2", "display2", true, testObj, GETTER, SETTER);

    assertNotNull(arg1);
    assertEquals("name1", arg1.getName());
    assertEquals("display1", arg1.getDisplayName());
    assertFalse(arg1.isFlag());
    assertNull(arg1.getValue());

    assertNotNull(arg2);
    assertEquals("name2", arg2.getName());
    assertEquals("display2", arg2.getDisplayName());
    assertTrue(arg2.isFlag());
    assertNull(arg2.getValue());
  }

  @Test
  public void setName() {
    SqoopImportConfig testObj = new SqoopImportConfig();
    ArgumentWrapper arg = new ArgumentWrapper("name", "display", false, testObj, GETTER, SETTER);
    arg.setName("testing");
    assertEquals("testing", arg.getName());
  }

  @Test
  public void setDisplayName() {
    SqoopImportConfig testObj = new SqoopImportConfig();
    ArgumentWrapper arg = new ArgumentWrapper("name", "display", false, testObj, GETTER, SETTER);
    arg.setDisplayName("testing");
    assertEquals("testing", arg.getDisplayName());
  }

  @Test
  public void setValue() {
    SqoopImportConfig testObj = new SqoopImportConfig();
    PersistentPropertyChangeListener listener = new PersistentPropertyChangeListener();
    testObj.addPropertyChangeListener(FIELD_NAME, listener);
    ArgumentWrapper arg = new ArgumentWrapper("name", "display", false, testObj, GETTER, SETTER);
    assertNull(testObj.getJobEntryName());
    arg.setValue("testing");
    assertEquals("testing", testObj.getJobEntryName());
    assertEquals(1, listener.getReceivedEvents().size());
  }

  @Test
  public void getValue() {
    SqoopImportConfig testObj = new SqoopImportConfig();
    testObj.setJobEntryName("testing");
    ArgumentWrapper arg = new ArgumentWrapper("name", "display", false, testObj, GETTER, SETTER);
    assertEquals("testing", arg.getValue());
  }

  @Test
  public void setFlag() {
    SqoopImportConfig testObj = new SqoopImportConfig();
    ArgumentWrapper arg = new ArgumentWrapper("name", "display", false, testObj, GETTER, SETTER);
    assertFalse(arg.isFlag());
    arg.setFlag(true);
    assertTrue(arg.isFlag());
  }
}
