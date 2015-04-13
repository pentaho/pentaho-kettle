/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2015 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.core.database;

import org.junit.Test;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaString;

import static org.junit.Assert.assertEquals;

/**
 * User: Dzmitry Stsiapanau Date: 1/14/14 Time: 5:08 PM
 */
public class HypersonicDatabaseMetaTest {
  private HypersonicDatabaseMeta hypersonicDatabaseMeta;
  private HypersonicDatabaseMeta hypersonicDatabaseMetaQouting;
  private HypersonicDatabaseMeta hypersonicDatabaseMetaUppercase;
  private String tableName = "teST";
  private String sequenceName = "seQuence";
  private String schemaName = "SCHema";

  public HypersonicDatabaseMetaTest() {
    hypersonicDatabaseMeta = new HypersonicDatabaseMeta();
    hypersonicDatabaseMetaQouting = new HypersonicDatabaseMeta();
    hypersonicDatabaseMetaQouting.setQuoteAllFields( true );
    hypersonicDatabaseMetaUppercase = new HypersonicDatabaseMeta();
    hypersonicDatabaseMetaUppercase.setForcingIdentifiersToUpperCase( true );
  }

  @Test
  public void testGetSQLSequenceExists() throws Exception {
    String sql = hypersonicDatabaseMeta.getSQLSequenceExists( sequenceName );
    String expectedSql = "SELECT * FROM INFORMATION_SCHEMA.SYSTEM_SEQUENCES WHERE SEQUENCE_NAME = 'seQuence'";
    assertEquals( expectedSql, sql );
    sql = hypersonicDatabaseMetaQouting.getSQLSequenceExists( sequenceName );
    assertEquals( expectedSql, sql );
    sql = hypersonicDatabaseMetaUppercase.getSQLSequenceExists( sequenceName );
    assertEquals( expectedSql, sql );
  }

  @Test
  public void testGetSQLCurrentSequenceValue() throws Exception {
    String sql = hypersonicDatabaseMeta.getSQLCurrentSequenceValue( sequenceName );
    String expectedSql =
        "SELECT seQuence.currval FROM INFORMATION_SCHEMA.SYSTEM_SEQUENCES WHERE SEQUENCE_NAME = 'seQuence'";
    assertEquals( expectedSql, sql );
    sql = hypersonicDatabaseMetaQouting.getSQLCurrentSequenceValue( sequenceName );
    assertEquals( expectedSql, sql );
    sql = hypersonicDatabaseMetaUppercase.getSQLCurrentSequenceValue( sequenceName );
    assertEquals( expectedSql, sql );
  }

  @Test
  public void testGetSQLNextSequenceValue() throws Exception {
    String sql = hypersonicDatabaseMeta.getSQLNextSequenceValue( sequenceName );
    String expectedSql =
        "SELECT NEXT VALUE FOR seQuence FROM INFORMATION_SCHEMA.SYSTEM_SEQUENCES WHERE SEQUENCE_NAME = 'seQuence'";
    assertEquals( expectedSql, sql );
    sql = hypersonicDatabaseMetaQouting.getSQLNextSequenceValue( sequenceName );
    assertEquals( expectedSql, sql );
    sql = hypersonicDatabaseMetaUppercase.getSQLNextSequenceValue( sequenceName );
    assertEquals( expectedSql, sql );
  }

  @Test
  public void testGetSQLQueryFields() throws Exception {
    String sql = hypersonicDatabaseMeta.getSQLQueryFields( tableName );
    String expectedSql = "SELECT * FROM teST";
    assertEquals( expectedSql, sql );
    sql = hypersonicDatabaseMetaQouting.getSQLQueryFields( tableName );
    assertEquals( expectedSql, sql );
    sql = hypersonicDatabaseMetaUppercase.getSQLQueryFields( tableName );
    assertEquals( expectedSql, sql );
  }

  @Test
  public void testGetSchemaTableCombination() throws Exception {
    String sql = hypersonicDatabaseMeta.getSchemaTableCombination( schemaName, tableName );
    String expectedSql = "SCHema.teST";
    assertEquals( expectedSql, sql );
    sql = hypersonicDatabaseMetaQouting.getSchemaTableCombination( schemaName, tableName );
    assertEquals( expectedSql, sql );
    sql = hypersonicDatabaseMetaUppercase.getSchemaTableCombination( schemaName, tableName );
    assertEquals( expectedSql, sql );
  }

  @Test
  public void testGetTruncateTableStatement() throws Exception {
    String sql = hypersonicDatabaseMeta.getTruncateTableStatement( tableName );
    String expectedSql = "TRUNCATE TABLE teST";
    assertEquals( expectedSql, sql );
    sql = hypersonicDatabaseMetaQouting.getTruncateTableStatement( tableName );
    assertEquals( expectedSql, sql );
    sql = hypersonicDatabaseMetaUppercase.getTruncateTableStatement( tableName );
    assertEquals( expectedSql, sql );
  }

  @Test
  public void testGetFieldDefinition() throws Exception {
    ValueMetaInterface vm = new ValueMetaString();
    String sql = hypersonicDatabaseMeta.getFieldDefinition( vm, null, null, false, false, false );
    String expectedSql = "VARCHAR()";
    assertEquals( "Check PDI-11461 without length", expectedSql, sql );
    vm.setLength( DatabaseMeta.CLOB_LENGTH - 1 );
    sql = hypersonicDatabaseMeta.getFieldDefinition( vm, null, null, false, false, false );
    expectedSql = "VARCHAR(" + ( DatabaseMeta.CLOB_LENGTH - 1 ) + ")";
    assertEquals( "Check PDI-11461 with length", expectedSql, sql );
    vm.setLength( DatabaseMeta.CLOB_LENGTH );
    sql = hypersonicDatabaseMeta.getFieldDefinition( vm, null, null, false, false, false );
    expectedSql = "LONGVARCHAR";
    assertEquals( "Check PDI-11461 with clob/text length", expectedSql, sql );
  }
}
