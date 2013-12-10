/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2013 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.core.sql;

import java.util.List;

import org.pentaho.di.core.exception.KettleSQLException;
import org.pentaho.di.core.jdbc.ThinUtil;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;

import junit.framework.TestCase;

public class SQLTest extends TestCase {

  public void testSql01() throws KettleSQLException {
    RowMetaInterface rowMeta = generateTest3RowMeta();

    String sqlString = "SELECT A, B, C\nFROM Service\nWHERE B > 5\nORDER BY B DESC";

    SQL sql = new SQL( sqlString );

    assertEquals( "Service", sql.getServiceName() );
    sql.parse( rowMeta );

    assertEquals( "A, B, C", sql.getSelectClause() );
    List<SQLField> selectFields = sql.getSelectFields().getFields();
    assertEquals( 3, selectFields.size() );

    assertEquals( "B > 5", sql.getWhereClause() );
    SQLCondition whereCondition = sql.getWhereCondition();
    assertNotNull( whereCondition.getCondition() );

    assertNull( sql.getGroupClause() );
    assertNull( sql.getHavingClause() );
    assertEquals( "B DESC", sql.getOrderClause() );
    List<SQLField> orderFields = sql.getOrderFields().getFields();
    assertEquals( 1, orderFields.size() );
    SQLField orderField = orderFields.get( 0 );
    assertTrue( orderField.isOrderField() );
    assertFalse( orderField.isAscending() );
    assertNull( orderField.getAlias() );
    assertEquals( "B", orderField.getValueMeta().getName().toUpperCase() );
  }

  public void testSql02() throws KettleSQLException {
    RowMetaInterface rowMeta = generateTest3RowMeta();

    String sqlString = "SELECT A as \"FROM\", B as \"TO\", C\nFROM Service\nWHERE B > 5\nORDER BY B DESC";

    SQL sql = new SQL( sqlString );

    assertEquals( "Service", sql.getServiceName() );
    sql.parse( rowMeta );

    assertEquals( "A as \"FROM\", B as \"TO\", C", sql.getSelectClause() );
    assertEquals( "B > 5", sql.getWhereClause() );
    assertNull( sql.getGroupClause() );
    assertNull( sql.getHavingClause() );
    assertEquals( "B DESC", sql.getOrderClause() );
  }

  public void testSql03() throws KettleSQLException {
    RowMetaInterface rowMeta = generateTest3RowMeta();

    String sqlString =
      "SELECT A as \"FROM\", B as \"TO\", C, COUNT(*)\nFROM Service\nWHERE B > 5\nGROUP BY A,B,C\nHAVING COUNT(*) > 100\nORDER BY A,B,C";

    SQL sql = new SQL( sqlString );

    assertEquals( "Service", sql.getServiceName() );
    sql.parse( rowMeta );

    assertEquals( "A as \"FROM\", B as \"TO\", C, COUNT(*)", sql.getSelectClause() );
    assertEquals( "B > 5", sql.getWhereClause() );
    assertEquals( "A,B,C", sql.getGroupClause() );
    assertEquals( "COUNT(*) > 100", sql.getHavingClause() );
    assertEquals( "A,B,C", sql.getOrderClause() );
  }

  public void testSql04() throws KettleSQLException {
    RowMetaInterface rowMeta = generateTest3RowMeta();

    String sqlString = "SELECT *\nFROM Service\nWHERE B > 5\nORDER BY B DESC";

    SQL sql = new SQL( sqlString );

    assertEquals( "Service", sql.getServiceName() );
    sql.parse( rowMeta );

    assertEquals( "*", sql.getSelectClause() );
    List<SQLField> selectFields = sql.getSelectFields().getFields();
    assertEquals( 3, selectFields.size() );

    assertEquals( "B > 5", sql.getWhereClause() );
    SQLCondition whereCondition = sql.getWhereCondition();
    assertNotNull( whereCondition.getCondition() );

    assertNull( sql.getGroupClause() );
    assertNull( sql.getHavingClause() );
    assertEquals( "B DESC", sql.getOrderClause() );
    List<SQLField> orderFields = sql.getOrderFields().getFields();
    assertEquals( 1, orderFields.size() );
    SQLField orderField = orderFields.get( 0 );
    assertTrue( orderField.isOrderField() );
    assertFalse( orderField.isAscending() );
    assertNull( orderField.getAlias() );
    assertEquals( "B", orderField.getValueMeta().getName().toUpperCase() );
  }

