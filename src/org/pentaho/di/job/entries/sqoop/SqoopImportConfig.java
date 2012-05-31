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

package org.pentaho.di.job.entries.sqoop;

/**
 * Configuration for a Sqoop Import
 */
public class SqoopImportConfig extends SqoopConfig {
  // Import control arguments
  public static final String TARGET_DIR = "targetDir";
  public static final String WAREHOUSE_DIR = "warehouseDir";
  public static final String APPEND = "append";
  public static final String AS_AVRODATAFILE = "asAvrodatafile";
  public static final String AS_SEQUENCEFILE = "asSequencefile";
  public static final String AS_TEXTFILE = "asTextfile";
  public static final String BOUNDARY_QUERY = "boundaryQuery";
  public static final String COLUMNS = "columns";
  public static final String DIRECT = "direct";
  public static final String DIRECT_SPLIT_SIZE = "directSplitSize";
  public static final String INLINE_LOB_LIMIT = "inlineLobLimit";
  public static final String SPLIT_BY = "splitBy";
  public static final String QUERY = "query";
  public static final String WHERE = "where";
  public static final String COMPRESS = "compress";
  public static final String COMPRESSION_CODEC = "compressionCodec";
  public static final String NULL_STRING = "nullString";
  public static final String NULL_NON_STRING = "nullNonString";

  // Incremental import arguments
  public static final String CHECK_COLUMN = "checkColumn";
  public static final String INCREMENTAL = "incremental";
  public static final String LAST_VALUE = "lastValue";

  // Hive arguments
  public static final String HIVE_HOME = "hiveHome";
  public static final String HIVE_IMPORT = "hiveImport";
  public static final String HIVE_OVERWRITE = "hiveOverwrite";
  public static final String CREATE_HIVE_TABLE = "createHiveTable";
  public static final String HIVE_TABLE = "hiveTable";
  public static final String HIVE_DROP_IMPORT_DELIMS = "hiveDropImportDelims";
  public static final String HIVE_DELIMS_REPLACEMENT = "hiveDelimsReplacement";
  public static final String HIVE_PARTITION_KEY = "hivePartitionKey";
  public static final String HIVE_PARTITION_VALUE = "hivePartitionValue";
  public static final String MAP_COLUMN_HIVE = "mapColumnHive";

  // HBase arguments
  public static final String COLUMN_FAMILY = "columnFamily";
  public static final String HBASE_CREATE_TABLE = "hbaseCreateTable";
  public static final String HBASE_ROW_KEY = "hbaseRowKey";
  public static final String HBASE_TABLE = "hbaseTable";

  // Import control arguments
  @CommandLineArgument(name = "target-dir")
  private String targetDir;
  @CommandLineArgument(name = "warehouse-dir")
  private String warehouseDir;
  @CommandLineArgument(name = APPEND, flag = true)
  private String append;
  @CommandLineArgument(name = "as-avrodatafile", flag = true)
  private String asAvrodatafile;
  @CommandLineArgument(name = "as-sequencefile", flag = true)
  private String asSequencefile;
  @CommandLineArgument(name = "as-textfile", flag = true)
  private String asTextfile;
  @CommandLineArgument(name = "boundary-query")
  private String boundaryQuery;
  @CommandLineArgument(name = COLUMNS)
  private String columns;
  @CommandLineArgument(name = DIRECT, flag = true)
  private String direct;
  @CommandLineArgument(name = "direct-split-size")
  private String directSplitSize;
  @CommandLineArgument(name = "inline-lob-limit")
  private String inlineLobLimit;
  @CommandLineArgument(name = "split-by")
  private String splitBy;
  @CommandLineArgument(name = QUERY)
  private String query;
  @CommandLineArgument(name = WHERE)
  private String where;
  @CommandLineArgument(name = COMPRESS, flag = true)
  private String compress;
  @CommandLineArgument(name = "compression-codec")
  private String compressionCodec;
  @CommandLineArgument(name = "null-string")
  private String nullString;
  @CommandLineArgument(name = "null-non-string")
  private String nullNonString;

