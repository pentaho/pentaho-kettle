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


package org.pentaho.di.trans.steps.textfileinput;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.pentaho.di.TestFailedException;
import org.pentaho.di.TestUtilities;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.compress.CompressionPluginType;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaInteger;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.trans.RowProducer;
import org.pentaho.di.trans.RowStepCollector;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransHopMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.reporting.libraries.base.util.CSVTokenizer;

import junit.framework.TestCase;

/**
 * This class was a "copy and modification" of Kettle's CsvInput1Test. I added comments as I was learning the
 * architecture of the class.
 *
 * @author sflatley
 */
public class TextFileInputIT extends TestCase {

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    PluginRegistry.addPluginType( CompressionPluginType.getInstance() );
    PluginRegistry.init( true );
  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {
  }

  /**
   * Write the file to be used as input (as a temporary file).
   *
   * @return Absolute file name/path of the created file.
   * @throws IOException
   *           UPON
   */
  public String writeInputFile( int testNum ) throws IOException {

    String rcode = null;
    File tempFile = File.createTempFile( "PDI_tmp", ".tmp" );
    tempFile.deleteOnExit();
    rcode = tempFile.getAbsolutePath();
    String endOfLineCharacters = TestUtilities.getEndOfLineCharacters();

    FileWriter fout = new FileWriter( tempFile );
    switch ( testNum ) {
      case 1: {
        writeInput1( endOfLineCharacters, fout );
        break;
      }
      case 2: {
        writeInput2( endOfLineCharacters, fout );
        break;
      }
      default:
        break;
    }
    fout.close();

    return rcode;
  }

  private void writeInput2( String endOfLineCharacters, FileWriter fout ) throws IOException {
    fout.write( "\t123456\t;\t1234567\t" + endOfLineCharacters );
    fout.write( "\t654321\t;\t7654321\t" + endOfLineCharacters );

  }

  private void writeInput1( String endOfLineCharacters, FileWriter fout ) throws IOException {
    fout.write( "A;B;C;D;E" + endOfLineCharacters );
    fout.write( "1;b1;c1;d1;e1" + endOfLineCharacters );
    fout.write( "2;b2;c2;d2;e2" + endOfLineCharacters );
    fout.write( "3;b3;c3;d3;e3" + endOfLineCharacters );
  }

  /**
   * Create result data for test case 1. Each Object array in element in list should mirror the data written by
   * writeInputFile().
   *
   * @return list of metadata/data couples of how the result should look like.
   */
  public List<RowMetaAndData> createResultData1() {
    List<RowMetaAndData> list = new ArrayList<RowMetaAndData>();
    ValueMetaInterface[] valuesMeta =
    { new ValueMetaInteger( "a" ), new ValueMetaString( "b" ),
      new ValueMetaString( "c" ), new ValueMetaString( "d" ),
      new ValueMetaString( "e" ), new ValueMetaString( "filename" ), };
    RowMetaInterface rm = createResultRowMetaInterface( valuesMeta );

    Object[] r1 = new Object[] { new Long( 1L ), "b1", "c1", "d1", "e1", "fileName" };
    Object[] r2 = new Object[] { new Long( 2L ), "b2", "c2", "d2", "e2", "fileName" };
    Object[] r3 = new Object[] { new Long( 3L ), "b3", "c3", "d3", "e3", "fileName" };

    list.add( new RowMetaAndData( rm, r1 ) );
    list.add( new RowMetaAndData( rm, r2 ) );
    list.add( new RowMetaAndData( rm, r3 ) );

    return list;
  }

