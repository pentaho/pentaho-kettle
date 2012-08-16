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

package org.pentaho.hadoop.shim.spi;

import java.util.List;
import java.util.NavigableMap;
import java.util.Properties;

import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.hadoop.shim.ShimVersion;
import org.pentaho.hbase.shim.api.ColumnFilter;
import org.pentaho.hbase.shim.api.HBaseValueMeta;
import org.pentaho.hbase.shim.spi.HBaseBytesUtilShim;
import org.pentaho.hbase.shim.spi.HBaseShim;

public class MockHBaseShim extends HBaseShim {

  @Override
  public ShimVersion getVersion() {
    return null;
  }

  @Override
  public HBaseBytesUtilShim getBytesUtil() throws Exception {
    return null;
  }

  @Override
  public void configureConnection(Properties connProps, List<String> logMessages) throws Exception {
  }

  @Override
  public void checkHBaseAvailable() throws Exception {
  }

  @Override
  public List<String> listTableNames() throws Exception {
    return null;
  }

  @Override
  public boolean tableExists(String tableName) throws Exception {
    return false;
  }

  @Override
  public boolean isTableDisabled(String tableName) throws Exception {
    return false;
  }

  @Override
  public boolean isTableAvailable(String tableName) throws Exception {
    return false;
  }

  @Override
  public void disableTable(String tableName) throws Exception {
  }

  @Override
  public void enableTable(String tableName) throws Exception {
  }

  @Override
  public void deleteTable(String tableName) throws Exception {
  }

  @Override
  public void executeTargetTableDelete(byte[] rowKey) throws Exception {
  }

  @Override
  public void createTable(String tableName, List<String> colFamilyNames, Properties creationProps) throws Exception {
  }

  @Override
  public List<String> getTableFamiles(String tableName) throws Exception {
    return null;
  }

  @Override
  public void newSourceTable(String tableName) throws Exception {
  }

  @Override
  public boolean sourceTableRowExists(byte[] rowKey) throws Exception {
    return false;
  }

  @Override
  public void newSourceTableScan(byte[] keyLowerBound, byte[] keyUpperBound, int cacheSize) throws Exception {
  }

  @Override
  public void newTargetTablePut(byte[] key, boolean writeToWAL) throws Exception {
  }

  @Override
  public boolean targetTableIsAutoFlush() throws Exception {
    return false;
  }

  @Override
  public void executeTargetTablePut() throws Exception {
  }

  @Override
  public void flushCommitsTargetTable() throws Exception {
  }

  @Override
  public void addColumnToTargetPut(String columnFamily, String columnName, boolean colNameIsBinary, byte[] colValue)
      throws Exception {
  }

  @Override
  public void addColumnFilterToScan(ColumnFilter cf, HBaseValueMeta columnMeta, VariableSpace vars, boolean matchAny)
      throws Exception {
  }

  @Override
  public void addColumnToScan(String colFamilyName, String colName, boolean colNameIsBinary) throws Exception {
  }

  @Override
  public void executeSourceTableScan() throws Exception {
  }

  @Override
  public boolean resultSetNextRow() throws Exception {
    return false;
  }

  @Override
  public byte[] getRowKey(Object aRow) throws Exception {
    return null;
  }

  @Override
  public byte[] getResultSetCurrentRowKey() throws Exception {
    return null;
  }

  @Override
  public byte[] getRowColumnLatest(Object aRow, String colFamilyName, String colName, boolean colNameIsBinary)
      throws Exception {
    return null;
  }

  @Override
  public boolean checkForHBaseRow(Object rowToCheck) {
    return false;
  }

  @Override
  public byte[] getResultSetCurrentRowColumnLatest(String colFamilyName, String colName, boolean colNameIsBinary)
      throws Exception {
    return null;
  }

  @Override
  public NavigableMap<byte[], byte[]> getRowFamilyMap(Object aRow, String familyName) throws Exception {
    return null;
  }

  @Override
  public NavigableMap<byte[], byte[]> getResultSetCurrentRowFamilyMap(String familyName) throws Exception {
    return null;
  }

  @Override
  public NavigableMap<byte[], NavigableMap<byte[], NavigableMap<Long, byte[]>>> getRowMap(Object aRow) throws Exception {
    return null;
  }

  @Override
  public NavigableMap<byte[], NavigableMap<byte[], NavigableMap<Long, byte[]>>> getResultSetCurrentRowMap()
      throws Exception {
    return null;
  }

  @Override
  public void closeSourceTable() throws Exception {
  }

  @Override
  public void closeSourceResultSet() throws Exception {
  }

  @Override
  public void newTargetTable(String tableName, Properties props) throws Exception {
  }

  @Override
  public void closeTargetTable() throws Exception {
  }

  @Override
  public boolean isImmutableBytesWritable(Object o) {
    return false;
  }

}
