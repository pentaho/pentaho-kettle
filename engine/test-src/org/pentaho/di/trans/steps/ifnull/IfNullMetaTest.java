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
package org.pentaho.di.trans.steps.ifnull;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.trans.steps.loadsave.LoadSaveTester;
import org.pentaho.di.trans.steps.loadsave.validator.ArrayLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.BooleanLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.PrimitiveBooleanArrayLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.StringLoadSaveValidator;

public class IfNullMetaTest {

  LoadSaveTester loadSaveTester;

  @Before
  public void setUp() throws Exception {
    List<String> attributes =
        Arrays.asList( "fieldName", "replaceValue", "typeName", "typereplaceValue", "typereplaceMask", "replaceMask",
            "setTypeEmptyString", "setEmptyString", "selectFields", "selectValuesType",
            "replaceAllByValue", "replaceAllMask", "setEmptyStringAll" );

    Map<String, String> getterMap = new HashMap<String, String>() {
      {
        put( "fieldName", "getFieldName" );
        put( "replaceValue", "getReplaceValue" );
        put( "typeName", "getTypeName" );
        put( "typereplaceValue", "getTypeReplaceValue" );
        put( "typereplaceMask", "getTypeReplaceMask" );
        put( "replaceMask", "getReplaceMask" );
        put( "setTypeEmptyString", "isSetTypeEmptyString" );
        put( "setEmptyString", "isSetEmptyString" );
        put( "selectFields", "isSelectFields" );
        put( "selectValuesType", "isSelectValuesType" );
        put( "replaceAllByValue", "getReplaceAllByValue" );
        put( "replaceAllMask", "getReplaceAllMask" );
        put( "setEmptyStringAll", "isSetEmptyStringAll" );
      }
    };

    Map<String, String> setterMap = new HashMap<String, String>() {
      {
        put( "fieldName", "setFieldName" );
        put( "replaceValue", "setReplaceValue" );
        put( "typeName", "setTypeName" );
        put( "typereplaceValue", "setTypeReplaceValue" );
        put( "typereplaceMask", "setTypeReplaceMask" );
        put( "replaceMask", "setReplaceMask" );
        put( "setTypeEmptyString", "setTypeEmptyString" );
        put( "setEmptyString", "setEmptyString" );
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
    typeValidatorMap.put( boolean[].class.getCanonicalName(), new PrimitiveBooleanArrayLoadSaveValidator( new BooleanLoadSaveValidator(), 3 ) );

    loadSaveTester = new LoadSaveTester( IfNullMeta.class, attributes, getterMap, setterMap, attrValidatorMap, typeValidatorMap );
  }

  @Test
  public void testLoadSave() throws KettleException {
    loadSaveTester.testSerialization();
  }

  @Test
  public void testSetDefault() throws Exception {
    IfNullMeta inm = new IfNullMeta();
    inm.setDefault();
    assertTrue( ( inm.getTypeName() != null ) && ( inm.getTypeName().length == 0 ) );
    assertTrue( ( inm.getTypeReplaceValue() != null ) && ( inm.getTypeReplaceValue().length == 0 ) );
    assertTrue( ( inm.getTypeReplaceMask() != null ) && ( inm.getTypeReplaceMask().length == 0 ) );
    assertTrue( ( inm.isSetTypeEmptyString() != null ) && ( inm.isSetTypeEmptyString().length == 0 ) );
    assertTrue( ( inm.getFieldName() != null ) && ( inm.getFieldName().length == 0 ) );
    assertTrue( ( inm.getReplaceValue() != null ) && ( inm.getReplaceValue().length == 0 ) );
    assertTrue( ( inm.getReplaceMask() != null ) && ( inm.getReplaceMask().length == 0 ) );
    assertTrue( ( inm.isSetEmptyString() != null ) && ( inm.isSetEmptyString().length == 0 ) );
    assertFalse( inm.isSelectFields() );
    assertFalse( inm.isSelectValuesType() );
  }

}