  /**
   * Create result data for test case 2. Each Object array in element in list should mirror the data written by
   * writeInputFile().
   *
   * @return list of metadata/data couples of how the result should look like.
   */
  public List<RowMetaAndData> createResultData2() {
    List<RowMetaAndData> list = new ArrayList<RowMetaAndData>();
    ValueMetaInterface[] valuesMeta =
    { new ValueMetaInteger( "a", 15, -1 ), new ValueMetaInteger( "b", 15, -1 ) };

    RowMetaInterface rm = createResultRowMetaInterface( valuesMeta );

    Object[] r1 = new Object[] { 123456L, (long) 1234567 };
    Object[] r2 = new Object[] { 654321L, (long) 7654321 };

    list.add( new RowMetaAndData( rm, r1 ) );
    list.add( new RowMetaAndData( rm, r2 ) );

    return list;
  }

  /**
   * Creates a RowMetaInterface with a ValueMetaInterface with the name "filename".
   *
   * @return
   */
  public RowMetaInterface createRowMetaInterface() {
    RowMetaInterface rowMeta = new RowMeta();

    ValueMetaInterface[] valuesMeta = { new ValueMetaString( "filename" ), };
    for ( int i = 0; i < valuesMeta.length; i++ ) {
      rowMeta.addValueMeta( valuesMeta[i] );
    }

    return rowMeta;
  }

  /**
   * Creates data... Will add more as I figure what the data is.
   *
   * @param fileName
   * @return
   */
  public List<RowMetaAndData> createData( String fileName ) {
    List<RowMetaAndData> list = new ArrayList<RowMetaAndData>();
    RowMetaInterface rm = createRowMetaInterface();
    Object[] r1 = new Object[] { fileName };
    list.add( new RowMetaAndData( rm, r1 ) );
    return list;
  }

  /**
   * Creates a row meta interface for the fields that are defined by performing a getFields and by checking "Result
   * filenames - Add filenames to result from "Text File Input" dialog.
   *
   * @return
   */
  public RowMetaInterface createResultRowMetaInterface( ValueMetaInterface[] valuesMeta ) {
    RowMetaInterface rm = new RowMeta();

    for ( int i = 0; i < valuesMeta.length; i++ ) {
      rm.addValueMeta( valuesMeta[i] );
    }

    return rm;
  }

