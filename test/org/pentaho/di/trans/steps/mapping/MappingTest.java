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

package org.pentaho.di.trans.steps.mapping;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;

import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.ObjectLocationSpecificationMethod;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.trans.RowStepCollector;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransHopMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepIOMetaInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.rowgenerator.RowGeneratorMeta;

public class MappingTest extends TestCase {

  /**
   * Builds a {@link org.pentaho.di.trans.steps.rowgenerator.RowGenerator} Step 
   * with a single String field.
   *
   * @param registry
   *          Plugin Registry.
   * @param stepName
   *          Name to use for step
   * @return {@link StepMeta} for a Row Generator step.
   */
  private StepMeta buildRowGeneratorStep( PluginRegistry registry, String stepName ) {
    RowGeneratorMeta rm = new RowGeneratorMeta();

    // Set the information of the row generator.
    String rowGeneratorPid = registry.getPluginId( StepPluginType.class, rm );
    StepMeta rowGeneratorStep = new StepMeta( rowGeneratorPid, stepName, rm );

    String[] fieldName = { "string" };
    String[] type = { "String" };
    String[] value = { "string_value" };
    String[] fieldFormat = { "" };
    String[] group = { "" };
    String[] decimal = { "" };
    String[] currency = { "", };
    int[] intDummies = { -1, -1, -1 };
    boolean[] setEmptystring = { false, false, false };

    rm.setDefault();
    rm.setFieldName( fieldName );
    rm.setFieldType( type );
    rm.setValue( value );
    rm.setFieldLength( intDummies );
    rm.setFieldPrecision( intDummies );
    rm.setRowLimit( "1" );
    rm.setFieldFormat( fieldFormat );
    rm.setGroup( group );
    rm.setDecimal( decimal );
    rm.setCurrency( currency );
    rm.setEmptyString( setEmptystring );

    return rowGeneratorStep;
  }

  private MappingIODefinition createMappingDef( String inputStepName, String mappingStepName,
    String sourceValueName, String targetValueName ) {
    MappingIODefinition def = new MappingIODefinition();
    def.setInputStepname( inputStepName );
    def.setOutputStepname( mappingStepName );
    def.setValueRenames( Collections.singletonList( new MappingValueRename( sourceValueName, targetValueName ) ) );
    return def;
  }

  /**
   * Tests that info steps are correctly identified via StepMetaInterface.getStepIOMeta()
   */
  public void testInfoStreams_single() throws Exception {
    KettleEnvironment.init();
    PluginRegistry registry = PluginRegistry.getInstance();

    //
    // Create a new transformation with a row generator that feeds a Mapping (Sub-Transformation) Step
    //
    TransMeta transMeta = new TransMeta();
    transMeta.setName( "Mapping Info Test" );
    StepMeta rowGenerator = buildRowGeneratorStep( registry, "Generate Rows" );
    transMeta.addStep( rowGenerator );

    String mappingName = "mapping";
    MappingMeta mappingMeta = new MappingMeta();
    mappingMeta.setSpecificationMethod( ObjectLocationSpecificationMethod.FILENAME );
    mappingMeta.setFileName( "test/org/pentaho/di/trans/steps/mapping/subtrans.ktr" );
    String mappingInputStepName = "input";
    mappingMeta.setInputMappings( Collections.singletonList( createMappingDef(
      rowGenerator.getName(), mappingInputStepName, "string", "a" ) ) );
    String mappingPid = registry.getPluginId( StepPluginType.class, mappingMeta );
    StepMeta mapping = new StepMeta( mappingPid, mappingName, mappingMeta );
    transMeta.addStep( mapping );

    TransHopMeta hopGeneratorToMapping = new TransHopMeta( rowGenerator, mapping );
    transMeta.addTransHop( hopGeneratorToMapping );

    Trans trans = new Trans( transMeta );
    trans.prepareExecution( null );

    // Mimic how a transformation is loaded and initialized from TransMeta.loadXML() or
    // KettleDatabaseRepositoryTransDelegate.loadTransformation()
    // so the StepMeta references are wired up in the MappingMeta properly
    // (Copied from TransMeta.loadXML())
    for ( int i = 0; i < transMeta.nrSteps(); i++ ) {
      StepMeta stepMeta = transMeta.getStep( i );
      StepMetaInterface sii = stepMeta.getStepMetaInterface();
      if ( sii != null ) {
        sii.searchInfoAndTargetSteps( transMeta.getSteps() );
      }
    }

    // Verify the transformation was configured properly
    assertEquals( "Transformation not initialized properly", 2, transMeta.nrSteps() );

    StepMeta meta = transMeta.getStep( 1 );
    assertTrue( "Transformation not initialized properly", meta.getStepMetaInterface() instanceof MappingMeta );

    MappingMeta loadedMappingMeta = (MappingMeta) meta.getStepMetaInterface();
    assertEquals( "Expected a single input mapping definition", 1, loadedMappingMeta.getInputMappings().size() );

    StepIOMetaInterface ioMeta = loadedMappingMeta.getStepIOMeta();
    assertEquals( "Expected a single Info Stream", 1, ioMeta.getInfoStreams().size() );
    assertEquals( "Expected a single Info Step", 1, loadedMappingMeta.getInfoSteps().length );

    // Verify the transformation can be executed
    StepInterface si = trans.getStepInterface( mappingName, 0 );
    RowStepCollector rc = new RowStepCollector();
    si.addRowListener( rc );

    trans.startThreads();
    trans.waitUntilFinished();

    assertEquals( 1, rc.getRowsRead().size() );
    assertEquals( 1, rc.getRowsWritten().size() );
  }

