/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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
package org.pentaho.di.trans.steps.stepsmetrics;

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
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.loadsave.LoadSaveTester;
import org.pentaho.di.trans.steps.loadsave.initializer.InitializerInterface;
import org.pentaho.di.trans.steps.loadsave.validator.ArrayLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.StringLoadSaveValidator;

public class StepsMetricsMetaTest implements InitializerInterface<StepMetaInterface> {
  LoadSaveTester loadSaveTester;
  Class<StepsMetricsMeta> testMetaClass = StepsMetricsMeta.class;
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @Before
  public void setUpLoadSave() throws Exception {
    KettleEnvironment.init();
    PluginRegistry.init( false );
    List<String> attributes =
        Arrays.asList( "stepName", "stepCopyNr", "stepRequired", "stepnamefield", "stepidfield", "steplinesinputfield",
            "steplinesoutputfield", "steplinesreadfield", "steplinesupdatedfield", "steplineswrittentfield",
            "steplineserrorsfield", "stepsecondsfield" );

    Map<String, String> getterMap = new HashMap<String, String>() {
      {
        put( "stepName", "getStepName" );
        put( "stepCopyNr", "getStepCopyNr" );
        put( "stepRequired", "getStepRequired" );
        put( "stepnamefield", "getStepNameFieldName" );
        put( "stepidfield", "getStepIdFieldName" );
        put( "steplinesinputfield", "getStepLinesInputFieldName" );
        put( "steplinesoutputfield", "getStepLinesOutputFieldName" );
        put( "steplinesreadfield", "getStepLinesReadFieldName" );
        put( "steplinesupdatedfield", "getStepLinesUpdatedFieldName" );
        put( "steplineswrittentfield", "getStepLinesWrittenFieldName" );
        put( "steplineserrorsfield", "getStepLinesErrorsFieldName" );
        put( "stepsecondsfield", "getStepSecondsFieldName" );
      }
    };
    Map<String, String> setterMap = new HashMap<String, String>() {
      {
        put( "stepName", "setStepName" );
        put( "stepCopyNr", "setStepCopyNr" );
        put( "stepRequired", "setStepRequired" );
        put( "stepnamefield", "setStepNameFieldName" );
        put( "stepidfield", "setStepIdFieldName" );
        put( "steplinesinputfield", "setStepLinesInputFieldName" );
        put( "steplinesoutputfield", "setStepLinesOutputFieldName" );
        put( "steplinesreadfield", "setStepLinesReadFieldName" );
        put( "steplinesupdatedfield", "setStepLinesUpdatedFieldName" );
        put( "steplineswrittentfield", "setStepLinesWrittenFieldName" );
        put( "steplineserrorsfield", "setStepLinesErrorsFieldName" );
        put( "stepsecondsfield", "setStepSecondsFieldName" );
      }
    };
    FieldLoadSaveValidator<String[]> stringArrayLoadSaveValidator =
        new ArrayLoadSaveValidator<String>( new StringLoadSaveValidator(), 5 );


    Map<String, FieldLoadSaveValidator<?>> attrValidatorMap = new HashMap<String, FieldLoadSaveValidator<?>>();
    attrValidatorMap.put( "stepName", stringArrayLoadSaveValidator );
    attrValidatorMap.put( "stepCopyNr", stringArrayLoadSaveValidator );
    attrValidatorMap.put( "stepRequired", stringArrayLoadSaveValidator );
    // attrValidatorMap.put( "setEmptyString",
    //     new PrimitiveBooleanArrayLoadSaveValidator( new BooleanLoadSaveValidator(), 5 ) );


    Map<String, FieldLoadSaveValidator<?>> typeValidatorMap = new HashMap<String, FieldLoadSaveValidator<?>>();

    loadSaveTester =
        new LoadSaveTester( testMetaClass, attributes, new ArrayList<String>(), new ArrayList<String>(),
            getterMap, setterMap, attrValidatorMap, typeValidatorMap, this );
  }

  // Call the allocate method on the LoadSaveTester meta class
  public void modify( StepMetaInterface someMeta ) {
    if ( someMeta instanceof StepsMetricsMeta ) {
      ( (StepsMetricsMeta) someMeta ).allocate( 5 );
    }
  }

  @Test
  public void testSerialization() throws KettleException {
    loadSaveTester.testSerialization();
  }
}