  private StepMeta createTextFileInputStep( String name, String fileName, PluginRegistry registry ) {

    // Create a Text File Input step
    String testFileInputName = "text file input step";
    TextFileInputMeta textFileInputMeta = new TextFileInputMeta();
    String textFileInputPid = registry.getPluginId( StepPluginType.class, textFileInputMeta );
    StepMeta textFileInputStep = new StepMeta( textFileInputPid, testFileInputName, textFileInputMeta );

    // initialize the fields
    TextFileInputField[] fields = new TextFileInputField[5];
    for ( int idx = 0; idx < fields.length; idx++ ) {
      fields[idx] = new TextFileInputField();
    }

    // populate the fields
    // it is important that the setPosition(int)
    // is invoked with the correct position as
    // we are testing the reading of a delimited file.
    fields[0].setName( "a" );
    fields[0].setType( ValueMetaInterface.TYPE_INTEGER );
    fields[0].setFormat( "" );
    fields[0].setLength( -1 );
    fields[0].setPrecision( -1 );
    fields[0].setCurrencySymbol( "" );
    fields[0].setDecimalSymbol( "" );
    fields[0].setGroupSymbol( "" );
    fields[0].setTrimType( ValueMetaInterface.TRIM_TYPE_NONE );
    fields[0].setPosition( 1 );

    fields[1].setName( "b" );
    fields[1].setType( ValueMetaInterface.TYPE_STRING );
    fields[1].setFormat( "" );
    fields[1].setLength( -1 );
    fields[1].setPrecision( -1 );
    fields[1].setCurrencySymbol( "" );
    fields[1].setDecimalSymbol( "" );
    fields[1].setGroupSymbol( "" );
    fields[1].setTrimType( ValueMetaInterface.TRIM_TYPE_NONE );
    fields[1].setPosition( 2 );

    fields[2].setName( "c" );
    fields[2].setType( ValueMetaInterface.TYPE_STRING );
    fields[2].setFormat( "" );
    fields[2].setLength( -1 );
    fields[2].setPrecision( -1 );
    fields[2].setCurrencySymbol( "" );
    fields[2].setDecimalSymbol( "" );
    fields[2].setGroupSymbol( "" );
    fields[2].setTrimType( ValueMetaInterface.TRIM_TYPE_NONE );
    fields[2].setPosition( 3 );

    fields[3].setName( "d" );
    fields[3].setType( ValueMetaInterface.TYPE_STRING );
    fields[3].setFormat( "" );
    fields[3].setLength( -1 );
    fields[3].setPrecision( -1 );
    fields[3].setCurrencySymbol( "" );
    fields[3].setDecimalSymbol( "" );
    fields[3].setGroupSymbol( "" );
    fields[3].setTrimType( ValueMetaInterface.TRIM_TYPE_NONE );
    fields[3].setPosition( 4 );

    fields[4].setName( "e" );
    fields[4].setType( ValueMetaInterface.TYPE_STRING );
    fields[4].setFormat( "" );
    fields[4].setLength( -1 );
    fields[4].setPrecision( -1 );
    fields[4].setCurrencySymbol( "" );
    fields[4].setDecimalSymbol( "" );
    fields[4].setGroupSymbol( "" );
    fields[4].setTrimType( ValueMetaInterface.TRIM_TYPE_NONE );
    fields[4].setPosition( 5 );

    // call this so that we allocate the arrays
    // for files, fields and filters.
    // we are testing one file and one set
    // of fields. No filters
    textFileInputMeta.allocate( 1, 1, 0 );

    // set meta properties- these were determined by running Spoon
    // and setting up the transformation we are setting up here.
    // i.e. - the dialog told me what I had to set to avoid
    // NPEs during the transformation.
    String[] filesRequired = new String[] { "N" };
    String[] includeSubfolders = new String[] { "N" };
    textFileInputMeta.setFilenameField( "filename" );
    textFileInputMeta.setEnclosure( "\"" );
    textFileInputMeta.setAddResultFile( false );
    textFileInputMeta.setFileName( new String[] { fileName } );
    textFileInputMeta.setFileFormat( TestUtilities.getFileFormat() );
    textFileInputMeta.setFileType( "CSV" );
    textFileInputMeta.setSeparator( ";" );
    textFileInputMeta.setFileRequired( filesRequired );
    textFileInputMeta.setIncludeSubFolders( includeSubfolders );
    textFileInputMeta.setInputFields( fields );
    textFileInputMeta.setHeader( true );
    textFileInputMeta.setNrHeaderLines( 1 );
    textFileInputMeta.setFileCompression( "None" );
    textFileInputMeta.setNoEmptyLines( true );
    textFileInputMeta.setRowLimit( 0 );
    textFileInputMeta.setAddResultFile( true );
    textFileInputMeta.setDateFormatLocale( new Locale( "en_US" ) );
    textFileInputMeta.setIncludeFilename( true );

    return textFileInputStep;

  }

