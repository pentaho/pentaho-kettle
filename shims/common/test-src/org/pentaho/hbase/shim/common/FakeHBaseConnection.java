package org.pentaho.hbase.shim.common;

import java.util.ArrayList;
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

import org.apache.hadoop.hbase.util.Bytes;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.hadoop.shim.ShimVersion;
import org.pentaho.hbase.shim.api.ColumnFilter;
import org.pentaho.hbase.shim.api.HBaseValueMeta;
import org.pentaho.hbase.shim.spi.HBaseBytesUtilShim;
import org.pentaho.hbase.shim.spi.HBaseConnection;

public class FakeHBaseConnection extends HBaseConnection {

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
          Bytes.BYTES_COMPARATOR);

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

        subMap = m_table.subMap(startKey, upperKey);
      }

      subMap = m_table.subMap(startKey, stopKey);

      return subMap;
    }
  }

  protected class Scan {
    protected byte[] m_startKey; // inclusive
    protected byte[] m_stopKey; // exclusive

    private class ScanCol {
      protected byte[] m_colFamName;
      protected byte[] m_colName;

      public ScanCol(byte[] colFamName, byte[] colName) {
        m_colFamName = colName;
        m_colName = colName;
      }
    }

    protected List<ScanCol> m_cols = new ArrayList<ScanCol>();

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
      m_cols.add(new ScanCol(colFamName, colName));
    }

    public List<ScanCol> getColumns() {
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

      NavigableMap<byte[], NavigableMap<byte[], NavigableMap<Long, byte[]>>> colLimited = new TreeMap<byte[], NavigableMap<byte[], NavigableMap<Long, byte[]>>>();

      for (ScanCol col : m_cols) {
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
              resultCols = new TreeMap<byte[], NavigableMap<Long, byte[]>>();
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

      TreeMap<byte[], byte[]> famMapLatestVals = new TreeMap<byte[], byte[]>();
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
      Entry<byte[], NavigableMap<byte[], NavigableMap<byte[], NavigableMap<Long, byte[]>>>> nextR = m_rowIterator
          .next();

      if (nextR == null) {
        return null;
      }

      // TODO create new navigable map structure containing only the scan
      // columns
      Result r = m_scan.columnLimitedRow(nextR.getKey(), nextR.getValue());

      return r;
    }
  }

  protected Map<String, FakeTable> m_db = new HashMap<String, FakeTable>();

  protected String m_sourceTable;
  protected String m_targetTable;
  protected Scan m_sourceScan;
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
  public void addColumnToTargetPut(String arg0, String arg1, boolean arg2,
      byte[] arg3) throws Exception {
    // TODO Auto-generated method stub

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
    // TODO Auto-generated method stub

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
    // TODO Auto-generated method stub

  }

  @Override
  public void executeTargetTableDelete(byte[] arg0) throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void executeTargetTablePut() throws Exception {
    // TODO Auto-generated method stub

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
  public NavigableMap<byte[], byte[]> getRowFamilyMap(Object arg0, String arg1)
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
  public List<String> getTableFamiles(String arg0) throws Exception {
    // TODO Auto-generated method stub
    return null;
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
  public void newTargetTablePut(byte[] arg0, boolean arg1) throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public boolean resultSetNextRow() throws Exception {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean sourceTableRowExists(byte[] arg0) throws Exception {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean tableExists(String arg0) throws Exception {
    // TODO Auto-generated method stub
    return false;
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
