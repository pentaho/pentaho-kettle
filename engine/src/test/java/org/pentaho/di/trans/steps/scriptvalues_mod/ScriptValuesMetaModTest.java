/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2024 by Hitachi Vantara : http://www.pentaho.com
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
package org.pentaho.di.trans.steps.scriptvalues_mod;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
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

import static org.pentaho.test.util.InternalState.setInternalState;

public class ScriptValuesMetaModTest implements InitializerInterface<StepMetaInterface> {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  LoadSaveTester loadSaveTester;
  Class<ScriptValuesMetaMod> testMetaClass = ScriptValuesMetaMod.class;

  @Before
  public void setUpLoadSave() throws Exception {
    KettleEnvironment.init();
    PluginRegistry.init( false );
    List<String> attributes =
        Arrays.asList( "fieldname", "rename", "type", "length", "precision", "replace", "jsScripts", "compatible", "optimizationLevel" );

    Map<String, String> getterMap = new HashMap<>() {
      {
        put( "fieldname", "getFieldname" );
        put( "rename", "getRename" );
        put( "type", "getType" );
        put( "length", "getLength" );
        put( "precision", "getPrecision" );
        put( "replace", "getReplace" );
        put( "compatible", "isCompatible" );
        put( "optimizationLevel", "getOptimizationLevel" );
        put( "jsScripts", "getJSScripts" );
      }
    };
    Map<String, String> setterMap = new HashMap<>() {
      {
        put( "fieldname", "setFieldname" );
        put( "rename", "setRename" );
        put( "type", "setType" );
        put( "length", "setLength" );
        put( "precision", "setPrecision" );
        put( "replace", "setReplace" );
        put( "compatible", "setCompatible" );
        put( "optimizationLevel", "setOptimizationLevel" );
        put( "jsScripts", "setJSScripts" );
      }
    };
    FieldLoadSaveValidator<String[]> stringArrayLoadSaveValidator =
      new ArrayLoadSaveValidator<>( new StringLoadSaveValidator(), 5 );

    FieldLoadSaveValidator<ScriptValuesScript[]> svsArrayLoadSaveValidator =
      new ArrayLoadSaveValidator<>( new ScriptValuesScriptLoadSaveValidator(), 5 );

    Map<String, FieldLoadSaveValidator<?>> attrValidatorMap = new HashMap<>();
    attrValidatorMap.put( "fieldname", stringArrayLoadSaveValidator );
    attrValidatorMap.put( "rename", stringArrayLoadSaveValidator );
    attrValidatorMap.put( "type", new PrimitiveIntArrayLoadSaveValidator( new IntLoadSaveValidator( 9 ), 5 ) );
    attrValidatorMap.put( "length", new PrimitiveIntArrayLoadSaveValidator( new IntLoadSaveValidator( 100 ), 5 ) );
    attrValidatorMap.put( "precision", new PrimitiveIntArrayLoadSaveValidator( new IntLoadSaveValidator( 6 ), 5 ) );
    attrValidatorMap.put( "replace",
        new PrimitiveBooleanArrayLoadSaveValidator( new BooleanLoadSaveValidator(), 5 ) );
    attrValidatorMap.put( "jsScripts", svsArrayLoadSaveValidator );

    Map<String, FieldLoadSaveValidator<?>> typeValidatorMap = new HashMap<>();

    loadSaveTester =
        new LoadSaveTester( testMetaClass, attributes, new ArrayList<String>(), new ArrayList<String>(),
            getterMap, setterMap, attrValidatorMap, typeValidatorMap, this );
  }

  // Call the allocate method on the LoadSaveTester meta class
  public void modify( StepMetaInterface someMeta ) {
    if ( someMeta instanceof ScriptValuesMetaMod ) {
      ( (ScriptValuesMetaMod) someMeta ).allocate( 5 );
    }
  }

  @Test
  public void testSerialization() throws KettleException {
    loadSaveTester.testSerialization();
  }

