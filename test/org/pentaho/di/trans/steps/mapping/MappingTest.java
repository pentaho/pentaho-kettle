/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
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
import org.pentaho.di.trans.steps.rowgenerator.RowGenerator;
import org.pentaho.di.trans.steps.rowgenerator.RowGeneratorMeta;

public class MappingTest extends TestCase {

  /**
   * Builds a {@link RowGenerator} Step with a single String field.
   * 
   * @param registry Plugin Registry.
   * @param stepName Name to use for step
   * @return {@link StepMeta} for a Row Generator step. 
   */
  private StepMeta buildRowGeneratorStep(PluginRegistry registry, String stepName) {
    RowGeneratorMeta rm = new RowGeneratorMeta();

    // Set the information of the row generator.                
    String rowGeneratorPid = registry.getPluginId(StepPluginType.class, rm);
    StepMeta rowGeneratorStep = new StepMeta(rowGeneratorPid, stepName, rm);

    String fieldName[] = { "string" }; //$NON-NLS-1$
    String type[] = { "String" }; //$NON-NLS-1$
    String value[] = { "string_value" }; //$NON-NLS-1$
    String fieldFormat[] = { "" }; //$NON-NLS-1$
    String group[] = { "" }; //$NON-NLS-1$
    String decimal[] = { "" }; //$NON-NLS-1$
    int intDummies[] = { -1, -1, -1 };
    boolean    setEmptystring[]  = { false, false, false};

    rm.setDefault();
    rm.setFieldName(fieldName);
    rm.setFieldType(type);
    rm.setValue(value);
    rm.setFieldLength(intDummies);
    rm.setFieldPrecision(intDummies);
    rm.setRowLimit("1"); //$NON-NLS-1$
    rm.setFieldFormat(fieldFormat);
    rm.setGroup(group);
    rm.setDecimal(decimal);
    rm.setEmptyString(setEmptystring);

    return rowGeneratorStep;
  }

