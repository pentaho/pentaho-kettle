/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2016 by Pentaho : http://www.pentaho.com
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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import junit.framework.TestCase;

import org.junit.Test;
import org.pentaho.di.TestFailedException;
import org.pentaho.di.TestUtilities;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaInteger;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.trans.RowStepCollector;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransHopMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.dummytrans.DummyTransMeta;
import org.pentaho.di.trans.steps.rowgenerator.RowGeneratorMeta;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * This class tests the functionality of the TextFileOutput step.
 */
public class TextFileOutputTests extends TestCase {

  private static final String SEPARATOR = ";";
  private static final String EXTENSION = "txt";

  /**
   * Creates a row generator step for this class..
   *
   * @param name
   * @param registry
   * @return
   */
  private StepMeta createRowGeneratorStep( String name, PluginRegistry registry ) {

    // Default the name if it is empty
    String testFileOutputName = ( Utils.isEmpty( name ) ? "generate rows" : name );

    // create the RowGenerator and Step Meta
    RowGeneratorMeta rowGeneratorMeta = new RowGeneratorMeta();
    String rowGeneratorPid = registry.getPluginId( StepPluginType.class, rowGeneratorMeta );
    StepMeta generateRowsStep = new StepMeta( rowGeneratorPid, testFileOutputName, rowGeneratorMeta );

    // Set the field names, types and values
    rowGeneratorMeta.setFieldName( new String[] { "Id", "State", "City" } );
    rowGeneratorMeta.setFieldType( new String[] { "Integer", "String", "String" } );
    rowGeneratorMeta.setValue( new String[] { "1", "Orlando", "Florida" } );
    rowGeneratorMeta.setFieldLength( new int[] { -1, -1, -1 } );
    rowGeneratorMeta.setFieldPrecision( new int[] { -1, -1, -1 } );
    rowGeneratorMeta.setGroup( new String[] { "", "", "" } );
    rowGeneratorMeta.setDecimal( new String[] { "", "", "" } );
    rowGeneratorMeta.setCurrency( new String[] { "", "", "" } );
    rowGeneratorMeta.setFieldFormat( new String[] { "", "", "" } );
    rowGeneratorMeta.setRowLimit( "10" );

    // return the step meta
    return generateRowsStep;
  }

  /**
   * Create a dummy step for this class.
   *
   * @param name
   * @param registry
   * @return
   */
  private StepMeta createDummyStep( String name, PluginRegistry registry ) {
    // Create a dummy step 1 and add it to the tranMeta
    String dummyStepName = "dummy step";
    DummyTransMeta dm1 = new DummyTransMeta();
    String dummyPid1 = registry.getPluginId( StepPluginType.class, dm1 );
    StepMeta dummyStep = new StepMeta( dummyPid1, dummyStepName, dm1 );

    return dummyStep;
  }

  /**
   * Create result data for test case 1. Each Object array in element in list should mirror the data written by the row
   * generator created by the createRowGenerator method.
   *
   * @return list of metadata/data couples of how the result should look like.
   */
  public List<RowMetaAndData> createResultData1() {
    List<RowMetaAndData> list = new ArrayList<RowMetaAndData>();

    RowMetaInterface rowMetaInterface = createResultRowMetaInterface();

    Object[] r1 = new Object[] { new Long( 1L ), "Orlando", "Florida" };
    Object[] r2 = new Object[] { new Long( 1L ), "Orlando", "Florida" };
    Object[] r3 = new Object[] { new Long( 1L ), "Orlando", "Florida" };
    Object[] r4 = new Object[] { new Long( 1L ), "Orlando", "Florida" };
    Object[] r5 = new Object[] { new Long( 1L ), "Orlando", "Florida" };
    Object[] r6 = new Object[] { new Long( 1L ), "Orlando", "Florida" };
    Object[] r7 = new Object[] { new Long( 1L ), "Orlando", "Florida" };
    Object[] r8 = new Object[] { new Long( 1L ), "Orlando", "Florida" };
    Object[] r9 = new Object[] { new Long( 1L ), "Orlando", "Florida" };
    Object[] r10 = new Object[] { new Long( 1L ), "Orlando", "Florida" };

    list.add( new RowMetaAndData( rowMetaInterface, r1 ) );
    list.add( new RowMetaAndData( rowMetaInterface, r2 ) );
    list.add( new RowMetaAndData( rowMetaInterface, r3 ) );
    list.add( new RowMetaAndData( rowMetaInterface, r4 ) );
    list.add( new RowMetaAndData( rowMetaInterface, r5 ) );
    list.add( new RowMetaAndData( rowMetaInterface, r6 ) );
    list.add( new RowMetaAndData( rowMetaInterface, r7 ) );
    list.add( new RowMetaAndData( rowMetaInterface, r8 ) );
    list.add( new RowMetaAndData( rowMetaInterface, r9 ) );
    list.add( new RowMetaAndData( rowMetaInterface, r10 ) );
    return list;
  }

