package org.pentaho.hbase;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Test;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
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
    assertEquals(result.toString(), "My test value");
  }

  @Test
  public void testDecodeKeyValueUnsignedLong() throws Exception {
    CommonHBaseBytesUtil bu = new CommonHBaseBytesUtil();

    byte[] testVal = bu.toBytes(42L);
    Mapping dummy = new Mapping("DummyTable", "DummyMapping", "MyKey",
        Mapping.KeyType.UNSIGNED_LONG);
    Object result = HBaseValueMeta.decodeKeyValue(testVal, dummy, bu);

    assertTrue(result instanceof Long);
    assertEquals(((Long) result).longValue(), 42L);
  }

  @Test
  public void testDecodeKeyValueUnsignedDate() throws Exception {
    CommonHBaseBytesUtil bu = new CommonHBaseBytesUtil();

    long currTime = System.currentTimeMillis();
    byte[] testVal = bu.toBytes(currTime);
    Mapping dummy = new Mapping("DummyTable", "DummyMapping", "MyKey",
        Mapping.KeyType.UNSIGNED_DATE);
    Object result = HBaseValueMeta.decodeKeyValue(testVal, dummy, bu);

    assertTrue(result instanceof Date);
    assertEquals(((Date) result).getTime(), currTime);
  }

  @Test
  public void testDecodeKeyValueUnsignedInteger() throws Exception {
    CommonHBaseBytesUtil bu = new CommonHBaseBytesUtil();

    byte[] testVal = bu.toBytes(42);
    Mapping dummy = new Mapping("DummyTable", "DummyMapping", "MyKey",
        Mapping.KeyType.UNSIGNED_INTEGER);
    Object result = HBaseValueMeta.decodeKeyValue(testVal, dummy, bu);

    assertTrue(result instanceof Long); // returns a long as kettle uses longs
                                        // internally
    assertEquals(((Long) result).longValue(), 42L);
  }

  @Test
  public void testDecodeKeyValueSignedInteger() throws Exception {
    CommonHBaseBytesUtil bu = new CommonHBaseBytesUtil();

    // flip the sign bit so keys sort correctly
    int tempInt = -42;
    tempInt ^= (1 << 31);
    byte[] testVal = bu.toBytes(tempInt);

    Mapping dummy = new Mapping("DummyTable", "DummyMapping", "MyKey",
        Mapping.KeyType.INTEGER);
    Object result = HBaseValueMeta.decodeKeyValue(testVal, dummy, bu);

    assertTrue(result instanceof Long); // returns a long as kettle uses longs
                                        // internally
    assertEquals(((Long) result).longValue(), -42L);
  }

  @Test
  public void testDecodeKeyValueSignedLong() throws Exception {
    CommonHBaseBytesUtil bu = new CommonHBaseBytesUtil();

    // flip the sign bit so keys sort correctly
    long tempVal = -42L;
    tempVal ^= (1L << 63);
    byte[] testVal = bu.toBytes(tempVal);

    Mapping dummy = new Mapping("DummyTable", "DummyMapping", "MyKey",
        Mapping.KeyType.LONG);
    Object result = HBaseValueMeta.decodeKeyValue(testVal, dummy, bu);

    assertTrue(result instanceof Long);
    assertEquals(((Long) result).longValue(), -42L);
  }

  @Test
  public void testDecodeKeyValueSignedDate() throws Exception {
    CommonHBaseBytesUtil bu = new CommonHBaseBytesUtil();

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    Date aDate = sdf.parse("1969-08-28");
    long negTime = aDate.getTime();
    long negTimeOrig = negTime;
    negTime ^= (1L << 63);

    byte[] testVal = bu.toBytes(negTime);
    Mapping dummy = new Mapping("DummyTable", "DummyMapping", "MyKey",
        Mapping.KeyType.DATE);
    Object result = HBaseValueMeta.decodeKeyValue(testVal, dummy, bu);

    assertTrue(result instanceof Date);
    assertEquals(((Date) result).getTime(), negTimeOrig);
  }

  @Test
  public void testEncodeKeyValueBinary() throws Exception {
    CommonHBaseBytesUtil bu = new CommonHBaseBytesUtil();
    byte[] testVal = bu.toBytes("Blah");

    byte[] result = HBaseValueMeta.encodeKeyValue(testVal,
        Mapping.KeyType.BINARY, bu);

    assertTrue(result != null);
    assertEquals(result.length, testVal.length);
    FakeHBaseConnection.BytesComparator comp = new FakeHBaseConnection.BytesComparator();

    // encoding a binary value to binary should just return the same value
    assertEquals(comp.compare(testVal, result), 0);
  }

  @Test
  public void testEncodeKeyValueString() throws Exception {
    CommonHBaseBytesUtil bu = new CommonHBaseBytesUtil();
    String testVal = "Blah";

    byte[] result = HBaseValueMeta.encodeKeyValue(testVal,
        Mapping.KeyType.STRING, bu);
    assertTrue(result != null);
    assertEquals(bu.toString(result), testVal);
  }

  @Test
  public void testEncodeKeyValueUnsignedLong() throws Exception {
    CommonHBaseBytesUtil bu = new CommonHBaseBytesUtil();
    Long testVal = new Long(42L);

    byte[] result = HBaseValueMeta.encodeKeyValue(testVal,
        Mapping.KeyType.UNSIGNED_LONG, bu);
    assertTrue(result != null);
    assertEquals(bu.toLong(result), 42L);
  }

  @Test
  public void testEncodeKeyValueUnsignedDate() throws Exception {
    CommonHBaseBytesUtil bu = new CommonHBaseBytesUtil();
    long time = System.currentTimeMillis();
    Date testVal = new Date(time);

    byte[] result = HBaseValueMeta.encodeKeyValue(testVal,
        Mapping.KeyType.UNSIGNED_DATE, bu);
    assertTrue(result != null);
    assertEquals(bu.toLong(result), time);
  }

  @Test
  public void testEncodeKeyValueUnsignedInteger() throws Exception {
    CommonHBaseBytesUtil bu = new CommonHBaseBytesUtil();
    Integer testVal = new Integer(42);

    byte[] result = HBaseValueMeta.encodeKeyValue(testVal,
        Mapping.KeyType.UNSIGNED_INTEGER, bu);
    assertTrue(result != null);
    assertEquals(bu.toInt(result), 42);
  }

  @Test
  public void testEncodeKeyValueSignedInteger() throws Exception {
    CommonHBaseBytesUtil bu = new CommonHBaseBytesUtil();
    Integer testVal = new Integer(-42);

    byte[] result = HBaseValueMeta.encodeKeyValue(testVal,
        Mapping.KeyType.INTEGER, bu);
    assertTrue(result != null);
    int resultInt = bu.toInt(result);
    resultInt ^= (1 << 31);
    assertEquals(resultInt, -42);
  }

  @Test
  public void testEncodeKeyValueSignedLong() throws Exception {
    CommonHBaseBytesUtil bu = new CommonHBaseBytesUtil();
    Long testVal = new Long(-42L);

    byte[] result = HBaseValueMeta.encodeKeyValue(testVal,
        Mapping.KeyType.LONG, bu);
    assertTrue(result != null);

    long longResult = bu.toLong(result);
    longResult ^= (1L << 63);

    assertEquals(longResult, -42L);
  }

  @Test
  public void testEncodeKeyValueSignedDate() throws Exception {
    CommonHBaseBytesUtil bu = new CommonHBaseBytesUtil();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    Date aDate = sdf.parse("1969-08-28");
    long negTime = aDate.getTime();

    byte[] result = HBaseValueMeta.encodeKeyValue(aDate, Mapping.KeyType.DATE,
        bu);

    assertTrue(result != null);
    long timeResult = bu.toLong(result);
    timeResult ^= (1L << 63);

    assertEquals(timeResult, negTime);
  }

  @Test
  public void testEncodeColumnValueString() throws Exception {
    String value = "My test value";
    ValueMetaInterface valMeta = new ValueMeta("TestString",
        ValueMetaInterface.TYPE_STRING);

    HBaseValueMeta mappingMeta = new HBaseValueMeta("famliy1"
        + HBaseValueMeta.SEPARATOR + "testcol" + HBaseValueMeta.SEPARATOR
        + "anAlias", ValueMetaInterface.TYPE_STRING, -1, -1);

    CommonHBaseBytesUtil bu = new CommonHBaseBytesUtil();
    byte[] encoded = HBaseValueMeta.encodeColumnValue(value, valMeta,
        mappingMeta, bu);

    assertTrue(encoded != null);
    assertEquals(bu.toString(encoded), value);
  }

  @Test
  public void testEncodeColumnValueLong() throws Exception {
    Long value = new Long(42);
    ValueMetaInterface valMeta = new ValueMeta("TestLong",
        ValueMetaInterface.TYPE_INTEGER);

    HBaseValueMeta mappingMeta = new HBaseValueMeta("famliy1"
        + HBaseValueMeta.SEPARATOR + "testcol" + HBaseValueMeta.SEPARATOR
        + "anAlias", ValueMetaInterface.TYPE_INTEGER, -1, -1);
    mappingMeta.setIsLongOrDouble(true);
    CommonHBaseBytesUtil bu = new CommonHBaseBytesUtil();
    byte[] encoded = HBaseValueMeta.encodeColumnValue(value, valMeta,
        mappingMeta, bu);

    assertTrue(encoded != null);
    assertEquals(encoded.length, bu.getSizeOfLong());
    assertEquals(bu.toLong(encoded), value.longValue());
  }

  @Test
  public void testEncodeColumnValueInteger() throws Exception {
    Long value = new Long(42);
    ValueMetaInterface valMeta = new ValueMeta("TestLong",
        ValueMetaInterface.TYPE_INTEGER);

    HBaseValueMeta mappingMeta = new HBaseValueMeta("famliy1"
        + HBaseValueMeta.SEPARATOR + "testcol" + HBaseValueMeta.SEPARATOR
        + "anAlias", ValueMetaInterface.TYPE_INTEGER, -1, -1);
    mappingMeta.setIsLongOrDouble(false);
    CommonHBaseBytesUtil bu = new CommonHBaseBytesUtil();
    byte[] encoded = HBaseValueMeta.encodeColumnValue(value, valMeta,
        mappingMeta, bu);

    assertTrue(encoded != null);
    assertEquals(encoded.length, bu.getSizeOfInt());
    assertEquals(bu.toInt(encoded), value.intValue());
  }

  @Test
  public void testEncodeColumnValueDouble() throws Exception {
    Double value = new Double(42.0);
    ValueMetaInterface valMeta = new ValueMeta("TestLong",
        ValueMetaInterface.TYPE_NUMBER);

    HBaseValueMeta mappingMeta = new HBaseValueMeta("famliy1"
        + HBaseValueMeta.SEPARATOR + "testcol" + HBaseValueMeta.SEPARATOR
        + "anAlias", ValueMetaInterface.TYPE_NUMBER, -1, -1);
    mappingMeta.setIsLongOrDouble(true);
    CommonHBaseBytesUtil bu = new CommonHBaseBytesUtil();
    byte[] encoded = HBaseValueMeta.encodeColumnValue(value, valMeta,
        mappingMeta, bu);

    assertTrue(encoded != null);
    assertEquals(encoded.length, bu.getSizeOfDouble());
    assertEquals(bu.toDouble(encoded), value.doubleValue(), 0.0001);
  }

  @Test
  public void testEncodeColumnValueFloat() throws Exception {
    Double value = new Double(42.0);
    ValueMetaInterface valMeta = new ValueMeta("TestLong",
        ValueMetaInterface.TYPE_NUMBER);

    HBaseValueMeta mappingMeta = new HBaseValueMeta("famliy1"
        + HBaseValueMeta.SEPARATOR + "testcol" + HBaseValueMeta.SEPARATOR
        + "anAlias", ValueMetaInterface.TYPE_NUMBER, -1, -1);
    mappingMeta.setIsLongOrDouble(false);
    CommonHBaseBytesUtil bu = new CommonHBaseBytesUtil();
    byte[] encoded = HBaseValueMeta.encodeColumnValue(value, valMeta,
        mappingMeta, bu);

    assertTrue(encoded != null);
    assertEquals(encoded.length, bu.getSizeOfFloat());
    assertEquals(bu.toFloat(encoded), value.floatValue(), 0.0001f);
  }

  public static void main(String[] args) {
    try {
      HBaseValueMetaTest test = new HBaseValueMetaTest();
      test.testDecodeKeyValueSignedDate();
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }
}
