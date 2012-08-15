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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.VFS;
import org.junit.Test;
import org.pentaho.hadoop.shim.spi.HadoopShim;
import org.pentaho.hadoop.shim.spi.MockHBaseShim;
import org.pentaho.hadoop.shim.spi.MockHadoopShim;
import org.pentaho.hadoop.shim.spi.MockPigShim;
import org.pentaho.hadoop.shim.spi.MockSnappyShim;
import org.pentaho.hadoop.shim.spi.MockSqoopShim;
import org.pentaho.hadoop.shim.spi.PigShim;
import org.pentaho.hadoop.shim.spi.SnappyShim;
import org.pentaho.hadoop.shim.spi.SqoopShim;
import org.pentaho.hbase.shim.spi.HBaseShim;

public class HadoopConfigurationTest {
  
  @Test(expected=NullPointerException.class)
  public void instantiation_null_location() {
    new HadoopConfiguration(null, "id", "name", new MockHadoopShim());
  }

  @Test(expected=NullPointerException.class)
  public void instantiation_null_id() throws Exception {
    new HadoopConfiguration(VFS.getManager().resolveFile("ram:///"), null, "name", new MockHadoopShim());
  }

  @Test(expected=NullPointerException.class)
  public void instantiation_null_name() throws Exception {
    new HadoopConfiguration(VFS.getManager().resolveFile("ram:///"), "id", null, new MockHadoopShim());
  }
  
  @Test(expected=NullPointerException.class)
  public void instantiation_null_hadoop_shim() throws Exception {
    new HadoopConfiguration(VFS.getManager().resolveFile("ram:///"), "id", "name", null);
  }

  @Test
  public void getLocation() throws Exception {
    FileObject location = VFS.getManager().resolveFile("ram:///");
    HadoopConfiguration c = new HadoopConfiguration(location, "id", "name", new MockHadoopShim());
    assertEquals(location, c.getLocation());
  }

  @Test
  public void getHadoopShim() throws Exception {
    HadoopShim hadoopShim = new MockHadoopShim();
    HadoopConfiguration c = new HadoopConfiguration(VFS.getManager().resolveFile("ram:///"), "id", "name", hadoopShim);
    assertEquals(hadoopShim, c.getHadoopShim());
  }

  @Test
  public void getSqoopShim() throws Exception {
    HadoopShim hadoopShim = new MockHadoopShim();
    SqoopShim sqoopShim = new MockSqoopShim();
    HadoopConfiguration c = new HadoopConfiguration(VFS.getManager().resolveFile("ram:///"), "id", "name", hadoopShim, sqoopShim);
    assertEquals(sqoopShim, c.getSqoopShim());
  }

  @Test
  public void getSqoopShim_not_set() throws Exception {
    HadoopShim hadoopShim = new MockHadoopShim();
    HadoopConfiguration c = new HadoopConfiguration(VFS.getManager().resolveFile("ram:///"), "id", "name", hadoopShim);
    try {
      c.getSqoopShim();
      fail("Expected exception");
    } catch (ConfigurationException ex) {
      assertNotNull(ex.getMessage());
    }
  }

  @Test
  public void getPigShim() throws Exception {
    HadoopShim hadoopShim = new MockHadoopShim();
    PigShim pigShim = new MockPigShim();
    HadoopConfiguration c = new HadoopConfiguration(VFS.getManager().resolveFile("ram:///"), "id", "name", hadoopShim, pigShim, null);
    assertEquals(pigShim, c.getPigShim());
  }

  @Test
  public void getPigShim_not_set() throws Exception {
    HadoopShim hadoopShim = new MockHadoopShim();
    HadoopConfiguration c = new HadoopConfiguration(VFS.getManager().resolveFile("ram:///"), "id", "name", hadoopShim);
    try {
      c.getPigShim();
      fail("Expected exception");
    } catch (ConfigurationException ex) {
      assertNotNull(ex.getMessage());
    }
  }

  @Test
  public void getSnappyShim() throws Exception {
    HadoopShim hadoopShim = new MockHadoopShim();
    SnappyShim snappyShim = new MockSnappyShim();
    HadoopConfiguration c = new HadoopConfiguration(VFS.getManager().resolveFile("ram:///"), "id", "name", hadoopShim, snappyShim);
    assertEquals(snappyShim, c.getSnappyShim());
  }
  
  @Test
  public void getSnappyShim_not_set() throws Exception {
    HadoopShim hadoopShim = new MockHadoopShim();
    HadoopConfiguration c = new HadoopConfiguration(VFS.getManager().resolveFile("ram:///"), "id", "name", hadoopShim);
    try {
      c.getSnappyShim();
      fail("Expected exception");
    } catch (ConfigurationException ex) {
      assertNotNull(ex.getMessage());
    }
  }
  
  @Test
  public void getHBaseShim() throws Exception {
    HadoopShim hadoopShim = new MockHadoopShim();
    HBaseShim hbaseShim = new MockHBaseShim();
    HadoopConfiguration c = new HadoopConfiguration(VFS.getManager().resolveFile("ram:///"), "id", "name", hadoopShim, hbaseShim);
    assertEquals(hbaseShim, c.getHBaseShim());
  }

  @Test
  public void getHBaseShim_not_set() throws Exception {
    HadoopShim hadoopShim = new MockHadoopShim();
    HadoopConfiguration c = new HadoopConfiguration(VFS.getManager().resolveFile("ram:///"), "id", "name", hadoopShim);
    try {
      c.getHBaseShim();
      fail("Expected exception");
    } catch (ConfigurationException ex) {
      assertNotNull(ex.getMessage());
    }
  }
  
  @Test
  public void testToString() throws Exception {
    HadoopConfiguration c = new HadoopConfiguration(VFS.getManager().resolveFile("ram:///"), "id", "name", new MockHadoopShim());
    assertEquals(c.getIdentifier(), c.toString());
  }
}
