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

package org.pentaho.di.trans.steps.fileinput.text;

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.pentaho.di.core.compress.CompressionProviderFactory;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.fileinput.FileInputList;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepHelper;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.file.BaseFileField;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

public class TextFileInputHelperTest {
  @Mock private TransMeta transMeta;
  @Mock private org.pentaho.di.trans.step.StepMeta stepMeta;
  @Mock private TextFileInputMeta textFileInputMeta;
  @Mock private FileInputList fileInputList;
  @Mock private BaseFileField baseFileField;

  private TextFileInputHelper helper;
  private Map<String, String> queryParams;
  private AutoCloseable mocks;

  @Before
  public void setUp() {
    mocks = MockitoAnnotations.openMocks( this );
    helper = new TextFileInputHelper();
    queryParams = new HashMap<>();
    queryParams.put( "stepName", "testStep" );
  }

  @After
  public void tearDown() throws Exception {
    if ( mocks != null ) {
      mocks.close();
    }
  }

  @Test
  public void testHandleStepAction_GetFields() {
    setupBasicMocks();
    when( textFileInputMeta.getFileTypeNr() ).thenReturn( 1 ); // Non-CSV type

    try ( MockedStatic<TextFileInputUtils> mockedUtils = mockStatic( TextFileInputUtils.class ) ) {
      mockedUtils.when( () -> TextFileInputUtils.getLine( any(), any(), any(), anyInt(), any(), any(), any() ) )
        .thenReturn( "line1" );

      JSONObject result = helper.handleStepAction( "getFields", transMeta, queryParams );

      assertEquals( StepInterface.SUCCESS_RESPONSE, result.get( StepInterface.ACTION_STATUS ) );
      assertTrue( result.containsKey( "fields" ) );
    }
  }

  @Test
  public void testHandleStepAction_GetFieldNames() {
    setupBasicMocks();

    JSONObject result = helper.handleStepAction( "getFieldNames", transMeta, queryParams );

    assertEquals( StepInterface.SUCCESS_RESPONSE, result.get( StepInterface.ACTION_STATUS ) );
    assertTrue( result.containsKey( "fieldNames" ) );
  }

  @Test
  public void testHandleStepAction_ShowFiles() {
    setupBasicMocks();
    when( textFileInputMeta.getFilePaths( any(), any() ) ).thenReturn( new String[] { "file1.txt", "file2.txt" } );

    JSONObject result = helper.handleStepAction( "showFiles", transMeta, queryParams );

    assertEquals( StepInterface.SUCCESS_RESPONSE, result.get( StepInterface.ACTION_STATUS ) );
    assertTrue( result.containsKey( "files" ) );
  }

  @Test
  public void testHandleStepAction_ValidateShowContent() {
    setupBasicMocks();
    when( textFileInputMeta.getFileInputList( any(), any() ) ).thenReturn( fileInputList );
    when( fileInputList.nrOfFiles() ).thenReturn( 1 );

    JSONObject result = helper.handleStepAction( "validateShowContent", transMeta, queryParams );

    assertEquals( StepInterface.SUCCESS_RESPONSE, result.get( StepInterface.ACTION_STATUS ) );
  }

  @Test
  public void testShowFilesAction_WithRegexMatch_MultipleFiles() {
    setupBasicMocks();
    queryParams.put( "filter", ".*\\.csv" );
    queryParams.put( "isRegex", "true" );
    when( textFileInputMeta.getFilePaths( any(), any() ) )
      .thenReturn( new String[] { "data.csv", "notes.txt", "data2.csv" } );

    JSONObject result = helper.showFilesAction( transMeta, queryParams );
    JSONArray files = (JSONArray) result.get( "files" );

    assertEquals( StepInterface.SUCCESS_RESPONSE, result.get( StepInterface.ACTION_STATUS ) );
    assertEquals( 2, files.size() );
    assertTrue( files.contains( "data.csv" ) );
    assertTrue( files.contains( "data2.csv" ) );
  }

  @Test
  public void testShowFilesAction_RegexNoMatch() {
    setupBasicMocks();
    queryParams.put( "filter", ".*\\.json" );
    queryParams.put( "isRegex", "true" );

    when( textFileInputMeta.getFilePaths( any(), any() ) ).thenReturn( new String[] { "abc.txt" } );
    JSONObject result = helper.showFilesAction( transMeta, queryParams );
    JSONArray files = (JSONArray) result.get( "files" );
    assertTrue( files.isEmpty() );
  }

