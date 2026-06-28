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

package org.pentaho.di.trans.steps.pgbulkloader;

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
public class PGBulkLoaderHelperTest {

  @Mock private TransMeta transMeta;
  @Mock private StepMeta stepMeta;
  @Mock private PGBulkLoaderMeta meta;
  @Mock private SQLStatement sqlStatement;
  @Mock private RowMetaInterface prevFields;
  @Mock private DatabaseMeta databaseMeta;

  private PGBulkLoaderHelper helper;
  private Map<String, String> queryParams;

  @Before
  public void setUp() throws Exception {
    // Initialize Kettle environment to set up logging
    if ( !KettleEnvironment.isInitialized() ) {
      KettleEnvironment.init();
    }
    helper = new PGBulkLoaderHelper();
    queryParams = new HashMap<>();
  }

  @Test
  public void testHandleStepAction_GetSQL_Success() throws Exception {
    queryParams.put( PGBulkLoaderHelper.STEP_NAME, "pgStep" );
    queryParams.put( PGBulkLoaderHelper.CONNECTION, "conn" );

    when( transMeta.findStep( "pgStep" ) ).thenReturn( stepMeta );
    when( stepMeta.getStepMetaInterface() ).thenReturn( meta );
    when( transMeta.findDatabase( "conn" ) ).thenReturn( databaseMeta );
    when( transMeta.getPrevStepFields( "pgStep" ) ).thenReturn( validRowMeta() );
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
    queryParams.put( PGBulkLoaderHelper.STEP_NAME, "pgStep" );
    when( transMeta.findStep( "pgStep" ) ).thenThrow( new RuntimeException( "Test exception" ) );

    JSONObject response = helper.handleStepAction( "getSQL", transMeta, queryParams );

    assertEquals( BaseStepHelper.FAILURE_RESPONSE, response.get( BaseStepHelper.ACTION_STATUS ) );
    assertEquals( "Test exception", response.get( PGBulkLoaderHelper.DETAILS ) );
  }

  @Test
  public void testGetSQLAction_Success_WithSQL() throws Exception {
    queryParams.put( PGBulkLoaderHelper.STEP_NAME, "pgStep" );
    queryParams.put( PGBulkLoaderHelper.CONNECTION, "conn" );

    when( transMeta.findStep( "pgStep" ) ).thenReturn( stepMeta );
    when( stepMeta.getStepMetaInterface() ).thenReturn( meta );
    when( transMeta.findDatabase( "conn" ) ).thenReturn( databaseMeta );
    when( transMeta.getPrevStepFields( "pgStep" ) ).thenReturn( validRowMeta() );
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
    queryParams.put( PGBulkLoaderHelper.STEP_NAME, "pgStep" );
    queryParams.put( PGBulkLoaderHelper.CONNECTION, "conn" );

    when( transMeta.findStep( "pgStep" ) ).thenReturn( stepMeta );
    when( stepMeta.getStepMetaInterface() ).thenReturn( meta );
    when( transMeta.findDatabase( "conn" ) ).thenReturn( databaseMeta );
    when( transMeta.getPrevStepFields( "pgStep" ) ).thenReturn( validRowMeta() );
    when( meta.getFieldStream() ).thenReturn( new String[] { "col1" } );
    when( meta.getSQLStatements( any(), any(), any(), isNull(), isNull() ) ).thenReturn( sqlStatement );
    when( sqlStatement.hasError() ).thenReturn( false );
    when( sqlStatement.hasSQL() ).thenReturn( false );

    JSONObject response = helper.getSQLAction( transMeta, queryParams );

    assertEquals( StepInterface.SUCCESS_RESPONSE, response.get( "actionStatus" ) );
    assertNotNull( response.get( PGBulkLoaderHelper.DETAILS ) );
  }

