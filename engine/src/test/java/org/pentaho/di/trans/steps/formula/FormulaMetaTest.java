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


package org.pentaho.di.trans.steps.formula;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.value.ValueMetaFactory;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.trans.steps.loadsave.LoadSaveTester;
import org.pentaho.di.trans.steps.loadsave.validator.ArrayLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;

public class FormulaMetaTest {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @BeforeClass
  public static void setUp() throws KettleException {
    KettleEnvironment.init();
  }

  public class FormulaMetaFunctionFieldLoadSaveValidator implements FieldLoadSaveValidator<FormulaMetaFunction> {
    @Override
    public FormulaMetaFunction getTestObject() {
      return new FormulaMetaFunction(
        UUID.randomUUID().toString(),
        UUID.randomUUID().toString(),
        new Random().nextInt( ValueMetaFactory.getAllValueMetaNames().length ),
        new Random().nextInt(),
        new Random().nextInt(),
        UUID.randomUUID().toString() );
    }

    @Override
    public boolean validateTestObject( FormulaMetaFunction testObject, Object actual ) {
      return testObject.equals( actual );
    }
  }

  @Test
  public void testStepMeta() throws KettleException {
    List<String> attributes = Arrays.asList( "formula" );

    Map<String, String> getterMap = new HashMap<String, String>();
    getterMap.put( "formula", "getFormula" );

    Map<String, String> setterMap = new HashMap<String, String>();
    setterMap.put( "formula", "setFormula" );

    Map<String, FieldLoadSaveValidator<?>> fieldLoadSaveValidatorAttributeMap =
      new HashMap<String, FieldLoadSaveValidator<?>>();

    FieldLoadSaveValidator<FormulaMetaFunction[]> formulaMetaFunctionArrayLoadSaveValidator =
      new ArrayLoadSaveValidator<FormulaMetaFunction>( new FormulaMetaFunctionFieldLoadSaveValidator(), 25 );

    fieldLoadSaveValidatorAttributeMap.put( "formula", formulaMetaFunctionArrayLoadSaveValidator );

    LoadSaveTester loadSaveTester = new LoadSaveTester( FormulaMeta.class, attributes, getterMap, setterMap,
      fieldLoadSaveValidatorAttributeMap, new HashMap<String, FieldLoadSaveValidator<?>>() );
    loadSaveTester.testSerialization();
  }
}
