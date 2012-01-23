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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.util.Date;

import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.util.Bytes;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;

/**
 * Class that extends ValueMeta to add a few fields specific to HBase
 * and a constructor that allows a fully qualified name 
 * (i.e column_family,column_name).
 * 
 * Note that for ordinary (non-key) columns in HBase, dates are assumed
 * to be just raw long values. Filtering on dates involves using a WritableByteArrayComparable
 * comparator in order to first deserialize the long before performing any comparison. 
 * This differs from the key of the table where we allow either raw longs or longs where 
 * the sign bit has been flipped (so that the natural sort order is preserved by HBases's 
 * byte-wise comparison). 
 * 
 * @author Mark Hall (mhall{[at]}pentaho{[dot]}com)
 * @version $Revision$
 *
 */
public class HBaseValueMeta extends ValueMeta {
  public static final String SEPARATOR = ",";

  /** The table name that this particular column mapping is for */
  protected String m_tableName;

  /** The mapping name that this particular column mapping belongs to */
  protected String m_mappingName;

  protected String m_columnFamily;
  protected String m_columnName;
  
  protected boolean m_isKey;
  
  /** 
   * In HBase, for filtering on unsigned columns, we need to know if a number
   * is double/long or float/int in order to convert the comparison constant
   * to the right number of bytes for a lexical comparison to work properly
   */
  protected boolean m_isLongOrDouble = true;

  public HBaseValueMeta(String name, int type, int length, int precision) 
  throws IllegalArgumentException {

    super(name, type, length, precision);

    // format - colFamily,colName,alias
    String[] parts = name.split(SEPARATOR);

    if (parts.length < 2) {
      throw new IllegalArgumentException("A HBaseValueMeta must have at least a " +
      "column family name and a column name");
    }

    if (parts.length > 3) {
      throw new IllegalArgumentException("Problem parsing HBaseValueMeta column "
          + "description. Can't have more than three parts!");
    }

    setColumnFamily(parts[0]);
    setColumnName(parts[1]);
    if (parts.length == 2) {

      // alias needs to be unique, so should be family+col in this case
      setAlias(name);
    } else {
      setAlias(parts[2]);
    }
  }    

  /**
   * Set the name of the table that this field belongs to
   * 
   * @param tableName the name of the table
   */
  public void setTableName(String tableName) {
    m_tableName = tableName;
  }

  /**
   * Get the name of the table that this field belongs to.
   * 
   * @return the name of the table that this field belongs to.
   */
  public String getTableName() {
    return m_tableName;
  }

  /**
   * Set the name of the mapping that this field has been defined in
   * 
   * @param mappingName the name of the mapping that defines this field
   */
  public void setMappingName(String mappingName) {
    m_mappingName = mappingName;
  }

  /**
   * Get the name of the mapping that this field is defined in
   * 
   * @return the name of the mapping that this field is defined in
   */
  public String getMappingName() {
    return m_mappingName;
  }

  /**
   * Set the column family that this field comes from
   * 
   * @param family the name of the column family that this field comes from
   */
  public void setColumnFamily(String family) {
    m_columnFamily = family;
  }

  /**
   * Get the name of the column family that this field comes from
   * 
   * @return the name of the column family that this field comes from
   */
  public String getColumnFamily() {
    return m_columnFamily;
  }

  /**
   * Set the alias to use for this field
   * 
   * @param alias the alias to use
   */
  public void setAlias(String alias) {
    setName(alias);
  }

  /**
   * Get the alias that this field goes by
   * 
   * @return the alias that this field goes by
   */
  public String getAlias() {
    return getName();
  }

  /**
   * Set the column name for this field
   * 
   * @param columnName the column name for this field
   */
  public void setColumnName(String columnName) {
    m_columnName = columnName;
  }

  /**
   * Get the column name for this field
   * 
   * @return the column name for this field
   */
  public String getColumnName() {
    return m_columnName;
  }
  
