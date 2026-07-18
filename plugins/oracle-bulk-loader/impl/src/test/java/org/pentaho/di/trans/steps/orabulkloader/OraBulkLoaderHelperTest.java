/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/


package org.pentaho.di.trans.steps.orabulkloader;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.SQLStatement;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepHelper;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith( MockitoJUnitRunner.class )
public class OraBulkLoaderHelperTest {

  @Mock private TransMeta transMeta;
  @Mock private StepMeta stepMeta;
  @Mock private OraBulkLoaderMeta meta;
  @Mock private SQLStatement sqlStatement;
  @Mock private RowMetaInterface prevFields;
  @Mock private DatabaseMeta databaseMeta;

  private OraBulkLoaderHelper helper;
  private Map<String, String> queryParams;

  @Before
  public void setUp() throws Exception {
    // Initialize Kettle environment to set up logging
    if ( !KettleEnvironment.isInitialized() ) {
      KettleEnvironment.init();
    }
    helper = new OraBulkLoaderHelper();
    queryParams = new HashMap<>();
  }

  @Test
  public void testHandleStepAction_GetSQL_Success() throws Exception {
    queryParams.put( OraBulkLoaderHelper.STEP_NAME, "oraStep" );
    queryParams.put( OraBulkLoaderHelper.CONNECTION, "conn" );

    when( transMeta.findStep( "oraStep" ) ).thenReturn( stepMeta );
    when( stepMeta.getStepMetaInterface() ).thenReturn( meta );
    when( transMeta.findDatabase( "conn" ) ).thenReturn( databaseMeta );
    when( transMeta.getPrevStepFields( "oraStep" ) ).thenReturn( validRowMeta() );
    when( meta.getFieldStream() ).thenReturn( new String[] { "col1" } );
    when( meta.getSQLStatements( any(), any(), any(), isNull(), isNull() ) ).thenReturn( sqlStatement );
    when( sqlStatement.hasError() ).thenReturn( false );
    when( sqlStatement.hasSQL() ).thenReturn( true );
    when( sqlStatement.getSQL() ).thenReturn( "CREATE TABLE x (...)" );

    JSONObject response = helper.handleStepAction( "getSQL", transMeta, queryParams );

    assertEquals( StepInterface.SUCCESS_RESPONSE, response.get( "actionStatus" ) );
    assertEquals( "CREATE TABLE x (...)", response.get( "sqlString" ) );
  }

  @Test
  public void testHandleStepAction_InvalidMethod() {
    JSONObject response = helper.handleStepAction( "invalidMethod", transMeta, queryParams );

    assertEquals( BaseStepHelper.FAILURE_METHOD_NOT_FOUND_RESPONSE, response.get( BaseStepHelper.ACTION_STATUS ) );
  }

  @Test
  public void testHandleStepAction_ExceptionHandling() {
    queryParams.put( OraBulkLoaderHelper.STEP_NAME, "oraStep" );
    when( transMeta.findStep( "oraStep" ) ).thenThrow( new RuntimeException( "Test exception" ) );

    JSONObject response = helper.handleStepAction( "getSQL", transMeta, queryParams );

    assertEquals( BaseStepHelper.FAILURE_RESPONSE, response.get( BaseStepHelper.ACTION_STATUS ) );
    assertEquals( "Test exception", response.get( OraBulkLoaderHelper.DETAILS ) );
  }

