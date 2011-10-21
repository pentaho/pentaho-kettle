/* Copyright (c) 2011 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
 */

package org.pentaho.cassandra;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.cassandra.thrift.CfDef;
import org.apache.cassandra.thrift.Column;
import org.apache.cassandra.thrift.ColumnDef;
import org.apache.cassandra.thrift.CqlRow;
import org.apache.cassandra.thrift.KsDef;
import org.apache.cassandra.utils.UUIDGen;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;

/**
 * Class encapsulating schema information for a colum family. Has utility
 * routines for converting between Cassandra meta data and Kettle meta data, and
 * for deserializing values.
 * 
 * @author Mark Hall (mhall{[at]}pentaho{[dot]}com)
 * @version $Revision$
 */
public class CassandraColumnMetaData {
  public static final String UTF8 = "UTF-8";
  public static final String ASCII = "US-ASCII";
  
  /** Name of the column family this meta data refers to */
  protected String m_columnFamilyName; // can be used as the key name
  
  /** Type of the key */
  protected String m_keyValidator; // name of the class for key validation
  
  /** Type of the column names (used for sorting columns) */
  protected String m_columnComparator; // name of the class for sorting column names
  
  /** m_columnComparator converted to Charset encoding string */
  protected String m_columnNameEncoding;
  
  /** 
   * Default validator for the column family (table) - we can use this as
   * the type for any columns specified in a SELECT clause which *arent* in
   * the meta data
   */
  protected String m_defaultValidationClass;
  
  /** Map of column names/types */
  protected Map<String, String> m_columnMeta;
  
  // serialization/deserialization buffers
  protected ByteBuffer m_longBuffer = ByteBuffer.allocate(8);
  protected ByteBuffer m_doubleBuffer = ByteBuffer.allocate(8);
  protected ByteBuffer m_dateBuffer = ByteBuffer.allocate(8); // same as a long (I think)
  protected ByteBuffer m_intBuffer = ByteBuffer.allocate(4);
  protected ByteBuffer m_floatBuffer = ByteBuffer.allocate(4);
  protected StringBuffer m_schemaDescription = new StringBuffer();
  
  
  /**
   * Constructor.
   * 
   * @param conn connection to cassandra
   * @param columnFamily the name of the column family to maintain meta data for.
   * @throws Exception if a problem occurs during connection or when fetching meta
   * data
   */
  public CassandraColumnMetaData(CassandraConnection conn, 
      String columnFamily) throws Exception {
    m_columnFamilyName = columnFamily;
    
    
    // column families               
    KsDef keySpace = conn.describeKeyspace();
    List<CfDef> colFams = null;
    if (keySpace != null) {
      colFams = keySpace.getCf_defs();
    } else {
      throw new Exception("Unable to get meta data on keyspace '" 
          + conn.m_keyspaceName + "'");
    }
    
    // look for the requested column family
    CfDef colDefs = null;
    for (CfDef fam : colFams) {
      String columnFamilyName = fam.getName(); // table name
      if (columnFamilyName.equals(columnFamily)) {
        m_schemaDescription.append("Column family: " + m_columnFamilyName);
        m_keyValidator = fam.getKey_validation_class(); // key type                                                
        m_columnComparator = fam.getComparator_type(); // column names encoded as
        m_defaultValidationClass = fam.getDefault_validation_class(); // default column type
        m_schemaDescription.append("\n\tKey validator: " 
            + m_keyValidator.substring(m_keyValidator.lastIndexOf(".")+1, m_keyValidator.length()));
        m_schemaDescription.append("\n\tColumn comparator: " 
            + m_columnComparator.substring(m_columnComparator.lastIndexOf(".")+1, m_columnComparator.length()));
        m_schemaDescription.append("\n\tDefault column validator: " 
            + m_defaultValidationClass.substring(m_defaultValidationClass.lastIndexOf(".")+1, 
                m_defaultValidationClass.length()));
        m_schemaDescription.append("\n\tMemtable operations: " + fam.getMemtable_operations_in_millions());
        m_schemaDescription.append("\n\tMemtable throughput: " + fam.getMemtable_throughput_in_mb());
        m_schemaDescription.append("\n\tMemtable flush after: " + fam.getMemtable_flush_after_mins());
        m_schemaDescription.append("\n\tRows cached: " + fam.getRow_cache_size());
        m_schemaDescription.append("\n\tRow cache save period: " + fam.getRow_cache_save_period_in_seconds());
        m_schemaDescription.append("\n\tKeys cached: " + fam.getKey_cache_size());
        m_schemaDescription.append("\n\tKey cached save period: " + fam.getKey_cache_save_period_in_seconds());
        m_schemaDescription.append("\n\tRead repair chance: " + fam.getRead_repair_chance());
        m_schemaDescription.append("\n\tGC grace: " + fam.getGc_grace_seconds());
        m_schemaDescription.append("\n\tMin compaction threshold: " + fam.getMin_compaction_threshold());
        m_schemaDescription.append("\n\tMax compaction threshold: " + fam.getMax_compaction_threshold());
        m_schemaDescription.append("\n\tReplicate on write: " + fam.replicate_on_write);
        String rowCacheP = fam.getRow_cache_provider();
        m_schemaDescription.append("\n\tRow cache provider: " 
            + rowCacheP.substring(rowCacheP.lastIndexOf(".")+1, rowCacheP.length()));
        m_schemaDescription.append("\n\n\tColumn metadata:");
        
        colDefs = fam;
        break;
      }          
    }
    
    if (colDefs == null) {
      throw new Exception("Unable to find requested column family '" 
          + columnFamily + "' in keyspace '" + conn.m_keyspaceName + "'");
    }
    
    if (m_columnComparator.indexOf("UTF8Type") > 0) {
      m_columnNameEncoding = UTF8;
    } else if (m_columnComparator.indexOf("AsciiType") > 0) {
      m_columnNameEncoding = ASCII;
    } else {
      throw new Exception("Column names are neither UTF-8 or ASCII!");
    }
    
    // set up our meta data map
    m_columnMeta = new TreeMap<String, String>();
    List<ColumnDef> colMetaData = colDefs.getColumn_metadata();
    if (colMetaData != null) {
      for (int i = 0; i < colMetaData.size(); i++) {
        String colName = new String(colMetaData.get(i).getName(), 
            Charset.forName(m_columnNameEncoding));
        //      System.out.println("Col name: " + colName);
        String colType = colMetaData.get(i).getValidation_class();
        //      System.out.println("Validation (type): " + colType);
        m_columnMeta.put(colName, colType);
        
        m_schemaDescription.append("\n\tColumn name: " + colName);
        m_schemaDescription.append("\n\t\tColumn validator: " 
            + colType.substring(colType.lastIndexOf(".")+1, colType.length()));
        String indexName = colMetaData.get(i).getIndex_name();
        if (!Const.isEmpty(indexName)) {
          m_schemaDescription.append("\n\t\tIndex name: " + colMetaData.get(i).getIndex_name());
        }        
      }
    }
    
//    System.out.println(m_schemaDescription.toString());
  }
  
