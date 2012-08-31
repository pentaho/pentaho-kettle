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

package org.pentaho.hbase.shim.spi;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.NavigableMap;
import java.util.Properties;

import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.hbase.shim.api.ColumnFilter;
import org.pentaho.hbase.shim.api.HBaseValueMeta;

public abstract class HBaseConnection {

  // version key
  public static final String HBASE_VERSION_KEY = "hbase.defaults.for.version";

  // constant connection keys
  public static final String DEFAULTS_KEY = "hbase.default";
  public static final String SITE_KEY = "hbase.site";
  public static final String ZOOKEEPER_QUORUM_KEY = "hbase.zookeeper.quorum";
  public static final String ZOOKEEPER_PORT_KEY = "hbase.zookeeper.property.clientPort";

  // constant table creation option keys (commented out keys don't exist as
  // options for HColumnDescriptor in 0.90.3)
  // public static final String COL_DESCRIPTOR_MIN_VERSIONS_KEY =
  // "col.descriptor.minVersions";
  public static final String COL_DESCRIPTOR_MAX_VERSIONS_KEY = "col.descriptor.maxVersions";
  // public static final String COL_DESCRIPTOR_KEEP_DELETED_CELLS_KEY =
  // "col.descriptor.keepDeletedCells";
  public static final String COL_DESCRIPTOR_COMPRESSION_KEY = "col.descriptor.compression";
  // public static final String COL_DESCRIPTOR_ENCODE_ON_DISK_KEY =
  // "col.descriptor.encodeOnDisk";
  // public static final String COL_DESCRIPTOR_DATA_BLOCK_ENCODING_KEY =
  // "col.descriptor.dataBlockEncoding";
  public static final String COL_DESCRIPTOR_IN_MEMORY_KEY = "col.descriptor.inMemory";
  public static final String COL_DESCRIPTOR_BLOCK_CACHE_ENABLED_KEY = "col.descriptor.blockCacheEnabled";
  public static final String COL_DESCRIPTOR_BLOCK_SIZE_KEY = "col.descriptor.blockSize";
  public static final String COL_DESCRIPTOR_TIME_TO_LIVE_KEY = "col.desciptor.timeToLive";
  public static final String COL_DESCRIPTOR_BLOOM_FILTER_KEY = "col.descriptor.bloomFilter";
  public static final String COL_DESCRIPTOR_SCOPE_KEY = "col.descriptor.scope";

  // constant HTable writing keys
  public static final String HTABLE_WRITE_BUFFER_SIZE_KEY = "htable.writeBufferSize";

  /**
   * Method for getting a byte utility implementation
   * 
   * @return
   * @throws Exception
   */
  public abstract HBaseBytesUtilShim getBytesUtil() throws Exception;

  /**
   * Configure the HBase connection using the supplied connection properties
   * 
   * @param connProps the properties supplying connection details
   * @param logMessages will hold any log messages generated during the
   *          connection configuration process
   * @throws Exception if a problem occurs
   */
  public abstract void configureConnection(Properties connProps,
      List<String> logMessages) throws Exception;

  /**
   * Check if HBase is available and running
   * 
   * @throws Exception if a problem occurs
   */
  public abstract void checkHBaseAvailable() throws Exception;

  /**
   * Gets a list of tables from HBase
   * 
   * @return a list of tables
   * @throws Exception if a problem occurs
   */
  public abstract List<String> listTableNames() throws Exception;

  /**
   * Returns true if the named table exists in HBase
   * 
   * @param tableName the name of the table to check
   * @return true if the table exists
   * @throws Exception if a problem occurs
   */
  public abstract boolean tableExists(String tableName) throws Exception;

  /**
   * Returns true if the named table is disabled in HBase
   * 
   * @param tableName the name of the table to check
   * @return true if the table is disabled
   * @throws Exception if a problem occurs
   */
  public abstract boolean isTableDisabled(String tableName) throws Exception;