  @Test
  public void testGetSQLAction_Success_WithSQL() throws Exception {
    queryParams.put( OraBulkLoaderHelper.STEP_NAME, "oraStep" );
    queryParams.put( OraBulkLoaderHelper.CONNECTION, "conn" );

    when( transMeta.findStep( "oraStep" ) ).thenReturn( stepMeta );
    when( stepMeta.getStepMetaInterface() ).thenReturn( meta );
    when( transMeta.findDatabase( "conn" ) ).thenReturn( databaseMeta );
    when( transMeta.getPrevStepFields( "oraStep" ) ).thenReturn( validRowMeta() );
    when( meta.getFieldStream() ).thenReturn( new String[] { "col1" } );
    when( meta.getSQLStatements( any(), any(), any(), isNull(), isNull() ) ).thenReturn( sqlStatement );
    when( sqlStatement.hasError() ).thenReturn( false );
    when( sqlStatement.hasSQL() ).thenReturn( true );
    when( sqlStatement.getSQL() ).thenReturn( "CREATE TABLE x (...)" );

    JSONObject response = helper.getSQLAction( transMeta, queryParams );

    assertEquals( StepInterface.SUCCESS_RESPONSE, response.get( "actionStatus" ) );
    assertEquals( "CREATE TABLE x (...)", response.get( "sqlString" ) );
  }

  @Test
  public void testGetSQLAction_Success_NoSQLNeeded() throws Exception {
    queryParams.put( OraBulkLoaderHelper.STEP_NAME, "oraStep" );
    queryParams.put( OraBulkLoaderHelper.CONNECTION, "conn" );

    when( transMeta.findStep( "oraStep" ) ).thenReturn( stepMeta );
    when( stepMeta.getStepMetaInterface() ).thenReturn( meta );
    when( transMeta.findDatabase( "conn" ) ).thenReturn( databaseMeta );
    when( transMeta.getPrevStepFields( "oraStep" ) ).thenReturn( validRowMeta() );
    when( meta.getFieldStream() ).thenReturn( new String[] { "col1" } );
    when( meta.getSQLStatements( any(), any(), any(), isNull(), isNull() ) ).thenReturn( sqlStatement );
    when( sqlStatement.hasError() ).thenReturn( false );
    when( sqlStatement.hasSQL() ).thenReturn( false );

    JSONObject response = helper.getSQLAction( transMeta, queryParams );

    assertEquals( StepInterface.SUCCESS_RESPONSE, response.get( "actionStatus" ) );
    assertNotNull( response.get( OraBulkLoaderHelper.DETAILS ) );
  }

  @Test
  public void testGetSQLAction_SQLError() throws Exception {
    queryParams.put( OraBulkLoaderHelper.STEP_NAME, "oraStep" );
    queryParams.put( OraBulkLoaderHelper.CONNECTION, "conn" );

    when( transMeta.findStep( "oraStep" ) ).thenReturn( stepMeta );
    when( stepMeta.getStepMetaInterface() ).thenReturn( meta );
    when( transMeta.findDatabase( "conn" ) ).thenReturn( databaseMeta );
    when( transMeta.getPrevStepFields( "oraStep" ) ).thenReturn( validRowMeta() );
    when( meta.getFieldStream() ).thenReturn( new String[] { "col1" } );
    when( meta.getSQLStatements( any(), any(), any(), isNull(), isNull() ) ).thenReturn( sqlStatement );
    when( sqlStatement.hasError() ).thenReturn( true );
    when( sqlStatement.getError() ).thenReturn( "SQL Error occurred" );

    JSONObject response = helper.getSQLAction( transMeta, queryParams );

    assertEquals( StepInterface.FAILURE_RESPONSE, response.get( "actionStatus" ) );
    assertEquals( "SQL Error occurred", response.get( OraBulkLoaderHelper.DETAILS ) );
  }

  @Test
  public void testGetSQLAction_NullSQL() throws Exception {
    queryParams.put( OraBulkLoaderHelper.STEP_NAME, "oraStep" );
    queryParams.put( OraBulkLoaderHelper.CONNECTION, "conn" );

    when( transMeta.findStep( "oraStep" ) ).thenReturn( stepMeta );
    when( stepMeta.getStepMetaInterface() ).thenReturn( meta );
    when( transMeta.findDatabase( "conn" ) ).thenReturn( databaseMeta );
    when( transMeta.getPrevStepFields( "oraStep" ) ).thenReturn( prevFields );
    when( prevFields.size() ).thenReturn( 0 );

    JSONObject response = helper.getSQLAction( transMeta, queryParams );

    assertEquals( StepInterface.FAILURE_RESPONSE, response.get( "actionStatus" ) );
    assertNotNull( response.get( OraBulkLoaderHelper.DETAILS ) );
  }

