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

package org.pentaho.di.trans.steps.regexeval;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaBase;
import org.pentaho.di.core.row.value.ValueMetaPluginType;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.loadsave.LoadSaveTester;
import org.pentaho.di.trans.steps.loadsave.initializer.InitializerInterface;
import org.pentaho.di.trans.steps.loadsave.validator.ArrayLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.IntLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.PrimitiveIntArrayLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.StringLoadSaveValidator;

public class RegexEvalMetaTest implements InitializerInterface<StepMetaInterface> {
  RowMetaInterface mockInputRowMeta;
  VariableSpace mockVariableSpace;
  LoadSaveTester loadSaveTester;
  Class<RegexEvalMeta> testMetaClass = RegexEvalMeta.class;

  @BeforeClass
  public static void setupClass() throws KettleException {
    ValueMetaPluginType.getInstance().searchPlugins();
  }

  @Before
  public void setup() {
    mockInputRowMeta = mock( RowMetaInterface.class );
    mockVariableSpace = mock( VariableSpace.class );
  }

  @Test
  public void testGetFieldsReplacesResultFieldIfItExists() throws KettleStepException {
    RegexEvalMeta regexEvalMeta = new RegexEvalMeta();
    String name = "TEST_NAME";
    String resultField = "result";
    regexEvalMeta.setResultFieldName( resultField );
    when( mockInputRowMeta.indexOfValue( resultField ) ).thenReturn( 0 );
    ValueMetaInterface mockValueMeta = mock( ValueMetaInterface.class );
    String mockName = "MOCK_NAME";
    when( mockValueMeta.getName() ).thenReturn( mockName );
    when( mockInputRowMeta.getValueMeta( 0 ) ).thenReturn( mockValueMeta );
    regexEvalMeta.setReplacefields( true );
    regexEvalMeta.getFields( mockInputRowMeta, name, null, null, mockVariableSpace, null, null );
    ArgumentCaptor<ValueMetaInterface> captor = ArgumentCaptor.forClass( ValueMetaInterface.class );
    verify( mockInputRowMeta ).setValueMeta( eq( 0 ), captor.capture() );
    assertEquals( mockName, captor.getValue().getName() );
  }

  @Test
  public void testGetFieldsAddsResultFieldIfDoesntExist() throws KettleStepException {
    RegexEvalMeta regexEvalMeta = new RegexEvalMeta();
    String name = "TEST_NAME";
    String resultField = "result";
    regexEvalMeta.setResultFieldName( resultField );
    when( mockInputRowMeta.indexOfValue( resultField ) ).thenReturn( -1 );
    ValueMetaInterface mockValueMeta = mock( ValueMetaInterface.class );
    String mockName = "MOCK_NAME";
    when( mockVariableSpace.environmentSubstitute( resultField ) ).thenReturn( mockName );
    when( mockInputRowMeta.getValueMeta( 0 ) ).thenReturn( mockValueMeta );
    regexEvalMeta.setReplacefields( true );
    regexEvalMeta.getFields( mockInputRowMeta, name, null, null, mockVariableSpace, null, null );
    ArgumentCaptor<ValueMetaInterface> captor = ArgumentCaptor.forClass( ValueMetaInterface.class );
    verify( mockInputRowMeta ).addValueMeta( captor.capture() );
    assertEquals( mockName, captor.getValue().getName() );
  }

