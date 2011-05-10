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

package org.pentaho.hbase.mapping;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.KeyOnlyFilter;
import org.apache.hadoop.hbase.util.Bytes;
import org.pentaho.di.core.row.ValueMetaInterface;

/**
 * Class for managing a mapping table in HBase. Has routines for creating the mapping
 * table, writing and reading mappings to/from the table and creating a test table
 * for debugging purposes. Also has a rough and ready command line interface. For more
 * information on the structure of a table mapping see org.pentaho.hbase.mapping.Mapping.
 * 
 * @author Mark Hall (mhall[{at]}pentaho{[dot]}com)
 * @version $Revision$
 */
public class MappingAdmin {
  
  /** Configuration object for the connection */
  protected Configuration m_connection;
  
  /** Admin object */
  protected HBaseAdmin m_admin;
  
  /** Name of the mapping table (might make this configurable at some stage) */
  protected String m_mappingTableName = "pentaho_mappings";
  
  /** family name to hold the mapped column meta data in a mapping */
  public static final String COLUMNS_FAMILY_NAME = "columns";
  
  /**
   * family name to hold the key meta data in a mapping. This meta data will
   * be the same for any mapping defined on the same table 
   */
  public static final String KEY_FAMILY_NAME = "key";
  
  /**
   * Constructor. No conneciton information configured.
   */
  public MappingAdmin() {
  }
  
  /**
   * Constructor
   * 
   * @param conf a configuration object containing connection information
   * @throws Exception if a problem occurs
   */
  public MappingAdmin(Configuration conf) throws Exception {
    setConnection(conf);
  }
  
  /**
   * Set the connection to use
   * 
   * @param con a configuration object containing connection information.
   * @throws Exception if a problem occurs
   */
  public void setConnection(Configuration con) throws Exception {
    m_connection = con;
    m_admin = new HBaseAdmin(m_connection);
  }
  
  /**
   * Just use whatever can be loaded from the classpath for the connection
   * 
   * @throws Exception
   */
  public void setUseDefaultConnection() throws Exception {
    m_connection = HBaseConfiguration.create();
    m_admin = new HBaseAdmin(m_connection);    
  }
  
  /**
   * Get the configuration being used for the connection
   * 
   * @return the configuration encapsulating connection information
   */
  public Configuration getConnection() {
    return m_connection;
  }
  
  /**
   * Set the name of the mapping table.
   * 
   * @param tableName the name to use for the mapping table.
   */
  public void setMappingTableName(String tableName) {
    m_mappingTableName = tableName;
  }
  
  /**
   * Get the name of the mapping table
   * 
   * @return the name of the mapping table
   */
  public String getMappingTableName() {
    return m_mappingTableName;
  }
  