  @Test
  public void testGetSqlActionKettleStepException() throws Exception {
    OraBulkLoaderHelper spyHelper = spy( helper );
    doThrow( new KettleStepException( "Kettle exception" ) ).when( spyHelper ).sql( any(), anyString(), anyString() );

    queryParams.put( OraBulkLoaderHelper.STEP_NAME, "oraStep" );
    queryParams.put( OraBulkLoaderHelper.CONNECTION, "conn" );

    JSONObject response = spyHelper.getSQLAction( transMeta, queryParams );

    assertEquals( StepInterface.FAILURE_METHOD_NOT_RESPONSE, response.get( "actionStatus" ) );
    assertEquals( "Kettle exception", response.get( OraBulkLoaderHelper.DETAILS ).toString().trim() );
  }

  @Test
  public void testSQL_NullStepName() throws Exception {
    assertNull( helper.sql( transMeta, null, "conn" ) );
  }

  @Test
  public void testSQL_EmptyStepName() throws Exception {
    assertNull( helper.sql( transMeta, "", "conn" ) );
  }

  @Test
  public void testSQL_MissingStepMeta() throws Exception {
    when( transMeta.findStep( "oraStep" ) ).thenReturn( null );
    assertNull( helper.sql( transMeta, "oraStep", "conn" ) );
  }

  @Test
  public void testSQL_WrongStepType() throws Exception {
    when( transMeta.findStep( "oraStep" ) ).thenReturn( stepMeta );
    when( stepMeta.getStepMetaInterface() ).thenReturn( mock( org.pentaho.di.trans.step.StepMetaInterface.class ) );
    assertNull( helper.sql( transMeta, "oraStep", "conn" ) );
  }

  @Test
  public void testSQL_InvalidRowMeta_Null() throws Exception {
    when( transMeta.findStep( "oraStep" ) ).thenReturn( stepMeta );
    when( stepMeta.getStepMetaInterface() ).thenReturn( meta );
    when( transMeta.findDatabase( "conn" ) ).thenReturn( databaseMeta );
    when( transMeta.getPrevStepFields( "oraStep" ) ).thenReturn( null );

    assertNull( helper.sql( transMeta, "oraStep", "conn" ) );
    verify( meta ).setDatabaseMeta( databaseMeta );
  }

  @Test
  public void testSQL_InvalidRowMeta_Empty() throws Exception {
    when( transMeta.findStep( "oraStep" ) ).thenReturn( stepMeta );
    when( stepMeta.getStepMetaInterface() ).thenReturn( meta );
    when( transMeta.findDatabase( "conn" ) ).thenReturn( databaseMeta );
    when( transMeta.getPrevStepFields( "oraStep" ) ).thenReturn( prevFields );
    when( prevFields.size() ).thenReturn( 0 );

    assertNull( helper.sql( transMeta, "oraStep", "conn" ) );
  }

  @Test
  public void testSQL_FieldStream_Null() throws Exception {
    when( transMeta.findStep( "oraStep" ) ).thenReturn( stepMeta );
    when( stepMeta.getStepMetaInterface() ).thenReturn( meta );
    when( transMeta.findDatabase( "conn" ) ).thenReturn( databaseMeta );
    when( transMeta.getPrevStepFields( "oraStep" ) ).thenReturn( validRowMeta() );
    when( meta.getFieldStream() ).thenReturn( null );

    assertNull( helper.sql( transMeta, "oraStep", "conn" ) );
  }