  @Test
  public void testGetFieldsReplacesFieldIfItExists() throws KettleStepException {
    RegexEvalMeta regexEvalMeta = new RegexEvalMeta();
    String name = "TEST_NAME";
    regexEvalMeta.allocate( 1 );
    String fieldName = "fieldname";
    //CHECKSTYLE:Indentation:OFF
    regexEvalMeta.getFieldName()[0] = fieldName;
    when( mockInputRowMeta.indexOfValue( fieldName ) ).thenReturn( 0 );
    ValueMetaInterface mockValueMeta = mock( ValueMetaInterface.class );
    String mockName = "MOCK_NAME";
    when( mockValueMeta.getName() ).thenReturn( mockName );
    when( mockInputRowMeta.getValueMeta( 0 ) ).thenReturn( mockValueMeta );
    regexEvalMeta.setReplacefields( true );
    regexEvalMeta.setAllowCaptureGroupsFlag( true );
    regexEvalMeta.getFields( mockInputRowMeta, name, null, null, mockVariableSpace, null, null );
    ArgumentCaptor<ValueMetaInterface> captor = ArgumentCaptor.forClass( ValueMetaInterface.class );
    verify( mockInputRowMeta ).setValueMeta( eq( 0 ), captor.capture() );
    assertEquals( mockName, captor.getValue().getName() );
  }

  @Test
  public void testGetFieldsAddsFieldIfDoesntExist() throws KettleStepException {
    RegexEvalMeta regexEvalMeta = new RegexEvalMeta();
    String name = "TEST_NAME";
    regexEvalMeta.allocate( 1 );
    String fieldName = "fieldname";
    regexEvalMeta.getFieldName()[0] = fieldName;
    when( mockInputRowMeta.indexOfValue( fieldName ) ).thenReturn( -1 );
    ValueMetaInterface mockValueMeta = mock( ValueMetaInterface.class );
    String mockName = "MOCK_NAME";
    when( mockVariableSpace.environmentSubstitute( fieldName ) ).thenReturn( mockName );
    when( mockInputRowMeta.getValueMeta( 0 ) ).thenReturn( mockValueMeta );
    regexEvalMeta.setReplacefields( true );
    regexEvalMeta.setAllowCaptureGroupsFlag( true );
    regexEvalMeta.getFields( mockInputRowMeta, name, null, null, mockVariableSpace, null, null );
    ArgumentCaptor<ValueMetaInterface> captor = ArgumentCaptor.forClass( ValueMetaInterface.class );
    verify( mockInputRowMeta ).addValueMeta( captor.capture() );
    assertEquals( fieldName, captor.getValue().getName() );
  }