  private StepMeta createTextFileInputStep2( String name, String fileName, PluginRegistry registry ) {

    // Create a Text File Input step
    TextFileInputMeta textFileInputMeta = new TextFileInputMeta();
    String textFileInputPid = registry.getPluginId( StepPluginType.class, textFileInputMeta );
    StepMeta textFileInputStep = new StepMeta( textFileInputPid, name, textFileInputMeta );

    // initialize the fields
    TextFileInputField[] fields = new TextFileInputField[2];
    for ( int idx = 0; idx < fields.length; idx++ ) {
      fields[idx] = new TextFileInputField();
    }

    // populate the fields
    // it is important that the setPosition(int)
    // is invoked with the correct position as
    // we are testing the reading of a delimited file.
    fields[0].setName( "a" );
    fields[0].setType( ValueMetaInterface.TYPE_INTEGER );
    fields[0].setFormat( "" );
    fields[0].setLength( 15 );
    fields[0].setPrecision( -1 );
    fields[0].setCurrencySymbol( "" );
    fields[0].setDecimalSymbol( "" );
    fields[0].setGroupSymbol( "" );
    fields[0].setTrimType( ValueMetaInterface.TRIM_TYPE_NONE );
    fields[0].setPosition( 1 );

    fields[1].setName( "b" );
    fields[1].setType( ValueMetaInterface.TYPE_INTEGER );
    fields[1].setFormat( "" );
    fields[1].setLength( 15 );
    fields[1].setPrecision( -1 );
    fields[1].setCurrencySymbol( "" );
    fields[1].setDecimalSymbol( "" );
    fields[1].setGroupSymbol( "" );
    fields[1].setTrimType( ValueMetaInterface.TRIM_TYPE_NONE );
    fields[1].setPosition( 2 );

    // call this so that we allocate the arrays
    // for files, fields and filters.
    // we are testing one file and one set
    // of fields. No filters
    textFileInputMeta.allocate( 1, 1, 0 );

    // set meta properties- these were determined by running Spoon
    // and setting up the transformation we are setting up here.
    // i.e. - the dialog told me what I had to set to avoid
    // NPEs during the transformation.
    String[] filesRequired = new String[] { "N" };
    String[] includeSubfolders = new String[] { "N" };
    textFileInputMeta.setEnclosure( "$[09]" );
    textFileInputMeta.setAddResultFile( false );
    textFileInputMeta.setFileName( new String[] { fileName } );
    textFileInputMeta.setFileFormat( TestUtilities.getFileFormat() );
    textFileInputMeta.setFileType( "CSV" );
    textFileInputMeta.setSeparator( ";" );
    textFileInputMeta.setFileRequired( filesRequired );
    textFileInputMeta.setIncludeSubFolders( includeSubfolders );
    textFileInputMeta.setInputFields( fields );
    textFileInputMeta.setHeader( false );
    textFileInputMeta.setNrHeaderLines( 0 );
    textFileInputMeta.setFileCompression( "None" );
    textFileInputMeta.setNoEmptyLines( true );
    textFileInputMeta.setRowLimit( 0 );
    textFileInputMeta.setDateFormatLocale( new Locale( "en_US" ) );
    textFileInputMeta.setIncludeFilename( false );

    return textFileInputStep;
  }

  public void testTextFileInput1() throws Exception {
    KettleEnvironment.init();

    // Create a new transformation...
    //
    TransMeta transMeta = new TransMeta();
    transMeta.setName( "testTextFileInput1" );
    PluginRegistry registry = PluginRegistry.getInstance();

    // write the data that is to be read in
    // by the step we are testing
    String fileName = writeInputFile( 1 );

    // create an injector step and add it to the trans meta
    String injectorStepName = "injector step";
    StepMeta injectorStep = TestUtilities.createInjectorStep( injectorStepName, registry );
    transMeta.addStep( injectorStep );

    // Create a Text File Input step
    String testFileInputName = "text file input step";
    StepMeta textFileInputStep = createTextFileInputStep( testFileInputName, fileName, registry );
    transMeta.addStep( textFileInputStep );

    // create a TransHopMeta for textFileInputStep and add it to the transMeta
    TransHopMeta hopInputTextFile = new TransHopMeta( injectorStep, textFileInputStep );
    transMeta.addTransHop( hopInputTextFile );

    // Create a dummy step 1 and add it to the tranMeta
    String dummyStepName = "dummy step";
    StepMeta dummyStep = TestUtilities.createDummyStep( dummyStepName, registry );
    transMeta.addStep( dummyStep );

    // create transHopMeta for the hop from text file input to the dummy step
    TransHopMeta hop_textFileInputStep_dummyStep = new TransHopMeta( textFileInputStep, dummyStep );
    transMeta.addTransHop( hop_textFileInputStep_dummyStep );

    // Now execute the transformation...
    Trans trans = new Trans( transMeta );
    trans.prepareExecution( null );

    // create a row collector and add it to a row listener for the dummy step
    StepInterface si = trans.getStepInterface( dummyStepName, 0 );
    RowStepCollector dummyRowCollector = new RowStepCollector();
    si.addRowListener( dummyRowCollector );

    // Create a row producer for trans
    RowProducer rowProducer = trans.addRowProducer( injectorStepName, 0 );
    trans.startThreads();

    // create the filename rows
    List<RowMetaAndData> inputList = createData( fileName );
    Iterator<RowMetaAndData> it = inputList.iterator();
    while ( it.hasNext() ) {
      RowMetaAndData rowMetaAndData = it.next();
      rowProducer.putRow( rowMetaAndData.getRowMeta(), rowMetaAndData.getData() );
    }
    rowProducer.finished();

    trans.waitUntilFinished();

    // Compare the results
    List<RowMetaAndData> resultRows = dummyRowCollector.getRowsWritten();
    List<RowMetaAndData> goldenImageRows = createResultData1();
    try {
      TestUtilities.checkRows( goldenImageRows, resultRows, 5 );
    } catch ( TestFailedException tfe ) {
      fail( tfe.getMessage() );
    }
  }

