/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/

package org.pentaho.di.trans.steps.tableinput;

import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaBase;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepHelper;
import org.pentaho.di.trans.step.StepInterface;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

@RunWith( MockitoJUnitRunner.class )
public class TableInputHelperTest {

  @Mock private TransMeta transMeta;
  @Mock private DatabaseMeta databaseMeta;

  private static final String SCHEMA_TABLE_SEPARATOR = ".";
  private Map<String, String> queryParams;
  private final String connection = "testConnection";
  private final String schema = "testSchema";
  private final String table = "testTable";

  private TableInputHelper tableInputHelper;

  @Before
  public void setUp() {
    tableInputHelper = new TableInputHelper();
    queryParams = new HashMap<>();
    queryParams.put( "connection", connection );
    queryParams.put( "schema", schema );
    queryParams.put( "table", table );
  }

  @Test
  public void testHandleStepAction_GetColumns_Success() {
    String schemaTableCombination = schema + SCHEMA_TABLE_SEPARATOR + table;

    try ( MockedConstruction<Database> ignored = mockConstruction( Database.class, ( mock, context ) -> {
      doNothing().when( mock ).connect();
      when( mock.getQueryFields( anyString(), anyBoolean() ) ).thenReturn( null );
    } ) ) {
      when( transMeta.findDatabase( connection ) ).thenReturn( databaseMeta );
      when( databaseMeta.getQuotedSchemaTableCombination( anyString(), anyString() ) ).thenReturn(
        schemaTableCombination );

      JSONObject response = tableInputHelper.handleStepAction( "getColumns", transMeta, queryParams );

      assertEquals( StepInterface.SUCCESS_RESPONSE, response.get( "actionStatus" ) );
      assertNotNull( response.get( "sql" ) );
    }
  }

  @Test
  public void testHandleStepAction_InvalidMethod() {
    JSONObject response = tableInputHelper.handleStepAction( "invalidMethod", transMeta, queryParams );

    assertEquals( BaseStepHelper.FAILURE_METHOD_NOT_FOUND_RESPONSE, response.get( BaseStepHelper.ACTION_STATUS ) );
  }

  @Test
  public void testHandleStepAction_ExceptionHandling() {
    when( transMeta.findDatabase( connection ) ).thenThrow( new RuntimeException( "Test exception" ) );

    JSONObject response = tableInputHelper.handleStepAction( "getColumns", transMeta, queryParams );

    assertEquals( BaseStepHelper.FAILURE_RESPONSE, response.get( BaseStepHelper.ACTION_STATUS ) );
  }

  @Test
  public void testGetColumnsAction_WithFields() throws KettleException {
    String column1 = "sampleColumn1";
    String column2 = "sampleColumn2";
    String schemaTableCombination = schema + SCHEMA_TABLE_SEPARATOR + table;

    try ( MockedConstruction<Database> ignored = mockConstruction( Database.class, ( mock, context ) -> {
      RowMeta rowMeta = new RowMeta();
      List<ValueMetaInterface> valueMetaInterfaceList = new ArrayList<>();
      ValueMetaBase valueMetaBase = new ValueMetaBase( column1, ValueMetaInterface.TYPE_INTEGER );
      ValueMetaBase valueMetaBase2 = new ValueMetaBase( column2, ValueMetaInterface.TYPE_INTEGER );
      valueMetaInterfaceList.add( valueMetaBase );
      valueMetaInterfaceList.add( valueMetaBase2 );

      rowMeta.setValueMetaList( valueMetaInterfaceList );

      doNothing().when( mock ).connect();
      when( mock.getQueryFields( anyString(), anyBoolean() ) ).thenReturn( rowMeta );
    } ) ) {
      when( transMeta.findDatabase( connection ) ).thenReturn( databaseMeta );
      when( databaseMeta.getQuotedSchemaTableCombination( anyString(), anyString() ) ).thenReturn(
        schemaTableCombination );
      when( databaseMeta.getStartQuote() ).thenReturn( "" );
      doCallRealMethod().when( databaseMeta ).quoteField( anyString() );

      JSONObject response = tableInputHelper.getColumnsAction( transMeta, queryParams );

      assertEquals( StepInterface.SUCCESS_RESPONSE, response.get( "actionStatus" ) );
      assertNotNull( response.get( "sql" ) );
      String sql = (String) response.get( "sql" );
      assertTrue( sql.contains( "SELECT" ) );
      assertTrue( sql.contains( column1 ) );
      assertTrue( sql.contains( column2 ) );
    }
  }

