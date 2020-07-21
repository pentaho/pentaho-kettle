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
package org.pentaho.di.trans.steps.pentahoreporting;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.trans.steps.loadsave.LoadSaveTester;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.MapLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.StringLoadSaveValidator;
import org.pentaho.di.trans.steps.pentahoreporting.PentahoReportingOutputMeta.ProcessorType;

public class PentahoReportingOutputMetaLoadSaveTest {
  LoadSaveTester loadSaveTester;
  Class<PentahoReportingOutputMeta> testMetaClass = PentahoReportingOutputMeta.class;
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @Before
  public void setUpLoadSave() throws Exception {
    KettleEnvironment.init();
    PluginRegistry.init( false );
    List<String> attributes = Arrays.asList( "inputFileField", "outputFileField", "inputFile", "outputFile",
      "parameterFieldMap", "outputProcessorType", "createParentfolder", "useValuesFromFields" );

    Map<String, String> getterMap = new HashMap<String, String>();
    Map<String, String> setterMap = new HashMap<String, String>();

    Map<String, FieldLoadSaveValidator<?>> attrValidatorMap = new HashMap<String, FieldLoadSaveValidator<?>>();
    attrValidatorMap.put( "parameterFieldMap",
        new MapLoadSaveValidator<String, String>( new StringLoadSaveValidator(), new StringLoadSaveValidator(), 5 ) );
    attrValidatorMap.put( "outputProcessorType", new ProcessorTypeLoadSaveValidator() );

    Map<String, FieldLoadSaveValidator<?>> typeValidatorMap = new HashMap<String, FieldLoadSaveValidator<?>>();

    loadSaveTester =
        new LoadSaveTester( testMetaClass, attributes, getterMap, setterMap, attrValidatorMap, typeValidatorMap );
  }

  @Test
  public void testSerialization() throws KettleException {
    loadSaveTester.testSerialization();
  }

  public class ProcessorTypeLoadSaveValidator implements FieldLoadSaveValidator<ProcessorType> {
    final Random rand = new Random();
    @Override
    public ProcessorType getTestObject() {
      ProcessorType[] vals = ProcessorType.values();
      return vals[ rand.nextInt( vals.length ) ];
    }

    @Override
    public boolean validateTestObject( ProcessorType testObject, Object actual ) {
      if ( !( actual instanceof ProcessorType ) ) {
        return false;
      }
      ProcessorType another = (ProcessorType) actual;
      return testObject.equals( another );
    }
  }

}
