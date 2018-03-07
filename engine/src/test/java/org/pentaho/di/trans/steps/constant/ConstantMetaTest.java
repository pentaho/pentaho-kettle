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

package org.pentaho.di.trans.steps.constant;

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
import org.pentaho.di.trans.steps.loadsave.LoadSaveTester;
import org.pentaho.di.trans.steps.loadsave.initializer.InitializerInterface;
import org.pentaho.di.trans.steps.loadsave.validator.ArrayLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.BooleanLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.IntLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.PrimitiveBooleanArrayLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.PrimitiveIntArrayLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.StringLoadSaveValidator;

public class ConstantMetaTest implements InitializerInterface<ConstantMeta> {
  LoadSaveTester<ConstantMeta> loadSaveTester;
  Class<ConstantMeta> testMetaClass = ConstantMeta.class;
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @Before
  public void setUpLoadSave() throws Exception {
    KettleEnvironment.init();
    PluginRegistry.init( false );
    List<String> attributes =
        Arrays.asList( "currency", "decimal", "group", "value", "fieldName", "fieldType", "fieldFormat", "fieldLength",
            "fieldPrecision", "setEmptyString" );

    Map<String, String> getterMap = new HashMap<String, String>() {
      {
        put( "setEmptyString", "isSetEmptyString" );
      }
    };
    Map<String, String> setterMap = new HashMap<String, String>() {
      {
        put( "setEmptyString", "setEmptyString" );
      }
    };
    FieldLoadSaveValidator<String[]> stringArrayLoadSaveValidator =
        new ArrayLoadSaveValidator<String>( new StringLoadSaveValidator(), 5 );

    PrimitiveIntArrayLoadSaveValidator intArrayLoadSaveValidator = new PrimitiveIntArrayLoadSaveValidator( new IntLoadSaveValidator(), 5 );
    PrimitiveBooleanArrayLoadSaveValidator booleanArrayLoadSaveValidator = new PrimitiveBooleanArrayLoadSaveValidator( new BooleanLoadSaveValidator(), 5 );

    Map<String, FieldLoadSaveValidator<?>> attrValidatorMap = new HashMap<String, FieldLoadSaveValidator<?>>();
    attrValidatorMap.put( "currency", stringArrayLoadSaveValidator );
    attrValidatorMap.put( "decimal", stringArrayLoadSaveValidator );
    attrValidatorMap.put( "group", stringArrayLoadSaveValidator );
    attrValidatorMap.put( "value", stringArrayLoadSaveValidator );
    attrValidatorMap.put( "fieldName", stringArrayLoadSaveValidator );
    attrValidatorMap.put( "fieldType", stringArrayLoadSaveValidator );
    attrValidatorMap.put( "fieldFormat", stringArrayLoadSaveValidator );

    attrValidatorMap.put( "fieldLength", intArrayLoadSaveValidator );
    attrValidatorMap.put( "fieldPrecision", intArrayLoadSaveValidator );
    attrValidatorMap.put( "setEmptyString", booleanArrayLoadSaveValidator );


    Map<String, FieldLoadSaveValidator<?>> typeValidatorMap = new HashMap<String, FieldLoadSaveValidator<?>>();

    loadSaveTester =
      new LoadSaveTester<ConstantMeta>( testMetaClass, attributes, new ArrayList<String>(), new ArrayList<String>(),
        getterMap, setterMap, attrValidatorMap, typeValidatorMap, this );
  }

  // Call the allocate method on the LoadSaveTester meta class
  @Override
  public void modify( ConstantMeta someMeta ) {
    if ( someMeta instanceof ConstantMeta ) {
      ( (ConstantMeta) someMeta ).allocate( 5 );
    }
  }

  @Test
  public void testSerialization() throws KettleException {
    loadSaveTester.testSerialization();
  }
}