  @Test
  public void testGetSQLAction_SQLError() throws Exception {
    queryParams.put( PGBulkLoaderHelper.STEP_NAME, "pgStep" );
    queryParams.put( PGBulkLoaderHelper.CONNECTION, "conn" );

    when( transMeta.findStep( "pgStep" ) ).thenReturn( stepMeta );
    when( stepMeta.getStepMetaInterface() ).thenReturn( meta );
    when( transMeta.findDatabase( "conn" ) ).thenReturn( databaseMeta );
    when( transMeta.getPrevStepFields( "pgStep" ) ).thenReturn( validRowMeta() );
    when( meta.getFieldStream() ).thenReturn( new String[] { "col1" } );
    when( meta.getSQLStatements( any(), any(), any(), isNull(), isNull() ) ).thenReturn( sqlStatement );
    when( sqlStatement.hasError() ).thenReturn( true );
    when( sqlStatement.getError() ).thenReturn( "SQL Error occurred" );

    JSONObject response = helper.getSQLAction( transMeta, queryParams );

    assertEquals( StepInterface.FAILURE_RESPONSE, response.get( "actionStatus" ) );
    assertEquals( "SQL Error occurred", response.get( PGBulkLoaderHelper.DETAILS ) );
  }

  @Test
  public void testGetSQLAction_NullSQL() throws Exception {
    queryParams.put( PGBulkLoaderHelper.STEP_NAME, "pgStep" );
    queryParams.put( PGBulkLoaderHelper.CONNECTION, "conn" );

    when( transMeta.findStep( "pgStep" ) ).thenReturn( stepMeta );
    when( stepMeta.getStepMetaInterface() ).thenReturn( meta );
    when( transMeta.findDatabase( "conn" ) ).thenReturn( databaseMeta );
    when( transMeta.getPrevStepFields( "pgStep" ) ).thenReturn( prevFields );
    when( prevFields.size() ).thenReturn( 0 );

    JSONObject response = helper.getSQLAction( transMeta, queryParams );

    assertEquals( StepInterface.FAILURE_RESPONSE, response.get( "actionStatus" ) );
    assertNotNull( response.get( PGBulkLoaderHelper.DETAILS ) );
  }

  @Test
  public void testGetSqlActionKettleStepException() throws Exception {
    PGBulkLoaderHelper spyHelper = spy( helper );
    doThrow( new KettleStepException( "Kettle exception" ) ).when( spyHelper ).sql( any(), anyString(), anyString() );

    queryParams.put( PGBulkLoaderHelper.STEP_NAME, "pgStep" );
    queryParams.put( PGBulkLoaderHelper.CONNECTION, "conn" );

    JSONObject response = spyHelper.getSQLAction( transMeta, queryParams );

    assertEquals( StepInterface.FAILURE_METHOD_NOT_RESPONSE, response.get( "actionStatus" ) );
    assertEquals( "Kettle exception", response.get( PGBulkLoaderHelper.DETAILS ).toString().trim() );
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
    when( transMeta.findStep( "pgStep" ) ).thenReturn( null );
    assertNull( helper.sql( transMeta, "pgStep", "conn" ) );
  }

  @Test
  public void testSQL_WrongStepType() throws Exception {
    when( transMeta.findStep( "pgStep" ) ).thenReturn( stepMeta );
    when( stepMeta.getStepMetaInterface() ).thenReturn( mock( org.pentaho.di.trans.step.StepMetaInterface.class ) );
    assertNull( helper.sql( transMeta, "pgStep", "conn" ) );
  }

  @Test
  public void testSQL_InvalidRowMeta_Null() throws Exception {
    when( transMeta.findStep( "pgStep" ) ).thenReturn( stepMeta );
    when( stepMeta.getStepMetaInterface() ).thenReturn( meta );
    when( transMeta.findDatabase( "conn" ) ).thenReturn( databaseMeta );
    when( transMeta.getPrevStepFields( "pgStep" ) ).thenReturn( null );

    assertNull( helper.sql( transMeta, "pgStep", "conn" ) );
    verify( meta ).setDatabaseMeta( databaseMeta );
  }

