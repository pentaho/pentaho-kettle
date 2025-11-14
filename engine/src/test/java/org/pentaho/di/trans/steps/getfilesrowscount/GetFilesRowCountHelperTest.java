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

package org.pentaho.di.trans.steps.getfilesrowscount;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.di.core.fileinput.FileInputList;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepHelper;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@RunWith( MockitoJUnitRunner.class )
public class GetFilesRowCountHelperTest {

  @Mock
  private TransMeta transMeta;

  @Mock
  private GetFilesRowsCountMeta getFilesRowsCountMeta;

  @Mock
  private StepMeta stepMeta;

  @Mock
  private FileInputList fileInputList;

  private GetFilesRowCountHelper helper;
  private Map<String, String> queryParams;

  @Before
  public void setUp() {
    helper = new GetFilesRowCountHelper();
    queryParams = new HashMap<>();
    queryParams.put( "stepName", "testStep" );
  }

  @Test
  public void testConstructor() {
    GetFilesRowCountHelper newHelper = new GetFilesRowCountHelper();
    assertNotNull( newHelper );
  }

  @Test
  public void testHandleStepAction_ShowFiles_Success() {
    // Setup mocks
    when( transMeta.findStep( "testStep" ) ).thenReturn( stepMeta );
    when( stepMeta.getStepMetaInterface() ).thenReturn( getFilesRowsCountMeta );
    when( getFilesRowsCountMeta.getFiles( any(), any() ) ).thenReturn( fileInputList );
    when( fileInputList.getFileStrings() ).thenReturn( new String[] { "file1.txt", "file2.txt" } );

    // Mock BaseMessages static call
    try ( MockedStatic<BaseMessages> mockedBaseMessages = mockStatic( BaseMessages.class ) ) {
      JSONObject result = helper.handleStepAction( "showFiles", transMeta, queryParams );

      assertNotNull( result );
      JSONArray files = (JSONArray) result.get( "files" );
      assertNotNull( files );
      assertEquals( 2, files.size() );
      assertTrue( files.contains( "file1.txt" ) );
      assertTrue( files.contains( "file2.txt" ) );
      assertEquals( StepInterface.SUCCESS_RESPONSE, result.get( StepInterface.ACTION_STATUS ) );
    }
  }

  @Test
  public void testHandleStepAction_InvalidMethods() {
    String[] invalidMethods = { "invalidMethod", null, "" };

    for ( String method : invalidMethods ) {
      JSONObject result = helper.handleStepAction( method, transMeta, queryParams );

      assertNotNull( "Result should not be null for method: " + method, result );
      assertEquals( "Should return FAILURE_METHOD_NOT_FOUND_RESPONSE for method: " + method,
        BaseStepHelper.FAILURE_METHOD_NOT_FOUND_RESPONSE, result.get( BaseStepHelper.ACTION_STATUS ) );
    }
  }

  @Test
  public void testHandleStepAction_Exception() {
    when( transMeta.findStep( anyString() ) ).thenThrow( new RuntimeException( "Test exception" ) );

    JSONObject result = helper.handleStepAction( "showFiles", transMeta, queryParams );

    assertNotNull( result );
    assertEquals( BaseStepHelper.FAILURE_RESPONSE, result.get( BaseStepHelper.ACTION_STATUS ) );
  }

  @Test
  public void testGetFilesAction_WithFiles() {
    // Setup mocks
    when( transMeta.findStep( "testStep" ) ).thenReturn( stepMeta );
    when( stepMeta.getStepMetaInterface() ).thenReturn( getFilesRowsCountMeta );
    when( getFilesRowsCountMeta.getFiles( any(), any() ) ).thenReturn( fileInputList );
    when( fileInputList.getFileStrings() ).thenReturn( new String[] { "file1.txt", "file2.txt", "file3.txt" } );

    try ( MockedStatic<BaseMessages> mockedBaseMessages = mockStatic( BaseMessages.class ) ) {
      JSONObject result = helper.getFilesAction( transMeta, queryParams );

      assertNotNull( result );
      JSONArray files = (JSONArray) result.get( "files" );
      assertNotNull( files );
      assertEquals( 3, files.size() );
      assertTrue( files.contains( "file1.txt" ) );
      assertTrue( files.contains( "file2.txt" ) );
      assertTrue( files.contains( "file3.txt" ) );
      assertEquals( StepInterface.SUCCESS_RESPONSE, result.get( StepInterface.ACTION_STATUS ) );
    }
  }