  /**
   * Create result data for test case 1. Each Object array in element in list should mirror the data written by the row
   * generator created by the createRowGenerator method.
   *
   * @return list of metadata/data couples of how the result should look like.
   */
  public List<RowMetaAndData> createResultDataFromObjects( Object[][] objs ) {
    List<RowMetaAndData> list = new ArrayList<RowMetaAndData>();

    RowMetaInterface rowMetaInterface = createResultRowMetaInterface();

    for ( Object[] o : objs ) {
      list.add( new RowMetaAndData( rowMetaInterface, o ) );
    }

    return list;
  }

  /**
   * Creates a RowMetaInterface with a ValueMetaInterface with the name "filename".
   *
   * @return
   */
  public RowMetaInterface createRowMetaInterface() {
    RowMetaInterface rowMetaInterface = new RowMeta();

    ValueMetaInterface[] valuesMeta = { new ValueMetaString( "filename" ), };
    for ( int i = 0; i < valuesMeta.length; i++ ) {
      rowMetaInterface.addValueMeta( valuesMeta[i] );
    }

    return rowMetaInterface;
  }

  /**
   * Creates data... Will add more as I figure what the data is.
   *
   * @param fileName
   * @return
   */
  public List<RowMetaAndData> createData() {
    List<RowMetaAndData> list = new ArrayList<RowMetaAndData>();
    RowMetaInterface rowMetaInterface = createRowMetaInterface();
    Object[] r1 = new Object[] {};
    list.add( new RowMetaAndData( rowMetaInterface, r1 ) );
    return list;
  }

  /**
   * Creates a row meta interface for the fields that are defined by performing a getFields and by checking "Result
   * filenames - Add filenames to result from "Text File Input" dialog.
   *
   * @return
   */
  public RowMetaInterface createResultRowMetaInterface() {
    RowMetaInterface rowMetaInterface = new RowMeta();

    ValueMetaInterface[] valuesMeta =
        { new ValueMetaInteger( "Id" ), new ValueMetaString( "City" ), new ValueMetaString( "State" ) };

    for ( int i = 0; i < valuesMeta.length; i++ ) {
      rowMetaInterface.addValueMeta( valuesMeta[i] );
    }

    return rowMetaInterface;
  }

  private static String getXML1() {
    String xml1 =
      "  <step>"
        + "    <name>Text file output</name>" + "    <type>TextFileOutput</type>" + "    <description/>"
        + "    <distribute>Y</distribute>" + "    <copies>1</copies>" + "         <partitioning>"
        + "           <method>none</method>" + "           <schema_name/>" + "           </partitioning>"
        + "    <separator>;</separator>" + "    <enclosure>&quot;</enclosure>"
        + "    <enclosure_forced>N</enclosure_forced>"
        + "    <enclosure_fix_disabled>N</enclosure_fix_disabled>" + "    <header>Y</header>"
        + "    <footer>N</footer>" + "    <format>DOS</format>" + "    <compression>None</compression>"
        + "    <encoding/>" + "    <endedLine/>" + "    <fileNameInField>N</fileNameInField>"
        + "    <fileNameField/>" + "    <file>" + "      <name>file</name>"
        + "      <is_command>N</is_command>" + "      <servlet_output>N</servlet_output>"
        + "      <do_not_open_new_file_init>N</do_not_open_new_file_init>"
        + "      <extention>txt</extention>" + "      <append>N</append>" + "      <split>N</split>"
        + "      <haspartno>N</haspartno>" + "      <add_date>N</add_date>" + "      <add_time>N</add_time>"
        + "      <SpecifyFormat>N</SpecifyFormat>" + "      <date_time_format/>"
        + "      <add_to_result_filenames>Y</add_to_result_filenames>" + "      <pad>N</pad>"
        + "      <fast_dump>N</fast_dump>" + "      <splitevery>0</splitevery>" + "    </file>"
        + "    <fields>" + "    </fields>" + "     <cluster_schema/>"
        + " <remotesteps>   <input>   </input>   <output>   </output> </remotesteps>    <GUI>"
        + "      <xloc>183</xloc>" + "      <yloc>66</yloc>" + "      <draw>Y</draw>" + "      </GUI>"
        + "    </step>";

    return xml1;
  }