  /**
   * Set the type for this field from a string
   * 
   * @param hbaseType the type for this field as a string
   * @throws IllegalArgumentException if the type is unknown
   */
  public void setHBaseTypeFromString(String hbaseType) throws IllegalArgumentException {
    if (hbaseType.equalsIgnoreCase("Integer")) {
      setType(ValueMeta.getType(hbaseType));
      setIsLongOrDouble(false);
      return;
    }
    if (hbaseType.equalsIgnoreCase("Long")) {
      setType(ValueMeta.getType("Integer"));
      setIsLongOrDouble(true);
      return;
    }
    if (hbaseType.equals("Float")) {
      setType(ValueMeta.getType("Number"));
      setIsLongOrDouble(false);
      return;
    }
    if (hbaseType.equals("Double")) {
      setType(ValueMeta.getType("Number"));
      setIsLongOrDouble(true);
      return;
    }
    
    // default
    int type = ValueMeta.getType(hbaseType);
    if (type == ValueMetaInterface.TYPE_NONE) {
      throw new IllegalArgumentException("Unknown type \"" + hbaseType + "\"");
    }

    setType(type);
  }
  
  /**
   * Get the type of this field as a string
   * 
   * @return the type of this field as a string
   */
  public String getHBaseTypeDesc() {
    if (isInteger()) {
      return (getIsLongOrDouble() ? "Long" : "Integer");
    }
    if (isNumber()) {
      return (getIsLongOrDouble() ? "Double" : "Float");
    }
    
    return ValueMeta.getTypeDesc(getType());
  }
  
  /**
   * Set whether this field is a long integer or a double precision floating point
   * number
   * 
   * @param ld true if this field is either a long integer or a double
   * precision floating point number
   */
  public void setIsLongOrDouble(boolean ld) {
    m_isLongOrDouble = ld;
  }
  
  /**
   * Returns true if this field is either a long integer or double precision floating
   * point number
   * 
   * @return true if this field is a long integer or double precision floating point
   * number
   */
  public boolean getIsLongOrDouble() {
    return m_isLongOrDouble;
  }
  
  /**
   * Set whether this field is the key for the mapped table or not
   * 
   * @param key true if this field is the key
   */
  public void setKey(boolean key) {
    m_isKey = key;
  }
  
  /**
   * Get whether this field is the key for the mapped table or not
   * 
   * @return true if this field is the key
   */
  public boolean isKey() {
    return m_isKey;
  }
  
  /**
   * Encode a keyValue (with associated meta data) to an array of bytes with respect
   * to the key type specified in a mapping.
   * 
   * @param keyValue the key value (object) to encode
   * @param keyMeta meta data about the key value
   * @param keyType the target type of the encoded key value
   * @return the key encoded as an array of bytes
   * @throws KettleException if something goes wrong
   */
  public static byte[] encodeKeyValue(Object keyValue, ValueMetaInterface keyMeta, 
      Mapping.KeyType keyType) throws KettleException {
    
    byte[] result = null;
    
    switch (keyType) {
    case STRING:
      String stringKey = keyMeta.getString(keyValue);
      result = encodeKeyValue(stringKey, keyType);
      break;
    case DATE:
    case UNSIGNED_DATE:
      Date dateKey = keyMeta.getDate(keyValue);
      if (keyType == Mapping.KeyType.UNSIGNED_DATE && dateKey.getTime() < 0) {
        throw new KettleException("Key for mapping is UNSIGNED_DATE, but incoming " +
        		"date value is negative (i.e. < 1st Jan 1970)");
      }
      result = encodeKeyValue(dateKey, keyType);
      break;
    case INTEGER:
    case UNSIGNED_INTEGER:
      int keyInt = keyMeta.getInteger(keyValue).intValue();
      if (keyType == Mapping.KeyType.UNSIGNED_INTEGER && keyInt < 0) {
        throw new KettleException("Key for mapping is UNSIGNED_INTEGER, but incoming " +
        		"integer value is negative.");
      }
      result = encodeKeyValue(new Integer(keyInt), keyType);
      break;
    case LONG:
    case UNSIGNED_LONG:
      long keyLong = keyMeta.getInteger(keyValue).longValue();
      if (keyType == Mapping.KeyType.UNSIGNED_LONG && keyLong < 0) {
        throw new KettleException("Key for mapping is UNSIGNED_LONG, but incoming " +
        "long value is negative.");
      }
      result = encodeKeyValue(new Long(keyLong), keyType);
      break;      
    }
    
    if (result == null) {
      throw new KettleException("Unknown type for table key!");    
    }
    
    return result;
  }
  