  @Test
  public void testShowContentAction_NoStepInTransMeta() throws Exception {
    when( transMeta.findStep( anyString() ) ).thenReturn( null );
    queryParams.put( "nrlines", "5" );
    queryParams.put( "skipHeaders", "true" );
    JSONObject result = helper.showContentAction( transMeta, queryParams );
    JSONArray firstFileContent = (JSONArray) result.get( "firstFileContent" );
    assertTrue( firstFileContent.isEmpty() );
  }

  @Test
  public void testHandleStepAction_UnknownMethod() {
    JSONObject result = helper.handleStepAction( "nonExistingMethod", transMeta, queryParams );
    assertEquals( BaseStepHelper.FAILURE_METHOD_NOT_FOUND_RESPONSE, result.get( BaseStepHelper.ACTION_STATUS ) );
  }

  @Test
  public void testHandleStepAction_FailureCatchBlock() {
    setupBasicMocks();
    JSONObject erroringResponse = helper.handleStepAction( null, transMeta, queryParams );
    assertEquals( BaseStepHelper.FAILURE_RESPONSE, erroringResponse.get( BaseStepHelper.ACTION_STATUS ) );
    assertNotNull( erroringResponse );
  }

  @Test
  public void testHandleStepAction_ShowContent() {
    setupBasicMocks();
    queryParams.put( "nrlines", "10" );
    queryParams.put( "skipHeaders", "false" );

    JSONObject result = helper.handleStepAction( "showContent", transMeta, queryParams );

    assertEquals( StepInterface.SUCCESS_RESPONSE, result.get( StepInterface.ACTION_STATUS ) );
    assertTrue( result.containsKey( "firstFileContent" ) );
  }

  @Test
  public void testHandleStepAction_SetMinimalWidth() {
    setupBasicMocks();
    when( textFileInputMeta.getInputFields() ).thenReturn( new BaseFileField[] { baseFileField } );
    when( baseFileField.getName() ).thenReturn( "testField" );
    when( baseFileField.getTypeDesc() ).thenReturn( "String" );
    when( baseFileField.getType() ).thenReturn( ValueMetaInterface.TYPE_STRING );
    when( baseFileField.getPosition() ).thenReturn( 1 );
    when( baseFileField.isRepeated() ).thenReturn( false );

    JSONObject result = helper.handleStepAction( "setMinimalWidth", transMeta, queryParams );

    assertEquals( StepInterface.SUCCESS_RESPONSE, result.get( StepInterface.ACTION_STATUS ) );
    assertTrue( result.containsKey( "updatedData" ) );
  }

  @Test
  public void testHandleStepAction_InvalidMethod() {
    JSONObject result = helper.handleStepAction( "invalidMethod", transMeta, queryParams );

    assertEquals( BaseStepHelper.FAILURE_METHOD_NOT_FOUND_RESPONSE, result.get( BaseStepHelper.ACTION_STATUS ) );
  }

  @Test
  public void testHandleStepAction_ExceptionHandling() {
    when( transMeta.findStep( anyString() ) ).thenThrow( new RuntimeException( "Test exception" ) );

    JSONObject result = helper.handleStepAction( "getFields", transMeta, queryParams );

    assertEquals( BaseStepHelper.FAILURE_RESPONSE, result.get( BaseStepHelper.ACTION_STATUS ) );
  }

  @Test
  public void testShowFilesAction_WithFilter() {
    setupBasicMocks();
    queryParams.put( "filter", "file1" );
    queryParams.put( "isRegex", "false" );
    when( textFileInputMeta.getFilePaths( any(), any() ) ).thenReturn( new String[] { "file1.txt", "file2.txt" } );

    JSONObject result = helper.showFilesAction( transMeta, queryParams );

    assertEquals( StepInterface.SUCCESS_RESPONSE, result.get( StepInterface.ACTION_STATUS ) );
    JSONArray files = (JSONArray) result.get( "files" );
    assertEquals( 1, files.size() );
    assertEquals( "file1.txt", files.get( 0 ) );
  }

