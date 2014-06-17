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

package org.pentaho.di.trans.steps.jsonoutput;

import java.io.File;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.junit.Assert;
import org.junit.Test;
import org.pentaho.di.TestUtilities;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.RowStepCollector;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransHopMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.dummytrans.DummyTransMeta;
import org.pentaho.di.trans.steps.rowgenerator.RowGeneratorMeta;

/**
 * This class was a "copy and modification" of Kettle's JsonOutputTests.
 * 
 * @author Hendy Irawan <hendy@soluvas.com> Modified by Sean Flatley, removing dependency on external text file to hold
 *         expected results and modifying code to handle "Compatibility Mode".
 */
public class JsonOutputTest {

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

  private static final String EXPECTED_COMP_NO_BLOC = "{\"\":[{\"id\":1},{\"state\":\"Florida\"},"
      + "{\"city\":\"Orlando\"},{\"id\":1},{\"state\":\"Florida\"},{\"city\":\"Orlando\"},"
      + "{\"id\":1},{\"state\":\"Florida\"},{\"city\":\"Orlando\"},{\"id\":1},"
      + "{\"state\":\"Florida\"},{\"city\":\"Orlando\"},{\"id\":1},{\"state\":\"Florida\"},"
      + "{\"city\":\"Orlando\"},{\"id\":1},{\"state\":\"Florida\"},{\"city\":\"Orlando\"},"
      + "{\"id\":1},{\"state\":\"Florida\"},{\"city\":\"Orlando\"},{\"id\":1},"
      + "{\"state\":\"Florida\"},{\"city\":\"Orlando\"},{\"id\":1},{\"state\":\"Florida\"},"
      + "{\"city\":\"Orlando\"},{\"id\":1},{\"state\":\"Florida\"},{\"city\":\"Orlando\"}]}";

  private static final String EXPECTED_NONCOMP_NO_BLOC = "[{\"id\":1,\"state\":\"Florida\",\"city\":\"Orlando\"},"
      + "{\"id\":1,\"state\":\"Florida\",\"city\":\"Orlando\"},"
      + "{\"id\":1,\"state\":\"Florida\",\"city\":\"Orlando\"},"
      + "{\"id\":1,\"state\":\"Florida\",\"city\":\"Orlando\"},"
      + "{\"id\":1,\"state\":\"Florida\",\"city\":\"Orlando\"},"
      + "{\"id\":1,\"state\":\"Florida\",\"city\":\"Orlando\"},"
      + "{\"id\":1,\"state\":\"Florida\",\"city\":\"Orlando\"},"
      + "{\"id\":1,\"state\":\"Florida\",\"city\":\"Orlando\"},"
      + "{\"id\":1,\"state\":\"Florida\",\"city\":\"Orlando\"},"
      + "{\"id\":1,\"state\":\"Florida\",\"city\":\"Orlando\"}]";