  /**
   * Encode a key value (object) to an array of bytes with respect to the key type
   * specified in a mapping.
   * 
   * @param keyValue the key value (object) to encode
   * @param keyType the type of the key to encode
   * @return the key encoded as an array of bytes
   * @throws KettleException if something goes wrong
   */
  public static byte[] encodeKeyValue(Object keyValue, Mapping.KeyType keyType) 
    throws KettleException {
    
    if (keyType == Mapping.KeyType.STRING) {
      return encodeKeyValue((String)keyValue, keyType);
    }
    
    if (keyType == Mapping.KeyType.UNSIGNED_LONG || 
        keyType == Mapping.KeyType.UNSIGNED_DATE) {
      if (keyValue == null) {
        return Bytes.toBytes(0L); // minimum positive long
      } else {
        long longVal;
        if (keyType == Mapping.KeyType.UNSIGNED_LONG) {
          longVal = ((Number)keyValue).longValue();
        } else {
          longVal = ((Date)keyValue).getTime();
        }
        if (longVal < 0) {
          throw new KettleException("Key for mapping is UNSIGNED_LONG/UNSIGNED_DATE" +
          		", but incoming long/date value is negative.");
        }
        
        return Bytes.toBytes(longVal);
      }
    }
    
    if (keyType == Mapping.KeyType.UNSIGNED_INTEGER) {
      if (keyValue == null) {
        return Bytes.toBytes(0);
      } else {
        if (((Number)keyValue).intValue() < 0) {
          throw new KettleException("Key for mapping is UNSIGNED_INTEGER, but incoming " +
                        "integer value is negative.");
        }
        return Bytes.toBytes(((Number)keyValue).intValue());
      }
    }
    
    if (keyType == Mapping.KeyType.INTEGER) {
      if (keyValue == null) {
        return Bytes.toBytes(0);
      } else {
        int bound = ((Number)keyValue).intValue();
        // to ensure correct sort order we need to flip the sign bit
        bound ^= (1 << 31);
        return Bytes.toBytes(bound);
      }      
    }
    
    if (keyType == Mapping.KeyType.LONG || 
        keyType == Mapping.KeyType.DATE) {
      if (keyValue == null) {
        return Bytes.toBytes(0L);
      } else {
        long bound = (keyType == Mapping.KeyType.DATE) 
          ? ((Date)keyValue).getTime() 
              : ((Number)keyValue).longValue();
          
        // to ensure correct sort order we need to flip the sign bit
        bound ^= (1L << 63);
        return Bytes.toBytes(bound);
      }
    }

    throw new KettleException("Unknown type for table key!");    
  }