  /**
   * Static utility routine for checking for the existence of
   * a column family (table)
   * 
   * @param conn the connection to use
   * @param columnFamily the column family to check for
   * @return true if the supplied column family name exists in the keyspace
   * @throws Exception if a problem occurs
   */
  public static boolean columnFamilyExists(CassandraConnection conn,
      String columnFamily) throws Exception {
    
    boolean found = false;

    // column families               
    KsDef keySpace = conn.describeKeyspace();
    List<CfDef> colFams = null;
    if (keySpace != null) {
      colFams = keySpace.getCf_defs();
    } else {
      throw new Exception("Unable to get meta data on keyspace '" 
          + conn.m_keyspaceName + "'");
    }

    // look for the requested column family
    CfDef colDefs = null;
    for (CfDef fam : colFams) {
      String columnFamilyName = fam.getName(); // table name
      if (columnFamilyName.equals(columnFamily)) {
        found = true;
        break;
      }
    }
    
    return found;
  }
  
  /**
   * Return the schema overview information
   * 
   * @return the textual description of the schema
   */
  public String getSchemaDescription() {
    return m_schemaDescription.toString();
  }
  
  public ValueMetaInterface getValueMetaForKey() {
    return getValueMetaForColumn(getKeyName());
  }
  
  public ValueMetaInterface getValueMetaForColumn(String colName) {
    String type = null;
    // check the key first
    if (colName.equals(getKeyName())) {
      type = m_keyValidator;
    } else {
      type = m_columnMeta.get(colName);
      if (type == null) {
        type = m_defaultValidationClass;
      }
    }
    
    int kettleType = 0;
    if (type.indexOf("UTF8Type") > 0 || type.indexOf("AsciiType") > 0) {
      kettleType = ValueMetaInterface.TYPE_STRING;
    }
    if (type.indexOf("LongType") > 0 || type.indexOf("IntegerType") > 0) {
      kettleType = ValueMetaInterface.TYPE_INTEGER;
    }
    if (type.indexOf("DoubleType") > 0 || type.indexOf("FloatType") > 0) {
      kettleType = ValueMetaInterface.TYPE_NUMBER;
    }
    if (type.indexOf("DateType") > 0) {
      kettleType = ValueMetaInterface.TYPE_DATE;
    }
    if (type.indexOf("BytesType") > 0) {
      kettleType = ValueMetaInterface.TYPE_BINARY;
    }
    if (type.indexOf("UUIDType") > 0) {
      // users can always parse this downstream
      kettleType = ValueMetaInterface.TYPE_STRING;
    }
    
    ValueMetaInterface newVM = new ValueMeta(colName, kettleType);
    
    return newVM;
  }  
  
