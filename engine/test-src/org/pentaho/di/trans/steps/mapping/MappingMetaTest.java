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
package org.pentaho.di.trans.steps.mapping;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.trans.steps.loadsave.LoadSaveTester;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.ListLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.MappingIODefinitionLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.MappingParametersLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.ObjectIdLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.ObjectLocationSpecificationMethodLoadSaveValidator;

public class MappingMetaTest {
  LoadSaveTester loadSaveTester;
  Class<MappingMeta> testMetaClass = MappingMeta.class;

  @Before
  public void setUpLoadSave() throws Exception {
    KettleEnvironment.init();
    PluginRegistry.init( true );
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

}
