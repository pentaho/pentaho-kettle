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

package org.pentaho.hbase.shim.fake;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Properties;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.hadoop.shim.ShimVersion;
import org.pentaho.hbase.shim.api.ColumnFilter;
import org.pentaho.hbase.shim.api.HBaseValueMeta;
import org.pentaho.hbase.shim.common.CommonHBaseBytesUtil;
import org.pentaho.hbase.shim.spi.HBaseBytesUtilShim;
import org.pentaho.hbase.shim.spi.HBaseConnection;

import org.pentaho.hbase.shim.common.CommonHBaseBytesUtil;

/**
 * Implementation of HBaseConnection that partially "simulates" a real HBase
 * instance. Used for unit testing the HBase steps and supporting classes.
 * 
 * @author Mark Hall (mhall{[at]}pentaho{[dot]}com)
 */
public class FakeHBaseConnection extends HBaseConnection {

  public static class BytesComparator implements Comparator<byte[]> {
    /**
     * Lexographically compare two arrays.
     * 
     * @param buffer1 left operand
     * @param buffer2 right operand
     * @param offset1 Where to start comparing in the left buffer
     * @param offset2 Where to start comparing in the right buffer
     * @param length1 How much to compare from the left buffer
     * @param length2 How much to compare from the right buffer
     * @return 0 if equal, < 0 if left is less than right, etc.
     */
    public int compareTo(byte[] buffer1, int offset1, int length1,
        byte[] buffer2, int offset2, int length2) {
      int end1 = offset1 + length1;
      int end2 = offset2 + length2;
      for (int i = offset1, j = offset2; i < end1 && j < end2; i++, j++) {
        int a = (buffer1[i] & 0xff);
        int b = (buffer2[j] & 0xff);
        if (a != b) {
          return a - b;
        }
      }
      return length1 - length2;
    }

    /**
     * @param left left operand
     * @param right right operand
     * @return 0 if equal, < 0 if left is less than right, etc.
     */
    public int compareTo(final byte[] left, final byte[] right) {
      return compareTo(left, 0, left.length, right, 0, right.length);
    }

    public int compare(byte[] left, byte[] right) {
      return compareTo(left, right);
    }
  }

  protected HBaseBytesUtilShim m_bytesUtil;

  protected class FakeTable {
    public String m_tableName;

    protected Set<String> m_families = new HashSet<String>();
    protected boolean m_enabled;
    protected boolean m_available;

    // row key -> family map -> column map - > timestamp map
    public NavigableMap<byte[], NavigableMap<byte[], NavigableMap<byte[], NavigableMap<Long, byte[]>>>> m_table;

    public FakeTable(List<String> families) {
      m_table = new TreeMap<byte[], NavigableMap<byte[], NavigableMap<byte[], NavigableMap<Long, byte[]>>>>(
          new BytesComparator());

      for (String fam : families) {
        m_families.add(fam);
      }
      m_enabled = true;
      m_available = true;
    }

    public String getName() {
      return m_tableName;
    }

    public boolean getEnabled() {
      return m_enabled;
    }

    public void setEnabled(boolean enabled) {
      m_enabled = enabled;
    }

    public void setAvailable(boolean avail) {
      m_available = avail;
    }

    public boolean getAvailable() {
      return m_available;
    }

    public List<String> getFamilies() {
      List<String> fams = new ArrayList<String>();
      for (String f : m_families) {
        fams.add(f);
      }
      return fams;
    }

    public Result get(byte[] rowKey) {
      NavigableMap<byte[], NavigableMap<byte[], NavigableMap<Long, byte[]>>> row = m_table
          .get(rowKey);

      if (row == null) {
        return null;
      }

      return new Result(rowKey, row);
    }

    public void put(Put toPut) {
      byte[] key = toPut.getKey();
      List<Col> colsToPut = toPut.getColumns();

      NavigableMap<byte[], NavigableMap<byte[], NavigableMap<Long, byte[]>>> row = m_table
          .get(key);
      if (row == null) {
        row = new TreeMap<byte[], NavigableMap<byte[], NavigableMap<Long, byte[]>>>(
            new BytesComparator());
        m_table.put(key, row);
      }

      for (Col c : colsToPut) {
        NavigableMap<byte[], NavigableMap<Long, byte[]>> colsForFam = row
            .get(c.m_colFamName);
        if (colsForFam == null) {
          colsForFam = new TreeMap<byte[], NavigableMap<Long, byte[]>>(
              new BytesComparator());
          row.put(c.m_colFamName, colsForFam);
        }

        NavigableMap<Long, byte[]> valsForCol = colsForFam.get(c.m_colName);
        if (valsForCol == null) {
          valsForCol = new TreeMap<Long, byte[]>();
          colsForFam.put(c.m_colName, valsForCol);
        }

        Long ts = new Long(System.currentTimeMillis());
        valsForCol.put(ts, c.m_value);
      }
    }

