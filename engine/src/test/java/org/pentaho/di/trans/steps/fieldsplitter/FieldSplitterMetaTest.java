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

package org.pentaho.di.trans.steps.fieldsplitter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.value.ValueMetaBase;
import org.pentaho.di.core.row.value.ValueMetaFactory;
import org.pentaho.di.trans.steps.loadsave.LoadSaveTester;
import org.pentaho.di.trans.steps.loadsave.validator.ArrayLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.BooleanLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.IntLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.PrimitiveBooleanArrayLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.PrimitiveIntArrayLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.StringLoadSaveValidator;

public class FieldSplitterMetaTest {
  @BeforeClass
  public static void setUpBeforeClass() throws KettleException {
    KettleEnvironment.init();
  }

  @Test
  public void testRoundTrip() throws KettleException {
    List<String> attributes =
      Arrays.asList( "splitfield", "delimiter", "enclosure", "name", "id", "idrem", "type", "format",
        "group", "decimal", "currency", "length", "precision", "nullif", "ifnull", "trimtype" );

    Map<String, String> getterMap = new HashMap<String, String>();
    getterMap.put( "splitfield", "getSplitField" );
    getterMap.put( "delimiter", "getDelimiter" );
    getterMap.put( "enclosure", "getEnclosure" );
    getterMap.put( "name", "getFieldName" );
    getterMap.put( "id", "getFieldID" );
    getterMap.put( "idrem", "getFieldRemoveID" );
    getterMap.put( "type", "getFieldType" );
    getterMap.put( "format", "getFieldFormat" );
    getterMap.put( "group", "getFieldGroup" );
    getterMap.put( "decimal", "getFieldDecimal" );
    getterMap.put( "currency", "getFieldCurrency" );
    getterMap.put( "length", "getFieldLength" );
    getterMap.put( "precision", "getFieldPrecision" );
    getterMap.put( "nullif", "getFieldNullIf" );
    getterMap.put( "ifnull", "getFieldIfNull" );
    getterMap.put( "trimtype", "getFieldTrimType" );

    Map<String, String> setterMap = new HashMap<String, String>();
    setterMap.put( "splitfield", "setSplitField" );
    setterMap.put( "delimiter", "setDelimiter" );
    setterMap.put( "enclosure", "setEnclosure" );
    setterMap.put( "name", "setFieldName" );
    setterMap.put( "id", "setFieldID" );
    setterMap.put( "idrem", "setFieldRemoveID" );
    setterMap.put( "type", "setFieldType" );
    setterMap.put( "format", "setFieldFormat" );
    setterMap.put( "group", "setFieldGroup" );
    setterMap.put( "decimal", "setFieldDecimal" );
    setterMap.put( "currency", "setFieldCurrency" );
    setterMap.put( "length", "setFieldLength" );
    setterMap.put( "precision", "setFieldPrecision" );
    setterMap.put( "nullif", "setFieldNullIf" );
    setterMap.put( "ifnull", "setFieldIfNull" );
    setterMap.put( "trimtype", "setFieldTrimType" );

    Map<String, FieldLoadSaveValidator<?>> fieldLoadSaveValidatorAttributeMap =
      new HashMap<String, FieldLoadSaveValidator<?>>();

    fieldLoadSaveValidatorAttributeMap.put( "name",
      new ArrayLoadSaveValidator<String>( new StringLoadSaveValidator(), 5 ) );
    fieldLoadSaveValidatorAttributeMap.put( "id",
      new ArrayLoadSaveValidator<String>( new StringLoadSaveValidator(), 5 ) );
    fieldLoadSaveValidatorAttributeMap.put( "idrem",
      new PrimitiveBooleanArrayLoadSaveValidator( new BooleanLoadSaveValidator(), 5 ) );
    fieldLoadSaveValidatorAttributeMap.put( "type",
      new PrimitiveIntArrayLoadSaveValidator(
        new IntLoadSaveValidator( ValueMetaFactory.getAllValueMetaNames().length ), 5 ) );
    fieldLoadSaveValidatorAttributeMap.put( "format",
      new ArrayLoadSaveValidator<String>( new StringLoadSaveValidator(), 5 ) );
    fieldLoadSaveValidatorAttributeMap.put( "group",
      new ArrayLoadSaveValidator<String>( new StringLoadSaveValidator(), 5 ) );
    fieldLoadSaveValidatorAttributeMap.put( "decimal",
      new ArrayLoadSaveValidator<String>( new StringLoadSaveValidator(), 5 ) );
    fieldLoadSaveValidatorAttributeMap.put( "currency",
      new ArrayLoadSaveValidator<String>( new StringLoadSaveValidator(), 5 ) );
    fieldLoadSaveValidatorAttributeMap.put( "length",
      new PrimitiveIntArrayLoadSaveValidator( new IntLoadSaveValidator(), 5 ) );
    fieldLoadSaveValidatorAttributeMap.put( "precision",
      new PrimitiveIntArrayLoadSaveValidator( new IntLoadSaveValidator(), 5 ) );
    fieldLoadSaveValidatorAttributeMap.put( "nullif",
      new ArrayLoadSaveValidator<String>( new StringLoadSaveValidator(), 5 ) );
    fieldLoadSaveValidatorAttributeMap.put( "ifnull",
      new ArrayLoadSaveValidator<String>( new StringLoadSaveValidator(), 5 ) );
    fieldLoadSaveValidatorAttributeMap.put( "trimtype",
      new PrimitiveIntArrayLoadSaveValidator(
        new IntLoadSaveValidator( ValueMetaBase.getTrimTypeCodes().length ), 5 ) );

    LoadSaveTester loadSaveTester = new LoadSaveTester( FieldSplitterMeta.class, attributes, getterMap, setterMap,
      fieldLoadSaveValidatorAttributeMap, new HashMap<String, FieldLoadSaveValidator<?>>() );

    loadSaveTester.testSerialization();
  }

  @Test
  public void postAllocateChange() throws KettleException {
    FieldSplitterMeta meta = new FieldSplitterMeta();
    meta.allocate( 0 );

    meta.setDelimiter( ":" );
    meta.setEnclosure( "" );
    meta.setSplitField( "split" );

    // set properties after allocate (simulate metadata injection PDI-15679)
    meta.setFieldName( new String[] { "c1", "c2" } );

    meta.getXML();
  }

}
