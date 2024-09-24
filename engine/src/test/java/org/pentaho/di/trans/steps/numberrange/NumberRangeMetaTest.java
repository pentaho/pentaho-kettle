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

package org.pentaho.di.trans.steps.numberrange;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.trans.steps.loadsave.LoadSaveTester;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.ListLoadSaveValidator;

public class NumberRangeMetaTest {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @Test
  public void testStepMeta() throws KettleException {
    List<String> attributes = Arrays.asList( "inputField", "outputField", "fallBackValue", "rules" );

    Map<String, String> getterMap = new HashMap<String, String>();
    getterMap.put( "inputField", "getInputField" );
    getterMap.put( "outputField", "getOutputField" );
    getterMap.put( "fallBackValue", "getFallBackValue" );
    getterMap.put( "rules", "getRules" );

    Map<String, String> setterMap = new HashMap<String, String>();
    setterMap.put( "inputField", "setInputField" );
    setterMap.put( "outputField", "setOutputField" );
    setterMap.put( "fallBackValue", "setFallBackValue" );
    setterMap.put( "rules", "setRules" );

    Map<String, FieldLoadSaveValidator<?>> fieldLoadSaveValidatorAttributeMap =
      new HashMap<String, FieldLoadSaveValidator<?>>();
    fieldLoadSaveValidatorAttributeMap.put( "rules",
      new ListLoadSaveValidator<NumberRangeRule>( new NumberRangeRuleFieldLoadSaveValidator(), 25 ) );

    LoadSaveTester loadSaveTester = new LoadSaveTester(
      NumberRangeMeta.class, attributes, getterMap, setterMap,
      fieldLoadSaveValidatorAttributeMap, new HashMap<String, FieldLoadSaveValidator<?>>() );
    loadSaveTester.testSerialization();
  }

  public class NumberRangeRuleFieldLoadSaveValidator implements FieldLoadSaveValidator<NumberRangeRule> {
    @Override
    public NumberRangeRule getTestObject() {
      return new NumberRangeRule(
        new Random().nextDouble(),
        new Random().nextDouble(),
        UUID.randomUUID().toString() );
    }

    @Override
    public boolean validateTestObject( NumberRangeRule testObject, Object actual ) {
      return testObject.equals( actual );
    }
  }
}