  @Test
  public void testSQL_InvalidRowMeta_Empty() throws Exception {
    when( transMeta.findStep( "pgStep" ) ).thenReturn( stepMeta );
    when( stepMeta.getStepMetaInterface() ).thenReturn( meta );
    when( transMeta.findDatabase( "conn" ) ).thenReturn( databaseMeta );
    when( transMeta.getPrevStepFields( "pgStep" ) ).thenReturn( prevFields );
    when( prevFields.size() ).thenReturn( 0 );

    assertNull( helper.sql( transMeta, "pgStep", "conn" ) );
  }

  @Test
  public void testSQL_FieldStream_Null() throws Exception {
    when( transMeta.findStep( "pgStep" ) ).thenReturn( stepMeta );
    when( stepMeta.getStepMetaInterface() ).thenReturn( meta );
    when( transMeta.findDatabase( "conn" ) ).thenReturn( databaseMeta );
    when( transMeta.getPrevStepFields( "pgStep" ) ).thenReturn( validRowMeta() );
    when( meta.getFieldStream() ).thenReturn( null );

    assertNull( helper.sql( transMeta, "pgStep", "conn" ) );
  }

  @Test
  public void testSQL_FieldStream_Empty() throws Exception {
    when( transMeta.findStep( "pgStep" ) ).thenReturn( stepMeta );
    when( stepMeta.getStepMetaInterface() ).thenReturn( meta );
    when( transMeta.findDatabase( "conn" ) ).thenReturn( databaseMeta );
    when( transMeta.getPrevStepFields( "pgStep" ) ).thenReturn( validRowMeta() );
    when( meta.getFieldStream() ).thenReturn( new String[0] );

    assertNull( helper.sql( transMeta, "pgStep", "conn" ) );
  }

  @Test
  public void testSQL_Valid_ReturnsStatement() throws Exception {
    RowMetaInterface prev = validRowMeta();

    when( transMeta.findStep( "pgStep" ) ).thenReturn( stepMeta );
    when( stepMeta.getStepMetaInterface() ).thenReturn( meta );
    when( transMeta.findDatabase( "conn" ) ).thenReturn( databaseMeta );
    when( transMeta.getPrevStepFields( "pgStep" ) ).thenReturn( prev );
    when( meta.getFieldStream() ).thenReturn( new String[] { "col1" } );
    when( meta.getSQLStatements( any(), any(), eq( prev ), isNull(), isNull() ) ).thenReturn( sqlStatement );

    SQLStatement result = helper.sql( transMeta, "pgStep", "conn" );

    assertSame( sqlStatement, result );
    verify( meta ).setDatabaseMeta( databaseMeta );
  }

  @Test
  public void testSQL_Valid_WithMultipleFields() throws Exception {
    RowMeta rowMeta = new RowMeta();
    rowMeta.addValueMeta( new ValueMetaString( "col1" ) );
    rowMeta.addValueMeta( new ValueMetaString( "col2" ) );
    rowMeta.addValueMeta( new ValueMetaString( "col3" ) );

    when( transMeta.findStep( "pgStep" ) ).thenReturn( stepMeta );
    when( stepMeta.getStepMetaInterface() ).thenReturn( meta );
    when( transMeta.findDatabase( "conn" ) ).thenReturn( databaseMeta );
    when( transMeta.getPrevStepFields( "pgStep" ) ).thenReturn( rowMeta );
    when( meta.getFieldStream() ).thenReturn( new String[] { "col1", "col2", "col3" } );
    when( meta.getSQLStatements( any(), any(), eq( rowMeta ), isNull(), isNull() ) ).thenReturn( sqlStatement );

    SQLStatement result = helper.sql( transMeta, "pgStep", "conn" );

    assertSame( sqlStatement, result );
  }