  // create a test table in the same format as the test mapping
  public void createTestTable() throws IOException {
    if (m_connection == null) {
      throw new IOException("No connection exists yet!");
    }
    
    if (m_admin.tableExists("MarksTestTable")) {
      // drop/delete the table and re-create
      m_admin.disableTable(Bytes.toBytes("MarksTestTable"));
      m_admin.deleteTable(Bytes.toBytes("MarksTestTable"));
      //throw new IOException("MarksTestTable already exists!");
    }
    
    HTableDescriptor tableDescription = new HTableDescriptor("MarksTestTable");
    
    // two column families
    HColumnDescriptor mappingColumnFamily = new HColumnDescriptor("Family1");
    tableDescription.addFamily(mappingColumnFamily);
    mappingColumnFamily = new HColumnDescriptor("Family2");
    tableDescription.addFamily(mappingColumnFamily);
    
    m_admin.createTable(tableDescription);
    
    HTable testTable = new HTable(m_connection, "MarksTestTable");
    testTable.setAutoFlush(false);
    testTable.setWriteBufferSize(1024 * 1024 * 12);
    
    // insert 200 test rows of random stuff
    Random r = new Random();
    String[] nomVals = {"nomVal1", "nomVal2", "nomVal3"};
    Date date = new Date();
    Calendar c = new GregorianCalendar();
    c.setTime(date);
    Calendar c2 = new GregorianCalendar();
    c2.set(1970, 2, 1);
    for (long key = -500; key < 20000; key++) {
      //Put p = new Put(Bytes.toBytes(key));
      Put p = null;
      try {
        p = new Put(HBaseValueMeta.encodeKeyValue(new Long(key), Mapping.KeyType.LONG));
        p.setWriteToWAL(false);
      } catch (Exception ex) {
        ex.printStackTrace();
        return;
      }
      
      // unsigned (positive) integer column
      p.add(Bytes.toBytes("Family1"), Bytes.toBytes("first_integer_column"), 
          Bytes.toBytes((int)key / 10));
      
      // String column
      p.add(Bytes.toBytes("Family1"), Bytes.toBytes("first_string_column"), 
          Bytes.toBytes("StringValue_" + key));
      
      // have some null values - every 10th row has no value for the indexed
      // column
      if (key % 10L > 0) {
        int index = r.nextInt(3);
        String nomVal = nomVals[index];
        p.add(Bytes.toBytes("Family2"), Bytes.toBytes("first_indexed_column"), 
            Bytes.toBytes(nomVal));
      }
      
      // signed integer column
      double d = r.nextDouble();
      int signedInt = r.nextInt(100);
      if (d < 0.5) {
        signedInt = -signedInt;
      }
      p.add(Bytes.toBytes("Family2"), Bytes.toBytes("first_signed_int_column"), 
          Bytes.toBytes(signedInt));
      
      // unsigned (positive) float column
      float f = r.nextFloat() * 1000.0f;
      p.add(Bytes.toBytes("Family2"), Bytes.toBytes("first_unsigned_float_column"), 
          Bytes.toBytes(f));
      
      // signed float column
      if (d > 0.5) {
        f = -f;
      }
      
      p.add(Bytes.toBytes("Family2"), Bytes.toBytes("first_signed_float_column"), 
          Bytes.toBytes(f));
      
      // unsigned double column
      double dd = d * 10000 * r.nextDouble();
      p.add(Bytes.toBytes("Family2"), Bytes.toBytes("first_unsigned_double_column"), 
          Bytes.toBytes(dd));
      
      // signed double
      if (d > 0.5) {
        dd = -dd;
      }
      p.add(Bytes.toBytes("Family2"), Bytes.toBytes("first_signed_double_column"), 
          Bytes.toBytes(dd));
      
      // unsigned long
      long l = (long)r.nextInt(300);
      p.add(Bytes.toBytes("Family2"), Bytes.toBytes("first_unsigned_long_column"), 
          Bytes.toBytes(l));
      
      if (d < 0.5) {
        l = -l;
      }
      p.add(Bytes.toBytes("Family2"), Bytes.toBytes("first_signed_long_column"), 
          Bytes.toBytes(l));
      
      // unsigned date (vals >= 1st Jan 1970)
        c.add(Calendar.DAY_OF_YEAR, 1);
//        c.add(Calendar.MONTH, 1);

      long longd = c.getTimeInMillis();
      p.add(Bytes.toBytes("Family1"), Bytes.toBytes("first_unsigned_date_column"), 
          Bytes.toBytes(longd));
      
      // signed date (vals < 1st Jan 1970)
      c2.add(Calendar.DAY_OF_YEAR, -1);
      longd = c2.getTimeInMillis();
      //System.out.println(":::: " + longd);
      p.add(Bytes.toBytes("Family1"), Bytes.toBytes("first_signed_date_column"), 
          Bytes.toBytes(longd));
      
      // boolean column
      String bVal = "";
      if (d < 0.5) {
        bVal = "N";
      } else {
        bVal = "Y";
      }
      p.add(Bytes.toBytes("Family1"), Bytes.toBytes("first_boolean_column"), 
          Bytes.toBytes(bVal));
      
      // serialized objects
      byte[] serialized = HBaseValueMeta.encodeObject(new Double(d));
      //System.err.println(":::::::  " + serialized.length);
//      Object decoded = HBaseValueMeta.decodeObject(serialized);

      p.add(Bytes.toBytes("Family1"), Bytes.toBytes("first_serialized_column"), 
          serialized);
      
      // binary (raw bytes)
      byte[] rawStuff = Bytes.toBytes(5034555);
      p.add(Bytes.toBytes("Family1"), Bytes.toBytes("first_binary_column"), 
          rawStuff);
      
      testTable.put(p);
    }
    testTable.flushCommits();
    testTable.close();
    
    // -----
    
    /*Put p = new Put(Bytes.toBytes(5));
    p.add(Bytes.toBytes("dummy"), Bytes.toBytes("col"), Bytes.toBytes("dummyVal"));
    testTable.put(p);
    
    p = new Put(Bytes.toBytes(1));
    p.add(Bytes.toBytes("dummy"), Bytes.toBytes("col"), Bytes.toBytes("dummyVal"));
    testTable.put(p);
    
    p = new Put(Bytes.toBytes(-6));
    p.add(Bytes.toBytes("dummy"), Bytes.toBytes("col"), Bytes.toBytes("dummyVal"));
    testTable.put(p); */
  }
  
