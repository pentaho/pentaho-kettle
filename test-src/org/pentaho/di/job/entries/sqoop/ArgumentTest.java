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

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class ArgumentTest {
  @Test
  public void testEquals() {
    Argument a = new Argument("arg", "value1");
    Argument b = new Argument("arg", "value2");

    assertTrue(a.equals(b));
  }

  @Test
  public void testHashCode() {
    Argument a = new Argument("arg", "value1");
    Argument b = new Argument("arg", "value2");

    assertEquals(a.hashCode(), b.hashCode());
  }

  @Test
  public void name() {
    String name = "test";
    Argument a = new Argument();
    assertNull(a.getName());
    a.setName(name);
    assertEquals(name, a.getName());
  }

  @Test
  public void displayName() {
    String displayName = "test";
    Argument a = new Argument();
    assertNull(a.getDisplayName());
    a.setDisplayName(displayName);
    assertEquals(displayName, a.getDisplayName());
  }

  @Test
  public void flag() {
    Argument a = new Argument();
    assertFalse(a.isFlag());

    a.setFlag(false);
    assertFalse(a.isFlag());

    a.setFlag(true);
    assertTrue(a.isFlag());
  }

  @Test(expected = NullPointerException.class)
  public void setFlag_null() {
    Argument a = new Argument();
    a.setFlag(null);
  }
}
