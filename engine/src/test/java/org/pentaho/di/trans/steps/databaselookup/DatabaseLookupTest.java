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

package org.pentaho.di.trans.steps.databaselookup;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.Assert;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaBase;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.steps.mock.StepMockHelper;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockConstruction;

public class DatabaseLookupTest {

  protected StepMockHelper<DatabaseLookupMeta, DatabaseLookupData> helper;
  DatabaseMeta databaseMeta;
  Map<String, Object> queryParams;
  List<ValueMetaInterface> valueMetaInterfaceList;
  RowMeta rowMeta;
  String connection;
  String schema;
  String table;
  DatabaseLookup databaseLookup;

  @BeforeClass
  public static void init() throws KettleException {
    KettleClientEnvironment.init();
  }

  @Before
  public void setUp() {
    helper = new StepMockHelper<>( "database look up test", DatabaseLookupMeta.class, DatabaseLookupData.class );
    when( helper.logChannelInterfaceFactory.create( any(), any( LoggingObjectInterface.class ) ) ).thenReturn(
        helper.logChannelInterface );
    when( helper.trans.isRunning() ).thenReturn( true );
    databaseLookup = new DatabaseLookup( helper.stepMeta, helper.stepDataInterface, 0, helper.transMeta, helper.trans );
    connection = "Hypersonic";
    schema = "public";
    table = "employee";

    queryParams = new HashMap<>();
    queryParams.put( "connection", connection );
    queryParams.put( "schema", schema );
    queryParams.put( "table", table );

    databaseMeta = mock( DatabaseMeta.class );
    when( helper.transMeta.findDatabase( connection ) ).thenReturn( databaseMeta );

    valueMetaInterfaceList = new ArrayList<>();
    rowMeta = new RowMeta();
  }

  @Test
  public void getTableFieldAndTypeActionTest() {
    mockConstruction( Database.class, (mock, context) -> {
      ValueMetaBase valueMetaBase1 = new ValueMetaBase( "column1", ValueMetaInterface.TYPE_STRING );
      ValueMetaBase valueMetaBase2 = new ValueMetaBase( "column2", ValueMetaInterface.TYPE_INTEGER );
      valueMetaInterfaceList.add( valueMetaBase1 );
      valueMetaInterfaceList.add( valueMetaBase2 );

      rowMeta.setValueMetaList( valueMetaInterfaceList );
      doNothing().when( mock ).connect();
      when( mock.getTableFieldsMeta( schema, table ) ).thenReturn( rowMeta );

      Method getTableFieldAndTypeMethod = DatabaseLookup.class.getDeclaredMethod( "getTableFieldAndTypeAction", Map.class );
      getTableFieldAndTypeMethod.setAccessible( true );
      JSONObject jsonObject = (JSONObject) getTableFieldAndTypeMethod.invoke( databaseLookup, queryParams );

      Assert.assertNotNull( jsonObject );
      Assert.assertEquals( StepInterface.SUCCESS_RESPONSE, jsonObject.get( StepInterface.ACTION_STATUS ) );
      JSONArray jsonArray = (JSONArray) jsonObject.get( "columns" );
      Assert.assertEquals( 2, jsonArray.size() );
      mock.close();
    } );
  }

  @Test
  public void getNullConnectionTest() throws Exception {
    queryParams.put( "connection", null );

    Method getTableFieldAndTypeMethod = DatabaseLookup.class.getDeclaredMethod( "getTableFieldAndTypeAction", Map.class );
    getTableFieldAndTypeMethod.setAccessible( true );
    JSONObject jsonObject = (JSONObject) getTableFieldAndTypeMethod.invoke( databaseLookup, queryParams );

    Assert.assertNotNull( jsonObject );
    Assert.assertEquals( StepInterface.FAILURE_RESPONSE, jsonObject.get( StepInterface.ACTION_STATUS ) );
    Assert.assertNotNull( jsonObject.get( "error" ) );
  }

  @Test
  public void getNullRowMetaTest() throws Exception {
    try ( Database database = mock( Database.class ) ) {
      doNothing().when( database ).connect();
      when( database.getTableFieldsMeta( schema, table ) ).thenReturn( null );
      Method getTableFieldAndTypeMethod = DatabaseLookup.class.getDeclaredMethod( "getTableFieldAndTypeAction", Map.class );
      getTableFieldAndTypeMethod.setAccessible( true );
      JSONObject jsonObject = (JSONObject) getTableFieldAndTypeMethod.invoke( databaseLookup, queryParams );

      Assert.assertNotNull( jsonObject );
      Assert.assertEquals( StepInterface.SUCCESS_RESPONSE, jsonObject.get( StepInterface.ACTION_STATUS ) );
      JSONArray jsonArray = (JSONArray) jsonObject.get( "columns" );
      Assert.assertEquals( 0, jsonArray.size() );
    }
  }

  @Test
  public void expectExceptionWithDatabaseMetaTest() throws Exception {
    when( helper.transMeta.findDatabase( connection ) ).thenReturn( null );

    Method getTableFieldAndTypeMethod = DatabaseLookup.class.getDeclaredMethod( "getTableFieldAndTypeAction", Map.class );
    getTableFieldAndTypeMethod.setAccessible( true );
    JSONObject jsonObject = (JSONObject) getTableFieldAndTypeMethod.invoke( databaseLookup, queryParams );

    Assert.assertNotNull( jsonObject );
    Assert.assertEquals( StepInterface.FAILURE_RESPONSE, jsonObject.get( StepInterface.ACTION_STATUS ) );
    Assert.assertNotNull( jsonObject.get( "error" ) );
  }

  @Test
  public void expectExceptionWithDatabaseTest() throws Exception {
    try ( Database database = mock( Database.class ) ) {
      doThrow( new KettleDatabaseException() ).when( database ).connect();

      Method getTableFieldAndTypeMethod = DatabaseLookup.class.getDeclaredMethod( "getTableFieldAndTypeAction", Map.class );
      getTableFieldAndTypeMethod.setAccessible( true );
      JSONObject jsonObject = (JSONObject) getTableFieldAndTypeMethod.invoke( databaseLookup, queryParams );

      Assert.assertNotNull( jsonObject );
      Assert.assertEquals( StepInterface.SUCCESS_RESPONSE, jsonObject.get( StepInterface.ACTION_STATUS ) );
      JSONArray jsonArray = (JSONArray) jsonObject.get( "columns" );
      Assert.assertEquals( 0, jsonArray.size() );
    }
  }
}
