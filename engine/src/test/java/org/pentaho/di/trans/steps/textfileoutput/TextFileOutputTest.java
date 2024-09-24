/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2024 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *******************************************************************************/

package org.pentaho.di.trans.steps.textfileoutput;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
//import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs2.FileObject;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.bowl.DefaultBowl;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.RowSet;
import org.pentaho.di.core.compress.CompressionOutputStream;
import org.pentaho.di.core.compress.CompressionPluginType;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaBase;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.mock.StepMockHelper;
import org.pentaho.di.utils.TestUtils;

/**
 * User: Dzmitry Stsiapanau Date: 10/18/13 Time: 2:23 PM
 */
public class TextFileOutputTest {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  private static final String EMPTY_FILE_NAME = "Empty File";
  private static final Boolean[] BOOL_VALUE_LIST = new Boolean[] { false, true };
  private static final String TEXT_FILE_OUTPUT_PREFIX = "textFileOutput";
  private static final String TEXT_FILE_OUTPUT_EXTENSION = ".txt";
  private static final String END_LINE = " endLine ";
  private static final String RESULT_ROWS = "\"some data\" \"another data\"\n" + "\"some data2\" \"another data2\"\n";
  private static final String TEST_PREVIOUS_DATA = "testPreviousData\n";

  private StepMockHelper<TextFileOutputMeta, TextFileOutputData> stepMockHelper;
  private static final TextFileField textFileField =
    new TextFileField( "Name", 2, Const.EMPTY_STRING, 10, 20, Const.EMPTY_STRING, Const.EMPTY_STRING,
      Const.EMPTY_STRING, Const.EMPTY_STRING );
  private static final TextFileField textFileField2 =
    new TextFileField( "Surname", 2, Const.EMPTY_STRING, 10, 20, Const.EMPTY_STRING, Const.EMPTY_STRING,
      Const.EMPTY_STRING, Const.EMPTY_STRING );
  private final TextFileField[] textFileFields = new TextFileField[] { textFileField, textFileField2 };
  private static final Object[] firstRow = new Object[] { "some data", "another data" };
  private static final Object[] secondRow = new Object[] { "some data2", "another data2" };
  private final List<Object[]> emptyRows = new ArrayList<>();
  private final List<Object[]> rows = new ArrayList<>();
  private final List<String> contents = new ArrayList<>();
  private TextFileOutput textFileOutput;

  {
    rows.add( firstRow );
    rows.add( secondRow );

    contents.add( Const.EMPTY_STRING );
    contents.add( Const.EMPTY_STRING );
    contents.add( END_LINE );
    contents.add( END_LINE );
    contents.add( null );
    contents.add( null );
    contents.add( END_LINE );
    contents.add( END_LINE );
    contents.add( RESULT_ROWS );
    contents.add( RESULT_ROWS );
    contents.add( RESULT_ROWS + END_LINE );
    contents.add( RESULT_ROWS + END_LINE );
    contents.add( RESULT_ROWS );
    contents.add( RESULT_ROWS );
    contents.add( RESULT_ROWS + END_LINE );
    contents.add( RESULT_ROWS + END_LINE );
    contents.add( Const.EMPTY_STRING );
    contents.add( TEST_PREVIOUS_DATA );
    contents.add( END_LINE );
    contents.add( TEST_PREVIOUS_DATA + END_LINE );
    contents.add( TEST_PREVIOUS_DATA );
    contents.add( TEST_PREVIOUS_DATA );
    contents.add( END_LINE );
    contents.add( TEST_PREVIOUS_DATA + END_LINE );
    contents.add( RESULT_ROWS );
    contents.add( TEST_PREVIOUS_DATA + RESULT_ROWS );
    contents.add( RESULT_ROWS + END_LINE );
    contents.add( TEST_PREVIOUS_DATA + RESULT_ROWS + END_LINE );
    contents.add( RESULT_ROWS );
    contents.add( TEST_PREVIOUS_DATA + RESULT_ROWS );
    contents.add( RESULT_ROWS + END_LINE );
    contents.add( TEST_PREVIOUS_DATA + RESULT_ROWS + END_LINE );
  }

  protected static class TextFileOutputTestHandler extends TextFileOutput {
    public List<Throwable> errors = new ArrayList<>();
    private Object[] row;

    TextFileOutputTestHandler( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr,
                               TransMeta transMeta, Trans trans ) {
      super( stepMeta, stepDataInterface, copyNr, transMeta, trans );
    }

    public void setRow( Object[] row ) {
      this.row = row;
    }

    @Override public String buildFilename( String filename, boolean ziparchive ) {
      return filename;
    }

    @Override
    public Object[] getRow() throws KettleException {
      return row;
    }

    @Override
    public void putRow( RowMetaInterface rowMeta, Object[] row ) throws KettleStepException {

    }

    @Override
    public void logError( String message ) {
      errors.add( new KettleException( message ) );
    }

    @Override
    public void logError( String message, Throwable thr ) {
      errors.add( thr );
    }

    @Override
    public void logError( String message, Object... arguments ) {
      errors.add( new KettleException( message ) );
    }
  }

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    PluginRegistry.addPluginType( CompressionPluginType.getInstance() );
    PluginRegistry.init( false );
  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {
    FileUtils.deleteQuietly( Paths.get( TEXT_FILE_OUTPUT_PREFIX + TEXT_FILE_OUTPUT_EXTENSION ).toFile() );
  }