  public void testSql05() throws KettleSQLException {
    RowMetaInterface rowMeta = generateTest3RowMeta();

    String sqlString = "SELECT count(*) as NrOfRows FROM Service";

    SQL sql = new SQL( sqlString );

    assertEquals( "Service", sql.getServiceName() );
    sql.parse( rowMeta );

    assertEquals( "count(*) as NrOfRows", sql.getSelectClause() );
    List<SQLField> selectFields = sql.getSelectFields().getFields();
    assertEquals( 1, selectFields.size() );
    SQLField countField = selectFields.get( 0 );
    assertTrue( countField.isCountStar() );
    assertEquals( "*", countField.getField() );
    assertEquals( "NrOfRows", countField.getAlias() );

    assertNull( sql.getGroupClause() );
    assertNotNull( sql.getGroupFields() );
    assertNull( sql.getHavingClause() );
    assertNull( sql.getOrderClause() );
  }

  /**
   * Query generated by interactive reporting.
   *
   * @throws KettleSQLException
   */
  public void testSql06() throws KettleSQLException {
    RowMetaInterface rowMeta = generateServiceRowMeta();

    String sqlString =
      "SELECT DISTINCT\n          BT_SERVICE_SERVICE.Category AS COL0\n         ,BT_SERVICE_SERVICE.Country AS COL1\n         ,BT_SERVICE_SERVICE.products_sold AS COL2\n         ,BT_SERVICE_SERVICE.sales_amount AS COL3\n"
        + "FROM \n          Service BT_SERVICE_SERVICE\n" + "ORDER BY\n          COL0";

    SQL sql = new SQL( ThinUtil.stripNewlines( sqlString ) );

    assertEquals( "Service", sql.getServiceName() );
    sql.parse( rowMeta );

    assertTrue( sql.getSelectFields().isDistinct() );
    List<SQLField> selectFields = sql.getSelectFields().getFields();
    assertEquals( 4, selectFields.size() );
    assertEquals( "COL0", selectFields.get( 0 ).getAlias() );
    assertEquals( "COL1", selectFields.get( 1 ).getAlias() );
    assertEquals( "COL2", selectFields.get( 2 ).getAlias() );
    assertEquals( "COL3", selectFields.get( 3 ).getAlias() );

    List<SQLField> orderFields = sql.getOrderFields().getFields();
    assertEquals( 1, orderFields.size() );
  }

  /**
   * Query generated by Mondrian / Analyzer.
   *
   * @throws KettleSQLException
   */
  public void testSql07() throws KettleSQLException {
    RowMetaInterface rowMeta = generateServiceRowMeta();

    String sqlString =
      "select \"Service\".\"Category\" as \"c0\" from \"Service\" as \"Service\" group by \"Service\".\"Category\" order by CASE WHEN \"Service\".\"Category\" IS NULL THEN 1 ELSE 0 END, \"Service\".\"Category\" ASC";

    SQL sql = new SQL( ThinUtil.stripNewlines( sqlString ) );

    assertEquals( "Service", sql.getServiceName() );
    sql.parse( rowMeta );

    assertFalse( sql.getSelectFields().isDistinct() );
    List<SQLField> selectFields = sql.getSelectFields().getFields();
    assertEquals( 1, selectFields.size() );
    assertEquals( "c0", selectFields.get( 0 ).getAlias() );

    List<SQLField> orderFields = sql.getOrderFields().getFields();
    assertEquals( 2, orderFields.size() );
  }

  //
  /**
   * Query generated by PIR
   *
   * @throws KettleSQLException
   */
  public void testSql08() throws KettleSQLException {
    RowMetaInterface rowMeta = generateZipsRowMeta();

    String sqlString =
      "SELECT            BT_MONGODB_MONGODB.state AS COL0          ,SUM(BT_MONGODB_MONGODB.rows) AS COL1 FROM            MongoDB BT_MONGODB_MONGODB GROUP BY            BT_MONGODB_MONGODB.state ORDER BY            COL1 DESC";

    SQL sql = new SQL( ThinUtil.stripNewlines( sqlString ) );

    assertEquals( "MongoDB", sql.getServiceName() );
    sql.parse( rowMeta );

    assertFalse( sql.getSelectFields().isDistinct() );
    List<SQLField> selectFields = sql.getSelectFields().getFields();
    assertEquals( 2, selectFields.size() );
    assertEquals( "state", selectFields.get( 0 ).getField() );
    assertEquals( "COL0", selectFields.get( 0 ).getAlias() );
    assertEquals( "rows", selectFields.get( 1 ).getField() );
    assertEquals( "COL1", selectFields.get( 1 ).getAlias() );

    List<SQLField> orderFields = sql.getOrderFields().getFields();
    assertEquals( 1, orderFields.size() );
  }

