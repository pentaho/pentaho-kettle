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

package org.pentaho.di.trans.steps.script;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

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
import org.pentaho.di.trans.steps.loadsave.validator.BooleanLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.IntLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.PrimitiveBooleanArrayLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.PrimitiveIntArrayLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.StringLoadSaveValidator;

public class ScriptMetaTest implements InitializerInterface<StepMetaInterface> {
  LoadSaveTester loadSaveTester;
  Class<ScriptMeta> testMetaClass = ScriptMeta.class;
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @Before
  public void setUpLoadSave() throws Exception {
    KettleEnvironment.init();
    PluginRegistry.init( false );
    List<String> attributes =
        Arrays.asList( "fieldname", "rename", "type", "length", "precision", "replace", "jsScripts" );

    Map<String, String> getterMap = new HashMap<String, String>() {
      {
        put( "fieldname", "getFieldname" );
        put( "rename", "getRename" );
        put( "type", "getType" );
        put( "length", "getLength" );
        put( "precision", "getPrecision" );
        put( "replace", "getReplace" );
        put( "jsScripts", "getJSScripts" );
      }
    };
    Map<String, String> setterMap = new HashMap<String, String>() {
      {
        put( "fieldname", "setFieldname" );
        put( "rename", "setRename" );
        put( "type", "setType" );
        put( "length", "setLength" );
        put( "precision", "setPrecision" );
        put( "replace", "setReplace" );
        put( "jsScripts", "setJSScripts" );
      }
    };
    FieldLoadSaveValidator<String[]> stringArrayLoadSaveValidator =
        new ArrayLoadSaveValidator<String>( new StringLoadSaveValidator(), 5 );

    FieldLoadSaveValidator<ScriptValuesScript[]> svsArrayLoadSaveValidator =
        new ArrayLoadSaveValidator<ScriptValuesScript>( new ScriptValuesScriptLoadSaveValidator(), 5 );

    Map<String, FieldLoadSaveValidator<?>> attrValidatorMap = new HashMap<String, FieldLoadSaveValidator<?>>();
    attrValidatorMap.put( "fieldname", stringArrayLoadSaveValidator );
    attrValidatorMap.put( "rename", stringArrayLoadSaveValidator );
    attrValidatorMap.put( "type", new PrimitiveIntArrayLoadSaveValidator( new IntLoadSaveValidator( 9 ), 5 ) );
    attrValidatorMap.put( "length", new PrimitiveIntArrayLoadSaveValidator( new IntLoadSaveValidator( 100 ), 5 ) );
    attrValidatorMap.put( "precision", new PrimitiveIntArrayLoadSaveValidator( new IntLoadSaveValidator( 6 ), 5 ) );
    attrValidatorMap.put( "replace",
        new PrimitiveBooleanArrayLoadSaveValidator( new BooleanLoadSaveValidator(), 5 ) );
    attrValidatorMap.put( "jsScripts", svsArrayLoadSaveValidator );

    Map<String, FieldLoadSaveValidator<?>> typeValidatorMap = new HashMap<String, FieldLoadSaveValidator<?>>();

    loadSaveTester =
        new LoadSaveTester( testMetaClass, attributes, new ArrayList<String>(), new ArrayList<String>(),
            getterMap, setterMap, attrValidatorMap, typeValidatorMap, this );
  }

  // Call the allocate method on the LoadSaveTester meta class
  public void modify( StepMetaInterface someMeta ) {
    if ( someMeta instanceof ScriptMeta ) {
      ( (ScriptMeta) someMeta ).allocate( 5 );
    }
  }

  @Test
  public void testSerialization() throws KettleException {
    loadSaveTester.testSerialization();
  }

  public class ScriptValuesScriptLoadSaveValidator implements FieldLoadSaveValidator<ScriptValuesScript> {
    final Random rand = new Random();
    @Override
    public ScriptValuesScript getTestObject() {
      int scriptType = rand.nextInt( 4 );
      if ( scriptType == 3 ) {
        scriptType = -1;
      }
      ScriptValuesScript rtn = new ScriptValuesScript( scriptType, UUID.randomUUID().toString(), UUID.randomUUID().toString() );
      return rtn;
    }

    @Override
    public boolean validateTestObject( ScriptValuesScript testObject, Object actual ) {
      if ( !( actual instanceof ScriptValuesScript ) ) {
        return false;
      }
      return ( actual.toString().equals( testObject.toString() ) );
    }
  }

}
