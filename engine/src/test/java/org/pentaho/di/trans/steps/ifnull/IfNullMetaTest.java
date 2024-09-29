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
package org.pentaho.di.trans.steps.ifnull;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.trans.steps.ifnull.IfNullMeta.Fields;
import org.pentaho.di.trans.steps.ifnull.IfNullMeta.ValueTypes;
import org.pentaho.di.trans.steps.loadsave.LoadSaveTester;
import org.pentaho.di.trans.steps.loadsave.validator.ArrayLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.BooleanLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.PrimitiveBooleanArrayLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.StringLoadSaveValidator;

public class IfNullMetaTest {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  LoadSaveTester loadSaveTester;

  @Before
  public void setUp() throws Exception {
    List<String> attributes =
        Arrays.asList( "fields", "valueTypes", "selectFields", "selectValuesType", "replaceAllByValue",
            "replaceAllMask", "setEmptyStringAll" );

    Map<String, String> getterMap = new HashMap<String, String>() {
      {
        put( "fields", "getFields" );
        put( "valueTypes", "getValueTypes" );
        put( "selectFields", "isSelectFields" );
        put( "selectValuesType", "isSelectValuesType" );
        put( "replaceAllByValue", "getReplaceAllByValue" );
        put( "replaceAllMask", "getReplaceAllMask" );
        put( "setEmptyStringAll", "isSetEmptyStringAll" );
      }
    };

    Map<String, String> setterMap = new HashMap<String, String>() {
      {
        put( "fields", "setFields" );
        put( "valueTypes", "setValueTypes" );
        put( "selectFields", "setSelectFields" );
        put( "selectValuesType", "setSelectValuesType" );
        put( "replaceAllByValue", "setReplaceAllByValue" );
        put( "replaceAllMask", "setReplaceAllMask" );
        put( "setEmptyStringAll", "setEmptyStringAll" );
      }
    };
    FieldLoadSaveValidator<String[]> stringArrayLoadSaveValidator =
        new ArrayLoadSaveValidator<String>( new StringLoadSaveValidator(), 3 );
    Map<String, FieldLoadSaveValidator<?>> attrValidatorMap = new HashMap<String, FieldLoadSaveValidator<?>>();
    attrValidatorMap.put( "fieldName", stringArrayLoadSaveValidator );
    attrValidatorMap.put( "replaceValue", stringArrayLoadSaveValidator );
    attrValidatorMap.put( "typeName", stringArrayLoadSaveValidator );
    attrValidatorMap.put( "typereplaceValue", stringArrayLoadSaveValidator );
    attrValidatorMap.put( "typereplaceMask", stringArrayLoadSaveValidator );
    attrValidatorMap.put( "replaceMask", stringArrayLoadSaveValidator );

    Map<String, FieldLoadSaveValidator<?>> typeValidatorMap = new HashMap<String, FieldLoadSaveValidator<?>>();
    typeValidatorMap.put( boolean[].class.getCanonicalName(), new PrimitiveBooleanArrayLoadSaveValidator(
        new BooleanLoadSaveValidator(), 3 ) );

    Fields field = new Fields();
    field.setFieldName( "fieldName" );
    field.setReplaceValue( "replaceValue" );
    field.setReplaceMask( "replaceMask" );
    field.setEmptyString( true );
    typeValidatorMap.put( Fields[].class.getCanonicalName(), new ArrayLoadSaveValidator<Fields>(
        new FieldsLoadSaveValidator( field ), 3 ) );

    ValueTypes type = new ValueTypes();
    type.setTypeName( "typeName" );
    type.setTypereplaceValue( "typereplaceValue" );
    type.setTypereplaceMask( "typereplaceMask" );
    type.setTypeEmptyString( true );
    typeValidatorMap.put( ValueTypes[].class.getCanonicalName(), new ArrayLoadSaveValidator<ValueTypes>(
        new ValueTypesLoadSaveValidator( type ), 3 ) );

    loadSaveTester =
        new LoadSaveTester( IfNullMeta.class, attributes, getterMap, setterMap, attrValidatorMap, typeValidatorMap );
  }

  @Test
  public void testLoadSave() throws KettleException {
    loadSaveTester.testSerialization();
  }

  @Test
  public void testSetDefault() throws Exception {
    IfNullMeta inm = new IfNullMeta();
    inm.setDefault();
    assertTrue( ( inm.getValueTypes() != null ) && ( inm.getValueTypes().length == 0 ) );
    assertTrue( ( inm.getFields() != null ) && ( inm.getFields().length == 0 ) );
    assertFalse( inm.isSelectFields() );
    assertFalse( inm.isSelectValuesType() );
  }

  public static class FieldsLoadSaveValidator implements FieldLoadSaveValidator<Fields> {

    private final Fields defaultValue;

    public FieldsLoadSaveValidator( Fields defaultValue ) {
      this.defaultValue = defaultValue;
    }

    @Override
    public Fields getTestObject() {
      return defaultValue;
    }

    @Override
    public boolean validateTestObject( Fields testObject, Object actual ) {
      return EqualsBuilder.reflectionEquals( testObject, actual );
    }
  }

  public static class ValueTypesLoadSaveValidator implements FieldLoadSaveValidator<ValueTypes> {

    private final ValueTypes defaultValue;

    public ValueTypesLoadSaveValidator( ValueTypes defaultValue ) {
      this.defaultValue = defaultValue;
    }

    @Override
    public ValueTypes getTestObject() {
      return defaultValue;
    }

    @Override
    public boolean validateTestObject( ValueTypes testObject, Object actual ) {
      return EqualsBuilder.reflectionEquals( testObject, actual );
    }
  }
}