  @Test
  public void testSQL_FieldStream_Empty() throws Exception {
    when( transMeta.findStep( "oraStep" ) ).thenReturn( stepMeta );
    when( stepMeta.getStepMetaInterface() ).thenReturn( meta );
    when( transMeta.findDatabase( "conn" ) ).thenReturn( databaseMeta );
    when( transMeta.getPrevStepFields( "oraStep" ) ).thenReturn( validRowMeta() );
    when( meta.getFieldStream() ).thenReturn( new String[0] );

    assertNull( helper.sql( transMeta, "oraStep", "conn" ) );
  }

  @Test
  public void testSQL_Valid_ReturnsStatement() throws Exception {
    RowMetaInterface prev = validRowMeta();

    when( transMeta.findStep( "oraStep" ) ).thenReturn( stepMeta );
    when( stepMeta.getStepMetaInterface() ).thenReturn( meta );
    when( transMeta.findDatabase( "conn" ) ).thenReturn( databaseMeta );
    when( transMeta.getPrevStepFields( "oraStep" ) ).thenReturn( prev );
    when( meta.getFieldStream() ).thenReturn( new String[] { "col1" } );
    when( meta.getSQLStatements( any(), any(), eq( prev ), isNull(), isNull() ) ).thenReturn( sqlStatement );

    SQLStatement result = helper.sql( transMeta, "oraStep", "conn" );

    assertSame( sqlStatement, result );
    verify( meta ).setDatabaseMeta( databaseMeta );
  }

  @Test
  public void testSQL_Valid_WithMultipleFields() throws Exception {
    RowMeta rowMeta = new RowMeta();
    rowMeta.addValueMeta( new ValueMetaString( "col1" ) );
    rowMeta.addValueMeta( new ValueMetaString( "col2" ) );
    rowMeta.addValueMeta( new ValueMetaString( "col3" ) );

    when( transMeta.findStep( "oraStep" ) ).thenReturn( stepMeta );
    when( stepMeta.getStepMetaInterface() ).thenReturn( meta );
    when( transMeta.findDatabase( "conn" ) ).thenReturn( databaseMeta );
    when( transMeta.getPrevStepFields( "oraStep" ) ).thenReturn( rowMeta );
    when( meta.getFieldStream() ).thenReturn( new String[] { "col1", "col2", "col3" } );
    when( meta.getSQLStatements( any(), any(), eq( rowMeta ), isNull(), isNull() ) ).thenReturn( sqlStatement );

    SQLStatement result = helper.sql( transMeta, "oraStep", "conn" );

    assertSame( sqlStatement, result );
  }

  @Test
  public void testSQL_InitialFailureStatus() throws Exception {
    queryParams.put( OraBulkLoaderHelper.STEP_NAME, "oraStep" );
    queryParams.put( OraBulkLoaderHelper.CONNECTION, "conn" );

    when( transMeta.findStep( "oraStep" ) ).thenReturn( stepMeta );
    when( stepMeta.getStepMetaInterface() ).thenReturn( meta );
    when( transMeta.findDatabase( "conn" ) ).thenReturn( databaseMeta );
    when( transMeta.getPrevStepFields( "oraStep" ) ).thenReturn( validRowMeta() );
    when( meta.getFieldStream() ).thenReturn( new String[] { "col1" } );
    when( meta.getSQLStatements( any(), any(), any(), isNull(), isNull() ) ).thenReturn( sqlStatement );
    when( sqlStatement.hasError() ).thenReturn( false );
    when( sqlStatement.hasSQL() ).thenReturn( true );
    when( sqlStatement.getSQL() ).thenReturn( "CREATE TABLE x (...)" );

    JSONObject response = helper.getSQLAction( transMeta, queryParams );

    // Method initially sets FAILURE but then overrides with SUCCESS
    assertEquals( StepInterface.SUCCESS_RESPONSE, response.get( "actionStatus" ) );
  }