  @Test
  public void testShowFilesAction_WithRegex() {
    setupBasicMocks();
    queryParams.put( "filter", ".*\\.txt" );
    queryParams.put( "isRegex", "true" );
    when( textFileInputMeta.getFilePaths( any(), any() ) ).thenReturn( new String[] { "file1.txt", "file2.doc" } );

    JSONObject result = helper.showFilesAction( transMeta, queryParams );

    JSONArray files = (JSONArray) result.get( "files" );
    assertEquals( 1, files.size() );
    assertEquals( "file1.txt", files.get( 0 ) );
  }

  @Test
  public void testShowFilesAction_EmptyStepName() {
    queryParams.put( "stepName", "" );

    JSONObject result = helper.showFilesAction( transMeta, queryParams );

    assertEquals( StepInterface.SUCCESS_RESPONSE, result.get( StepInterface.ACTION_STATUS ) );
  }

  @Test
  public void testShowFilesAction_NullStepName() {
    queryParams.remove( "stepName" );

    JSONObject result = helper.showFilesAction( transMeta, queryParams );

    assertEquals( StepInterface.SUCCESS_RESPONSE, result.get( StepInterface.ACTION_STATUS ) );
  }

  @Test
  public void testGetFieldsAction_EmptyStepName() throws Exception {
    queryParams.put( "stepName", "" );

    JSONObject result = helper.getFieldsAction( transMeta, queryParams );

    assertTrue( result.containsKey( "fields" ) );
    JSONArray fields = (JSONArray) result.get( "fields" );
    assertTrue( fields.isEmpty() );
  }

  @Test
  public void testGetFieldsAction_NullStepName() throws Exception {
    queryParams.remove( "stepName" );

    JSONObject result = helper.getFieldsAction( transMeta, queryParams );

    assertTrue( result.containsKey( "fields" ) );
    JSONArray fields = (JSONArray) result.get( "fields" );
    assertTrue( fields.isEmpty() );
  }

  @Test
  public void testShowFilesAction_NullStepMeta() {
    when( transMeta.findStep( "testStep" ) ).thenReturn( null );

    JSONObject result = helper.showFilesAction( transMeta, queryParams );

    // The action should still return success status
    assertEquals( StepInterface.SUCCESS_RESPONSE, result.get( StepInterface.ACTION_STATUS ) );

    if ( result.containsKey( "files" ) ) {
      assertTrue( ( (JSONArray) result.get( "files" ) ).isEmpty() );
    } else {
      assertNotNull( result );
    }
  }

  @Test
  public void testShowFilesAction_NoFiles() {
    setupBasicMocks();
    when( textFileInputMeta.getFilePaths( any(), any() ) ).thenReturn( new String[ 0 ] );
    JSONObject result = helper.showFilesAction( transMeta, queryParams );
    assertEquals( StepInterface.SUCCESS_RESPONSE, result.get( StepInterface.ACTION_STATUS ) );
    assertTrue( result.containsKey( "message" ) );
  }

  @Test
  public void testHandleStepAction_GetFields_ExceptionInPopulateMeta() {
    setupBasicMocks();
    when( textFileInputMeta.getFileTypeNr() ).thenReturn( TextFileInputMeta.FILE_TYPE_CSV );
    when( textFileInputMeta.getHeaderFileObject( transMeta ) ).thenThrow( new RuntimeException( "mock failure" ) );

    JSONObject result = helper.handleStepAction( "getFields", transMeta, queryParams );
    assertEquals( BaseStepHelper.FAILURE_RESPONSE, result.get( BaseStepHelper.ACTION_STATUS ) );
  }

  @Test
  public void testHandleStepAction_ShowContent_Exception() {
    setupBasicMocks();

    // Force exception by mocking getFileInputList to throw
    when( textFileInputMeta.getFileInputList( any(), any() ) ).thenThrow( new RuntimeException( "Bad file list" ) );

    queryParams.put( "nrlines", "10" );
    queryParams.put( "skipHeaders", "false" );

    JSONObject result = helper.handleStepAction( "showContent", transMeta, queryParams );

    assertEquals( BaseStepHelper.FAILURE_RESPONSE, result.get( BaseStepHelper.ACTION_STATUS ) );
  }

