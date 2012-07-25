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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.hbase.shim.HBaseAdmin;
import org.pentaho.hbase.shim.HBaseBytesUtil;

/**
 * Class for managing a mapping table in HBase. Has routines for creating the
 * mapping table, writing and reading mappings to/from the table and creating a
 * test table for debugging purposes. Also has a rough and ready command line
 * interface. For more information on the structure of a table mapping see
 * org.pentaho.hbase.mapping.Mapping.
 * 
 * @author Mark Hall (mhall[{at]}pentaho{[dot]}com)
 */
public class MappingAdmin {

  /**
   * Configuration object for the connection protected Configuration
   * m_connection;
   */

  /** Admin object */
  protected HBaseAdmin m_admin;

  /** Byte utils */
  protected HBaseBytesUtil m_bytesUtil;

  /** Name of the mapping table (might make this configurable at some stage) */
  protected String m_mappingTableName = "pentaho_mappings";

  /** family name to hold the mapped column meta data in a mapping */
  public static final String COLUMNS_FAMILY_NAME = "columns";

  /**
   * family name to hold the key meta data in a mapping. This meta data will be
   * the same for any mapping defined on the same table
   */
  public static final String KEY_FAMILY_NAME = "key";

  /**
   * Constructor. No conneciton information configured.
   */
  public MappingAdmin() {
    try {
      m_bytesUtil = HBaseAdmin.getBytesUtil();
    } catch (Exception ex) {
      // catastrophic failure if we can't obtain a concrete implementation
      throw new RuntimeException(ex);
    }
  }

  /**
   * Constructor
   * 
   * @param conf a configuration object containing connection information
   * @throws Exception if a problem occurs
   * 
   *           public MappingAdmin(Configuration conf) throws Exception {
   *           setConnection(conf); }
   */

  public MappingAdmin(HBaseAdmin conn) {
    this();
    setConnection(conn);
  }

  /**
   * Set the connection to use
   * 
   * @param con a configuration object containing connection information.
   * @throws Exception if a problem occurs
   * 
   *           public void setConnection(Configuration con) throws Exception {
   *           m_connection = con; m_admin = new HBaseAdmin(m_connection); }
   */

  public void setConnection(HBaseAdmin conn) {
    m_admin = conn;
  }

  /**
   * Just use whatever can be loaded from the classpath for the connection
   * 
   * @throws Exception
   * 
   *           public void setUseDefaultConnection() throws Exception {
   *           m_connection = HBaseConfiguration.create(); m_admin = new
   *           HBaseAdmin(m_connection); }
   */

  /**
   * Get the configuration being used for the connection
   * 
   * @return the configuration encapsulating connection information
   * 
   *         public Configuration getConnection() { return m_connection; }
   */