  public List<ValueMetaInterface> getValueMetasForSchema() {
    List<ValueMetaInterface> newL = new ArrayList<ValueMetaInterface>();
    
    for (String colName : m_columnMeta.keySet()) {
      ValueMetaInterface colVM = getValueMetaForColumn(colName);
      newL.add(colVM);
    }
    
    return newL;
  }
  
  public Set<String> getColumnNames() {
    // only returns those column names that are defined in the schema!
    return m_columnMeta.keySet();
  }
  
  public boolean columnExistsInSchema(String colName) {
    return (m_columnMeta.get(colName) != null);
  }
  
  public String getKeyName() {
    // we use the column family/table name as the key
    return getColumnFamilyName();
  }
  
  public String getColumnFamilyName() {
    return m_columnFamilyName;
  }
  
  /**
   * Return the decoded key value of a row. Assumes that the supplied
   * row comes from the column family that this meta data represents!!
   * 
   * @param row a Cassandra row
   * @return the decoded key value
   */
  public Object getKeyValue(CqlRow row) {
    byte[] key = row.getKey();
    
    return getColumnValue(key, m_keyValidator);
  }
  
  public String getColumnName(Column aCol) {
    byte[] colName = aCol.getName();
    String decodedColName = new String(colName, Charset.forName(m_columnNameEncoding));
    
    // assmue that any columns that are not in teh schema contain values that
    // are compatible with the default column validator
    
//    if (m_columnMeta.containsKey(decodedColName)) {
      return decodedColName;
//    }
  //  return null;
  }
  
  private Object getColumnValue(byte[] val, String decoder) {
    if (val == null) {
      return null;
    }
    
    if (decoder.indexOf("UTF8Type") > 0) {
      return getString(val, UTF8);
    }
    
    if (decoder.indexOf("AsciiType") > 0) {
      return getString(val, ASCII);
    }
    
    if (decoder.indexOf("LongType") > 0) {
      return getLong(val);
    }
    
    if (decoder.indexOf("DoubleType") > 0) {
      return getDouble(val);
    }
    
    if (decoder.indexOf("DateType") > 0) {
      return getDate(val);
    }
    
    if (decoder.indexOf("IntegerType") > 0) {
      return getInteger(val);
    }
    
    if (decoder.indexOf("FloatType") > 0) {
      return getFloat(val);
    }
    
    if (decoder.indexOf("UUIDType") > 0) {
      return getUUIDAsString(val);
    }    
    
    // default - ByteType
    return val;
  }
  
  public Object getColumnValue(Column aCol) {
    String colName = getColumnName(aCol);
    
    // Clients should use getKey() for getting the key
    if (colName.equals("KEY")) {
      return null;
    }
    
    String decoder = m_columnMeta.get(colName);
    if (decoder == null) {
      // column is not in schema so use default validator
      decoder = m_defaultValidationClass;
    }
    
    byte[] val = aCol.getValue();
    return getColumnValue(val, decoder);
  }
  
  public String getString(byte[] raw, String encoding) {
    return new String(raw, Charset.forName(encoding));
  }
  
  public String getUUIDAsString(byte[] raw) {
    if (raw.length == 0) {
      return "";
    }
    
    if (raw.length != 16) {
      throw new RuntimeException("UUIDs must be exactly 16 bytes");
    }
    
    return UUIDGen.getUUID(ByteBuffer.wrap(raw)).toString();
  }
  
  public long getLong(byte[] raw) {
    m_longBuffer.clear();
    m_longBuffer.put(raw);
    
    return m_longBuffer.getLong(0);
  }
  
  public double getDouble(byte[] raw) {
    m_doubleBuffer.clear();
    m_doubleBuffer.put(raw);
    
    return m_doubleBuffer.getDouble(0);
  }
  
  public Date getDate(byte[] raw) {
    m_dateBuffer.clear();
    m_dateBuffer.put(raw);
    
    long d = m_dateBuffer.getLong(0);
    return new Date(d);
  }
  
  public int getInteger(byte[] raw) {
    m_intBuffer.clear();
    m_intBuffer.put(raw);
    
    return m_intBuffer.getInt(0);
  }
  
  public float getFloat(byte[] raw) {
    m_floatBuffer.clear();
    m_floatBuffer.put(raw);
    
    return m_floatBuffer.getFloat(0);
  }
}
