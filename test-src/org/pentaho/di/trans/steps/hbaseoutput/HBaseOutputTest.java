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

package org.pentaho.di.trans.steps.hbaseoutput;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.trans.steps.hbaseinput.HBaseInputData;
import org.pentaho.hbase.mapping.MappingAdmin;
import org.pentaho.hbase.shim.api.HBaseValueMeta;
import org.pentaho.hbase.shim.api.Mapping;
import org.pentaho.hbase.shim.common.CommonHBaseBytesUtil;
import org.pentaho.hbase.shim.fake.FakeHBaseConnection;
import org.pentaho.hbase.shim.spi.HBaseBytesUtilShim;
import org.pentaho.hbase.shim.spi.HBaseConnection;

public class HBaseOutputTest {

  protected MappingAdmin initMappingAdmin(HBaseConnection conn)
      throws Exception {
    HBaseBytesUtilShim bytesUtil = conn.getBytesUtil();
    MappingAdmin ma = new MappingAdmin(conn, bytesUtil);
    ma.createMappingTable();

    return ma;
  }

  protected void checkTableContents(FakeHBaseConnection conn,
      Mapping tableMapping, Map<String, HBaseValueMeta> colsMappedByAlias,
      List<HBaseValueMeta> subsetOfCols, CommonHBaseBytesUtil bu,
      List<Object[]> reference, RowMetaInterface rowMeta) throws Exception {

    conn.newSourceTable("MarksTestTable");
    VariableSpace vars = new Variables();

    HBaseInputData.initializeScan(conn, bu, tableMapping, null, null, null,
        null, null, vars);

    conn.executeSourceTableScan();
    int count = 0;
    while (conn.resultSetNextRow()) {
      if (count < reference.size()) {
        Object[] referenceRow = reference.get(count);

        Object[] decodedRow = HBaseInputData.getOutputRow(conn, subsetOfCols,
            colsMappedByAlias, tableMapping, rowMeta, bu);
        for (int i = 0; i < 3; i++) {
          assertEquals(referenceRow[i], decodedRow[i]);
        }
      }

      count++;
    }

    assertEquals(reference.size(), count);
  }

  @Test
  public void testWrite() throws Exception {
    FakeHBaseConnection conn = new FakeHBaseConnection();

    MappingAdmin ma = initMappingAdmin(conn);
    ma.createTestMapping();
    assertTrue(ma.mappingExists("MarksTestTable", "MarksTestMapping"));

    List<String> colFamilies = new ArrayList<String>();
    colFamilies.add("Family1");
    colFamilies.add("Family2");
    conn.createTable("MarksTestTable", colFamilies, null);
    assertTrue(conn.tableExists("MarksTestTable"));

    Mapping tableMapping = ma.getMapping("MarksTestTable", "MarksTestMapping");
    assertTrue(tableMapping != null);

    List<HBaseValueMeta> subsetOfCols = new ArrayList<HBaseValueMeta>();
    Map<String, HBaseValueMeta> colsMappedByAlias = tableMapping
        .getMappedColumns();
    assertTrue(colsMappedByAlias.size() > 0);

    HBaseValueMeta keyMeta = new HBaseValueMeta("BogusFamily"
        + HBaseValueMeta.SEPARATOR + tableMapping.getKeyName()
        + HBaseValueMeta.SEPARATOR + tableMapping.getKeyName(),
        ValueMetaInterface.TYPE_INTEGER, -1, -1);
    keyMeta.setKey(true);
    subsetOfCols.add(keyMeta);

    HBaseValueMeta one = colsMappedByAlias.get("first_string_column");
    assertTrue(one != null);
    subsetOfCols.add(one);
    HBaseValueMeta two = colsMappedByAlias.get("first_unsigned_int_column");
    assertTrue(two != null);
    subsetOfCols.add(two);

    CommonHBaseBytesUtil bu = new CommonHBaseBytesUtil();

    conn.newTargetTable("MarksTestTable", null);
    RowMetaInterface inputRowMeta = new RowMeta();

    for (HBaseValueMeta hbv : subsetOfCols) {
      inputRowMeta.addValueMeta(hbv);
    }

    Object[] inRow = RowDataUtil.allocateRowData(3);

    List<Object[]> reference = new ArrayList<Object[]>();
    for (int i = 0; i < 100; i++) {
      inRow = RowDataUtil.allocateRowData(3);
      inRow[0] = new Long(i);
      inRow[1] = "A string value " + i;
      inRow[2] = new Long(i * 10);
      reference.add(inRow);

      assertTrue(HBaseOutputData.initializeNewPut(inputRowMeta, 0, inRow,
          tableMapping, bu, conn, true));

      HBaseOutputData.addColumnsToPut(inputRowMeta, inRow, 0,
          colsMappedByAlias, conn, bu);
      conn.executeTargetTablePut();
    }

    conn.closeTargetTable();

    checkTableContents(conn, tableMapping, colsMappedByAlias, subsetOfCols, bu,
        reference, inputRowMeta);
  }

  @Test
  public void testWriteNullRowKey() throws Exception {
    FakeHBaseConnection conn = new FakeHBaseConnection();

    MappingAdmin ma = initMappingAdmin(conn);
    ma.createTestMapping();
    assertTrue(ma.mappingExists("MarksTestTable", "MarksTestMapping"));

    List<String> colFamilies = new ArrayList<String>();
    colFamilies.add("Family1");
    colFamilies.add("Family2");
    conn.createTable("MarksTestTable", colFamilies, null);
    assertTrue(conn.tableExists("MarksTestTable"));

    Mapping tableMapping = ma.getMapping("MarksTestTable", "MarksTestMapping");
    assertTrue(tableMapping != null);

    List<HBaseValueMeta> subsetOfCols = new ArrayList<HBaseValueMeta>();
    Map<String, HBaseValueMeta> colsMappedByAlias = tableMapping
        .getMappedColumns();
    assertTrue(colsMappedByAlias.size() > 0);

    HBaseValueMeta keyMeta = new HBaseValueMeta("BogusFamily"
        + HBaseValueMeta.SEPARATOR + tableMapping.getKeyName()
        + HBaseValueMeta.SEPARATOR + tableMapping.getKeyName(),
        ValueMetaInterface.TYPE_INTEGER, -1, -1);
    keyMeta.setKey(true);
    subsetOfCols.add(keyMeta);

    HBaseValueMeta one = colsMappedByAlias.get("first_string_column");
    assertTrue(one != null);
    subsetOfCols.add(one);
    HBaseValueMeta two = colsMappedByAlias.get("first_unsigned_int_column");
    assertTrue(two != null);
    subsetOfCols.add(two);

    CommonHBaseBytesUtil bu = new CommonHBaseBytesUtil();

    conn.newTargetTable("MarksTestTable", null);
    RowMetaInterface inputRowMeta = new RowMeta();

    for (HBaseValueMeta hbv : subsetOfCols) {
      inputRowMeta.addValueMeta(hbv);
    }

    Object[] inRow = RowDataUtil.allocateRowData(3);
    inRow[0] = null;
    inRow[1] = "A string value ";
    inRow[2] = new Long(10);

    assertFalse(HBaseOutputData.initializeNewPut(inputRowMeta, 0, inRow,
        tableMapping, bu, conn, true));
  }
}