  private StepMeta createTextFileOutputStep( String name, String textFileName, String compression,
    PluginRegistry registry ) {

    // Create a Text File Output step
    String testFileOutputName = name;
    TextFileOutputMeta textFileOutputMeta = new TextFileOutputMeta();
    String textFileInputPid = registry.getPluginId( StepPluginType.class, textFileOutputMeta );
    StepMeta textFileOutputStep = new StepMeta( textFileInputPid, testFileOutputName, textFileOutputMeta );

    // initialize the fields
    TextFileField[] fields = new TextFileField[3];
    for ( int idx = 0; idx < fields.length; idx++ ) {
      fields[idx] = new TextFileField();
    }

    // populate the fields
    // it is important that the setPosition(int)
    // is invoked with the correct position as
    // we are testing the reading of a delimited file.
    fields[0].setName( "id" );
    fields[0].setType( ValueMetaInterface.TYPE_INTEGER );
    fields[0].setFormat( "" );
    fields[0].setLength( -1 );
    fields[0].setPrecision( -1 );
    fields[0].setCurrencySymbol( "" );
    fields[0].setDecimalSymbol( "" );
    fields[0].setTrimType( ValueMetaInterface.TRIM_TYPE_NONE );

    // here we're swapping the order of the last 2 columns
    fields[1].setName( "city" );
    fields[1].setType( ValueMetaInterface.TYPE_STRING );
    fields[1].setFormat( "" );
    fields[1].setLength( -1 );
    fields[1].setPrecision( -1 );
    fields[1].setCurrencySymbol( "" );
    fields[1].setDecimalSymbol( "" );
    fields[1].setTrimType( ValueMetaInterface.TRIM_TYPE_NONE );

    fields[2].setName( "state" );
    fields[2].setType( ValueMetaInterface.TYPE_STRING );
    fields[2].setFormat( "" );
    fields[2].setLength( -1 );
    fields[2].setPrecision( -1 );
    fields[2].setCurrencySymbol( "" );
    fields[2].setDecimalSymbol( "" );
    fields[2].setTrimType( ValueMetaInterface.TRIM_TYPE_NONE );

    // call this to allocate the number of fields
    textFileOutputMeta.allocate( 3 );
    textFileOutputMeta.setOutputFields( fields );

    // set meta properties- these were determined by running Spoon
    // and setting up the transformation we are setting up here.
    // i.e. - the dialog told me what I had to set to avoid
    // NPEs during the transformation.

    // We need a file name so we will generate a temp file
    textFileOutputMeta.setFileName( textFileName );
    textFileOutputMeta.setFileNameInField( false );
    textFileOutputMeta.setExtension( EXTENSION );
    textFileOutputMeta.setEnclosure( "\"" );
    textFileOutputMeta.setFileCompression( compression );
    textFileOutputMeta.setSeparator( SEPARATOR );
    textFileOutputMeta.setFileFormat( TestUtilities.getFileFormat() );
    textFileOutputMeta.setAddToResultFiles( false );
    textFileOutputMeta.setNewline( TestUtilities.getEndOfLineCharacters() );
    textFileOutputMeta.setSeparator( ";" );
    textFileOutputMeta.setEnclosure( "\"" );

    return textFileOutputStep;

  }


  private void readData1Rows( Object[][] rows, BufferedReader input ) {
    int rowCount = 0;
    try {
      String inputLine = input.readLine();
      while ( inputLine != null ) {
        String[] columns = inputLine.split( ";" );
        if ( columns.length != 3 ) {
          fail( "Expected 3 columns, got " + columns.length );
        }
        rows[rowCount][0] = Long.parseLong( columns[0].trim() );
        // column order swapped as expected from outputFields
        rows[rowCount][1] = columns[2];
        rows[rowCount][2] = columns[1];
        rowCount++;
        inputLine = input.readLine();
      }
    } catch ( IOException ioe ) {
      // Ignore errors
    }
  }