  // Incremental import arguments
  @CommandLineArgument(name = "check-column")
  private String checkColumn;
  @CommandLineArgument(name = INCREMENTAL)
  private String incremental;
  @CommandLineArgument(name = "last-value")
  private String lastValue;

  // Hive arguments
  @CommandLineArgument(name = "hive-home")
  private String hiveHome;
  @CommandLineArgument(name = "hive-import", flag = true)
  private String hiveImport;
  @CommandLineArgument(name = "hive-overwrite", flag = true)
  private String hiveOverwrite;
  @CommandLineArgument(name = "create-hive-table", flag = true)
  private String createHiveTable;
  @CommandLineArgument(name = "hive-table")
  private String hiveTable;
  @CommandLineArgument(name = "hive-drop-import-delims", flag = true)
  private String hiveDropImportDelims;
  @CommandLineArgument(name = "hive-delims-replacement")
  private String hiveDelimsReplacement;
  @CommandLineArgument(name = "hive-partition-key")
  private String hivePartitionKey;
  @CommandLineArgument(name = "hive-partition-value")
  private String hivePartitionValue;
  @CommandLineArgument(name = "map-column-hive")
  private String mapColumnHive;

  // HBase arguments
  @CommandLineArgument(name = "column-family")
  private String columnFamily;
  @CommandLineArgument(name = "hbase-create-table", flag = true)
  private String hbaseCreateTable;
  @CommandLineArgument(name = "hbase-row-key")
  private String hbaseRowKey;
  @CommandLineArgument(name = "hbase-table")
  private String hbaseTable;

  public String getTargetDir() {
    return targetDir;
  }

  public void setTargetDir(String targetDir) {
    String old = this.targetDir;
    this.targetDir = targetDir;
    pcs.firePropertyChange(TARGET_DIR, old, this.targetDir);
  }

  public String getWarehouseDir() {
    return warehouseDir;
  }

  public void setWarehouseDir(String warehouseDir) {
    String old = this.warehouseDir;
    this.warehouseDir = warehouseDir;
    pcs.firePropertyChange(WAREHOUSE_DIR, old, this.warehouseDir);
  }

  public String getAppend() {
    return append;
  }

  public void setAppend(String append) {
    String old = this.append;
    this.append = append;
    pcs.firePropertyChange(APPEND, old, this.append);
  }

  public String getAsAvrodatafile() {
    return asAvrodatafile;
  }

  public void setAsAvrodatafile(String asAvrodatafile) {
    String old = this.asAvrodatafile;
    this.asAvrodatafile = asAvrodatafile;
    pcs.firePropertyChange(AS_AVRODATAFILE, old, this.asAvrodatafile);
  }

  public String getAsSequencefile() {
    return asSequencefile;
  }

  public void setAsSequencefile(String asSequencefile) {
    String old = this.asSequencefile;
    this.asSequencefile = asSequencefile;
    pcs.firePropertyChange(AS_SEQUENCEFILE, old, this.asSequencefile);
  }

  public String getAsTextfile() {
    return asTextfile;
  }

  public void setAsTextfile(String asTextfile) {
    String old = this.asTextfile;
    this.asTextfile = asTextfile;
    pcs.firePropertyChange(AS_TEXTFILE, old, this.asTextfile);
  }

  public String getBoundaryQuery() {
    return boundaryQuery;
  }

  public void setBoundaryQuery(String boundaryQuery) {
    String old = this.boundaryQuery;
    this.boundaryQuery = boundaryQuery;
    pcs.firePropertyChange(BOUNDARY_QUERY, old, this.boundaryQuery);
  }

  public String getColumns() {
    return columns;
  }

  public void setColumns(String columns) {
    String old = this.columns;
    this.columns = columns;
    pcs.firePropertyChange(COLUMNS, old, this.columns);
  }

  public String getDirect() {
    return direct;
  }