  private RowMetaInterface validRowMeta() {
    RowMeta rowMeta = new RowMeta();
    rowMeta.addValueMeta( new ValueMetaString( "col1" ) );
    return rowMeta;
  }
  @Test
  public void testGetTableFieldAndType_NullConnection() {
    queryParams.put( OraBulkLoaderHelper.CONNECTION, null );
    queryParams.put( OraBulkLoaderHelper.SCHEMA, "mySchema" );
    queryParams.put( OraBulkLoaderHelper.TABLE, "my_table" );
    when( transMeta.environmentSubstitute( "mySchema" ) ).thenReturn( "mySchema" );
    when( transMeta.environmentSubstitute( "my_table" ) ).thenReturn( "my_table" );

    JSONObject response = helper.getTableFieldAndType( transMeta, queryParams );

    assertEquals( StepInterface.FAILURE_RESPONSE, response.get( StepInterface.ACTION_STATUS ) );
    assertNotNull( response.get( "error" ) );
  }

  @Test
  public void testGetTableFieldAndType_BlankConnection() {
    queryParams.put( OraBulkLoaderHelper.CONNECTION, "   " );
    queryParams.put( OraBulkLoaderHelper.SCHEMA, "mySchema" );
    queryParams.put( OraBulkLoaderHelper.TABLE, "my_table" );
    when( transMeta.environmentSubstitute( "mySchema" ) ).thenReturn( "mySchema" );
    when( transMeta.environmentSubstitute( "my_table" ) ).thenReturn( "my_table" );

    JSONObject response = helper.getTableFieldAndType( transMeta, queryParams );

    assertEquals( StepInterface.FAILURE_RESPONSE, response.get( StepInterface.ACTION_STATUS ) );
    assertNotNull( response.get( "error" ) );
  }

  @Test
  public void testGetTableFieldAndType_NullSchema() {
    queryParams.put( OraBulkLoaderHelper.CONNECTION, "conn" );
    queryParams.put( OraBulkLoaderHelper.SCHEMA, null );
    queryParams.put( OraBulkLoaderHelper.TABLE, "my_table" );
    when( transMeta.environmentSubstitute( "my_table" ) ).thenReturn( "my_table" );
    when( transMeta.findDatabase( "conn" ) ).thenReturn( databaseMeta );

    try ( MockedConstruction<Database> ignored = mockConstruction( Database.class, ( mock, context ) -> {
      doNothing().when( mock ).connect();
      when( mock.getTableFieldsMeta( null, "my_table" ) ).thenReturn( null );
    } ) ) {
      JSONObject response = helper.getTableFieldAndType( transMeta, queryParams );

      assertEquals( StepInterface.SUCCESS_RESPONSE, response.get( StepInterface.ACTION_STATUS ) );
      JSONArray columns = (JSONArray) response.get( "columns" );
      assertNotNull( columns );
      assertEquals( 0, columns.size() );
    }
  }

  @Test
  public void testGetTableFieldAndType_BlankSchema() {
    queryParams.put( OraBulkLoaderHelper.CONNECTION, "conn" );
    queryParams.put( OraBulkLoaderHelper.SCHEMA, "" );
    queryParams.put( OraBulkLoaderHelper.TABLE, "my_table" );
    when( transMeta.environmentSubstitute( "my_table" ) ).thenReturn( "my_table" );
    when( transMeta.findDatabase( "conn" ) ).thenReturn( databaseMeta );

    try ( MockedConstruction<Database> ignored = mockConstruction( Database.class, ( mock, context ) -> {
      doNothing().when( mock ).connect();
      when( mock.getTableFieldsMeta( null, "my_table" ) ).thenReturn( null );
    } ) ) {
      JSONObject response = helper.getTableFieldAndType( transMeta, queryParams );

      assertEquals( StepInterface.SUCCESS_RESPONSE, response.get( StepInterface.ACTION_STATUS ) );
      JSONArray columns = (JSONArray) response.get( "columns" );
      assertNotNull( columns );
      assertEquals( 0, columns.size() );
    }
  }

