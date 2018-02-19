/*! ******************************************************************************
*
* Pentaho Data Integration
*
* Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaTimestamp;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.trans.steps.getvariable.GetVariableMeta.FieldDefinition;
import org.pentaho.di.trans.steps.loadsave.LoadSaveTester;
import org.pentaho.di.trans.steps.loadsave.initializer.InitializerInterface;
import org.pentaho.di.trans.steps.loadsave.validator.ArrayLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;

public class GetVariableMetaTest implements InitializerInterface<GetVariableMeta> {
  LoadSaveTester<GetVariableMeta> loadSaveTester;
  Class<GetVariableMeta> testMetaClass = GetVariableMeta.class;
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @BeforeClass
  public static void setUpBeforeClass() throws KettleException {
    KettleEnvironment.init();
    PluginRegistry.init( false );
  }

  @Before
  public void setUpLoadSave() throws Exception {
    List<String> attributes = Arrays.asList( "fieldDefinitions" );

    Map<String, String> getterMap = new HashMap<String, String>();
    getterMap.put( "fieldDefinitions", "getFieldDefinitions" );

    Map<String, String> setterMap = new HashMap<String, String>();
    setterMap.put( "fieldDefinitions", "setFieldDefinitions" );

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
      new LoadSaveTester<>( testMetaClass, attributes, Collections.emptyList(), Collections.emptyList(), getterMap,
        setterMap, attrValidatorMap, typeValidatorMap, this );
  }

  // Call the allocate method on the LoadSaveTester meta class
  @Override
  public void modify( GetVariableMeta someMeta ) {
    someMeta.allocate( 5 );
  }

  @Test
  public void testSerialization() throws KettleException {
    loadSaveTester.testSerialization();
  }

  @Test
  public void testGetValueMetaPlugin() throws KettleStepException {
    GetVariableMeta meta = new GetVariableMeta();
    meta.setDefault();

    FieldDefinition field = new FieldDefinition();
    field.setFieldName( "outputField" );
    field.setVariableString( String.valueOf( 2000000L ) );
    field.setFieldType( ValueMetaInterface.TYPE_TIMESTAMP );
    meta.setFieldDefinitions( new FieldDefinition[]{ field } );

    RowMetaInterface rowMeta = new RowMeta();
    meta.getFields( rowMeta, "stepName", null, null, new Variables(), null, null );

    assertNotNull( rowMeta );
    assertEquals( 1, rowMeta.size() );
    assertEquals( "outputField", rowMeta.getFieldNames()[0] );
    assertEquals( ValueMetaInterface.TYPE_TIMESTAMP, rowMeta.getValueMeta( 0 ).getType() );
    assertTrue( rowMeta.getValueMeta( 0 ) instanceof ValueMetaTimestamp );
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
