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

package org.pentaho.di.trans.steps.hbaseinput;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.hbase.HBaseRowToKettleTuple;
import org.pentaho.hbase.mapping.MappingAdmin;
import org.pentaho.hbase.shim.api.HBaseValueMeta;
import org.pentaho.hbase.shim.api.Mapping;
import org.pentaho.hbase.shim.common.CommonHBaseBytesUtil;
import org.pentaho.hbase.shim.fake.FakeHBaseConnection;
import org.pentaho.hbase.shim.spi.HBaseBytesUtilShim;
import org.pentaho.hbase.shim.spi.HBaseConnection;

public class HBaseInputTest {

  protected MappingAdmin initMappingAdmin(HBaseConnection conn)
      throws Exception {
    HBaseBytesUtilShim bytesUtil = conn.getBytesUtil();
    MappingAdmin ma = new MappingAdmin(conn, bytesUtil);
    ma.createMappingTable();

    return ma;
  }

  protected RowMetaInterface configureRowMeta(Mapping mapping,
      List<HBaseValueMeta> subsetOfCols) {
    RowMeta rowMeta = new RowMeta();

    if (subsetOfCols != null) {
      for (HBaseValueMeta v : subsetOfCols) {
        rowMeta.addValueMeta(v);
      }
    } else {

      int kettleType;
      if (mapping.getKeyType() == Mapping.KeyType.DATE
          || mapping.getKeyType() == Mapping.KeyType.UNSIGNED_DATE) {
        kettleType = ValueMetaInterface.TYPE_DATE;
      } else if (mapping.getKeyType() == Mapping.KeyType.STRING) {
        kettleType = ValueMetaInterface.TYPE_STRING;
      } else if (mapping.getKeyType() == Mapping.KeyType.BINARY) {
        kettleType = ValueMetaInterface.TYPE_BINARY;
      } else {
        kettleType = ValueMetaInterface.TYPE_INTEGER;
      }

      ValueMetaInterface keyMeta = new ValueMeta(mapping.getKeyName(),
          kettleType);

      rowMeta.addValueMeta(keyMeta);

      // Add the rest of the fields in the mapping
      Map<String, HBaseValueMeta> mappedColumnsByAlias = mapping
          .getMappedColumns();
      Set<String> aliasSet = mappedColumnsByAlias.keySet();
      for (String alias : aliasSet) {
        HBaseValueMeta columnMeta = mappedColumnsByAlias.get(alias);
        rowMeta.addValueMeta(columnMeta);
      }
    }

    return rowMeta;
  }

  @Test
  public void testFullTableScanAllColsInMapping() throws Exception {
    FakeHBaseConnection conn = new FakeHBaseConnection();

    MappingAdmin ma = initMappingAdmin(conn);
    ma.createTestTable();
    assertTrue(conn.tableExists("MarksTestTable"));
    ma.createTestMapping();
    assertTrue(ma.mappingExists("MarksTestTable", "MarksTestMapping"));

    Mapping tableMapping = ma.getMapping("MarksTestTable", "MarksTestMapping");
    assertTrue(tableMapping != null);

    CommonHBaseBytesUtil bu = new CommonHBaseBytesUtil();

    conn.newSourceTable("MarksTestTable");
    VariableSpace vars = new Variables();
    HBaseInputData.initializeScan(conn, bu, tableMapping, null, null, null,
        null, null, vars);

    RowMetaInterface outputRowMeta = configureRowMeta(tableMapping, null);
    conn.executeSourceTableScan();

    // +1 for the key
    int expectedNumColsPerRow = RowDataUtil.OVER_ALLOCATE_SIZE
        + tableMapping.getMappedColumns().size() + 1;
    int count = 0;
    Object[] decodedRow = null;
    while (conn.resultSetNextRow()) {
      decodedRow = HBaseInputData.getOutputRow(conn, null,
          tableMapping.getMappedColumns(), tableMapping, outputRowMeta, bu);

      assertTrue(decodedRow != null);
      assertEquals(decodedRow.length, expectedNumColsPerRow);
      count++;
    }

    // 20500 rows in the test table
    assertEquals(20500, count);

    // value of last row should by 19999
    assertEquals(new Long(19999), decodedRow[0]);
  }