    public SortedMap<byte[], NavigableMap<byte[], NavigableMap<byte[], NavigableMap<Long, byte[]>>>> getRows(
        byte[] startKey, byte[] stopKey) {
      SortedMap<byte[], NavigableMap<byte[], NavigableMap<byte[], NavigableMap<Long, byte[]>>>> subMap = null;
      if (startKey == null && stopKey == null) {
        return m_table; // full table
      }

      if (stopKey == null) {
        Map.Entry<byte[], NavigableMap<byte[], NavigableMap<byte[], NavigableMap<Long, byte[]>>>> lastE = m_table
            .lastEntry();
        byte[] upperKey = lastE.getKey();

        // with no stop key specified we return the last row inclusive
        subMap = m_table.subMap(startKey, true, upperKey, true);
      } else {
        BytesComparator comp = new BytesComparator();
        if (comp.compare(startKey, stopKey) == 0) {
          subMap = m_table.subMap(startKey, true, stopKey, true);
        } else {
          subMap = m_table.subMap(startKey, stopKey);
        }
      }

      return subMap;
    }

    public void deleteRow(byte[] rowKey) {
      m_table.remove(rowKey);
    }
  }

  protected class Col {
    protected byte[] m_colFamName;
    protected byte[] m_colName;
    protected byte[] m_value;

    public Col(byte[] colFamName, byte[] colName, byte[] value) {
      m_colFamName = colFamName;
      m_colName = colName;
      m_value = value;
    }

    public Col(byte[] colFamName, byte[] colName) {
      this(colFamName, colName, null);
    }
  }

  protected class Put {
    protected byte[] m_key;

    protected List<Col> m_cols = new ArrayList<Col>();

    public Put(byte[] key) {
      m_key = key;
    }

    public void addColumn(byte[] colFamName, byte[] colName, byte[] colVal) {
      m_cols.add(new Col(colFamName, colName, colVal));
    }

    public byte[] getKey() {
      return m_key;
    }

    public List<Col> getColumns() {
      return m_cols;
    }
  }

  protected class Scan {
    protected byte[] m_startKey; // inclusive
    protected byte[] m_stopKey; // exclusive

    protected List<Col> m_cols = new ArrayList<Col>();

    public Scan() {
      // full table scan
    }

    public Scan(byte[] startKey) {
      m_startKey = startKey;
    }

    public Scan(byte[] startKey, byte[] stopKey) {
      m_startKey = startKey;
      m_stopKey = stopKey;
    }

    public void addColumn(byte[] colFamName, byte[] colName) {
      m_cols.add(new Col(colFamName, colName));
    }

    public List<Col> getColumns() {
      return m_cols;
    }

    public ResultScanner getScanner(String tableName) {
      FakeTable table = m_db.get(tableName);
      if (table == null) {
        return null;
      }

      SortedMap<byte[], NavigableMap<byte[], NavigableMap<byte[], NavigableMap<Long, byte[]>>>> subMap = table
          .getRows(m_startKey, m_stopKey);

      return new ResultScanner(this, subMap);
    }

    /**
     * Takes a full row and returns a Result encapsulating a reduced row (i.e.
     * containing only the columns specified for this scan). If no columns are
     * specified then the full row is encapsulated in the Result.
     * 
     * @param rowKey the key of the full row
     * @param fullRow the row itself
     * @return
     */
    public Result columnLimitedRow(
        byte[] rowKey,
        NavigableMap<byte[], NavigableMap<byte[], NavigableMap<Long, byte[]>>> fullRow) {

      if (getColumns() == null || getColumns().size() == 0) {
        return new Result(rowKey, fullRow);
      }

      NavigableMap<byte[], NavigableMap<byte[], NavigableMap<Long, byte[]>>> colLimited = new TreeMap<byte[], NavigableMap<byte[], NavigableMap<Long, byte[]>>>(
          new BytesComparator());

      for (Col col : m_cols) {
        // family first - look for this in the full row
        NavigableMap<byte[], NavigableMap<Long, byte[]>> colsForFam = fullRow
            .get(col.m_colFamName);

        if (colsForFam != null) {
          // now look for the column
          NavigableMap<Long, byte[]> theCol = colsForFam.get(col.m_colName);

          if (theCol != null) {
            // now add it
            NavigableMap<byte[], NavigableMap<Long, byte[]>> resultCols = colLimited
                .get(col.m_colFamName);
            if (resultCols == null) {
              resultCols = new TreeMap<byte[], NavigableMap<Long, byte[]>>(
                  new BytesComparator());
              // store this new map of columns in the family map
              colLimited.put(col.m_colFamName, resultCols);
            }

            // add the column itself to this family's map of columns
            resultCols.put(col.m_colName, theCol);
          }
        }
      }

      return new Result(rowKey, colLimited);
    }
  }