  @Before
  public void setUp() throws Exception {
    stepMockHelper =
      new StepMockHelper<>( "TEXT FILE OUTPUT TEST", TextFileOutputMeta.class, TextFileOutputData.class );
    when( stepMockHelper.logChannelInterfaceFactory.create( any(), any( LoggingObjectInterface.class ) ) ).thenReturn(
        stepMockHelper.logChannelInterface );
    verify( stepMockHelper.logChannelInterface, never() ).logError( anyString() );
    verify( stepMockHelper.logChannelInterface, never() ).logError( anyString(), any( Object[].class ) );
    verify( stepMockHelper.logChannelInterface, never() ).logError( anyString(), any( Throwable.class ) );
    when( stepMockHelper.trans.isRunning() ).thenReturn( true );
    verify( stepMockHelper.trans, never() ).stopAll();
    when( stepMockHelper.processRowsStepMetaInterface.getSeparator() ).thenReturn( " " );
    when( stepMockHelper.processRowsStepMetaInterface.getEnclosure() ).thenReturn( "\"" );
    when( stepMockHelper.processRowsStepMetaInterface.getNewline() ).thenReturn( "\n" );
    when( stepMockHelper.transMeta.listVariables() ).thenReturn( new String[0] );
    when( stepMockHelper.transMeta.getBowl() ).thenReturn( DefaultBowl.getInstance() );
  }

  @After
  public void tearDown() throws Exception {
    stepMockHelper.cleanUp();
  }

  @Test
  public void testCloseFileDataOutIsNullCase() {
    textFileOutput =
        new TextFileOutput( stepMockHelper.stepMeta, stepMockHelper.stepDataInterface, 0, stepMockHelper.transMeta,
            stepMockHelper.trans );
    textFileOutput.data = mock( TextFileOutputData.class );

    assertNull( textFileOutput.data.out );
    textFileOutput.closeFile();
  }

  @Test
  public void testCloseFileDataOutIsNotNullCase() {
    textFileOutput =
        new TextFileOutput( stepMockHelper.stepMeta, stepMockHelper.stepDataInterface, 0, stepMockHelper.transMeta,
            stepMockHelper.trans );
    textFileOutput.data = mock( TextFileOutputData.class );
    textFileOutput.data.out = mock( CompressionOutputStream.class );

    textFileOutput.closeFile();
    assertNull( textFileOutput.data.out );
  }

  private FileObject createTemplateFile() {
    String path =
        TestUtils.createRamFile( getClass().getSimpleName() + "/" + TEXT_FILE_OUTPUT_PREFIX + new Random().nextLong()
            + TEXT_FILE_OUTPUT_EXTENSION, stepMockHelper.transMeta );
    return TestUtils.getFileObject( path, stepMockHelper.transMeta );
  }

  private FileObject createTemplateFile( String content ) throws IOException {
    FileObject f2 = createTemplateFile();
    if ( content == null ) {
      f2.delete();
    } else {
      try ( OutputStreamWriter fw = new OutputStreamWriter( f2.getContent().getOutputStream() ) ) {
        fw.write( content );
      }
    }
    return f2;
  }

  @Test
  public void testsIterate() {
    FileObject resultFile = null;
    FileObject contentFile;
    String content = null;
    int i = 0;
    for ( Boolean fileExists : BOOL_VALUE_LIST ) {
      for ( Boolean dataReceived : BOOL_VALUE_LIST ) {
        for ( Boolean isDoNotOpenNewFileInit : BOOL_VALUE_LIST ) {
          for ( Boolean endLineExists : BOOL_VALUE_LIST ) {
            for ( Boolean append : BOOL_VALUE_LIST ) {
              try {
                resultFile = helpTestInit( fileExists, dataReceived, isDoNotOpenNewFileInit, endLineExists, append );
                content = (String) contents.toArray()[i++];
                contentFile = createTemplateFile( content );
                if ( resultFile.exists() ) {
                  assertTrue( IOUtils.contentEquals( resultFile.getContent().getInputStream(), contentFile.getContent()
                      .getInputStream() ) );
                } else {
                  assertFalse( contentFile.exists() );
                }
              } catch ( Exception e ) {
                fail( e.getMessage() + "\n FileExists = " + fileExists + "\n DataReceived = " + dataReceived
                    + "\n isDoNotOpenNewFileInit = " + isDoNotOpenNewFileInit + "\n EndLineExists = " + endLineExists
                    + "\n Append = " + append + "\n Content = " + ( content != null ? content : "<null>" )
                    + "\n resultFile = " + ( resultFile != null ? resultFile : "<null>" ) );
              }
            }
          }
        }
      }
    }
  }

  /**
   * Tests the RULE#1: If 'Do not create file at start' checkbox is cheked AND 'Add landing line of file' is NOT set AND
   * transformation does not pass any rows to the file input step, then NO output file should be created.
   */
  @Test
  public void testNoOpenFileCall_IfRule_1() throws Exception {

    TextFileField tfFieldMock = mock( TextFileField.class );
    TextFileField[] textFileFields = { tfFieldMock };

    when( stepMockHelper.initStepMetaInterface.getEndedLine() ).thenReturn( Const.EMPTY_STRING );
    when( stepMockHelper.initStepMetaInterface.getOutputFields() ).thenReturn( textFileFields );
    when( stepMockHelper.initStepMetaInterface.isDoNotOpenNewFileInit() ).thenReturn( true );

    when( stepMockHelper.processRowsStepMetaInterface.getEndedLine() ).thenReturn( Const.EMPTY_STRING );
    when( stepMockHelper.processRowsStepMetaInterface.getFileName() ).thenReturn( EMPTY_FILE_NAME );
    when( stepMockHelper.processRowsStepMetaInterface.isDoNotOpenNewFileInit() ).thenReturn( true );
    when( stepMockHelper.processRowsStepMetaInterface.getOutputFields() ).thenReturn( textFileFields );

    textFileOutput =
        new TextFileOutput( stepMockHelper.stepMeta, stepMockHelper.stepDataInterface, 0, stepMockHelper.transMeta,
            stepMockHelper.trans );
    TextFileOutput textFileOutputSpy = spy( textFileOutput );
    doReturn( false ).when( textFileOutputSpy ).isWriteHeader( TEXT_FILE_OUTPUT_PREFIX + TEXT_FILE_OUTPUT_EXTENSION );
    doCallRealMethod().when( textFileOutputSpy ).initFileStreamWriter( EMPTY_FILE_NAME );
    doNothing().when( textFileOutputSpy ).flushOpenFiles( true );

    textFileOutputSpy.init( stepMockHelper.initStepMetaInterface, stepMockHelper.initStepDataInterface );

    when( stepMockHelper.processRowsStepMetaInterface.buildFilename( anyString(), anyString(),
      any( VariableSpace.class ), anyInt(), anyString(), anyInt(), anyBoolean(),
      any( TextFileOutputMeta.class ) ) ).
      thenReturn( TEXT_FILE_OUTPUT_PREFIX + TEXT_FILE_OUTPUT_EXTENSION );

    textFileOutputSpy.processRow( stepMockHelper.processRowsStepMetaInterface, stepMockHelper.initStepDataInterface );
    verify( textFileOutputSpy, never() ).initFileStreamWriter( EMPTY_FILE_NAME );
    verify( textFileOutputSpy, never() ).writeEndedLine();
    verify( textFileOutputSpy ).setOutputDone();
  }

