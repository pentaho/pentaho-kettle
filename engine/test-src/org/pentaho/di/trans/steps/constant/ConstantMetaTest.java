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
package org.pentaho.di.trans.steps.constant;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.loadsave.LoadSaveTester;
import org.pentaho.di.trans.steps.loadsave.initializer.InitializerInterface;
import org.pentaho.di.trans.steps.loadsave.validator.ArrayLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.BooleanLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.IntLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.PrimitiveBooleanArrayLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.PrimitiveIntArrayLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.StringLoadSaveValidator;

public class ConstantMetaTest implements InitializerInterface<StepMetaInterface> {
  LoadSaveTester loadSaveTester;
  Class<ConstantMeta> testMetaClass = ConstantMeta.class;

  @Before
  public void setUpLoadSave() throws Exception {
    KettleEnvironment.init();
    PluginRegistry.init( true );
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
        new LoadSaveTester( testMetaClass, attributes, new ArrayList<String>(), new ArrayList<String>(),
            getterMap, setterMap, attrValidatorMap, typeValidatorMap, this );
  }

  // Call the allocate method on the LoadSaveTester meta class
  @Override
  public void modify( StepMetaInterface someMeta ) {
    if ( someMeta instanceof ConstantMeta ) {
      ( (ConstantMeta) someMeta ).allocate( 5 );
    }
  }

  @Test
  public void testSerialization() throws KettleException {
    loadSaveTester.testSerialization();
  }


  @Test
  public void cloneTest() throws Exception {
    ConstantMeta meta = new ConstantMeta();
    meta.allocate( 2 );
    meta.setFieldName( new String[] { "fieldname1", "fieldname2" } );
    meta.setFieldType( new String[] { "fieldtype1", "fieldtype2" } );
    meta.setFieldFormat( new String[] { "fieldformat1", "fieldformat2" } );
    meta.setFieldLength( new int[] { 10, 20 } );
    meta.setFieldPrecision( new int[] { 3, 5 } );
    meta.setCurrency( new String[] { "currency1", "currency2" } );
    meta.setDecimal( new String[] { "decimal1", "decimal2" } );
    meta.setGroup( new String[] { "group1", "group2" } );
    meta.setValue( new String[] { "value1", "value2" } );
    meta.setEmptyString( new boolean[] { false, true } );
    ConstantMeta aClone = (ConstantMeta) meta.clone();
    assertFalse( aClone == meta );
    assertTrue( Arrays.equals( meta.getFieldName(), aClone.getFieldName() ) );
    assertTrue( Arrays.equals( meta.getFieldType(), aClone.getFieldType() ) );
    assertTrue( Arrays.equals( meta.getFieldFormat(), aClone.getFieldFormat() ) );
    assertTrue( Arrays.equals( meta.getFieldLength(), aClone.getFieldLength() ) );
    assertTrue( Arrays.equals( meta.getFieldPrecision(), aClone.getFieldPrecision() ) );
    assertTrue( Arrays.equals( meta.getCurrency(), aClone.getCurrency() ) );
    assertTrue( Arrays.equals( meta.getDecimal(), aClone.getDecimal() ) );
    assertTrue( Arrays.equals( meta.getGroup(), aClone.getGroup() ) );
    assertTrue( Arrays.equals( meta.getValue(), aClone.getValue() ) );
    assertTrue( Arrays.equals( meta.isSetEmptyString(), aClone.isSetEmptyString() ) );
    assertEquals( meta.getXML(), aClone.getXML() );
  }
}
