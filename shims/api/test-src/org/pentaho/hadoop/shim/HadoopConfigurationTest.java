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

import org.junit.Test;
import org.pentaho.hadoop.shim.spi.HadoopShim;
import org.pentaho.hadoop.shim.spi.MockHadoopShim;
import org.pentaho.hadoop.shim.spi.MockPigShim;
import org.pentaho.hadoop.shim.spi.MockSnappyShim;
import org.pentaho.hadoop.shim.spi.MockSqoopShim;
import org.pentaho.hadoop.shim.spi.PigShim;
import org.pentaho.hadoop.shim.spi.SnappyShim;
import org.pentaho.hadoop.shim.spi.SqoopShim;

public class HadoopConfigurationTest {

  @Test(expected=NullPointerException.class)
  public void instantiation_null() {
    new HadoopConfiguration("id", "name", null, null, null, null);
  }

  @Test
  public void getHadoopShim() {
    HadoopShim hadoopShim = new MockHadoopShim();
    HadoopConfiguration c = new HadoopConfiguration("id", "name", hadoopShim, null, null, null);
    assertEquals(hadoopShim, c.getHadoopShim());
  }
  
  @Test
  public void getSqoopShim() throws ConfigurationException {
    HadoopShim hadoopShim = new MockHadoopShim();
    SqoopShim sqoopShim = new MockSqoopShim();
    HadoopConfiguration c = new HadoopConfiguration("id", "name", hadoopShim, sqoopShim, null, null);
    assertEquals(sqoopShim, c.getSqoopShim());
  }

  @Test
  public void getSqoopShim_not_set() {
    HadoopShim hadoopShim = new MockHadoopShim();
    HadoopConfiguration c = new HadoopConfiguration("id", "name", hadoopShim, null, null, null);
    try {
      c.getSqoopShim();
      fail("Expected exception");
    } catch (ConfigurationException ex) {
      assertNotNull(ex.getMessage());
    }
  }
  
  @Test
  public void getPigShim() throws ConfigurationException {
    HadoopShim hadoopShim = new MockHadoopShim();
    PigShim pigShim = new MockPigShim();
    HadoopConfiguration c = new HadoopConfiguration("id", "name", hadoopShim, null, pigShim, null);
    assertEquals(pigShim, c.getPigShim());
  }
  
  @Test
  public void getPigShim_not_set() {
    HadoopShim hadoopShim = new MockHadoopShim();
    HadoopConfiguration c = new HadoopConfiguration("id", "name", hadoopShim, null, null, null);
    try {
      c.getPigShim();
      fail("Expected exception");
    } catch (ConfigurationException ex) {
      assertNotNull(ex.getMessage());
    }
  }
  
  @Test
  public void getSnappyShim() throws ConfigurationException {
    HadoopShim hadoopShim = new MockHadoopShim();
    SnappyShim snappyShim = new MockSnappyShim();
    HadoopConfiguration c = new HadoopConfiguration("id", "name", hadoopShim, null, null, snappyShim);
    assertEquals(snappyShim, c.getSnappyShim());
  }
  
  @Test
  public void getSNappyShim_not_set() {
    HadoopShim hadoopShim = new MockHadoopShim();
    HadoopConfiguration c = new HadoopConfiguration("id", "name", hadoopShim, null, null, null);
    try {
      c.getSnappyShim();
      fail("Expected exception");
    } catch (ConfigurationException ex) {
      assertNotNull(ex.getMessage());
    }
  }
  
  @Test
  public void testToString() {
    HadoopConfiguration c = new HadoopConfiguration("id", "name", new MockHadoopShim(), null, null, null);
    assertEquals(c.getIdentifier(), c.toString());
  }
}
