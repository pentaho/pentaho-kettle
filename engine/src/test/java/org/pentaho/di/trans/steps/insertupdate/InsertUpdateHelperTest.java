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

package org.pentaho.di.trans.steps.insertupdate;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.SQLStatement;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.mock.StepMockHelper;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class InsertUpdateHelperTest {

  private StepMockHelper<InsertUpdateMeta, InsertUpdateData> mockHelper;

  @Before
  public void setUp() {
    mockHelper = new StepMockHelper<>( "InsertUpdate", InsertUpdateMeta.class, InsertUpdateData.class );
    when( mockHelper.logChannelInterfaceFactory.create( any(), any() ) )
      .thenReturn( mockHelper.logChannelInterface );
  }

  @Test
  public void testGetComparatorsAction() {
    InsertUpdateHelper helper = new InsertUpdateHelper();
    JSONObject response = helper.getComparatorsAction();
    Assert.assertNotNull( response );
    JSONArray comparators = (JSONArray) response.get( "comparators" );
    Assert.assertNotNull( comparators );
    assertEquals( 11, comparators.size() );
  }

  @Test
  public void testGetSQLAction() throws Exception {
    InsertUpdateMeta mockMeta = mock( InsertUpdateMeta.class );
    StepMeta mockStepMeta = mock( StepMeta.class );
    when( mockStepMeta.getStepMetaInterface() ).thenReturn( mockMeta );
    when( mockStepMeta.getName() ).thenReturn( "InsertUpdateStep" );
    RowMetaInterface mockRowMeta = new RowMeta();
    when( mockHelper.transMeta.getPrevStepFields( "InsertUpdateStep" ) ).thenReturn( mockRowMeta );
    DatabaseMeta mockDbMeta = mock( DatabaseMeta.class );
    SQLStatement mockSQL = new SQLStatement( "InsertUpdateStep", mockDbMeta, "SELECT * FROM dummy_table" );
    when( mockMeta.getSQLStatements( any(), any(), any(), any(), any() ) ).thenReturn( mockSQL );
    InsertUpdateHelper helper = new InsertUpdateHelper();
    JSONObject response = helper.getSQLAction( mockHelper.transMeta, mockStepMeta );
    Assert.assertNotNull( response );
    Assert.assertTrue( response.containsKey( "sql" ) );
    assertEquals( "SELECT * FROM dummy_table", response.get( "sql" ) );
  }

  @Test
  public void testGetSQLActionStepMetaNull() {
    InsertUpdateHelper helper = new InsertUpdateHelper();
    JSONObject response = helper.getSQLAction( mockHelper.transMeta, null );
    Assert.assertNotNull( response );
    Assert.assertTrue( response.containsKey( "error" ) );
    assertEquals( "There is no connection defined in this step.", response.get( "error" ) );
  }

  @Test
  public void testGetSQLActionWithSQLError() throws Exception {
    InsertUpdateMeta mockMeta = mock( InsertUpdateMeta.class );
    StepMeta mockStepMeta = mock( StepMeta.class );
    when( mockStepMeta.getStepMetaInterface() ).thenReturn( mockMeta );
    when( mockStepMeta.getName() ).thenReturn( "InsertUpdateStep" );
    SQLStatement sqlWithError = new SQLStatement( "InsertUpdateStep", null, null );
    sqlWithError.setError( "SQL error occurred" );
    when( mockMeta.getSQLStatements( any(), any(), any(), any(), any() ) ).thenReturn( sqlWithError );
    InsertUpdateHelper helper = new InsertUpdateHelper();
    JSONObject response = helper.getSQLAction( mockHelper.transMeta, mockStepMeta );
    Assert.assertNotNull( response );
    Assert.assertTrue( response.containsKey( "error" ) );
    assertEquals( "SQL error occurred", response.get( "error" ) );
  }

  @Test
  public void testGetSQLActionWithException() throws Exception {
    InsertUpdateMeta mockMeta = mock( InsertUpdateMeta.class );
    StepMeta mockStepMeta = mock( StepMeta.class );
    when( mockStepMeta.getStepMetaInterface() ).thenReturn( mockMeta );
    when( mockStepMeta.getName() ).thenReturn( "InsertUpdateStep" );
    when( mockHelper.transMeta.getPrevStepFields( anyString() ) ).thenThrow( new RuntimeException( "Test Exception" ) );
    InsertUpdateHelper helper = new InsertUpdateHelper();
    JSONObject response = helper.getSQLAction( mockHelper.transMeta, mockStepMeta );
    Assert.assertNotNull( response );
    Assert.assertTrue( response.containsKey( "error" ) );
    Assert.assertTrue( response.get( "error" ).toString().contains( "Error generating SQL: Test Exception" ) );
  }

  @Test
  public void testHandleStepActionGetSQL() {
    InsertUpdateHelper helper = spy( new InsertUpdateHelper() );
    JSONObject expected = new JSONObject();
    expected.put( "sql", "SELECT 1" );
    doReturn( expected ).when( helper ).getSQLAction( any(), any() );
    JSONObject response = helper.handleStepAction( "getSQL", mockHelper.transMeta, new HashMap<>() );
    assertEquals( expected, response );
  }

  @Test
  public void testHandleStepActionGetComparators() {
    InsertUpdateHelper helper = spy( new InsertUpdateHelper() );
    JSONObject expected = new JSONObject();
    expected.put( "comparators", new JSONArray() );
    doReturn( expected ).when( helper ).getComparatorsAction();
    JSONObject response = helper.handleStepAction( "getComparators", mockHelper.transMeta, new HashMap<>() );
    assertEquals( expected, response );
  }
}