  /**
   * Create the mapping table
   * 
   * @throws IOException if there is no connection specified or the mapping
   * table already exists.
   */
  public void createMappingTable() throws IOException {
    if (m_connection == null) {
      throw new IOException("No connection exists yet!");
    }
    
    if (m_admin.tableExists(m_mappingTableName)) {
      throw new IOException("Mapping table already exists!");
    }
    
    HTableDescriptor tableDescription = new HTableDescriptor(m_mappingTableName);
    
    // One column family for the mapped columns meta data
    HColumnDescriptor mappingColumnFamily = new HColumnDescriptor(COLUMNS_FAMILY_NAME);
    tableDescription.addFamily(mappingColumnFamily);
    
    // One column family for the key meta data
    HColumnDescriptor keyColumnFamily = new HColumnDescriptor(KEY_FAMILY_NAME);
    tableDescription.addFamily(keyColumnFamily);
    
    m_admin.createTable(tableDescription);
  }
  
  /**
   * Check to see if the specified mapping name exists for the specified table
   * 
   * @param tableName the name of the table
   * @param mappingName the name of the mapping
   * @return true if the specified mapping exists for the specified table
   * @throws IOException if a problem occurs
   */
  public boolean mappingExists(String tableName, String mappingName) throws IOException {
    if (m_connection == null) {
      throw new IOException("No connection exists yet!");
    }
    
    if (m_admin.tableExists(m_mappingTableName)) {
      // String paddedTableName = pad(tableName, " ", TABLE_NAME_LENGTH, true);
      String compoundKey = tableName + HBaseValueMeta.SEPARATOR + mappingName;
      
      HTable mappingTable = new HTable(m_connection, m_mappingTableName);
      Get g = new Get(Bytes.toBytes(compoundKey));
      Result r = mappingTable.get(g);
      boolean result = r.isEmpty();
      
      mappingTable.close();
      //return (!r.isEmpty());
      return (!result);
    }
    return false;    
  }
  
  /**
   * Get a list of tables that have mappings. List will be empty if 
   * there are no mappings defined yet.
   * 
   * @return a list of tables that have mappings.
   * @throws IOException if something goes wrong
   */
  public Set<String> getMappedTables() throws IOException {
    
    if (m_connection == null) {
      throw new IOException("No connection exists yet!");
    }
    
    HashSet<String> tableNames = new HashSet<String>();
    if (m_admin.tableExists(m_mappingTableName)) {

      KeyOnlyFilter f = new KeyOnlyFilter();
      Scan s = new Scan(Bytes.toBytes(""), f);
      HTable mappingTable = new HTable(m_connection, m_mappingTableName);
      ResultScanner rs = mappingTable.getScanner(s);

      //HashSet<String> tableNames = new HashSet<String>();

      for (Result r : rs) {
        byte[] rawKey = r.getRow();
        String decodedKey = Bytes.toString(rawKey);

        // extract the table name
        String tableName = 
          decodedKey.substring(0, decodedKey.indexOf(HBaseValueMeta.SEPARATOR));
        tableNames.add(tableName.trim());
      }

      rs.close();
      mappingTable.close();
    }
    
    return tableNames;
  }
  