  /**
   * Tests output rows
   */
  @Test
  public void testTextFileOutput1() throws Exception {
    KettleEnvironment.init();

    // Create a new transformation...
    //
    TransMeta transMeta = new TransMeta();
    transMeta.setName( "testTextFileOutput1" );
    PluginRegistry registry = PluginRegistry.getInstance();

    // create an injector step
    String injectorStepName = "injector step";
    StepMeta injectorStep = TestUtilities.createInjectorStep( injectorStepName, registry );
    transMeta.addStep( injectorStep );

    // create a row generator step
    StepMeta rowGeneratorStep = createRowGeneratorStep( "Create rows for testTextFileOutput1", registry );
    transMeta.addStep( rowGeneratorStep );

    // create a TransHopMeta for injector and add it to the transMeta
    TransHopMeta hop_injectory_rowGenerator = new TransHopMeta( injectorStep, rowGeneratorStep );
    transMeta.addTransHop( hop_injectory_rowGenerator );

    // create the text file output step
    // but first lets get a filename
    String textFileName = TestUtilities.createEmptyTempFile( "testTextFileOutput1" );
    StepMeta textFileOutputStep =
      createTextFileOutputStep( "text file output step", textFileName, "None", registry );
    transMeta.addStep( textFileOutputStep );

    // create a TransHopMeta for textFileOutputStep and add it to the transMeta
    TransHopMeta hop_RowGenerator_outputTextFile = new TransHopMeta( rowGeneratorStep, textFileOutputStep );
    transMeta.addTransHop( hop_RowGenerator_outputTextFile );

    // Create a dummy step and add it to the tranMeta
    String dummyStepName = "dummy step";
    StepMeta dummyStep = createDummyStep( dummyStepName, registry );
    transMeta.addStep( dummyStep );

    // create a TransHopMeta for the
    TransHopMeta hop_outputTextFile_dummyStep = new TransHopMeta( textFileOutputStep, dummyStep );
    transMeta.addTransHop( hop_outputTextFile_dummyStep );

    // Now execute the transformation...
    Trans trans = new Trans( transMeta );
    trans.prepareExecution( null );

    // Create a row collector and add it to the dummy step interface
    StepInterface dummyStepInterface = trans.getStepInterface( dummyStepName, 0 );
    RowStepCollector dummyRowCollector = new RowStepCollector();
    dummyStepInterface.addRowListener( dummyRowCollector );

    // RowProducer rowProducer = trans.addRowProducer(injectorStepName, 0);
    trans.startThreads();
    trans.waitUntilFinished();

    // Compare the results
    List<RowMetaAndData> resultRows = dummyRowCollector.getRowsWritten();
    List<RowMetaAndData> goldenImageRows = createResultData1();

    try {
      TestUtilities.checkRows( goldenImageRows, resultRows );
    } catch ( TestFailedException tfe ) {
      fail( tfe.getMessage() );
    }
  }

  /**
   * Tests the default setting of createparentfolder to true by creating a new TextFileOutputMeta and verifying that
   * createparentfolder is true
   *
   * @throws Exception
   */
  @Test
  public void testTextFileOutput2() throws Exception {
    KettleEnvironment.init();

    TextFileOutputMeta textFileOutputMeta = new TextFileOutputMeta();
    assertTrue( textFileOutputMeta.isCreateParentFolder() );
    textFileOutputMeta.setDefault();
    assertTrue( textFileOutputMeta.isCreateParentFolder() );
  }

  /**
   * Tests the default setting of createparentfolder to true by creating a new TextFileOutputMeta using a sample XML
   * step (from a real transformation) and verifying that createparentfolder is true
   *
   * @throws Exception
   */
  @Test
  public void testTextFileOutput3() throws Exception {
    KettleEnvironment.init();

    // Create a new transformation from the same XML file
    //
    ByteArrayInputStream xmlStream = new ByteArrayInputStream( getXML1().getBytes( "UTF-8" ) );
    Document doc = XMLHandler.loadXMLFile( xmlStream, null, false, false );
    Node stepnode = XMLHandler.getSubNode( doc, "step" );
    TextFileOutputMeta textFileOutputMeta = new TextFileOutputMeta();
    textFileOutputMeta.loadXML( stepnode, null, (IMetaStore) null );
    assertTrue( textFileOutputMeta.isCreateParentFolder() );
  }