  /**
   * Returns true if the named table is available
   * 
   * @param tableName the name of the table to check
   * @return true if the table is available
   * @throws Exception if a problem occurs
   */
  public abstract boolean isTableAvailable(String tableName) throws Exception;

  /**
   * Disable the named table
   * 
   * @param tableName the name of the table to disable
   * @throws Exception if a problem occurs
   */
  public abstract void disableTable(String tableName) throws Exception;

  /**
   * Enable the named table
   * 
   * @param tableName the name of the table to enable
   * @throws Exception if a problem occurs
   */
  public abstract void enableTable(String tableName) throws Exception;

  /**
   * Delete the named table from HBase
   * 
   * @param tableName the name of the table to delete
   * @throws Exception if a problem occurs
   */
  public abstract void deleteTable(String tableName) throws Exception;

  /**
   * Delete a row from the current target table
   * 
   * @param rowKey the key of the row to delete
   * @throws Exception if no target table has been specified yet via
   *           <code>newTargetTable</code> or a problem occurs during the
   *           operation.
   */
  public abstract void executeTargetTableDelete(byte[] rowKey) throws Exception;

  /**
   * Create the named table in HBase
   * 
   * @param tableName the name of the table to create
   * @param colFamilyNames a list of column families to create in the new table
   * @param creationProps options for table creation (see the constant table
   *          creation keys).
   * @throws Exception a problem occurs
   */
  public abstract void createTable(String tableName,
      List<String> colFamilyNames, Properties creationProps) throws Exception;

  /**
   * Return a list of column families for the supplied table name
   * 
   * @param tableName the name of the table to list column families for
   * @return a list of column families existing in the named table
   * @throws Exception if a problem occurs
   */
  public abstract List<String> getTableFamiles(String tableName)
      throws Exception;

  /**
   * Specify a new source table to use for read operations
   * 
   * @param tableName the name of the table to read from
   * @throws Exception if a problem occurs
   */
  public abstract void newSourceTable(String tableName) throws Exception;

  /**
   * Returns true if the source table contains a row with the given row key
   * 
   * @param rowKey the row key to check for
   * @return true if the source table contains a row with the given row key
   * @throws Exception if a problem occurs
   */
  public abstract boolean sourceTableRowExists(byte[] rowKey) throws Exception;

  /**
   * Configure a new source table scan. HBase can do a full table scan if no
   * lower and upper bound are supplied or an open upper-ended scan if a lower
   * bound but no upper bound is specified. An upper bound with no lower bound
   * is not allowed.
   * 
   * @param keyLowerBound the lower bound of the scan range (may be null for no
   *          lower bound)
   * @param keyUpperBound the upper bound of the scan range (man be null for no
   *          upper bound).
   * @param cacheSize the size of the scanner cache
   * @throws Exception no source table has been specified or if a problem occurs
   */
  public abstract void newSourceTableScan(byte[] keyLowerBound,
      byte[] keyUpperBound, int cacheSize) throws Exception;

  /**
   * Configure a new target table put
   * 
   * @param key the key of the row that will be inserted into the target table
   * @param writeToWAL false to disable the write to WAL
   * @throws Exception if no target table has been specified or if a problem
   *           occurs
   */
  public abstract void newTargetTablePut(byte[] key, boolean writeToWAL)
      throws Exception;

  /**
   * Returns true if the target table is set up to automatically flush commits
   * 
   * @return true if the target table is auto flush
   * @throws Exception if no target table has been specified or if a problem
   *           occurs
   */
  public abstract boolean targetTableIsAutoFlush() throws Exception;

  /**
   * Executes the last configured target table push
   * 
   * @throws Exception if no target table has been specified or if a problem
   *           occurs
   */
  public abstract void executeTargetTablePut() throws Exception;

  /**
   * Flush any buffered commits for the target table
   * 
   * @throws Exception if a problem occurs
   */
  public abstract void flushCommitsTargetTable() throws Exception;

