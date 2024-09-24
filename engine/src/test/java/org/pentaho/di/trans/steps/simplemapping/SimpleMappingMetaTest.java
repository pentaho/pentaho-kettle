/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/
package org.pentaho.di.trans.steps.simplemapping;

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
import org.pentaho.di.trans.steps.loadsave.LoadSaveTester;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.MappingIODefinitionLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.MappingParametersLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.ObjectIdLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.ObjectLocationSpecificationMethodLoadSaveValidator;

public class SimpleMappingMetaTest {
  LoadSaveTester loadSaveTester;
  Class<SimpleMappingMeta> testMetaClass = SimpleMappingMeta.class;
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @Before
  public void setUpLoadSave() throws Exception {
    KettleEnvironment.init();
    PluginRegistry.init( false );
    List<String> attributes =
        Arrays.asList( "transName", "fileName", "directoryPath", "transObjectId", "specificationMethod", "inputMapping", "outputMapping", "mappingParameters" );

    Map<String, String> getterMap = new HashMap<String, String>();
    Map<String, String> setterMap = new HashMap<String, String>();

    Map<String, FieldLoadSaveValidator<?>> attrValidatorMap = new HashMap<String, FieldLoadSaveValidator<?>>();
    attrValidatorMap.put( "transObjectId", new ObjectIdLoadSaveValidator() );
    attrValidatorMap.put( "specificationMethod", new ObjectLocationSpecificationMethodLoadSaveValidator() );
    attrValidatorMap.put( "inputMapping", new MappingIODefinitionLoadSaveValidator() );
    attrValidatorMap.put( "outputMapping", new MappingIODefinitionLoadSaveValidator() );
    attrValidatorMap.put( "mappingParameters", new MappingParametersLoadSaveValidator() );

    Map<String, FieldLoadSaveValidator<?>> typeValidatorMap = new HashMap<String, FieldLoadSaveValidator<?>>();

    loadSaveTester =
        new LoadSaveTester( testMetaClass, attributes, getterMap, setterMap, attrValidatorMap, typeValidatorMap );
  }

  @Test
  public void testSerialization() throws KettleException {
    loadSaveTester.testSerialization();
  }

}
