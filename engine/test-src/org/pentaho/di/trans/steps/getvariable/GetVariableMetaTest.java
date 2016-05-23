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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.row.value.ValueMetaBase;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.loadsave.LoadSaveTester;
import org.pentaho.di.trans.steps.loadsave.initializer.InitializerInterface;
import org.pentaho.di.trans.steps.loadsave.validator.ArrayLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.IntLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.NonZeroIntLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.PrimitiveIntArrayLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.StringLoadSaveValidator;

public class GetVariableMetaTest implements InitializerInterface<StepMetaInterface> {
  LoadSaveTester loadSaveTester;
  Class<GetVariableMeta> testMetaClass = GetVariableMeta.class;

  @Before
  public void setUpLoadSave() throws Exception {
    KettleEnvironment.init();
    PluginRegistry.init( true );
    List<String> attributes =
        Arrays.asList( "fieldName", "variableString", "fieldFormat", "fieldType", "fieldLength", "fieldPrecision", "currency", "decimal", "group", "trimType" );

    Map<String, String> getterMap = new HashMap<String, String>() {
      {
        put( "fieldName", "getFieldName" );
        put( "variableString", "getVariableString" );
        put( "fieldFormat", "getFieldFormat" );
        put( "fieldType", "getFieldType" );
        put( "fieldLength", "getFieldLength" );
        put( "fieldPrecision", "getFieldPrecision" );
        put( "currency", "getCurrency" );
        put( "decimal", "getDecimal" );
        put( "group", "getGroup" );
        put( "trimType", "getTrimType" );
      }
    };
    Map<String, String> setterMap = new HashMap<String, String>() {
      {
        put( "fieldName", "setFieldName" );
        put( "variableString", "setVariableString" );
        put( "fieldFormat", "setFieldFormat" );
        put( "fieldType", "setFieldType" );
        put( "fieldLength", "setFieldLength" );
        put( "fieldPrecision", "setFieldPrecision" );
        put( "currency", "setCurrency" );
        put( "decimal", "setDecimal" );
        put( "group", "setGroup" );
        put( "trimType", "setTrimType" );
      }
    };
    FieldLoadSaveValidator<String[]> stringArrayLoadSaveValidator =
        new ArrayLoadSaveValidator<String>( new StringLoadSaveValidator(), 5 );

    Map<String, FieldLoadSaveValidator<?>> attrValidatorMap = new HashMap<String, FieldLoadSaveValidator<?>>();
    attrValidatorMap.put( "fieldName", stringArrayLoadSaveValidator );
    attrValidatorMap.put( "variableString", stringArrayLoadSaveValidator );
    attrValidatorMap.put( "fieldFormat", stringArrayLoadSaveValidator );
    attrValidatorMap.put( "currency", stringArrayLoadSaveValidator );
    attrValidatorMap.put( "decimal", stringArrayLoadSaveValidator );
    attrValidatorMap.put( "group", stringArrayLoadSaveValidator );
    attrValidatorMap.put( "trimType", new PrimitiveIntArrayLoadSaveValidator( new IntLoadSaveValidator( ValueMetaBase.getTrimTypeCodes().length ), 5 ) );
    attrValidatorMap.put( "fieldLength", new PrimitiveIntArrayLoadSaveValidator( new IntLoadSaveValidator( 100 ), 5 ) );
    attrValidatorMap.put( "fieldPrecision", new PrimitiveIntArrayLoadSaveValidator( new IntLoadSaveValidator( 9 ), 5 ) );
    attrValidatorMap.put( "fieldType", new PrimitiveIntArrayLoadSaveValidator( new NonZeroIntLoadSaveValidator( 7 ), 5 ) );

    Map<String, FieldLoadSaveValidator<?>> typeValidatorMap = new HashMap<String, FieldLoadSaveValidator<?>>();
    loadSaveTester =
        new LoadSaveTester( testMetaClass, attributes, new ArrayList<String>(), new ArrayList<String>(),
            getterMap, setterMap, attrValidatorMap, typeValidatorMap, this );
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
}