  public static class ScriptValuesScriptLoadSaveValidator implements FieldLoadSaveValidator<ScriptValuesScript> {
    final Random rand = new Random();
    @Override
    public ScriptValuesScript getTestObject() {
      int scriptType = rand.nextInt( 4 );
      if ( scriptType == 3 ) {
        scriptType = -1;
      }
      return new ScriptValuesScript( scriptType, UUID.randomUUID().toString(), UUID.randomUUID().toString() );
    }

    @Override
    public boolean validateTestObject( ScriptValuesScript testObject, Object actual ) {
      if ( !( actual instanceof ScriptValuesScript ) ) {
        return false;
      }
      return ( actual.toString().equals( testObject.toString() ) );
    }
  }

  @Test
  public void testExtend() {
    ScriptValuesMetaMod meta = new ScriptValuesMetaMod();
    int size = 1;
    meta.extend( size );

    Assert.assertEquals( size, meta.getFieldname().length );
    Assert.assertNull( meta.getFieldname()[ 0 ] );
    Assert.assertEquals( size, meta.getRename().length );
    Assert.assertNull( meta.getRename()[ 0 ] );
    Assert.assertEquals( size, meta.getType().length );
    Assert.assertEquals( -1, meta.getType()[ 0 ] );
    Assert.assertEquals( size, meta.getLength().length );
    Assert.assertEquals( -1, meta.getLength()[ 0 ] );
    Assert.assertEquals( size, meta.getPrecision().length );
    Assert.assertEquals( -1, meta.getPrecision()[ 0 ] );
    Assert.assertEquals( size, meta.getReplace().length );
    Assert.assertFalse( meta.getReplace()[ 0 ] );

    meta = new ScriptValuesMetaMod();
    // set some values, uneven lengths
    setInternalState( meta, "fieldname", new String[] { "Field 1", "Field 2", "Field 3" } );
    setInternalState( meta, "rename", new String[] { "Field 1 - new" } );
    setInternalState( meta, "type", new int[] { ValueMetaInterface.TYPE_STRING, ValueMetaInterface
      .TYPE_INTEGER, ValueMetaInterface.TYPE_NUMBER } );

    meta.extend( 3 );
    validateExtended( meta );
  }

  private void validateExtended( final ScriptValuesMetaMod meta ) {

    Assert.assertEquals( 3, meta.getFieldname().length );
    Assert.assertEquals( "Field 1", meta.getFieldname()[ 0 ] );
    Assert.assertEquals( "Field 2", meta.getFieldname()[ 1 ] );
    Assert.assertEquals( "Field 3", meta.getFieldname()[ 2 ] );
    Assert.assertEquals( 3, meta.getRename().length );
    Assert.assertEquals( "Field 1 - new", meta.getRename()[ 0 ] );
    Assert.assertNull( meta.getRename()[ 1 ] );
    Assert.assertNull( meta.getRename()[ 2 ] );
    Assert.assertEquals( 3, meta.getType().length );
    Assert.assertEquals( ValueMetaInterface.TYPE_STRING, meta.getType()[ 0 ] );
    Assert.assertEquals( ValueMetaInterface.TYPE_INTEGER, meta.getType()[ 1 ] );
    Assert.assertEquals( ValueMetaInterface.TYPE_NUMBER, meta.getType()[ 2 ] );
    Assert.assertEquals( 3, meta.getLength().length );
    Assert.assertEquals( -1, meta.getLength()[ 0 ] );
    Assert.assertEquals( -1, meta.getLength()[ 1 ] );
    Assert.assertEquals( -1, meta.getLength()[ 2 ] );
    Assert.assertEquals( 3, meta.getPrecision().length );
    Assert.assertEquals( -1, meta.getPrecision()[ 0 ] );
    Assert.assertEquals( -1, meta.getPrecision()[ 1 ] );
    Assert.assertEquals( -1, meta.getPrecision()[ 2 ] );
    Assert.assertEquals( 3, meta.getReplace().length );
    Assert.assertFalse( meta.getReplace()[ 0 ] );
    Assert.assertFalse( meta.getReplace()[ 1 ] );
    Assert.assertFalse( meta.getReplace()[ 2 ] );
  }
}