  protected class Result {
    protected byte[] m_rowKey;
    protected NavigableMap<byte[], NavigableMap<byte[], NavigableMap<Long, byte[]>>> m_row;

    public Result(
        byte[] rowKey,
        NavigableMap<byte[], NavigableMap<byte[], NavigableMap<Long, byte[]>>> row) {
      m_rowKey = rowKey;
      m_row = row;
    }

    /**
     * Return the row key
     * 
     * @return the row key
     */
    public byte[] getRow() {
      return m_rowKey;
    }

    public byte[] getValue(byte[] colFam, byte[] colName) {
      NavigableMap<byte[], NavigableMap<Long, byte[]>> colMapForFam = m_row
          .get(colFam);
      if (colMapForFam == null) {
        return null;
      }

      NavigableMap<Long, byte[]> versionsOfCol = colMapForFam.get(colName);
      if (versionsOfCol == null) {
        return null;
      }

      return versionsOfCol.lastEntry().getValue();
    }

    public NavigableMap<byte[], NavigableMap<byte[], NavigableMap<Long, byte[]>>> getMap() {
      return m_row;
    }

    public NavigableMap<byte[], byte[]> getFamilyMap(byte[] colFamily) {
      NavigableMap<byte[], NavigableMap<Long, byte[]>> famMap = m_row
          .get(colFamily);

      if (famMap == null) {
        return null;
      }

      TreeMap<byte[], byte[]> famMapLatestVals = new TreeMap<byte[], byte[]>(
          new BytesComparator());
      Set<Map.Entry<byte[], NavigableMap<Long, byte[]>>> es = famMap.entrySet();
      for (Map.Entry<byte[], NavigableMap<Long, byte[]>> e : es) {
        famMapLatestVals.put(e.getKey(), e.getValue().lastEntry().getValue());
      }

      return famMapLatestVals;
    }
  }

  protected class ResultScanner {
    protected Scan m_scan;
    protected SortedMap<byte[], NavigableMap<byte[], NavigableMap<byte[], NavigableMap<Long, byte[]>>>> m_rows;
    protected Iterator<Entry<byte[], NavigableMap<byte[], NavigableMap<byte[], NavigableMap<Long, byte[]>>>>> m_rowIterator;

    public ResultScanner(
        Scan scan,
        SortedMap<byte[], NavigableMap<byte[], NavigableMap<byte[], NavigableMap<Long, byte[]>>>> rows) {
      m_scan = scan;
      m_rows = rows;
      m_rowIterator = m_rows.entrySet().iterator();
    }

    public Result next() {
      if (!m_rowIterator.hasNext()) {
        return null;
      }

      Entry<byte[], NavigableMap<byte[], NavigableMap<byte[], NavigableMap<Long, byte[]>>>> nextR = m_rowIterator
          .next();

      if (nextR == null) {
        return null;
      }

      Result r = m_scan.columnLimitedRow(nextR.getKey(), nextR.getValue());

      return r;
    }
  }

  protected Map<String, FakeTable> m_db = new HashMap<String, FakeTable>();

  protected String m_sourceTable;
  protected String m_targetTable;
  protected Scan m_sourceScan;
  protected Put m_currentTargetPut;
  protected ResultScanner m_resultSet;
  protected Result m_currentResultSetRow;