  @Test
  public void testSQL_InitialFailureStatus() throws Exception {
    queryParams.put( PGBulkLoaderHelper.STEP_NAME, "pgStep" );
    queryParams.put( PGBulkLoaderHelper.CONNECTION, "conn" );

    when( transMeta.findStep( "pgStep" ) ).thenReturn( stepMeta );
    when( stepMeta.getStepMetaInterface() ).thenReturn( meta );
    when( transMeta.findDatabase( "conn" ) ).thenReturn( databaseMeta );
    when( transMeta.getPrevStepFields( "pgStep" ) ).thenReturn( validRowMeta() );
    when( meta.getFieldStream() ).thenReturn( new String[] { "col1" } );
    when( meta.getSQLStatements( any(), any(), any(), isNull(), isNull() ) ).thenReturn( sqlStatement );
    when( sqlStatement.hasError() ).thenReturn( false );
    when( sqlStatement.hasSQL() ).thenReturn( true );
    when( sqlStatement.getSQL() ).thenReturn( "CREATE TABLE x (...)" );

    JSONObject response = helper.getSQLAction( transMeta, queryParams );

    // Method initially sets FAILURE but then overrides with SUCCESS
    assertEquals( StepInterface.SUCCESS_RESPONSE, response.get( "actionStatus" ) );
  }

  @Test
  public void testGetTableFieldAndType_NullConnection() {
    queryParams.put( PGBulkLoaderHelper.CONNECTION, null );
    queryParams.put( PGBulkLoaderHelper.SCHEMA, "public" );
    queryParams.put( PGBulkLoaderHelper.TABLE, "person_data" );
    when( transMeta.environmentSubstitute( "public" ) ).thenReturn( "public" );
    when( transMeta.environmentSubstitute( "person_data" ) ).thenReturn( "person_data" );

    JSONObject response = helper.getTableFieldAndType( transMeta, queryParams );

    assertEquals( StepInterface.FAILURE_RESPONSE, response.get( StepInterface.ACTION_STATUS ) );
    assertNotNull( response.get( "error" ) );
  }

  @Test
  public void testGetTableFieldAndType_BlankConnection() {
    queryParams.put( PGBulkLoaderHelper.CONNECTION, "   " );
    queryParams.put( PGBulkLoaderHelper.SCHEMA, "public" );
    queryParams.put( PGBulkLoaderHelper.TABLE, "person_data" );
    when( transMeta.environmentSubstitute( "public" ) ).thenReturn( "public" );
    when( transMeta.environmentSubstitute( "person_data" ) ).thenReturn( "person_data" );

    JSONObject response = helper.getTableFieldAndType( transMeta, queryParams );

    assertEquals( StepInterface.FAILURE_RESPONSE, response.get( StepInterface.ACTION_STATUS ) );
    assertNotNull( response.get( "error" ) );
  }

  @Test
  public void testGetTableFieldAndType_NullSchema() {
    queryParams.put( PGBulkLoaderHelper.CONNECTION, "conn" );
    queryParams.put( PGBulkLoaderHelper.SCHEMA, null );
    queryParams.put( PGBulkLoaderHelper.TABLE, "person_data" );
    when( transMeta.findDatabase( "conn" ) ).thenReturn( databaseMeta );
    when( transMeta.environmentSubstitute( "person_data" ) ).thenReturn( "person_data" );

    try ( MockedConstruction<Database> ignored = mockConstruction( Database.class, ( mock, context ) -> {
      RowMeta rowMeta = new RowMeta();
      rowMeta.addValueMeta( new ValueMetaString( "id" ) );
      doNothing().when( mock ).connect();
      when( databaseMeta.getQuotedSchemaTableCombination( isNull(), eq( "person_data" ) ) )
          .thenReturn( "person_data" );
      when( mock.getTableFields( "person_data" ) ).thenReturn( rowMeta );
    } ) ) {
      JSONObject response = helper.getTableFieldAndType( transMeta, queryParams );

      assertEquals( StepInterface.SUCCESS_RESPONSE, response.get( StepInterface.ACTION_STATUS ) );
      JSONArray columns = (JSONArray) response.get( "columns" );
      assertNotNull( columns );
      assertEquals( 1, columns.size() );
      assertEquals( "id", ( (JSONObject) columns.get( 0 ) ).get( "columnName" ) );
    }
  }