  @Before
  public void setUpLoadSave() throws Exception {
    KettleEnvironment.init();
    PluginRegistry.init( true );
    List<String> attributes =
        Arrays.asList( "script", "matcher", "resultfieldname", "usevar", "allowcapturegroups", "replacefields", "canoneq",
            "caseinsensitive", "comment", "dotall", "multiline", "unicode", "unix", "fieldName", "fieldFormat", "fieldGroup",
            "fieldDecimal", "fieldCurrency", "fieldNullIf", "fieldIfNull", "fieldTrimType", "fieldLength", "fieldPrecision",
            "fieldType" );

    Map<String, String> getterMap = new HashMap<String, String>() {
      {
        put( "script", "getScript" );
        put( "matcher", "getMatcher" );
        put( "resultfieldname", "getResultFieldName" );
        put( "usevar", "isUseVariableInterpolationFlagSet" );
        put( "allowcapturegroups", "isAllowCaptureGroupsFlagSet" );
        put( "replacefields", "isReplacefields" );
        put( "canoneq", "isCanonicalEqualityFlagSet" );
        put( "caseinsensitive", "isCaseInsensitiveFlagSet" );
        put( "comment", "isCommentFlagSet" );
        put( "dotall", "isDotAllFlagSet" );
        put( "multiline", "isMultilineFlagSet" );
        put( "unicode", "isUnicodeFlagSet" );
        put( "unix", "isUnixLineEndingsFlagSet" );
        put( "fieldName", "getFieldName" );
        put( "fieldFormat", "getFieldFormat" );
        put( "fieldGroup", "getFieldGroup" );
        put( "fieldDecimal", "getFieldDecimal" );
        put( "fieldCurrency", "getFieldCurrency" );
        put( "fieldNullIf", "getFieldNullIf" );
        put( "fieldIfNull", "getFieldIfNull" );
        put( "fieldTrimType", "getFieldTrimType" );
        put( "fieldLength", "getFieldLength" );
        put( "fieldPrecision", "getFieldPrecision" );
        put( "fieldType", "getFieldType" );
      }
    };
    Map<String, String> setterMap = new HashMap<String, String>() {
      {
        put( "script", "setScript" );
        put( "matcher", "setMatcher" );
        put( "resultfieldname", "setResultFieldName" );
        put( "usevar", "setUseVariableInterpolationFlag" );
        put( "allowcapturegroups", "setAllowCaptureGroupsFlag" );
        put( "replacefields", "setReplacefields" );
        put( "canoneq", "setCanonicalEqualityFlag" );
        put( "caseinsensitive", "setCaseInsensitiveFlag" );
        put( "comment", "setCommentFlag" );
        put( "dotall", "setDotAllFlag" );
        put( "multiline", "setMultilineFlag" );
        put( "unicode", "setUnicodeFlag" );
        put( "unix", "setUnixLineEndingsFlag" );
        put( "fieldName", "setFieldName" );
        put( "fieldFormat", "setFieldFormat" );
        put( "fieldGroup", "setFieldGroup" );
        put( "fieldDecimal", "setFieldDecimal" );
        put( "fieldCurrency", "setFieldCurrency" );
        put( "fieldNullIf", "setFieldNullIf" );
        put( "fieldIfNull", "setFieldIfNull" );
        put( "fieldTrimType", "setFieldTrimType" );
        put( "fieldLength", "setFieldLength" );
        put( "fieldPrecision", "setFieldPrecision" );
        put( "fieldType", "setFieldType" );
      }
    };
    FieldLoadSaveValidator<String[]> stringArrayLoadSaveValidator =
        new ArrayLoadSaveValidator<String>( new StringLoadSaveValidator(), 5 );


    Map<String, FieldLoadSaveValidator<?>> attrValidatorMap = new HashMap<String, FieldLoadSaveValidator<?>>();
    attrValidatorMap.put( "fieldName", stringArrayLoadSaveValidator );
    attrValidatorMap.put( "fieldFormat", stringArrayLoadSaveValidator );
    attrValidatorMap.put( "fieldGroup", stringArrayLoadSaveValidator );
    attrValidatorMap.put( "fieldDecimal", stringArrayLoadSaveValidator );
    attrValidatorMap.put( "fieldCurrency", stringArrayLoadSaveValidator );
    attrValidatorMap.put( "fieldNullIf", stringArrayLoadSaveValidator );
    attrValidatorMap.put( "fieldIfNull", stringArrayLoadSaveValidator );
    attrValidatorMap.put( "fieldTrimType", new PrimitiveIntArrayLoadSaveValidator( new IntLoadSaveValidator( ValueMetaBase.getTrimTypeCodes().length ), 5 ) );
    attrValidatorMap.put( "fieldLength", new PrimitiveIntArrayLoadSaveValidator( new IntLoadSaveValidator( 100 ), 5 ) );
    attrValidatorMap.put( "fieldPrecision", new PrimitiveIntArrayLoadSaveValidator( new IntLoadSaveValidator( 9 ), 5 ) );
    attrValidatorMap.put( "fieldType", new PrimitiveIntArrayLoadSaveValidator( new IntLoadSaveValidator( 9 ), 5 ) );


    Map<String, FieldLoadSaveValidator<?>> typeValidatorMap = new HashMap<String, FieldLoadSaveValidator<?>>();

    loadSaveTester =
        new LoadSaveTester( testMetaClass, attributes, new ArrayList<String>(), new ArrayList<String>(),
            getterMap, setterMap, attrValidatorMap, typeValidatorMap, this );
  }

  // Call the allocate method on the LoadSaveTester meta class
  public void modify( StepMetaInterface someMeta ) {
    if ( someMeta instanceof RegexEvalMeta ) {
      ( (RegexEvalMeta) someMeta ).allocate( 5 );
    }
  }

  @Test
  public void testSerialization() throws KettleException {
    loadSaveTester.testSerialization();
  }


}