  public void setDirect(String direct) {
    String old = this.direct;
    this.direct = direct;
    pcs.firePropertyChange(DIRECT, old, this.direct);
  }

  public String getDirectSplitSize() {
    return directSplitSize;
  }

  public void setDirectSplitSize(String directSplitSize) {
    String old = this.directSplitSize;
    this.directSplitSize = directSplitSize;
    pcs.firePropertyChange(DIRECT_SPLIT_SIZE, old, this.directSplitSize);
  }

  public String getInlineLobLimit() {
    return inlineLobLimit;
  }

  public void setInlineLobLimit(String inlineLobLimit) {
    String old = this.inlineLobLimit;
    this.inlineLobLimit = inlineLobLimit;
    pcs.firePropertyChange(INLINE_LOB_LIMIT, old, this.inlineLobLimit);
  }

  public String getSplitBy() {
    return splitBy;
  }

  public void setSplitBy(String splitBy) {
    String old = this.splitBy;
    this.splitBy = splitBy;
    pcs.firePropertyChange(SPLIT_BY, old, this.splitBy);
  }

  public String getQuery() {
    return query;
  }

  public void setQuery(String query) {
    String old = this.query;
    this.query = query;
    pcs.firePropertyChange(QUERY, old, this.query);
  }

  public String getWhere() {
    return where;
  }

  public void setWhere(String where) {
    String old = this.where;
    this.where = where;
    pcs.firePropertyChange(WHERE, old, this.where);
  }

  public String getCompress() {
    return compress;
  }

  public void setCompress(String compress) {
    String old = this.compress;
    this.compress = compress;
    pcs.firePropertyChange(COMPRESS, old, this.compress);
  }

  public String getCompressionCodec() {
    return compressionCodec;
  }

  public void setCompressionCodec(String compressionCodec) {
    String old = this.compressionCodec;
    this.compressionCodec = compressionCodec;
    pcs.firePropertyChange(COMPRESSION_CODEC, old, this.compressionCodec);
  }

  public String getNullString() {
    return nullString;
  }

  public void setNullString(String nullString) {
    String old = this.nullString;
    this.nullString = nullString;
    pcs.firePropertyChange(NULL_STRING, old, this.nullString);
  }

  public String getNullNonString() {
    return nullNonString;
  }

  public void setNullNonString(String nullNonString) {
    String old = this.nullNonString;
    this.nullNonString = nullNonString;
    pcs.firePropertyChange(NULL_NON_STRING, old, this.nullNonString);
  }

  public String getCheckColumn() {
    return checkColumn;
  }

  public void setCheckColumn(String checkColumn) {
    String old = this.checkColumn;
    this.checkColumn = checkColumn;
    pcs.firePropertyChange(CHECK_COLUMN, old, this.checkColumn);
  }

  public String getIncremental() {
    return incremental;
  }

  public void setIncremental(String incremental) {
    String old = this.incremental;
    this.incremental = incremental;
    pcs.firePropertyChange(INCREMENTAL, old, this.incremental);
  }

  public String getLastValue() {
    return lastValue;
  }

  public void setLastValue(String lastValue) {
    String old = this.lastValue;
    this.lastValue = lastValue;
    pcs.firePropertyChange(LAST_VALUE, old, this.lastValue);
  }

  public String getHiveHome() {
    return hiveHome;
  }

  public void setHiveHome(String hiveHome) {
    String old = this.hiveHome;
    this.hiveHome = hiveHome;
    pcs.firePropertyChange(HIVE_HOME, old, this.hiveHome);
  }

  public String getHiveImport() {
    return hiveImport;
  }

  public void setHiveImport(String hiveImport) {
    String old = this.hiveImport;
    this.hiveImport = hiveImport;
    pcs.firePropertyChange(HIVE_IMPORT, old, this.hiveImport);
  }

  public String getHiveOverwrite() {
    return hiveOverwrite;
  }