  public void testTextFileInput2() throws Exception {
    KettleEnvironment.init();

    // Create a new transformation...
    //
    TransMeta transMeta = new TransMeta();
    transMeta.setName( "testTextFileInput2" );
    PluginRegistry registry = PluginRegistry.getInstance();

    // write the data that is to be read in
    // by the step we are testing
    String fileName = writeInputFile( 2 );

    // Create a Text File Input step
    String testFileInputName = "text file input step";
    StepMeta textFileInputStep = createTextFileInputStep2( testFileInputName, fileName, registry );
    transMeta.addStep( textFileInputStep );

    // Create a dummy step 1 and add it to the tranMeta
    String dummyStepName = "dummy step";
    StepMeta dummyStep = TestUtilities.createDummyStep( dummyStepName, registry );
    transMeta.addStep( dummyStep );

    // create transHopMeta for the hop from text file input to the dummy step
    TransHopMeta hop_textFileInputStep_dummyStep = new TransHopMeta( textFileInputStep, dummyStep );
    transMeta.addTransHop( hop_textFileInputStep_dummyStep );

    // Now execute the transformation...
    Trans trans = new Trans( transMeta );
    trans.prepareExecution( null );

    // create a row collector and add it to a row listener for the dummy step
    StepInterface si = trans.getStepInterface( dummyStepName, 0 );
    RowStepCollector dummyRowCollector = new RowStepCollector();
    si.addRowListener( dummyRowCollector );

    trans.startThreads();

    trans.waitUntilFinished();

    // Compare the results
    List<RowMetaAndData> resultRows = dummyRowCollector.getRowsWritten();
    List<RowMetaAndData> goldenImageRows = createResultData2();
    try {
      TestUtilities.checkRows( goldenImageRows, resultRows, 0 );
    } catch ( TestFailedException tfe ) {
      fail( tfe.getMessage() );
    }
  }

  /**
   * Verify that lines are properly identified when parsing a mixed format file.
   */
  public void testGetLine_FILE_FORMAT_MIXED() throws Exception {
    String fileLocation = "src/it/resources/example.csv";
    InputStream inputStream = KettleVFS.getInputStream( fileLocation );
    InputStreamReader reader = new InputStreamReader( inputStream );
    // Grab the first line and verify it only has 4 tokens instead of 24 (the total tokens in the file)
    StringBuilder stringBuilder = new StringBuilder( 1000 );
    String line = TextFileInput.getLine( null, reader, TextFileInputMeta.FILE_FORMAT_MIXED, stringBuilder );
    CSVTokenizer csvt = new CSVTokenizer( line, ",", "\"" );
    assertEquals( 4, csvt.countTokens() );
  }
}
