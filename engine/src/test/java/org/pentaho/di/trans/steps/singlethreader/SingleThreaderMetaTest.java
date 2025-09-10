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

package org.pentaho.di.trans.steps.singlethreader;

import java.util.ArrayList;
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
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.loadsave.LoadSaveTester;
import org.pentaho.di.trans.steps.loadsave.initializer.InitializerInterface;
import org.pentaho.di.trans.steps.loadsave.validator.ArrayLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.ObjectIdLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.ObjectLocationSpecificationMethodLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.StringLoadSaveValidator;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class SingleThreaderMetaTest implements InitializerInterface<StepMetaInterface> {
  LoadSaveTester loadSaveTester;
  Class<SingleThreaderMeta> testMetaClass = SingleThreaderMeta.class;
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @Before
  public void setUpLoadSave() throws Exception {
    KettleEnvironment.init();
    PluginRegistry.init( false );
    List<String> attributes =
        Arrays.asList( "transName", "fileName", "directoryPath", "batchSize", "batchTime", "injectStep", "retrieveStep",
            "passingAllParameters", "specificationMethod", "transObjectId", "parameters", "parameterValues" );

    Map<String, String> getterMap = new HashMap<String, String>();
    Map<String, String> setterMap = new HashMap<String, String>();

    FieldLoadSaveValidator<String[]> stringArrayLoadSaveValidator =
        new ArrayLoadSaveValidator<String>( new StringLoadSaveValidator(), 5 );


    Map<String, FieldLoadSaveValidator<?>> attrValidatorMap = new HashMap<String, FieldLoadSaveValidator<?>>();
    attrValidatorMap.put( "parameters", stringArrayLoadSaveValidator );
    attrValidatorMap.put( "parameterValues", stringArrayLoadSaveValidator );
    attrValidatorMap.put( "specificationMethod", new ObjectLocationSpecificationMethodLoadSaveValidator() );
    attrValidatorMap.put( "transObjectId", new ObjectIdLoadSaveValidator() );

    Map<String, FieldLoadSaveValidator<?>> typeValidatorMap = new HashMap<String, FieldLoadSaveValidator<?>>();

    loadSaveTester =
        new LoadSaveTester( testMetaClass, attributes, new ArrayList<String>(), new ArrayList<String>(),
            getterMap, setterMap, attrValidatorMap, typeValidatorMap, this );
  }

  // Call the allocate method on the LoadSaveTester meta class
  @Override
  public void modify( StepMetaInterface someMeta ) {
    if ( someMeta instanceof SingleThreaderMeta ) {
      ( (SingleThreaderMeta) someMeta ).allocate( 5 );
    }
  }

  @Test
  public void testSerialization() throws KettleException {
    loadSaveTester.testSerialization();
  }

  // Note - cloneTest() removed as the loadsave tester now performs clone testing as well.

  @Test
  public void testGetStepHelperInterface() {
    SingleThreaderMeta singleThreaderMeta = new SingleThreaderMeta();
    StepHelperInterface stepHelperInterface = singleThreaderMeta.getStepHelperInterface();

    assertNotNull( "StepHelperInterface should not be null", stepHelperInterface );
    assertTrue( "StepHelperInterface should be an instance of SingleThreaderHelper",
        stepHelperInterface instanceof SingleThreaderHelper );
  }
}

