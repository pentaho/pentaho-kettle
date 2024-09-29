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
 ******************************************************************************/

package org.pentaho.di.trans.steps.jsonoutput;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import junit.framework.TestCase;
import org.apache.commons.io.FileUtils;
import org.json.simple.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.pentaho.di.TestUtilities;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaInteger;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.trans.RowStepCollector;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransHopMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.dummytrans.DummyTransMeta;
import org.pentaho.di.trans.steps.mock.StepMockHelper;
import org.pentaho.di.trans.steps.rowgenerator.RowGeneratorMeta;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * This class was a "copy and modification" of Kettle's JsonOutputTests.
 * 
 * @author Hendy Irawan <hendy@soluvas.com> Modified by Sean Flatley, removing dependency on external text file to hold
 *         expected results and modifying code to handle "Compatibility Mode".
 */
public class JsonOutputTest extends TestCase {

  private static final String EXPECTED_NON_COMPATIBILITY_JSON =
      "{\"data\":[{\"id\":1,\"state\":\"Florida\",\"city\":\"Orlando\"},"
          + "{\"id\":1,\"state\":\"Florida\",\"city\":\"Orlando\"},"
          + "{\"id\":1,\"state\":\"Florida\",\"city\":\"Orlando\"},"
          + "{\"id\":1,\"state\":\"Florida\",\"city\":\"Orlando\"},"
          + "{\"id\":1,\"state\":\"Florida\",\"city\":\"Orlando\"},"
          + "{\"id\":1,\"state\":\"Florida\",\"city\":\"Orlando\"},"
          + "{\"id\":1,\"state\":\"Florida\",\"city\":\"Orlando\"},"
          + "{\"id\":1,\"state\":\"Florida\",\"city\":\"Orlando\"},"
          + "{\"id\":1,\"state\":\"Florida\",\"city\":\"Orlando\"},"
          + "{\"id\":1,\"state\":\"Florida\",\"city\":\"Orlando\"}]}";

  private static final String EXPECTED_COMPATIBILITY_MODE_JSON =
      "{\"data\":[{\"id\":1},{\"state\":\"Florida\"},{\"city\":\"Orlando\"},{\"id\":1},{\"state\":\"Florida\"},"
          + "{\"city\":\"Orlando\"},{\"id\":1},{\"state\":\"Florida\"},{\"city\":\"Orlando\"},{\"id\":1},"
          + "{\"state\":\"Florida\"},{\"city\":\"Orlando\"},{\"id\":1},{\"state\":\"Florida\"},"
          + "{\"city\":\"Orlando\"},{\"id\":1},{\"state\":\"Florida\"},{\"city\":\"Orlando\"},{\"id\":1},"
          + "{\"state\":\"Florida\"},{\"city\":\"Orlando\"},{\"id\":1},{\"state\":\"Florida\"},"
          + "{\"city\":\"Orlando\"},{\"id\":1},{\"state\":\"Florida\"},{\"city\":\"Orlando\"},{\"id\":1},"
          + "{\"state\":\"Florida\"},{\"city\":\"Orlando\"}]}";

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
    rowGeneratorMeta.setValue( new String[] { "1", "Florida", "Orlando" } );
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
      { new ValueMetaInteger( "Id" ), new ValueMetaString( "State" ), new ValueMetaString( "City" ) };

    for ( int i = 0; i < valuesMeta.length; i++ ) {
      rowMetaInterface.addValueMeta( valuesMeta[i] );
    }

