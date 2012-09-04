package org.pentaho.hbase;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.pentaho.hbase.shim.api.HBaseValueMeta;
import org.pentaho.hbase.shim.api.Mapping;
import org.pentaho.hbase.shim.common.CommonHBaseBytesUtil;
import org.pentaho.hbase.shim.fake.FakeHBaseConnection;

public class HBaseValueMetaTest {

  @Test
  public void testDecodeKeyValueBinary() throws Exception {
    CommonHBaseBytesUtil bu = new CommonHBaseBytesUtil();

    byte[] testVal = bu.toBytes("My test value");
    Mapping dummy = new Mapping("DummyTable", "DummyMapping", "MyKey",
        Mapping.KeyType.BINARY);
    Object result = HBaseValueMeta.decodeKeyValue(testVal, dummy, bu);

    assertTrue(result instanceof byte[]);
    assertEquals(((byte[]) result).length, testVal.length);

    FakeHBaseConnection.BytesComparator comp = new FakeHBaseConnection.BytesComparator();
    assertEquals(comp.compare(testVal, (byte[]) result), 0);
  }

  @Test
  public void testDecodeKeyValueString() throws Exception {
    CommonHBaseBytesUtil bu = new CommonHBaseBytesUtil();

    byte[] testVal = bu.toBytes("My test value");
    Mapping dummy = new Mapping("DummyTable", "DummyMapping", "MyKey",
        Mapping.KeyType.STRING);
    Object result = HBaseValueMeta.decodeKeyValue(testVal, dummy, bu);

    assertTrue(result instanceof String);
    assertEquals(result.toString(), "Myt test value");
  }

  @Test
  public void testDecodeKeyValueUnsignedLong() throws Exception {
    CommonHBaseBytesUtil bu = new CommonHBaseBytesUtil();

    byte[] testVal = bu.toBytes(new Long(42));
    Mapping dummy = new Mapping("DummyTable", "DummyMapping", "MyKey",
        Mapping.KeyType.UNSIGNED_LONG);
    Object result = HBaseValueMeta.decodeKeyValue(testVal, dummy, bu);

    assertTrue(result instanceof Long);
    assertEquals(((Long) result).longValue(), 42L);
  }
}