  @Test
  public void testGetFieldNamesAction_ErrorInProcessing() {
    setupBasicMocks();
    FileObject headerFile = mock( FileObject.class );
    when( textFileInputMeta.getHeaderFileObject( transMeta ) ).thenReturn( headerFile );

    try ( MockedStatic<KettleVFS> kettleVfs = mockStatic( KettleVFS.class ) ) {
      InputStream fakeStream = mock( InputStream.class );
      kettleVfs.when( () -> KettleVFS.getInputStream( headerFile ) ).thenReturn( fakeStream );

      // Throw during compression open
      try ( MockedStatic<CompressionProviderFactory> mockProviderFactory = mockStatic(
        CompressionProviderFactory.class ) ) {
        CompressionProviderFactory mockFactory = mock( CompressionProviderFactory.class );
        mockProviderFactory.when( CompressionProviderFactory::getInstance ).thenReturn( mockFactory );
        when( mockFactory.createCompressionProviderInstance( anyString() ) ).thenThrow(
          new RuntimeException( "Compression fail" ) );

        JSONObject result = helper.getFieldNamesAction( transMeta, queryParams );
        assertEquals( StepInterface.SUCCESS_RESPONSE, result.get( StepInterface.ACTION_STATUS ) );
        assertTrue( ( (JSONArray) result.get( "fieldNames" ) ).isEmpty() );
      }
    }
  }


  @Test
  public void testValidateShowContentAction_NoFilesCase() {
    setupBasicMocks();
    when( textFileInputMeta.getFileInputList( any(), any() ) ).thenReturn( fileInputList );
    when( fileInputList.nrOfFiles() ).thenReturn( 0 );

    JSONObject result = helper.validateShowContentAction( transMeta, queryParams );
    assertEquals( StepInterface.SUCCESS_RESPONSE, result.get( StepInterface.ACTION_STATUS ) );
    assertTrue( result.containsKey( "message" ) );
  }

  @Test
  public void testValidateShowContentAction_NoStepFound() {
    when( transMeta.findStep( "testStep" ) ).thenReturn( null );
    JSONObject result = helper.validateShowContentAction( transMeta, queryParams );
    assertEquals( StepInterface.SUCCESS_RESPONSE, result.get( StepInterface.ACTION_STATUS ) );
  }

  @Test
  public void testHandleStepAction_SetMinimalWidth_ExceptionHandled() {
    setupBasicMocks();
    when( textFileInputMeta.getInputFields() ).thenThrow( new RuntimeException( "bad field access" ) );
    JSONObject result = helper.handleStepAction( "setMinimalWidth", transMeta, queryParams );

    assertEquals( BaseStepHelper.FAILURE_RESPONSE, result.get( BaseStepHelper.ACTION_STATUS ) );
  }

  @Test
  public void testSetMinimalWidthAction_HandlesJsonProcessingException() throws Exception {
    setupBasicMocks();
    when( textFileInputMeta.getInputFields() ).thenReturn( new BaseFileField[] { baseFileField } );
    when( baseFileField.getName() ).thenReturn( "badField" );
    when( baseFileField.getTypeDesc() ).thenReturn( "WeirdType" );
    when( baseFileField.getType() ).thenReturn( ValueMetaInterface.TYPE_STRING );
    when( baseFileField.isRepeated() ).thenReturn( false );
    when( baseFileField.getPosition() ).thenReturn( 0 );

    JSONObject result = helper.setMinimalWidthAction( transMeta, queryParams );
    assertEquals( StepInterface.SUCCESS_RESPONSE, result.get( StepInterface.ACTION_STATUS ) );
    assertTrue( result.containsKey( "updatedData" ) );
  }

  @Test
  public void testGetFieldsAction_InvalidSampleValue() {
    setupBasicMocks();
    when( textFileInputMeta.getFileTypeNr() ).thenReturn( TextFileInputMeta.FILE_TYPE_CSV );
    FileObject headerFile = mock( FileObject.class );
    when( textFileInputMeta.getHeaderFileObject( transMeta ) ).thenReturn( headerFile );
    queryParams.put( "noOfFields", "nonNumeric" );

    JSONObject result = helper.handleStepAction( "getFields", transMeta, queryParams );
    assertTrue( result.containsKey( StepInterface.ACTION_STATUS ) );
  }


  @Test
  public void testGetFieldNamesAction_HeaderFileNull() {
    setupBasicMocks();
    when( textFileInputMeta.getHeaderFileObject( any() ) ).thenReturn( null );
    JSONObject result = helper.getFieldNamesAction( transMeta, queryParams );
    assertEquals( StepInterface.SUCCESS_RESPONSE, result.get( StepInterface.ACTION_STATUS ) );
    assertTrue( result.containsKey( "fieldNames" ) );
    assertTrue( ( (JSONArray) result.get( "fieldNames" ) ).isEmpty() );
  }

