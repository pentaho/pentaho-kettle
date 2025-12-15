
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

package org.pentaho.di.trans.steps.getfilenames;

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

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@RunWith( MockitoJUnitRunner.class )
public class GetFileNamesHelperTest {

  @Mock
  private TransMeta transMeta;

  @Mock
  private GetFileNamesMeta getFileNamesMeta;

  @Mock
  private FileInputList fileInputList;

  private GetFileNamesHelper helper;
  private Map<String, String> queryParams;

  @Before
  public void setUp() {
    helper = new GetFileNamesHelper( getFileNamesMeta );
    queryParams = new HashMap<>();
    queryParams.put( "stepName", "testStep" );
  }

  @Test
  public void testConstructor() {
    GetFileNamesHelper newHelper = new GetFileNamesHelper( getFileNamesMeta );
    assertNotNull( newHelper );
  }

  @Test
  public void testHandleStepAction_ShowFiles_Success() {
    // Setup mocks
    when( getFileNamesMeta.getFileList( any(), any() ) ).thenReturn( fileInputList );
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

    JSONObject result = helper.handleStepAction( "showFiles", transMeta, queryParams );

    assertNotNull( result );
    assertEquals( BaseStepHelper.FAILURE_RESPONSE, result.get( BaseStepHelper.ACTION_STATUS ) );
  }

  @Test
  public void testGetFilesAction_WithFiles() {
    // Setup mocks
    when( getFileNamesMeta.getFileList( any(), any() ) ).thenReturn( fileInputList );
    when( fileInputList.getFileStrings() ).thenReturn( new String[] { "file1.txt", "file2.txt", "file3.txt" } );

    try ( MockedStatic<BaseMessages> mockedBaseMessages = mockStatic( BaseMessages.class ) ) {
      JSONObject result = helper.getFilesAction( transMeta );

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
  public void testGetFilesAction_ParameterizedScenarios() {
    Object[][] testData = {
      { "NullArray", null, 0, "No files found", null },
      { "EmptyArray", new String[ 0 ], 0, "No files found", null },
      { "SingleFile", new String[] { "singleFile.txt" }, 1, null, StepInterface.SUCCESS_RESPONSE }
    };

    for ( Object[] data : testData ) {
      String[] fileArray = (String[]) data[ 1 ];
      int expectedSize = (Integer) data[ 2 ];
      String expectedMessage = (String) data[ 3 ];
      String expectedStatus = (String) data[ 4 ];

      testGetFilesActionScenario( fileArray, expectedSize, expectedMessage, expectedStatus );
    }
  }

  private void testGetFilesActionScenario( String[] fileArray, int expectedSize,
                                           String expectedMessage, String expectedStatus ) {
    // Setup mocks
    when( getFileNamesMeta.getFileList( any(), any() ) ).thenReturn( fileInputList );
    when( fileInputList.getFileStrings() ).thenReturn( fileArray );

    try ( MockedStatic<BaseMessages> mockedBaseMessages = mockStatic( BaseMessages.class ) ) {
      if ( expectedMessage != null ) {
        mockedBaseMessages.when( () -> BaseMessages.getString( GetFileNamesMeta.class,
            "GetFileNamesDialog.NoFilesFound.DialogMessage" ) )
          .thenReturn( expectedMessage );
      }

      JSONObject result = helper.getFilesAction( transMeta );

      assertNotNull( result );
      JSONArray files = (JSONArray) result.get( "files" );
      assertNotNull( files );
      assertEquals( expectedSize, files.size() );

      if ( expectedMessage != null ) {
        assertEquals( expectedMessage, result.get( "message" ) );
      }

      if ( expectedStatus != null ) {
        assertEquals( expectedStatus, result.get( StepInterface.ACTION_STATUS ) );
      } else {
        assertNull( result.get( StepInterface.ACTION_STATUS ) );
      }

      // Additional check for single file case
      if ( fileArray != null && fileArray.length == 1 ) {
        assertTrue( files.contains( fileArray[ 0 ] ) );
      }
    }
  }

  @Test
  public void testGetFilesAction_ExceptionIngetFileList() {
    // Setup mocks to throw exception during getFilesAction execution
    when( getFileNamesMeta.getFileList( any(), any() ) ).thenThrow( new RuntimeException( "File access error" ) );

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
    when( getFileNamesMeta.getFileList( any(), any() ) ).thenReturn( fileInputList );
    when( fileInputList.getFileStrings() ).thenReturn( new String[] { "direct.txt" } );

    try ( MockedStatic<BaseMessages> mockedBaseMessages = mockStatic( BaseMessages.class ) ) {
      JSONObject result = helper.getFilesAction( transMeta );

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
    when( getFileNamesMeta.getFileList( any(), any() ) ).thenReturn( fileInputList );
    when( fileInputList.getFileStrings() ).thenReturn( null );

    try ( MockedStatic<BaseMessages> mockedBaseMessages = mockStatic( BaseMessages.class ) ) {
      mockedBaseMessages.when( () -> BaseMessages.getString( GetFileNamesMeta.class,
          "GetFileNamesDialog.NoFilesFound.DialogMessage" ) )
        .thenReturn( "No files found directly" );

      JSONObject result = helper.getFilesAction( transMeta );

      assertNotNull( result );
      JSONArray files = (JSONArray) result.get( "files" );
      assertNotNull( files );
      assertEquals( 0, files.size() );
      assertEquals( "No files found directly", result.get( "message" ) );
      assertNull( result.get( StepInterface.ACTION_STATUS ) );
    }
  }
}