  private FileObject helpTestInit( Boolean fileExists, Boolean dataReceived, Boolean isDoNotOpenNewFileInit,
      Boolean endLineExists, Boolean append ) throws Exception {

    FileObject f = createTemplateFile( fileExists ? TEST_PREVIOUS_DATA : null );
    List<Object[]> rows = dataReceived ? this.rows : this.emptyRows;
    String endLine = endLineExists ? END_LINE : null;

    List<Throwable> errors =
        doOutput( textFileFields, rows, f.getName().getURI(), endLine, false, isDoNotOpenNewFileInit, append );
    if ( !errors.isEmpty() ) {
      StringBuilder str = new StringBuilder();
      for ( Throwable thr : errors ) {
        str.append( thr );
      }
      fail( str.toString() );
    }

    return f;
  }

  private List<Throwable> doOutput( TextFileField[] textFileField, List<Object[]> rows, String pathToFile,
      String endedLine, Boolean isHeaderEnabled, Boolean isDoNotOpenNewFileInit, Boolean append)
        throws KettleException {
    TextFileOutputData textFileOutputData = new TextFileOutputData();
    TextFileOutputTestHandler textFileOutput =
      new TextFileOutputTestHandler( stepMockHelper.stepMeta, textFileOutputData, 0, stepMockHelper.transMeta,
        stepMockHelper.trans );

    // init step meta and process step meta should be the same in this case
    when( stepMockHelper.processRowsStepMetaInterface.isDoNotOpenNewFileInit() ).thenReturn( isDoNotOpenNewFileInit );
    when( stepMockHelper.processRowsStepMetaInterface.isFileAppended() ).thenReturn( append );

    when( stepMockHelper.processRowsStepMetaInterface.isHeaderEnabled() ).thenReturn( isHeaderEnabled );
    when( stepMockHelper.processRowsStepMetaInterface.getFileName() ).thenReturn( pathToFile );
    when( stepMockHelper.processRowsStepMetaInterface.buildFilename( anyString(), anyString(),
      any( VariableSpace.class ), anyInt(), anyString(), anyInt(), anyBoolean(),
      any( TextFileOutputMeta.class ) ) ).thenReturn( pathToFile );

    when( stepMockHelper.processRowsStepMetaInterface.getOutputFields() ).thenReturn( textFileField );

    textFileOutput.init( stepMockHelper.processRowsStepMetaInterface, textFileOutputData );

    // Process rows

    RowSet rowSet = stepMockHelper.getMockInputRowSet( rows );
    RowMetaInterface inputRowMeta = mock( RowMetaInterface.class );
    textFileOutput.setInputRowMeta( inputRowMeta );

//    when( rowSet.getRowWait( anyInt(), any( TimeUnit.class ) ) )
//      .thenReturn( rows.isEmpty() ? null : rows.iterator().next() );
    when( rowSet.getRowMeta() ).thenReturn( inputRowMeta );
    when( inputRowMeta.clone() ).thenReturn( inputRowMeta );

    for ( int i = 0; i < textFileField.length; i++ ) {
      String name = textFileField[i].getName();
      ValueMetaString valueMetaString = new ValueMetaString( name );
      when( inputRowMeta.getValueMeta( i ) ).thenReturn( valueMetaString );
      when( inputRowMeta.indexOfValue( name ) ).thenReturn( i );
    }

    textFileOutput.addRowSetToInputRowSets( rowSet );
    textFileOutput.addRowSetToOutputRowSets( rowSet );

    when( stepMockHelper.processRowsStepMetaInterface.getEndedLine() ).thenReturn( endedLine );
    when( stepMockHelper.processRowsStepMetaInterface.isFastDump() ).thenReturn( true );
    doCallRealMethod().when( stepMockHelper.processRowsStepMetaInterface )
      .calcMetaWithFieldOptions( any( TextFileOutputData.class ) );
    when( stepMockHelper.processRowsStepMetaInterface.getMetaWithFieldOptions() ).thenCallRealMethod();

    // This is necessary because the step meta code is being executed multiple times without being fully
    // re-initialized (in `testsIterate`), and the step code expects this field to be null when processing the first
    // row during execution.
    stepMockHelper.processRowsStepMetaInterface.metaWithFieldOptions = null;
    for ( Object[] row : rows ) {
      textFileOutput.setRow( row );
      textFileOutput.processRow( stepMockHelper.processRowsStepMetaInterface, textFileOutputData );
    }
    textFileOutput.setRow( null );
    textFileOutput.processRow( stepMockHelper.processRowsStepMetaInterface, textFileOutputData );
    textFileOutput.dispose( stepMockHelper.processRowsStepMetaInterface, textFileOutputData );
    return textFileOutput.errors;
  }

