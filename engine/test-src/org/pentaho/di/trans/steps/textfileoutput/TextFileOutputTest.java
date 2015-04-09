/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2013 by Pentaho : http://www.pentaho.com
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

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;
import static org.pentaho.di.core.util.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.RowSet;
import org.pentaho.di.core.compress.CompressionPluginType;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.mock.StepMockHelper;

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
  private static final String RESULT_ROWS = "\"some data\" \"another data\"\n"
    + "\"some data2\" \"another data2\"\n";
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
      new StepMockHelper<TextFileOutputMeta, TextFileOutputData>(
        "TEXT FILE OUTPUT TEST", TextFileOutputMeta.class, TextFileOutputData.class );
    when( stepMockHelper.logChannelInterfaceFactory.create( any(), any( LoggingObjectInterface.class ) ) )
      .thenReturn( stepMockHelper.logChannelInterface );
    verify( stepMockHelper.logChannelInterface, never() ).logError( anyString() );
    verify( stepMockHelper.logChannelInterface, never() ).logError( anyString(), anyObject() );
    verify( stepMockHelper.logChannelInterface, never() ).logError( anyString(), (Throwable) anyObject() );
    when( stepMockHelper.trans.isRunning() ).thenReturn( true );
    verify( stepMockHelper.trans, never() ).stopAll();
    stepMockHelper.stepDataInterface.previouslyOpenedFiles = new ArrayList<String>();
    when( stepMockHelper.processRowsStepMetaInterface.getSeparator() ).thenReturn( " " );
    when( stepMockHelper.processRowsStepMetaInterface.getEnclosure() ).thenReturn( "\"" );
    when( stepMockHelper.processRowsStepMetaInterface.getNewline() ).thenReturn( "\n" );
  }

  @After
  public void tearDown() throws Exception {
    stepMockHelper.cleanUp();
  }

  private File createTemplateFile() throws IOException {
    File f = File.createTempFile( TEXT_FILE_OUTPUT_PREFIX, TEXT_FILE_OUTPUT_EXTENSION );
    // comment deletion for debugging
    f.deleteOnExit();
    return f;
  }

  private File createTemplateFile( String content ) throws IOException {
    File f2 = createTemplateFile();
    if ( content == null ) {
      f2.delete();
    } else {
      FileWriter fw = null;
      try {
        fw = new FileWriter( f2 );
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
    File resultFile = null;
    File contentFile;
    String content = null;
    Boolean[] bool = new Boolean[] { false, true };
    int i = 0;
    for ( Boolean fileExists : bool ) {
      for ( Boolean dataReceived : bool ) {
        for ( Boolean isDoNotOpenNewFileInit : bool ) {
          for ( Boolean endLineExists : bool ) {
            for ( Boolean append : bool ) {
              try {
                resultFile =
                  helpTestInit( fileExists, dataReceived, isDoNotOpenNewFileInit, endLineExists, append );
                content = (String) contents.toArray()[ i++ ];
                contentFile = createTemplateFile( content );
                assertTrue( FileUtils.contentEquals( resultFile, contentFile ) );
              } catch ( Exception e ) {
                Assert.fail( e.getMessage()
                  + "\n FileExists = " + fileExists + "\n DataReceived = " + dataReceived
                  + "\n isDoNotOpenNewFileInit = " + isDoNotOpenNewFileInit + "\n EndLineExists = "
                  + endLineExists + "\n Append = " + append + "\n Content = " + content + "\n resultFile = "
                  + resultFile );
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

    TextFileField tfFieldMock = mock( TextFileField.class );
    TextFileField[] textFileFields = { tfFieldMock };

    when( stepMockHelper.initStepMetaInterface.getEndedLine() ).thenReturn( EMPTY_STRING );
    when( stepMockHelper.initStepMetaInterface.getOutputFields() ).thenReturn( textFileFields );
    when( stepMockHelper.initStepMetaInterface.isDoNotOpenNewFileInit() ).thenReturn( true );

    when( stepMockHelper.processRowsStepMetaInterface.getEndedLine() ).thenReturn( EMPTY_STRING );
    when( stepMockHelper.processRowsStepMetaInterface.getFileName() ).thenReturn( EMPTY_FILE_NAME );
    when( stepMockHelper.processRowsStepMetaInterface.isDoNotOpenNewFileInit() ).thenReturn( true );
    when( stepMockHelper.processRowsStepMetaInterface.getOutputFields() ).thenReturn( textFileFields );

    when( stepMockHelper.processRowsStepDataInterface.getPreviouslyOpenedFiles() )
      .thenReturn( new ArrayList<String>() );
    textFileOutput =
      new TextFileOutput( stepMockHelper.stepMeta, stepMockHelper.stepDataInterface, 0, stepMockHelper.transMeta,
        stepMockHelper.trans );
    TextFileOutput textFileoutputSpy = spy( textFileOutput );
    doNothing().when( textFileoutputSpy ).openNewFile( EMPTY_FILE_NAME );
    textFileoutputSpy.init( stepMockHelper.initStepMetaInterface, stepMockHelper.initStepDataInterface );

    textFileoutputSpy.processRow( stepMockHelper.processRowsStepMetaInterface, stepMockHelper.initStepDataInterface );
    verify( textFileoutputSpy, never() ).openNewFile( EMPTY_FILE_NAME );
    verify( textFileoutputSpy, never() ).writeEndedLine();
    verify( textFileoutputSpy ).setOutputDone();
  }

  private File helpTestInit( Boolean fileExists, Boolean dataReceived, Boolean isDoNotOpenNewFileInit,
                             Boolean endLineExists, Boolean append ) throws Exception {
    File f;
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
      doOutput( textFileFields, rows, f.getPath(), endLine, false, isDoNotOpenNewFileInit, append );
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
                                    String endedLine, Boolean isHeaderEnabled, Boolean isDoNotOpenNewFileInit,
                                    Boolean append ) throws KettleException {
    TextFileOutputData textFileOutputData = new TextFileOutputData();
    TextFileOutput textFileOutput =
      new TextFileOutputTestHandler(
        stepMockHelper.stepMeta, textFileOutputData, 0, stepMockHelper.transMeta, stepMockHelper.trans );

    // init step meta and process step meta should be the same in this case
    when( stepMockHelper.processRowsStepMetaInterface.isDoNotOpenNewFileInit() ).thenReturn(
      isDoNotOpenNewFileInit );
    when( stepMockHelper.processRowsStepMetaInterface.isFileAppended() ).thenReturn( append );

    when( stepMockHelper.processRowsStepMetaInterface.isHeaderEnabled() ).thenReturn( isHeaderEnabled );
    when( stepMockHelper.processRowsStepMetaInterface.getFileName() ).thenReturn( pathToFile );
    when(
      stepMockHelper.processRowsStepMetaInterface.buildFilename(
        anyString(), anyString(), ( (VariableSpace) anyObject() ), anyInt(), anyString(), anyInt(),
        anyBoolean(), (TextFileOutputMeta) anyObject() ) ).thenReturn( pathToFile );

    when( stepMockHelper.processRowsStepMetaInterface.getOutputFields() ).thenReturn( textFileField );

    textFileOutput.init( stepMockHelper.processRowsStepMetaInterface, textFileOutputData );

    // Process rows

    RowSet rowSet = stepMockHelper.getMockInputRowSet( rows );
    RowMetaInterface inputRowMeta = mock( RowMetaInterface.class );
    textFileOutput.setInputRowMeta( inputRowMeta );

    when( rowSet.getRowWait( anyInt(), (TimeUnit) anyObject() ) ).thenReturn(
      rows.isEmpty() ? null : rows.iterator().next() );
    when( rowSet.getRowMeta() ).thenReturn( inputRowMeta );
    when( inputRowMeta.clone() ).thenReturn( inputRowMeta );

    for ( int i = 0; i < textFileField.length; i++ ) {
      String name = textFileField[ i ].getName();
      when( inputRowMeta.getValueMeta( i ) ).thenReturn( new ValueMetaString( name ) );
      when( inputRowMeta.indexOfValue( name ) ).thenReturn( i );
    }

    textFileOutput.getInputRowSets().add( rowSet );
    textFileOutput.getOutputRowSets().add( rowSet );

    when( stepMockHelper.processRowsStepMetaInterface.getEndedLine() ).thenReturn( endedLine );
    when( stepMockHelper.processRowsStepMetaInterface.isFastDump() ).thenReturn( true );

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

    if (field != null) {
      meta.setOutputFields( new TextFileField[] { field } );
    }

    step = spy( step );
    step.writeHeader();
    verify( step )
      .containsSeparatorOrEnclosure( any( byte[].class ), any( byte[].class ), any( byte[].class ) );
  }
}
