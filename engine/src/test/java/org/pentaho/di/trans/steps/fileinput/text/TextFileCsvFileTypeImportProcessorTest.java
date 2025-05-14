package org.pentaho.di.trans.steps.fileinput.text;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.vfs2.FileObject;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.compress.CompressionInputStream;
import org.pentaho.di.core.compress.CompressionProvider;
import org.pentaho.di.core.compress.CompressionProviderFactory;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.fileinput.FileInputList;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.logging.LoggingObject;
import org.pentaho.di.core.logging.LoggingObjectType;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.steps.file.BaseFileField;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TextFileCsvFileTypeImportProcessorTest {

  private TextFileCsvFileTypeImportProcessor processor;
  private TextFileInputMeta meta;
  private TransMeta transMetaMock;

  private static MockedStatic<FileInputList> fileInputListMockedStatic;
  private static MockedStatic<TextFileInputUtils> textFileInputUtilsMockedStatic;

  private MockedConstruction<LogChannel> mockedLogChannel;

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    KettleEnvironment.init();
  }

  @AfterClass
  public static void tearDown() {
    KettleEnvironment.reset();
  }

  @Before
  @SuppressWarnings( "java:S1874" )
  // TextFileInput uses deprecated class FileInputList.createFilePathList to create file path
  public void setUp() throws IOException {
    List<ValueMetaInterface> valueMetaList = new ArrayList<>();
    String headerLine = "Integer,String,BigNumber,Boolean,Date,Number";
    String[] headerArray = Arrays.stream( headerLine.split( "," ) )
      .map( String::trim )
      .toArray( String[]::new );
    BaseFileField[] baseFileFields = BaseFileFields( headerArray );
    String sampleLine1 = "1,name,3.14159,false,1954/02/07,145.00%";
    String sampleLine2 = "2,レコード名1,1.7976E308,true,1991/11/19,13326523.33%";
    String[] fieldsData = { "field1", "field2", "field3", "field4", "field5", "field6" };
    TextFileLine textFileLine1 = new TextFileLine( sampleLine1, 1, null );
    TextFileLine textFileLine2 = new TextFileLine( sampleLine2, 1, null );
    String fileContent = headerLine + "\r\n" + sampleLine1 + "\r\n" + sampleLine2 + "\r\n";

    BufferedInputStreamReader reader = new BufferedInputStreamReader(
      new InputStreamReader( new ByteArrayInputStream( fileContent.getBytes( StandardCharsets.UTF_8 ) ) ) );

    LoggingObject loggingObject = mock( LoggingObject.class );
    TextFileInputMeta.Content mockContent = mock( TextFileInputMeta.Content.class );
    RowMetaInterface outputRowMeta = Mockito.mock( RowMeta.class );
    FileObject mockFileObject = mock( FileObject.class );
    CompressionProviderFactory mockCPFactory = mock( CompressionProviderFactory.class );
    CompressionProvider mockCP = mock( CompressionProvider.class );
    CompressionInputStream mockCStream = mock( CompressionInputStream.class );
    FileInputList mockFileList = mock( FileInputList.class );

    transMetaMock = mock( TransMeta.class );
    meta = spy( createSampleMeta( baseFileFields ) );
    meta.content = mockContent;
    meta.content.fileType = "CSV";
    meta.content.header = true;

    fileInputListMockedStatic = mockStatic( FileInputList.class );
    textFileInputUtilsMockedStatic = mockStatic( TextFileInputUtils.class );

    mockedLogChannel = Mockito.mockConstruction( LogChannel.class, ( mockedLogChannel, context ) -> {
      when( loggingObject.getObjectType() ).thenReturn( LoggingObjectType.GENERAL );
      when( loggingObject.getObjectName() ).thenReturn( "Test" );
      when( loggingObject.getFilename() ).thenReturn( "filename" );
    } );

    when( meta.getInputFields() ).thenReturn( baseFileFields );
    when( meta.clone() ).thenReturn( meta );
    when( meta.getFileTypeNr() ).thenReturn( TextFileInputMeta.FILE_TYPE_CSV );
    when( outputRowMeta.getValueMetaList() ).thenReturn( valueMetaList );
    when( mockCPFactory.createCompressionProviderInstance( any() ) ).thenReturn( mockCP );
    when( mockCP.createInputStream( any( InputStream.class ) ) ).thenReturn( mockCStream );
    when( mockFileList.nrOfFiles() ).thenReturn( 1 );
    when( mockFileList.getFile( 0 ) ).thenReturn( mockFileObject );
    when( transMetaMock.environmentSubstitute( anyString() ) ).thenAnswer(
      invocation -> invocation.getArgument( 0 ) );

    fileInputListMockedStatic.when(
      () -> FileInputList.createFilePathList( any( VariableSpace.class ), any(), any(), any(), any(), any()
      ) ).thenReturn( new String[] { "test.csv" } );
    textFileInputUtilsMockedStatic.when(
        () -> TextFileInputUtils.getLine( any(), any( BufferedInputStreamReader.class ), any(), anyInt(), any(),
          any(),
          any() ) )
      .thenReturn( sampleLine1 ).thenReturn( sampleLine2 );
    textFileInputUtilsMockedStatic.when(
        () -> TextFileInputUtils.getLine( any(), any( BufferedInputStreamReader.class ), any(), anyInt(), any(),
          any(),
          any(), anyLong() ) )
      .thenReturn( textFileLine1 ).thenReturn( textFileLine2 );
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
      ) ).thenReturn( Arrays.stream( sampleLine1.split( "," ) ).toArray() )
      .thenReturn( Arrays.stream( sampleLine2.split( "," ) ).toArray() )
      .thenReturn( null );

    processor = new TextFileCsvFileTypeImportProcessor( meta, transMetaMock, reader, 10, true );
  }

  @After
  public void cleanUp() {
    if ( !fileInputListMockedStatic.isClosed() ) {
      fileInputListMockedStatic.close();
    }
    if ( !textFileInputUtilsMockedStatic.isClosed() ) {
      textFileInputUtilsMockedStatic.close();
    }
    if ( !mockedLogChannel.isClosed() ) {
      mockedLogChannel.close();
    }
  }

  @Test
  public void testGetFieldCount() {
    when( meta.getInputFields() ).thenReturn( new BaseFileField[ 5 ] );
    assertEquals( 6, processor.getFieldCount() );
  }

  @Test
  public void testGetFieldName() {
    BaseFileField field = new BaseFileField( "fieldName", -1, -1 );
    assertEquals( "fieldName", processor.getFieldName( field ) );
  }

  @Test
  public void testGetFieldTypeDesc() {
    BaseFileField field = new BaseFileField( "fieldName", -1, -1 );
    field.setType( ValueMetaInterface.TYPE_STRING );
    assertEquals( "String", processor.getFieldTypeDesc( field ) );
    assertEquals( -1, processor.getFieldPrecision( field ) );
  }

  @Test
  public void testHasHeader() {
    when( meta.hasHeader() ).thenReturn( true );
    assertTrue( processor.hasHeader() );
  }

  @Test
  public void testGetFields() throws KettleStepException {
    RowMetaInterface rowMetaMock = mock( RowMetaInterface.class );
    processor.getFields( rowMetaMock );
    verify( meta ).getFields( eq( rowMetaMock ), anyString(), any(), any(), eq( transMetaMock ), any(), any() );
  }

  @Test
  public void testInitializeField() {
    BaseFileField field = new BaseFileField( "fieldName", -1, -1 );
    DecimalFormatSymbols dfs = new DecimalFormatSymbols();
    dfs.setCurrencySymbol( "$" );
    dfs.setDecimalSeparator( '.' );
    dfs.setGroupingSeparator( ',' );

    processor.initializeField( field, dfs );

    assertEquals( "$", field.getCurrencySymbol() );
    assertEquals( ".", field.getDecimalSymbol() );
    assertEquals( ",", field.getGroupSymbol() );
  }

  @Test
  public void testCloneMeta() {
    when( meta.clone() ).thenReturn( meta );
    assertEquals( meta, processor.cloneMeta() );
  }

  @Test
  public void testAnalyzeFile() throws KettleException {
    String response = processor.analyzeFile( true );

    assertNotNull( response );
    assertNotNull( processor.getMessage() );
    assertNotNull( processor.getInputFieldsDto() );
    TextFileInputFieldDTO field = processor.getInputFieldsDto()[ 0 ];
    assertEquals( "Integer", field.getName() );
    assertEquals( "Integer", field.getType() );
    assertEquals( "#", field.getFormat() );
    assertEquals( "", field.getPosition() );
    assertEquals( "15", field.getLength() );
    assertEquals( "0", field.getPrecision() );
    assertNotNull( "$", field.getCurrency() );
    assertEquals( ".", field.getDecimal() );
    assertEquals( ",", field.getGroup() );
    assertEquals( "-", field.getNullif() );
    assertEquals( "", field.getIfnull() );
    assertEquals( "none", field.getTrimType() );
    assertEquals( "N", field.getRepeat() );

    response = processor.analyzeFile( false );
    assertNotNull( response );
  }

  private TextFileInputMeta createSampleMeta( BaseFileField[] baseFileFields ) {
    TextFileInputMeta sampleMeta = new TextFileInputMeta();
    sampleMeta.setDefault();
    sampleMeta.content.fileType = "CSV";
    sampleMeta.content.separator = ",";
    sampleMeta.content.enclosure = "\"";
    sampleMeta.content.header = true;
    sampleMeta.content.nrHeaderLines = 1;
    sampleMeta.inputFiles.fileName = new String[] { "sample.csv" };
    sampleMeta.inputFiles.fileMask = new String[] { "" };
    sampleMeta.inputFiles.excludeFileMask = new String[] { "" };
    sampleMeta.inputFiles.fileRequired = new String[] { "Y" };
    sampleMeta.inputFiles.includeSubFolders = new String[] { "N" };
    sampleMeta.inputFields = baseFileFields;

    return sampleMeta;
  }

  private BaseFileField[] BaseFileFields( String... names ) {
    BaseFileField[] fields = new BaseFileField[ names.length ];
    for ( int i = 0; i < names.length; i++ ) {
      fields[ i ] = createBaseFileField( names[ i ] );
    }
    return fields;
  }

  private BaseFileField createBaseFileField( String name ) {
    BaseFileField field = new BaseFileField();
    field.setName( name );
    field.setType( ValueMetaInterface.getTypeCode( name ) );
    return field;
  }

}