  @Test
  public void testFullTableScanSubsetOfColsInMapping() throws Exception {
    FakeHBaseConnection conn = new FakeHBaseConnection();

    MappingAdmin ma = initMappingAdmin(conn);
    ma.createTestTable();
    assertTrue(conn.tableExists("MarksTestTable"));
    ma.createTestMapping();
    assertTrue(ma.mappingExists("MarksTestTable", "MarksTestMapping"));

    Mapping tableMapping = ma.getMapping("MarksTestTable", "MarksTestMapping");
    assertTrue(tableMapping != null);

    List<HBaseValueMeta> subsetOfCols = new ArrayList<HBaseValueMeta>();
    Map<String, HBaseValueMeta> colsMappedByAlias = tableMapping
        .getMappedColumns();
    assertTrue(colsMappedByAlias.size() > 0);
    HBaseValueMeta one = colsMappedByAlias.get("first_string_column");
    assertTrue(one != null);
    subsetOfCols.add(one);
    HBaseValueMeta two = colsMappedByAlias.get("first_unsigned_int_column");
    assertTrue(two != null);
    subsetOfCols.add(two);

    CommonHBaseBytesUtil bu = new CommonHBaseBytesUtil();

    conn.newSourceTable("MarksTestTable");
    VariableSpace vars = new Variables();
    HBaseInputData.initializeScan(conn, bu, tableMapping, null, null, null,
        null, null, vars);

    RowMetaInterface outputRowMeta = configureRowMeta(tableMapping,
        subsetOfCols);
    conn.executeSourceTableScan();

    // for the key
    int expectedNumColsPerRow = RowDataUtil.OVER_ALLOCATE_SIZE
        + subsetOfCols.size();
    int count = 0;
    Object[] decodedRow = null;
    while (conn.resultSetNextRow()) {
      decodedRow = HBaseInputData.getOutputRow(conn, subsetOfCols,
          tableMapping.getMappedColumns(), tableMapping, outputRowMeta, bu);

      assertTrue(decodedRow != null);
      assertEquals(decodedRow.length, expectedNumColsPerRow);
      count++;
    }

    // 20500 rows in the test table
    assertEquals(20500, count);

    // unsigned int column is set to key / 10.
    assertEquals(new Long(1999), decodedRow[1]);
  }

  @Test
  public void testTableScanFromLowerKeyBound() throws Exception {
    FakeHBaseConnection conn = new FakeHBaseConnection();

    MappingAdmin ma = initMappingAdmin(conn);
    ma.createTestTable();
    assertTrue(conn.tableExists("MarksTestTable"));
    ma.createTestMapping();
    assertTrue(ma.mappingExists("MarksTestTable", "MarksTestMapping"));

    Mapping tableMapping = ma.getMapping("MarksTestTable", "MarksTestMapping");
    assertTrue(tableMapping != null);

    CommonHBaseBytesUtil bu = new CommonHBaseBytesUtil();

    conn.newSourceTable("MarksTestTable");
    VariableSpace vars = new Variables();

    HBaseInputData.initializeScan(conn, bu, tableMapping, null, "-10", null,
        null, null, vars);

    RowMetaInterface outputRowMeta = configureRowMeta(tableMapping, null);
    conn.executeSourceTableScan();

    // +1 for the key
    int expectedNumColsPerRow = RowDataUtil.OVER_ALLOCATE_SIZE
        + tableMapping.getMappedColumns().size() + 1;
    int count = 0;
    while (conn.resultSetNextRow()) {
      Object[] decodedRow = HBaseInputData.getOutputRow(conn, null,
          tableMapping.getMappedColumns(), tableMapping, outputRowMeta, bu);

      assertTrue(decodedRow != null);
      assertEquals(decodedRow.length, expectedNumColsPerRow);

      if (count == 0) {
        // check the key of the first row returned
        assertTrue(decodedRow[0] != null);
        assertTrue(decodedRow[0] instanceof Long);
        assertEquals(new Long(-10), decodedRow[0]);
      }
      count++;
    }

    // 20010 rows returned by this scan
    assertEquals(20010, count);
  }

  @Test
  public void testTableScanLowerAndUpperKeyBounds() throws Exception {
    FakeHBaseConnection conn = new FakeHBaseConnection();

    MappingAdmin ma = initMappingAdmin(conn);
    ma.createTestTable();
    assertTrue(conn.tableExists("MarksTestTable"));
    ma.createTestMapping();
    assertTrue(ma.mappingExists("MarksTestTable", "MarksTestMapping"));

    Mapping tableMapping = ma.getMapping("MarksTestTable", "MarksTestMapping");
    assertTrue(tableMapping != null);

    CommonHBaseBytesUtil bu = new CommonHBaseBytesUtil();

    conn.newSourceTable("MarksTestTable");
    VariableSpace vars = new Variables();

    HBaseInputData.initializeScan(conn, bu, tableMapping, null, "-10", "50",
        null, null, vars);

    RowMetaInterface outputRowMeta = configureRowMeta(tableMapping, null);
    conn.executeSourceTableScan();

    // +1 for the key
    int expectedNumColsPerRow = RowDataUtil.OVER_ALLOCATE_SIZE
        + tableMapping.getMappedColumns().size() + 1;
    int count = 0;
    Object[] decodedRow = null;
    while (conn.resultSetNextRow()) {
      decodedRow = HBaseInputData.getOutputRow(conn, null,
          tableMapping.getMappedColumns(), tableMapping, outputRowMeta, bu);

      assertTrue(decodedRow != null);
      assertEquals(decodedRow.length, expectedNumColsPerRow);

      if (count == 0) {
        // check the key of the first row returned
        assertTrue(decodedRow[0] != null);
        assertTrue(decodedRow[0] instanceof Long);
        assertEquals(new Long(-10), decodedRow[0]);
      }
      count++;
    }

    // 60 rows from this scan
    assertEquals(60, count);

    // upper bound is exclusive - last key value should by 49 (not 50)
    assertEquals(new Long(49), decodedRow[0]);
  }

