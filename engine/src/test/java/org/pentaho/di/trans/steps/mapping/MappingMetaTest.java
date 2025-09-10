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

package org.pentaho.di.trans.steps.mapping;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.trans.step.StepHelperInterface;
import org.pentaho.di.trans.steps.loadsave.LoadSaveTester;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.ListLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.MappingIODefinitionLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.MappingParametersLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.ObjectIdLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.ObjectLocationSpecificationMethodLoadSaveValidator;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class MappingMetaTest {
  LoadSaveTester loadSaveTester;
  Class<MappingMeta> testMetaClass = MappingMeta.class;
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @Before
  public void setUpLoadSave() throws Exception {
    KettleEnvironment.init();
    PluginRegistry.init( false );
    List<String> attributes =
        Arrays.asList( "transName", "fileName", "directoryPath", "allowingMultipleInputs", "allowingMultipleOutputs",
            "specificationMethod", "transObjectId", "inputMappings", "outputMappings", "mappingParameters" );

    Map<String, FieldLoadSaveValidator<?>> attrValidatorMap = new HashMap<String, FieldLoadSaveValidator<?>>();
    attrValidatorMap.put( "specificationMethod", new ObjectLocationSpecificationMethodLoadSaveValidator() );
    attrValidatorMap.put( "transObjectId", new ObjectIdLoadSaveValidator() );
    attrValidatorMap.put( "inputMappings", new ListLoadSaveValidator<MappingIODefinition>( new MappingIODefinitionLoadSaveValidator(), 5 ) );
    attrValidatorMap.put( "outputMappings", new ListLoadSaveValidator<MappingIODefinition>( new MappingIODefinitionLoadSaveValidator(), 5 ) );
    attrValidatorMap.put( "mappingParameters", new MappingParametersLoadSaveValidator() );

    Map<String, FieldLoadSaveValidator<?>> typeValidatorMap = new HashMap<String, FieldLoadSaveValidator<?>>();

    loadSaveTester =
        new LoadSaveTester( testMetaClass, attributes, new HashMap<String, String>(), new HashMap<String, String>(),
            attrValidatorMap, typeValidatorMap );
  }

  @Test
  public void testSerialization() throws KettleException {
    loadSaveTester.testSerialization();
  }

  @Test
  public void testGetStepHelperInterface() {
    MappingMeta metaInjectMeta = new MappingMeta();
    StepHelperInterface stepHelperInterface = metaInjectMeta.getStepHelperInterface();

    assertNotNull( "StepHelperInterface should not be null", stepHelperInterface );
    assertTrue( "StepHelperInterface should be an instance of MappingHelper",
        stepHelperInterface instanceof MappingHelper );
  }
}