  /**
   * Get a list of mappings for the supplied table name. List will be empty
   * if there are no mappings defined for the table.
   * 
   * @param tableName the table name
   * @return a list of mappings
   * @throws IOException if something goes wrong.
   */
  public List<String> getMappingNames(String tableName) throws IOException {
    
    if (m_connection == null) {
      throw new IOException("No connection exists yet!");
    }
    
    // String paddedTableName = pad(tableName, " ", TABLE_NAME_LENGTH, true);
    
    //TODO may have to use a RowFilter (with BinaryPrefixComparator) instead
    //FilterList fl = new FilterList(FilterList.Operator.MUST_PASS_ALL);
//    FilterList fl = new FilterList();
    //fl.addFilter(new KeyOnlyFilter());
    //fl.addFilter(new PrefixFilter(Bytes.toBytes(paddedTableName)));

    //Scan s = new Scan(Bytes.toBytes(paddedTableName), fl);
    List<String> mappingsForTable = new ArrayList<String>();
    if (m_admin.tableExists(m_mappingTableName)) {
      Scan s = new Scan();
      HTable mappingTable = new HTable(m_connection, m_mappingTableName);
      ResultScanner rs = mappingTable.getScanner(s);        


      for (Result r : rs) {
        byte[] rawKey = r.getRow();
        String decodedKey = Bytes.toString(rawKey);
        String tableN = decodedKey.substring(0, decodedKey.indexOf(HBaseValueMeta.SEPARATOR)).trim();

        if (tableName.equals(tableN)) {
          // extract out the mapping name
          String mappingName = 
            decodedKey.substring(decodedKey.indexOf(HBaseValueMeta.SEPARATOR) + 1, 
                decodedKey.length());
          mappingsForTable.add(mappingName);
        }
      }

      rs.close();
      mappingTable.close();
    }
    
    return mappingsForTable;
  }
  
  /**
   * Delete a mapping from the mapping table
   * 
   * @param tableName name of the table in question
   * @param mappingName name of the mapping in question
   * @return true if the named mapping for the named table was deleted
   * successfully; false if the mapping table does not exist or the named 
   * mapping for the named table does not exist in the mapping table
   * @throws IOException if a problem occurs during deletion
   */
  public boolean deleteMapping(String tableName, String mappingName) throws IOException {
    String compoundKey = tableName + HBaseValueMeta.SEPARATOR + mappingName;
    
    if (!m_admin.tableExists(m_mappingTableName)) {      
      // create the mapping table
      createMappingTable();         
      return false; // no mapping table so nothing to delete!
    }
    
    HTable mappingTable = new HTable(m_connection, m_mappingTableName);
    
    if (m_admin.isTableDisabled(m_mappingTableName)) {
      m_admin.enableTable(m_mappingTableName);
    }
    
    boolean mappingExists = mappingExists(tableName, mappingName); 
    if (!mappingExists) {
      return false; // mapping doesn't seem to exist
    }
    
    Delete d = new Delete(Bytes.toBytes(compoundKey));
    mappingTable.delete(d);
    
    return true;
  }
  
  /**
   * Delete a mapping from the mapping table
   * 
   * @param theMapping the mapping to delete
   * @return true if the mapping was deleted successfully; false if the mapping
   * table does not exist or the suppied mapping does not exist in the mapping
   * table
   * @throws IOException if a problem occurs during deletion
   */
  public boolean deleteMapping(Mapping theMapping) throws IOException {
    String tableName = theMapping.getTableName();
    String mappingName = theMapping.getMappingName();
    
    return deleteMapping(tableName, mappingName);
  }
  