  @Test
  public void containsSeparatorOrEnclosureIsNotUnnecessaryInvoked_SomeFieldsFromMeta() {
    TextFileField field = new TextFileField();
    field.setName( "name" );
    assertNotInvokedTwice( field );
  }

  @Test
  public void containsSeparatorOrEnclosureIsNotUnnecessaryInvoked_AllFieldsFromMeta() {
    assertNotInvokedTwice( null );
  }

  @Test
  public void testEndedLineVar() throws Exception {
    TextFileOutputData data = new TextFileOutputData();
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    data.writer = baos;
    TextFileOutputMeta meta = new TextFileOutputMeta();
    meta.setEndedLine( "${endvar}" );
    meta.setDefault();
    meta.setEncoding( StandardCharsets.UTF_8.name() );
    stepMockHelper.stepMeta.setStepMetaInterface( meta );
    TextFileOutput textFileOutput =
      new TextFileOutputTestHandler( stepMockHelper.stepMeta, data, 0, stepMockHelper.transMeta,
        stepMockHelper.trans );
    textFileOutput.meta = meta;
    textFileOutput.data = data;
    textFileOutput.setVariable( "endvar", "this is the end" );
    textFileOutput.writeEndedLine();
    assertEquals( "this is the end", baos.toString( StandardCharsets.UTF_8.name() ) );
  }

  private void assertNotInvokedTwice( TextFileField field ) {
    TextFileOutput step =
        new TextFileOutput( stepMockHelper.stepMeta, stepMockHelper.stepDataInterface, 1, stepMockHelper.transMeta,
            stepMockHelper.trans );

    TextFileOutputMeta meta = new TextFileOutputMeta();
    meta.setEnclosureForced( false );
    meta.setEnclosureFixDisabled( false );
    step.meta = meta;

    TextFileOutputData data = new TextFileOutputData();
    data.binarySeparator = " ".getBytes();
    data.binaryEnclosure = "\"".getBytes();
    data.binaryNewline = "\n".getBytes();
    step.data = data;

    data.outputRowMeta = new RowMeta();
    data.outputRowMeta.addValueMeta( new ValueMetaString( "name" ) );

    data.writer = new ByteArrayOutputStream();

    if ( field != null ) {
      meta.setOutputFields( new TextFileField[] { field } );
    }

    step = spy( step );
    step.writeHeader();
    verify( step ).containsSeparatorOrEnclosure( any( byte[].class ), any( byte[].class ),
            any( byte[].class ) );
  }

  /**
   * PDI-15650
   * File Exists=N Flag Set=N Add Header=Y Append=Y
   * Result = File is created, header is written at top of file (this changed by the fix)
   */
  @Test
  public void testProcessRule_2() throws Exception {

    TextFileField tfFieldMock = mock( TextFileField.class );
    TextFileField[] textFileFields = { tfFieldMock };

    when( stepMockHelper.initStepMetaInterface.getEndedLine() ).thenReturn( Const.EMPTY_STRING );
    when( stepMockHelper.initStepMetaInterface.getOutputFields() ).thenReturn( textFileFields );
    when( stepMockHelper.initStepMetaInterface.isDoNotOpenNewFileInit() ).thenReturn( true );

    when( stepMockHelper.initStepDataInterface.getFileStreamsCollection() ).thenCallRealMethod();

    when( stepMockHelper.processRowsStepMetaInterface.getEndedLine() ).thenReturn( Const.EMPTY_STRING );
    when( stepMockHelper.processRowsStepMetaInterface.getFileName() ).thenReturn( TEXT_FILE_OUTPUT_PREFIX + TEXT_FILE_OUTPUT_EXTENSION );
    when( stepMockHelper.processRowsStepMetaInterface.isFileAppended() ).thenReturn( true );
    when( stepMockHelper.processRowsStepMetaInterface.isHeaderEnabled() ).thenReturn( true );
    when( stepMockHelper.processRowsStepMetaInterface.getOutputFields() ).thenReturn( textFileFields );
    when( stepMockHelper.processRowsStepMetaInterface.isDoNotOpenNewFileInit() ).thenReturn( true );
    when( stepMockHelper.processRowsStepMetaInterface.isFileNameInField() ).thenReturn( false );
    when( stepMockHelper.processRowsStepMetaInterface.isAddToResultFiles() ).thenReturn( true );

    Object[] rowData = new Object[] {"data text"};
    textFileOutput =
      new TextFileOutputTestHandler( stepMockHelper.stepMeta, stepMockHelper.stepDataInterface, 0,
        stepMockHelper.transMeta,
        stepMockHelper.trans );
    ( (TextFileOutputTestHandler) textFileOutput ).setRow( rowData );
    RowMetaInterface inputRowMeta = mock( RowMetaInterface.class );

    ValueMetaInterface valueMetaInterface = mock( ValueMetaInterface.class );
    when( valueMetaInterface.getString( any() ) ).thenReturn( TEXT_FILE_OUTPUT_PREFIX + TEXT_FILE_OUTPUT_EXTENSION );
    when( inputRowMeta.getValueMeta( anyInt() ) ).thenReturn( valueMetaInterface );
    when( inputRowMeta.clone() ).thenReturn( inputRowMeta );

    textFileOutput.setInputRowMeta( inputRowMeta );

    TextFileOutput textFileOutputSpy = spy( textFileOutput );
    doCallRealMethod().when( textFileOutputSpy ).initFileStreamWriter( TEXT_FILE_OUTPUT_PREFIX + TEXT_FILE_OUTPUT_EXTENSION );
    doNothing().when( textFileOutputSpy ).writeRow( inputRowMeta, rowData );
    doReturn( false ).when( textFileOutputSpy ).isFileExists( TEXT_FILE_OUTPUT_PREFIX + TEXT_FILE_OUTPUT_EXTENSION );
    doReturn( true ).when( textFileOutputSpy ).isWriteHeader( TEXT_FILE_OUTPUT_PREFIX + TEXT_FILE_OUTPUT_EXTENSION );
    textFileOutputSpy.init( stepMockHelper.processRowsStepMetaInterface, stepMockHelper.initStepDataInterface );
    when( stepMockHelper.processRowsStepMetaInterface.buildFilename( TEXT_FILE_OUTPUT_PREFIX + TEXT_FILE_OUTPUT_EXTENSION, null,
            textFileOutputSpy, 0, null, 0, true, stepMockHelper.processRowsStepMetaInterface ) ).
            thenReturn( TEXT_FILE_OUTPUT_PREFIX + TEXT_FILE_OUTPUT_EXTENSION );

    textFileOutputSpy.processRow( stepMockHelper.processRowsStepMetaInterface, stepMockHelper.initStepDataInterface );
    verify( textFileOutputSpy, times( 1 ) ).writeHeader(  );
    assertNotNull( textFileOutputSpy.getResultFiles() );
    assertEquals( 1, textFileOutputSpy.getResultFiles().size() );
  }

