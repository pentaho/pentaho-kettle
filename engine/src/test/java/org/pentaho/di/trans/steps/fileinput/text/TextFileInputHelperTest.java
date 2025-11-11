/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2025 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/

package org.pentaho.di.trans.steps.fileinput.text;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.pentaho.di.core.KettleEnvironment;
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
import org.pentaho.di.trans.steps.file.BaseFileField;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class TextFileInputHelperTest {

  @Mock private TransMeta transMeta;
  @Mock private TextFileInputMeta textFileInputMeta;
  @Mock private FileInputList fileInputList;
  @Mock private CompressionProvider provider;
  @Mock private CompressionInputStream compressionStream;

  private AutoCloseable mocks;
  private TextFileInputHelper helper;
  private Map<String, String> queryParams;

  @BeforeClass
  public static void initkettle() throws KettleException {
    KettleEnvironment.init();
  }

  @Before
  public void setUp() {
    mocks = MockitoAnnotations.openMocks( this );
    helper = new TextFileInputHelper( textFileInputMeta );
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
  public void testHandleStepAction_Exception() throws KettleException, JsonProcessingException {
    TextFileInputHelper spyHelper = spy( helper );
    doThrow( new RuntimeException( "boom" ) ).when( spyHelper ).getFieldsAction( any(), any() );
    JSONObject result = spyHelper.handleStepAction( "getFields", transMeta, queryParams );
    assertEquals( BaseStepHelper.FAILURE_RESPONSE, result.get( BaseStepHelper.ACTION_STATUS ) );
  }

  @Test
  public void testShowFilesAction_Filters() {
    String[] files = { "data.csv", "notes.txt" };
    when( textFileInputMeta.getFilePaths( any(), any() ) ).thenReturn( files );

    // regex filter
    queryParams.put( "filter", ".*\\.csv" );
    queryParams.put( "isRegex", "true" );
    JSONObject result = helper.showFilesAction( transMeta, queryParams );
    JSONArray arr = (JSONArray) result.get( "files" );
    assertEquals( 1, arr.size() );
    assertTrue( arr.contains( "data.csv" ) );

    queryParams.put( "filter", "notes" );
    queryParams.put( "isRegex", "false" );
    result = helper.showFilesAction( transMeta, queryParams );
    arr = (JSONArray) result.get( "files" );
    assertEquals( 1, arr.size() );

    when( textFileInputMeta.getFilePaths( any(), any() ) ).thenReturn( new String[ 0 ] );
    result = helper.showFilesAction( transMeta, queryParams );
    assertTrue( result.containsKey( "message" ) );
  }

  @Test
  public void testShowFilesAction_BlankStep() {
    queryParams.put( "stepName", "" );
    JSONObject result = helper.showFilesAction( transMeta, queryParams );
    assertEquals( StepInterface.SUCCESS_RESPONSE, result.get( StepInterface.ACTION_STATUS ) );
  }

  @Test
  public void testGetFieldsAction_NonCsv() throws Exception {
    when( textFileInputMeta.getFileTypeNr() ).thenReturn( 2 );
    mockFileList();

    try (
      MockedStatic<KettleVFS> vfs = mockStatic( KettleVFS.class );
      MockedStatic<CompressionProviderFactory> provFactory = mockStatic( CompressionProviderFactory.class );
      MockedStatic<TextFileInputUtils> utils = mockStatic( TextFileInputUtils.class )
    ) {
      vfs.when( () -> KettleVFS.getInputStream( any( org.apache.commons.vfs2.FileObject.class ) ) )
        .thenReturn( new ByteArrayInputStream( "dummy".getBytes() ) );

      CompressionProviderFactory factory = mock( CompressionProviderFactory.class );
      CompressionProvider cprovider = mock( CompressionProvider.class );
      CompressionInputStream compressionInputStream = mock( CompressionInputStream.class );

      provFactory.when( CompressionProviderFactory::getInstance ).thenReturn( factory );
      when( factory.createCompressionProviderInstance( anyString() ) ).thenReturn( cprovider );
      when( cprovider.createInputStream( any( InputStream.class ) ) ).thenReturn( compressionInputStream );

      utils.when( () -> TextFileInputUtils.getLine(
        any(), any(), any(), anyInt(), any(), any(), any()
      ) ).thenReturn( "row-1" ).thenReturn( null );

      JSONObject result = helper.getFieldsAction( transMeta, queryParams );
      assertTrue( result.containsKey( "fields" ) );
    }
  }

  @Test
  public void testValidateShowContentAction() {
    when( textFileInputMeta.getFileInputList( any(), any() ) ).thenReturn( fileInputList );
    when( fileInputList.nrOfFiles() ).thenReturn( 0 );
    JSONObject result = helper.validateShowContentAction( transMeta, queryParams );
    assertTrue( result.containsKey( "message" ) );
  }

  @Test
  public void testShowContentAction() throws Exception {
    mockFileList();
    queryParams.put( "nrlines", "2" );
    queryParams.put( "skipHeaders", "false" );

    try (
      MockedStatic<KettleVFS> vfs = mockStatic( KettleVFS.class );
      MockedStatic<CompressionProviderFactory> provFactory = mockStatic( CompressionProviderFactory.class );
      MockedStatic<TextFileInputUtils> utils = mockStatic( TextFileInputUtils.class )
    ) {
      vfs.when( () -> KettleVFS.getInputStream( (FileObject) any() ) )
        .thenReturn( new ByteArrayInputStream( "line1\nline2".getBytes() ) );

      CompressionProviderFactory factory = mock( CompressionProviderFactory.class );
      CompressionProvider cprovider = mock( CompressionProvider.class );
      provFactory.when( CompressionProviderFactory::getInstance ).thenReturn( factory );
      when( factory.createCompressionProviderInstance( anyString() ) ).thenReturn( cprovider );
      when( cprovider.createInputStream( any() ) ).thenReturn( compressionStream );

      utils.when( () -> TextFileInputUtils.getLine(
        any(), any(), any(), anyInt(), any(), any(), any()
      ) ).thenReturn( "line1" ).thenReturn( "line2" ).thenReturn( null );

      JSONObject result = helper.showContentAction( transMeta, queryParams );

      assertTrue( result.containsKey( "firstFileContent" ) );
      JSONArray arr = (JSONArray) result.get( "firstFileContent" );
      assertFalse( arr.isEmpty() );
      assertTrue( arr.contains( "line1" ) );
    }
  }

  @Test
  public void testSetMinimalWidthAction_AllTypes() throws Exception {
    BaseFileField f1 = mockField( "f1", ValueMetaInterface.TYPE_STRING );
    BaseFileField f2 = mockField( "f2", ValueMetaInterface.TYPE_INTEGER );
    BaseFileField f3 = mockField( "f3", ValueMetaInterface.TYPE_NUMBER );
    BaseFileField f4 = mockField( "f4", ValueMetaInterface.TYPE_DATE );
    when( textFileInputMeta.getInputFields() ).thenReturn( new BaseFileField[] { f1, f2, f3, f4 } );
    JSONObject result = helper.setMinimalWidthAction( transMeta, queryParams );
    JSONArray updated = (JSONArray) result.get( "updatedData" );
    assertEquals( 4, updated.size() );
  }

  @Test
  public void testGetFields_GapAndNoFields() {
    TextFileInputMeta info = new TextFileInputMeta();
    BaseFileField f1 = new BaseFileField( "A", 0, 2 );
    BaseFileField f2 = new BaseFileField( "B", 5, 2 ); // gap
    info.inputFields = new BaseFileField[] { f1, f2 };
    List<String> rows = List.of( "abcdef" );
    List<?> vec = helper.getFields( info, rows );
    assertTrue( vec.size() >= 3 );

    info.inputFields = new BaseFileField[ 0 ];
    vec = helper.getFields( info, rows );
    assertEquals( 1, vec.size() );
  }

  @Test
  public void testMassageFieldName() {
    String res = helper.massageFieldName( "My- Field" );
    assertEquals( "My__Field", res );
  }

  private void mockFileList() {
    FileObject file = mock( FileObject.class );
    when( file.getName() ).thenReturn( mock( FileName.class ) );
    FileInputList list = mock( FileInputList.class );
    when( list.nrOfFiles() ).thenReturn( 1 );
    when( list.getFile( 0 ) ).thenReturn( file );
    when( textFileInputMeta.getFileInputList( any(), any() ) ).thenReturn( list );
    TextFileInputMeta.Content c = new TextFileInputMeta.Content();
    c.fileCompression = "None";
    textFileInputMeta.content = c;
  }

  private BaseFileField mockField( String name, int type ) {
    BaseFileField f = mock( BaseFileField.class );
    when( f.getName() ).thenReturn( name );
    when( f.getTypeDesc() ).thenReturn( "desc" );
    when( f.getType() ).thenReturn( type );
    when( f.getCurrencySymbol() ).thenReturn( "$" );
    when( f.getDecimalSymbol() ).thenReturn( "." );
    when( f.getGroupSymbol() ).thenReturn( "," );
    when( f.getNullString() ).thenReturn( "null" );
    when( f.getIfNullValue() ).thenReturn( "" );
    when( f.getPosition() ).thenReturn( 1 );
    when( f.isRepeated() ).thenReturn( false );
    return f;
  }

  @Test( expected = NullPointerException.class )
  public void testPopulateMeta_StepNotFound() throws Exception {
    queryParams.put( "isSampleSummary", "true" );
    queryParams.put( "noOfFields", "100" );

    when( transMeta.findStep( "testStep" ) ).thenReturn( null );
    when( textFileInputMeta.getFileTypeNr() ).thenReturn( TextFileInputMeta.FILE_TYPE_CSV );

    helper.populateMeta( transMeta, textFileInputMeta, queryParams );
  }

  @Test( expected = ClassCastException.class )
  public void testPopulateMeta_WrongStepType() throws Exception {
    queryParams.put( "isSampleSummary", "true" );
    queryParams.put( "noOfFields", "100" );

    org.pentaho.di.trans.step.StepMeta stepMeta = mock( org.pentaho.di.trans.step.StepMeta.class );
    org.pentaho.di.trans.step.StepMetaInterface wrongMeta = mock( org.pentaho.di.trans.step.StepMetaInterface.class );

    when( transMeta.findStep( "testStep" ) ).thenReturn( stepMeta );
    when( stepMeta.getStepMetaInterface() ).thenReturn( wrongMeta );
    when( textFileInputMeta.getFileTypeNr() ).thenReturn( TextFileInputMeta.FILE_TYPE_CSV );

    helper.populateMeta( transMeta, textFileInputMeta, queryParams );
  }

  @Test
  public void testHandleStepAction_GetFields() throws Exception {
    when( textFileInputMeta.getFileTypeNr() ).thenReturn( 2 );
    mockFileList();

    try (
      MockedStatic<KettleVFS> vfs = mockStatic( KettleVFS.class );
      MockedStatic<CompressionProviderFactory> provFactory = mockStatic( CompressionProviderFactory.class );
      MockedStatic<TextFileInputUtils> utils = mockStatic( TextFileInputUtils.class )
    ) {
      vfs.when( () -> KettleVFS.getInputStream( any( FileObject.class ) ) )
        .thenReturn( new ByteArrayInputStream( "line1".getBytes() ) );

      CompressionProviderFactory factory = mock( CompressionProviderFactory.class );
      provFactory.when( CompressionProviderFactory::getInstance ).thenReturn( factory );
      when( factory.createCompressionProviderInstance( anyString() ) ).thenReturn( provider );
      when( provider.createInputStream( any() ) ).thenReturn( compressionStream );

      utils.when( () -> TextFileInputUtils.getLine(
        any(), any(), any(), anyInt(), any(), any(), any()
      ) ).thenReturn( "line1" ).thenReturn( null );

      JSONObject result = helper.handleStepAction( "getFields", transMeta, queryParams );

      assertEquals( StepInterface.SUCCESS_RESPONSE, result.get( StepInterface.ACTION_STATUS ) );
      assertTrue( result.containsKey( "fields" ) );
    }
  }

  @Test
  public void testHandleStepAction_ShowFiles() {
    when( textFileInputMeta.getFilePaths( any(), any() ) ).thenReturn( new String[] { "file1.txt", "file2.csv" } );

    JSONObject result = helper.handleStepAction( "showFiles", transMeta, queryParams );

    assertEquals( StepInterface.SUCCESS_RESPONSE, result.get( StepInterface.ACTION_STATUS ) );
    assertTrue( result.containsKey( "files" ) );
    JSONArray files = (JSONArray) result.get( "files" );
    assertEquals( 2, files.size() );
  }

  @Test
  public void testHandleStepAction_ValidateShowContent() {
    when( textFileInputMeta.getFileInputList( any(), any() ) ).thenReturn( fileInputList );
    when( fileInputList.nrOfFiles() ).thenReturn( 5 );

    JSONObject result = helper.handleStepAction( "validateShowContent", transMeta, queryParams );

    assertEquals( StepInterface.SUCCESS_RESPONSE, result.get( StepInterface.ACTION_STATUS ) );
    assertFalse( result.containsKey( "message" ) );
  }

  @Test
  public void testHandleStepAction_ShowContent() throws Exception {
    mockFileList();
    queryParams.put( "nrlines", "3" );
    queryParams.put( "skipHeaders", "true" );

    try (
      MockedStatic<KettleVFS> vfs = mockStatic( KettleVFS.class );
      MockedStatic<CompressionProviderFactory> provFactory = mockStatic( CompressionProviderFactory.class );
      MockedStatic<TextFileInputUtils> utils = mockStatic( TextFileInputUtils.class )
    ) {
      vfs.when( () -> KettleVFS.getInputStream( any( FileObject.class ) ) )
        .thenReturn( new ByteArrayInputStream( "data".getBytes() ) );

      CompressionProviderFactory factory = mock( CompressionProviderFactory.class );
      provFactory.when( CompressionProviderFactory::getInstance ).thenReturn( factory );
      when( factory.createCompressionProviderInstance( anyString() ) ).thenReturn( provider );
      when( provider.createInputStream( any() ) ).thenReturn( compressionStream );

      utils.when( () -> TextFileInputUtils.getLine(
        any(), any(), any(), anyInt(), any(), any(), any()
      ) ).thenReturn( "row1" ).thenReturn( "row2" ).thenReturn( "row3" ).thenReturn( null );

      utils.when( () -> TextFileInputUtils.skipLines(
        any(), any(), any(), anyInt(), any(), anyInt(), any(), any(), anyInt()
      ) ).thenReturn( 0L );

      JSONObject result = helper.handleStepAction( "showContent", transMeta, queryParams );

      assertEquals( StepInterface.SUCCESS_RESPONSE, result.get( StepInterface.ACTION_STATUS ) );
      assertTrue( result.containsKey( "firstFileContent" ) );
    }
  }

  @Test
  public void testHandleStepAction_SetMinimalWidth() {
    BaseFileField field = mockField( "testField", ValueMetaInterface.TYPE_STRING );
    when( textFileInputMeta.getInputFields() ).thenReturn( new BaseFileField[] { field } );

    JSONObject result = helper.handleStepAction( "setMinimalWidth", transMeta, queryParams );

    assertEquals( StepInterface.SUCCESS_RESPONSE, result.get( StepInterface.ACTION_STATUS ) );
    assertTrue( result.containsKey( "updatedData" ) );
    JSONArray updatedData = (JSONArray) result.get( "updatedData" );
    assertEquals( 1, updatedData.size() );
  }

  @Test
  public void testHandleStepAction_UnknownMethod() {
    JSONObject result = helper.handleStepAction( "unknownMethod", transMeta, queryParams );

    assertEquals( BaseStepHelper.FAILURE_METHOD_NOT_FOUND_RESPONSE, result.get( BaseStepHelper.ACTION_STATUS ) );
  }

  @Test
  public void testHandleStepAction_GetFieldsException() throws Exception {
    TextFileInputHelper spyHelper = spy( helper );
    doThrow( new KettleException( "Test error" ) ).when( spyHelper ).getFieldsAction( any(), any() );

    JSONObject result = spyHelper.handleStepAction( "getFields", transMeta, queryParams );

    assertEquals( BaseStepHelper.FAILURE_RESPONSE, result.get( BaseStepHelper.ACTION_STATUS ) );
  }

  @Test
  public void testHandleStepAction_GetFieldNamesException() {
    TextFileInputHelper spyHelper = spy( helper );
    doThrow( new RuntimeException( "Test error" ) ).when( spyHelper ).getFieldNamesAction( any(), any() );

    JSONObject result = spyHelper.handleStepAction( "getFieldNames", transMeta, queryParams );

    assertEquals( BaseStepHelper.FAILURE_RESPONSE, result.get( BaseStepHelper.ACTION_STATUS ) );
  }

  @Test
  public void testHandleStepAction_ShowFilesException() {
    TextFileInputHelper spyHelper = spy( helper );
    doThrow( new RuntimeException( "File error" ) ).when( spyHelper ).showFilesAction( any(), any() );

    JSONObject result = spyHelper.handleStepAction( "showFiles", transMeta, queryParams );

    assertEquals( BaseStepHelper.FAILURE_RESPONSE, result.get( BaseStepHelper.ACTION_STATUS ) );
  }

  @Test
  public void testHandleStepAction_ValidateShowContentException() {
    TextFileInputHelper spyHelper = spy( helper );
    doThrow( new RuntimeException( "Validation error" ) ).when( spyHelper ).validateShowContentAction( any(), any() );

    JSONObject result = spyHelper.handleStepAction( "validateShowContent", transMeta, queryParams );

    assertEquals( BaseStepHelper.FAILURE_RESPONSE, result.get( BaseStepHelper.ACTION_STATUS ) );
  }

  @Test
  public void testHandleStepAction_ShowContentException() throws Exception {
    TextFileInputHelper spyHelper = spy( helper );
    doThrow( new KettleException( "Content error" ) ).when( spyHelper ).showContentAction( any(), any() );

    JSONObject result = spyHelper.handleStepAction( "showContent", transMeta, queryParams );

    assertEquals( BaseStepHelper.FAILURE_RESPONSE, result.get( BaseStepHelper.ACTION_STATUS ) );
  }

  @Test
  public void testHandleStepAction_SetMinimalWidthException() throws Exception {
    TextFileInputHelper spyHelper = spy( helper );
    doThrow( new JsonProcessingException( "JSON error" ) {
    } ).when( spyHelper ).setMinimalWidthAction( any(), any() );

    JSONObject result = spyHelper.handleStepAction( "setMinimalWidth", transMeta, queryParams );

    assertEquals( BaseStepHelper.FAILURE_RESPONSE, result.get( BaseStepHelper.ACTION_STATUS ) );
  }

  @Test
  public void testHandleStepAction_NullPointerException() {
    TextFileInputHelper spyHelper = spy( helper );
    doThrow( new NullPointerException( "Null error" ) ).when( spyHelper ).showFilesAction( any(), any() );

    JSONObject result = spyHelper.handleStepAction( "showFiles", transMeta, queryParams );

    assertEquals( BaseStepHelper.FAILURE_RESPONSE, result.get( BaseStepHelper.ACTION_STATUS ) );
  }

  @Test
  public void testHandleStepAction_SetTransMetaCalled() throws Exception {
    when( textFileInputMeta.getFileTypeNr() ).thenReturn( 2 );
    mockFileList();

    try (
      MockedStatic<KettleVFS> vfs = mockStatic( KettleVFS.class );
      MockedStatic<CompressionProviderFactory> provFactory = mockStatic( CompressionProviderFactory.class );
      MockedStatic<TextFileInputUtils> utils = mockStatic( TextFileInputUtils.class )
    ) {
      vfs.when( () -> KettleVFS.getInputStream( any( FileObject.class ) ) )
        .thenReturn( new ByteArrayInputStream( "test".getBytes() ) );

      CompressionProviderFactory factory = mock( CompressionProviderFactory.class );
      provFactory.when( CompressionProviderFactory::getInstance ).thenReturn( factory );
      when( factory.createCompressionProviderInstance( anyString() ) ).thenReturn( provider );
      when( provider.createInputStream( any() ) ).thenReturn( compressionStream );

      utils.when( () -> TextFileInputUtils.getLine(
        any(), any(), any(), anyInt(), any(), any(), any()
      ) ).thenReturn( "test" ).thenReturn( null );

      helper.handleStepAction( "getFields", transMeta, queryParams );

      assertEquals( transMeta, helper.getTransMeta() );
    }
  }

  @Test
  public void testHandleStepAction() {
    String[] methods =
      { "getFields", "getFieldNames", "showFiles", "validateShowContent", "showContent", "setMinimalWidth" };

    for ( String method : methods ) {
      JSONObject result = null;
      try {
        if ( "getFields".equals( method ) ) {
          when( textFileInputMeta.getFileTypeNr() ).thenReturn( 2 );
          mockFileList();
          try (
            MockedStatic<KettleVFS> vfs = mockStatic( KettleVFS.class );
            MockedStatic<CompressionProviderFactory> provFactory = mockStatic( CompressionProviderFactory.class );
            MockedStatic<TextFileInputUtils> utils = mockStatic( TextFileInputUtils.class )
          ) {
            vfs.when( () -> KettleVFS.getInputStream( any( FileObject.class ) ) )
              .thenReturn( new ByteArrayInputStream( "test".getBytes() ) );
            CompressionProviderFactory factory = mock( CompressionProviderFactory.class );
            provFactory.when( CompressionProviderFactory::getInstance ).thenReturn( factory );
            when( factory.createCompressionProviderInstance( anyString() ) ).thenReturn( provider );
            when( provider.createInputStream( any() ) ).thenReturn( compressionStream );
            utils.when( () -> TextFileInputUtils.getLine(
              any(), any(), any(), anyInt(), any(), any(), any()
            ) ).thenReturn( null );
            result = helper.handleStepAction( method, transMeta, queryParams );
          }
        } else if ( "setMinimalWidth".equals( method ) ) {
          when( textFileInputMeta.getInputFields() ).thenReturn( new BaseFileField[] {} );
          result = helper.handleStepAction( method, transMeta, queryParams );
        } else {
          result = helper.handleStepAction( method, transMeta, queryParams );
        }

        assertNotNull( "Method " + method + " should return a response", result );
      } catch ( Exception e ) {
        fail( "Method " + method + " should not throw an exception: " + e.getMessage() );
      }
    }
  }

}