  @Test
  public void testSetMinimalWidthAction_NullStepMeta() throws Exception {
    queryParams.put( "stepName", "unknownStep" );
    when( transMeta.findStep( "unknownStep" ) ).thenReturn( null );
    JSONObject result = helper.setMinimalWidthAction( transMeta, queryParams );
    assertEquals( StepInterface.SUCCESS_RESPONSE, result.get( StepInterface.ACTION_STATUS ) );
    assertTrue( ( (JSONArray) result.get( "updatedData" ) ).isEmpty() );
  }

  @Test
  public void testGetUpdatedTextFields_EmptyFields() throws Exception {
    setupBasicMocks();
    when( textFileInputMeta.getInputFields() ).thenReturn( new BaseFileField[ 0 ] );
    JSONObject result = helper.setMinimalWidthAction( transMeta, queryParams );
    assertEquals( StepInterface.SUCCESS_RESPONSE, result.get( StepInterface.ACTION_STATUS ) );
    JSONArray updatedData = (JSONArray) result.get( "updatedData" );
    assertTrue( updatedData.isEmpty() );
  }

  @Test
  public void testShowContentAction_NoFiles() throws Exception {
    setupBasicMocks();
    when( textFileInputMeta.getFileInputList( any(), any() ) ).thenReturn( fileInputList );
    when( fileInputList.nrOfFiles() ).thenReturn( 0 );
    queryParams.put( "nrlines", "10" );
    JSONObject result = helper.showContentAction( transMeta, queryParams );
    assertEquals( StepInterface.SUCCESS_RESPONSE, result.get( StepInterface.ACTION_STATUS ) );
    JSONArray content = (JSONArray) result.get( "firstFileContent" );
    assertTrue( content.isEmpty() );
  }

  @Test
  public void testGetFieldsAction_WrongStepType() throws Exception {
    when( transMeta.findStep( "testStep" ) ).thenReturn( stepMeta );
    when( stepMeta.getStepMetaInterface() ).thenReturn( mock( org.pentaho.di.trans.step.StepMetaInterface.class ) );

    JSONObject result = helper.getFieldsAction( transMeta, queryParams );

    assertTrue( result.containsKey( "fields" ) );
    JSONArray fields = (JSONArray) result.get( "fields" );
    assertTrue( fields.isEmpty() );
  }


  @Test
  public void testGetFieldNamesAction_NoHeader() {
    setupBasicMocks();
    when( textFileInputMeta.getHeaderFileObject( transMeta ) ).thenReturn( null );

    JSONObject result = helper.getFieldNamesAction( transMeta, queryParams );

    assertEquals( StepInterface.SUCCESS_RESPONSE, result.get( StepInterface.ACTION_STATUS ) );
    JSONArray fieldNames = (JSONArray) result.get( "fieldNames" );
    assertTrue( fieldNames.isEmpty() );
  }


  @Test
  public void testShowContentAction_EmptyStepName() throws Exception {
    queryParams.put( "stepName", "" );
    queryParams.put( "nrlines", "10" );
    queryParams.put( "skipHeaders", "false" );

    JSONObject result = helper.showContentAction( transMeta, queryParams );

    assertEquals( StepInterface.SUCCESS_RESPONSE, result.get( StepInterface.ACTION_STATUS ) );
    JSONArray content = (JSONArray) result.get( "firstFileContent" );
    assertTrue( content.isEmpty() );
  }

  @Test
  public void testSetMinimalWidthAction_EmptyStepName() throws Exception {
    queryParams.put( "stepName", "" );

    JSONObject result = helper.setMinimalWidthAction( transMeta, queryParams );

    assertEquals( StepInterface.SUCCESS_RESPONSE, result.get( StepInterface.ACTION_STATUS ) );
    JSONArray updatedData = (JSONArray) result.get( "updatedData" );
    assertTrue( updatedData.isEmpty() );
  }