  @Test
  public void testGetTableFieldAndType_NullTable() {
    queryParams.put( OraBulkLoaderHelper.CONNECTION, "conn" );
    queryParams.put( OraBulkLoaderHelper.SCHEMA, "mySchema" );
    queryParams.put( OraBulkLoaderHelper.TABLE, null );
    when( transMeta.environmentSubstitute( "mySchema" ) ).thenReturn( "mySchema" );

    JSONObject response = helper.getTableFieldAndType( transMeta, queryParams );

    assertEquals( StepInterface.FAILURE_RESPONSE, response.get( StepInterface.ACTION_STATUS ) );
    assertNotNull( response.get( "error" ) );
  }

  @Test
  public void testGetTableFieldAndType_BlankTable() {
    queryParams.put( OraBulkLoaderHelper.CONNECTION, "conn" );
    queryParams.put( OraBulkLoaderHelper.SCHEMA, "mySchema" );
    queryParams.put( OraBulkLoaderHelper.TABLE, "" );
    when( transMeta.environmentSubstitute( "mySchema" ) ).thenReturn( "mySchema" );
    when( transMeta.environmentSubstitute( "" ) ).thenReturn( "" );

    JSONObject response = helper.getTableFieldAndType( transMeta, queryParams );

    assertEquals( StepInterface.FAILURE_RESPONSE, response.get( StepInterface.ACTION_STATUS ) );
    assertNotNull( response.get( "error" ) );
  }

  @Test
  public void testGetTableFieldAndType_DatabaseNotFound() {
    queryParams.put( OraBulkLoaderHelper.CONNECTION, "missingConn" );
    queryParams.put( OraBulkLoaderHelper.SCHEMA, "mySchema" );
    queryParams.put( OraBulkLoaderHelper.TABLE, "my_table" );
    when( transMeta.environmentSubstitute( "mySchema" ) ).thenReturn( "mySchema" );
    when( transMeta.environmentSubstitute( "my_table" ) ).thenReturn( "my_table" );
    when( transMeta.findDatabase( "missingConn" ) ).thenReturn( null );

    JSONObject response = helper.getTableFieldAndType( transMeta, queryParams );

    assertEquals( StepInterface.FAILURE_RESPONSE, response.get( StepInterface.ACTION_STATUS ) );
    assertNotNull( response.get( "error" ) );
  }

  @Test
  public void testGetTableFieldAndType_Success_WithColumns() {
    queryParams.put( OraBulkLoaderHelper.CONNECTION, "conn" );
    queryParams.put( OraBulkLoaderHelper.SCHEMA, "mySchema" );
    queryParams.put( OraBulkLoaderHelper.TABLE, "my_table" );
    when( transMeta.environmentSubstitute( "mySchema" ) ).thenReturn( "mySchema" );
    when( transMeta.environmentSubstitute( "my_table" ) ).thenReturn( "my_table" );
    when( transMeta.findDatabase( "conn" ) ).thenReturn( databaseMeta );

    try ( MockedConstruction<Database> ignored = mockConstruction( Database.class, ( mock, context ) -> {
      RowMeta rowMeta = new RowMeta();
      rowMeta.addValueMeta( new ValueMetaString( "id" ) );
      rowMeta.addValueMeta( new ValueMetaString( "name" ) );
      doNothing().when( mock ).connect();
      when( mock.getTableFieldsMeta( "mySchema", "my_table" ) ).thenReturn( rowMeta );
    } ) ) {
      JSONObject response = helper.getTableFieldAndType( transMeta, queryParams );

      assertEquals( StepInterface.SUCCESS_RESPONSE, response.get( StepInterface.ACTION_STATUS ) );
      JSONArray columns = (JSONArray) response.get( "columns" );
      assertNotNull( columns );
      assertEquals( 2, columns.size() );
      assertEquals( "id", ( (JSONObject) columns.get( 0 ) ).get( "columnName" ) );
      assertEquals( "String", ( (JSONObject) columns.get( 0 ) ).get( "columnType" ) );
      assertEquals( "name", ( (JSONObject) columns.get( 1 ) ).get( "columnName" ) );
    }
  }