  /**
   * Tests that an input step that is a main data path is not flagged as an info stream
   */
  public void testInfoStreams_with_main_data_path() throws Exception {
    KettleEnvironment.init();
    PluginRegistry registry = PluginRegistry.getInstance();

    //
    // Create a new transformation with a row generator that feeds a Mapping (Sub-Transformation) Step
    //
    TransMeta transMeta = new TransMeta();
    transMeta.setName( "Mapping Info Test" );

    StepMeta rowGenerator = buildRowGeneratorStep( registry, "Generate Rows" );
    transMeta.addStep( rowGenerator );

    StepMeta rowGeneratorMain = buildRowGeneratorStep( registry, "Generate Rows Main" );
    transMeta.addStep( rowGeneratorMain );

    String mappingName = "mapping";
    MappingMeta mappingMeta = new MappingMeta();
    mappingMeta.setSpecificationMethod( ObjectLocationSpecificationMethod.FILENAME );
    mappingMeta.setFileName( "test/org/pentaho/di/trans/steps/mapping/subtrans.ktr" );
    List<MappingIODefinition> inputMappings = new ArrayList<MappingIODefinition>();
    String mappingInputStepName = "input";
    inputMappings.add( createMappingDef( rowGenerator.getName(), mappingInputStepName, "string", "a" ) );

    // Create the main data path mapping
    MappingIODefinition mainMappingDef =
      createMappingDef( rowGeneratorMain.getName(), mappingInputStepName, "string", "a" );
    mainMappingDef.setMainDataPath( true );
    inputMappings.add( mainMappingDef );

    mappingMeta.setInputMappings( inputMappings );
    String mappingPid = registry.getPluginId( StepPluginType.class, mappingMeta );
    StepMeta mapping = new StepMeta( mappingPid, mappingName, mappingMeta );
    transMeta.addStep( mapping );

    TransHopMeta hopGeneratorToMapping = new TransHopMeta( rowGenerator, mapping );
    transMeta.addTransHop( hopGeneratorToMapping );
    hopGeneratorToMapping = new TransHopMeta( rowGeneratorMain, mapping );
    transMeta.addTransHop( hopGeneratorToMapping );

    Trans trans = new Trans( transMeta );
    trans.prepareExecution( null );

    // Mimic how a transformation is loaded and initialized from TransMeta.loadXML() or
    // KettleDatabaseRepositoryTransDelegate.loadTransformation()
    // so the StepMeta references are wired up in the MappingMeta properly
    // (Copied from TransMeta.loadXML())
    for ( int i = 0; i < transMeta.nrSteps(); i++ ) {
      StepMeta stepMeta = transMeta.getStep( i );
      StepMetaInterface sii = stepMeta.getStepMetaInterface();
      if ( sii != null ) {
        sii.searchInfoAndTargetSteps( transMeta.getSteps() );
      }
    }

    // Verify the transformation was configured properly
    assertEquals( "Transformation not initialized properly", 3, transMeta.nrSteps() );

    StepMeta meta = transMeta.getStep( 2 );
    assertTrue( "Transformation not initialized properly", meta.getStepMetaInterface() instanceof MappingMeta );

    MappingMeta loadedMappingMeta = (MappingMeta) meta.getStepMetaInterface();
    assertEquals( "Expected a two input mapping definition", 2, loadedMappingMeta.getInputMappings().size() );

    StepIOMetaInterface ioMeta = loadedMappingMeta.getStepIOMeta();
    assertEquals( "Expected a single Info Stream", 1, ioMeta.getInfoStreams().size() );
    assertEquals( "Expected a single Info Step", 1, loadedMappingMeta.getInfoSteps().length );
  }