  public HBaseAdmin getConnection() {
    return m_admin;
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
  public void createTestTable() throws Exception {

    if (m_admin == null) {
      throw new IOException("No connection exists yet!");
    }

    if (m_admin.tableExists("MarksTestTable")) {
      // drop/delete the table and re-create
      m_admin.disableTable("MarksTestTable");
      m_admin.deleteTable("MarksTestTable");
    }

    List<String> colFamilies = new ArrayList<String>();
    colFamilies.add("Family1");
    colFamilies.add("Family2");
    m_admin.createTable("MarksTestTable", colFamilies, null);

    Properties props = new Properties();
    props.setProperty(HBaseAdmin.HTABLE_WRITE_BUFFER_SIZE_KEY, ""
        + (1024 * 1024 * 12));
    m_admin.newTargetTable("MarksTestTable", props);

    // insert 200 test rows of random stuff
    Random r = new Random();
    String[] nomVals = { "nomVal1", "nomVal2", "nomVal3" };
    Date date = new Date();
    Calendar c = new GregorianCalendar();
    c.setTime(date);
    Calendar c2 = new GregorianCalendar();
    c2.set(1970, 2, 1);
    for (long key = -500; key < 20000; key++) {
      m_admin.newTargetTablePut(
          HBaseValueMeta.encodeKeyValue(new Long(key), Mapping.KeyType.LONG),
          false);

      // unsigned (positive) integer column
      m_admin.addColumnToTargetPut("Family1", "first_integer_column", false,
          m_bytesUtil.toBytes((int) key / 10));

      // String column
      m_admin.addColumnToTargetPut("Family1", "first_string_column", false,
          m_bytesUtil.toBytes("StringValue_" + key));

      // have some null values - every 10th row has no value for the indexed
      // column
      if (key % 10L > 0) {
        int index = r.nextInt(3);
        String nomVal = nomVals[index];
        m_admin.addColumnToTargetPut("Family2", "first_indexed_column", false,
            m_bytesUtil.toBytes(nomVal));
      }

      // signed integer column
      double d = r.nextDouble();
      int signedInt = r.nextInt(100);
      if (d < 0.5) {
        signedInt = -signedInt;
      }
      m_admin.addColumnToTargetPut("Family2", ",first_unsigned_int_column",
          false, m_bytesUtil.toBytes(signedInt));

      // unsigned (positive) float column
      float f = r.nextFloat() * 1000.0f;
      m_admin.addColumnToTargetPut("Family2", ",first_unsigned_float_column",
          false, m_bytesUtil.toBytes(f));

      // signed float column
      if (d > 0.5) {
        f = -f;
      }
      m_admin.addColumnToTargetPut("Family2", "first_signed_float_column",
          false, m_bytesUtil.toBytes(f));

      // unsigned double column
      double dd = d * 10000 * r.nextDouble();
      m_admin.addColumnToTargetPut("Family2", "first_unsigned_double_column",
          false, m_bytesUtil.toBytes(dd));

      // signed double
      if (d > 0.5) {
        dd = -dd;
      }
      m_admin.addColumnToTargetPut("Family2", "first_signed_double_column",
          false, m_bytesUtil.toBytes(dd));

      // unsigned long
      long l = r.nextInt(300);
      m_admin.addColumnToTargetPut("Family2", "first_unsigned_long_column",
          false, m_bytesUtil.toBytes(l));

      if (d < 0.5) {
        l = -l;
      }
      m_admin.addColumnToTargetPut("Family2", "first_signed_long_column",
          false, m_bytesUtil.toBytes(l));

      // unsigned date (vals >= 1st Jan 1970)
      c.add(Calendar.DAY_OF_YEAR, 1);

      long longd = c.getTimeInMillis();
      m_admin.addColumnToTargetPut("Family1", "first_unsigned_date_column",
          false, m_bytesUtil.toBytes(longd));

      // signed date (vals < 1st Jan 1970)
      c2.add(Calendar.DAY_OF_YEAR, -1);
      longd = c2.getTimeInMillis();

      m_admin.addColumnToTargetPut("Family1", "first_signed_date_column",
          false, m_bytesUtil.toBytes(longd));

      // boolean column
      String bVal = "";
      if (d < 0.5) {
        bVal = "N";
      } else {
        bVal = "Y";
      }
      m_admin.addColumnToTargetPut("Family1", "first_boolean_column", false,
          m_bytesUtil.toBytes(bVal));

      // serialized objects
      byte[] serialized = HBaseValueMeta.encodeObject(new Double(d));

      m_admin.addColumnToTargetPut("Family1", "first_serialized_column", false,
          serialized);

      // binary (raw bytes)
      byte[] rawStuff = m_bytesUtil.toBytes(5034555);
      m_admin.addColumnToTargetPut("Family1", "first_binary_column", false,
          rawStuff);

      m_admin.executeTargetTablePut();
    }

    m_admin.flushCommitsTargetTable();
    m_admin.closeTargetTable();
  }

  /**
   * Create the mapping table
   * 
   * @throws Exception if there is no connection specified or the mapping table
   *           already exists.
   */
  public void createMappingTable() throws Exception {

    if (m_admin == null) {
      throw new IOException("No connection exists yet!");
    }

    if (m_admin.tableExists(m_mappingTableName)) {
      throw new IOException("Mapping table already exists!");
    }

    List<String> colFamNames = new ArrayList<String>();
    colFamNames.add(COLUMNS_FAMILY_NAME);
    colFamNames.add(KEY_FAMILY_NAME);

    m_admin.createTable(m_mappingTableName, colFamNames, null);
  }

  /**
   * Check to see if the specified mapping name exists for the specified table
   * 
   * @param tableName the name of the table
   * @param mappingName the name of the mapping
   * @return true if the specified mapping exists for the specified table
   * @throws IOException if a problem occurs
   */
  public boolean mappingExists(String tableName, String mappingName)
      throws Exception {

    if (m_admin == null) {
      throw new IOException("No connection exists yet!");
    }

    if (m_admin.tableExists(m_mappingTableName)) {
      m_admin.newSourceTable(m_mappingTableName);

      String compoundKey = tableName + HBaseValueMeta.SEPARATOR + mappingName;

      boolean result = m_admin.sourceTableRowExists(m_bytesUtil
          .toBytes(compoundKey));
      m_admin.closeSourceTable();

      return result;
    }
    return false;
  }

  /**
   * Get a list of tables that have mappings. List will be empty if there are no
   * mappings defined yet.
   * 
   * @return a list of tables that have mappings.
   * @throws IOException if something goes wrong
   */
  public Set<String> getMappedTables() throws Exception {

    if (m_admin == null) {
      throw new IOException("No connection exists yet!");
    }

    HashSet<String> tableNames = new HashSet<String>();
    if (m_admin.tableExists(m_mappingTableName)) {

      m_admin.newSourceTable(m_mappingTableName);
      m_admin.newSourceTableScan(null, null, 10);

      m_admin.executeSourceTableScan();

      while (m_admin.resultSetNextRow()) {
        byte[] rawKey = m_admin.getResultSetCurrentRowKey();
        String decodedKey = m_bytesUtil.toString(rawKey);

        // extract the table name
        String tableName = decodedKey.substring(0,
            decodedKey.indexOf(HBaseValueMeta.SEPARATOR));
        tableNames.add(tableName.trim());
      }

      m_admin.closeSourceTable();
    }

    return tableNames;
  }

  /**
   * Get a list of mappings for the supplied table name. List will be empty if
   * there are no mappings defined for the table.
   * 
   * @param tableName the table name
   * @return a list of mappings
   * @throws Exception if something goes wrong.
   */
  public List<String> getMappingNames(String tableName) throws Exception {

    if (m_admin == null) {
      throw new IOException("No connection exists yet!");
    }

    List<String> mappingsForTable = new ArrayList<String>();
    if (m_admin.tableExists(m_mappingTableName)) {
      m_admin.newSourceTable(m_mappingTableName);
      m_admin.newSourceTableScan(null, null, 10);
      m_admin.executeSourceTableScan();

      while (m_admin.resultSetNextRow()) {
        byte[] rowKey = m_admin.getResultSetCurrentRowKey();
        String decodedKey = m_bytesUtil.toString(rowKey);
        String tableN = decodedKey.substring(0,
            decodedKey.indexOf(HBaseValueMeta.SEPARATOR)).trim();

        if (tableName.equals(tableN)) {
          // extract out the mapping name
          String mappingName = decodedKey.substring(
              decodedKey.indexOf(HBaseValueMeta.SEPARATOR) + 1,
              decodedKey.length());
          mappingsForTable.add(mappingName);
        }
      }

      m_admin.closeSourceTable();
    }

    return mappingsForTable;
  }

  /**
   * Delete a mapping from the mapping table
   * 
   * @param tableName name of the table in question
   * @param mappingName name of the mapping in question
   * @return true if the named mapping for the named table was deleted
   *         successfully; false if the mapping table does not exist or the
   *         named mapping for the named table does not exist in the mapping
   *         table
   * @throws Exception if a problem occurs during deletion
   */
  public boolean deleteMapping(String tableName, String mappingName)
      throws Exception {
    String compoundKey = tableName + HBaseValueMeta.SEPARATOR + mappingName;

    if (!m_admin.tableExists(m_mappingTableName)) {
      // create the mapping table
      createMappingTable();
      return false; // no mapping table so nothing to delete!
    }

    if (m_admin.isTableDisabled(m_mappingTableName)) {
      m_admin.enableTable(m_mappingTableName);
    }

    boolean mappingExists = mappingExists(tableName, mappingName);
    if (!mappingExists) {
      return false; // mapping doesn't seem to exist
    }

    m_admin.newTargetTable(m_mappingTableName, null);
    byte[] key = m_bytesUtil.toBytes(compoundKey);

    m_admin.executeTargetTableDelete(key);

    return true;
  }

  /**
   * Delete a mapping from the mapping table
   * 
   * @param theMapping the mapping to delete
   * @return true if the mapping was deleted successfully; false if the mapping
   *         table does not exist or the suppied mapping does not exist in the
   *         mapping table
   * @throws Exception if a problem occurs during deletion
   */
  public boolean deleteMapping(Mapping theMapping) throws Exception {
    String tableName = theMapping.getTableName();
    String mappingName = theMapping.getMappingName();

    return deleteMapping(tableName, mappingName);
  }

  /**
   * Add a mapping into the mapping table. Can either throw an IOException if
   * the mapping already exists in the table, or overwrite (delete and then add)
   * it if the overwrite parameter is set to true.
   * 
   * @param tableName
   * @param mappingName
   * @param mapping
   * @param overwrite
   * @throws IOException
   */
  public void putMapping(Mapping theMapping, boolean overwrite)
      throws Exception {

    String tableName = theMapping.getTableName();
    String mappingName = theMapping.getMappingName();
    Map<String, HBaseValueMeta> mapping = theMapping.getMappedColumns();
    String keyName = theMapping.getKeyName();
    Mapping.KeyType keyType = theMapping.getKeyType();
    boolean isTupleMapping = theMapping.isTupleMapping();
    String tupleFamilies = theMapping.getTupleFamilies();

    if (m_admin == null) {
      throw new IOException("No connection exists yet!");
    }

    String compoundKey = tableName + HBaseValueMeta.SEPARATOR + mappingName;

    if (!m_admin.tableExists(m_mappingTableName)) {

      // create the mapping table
      createMappingTable();
    }

    m_admin.newTargetTable(m_mappingTableName, null);

    if (m_admin.isTableDisabled(m_mappingTableName)) {
      m_admin.enableTable(m_mappingTableName);
    }

    boolean mappingExists = mappingExists(tableName, mappingName);
    if (mappingExists && !overwrite) {
      throw new IOException("The mapping \"" + mappingName
          + "\" already exists " + "for table \"" + tableName + "\"");
    }

    if (mappingExists) {
      // delete it first before adding the new one
      m_admin.executeTargetTableDelete(m_bytesUtil.toBytes(compoundKey));
    }

    // add the new mapping
    m_admin.newTargetTablePut(m_bytesUtil.toBytes(compoundKey), true);

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

      // check for nominal/indexed
      if (vm.getStorageType() == ValueMetaInterface.STORAGE_TYPE_INDEXED
          && vm.isString()) {
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
      m_admin.addColumnToTargetPut(family, qualifier, false,
          m_bytesUtil.toBytes(valueType));
    }

    // now do the key
    family = KEY_FAMILY_NAME;
    String qualifier = keyName;

    // indicate that this is a tuple mapping by appending SEPARATOR to the name
    // of the key + any specified column families to extract from
    if (isTupleMapping) {
      qualifier += HBaseValueMeta.SEPARATOR;
      if (!Const.isEmpty(tupleFamilies)) {
        qualifier += tupleFamilies;
      }
    }
    String valueType = keyType.toString();

    m_admin.addColumnToTargetPut(family, qualifier, false,
        m_bytesUtil.toBytes(valueType));

    // add the row
    m_admin.executeTargetTablePut();
    m_admin.flushCommitsTargetTable();
    m_admin.closeTargetTable();
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
      throws Exception {

    return describeMapping(getMapping(tableName, mappingName));
  }

  /**
   * Returns a textual description of a mapping
   * 
   * @param aMapping the mapping
   * @return a textual description of the supplied mapping object
   * @throws IOException if a problem occurs
   */
  public String describeMapping(Mapping aMapping) throws IOException {

    return aMapping.toString();
  }

  /**
   * Get a mapping for the specified table under the specified mapping name
   * 
   * @param tableName the name of the table
   * @param mappingName the name of the mapping to get for the table
   * @return a mapping for the supplied table
   * @throws Exception if a mapping by the given name does not exist for the
   *           given table
   */
  public Mapping getMapping(String tableName, String mappingName)
      throws Exception {

    if (m_admin == null) {
      throw new IOException("No connection exists yet!");
    }

    String compoundKey = tableName + HBaseValueMeta.SEPARATOR + mappingName;

    if (!m_admin.tableExists(m_mappingTableName)) {

      // create the mapping table
      createMappingTable();

      throw new IOException("Mapping \"" + compoundKey + "\" does not exist!");
    }

    m_admin.newSourceTable(m_mappingTableName);
    m_admin.newSourceTableScan(m_bytesUtil.toBytes(compoundKey),
        m_bytesUtil.toBytes(compoundKey), 10);
    m_admin.executeSourceTableScan();

    if (!m_admin.resultSetNextRow()) {
      throw new IOException("Mapping \"" + compoundKey + "\" does not exist!");
    }

    NavigableMap<byte[], byte[]> colsInKeyFamily = m_admin
        .getResultSetCurrentRowFamilyMap(KEY_FAMILY_NAME);

    Set<byte[]> keyCols = colsInKeyFamily.keySet();
    // should only be one key defined!!
    if (keyCols.size() != 1) {
      throw new IOException("Mapping \"" + compoundKey
          + "\" has more than one key defined!");
    }

    byte[] keyNameB = keyCols.iterator().next();
    String decodedKeyName = m_bytesUtil.toString(keyNameB);
    byte[] keyTypeB = colsInKeyFamily.get(keyNameB);
    String decodedKeyType = m_bytesUtil.toString(keyTypeB);
    Mapping.KeyType keyType = null;

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

    String tupleFamilies = "";
    boolean isTupleMapping = false;
    if (decodedKeyName.indexOf(',') > 0) {

      isTupleMapping = true;

      if (decodedKeyName.indexOf(',') != decodedKeyName.length() - 1) {
        tupleFamilies = decodedKeyName.substring(
            decodedKeyName.indexOf(',') + 1, decodedKeyName.length());
      }
      decodedKeyName = decodedKeyName.substring(0, decodedKeyName.indexOf(','));
    }

    Mapping resultMapping = new Mapping(tableName, mappingName, decodedKeyName,
        keyType);
    resultMapping.setTupleMapping(isTupleMapping);
    if (!Const.isEmpty(tupleFamilies)) {
      resultMapping.setTupleFamilies(tupleFamilies);
    }

    Map<String, HBaseValueMeta> resultCols = new TreeMap<String, HBaseValueMeta>();

    // now process the mapping
    NavigableMap<byte[], byte[]> colsInMapping = m_admin
        .getResultSetCurrentRowFamilyMap(COLUMNS_FAMILY_NAME);

    Set<byte[]> colNames = colsInMapping.keySet();

    for (byte[] b : colNames) {
      String decodedName = m_bytesUtil.toString(b);
      byte[] c = colsInMapping.get(b);
      if (c == null) {
        throw new IOException("No type declaration for column \"" + decodedName
            + "\"");
      }

      String decodedType = m_bytesUtil.toString(c);

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
        newMeta = new HBaseValueMeta(decodedName, ValueMetaInterface.TYPE_DATE,
            -1, -1);
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

        Object[] labels = null;
        try {
          labels = HBaseValueMeta.stringIndexListToObjects(decodedType);
        } catch (IllegalArgumentException ex) {
          throw new IOException("Indexed/nominal type must have at least one "
              + "label declared");
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
        throw new IOException("Error in mapping. Column \""
            + newMeta.getAlias() + "\" has the same name as the table key ("
            + resultMapping.getKeyName() + ")");
      }

      resultCols.put(newMeta.getAlias(), newMeta);
    }

    resultMapping.setMappedColumns(resultCols);

    m_admin.closeSourceTable();

    return resultMapping;
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
      admin.setConnection(HBaseAdmin.createHBaseAdmin());

      if (args.length == 0 || args[0].equalsIgnoreCase("-h")
          || args[0].endsWith("help")) {
        System.err.println("Commands:\n");
        System.err.println("\tlist tables - lists all tables with one or "
            + "more mappings defined");
        System.err.println("\tlist mappings for table <tableName> - list all "
            + "mappings for table <tableName>");
        System.err.println("\tdescribe mapping <mappingName> on table "
            + "<tableName> - print out meta data for mapping "
            + "<mapping name> on table <tableName");

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
        Mapping testMapping = new Mapping(tableName, mappingName, keyName,
            keyType);

        String family1 = "Family1";
        String colA = "first_string_column";
        String combined = family1 + HBaseValueMeta.SEPARATOR + colA
            + HBaseValueMeta.SEPARATOR + colA;
        HBaseValueMeta vm = new HBaseValueMeta(combined,
            ValueMetaInterface.TYPE_STRING, -1, -1);
        vm.setTableName(tableName);
        vm.setMappingName(mappingName);
        testMapping.addMappedColumn(vm, false);

        String colB = "first_integer_column";
        combined = family1 + HBaseValueMeta.SEPARATOR + colB
            + HBaseValueMeta.SEPARATOR + colB;
        vm = new HBaseValueMeta(combined, ValueMetaInterface.TYPE_INTEGER, -1,
            -1);
        vm.setTableName(tableName);
        vm.setMappingName(mappingName);
        testMapping.addMappedColumn(vm, false);

        String family2 = "Family2";
        String colC = "first_indexed_column";
        combined = family2 + HBaseValueMeta.SEPARATOR + colC
            + HBaseValueMeta.SEPARATOR + colC;
        vm = new HBaseValueMeta(combined, ValueMetaInterface.TYPE_STRING, -1,
            -1);
        vm.setTableName(tableName);
        vm.setMappingName(mappingName);
        vm.setStorageType(ValueMetaInterface.STORAGE_TYPE_INDEXED);
        Object[] vals = { "nomVal1", "nomVal2", "nomVal3" };
        vm.setIndex(vals);
        testMapping.addMappedColumn(vm, false);

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
        if (!args[3].equalsIgnoreCase("on")
            && !args[4].equalsIgnoreCase("table")) {
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
        if (!args[1].equalsIgnoreCase("mappings")
            || !args[2].equalsIgnoreCase("for")
            || !args[3].equalsIgnoreCase("table")) {
          System.err.println(usage);
          System.exit(1);
        }

        List<String> mappings = admin.getMappingNames(args[4]);
        System.out.println("Mappings that exist for table \"" + args[4]
            + "\":\n");
        for (String m : mappings) {
          System.out.println("\t" + m);
        }

      }
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }
}