  /**
   * PDI-15650
   * File Exists=N Flag Set=N Add Header=Y Append=Y
   * Result = File is created, header is written at top of file (this changed by the fix)
   * with file name in stream
   */
  @Test
  public void testProcessRule_2FileNameInField() throws Exception {

    TextFileField tfFieldMock = mock( TextFileField.class );
    TextFileField[] textFileFields = { tfFieldMock };

    when( stepMockHelper.initStepMetaInterface.getEndedLine() ).thenReturn( Const.EMPTY_STRING );
    when( stepMockHelper.initStepMetaInterface.getOutputFields() ).thenReturn( textFileFields );
    when( stepMockHelper.initStepMetaInterface.isDoNotOpenNewFileInit() ).thenReturn( true );

    when( stepMockHelper.initStepDataInterface.getFileStreamsCollection() ).thenCallRealMethod();

    when( stepMockHelper.processRowsStepMetaInterface.getEndedLine() ).thenReturn( Const.EMPTY_STRING );
    when( stepMockHelper.processRowsStepMetaInterface.getFileName() ).thenReturn( TEXT_FILE_OUTPUT_PREFIX + TEXT_FILE_OUTPUT_EXTENSION );
    when( stepMockHelper.processRowsStepMetaInterface.isFileAppended() ).thenReturn( true );
    when( stepMockHelper.processRowsStepMetaInterface.isHeaderEnabled() ).thenReturn( true );
    when( stepMockHelper.processRowsStepMetaInterface.getOutputFields() ).thenReturn( textFileFields );
    when( stepMockHelper.processRowsStepMetaInterface.isDoNotOpenNewFileInit() ).thenReturn( true );
    when( stepMockHelper.processRowsStepMetaInterface.isAddToResultFiles() ).thenReturn( true );
    when( stepMockHelper.processRowsStepMetaInterface.isFileNameInField() ).thenReturn( true );

    Object[] rowData = new Object[] {"data text"};
    textFileOutput =
      new TextFileOutputTestHandler( stepMockHelper.stepMeta, stepMockHelper.stepDataInterface, 0,
        stepMockHelper.transMeta,
        stepMockHelper.trans );
    ( (TextFileOutputTestHandler) textFileOutput ).setRow( rowData );
    RowMetaInterface inputRowMeta = mock( RowMetaInterface.class );

    ValueMetaInterface valueMetaInterface = mock( ValueMetaInterface.class );
    when( valueMetaInterface.getString( any() ) ).thenReturn( TEXT_FILE_OUTPUT_PREFIX + TEXT_FILE_OUTPUT_EXTENSION );
    when( inputRowMeta.getValueMeta( anyInt() ) ).thenReturn( valueMetaInterface );
    when( inputRowMeta.clone() ).thenReturn( inputRowMeta );

    textFileOutput.setInputRowMeta( inputRowMeta );

    TextFileOutput textFileOutputSpy = spy( textFileOutput );
    doCallRealMethod().when( textFileOutputSpy ).initFileStreamWriter( TEXT_FILE_OUTPUT_PREFIX + TEXT_FILE_OUTPUT_EXTENSION );
    doReturn( false ).when( textFileOutputSpy ).isFileExists( TEXT_FILE_OUTPUT_PREFIX + TEXT_FILE_OUTPUT_EXTENSION );
    doReturn( true ).when( textFileOutputSpy ).isWriteHeader( TEXT_FILE_OUTPUT_PREFIX + TEXT_FILE_OUTPUT_EXTENSION );
    doNothing().when( textFileOutputSpy ).writeRow( inputRowMeta, rowData );
    textFileOutputSpy.init( stepMockHelper.processRowsStepMetaInterface, stepMockHelper.initStepDataInterface );
    when( stepMockHelper.processRowsStepMetaInterface.buildFilename( TEXT_FILE_OUTPUT_PREFIX + TEXT_FILE_OUTPUT_EXTENSION, null,
            textFileOutputSpy, 0, null, 0, true, stepMockHelper.processRowsStepMetaInterface ) ).
            thenReturn( TEXT_FILE_OUTPUT_PREFIX + TEXT_FILE_OUTPUT_EXTENSION );

    textFileOutputSpy.processRow( stepMockHelper.processRowsStepMetaInterface, stepMockHelper.initStepDataInterface );
    verify( textFileOutputSpy, times( 1 ) ).writeHeader();
    assertNotNull( textFileOutputSpy.getResultFiles() );
    assertEquals( 1, textFileOutputSpy.getResultFiles().size() );
  }

