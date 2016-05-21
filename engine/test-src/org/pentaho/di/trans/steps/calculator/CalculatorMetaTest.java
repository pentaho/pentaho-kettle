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
package org.pentaho.di.trans.steps.calculator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.loadsave.LoadSaveTester;
import org.pentaho.di.trans.steps.loadsave.initializer.InitializerInterface;
import org.pentaho.di.trans.steps.loadsave.validator.ArrayLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;

public class CalculatorMetaTest implements InitializerInterface<StepMetaInterface> {
  LoadSaveTester loadSaveTester;
  Class<CalculatorMeta> testMetaClass = CalculatorMeta.class;

  @Before
  public void setUpLoadSave() throws Exception {
    KettleEnvironment.init();
    PluginRegistry.init( true );
    List<String> attributes =
        Arrays.asList( "calculation" );

    Map<String, String> getterMap = new HashMap<String, String>() {
      {
        put( "calculation", "getCalculation" );
      }
    };
    Map<String, String> setterMap = new HashMap<String, String>() {
      {
        put( "calculation", "setCalculation" );
      }
    };
    FieldLoadSaveValidator<CalculatorMetaFunction[]> calculationMetaFunctionArrayLoadSaveValidator =
        new ArrayLoadSaveValidator<CalculatorMetaFunction>( new CalculatorMetaFunctionLoadSaveValidator(), 5 );


    Map<String, FieldLoadSaveValidator<?>> attrValidatorMap = new HashMap<String, FieldLoadSaveValidator<?>>();
    attrValidatorMap.put( "calculation", calculationMetaFunctionArrayLoadSaveValidator );

    Map<String, FieldLoadSaveValidator<?>> typeValidatorMap = new HashMap<String, FieldLoadSaveValidator<?>>();

    loadSaveTester =
        new LoadSaveTester( testMetaClass, attributes, new ArrayList<String>(), new ArrayList<String>(),
            getterMap, setterMap, attrValidatorMap, typeValidatorMap, this );
  }

  // Call the allocate method on the LoadSaveTester meta class
  @Override
  public void modify( StepMetaInterface someMeta ) {
    if ( someMeta instanceof CalculatorMeta ) {
      ( (CalculatorMeta) someMeta ).allocate( 5 );
    }
  }

  @Test
  public void testSerialization() throws KettleException {
    loadSaveTester.testSerialization();
  }

  public class CalculatorMetaFunctionLoadSaveValidator implements FieldLoadSaveValidator<CalculatorMetaFunction> {
    final Random rand = new Random();
    @Override
    public CalculatorMetaFunction getTestObject() {
      CalculatorMetaFunction rtn = new CalculatorMetaFunction();
      rtn.setCalcType( rand.nextInt( CalculatorMetaFunction.calc_desc.length ) );
      rtn.setConversionMask( UUID.randomUUID().toString() );
      rtn.setCurrencySymbol( UUID.randomUUID().toString() );
      rtn.setDecimalSymbol( UUID.randomUUID().toString() );
      rtn.setFieldA( UUID.randomUUID().toString() );
      rtn.setFieldB( UUID.randomUUID().toString() );
      rtn.setFieldC( UUID.randomUUID().toString() );
      rtn.setFieldName( UUID.randomUUID().toString() );
      rtn.setGroupingSymbol( UUID.randomUUID().toString() );
      rtn.setValueLength( rand.nextInt( 50 ) );
      rtn.setValuePrecision( rand.nextInt( 9 ) );
      rtn.setValueType( rand.nextInt( 7 ) + 1 );
      rtn.setRemovedFromResult( rand.nextBoolean() );
      return rtn;
    }

    @Override
    public boolean validateTestObject( CalculatorMetaFunction testObject, Object actual ) {
      if ( !( actual instanceof  CalculatorMetaFunction ) ) {
        return false;
      }
      CalculatorMetaFunction actualInput = (CalculatorMetaFunction) actual;
      return ( testObject.getXML().equals( actualInput.getXML() ) );
    }
  }


}
