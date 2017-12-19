/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.formula;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.value.ValueMetaFactory;
import org.pentaho.di.trans.steps.loadsave.LoadSaveTester;
import org.pentaho.di.trans.steps.loadsave.validator.ArrayLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;

public class FormulaMetaTest {

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