  /**
   * Encode a key value (string) to an array of bytes. Useful for 
   * encoding values entered by the user through the UI for the
   * column filter editor
   * 
   * @param keyValue the key value (string) to encode
   * @param keyType the type of the key to encode
   * @return the key encoded as an array of bytes
   * @throws KettleException if something goes wrong
   */
  public static byte[] encodeKeyValue(String keyValue, Mapping.KeyType keyType) 
    throws KettleException {
    // if keyValue is null, we assume that the smallest possible value is wanted

    //Mapping.KeyType keyType = tableMapping.getKeyType();

    if (keyType == Mapping.KeyType.STRING) {
      if (Const.isEmpty(keyValue)) {
        return Bytes.toBytes("");
      } else {
        return Bytes.toBytes(keyValue);
      }
    }

    if (keyType == Mapping.KeyType.UNSIGNED_LONG || 
        keyType == Mapping.KeyType.UNSIGNED_DATE) {
      if (Const.isEmpty(keyValue)) {
        return Bytes.toBytes(0L); // minimum positive long
      } else {
        if (keyType == Mapping.KeyType.UNSIGNED_DATE) {
          throw new KettleException("Can't parse a date from a string if there is " +
          		"no format available");
        }
        
        return Bytes.toBytes(Long.parseLong(keyValue));
      }
    }
    
    if (keyType == Mapping.KeyType.UNSIGNED_INTEGER) {
      if (Const.isEmpty(keyValue)) {
        return Bytes.toBytes(0);
      } else {
        return Bytes.toBytes(Integer.parseInt(keyValue));
      }
    }

    if (keyType == Mapping.KeyType.INTEGER) {
      if (Const.isEmpty(keyValue)) {
        return Bytes.toBytes(0);
      } else {
        int bound = Integer.parseInt(keyValue);
        // to ensure correct sort order we need to flip the sign bit
        bound ^= (1 << 31);
        return Bytes.toBytes(bound);
      }      
    }

    if (keyType == Mapping.KeyType.LONG || 
        keyType == Mapping.KeyType.DATE) {
      if (Const.isEmpty(keyValue)) {
        return Bytes.toBytes(0L);
      } else {
        if (keyType == Mapping.KeyType.DATE) {
          throw new KettleException("Can't parse a date from a string if there is " +
                        "no format available");
        }
        
        long bound = Long.parseLong(keyValue);
        // to ensure correct sort order we need to flip the sign bit
        bound ^= (1L << 63);
        return Bytes.toBytes(bound);
      }
    }

    throw new KettleException("Unknown type for table key!");

  }

  /**
   * Decode a raw key value to the correct Object type
   * 
   * @param rawKey the key as an array of bytes
   * @param tableMapping the Mapping containing type information for the key
   * @return the decoded key value
   * @throws KettleException if something goes wrong
   */
  public static Object decodeKeyValue(byte[] rawKey, Mapping tableMapping) 
  throws KettleException {

    Mapping.KeyType keyType = tableMapping.getKeyType();

    if (rawKey == null) {
      return null;
    }

    if (keyType == Mapping.KeyType.STRING) {
      return Bytes.toString(rawKey);
    }

    if (keyType == Mapping.KeyType.UNSIGNED_LONG ||
        keyType == Mapping.KeyType.UNSIGNED_DATE) {
      if (keyType == Mapping.KeyType.UNSIGNED_DATE) {
        return new Date(Bytes.toLong(rawKey));
      }
      return new Long(Bytes.toLong(rawKey));
    }
    
    if (keyType == Mapping.KeyType.UNSIGNED_INTEGER) {
      return new Long(Bytes.toInt(rawKey));
    }

    if (keyType == Mapping.KeyType.INTEGER) {
      int tempInt = Bytes.toInt(rawKey);
      // flip the sign bit
      tempInt ^= (1 << 31);
      return new Long(tempInt); // Kettle uses longs
    }

    if (keyType == Mapping.KeyType.LONG ||
        keyType == Mapping.KeyType.DATE) {
      long tempLong = Bytes.toLong(rawKey);
      // flip the sign bit
      tempLong ^= (1L << 63);
      
      if (keyType == Mapping.KeyType.DATE) {
        return new Date(tempLong);
      }
      
      return new Long(tempLong);
    }

    throw new KettleException("Unknown type for table key!");
  }
  
