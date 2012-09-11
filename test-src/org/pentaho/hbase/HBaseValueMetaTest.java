package org.pentaho.hbase;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.math.BigDecimal;
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
    ValueMetaInterface valMeta = new ValueMeta("TestInt",
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
    ValueMetaInterface valMeta = new ValueMeta("TestDouble",
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
    ValueMetaInterface valMeta = new ValueMeta("TestFloat",
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

  @Test
  public void testEncodeColumnValueDate() throws Exception {
    Date value = new Date();
    ValueMetaInterface valMeta = new ValueMeta("TestDate",
        ValueMetaInterface.TYPE_DATE);

    HBaseValueMeta mappingMeta = new HBaseValueMeta("famliy1"
        + HBaseValueMeta.SEPARATOR + "testcol" + HBaseValueMeta.SEPARATOR
        + "anAlias", ValueMetaInterface.TYPE_DATE, -1, -1);

    CommonHBaseBytesUtil bu = new CommonHBaseBytesUtil();
    byte[] encoded = HBaseValueMeta.encodeColumnValue(value, valMeta,
        mappingMeta, bu);

    assertTrue(encoded != null);
    assertEquals(encoded.length, bu.getSizeOfLong());
    assertEquals(bu.toLong(encoded), value.getTime());
  }

  @Test
  public void testEncodeColumnValueBoolean() throws Exception {
    Boolean value = new Boolean(true);
    ValueMetaInterface valMeta = new ValueMeta("TestBool",
        ValueMetaInterface.TYPE_BOOLEAN);

    HBaseValueMeta mappingMeta = new HBaseValueMeta("famliy1"
        + HBaseValueMeta.SEPARATOR + "testcol" + HBaseValueMeta.SEPARATOR
        + "anAlias", ValueMetaInterface.TYPE_BOOLEAN, -1, -1);

    CommonHBaseBytesUtil bu = new CommonHBaseBytesUtil();
    byte[] encoded = HBaseValueMeta.encodeColumnValue(value, valMeta,
        mappingMeta, bu);

    assertTrue(encoded != null);
    assertEquals(encoded.length, 1);
    assertEquals(bu.toString(encoded), "Y");
  }

  @Test
  public void testEncodeColumnValueBigNumber() throws Exception {
    BigDecimal value = new BigDecimal(42);
    ValueMetaInterface valMeta = new ValueMeta("TestBigNum",
        ValueMetaInterface.TYPE_BIGNUMBER);

    HBaseValueMeta mappingMeta = new HBaseValueMeta("famliy1"
        + HBaseValueMeta.SEPARATOR + "testcol" + HBaseValueMeta.SEPARATOR
        + "anAlias", ValueMetaInterface.TYPE_BIGNUMBER, -1, -1);

    CommonHBaseBytesUtil bu = new CommonHBaseBytesUtil();
    byte[] encoded = HBaseValueMeta.encodeColumnValue(value, valMeta,
        mappingMeta, bu);

    assertTrue(encoded != null);
    assertEquals(bu.toString(encoded), value.toString());
  }

  @Test
  public void testEncodeColumnValueSerializable() throws Exception {
    String value = "Hi there bob!";
    ValueMetaInterface valMeta = new ValueMeta("TestSerializable",
        ValueMetaInterface.TYPE_STRING);

    HBaseValueMeta mappingMeta = new HBaseValueMeta("famliy1"
        + HBaseValueMeta.SEPARATOR + "testcol" + HBaseValueMeta.SEPARATOR
        + "anAlias", ValueMetaInterface.TYPE_SERIALIZABLE, -1, -1);

    CommonHBaseBytesUtil bu = new CommonHBaseBytesUtil();
    byte[] encoded = HBaseValueMeta.encodeColumnValue(value, valMeta,
        mappingMeta, bu);

    byte[] reference = HBaseValueMeta.encodeObject(value);

    assertTrue(encoded != null);
    assertEquals(encoded.length, reference.length);
    assertEquals(HBaseValueMeta.decodeObject(encoded), value);
  }

  @Test
  public void testEncodeColumnValueBinary() throws Exception {
    byte[] value = new String("Hi there bob!").getBytes();
    ValueMetaInterface valMeta = new ValueMeta("TestBinary",
        ValueMetaInterface.TYPE_BINARY);

    HBaseValueMeta mappingMeta = new HBaseValueMeta("famliy1"
        + HBaseValueMeta.SEPARATOR + "testcol" + HBaseValueMeta.SEPARATOR
        + "anAlias", ValueMetaInterface.TYPE_BINARY, -1, -1);

    CommonHBaseBytesUtil bu = new CommonHBaseBytesUtil();
    byte[] encoded = HBaseValueMeta.encodeColumnValue(value, valMeta,
        mappingMeta, bu);

    assertTrue(encoded != null);
    assertEquals(encoded.length, value.length);
    for (int i = 0; i < encoded.length; i++) {
      assertEquals(encoded[i], value[i]);
    }
  }

  @Test
  public void testDecodeColumnValueString() throws Exception {
    String value = "Hi there bob";
    CommonHBaseBytesUtil bu = new CommonHBaseBytesUtil();
    byte[] encoded = bu.toBytes(value);

    HBaseValueMeta mappingMeta = new HBaseValueMeta("famliy1"
        + HBaseValueMeta.SEPARATOR + "testcol" + HBaseValueMeta.SEPARATOR
        + "anAlias", ValueMetaInterface.TYPE_STRING, -1, -1);

    Object decoded = HBaseValueMeta.decodeColumnValue(encoded, mappingMeta, bu);

    assertTrue(decoded != null);
    assertEquals(decoded, value);
  }

  @Test
  public void testDecodeColumnValueStringIndexedStorage() throws Exception {
    String value = "Value2";
    CommonHBaseBytesUtil bu = new CommonHBaseBytesUtil();
    byte[] encoded = bu.toBytes(value);

    HBaseValueMeta mappingMeta = new HBaseValueMeta("famliy1"
        + HBaseValueMeta.SEPARATOR + "testcol" + HBaseValueMeta.SEPARATOR
        + "anAlias", ValueMetaInterface.TYPE_STRING, -1, -1);
    mappingMeta.setStorageType(ValueMetaInterface.STORAGE_TYPE_INDEXED);
    Object[] legalVals = new Object[] { "Value1", "Value2", "Value3" };
    mappingMeta.setIndex(legalVals);

    Object decoded = HBaseValueMeta.decodeColumnValue(encoded, mappingMeta, bu);

    assertTrue(decoded != null);
    assertEquals(decoded, new Integer(1));
  }

  @Test
  public void testDecodeColumnValueStringIndexedStorageIllegalValue()
      throws Exception {
    String value = "Bogus";
    CommonHBaseBytesUtil bu = new CommonHBaseBytesUtil();
    byte[] encoded = bu.toBytes(value);

    HBaseValueMeta mappingMeta = new HBaseValueMeta("famliy1"
        + HBaseValueMeta.SEPARATOR + "testcol" + HBaseValueMeta.SEPARATOR
        + "anAlias", ValueMetaInterface.TYPE_STRING, -1, -1);
    mappingMeta.setStorageType(ValueMetaInterface.STORAGE_TYPE_INDEXED);
    Object[] legalVals = new Object[] { "Value1", "Value2", "Value3" };
    mappingMeta.setIndex(legalVals);

    try {
      Object decoded = HBaseValueMeta.decodeColumnValue(encoded, mappingMeta,
          bu);
      fail("Was expecting an exception because the supplied value is not in "
          + "the list of indexed values");
    } catch (Exception ex) {
    }
  }

  @Test
  public void testDecodeColumnValueFloat() throws Exception {
    float value = 42f;
    CommonHBaseBytesUtil bu = new CommonHBaseBytesUtil();
    byte[] encoded = bu.toBytes(value);

    HBaseValueMeta mappingMeta = new HBaseValueMeta("famliy1"
        + HBaseValueMeta.SEPARATOR + "testcol" + HBaseValueMeta.SEPARATOR
        + "anAlias", ValueMetaInterface.TYPE_NUMBER, -1, -1);

    Object decoded = HBaseValueMeta.decodeColumnValue(encoded, mappingMeta, bu);
    assertTrue(decoded != null);
    assertTrue(decoded instanceof Double);
    assertEquals(decoded, new Double(42));
  }

  @Test
  public void testDecodeColumnValueDouble() throws Exception {
    double value = 42;
    CommonHBaseBytesUtil bu = new CommonHBaseBytesUtil();
    byte[] encoded = bu.toBytes(value);

    HBaseValueMeta mappingMeta = new HBaseValueMeta("famliy1"
        + HBaseValueMeta.SEPARATOR + "testcol" + HBaseValueMeta.SEPARATOR
        + "anAlias", ValueMetaInterface.TYPE_NUMBER, -1, -1);

    Object decoded = HBaseValueMeta.decodeColumnValue(encoded, mappingMeta, bu);
    assertTrue(decoded != null);
    assertTrue(decoded instanceof Double);
    assertEquals(decoded, new Double(42));
  }

  @Test
  public void testDecodeColumnValueInteger() throws Exception {
    int value = 42;
    CommonHBaseBytesUtil bu = new CommonHBaseBytesUtil();
    byte[] encoded = bu.toBytes(value);

    HBaseValueMeta mappingMeta = new HBaseValueMeta("famliy1"
        + HBaseValueMeta.SEPARATOR + "testcol" + HBaseValueMeta.SEPARATOR
        + "anAlias", ValueMetaInterface.TYPE_INTEGER, -1, -1);

    Object decoded = HBaseValueMeta.decodeColumnValue(encoded, mappingMeta, bu);
    assertTrue(decoded != null);
    assertTrue(decoded instanceof Long);
    assertEquals(decoded, new Long(42));
  }

  @Test
  public void testDecodeColumnValueLong() throws Exception {
    long value = 42;
    CommonHBaseBytesUtil bu = new CommonHBaseBytesUtil();
    byte[] encoded = bu.toBytes(value);

    HBaseValueMeta mappingMeta = new HBaseValueMeta("famliy1"
        + HBaseValueMeta.SEPARATOR + "testcol" + HBaseValueMeta.SEPARATOR
        + "anAlias", ValueMetaInterface.TYPE_INTEGER, -1, -1);

    Object decoded = HBaseValueMeta.decodeColumnValue(encoded, mappingMeta, bu);
    assertTrue(decoded != null);
    assertTrue(decoded instanceof Long);
    assertEquals(decoded, new Long(42));
  }

  @Test
  public void testDecodeColumnValueBooleanAsString() throws Exception {
    String value = "TRUE";
    CommonHBaseBytesUtil bu = new CommonHBaseBytesUtil();
    byte[] encoded = bu.toBytes(value);

    HBaseValueMeta mappingMeta = new HBaseValueMeta("famliy1"
        + HBaseValueMeta.SEPARATOR + "testcol" + HBaseValueMeta.SEPARATOR
        + "anAlias", ValueMetaInterface.TYPE_BOOLEAN, -1, -1);

    Object decoded = HBaseValueMeta.decodeColumnValue(encoded, mappingMeta, bu);
    assertTrue(decoded != null);
    assertTrue(decoded instanceof Boolean);
    assertEquals(decoded, new Boolean(true));
  }

  @Test
  public void testDecodeColumnValueBooleanAsInteger() throws Exception {
    Integer value = 0;
    CommonHBaseBytesUtil bu = new CommonHBaseBytesUtil();
    byte[] encoded = bu.toBytes(value);

    HBaseValueMeta mappingMeta = new HBaseValueMeta("famliy1"
        + HBaseValueMeta.SEPARATOR + "testcol" + HBaseValueMeta.SEPARATOR
        + "anAlias", ValueMetaInterface.TYPE_BOOLEAN, -1, -1);

    Object decoded = HBaseValueMeta.decodeColumnValue(encoded, mappingMeta, bu);
    assertTrue(decoded != null);
    assertTrue(decoded instanceof Boolean);
    assertEquals(decoded, new Boolean(false));
  }

  @Test
  public void testDecodeColumnValueBigDecimal() throws Exception {
    BigDecimal bd = new BigDecimal(42);
    String value = bd.toString();

    CommonHBaseBytesUtil bu = new CommonHBaseBytesUtil();
    byte[] encoded = bu.toBytes(value);

    HBaseValueMeta mappingMeta = new HBaseValueMeta("famliy1"
        + HBaseValueMeta.SEPARATOR + "testcol" + HBaseValueMeta.SEPARATOR
        + "anAlias", ValueMetaInterface.TYPE_BIGNUMBER, -1, -1);

    Object decoded = HBaseValueMeta.decodeColumnValue(encoded, mappingMeta, bu);
    assertTrue(decoded != null);
    assertTrue(decoded instanceof BigDecimal);
    assertEquals(decoded, bd);
  }

  @Test
  public void testDecodeColumnValueSerializable() throws Exception {
    byte[] value = HBaseValueMeta.encodeObject(new String("Hi there bob!"));
    CommonHBaseBytesUtil bu = new CommonHBaseBytesUtil();

    HBaseValueMeta mappingMeta = new HBaseValueMeta("famliy1"
        + HBaseValueMeta.SEPARATOR + "testcol" + HBaseValueMeta.SEPARATOR
        + "anAlias", ValueMetaInterface.TYPE_SERIALIZABLE, -1, -1);

    Object decoded = HBaseValueMeta.decodeColumnValue(value, mappingMeta, bu);

    assertTrue(decoded != null);
    assertTrue(decoded instanceof String);
    assertEquals(decoded, "Hi there bob!");
  }

  @Test
  public void testDecodeColumnValueDate() throws Exception {
    Date value = new Date();
    CommonHBaseBytesUtil bu = new CommonHBaseBytesUtil();
    byte[] encoded = bu.toBytes(value.getTime());

    HBaseValueMeta mappingMeta = new HBaseValueMeta("famliy1"
        + HBaseValueMeta.SEPARATOR + "testcol" + HBaseValueMeta.SEPARATOR
        + "anAlias", ValueMetaInterface.TYPE_DATE, -1, -1);

    Object decoded = HBaseValueMeta.decodeColumnValue(encoded, mappingMeta, bu);
    assertTrue(decoded != null);
    assertTrue(decoded instanceof Date);
    assertEquals(decoded, value);
  }

  @Test
  public void testDecodeColumnValueBinary() throws Exception {
    CommonHBaseBytesUtil bu = new CommonHBaseBytesUtil();
    byte[] value = new String("Hi there bob!").getBytes();

    HBaseValueMeta mappingMeta = new HBaseValueMeta("famliy1"
        + HBaseValueMeta.SEPARATOR + "testcol" + HBaseValueMeta.SEPARATOR
        + "anAlias", ValueMetaInterface.TYPE_BINARY, -1, -1);

    Object decoded = HBaseValueMeta.decodeColumnValue(value, mappingMeta, bu);

    assertTrue(decoded != null);
    assertTrue(decoded instanceof byte[]);
    assertEquals(new String((byte[]) decoded), "Hi there bob!");
  }
}
