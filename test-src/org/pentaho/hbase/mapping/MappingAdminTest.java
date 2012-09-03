package org.pentaho.hbase.mapping;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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

    Mapping aMapping = new Mapping("TestTable", "TestMapping", "KEY",
        Mapping.KeyType.STRING);

    HBaseValueMeta vm = new HBaseValueMeta("Family1" + HBaseValueMeta.SEPARATOR
        + "Col1" + HBaseValueMeta.SEPARATOR + "Col1",
        ValueMetaInterface.TYPE_STRING, -1, -1);
    aMapping.addMappedColumn(vm, false);

    ma.putMapping(aMapping, false);

    assertTrue(ma.mappingExists("TestTable", "TestMapping"));
  }
}