  private MappingIODefinition createMappingDef(String inputStepName, String mappingStepName, String sourceValueName,
      String targetValueName) {
    MappingIODefinition def = new MappingIODefinition();
    def.setInputStepname(inputStepName);
    def.setOutputStepname(mappingStepName);
    def.setValueRenames(Collections.singletonList(new MappingValueRename(sourceValueName, targetValueName)));
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
    transMeta.setName("Mapping Info Test"); //$NON-NLS-1$
    StepMeta rowGenerator = buildRowGeneratorStep(registry, "Generate Rows"); //$NON-NLS-1$
    transMeta.addStep(rowGenerator);

    String mappingName = "mapping"; //$NON-NLS-1$
    MappingMeta mappingMeta = new MappingMeta();
    mappingMeta.setSpecificationMethod(ObjectLocationSpecificationMethod.FILENAME);
    mappingMeta.setFileName("test/org/pentaho/di/trans/steps/mapping/subtrans.ktr"); //$NON-NLS-1$
    String mappingInputStepName = "input"; //$NON-NLS-1$
    mappingMeta
        .setInputMappings(Collections.singletonList(createMappingDef(rowGenerator.getName(), mappingInputStepName, "string", "a"))); //$NON-NLS-1$ //$NON-NLS-2$
    String mappingPid = registry.getPluginId(StepPluginType.class, mappingMeta);
    StepMeta mapping = new StepMeta(mappingPid, mappingName, mappingMeta);
    transMeta.addStep(mapping);

    TransHopMeta hopGeneratorToMapping = new TransHopMeta(rowGenerator, mapping);
    transMeta.addTransHop(hopGeneratorToMapping);

    Trans trans = new Trans(transMeta);
    trans.prepareExecution(null);

    // Mimic how a transformation is loaded and initialized from TransMeta.loadXML() or KettleDatabaseRepositoryTransDelegate.loadTransformation()
    // so the StepMeta references are wired up in the MappingMeta properly
    // (Copied from TransMeta.loadXML())
    for (int i = 0; i < transMeta.nrSteps(); i++) {
      StepMeta stepMeta = transMeta.getStep(i);
      StepMetaInterface sii = stepMeta.getStepMetaInterface();
      if (sii != null)
        sii.searchInfoAndTargetSteps(transMeta.getSteps());
    }

    // Verify the transformation was configured properly
    assertEquals("Transformation not initialized properly", 2, transMeta.nrSteps()); //$NON-NLS-1$

    StepMeta meta = transMeta.getStep(1);
    assertTrue("Transformation not initialized properly", meta.getStepMetaInterface() instanceof MappingMeta); //$NON-NLS-1$

    MappingMeta loadedMappingMeta = (MappingMeta) meta.getStepMetaInterface();
    assertEquals("Expected a single input mapping definition", 1, loadedMappingMeta.getInputMappings().size()); //$NON-NLS-1$

    StepIOMetaInterface ioMeta = loadedMappingMeta.getStepIOMeta();
    assertEquals("Expected a single Info Stream", 1, ioMeta.getInfoStreams().size()); //$NON-NLS-1$
    assertEquals("Expected a single Info Step", 1, loadedMappingMeta.getInfoSteps().length); //$NON-NLS-1$

    // Verify the transformation can be executed
    StepInterface si = trans.getStepInterface(mappingName, 0);
    RowStepCollector rc = new RowStepCollector();
    si.addRowListener(rc);

    trans.startThreads();
    trans.waitUntilFinished();

    assertEquals(1, rc.getRowsRead().size());
    assertEquals(1, rc.getRowsWritten().size());
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
    transMeta.setName("Mapping Info Test"); //$NON-NLS-1$

    StepMeta rowGenerator = buildRowGeneratorStep(registry, "Generate Rows"); //$NON-NLS-1$
    transMeta.addStep(rowGenerator);

    StepMeta rowGeneratorMain = buildRowGeneratorStep(registry, "Generate Rows Main"); //$NON-NLS-1$
    transMeta.addStep(rowGeneratorMain);

    String mappingName = "mapping"; //$NON-NLS-1$
    MappingMeta mappingMeta = new MappingMeta();
    mappingMeta.setSpecificationMethod(ObjectLocationSpecificationMethod.FILENAME);
    mappingMeta.setFileName("test/org/pentaho/di/trans/steps/mapping/subtrans.ktr"); //$NON-NLS-1$
    List<MappingIODefinition> inputMappings = new ArrayList<MappingIODefinition>();
    String mappingInputStepName = "input"; //$NON-NLS-1$
    inputMappings.add(createMappingDef(rowGenerator.getName(), mappingInputStepName, "string", "a")); //$NON-NLS-1$ //$NON-NLS-2$
    
    // Create the main data path mapping
    MappingIODefinition mainMappingDef = createMappingDef(rowGeneratorMain.getName(), mappingInputStepName, "string", "a"); //$NON-NLS-1$ //$NON-NLS-2$
    mainMappingDef.setMainDataPath(true);
    inputMappings.add(mainMappingDef);

    mappingMeta.setInputMappings(inputMappings);
    String mappingPid = registry.getPluginId(StepPluginType.class, mappingMeta);
    StepMeta mapping = new StepMeta(mappingPid, mappingName, mappingMeta);
    transMeta.addStep(mapping);

    TransHopMeta hopGeneratorToMapping = new TransHopMeta(rowGenerator, mapping);
    transMeta.addTransHop(hopGeneratorToMapping);
    hopGeneratorToMapping = new TransHopMeta(rowGeneratorMain, mapping);
    transMeta.addTransHop(hopGeneratorToMapping);
    
    Trans trans = new Trans(transMeta);
    trans.prepareExecution(null);

    // Mimic how a transformation is loaded and initialized from TransMeta.loadXML() or KettleDatabaseRepositoryTransDelegate.loadTransformation()
    // so the StepMeta references are wired up in the MappingMeta properly
    // (Copied from TransMeta.loadXML())
    for (int i = 0; i < transMeta.nrSteps(); i++) {
      StepMeta stepMeta = transMeta.getStep(i);
      StepMetaInterface sii = stepMeta.getStepMetaInterface();
      if (sii != null)
        sii.searchInfoAndTargetSteps(transMeta.getSteps());
    }

    // Verify the transformation was configured properly
    assertEquals("Transformation not initialized properly", 3, transMeta.nrSteps()); //$NON-NLS-1$

    StepMeta meta = transMeta.getStep(2);
    assertTrue("Transformation not initialized properly", meta.getStepMetaInterface() instanceof MappingMeta); //$NON-NLS-1$

    MappingMeta loadedMappingMeta = (MappingMeta) meta.getStepMetaInterface();
    assertEquals("Expected a two input mapping definition", 2, loadedMappingMeta.getInputMappings().size()); //$NON-NLS-1$

    StepIOMetaInterface ioMeta = loadedMappingMeta.getStepIOMeta();
    assertEquals("Expected a single Info Stream", 1, ioMeta.getInfoStreams().size()); //$NON-NLS-1$
    assertEquals("Expected a single Info Step", 1, loadedMappingMeta.getInfoSteps().length); //$NON-NLS-1$    
  }
}