  /**
   * Creates a row generator step for this class..
   * 
   * @param name
   * @param registry
   * @return
   */
  private StepMeta createRowGeneratorStep( String name, PluginRegistry registry, String amount ) {

    // Default the name if it is empty
    String testFileOutputName = ( Const.isEmpty( name ) ? "generate rows" : name );

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
    rowGeneratorMeta.setRowLimit( amount );

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
   * Creates a RowMetaInterface with a ValueMetaInterface with the name "filename".
   * 
   * @return
   */
  public RowMetaInterface createRowMetaInterface() {
    RowMetaInterface rowMetaInterface = new RowMeta();

    ValueMetaInterface[] valuesMeta = { new ValueMeta( "filename", ValueMeta.TYPE_STRING ), };
    for ( int i = 0; i < valuesMeta.length; i++ ) {
      rowMetaInterface.addValueMeta( valuesMeta[i] );
    }

    return rowMetaInterface;
  }

  /**
   * Creates a row meta interface for the fields that are defined by performing a getFields and by checking "Result
   * filenames - Add filenames to result from "Text File Input" dialog.
   * 
   * @return
   */
  public RowMetaInterface createResultRowMetaInterface() {
    RowMetaInterface rowMetaInterface = new RowMeta();

    ValueMetaInterface[] valuesMeta = { new ValueMeta( "Id", ValueMeta.TYPE_INTEGER ),
      new ValueMeta( "State", ValueMeta.TYPE_STRING ), new ValueMeta( "City", ValueMeta.TYPE_STRING ) };

    for ( int i = 0; i < valuesMeta.length; i++ ) {
      rowMetaInterface.addValueMeta( valuesMeta[i] );
    }

    return rowMetaInterface;
  }

  private StepMeta createJsonOutputStep( String name, String jsonFileName, PluginRegistry registry, String jsonBlock ) {

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
    jsonOutputMeta.setJsonBloc( jsonBlock );

    return jsonOutputStep;

  }

  /**
   * 
   * @param compatibilityMode
   * @param jsonBlock - json block name
   * @param amount - amount of data to generate
   * @param nbrRowsInBlock - number rows in a block
   * @return
   * @throws Exception
   */
  public String test( boolean compatibilityMode, String jsonBlock, String amount, String nbrRowsInBlock ) throws Exception {
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
    StepMeta rowGeneratorStep = createRowGeneratorStep( "Create rows for testJsonOutput1", registry, amount );
    transMeta.addStep( rowGeneratorStep );

    // create a TransHopMeta for injector and add it to the transMeta
    TransHopMeta hop_injectory_rowGenerator = new TransHopMeta( injectorStep, rowGeneratorStep );
    transMeta.addTransHop( hop_injectory_rowGenerator );

    // create the json output step
    // but first lets get a filename
    String jsonFileName = TestUtilities.createEmptyTempFile( "testJsonOutput1_" );
    StepMeta jsonOutputStep = createJsonOutputStep( "json output step", jsonFileName, registry, jsonBlock );
    ( (JsonOutputMeta) jsonOutputStep.getStepMetaInterface() ).setCompatibilityMode( compatibilityMode );
    ( (JsonOutputMeta) jsonOutputStep.getStepMetaInterface() ).setNrRowsInBloc( nbrRowsInBlock );
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

  /**
   * PDI-7159 test that Json output step can produce backward compatible json.
   * 
   * @throws Exception
   */
  @Test
  public void testNonCompatibilityMode() throws Exception {
    String jsonStructure = test( false, "data", "10", "0" );
    ObjectMapper mapper = new ObjectMapper();
    JsonNode tree1 = mapper.readTree( EXPECTED_NON_COMPATIBILITY_JSON );
    JsonNode tree2 = mapper.readTree( jsonStructure );
    Assert.assertEquals( "Json objects is equals for backward compatibility mode", tree1, tree2 );
  }

  /**
   * PDI-7159 test that Json output step can produce fixed compatible json.
   * 
   * @throws Exception
   */
  @Test
  public void testCompatibilityMode() throws Exception {
    String jsonStructure = test( true, "data", "10", "0" );
    ObjectMapper mapper = new ObjectMapper();
    JsonNode tree1 = mapper.readTree( EXPECTED_COMPATIBILITY_MODE_JSON );
    JsonNode tree2 = mapper.readTree( jsonStructure );
    Assert.assertEquals( "Json objects are equals", tree1, tree2 );
  }

  /**
   * PDI-7243 test that Json output step can produce backward compatible json If json block are not specified it will be
   * produced as array.
   * 
   * @throws Exception
   * @see {@link #EXPECTED_NONCOMP_NO_BLOC}
   */
  @Test
  public void testNonCompatibilityModeNoBlock() throws Exception {
    String jsonStructure = test( false, "", "10", "0" );
    ObjectMapper mapper = new ObjectMapper();
    JsonNode tree1 = mapper.readTree( EXPECTED_NONCOMP_NO_BLOC );
    JsonNode tree2 = mapper.readTree( jsonStructure );
    Assert.assertEquals( "Json objects is equals for backward compatibility mode" + "with no block name specified",
        tree1, tree2 );
  }

  /**
   * PDI-7243 test that Json output step can produce fixed compatible json If json block are not specified it will be
   * produced as array.
   * 
   * @throws Exception
   * @see {@link #EXPECTED_COMP_NO_BLOC}
   */
  @Test
  public void testCompatibilityModeNoBlock() throws Exception {
    String jsonStructure = test( true, "", "10", "0" );
    ObjectMapper mapper = new ObjectMapper();
    JsonNode tree1 = mapper.readTree( EXPECTED_COMP_NO_BLOC );
    JsonNode tree2 = mapper.readTree( jsonStructure );
    Assert.assertEquals( "Json objects are equals with no block name specified", tree1, tree2 );
  }

  /**
   * PDI-7243 test that Json output step in case of no rows
   * 
   * @throws Exception
   */
  @Test
  public void testNoRowsAvailable() throws Exception {
    String jsonStructure = test( false, "", "0", "0" );
    ObjectMapper mapper = new ObjectMapper();
    JsonNode tree1 = mapper.readTree( "[]" );
    JsonNode tree2 = mapper.readTree( jsonStructure );
    Assert.assertEquals( "Json objects are equals if output object is empty", tree1, tree2 );
  }

  /**
   * PDI-8395 - test that if number rows in a block is limited output file is consistent.
   * 
   * Since step output file just dump every output json value we need to do some magic to discover that output dump file
   * is correct.
   * 
   * @throws Exception
   */
  @Test
  public void testOutputFileIsConsistent() throws Exception {
    String jsonStructure = test( false, "", "3", "2" );
    // hack to handle separate objects
    jsonStructure = jsonStructure.replaceAll( "\\]\\s*\\[", "," );
    ObjectMapper mapper = new ObjectMapper();
    List<Cont> obj = mapper.readValue( jsonStructure, new TypeReference<List<Cont>>() {
    } );
    Assert.assertEquals( "Json objects are equals if output object is empty", 3, obj.size() );
  }

  /**
   * Local helper class to parse json avoiding blind String comparisons.
   *
   */
  static class Cont {
    public Cont() {
    }

    int id;

    public int getId() {
      return id;
    }

    public void setId( int id ) {
      this.id = id;
    }

    public String getState() {
      return state;
    }

    public void setState( String state ) {
      this.state = state;
    }

    public String getCity() {
      return city;
    }

    public void setCity( String city ) {
      this.city = city;
    }

    String state;
    String city;
  }

}
