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

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.commons.vfs2.FileContent;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.pentaho.di.core.compress.CompressionInputStream;
import org.pentaho.di.core.compress.CompressionProvider;
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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
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

  @SuppressWarnings( "java:S00112" )
  @Test
  public void testGetFieldsAction_EmptyStepName() throws KettleException {
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
  public void testHandleStepAction_GetFields_CsvHeaderEmptyStream() {
    setupBasicMocks();
    when( textFileInputMeta.getFileTypeNr() ).thenReturn( TextFileInputMeta.FILE_TYPE_CSV );
    FileObject mockFile = mock( FileObject.class );
    when( textFileInputMeta.getHeaderFileObject( transMeta ) ).thenReturn( mockFile );

    try ( MockedStatic<KettleVFS> kettleVfs = mockStatic( KettleVFS.class ) ) {
      kettleVfs.when( () -> KettleVFS.getInputStream( mockFile ) ).thenReturn( InputStream.nullInputStream() );
      JSONObject result = helper.handleStepAction( "getFields", transMeta, queryParams );
      assertNotNull( result );
      assertTrue( result.containsKey( StepInterface.ACTION_STATUS ) );
    }
  }

  @Test
  public void testGetFieldNamesAction_CompressionErrorPath() {
    setupBasicMocks();
    FileObject fileObject = mock( FileObject.class );
    when( textFileInputMeta.getHeaderFileObject( transMeta ) ).thenReturn( fileObject );

    try ( MockedStatic<KettleVFS> kettleVfs = mockStatic( KettleVFS.class );
          MockedStatic<CompressionProviderFactory> providerFactory = mockStatic( CompressionProviderFactory.class ) ) {
      InputStream inputStream = mock( InputStream.class );
      kettleVfs.when( () -> KettleVFS.getInputStream( fileObject ) ).thenReturn( inputStream );
      CompressionProviderFactory mockFactory = mock( CompressionProviderFactory.class );
      providerFactory.when( CompressionProviderFactory::getInstance ).thenReturn( mockFactory );
      when( mockFactory.createCompressionProviderInstance( anyString() ) )
        .thenThrow( new RuntimeException( "Compression error" ) );

      JSONObject result = helper.getFieldNamesAction( transMeta, queryParams );
      assertEquals( StepInterface.SUCCESS_RESPONSE, result.get( StepInterface.ACTION_STATUS ) );
      assertTrue( ( (JSONArray) result.get( "fieldNames" ) ).isEmpty() );
    }
  }

  @Test
  public void testShowFilesAction_StepNameVariants() {
    setupBasicMocks();
    String[] stepNames = { "", null, "testStep" };
    for ( String name : stepNames ) {
      queryParams.put( "stepName", name );
      when( textFileInputMeta.getFilePaths( any(), any() ) ).thenReturn( new String[ 0 ] );
      JSONObject result = helper.showFilesAction( transMeta, queryParams );
      assertEquals( StepInterface.SUCCESS_RESPONSE, result.get( StepInterface.ACTION_STATUS ) );
    }
  }

  @Test
  public void testSetMinimalWidthAction_MixedFields() throws JsonProcessingException {
    setupBasicMocks();

    BaseFileField field1 = mock( BaseFileField.class );
    BaseFileField field2 = mock( BaseFileField.class );
    when( textFileInputMeta.getInputFields() ).thenReturn( new BaseFileField[] { field1, field2 } );

    when( field1.getName() ).thenReturn( "dateField" );
    when( field1.getTypeDesc() ).thenReturn( "Date" );
    when( field1.getType() ).thenReturn( ValueMetaInterface.TYPE_DATE );
    when( field1.getPosition() ).thenReturn( 3 );
    when( field1.isRepeated() ).thenReturn( false );

    when( field2.getName() ).thenReturn( "numField" );
    when( field2.getTypeDesc() ).thenReturn( "Number" );
    when( field2.getType() ).thenReturn( ValueMetaInterface.TYPE_NUMBER );
    when( field2.getPosition() ).thenReturn( -1 );
    when( field2.isRepeated() ).thenReturn( true );

    JSONObject result = helper.setMinimalWidthAction( transMeta, queryParams );

    assertNotNull( result );
    assertEquals( StepInterface.SUCCESS_RESPONSE, result.get( StepInterface.ACTION_STATUS ) );

    JSONArray updated = (JSONArray) result.get( "updatedData" );
    assertNotNull( updated );
    assertTrue( updated.size() >= 1 );
  }


  @Test
  public void testShowContentAction_FileListThrows() {
    setupBasicMocks();
    when( textFileInputMeta.getFileInputList( any(), any() ) )
      .thenThrow( new RuntimeException( "I/O error" ) );
    queryParams.put( "nrlines", "10" );

    try {
      helper.showContentAction( transMeta, queryParams );
      fail( "Expected RuntimeException to propagate" );
    } catch ( RuntimeException | KettleException e ) {
      assertEquals( "I/O error", e.getMessage() );
    }
  }

  @Test
  public void testHandleStepAction_UnknownActionName() {
    JSONObject result = helper.handleStepAction( "nonExistentAction", transMeta, queryParams );
    assertEquals( BaseStepHelper.FAILURE_METHOD_NOT_FOUND_RESPONSE, result.get( BaseStepHelper.ACTION_STATUS ) );
  }

  @Test
  public void testGetFieldsAction_NotTextFileInputMeta() throws Exception {
    when( transMeta.findStep( "testStep" ) ).thenReturn( stepMeta );
    when( stepMeta.getStepMetaInterface() ).thenReturn( mock( StepMetaInterface.class ) );
    JSONObject result = helper.getFieldsAction( transMeta, queryParams );
    assertTrue( ( (JSONArray) result.get( "fields" ) ).isEmpty() );
  }

  @Test
  public void testLogMethods() {
    assertNotNull( helper.logChannel() );
    assertNotNull( helper.getLogChannel() );
  }

  @Test
  public void testPopulateMeta_SuccessPath() throws Exception {
    setupBasicMocks();

    FileObject fileObject = mock( FileObject.class );
    FileContent mockContent = mock( FileContent.class );
    InputStream mockStream = mock( InputStream.class );

    when( fileObject.getContent() ).thenReturn( mockContent );
    when( mockContent.getInputStream() ).thenReturn( mockStream );
    when( textFileInputMeta.getHeaderFileObject( transMeta ) ).thenReturn( fileObject );
    when( KettleVFS.getInputStream( fileObject ) ).thenReturn( mockStream );

    CompressionProvider mockProvider = mock( CompressionProvider.class );
    CompressionInputStream mockComp = mock( CompressionInputStream.class, RETURNS_DEEP_STUBS );
    try ( MockedStatic<CompressionProviderFactory> factoryMock = mockStatic( CompressionProviderFactory.class );
          MockedConstruction<TextFileCsvFileTypeImportProcessor> processorMock =
            mockConstruction( TextFileCsvFileTypeImportProcessor.class, ( mock, context ) -> {
              when( mock.analyzeFile( anyBoolean() ) ).thenReturn( "summary" );
              TextFileInputFieldDTO dto = new TextFileInputFieldDTO();
              dto.setName( "col1" );
              dto.setType( "String" );
              when( mock.getInputFieldsDto() ).thenReturn( new TextFileInputFieldDTO[] { dto } );
            } ) ) {

      CompressionProviderFactory mockFactory = mock( CompressionProviderFactory.class );
      factoryMock.when( CompressionProviderFactory::getInstance ).thenReturn( mockFactory );
      when( mockFactory.createCompressionProviderInstance( anyString() ) ).thenReturn( mockProvider );
      when( mockProvider.createInputStream( any( InputStream.class ) ) ).thenReturn( mockComp );

      Map<String, String> params = new HashMap<>();
      params.put( "isSampleSummary", "false" );
      params.put( "noOfFields", "1" );

      TextFileInputHelper helperSpy = new TextFileInputHelper();

      JSONObject result = helperSpy.handleStepAction( "getFields", transMeta, params );

      assertNotNull( "Result should not be null", result );
      assertTrue( "Result should contain action status or fields",
        result.containsKey( StepInterface.ACTION_STATUS )
          || result.containsKey( "updatedData" )
          || result.containsKey( "fields" ) );
    }
  }

  @Test
  public void testCreateReaderAndReadFirstLine() throws Exception {
    setupBasicMocks();

    CompressionInputStream mockComp = mock( CompressionInputStream.class, RETURNS_DEEP_STUBS );
    when( textFileInputMeta.getEncoding() ).thenReturn( "UTF-8" );

    var createReader = TextFileInputHelper.class
      .getDeclaredMethod( "createReader", TextFileInputMeta.class, CompressionInputStream.class );
    createReader.setAccessible( true );
    Object reader = createReader.invoke( helper, textFileInputMeta, mockComp );
    assertNotNull( reader );

    BufferedInputStreamReader mockReader = mock( BufferedInputStreamReader.class );
    when( mockReader.getEncoding() ).thenReturn( "UTF-8" );

    try ( MockedStatic<TextFileInputUtils> utilsMock = mockStatic( TextFileInputUtils.class ) ) {
      utilsMock.when( () -> TextFileInputUtils.getLine( any(), any(), any(), anyInt(), any(), any(), any() ) )
        .thenReturn( "abc" );
      var readFirstLine = TextFileInputHelper.class.getDeclaredMethod(
        "readFirstLine", TransMeta.class, TextFileInputMeta.class, BufferedInputStreamReader.class );
      readFirstLine.setAccessible( true );
      Object line = readFirstLine.invoke( helper, transMeta, textFileInputMeta, mockReader );
      assertEquals( "abc", line );
    }
  }

  @Test
  public void testParseFieldNames_BlankAndValid() throws Exception {
    setupBasicMocks();

    Method method = TextFileInputHelper.class.getDeclaredMethod(
      "parseFieldNames", TransMeta.class, TextFileInputMeta.class, String.class
    );
    method.setAccessible( true );

    String[] resultEmpty = (String[]) method.invoke( helper, transMeta, textFileInputMeta, "  " );
    assertEquals( 0, resultEmpty.length );

    try ( MockedStatic<TextFileInputUtils> mockUtils = mockStatic( TextFileInputUtils.class ) ) {
      mockUtils.when( () -> TextFileInputUtils.guessStringsFromLine(
        any(), any(), any(), any(), any(), any(), any()
      ) ).thenReturn( new String[] { "a", "b" } );

      String[] result = (String[]) method.invoke( helper, transMeta, textFileInputMeta, "abc" );
      assertEquals( 2, result.length );
      assertArrayEquals( new String[] { "a", "b" }, result );
    }
  }

  @Test
  public void testGetFirst_ExceptionPath() throws Exception {
    setupBasicMocks();
    TextFileInputMeta.Content content = new TextFileInputMeta.Content();
    content.fileCompression = "None";
    content.header = false;
    content.fileType = "FIXED";
    textFileInputMeta.content = content;
    when( textFileInputMeta.getEncoding() ).thenReturn( "UTF-8" );
    when( textFileInputMeta.getEnclosure() ).thenReturn( "\"" );
    when( textFileInputMeta.getEscapeCharacter() ).thenReturn( "\\" );

    FileInputList fileInputListLocal = mock( FileInputList.class );
    FileObject mockFile = mock( FileObject.class );
    FileName mockFileName = mock( FileName.class );
    when( fileInputListLocal.nrOfFiles() ).thenReturn( 1 );
    when( fileInputListLocal.getFile( 0 ) ).thenReturn( mockFile );
    when( mockFile.getName() ).thenReturn( mockFileName );
    when( mockFileName.getURI() ).thenReturn( "mock:///demo.txt" );
    when( textFileInputMeta.getFileInputList( any(), any() ) ).thenReturn( fileInputListLocal );

    try (
      MockedStatic<KettleVFS> vfsMock = mockStatic( KettleVFS.class )
    ) {
      vfsMock.when( () -> KettleVFS.getInputStream( mockFile ) ).thenThrow( new RuntimeException( "mock io error" ) );

      Method getFirstM = TextFileInputHelper.class.getDeclaredMethod(
        "getFirst", TextFileInputMeta.class, TransMeta.class, int.class, boolean.class );
      getFirstM.setAccessible( true );

      try {
        getFirstM.invoke( helper, textFileInputMeta, transMeta, 1, false );
        fail( "Expected KettleException to be thrown" );
      } catch ( InvocationTargetException ite ) {
        Throwable cause = ite.getCause();
        assertTrue( cause instanceof KettleException );
        assertTrue( cause.getMessage().contains( "Error getting first" ) );
        assertTrue( cause.getMessage().contains( "mock:///demo.txt" ) );
        assertNotNull( cause.getCause() );
        assertEquals( "mock io error", cause.getCause().getMessage() );
      }
    }
  }

  @Test
  public void testGetFieldNamesAction_Success() throws Exception {
    setupBasicMocks();
    queryParams.put( "stepName", "testStep" );
    FileObject fileObject = mock( FileObject.class );

    TextFileInputMeta.Content content = new TextFileInputMeta.Content();
    content.fileCompression = "None";
    content.fileType = "CSV";
    textFileInputMeta.content = content;
    when( textFileInputMeta.getEncoding() ).thenReturn( "UTF-8" );
    when( textFileInputMeta.getHeaderFileObject( transMeta ) ).thenReturn( fileObject );

    when( transMeta.findStep( "testStep" ) ).thenReturn( stepMeta );
    when( stepMeta.getStepMetaInterface() ).thenReturn( textFileInputMeta );

    InputStream fakeStream = new java.io.ByteArrayInputStream( "a,b".getBytes() );
    try (
      MockedStatic<KettleVFS> vfsMock = mockStatic( KettleVFS.class );
      MockedStatic<CompressionProviderFactory> provFactory = mockStatic( CompressionProviderFactory.class );
      MockedStatic<TextFileInputUtils> utilsMock = mockStatic( TextFileInputUtils.class )
    ) {
      vfsMock.when( () -> KettleVFS.getInputStream( fileObject ) ).thenReturn( fakeStream );

      CompressionProviderFactory factory = mock( CompressionProviderFactory.class );
      CompressionProvider provider = mock( CompressionProvider.class );
      CompressionInputStream cstream = mock( CompressionInputStream.class );
      provFactory.when( CompressionProviderFactory::getInstance ).thenReturn( factory );
      when( factory.createCompressionProviderInstance( anyString() ) ).thenReturn( provider );
      when( provider.createInputStream( any( InputStream.class ) ) ).thenReturn( cstream );

      utilsMock.when( () -> TextFileInputUtils.getLine( any(), any(), any(), anyInt(), any(), any(), any() ) )
        .thenReturn( "col1,col2" );
      utilsMock.when( () -> TextFileInputUtils.guessStringsFromLine( any(), any(), any(), any(), any(), any(), any() ) )
        .thenReturn( new String[] { "col1", "col2" } );

      JSONObject result = helper.getFieldNamesAction( transMeta, queryParams );
      JSONArray fields = (JSONArray) result.get( "fieldNames" );
      assertEquals( 2, fields.size() );
      assertTrue( fields.contains( "col1" ) );
      assertTrue( fields.contains( "col2" ) );
      assertEquals( StepInterface.SUCCESS_RESPONSE, result.get( StepInterface.ACTION_STATUS ) );
    }
  }

  @Test
  public void testGetFirst_SkipsHeaderAndDocHeaderLines() throws Exception {
    setupBasicMocks();
    TextFileInputMeta.Content content = new TextFileInputMeta.Content();
    content.fileCompression = "None";
    content.header = true;
    content.nrHeaderLines = 3;
    content.layoutPaged = true;
    content.nrLinesDocHeader = 2;
    content.fileType = "FIXED";
    textFileInputMeta.content = content;
    when( textFileInputMeta.getEncoding() ).thenReturn( "UTF-8" );
    when( textFileInputMeta.getEnclosure() ).thenReturn( "\"" );
    when( textFileInputMeta.getEscapeCharacter() ).thenReturn( "\\" );

    FileInputList fileInputListLocal = mock( FileInputList.class );
    FileObject mockFile = mock( FileObject.class );
    FileName mockFileName = mock( FileName.class );
    when( fileInputListLocal.nrOfFiles() ).thenReturn( 1 );
    when( fileInputListLocal.getFile( 0 ) ).thenReturn( mockFile );
    when( mockFile.getName() ).thenReturn( mockFileName );
    when( mockFileName.getURI() ).thenReturn( "mock:///doc.csv" );
    when( textFileInputMeta.getFileInputList( any(), any() ) ).thenReturn( fileInputListLocal );

    try (
      MockedStatic<KettleVFS> vfsMock = mockStatic( KettleVFS.class );
      MockedStatic<CompressionProviderFactory> provFactory = mockStatic( CompressionProviderFactory.class );
      MockedStatic<TextFileInputUtils> utilsMock = mockStatic( TextFileInputUtils.class )
    ) {
      InputStream fakeStream = new java.io.ByteArrayInputStream( "dummy".getBytes() );
      vfsMock.when( () -> KettleVFS.getInputStream( mockFile ) ).thenReturn( fakeStream );

      CompressionProviderFactory factory = mock( CompressionProviderFactory.class );
      CompressionProvider provider = mock( CompressionProvider.class );
      CompressionInputStream cstream = mock( CompressionInputStream.class );
      provFactory.when( CompressionProviderFactory::getInstance ).thenReturn( factory );
      when( factory.createCompressionProviderInstance( anyString() ) ).thenReturn( provider );
      when( provider.createInputStream( any( InputStream.class ) ) ).thenReturn( cstream );

      utilsMock.when(
          () -> TextFileInputUtils.skipLines( any(), any(), any(), anyInt(), any(), anyInt(), any(), any(),
            anyLong() ) )
        .then( invocation -> null );

      utilsMock.when( () -> TextFileInputUtils.getLine( any(), any(), any(), anyInt(), any(), any(), any() ) )
        .thenReturn( "the-line" )
        .thenReturn( null );

      Method getFirstM = TextFileInputHelper.class.getDeclaredMethod(
        "getFirst", TextFileInputMeta.class, TransMeta.class, int.class, boolean.class );
      getFirstM.setAccessible( true );

      @SuppressWarnings( "unchecked" )
      List<String> result = (List<String>) getFirstM.invoke( helper, textFileInputMeta, transMeta, 1, true );

      assertNotNull( result );
      assertEquals( 1, result.size() );
      assertEquals( "the-line", result.get( 0 ) );

      utilsMock.verify( () -> TextFileInputUtils.skipLines(
        any(), any(), any(), anyInt(), any(), eq( content.nrLinesDocHeader - 1 ), any(), any(), anyLong() ) );
      utilsMock.verify( () -> TextFileInputUtils.skipLines(
        any(), any(), any(), anyInt(), any(), eq( content.nrHeaderLines - 1 ), any(), any(), anyLong() ) );
    }
  }


  @Test
  public void testGetFirst_SuccessPath() throws Exception {
    setupBasicMocks();
    TextFileInputMeta.Content content = new TextFileInputMeta.Content();
    content.fileCompression = "None";
    content.header = false;
    content.fileType = "FIXED";
    textFileInputMeta.content = content;
    when( textFileInputMeta.getEncoding() ).thenReturn( "UTF-8" );
    when( textFileInputMeta.getEnclosure() ).thenReturn( "\"" );
    when( textFileInputMeta.getEscapeCharacter() ).thenReturn( "\\" );

    FileInputList fileInputListLocal = mock( FileInputList.class );
    FileObject mockFile = mock( FileObject.class );
    FileName mockFileName = mock( FileName.class );

    when( fileInputListLocal.nrOfFiles() ).thenReturn( 1 );
    when( fileInputListLocal.getFile( 0 ) ).thenReturn( mockFile );
    when( mockFile.getName() ).thenReturn( mockFileName );
    when( mockFileName.getURI() ).thenReturn( "mock:///demo.txt" );
    when( textFileInputMeta.getFileInputList( any(), any() ) ).thenReturn( fileInputListLocal );

    try (
      MockedStatic<KettleVFS> vfsMock = mockStatic( KettleVFS.class );
      MockedStatic<CompressionProviderFactory> provFactory = mockStatic( CompressionProviderFactory.class );
      MockedStatic<TextFileInputUtils> inputUtilsMock = mockStatic( TextFileInputUtils.class )
    ) {
      InputStream fakeStream = new java.io.ByteArrayInputStream( "123".getBytes() );
      vfsMock.when( () -> KettleVFS.getInputStream( mockFile ) ).thenReturn( fakeStream );

      CompressionProviderFactory factory = mock( CompressionProviderFactory.class );
      CompressionProvider provider = mock( CompressionProvider.class );
      CompressionInputStream cstream = mock( CompressionInputStream.class );
      provFactory.when( CompressionProviderFactory::getInstance ).thenReturn( factory );
      when( factory.createCompressionProviderInstance( anyString() ) ).thenReturn( provider );
      when( provider.createInputStream( any( InputStream.class ) ) ).thenReturn( cstream );

      inputUtilsMock.when( () -> TextFileInputUtils.getLine( any(), any(), any(), anyInt(), any(), any(), any() ) )
        .thenReturn( "row-1" )
        .thenReturn( null );

      Method getFirstM = TextFileInputHelper.class.getDeclaredMethod(
        "getFirst", TextFileInputMeta.class, TransMeta.class, int.class, boolean.class );
      getFirstM.setAccessible( true );

      List<String> result = (List<String>) getFirstM.invoke( helper, textFileInputMeta, transMeta, 1, false );

      assertNotNull( result );
      assertEquals( 1, result.size() );
      assertEquals( "row-1", result.get( 0 ) );
    }
  }

  @Test
  public void testPopulateMeta_Path() throws Exception {
    setupBasicMocks();
    FileObject fileObject = mock( FileObject.class );
    when( textFileInputMeta.getHeaderFileObject( transMeta ) ).thenReturn( fileObject );

    TextFileInputMeta.Content content = new TextFileInputMeta.Content();
    content.fileCompression = "None";
    textFileInputMeta.content = content;
    when( textFileInputMeta.getEncoding() ).thenReturn( "UTF-8" );

    InputStream fakeStream = new java.io.ByteArrayInputStream( "header\nval1".getBytes() );
    when( textFileInputMeta.getEncoding() ).thenReturn( "UTF-8" );

    try (
      MockedStatic<KettleVFS> vfsMock = mockStatic( KettleVFS.class );
      MockedStatic<CompressionProviderFactory> provFactory = mockStatic( CompressionProviderFactory.class );
      MockedConstruction<TextFileCsvFileTypeImportProcessor> processorCons = mockConstruction(
        TextFileCsvFileTypeImportProcessor.class, ( mock, ctx ) -> {
          when( mock.analyzeFile( anyBoolean() ) ).thenReturn( "summary" );
          when( mock.getInputFieldsDto() ).thenReturn(
            new TextFileInputFieldDTO[] {
              new TextFileInputFieldDTO() {{
                setName( "m1" );
                setType( "String" );
              }}
            }
          );
        } )
    ) {
      vfsMock.when( () -> KettleVFS.getInputStream( fileObject ) ).thenReturn( fakeStream );

      CompressionProviderFactory factory = mock( CompressionProviderFactory.class );
      CompressionProvider provider = mock( CompressionProvider.class );
      CompressionInputStream cstream = mock( CompressionInputStream.class );
      provFactory.when( CompressionProviderFactory::getInstance ).thenReturn( factory );
      when( factory.createCompressionProviderInstance( anyString() ) ).thenReturn( provider );
      when( provider.createInputStream( any( InputStream.class ) ) ).thenReturn( cstream );

      queryParams.put( "noOfFields", "1" );
      queryParams.put( "isSampleSummary", "true" );

      Method populateMeta = TextFileInputHelper.class.getDeclaredMethod(
        "populateMeta", TransMeta.class, TextFileInputMeta.class, Map.class
      );
      populateMeta.setAccessible( true );
      JSONObject result = (JSONObject) populateMeta.invoke( helper, transMeta, textFileInputMeta, queryParams );

      assertEquals( StepInterface.SUCCESS_RESPONSE, result.get( StepInterface.ACTION_STATUS ) );
      assertTrue( result.containsKey( "fields" ) );
      assertTrue( result.containsKey( "summary" ) );
    }
  }


  @Test
  public void testParseFieldNames_CoversBranches() throws Exception {
    setupBasicMocks();

    Method method = TextFileInputHelper.class.getDeclaredMethod(
      "parseFieldNames", TransMeta.class, TextFileInputMeta.class, String.class );
    method.setAccessible( true );

    String[] resultEmpty = (String[]) method.invoke( helper, transMeta, textFileInputMeta, "   " );
    assertEquals( 0, resultEmpty.length );

    try ( MockedStatic<TextFileInputUtils> mockUtils = mockStatic( TextFileInputUtils.class ) ) {
      mockUtils.when( () -> TextFileInputUtils.guessStringsFromLine(
        any(), any(), any(), any(), any(), any(), any()
      ) ).thenReturn( new String[] { "col1", "col2" } );

      String[] result = (String[]) method.invoke( helper, transMeta, textFileInputMeta, "abc,def" );
      assertArrayEquals( new String[] { "col1", "col2" }, result );
    }
  }

  @Test
  public void testConvertFieldsToJsonArray() throws Exception {
    Method method = TextFileInputHelper.class.getDeclaredMethod(
      "convertFieldsToJsonArray", TextFileInputFieldDTO[].class );
    method.setAccessible( true );

    TextFileInputFieldDTO dto1 = new TextFileInputFieldDTO();
    dto1.setName( "name" );
    dto1.setType( "String" );

    TextFileInputFieldDTO dto2 = new TextFileInputFieldDTO();
    dto2.setName( "age" );
    dto2.setType( "Integer" );

    TextFileInputFieldDTO[] arr = new TextFileInputFieldDTO[] { dto1, dto2 };

    JSONArray result = (JSONArray) method.invoke( helper, new Object[] { arr } );
    assertNotNull( result );
    assertEquals( 2, result.size() );
    assertTrue( result.toJSONString().contains( "name" ) );
    assertTrue( result.toJSONString().contains( "age" ) );
  }

  @Test
  public void testShowFilesAction_NullStepMeta() {
    when( transMeta.findStep( "testStep" ) ).thenReturn( null );

    JSONObject result = helper.showFilesAction( transMeta, queryParams );

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

    InputStream fakeStream = mock( InputStream.class );

    try (
      MockedStatic<KettleVFS> kettleVfs = mockStatic( KettleVFS.class );
      MockedStatic<CompressionProviderFactory> mockProviderFactory = mockStatic( CompressionProviderFactory.class )
    ) {
      kettleVfs.when( () -> KettleVFS.getInputStream( headerFile ) ).thenReturn( fakeStream );

      CompressionProviderFactory mockFactory = mock( CompressionProviderFactory.class );
      mockProviderFactory.when( CompressionProviderFactory::getInstance ).thenReturn( mockFactory );
      when( mockFactory.createCompressionProviderInstance( anyString() ) )
        .thenThrow( new RuntimeException( "Compression fail" ) );

      JSONObject result = helper.getFieldNamesAction( transMeta, queryParams );

      assertNotNull( result );
      assertEquals( StepInterface.SUCCESS_RESPONSE, result.get( StepInterface.ACTION_STATUS ) );
      assertTrue( ( (JSONArray) result.get( "fieldNames" ) ).isEmpty() );
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
    assertNotNull( result.get( "message" ) );
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
      kettleVfs.when( () -> KettleVFS.getInputStream( file ) ).thenThrow( new RuntimeException( "io error" ) );

      queryParams.put( "nrlines", "10" );
      queryParams.put( "skipHeaders", "false" );

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
