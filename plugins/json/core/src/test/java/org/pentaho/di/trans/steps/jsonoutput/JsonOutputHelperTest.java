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

package org.pentaho.di.trans.steps.jsonoutput;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

public class JsonOutputHelperTest {

  private JsonOutputHelper helper;
  private Map<String, String> queryParams;
  private TransMeta transMeta;
  private StepMeta stepMeta;
  private JsonOutputMeta jsonOutputMeta;

  @Before
  public void setUp() {
    helper = new JsonOutputHelper();
    queryParams = new HashMap<>();
    queryParams.put( "stepName", "step1" );
    transMeta = mock( TransMeta.class );
    stepMeta = mock( StepMeta.class );
    jsonOutputMeta = mock( JsonOutputMeta.class );
  }

  @Test
  public void testHandleStepAction_getOperationTypes() {
    JSONObject result = helper.handleStepAction( "getOperationTypes", transMeta, queryParams );
    assertEquals( StepInterface.SUCCESS_RESPONSE, result.get( StepInterface.ACTION_STATUS ) );
    assertThat( result.get( "operationTypes" ), instanceOf( JSONArray.class ) );
  }

  @Test
  public void testHandleStepAction_getEncodingTypes() {
    JSONObject result = helper.handleStepAction( "getEncodingTypes", transMeta, queryParams );
    assertEquals( StepInterface.SUCCESS_RESPONSE, result.get( StepInterface.ACTION_STATUS ) );
    JSONArray arr = (JSONArray) result.get( "encoding" );
    assertThat( arr.size(), is( Charset.availableCharsets().size() ) );
  }

  @Test
  public void testHandleStepAction_showFileName_success() {
    when( transMeta.findStep( "step1" ) ).thenReturn( stepMeta );
    when( stepMeta.getStepMetaInterface() ).thenReturn( jsonOutputMeta );
    when( jsonOutputMeta.getFileName() ).thenReturn( "file" );
    when( jsonOutputMeta.getNrRowsInBloc() ).thenReturn( "1" );
    when( jsonOutputMeta.isDateInFilename() ).thenReturn( false );
    when( jsonOutputMeta.isTimeInFilename() ).thenReturn( false );
    when( jsonOutputMeta.getExtension() ).thenReturn( "json" );

    JSONObject result = helper.handleStepAction( "showFileName", transMeta, queryParams );
    JSONArray files = (JSONArray) result.get( "files" );
    assertEquals( 1, files.size() );
    assertTrue( files.get( 0 ).toString().startsWith( "file" ) );
    assertTrue( files.get( 0 ).toString().endsWith( ".json" ) );
  }

  @Test
  public void testHandleStepAction_showFileName_multipleFiles() {
    when( transMeta.findStep( "step1" ) ).thenReturn( stepMeta );
    when( stepMeta.getStepMetaInterface() ).thenReturn( jsonOutputMeta );
    when( jsonOutputMeta.getFileName() ).thenReturn( "file" );
    when( jsonOutputMeta.getNrRowsInBloc() ).thenReturn( "5" );
    when( jsonOutputMeta.isDateInFilename() ).thenReturn( true );
    when( jsonOutputMeta.isTimeInFilename() ).thenReturn( true );
    when( jsonOutputMeta.getExtension() ).thenReturn( "json" );

    JSONObject result = helper.handleStepAction( "showFileName", transMeta, queryParams );
    JSONArray files = (JSONArray) result.get( "files" );
    assertEquals( 3, files.size() );
    for ( Object f : files ) {
      String name = f.toString();
      assertTrue( name.contains( "_" ) );
      assertTrue( name.endsWith( ".json" ) );
    }
  }

  @Test
  public void testHandleStepAction_invalidMethod() {
    JSONObject result = helper.handleStepAction( "invalid", transMeta, queryParams );
    assertEquals( JsonOutputHelper.FAILURE_METHOD_NOT_FOUND_RESPONSE, result.get( JsonOutputHelper.ACTION_STATUS ) );
  }

  @Test
  public void testHandleStepAction_exception() {
    TransMeta brokenTransMeta = mock( TransMeta.class );
    when( brokenTransMeta.findStep( anyString() ) ).thenThrow( new RuntimeException( "fail" ) );
    JSONObject result = helper.handleStepAction( "showFileName", brokenTransMeta, queryParams );
    assertEquals( JsonOutputHelper.FAILURE_RESPONSE, result.get( JsonOutputHelper.ACTION_STATUS ) );
  }

  @Test
  public void testGetOperationTypesAction() {
    JSONObject result = helper.getOperationTypesAction( null );
    assertEquals( StepInterface.SUCCESS_RESPONSE, result.get( StepInterface.ACTION_STATUS ) );
    JSONArray arr = (JSONArray) result.get( "operationTypes" );
    assertEquals( JsonOutputMeta.operationTypeCode.length, arr.size() );
    for ( int i = 0; i < arr.size(); i++ ) {
      JSONObject op = (JSONObject) arr.get( i );
      assertEquals( JsonOutputMeta.operationTypeCode[ i ], op.get( "id" ) );
      assertEquals( JsonOutputMeta.operationTypeDesc[ i ], op.get( "name" ) );
    }
  }

  @Test
  public void testGetEncodingTypesAction() {
    JSONObject result = helper.getEncodingTypesAction( null );
    assertEquals( StepInterface.SUCCESS_RESPONSE, result.get( StepInterface.ACTION_STATUS ) );
    JSONArray arr = (JSONArray) result.get( "encoding" );
    assertThat( arr.size(), is( Charset.availableCharsets().size() ) );
  }