  /**
   * Add a column value to the current target table push
   * 
   * @param columnFamily the column family to add the column to
   * @param columnName the name of the column to add
   * @param colNameIsBinary true if the column name is binary
   * @param colValue the encoded column value to add
   * @throws Exception if a problem occurs
   */
  public abstract void addColumnToTargetPut(String columnFamily,
      String columnName, boolean colNameIsBinary, byte[] colValue)
      throws Exception;

  /**
   * Add a column filter to the list of filters that the scanner will apply to
   * rows server-side.
   * 
   * @param cf the column filter to add
   * @param columnMeta the meta data for the column used in the filter to add
   * @param vars environment variables
   * @param matchAny true if the list of filters (if not created yet) should be
   *          "match one" (and false if it should be "match all")
   * @throws Exception if a problem occurs
   */
  public abstract void addColumnFilterToScan(ColumnFilter cf,
      HBaseValueMeta columnMeta, VariableSpace vars, boolean matchAny)
      throws Exception;

  /**
   * Add a specific column to the current source table scan
   * 
   * @param colFamilyName the name of the column family containing the column to
   *          add
   * @param colName the name of the column
   * @param colNameIsBinary true if the column name is binary
   * @throws Exception if a problem occurs
   */
  public abstract void addColumnToScan(String colFamilyName, String colName,
      boolean colNameIsBinary) throws Exception;

  /**
   * Execute the current source table scan
   * 
   * @throws Exception if a problem occurs
   */
  public abstract void executeSourceTableScan() throws Exception;

  /**
   * Advance the source table scanner to the next row.
   * 
   * @return true if the scanner was advanced and the next row is now the
   *         current row; false if there is no next row.
   * @throws Exception if a source table or source scan has not been specified,
   *           if the scan has not been "executed" or if a problem occurs.
   */
  public abstract boolean resultSetNextRow() throws Exception;

  /**
   * Get the row key of the supplied row object.
   * 
   * @param aRow an HBase row
   * @return the raw row key
   * @throws Exception if the supplied object is not of the correct type for the
   *           current instance/version of HBase wrapped by this HBaseAdmin or
   *           if a problem occurs
   */
  public abstract byte[] getRowKey(Object aRow) throws Exception;

  /**
   * Get the row key of the current row from the current source table scan
   * 
   * @return the raw row key of the current source table row.
   * @throws Exception if source table or source scan has not been configured,
   *           the source scan has not been "executed" or a problem occurs
   */
  public abstract byte[] getResultSetCurrentRowKey() throws Exception;

  /**
   * get the latest version of a column in the supplied row object
   * 
   * @param aRow an HBase row
   * @param colFamilyName the name of the column family that the column belongs
   *          to
   * @param colName the name of the column in question
   * @param colNameIsBinary true if the column name is binary
   * @return the raw column value
   * @throws Exception if the supplied object is not of the correct type for the
   *           current instance/version of HBase wrapped by this HBaseAdmin or
   *           if a problem occurs
   */
  public abstract byte[] getRowColumnLatest(Object aRow, String colFamilyName,
      String colName, boolean colNameIsBinary) throws Exception;

  /**
   * Checks if the supplied object is a HBase "row" for the instance/version of
   * HBase wrapped by this HBaseAdmin
   * 
   * @param rowToCheck an HBase row to check for validity
   * @return true if the supplied object is an HBase "row" object
   */
  public abstract boolean checkForHBaseRow(Object rowToCheck);

  /**
   * Gets the value of a column from the current row from the source table scan
   * 
   * @param colFamilyName the name of the column family to look for the column
   *          in
   * @param colName the name of the column
   * @param colNameIsBinary true if the column name is binary
   * 
   * @return the value of the column or null if the column is not in the row (or
   *         in the subset of columns specified in the scan for the current
   *         row).
   * @throws Exception if the source table or scan isn't configured, the source
   *           scan has not been "executed" or a problem occurs
   */
  public abstract byte[] getResultSetCurrentRowColumnLatest(
      String colFamilyName, String colName, boolean colNameIsBinary)
      throws Exception;

