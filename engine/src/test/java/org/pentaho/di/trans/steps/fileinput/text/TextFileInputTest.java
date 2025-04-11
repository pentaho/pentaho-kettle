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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs2.FileContent;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.json.simple.JSONObject;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.RowSet;
import org.pentaho.di.core.bowl.DefaultBowl;
import org.pentaho.di.core.compress.CompressionInputStream;
import org.pentaho.di.core.compress.CompressionProvider;
import org.pentaho.di.core.compress.CompressionProviderFactory;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.fileinput.FileInputList;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.playlist.FilePlayListAll;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaFactory;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.core.util.Assert;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.core.vfs.IKettleVFS;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.TransTestingUtil;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.errorhandling.AbstractFileErrorHandler;
import org.pentaho.di.trans.step.errorhandling.FileErrorHandler;
import org.pentaho.di.trans.steps.StepMockUtil;
import org.pentaho.di.trans.steps.common.CsvInputAwareMeta;
import org.pentaho.di.trans.steps.common.CsvInputAwareStep;
import org.pentaho.di.trans.steps.file.BaseFileField;
import org.pentaho.di.trans.steps.file.BaseFileInputFiles;
import org.pentaho.di.trans.steps.file.IBaseFileInputReader;
import org.pentaho.di.trans.steps.file.IBaseFileInputStepControl;
import org.pentaho.di.utils.TestUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class TextFileInputTest {
  public static final String TEST_TXT = "test.txt";
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @BeforeClass
  public static void initKettle() throws Exception {
    KettleEnvironment.init();
  }

  private static BufferedInputStreamReader getInputStreamReader( String data ) throws UnsupportedEncodingException {
    return new BufferedInputStreamReader(
      new InputStreamReader( new ByteArrayInputStream( data.getBytes( ( "UTF-8" ) ) ) ) );
  }

  @Test
  public void testGetLineDOS() throws KettleFileException, UnsupportedEncodingException {
    String input = "col1\tcol2\tcol3\r\ndata1\tdata2\tdata3\r\n";
    String expected = "col1\tcol2\tcol3";
    String output =
        TextFileInputUtils.getLine( null, getInputStreamReader( input ), TextFileInputMeta.FILE_FORMAT_DOS,
            new StringBuilder( 1000 ) );
    assertEquals( expected, output );
  }

  @Test
  public void testGetLineUnix() throws KettleFileException, UnsupportedEncodingException {
    String input = "col1\tcol2\tcol3\ndata1\tdata2\tdata3\n";
    String expected = "col1\tcol2\tcol3";
    String output =
        TextFileInputUtils.getLine( null, getInputStreamReader( input ), TextFileInputMeta.FILE_FORMAT_UNIX,
            new StringBuilder( 1000 ) );
    assertEquals( expected, output );
  }

  @Test
  public void testGetLineOSX() throws KettleFileException, UnsupportedEncodingException {
    String input = "col1\tcol2\tcol3\rdata1\tdata2\tdata3\r";
    String expected = "col1\tcol2\tcol3";
    String output =
        TextFileInputUtils.getLine( null, getInputStreamReader( input ), TextFileInputMeta.FILE_FORMAT_UNIX,
            new StringBuilder( 1000 ) );
    assertEquals( expected, output );
  }

  @Test
  public void testGetLineMixed() throws KettleFileException, UnsupportedEncodingException {
    String input = "col1\tcol2\tcol3\r\ndata1\tdata2\tdata3\r";
    String expected = "col1\tcol2\tcol3";
    String output =
        TextFileInputUtils.getLine( null, getInputStreamReader( input ), TextFileInputMeta.FILE_FORMAT_MIXED,
            new StringBuilder( 1000 ) );
    assertEquals( expected, output );
  }

  @Test
  public void testGetLineMixedOS9() throws KettleFileException, UnsupportedEncodingException {
    String input = "col1\tcol2\tcol3\rdata1\tdata2\tdata3\r";
    String expected = "col1\tcol2\tcol3";
    String output =
      TextFileInputUtils.getLine( null, getInputStreamReader( input ), TextFileInputMeta.FILE_FORMAT_MIXED,
        new StringBuilder( 1000 ) );
    assertEquals( expected, output );
  }

  @Test( timeout = 100 )
  public void test_PDI695() throws KettleFileException, UnsupportedEncodingException {
    String inputDOS = "col1\tcol2\tcol3\r\ndata1\tdata2\tdata3\r\n";
    String inputUnix = "col1\tcol2\tcol3\ndata1\tdata2\tdata3\n";
    String inputOSX = "col1\tcol2\tcol3\rdata1\tdata2\tdata3\r";
    String expected = "col1\tcol2\tcol3";

    assertEquals( expected, TextFileInputUtils.getLine( null, getInputStreamReader( inputDOS ),
        TextFileInputMeta.FILE_FORMAT_UNIX, new StringBuilder( 1000 ) ) );
    assertEquals( expected, TextFileInputUtils.getLine( null, getInputStreamReader( inputUnix ),
        TextFileInputMeta.FILE_FORMAT_UNIX, new StringBuilder( 1000 ) ) );
    assertEquals( expected, TextFileInputUtils.getLine( null, getInputStreamReader( inputOSX ),
        TextFileInputMeta.FILE_FORMAT_UNIX, new StringBuilder( 1000 ) ) );
  }

  @Test
  public void readWrappedInputWithoutHeaders() throws Exception {
    final String content = new StringBuilder()
      .append( "r1c1" ).append( '\n' ).append( ";r1c2\n" )
      .append( "r2c1" ).append( '\n' ).append( ";r2c2" )
      .toString();
    final String virtualFile = createVirtualFile( "pdi-2607.txt", content );

    TextFileInputMeta meta = createMetaObject( field( "col1" ), field( "col2" ) );
    meta.content.lineWrapped = true;
    meta.content.nrWraps = 1;

    TextFileInputData data = createDataObject( virtualFile, ";", "col1", "col2" );

    TextFileInput input = StepMockUtil.getStep( TextFileInput.class, TextFileInputMeta.class, "test" );
    List<Object[]> output = TransTestingUtil.execute( input, meta, data, 2, false );
    TransTestingUtil.assertResult( new Object[] { "r1c1", "r1c2" }, output.get( 0 ) );
    TransTestingUtil.assertResult( new Object[] { "r2c1", "r2c2" }, output.get( 1 ) );

    deleteVfsFile( virtualFile );
  }

  @Test
  public void readInputWithMissedValues() throws Exception {
    final String virtualFile = createVirtualFile( "pdi-14172.txt", "1,1,1\n", "2,,2\n" );

    BaseFileField field2 = field( "col2" );
    field2.setRepeated( true );

    TextFileInputMeta meta = createMetaObject( field( "col1" ), field2, field( "col3" ) );
    TextFileInputData data = createDataObject( virtualFile, ",", "col1", "col2", "col3" );

    TextFileInput input = StepMockUtil.getStep( TextFileInput.class, TextFileInputMeta.class, "test" );
    List<Object[]> output = TransTestingUtil.execute( input, meta, data, 2, false );
    TransTestingUtil.assertResult( new Object[] { "1", "1", "1" }, output.get( 0 ) );
    TransTestingUtil.assertResult( new Object[] { "2", "1", "2" }, output.get( 1 ) );

    deleteVfsFile( virtualFile );
  }

  @Test
  public void readInputWithNonEmptyNullif() throws Exception {
    final String virtualFile = createVirtualFile( "pdi-14358.txt", "-,-\n" );

    BaseFileField col2 = field( "col2" );
    col2.setNullString( "-" );

    TextFileInputMeta meta = createMetaObject( field( "col1" ), col2 );
    TextFileInputData data = createDataObject( virtualFile, ",", "col1", "col2" );

    TextFileInput input = StepMockUtil.getStep( TextFileInput.class, TextFileInputMeta.class, "test" );
    List<Object[]> output = TransTestingUtil.execute( input, meta, data, 1, false );
    TransTestingUtil.assertResult( new Object[] { "-" }, output.get( 0 ) );

    deleteVfsFile( virtualFile );
  }

  @Test
  public void readInputWithDefaultValues() throws Exception {
    final String virtualFile = createVirtualFile( "pdi-14832.txt", "1,\n" );

    BaseFileField col2 = field( "col2" );
    col2.setIfNullValue( "DEFAULT" );

    TextFileInputMeta meta = createMetaObject( field( "col1" ), col2 );
    TextFileInputData data = createDataObject( virtualFile, ",", "col1", "col2" );

    TextFileInput input = StepMockUtil.getStep( TextFileInput.class, TextFileInputMeta.class, "test" );
    List<Object[]> output = TransTestingUtil.execute( input, meta, data, 1, false );
    TransTestingUtil.assertResult( new Object[] { "1", "DEFAULT" }, output.get( 0 ) );

    deleteVfsFile( virtualFile );
  }

  @Test
  public void testErrorHandlerLineNumber() throws Exception {
    final String content = new StringBuilder()
      .append( "123" ).append( '\n' ).append( "333\n" )
      .append( "345" ).append( '\n' ).append( "773\n" )
      .append( "aaa" ).append( '\n' ).append( "444" )
      .toString();
    final String virtualFile = createVirtualFile( "pdi-2607.txt", content );

    TextFileInputMeta meta = createMetaObject( field( "col1" ) );

    meta.inputFields[0].setType( 1 );
    meta.content.lineWrapped = false;
    meta.content.nrWraps = 1;
    meta.errorHandling.errorIgnored = true;
    TextFileInputData data = createDataObject( virtualFile, ";", "col1" );
    data.dataErrorLineHandler = Mockito.mock( FileErrorHandler.class );
    TextFileInput input = StepMockUtil.getStep( TextFileInput.class, TextFileInputMeta.class, "test" );

    List<Object[]> output = TransTestingUtil.execute( input, meta, data, 4, false );

    Mockito.verify( data.dataErrorLineHandler ).handleLineError( 4, AbstractFileErrorHandler.NO_PARTS );
    deleteVfsFile( virtualFile );
  }

  @Test
  public void testHandleOpenFileException() throws Exception {
    final String content = new StringBuilder()
      .append( "123" ).append( '\n' ).append( "333\n" ).toString();
    final String virtualFile = createVirtualFile( "pdi-16697.txt", content );

    TextFileInputMeta meta = createMetaObject( field( "col1" ) );

    meta.inputFields[ 0 ].setType( 1 );
    meta.errorHandling.errorIgnored = true;
    meta.errorHandling.skipBadFiles = true;

    TextFileInputData data = createDataObject( virtualFile, ";", "col1" );
    data.dataErrorLineHandler = Mockito.mock( FileErrorHandler.class );

    TestTextFileInput textFileInput = StepMockUtil.getStep( TestTextFileInput.class, TextFileInputMeta.class, "test" );
    StepMeta stepMeta = textFileInput.getStepMeta();
    Mockito.doReturn( true ).when( stepMeta ).isDoingErrorHandling();

    List<Object[]> output = TransTestingUtil.execute( textFileInput, meta, data, 0, false );

    deleteVfsFile( virtualFile );

    assertEquals( 1, data.rejectedFiles.size() );
    assertEquals( 0, textFileInput.getErrors() );
  }

  @Test
  public void test_PDI17117() throws Exception {
    final String virtualFile = createVirtualFile( "pdi-14832.txt", "1,\n" );

    BaseFileField col2 = field( "col2" );
    col2.setIfNullValue( "DEFAULT" );

    TextFileInputMeta meta = createMetaObject( field( "col1" ), col2 );

    meta.inputFiles.passingThruFields = true;
    meta.inputFiles.acceptingFilenames = true;
    TextFileInputData data = createDataObject( virtualFile, ",", "col1", "col2" );

    TextFileInput input = spy( StepMockUtil.getStep( TextFileInput.class, TextFileInputMeta.class, "test" ) );

    RowSet rowset = Mockito.mock( RowSet.class );
    RowMetaInterface rwi = Mockito.mock( RowMetaInterface.class );
    Object[] obj1 = new Object[2];
    Object[] obj2 = new Object[2];
    Mockito.doReturn( rowset ).when( input ).findInputRowSet( null );
    Mockito.doReturn( null ).when( input ).getRowFrom( rowset );
    Mockito.when( input.getRowFrom( rowset ) ).thenReturn( obj1, obj2, null );
    Mockito.doReturn( rwi ).when( rowset ).getRowMeta();
    Mockito.when( rwi.getString( obj2, 0 ) ).thenReturn( "filename1", "filename2" );
    Mockito.when( input.getTransMeta().getBowl() ).thenReturn( DefaultBowl.getInstance() );
    List<Object[]> output = TransTestingUtil.execute( input, meta, data, 0, false );

    List<String> passThroughKeys = new ArrayList<>( data.passThruFields.keySet() );
    Assert.assertNotNull( passThroughKeys );
    // set order is not guaranteed - order alphabetically
    passThroughKeys.sort( String.CASE_INSENSITIVE_ORDER );
    assertEquals( 2, passThroughKeys.size() );

    Assert.assertNotNull( passThroughKeys.get( 0 ) );
    Assert.assertTrue( passThroughKeys.get( 0 ).startsWith( "0_file" ) );
    Assert.assertTrue( passThroughKeys.get( 0 ).endsWith( "filename1" ) );

    Assert.assertNotNull( passThroughKeys.get( 1 ) );
    Assert.assertTrue( passThroughKeys.get( 1 ).startsWith( "1_file" ) );
    Assert.assertTrue( passThroughKeys.get( 1 ).endsWith( "filename2" ) );

    deleteVfsFile( virtualFile );
  }

  /**
   * This test handles the case where a folder is given for the target of the previous step. Note, that it uses
   * 2 ram files and sets the mock to return "ram://." to trigger the directory logic.
   * @throws Exception - Test exception
   */
  @Test
  public void testFolderFromPreviousStep() throws Exception {
    final String virtualFile = createVirtualFile( "test-file1.txt", "1,\n" );
    final String virtualFile2 = createVirtualFile( "test-file2.txt", "1,\n" );

    BaseFileField col2 = field( "col2" );
    col2.setIfNullValue( "DEFAULT" );

    TextFileInputMeta meta = createMetaObject( field( "col1" ), col2 );

    VariableSpace space = new Variables();
    space.initializeVariablesFrom( null );

    meta.inputFiles.passingThruFields = true;
    meta.inputFiles.acceptingFilenames = true;
    TextFileInputData data = createDataObject( virtualFile, ",", "col1", "col2" );
    data.files.addFile( KettleVFS.getFileObject( virtualFile2 ) );

    TextFileInput input = spy( StepMockUtil.getStep( TextFileInput.class, TextFileInputMeta.class, "test" ) );


    RowSet rowset = Mockito.mock( RowSet.class );
    RowMetaInterface rwi = Mockito.mock( RowMetaInterface.class );
    Object[] obj1 = new Object[2];
    Object[] obj2 = new Object[2];
    Mockito.doReturn( rowset ).when( input ).findInputRowSet( null );
    Mockito.doReturn( null ).when( input ).getRowFrom( rowset );
    Mockito.when( input.getRowFrom( rowset ) ).thenReturn( obj1, null );
    Mockito.when( input.getTransMeta().listVariables() ).thenReturn( space.listVariables() );
    Mockito.when( input.getTransMeta().getVariable( anyString() ) ).thenAnswer( (Answer<String>)
      invocation -> space.getVariable( (String) invocation.getArguments()[0] ) );
    Mockito.when( input.getTransMeta().getBowl() ).thenReturn( DefaultBowl.getInstance() );

    Mockito.doReturn( rwi ).when( rowset ).getRowMeta();
    Mockito.when( rwi.getString( obj2, 0 ) ).thenReturn( "ram:///." );
    List<Object[]> output = TransTestingUtil.execute( input, meta, data, 0, false );

    List<String> passThroughKeys = new ArrayList<>( data.passThruFields.keySet() );
    Assert.assertNotNull( passThroughKeys );
    // set order is not guaranteed - order alphabetically
    passThroughKeys.sort( String.CASE_INSENSITIVE_ORDER );
    assertEquals( 2, passThroughKeys.size() );

    Assert.assertNotNull( passThroughKeys.get( 0 ) );
    Assert.assertTrue( passThroughKeys.get( 0 ).startsWith( "0_ram" ) );
    Assert.assertTrue( passThroughKeys.get( 0 ).endsWith( "test-file1.txt" ) );

    Assert.assertNotNull( passThroughKeys.get( 1 ) );
    Assert.assertTrue( passThroughKeys.get( 1 ).startsWith( "1_ram" ) );
    Assert.assertTrue( passThroughKeys.get( 1 ).endsWith( "test-file2.txt" ) );

    deleteVfsFile( virtualFile );
    deleteVfsFile( virtualFile2 );
  }

  @Test
  public void testClose() throws Exception {

    TextFileInputMeta mockTFIM = createMetaObject( null );
    String virtualFile = createVirtualFile( "pdi-17267.txt", null );
    TextFileInputData mockTFID = createDataObject( virtualFile, ";", null );
    mockTFID.lineBuffer = new ArrayList<>();
    mockTFID.lineBuffer.add( new TextFileLine( null, 0l, null ) );
    mockTFID.lineBuffer.add( new TextFileLine( null, 0l, null ) );
    mockTFID.lineBuffer.add( new TextFileLine( null, 0l, null ) );
    mockTFID.filename = "";

    FileContent mockFileContent = mock( FileContent.class );
    InputStream mockInputStream = mock( InputStream.class );
    when( mockFileContent.getInputStream() ).thenReturn( mockInputStream );
    FileObject mockFO = mock( FileObject.class );
    when( mockFO.getContent() ).thenReturn( mockFileContent );

    TextFileInputReader tFIR = new TextFileInputReader( mock( IBaseFileInputStepControl.class ),
      mockTFIM, mockTFID, mockFO, mock( LogChannelInterface.class ) );

    assertEquals( 3, mockTFID.lineBuffer.size() );
    tFIR.close();
    // After closing the file, the buffer must be empty!
    assertEquals( 0, mockTFID.lineBuffer.size() );
  }

  @Test
  public void fieldsWithLineBreaksTest() throws Exception {

    final String content = new StringBuilder()
      .append( "aaa,\"b" ).append( '\n' )
      .append( "bb\",ccc" ).append( '\n' )
      .append( "zzz,yyy,xxx" ).toString();
    final String virtualFile = createVirtualFile( "pdi-18175.txt", content );

    TextFileInputMeta meta = createMetaObject( field( "col1" ), field( "col2" ), field( "col3" ) );
    TextFileInputData data = createDataObject( virtualFile, ",", "col1", "col2", "col3" );

    TextFileInput input = StepMockUtil.getStep( TextFileInput.class, TextFileInputMeta.class, "test" );
    List<Object[]> output = TransTestingUtil.execute( input, meta, data, 2, false );
    TransTestingUtil.assertResult( new Object[] { "aaa", "\"b\nbb\"", "ccc" }, output.get( 0 ) );
    TransTestingUtil.assertResult( new Object[] { "zzz", "yyy", "xxx" }, output.get( 1 ) );

    deleteVfsFile( virtualFile );
  }

  @Test
  public void fieldsWithLineBreaksAndNoEmptyLinesTest() throws Exception {

    final String content = new StringBuilder()
      .append( "aaa,\"b" ).append( '\n' )
      .append( "bb\",ccc" ).append( '\n' )
      .append( '\n' )
      .append( "zzz,yyy,xxx" ).toString();
    final String virtualFile = createVirtualFile( "pdi-18175.txt", content );

    TextFileInputMeta meta = createMetaObject( field( "col1" ), field( "col2" ), field( "col3" ) );
    meta.content.noEmptyLines = true;
    TextFileInputData data = createDataObject( virtualFile, ",", "col1", "col2", "col3" );


    TextFileInput input = StepMockUtil.getStep( TextFileInput.class, TextFileInputMeta.class, "test" );
    List<Object[]> output = TransTestingUtil.execute( input, meta, data, 2, false );
    TransTestingUtil.assertResult( new Object[] { "aaa", "\"b\nbb\"", "ccc" }, output.get( 0 ) );
    TransTestingUtil.assertResult( new Object[] { "zzz", "yyy", "xxx" }, output.get( 1 ) );

    deleteVfsFile( virtualFile );
  }

  @Test
  public void fieldsWithLineBreaksWithEmptyLinesTest() throws Exception {

    final String content = new StringBuilder()
      .append( "aaa,\"b" ).append( '\n' )
      .append( "bb\",ccc" ).append( '\n' )
      .append( '\n' )
      .append( "zzz,yyy,xxx" ).toString();
    final String virtualFile = createVirtualFile( "pdi-18175.txt", content );

    TextFileInputMeta meta = createMetaObject( field( "col1" ), field( "col2" ), field( "col3" ) );
    meta.content.noEmptyLines = false;
    TextFileInputData data = createDataObject( virtualFile, ",", "col1", "col2", "col3" );


    TextFileInput input = StepMockUtil.getStep( TextFileInput.class, TextFileInputMeta.class, "test" );
    List<Object[]> output = TransTestingUtil.execute( input, meta, data, 3, false );
    TransTestingUtil.assertResult( new Object[] { "aaa", "\"b\nbb\"", "ccc" }, output.get( 0 ) );
    TransTestingUtil.assertResult( new Object[] { null }, output.get( 1 ) );
    TransTestingUtil.assertResult( new Object[] { "zzz", "yyy", "xxx" }, output.get( 2 ) );

    deleteVfsFile( virtualFile );
  }

  private TextFileInputMeta createMetaObject( BaseFileField... fields ) {
    TextFileInputMeta meta = new TextFileInputMeta();
    meta.content.enclosure = "\"";
    meta.content.fileCompression = "None";
    meta.content.fileType = "CSV";
    meta.content.header = false;
    meta.content.nrHeaderLines = -1;
    meta.content.footer = false;
    meta.content.nrFooterLines = -1;

    meta.inputFields = fields;
    return meta;
  }

  private TextFileInputData createDataObject( String file,
                                              String separator,
                                              String... outputFields ) throws Exception {
    TextFileInputData data = new TextFileInputData();
    data.files = new FileInputList();
    data.files.addFile( KettleVFS.getFileObject( file ) );

    data.separator = separator;

    data.outputRowMeta = new RowMeta();
    if ( outputFields != null ) {
      for ( String field : outputFields ) {
        data.outputRowMeta.addValueMeta( new ValueMetaString( field ) );
      }
    }

    data.dataErrorLineHandler = mock( FileErrorHandler.class );
    data.fileFormatType = TextFileInputMeta.FILE_FORMAT_UNIX;
    data.filterProcessor = new TextFileFilterProcessor( new TextFileFilter[ 0 ], new Variables() );
    data.filePlayList = new FilePlayListAll();
    return data;
  }

  private static String createVirtualFile( String filename, String... rows ) throws Exception {
    String virtualFile = TestUtils.createRamFile( filename );

    StringBuilder content = new StringBuilder();
    if ( rows != null ) {
      for ( String row : rows ) {
        content.append( row );
      }
    }
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    bos.write( content.toString().getBytes() );

    try ( OutputStream os = KettleVFS.getFileObject( virtualFile ).getContent().getOutputStream() ) {
      IOUtils.copy( new ByteArrayInputStream( bos.toByteArray() ), os );
    }

    return virtualFile;
  }

  private static void deleteVfsFile( String path ) throws Exception {
    FileObject fileObject = TestUtils.getFileObject( path );
    fileObject.close();
    fileObject.delete();
  }

  private static BaseFileField field( String name ) {
    return new BaseFileField( name, -1, -1 );
  }

  public static class TestTextFileInput extends TextFileInput {
    public TestTextFileInput( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta,
                              Trans trans ) {
      super( stepMeta, stepDataInterface, copyNr, transMeta, trans );
    }

    @Override
    protected IBaseFileInputReader createReader( TextFileInputMeta meta, TextFileInputData data, FileObject file )
      throws Exception {
      throw new Exception( "Can not create reader for the file object " + file );
    }
  }

  @Test
  public void testDoAction() throws Exception {

    try ( MockedStatic<KettleVFS> kettleVFSMockedStatic = Mockito.mockStatic( KettleVFS.class ) ) {
      TextFileInput input = StepMockUtil.getStep( TextFileInput.class, TextFileInputMeta.class, "test" );
      TextFileInputMeta meta = mock( TextFileInputMeta.class );
      BaseFileField[] fields = new BaseFileField[ 4 ];
      fields[ 0 ] = new BaseFileField( "field1", 1, 1 );
      fields[ 1 ] = new BaseFileField( "field2", 1, 1 );
      fields[ 2 ] = new BaseFileField( "field3", 1, 1 );
      fields[ 3 ] = new BaseFileField( "field4", 1, 1 );
      fields[ 1 ].setType( ValueMetaInterface.TYPE_NUMBER );
      fields[ 2 ].setType( ValueMetaInterface.TYPE_INTEGER );
      fields[ 3 ].setType( ValueMetaInterface.TYPE_BOOLEAN );

      when( meta.getInputFields() ).thenReturn( fields );
      TransMeta mockTransMeta = input.getTransMeta();
      Trans mockTrans = input.getTrans();
      Map<String, String> queryMap = new HashMap<>();

      // setMinimalWidth test case
      JSONObject response = input.doAction( "setMinimalWidth", meta, mockTransMeta, mockTrans, queryMap );
      assertEquals( StepInterface.SUCCESS_RESPONSE, response.get( StepInterface.ACTION_STATUS ) );

      // getFields test case
      InputStream mockInputStream = mock( InputStream.class );
      FileObject mockFileObject = mock( FileObject.class );
      when( meta.getHeaderFileObject( any() ) ).thenReturn( mockFileObject );
      MockedStatic<CompressionProviderFactory> compressionProviderFactoryMockedStatic =
        Mockito.mockStatic( CompressionProviderFactory.class );
      CompressionProviderFactory mockCPFactory = mock( CompressionProviderFactory.class );
      CompressionProvider mockCP = mock( CompressionProvider.class );
      CompressionInputStream mockCStream = mock( CompressionInputStream.class );
      compressionProviderFactoryMockedStatic.when( () -> CompressionProviderFactory.getInstance() )
        .thenReturn( mockCPFactory );
      when( mockCPFactory.createCompressionProviderInstance( any() ) ).thenReturn( mockCP );
      when( mockCP.createInputStream( any( InputStream.class ) ) ).thenReturn( mockCStream );
      TextFileInputMeta.Content mockContent = mock( TextFileInputMeta.Content.class );
      meta.content = mockContent;
      kettleVFSMockedStatic.when( () -> KettleVFS.getInputStream( any( FileObject.class ) ) )
        .thenReturn( mockInputStream );

      MockedStatic<TextFileInputUtils> textFileInputUtilsMockedStatic = Mockito.mockStatic( TextFileInputUtils.class );
      textFileInputUtilsMockedStatic.when( () -> TextFileInputUtils.getLine(
          any( LogChannelInterface.class ), any( BufferedInputStreamReader.class ),
          any( EncodingType.class ), anyInt(), any( StringBuilder.class ), anyString(), anyString() ) )
        .thenReturn( "line" );

      FileInputList mockFileList = mock( FileInputList.class );
      when( meta.getFileInputList( any(), any() ) ).thenReturn( mockFileList );
      when( mockFileList.nrOfFiles() ).thenReturn( 1 );
      when( meta.getFileTypeNr() ).thenReturn( TextFileInputMeta.FILE_TYPE_FIXED );
      when( mockFileList.getFile( 0 ) ).thenReturn( mockFileObject );
      meta.inputFields = new BaseFileField[] { field( "field1" ) };

      response = input.doAction( "getFields", meta, mockTransMeta, mockTrans, queryMap );
      assertEquals( StepInterface.SUCCESS_RESPONSE, response.get( StepInterface.ACTION_STATUS ) );

      when( meta.getFileTypeNr() ).thenReturn( TextFileInputMeta.FILE_TYPE_CSV );
      response = input.doAction( "getFields", meta, mockTransMeta, mockTrans, queryMap );
      assertEquals( StepInterface.SUCCESS_RESPONSE, response.get( StepInterface.ACTION_STATUS ) );

      // getFieldNames test cases
      response = input.doAction( "getFieldNames", meta, mockTransMeta, mockTrans, queryMap );
      assertEquals( StepInterface.SUCCESS_RESPONSE, response.get( StepInterface.ACTION_STATUS ) );

      // showFiles test cases
      String[] fileNames = new String[] { TEST_TXT };
      doReturn( fileNames ).when( mockTransMeta ).environmentSubstitute( (String[]) any() );
      BaseFileInputFiles inputFiles = mock( BaseFileInputFiles.class );
      meta.inputFiles = inputFiles;
      inputFiles.fileRequired = new String[] { "N" };
      when( inputFiles.includeSubFolderBoolean() ).thenReturn( new boolean[] { false } );
      IKettleVFS mockKettle = mock( IKettleVFS.class );
      FileName mockFileName = mock( FileName.class );
      when( mockFileName.getURI() ).thenReturn( TEST_TXT );
      kettleVFSMockedStatic.when( () -> KettleVFS.getInstance( DefaultBowl.getInstance() ) )
        .thenReturn( mockKettle );
      when( mockTransMeta.getBowl() ).thenReturn( DefaultBowl.getInstance() );
      when( mockFileObject.exists() ).thenReturn( true );
      when( mockFileObject.isReadable() ).thenReturn( true );
      when( mockFileObject.getName() ).thenReturn( mockFileName );
      when( mockKettle.getFileObject( TEST_TXT, mockTransMeta ) ).thenReturn( mockFileObject );

      response = input.doAction( "showFiles", meta, mockTransMeta, mockTrans, queryMap );
      assertEquals( StepInterface.SUCCESS_RESPONSE, response.get( StepInterface.ACTION_STATUS ) );

      // validateShowContent test cases
      when( mockFileList.nrOfFiles() ).thenReturn( 1 );
      response = input.doAction( "validateShowContent", meta, mockTransMeta, mockTrans, queryMap );
      assertEquals( StepInterface.SUCCESS_RESPONSE, response.get( StepInterface.ACTION_STATUS ) );

      when( mockFileList.nrOfFiles() ).thenReturn( 0 );
      response = input.doAction( "validateShowContent", meta, mockTransMeta, mockTrans, queryMap );
      assertEquals( StepInterface.SUCCESS_RESPONSE, response.get( StepInterface.ACTION_STATUS ) );

      // showContent test cases
      response = input.doAction( "showContent", meta, mockTransMeta, mockTrans, queryMap );
      assertEquals( StepInterface.SUCCESS_RESPONSE, response.get( StepInterface.ACTION_STATUS ) );

      textFileInputUtilsMockedStatic.close();
      compressionProviderFactoryMockedStatic.close();
    }
  }

  @Test
  @SuppressWarnings( "java:S1874" )
  // TextFileInput uses deprecated class FileInputList.createFilePathList to create file path
  public void testGetFieldsAction() throws Exception {
    List<ValueMetaInterface> valueMetaList = new ArrayList<>();
    valueMetaList.add( ValueMetaFactory.createValueMeta( "field1", ValueMetaInterface.TYPE_STRING ) );
    valueMetaList.add( ValueMetaFactory.createValueMeta( "field2", ValueMetaInterface.TYPE_NUMBER ) );
    BaseFileField[] fields = new BaseFileField[ 1 ];
    fields[ 0 ] = new BaseFileField( "field1", 1, 1 );
    String sampleData = "1,name,3.14159,city,1954/02/07,145.00,ALASKA";
    String[] fieldsData = { "field1", "field2", "field3", "field4", "field5", "field6", "field7" };
    TextFileLine textFileLine = new TextFileLine( sampleData, 1, null );
    Map<String, String> queryParams = new HashMap<>();
    queryParams.put( "noOfFields", "10" );
    queryParams.put( "isSampleSummary", "true" );

    TextFileInput input = StepMockUtil.getStep( TextFileInput.class, TextFileInputMeta.class, "test" );
    TextFileInputMeta meta = mock( TextFileInputMeta.class );
    CsvInputAwareStep csvInputAwareStep = mock( CsvInputAwareStep.class );
    TransMeta mockTransMeta = input.getTransMeta();
    Trans mockTrans = input.getTrans();

    RowMetaInterface outputRowMeta = Mockito.mock( RowMeta.class );
    InputStream mockInputStream = mock( InputStream.class );
    InputStreamReader inputStreamReader =
      new InputStreamReader( new ByteArrayInputStream( sampleData.getBytes() ) );
    BufferedInputStreamReader bufferedInputStreamReader = new BufferedInputStreamReader( inputStreamReader );
    FileObject mockFileObject = mock( FileObject.class );
    CompressionProviderFactory mockCPFactory = mock( CompressionProviderFactory.class );
    CompressionProvider mockCP = mock( CompressionProvider.class );
    CompressionInputStream mockCStream = mock( CompressionInputStream.class );
    FileInputList mockFileList = mock( FileInputList.class );
    BaseFileInputFiles inputFiles = mock( BaseFileInputFiles.class );
    TextFileInputMeta.Content mockContent = mock( TextFileInputMeta.Content.class );

    when( meta.getInputFields() ).thenReturn( fields );
    when( meta.getHeaderFileObject( any() ) ).thenReturn( mockFileObject );
    when( meta.clone() ).thenReturn( meta );
    when( meta.getFileInputList( any(), any() ) ).thenReturn( mockFileList );
    when( meta.getFileTypeNr() ).thenReturn( TextFileInputMeta.FILE_TYPE_CSV );
    when( outputRowMeta.getValueMetaList() ).thenReturn( valueMetaList );
    when( mockCPFactory.createCompressionProviderInstance( any() ) ).thenReturn( mockCP );
    when( mockCP.createInputStream( any( InputStream.class ) ) ).thenReturn( mockCStream );
    when( mockFileList.nrOfFiles() ).thenReturn( 1 );
    when( mockFileList.getFile( 0 ) ).thenReturn( mockFileObject );
    when( csvInputAwareStep.getFieldNames( any( CsvInputAwareMeta.class ) ) ).thenReturn( fieldsData );
    when(
      csvInputAwareStep.getBufferedReader( any( CsvInputAwareMeta.class ), any( InputStream.class ) ) ).thenReturn(
      bufferedInputStreamReader );
    when( mockTransMeta.environmentSubstitute( anyString() ) ).thenAnswer(
      invocation -> invocation.getArgument( 0 ) );

    meta.content = mockContent;
    meta.setDefault();
    meta.inputFields = new BaseFileField[] { field( "field1" ) };
    meta.inputFiles = inputFiles;
    meta.inputFiles.fileName = new String[] { "test.csv" };
    meta.inputFiles.fileMask = new String[] { "" };
    meta.inputFiles.excludeFileMask = new String[] { "" };
    meta.inputFiles.fileRequired = new String[] { "Y" };
    meta.inputFiles.includeSubFolders = new String[] { "false" };

    try ( MockedStatic<KettleVFS> kettleVFSMockedStatic = mockStatic( KettleVFS.class );
          MockedStatic<FileInputList> fileInputListMock = mockStatic( FileInputList.class );
          MockedStatic<TextFileInputUtils> textFileInputUtilsMockedStatic = mockStatic( TextFileInputUtils.class );
          MockedStatic<CompressionProviderFactory> compressionProviderFactoryMockedStatic = mockStatic(
            CompressionProviderFactory.class ) ) {

      compressionProviderFactoryMockedStatic.when( () -> CompressionProviderFactory.getInstance() )
        .thenReturn( mockCPFactory );
      kettleVFSMockedStatic.when( () -> KettleVFS.getInputStream( any( FileObject.class ) ) )
        .thenReturn( mockInputStream );
      fileInputListMock.when(
        () -> FileInputList.createFilePathList( any( VariableSpace.class ), any(), any(), any(), any(), any()
        ) ).thenReturn( new String[] { "test.csv" } );
      textFileInputUtilsMockedStatic.when(
          () -> TextFileInputUtils.getLine( any(), any( BufferedInputStreamReader.class ), any(), anyInt(), any(),
            any(),
            any() ) )
        .thenReturn( sampleData );
      textFileInputUtilsMockedStatic.when(
          () -> TextFileInputUtils.getLine( any(), any( BufferedInputStreamReader.class ), any(), anyInt(), any(),
            any(),
            any(), anyLong() ) )
        .thenReturn( textFileLine );
      textFileInputUtilsMockedStatic.when(
          () -> TextFileInputUtils.guessStringsFromLine( isNull(), any(), anyString(), any(), isNull(), isNull(),
            isNull() ) )
        .thenReturn( fieldsData );
      textFileInputUtilsMockedStatic.when( () -> TextFileInputUtils.convertLineToRow(
          any( LogChannelInterface.class ), any( TextFileLine.class ), any( TextFileInputMeta.class ), isNull(),
          eq( 0 ),
          any( RowMetaInterface.class ), any( RowMetaInterface.class ), anyString(), anyLong(),
          isNull(), isNull(), isNull(), isNull(), any(), isNull(), isNull(), eq( false ), isNull(), isNull(), isNull(),
          isNull(), isNull(), anyBoolean()
        ) ).thenReturn( new Object[] { "1", "name", "3.14159", "city", "1954/02/07", "145.00", "ALASKA" } )
        .thenReturn( null );

      JSONObject response = input.doAction( "getFields", meta, mockTransMeta, mockTrans, queryParams );

      assertEquals( StepInterface.SUCCESS_RESPONSE, response.get( StepInterface.ACTION_STATUS ) );
      assertNotNull( response.get( "fields" ) );
      assertNotNull( response.get( "summary" ) );
    }
  }

}