  public static byte[] encodeColumnValue(Object columnValue, ValueMetaInterface colMeta, 
      HBaseValueMeta mappingColMeta) throws KettleException {
    
    byte[] encoded = null;
    switch (mappingColMeta.getType()) {
    case TYPE_STRING:
      String toEncode = colMeta.getString(columnValue);
      encoded = Bytes.toBytes(toEncode);
      break;
    case TYPE_INTEGER:
      Long l = colMeta.getInteger(columnValue);
      if (mappingColMeta.getIsLongOrDouble()) {
        encoded = Bytes.toBytes(l.longValue());
      } else {
        encoded = Bytes.toBytes(l.intValue());
      }
      break;
    case TYPE_NUMBER:
      Double d = colMeta.getNumber(columnValue);
      if (mappingColMeta.getIsLongOrDouble()) {
        encoded = Bytes.toBytes(d.doubleValue());
      } else {
        encoded = Bytes.toBytes(d.floatValue());
      }
      break;
    case TYPE_DATE:
      Date date = colMeta.getDate(columnValue);
      encoded = Bytes.toBytes(date.getTime());
      break;      
    case TYPE_BOOLEAN:
      Boolean b = colMeta.getBoolean(columnValue);
      String boolString = (b.booleanValue()) ? "Y" : "N";
      encoded = Bytes.toBytes(boolString);
      break;
    case TYPE_BIGNUMBER:
      BigDecimal bd = colMeta.getBigNumber(columnValue);
      String bds = bd.toString();
      encoded = Bytes.toBytes(bds);
      break;
    case TYPE_SERIALIZABLE:
      try {
        encoded = encodeObject(columnValue);
      } catch (IOException e) {
        throw new KettleException("Unable to serialize serializable type \""
            + colMeta.getName() + "\"", e);
      }
      break;
    case TYPE_BINARY:
      encoded = colMeta.getBinary(columnValue);
      break;      
    }
    
    if (encoded == null) {
      throw new KettleException("Unknown type for column!");
    }
    
    return encoded;
  }

  /**
   * Decode a raw column value
   * 
   * @param columnValue the raw column value to decode
   * @param columnMeta the meta data on the column
   * @return the decoded column value
   * @throws KettleException if something goes wrong
   */
  public static Object decodeColumnValue(KeyValue columnValue, 
      HBaseValueMeta columnMeta) throws KettleException {

//    System.err.println(":::: Column type for " +columnMeta.getAlias() + "  " + columnMeta.getTypeDesc());
    // just return null if this column doesn't have a value for the row
    if (columnValue == null) {
  //    System.err.println("No value for this col.");
      return null;
    }
    

    byte[] rawColValue = columnValue.getValue();

    if (columnMeta.isString()) {
      String convertedString = Bytes.toString(rawColValue);
      if (columnMeta.getStorageType() == ValueMetaInterface.STORAGE_TYPE_INDEXED) {
        // need to return the integer index of this value
        Object[] legalVals = columnMeta.getIndex();
        int foundIndex = -1;
        for (int i = 0; i < legalVals.length; i++) {
          if (legalVals[i].toString().trim().equals(convertedString.trim())) {
            foundIndex = i;
            break;
          }
        }
        if (foundIndex >= 0) {
          return new Integer(foundIndex);
        }
        throw new KettleException("Value \"" + convertedString + "\" is not in the " +
            "list of legal values for indexed column \"" 
            + columnMeta.getAlias() + "\"");
      } else {
        return convertedString;
      }
    }

    if (columnMeta.isNumber()) {
      if (rawColValue.length == Bytes.SIZEOF_FLOAT) {
        float floatResult = Bytes.toFloat(rawColValue);
        return new Double(floatResult);
      }

      if (rawColValue.length == Bytes.SIZEOF_DOUBLE) {
        return new Double(Bytes.toDouble(rawColValue));
      }
    }

    if (columnMeta.isInteger()) {
      if (rawColValue.length == Bytes.SIZEOF_INT) {
        int intResult = Bytes.toInt(rawColValue);
        return new Long(intResult);
      }

      if (rawColValue.length == Bytes.SIZEOF_LONG) {
        return new Long(Bytes.toLong(rawColValue));
      }
      if (rawColValue.length == Bytes.SIZEOF_SHORT) {
        // be lenient on reading from HBase - accept and convert shorts
        // even though our mapping defines only longs and integers
        // TODO add short to the types that can be mapped?
        short tempShort = Bytes.toShort(rawColValue);
        return new Long(tempShort);
      }

      throw new KettleException("Length of integer column value " +
          "is not equal to the " +
      "defined length of an short, int or long!");
    }

    if (columnMeta.isBoolean()) {
      // try as a string first
      Boolean result = decodeBoolFromString(rawColValue);
      if (result == null) {
        // try as a number
        result = decodeBoolFromNumber(rawColValue);
      }
      
      if (result != null) {
        return result;
      }

      throw new KettleException("Unable to decode boolean value!");
    }

    if (columnMeta.isBigNumber()) {
      BigDecimal result = decodeBigDecimal(rawColValue);

      if (result == null) {
        throw new KettleException("Unable to decode a BigDecimal from " +
        "either a String or a serialized Java object.");
      }

      return result;
    }

    if (columnMeta.isSerializableType()) {
      Object result = decodeObject(rawColValue);

      if (result == null) {
        throw new KettleException("Unable to de-serialize Object from raw " +
        "column value.");
      }
      
//      System.out.println(":::::::::::: Deserialized " + result.toString());
      
      return result;
    }

    if (columnMeta.isBinary()) {
      // just return the raw array of bytes
      return rawColValue;
    }

    if (columnMeta.isDate()) {
      if (rawColValue.length != Bytes.SIZEOF_LONG) {
        throw new KettleException("Length of date column value must equal " +
        "to the length of a long!");
      }
      long millis = Bytes.toLong(rawColValue);
      Date d = new Date(millis);
      return d;
    }



    throw new KettleException("Unsupported column type!");
  }

