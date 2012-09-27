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

package org.pentaho.hbase.mapping;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.hbase.shim.api.HBaseValueMeta;
import org.pentaho.hbase.shim.api.Mapping;
import org.pentaho.hbase.shim.fake.FakeHBaseConnection;
import org.pentaho.hbase.shim.spi.HBaseBytesUtilShim;
import org.pentaho.hbase.shim.spi.HBaseConnection;

public class MappingAdminTest {

  protected MappingAdmin initMappingAdmin(HBaseConnection conn)
      throws Exception {
    HBaseBytesUtilShim bytesUtil = conn.getBytesUtil();
    MappingAdmin ma = new MappingAdmin(conn, bytesUtil);
    ma.createMappingTable();

    return ma;
  }

  protected void createAMapping(MappingAdmin ma, boolean overwrite)
      throws Exception {
    Mapping aMapping = new Mapping("TestTable", "TestMapping", "TestTableKey",
        Mapping.KeyType.STRING);

    HBaseValueMeta vm = new HBaseValueMeta("Family1" + HBaseValueMeta.SEPARATOR
        + "Col1" + HBaseValueMeta.SEPARATOR + "Col1",
        ValueMetaInterface.TYPE_STRING, -1, -1);
    aMapping.addMappedColumn(vm, false);

    ma.putMapping(aMapping, overwrite);
  }

  @Test
  public void testCreateMappingTable() throws Exception {
    FakeHBaseConnection conn = new FakeHBaseConnection();

    MappingAdmin ma = initMappingAdmin(conn);

    assertTrue(conn.tableExists(ma.m_mappingTableName));
  }

  @Test
  public void testCreateMappingTableTwice() throws Exception {
    FakeHBaseConnection conn = new FakeHBaseConnection();

    MappingAdmin ma = initMappingAdmin(conn);

    assertTrue(conn.tableExists(ma.m_mappingTableName));

    // create again - should get an exception
    try {
      ma.createMappingTable();
      fail("Was expecting an exception as the mapping table already exists");
    } catch (Exception ex) {
    }
  }

  @Test
  public void testAddMapping() throws Exception {
    FakeHBaseConnection conn = new FakeHBaseConnection();

    MappingAdmin ma = initMappingAdmin(conn);
    createAMapping(ma, false);

    assertTrue(ma.mappingExists("TestTable", "TestMapping"));
  }

  @Test
  public void testAddMappingTwiceNoOverwrite() throws Exception {
    FakeHBaseConnection conn = new FakeHBaseConnection();

    MappingAdmin ma = initMappingAdmin(conn);
    createAMapping(ma, false);

    assertTrue(ma.mappingExists("TestTable", "TestMapping"));

    try {
      createAMapping(ma, false);
      fail("Was expecting an exception to be generated when adding a mapping that "
          + "already exists when overwrite is set to false");
    } catch (Exception ex) {
    }
  }

  @Test
  public void testAddMappingTwiceOverwrite() throws Exception {
    FakeHBaseConnection conn = new FakeHBaseConnection();

    MappingAdmin ma = initMappingAdmin(conn);
    createAMapping(ma, false);

    assertTrue(ma.mappingExists("TestTable", "TestMapping"));

    createAMapping(ma, true);
    assertTrue(ma.mappingExists("TestTable", "TestMapping"));
    assertEquals(ma.getMappedTables().size(), 1);
    assertEquals(ma.getMappingNames("TestTable").size(), 1);
    assertEquals(ma.getMappingNames("TestTable").get(0), "TestMapping");
  }

  @Test
  public void testGetMappedTables() throws Exception {
    FakeHBaseConnection conn = new FakeHBaseConnection();

    MappingAdmin ma = initMappingAdmin(conn);

    // no mapped tables yet
    assertTrue(ma.getMappedTables().size() == 0);

    createAMapping(ma, false);
    assertTrue(ma.getMappedTables().size() == 1);
  }

  @Test
  public void testGetMappingNames() throws Exception {
    FakeHBaseConnection conn = new FakeHBaseConnection();

    MappingAdmin ma = initMappingAdmin(conn);

    // no mapped tables yet
    assertTrue(ma.getMappedTables().size() == 0);

    createAMapping(ma, false);

    List<String> mappings = ma.getMappingNames("TestTable");
    assertTrue(mappings != null);
    assertEquals(mappings.size(), 1);
    assertEquals(mappings.get(0), "TestMapping");
  }

  @Test
  public void testRetrieveMapping() throws Exception {
    FakeHBaseConnection conn = new FakeHBaseConnection();

    MappingAdmin ma = initMappingAdmin(conn);
    createAMapping(ma, false);

    assertTrue(ma.mappingExists("TestTable", "TestMapping"));

    Mapping retrieved = ma.getMapping("TestTable", "TestMapping");

    assertTrue(retrieved != null);

    assertEquals(retrieved.getKeyName(), "TestTableKey");
    assertEquals(retrieved.getKeyType(), Mapping.KeyType.STRING);
    assertTrue(!retrieved.isTupleMapping());
    assertTrue(retrieved.getMappedColumns() != null);
    assertTrue(retrieved.getMappedColumns().size() == 1);
    Set<Map.Entry<String, HBaseValueMeta>> cols = retrieved.getMappedColumns()
        .entrySet();
    HBaseValueMeta theCol = cols.iterator().next().getValue();
    assertEquals(theCol.getColumnFamily(), "Family1");
    assertEquals(theCol.getAlias(), "Col1");
    assertEquals(theCol.getColumnName(), "Col1");
  }

  @Test
  public void testRetrieveNonExistentMapping() throws Exception {
    FakeHBaseConnection conn = new FakeHBaseConnection();

    MappingAdmin ma = initMappingAdmin(conn);

    try {
      ma.getMapping("BogusTable", "BogusMapping");
      fail("Was expecting an exception as this table/mapping combo doesn't exist!");
    } catch (IOException ex) {
    }
  }

  @Test
  public void testDeleteMapping() throws Exception {
    FakeHBaseConnection conn = new FakeHBaseConnection();
    MappingAdmin ma = initMappingAdmin(conn);
    createAMapping(ma, false);

    assertTrue(ma.mappingExists("TestTable", "TestMapping"));

    ma.deleteMapping("TestTable", "TestMapping");

    assertFalse(ma.mappingExists("TestTable", "TestMapping"));
  }
}
