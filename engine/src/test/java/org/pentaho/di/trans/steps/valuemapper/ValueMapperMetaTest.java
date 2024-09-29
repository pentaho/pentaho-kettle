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
package org.pentaho.di.trans.steps.valuemapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
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
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.StringLoadSaveValidator;

public class ValueMapperMetaTest implements InitializerInterface<StepMetaInterface> {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();
  LoadSaveTester loadSaveTester;
  Class<ValueMapperMeta> testMetaClass = ValueMapperMeta.class;

  @Before
  public void setUpLoadSave() throws Exception {
    FieldLoadSaveValidator<String[]> stringArrayLoadSaveValidator =
        new ArrayLoadSaveValidator<String>( new StringLoadSaveValidator(), 7 );

    init( stringArrayLoadSaveValidator, stringArrayLoadSaveValidator );
  }

  private void init( FieldLoadSaveValidator<String[]> sourceStringArrayLoadSaveValidator,
    FieldLoadSaveValidator<String[]> targetStringArrayLoadSaveValidator ) throws KettleException {

    KettleEnvironment.init();
    PluginRegistry.init( false );
    List<String> attributes =
      Arrays.asList( "fieldToUse", "targetField", "nonMatchDefault", "sourceValue", "targetValue" );

    Map<String, String> getterMap = new HashMap<String, String>() {
      {
        put( "fieldToUse", "getFieldToUse" );
        put( "targetField", "getTargetField" );
        put( "nonMatchDefault", "getNonMatchDefault" );
        put( "sourceValue", "getSourceValue" );
        put( "targetValue", "getTargetValue" );
      }
    };
    Map<String, String> setterMap = new HashMap<String, String>() {
      {
        put( "fieldToUse", "setFieldToUse" );
        put( "targetField", "setTargetField" );
        put( "nonMatchDefault", "setNonMatchDefault" );
        put( "sourceValue", "setSourceValue" );
        put( "targetValue", "setTargetValue" );
      }
    };

    Map<String, FieldLoadSaveValidator<?>> attrValidatorMap = new HashMap<String, FieldLoadSaveValidator<?>>();
    attrValidatorMap.put( "sourceValue", sourceStringArrayLoadSaveValidator );
    attrValidatorMap.put( "targetValue", targetStringArrayLoadSaveValidator );

    Map<String, FieldLoadSaveValidator<?>> typeValidatorMap = new HashMap<String, FieldLoadSaveValidator<?>>();

    loadSaveTester =
      new LoadSaveTester( testMetaClass, attributes, new ArrayList<String>(), new ArrayList<String>(),
        getterMap, setterMap, attrValidatorMap, typeValidatorMap, this );
  }

  // Call the allocate method on the LoadSaveTester meta class
  @Override
  public void modify( StepMetaInterface someMeta ) {
    if ( someMeta instanceof ValueMapperMeta ) {
      ( (ValueMapperMeta) someMeta ).allocate( 7 );
    }
  }

  @Test
  public void testSerialization() throws KettleException {
    loadSaveTester.testSerialization();
  }

  @Test
  public void testSerializationWithNullAttr() throws KettleException {
    String abc = "abc";
    String stringNull = "null";
    String[] sourceAttrs = { abc, null, abc, null, stringNull, null, stringNull };
    String[] targetAttrs = { abc, null, null, abc, null, stringNull, stringNull };

    FieldLoadSaveValidator<String[]> sourceValidator =
      new ArrayLoadSaveValidator<String>( new CustomStringLoadSaveValidator( sourceAttrs ), sourceAttrs.length );
    FieldLoadSaveValidator<String[]> targetValidator =
      new ArrayLoadSaveValidator<String>( new CustomStringLoadSaveValidator( targetAttrs ), targetAttrs.length );

    init( sourceValidator, targetValidator );

    loadSaveTester.testSerialization();
  }

  private static class CustomStringLoadSaveValidator extends StringLoadSaveValidator {

    private String[] values;
    private int index = 0;

    public CustomStringLoadSaveValidator( String... values ) {
      this.values = values;
    }

    @Override
    public String getTestObject() {
      int i = index;
      index = ++index % values.length;
      return values[i];
    }

    @Override
    public boolean validateTestObject( String test, Object actual ) {
      return test == null ? nullOrEmpty( actual ) : test.equals( actual );
    }

    private boolean nullOrEmpty( Object o ) {
      return o == null || StringUtils.isEmpty( o.toString() );
    }
  }

  @Test
  public void testPDI16559() throws Exception {
    ValueMapperMeta valueMapper = new ValueMapperMeta();
    valueMapper.setSourceValue( new String[] { "value1", "value2", "value3", "value4" } );
    valueMapper.setTargetValue( new String[] { "targ1", "targ2" } );

    try {
      String badXml = valueMapper.getXML();
      Assert.fail( "Before calling afterInjectionSynchronization, should have thrown an ArrayIndexOOB" );
    } catch ( Exception expected ) {
      // Do Nothing
    }
    valueMapper.afterInjectionSynchronization();
    //run without a exception
    String ktrXml = valueMapper.getXML();

    Assert.assertEquals( valueMapper.getSourceValue().length, valueMapper.getTargetValue().length );

  }
}