  @Test
  public void testGetTableFieldAndType_BlankSchema() {
    queryParams.put( PGBulkLoaderHelper.CONNECTION, "conn" );
    queryParams.put( PGBulkLoaderHelper.SCHEMA, "" );
    queryParams.put( PGBulkLoaderHelper.TABLE, "person_data" );
    when( transMeta.findDatabase( "conn" ) ).thenReturn( databaseMeta );
    when( transMeta.environmentSubstitute( "" ) ).thenReturn( "" );
    when( transMeta.environmentSubstitute( "person_data" ) ).thenReturn( "person_data" );

    try ( MockedConstruction<Database> ignored = mockConstruction( Database.class, ( mock, context ) -> {
      RowMeta rowMeta = new RowMeta();
      rowMeta.addValueMeta( new ValueMetaString( "id" ) );
      doNothing().when( mock ).connect();
      when( databaseMeta.getQuotedSchemaTableCombination( isNull(), eq( "person_data" ) ) )
          .thenReturn( "person_data" );
      when( mock.getTableFields( "person_data" ) ).thenReturn( rowMeta );
    } ) ) {
      JSONObject response = helper.getTableFieldAndType( transMeta, queryParams );

      assertEquals( StepInterface.SUCCESS_RESPONSE, response.get( StepInterface.ACTION_STATUS ) );
      JSONArray columns = (JSONArray) response.get( "columns" );
      assertNotNull( columns );
      assertEquals( 1, columns.size() );
      assertEquals( "id", ( (JSONObject) columns.get( 0 ) ).get( "columnName" ) );
    }
  }

  @Test
  public void testGetTableFieldAndType_NullTable() {
    queryParams.put( PGBulkLoaderHelper.CONNECTION, "conn" );
    queryParams.put( PGBulkLoaderHelper.SCHEMA, "public" );
    queryParams.put( PGBulkLoaderHelper.TABLE, null );
    when( transMeta.environmentSubstitute( "public" ) ).thenReturn( "public" );
    // environmentSubstitute(null) returns null by default from Mockito

    JSONObject response = helper.getTableFieldAndType( transMeta, queryParams );

    assertEquals( StepInterface.FAILURE_RESPONSE, response.get( StepInterface.ACTION_STATUS ) );
    assertNotNull( response.get( "error" ) );
  }

  @Test
  public void testGetTableFieldAndType_BlankTable() {
    queryParams.put( PGBulkLoaderHelper.CONNECTION, "conn" );
    queryParams.put( PGBulkLoaderHelper.SCHEMA, "public" );
    queryParams.put( PGBulkLoaderHelper.TABLE, "" );
    when( transMeta.environmentSubstitute( "public" ) ).thenReturn( "public" );
    when( transMeta.environmentSubstitute( "" ) ).thenReturn( "" );

    JSONObject response = helper.getTableFieldAndType( transMeta, queryParams );

    assertEquals( StepInterface.FAILURE_RESPONSE, response.get( StepInterface.ACTION_STATUS ) );
    assertNotNull( response.get( "error" ) );
  }

  @Test
  public void testGetTableFieldAndType_DatabaseNotFound() {
    queryParams.put( PGBulkLoaderHelper.CONNECTION, "missingConn" );
    queryParams.put( PGBulkLoaderHelper.SCHEMA, "public" );
    queryParams.put( PGBulkLoaderHelper.TABLE, "person_data" );
    when( transMeta.environmentSubstitute( "public" ) ).thenReturn( "public" );
    when( transMeta.environmentSubstitute( "person_data" ) ).thenReturn( "person_data" );
    when( transMeta.findDatabase( "missingConn" ) ).thenReturn( null );

    JSONObject response = helper.getTableFieldAndType( transMeta, queryParams );

    assertEquals( StepInterface.FAILURE_RESPONSE, response.get( StepInterface.ACTION_STATUS ) );
    assertNotNull( response.get( "error" ) );
  }