  /**
   * Tests schema.table format
   *
   * @throws KettleSQLException
   */
  public void testSql09() throws KettleSQLException {
    RowMetaInterface rowMeta = generateServiceRowMeta();

    String sqlString = "SELECT Category, Country, products_sold, sales_amount FROM Kettle.Service";

    SQL sql = new SQL( ThinUtil.stripNewlines( sqlString ) );

    assertEquals( "Kettle", sql.getNamespace() );
    assertEquals( "Service", sql.getServiceName() );
    sql.parse( rowMeta );

    assertFalse( sql.getSelectFields().isDistinct() );
    List<SQLField> selectFields = sql.getSelectFields().getFields();
    assertEquals( 4, selectFields.size() );
    assertEquals( "Category", selectFields.get( 0 ).getField() );
    assertEquals( "Country", selectFields.get( 1 ).getField() );
    assertEquals( "products_sold", selectFields.get( 2 ).getField() );
    assertEquals( "sales_amount", selectFields.get( 3 ).getField() );
  }

  /**
   * Tests schema."table" format
   *
   * @throws KettleSQLException
   */
  public void testSql10() throws KettleSQLException {
    RowMetaInterface rowMeta = generateServiceRowMeta();

    String sqlString = "SELECT Category, Country, products_sold, sales_amount FROM Kettle.\"Service\"";

    SQL sql = new SQL( ThinUtil.stripNewlines( sqlString ) );

    assertEquals( "Kettle", sql.getNamespace() );
    assertEquals( "Service", sql.getServiceName() );
    sql.parse( rowMeta );

    assertFalse( sql.getSelectFields().isDistinct() );
    List<SQLField> selectFields = sql.getSelectFields().getFields();
    assertEquals( 4, selectFields.size() );
    assertEquals( "Category", selectFields.get( 0 ).getField() );
    assertEquals( "Country", selectFields.get( 1 ).getField() );
    assertEquals( "products_sold", selectFields.get( 2 ).getField() );
    assertEquals( "sales_amount", selectFields.get( 3 ).getField() );
  }

  /**
   * Tests "schema".table format
   *
   * @throws KettleSQLException
   */
  public void testSql11() throws KettleSQLException {
    RowMetaInterface rowMeta = generateServiceRowMeta();

    String sqlString = "SELECT Category, Country, products_sold, sales_amount FROM \"Kettle\".Service";

    SQL sql = new SQL( ThinUtil.stripNewlines( sqlString ) );

    assertEquals( "Kettle", sql.getNamespace() );
    assertEquals( "Service", sql.getServiceName() );
    sql.parse( rowMeta );

    assertFalse( sql.getSelectFields().isDistinct() );
    List<SQLField> selectFields = sql.getSelectFields().getFields();
    assertEquals( 4, selectFields.size() );
    assertEquals( "Category", selectFields.get( 0 ).getField() );
    assertEquals( "Country", selectFields.get( 1 ).getField() );
    assertEquals( "products_sold", selectFields.get( 2 ).getField() );
    assertEquals( "sales_amount", selectFields.get( 3 ).getField() );
  }

  /**
   * Tests "schema"."table" format
   *
   * @throws KettleSQLException
   */
  public void testSql12() throws KettleSQLException {
    RowMetaInterface rowMeta = generateServiceRowMeta();

    String sqlString = "SELECT Category, Country, products_sold, sales_amount FROM \"Kettle\".\"Service\"";

    SQL sql = new SQL( ThinUtil.stripNewlines( sqlString ) );

    assertEquals( "Kettle", sql.getNamespace() );
    assertEquals( "Service", sql.getServiceName() );
    sql.parse( rowMeta );

    assertFalse( sql.getSelectFields().isDistinct() );
    List<SQLField> selectFields = sql.getSelectFields().getFields();
    assertEquals( 4, selectFields.size() );
    assertEquals( "Category", selectFields.get( 0 ).getField() );
    assertEquals( "Country", selectFields.get( 1 ).getField() );
    assertEquals( "products_sold", selectFields.get( 2 ).getField() );
    assertEquals( "sales_amount", selectFields.get( 3 ).getField() );
  }