  /**
   * Decode/deserialize an object from an array of bytes
   * 
   * @param rawEncoded the raw encoded form
   * @return the deserialized object
   */
  public static Object decodeObject(byte[] rawEncoded) {
    try {
      ByteArrayInputStream bis = new ByteArrayInputStream(rawEncoded);
      BufferedInputStream buf = new BufferedInputStream(bis);
      ObjectInputStream ois = new ObjectInputStream(buf);
      Object result = ois.readObject();

      return result;
    } catch (Exception ex) {
      ex.printStackTrace();
    }

    return null;
  }

  /**
   * Decode/deserialize a big decimal. Tries the raw value as a string first. If
   * this fails then it tries to decode the big decimal as a serialized object.
   * 
   * @param rawEncoded the encoded big decimal as an array of bytes
   * @return the big decimal as a BigDecimal object
   */
  public static BigDecimal decodeBigDecimal(byte[] rawEncoded) {
    
    // try string first
    String tempString = Bytes.toString(rawEncoded);
    try {
      BigDecimal result = new BigDecimal(tempString);
      return result;
    } catch (NumberFormatException e) { }

    // now try as a serialized java object
    Object obj = decodeObject(rawEncoded);
    if (obj != null) {
      try {
        BigDecimal result = (BigDecimal)obj;
        return result;
      } catch (Exception e) {}
    }

    // unable to deserialize from either serialized object or String 
    return null;
  }

  /**
   * Encodes and object via serialization
   * 
   * @param obj the object to encode
   * @return an array of bytes containing the serialized object
   * @throws IOException if serialization fails
   */
  public static byte[] encodeObject(Object obj) throws IOException {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    BufferedOutputStream buf = new BufferedOutputStream(bos);
    ObjectOutputStream oos = new ObjectOutputStream(buf);
    oos.writeObject(obj);
    buf.flush();

    return bos.toByteArray();
  }

  /**
   * Encodes a big decimal by serialization
   * 
   * @param decimal the big decimal to encode
   * @return an array of bytes containing the serialized big decimal
   * @throws IOException if serialization fails
   */
  public static byte[] encodeBigDecimal(BigDecimal decimal) throws IOException {
    // serialize it as an object
    return encodeObject(decimal);
  }