  /**
   * Test for PDI-13987
   */
  @Test
  public void testFastDumpDisableStreamEncodeTest() throws Exception {

    textFileOutput =
      new TextFileOutputTestHandler( stepMockHelper.stepMeta, stepMockHelper.stepDataInterface, 0,
        stepMockHelper.transMeta,
        stepMockHelper.trans );
    textFileOutput.meta = stepMockHelper.processRowsStepMetaInterface;

    String testString = "ÖÜä";
    String inputEncode = StandardCharsets.UTF_8.name();
    String outputEncode = StandardCharsets.ISO_8859_1.name();
    Object[] rows = { testString.getBytes( inputEncode ) };

    ValueMetaBase valueMetaInterface = new ValueMetaBase( "test", ValueMetaInterface.TYPE_STRING );
    valueMetaInterface.setStringEncoding( inputEncode );
    valueMetaInterface.setStorageType( ValueMetaInterface.STORAGE_TYPE_BINARY_STRING );
    valueMetaInterface.setStorageMetadata( new ValueMetaString() );

    TextFileOutputData data = new TextFileOutputData();
    data.binarySeparator = " ".getBytes();
    data.binaryEnclosure = "\"".getBytes();
    data.binaryNewline = "\n".getBytes();
    textFileOutput.data = data;

    RowMeta rowMeta = new RowMeta();
    rowMeta.addValueMeta( valueMetaInterface );

    doReturn( outputEncode ).when( stepMockHelper.processRowsStepMetaInterface ).getEncoding();
    textFileOutput.data.writer = mock( BufferedOutputStream.class );

    textFileOutput.writeRow( rowMeta, rows );
    verify( textFileOutput.data.writer ).write( testString.getBytes( outputEncode ) );
  }

  /**
   * Test for writeRowToFile not to call #initFileStreamWriter() if a variable is set.
   * Performance issue discovered, that previous implementation called #initFileStreamWriter() for every invocation of
   * #processRow() and thus #writeToFile().
   * see BACKLOG-28333 for more information.
   */
  @Test
  public void testWriteRowToFile_NoinitFileStreamWriter() throws Exception {

    // SETUP
    textFileOutput =
      new TextFileOutputTestHandler( stepMockHelper.stepMeta, stepMockHelper.stepDataInterface, 0,
        stepMockHelper.transMeta,
        stepMockHelper.trans );
    TextFileOutputMeta mockTFOMeta = mock( TextFileOutputMeta.class );
    when( mockTFOMeta.isServletOutput() ).thenReturn( false );
    Path tempDirWithPrefix = Files.createTempDirectory( "pdi-textFileOutputTest" );
    when( mockTFOMeta.getFileName() ).thenReturn( tempDirWithPrefix.toString() + "/wtf.txt" );
    textFileOutput.meta = mockTFOMeta;

    TextFileOutputData data = new TextFileOutputData();
    data.binarySeparator = " ".getBytes();
    data.binaryEnclosure = "\"".getBytes();
    data.binaryNewline = "\n".getBytes();
    textFileOutput.data = data;

    data.outputRowMeta = new RowMeta();
    data.outputRowMeta.addValueMeta( new ValueMetaString( "name" ) );

    OutputStream originalWriter = mock( BufferedOutputStream.class );

    // variable set
    textFileOutput.data.writer = originalWriter;

    // EXECUTE
    textFileOutput.writeRowTo( secondRow );

    // VERIFY
    assertEquals( originalWriter, textFileOutput.data.writer );
  }

  /**
   * Test for writeRowToFile not to call #initFileStreamWriter() if a variable is set.
   * Normal behavior, if not set, call #initFileStreamWriter.
   * see BACKLOG-28333 for more information.
   */
  @Test
  public void testWriteRowToFile_initFileStreamWriter() throws Exception {

    // SETUP
    textFileOutput =
      new TextFileOutputTestHandler( stepMockHelper.stepMeta, stepMockHelper.stepDataInterface, 0,
        stepMockHelper.transMeta,
        stepMockHelper.trans );

    TextFileOutputMeta mockTFOMeta = mock( TextFileOutputMeta.class );
    when( mockTFOMeta.isServletOutput() ).thenReturn( false );
    Path tempDirWithPrefix = Files.createTempDirectory( "pdi-textFileOutputTest" );
    when( mockTFOMeta.getFileName() ).thenReturn( tempDirWithPrefix.toString() + "/wtf.txt" );
    textFileOutput.meta = mockTFOMeta;

    TextFileOutputData data = new TextFileOutputData();
    data.binarySeparator = " ".getBytes();
    data.binaryEnclosure = "\"".getBytes();
    data.binaryNewline = "\n".getBytes();
    textFileOutput.data = data;

    data.outputRowMeta = new RowMeta();
    data.outputRowMeta.addValueMeta( new ValueMetaString( "name" ) );

    // variable not set
    textFileOutput.data.writer = null;

    // EXECUTE
    textFileOutput.writeRowTo( secondRow );

    // VERIFY
    assertNotNull( textFileOutput.data.writer );
  }

  /**
   * Test for writeRowToFile not to call #initServletStreamWriter() if a variable is set.
   * Performance issue discovered, that previous implementation called #initServletStreamWriter() for every invocation of
   * #processRow() and thus #writeToFile()
   * see BACKLOG-28333 for more information.
   */
  @Test
  public void testWriteRowToFile_NoinitServletStreamWriter() throws Exception {

    // SETUP
    textFileOutput =
      new TextFileOutputTestHandler( stepMockHelper.stepMeta, stepMockHelper.stepDataInterface, 0,
        stepMockHelper.transMeta,
        stepMockHelper.trans );
    TextFileOutputMeta mockTFOMeta = mock( TextFileOutputMeta.class );
    when( mockTFOMeta.isServletOutput() ).thenReturn( true );
    textFileOutput.meta = mockTFOMeta;

    TextFileOutputData data = new TextFileOutputData();
    data.binarySeparator = " ".getBytes();
    data.binaryEnclosure = "\"".getBytes();
    data.binaryNewline = "\n".getBytes();
    textFileOutput.data = data;

    data.outputRowMeta = new RowMeta();
    data.outputRowMeta.addValueMeta( new ValueMetaString( "name" ) );

    OutputStream originalWriter = mock( BufferedOutputStream.class );

    // variable set
    textFileOutput.data.writer = originalWriter;

    // EXECUTE
    textFileOutput.writeRowTo( secondRow );

    // VERIFY
    assertEquals( originalWriter, textFileOutput.data.writer );
  }