  @Test
  public void testGetUpdatedTextFields_IntegerType() throws Exception {
    setupBasicMocks();
    when( textFileInputMeta.getInputFields() ).thenReturn( new BaseFileField[] { baseFileField } );
    when( baseFileField.getName() ).thenReturn( "intField" );
    when( baseFileField.getTypeDesc() ).thenReturn( "Integer" );
    when( baseFileField.getType() ).thenReturn( ValueMetaInterface.TYPE_INTEGER );
    when( baseFileField.getPosition() ).thenReturn( -1 );
    when( baseFileField.isRepeated() ).thenReturn( true );

    JSONObject result = helper.setMinimalWidthAction( transMeta, queryParams );

    assertEquals( StepInterface.SUCCESS_RESPONSE, result.get( StepInterface.ACTION_STATUS ) );
  }

  @Test
  public void testGetUpdatedTextFields_NumberType() throws Exception {
    setupBasicMocks();
    when( textFileInputMeta.getInputFields() ).thenReturn( new BaseFileField[] { baseFileField } );
    when( baseFileField.getName() ).thenReturn( "numberField" );
    when( baseFileField.getTypeDesc() ).thenReturn( "Number" );
    when( baseFileField.getType() ).thenReturn( ValueMetaInterface.TYPE_NUMBER );
    when( baseFileField.getPosition() ).thenReturn( 5 );
    when( baseFileField.isRepeated() ).thenReturn( false );

    JSONObject result = helper.setMinimalWidthAction( transMeta, queryParams );

    assertEquals( StepInterface.SUCCESS_RESPONSE, result.get( StepInterface.ACTION_STATUS ) );
  }

  @Test
  public void testGetUpdatedTextFields_DefaultType() throws Exception {
    setupBasicMocks();
    when( textFileInputMeta.getInputFields() ).thenReturn( new BaseFileField[] { baseFileField } );
    when( baseFileField.getName() ).thenReturn( "otherField" );
    when( baseFileField.getTypeDesc() ).thenReturn( "Date" );
    when( baseFileField.getType() ).thenReturn( ValueMetaInterface.TYPE_DATE );
    when( baseFileField.getPosition() ).thenReturn( 1 );
    when( baseFileField.isRepeated() ).thenReturn( false );

    JSONObject result = helper.setMinimalWidthAction( transMeta, queryParams );

    assertEquals( StepInterface.SUCCESS_RESPONSE, result.get( StepInterface.ACTION_STATUS ) );
  }

  @Test
  public void testReadFirst_NoFiles() throws Exception {
    setupBasicMocks();
    when( textFileInputMeta.getFileInputList( any(), any() ) ).thenReturn( fileInputList );
    when( fileInputList.nrOfFiles() ).thenReturn( 0 );
    queryParams.put( "nrlines", "10" );

    JSONObject result = helper.showContentAction( transMeta, queryParams );

    JSONArray content = (JSONArray) result.get( "firstFileContent" );
    assertTrue( content.isEmpty() );
  }

  @Test
  public void testLogChannel() {
    assertNotNull( helper.logChannel() );
  }

  @Test
  public void testGetLogChannel() {
    assertNotNull( helper.getLogChannel() );
  }

  @Test
  public void testGetFieldsAction_CsvHeaderMissing() throws Exception {
    setupBasicMocks();
    when( textFileInputMeta.getFileTypeNr() ).thenReturn( TextFileInputMeta.FILE_TYPE_CSV );

    org.apache.commons.vfs2.FileObject headerFile = mock( org.apache.commons.vfs2.FileObject.class );
    when( textFileInputMeta.getHeaderFileObject( transMeta ) ).thenReturn( headerFile );

    try ( MockedStatic<KettleVFS> kettleVfs = mockStatic( KettleVFS.class ) ) {
      // Simulate null input stream for header file to trigger the error path in populateMeta
      kettleVfs.when( () -> KettleVFS.getInputStream( headerFile ) ).thenReturn( null );

      JSONObject result = helper.getFieldsAction( transMeta, queryParams );

      assertEquals( StepInterface.FAILURE_RESPONSE, result.get( StepInterface.ACTION_STATUS ) );
      assertTrue( result.containsKey( "message" ) );
    }
  }