  public void setHiveOverwrite(String hiveOverwrite) {
    String old = this.hiveOverwrite;
    this.hiveOverwrite = hiveOverwrite;
    pcs.firePropertyChange(HIVE_OVERWRITE, old, this.hiveOverwrite);
  }

  public String getCreateHiveTable() {
    return createHiveTable;
  }

  public void setCreateHiveTable(String createHiveTable) {
    String old = this.createHiveTable;
    this.createHiveTable = createHiveTable;
    pcs.firePropertyChange(CREATE_HIVE_TABLE, old, this.createHiveTable);
  }

  public String getHiveTable() {
    return hiveTable;
  }

  public void setHiveTable(String hiveTable) {
    String old = this.hiveTable;
    this.hiveTable = hiveTable;
    pcs.firePropertyChange(HIVE_TABLE, old, this.hiveTable);
  }

  public String getHiveDropImportDelims() {
    return hiveDropImportDelims;
  }

  public void setHiveDropImportDelims(String hiveDropImportDelims) {
    String old = this.hiveDropImportDelims;
    this.hiveDropImportDelims = hiveDropImportDelims;
    pcs.firePropertyChange(HIVE_DROP_IMPORT_DELIMS, old, this.hiveDropImportDelims);
  }

  public String getHiveDelimsReplacement() {
    return hiveDelimsReplacement;
  }

  public void setHiveDelimsReplacement(String hiveDelimsReplacement) {
    String old = this.hiveDelimsReplacement;
    this.hiveDelimsReplacement = hiveDelimsReplacement;
    pcs.firePropertyChange(HIVE_DELIMS_REPLACEMENT, old, this.hiveDelimsReplacement);
  }

  public String getHivePartitionKey() {
    return hivePartitionKey;
  }

  public void setHivePartitionKey(String hivePartitionKey) {
    String old = this.hivePartitionKey;
    this.hivePartitionKey = hivePartitionKey;
    pcs.firePropertyChange(HIVE_PARTITION_KEY, old, this.hivePartitionKey);
  }

  public String getHivePartitionValue() {
    return hivePartitionValue;
  }

  public void setHivePartitionValue(String hivePartitionValue) {
    String old = this.hivePartitionValue;
    this.hivePartitionValue = hivePartitionValue;
    pcs.firePropertyChange(HIVE_PARTITION_VALUE, old, this.hivePartitionValue);
  }

  public String getMapColumnHive() {
    return mapColumnHive;
  }

  public void setMapColumnHive(String mapColumnHive) {
    String old = this.mapColumnHive;
    this.mapColumnHive = mapColumnHive;
    pcs.firePropertyChange(MAP_COLUMN_HIVE, old, this.mapColumnHive);
  }

  public String getColumnFamily() {
    return columnFamily;
  }

  public void setColumnFamily(String columnFamily) {
    String old = this.columnFamily;
    this.columnFamily = columnFamily;
    pcs.firePropertyChange(COLUMN_FAMILY, old, this.columnFamily);
  }

  public String getHbaseCreateTable() {
    return hbaseCreateTable;
  }

  public void setHbaseCreateTable(String hbaseCreateTable) {
    String old = this.hbaseCreateTable;
    this.hbaseCreateTable = hbaseCreateTable;
    pcs.firePropertyChange(HBASE_CREATE_TABLE, old, this.hbaseCreateTable);
  }

  public String getHbaseRowKey() {
    return hbaseRowKey;
  }

  public void setHbaseRowKey(String hbaseRowKey) {
    String old = this.hbaseRowKey;
    this.hbaseRowKey = hbaseRowKey;
    pcs.firePropertyChange(HBASE_ROW_KEY, old, this.hbaseRowKey);
  }

  public String getHbaseTable() {
    return hbaseTable;
  }

  public void setHbaseTable(String hbaseTable) {
    String old = this.hbaseTable;
    this.hbaseTable = hbaseTable;
    pcs.firePropertyChange(HBASE_TABLE, old, this.hbaseTable);
  }
}