  public void testMapping_WhenSharingPreviousStepWithAnother() throws Exception {
    KettleEnvironment.init();

    TransMeta transMeta = new TransMeta( "testfiles/org/pentaho/di/trans/steps/mapping/pdi-13435/PDI-13435-main.ktr" );
    transMeta.setTransformationType( TransMeta.TransformationType.Normal );

    Trans trans = new Trans( transMeta );
    trans.prepareExecution( null );
    trans.startThreads();
    trans.waitUntilFinished();

    assertEquals( 0, trans.getErrors() );
  }


  /**
   * This test case relates to PDI-13545. It executes a transformation with a Mapping step that is not configured
   * manually with an <tt>outputStepname</tt> property and, therefore, it is set to be a mapping output step from the
   * internal transformation.
   *
   * @throws Exception
   */
  public void testMapping_WhenNextStepHasTwoCopies_AndOutputIsNotDefinedExplicitly() throws Exception {
    runTransWhenMappingsIsFollowedByCopiedStep(
      "testfiles/org/pentaho/di/trans/steps/mapping/pdi-13545/pdi-13545-1.ktr" );
  }

  /**
   * This test case relates to PDI-13545. It executes a transformation with a Mapping step that is configured manually
   * with an <tt>outputStepname</tt> property.
   *
   * @throws Exception
   */
  public void testMapping_WhenNextStepHasTwoCopies_AndOutputIsDefinedExplicitly() throws Exception {
    runTransWhenMappingsIsFollowedByCopiedStep(
      "testfiles/org/pentaho/di/trans/steps/mapping/pdi-13545/pdi-13545-2.ktr" );
  }

  /**
   * This method runs transformations related to PDI-13545.<br/> The scenario is the following: there are two step
   * generating data, the latter of which is a Mapping step. They are followed with a Join Rows step, that has two
   * copies. The last in a row is a Dummy step, named "Last". Since both generating steps output 3 rows ([10, 20, 30]
   * and [1, 2, 3] respectively), the last step must obtain 3*3=9 rows.
   *
   * @param transPath a path to transformation file
   * @throws Exception
   */
  private void runTransWhenMappingsIsFollowedByCopiedStep( String transPath ) throws Exception {
    KettleEnvironment.init();

    TransMeta transMeta = new TransMeta( transPath );
    transMeta.setTransformationType( TransMeta.TransformationType.Normal );

    Trans trans = new Trans( transMeta );
    trans.prepareExecution( null );
    trans.startThreads();
    trans.waitUntilFinished();

    assertEquals( 0, trans.getErrors() );

    List<StepInterface> list = trans.findBaseSteps( "Last" );
    assertEquals( 1, list.size() );
    assertEquals( 9, list.get( 0 ).getLinesRead() );
  }
}