  /**
   * Test for writeRowToFile not to call #initFileStreamWriter() if a variable is set.
   * Normal behavior, if not set, call #initFileStreamWriter.
   * see BACKLOG-28333 for more information.
   */
  @Test
  public void testWriteRowToFile_initServletStreamWriter() throws Exception {

    // SETUP
    textFileOutput =
      new TextFileOutputTestHandler( stepMockHelper.stepMeta, stepMockHelper.stepDataInterface, 0,
        stepMockHelper.transMeta,
        stepMockHelper.trans );

    TextFileOutputMeta mockTFOMeta = mock( TextFileOutputMeta.class );
    when( mockTFOMeta.isServletOutput() ).thenReturn( true );
    textFileOutput.meta = mockTFOMeta;

    TextFileOutputData data = new TextFileOutputData();
    data.binarySeparator = " ".getBytes();
    data.binaryEnclosure = "enclosure".getBytes();
    data.binaryNewline = "\n".getBytes();
    textFileOutput.data = data;

    data.outputRowMeta = new RowMeta();
    data.outputRowMeta.addValueMeta( new ValueMetaString( "name" ) );

    when( textFileOutput.getTrans().getServletPrintWriter() ).thenReturn( mock( PrintWriter.class ) );

    // variable not set
    textFileOutput.data.writer = null;

    // EXECUTE
    textFileOutput.writeRowTo( secondRow );

    // VERIFY
    assertNotNull( textFileOutput.data.writer );
  }

  @Test
  public void testWriteEnclosedForValueMetaInterface() {
    TextFileOutputData data = new TextFileOutputData();
    data.binarySeparator = new byte[1];
    data.binaryEnclosure = new byte[1];
    data.writer = new ByteArrayOutputStream();
    TextFileOutputMeta meta = getTextFileOutputMeta();
    meta.setEnclosureFixDisabled(false);
    TextFileOutput textFileOutput = getTextFileOutput(data, meta);
    ValueMetaBase valueMetaInterface =getValueMetaInterface();
    assertFalse(textFileOutput.isWriteEnclosureForValueMetaInterface(valueMetaInterface));
  }

  @Test
  public void testWriteEnclosedForValueMetaInterfaceWithEnclosureForced() {
    TextFileOutputData data = new TextFileOutputData();
    data.binarySeparator = new byte[1];
    data.binaryEnclosure = new byte[1];
    data.writer = new ByteArrayOutputStream();
    TextFileOutputMeta meta = getTextFileOutputMeta();
    meta.setEnclosureForced(true);
    meta.setPadded(true);
    meta.setEnclosureFixDisabled(false);
    TextFileOutput textFileOutput = getTextFileOutput(data, meta);
    ValueMetaBase valueMetaInterface = getValueMetaInterface();
    assertTrue(textFileOutput.isWriteEnclosureForValueMetaInterface(valueMetaInterface));
  }

  @Test
  public void testWriteEnclosedForValueMetaInterfaceWithEnclosureFixDisabled() {
    TextFileOutputData data = new TextFileOutputData();
    data.binaryEnclosure = new byte[1];
    data.writer = new ByteArrayOutputStream();
    TextFileOutputMeta meta = getTextFileOutputMeta();
    meta.setEnclosureForced(false);
    meta.setEnclosureFixDisabled(true);
    TextFileOutput textFileOutput = getTextFileOutput(data, meta);
    ValueMetaBase valueMetaInterface = getValueMetaInterface();
    assertFalse(textFileOutput.isWriteEnclosureForValueMetaInterface(valueMetaInterface));
  }

  @Test
  public void testWriteEnclosedForValueMetaInterfaceWithEnclosureForcedAndEnclosureFixDisabled() {
    TextFileOutputData data = new TextFileOutputData();
    data.binaryEnclosure = new byte[]{101};
    data.binarySeparator = new byte[]{101};
    data.writer = new ByteArrayOutputStream();
    TextFileOutputMeta meta = getTextFileOutputMeta();
    meta.setEnclosureForced(false);
    meta.setEnclosureFixDisabled(false);
    TextFileOutput textFileOutput = getTextFileOutput(data, meta);
    ValueMetaBase valueMetaInterface = getValueMetaInterface();
    assertTrue(textFileOutput.isWriteEnclosureForValueMetaInterface(valueMetaInterface));
  }

  @Test
  public void testWriteEnclosureForFieldName() {
    TextFileOutputData data = new TextFileOutputData();
    data.binarySeparator = new byte[1];
    data.binaryEnclosure = new byte[1];
    data.writer = new ByteArrayOutputStream();
    TextFileOutputMeta meta = getTextFileOutputMeta();
    stepMockHelper.stepMeta.setStepMetaInterface( meta );
    TextFileOutput textFileOutput = getTextFileOutput(data, meta);
    ValueMetaBase valueMetaInterface = getValueMetaInterface();
    assertFalse(textFileOutput.isWriteEnclosureForFieldName(valueMetaInterface, "fieldName"));
  }

  @Test
  public void testWriteEnclosureForFieldNameWithEnclosureForced() {
    TextFileOutputData data = new TextFileOutputData();
    data.binarySeparator = new byte[1];
    data.binaryEnclosure = new byte[1];
    data.writer = new ByteArrayOutputStream();
    TextFileOutputMeta meta = getTextFileOutputMeta();
    meta.setEnclosureForced(true);
    meta.setEnclosureFixDisabled(false);
    stepMockHelper.stepMeta.setStepMetaInterface( meta );
    TextFileOutput textFileOutput = getTextFileOutput(data, meta);
    ValueMetaBase valueMetaInterface = getValueMetaInterface();
    assertTrue(textFileOutput.isWriteEnclosureForFieldName(valueMetaInterface, "fieldName"));
  }