  @Test
  public void testGetColumnsAction_WithNullFields() throws KettleException {
    String schemaTableCombination = schema + SCHEMA_TABLE_SEPARATOR + table;

    try ( MockedConstruction<Database> ignored = mockConstruction( Database.class, ( mock, context ) -> {
      doNothing().when( mock ).connect();
      when( mock.getQueryFields( anyString(), anyBoolean() ) ).thenReturn( null );
    } ) ) {
      when( transMeta.findDatabase( connection ) ).thenReturn( databaseMeta );
      when( databaseMeta.getQuotedSchemaTableCombination( anyString(), anyString() ) ).thenReturn(
        schemaTableCombination );

      JSONObject response = tableInputHelper.getColumnsAction( transMeta, queryParams );

      assertEquals( StepInterface.SUCCESS_RESPONSE, response.get( "actionStatus" ) );
      assertNotNull( response.get( "sql" ) );
      String sql = (String) response.get( "sql" );
      assertTrue( sql.contains( "SELECT *" ) );
      assertTrue( sql.contains( "FROM " + schemaTableCombination ) );
    }
  }

  @Test
  public void testGetColumnsAction_DatabaseException() {
    String schemaTableCombination = schema + SCHEMA_TABLE_SEPARATOR + table;

    try ( MockedConstruction<Database> ignored = mockConstruction( Database.class, ( mock, context ) -> {
      doThrow( new KettleDatabaseException() ).when( mock ).connect();
    } ) ) {
      when( transMeta.findDatabase( connection ) ).thenReturn( databaseMeta );
      when( databaseMeta.getQuotedSchemaTableCombination( anyString(), anyString() ) ).thenReturn(
        schemaTableCombination );

      try {
        tableInputHelper.getColumnsAction( transMeta, queryParams );
        fail( "Expected KettleException" );
      } catch ( KettleException e ) {
        assertNotNull( e.getCause() );
        assertTrue( e.getCause() instanceof KettleDatabaseException );
      }
    }
  }

  @Test
  public void testGetColumnsAction_EmptyFields() throws KettleException {
    String schemaTableCombination = schema + SCHEMA_TABLE_SEPARATOR + table;

    try ( MockedConstruction<Database> ignored = mockConstruction( Database.class, ( mock, context ) -> {
      RowMeta rowMeta = new RowMeta();
      rowMeta.setValueMetaList( new ArrayList<>() );

      doNothing().when( mock ).connect();
      when( mock.getQueryFields( anyString(), anyBoolean() ) ).thenReturn( rowMeta );
    } ) ) {
      when( transMeta.findDatabase( connection ) ).thenReturn( databaseMeta );
      when( databaseMeta.getQuotedSchemaTableCombination( anyString(), anyString() ) ).thenReturn(
        schemaTableCombination );

      JSONObject response = tableInputHelper.getColumnsAction( transMeta, queryParams );

      assertEquals( StepInterface.SUCCESS_RESPONSE, response.get( "actionStatus" ) );
      assertNotNull( response.get( "sql" ) );
      String sql = (String) response.get( "sql" );
      assertTrue( sql.contains( "SELECT" ) );
      assertTrue( sql.contains( "FROM " + schemaTableCombination ) );
    }
  }

  @Test
  public void testGetColumnsAction_InitialFailureStatus() throws KettleException {
    String schemaTableCombination = schema + SCHEMA_TABLE_SEPARATOR + table;

    try ( MockedConstruction<Database> ignored = mockConstruction( Database.class, ( mock, context ) -> {
      doNothing().when( mock ).connect();
      when( mock.getQueryFields( anyString(), anyBoolean() ) ).thenReturn( null );
    } ) ) {
      when( transMeta.findDatabase( connection ) ).thenReturn( databaseMeta );
      when( databaseMeta.getQuotedSchemaTableCombination( anyString(), anyString() ) ).thenReturn(
        schemaTableCombination );

      JSONObject response = tableInputHelper.getColumnsAction( transMeta, queryParams );

      // Method initially sets FAILURE but then overrides with SUCCESS
      assertEquals( StepInterface.SUCCESS_RESPONSE, response.get( "actionStatus" ) );
    }
  }
}
