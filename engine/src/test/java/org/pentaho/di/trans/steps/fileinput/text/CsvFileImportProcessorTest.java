package org.pentaho.di.trans.steps.fileinput.text;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormatSymbols;
import java.util.Arrays;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LoggingObject;
import org.pentaho.di.core.logging.LoggingObjectType;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.steps.csvinput.CsvInputMeta;
import org.pentaho.di.trans.steps.textfileinput.TextFileInputField;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CsvFileImportProcessorTest {


  private CsvFileImportProcessor processor;
  private CsvInputMeta meta;
  private TransMeta transMetaMock;

  private static MockedStatic<TextFileInputUtils> textFileInputUtilsMockedStatic;
  private MockedConstruction<LogChannel> mockedLogChannel;

  @SuppressWarnings( "java:S1874" )// CsvInput uses deprecated class TextFileInput to store data
  TextFileInputField[] textFileInputFields;

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    KettleEnvironment.init();
  }

  @AfterClass
  public static void tearDown() {
    KettleEnvironment.reset();
  }

  @Before
  public void setUp() {
    String headerLine = "Integer,String,BigNumber,Boolean,Date,Number";
    String[] headerArray = Arrays.stream( headerLine.split( "," ) )
      .map( String::trim )
      .toArray( String[]::new );
    textFileInputFields = getTextFileInputFields( headerArray );
    String sampleLine1 = "1,name,3.14159,false,1954/02/07 12:00:00.000,145.00";
    String sampleLine2 = "2,レコード名1,1.7976E308,true,1991/11/19 12:00:00.000,13326523.33";
    TextFileLine textFileLine1 = new TextFileLine( sampleLine1, 1, null );
    TextFileLine textFileLine2 = new TextFileLine( sampleLine2, 1, null );
    String fileContent = headerLine + "\r\n" + sampleLine1 + "\r\n" + sampleLine2 + "\r\n";

    BufferedInputStreamReader reader = new BufferedInputStreamReader(
      new InputStreamReader( new ByteArrayInputStream( fileContent.getBytes( StandardCharsets.UTF_8 ) ) ) );

    LoggingObject loggingObject = mock( LoggingObject.class );

    transMetaMock = mock( TransMeta.class );
    meta = spy( createSampleMeta() );
    meta.setHeaderPresent( true );
    meta.setFileFormat( "CSV" );
    meta.setEnclosure( "'" );
    meta.setInputFields( textFileInputFields );


    textFileInputUtilsMockedStatic = mockStatic( TextFileInputUtils.class );
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

    mockedLogChannel = Mockito.mockConstruction( LogChannel.class, ( mockedLogChannel, context ) -> {
      when( loggingObject.getObjectType() ).thenReturn( LoggingObjectType.GENERAL );
      when( loggingObject.getObjectName() ).thenReturn( "Test" );
      when( loggingObject.getFilename() ).thenReturn( "filename" );
    } );

    when( transMetaMock.environmentSubstitute( anyString() ) ).thenAnswer(
      invocation -> invocation.getArgument( 0 ) );

    processor = new CsvFileImportProcessor( meta, transMetaMock, reader, 10, true );
  }

  @After
  public void cleanUp() {
    if ( !textFileInputUtilsMockedStatic.isClosed() ) {
      textFileInputUtilsMockedStatic.close();
    }
    if ( !mockedLogChannel.isClosed() ) {
      mockedLogChannel.close();
    }
  }

  @Test
  public void testGetFieldCount() {
    assertEquals( 6, processor.getFieldCount() );
  }

  @Test
  public void testGetFieldName() {
    assertEquals( "Integer", processor.getFieldName( textFileInputFields[ 0 ] ) );
  }

  @Test
  public void testGetFieldTypeDesc() {
    assertEquals( "Integer", processor.getFieldTypeDesc( textFileInputFields[ 0 ] ) );
    assertEquals( -1, processor.getFieldPrecision( textFileInputFields[ 0 ] ) );
  }

  @Test
  public void testHasHeader() {
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
    DecimalFormatSymbols dfs = new DecimalFormatSymbols();
    dfs.setCurrencySymbol( "$" );
    dfs.setDecimalSeparator( '.' );
    dfs.setGroupingSeparator( ',' );

    processor.initializeField( textFileInputFields[ 0 ], dfs );

    assertEquals( "$", textFileInputFields[ 0 ].getCurrencySymbol() );
    assertEquals( ".", textFileInputFields[ 0 ].getDecimalSymbol() );
    assertEquals( ",", textFileInputFields[ 0 ].getGroupSymbol() );
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

    response = processor.analyzeFile( false );
    assertNotNull( response );
  }

  CsvInputMeta createSampleMeta() {
    CsvInputMeta sampleMeta = new CsvInputMeta();
    sampleMeta.setDefault();
    sampleMeta.setBufferSize( "1024" );
    sampleMeta.setDelimiter( "," );
    sampleMeta.setEnclosure( "\"" );
    sampleMeta.setEncoding( "utf-8" );
    sampleMeta.setHeaderPresent( false );
    return sampleMeta;
  }

  @SuppressWarnings( "java:S1874" )// CsvInput uses deprecated class TextFileInput to store data
  private TextFileInputField[] getTextFileInputFields( String... names ) {
    return Arrays.stream( names )
      .map( this::createField )
      .toArray( TextFileInputField[]::new );
  }

  @SuppressWarnings( "java:S1874" )// CsvInput uses deprecated class TextFileInput to store data
  private TextFileInputField createField( String name ) {
    return new TextFileInputField() {{
      setName( name );
      setType( ValueMetaInterface.getTypeCode( name ) );
    }};
  }

}