  @Test
  public void testWriteEnclosureForFieldNameWithoutEnclosureFixDisabled() {
    TextFileOutputData data = new TextFileOutputData();
    data.binarySeparator = new byte[1];
    data.binaryEnclosure = new byte[1];
    data.writer = new ByteArrayOutputStream();
    TextFileOutputMeta meta = getTextFileOutputMeta();
    meta.setEnclosureForced(false);
    meta.setEnclosureFixDisabled(true);
    stepMockHelper.stepMeta.setStepMetaInterface( meta );
    TextFileOutput textFileOutput = getTextFileOutput(data, meta);
    ValueMetaBase valueMetaInterface = getValueMetaInterface();
    assertFalse(textFileOutput.isWriteEnclosureForFieldName(valueMetaInterface, "fieldName"));
  }

  @Test
  public void testWriteEnclosureForFieldNameWithEnclosureFixDisabled() {
    TextFileOutputData data = new TextFileOutputData();
    data.binarySeparator = new byte[]{102};
    data.binaryEnclosure = new byte[]{102};
    data.writer = new ByteArrayOutputStream();
    TextFileOutputMeta meta = getTextFileOutputMeta();
    meta.setEnclosureForced(false);
    meta.setEnclosureFixDisabled(false);
    stepMockHelper.stepMeta.setStepMetaInterface( meta );
    TextFileOutput textFileOutput = getTextFileOutput(data, meta);
    ValueMetaBase valueMetaInterface = getValueMetaInterface();
    assertTrue(textFileOutput.isWriteEnclosureForFieldName(valueMetaInterface, "fieldName"));
  }

  @Test
  public void testWriteEnclosed() {
    TextFileOutputData data = new TextFileOutputData();
    data.binaryEnclosure = new byte[1];
    data.writer = new ByteArrayOutputStream();
    TextFileOutputMeta meta = getTextFileOutputMeta();
    meta.setEnclosureForced(true);
    stepMockHelper.stepMeta.setStepMetaInterface( meta );
    TextFileOutput textFileOutput = getTextFileOutput(data, meta);
    ValueMetaBase valueMetaInterface = getValueMetaInterface();
    valueMetaInterface.setType(ValueMetaInterface.TYPE_NUMBER);
    assertFalse(textFileOutput.isWriteEnclosed(valueMetaInterface));
    assertFalse(textFileOutput.isWriteEnclosed(null));
  }

  @Test
  public void testWriteEnclosureForWriteFieldWithSeparator() {
    TextFileOutputData data = new TextFileOutputData();
    data.binarySeparator = new byte[1];
    data.binaryEnclosure = new byte[1];
    data.writer = new ByteArrayOutputStream();
    TextFileOutputMeta meta = getTextFileOutputMeta();
    stepMockHelper.stepMeta.setStepMetaInterface( meta );
    TextFileOutput textFileOutput = getTextFileOutput(data, meta);
    byte[] str = new byte[1];
    assertTrue(textFileOutput.isWriteEnclosureForWriteField(str));
  }

  @Test
  public void testWriteEnclosureForWriteFieldWithoutSeparator() {
    TextFileOutputData data = new TextFileOutputData();
    data.writer = new ByteArrayOutputStream();
    TextFileOutputMeta meta = getTextFileOutputMeta();
    stepMockHelper.stepMeta.setStepMetaInterface( meta );
    TextFileOutput textFileOutput = getTextFileOutput(data, meta);
    byte[] str = new byte[1];
    assertFalse(textFileOutput.isWriteEnclosureForWriteField(str));
  }

  @Test
  public void testWriteEnclosureForWriteFieldWithEnclosureForced() {
    TextFileOutputData data = new TextFileOutputData();
    data.writer = new ByteArrayOutputStream();
    data.binarySeparator = new byte[1];
    TextFileOutputMeta meta = getTextFileOutputMeta();
    meta.setEnclosureForced(true);
    meta.setPadded(true);
    meta.setEnclosureFixDisabled(true);
    stepMockHelper.stepMeta.setStepMetaInterface( meta );
    TextFileOutput textFileOutput = getTextFileOutput(data, meta);
    byte[] str = new byte[1];
    assertFalse(textFileOutput.isWriteEnclosureForWriteField(str));
  }

  public TextFileOutput getTextFileOutput(TextFileOutputData data,TextFileOutputMeta meta) {
    TextFileOutput textFileOutput =
            new TextFileOutputTestHandler( stepMockHelper.stepMeta, data, 0, stepMockHelper.transMeta,
                    stepMockHelper.trans );
    textFileOutput.meta = meta;
    textFileOutput.data = data;
    textFileOutput.setVariable( "endvar", "this is the end" );
    textFileOutput.writeEndedLine();
    textFileOutput.setVariable( "endvar", "this is the end" );
    textFileOutput.writeEndedLine();
    return textFileOutput;
  }

  public TextFileOutputMeta getTextFileOutputMeta() {
    TextFileOutputMeta meta = new TextFileOutputMeta();
    meta.setEndedLine( "${endvar}" );
    meta.setDefault();
    meta.setEnclosureForced(false);
    meta.setPadded(false);
    meta.setEncoding( StandardCharsets.UTF_8.name() );
    return meta;
  }

  public ValueMetaBase getValueMetaInterface() {
    ValueMetaBase valueMetaInterface = new ValueMetaBase( "test", ValueMetaInterface.TYPE_STRING );
    String inputEncode = StandardCharsets.UTF_8.name();
    valueMetaInterface.setStringEncoding( inputEncode );
    valueMetaInterface.setStorageType( ValueMetaInterface.STORAGE_TYPE_BINARY_STRING );
    valueMetaInterface.setStorageMetadata( new ValueMetaString() );
    return valueMetaInterface;
  }
}