  /**
   * Tests quoting in literal strings
   *
   * @throws KettleSQLException
   */
  public void testSql13() throws KettleSQLException {
    RowMetaInterface rowMeta = generateGettingStartedRowMeta();

    String sqlString =
      "SELECT * FROM \"GETTING_STARTED\" WHERE \"GETTING_STARTED\".\"CUSTOMERNAME\" = 'ANNA''S DECORATIONS, LTD'";

    SQL sql = new SQL( ThinUtil.stripNewlines( sqlString ) );

    assertEquals( "GETTING_STARTED", sql.getServiceName() );
    sql.parse( rowMeta );

    assertNotNull( sql.getWhereCondition() );
    assertEquals( "CUSTOMERNAME", sql.getWhereCondition().getCondition().getLeftValuename() );
    assertEquals( "ANNA'S DECORATIONS, LTD", sql.getWhereCondition().getCondition().getRightExactString() );

  }

  /**
   * Tests empty literal strings
   *
   * @throws KettleSQLException
   */
  public void testSql14() throws KettleSQLException {
    RowMetaInterface rowMeta = generateGettingStartedRowMeta();

    String sqlString = "SELECT * FROM \"GETTING_STARTED\" WHERE \"GETTING_STARTED\".\"CUSTOMERNAME\" = ''";

    SQL sql = new SQL( ThinUtil.stripNewlines( sqlString ) );

    assertEquals( "GETTING_STARTED", sql.getServiceName() );
    sql.parse( rowMeta );

    assertNotNull( sql.getWhereCondition() );
    assertEquals( "CUSTOMERNAME", sql.getWhereCondition().getCondition().getLeftValuename() );
    assertEquals( "", sql.getWhereCondition().getCondition().getRightExactString() );

  }

  /**
   * Tests crazy quoting in literal strings
   *
   * @throws KettleSQLException
   */
  public void testSql15() throws KettleSQLException {
    RowMetaInterface rowMeta = generateGettingStartedRowMeta();

    String sqlString =
      "SELECT * FROM \"GETTING_STARTED\" WHERE \"GETTING_STARTED\".\"CUSTOMERNAME\" = ''''''''''''";

    SQL sql = new SQL( ThinUtil.stripNewlines( sqlString ) );

    assertEquals( "GETTING_STARTED", sql.getServiceName() );
    sql.parse( rowMeta );

    assertNotNull( sql.getWhereCondition() );
    assertEquals( "CUSTOMERNAME", sql.getWhereCondition().getCondition().getLeftValuename() );
    assertEquals( "'''''", sql.getWhereCondition().getCondition().getRightExactString() );

  }

  /**
   * Tests quoting in literal strings in IN clause
   *
   * @throws KettleSQLException
   */
  public void testSql16() throws KettleSQLException {
    RowMetaInterface rowMeta = generateGettingStartedRowMeta();

    String sqlString =
      "SELECT * FROM \"GETTING_STARTED\" WHERE \"GETTING_STARTED\".\"CUSTOMERNAME\" IN ('ANNA''S DECORATIONS, LTD', 'MEN ''R'' US RETAILERS, Ltd.' )";

    SQL sql = new SQL( ThinUtil.stripNewlines( sqlString ) );

    assertEquals( "GETTING_STARTED", sql.getServiceName() );
    sql.parse( rowMeta );

    assertNotNull( sql.getWhereCondition() );
    assertEquals( "CUSTOMERNAME", sql.getWhereCondition().getCondition().getLeftValuename() );
    assertEquals( "ANNA'S DECORATIONS, LTD;MEN 'R' US RETAILERS, Ltd.", sql
      .getWhereCondition().getCondition().getRightExactString() );

  }