  @Test
  public void testGetTableFieldAndType_Success_WithColumns() {
    queryParams.put( PGBulkLoaderHelper.CONNECTION, "conn" );
    queryParams.put( PGBulkLoaderHelper.SCHEMA, "public" );
    queryParams.put( PGBulkLoaderHelper.TABLE, "person_data" );
    when( transMeta.environmentSubstitute( "public" ) ).thenReturn( "public" );
    when( transMeta.environmentSubstitute( "person_data" ) ).thenReturn( "person_data" );
    when( transMeta.findDatabase( "conn" ) ).thenReturn( databaseMeta );
    when( databaseMeta.getQuotedSchemaTableCombination( "public", "person_data" ) )
        .thenReturn( "\"public\".\"person_data\"" );

    try ( MockedConstruction<Database> ignored = mockConstruction( Database.class, ( mock, context ) -> {
      RowMeta rowMeta = new RowMeta();
      rowMeta.addValueMeta( new ValueMetaString( "id" ) );
      rowMeta.addValueMeta( new ValueMetaString( "name" ) );
      doNothing().when( mock ).connect();
      when( mock.getTableFields( "\"public\".\"person_data\"" ) ).thenReturn( rowMeta );
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
    queryParams.put( PGBulkLoaderHelper.CONNECTION, "conn" );
    queryParams.put( PGBulkLoaderHelper.SCHEMA, "public" );
    queryParams.put( PGBulkLoaderHelper.TABLE, "person_data" );
    when( transMeta.environmentSubstitute( "public" ) ).thenReturn( "public" );
    when( transMeta.environmentSubstitute( "person_data" ) ).thenReturn( "person_data" );
    when( transMeta.findDatabase( "conn" ) ).thenReturn( databaseMeta );
    when( databaseMeta.getQuotedSchemaTableCombination( "public", "person_data" ) )
        .thenReturn( "\"public\".\"person_data\"" );

    try ( MockedConstruction<Database> ignored = mockConstruction( Database.class, ( mock, context ) -> {
      doNothing().when( mock ).connect();
      when( mock.getTableFields( "\"public\".\"person_data\"" ) ).thenReturn( null );
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
    queryParams.put( PGBulkLoaderHelper.CONNECTION, "conn" );
    queryParams.put( PGBulkLoaderHelper.SCHEMA, "public" );
    queryParams.put( PGBulkLoaderHelper.TABLE, "person_data" );
    when( transMeta.environmentSubstitute( "public" ) ).thenReturn( "public" );
    when( transMeta.environmentSubstitute( "person_data" ) ).thenReturn( "person_data" );
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
    queryParams.put( PGBulkLoaderHelper.CONNECTION, "conn" );
    queryParams.put( PGBulkLoaderHelper.SCHEMA, "public" );
    queryParams.put( PGBulkLoaderHelper.TABLE, "person_data" );
    when( transMeta.environmentSubstitute( "public" ) ).thenReturn( "public" );
    when( transMeta.environmentSubstitute( "person_data" ) ).thenReturn( "person_data" );
    when( transMeta.findDatabase( "conn" ) ).thenReturn( databaseMeta );
    when( databaseMeta.getQuotedSchemaTableCombination( "public", "person_data" ) )
        .thenReturn( "\"public\".\"person_data\"" );

    try ( MockedConstruction<Database> ignored = mockConstruction( Database.class, ( mock, context ) -> {
      RowMeta rowMeta = new RowMeta();
      rowMeta.addValueMeta( new ValueMetaString( "col1" ) );
      doNothing().when( mock ).connect();
      when( mock.getTableFields( "\"public\".\"person_data\"" ) ).thenReturn( rowMeta );
    } ) ) {
      JSONObject response = helper.handleStepAction( "getTableFieldAndType", transMeta, queryParams );

      assertEquals( StepInterface.SUCCESS_RESPONSE, response.get( StepInterface.ACTION_STATUS ) );
      JSONArray columns = (JSONArray) response.get( "columns" );
      assertNotNull( columns );
      assertEquals( 1, columns.size() );
    }
  }

  private RowMetaInterface validRowMeta() {
    RowMeta rowMeta = new RowMeta();
    rowMeta.addValueMeta( new ValueMetaString( "col1" ) );
    return rowMeta;
  }
}