  /**
   * Tests the ZIP output capability of the TextFileOutput step
   *
   * @throws Exception
   */
  @Test
  public void testTextFileOutput4() throws Exception {
    KettleEnvironment.init();

    // Create a new transformation...
    //
    TransMeta transMeta = new TransMeta();
    transMeta.setName( "testTextFileOutput4" );
    PluginRegistry registry = PluginRegistry.getInstance();

    // create an injector step
    String injectorStepName = "injector step";
    StepMeta injectorStep = TestUtilities.createInjectorStep( injectorStepName, registry );
    transMeta.addStep( injectorStep );

    // create a row generator step
    StepMeta rowGeneratorStep = createRowGeneratorStep( "Create rows for testTextFileOutput4", registry );
    transMeta.addStep( rowGeneratorStep );

    // create a TransHopMeta for injector and add it to the transMeta
    TransHopMeta hop_injectory_rowGenerator = new TransHopMeta( injectorStep, rowGeneratorStep );
    transMeta.addTransHop( hop_injectory_rowGenerator );

    // create the text file output step with ZIP compression
    // but first lets get a filename
    String textFileName = "testTextFileOutput4";
    String textFileOutputStepName = "text file output step";
    StepMeta textFileOutputStep = createTextFileOutputStep( textFileOutputStepName, textFileName, "Zip", registry );
    transMeta.addStep( textFileOutputStep );

    // create a TransHopMeta for textFileOutputStep and add it to the transMeta
    TransHopMeta hop_RowGenerator_outputTextFile = new TransHopMeta( rowGeneratorStep, textFileOutputStep );
    transMeta.addTransHop( hop_RowGenerator_outputTextFile );

    // Now execute the transformation...
    Trans trans = new Trans( transMeta );
    trans.prepareExecution( null );

    // Create a row collector and add it to the dummy step interface
    StepInterface dummyStepInterface = trans.getStepInterface( textFileOutputStepName, 0 );
    RowStepCollector dummyRowCollector = new RowStepCollector();
    dummyStepInterface.addRowListener( dummyRowCollector );

    trans.startThreads();
    trans.waitUntilFinished();

    // Compare the results
    List<RowMetaAndData> resultRows = dummyRowCollector.getRowsWritten();
    Object[][] rows = new Object[10][3];
    File f = new File( textFileName + ".zip" );
    f.deleteOnExit();
    try {
      ZipFile zf = new ZipFile( f );
      int zipEntryCount = 0;
      Enumeration<? extends ZipEntry> entries = zf.entries();

      while ( entries.hasMoreElements() ) {
        ZipEntry ze = entries.nextElement();
        zipEntryCount++;
        BufferedReader input = new BufferedReader( new InputStreamReader( zf.getInputStream( ze ) ) );
        readData1Rows( rows, input );
      }

      zf.close();
      assertEquals( zipEntryCount, 1 );

    } catch ( IOException e ) {
      fail( e.getLocalizedMessage() );
    }

    List<RowMetaAndData> outFileRows = createResultDataFromObjects( rows );

    try {
      TestUtilities.checkRows( resultRows, outFileRows );
    } catch ( TestFailedException tfe ) {
      fail( tfe.getMessage() );
    }
  }

  /**
   * Tests the GZIP output capability of the TextFileOutput step
   *
   * @throws Exception
   */
  @Test
  public void testTextFileOutput5() throws Exception {
    KettleEnvironment.init();

    // Create a new transformation...
    //
    TransMeta transMeta = new TransMeta();
    transMeta.setName( "testTextFileOutput5" );
    PluginRegistry registry = PluginRegistry.getInstance();

    // create an injector step
    String injectorStepName = "injector step";
    StepMeta injectorStep = TestUtilities.createInjectorStep( injectorStepName, registry );
    transMeta.addStep( injectorStep );

    // create a row generator step
    StepMeta rowGeneratorStep = createRowGeneratorStep( "Create rows for testTextFileOutput5", registry );
    transMeta.addStep( rowGeneratorStep );

    // create a TransHopMeta for injector and add it to the transMeta
    TransHopMeta hop_injectory_rowGenerator = new TransHopMeta( injectorStep, rowGeneratorStep );
    transMeta.addTransHop( hop_injectory_rowGenerator );

    // create the text file output step with GZIP compression
    // but first lets get a filename
    String textFileName = "testTextFileOutput5";
    String textFileOutputStepName = "text file output step";
    StepMeta textFileOutputStep =
      createTextFileOutputStep( textFileOutputStepName, textFileName, "GZip", registry );
    transMeta.addStep( textFileOutputStep );

    // create a TransHopMeta for textFileOutputStep and add it to the transMeta
    TransHopMeta hop_RowGenerator_outputTextFile = new TransHopMeta( rowGeneratorStep, textFileOutputStep );
    transMeta.addTransHop( hop_RowGenerator_outputTextFile );

    // Now execute the transformation...
    Trans trans = new Trans( transMeta );
    trans.prepareExecution( null );

    // Create a row collector and add it to the dummy step interface
    StepInterface dummyStepInterface = trans.getStepInterface( textFileOutputStepName, 0 );
    RowStepCollector dummyRowCollector = new RowStepCollector();
    dummyStepInterface.addRowListener( dummyRowCollector );

    trans.startThreads();
    trans.waitUntilFinished();

    // Compare the results
    List<RowMetaAndData> resultRows = dummyRowCollector.getRowsWritten();
    Object[][] rows = new Object[10][3];
    File f = new File( textFileName + "." + EXTENSION + ".gz" );
    f.deleteOnExit();
    try {
      FileInputStream fin = new FileInputStream( f );
      GZIPInputStream gzis = new GZIPInputStream( fin );
      InputStreamReader xover = new InputStreamReader( gzis );
      BufferedReader input = new BufferedReader( xover );

      readData1Rows( rows, input );

      fin.close();

    } catch ( IOException e ) {
      fail( e.getLocalizedMessage() );
    }

    List<RowMetaAndData> outFileRows = createResultDataFromObjects( rows );

    try {
      TestUtilities.checkRows( resultRows, outFileRows );
    } catch ( TestFailedException tfe ) {
      fail( tfe.getMessage() );
    }
  }