  @Test
  public void testGetFilesAction_NoFiles_NullArray() {
    // Setup mocks
    when( transMeta.findStep( "testStep" ) ).thenReturn( stepMeta );
    when( stepMeta.getStepMetaInterface() ).thenReturn( getFilesRowsCountMeta );
    when( getFilesRowsCountMeta.getFiles( any(), any() ) ).thenReturn( fileInputList );
    when( fileInputList.getFileStrings() ).thenReturn( null );

    try ( MockedStatic<BaseMessages> mockedBaseMessages = mockStatic( BaseMessages.class ) ) {
      mockedBaseMessages.when( () -> BaseMessages.getString( GetFilesRowsCountMeta.class,
          "GetFilesRowsCountDialog.NoFileFound.DialogMessage" ) )
        .thenReturn( "No files found" );

      JSONObject result = helper.getFilesAction( transMeta, queryParams );

      assertNotNull( result );
      JSONArray files = (JSONArray) result.get( "files" );
      assertNotNull( files );
      assertEquals( 0, files.size() );
      assertEquals( "No files found", result.get( "message" ) );
      assertNull( result.get( StepInterface.ACTION_STATUS ) );
    }
  }

  @Test
  public void testGetFilesAction_NoFiles_EmptyArray() {
    // Setup mocks
    when( transMeta.findStep( "testStep" ) ).thenReturn( stepMeta );
    when( stepMeta.getStepMetaInterface() ).thenReturn( getFilesRowsCountMeta );
    when( getFilesRowsCountMeta.getFiles( any(), any() ) ).thenReturn( fileInputList );
    when( fileInputList.getFileStrings() ).thenReturn( new String[ 0 ] );

    try ( MockedStatic<BaseMessages> mockedBaseMessages = mockStatic( BaseMessages.class ) ) {
      mockedBaseMessages.when( () -> BaseMessages.getString( GetFilesRowsCountMeta.class,
          "GetFilesRowsCountDialog.NoFileFound.DialogMessage" ) )
        .thenReturn( "No files found" );

      JSONObject result = helper.getFilesAction( transMeta, queryParams );

      assertNotNull( result );
      JSONArray files = (JSONArray) result.get( "files" );
      assertNotNull( files );
      assertEquals( 0, files.size() );
      assertEquals( "No files found", result.get( "message" ) );
      assertNull( result.get( StepInterface.ACTION_STATUS ) );
    }
  }

  @Test
  public void testGetFilesAction_SingleFile() {
    // Setup mocks
    when( transMeta.findStep( "testStep" ) ).thenReturn( stepMeta );
    when( stepMeta.getStepMetaInterface() ).thenReturn( getFilesRowsCountMeta );
    when( getFilesRowsCountMeta.getFiles( any(), any() ) ).thenReturn( fileInputList );
    when( fileInputList.getFileStrings() ).thenReturn( new String[] { "singleFile.txt" } );

    try ( MockedStatic<BaseMessages> mockedBaseMessages = mockStatic( BaseMessages.class ) ) {
      JSONObject result = helper.getFilesAction( transMeta, queryParams );

      assertNotNull( result );
      JSONArray files = (JSONArray) result.get( "files" );
      assertNotNull( files );
      assertEquals( 1, files.size() );
      assertTrue( files.contains( "singleFile.txt" ) );
      assertEquals( StepInterface.SUCCESS_RESPONSE, result.get( StepInterface.ACTION_STATUS ) );
    }
  }

  @Test
  public void testGetFilesAction_ExceptionInGetFiles() {
    // Setup mocks to throw exception during getFilesAction execution
    when( transMeta.findStep( "testStep" ) ).thenReturn( stepMeta );
    when( stepMeta.getStepMetaInterface() ).thenReturn( getFilesRowsCountMeta );
    when( getFilesRowsCountMeta.getFiles( any(), any() ) ).thenThrow( new RuntimeException( "File access error" ) );

    JSONObject result = helper.handleStepAction( "showFiles", transMeta, queryParams );

    assertNotNull( result );
    assertEquals( BaseStepHelper.FAILURE_RESPONSE, result.get( BaseStepHelper.ACTION_STATUS ) );
  }