  public FakeHBaseConnection() {
    try {
      getBytesUtil();
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  @Override
  public HBaseBytesUtilShim getBytesUtil() throws Exception {
    if (m_bytesUtil == null) {
      m_bytesUtil = new CommonHBaseBytesUtil();
    }

    return m_bytesUtil;
  }

  public ShimVersion getVersion() {
    return new ShimVersion(1, 0);
  }

  @Override
  public void addColumnFilterToScan(ColumnFilter arg0, HBaseValueMeta arg1,
      VariableSpace arg2, boolean arg3) throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void addColumnToScan(String colFamilyName, String colName,
      boolean colNameIsBinary) throws Exception {
    checkSourceScan();

    m_sourceScan.addColumn(
        m_bytesUtil.toBytes(colFamilyName),
        (colNameIsBinary) ? m_bytesUtil.toBytesBinary(colName) : m_bytesUtil
            .toBytes(colName));
  }

  @Override
  public void addColumnToTargetPut(String columnFamily, String columnName,
      boolean colNameIsBinary, byte[] colValue) throws Exception {
    checkTargetTable();
    checkTargetPut();
    m_currentTargetPut.addColumn(
        m_bytesUtil.toBytes(columnFamily),
        colNameIsBinary ? m_bytesUtil.toBytesBinary(columnName) : m_bytesUtil
            .toBytes(columnName), colValue);
  }

  @Override
  public boolean checkForHBaseRow(Object arg0) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public void checkHBaseAvailable() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void closeSourceResultSet() throws Exception {
    if (m_resultSet != null) {
      m_resultSet = null;
      m_currentResultSetRow = null;
    }

  }

  @Override
  public void closeSourceTable() throws Exception {
    closeSourceResultSet();
    m_sourceTable = null;
  }

  @Override
  public void closeTargetTable() throws Exception {
    m_targetTable = null;
  }

  @Override
  public void configureConnection(Properties connProps, List<String> logMessages)
      throws Exception {
    String defaultConfig = connProps.getProperty(DEFAULTS_KEY);
    String siteConfig = connProps.getProperty(SITE_KEY);
    String zookeeperQuorum = connProps.getProperty(ZOOKEEPER_QUORUM_KEY);
    String zookeeperPort = connProps.getProperty(ZOOKEEPER_PORT_KEY);

    try {
      if (!isEmpty(defaultConfig)) {
        stringToURL(defaultConfig);
      }

      if (!isEmpty(siteConfig)) {
        stringToURL(siteConfig);
      }
    } catch (Exception ex) {
      throw new IllegalArgumentException("Malformed URL");
    }

    if (!isEmpty(zookeeperPort)) {
      try {
        Integer.parseInt(zookeeperPort);
      } catch (NumberFormatException e) {
        if (logMessages != null) {
          logMessages.add("Unable to parse zookeeper port");
        }
      }
    }
  }

  @Override
  public void createTable(String tableName, List<String> colFamilyNames,
      Properties creationProps) throws Exception {

    if (m_db.containsKey(tableName)) {
      throw new Exception("Table already exists!");
    }

    FakeTable ft = new FakeTable(colFamilyNames);
    m_db.put(tableName, ft);
  }

  @Override
  public void deleteTable(String tableName) throws Exception {
    m_db.remove(tableName);
  }

  @Override
  public void disableTable(String tableName) throws Exception {
    if (!m_db.containsKey(tableName)) {
      throw new Exception("Can't disable table - it does not exist!");
    }

    m_db.get(tableName).setEnabled(false);
  }

  @Override
  public void enableTable(String tableName) throws Exception {
    if (!m_db.containsKey(tableName)) {
      throw new Exception("Can't enable table - it does not exist!");
    }

    m_db.get(tableName).setEnabled(true);
  }

  @Override
  public void executeSourceTableScan() throws Exception {
    checkSourceTable();
    checkSourceScan();

    m_resultSet = m_sourceScan.getScanner(m_sourceTable);
  }

  @Override
  public void executeTargetTableDelete(byte[] rowKey) throws Exception {
    checkTargetTable();

    FakeTable table = m_db.get(m_targetTable);
    if (table == null) {
      throw new Exception("Target table is null!!");
    }

    table.deleteRow(rowKey);
  }

  @Override
  public void executeTargetTablePut() throws Exception {
    checkTargetTable();
    checkTargetPut();

    FakeTable table = m_db.get(m_targetTable);
    if (table != null) {
      table.put(m_currentTargetPut);
    } else {
      throw new Exception("Target table doesn't exist!");
    }
  }

  @Override
  public void flushCommitsTargetTable() throws Exception {
  }

  @Override
  public byte[] getResultSetCurrentRowColumnLatest(String colFamilyName,
      String colName, boolean colNameIsBinary) throws Exception {

    return m_currentResultSetRow.getValue(
        m_bytesUtil.toBytes(colFamilyName),
        colNameIsBinary ? m_bytesUtil.toBytesBinary(colName) : m_bytesUtil
            .toBytes(colName));
  }

  @Override
  public NavigableMap<byte[], byte[]> getResultSetCurrentRowFamilyMap(
      String familyName) throws Exception {
    return m_currentResultSetRow.getFamilyMap(m_bytesUtil.toBytes(familyName));
  }

  @Override
  public byte[] getResultSetCurrentRowKey() throws Exception {
    checkSourceScan();
    checkResultSet();
    checkForCurrentResultSetRow();

    return m_currentResultSetRow.getRow();
  }

  @Override
  public NavigableMap<byte[], NavigableMap<byte[], NavigableMap<Long, byte[]>>> getResultSetCurrentRowMap()
      throws Exception {
    return m_currentResultSetRow.getMap();
  }

  @Override
  public byte[] getRowColumnLatest(Object arg0, String arg1, String arg2,
      boolean arg3) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public NavigableMap<byte[], byte[]> getRowFamilyMap(Object aRow, String family)
      throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public byte[] getRowKey(Object arg0) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public NavigableMap<byte[], NavigableMap<byte[], NavigableMap<Long, byte[]>>> getRowMap(
      Object arg0) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<String> getTableFamiles(String tableName) throws Exception {
    List<String> families = new ArrayList<String>();
    FakeTable tab = m_db.get(tableName);
    if (tab != null) {
      families = tab.getFamilies();
    }
    return families;
  }

  @Override
  public boolean isImmutableBytesWritable(Object arg0) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean isTableAvailable(String tableName) throws Exception {
    if (!m_db.containsKey(tableName)) {
      return false; // not available if it doesn't exist!
    }

    return m_db.get(tableName).getAvailable();
  }

  @Override
  public boolean isTableDisabled(String tableName) throws Exception {
    if (!m_db.containsKey(tableName)) {
      return true; // table is disabled if it doesn't exist!
    }

    return m_db.get(tableName).getEnabled();
  }

  @Override
  public List<String> listTableNames() throws Exception {
    List<String> names = new ArrayList<String>();

    for (String tabName : m_db.keySet()) {
      names.add(tabName);
    }

    return names;
  }

  @Override
  public void newSourceTable(String tableName) throws Exception {
    closeSourceTable();

    if (m_db.get(tableName) == null) {
      throw new Exception("Source table " + tableName + " does not exist!");
    }

    m_sourceTable = tableName;
  }

  @Override
  public void newSourceTableScan(byte[] keyLowerBound, byte[] keyUpperBound,
      int cacheSize) throws Exception {
    checkSourceTable();
    // checkSourceResultSet();

    m_sourceScan = new Scan(keyLowerBound, keyUpperBound);
  }

  @Override
  public void newTargetTable(String tableName, Properties arg1)
      throws Exception {
    closeTargetTable();

    m_targetTable = tableName;

  }

  @Override
  public void newTargetTablePut(byte[] key, boolean writeToWAL)
      throws Exception {
    checkTargetTable();
    m_currentTargetPut = new Put(key);
  }

  @Override
  public boolean resultSetNextRow() throws Exception {
    checkResultSet();
    m_currentResultSetRow = m_resultSet.next();

    return (m_currentResultSetRow != null);
  }

  @Override
  public boolean sourceTableRowExists(byte[] rowKey) throws Exception {
    checkSourceTable();
    FakeTable tab = m_db.get(m_sourceTable);
    if (tab == null) {
      return false;
    }

    if (tab.get(rowKey) == null) {
      return false;
    }

    return true;
  }

  @Override
  public boolean tableExists(String tableName) throws Exception {
    return (m_db.get(tableName) != null);
  }

  @Override
  public boolean targetTableIsAutoFlush() throws Exception {
    checkTargetTable();

    return true;
  }

  protected void checkSourceTable() throws Exception {
    if (m_sourceTable == null) {
      throw new Exception("No source table has been specified!");
    }
  }

  protected void checkTargetTable() throws Exception {
    if (m_targetTable == null) {
      throw new Exception("No target table has been specified!");
    }
  }

  protected void checkTargetPut() throws Exception {
    if (m_currentTargetPut == null) {
      throw new Exception("No target put configured!");
    }
  }

  protected void checkSourceScan() throws Exception {
    if (m_sourceScan == null) {
      throw new Exception("No source scan defined!");
    }
  }

  protected void checkResultSet() throws Exception {
    if (m_resultSet == null) {
      throw new Exception("No current result set!");
    }
  }

  protected void checkForCurrentResultSetRow() throws Exception {
    if (m_currentResultSetRow == null) {
      throw new Exception("No current resut set row available!");
    }
  }
}