  @Test( expected = KettleException.class )
  public void testShowContentAction_GetFirstThrows() throws Exception {
    setupBasicMocks();
    when( textFileInputMeta.getFileInputList( any(), any() ) ).thenReturn( fileInputList );
    when( fileInputList.nrOfFiles() ).thenReturn( 1 );

    FileObject file = mock( FileObject.class );
    FileName fileName = mock( FileName.class );
    when( file.getName() ).thenReturn( fileName );
    when( fileName.getURI() ).thenReturn( "mock:///testfile.txt" );
    when( fileInputList.getFile( 0 ) ).thenReturn( file );

    try ( MockedStatic<KettleVFS> kettleVfs = mockStatic( KettleVFS.class ) ) {
      // Simulate an exception when attempting to open the stream to trigger KettleException in getFirst
      kettleVfs.when( () -> KettleVFS.getInputStream( file ) ).thenThrow( new RuntimeException( "io error" ) );

      queryParams.put( "nrlines", "10" );
      queryParams.put( "skipHeaders", "false" );

      // This call should result in a KettleException being thrown because of stream error
      helper.showContentAction( transMeta, queryParams );
    }
  }

  @Test
  public void testHandleStepAction_SetMinimalWidth_EmptyStepName() {
    queryParams.put( "stepName", "" );
    JSONObject result = helper.handleStepAction( "setMinimalWidth", transMeta, queryParams );
    assertEquals( StepInterface.SUCCESS_RESPONSE, result.get( StepInterface.ACTION_STATUS ) );
    assertTrue( ( (JSONArray) result.get( "updatedData" ) ).isEmpty() );
  }

  @Test
  public void testHandleStepAction_SetMinimalWidth_WithFields() {
    setupBasicMocks();
    when( textFileInputMeta.getInputFields() ).thenReturn( new BaseFileField[] { baseFileField } );
    when( baseFileField.getName() ).thenReturn( "field1" );
    when( baseFileField.getTypeDesc() ).thenReturn( "String" );
    when( baseFileField.getType() ).thenReturn( ValueMetaInterface.TYPE_STRING );
    when( baseFileField.getPosition() ).thenReturn( 0 );
    when( baseFileField.isRepeated() ).thenReturn( false );

    JSONObject result = helper.handleStepAction( "setMinimalWidth", transMeta, queryParams );

    assertEquals( StepInterface.SUCCESS_RESPONSE, result.get( StepInterface.ACTION_STATUS ) );
    assertTrue( result.containsKey( "updatedData" ) );
    JSONArray updatedData = (JSONArray) result.get( "updatedData" );
    assertEquals( 1, updatedData.size() );
  }

  @Test
  public void testGetFieldNamesAction_IOException() {
    setupBasicMocks();
    FileObject headerFile = mock( FileObject.class );
    when( textFileInputMeta.getHeaderFileObject( transMeta ) ).thenReturn( headerFile );

    try ( MockedStatic<KettleVFS> kettleVfs = mockStatic( KettleVFS.class ) ) {
      kettleVfs.when( () -> KettleVFS.getInputStream( headerFile ) ).thenThrow( new RuntimeException( "io error" ) );

      JSONObject result = helper.getFieldNamesAction( transMeta, queryParams );

      assertEquals( StepInterface.SUCCESS_RESPONSE, result.get( StepInterface.ACTION_STATUS ) );
      assertTrue( result.containsKey( "fieldNames" ) );
      JSONArray fieldNames = (JSONArray) result.get( "fieldNames" );
      assertTrue( fieldNames.isEmpty() );
    }
  }

  @Test
  public void testShowContentAction_EmptyStep() throws Exception {
    JSONObject result = helper.showContentAction( transMeta, queryParams );
    assertEquals( StepInterface.SUCCESS_RESPONSE, result.get( StepInterface.ACTION_STATUS ) );
    JSONArray content = (JSONArray) result.get( "firstFileContent" );
    assertNotNull( content );
    assertTrue( content.isEmpty() );
  }

  @Test
  public void testGetFieldsAction_Csv_WrongStepType() throws Exception {
    when( transMeta.findStep( "testStep" ) ).thenReturn( stepMeta );
    when( stepMeta.getStepMetaInterface() ).thenReturn( mock( StepMetaInterface.class ) );

    JSONObject result = helper.getFieldsAction( transMeta, queryParams );

    assertTrue( result.containsKey( "fields" ) );
    JSONArray fields = (JSONArray) result.get( "fields" );
    assertTrue( fields.isEmpty() );
  }


  private void setupBasicMocks() {
    when( transMeta.findStep( "testStep" ) ).thenReturn( stepMeta );
    when( stepMeta.getStepMetaInterface() ).thenReturn( textFileInputMeta );
  }

}