  @Test
  public void testGetFilesAction_NullStepMeta() {
    // Setup mocks
    when( transMeta.findStep( "testStep" ) ).thenReturn( null );

    JSONObject result = helper.handleStepAction( "showFiles", transMeta, queryParams );

    assertNotNull( result );
    assertEquals( BaseStepHelper.FAILURE_RESPONSE, result.get( BaseStepHelper.ACTION_STATUS ) );
  }

  @Test
  public void testGetFilesAction_CastException() {
    // Setup mocks to cause ClassCastException
    when( transMeta.findStep( "testStep" ) ).thenReturn( stepMeta );
    when( stepMeta.getStepMetaInterface() ).thenReturn( null );

    JSONObject result = helper.handleStepAction( "showFiles", transMeta, queryParams );

    assertNotNull( result );
    assertEquals( BaseStepHelper.FAILURE_RESPONSE, result.get( BaseStepHelper.ACTION_STATUS ) );
  }

  @Test
  public void testGetFilesAction_NullQueryParams() {
    // Test with null queryParams
    JSONObject result = helper.handleStepAction( "showFiles", transMeta, null );

    assertNotNull( result );
    assertEquals( BaseStepHelper.FAILURE_RESPONSE, result.get( BaseStepHelper.ACTION_STATUS ) );
  }

  @Test
  public void testGetFilesAction_EmptyQueryParams() {
    // Test with empty queryParams
    Map<String, String> emptyParams = new HashMap<>();
    JSONObject result = helper.handleStepAction( "showFiles", transMeta, emptyParams );

    assertNotNull( result );
    assertEquals( BaseStepHelper.FAILURE_RESPONSE, result.get( BaseStepHelper.ACTION_STATUS ) );
  }

  @Test
  public void testGetFilesAction_DirectCall_WithFiles() {
    when( transMeta.findStep( "testStep" ) ).thenReturn( stepMeta );
    when( stepMeta.getStepMetaInterface() ).thenReturn( getFilesRowsCountMeta );
    when( getFilesRowsCountMeta.getFiles( any(), any() ) ).thenReturn( fileInputList );
    when( fileInputList.getFileStrings() ).thenReturn( new String[] { "direct.txt" } );

    try ( MockedStatic<BaseMessages> mockedBaseMessages = mockStatic( BaseMessages.class ) ) {
      JSONObject result = helper.getFilesAction( transMeta, queryParams );

      assertNotNull( result );
      JSONArray files = (JSONArray) result.get( "files" );
      assertNotNull( files );
      assertEquals( 1, files.size() );
      assertTrue( files.contains( "direct.txt" ) );
      assertEquals( StepInterface.SUCCESS_RESPONSE, result.get( StepInterface.ACTION_STATUS ) );
    }
  }

  @Test
  public void testGetFilesAction_DirectCall_NoFiles() {
    when( transMeta.findStep( "testStep" ) ).thenReturn( stepMeta );
    when( stepMeta.getStepMetaInterface() ).thenReturn( getFilesRowsCountMeta );
    when( getFilesRowsCountMeta.getFiles( any(), any() ) ).thenReturn( fileInputList );
    when( fileInputList.getFileStrings() ).thenReturn( null );

    try ( MockedStatic<BaseMessages> mockedBaseMessages = mockStatic( BaseMessages.class ) ) {
      mockedBaseMessages.when( () -> BaseMessages.getString( GetFilesRowsCountMeta.class,
          "GetFilesRowsCountDialog.NoFileFound.DialogMessage" ) )
        .thenReturn( "No files found directly" );

      JSONObject result = helper.getFilesAction( transMeta, queryParams );

      assertNotNull( result );
      JSONArray files = (JSONArray) result.get( "files" );
      assertNotNull( files );
      assertEquals( 0, files.size() );
      assertEquals( "No files found directly", result.get( "message" ) );
      assertNull( result.get( StepInterface.ACTION_STATUS ) );
    }
  }
}