  /**
   * Decodes a boolean value from an array of bytes that is assumed to hold a
   * string.
   * 
   * @param rawEncoded an array of bytes holding the string representation of
   * a boolean value
   * @return a Boolean object or null if it can't be decoded from the supplied
   * array of bytes.
   */
  public static Boolean decodeBoolFromString(byte[] rawEncoded) {
    
    // delegate to the special comparator for this. Delegate this way
    // rather than have the comparator delegate to us so that HBase
    // installations don't have to drag in the Kettle libraries just
    // so that our comparator can be used.
    return DeserializedBooleanComparator.decodeBoolFromString(rawEncoded);
    
/*    String tempString = Bytes.toString(rawEncoded);
    if (tempString.equalsIgnoreCase("Y") || tempString.equalsIgnoreCase("N") ||
        tempString.equalsIgnoreCase("YES") || tempString.equalsIgnoreCase("NO") ||
        tempString.equalsIgnoreCase("TRUE") || tempString.equalsIgnoreCase("FALSE") ||
        tempString.equalsIgnoreCase("T") || tempString.equalsIgnoreCase("F") ||
        tempString.equalsIgnoreCase("1") || tempString.equalsIgnoreCase("0")) {

      return Boolean.valueOf(tempString.equalsIgnoreCase("Y") || 
          tempString.equalsIgnoreCase("YES") ||
          tempString.equalsIgnoreCase("TRUE") ||
          tempString.equalsIgnoreCase("T") ||
          tempString.equalsIgnoreCase("1"));
    }

    // not identifiable from a string
    return null; */
  }  

  public static Boolean decodeBoolFromNumber(byte[] rawEncoded) {
    
    // delegate to the special comparator for this. Delegate this way
    // rather than have the comparator delegate to us so that HBase
    // installations don't have to drag in the Kettle libraries just
    // so that our comparator can be used.
    return DeserializedBooleanComparator.decodeBoolFromNumber(rawEncoded);
    
/*    if (rawEncoded.length == Bytes.SIZEOF_BYTE) {
      byte val = rawEncoded[0];
      if (val == 0 || val == 1) {
        return new Boolean(val == 1);
      }
    }

    if (rawEncoded.length == Bytes.SIZEOF_SHORT) {
      short tempShort = Bytes.toShort(rawEncoded);

      if (tempShort == 0 || tempShort == 1) {
        return new Boolean(tempShort == 1);
      }
    }

    if (rawEncoded.length == Bytes.SIZEOF_INT || 
        rawEncoded.length == Bytes.SIZEOF_FLOAT) {
      int tempInt = Bytes.toInt(rawEncoded);
      if (tempInt == 1 || tempInt == 0) {
        return new Boolean(tempInt == 1);
      }

      float tempFloat = Bytes.toFloat(rawEncoded);
      if (tempFloat == 0.0f || tempFloat == 1.0f) {
        return new Boolean(tempFloat == 1.0f);
      }
    }

    if (rawEncoded.length == Bytes.SIZEOF_LONG ||
        rawEncoded.length == Bytes.SIZEOF_DOUBLE) {
      long tempLong = Bytes.toLong(rawEncoded);
      if (tempLong == 0L || tempLong == 1L) {
        return new Boolean(tempLong == 1L);
      }

      double tempDouble = Bytes.toDouble(rawEncoded);
      if (tempDouble == 0.0 || tempDouble == 1.0) {
        return new Boolean(tempDouble == 1.0);
      }
    }

    // not identifiable from a number
    return null; */
  }
  
  /**
   * Utility method to convert a comma separated list of string values 
   * (for an indexed type) to an array of objects.
   * 
   * @param list the comma separated list of values to parse
   * @return an array of objects where each element contains one value from
   * the list
   * @throws IllegalArgumentException if the list is empty
   */
  public static Object[] stringIndexListToObjects(String list) 
    throws IllegalArgumentException {
    String[] labels = list.replace("{", "").replace("}", "").split(",");
    if (labels.length < 1) {
      throw new IllegalArgumentException("Indexed/nominal type must have at least one " +
                    "label declared");
    }
    for (int i = 0; i < labels.length; i++) {
      labels[i] = labels[i].trim();
    }
    
    return labels;
  }
  
  /**
   * Utility method to convert an array of objects containing indexed values to
   * a comma separated list as a string.
   * 
   * @param values the array of values to convert
   * @return a comma separated list as a string
   */
  public static String objectIndexValuesToString(Object[] values) {
    StringBuffer result = new StringBuffer();
    result.append("{");
    
    for (int i = 0; i < values.length; i++) { 
      if (i < values.length - 1) {
        result.append(values[i].toString().trim()).append(",");
      } else {
        result.append(values[i].toString().trim()).append("}");
      }
    }
    
    return result.toString();
  }
}