  /**
   * Add a mapping into the mapping table. Can either throw an
   * IOException if the mapping already exists in the table, or
   * overwrite (delete and then add) it if the overwrite parameter
   * is set to true.
   * 
   * @param tableName
   * @param mappingName
   * @param mapping
   * @param overwrite
   * @throws IOException
   */
  public void putMapping(Mapping theMapping, boolean overwrite) throws IOException {
    
    String tableName = theMapping.getTableName();
    String mappingName = theMapping.getMappingName();
    Map<String, HBaseValueMeta> mapping = theMapping.getMappedColumns();
    String keyName = theMapping.getKeyName();
    Mapping.KeyType keyType = theMapping.getKeyType();
    
    if (m_connection == null) {
      throw new IOException("No connection exists yet!");
    }
    
    //String paddedTableName = pad(tableName, " ", TABLE_NAME_LENGTH, true);
    //String compoundKey = paddedTableName + HBaseValueMeta.SEPARATOR + mappingName;
    String compoundKey = tableName + HBaseValueMeta.SEPARATOR + mappingName;
    
    if (!m_admin.tableExists(m_mappingTableName)) {
      
      // create the mapping table
      createMappingTable();         
    }
    
    HTable mappingTable = new HTable(m_connection, m_mappingTableName);
    
    if (m_admin.isTableDisabled(m_mappingTableName)) {
      m_admin.enableTable(m_mappingTableName);
    }    
    
    boolean mappingExists = mappingExists(tableName, mappingName); 
    if (mappingExists && !overwrite) {
      throw new IOException("The mapping \"" + mappingName + "\" already exists " +
      		"for table \"" + tableName + "\"");
    }
    
    if (mappingExists) {
      // delete it first before adding the new one
      Delete d = new Delete(Bytes.toBytes(compoundKey));
      mappingTable.delete(d);
    }
    
    
    // add the new mapping
    Put p = new Put(Bytes.toBytes(compoundKey));

    String family = COLUMNS_FAMILY_NAME;
    Set<String> aliases = mapping.keySet();
    for (String alias : aliases) {
      HBaseValueMeta vm = mapping.get(alias);
      String qualifier = vm.getColumnFamily() + HBaseValueMeta.SEPARATOR
        + vm.getColumnName() + HBaseValueMeta.SEPARATOR + alias;
      String valueType = ValueMetaInterface.typeCodes[vm.getType()];
      
      // make sure that we save the correct type name so that unsigned filtering
      // works correctly!
      if (vm.isInteger() && vm.getIsLongOrDouble()) {
        valueType = "Long";
      }
      
      if (vm.isNumber()) {
        if (vm.getIsLongOrDouble()) {
          valueType = "Double";
        } else {
          valueType = "Float";
        }
      }
      
      // check to see if we are storing a string date or date as
      // a long
/*      if (vm.isDate() && !Const.isEmpty(vm.getConversionMask())) {
        valueType += " " + vm.getConversionMask();
      } */
      
      // check for nominal/indexed
      if (vm.getStorageType() == ValueMetaInterface.STORAGE_TYPE_INDEXED &&
          vm.isString()) {
        Object[] labels = vm.getIndex();
        StringBuffer vals = new StringBuffer();
        vals.append("{");
        
        for (int i = 0; i < labels.length; i++) {
          if (i != labels.length - 1) {
            vals.append(labels[i].toString().trim()).append(",");
          } else {
            vals.append(labels[i].toString().trim()).append("}");
          }
        }
        valueType = vals.toString();
      }
     
      // add this mapped column in
      p.add(Bytes.toBytes(family), Bytes.toBytes(qualifier), Bytes.toBytes(valueType));
    }
    
    // now do the key
    family = KEY_FAMILY_NAME;
    String qualifier = keyName;
    String valueType = keyType.toString();

    p.add(Bytes.toBytes(family), Bytes.toBytes(qualifier), Bytes.toBytes(valueType));
    
    // add the row
    mappingTable.put(p);
    
    mappingTable.flushCommits();
    mappingTable.close();
  }
  
  /**
   * Returns a textual description of a mapping
   * 
   * @param tableName the table name
   * @param mappingName the mapping name
   * @return a string describing the specified mapping on the specified table
   * @throws IOException if a problem occurs
   */
  public String describeMapping(String tableName, String mappingName)
    throws IOException {    
    
    return describeMapping(getMapping(tableName, mappingName));
  }
  
  /**
   * Returns a textual description of a mapping
   * 
   * @param aMapping the mapping
   * @return a textual description of the supplied mapping object
   * @throws IOException if a problem occurs
   */
  public String describeMapping(Mapping aMapping)
    throws IOException {
    
    return aMapping.toString();    
  }
  
