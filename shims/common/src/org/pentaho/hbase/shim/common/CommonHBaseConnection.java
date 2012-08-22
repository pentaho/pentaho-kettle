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

package org.pentaho.hbase.shim.common;

import java.net.MalformedURLException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.NavigableMap;
import java.util.Properties;
import java.util.Set;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.CompareFilter;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.RegexStringComparator;
import org.apache.hadoop.hbase.filter.SingleColumnValueFilter;
import org.apache.hadoop.hbase.filter.SubstringComparator;
import org.apache.hadoop.hbase.filter.WritableByteArrayComparable;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.io.hfile.Compression;
import org.apache.hadoop.hbase.regionserver.StoreFile.BloomType;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.hbase.shim.api.ColumnFilter;
import org.pentaho.hbase.shim.api.HBaseValueMeta;
import org.pentaho.hbase.shim.spi.HBaseBytesUtilShim;
import org.pentaho.hbase.shim.spi.HBaseConnection;

/**
 * Concrete implementation for Hadoop 20.x.
 * 
 * @author Mark Hall (mhall{[at]}pentaho{[dot]}com)
 */
public class CommonHBaseConnection extends HBaseConnection {
  private static Class<?> PKG = CommonHBaseConnection.class;

  protected Configuration m_config = null;
  protected org.apache.hadoop.hbase.client.HBaseAdmin m_admin;

  protected HTable m_sourceTable;
  protected Scan m_sourceScan;
  protected ResultScanner m_resultSet;
  protected Result m_currentResultSetRow;
  protected HTable m_targetTable;
  protected Put m_currentTargetPut;

  protected HBaseBytesUtilShim m_bytesUtil;

