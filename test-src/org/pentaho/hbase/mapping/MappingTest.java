package org.pentaho.hbase.mapping;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.hbase.shim.api.HBaseValueMeta;
import org.pentaho.hbase.shim.api.Mapping;

public class MappingTest {

  @Test
  public void testMappingConfigBasic() {
    Mapping aMapping = new Mapping("TestTable", "TestMapping", "MyKey",
        Mapping.KeyType.INTEGER);

    assertEquals(aMapping.getTableName(), "TestTable");
    assertEquals(aMapping.getMappingName(), "TestMapping");
    assertEquals(aMapping.getKeyName(), "MyKey");
    assertEquals(aMapping.getKeyType(), Mapping.KeyType.INTEGER);
  }

  @Test
  public void testAddMappedColum() throws Exception {
    Mapping aMapping = new Mapping("TestTable", "TestMapping", "MyKey",
        Mapping.KeyType.INTEGER);

    HBaseValueMeta vm = new HBaseValueMeta("Family1" + HBaseValueMeta.SEPARATOR
        + "Col1" + HBaseValueMeta.SEPARATOR + "Col1",
        ValueMetaInterface.TYPE_STRING, -1, -1);

    aMapping.addMappedColumn(vm, false);

    assertTrue(aMapping.getMappedColumns().containsKey("Col1"));
  }

  @Test
  public void testAddSameMappedColumTwice() throws Exception {
    Mapping aMapping = new Mapping("TestTable", "TestMapping", "MyKey",
        Mapping.KeyType.INTEGER);

    HBaseValueMeta vm = new HBaseValueMeta("Family1" + HBaseValueMeta.SEPARATOR
        + "Col1" + HBaseValueMeta.SEPARATOR + "Col1",
        ValueMetaInterface.TYPE_STRING, -1, -1);

    aMapping.addMappedColumn(vm, false);
    assertTrue(aMapping.getMappedColumns().containsKey("Col1"));

    try {
      aMapping.addMappedColumn(vm, false);
      fail("Was expecting an exception because this family+column combo is already in the mapping");
    } catch (Exception ex) {
    }
  }

  @Test
  public void testAddColumnWithSameAliasTwice() throws Exception {
    Mapping aMapping = new Mapping("TestTable", "TestMapping", "MyKey",
        Mapping.KeyType.INTEGER);

    HBaseValueMeta vm = new HBaseValueMeta("Family1" + HBaseValueMeta.SEPARATOR
        + "Col1" + HBaseValueMeta.SEPARATOR + "Col1",
        ValueMetaInterface.TYPE_STRING, -1, -1);

    aMapping.addMappedColumn(vm, false);
    assertTrue(aMapping.getMappedColumns().containsKey("Col1"));

    vm = new HBaseValueMeta("Family2" + HBaseValueMeta.SEPARATOR + "Col1"
        + HBaseValueMeta.SEPARATOR + "Col1", ValueMetaInterface.TYPE_STRING,
        -1, -1);

    aMapping.addMappedColumn(vm, false);

    assertTrue(aMapping.getMappedColumns().containsKey("Col1_1"));
  }
}