  @Test
  public void testGetTableFieldAndType_Success_NullRowMeta() {
    queryParams.put( OraBulkLoaderHelper.CONNECTION, "conn" );
    queryParams.put( OraBulkLoaderHelper.SCHEMA, "mySchema" );
    queryParams.put( OraBulkLoaderHelper.TABLE, "my_table" );
    when( transMeta.environmentSubstitute( "mySchema" ) ).thenReturn( "mySchema" );
    when( transMeta.environmentSubstitute( "my_table" ) ).thenReturn( "my_table" );
    when( transMeta.findDatabase( "conn" ) ).thenReturn( databaseMeta );

    try ( MockedConstruction<Database> ignored = mockConstruction( Database.class, ( mock, context ) -> {
      doNothing().when( mock ).connect();
      when( mock.getTableFieldsMeta( "mySchema", "my_table" ) ).thenReturn( null );
    } ) ) {
      JSONObject response = helper.getTableFieldAndType( transMeta, queryParams );

      assertEquals( StepInterface.SUCCESS_RESPONSE, response.get( StepInterface.ACTION_STATUS ) );
      JSONArray columns = (JSONArray) response.get( "columns" );
      assertNotNull( columns );
      assertEquals( 0, columns.size() );
    }
  }

  @Test
  public void testGetTableFieldAndType_DBConnectionException() {
    queryParams.put( OraBulkLoaderHelper.CONNECTION, "conn" );
    queryParams.put( OraBulkLoaderHelper.SCHEMA, "mySchema" );
    queryParams.put( OraBulkLoaderHelper.TABLE, "my_table" );
    when( transMeta.environmentSubstitute( "mySchema" ) ).thenReturn( "mySchema" );
    when( transMeta.environmentSubstitute( "my_table" ) ).thenReturn( "my_table" );
    when( transMeta.findDatabase( "conn" ) ).thenReturn( databaseMeta );

    try ( MockedConstruction<Database> ignored = mockConstruction( Database.class, ( mock, context ) -> {
      doThrow( new RuntimeException( "Connection failed" ) ).when( mock ).connect();
    } ) ) {
      JSONObject response = helper.getTableFieldAndType( transMeta, queryParams );

      assertEquals( StepInterface.FAILURE_RESPONSE, response.get( StepInterface.ACTION_STATUS ) );
      assertNotNull( response.get( "error" ) );
    }
  }

  @Test
  public void testHandleStepAction_GetTableFieldAndType_Routing() {
    queryParams.put( OraBulkLoaderHelper.CONNECTION, "conn" );
    queryParams.put( OraBulkLoaderHelper.SCHEMA, "mySchema" );
    queryParams.put( OraBulkLoaderHelper.TABLE, "my_table" );
    when( transMeta.environmentSubstitute( "mySchema" ) ).thenReturn( "mySchema" );
    when( transMeta.environmentSubstitute( "my_table" ) ).thenReturn( "my_table" );
    when( transMeta.findDatabase( "conn" ) ).thenReturn( databaseMeta );

    try ( MockedConstruction<Database> ignored = mockConstruction( Database.class, ( mock, context ) -> {
      RowMeta rowMeta = new RowMeta();
      rowMeta.addValueMeta( new ValueMetaString( "col1" ) );
      doNothing().when( mock ).connect();
      when( mock.getTableFieldsMeta( "mySchema", "my_table" ) ).thenReturn( rowMeta );
    } ) ) {
      JSONObject response = helper.handleStepAction( "getTableFieldAndType", transMeta, queryParams );

      assertEquals( StepInterface.SUCCESS_RESPONSE, response.get( StepInterface.ACTION_STATUS ) );
      JSONArray columns = (JSONArray) response.get( "columns" );
      assertNotNull( columns );
      assertEquals( 1, columns.size() );
    }
  }
}


