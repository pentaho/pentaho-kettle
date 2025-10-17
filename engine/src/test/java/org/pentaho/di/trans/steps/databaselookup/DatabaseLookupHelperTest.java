/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2025 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 *
 ******************************************************************************/

package org.pentaho.di.trans.steps.databaselookup;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.MockedConstruction;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaBase;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepHelperInterface;
import org.pentaho.di.trans.step.StepInterface;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;
import static org.pentaho.di.trans.steps.databaselookup.DatabaseLookupHelper.COLUMNS;
import static org.pentaho.di.trans.steps.databaselookup.DatabaseLookupHelper.CONNECTION;
import static org.pentaho.di.trans.steps.databaselookup.DatabaseLookupHelper.GET_TABLE_FIELD_AND_TYPE;
import static org.pentaho.di.trans.steps.databaselookup.DatabaseLookupHelper.SCHEMA;
import static org.pentaho.di.trans.steps.databaselookup.DatabaseLookupHelper.TABLE;

public class DatabaseLookupHelperTest {

  DatabaseMeta databaseMeta;
  Map<String, String> queryParams;
  List<ValueMetaInterface> valueMetaInterfaceList;
  RowMeta rowMeta;
  String connection;
  String schema;
  String table;
  DatabaseLookupHelper databaseLookupHelper;
  TransMeta transMeta;

  @BeforeClass
  public static void init() throws KettleException {
    KettleClientEnvironment.init();
  }

  @Before
  public void setUp() {
    databaseLookupHelper = new DatabaseLookupHelper();
    transMeta = mock( TransMeta.class );
    connection = "Hypersonic";
    schema = "public";
    table = "employee";

    queryParams = new HashMap<>();
    queryParams.put( CONNECTION, connection );
    queryParams.put( SCHEMA, schema );
    queryParams.put( TABLE, table );

    databaseMeta = mock( DatabaseMeta.class );
    when( transMeta.findDatabase( connection ) ).thenReturn( databaseMeta );
    when( transMeta.environmentSubstitute( schema ) ).thenReturn( schema );
    when( transMeta.environmentSubstitute( table ) ).thenReturn( table );

    valueMetaInterfaceList = new ArrayList<>();
    rowMeta = new RowMeta();
  }

  @Test
  public void getTableFieldAndTypeActionTest() {
    try ( MockedConstruction<Database> ignored = mockConstruction( Database.class, (mock, context) -> {
      ValueMetaBase valueMetaBase1 = new ValueMetaBase( "column1", ValueMetaInterface.TYPE_STRING );
      ValueMetaBase valueMetaBase2 = new ValueMetaBase( "column2", ValueMetaInterface.TYPE_INTEGER );
      valueMetaInterfaceList.add( valueMetaBase1 );
      valueMetaInterfaceList.add( valueMetaBase2 );

      rowMeta.setValueMetaList( valueMetaInterfaceList );
      doNothing().when( mock ).connect();
      when( mock.getTableFieldsMeta( schema, table ) ).thenReturn( rowMeta );
    } ) ) {
      JSONObject jsonObject = databaseLookupHelper.stepAction( GET_TABLE_FIELD_AND_TYPE, transMeta, queryParams );

      assertNotNull( jsonObject );
      assertEquals( StepInterface.SUCCESS_RESPONSE, jsonObject.get( StepInterface.ACTION_STATUS ) );
      JSONArray jsonArray = (JSONArray) jsonObject.get( "columns" );
      assertEquals(  2, jsonArray.size() );
    }
  }

  @Test
  public void getNullConnectionTest() {
    getNullParamTest( CONNECTION );
  }

  @Test
  public void getNullSchemaTest() {
    getNullParamTest( SCHEMA );
  }

  @Test
  public void getNullTableTest() {
    getNullParamTest( TABLE );
  }

  @Test
  public void getNullRowMetaTest() {
    try ( MockedConstruction<Database> ignored = mockConstruction( Database.class, (mock, context) -> {
      doNothing().when( mock ).connect();
      when( mock.getTableFieldsMeta( schema, table ) ).thenReturn( null );
    } ) ) {
      JSONObject jsonObject = databaseLookupHelper.stepAction( GET_TABLE_FIELD_AND_TYPE, transMeta, queryParams );

      assertNotNull( jsonObject );
      assertEquals( StepInterface.SUCCESS_RESPONSE, jsonObject.get( StepInterface.ACTION_STATUS ) );
      JSONArray jsonArray = (JSONArray) jsonObject.get( COLUMNS );
      assertEquals( 0, jsonArray.size() );
    }
  }

  @Test
  public void invalidStepAction_forDatabaseLookup() {
    JSONObject jsonObject = databaseLookupHelper.stepAction( "invalidAction", transMeta, queryParams );

    assertNotNull( jsonObject );
    assertEquals( StepHelperInterface.FAILURE_METHOD_NOT_FOUND_RESPONSE, jsonObject.get( StepInterface.ACTION_STATUS ) );
  }

  private void getNullParamTest( String paramKey ) {
    queryParams.put( paramKey, null );

    JSONObject jsonObject = databaseLookupHelper.stepAction( GET_TABLE_FIELD_AND_TYPE, transMeta, queryParams );

    assertNotNull( jsonObject );
    assertEquals( StepInterface.FAILURE_RESPONSE, jsonObject.get( StepInterface.ACTION_STATUS ) );
    assertNotNull( jsonObject.get( "error" ) );
  }
}
