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

package org.pentaho.di.trans.steps.mysqlbulkloader;

import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.SQLStatement;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepHelper;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith( MockitoJUnitRunner.class )
public class MySQLBulkLoaderHelperTest {

  @ClassRule public static final RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    KettleEnvironment.init( false );
  }

  @Mock private TransMeta transMeta;
  @Mock private StepMeta stepMeta;
  @Mock private MySQLBulkLoaderMeta meta;
  @Mock private SQLStatement sqlStatement;
  @Mock private RowMetaInterface prevFields;
  @Mock private DatabaseMeta databaseMeta;

  private MySQLBulkLoaderHelper helper;
  private Map<String, String> queryParams;

  @Before
  public void setUp() {
    helper = new MySQLBulkLoaderHelper();
    queryParams = new HashMap<>();
  }

  @Test
  public void testHandleStepAction_GetSQL_Success() throws Exception {
    queryParams.put( MySQLBulkLoaderHelper.STEP_NAME, "mysqlStep" );
    queryParams.put( MySQLBulkLoaderHelper.CONNECTION, "conn" );

    when( transMeta.findStep( "mysqlStep" ) ).thenReturn( stepMeta );
    when( stepMeta.getStepMetaInterface() ).thenReturn( meta );
    when( transMeta.findDatabase( "conn" ) ).thenReturn( databaseMeta );
    when( transMeta.getPrevStepFields( "mysqlStep" ) ).thenReturn( validRowMeta() );
    when( meta.getFieldStream() ).thenReturn( new String[] { "col1" } );
    when( meta.getSQLStatements( any(), any(), any(), isNull(), isNull() ) ).thenReturn( sqlStatement );
    when( sqlStatement.hasError() ).thenReturn( false );
    when( sqlStatement.hasSQL() ).thenReturn( true );
    when( sqlStatement.getSQL() ).thenReturn( "CREATE TABLE x (col1 VARCHAR(255))" );

    JSONObject response = helper.handleStepAction( "getSQL", transMeta, queryParams );

    assertEquals( StepInterface.SUCCESS_RESPONSE, response.get( StepInterface.ACTION_STATUS ) );
    assertEquals( "CREATE TABLE x (col1 VARCHAR(255))", response.get( "sqlString" ) );
  }

  @Test
  public void testHandleStepAction_InvalidMethod() {
    JSONObject response = helper.handleStepAction( "invalidMethod", transMeta, queryParams );

    assertEquals( BaseStepHelper.FAILURE_METHOD_NOT_FOUND_RESPONSE, response.get( BaseStepHelper.ACTION_STATUS ) );
  }

  @Test
  public void testHandleStepAction_MissingQueryParams_NullParams() {
    JSONObject response = helper.handleStepAction( "getSQL", transMeta, null );

    assertEquals( StepInterface.FAILURE_RESPONSE, response.get( StepInterface.ACTION_STATUS ) );
    assertNotNull( response.get( MySQLBulkLoaderHelper.DETAILS ) );
  }

  @Test
  public void testHandleStepAction_MissingQueryParams_MissingConnection() {
    queryParams.put( MySQLBulkLoaderHelper.STEP_NAME, "mysqlStep" );

    JSONObject response = helper.handleStepAction( "getSQL", transMeta, queryParams );

    assertEquals( StepInterface.FAILURE_RESPONSE, response.get( StepInterface.ACTION_STATUS ) );
    assertEquals(
      BaseMessages.getString( MySQLBulkLoaderHelper.class, "MySQLBulkLoaderHelper.GetSQL.MissingStepAndConnection" ),
      response.get( MySQLBulkLoaderHelper.DETAILS ).toString() );
  }

  @Test
  public void testGetSQLAction_Success_NoSQLNeeded() throws Exception {
    queryParams.put( MySQLBulkLoaderHelper.STEP_NAME, "mysqlStep" );
    queryParams.put( MySQLBulkLoaderHelper.CONNECTION, "conn" );

    when( transMeta.findStep( "mysqlStep" ) ).thenReturn( stepMeta );
    when( stepMeta.getStepMetaInterface() ).thenReturn( meta );
    when( transMeta.findDatabase( "conn" ) ).thenReturn( databaseMeta );
    when( transMeta.getPrevStepFields( "mysqlStep" ) ).thenReturn( validRowMeta() );
    when( meta.getFieldStream() ).thenReturn( new String[] { "col1" } );
    when( meta.getSQLStatements( any(), any(), any(), isNull(), isNull() ) ).thenReturn( sqlStatement );
    when( sqlStatement.hasError() ).thenReturn( false );
    when( sqlStatement.hasSQL() ).thenReturn( false );

    JSONObject response = helper.getSQLAction( transMeta, queryParams );

    assertEquals( StepInterface.SUCCESS_RESPONSE, response.get( StepInterface.ACTION_STATUS ) );
    assertNotNull( response.get( MySQLBulkLoaderHelper.DETAILS ) );
  }

  @Test
  public void testGetSQLAction_SQLError() throws Exception {
    queryParams.put( MySQLBulkLoaderHelper.STEP_NAME, "mysqlStep" );
    queryParams.put( MySQLBulkLoaderHelper.CONNECTION, "conn" );

    when( transMeta.findStep( "mysqlStep" ) ).thenReturn( stepMeta );
    when( stepMeta.getStepMetaInterface() ).thenReturn( meta );
    when( transMeta.findDatabase( "conn" ) ).thenReturn( databaseMeta );
    when( transMeta.getPrevStepFields( "mysqlStep" ) ).thenReturn( validRowMeta() );
    when( meta.getFieldStream() ).thenReturn( new String[] { "col1" } );
    when( meta.getSQLStatements( any(), any(), any(), isNull(), isNull() ) ).thenReturn( sqlStatement );
    when( sqlStatement.hasError() ).thenReturn( true );
    when( sqlStatement.getError() ).thenReturn( "SQL Error occurred" );

    JSONObject response = helper.getSQLAction( transMeta, queryParams );

    assertEquals( StepInterface.FAILURE_RESPONSE, response.get( StepInterface.ACTION_STATUS ) );
    assertEquals( "SQL Error occurred", response.get( MySQLBulkLoaderHelper.DETAILS ) );
  }

  @Test
  public void testGetSQLAction_NullSQL() throws Exception {
    queryParams.put( MySQLBulkLoaderHelper.STEP_NAME, "mysqlStep" );
    queryParams.put( MySQLBulkLoaderHelper.CONNECTION, "conn" );

    when( transMeta.findStep( "mysqlStep" ) ).thenReturn( stepMeta );
    when( stepMeta.getStepMetaInterface() ).thenReturn( meta );
    when( transMeta.findDatabase( "conn" ) ).thenReturn( databaseMeta );
    when( transMeta.getPrevStepFields( "mysqlStep" ) ).thenReturn( prevFields );
    when( prevFields.size() ).thenReturn( 0 );

    JSONObject response = helper.getSQLAction( transMeta, queryParams );

    assertEquals( StepInterface.FAILURE_RESPONSE, response.get( StepInterface.ACTION_STATUS ) );
    assertNotNull( response.get( MySQLBulkLoaderHelper.DETAILS ) );
  }

  @Test
  public void testGetSQLAction_KettleStepException() throws Exception {
    MySQLBulkLoaderHelper spyHelper = spy( helper );
    doThrow( new KettleStepException( "Kettle exception" ) )
      .when( spyHelper ).sql( any(), anyString(), anyString() );

    queryParams.put( MySQLBulkLoaderHelper.STEP_NAME, "mysqlStep" );
    queryParams.put( MySQLBulkLoaderHelper.CONNECTION, "conn" );

    JSONObject response = spyHelper.getSQLAction( transMeta, queryParams );

    assertEquals( StepInterface.FAILURE_METHOD_NOT_RESPONSE, response.get( StepInterface.ACTION_STATUS ) );
    assertEquals( "Kettle exception", response.get( MySQLBulkLoaderHelper.DETAILS ).toString().trim() );
  }

  @Test
  public void testSQL_ExceptionHandling() throws Exception {
    when( transMeta.findStep( "mysqlStep" ) ).thenReturn( stepMeta );
    when( stepMeta.getStepMetaInterface() ).thenReturn( meta );
    when( transMeta.findDatabase( "conn" ) ).thenReturn( databaseMeta );
    when( transMeta.getPrevStepFields( "mysqlStep" ) )
      .thenThrow( new KettleStepException( "Test exception" ) );

    try {
      helper.sql( transMeta, "mysqlStep", "conn" );
      fail( "Expected KettleStepException" );
    } catch ( KettleStepException e ) {
      assertEquals( "Test exception", e.getMessage().trim() );
    }
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
    when( transMeta.findStep( "mysqlStep" ) ).thenReturn( null );

    assertNull( helper.sql( transMeta, "mysqlStep", "conn" ) );
  }

  @Test
  public void testSQL_WrongStepType() throws Exception {
    when( transMeta.findStep( "mysqlStep" ) ).thenReturn( stepMeta );
    when( stepMeta.getStepMetaInterface() ).thenReturn( mock( StepMetaInterface.class ) );

    assertNull( helper.sql( transMeta, "mysqlStep", "conn" ) );
  }

  @Test
  public void testSQL_InvalidRowMeta_Null() throws Exception {
    when( transMeta.findStep( "mysqlStep" ) ).thenReturn( stepMeta );
    when( stepMeta.getStepMetaInterface() ).thenReturn( meta );
    when( transMeta.findDatabase( "conn" ) ).thenReturn( databaseMeta );
    when( transMeta.getPrevStepFields( "mysqlStep" ) ).thenReturn( null );

    assertNull( helper.sql( transMeta, "mysqlStep", "conn" ) );
    verify( meta ).setDatabaseMeta( databaseMeta );
  }

  @Test
  public void testSQL_FieldStream_Null() throws Exception {
    when( transMeta.findStep( "mysqlStep" ) ).thenReturn( stepMeta );
    when( stepMeta.getStepMetaInterface() ).thenReturn( meta );
    when( transMeta.findDatabase( "conn" ) ).thenReturn( databaseMeta );
    when( transMeta.getPrevStepFields( "mysqlStep" ) ).thenReturn( validRowMeta() );
    when( meta.getFieldStream() ).thenReturn( null );

    assertNull( helper.sql( transMeta, "mysqlStep", "conn" ) );
  }

  @Test
  public void testSQL_FieldStream_Empty() throws Exception {
    when( transMeta.findStep( "mysqlStep" ) ).thenReturn( stepMeta );
    when( stepMeta.getStepMetaInterface() ).thenReturn( meta );
    when( transMeta.findDatabase( "conn" ) ).thenReturn( databaseMeta );
    when( transMeta.getPrevStepFields( "mysqlStep" ) ).thenReturn( validRowMeta() );
    when( meta.getFieldStream() ).thenReturn( new String[ 0 ] );

    assertNull( helper.sql( transMeta, "mysqlStep", "conn" ) );
  }

  @Test
  public void testSQL_Valid_ReturnsStatement() throws Exception {
    RowMetaInterface prev = validRowMeta();

    when( transMeta.findStep( "mysqlStep" ) ).thenReturn( stepMeta );
    when( stepMeta.getStepMetaInterface() ).thenReturn( meta );
    when( transMeta.findDatabase( "conn" ) ).thenReturn( databaseMeta );
    when( transMeta.getPrevStepFields( "mysqlStep" ) ).thenReturn( prev );
    when( meta.getFieldStream() ).thenReturn( new String[] { "col1" } );
    when( meta.getSQLStatements( any(), any(), any(), isNull(), isNull() ) ).thenReturn( sqlStatement );

    SQLStatement result = helper.sql( transMeta, "mysqlStep", "conn" );

    assertSame( sqlStatement, result );
    verify( meta ).setDatabaseMeta( databaseMeta );
  }

  private RowMetaInterface validRowMeta() {
    RowMeta rowMeta = new RowMeta();
    rowMeta.addValueMeta( new ValueMetaString( "col1" ) );
    return rowMeta;
  }
}
