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

package org.pentaho.di.trans.steps.tableoutput;

import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pentaho.di.core.SQLStatement;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaInteger;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class TableOutputHelperTest {

  @Mock private TransMeta transMeta;
  @Mock private StepMeta stepMeta;
  @Mock private TableOutputMeta tableOutputMeta;
  @Mock private DatabaseMeta databaseMeta;
  @Mock private RowMetaInterface prevFields;
  @Mock private ValueMetaInterface valueMeta;
  @Mock private SQLStatement sqlStatement;

  private TableOutputHelper helper;
  private Map<String, String> queryParams;

  @Before
  public void setUp() {
    MockitoAnnotations.openMocks( this );

    helper = new TableOutputHelper();
    queryParams = new HashMap<>();
  }

  @Test
  public void testHandleStepAction_GetSQL() throws KettleStepException {
    // Test successful getSQL action
    queryParams.put( "stepName", "testStep" );
    queryParams.put( "connection", "testConnection" );

    when( transMeta.findStep( "testStep" ) ).thenReturn( stepMeta );
    when( stepMeta.getStepMetaInterface() ).thenReturn( tableOutputMeta );
    when( transMeta.findDatabase( "testConnection" ) ).thenReturn( databaseMeta );
    when( transMeta.getPrevStepFields( "testStep" ) ).thenReturn( prevFields );
    when( tableOutputMeta.isTableNameInField() ).thenReturn( false );
    when( tableOutputMeta.specifyFields() ).thenReturn( false );
    when( tableOutputMeta.isReturningGeneratedKeys() ).thenReturn( false );
    when( prevFields.getValueMetaList() ).thenReturn( List.of( valueMeta ) );
    when( valueMeta.getName() ).thenReturn( "testField" );
    when( tableOutputMeta.getSQLStatements( any(), any(), any(), any(), anyBoolean(), any() ) ).thenReturn(
      sqlStatement );
    when( sqlStatement.hasError() ).thenReturn( false );
    when( sqlStatement.hasSQL() ).thenReturn( true );
    when( sqlStatement.getSQL() ).thenReturn( "SELECT * FROM test" );

    JSONObject result = helper.handleStepAction( "getSQL", transMeta, queryParams );

    assertEquals( "Action successful", result.get( "actionStatus" ) );
    assertEquals( "SELECT * FROM test", result.get( "sqlString" ) );
  }

  @Test
  public void testHandleStepAction_UnknownMethod() {
    JSONObject result = helper.handleStepAction( "unknownMethod", transMeta, queryParams );
    assertEquals( "Action failed with method not found", result.get( "actionStatus" ) );
  }

  @Test
  public void testHandleStepAction_Exception() {
    queryParams.put( "stepName", "testStep" );
    when( transMeta.findStep( "testStep" ) ).thenThrow( new RuntimeException( "Test exception" ) );

    JSONObject result = helper.handleStepAction( "getSQL", transMeta, queryParams );
    assertEquals( "Action failed", result.get( "actionStatus" ) );
  }

  @Test
  public void testGetSQLAction_SQLWithError() throws KettleStepException {
    queryParams.put( "stepName", "testStep" );
    queryParams.put( "connection", "testConnection" );

    when( transMeta.findStep( "testStep" ) ).thenReturn( stepMeta );
    when( stepMeta.getStepMetaInterface() ).thenReturn( tableOutputMeta );
    when( transMeta.findDatabase( "testConnection" ) ).thenReturn( databaseMeta );
    when( transMeta.getPrevStepFields( "testStep" ) ).thenReturn( prevFields );
    when( tableOutputMeta.isTableNameInField() ).thenReturn( false );
    when( tableOutputMeta.specifyFields() ).thenReturn( false );
    when( tableOutputMeta.isReturningGeneratedKeys() ).thenReturn( false );
    when( prevFields.getValueMetaList() ).thenReturn( List.of( valueMeta ) );
    when( valueMeta.getName() ).thenReturn( "testField" );
    when( tableOutputMeta.getSQLStatements( any(), any(), any(), any(), anyBoolean(), any() ) ).thenReturn(
      sqlStatement );
    when( sqlStatement.hasError() ).thenReturn( true );
    when( sqlStatement.getError() ).thenReturn( "SQL Error" );

    JSONObject result = helper.getSQLAction( transMeta, queryParams );

    assertEquals( "Action failed", result.get( "actionStatus" ) );
  }

  @Test
  public void testGetSQLAction_NoSQL() throws KettleStepException {
    queryParams.put( "stepName", "testStep" );
    queryParams.put( "connection", "testConnection" );

    when( transMeta.findStep( "testStep" ) ).thenReturn( stepMeta );
    when( stepMeta.getStepMetaInterface() ).thenReturn( tableOutputMeta );
    when( transMeta.findDatabase( "testConnection" ) ).thenReturn( databaseMeta );
    when( transMeta.getPrevStepFields( "testStep" ) ).thenReturn( prevFields );
    when( tableOutputMeta.isTableNameInField() ).thenReturn( false );
    when( tableOutputMeta.specifyFields() ).thenReturn( false );
    when( tableOutputMeta.isReturningGeneratedKeys() ).thenReturn( false );
    when( prevFields.getValueMetaList() ).thenReturn( List.of( valueMeta ) );
    when( valueMeta.getName() ).thenReturn( "testField" );
    when( tableOutputMeta.getSQLStatements( any(), any(), any(), any(), anyBoolean(), any() ) ).thenReturn(
      sqlStatement );
    when( sqlStatement.hasError() ).thenReturn( false );
    when( sqlStatement.hasSQL() ).thenReturn( false );


    JSONObject result = helper.getSQLAction( transMeta, queryParams );

    assertEquals( "Action failed", result.get( "actionStatus" ) );
  }

  @Test
  public void testGetSQLAction_NullSQL() throws KettleStepException {
    queryParams.put( "stepName", "testStep" );
    queryParams.put( "connection", "testConnection" );

    when( transMeta.findStep( "testStep" ) ).thenReturn( stepMeta );
    when( stepMeta.getStepMetaInterface() ).thenReturn( tableOutputMeta );
    when( transMeta.findDatabase( "testConnection" ) ).thenReturn( databaseMeta );
    when( transMeta.getPrevStepFields( "testStep" ) ).thenReturn( prevFields );
    when( tableOutputMeta.isTableNameInField() ).thenReturn( false );
    when( tableOutputMeta.specifyFields() ).thenReturn( false );
    when( tableOutputMeta.isReturningGeneratedKeys() ).thenReturn( false );
    when( prevFields.getValueMetaList() ).thenReturn( List.of( valueMeta ) );
    when( valueMeta.getName() ).thenReturn( "" );  // Empty name makes isValidRowMeta return false

    JSONObject result = helper.getSQLAction( transMeta, queryParams );

    assertEquals( "Action failed", result.get( "actionStatus" ) );
  }


  @Test
  public void testSQL_NullStepName() throws Exception {
    SQLStatement result = helper.sql( transMeta, null, "connection" );
    assertNull( result );
  }

  @Test
  public void testSQL_EmptyStepName() throws Exception {
    SQLStatement result = helper.sql( transMeta, "", "connection" );
    assertNull( result );
  }

  @Test
  public void testSQL_WrongStepType() throws Exception {
    StepMeta wrongStepMeta = mock( StepMeta.class );
    when( transMeta.findStep( "wrongStep" ) ).thenReturn( wrongStepMeta );
    when( wrongStepMeta.getStepMetaInterface() ).thenReturn(
      mock( org.pentaho.di.trans.step.StepMetaInterface.class ) );

    SQLStatement result = helper.sql( transMeta, "wrongStep", "connection" );
    assertNull( result );
  }

  @Test
  public void testSQL_WithTableNameInField() throws Exception {
    queryParams.put( "stepName", "testStep" );

    when( transMeta.findStep( "testStep" ) ).thenReturn( stepMeta );
    when( stepMeta.getStepMetaInterface() ).thenReturn( tableOutputMeta );
    when( transMeta.findDatabase( "connection" ) ).thenReturn( databaseMeta );
    when( transMeta.getPrevStepFields( "testStep" ) ).thenReturn( prevFields );
    when( tableOutputMeta.isTableNameInField() ).thenReturn( true );
    when( tableOutputMeta.isTableNameInTable() ).thenReturn( false );
    when( tableOutputMeta.getTableNameField() ).thenReturn( "tableName" );
    when( prevFields.indexOfValue( "tableName" ) ).thenReturn( 0 );
    when( tableOutputMeta.specifyFields() ).thenReturn( false );
    when( tableOutputMeta.isReturningGeneratedKeys() ).thenReturn( false );
    when( prevFields.getValueMetaList() ).thenReturn( List.of( valueMeta ) );
    when( valueMeta.getName() ).thenReturn( "testField" );
    when( tableOutputMeta.getSQLStatements( any(), any(), any(), any(), anyBoolean(), any() ) ).thenReturn(
      sqlStatement );

    SQLStatement result = helper.sql( transMeta, "testStep", "connection" );

    verify( prevFields ).removeValueMeta( 0 );
    assertSame( sqlStatement, result );
  }

  @Test
  public void testSQL_WithSpecifyFields() throws Exception {
    when( transMeta.findStep( "testStep" ) ).thenReturn( stepMeta );
    when( stepMeta.getStepMetaInterface() ).thenReturn( tableOutputMeta );
    when( transMeta.findDatabase( "connection" ) ).thenReturn( databaseMeta );
    when( transMeta.getPrevStepFields( "testStep" ) ).thenReturn( prevFields );
    when( tableOutputMeta.isTableNameInField() ).thenReturn( false );
    when( tableOutputMeta.specifyFields() ).thenReturn( true );
    when( tableOutputMeta.getFieldDatabase() ).thenReturn( new String[] { "dbField1" } );
    when( tableOutputMeta.getFieldStream() ).thenReturn( new String[] { "streamField1" } );

    ValueMetaInterface sourceValue = new ValueMetaString( "streamField1" );
    when( prevFields.searchValueMeta( "streamField1" ) ).thenReturn( sourceValue );

    when( tableOutputMeta.isReturningGeneratedKeys() ).thenReturn( false );
    when( tableOutputMeta.getSQLStatements( any(), any(), any(), any(), anyBoolean(), any() ) ).thenReturn(
      sqlStatement );

    SQLStatement result = helper.sql( transMeta, "testStep", "connection" );

    assertSame( sqlStatement, result );
  }

  @Test
  public void testSQL_SpecifyFields_FieldNotFound() throws Exception {
    when( transMeta.findStep( "testStep" ) ).thenReturn( stepMeta );
    when( stepMeta.getStepMetaInterface() ).thenReturn( tableOutputMeta );
    when( transMeta.findDatabase( "connection" ) ).thenReturn( databaseMeta );
    when( transMeta.getPrevStepFields( "testStep" ) ).thenReturn( prevFields );
    when( tableOutputMeta.isTableNameInField() ).thenReturn( false );
    when( tableOutputMeta.specifyFields() ).thenReturn( true );
    when( tableOutputMeta.getFieldDatabase() ).thenReturn( new String[] { "dbField1" } );
    when( tableOutputMeta.getFieldStream() ).thenReturn( new String[] { "nonExistentField" } );
    when( prevFields.searchValueMeta( "nonExistentField" ) ).thenReturn( null );

    try {
      helper.sql( transMeta, "testStep", "connection" );
      fail( "Expected KettleStepException" );
    } catch ( KettleStepException e ) {

    }
  }

  @Test
  public void testSQL_WithGeneratedKeys() throws Exception {
    when( transMeta.findStep( "testStep" ) ).thenReturn( stepMeta );
    when( stepMeta.getStepMetaInterface() ).thenReturn( tableOutputMeta );
    when( transMeta.findDatabase( "connection" ) ).thenReturn( databaseMeta );
    when( transMeta.getPrevStepFields( "testStep" ) ).thenReturn( prevFields );
    when( tableOutputMeta.isTableNameInField() ).thenReturn( false );
    when( tableOutputMeta.specifyFields() ).thenReturn( false );
    when( tableOutputMeta.isReturningGeneratedKeys() ).thenReturn( true );
    when( tableOutputMeta.getGeneratedKeyField() ).thenReturn( "id" );
    when( prevFields.getValueMetaList() ).thenReturn( List.of( valueMeta ) );
    when( valueMeta.getName() ).thenReturn( "testField" );
    when( tableOutputMeta.getSQLStatements( any(), any(), any(), eq( "id" ), eq( true ), eq( "id" ) ) ).thenReturn(
      sqlStatement );

    SQLStatement result = helper.sql( transMeta, "testStep", "connection" );

    verify( prevFields ).addValueMeta( eq( 0 ), any( ValueMetaInteger.class ) );
    assertSame( sqlStatement, result );
  }

  @Test
  public void testSQL_GeneratedKeysEmptyField() throws Exception {
    when( transMeta.findStep( "testStep" ) ).thenReturn( stepMeta );
    when( stepMeta.getStepMetaInterface() ).thenReturn( tableOutputMeta );
    when( transMeta.findDatabase( "connection" ) ).thenReturn( databaseMeta );
    when( transMeta.getPrevStepFields( "testStep" ) ).thenReturn( prevFields );
    when( tableOutputMeta.isTableNameInField() ).thenReturn( false );
    when( tableOutputMeta.specifyFields() ).thenReturn( false );
    when( tableOutputMeta.isReturningGeneratedKeys() ).thenReturn( true );
    when( tableOutputMeta.getGeneratedKeyField() ).thenReturn( "" ); // Empty field
    when( prevFields.getValueMetaList() ).thenReturn( List.of( valueMeta ) );
    when( valueMeta.getName() ).thenReturn( "testField" );
    when( tableOutputMeta.getSQLStatements( any(), any(), any(), isNull(), eq( false ), isNull() ) ).thenReturn(
      sqlStatement );

    SQLStatement result = helper.sql( transMeta, "testStep", "connection" );

    verify( prevFields, never() ).addValueMeta( anyInt(), any( ValueMetaInterface.class ) );
    assertSame( sqlStatement, result );
  }

  @Test
  public void testIsValidRowMeta_ValidRowMeta() throws Exception {
    RowMetaInterface validRowMeta = new RowMeta();
    ValueMetaInterface validValue = new ValueMetaString( "validField" );
    validRowMeta.addValueMeta( validValue );

    when( transMeta.findStep( "testStep" ) ).thenReturn( stepMeta );
    when( stepMeta.getStepMetaInterface() ).thenReturn( tableOutputMeta );
    when( transMeta.findDatabase( "connection" ) ).thenReturn( databaseMeta );
    when( transMeta.getPrevStepFields( "testStep" ) ).thenReturn( validRowMeta );
    when( tableOutputMeta.isTableNameInField() ).thenReturn( false );
    when( tableOutputMeta.specifyFields() ).thenReturn( false );
    when( tableOutputMeta.isReturningGeneratedKeys() ).thenReturn( false );
    when( tableOutputMeta.getSQLStatements( any(), any(), any(), any(), anyBoolean(), any() ) ).thenReturn(
      sqlStatement );

    SQLStatement result = helper.sql( transMeta, "testStep", "connection" );

    assertSame( sqlStatement, result );
  }

  @Test
  public void testIsValidRowMeta_NullRowMeta() throws Exception {
    when( transMeta.findStep( "testStep" ) ).thenReturn( stepMeta );
    when( stepMeta.getStepMetaInterface() ).thenReturn( tableOutputMeta );
    when( transMeta.findDatabase( "connection" ) ).thenReturn( databaseMeta );
    when( transMeta.getPrevStepFields( "testStep" ) ).thenReturn( null );

    SQLStatement result = helper.sql( transMeta, "testStep", "connection" );

    assertNull( result );
  }

  @Test
  public void testIsValidRowMeta_EmptyFieldName() throws Exception {
    RowMetaInterface invalidRowMeta = new RowMeta();
    ValueMetaInterface invalidValue = new ValueMetaString( "" ); // Empty name
    invalidRowMeta.addValueMeta( invalidValue );

    when( transMeta.findStep( "testStep" ) ).thenReturn( stepMeta );
    when( stepMeta.getStepMetaInterface() ).thenReturn( tableOutputMeta );
    when( transMeta.findDatabase( "connection" ) ).thenReturn( databaseMeta );
    when( transMeta.getPrevStepFields( "testStep" ) ).thenReturn( invalidRowMeta );
    when( tableOutputMeta.isTableNameInField() ).thenReturn( false );
    when( tableOutputMeta.specifyFields() ).thenReturn( false );
    when( tableOutputMeta.isReturningGeneratedKeys() ).thenReturn( false );

    SQLStatement result = helper.sql( transMeta, "testStep", "connection" );

    assertNull( result );
  }

  @Test
  public void testIsValidRowMeta_NullFieldName() throws Exception {
    RowMetaInterface invalidRowMeta = new RowMeta();
    ValueMetaInterface invalidValue = mock( ValueMetaInterface.class );
    when( invalidValue.getName() ).thenReturn( null ); // Null name
    invalidRowMeta.addValueMeta( invalidValue );

    when( transMeta.findStep( "testStep" ) ).thenReturn( stepMeta );
    when( stepMeta.getStepMetaInterface() ).thenReturn( tableOutputMeta );
    when( transMeta.findDatabase( "connection" ) ).thenReturn( databaseMeta );
    when( transMeta.getPrevStepFields( "testStep" ) ).thenReturn( invalidRowMeta );
    when( tableOutputMeta.isTableNameInField() ).thenReturn( false );
    when( tableOutputMeta.specifyFields() ).thenReturn( false );
    when( tableOutputMeta.isReturningGeneratedKeys() ).thenReturn( false );

    SQLStatement result = helper.sql( transMeta, "testStep", "connection" );

    assertNull( result );
  }
}