  /**
   * Tests the normal output capability of the TextFileOutput step
   *
   * @throws Exception
   */
  @Test
  public void testTextFileOutput6() throws Exception {
    KettleEnvironment.init();

    // Create a new transformation...
    //
    TransMeta transMeta = new TransMeta();
    transMeta.setName( "testTextFileOutput6" );
    PluginRegistry registry = PluginRegistry.getInstance();

    // create an injector step
    String injectorStepName = "injector step";
    StepMeta injectorStep = TestUtilities.createInjectorStep( injectorStepName, registry );
    transMeta.addStep( injectorStep );

    // create a row generator step
    StepMeta rowGeneratorStep = createRowGeneratorStep( "Create rows for testTextFileOutput5", registry );
    transMeta.addStep( rowGeneratorStep );

    // create a TransHopMeta for injector and add it to the transMeta
    TransHopMeta hop_injectory_rowGenerator = new TransHopMeta( injectorStep, rowGeneratorStep );
    transMeta.addTransHop( hop_injectory_rowGenerator );

    // create the text file output step with no compression
    // but first lets get a filename
    String textFileName = "testTextFileOutput6";
    String textFileOutputStepName = "text file output step";
    StepMeta textFileOutputStep =
      createTextFileOutputStep( textFileOutputStepName, textFileName, "None", registry );
    transMeta.addStep( textFileOutputStep );

    // create a TransHopMeta for textFileOutputStep and add it to the transMeta
    TransHopMeta hop_RowGenerator_outputTextFile = new TransHopMeta( rowGeneratorStep, textFileOutputStep );
    transMeta.addTransHop( hop_RowGenerator_outputTextFile );

    // Now execute the transformation...
    Trans trans = new Trans( transMeta );
    trans.prepareExecution( null );

    // Create a row collector and add it to the dummy step interface
    StepInterface dummyStepInterface = trans.getStepInterface( textFileOutputStepName, 0 );
    RowStepCollector dummyRowCollector = new RowStepCollector();
    dummyStepInterface.addRowListener( dummyRowCollector );

    trans.startThreads();
    trans.waitUntilFinished();

    // Compare the results
    List<RowMetaAndData> resultRows = dummyRowCollector.getRowsWritten();
    Object[][] rows = new Object[10][3];
    File f = new File( textFileName + "." + EXTENSION );
    f.deleteOnExit();
    try {
      FileInputStream fin = new FileInputStream( f );
      InputStreamReader xover = new InputStreamReader( fin );
      BufferedReader input = new BufferedReader( xover );

      readData1Rows( rows, input );

      fin.close();

    } catch ( IOException e ) {
      fail( e.getLocalizedMessage() );
    }

    List<RowMetaAndData> outFileRows = createResultDataFromObjects( rows );

    try {
      TestUtilities.checkRows( resultRows, outFileRows );
    } catch ( TestFailedException tfe ) {
      fail( tfe.getMessage() );
    }
  }
}