  /**
   * Get a mapping for the specified table under the specified mapping name
   * 
   * @param tableName the name of the table
   * @param mappingName the name of the mapping to get for the table
   * @return a mapping for the supplied table
   * @throws IOException if a mapping by the given name does not exist for
   * the given table
   */
  public Mapping getMapping(String tableName, String mappingName) 
    throws IOException {
    
    if (m_connection == null) {
      throw new IOException("No connection exists yet!");
    }
    
    //String paddedTableName = pad(tableName, " ", TABLE_NAME_LENGTH, true);
    //String compoundKey = paddedTableName + HBaseValueMeta.SEPARATOR + mappingName;
    String compoundKey = tableName + HBaseValueMeta.SEPARATOR + mappingName;
    
    if (!m_admin.tableExists(m_mappingTableName)) {
      
      // create the mapping table
      createMappingTable();
      
      throw new IOException("Mapping \"" + compoundKey + "\" does not exist!");
    }
        
    HTable mappingTable = new HTable(m_connection, m_mappingTableName);
    Get g = new Get(Bytes.toBytes(compoundKey));
    Result r = mappingTable.get(g);
    
    if (r.isEmpty()) {
      throw new IOException("Mapping \"" + compoundKey + "\" does not exist!");
    }
    
    //System.err.println("+++ " + r);
            
    NavigableMap<byte[], byte[]> colsInKeyFamily =
      r.getFamilyMap(Bytes.toBytes(KEY_FAMILY_NAME));
    Set<byte[]> keyCols = colsInKeyFamily.keySet();
    // should only be one key defined!!
    if (keyCols.size() != 1) {
      throw new IOException("Mapping \"" + compoundKey 
          + "\" has more than one key defined!");
    }
    byte[] keyNameB = keyCols.iterator().next();
    String decodedKeyName = Bytes.toString(keyNameB);
    byte[] keyTypeB = colsInKeyFamily.get(keyNameB);
    String decodedKeyType = Bytes.toString(keyTypeB);
//    String dateFormatString = null;
    Mapping.KeyType keyType = null;
/*    if (decodedKeyType.toLowerCase().startsWith(Mapping.KeyType.DATE.toString().toLowerCase())
        && decodedKeyType.length() > Mapping.KeyType.DATE.toString().length()) {
        
      dateFormatString = decodedKeyType.
        substring(Mapping.KeyType.DATE.toString().length(), 
            decodedKeyType.length()).trim();      
      
      decodedKeyType = Mapping.KeyType.DATE.toString();
    } */
    for (Mapping.KeyType t : Mapping.KeyType.values()) {
      if (decodedKeyType.equalsIgnoreCase(t.toString())) {
        keyType = t;
        break;
      }
    }
    
    if (keyType == null) {
      throw new IOException("Unrecognized type for the key column in \""
          + compoundKey + "\"");
    }
    
    /*if (keyType == Mapping.KeyType.DATE_AS_STRING &&
        (dateFormatString == null || dateFormatString.length() == 0)) {
      throw new IOException("No date formatting string supplied for the key " +
      		"in \"" + compoundKey + "\"");
    } */
    
    Mapping resultMapping = new Mapping(tableName, mappingName, 
        decodedKeyName, keyType);
    
  /*  if (dateFormatString != null) {
      resultMapping.setKeyStringDateFormat(dateFormatString);
    } */
    
    Map<String, HBaseValueMeta> resultCols = 
      new TreeMap<String, HBaseValueMeta>();
    
    // now process the mapping
    NavigableMap<byte[], byte[]> colsInMapping = 
      r.getFamilyMap(Bytes.toBytes(COLUMNS_FAMILY_NAME));
    
    Set<byte[]> colNames = colsInMapping.keySet();
    
    for (byte[] b : colNames) {
      String decodedName = Bytes.toString(b);
      byte[] c = colsInMapping.get(b);
      if (c == null) {
        throw new IOException("No type declaration for column \"" + decodedName + "\"");
      }
      
      String decodedType = Bytes.toString(c);
      HBaseValueMeta newMeta = null;
      if (decodedType.equalsIgnoreCase("Float")) {
        newMeta = new HBaseValueMeta(decodedName, 
            ValueMetaInterface.TYPE_NUMBER, -1, -1);
        
        // While passing through Kettle this will be represented
        // as a double
        newMeta.setIsLongOrDouble(false);
      } else if (decodedType.equalsIgnoreCase("Double")) {
        newMeta = new HBaseValueMeta(decodedName, 
            ValueMetaInterface.TYPE_NUMBER, -1, -1);
      } else if (decodedType.equalsIgnoreCase("String")) {
        newMeta = new HBaseValueMeta(decodedName, 
            ValueMetaInterface.TYPE_STRING, -1, -1);
      } else if (decodedType.toLowerCase().startsWith("date")) {
        newMeta = new HBaseValueMeta(decodedName, 
            ValueMetaInterface.TYPE_DATE, -1, -1);
        
/*        // check for a date format
        if (decodedType.length() > 4) {
          String format = decodedType.
            substring(4, decodedType.length()).trim();
          newMeta.setConversionMask(format);
        } else {
          newMeta.setConversionMask(null);
        } */
        
      } else if (decodedType.equalsIgnoreCase("Boolean")) {
        newMeta = new HBaseValueMeta(decodedName, 
            ValueMetaInterface.TYPE_BOOLEAN, -1, -1);
      } else if (decodedType.equalsIgnoreCase("Integer")) {
        newMeta = new HBaseValueMeta(decodedName, 
            ValueMetaInterface.TYPE_INTEGER, -1, -1);
        
        // Integer in the mapping is really an integer (not a long
        // as Kettle uses internally)
        newMeta.setIsLongOrDouble(false);
      } else if (decodedType.equalsIgnoreCase("Long")) { 
        newMeta = new HBaseValueMeta(decodedName, 
            ValueMetaInterface.TYPE_INTEGER, -1, -1);
      } else if (decodedType.equalsIgnoreCase("BigNumber")) {
        newMeta = new HBaseValueMeta(decodedName, 
            ValueMetaInterface.TYPE_BIGNUMBER, -1, -1);        
      } else if (decodedType.equalsIgnoreCase("Serializable")) {
        newMeta = new HBaseValueMeta(decodedName, 
            ValueMetaInterface.TYPE_SERIALIZABLE, -1, -1);
      } else if (decodedType.equalsIgnoreCase("Binary")) {
        newMeta = new HBaseValueMeta(decodedName, 
            ValueMetaInterface.TYPE_BINARY, -1, -1);
      } else if (decodedType.startsWith("{") && decodedType.endsWith("}")) {
        newMeta = new HBaseValueMeta(decodedName, 
            ValueMetaInterface.TYPE_STRING, -1, -1);
        
        // parse nominal/indexed values
/*        String[] labels = decodedType.replace("{", "").replace("}", "").split(",");
        if (labels.length < 1) {
          throw new IOException("Indexed/nominal type must have at least one " +
          		"label declared");
        }
        for (int i = 0; i < labels.length; i++) {
          labels[i] = labels[i].trim();
        } */
        Object[] labels = null;
        try {
          labels = HBaseValueMeta.stringIndexListToObjects(decodedType);
        } catch (IllegalArgumentException ex) {
          throw new IOException("Indexed/nominal type must have at least one " +
          "label declared");
        }
        newMeta.setIndex(labels);
        newMeta.setStorageType(ValueMetaInterface.STORAGE_TYPE_INDEXED);
      } else {
        throw new IOException("Unknown column type : \"" + decodedType + "\"");
      }
    
      newMeta.setTableName(tableName);
      newMeta.setMappingName(mappingName);
      // check that this one doesn't have the same name as the key!
      if (resultMapping.getKeyName().equals(newMeta.getAlias())) {
        throw new IOException("Error in mapping. Column \"" + newMeta.getAlias() 
            + "\" has the same name as the table key (" + resultMapping.getKeyName() 
            +")");
      }
      
      resultCols.put(newMeta.getAlias(), newMeta);            
    }
    
    resultMapping.setMappedColumns(resultCols);
    
    mappingTable.close();
    
    return resultMapping;
  }
  