  @Test
  public void testShowFileNameAction_emptyFileName() {
    when( transMeta.findStep( "step1" ) ).thenReturn( stepMeta );
    when( stepMeta.getStepMetaInterface() ).thenReturn( jsonOutputMeta );
    when( jsonOutputMeta.getFileName() ).thenReturn( "" );
    when( jsonOutputMeta.getNrRowsInBloc() ).thenReturn( "1" );
    when( jsonOutputMeta.isDateInFilename() ).thenReturn( false );
    when( jsonOutputMeta.isTimeInFilename() ).thenReturn( false );
    when( jsonOutputMeta.getExtension() ).thenReturn( null );

    try ( MockedStatic<BaseMessages> baseMessages = mockStatic( BaseMessages.class ) ) {
      baseMessages.when( () -> BaseMessages.getString( any(), anyString() ) )
        .thenReturn( "No files found" );
      JSONObject result = helper.showFileNameAction( transMeta, queryParams );
      assertThat( result.get( "files" ), instanceOf( JSONArray.class ) );
    }
  }

  @Test
  public void testShowFileNameAction_nullFileName() {
    when( transMeta.findStep( "step1" ) ).thenReturn( stepMeta );
    when( stepMeta.getStepMetaInterface() ).thenReturn( jsonOutputMeta );
    when( jsonOutputMeta.getFileName() ).thenReturn( null );
    when( jsonOutputMeta.getNrRowsInBloc() ).thenReturn( "1" );
    when( jsonOutputMeta.isDateInFilename() ).thenReturn( false );
    when( jsonOutputMeta.isTimeInFilename() ).thenReturn( false );
    when( jsonOutputMeta.getExtension() ).thenReturn( null );

    try ( MockedStatic<BaseMessages> baseMessages = mockStatic( BaseMessages.class ) ) {
      baseMessages.when( () -> BaseMessages.getString( any(), anyString() ) )
        .thenReturn( "No files found" );
      JSONObject result = helper.showFileNameAction( transMeta, queryParams );
      assertThat( result.get( "files" ), instanceOf( JSONArray.class ) );
    }
  }

  @Test
  public void testShowFileNameAction_emptyExtension() {
    when( transMeta.findStep( "step1" ) ).thenReturn( stepMeta );
    when( stepMeta.getStepMetaInterface() ).thenReturn( jsonOutputMeta );
    when( jsonOutputMeta.getFileName() ).thenReturn( "file" );
    when( jsonOutputMeta.getNrRowsInBloc() ).thenReturn( "1" );
    when( jsonOutputMeta.isDateInFilename() ).thenReturn( false );
    when( jsonOutputMeta.isTimeInFilename() ).thenReturn( false );
    when( jsonOutputMeta.getExtension() ).thenReturn( "" );

    JSONObject result = helper.showFileNameAction( transMeta, queryParams );
    JSONArray files = (JSONArray) result.get( "files" );
    assertEquals( "file", files.get( 0 ).toString() );
  }

  @Test
  public void testShowFileNameAction_withIndexAndExtension() {
    when( transMeta.findStep( "step1" ) ).thenReturn( stepMeta );
    when( stepMeta.getStepMetaInterface() ).thenReturn( jsonOutputMeta );
    when( jsonOutputMeta.getFileName() ).thenReturn( "file" );
    when( jsonOutputMeta.getNrRowsInBloc() ).thenReturn( "2" );
    when( jsonOutputMeta.isDateInFilename() ).thenReturn( false );
    when( jsonOutputMeta.isTimeInFilename() ).thenReturn( false );
    when( jsonOutputMeta.getExtension() ).thenReturn( "json" );

    JSONObject result = helper.showFileNameAction( transMeta, queryParams );
    JSONArray files = (JSONArray) result.get( "files" );
    assertEquals( 2, files.size() );
    assertTrue( files.get( 0 ).toString().contains( "_0.json" ) || files.get( 1 ).toString().contains( "_1.json" ) );
  }

  @Test
  public void testShowFileNameAction_withDateTime() {
    when( transMeta.findStep( "step1" ) ).thenReturn( stepMeta );
    when( stepMeta.getStepMetaInterface() ).thenReturn( jsonOutputMeta );
    when( jsonOutputMeta.getFileName() ).thenReturn( "file" );
    when( jsonOutputMeta.getNrRowsInBloc() ).thenReturn( "1" );
    when( jsonOutputMeta.isDateInFilename() ).thenReturn( true );
    when( jsonOutputMeta.isTimeInFilename() ).thenReturn( true );
    when( jsonOutputMeta.getExtension() ).thenReturn( "json" );

    JSONObject result = helper.showFileNameAction( transMeta, queryParams );
    JSONArray files = (JSONArray) result.get( "files" );
    String name = files.get( 0 ).toString();
    assertTrue( name.matches( "file_\\d{8}_\\d{6}\\.json" ) );
  }

  @Test
  public void testShowFileNameAction_filesToShowMax3() {
    when( transMeta.findStep( "step1" ) ).thenReturn( stepMeta );
    when( stepMeta.getStepMetaInterface() ).thenReturn( jsonOutputMeta );
    when( jsonOutputMeta.getFileName() ).thenReturn( "file" );
    when( jsonOutputMeta.getNrRowsInBloc() ).thenReturn( "10" );
    when( jsonOutputMeta.isDateInFilename() ).thenReturn( false );
    when( jsonOutputMeta.isTimeInFilename() ).thenReturn( false );
    when( jsonOutputMeta.getExtension() ).thenReturn( "json" );

    JSONObject result = helper.showFileNameAction( transMeta, queryParams );
    JSONArray files = (JSONArray) result.get( "files" );
    assertEquals( 3, files.size() );
  }
}