  /**
   * Return a map of columns to values from the supplied HBase row object for
   * the supplied column family name
   * 
   * @param aRow an HBase row object
   * @param familyName the column family to return columns from
   * @return a map of column names to values
   * @throws Exception if the supplied row object isn't valid with respect to
   *           the instance/versions of HBase wrapped by this HBaseAdmin or a
   *           problem occurs
   */
  public abstract NavigableMap<byte[], byte[]> getRowFamilyMap(Object aRow,
      String familyName) throws Exception;

  /**
   * Return a map of columns to values from the current source table scan row
   * for the supplied column family name.
   * 
   * @param familyName the column family to return rows from
   * @return a map of column names to values
   * @throws Exception if the supplied row object isn't valid with respect to
   *           the instance/versions of HBase wrapped by this HBaseAdmin or a
   *           problem occurs
   */
  public abstract NavigableMap<byte[], byte[]> getResultSetCurrentRowFamilyMap(
      String familyName) throws Exception;

  /**
   * Get a full map for the supplied HBase row (i.e. column family -> column
   * name -> col value).
   * 
   * @param aRow an HBase row object
   * @return a map of column families to columns to column values
   * @throws Exception if the supplied row object is not valid with respect to
   *           the instance/version of HBase wrapped by this HBaseAdmin or a
   *           problem occurs
   */
  public abstract NavigableMap<byte[], NavigableMap<byte[], NavigableMap<Long, byte[]>>> getRowMap(
      Object aRow) throws Exception;

  /**
   * Get a full map from the current source table scan row (i.e. column family
   * -> column name -> col value).
   * 
   * @return a map of column families to columns to column values
   * @throws Exception if the source table or scan is not configured, the scan
   *           hasn't been "executed" or a problem occurs
   */
  public abstract NavigableMap<byte[], NavigableMap<byte[], NavigableMap<Long, byte[]>>> getResultSetCurrentRowMap()
      throws Exception;

  /**
   * Close the current source table
   * 
   * @throws Exception if a problem occurs
   */
  public abstract void closeSourceTable() throws Exception;

  /**
   * Close the current source scan's result set
   * 
   * @throws Exception if a problem occurs
   */
  public abstract void closeSourceResultSet() throws Exception;

  /**
   * Specify an new target table to write to
   * 
   * @param tableName the name of the target table
   * @param props properties for writing (constant HTable writing keys)
   * @throws Exception if the connection HBaseAdmin hasn't been configured or a
   *           problem occurs
   */
  public abstract void newTargetTable(String tableName, Properties props)
      throws Exception;

  /**
   * Close the target table
   * 
   * @throws Exception if a problem occurs
   */
  public abstract void closeTargetTable() throws Exception;

  /**
   * Determines if the object is an
   * {@link org.apache.hadoop.hbase.io.ImmutableBytesWritable}.
   * 
   * @return {@code true} if {@code o} is an
   *         {@link org.apache.hadoop.hbase.io.ImmutableBytesWritable}.
   */
  public abstract boolean isImmutableBytesWritable(Object o);

  /**
   * Utility method to covert a string to a URL object.
   * 
   * @param pathOrURL file or http URL as a string
   * @return a URL
   * @throws MalformedURLException if there is a problem with the URL.
   */
  public static URL stringToURL(String pathOrURL) throws MalformedURLException {
    URL result = null;

    if (isEmpty(pathOrURL)) {
      if (pathOrURL.toLowerCase().startsWith("http://")
          || pathOrURL.toLowerCase().startsWith("file://")) {
        result = new URL(pathOrURL);
      } else {
        String c = "file://" + pathOrURL;
        result = new URL(c);
      }
    }

    return result;
  }

  public static boolean isEmpty(String toCheck) {
    return (toCheck == null || toCheck.length() == 0);
  }
}