  /**
   * Tests semi-coluns and quoting in literal strings in IN clause
   *
   * @throws KettleSQLException
   */
  public void testSql17() throws KettleSQLException {
    RowMetaInterface rowMeta = generateGettingStartedRowMeta();

    String sqlString =
      "SELECT * FROM \"GETTING_STARTED\" WHERE \"GETTING_STARTED\".\"CUSTOMERNAME\" IN ('ANNA''S DECORATIONS; LTD', 'MEN ''R'' US RETAILERS; Ltd.' )";

    SQL sql = new SQL( ThinUtil.stripNewlines( sqlString ) );

    assertEquals( "GETTING_STARTED", sql.getServiceName() );
    sql.parse( rowMeta );

    assertNotNull( sql.getWhereCondition() );
    assertEquals( "CUSTOMERNAME", sql.getWhereCondition().getCondition().getLeftValuename() );
    assertEquals( "ANNA'S DECORATIONS\\; LTD;MEN 'R' US RETAILERS\\; Ltd.", sql
      .getWhereCondition().getCondition().getRightExactString() );

  }

  public static RowMetaInterface generateTest2RowMeta() {
    RowMetaInterface rowMeta = new RowMeta();
    rowMeta.addValueMeta( new ValueMeta( "A", ValueMetaInterface.TYPE_STRING, 50 ) );
    rowMeta.addValueMeta( new ValueMeta( "B", ValueMetaInterface.TYPE_INTEGER, 7 ) );
    return rowMeta;
  }

  public static RowMetaInterface generateTest3RowMeta() {
    RowMetaInterface rowMeta = new RowMeta();
    rowMeta.addValueMeta( new ValueMeta( "A", ValueMetaInterface.TYPE_STRING, 50 ) );
    rowMeta.addValueMeta( new ValueMeta( "B", ValueMetaInterface.TYPE_INTEGER, 7 ) );
    rowMeta.addValueMeta( new ValueMeta( "C", ValueMetaInterface.TYPE_INTEGER, 7 ) );
    return rowMeta;
  }

  public static RowMetaInterface generateTest4RowMeta() {
    RowMetaInterface rowMeta = new RowMeta();
    rowMeta.addValueMeta( new ValueMeta( "A", ValueMetaInterface.TYPE_STRING, 50 ) );
    rowMeta.addValueMeta( new ValueMeta( "B", ValueMetaInterface.TYPE_INTEGER, 7 ) );
    rowMeta.addValueMeta( new ValueMeta( "C", ValueMetaInterface.TYPE_STRING, 50 ) );
    rowMeta.addValueMeta( new ValueMeta( "D", ValueMetaInterface.TYPE_INTEGER, 7 ) );
    return rowMeta;
  }

  public static RowMetaInterface generateServiceRowMeta() {
    RowMetaInterface rowMeta = new RowMeta();
    rowMeta.addValueMeta( new ValueMeta( "Category", ValueMetaInterface.TYPE_STRING, 50 ) );
    rowMeta.addValueMeta( new ValueMeta( "Country", ValueMetaInterface.TYPE_INTEGER, 7 ) );
    rowMeta.addValueMeta( new ValueMeta( "products_sold", ValueMetaInterface.TYPE_INTEGER, 7 ) );
    rowMeta.addValueMeta( new ValueMeta( "sales_amount", ValueMetaInterface.TYPE_NUMBER, 7, 2 ) );
    return rowMeta;
  }

  public static RowMetaInterface generateZipsRowMeta() {
    RowMetaInterface rowMeta = new RowMeta();
    rowMeta.addValueMeta( new ValueMeta( "zip", ValueMetaInterface.TYPE_STRING, 5 ) );
    rowMeta.addValueMeta( new ValueMeta( "city", ValueMetaInterface.TYPE_INTEGER, 50 ) );
    rowMeta.addValueMeta( new ValueMeta( "state", ValueMetaInterface.TYPE_STRING, 2 ) );
    rowMeta.addValueMeta( new ValueMeta( "rows", ValueMetaInterface.TYPE_INTEGER, 1 ) );
    return rowMeta;
  }

  public static RowMetaInterface generateGettingStartedRowMeta() {
    RowMetaInterface rowMeta = new RowMeta();
    rowMeta.addValueMeta( new ValueMeta( "CUSTOMERNAME", ValueMetaInterface.TYPE_STRING, 50 ) );
    rowMeta.addValueMeta( new ValueMeta( "MONTH_ID", ValueMetaInterface.TYPE_INTEGER, 4 ) );
    rowMeta.addValueMeta( new ValueMeta( "YEAR_ID", ValueMetaInterface.TYPE_INTEGER, 2 ) );
    rowMeta.addValueMeta( new ValueMeta( "STATE", ValueMetaInterface.TYPE_STRING, 30 ) );
    return rowMeta;
  }
}
