/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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
 ******************************************************************************/

package org.pentaho.di.trans.steps.textfileoutput;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs2.FileObject;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
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

  /**
   *
   */
  private static final String EMPTY_FILE_NAME = "Empty File";
  /**
   *
   */
  private static final String EMPTY_STRING = "";

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    PluginRegistry.addPluginType( CompressionPluginType.getInstance() );
    PluginRegistry.init( true );
  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {
    FileUtils.deleteQuietly( Paths.get( TEXT_FILE_OUTPUT_PREFIX + TEXT_FILE_OUTPUT_EXTENSION ).toFile() );
  }

  private class TextFileOutputTestHandler extends TextFileOutput {
    public List<Throwable> errors = new ArrayList<Throwable>();
    private Object[] row;

    public TextFileOutputTestHandler( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr,
        TransMeta transMeta, Trans trans ) {
      super( stepMeta, stepDataInterface, copyNr, transMeta, trans );
    }

    public void setRow( Object[] row ) {
      this.row = row;
    }

    @Override
    public Object[] getRow() throws KettleException {
      return row;
    }

    @Override
    public boolean checkPreviouslyOpened( String filename ) {
      return false;
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

  private static final String TEXT_FILE_OUTPUT_PREFIX = "textFileOutput";
  private static final String TEXT_FILE_OUTPUT_EXTENSION = ".txt";
  private static final String END_LINE = " endLine ";
  private static final String RESULT_ROWS = "\"some data\" \"another data\"\n" + "\"some data2\" \"another data2\"\n";
  private static final String TEST_PREVIOUS_DATA = "testPreviousData\n";

  private StepMockHelper<TextFileOutputMeta, TextFileOutputData> stepMockHelper;
  private TextFileField textFileField =
      new TextFileField( "Name", 2, EMPTY_STRING, 10, 20, EMPTY_STRING, EMPTY_STRING, EMPTY_STRING, EMPTY_STRING );
  private TextFileField textFileField2 =
      new TextFileField( "Surname", 2, EMPTY_STRING, 10, 20, EMPTY_STRING, EMPTY_STRING, EMPTY_STRING, EMPTY_STRING );
  private TextFileField[] textFileFields = new TextFileField[] { textFileField, textFileField2 };
  private Object[] row = new Object[] { "some data", "another data" };
  private Object[] row2 = new Object[] { "some data2", "another data2" };
  private List<Object[]> emptyRows = new ArrayList<Object[]>();
  private List<Object[]> rows = new ArrayList<Object[]>();
  private List<String> contents = new ArrayList<String>();
  private TextFileOutput textFileOutput;

  {
    rows.add( row );
    rows.add( row2 );

    contents.add( EMPTY_STRING );
    contents.add( EMPTY_STRING );
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
    contents.add( EMPTY_STRING );
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

  @Before
  public void setUp() throws Exception {
    stepMockHelper =
        new StepMockHelper<TextFileOutputMeta, TextFileOutputData>( "TEXT FILE OUTPUT TEST", TextFileOutputMeta.class,
            TextFileOutputData.class );
    Mockito.when( stepMockHelper.logChannelInterfaceFactory.create( Mockito.any(), Mockito.any( LoggingObjectInterface.class ) ) ).thenReturn(
        stepMockHelper.logChannelInterface );
    Mockito.verify( stepMockHelper.logChannelInterface, Mockito.never() ).logError( Mockito.anyString() );
    Mockito.verify( stepMockHelper.logChannelInterface, Mockito.never() ).logError( Mockito.anyString(), Mockito.any( Object[].class ) );
    Mockito.verify( stepMockHelper.logChannelInterface, Mockito.never() ).logError( Mockito.anyString(), (Throwable) Mockito.anyObject() );
    Mockito.when( stepMockHelper.trans.isRunning() ).thenReturn( true );
    Mockito.verify( stepMockHelper.trans, Mockito.never() ).stopAll();
    stepMockHelper.stepDataInterface.previouslyOpenedFiles = new ArrayList<>();
    Mockito.when( stepMockHelper.processRowsStepMetaInterface.getSeparator() ).thenReturn( " " );
    Mockito.when( stepMockHelper.processRowsStepMetaInterface.getEnclosure() ).thenReturn( "\"" );
    Mockito.when( stepMockHelper.processRowsStepMetaInterface.getNewline() ).thenReturn( "\n" );
    Mockito.when( stepMockHelper.transMeta.listVariables() ).thenReturn( new String[0] );
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
    textFileOutput.data = Mockito.mock( TextFileOutputData.class );

    Assert.assertNull( textFileOutput.data.out );
    textFileOutput.closeFile();
  }

  @Test
  public void testCloseFileDataOutIsNotNullCase() throws IOException {
    textFileOutput =
        new TextFileOutput( stepMockHelper.stepMeta, stepMockHelper.stepDataInterface, 0, stepMockHelper.transMeta,
            stepMockHelper.trans );
    textFileOutput.data = Mockito.mock( TextFileOutputData.class );
    textFileOutput.data.out = Mockito.mock( CompressionOutputStream.class );

    textFileOutput.closeFile();
    Mockito.verify( textFileOutput.data.out, Mockito.times( 1 ) ).close();
  }

  private FileObject createTemplateFile() throws IOException {
    String path =
        TestUtils.createRamFile( getClass().getSimpleName() + "/" + TEXT_FILE_OUTPUT_PREFIX + new Random().nextLong()
            + TEXT_FILE_OUTPUT_EXTENSION );
    return TestUtils.getFileObject( path );
  }

  private FileObject createTemplateFile( String content ) throws IOException {
    FileObject f2 = createTemplateFile();
    if ( content == null ) {
      f2.delete();
    } else {
      OutputStreamWriter fw = null;
      try {
        fw = new OutputStreamWriter( f2.getContent().getOutputStream() );
        fw.write( content );
      } finally {
        if ( fw != null ) {
          fw.close();
        }
      }
    }
    return f2;
  }

  @Test
  public void testsIterate() {
    FileObject resultFile = null;
    FileObject contentFile;
    String content = null;
    Boolean[] bool = new Boolean[] { false, true };
    int i = 0;
    for ( Boolean fileExists : bool ) {
      for ( Boolean dataReceived : bool ) {
        for ( Boolean isDoNotOpenNewFileInit : bool ) {
          for ( Boolean endLineExists : bool ) {
            for ( Boolean append : bool ) {
              try {
                resultFile = helpTestInit( fileExists, dataReceived, isDoNotOpenNewFileInit, endLineExists, append );
                content = (String) contents.toArray()[i++];
                contentFile = createTemplateFile( content );
                if ( resultFile.exists() ) {
                  Assert.assertTrue( IOUtils.contentEquals( resultFile.getContent().getInputStream(), contentFile.getContent()
                      .getInputStream() ) );
                } else {
                  Assert.assertFalse( contentFile.exists() );
                }
              } catch ( Exception e ) {
                Assert.fail( e.getMessage() + "\n FileExists = " + fileExists + "\n DataReceived = " + dataReceived
                    + "\n isDoNotOpenNewFileInit = " + isDoNotOpenNewFileInit + "\n EndLineExists = " + endLineExists
                    + "\n Append = " + append + "\n Content = " + content + "\n resultFile = " + resultFile );
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
   *
   * @throws KettleException
   */
  @Test
  public void testNoOpenFileCall_IfRule_1() throws KettleException {

    TextFileField tfFieldMock = Mockito.mock( TextFileField.class );
    TextFileField[] textFileFields = { tfFieldMock };

    Mockito.when( stepMockHelper.initStepMetaInterface.getEndedLine() ).thenReturn( EMPTY_STRING );
    Mockito.when( stepMockHelper.initStepMetaInterface.getOutputFields() ).thenReturn( textFileFields );
    Mockito.when( stepMockHelper.initStepMetaInterface.isDoNotOpenNewFileInit() ).thenReturn( true );

    Mockito.when( stepMockHelper.processRowsStepMetaInterface.getEndedLine() ).thenReturn( EMPTY_STRING );
    Mockito.when( stepMockHelper.processRowsStepMetaInterface.getFileName() ).thenReturn( EMPTY_FILE_NAME );
    Mockito.when( stepMockHelper.processRowsStepMetaInterface.isDoNotOpenNewFileInit() ).thenReturn( true );
    Mockito.when( stepMockHelper.processRowsStepMetaInterface.getOutputFields() ).thenReturn( textFileFields );

    Mockito.when( stepMockHelper.processRowsStepDataInterface.getPreviouslyOpenedFiles() ).thenReturn(
        new ArrayList<String>() );
    textFileOutput =
        new TextFileOutput( stepMockHelper.stepMeta, stepMockHelper.stepDataInterface, 0, stepMockHelper.transMeta,
            stepMockHelper.trans );
    TextFileOutput textFileoutputSpy = Mockito.spy( textFileOutput );
    Mockito.doNothing().when( textFileoutputSpy ).openNewFile( EMPTY_FILE_NAME );
    textFileoutputSpy.init( stepMockHelper.initStepMetaInterface, stepMockHelper.initStepDataInterface );

    Mockito.when( stepMockHelper.processRowsStepMetaInterface.buildFilename( Mockito.anyString(), Mockito.anyString(),
            Mockito.anyObject(), Mockito.anyInt(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyBoolean(), Mockito.anyObject() ) ).
            thenReturn( TEXT_FILE_OUTPUT_PREFIX + TEXT_FILE_OUTPUT_EXTENSION );

    textFileoutputSpy.processRow( stepMockHelper.processRowsStepMetaInterface, stepMockHelper.initStepDataInterface );
    Mockito.verify( textFileoutputSpy, Mockito.never() ).openNewFile( EMPTY_FILE_NAME );
    Mockito.verify( textFileoutputSpy, Mockito.never() ).writeEndedLine();
    Mockito.verify( textFileoutputSpy ).setOutputDone();
  }

  private FileObject helpTestInit( Boolean fileExists, Boolean dataReceived, Boolean isDoNotOpenNewFileInit,
      Boolean endLineExists, Boolean append ) throws Exception {
    FileObject f;
    String endLine = null;
    List<Object[]> rows;

    if ( fileExists ) {
      f = createTemplateFile( TEST_PREVIOUS_DATA );
    } else {
      f = createTemplateFile( null );

    }

    if ( dataReceived ) {
      rows = this.rows;
    } else {
      rows = this.emptyRows;
    }

    if ( endLineExists ) {
      endLine = END_LINE;
    }

    List<Throwable> errors =
        doOutput( textFileFields, rows, f.getName().getURI(), endLine, false, isDoNotOpenNewFileInit, append );
    if ( !errors.isEmpty() ) {
      StringBuilder str = new StringBuilder();
      for ( Throwable thr : errors ) {
        str.append( thr );
      }
      Assert.fail( str.toString() );
    }

    return f;

  }

  private List<Throwable> doOutput( TextFileField[] textFileField, List<Object[]> rows, String pathToFile,
      String endedLine, Boolean isHeaderEnabled, Boolean isDoNotOpenNewFileInit, Boolean append )
        throws KettleException {
    TextFileOutputData textFileOutputData = new TextFileOutputData();
    TextFileOutput textFileOutput =
        new TextFileOutputTestHandler( stepMockHelper.stepMeta, textFileOutputData, 0, stepMockHelper.transMeta,
            stepMockHelper.trans );

    // init step meta and process step meta should be the same in this case
    Mockito.when( stepMockHelper.processRowsStepMetaInterface.isDoNotOpenNewFileInit() ).thenReturn( isDoNotOpenNewFileInit );
    Mockito.when( stepMockHelper.processRowsStepMetaInterface.isFileAppended() ).thenReturn( append );

    Mockito.when( stepMockHelper.processRowsStepMetaInterface.isHeaderEnabled() ).thenReturn( isHeaderEnabled );
    Mockito.when( stepMockHelper.processRowsStepMetaInterface.getFileName() ).thenReturn( pathToFile );
    Mockito.when( stepMockHelper.processRowsStepMetaInterface.buildFilename( Mockito.anyString(), Mockito.anyString(),
        ( (VariableSpace) Mockito.anyObject() ), Mockito.anyInt(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyBoolean(),
        (TextFileOutputMeta) Mockito.anyObject() ) ).thenReturn( pathToFile );

    Mockito.when( stepMockHelper.processRowsStepMetaInterface.getOutputFields() ).thenReturn( textFileField );

    textFileOutput.init( stepMockHelper.processRowsStepMetaInterface, textFileOutputData );

    // Process rows

    RowSet rowSet = stepMockHelper.getMockInputRowSet( rows );
    RowMetaInterface inputRowMeta = Mockito.mock( RowMetaInterface.class );
    textFileOutput.setInputRowMeta( inputRowMeta );

    Mockito.when( rowSet.getRowWait( Mockito.anyInt(), (TimeUnit) Mockito.anyObject() ) ).thenReturn( rows.isEmpty() ? null : rows.iterator()
        .next() );
    Mockito.when( rowSet.getRowMeta() ).thenReturn( inputRowMeta );
    Mockito.when( inputRowMeta.clone() ).thenReturn( inputRowMeta );

    for ( int i = 0; i < textFileField.length; i++ ) {
      String name = textFileField[i].getName();
      ValueMetaString valueMetaString = new ValueMetaString( name );
      Mockito.when( inputRowMeta.getValueMeta( i ) ).thenReturn( valueMetaString );
      Mockito.when( inputRowMeta.indexOfValue( name ) ).thenReturn( i );
    }

    textFileOutput.addRowSetToInputRowSets( rowSet );
    textFileOutput.addRowSetToOutputRowSets( rowSet );

    Mockito.when( stepMockHelper.processRowsStepMetaInterface.getEndedLine() ).thenReturn( endedLine );
    Mockito.when( stepMockHelper.processRowsStepMetaInterface.isFastDump() ).thenReturn( true );

    for ( int i = 0; i < rows.size(); i++ ) {
      ( (TextFileOutputTestHandler) textFileOutput ).setRow( rows.get( i ) );
      textFileOutput.processRow( stepMockHelper.processRowsStepMetaInterface, textFileOutputData );
    }
    ( (TextFileOutputTestHandler) textFileOutput ).setRow( null );
    textFileOutput.processRow( stepMockHelper.processRowsStepMetaInterface, textFileOutputData );
    textFileOutput.dispose( stepMockHelper.processRowsStepMetaInterface, textFileOutputData );
    return ( (TextFileOutputTestHandler) textFileOutput ).errors;
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

    RowMeta rowMeta = new RowMeta();
    rowMeta.addValueMeta( new ValueMetaString( "name" ) );
    data.outputRowMeta = rowMeta;

    data.writer = new ByteArrayOutputStream();

    if ( field != null ) {
      meta.setOutputFields( new TextFileField[] { field } );
    }

    step = Mockito.spy( step );
    step.writeHeader();
    Mockito.verify( step ).containsSeparatorOrEnclosure( Mockito.any( byte[].class ), Mockito.any( byte[].class ),
            Mockito.any( byte[].class ) );
  }


  /**
   * PDI-15650
   * File Exists=N Flag Set=N Add Header=Y Append=Y
   * Result = File is created, header is written at top of file (this changed by the fix)
   *
   * @throws KettleException
   */
  @Test
  public void testProcessRule_2() throws Exception {

    TextFileField tfFieldMock = Mockito.mock( TextFileField.class );
    TextFileField[] textFileFields = { tfFieldMock };

    Mockito.when( stepMockHelper.initStepMetaInterface.getEndedLine() ).thenReturn( EMPTY_STRING );
    Mockito.when( stepMockHelper.initStepMetaInterface.getOutputFields() ).thenReturn( textFileFields );
    Mockito.when( stepMockHelper.initStepMetaInterface.isDoNotOpenNewFileInit() ).thenReturn( true );

    Mockito.when( stepMockHelper.processRowsStepMetaInterface.getEndedLine() ).thenReturn( EMPTY_STRING );
    Mockito.when( stepMockHelper.processRowsStepMetaInterface.getFileName() ).thenReturn( TEXT_FILE_OUTPUT_PREFIX + TEXT_FILE_OUTPUT_EXTENSION );
    Mockito.when( stepMockHelper.processRowsStepMetaInterface.isFileAppended() ).thenReturn( true );
    Mockito.when( stepMockHelper.processRowsStepMetaInterface.isHeaderEnabled() ).thenReturn( true );
    Mockito.when( stepMockHelper.processRowsStepMetaInterface.getOutputFields() ).thenReturn( textFileFields );
    Mockito.when( stepMockHelper.processRowsStepMetaInterface.isDoNotOpenNewFileInit() ).thenReturn( true );

    Mockito.when( stepMockHelper.processRowsStepMetaInterface.isFileNameInField() ).thenReturn( false );


    textFileOutput =
            new TextFileOutputTestHandler( stepMockHelper.stepMeta, stepMockHelper.stepDataInterface, 0, stepMockHelper.transMeta,
                    stepMockHelper.trans );
    ( (TextFileOutputTestHandler) textFileOutput ).setRow( new Object[] {"data text"} );
    RowMetaInterface inputRowMeta = Mockito.mock( RowMetaInterface.class );

    ValueMetaInterface valueMetaInterface = Mockito.mock( ValueMetaInterface.class );
    Mockito.when( valueMetaInterface.getString( Mockito.anyObject() ) ).thenReturn( TEXT_FILE_OUTPUT_PREFIX + TEXT_FILE_OUTPUT_EXTENSION );
    Mockito.when( inputRowMeta.getValueMeta( Mockito.anyInt() ) ).thenReturn( valueMetaInterface );
    Mockito.when( inputRowMeta.clone() ).thenReturn( inputRowMeta );

    textFileOutput.setInputRowMeta( inputRowMeta );

    stepMockHelper.initStepDataInterface.fileWriterMap = new HashMap<>();
    stepMockHelper.initStepDataInterface.fileName = TEXT_FILE_OUTPUT_PREFIX + TEXT_FILE_OUTPUT_EXTENSION;
    stepMockHelper.initStepDataInterface.fileWriterMap.put( TEXT_FILE_OUTPUT_PREFIX + TEXT_FILE_OUTPUT_EXTENSION,
            Mockito.mock( OutputStream.class ) );

    TextFileOutput textFileOutputSpy = Mockito.spy( textFileOutput );
    Mockito.doNothing().when( textFileOutputSpy ).openNewFile( EMPTY_FILE_NAME );
    Mockito.doReturn( false ).when( textFileOutputSpy ).isFileExist( TEXT_FILE_OUTPUT_PREFIX + TEXT_FILE_OUTPUT_EXTENSION );
    textFileOutputSpy.init( stepMockHelper.processRowsStepMetaInterface, stepMockHelper.initStepDataInterface );
    Mockito.when( stepMockHelper.processRowsStepMetaInterface.buildFilename( TEXT_FILE_OUTPUT_PREFIX + TEXT_FILE_OUTPUT_EXTENSION, null,
            textFileOutputSpy, 0, null, 0, true, stepMockHelper.processRowsStepMetaInterface ) ).
            thenReturn( TEXT_FILE_OUTPUT_PREFIX + TEXT_FILE_OUTPUT_EXTENSION );

    textFileOutputSpy.processRow( stepMockHelper.processRowsStepMetaInterface, stepMockHelper.initStepDataInterface );
    Mockito.verify( textFileOutputSpy, Mockito.times( 1 ) ).writeHeader(  );
    Files.deleteIfExists( new File( TEXT_FILE_OUTPUT_PREFIX + TEXT_FILE_OUTPUT_EXTENSION ).toPath() );
  }


  /**
   * PDI-15650
   * File Exists=N Flag Set=N Add Header=Y Append=Y
   * Result = File is created, header is written at top of file (this changed by the fix)
   * with file name in stream
   *
   * @throws KettleException
   */
  @Test
  public void testProcessRule_2FileNameInField() throws KettleException {

    TextFileField tfFieldMock = Mockito.mock( TextFileField.class );
    TextFileField[] textFileFields = { tfFieldMock };

    Mockito.when( stepMockHelper.initStepMetaInterface.getEndedLine() ).thenReturn( EMPTY_STRING );
    Mockito.when( stepMockHelper.initStepMetaInterface.getOutputFields() ).thenReturn( textFileFields );
    Mockito.when( stepMockHelper.initStepMetaInterface.isDoNotOpenNewFileInit() ).thenReturn( true );

    Mockito.when( stepMockHelper.processRowsStepMetaInterface.getEndedLine() ).thenReturn( EMPTY_STRING );
    Mockito.when( stepMockHelper.processRowsStepMetaInterface.getFileName() ).thenReturn( TEXT_FILE_OUTPUT_PREFIX + TEXT_FILE_OUTPUT_EXTENSION );
    Mockito.when( stepMockHelper.processRowsStepMetaInterface.isFileAppended() ).thenReturn( true );
    Mockito.when( stepMockHelper.processRowsStepMetaInterface.isHeaderEnabled() ).thenReturn( true );
    Mockito.when( stepMockHelper.processRowsStepMetaInterface.getOutputFields() ).thenReturn( textFileFields );
    Mockito.when( stepMockHelper.processRowsStepMetaInterface.isDoNotOpenNewFileInit() ).thenReturn( true );

    Mockito.when( stepMockHelper.processRowsStepMetaInterface.isFileNameInField() ).thenReturn( true );


    textFileOutput =
            new TextFileOutputTestHandler( stepMockHelper.stepMeta, stepMockHelper.stepDataInterface, 0, stepMockHelper.transMeta,
                    stepMockHelper.trans );
    ( (TextFileOutputTestHandler) textFileOutput ).setRow( new Object[] {"data text"} );
    RowMetaInterface inputRowMeta = Mockito.mock( RowMetaInterface.class );

    ValueMetaInterface valueMetaInterface = Mockito.mock( ValueMetaInterface.class );
    Mockito.when( valueMetaInterface.getString( Mockito.anyObject() ) ).thenReturn( TEXT_FILE_OUTPUT_PREFIX + TEXT_FILE_OUTPUT_EXTENSION );
    Mockito.when( inputRowMeta.getValueMeta( Mockito.anyInt() ) ).thenReturn( valueMetaInterface );
    Mockito.when( inputRowMeta.clone() ).thenReturn( inputRowMeta );

    textFileOutput.setInputRowMeta( inputRowMeta );

    stepMockHelper.initStepDataInterface.fileWriterMap = new HashMap<>();
    stepMockHelper.initStepDataInterface.fileName = TEXT_FILE_OUTPUT_PREFIX + TEXT_FILE_OUTPUT_EXTENSION;

    TextFileOutput textFileOutputSpy = Mockito.spy( textFileOutput );
    Mockito.doNothing().when( textFileOutputSpy ).openNewFile( EMPTY_FILE_NAME );
    Mockito.doReturn( false ).when( textFileOutputSpy ).isFileExist( TEXT_FILE_OUTPUT_PREFIX + TEXT_FILE_OUTPUT_EXTENSION );
    textFileOutputSpy.init( stepMockHelper.processRowsStepMetaInterface, stepMockHelper.initStepDataInterface );
    Mockito.when( stepMockHelper.processRowsStepMetaInterface.buildFilename( TEXT_FILE_OUTPUT_PREFIX + TEXT_FILE_OUTPUT_EXTENSION, null,
            textFileOutputSpy, 0, null, 0, true, stepMockHelper.processRowsStepMetaInterface ) ).
            thenReturn( TEXT_FILE_OUTPUT_PREFIX + TEXT_FILE_OUTPUT_EXTENSION );

    textFileOutputSpy.processRow( stepMockHelper.processRowsStepMetaInterface, stepMockHelper.initStepDataInterface );
    Mockito.verify( textFileOutputSpy, Mockito.times( 1 ) ).writeHeader(  );
  }

  /**
   * Test for PDI-13987
   * @throws Exception
   */
  @Test
  public void testFastDumpDisableStreamEncodeTest() throws Exception {

    textFileOutput =
            new TextFileOutputTestHandler( stepMockHelper.stepMeta, stepMockHelper.stepDataInterface, 0, stepMockHelper.transMeta,
                    stepMockHelper.trans );
    textFileOutput.meta = stepMockHelper.processRowsStepMetaInterface;

    String testString = "ÖÜä";
    String inputEncode = "UTF-8";
    String outputEncode = "Windows-1252";
    Object[] rows = {testString.getBytes( inputEncode )};

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

    Mockito.doReturn( outputEncode ).when( stepMockHelper.processRowsStepMetaInterface ).getEncoding();
    textFileOutput.data.writer = Mockito.mock( BufferedOutputStream.class );

    textFileOutput.writeRowToFile( rowMeta, rows );
    Mockito.verify( textFileOutput.data.writer, Mockito.times( 1 ) ).write( testString.getBytes( outputEncode ) );
  }

}