    return rowMetaInterface;
  }

  private StepMeta createJsonOutputStep( String name, String jsonFileName, PluginRegistry registry ) {

    // Create a Text File Output step
    String testFileOutputName = name;
    JsonOutputMeta jsonOutputMeta = new JsonOutputMeta();
    String textFileInputPid = registry.getPluginId( StepPluginType.class, jsonOutputMeta );
    StepMeta jsonOutputStep = new StepMeta( textFileInputPid, testFileOutputName, jsonOutputMeta );

    // initialize the fields
    JsonOutputField[] fields = new JsonOutputField[3];
    for ( int idx = 0; idx < fields.length; idx++ ) {
      fields[idx] = new JsonOutputField();
    }

    // populate the fields
    // it is important that the setPosition(int)
    // is invoked with the correct position as
    // we are testing the reading of a delimited file.
    fields[0].setFieldName( "id" );
    fields[0].setElementName( "id" );

    fields[1].setFieldName( "state" );
    fields[1].setElementName( "state" );

    fields[2].setFieldName( "city" );
    fields[2].setElementName( "city" );

    // call this to allocate the number of fields
    jsonOutputMeta.allocate( fields.length );
    jsonOutputMeta.setOutputFields( fields );

    // set meta properties- these were determined by running Spoon
    // and setting up the transformation we are setting up here.
    // i.e. - the dialog told me what I had to set to avoid
    // NPEs during the transformation.

    // We need a file name so we will generate a temp file
    jsonOutputMeta.setOperationType( JsonOutputMeta.OPERATION_TYPE_WRITE_TO_FILE );
    jsonOutputMeta.setOutputValue( "data" );
    jsonOutputMeta.setFileName( jsonFileName );
    jsonOutputMeta.setExtension( "js" );
    jsonOutputMeta.setNrRowsInBloc( "0" ); // a single "data" contains an array of all records
    jsonOutputMeta.setJsonBloc( "data" );

    return jsonOutputStep;
  }

  public String test( boolean compatibilityMode ) throws Exception {
    KettleEnvironment.init();

    // Create a new transformation...
    //
    TransMeta transMeta = new TransMeta();
    transMeta.setName( "testJsonOutput" );
    PluginRegistry registry = PluginRegistry.getInstance();

    // create an injector step
    String injectorStepName = "injector step";
    StepMeta injectorStep = TestUtilities.createInjectorStep( injectorStepName, registry );
    transMeta.addStep( injectorStep );

    // create a row generator step
    StepMeta rowGeneratorStep = createRowGeneratorStep( "Create rows for testJsonOutput1", registry );
    transMeta.addStep( rowGeneratorStep );

    // create a TransHopMeta for injector and add it to the transMeta
    TransHopMeta hop_injectory_rowGenerator = new TransHopMeta( injectorStep, rowGeneratorStep );
    transMeta.addTransHop( hop_injectory_rowGenerator );

    // create the json output step
    // but first lets get a filename
    String jsonFileName = TestUtilities.createEmptyTempFile( "testJsonOutput1_" );
    StepMeta jsonOutputStep = createJsonOutputStep( "json output step", jsonFileName, registry );
    ( (JsonOutputMeta) jsonOutputStep.getStepMetaInterface() ).setCompatibilityMode( compatibilityMode );
    transMeta.addStep( jsonOutputStep );

    // create a TransHopMeta for jsonOutputStep and add it to the transMeta
    TransHopMeta hop_RowGenerator_outputTextFile = new TransHopMeta( rowGeneratorStep, jsonOutputStep );
    transMeta.addTransHop( hop_RowGenerator_outputTextFile );

    // Create a dummy step and add it to the tranMeta
    String dummyStepName = "dummy step";
    StepMeta dummyStep = createDummyStep( dummyStepName, registry );
    transMeta.addStep( dummyStep );

    // create a TransHopMeta for the
    TransHopMeta hop_outputJson_dummyStep = new TransHopMeta( jsonOutputStep, dummyStep );
    transMeta.addTransHop( hop_outputJson_dummyStep );

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

    // get the results and return it
    File outputFile = new File( jsonFileName + ".js" );
    String jsonStructure = FileUtils.readFileToString( outputFile );

    return jsonStructure;
  }

  // The actual tests

  public void testNonCompatibilityMode() throws Exception {
    String jsonStructure = test( false );
    Assert.assertTrue( jsonEquals( EXPECTED_NON_COMPATIBILITY_JSON, jsonStructure ) );
  }

  public void testCompatibilityMode() throws Exception {
    String jsonStructure = test( true );
    Assert.assertEquals( EXPECTED_COMPATIBILITY_MODE_JSON, jsonStructure );
  }

  /* PDI-7243 */
  public void testNpeIsNotThrownOnNullInput() throws Exception {
    StepMockHelper<JsonOutputMeta, JsonOutputData> mockHelper =
        new StepMockHelper<JsonOutputMeta, JsonOutputData>( "jsonOutput", JsonOutputMeta.class, JsonOutputData.class );
    when( mockHelper.logChannelInterfaceFactory.create( any(), any( LoggingObjectInterface.class ) ) ).thenReturn(
        mockHelper.logChannelInterface );
    when( mockHelper.trans.isRunning() ).thenReturn( true );
    when( mockHelper.stepMeta.getStepMetaInterface() ).thenReturn( new JsonOutputMeta() );

    JsonOutput step =
        new JsonOutput( mockHelper.stepMeta, mockHelper.stepDataInterface, 0, mockHelper.transMeta, mockHelper.trans );
    step = spy( step );

    doReturn( null ).when( step ).getRow();

    step.processRow( mockHelper.processRowsStepMetaInterface, mockHelper.processRowsStepDataInterface );
  }

  public void testEmptyDoesntWriteToFile() throws Exception {
    StepMockHelper<JsonOutputMeta, JsonOutputData> mockHelper =
            new StepMockHelper<JsonOutputMeta, JsonOutputData>( "jsonOutput", JsonOutputMeta.class, JsonOutputData.class );
    when( mockHelper.logChannelInterfaceFactory.create( any(), any( LoggingObjectInterface.class ) ) ).thenReturn(
            mockHelper.logChannelInterface );
    when( mockHelper.trans.isRunning() ).thenReturn( true );
    when( mockHelper.stepMeta.getStepMetaInterface() ).thenReturn( new JsonOutputMeta() );

    JsonOutputData stepData = new JsonOutputData();
    stepData.writeToFile = true;
    JsonOutput step =
            new JsonOutput( mockHelper.stepMeta, stepData, 0, mockHelper.transMeta, mockHelper.trans );
    step = spy( step );

    doReturn( null ).when( step ).getRow();
    doReturn( true ).when( step ).openNewFile();
    doReturn( true ).when( step ).closeFile();

    step.processRow( mockHelper.processRowsStepMetaInterface, stepData );
    verify( step, times( 0 ) ).openNewFile();
    verify( step, times( 0 ) ).closeFile();
  }

  @SuppressWarnings( "unchecked" )
  public void testWriteToFile() throws Exception {
    StepMockHelper<JsonOutputMeta, JsonOutputData> mockHelper =
            new StepMockHelper<JsonOutputMeta, JsonOutputData>( "jsonOutput", JsonOutputMeta.class, JsonOutputData.class );
    when( mockHelper.logChannelInterfaceFactory.create( any(), any( LoggingObjectInterface.class ) ) ).thenReturn(
            mockHelper.logChannelInterface );
    when( mockHelper.trans.isRunning() ).thenReturn( true );
    when( mockHelper.stepMeta.getStepMetaInterface() ).thenReturn( new JsonOutputMeta() );

    JsonOutputData stepData = new JsonOutputData();
    stepData.writeToFile = true;
    JSONObject jsonObject = new JSONObject();
    jsonObject.put( "key", "value" );
    stepData.ja.add( jsonObject );
    stepData.writer = mock( Writer.class );

    JsonOutput step =
            new JsonOutput( mockHelper.stepMeta, stepData, 0, mockHelper.transMeta, mockHelper.trans );
    step = spy( step );

    doReturn( null ).when( step ).getRow();
    doReturn( true ).when( step ).openNewFile();
    doReturn( true ).when( step ).closeFile();
    doNothing().when( stepData.writer ).write( anyString() );

    step.processRow( mockHelper.processRowsStepMetaInterface, stepData );
    verify( step ).openNewFile();
    verify( step ).closeFile();
  }

  /**
   * compare json (deep equals ignoring order)
   */
  protected boolean jsonEquals( String json1, String json2 ) throws Exception {
    ObjectMapper om = new ObjectMapper();
    JsonNode parsedJson1 = om.readTree( json1 );
    JsonNode parsedJson2 = om.readTree( json2 );
    return parsedJson1.equals( parsedJson2 );
  }

  @Test
  public void testBuildFilenameWithForceSameOutputFile() {

    StepMeta stepMeta = mock( StepMeta.class );
    JsonOutputData jsonOutputData = mock( JsonOutputData.class );
    JsonOutputMeta jsonOutputMeta = mock( JsonOutputMeta.class );
    int copyNr = 1;
    TransMeta transMeta = mock( TransMeta.class );
    Trans trans = mock( Trans.class );
    RowMetaInterface rowMetaInterface = mock( RowMetaInterface.class );

    when( stepMeta.getName() ).thenReturn( UUID.randomUUID().toString() );
    when( stepMeta.hasTerminator() ).thenReturn( false );
    when( transMeta.findStep( any( String.class ) ) ).thenReturn( stepMeta );
    when( stepMeta.getStepMetaInterface() ).thenReturn( jsonOutputMeta );

    JsonOutput jsonOutput = spy( new JsonOutput( stepMeta, jsonOutputData, copyNr, transMeta, trans ) );
    ReflectionTestUtils.setField( jsonOutput, "meta", jsonOutputMeta );

    System.setProperty( Const.KETTLE_JSON_OUTPUT_FORCE_SAME_OUTPUT_FILE, "Y" );
    jsonOutput.buildFilename();

    verify( jsonOutputMeta, times( 1 ) ).buildFilename( any(), any() );
  }

  @Test
  public void testBuildFilenameWithoutForceSameOutputFile() {

    StepMeta stepMeta = mock( StepMeta.class );
    JsonOutputData jsonOutputData = mock( JsonOutputData.class );
    JsonOutputMeta jsonOutputMeta = mock( JsonOutputMeta.class );
    int copyNr = 1;
    TransMeta transMeta = mock( TransMeta.class );
    Trans trans = mock( Trans.class );
    RowMetaInterface rowMetaInterface = mock( RowMetaInterface.class );

    when( stepMeta.getName() ).thenReturn( UUID.randomUUID().toString() );
    when( stepMeta.hasTerminator() ).thenReturn( false );
    when( transMeta.findStep( any( String.class ) ) ).thenReturn( stepMeta );
    when( stepMeta.getStepMetaInterface() ).thenReturn( jsonOutputMeta );
    when( jsonOutputMeta.getParentStepMeta() ).thenReturn( stepMeta );

    JsonOutput jsonOutput = spy( new JsonOutput( stepMeta, jsonOutputData, copyNr, transMeta, trans ) );
    ReflectionTestUtils.setField( jsonOutput, "meta", jsonOutputMeta );
    ReflectionTestUtils.setField( jsonOutput, "data", jsonOutputData );

    System.setProperty( Const.KETTLE_JSON_OUTPUT_FORCE_SAME_OUTPUT_FILE, "N" );
    jsonOutput.buildFilename();

    verify( jsonOutputMeta, times( 0 ) ).buildFilename( anyString(), any( Date.class ) );
  }
}
