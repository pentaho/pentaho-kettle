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
package org.pentaho.di.trans.steps.getvariable;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.getvariable.GetVariableMeta.FieldDefinition;
import org.pentaho.di.trans.steps.loadsave.LoadSaveTester;
import org.pentaho.di.trans.steps.loadsave.initializer.InitializerInterface;
import org.pentaho.di.trans.steps.loadsave.validator.ArrayLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;

public class GetVariableMetaTest implements InitializerInterface<StepMetaInterface> {
  LoadSaveTester loadSaveTester;
  Class<GetVariableMeta> testMetaClass = GetVariableMeta.class;

  @Before
  public void setUpLoadSave() throws Exception {
    KettleEnvironment.init();
    PluginRegistry.init( true );
    List<String> attributes = Arrays.asList( "fieldDefinitions" );

    Map<String, String> getterMap = new HashMap<String, String>() {
      {
        put( "fieldDefinitions", "getFieldDefinitions" );
      }
    };
    Map<String, String> setterMap = new HashMap<String, String>() {
      {
        put( "fieldDefinitions", "setFieldDefinitions" );
      }
    };

    FieldDefinition fieldDefinition = new FieldDefinition();
    fieldDefinition.setFieldName( "fieldName" );
    fieldDefinition.setFieldLength( 4 );
    fieldDefinition.setCurrency( null );
    fieldDefinition.setFieldPrecision( 5 );
    fieldDefinition.setFieldType( ValueMetaInterface.TYPE_NUMBER );
    fieldDefinition.setGroup( "group" );
    fieldDefinition.setVariableString( "variableString" );

    FieldLoadSaveValidator<FieldDefinition[]> fieldDefinitionLoadSaveValidator =
        new ArrayLoadSaveValidator<FieldDefinition>( new FieldDefinitionLoadSaveValidator( fieldDefinition ), 5 );

    Map<String, FieldLoadSaveValidator<?>> attrValidatorMap = new HashMap<String, FieldLoadSaveValidator<?>>();
    attrValidatorMap.put( "fieldName", fieldDefinitionLoadSaveValidator );

    Map<String, FieldLoadSaveValidator<?>> typeValidatorMap = new HashMap<String, FieldLoadSaveValidator<?>>();
    typeValidatorMap.put( FieldDefinition[].class.getCanonicalName(), fieldDefinitionLoadSaveValidator );

    loadSaveTester =
        new LoadSaveTester( testMetaClass, attributes, Collections.emptyList(), Collections.emptyList(), getterMap,
            setterMap, attrValidatorMap, typeValidatorMap, this );
  }

  // Call the allocate method on the LoadSaveTester meta class
  public void modify( StepMetaInterface someMeta ) {
    if ( someMeta instanceof GetVariableMeta ) {
      ( (GetVariableMeta) someMeta ).allocate( 5 );
    }
  }

  @Test
  public void testSerialization() throws KettleException {
    loadSaveTester.testSerialization();
  }

  public static class FieldDefinitionLoadSaveValidator implements FieldLoadSaveValidator<FieldDefinition> {

    private final FieldDefinition defaultValue;

    public FieldDefinitionLoadSaveValidator( FieldDefinition defaultValue ) {
      this.defaultValue = defaultValue;
    }

    @Override
    public FieldDefinition getTestObject() {
      return defaultValue;
    }

    @Override
    public boolean validateTestObject( FieldDefinition testObject, Object actual ) {
      return EqualsBuilder.reflectionEquals( testObject, actual );
    }
  }
}