  @Test
  public void testTableScanEqualLowerAndUpperKeyBounds() throws Exception {
    FakeHBaseConnection conn = new FakeHBaseConnection();

    MappingAdmin ma = initMappingAdmin(conn);
    ma.createTestTable();
    assertTrue(conn.tableExists("MarksTestTable"));
    ma.createTestMapping();
    assertTrue(ma.mappingExists("MarksTestTable", "MarksTestMapping"));

    Mapping tableMapping = ma.getMapping("MarksTestTable", "MarksTestMapping");
    assertTrue(tableMapping != null);

    CommonHBaseBytesUtil bu = new CommonHBaseBytesUtil();

    conn.newSourceTable("MarksTestTable");
    VariableSpace vars = new Variables();

    HBaseInputData.initializeScan(conn, bu, tableMapping, null, "50", "50",
        null, null, vars);

    RowMetaInterface outputRowMeta = configureRowMeta(tableMapping, null);
    conn.executeSourceTableScan();

    // +1 for the key
    int expectedNumColsPerRow = RowDataUtil.OVER_ALLOCATE_SIZE
        + tableMapping.getMappedColumns().size() + 1;
    int count = 0;
    Object[] decodedRow = null;
    while (conn.resultSetNextRow()) {
      decodedRow = HBaseInputData.getOutputRow(conn, null,
          tableMapping.getMappedColumns(), tableMapping, outputRowMeta, bu);

      assertTrue(decodedRow != null);
      assertEquals(decodedRow.length, expectedNumColsPerRow);

      count++;
    }

    // just one row returned
    assertEquals(1, count);

    assertEquals(new Long(50), decodedRow[0]);
  }

  @Test
  public void testTableScanTupleMapping() throws Exception {
    FakeHBaseConnection conn = new FakeHBaseConnection();

    MappingAdmin ma = initMappingAdmin(conn);
    ma.createTupleTestTable();
    assertTrue(conn.tableExists("MarksTestTupleTable"));
    ma.createTestTupleMapping();
    assertTrue(ma.mappingExists("MarksTestTupleTable", "MarksTestTupleMapping"));

    CommonHBaseBytesUtil bu = new CommonHBaseBytesUtil();
    HBaseRowToKettleTuple tupleHandler = new HBaseRowToKettleTuple(bu);
    Mapping tableMapping = ma.getMapping("MarksTestTupleTable",
        "MarksTestTupleMapping");
    assertTrue(tableMapping != null);

    conn.newSourceTable("MarksTestTupleTable");
    VariableSpace vars = new Variables();

    HBaseInputData.initializeScan(conn, bu, tableMapping, null, null, null,
        null, null, vars);

    RowMetaInterface outputRowMeta = configureRowMeta(tableMapping, null);
    conn.executeSourceTableScan();

    int count = 1;
    List<Object[]> decodedRows = null;
    int expectedNumEvenTupleRows = 10;
    int expectedNumOddTupleRows = 20;
    int expectedNumColsPerRow = RowDataUtil.OVER_ALLOCATE_SIZE + 5; // 5 cols in
                                                                    // a tuple
                                                                    // mapping
    while (conn.resultSetNextRow()) {
      decodedRows = HBaseInputData.getTupleOutputRows(conn, null,
          tableMapping.getMappedColumns(), tableMapping, tupleHandler,
          outputRowMeta, bu);

      assertTrue(decodedRows != null);
      assertTrue(decodedRows.size() > 0);

      // check the key of the first hbase row
      if (count == 1) {
        assertEquals(new Long(1), decodedRows.get(0)[0]);
        assertEquals(expectedNumColsPerRow, decodedRows.get(0).length);
      }

      // check how many kettle rows this turned into
      if (count % 2 == 0) {
        assertEquals(expectedNumEvenTupleRows, decodedRows.size());
      } else {
        assertEquals(expectedNumOddTupleRows, decodedRows.size());
      }
      count++;
    }

    assertEquals(500, count);
  }
}
