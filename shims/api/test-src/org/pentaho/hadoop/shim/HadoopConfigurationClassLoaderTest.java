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

package org.pentaho.hadoop.shim;

import static org.junit.Assert.*;

import java.net.URL;

import org.junit.Test;

public class HadoopConfigurationClassLoaderTest {

  @Test(expected = NullPointerException.class)
  public void instantiation_null_URLs() {
    new HadoopConfigurationClassLoader(null, null);
  }

  @Test(expected = NullPointerException.class)
  public void instantiation_null_parent() {
    new HadoopConfigurationClassLoader(new URL[0], null);
  }

  @Test
  public void ignoreClass_built_ins() {
    HadoopConfigurationClassLoader hccl = new HadoopConfigurationClassLoader(new URL[0], getClass().getClassLoader());
    assertTrue(hccl.ignoreClass("org.apache.commons.log"));
    assertTrue(hccl.ignoreClass("org.apache.log4j"));
    assertTrue(hccl.ignoreClass("org.apache.log4j.Logger"));
    assertFalse(hccl.ignoreClass("bogus"));
    assertTrue(hccl.ignoreClass(null));
  }
}