  public CommonHBaseConnection() {
    try {
      getBytesUtil();
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  @Override
  public void configureConnection(Properties connProps, List<String> logMessages)
      throws Exception {
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
    try {
      String defaultConfig = connProps.getProperty(DEFAULTS_KEY);
      String siteConfig = connProps.getProperty(SITE_KEY);
      String zookeeperQuorum = connProps.getProperty(ZOOKEEPER_QUORUM_KEY);
      String zookeeperPort = connProps.getProperty(ZOOKEEPER_PORT_KEY);

      m_config = new Configuration();
      try {
        if (!isEmpty(defaultConfig)) {
          m_config.addResource(stringToURL(defaultConfig));
        } else {
          m_config.addResource("hbase-default.xml");
        }

        if (!isEmpty(siteConfig)) {
          m_config.addResource(stringToURL(siteConfig));
        } else {
          m_config.addResource("hbase-site.xml");
        }
      } catch (MalformedURLException e) {
        throw new IllegalArgumentException(BaseMessages.getString(PKG,
            "CommonHBaseConnection.Error.MalformedConfigURL"));
      }

      if (!isEmpty(zookeeperQuorum)) {
        m_config.set(ZOOKEEPER_QUORUM_KEY, zookeeperQuorum);
      }

      if (!isEmpty(zookeeperPort)) {
        try {
          int port = Integer.parseInt(zookeeperPort);
          m_config.setInt(ZOOKEEPER_PORT_KEY, port);
        } catch (NumberFormatException e) {
          if (logMessages != null) {
            logMessages.add(BaseMessages.getString(PKG,
                "CommonHBaseConnection.Error.UnableToParseZookeeperPort"));
          }
        }
      }

      m_admin = new org.apache.hadoop.hbase.client.HBaseAdmin(m_config);
    } finally {
      Thread.currentThread().setContextClassLoader(cl);
    }
  }

  @Override
  public HBaseBytesUtilShim getBytesUtil() throws Exception {
    if (m_bytesUtil == null) {
      m_bytesUtil = new CommonHBaseBytesUtil();
    }

    return m_bytesUtil;
  }

  protected void checkConfiguration() throws Exception {
    if (m_admin == null) {
      throw new Exception(BaseMessages.getString(PKG,
          "CommonHBaseConnection.Error.ConnectionHasNotBeenConfigured"));
    }
  }

  @Override
  public void checkHBaseAvailable() throws Exception {
    checkConfiguration();
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
    try {
      org.apache.hadoop.hbase.client.HBaseAdmin.checkHBaseAvailable(m_config);
    } finally {
      Thread.currentThread().setContextClassLoader(cl);
    }
  }

  @Override
  public List<String> listTableNames() throws Exception {
    checkConfiguration();

    HTableDescriptor[] tables = m_admin.listTables();
    List<String> tableNames = new ArrayList<String>();
    for (HTableDescriptor h : tables) {
      tableNames.add(h.getNameAsString());
    }

    return tableNames;
  }

  @Override
  public boolean tableExists(String tableName) throws Exception {
    checkConfiguration();

    return m_admin.tableExists(tableName);
  }

  @Override
  public void disableTable(String tableName) throws Exception {
    checkConfiguration();

    m_admin.disableTable(tableName);
  }

  @Override
  public void enableTable(String tableName) throws Exception {
    checkConfiguration();

    m_admin.enableTable(tableName);
  }

  @Override
  public boolean isTableDisabled(String tableName) throws Exception {
    checkConfiguration();

    return m_admin.isTableDisabled(tableName);
  }

  @Override
  public boolean isTableAvailable(String tableName) throws Exception {
    checkConfiguration();

    return m_admin.isTableAvailable(tableName);
  }

  @Override
  public void deleteTable(String tableName) throws Exception {
    checkConfiguration();

    m_admin.deleteTable(tableName);
  }

  @Override
  public List<String> getTableFamiles(String tableName) throws Exception {
    checkConfiguration();

    HTableDescriptor descriptor = m_admin.getTableDescriptor(m_bytesUtil
        .toBytes(tableName));
    Collection<HColumnDescriptor> families = descriptor.getFamilies();
    List<String> famList = new ArrayList<String>();
    for (HColumnDescriptor h : families) {
      famList.add(h.getNameAsString());
    }

    return famList;
  }

  protected void configureColumnDescriptor(HColumnDescriptor h, Properties p) {
    if (p != null) {
      // optional column family creation properties
      Set keys = p.keySet();
      for (Object key : keys) {
        String value = p.getProperty(key.toString());
        if (key.toString().equals(COL_DESCRIPTOR_MAX_VERSIONS_KEY)) {
          h.setMaxVersions(Integer.parseInt(value));
        } else if (key.toString().equals(COL_DESCRIPTOR_COMPRESSION_KEY)) {
          h.setCompressionType(Compression.getCompressionAlgorithmByName(value));
        } else if (key.toString().equals(COL_DESCRIPTOR_IN_MEMORY_KEY)) {
          boolean result = (value.toLowerCase().equals("Y")
              || value.toLowerCase().equals("yes") || value.toLowerCase()
              .equals("true"));
          h.setInMemory(result);
        } else if (key.toString()
            .equals(COL_DESCRIPTOR_BLOCK_CACHE_ENABLED_KEY)) {
          boolean result = (value.toLowerCase().equals("Y")
              || value.toLowerCase().equals("yes") || value.toLowerCase()
              .equals("true"));
          h.setBlockCacheEnabled(result);
        } else if (key.toString().equals(COL_DESCRIPTOR_BLOCK_SIZE_KEY)) {
          h.setBlocksize(Integer.parseInt(value));
        } else if (key.toString().equals(COL_DESCRIPTOR_TIME_TO_LIVE_KEY)) {
          h.setTimeToLive(Integer.parseInt(value));
        } else if (key.toString().equals(COL_DESCRIPTOR_BLOOM_FILTER_KEY)) {
          h.setBloomFilterType(BloomType.valueOf(value));
        } else if (key.toString().equals(COL_DESCRIPTOR_SCOPE_KEY)) {
          h.setScope(Integer.parseInt(value));
        }
      }
    }
  }

  protected void checkSourceTable() throws Exception {
    if (m_sourceTable == null) {
      throw new Exception(BaseMessages.getString(PKG,
          "CommonHBaseConnection.Error.NoSourceTable"));
    }
  }

  protected void checkSourceScan() throws Exception {
    if (m_sourceScan == null) {
      throw new Exception(BaseMessages.getString(PKG,
          "CommonHBaseConnection.Error.NoSourceScan"));
    }
  }

  @Override
  public void createTable(String tableName, List<String> colFamilyNames,
      Properties creationProps) throws Exception {
    checkConfiguration();

    HTableDescriptor tableDescription = new HTableDescriptor(tableName);

    for (String familyName : colFamilyNames) {
      HColumnDescriptor c = new HColumnDescriptor(familyName);
      configureColumnDescriptor(c, creationProps);
      tableDescription.addFamily(c);
    }

    m_admin.createTable(tableDescription);
  }

  @Override
  public void newSourceTable(String tableName) throws Exception {
    checkConfiguration();

    closeSourceTable();
    m_sourceTable = new HTable(m_config, tableName);
  }

  @Override
  public boolean sourceTableRowExists(byte[] rowKey) throws Exception {

    checkConfiguration();
    checkSourceTable();
    Get g = new Get(rowKey);
    Result r = m_sourceTable.get(g);

    return (!r.isEmpty());
  }

  @Override
  public void newSourceTableScan(byte[] keyLowerBound, byte[] keyUpperBound,
      int cacheSize) throws Exception {

    checkConfiguration();
    checkSourceTable();
    closeSourceResultSet();

    if (keyLowerBound != null) {
      if (keyUpperBound != null) {
        m_sourceScan = new Scan(keyLowerBound, keyUpperBound);
      } else {
        m_sourceScan = new Scan(keyLowerBound);
      }
    } else {
      m_sourceScan = new Scan();
    }

    if (cacheSize > 0) {
      m_sourceScan.setCaching(cacheSize);
    }
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

  /**
   * Add a column filter to the list of filters that the scanner will apply to
   * rows server-side.
   * 
   * @param cf the column filter to add
   * @param columnMeta the meta data for the column used in the filter to add
   * @param matchAny true if the list of filters (if not created yet) should be
   *          "match one" (and false if it should be "match all")
   * @param vars variables to use
   * @throws Exception if a problem occurs
   */
  @Override
  public void addColumnFilterToScan(ColumnFilter cf, HBaseValueMeta columnMeta,
      VariableSpace vars, boolean matchAny) throws Exception {

    checkSourceScan();

    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    Thread.currentThread().setContextClassLoader(getClass().getClassLoader());

    if (m_sourceScan.getFilter() == null) {
      // create a new FilterList
      FilterList fl = new FilterList(
          matchAny ? FilterList.Operator.MUST_PASS_ONE
              : FilterList.Operator.MUST_PASS_ALL);
      m_sourceScan.setFilter(fl);
    }

    FilterList fl = (FilterList) m_sourceScan.getFilter();

    CompareFilter.CompareOp comp = null;
    byte[] family = m_bytesUtil.toBytes(columnMeta.getColumnFamily());
    byte[] qualifier = m_bytesUtil.toBytes(columnMeta.getColumnName());
    ColumnFilter.ComparisonType op = cf.getComparisonOperator();

    switch (op) {
    case EQUAL:
      comp = CompareFilter.CompareOp.EQUAL;
      break;
    case NOT_EQUAL:
      comp = CompareFilter.CompareOp.NOT_EQUAL;
      break;
    case GREATER_THAN:
      comp = CompareFilter.CompareOp.GREATER;
      break;
    case GREATER_THAN_OR_EQUAL:
      comp = CompareFilter.CompareOp.GREATER_OR_EQUAL;
      break;
    case LESS_THAN:
      comp = CompareFilter.CompareOp.LESS;
      break;
    case LESS_THAN_OR_EQUAL:
      comp = CompareFilter.CompareOp.LESS_OR_EQUAL;
      break;
    }

    String comparisonString = cf.getConstant().trim();
    comparisonString = vars.environmentSubstitute(comparisonString);

    if (comp != null) {

      byte[] comparisonRaw = null;

      // do the numeric comparison stuff
      if (columnMeta.isNumeric()) {

        // Double/Float or Long/Integer
        DecimalFormat df = new DecimalFormat();
        String formatS = vars.environmentSubstitute(cf.getFormat());
        if (!isEmpty(formatS)) {
          df.applyPattern(formatS);
        }

        Number num = df.parse(comparisonString);

        if (columnMeta.isInteger()) {
          if (!columnMeta.getIsLongOrDouble()) {
            comparisonRaw = m_bytesUtil.toBytes(num.intValue());
          } else {
            comparisonRaw = m_bytesUtil.toBytes(num.longValue());
          }
        } else {
          if (!columnMeta.getIsLongOrDouble()) {
            comparisonRaw = m_bytesUtil.toBytes(num.floatValue());
          } else {
            comparisonRaw = m_bytesUtil.toBytes(num.doubleValue());
          }
        }

        if (!cf.getSignedComparison()) {
          SingleColumnValueFilter scf = new SingleColumnValueFilter(family,
              qualifier, comp, comparisonRaw);
          scf.setFilterIfMissing(true);
          fl.addFilter(scf);
        } else {
          // custom comparator for signed comparison
          DeserializedNumericComparator comparator = null;
          if (columnMeta.isInteger()) {
            if (columnMeta.getIsLongOrDouble()) {
              comparator = new DeserializedNumericComparator(
                  columnMeta.isInteger(), columnMeta.getIsLongOrDouble(),
                  num.longValue());
            } else {
              comparator = new DeserializedNumericComparator(
                  columnMeta.isInteger(), columnMeta.getIsLongOrDouble(),
                  num.intValue());
            }
          } else {
            if (columnMeta.getIsLongOrDouble()) {
              comparator = new DeserializedNumericComparator(
                  columnMeta.isInteger(), columnMeta.getIsLongOrDouble(),
                  num.doubleValue());
            } else {
              comparator = new DeserializedNumericComparator(
                  columnMeta.isInteger(), columnMeta.getIsLongOrDouble(),
                  num.floatValue());
            }
          }
          SingleColumnValueFilter scf = new SingleColumnValueFilter(family,
              qualifier, comp, comparator);
          scf.setFilterIfMissing(true);
          fl.addFilter(scf);
        }
      } else if (columnMeta.isDate()) {
        SimpleDateFormat sdf = new SimpleDateFormat();
        String formatS = vars.environmentSubstitute(cf.getFormat());
        if (!isEmpty(formatS)) {
          sdf.applyPattern(formatS);
        }
        Date d = sdf.parse(comparisonString);

        long dateAsMillis = d.getTime();
        if (!cf.getSignedComparison()) {
          comparisonRaw = m_bytesUtil.toBytes(dateAsMillis);

          SingleColumnValueFilter scf = new SingleColumnValueFilter(family,
              qualifier, comp, comparisonRaw);
          scf.setFilterIfMissing(true);
          fl.addFilter(scf);
        } else {
          // custom comparator for signed comparison
          DeserializedNumericComparator comparator = new DeserializedNumericComparator(
              true, true, dateAsMillis);
          SingleColumnValueFilter scf = new SingleColumnValueFilter(family,
              qualifier, comp, comparator);
          scf.setFilterIfMissing(true);
          fl.addFilter(scf);
        }
      } else if (columnMeta.isBoolean()) {

        // temporarily encode it so that we can use the utility routine in
        // HBaseValueMeta
        byte[] tempEncoded = m_bytesUtil.toBytes(comparisonString);
        Boolean decodedB = HBaseValueMeta.decodeBoolFromString(tempEncoded,
            m_bytesUtil);
        // skip if we can't parse the comparison value
        if (decodedB == null) {
          return;
        }

        DeserializedBooleanComparator comparator = new DeserializedBooleanComparator(
            decodedB.booleanValue());
        SingleColumnValueFilter scf = new SingleColumnValueFilter(family,
            qualifier, comp, comparator);
        scf.setFilterIfMissing(true);
        fl.addFilter(scf);
      }
    } else {
      WritableByteArrayComparable comparator = null;
      comp = CompareFilter.CompareOp.EQUAL;
      if (cf.getComparisonOperator() == ColumnFilter.ComparisonType.SUBSTRING) {
        comparator = new SubstringComparator(comparisonString);
      } else {
        comparator = new RegexStringComparator(comparisonString);
      }

      SingleColumnValueFilter scf = new SingleColumnValueFilter(family,
          qualifier, comp, comparator);
      scf.setFilterIfMissing(true);
      fl.addFilter(scf);
    }

    Thread.currentThread().setContextClassLoader(cl);
  }

  protected void checkResultSet() throws Exception {
    if (m_resultSet == null) {
      throw new Exception(BaseMessages.getString(PKG,
          "CommonHBaseConnection.Error.NoCurrentResultSet"));
    }
  }

  protected void checkForCurrentResultSetRow() throws Exception {
    if (m_currentResultSetRow == null) {
      throw new Exception(BaseMessages.getString(PKG,
          "CommonHBaseConnection.Error."));
    }
  }

  @Override
  public void executeSourceTableScan() throws Exception {
    checkConfiguration();
    checkSourceTable();
    checkSourceScan();

    if (m_sourceScan.getFilter() != null) {
      if (((FilterList) m_sourceScan.getFilter()).getFilters().size() == 0) {
        m_sourceScan.setFilter(null);
      }
    }

    m_resultSet = m_sourceTable.getScanner(m_sourceScan);
  }

  @Override
  public boolean resultSetNextRow() throws Exception {
    checkResultSet();

    m_currentResultSetRow = m_resultSet.next();

    return (m_currentResultSetRow != null);
  }

  @Override
  public boolean checkForHBaseRow(Object rowToCheck) {
    return (rowToCheck instanceof Result);
  }

  @Override
  public byte[] getRowKey(Object aRow) throws Exception {
    if (!checkForHBaseRow(aRow)) {
      throw new Exception(BaseMessages.getString(PKG,
          "CommonHBaseConnection.Error.ObjectIsNotAnHBaseRow"));
    }

    return ((Result) aRow).getRow();
  }

  @Override
  public byte[] getResultSetCurrentRowKey() throws Exception {

    checkSourceScan();
    checkResultSet();
    checkForCurrentResultSetRow();

    return getRowKey(m_currentResultSetRow);
  }

  @Override
  public byte[] getRowColumnLatest(Object aRow, String colFamilyName,
      String colName, boolean colNameIsBinary) throws Exception {

    if (!checkForHBaseRow(aRow)) {
      throw new Exception(BaseMessages.getString(PKG,
          "CommonHBaseConnection.Error.ObjectIsNotAnHBaseRow"));
    }

    byte[] result = ((Result) aRow).getValue(
        m_bytesUtil.toBytes(colFamilyName),
        colNameIsBinary ? m_bytesUtil.toBytesBinary(colName) : m_bytesUtil
            .toBytes(colName));

    return result;
  }

  @Override
  public byte[] getResultSetCurrentRowColumnLatest(String colFamilyName,
      String colName, boolean colNameIsBinary) throws Exception {
    checkSourceScan();
    checkResultSet();
    checkForCurrentResultSetRow();

    return getRowColumnLatest(m_currentResultSetRow, colFamilyName, colName,
        colNameIsBinary);
  }

  @Override
  public NavigableMap<byte[], byte[]> getRowFamilyMap(Object aRow,
      String familyName) throws Exception {

    if (!checkForHBaseRow(aRow)) {
      throw new Exception(BaseMessages.getString(PKG,
          "CommonHBaseConnection.Error.ObjectIsNotAnHBaseRow"));
    }

    return ((Result) aRow).getFamilyMap(m_bytesUtil.toBytes(familyName));
  }

  @Override
  public NavigableMap<byte[], byte[]> getResultSetCurrentRowFamilyMap(
      String familyName) throws Exception {
    checkSourceScan();
    checkResultSet();
    checkForCurrentResultSetRow();

    return getRowFamilyMap(m_currentResultSetRow, familyName);
  }

  @Override
  public NavigableMap<byte[], NavigableMap<byte[], NavigableMap<Long, byte[]>>> getRowMap(
      Object aRow) throws Exception {
    if (!checkForHBaseRow(aRow)) {
      throw new Exception(BaseMessages.getString(PKG,
          "CommonHBaseConnection.Error.ObjectIsNotAnHBaseRow"));
    }

    return ((Result) aRow).getMap();
  }

  @Override
  public NavigableMap<byte[], NavigableMap<byte[], NavigableMap<Long, byte[]>>> getResultSetCurrentRowMap()
      throws Exception {
    checkSourceScan();
    checkResultSet();
    checkForCurrentResultSetRow();

    return getRowMap(m_currentResultSetRow);
  }

  protected void checkTargetTable() throws Exception {
    if (m_targetTable == null) {
      throw new Exception(BaseMessages.getString(PKG,
          "CommonHBaseConnection.Error.NoTargetTable"));
    }
  }

  protected void checkTargetPut() throws Exception {
    if (m_currentTargetPut == null) {
      throw new Exception(BaseMessages.getString(PKG,
          "CommonHBaseConnection.Error.NoTargetPut"));
    }
  }

  @Override
  public void newTargetTable(String tableName, Properties props)
      throws Exception {
    checkConfiguration();
    closeTargetTable();

    m_targetTable = new HTable(m_config, tableName);

    if (props != null) {
      Set keys = props.keySet();
      for (Object key : keys) {
        String value = props.getProperty(key.toString());

        if (key.toString().equals(HTABLE_WRITE_BUFFER_SIZE_KEY)) {
          m_targetTable.setWriteBufferSize(Long.parseLong(value));
          m_targetTable.setAutoFlush(false);
        }
      }
    }
  }

  @Override
  public boolean targetTableIsAutoFlush() throws Exception {
    checkTargetTable();

    return m_targetTable.isAutoFlush();
  }

  @Override
  public void newTargetTablePut(byte[] key, boolean writeToWAL)
      throws Exception {
    checkTargetTable();

    m_currentTargetPut = new Put(key);
    m_currentTargetPut.setWriteToWAL(writeToWAL);
  }

  @Override
  public void executeTargetTablePut() throws Exception {
    checkConfiguration();
    checkTargetTable();
    checkTargetPut();

    m_targetTable.put(m_currentTargetPut);
  }

  @Override
  public void executeTargetTableDelete(byte[] rowKey) throws Exception {
    checkConfiguration();
    checkTargetTable();

    Delete d = new Delete(rowKey);
    m_targetTable.delete(d);
  }

  @Override
  public void flushCommitsTargetTable() throws Exception {
    checkConfiguration();
    checkTargetTable();

    m_targetTable.flushCommits();
  }

  @Override
  public void addColumnToTargetPut(String columnFamily, String columnName,
      boolean colNameIsBinary, byte[] colValue) throws Exception {

    checkTargetTable();
    checkTargetPut();

    m_currentTargetPut.add(
        m_bytesUtil.toBytes(columnFamily),
        colNameIsBinary ? m_bytesUtil.toBytesBinary(columnName) : m_bytesUtil
            .toBytes(columnName), colValue);
  }

  @Override
  public void closeTargetTable() throws Exception {
    checkConfiguration();

    if (m_targetTable != null) {
      if (!m_targetTable.isAutoFlush()) {
        flushCommitsTargetTable();
      }
      m_targetTable.close();
      m_targetTable = null;
    }
  }

  @Override
  public void closeSourceResultSet() throws Exception {
    checkConfiguration();

    // An open result set?
    if (m_resultSet != null) {
      m_resultSet.close();
      m_resultSet = null;
      m_currentResultSetRow = null;
    }
  }

  @Override
  public void closeSourceTable() throws Exception {
    checkConfiguration();
    closeSourceResultSet();

    if (m_sourceTable != null) {
      m_sourceTable.close();
      m_sourceTable = null;
    }
  }

  @Override
  public boolean isImmutableBytesWritable(Object o) {
    // For this to work the context class loader must be able to load
    // ImmutableBytesWritable.class from the same CL as o.getClass() was loaded
    // from
    return o instanceof ImmutableBytesWritable;
  }
}