  private static String pad(String source, String padChar, 
      int length, boolean leftPad) {
    StringBuffer temp = new StringBuffer();
    length = length - source.length();
    
    if (length > 0) {
      if (leftPad) {
        for (int i = 0; i< length; i++) {
          temp.append(padChar);
        }
        temp.append(source);
      } else {
        temp.append(source);
        for (int i = 0; i< length; i++) {
          temp.append(padChar);
        }
      }
    } else {
      // truncate over-long strings
      temp.append(source.subSequence(0, length));
    }
    return temp.toString();
  }
  
  /**
   * Main method for testing this class. Provides a very simple command-line
   * interface
   * 
   * @param args command line arguments
   */
  public static void main(String[] args) {
    try {
      String tableName = "MarksTestTable";
      String mappingName = "MarksTestMapping";
      MappingAdmin admin = new MappingAdmin();
      Configuration connection = new Configuration();
 //     connection.addResource(new URL("file:/Users/mhall/Documents/Pentaho/dev/HBase/hbase-0.90.1/conf_cloudera/hbase-default.xml"));
      connection.addResource("hbase-default.xml");
//      connection.addResource(new URL("file:/Users/mhall/Documents/Pentaho/dev/HBase/hbase-0.90.1/conf_cloudera/hbase-site.xml"));
      connection.addResource("hbase-site.xml");
      admin.setConnection(connection);
 //     admin.setUseDefaultConnection();
      //admin.createTestTable();
      
      if (args.length == 0 || args[0].equalsIgnoreCase("-h") || args[0].endsWith("help")) {
        System.err.println("Commands:\n");
        System.err.println("\tlist tables - lists all tables with one or " +
        		"more mappings defined");
        System.err.println("\tlist mappings for table <tableName> - list all " +
        		"mappings for table <tableName>");
        System.err.println("\tdescribe mapping <mappingName> on table " +
        		"<tableName> - print out meta data for mapping " +
        		"<mapping name> on table <tableName");

        System.exit(0);
      }
 
      // create test mapping or test table (according to test mapping)
      if (args[0].equalsIgnoreCase("create")) {
        if (args.length > 1 && args[1].equalsIgnoreCase("test")) {
         
          System.out.println("Creating a test table...");
          admin.createTestTable();
          
          return;
        }
        
        // otherwise create the test mapping in the mapping table
        String keyName = "MyKey";
        Mapping.KeyType keyType = Mapping.KeyType.LONG;
        Mapping testMapping = 
          new Mapping(tableName, mappingName, keyName, keyType);
        
        String family1 = "Family1";
        String colA = "first_string_column";
        String combined = family1 + HBaseValueMeta.SEPARATOR + colA 
          + HBaseValueMeta.SEPARATOR + colA;
        HBaseValueMeta vm = new HBaseValueMeta(combined, ValueMetaInterface.TYPE_STRING, -1, -1);
        vm.setTableName(tableName); vm.setMappingName(mappingName);
        testMapping.addMappedColumn(vm);
        
        String colB = "first_integer_column";
        combined = family1 + HBaseValueMeta.SEPARATOR + colB 
          + HBaseValueMeta.SEPARATOR + colB;
        vm = new HBaseValueMeta(combined, ValueMetaInterface.TYPE_INTEGER, -1, -1);
        vm.setTableName(tableName); vm.setMappingName(mappingName);
        testMapping.addMappedColumn(vm);
        
        String family2 = "Family2";
        String colC = "first_indexed_column";
        combined = family2 + HBaseValueMeta.SEPARATOR + colC 
          + HBaseValueMeta.SEPARATOR + colC;
        vm = new HBaseValueMeta(combined, ValueMetaInterface.TYPE_STRING, -1, -1);
        vm.setTableName(tableName); vm.setMappingName(mappingName);
        vm.setStorageType(ValueMetaInterface.STORAGE_TYPE_INDEXED);
        Object[] vals = {"nomVal1", "nomVal2", "nomVal3"}; 
        vm.setIndex(vals);
        testMapping.addMappedColumn(vm);        

        admin.putMapping(testMapping, false);
      } else if (args[0].equalsIgnoreCase("describe")) {
        String usage = "Usage: describe mapping <mappingName> on table <tableName>";
        if (args.length != 6) {
          System.err.println(usage);
          System.exit(1);
        }
        
        if (!args[1].equalsIgnoreCase("mapping")) {
          System.err.println(usage);
          System.exit(1);
        }
        String mName = args[2].trim();
        if (!args[3].equalsIgnoreCase("on") && !args[4].equalsIgnoreCase("table")) {
          System.err.println(usage);
          System.exit(1);
        }
        String tabName = args[5];
        
        String description = admin.describeMapping(tabName, mName);
        System.out.println(description);
      } else if (args[0].equalsIgnoreCase("list") && args.length == 2) {
        
        if (!args[1].equalsIgnoreCase("tables")) {
          System.err.println("Usage: list tables");
          System.exit(1);
        }
        
        Set<String> tables = admin.getMappedTables();
        System.out.println("Tables with mappings:\n");
        for (String t : tables) {
          System.out.println("\t" + t);
        }
      } else if (args[0].equalsIgnoreCase("list") && args.length > 2) {
        String usage = "Usage: list mappings for table <tableName>";
        if (args.length != 5) {
          System.err.println(usage);
          System.exit(1);
        }
        if (!args[1].equalsIgnoreCase("mappings") || 
            !args[2].equalsIgnoreCase("for") || 
            !args[3].equalsIgnoreCase("table")) {
          System.err.println(usage);
          System.exit(1);
        }

        List<String> mappings = admin.getMappingNames(args[4]);
        System.out.println("Mappings that exist for table \"" + args[4] + "\":\n");
        for (String m : mappings) {
          System.out.println("\t" + m);
        }

      }
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }
}