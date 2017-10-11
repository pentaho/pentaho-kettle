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

package org.pentaho.di.trans.steps.fieldsplitter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.trans.steps.mock.StepMockHelper;
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

  @Test
  public void testPDI16559() throws Exception {
    StepMockHelper<FieldSplitterMeta, FieldSplitterData> mockHelper =
            new StepMockHelper<FieldSplitterMeta, FieldSplitterData>( "fieldSplitter", FieldSplitterMeta.class, FieldSplitterData.class );

    FieldSplitterMeta fieldSplitter = new FieldSplitterMeta();
    fieldSplitter.setFieldName( new String[] { "field1", "field2", "field3", "field4", "field5", "field6", "field7", "field8", "field9", "field10", "field11", "field12" } );
    fieldSplitter.setFieldID( new String[] { "fieldID1", "fieldID2", "fieldID3", "fieldID4", "fieldID5", "fieldID6", "fieldID7", "fieldID8", "fieldID9", "fieldID10", "fieldID11" } );
    fieldSplitter.setFieldRemoveID( new boolean[] { true, false, false, false, false, false, true, false, true } );
    fieldSplitter.setFieldFormat( new String[] { "asdf", "asdf", "qwer", "qwer", "QErasdf", "zxvv", "fasdf", "qwerqwr" } );
    fieldSplitter.setFieldGroup( new String[] { "groupa", "groupb", "groupa", "groupb", "groupa", "groupb", "groupa", "groupb" } );
    fieldSplitter.setFieldDecimal( new String[] { "asdf", "qwer", "zxcvb", "erty", "asfaf", "fhdfhg" } );
    fieldSplitter.setFieldCurrency( new String[] { "$", "$", "$", "$", "$", "$" } );
    fieldSplitter.setFieldLength( new int[] { 12, 6, 15, 14, 23, 177, 13, 21 } );
    fieldSplitter.setFieldPrecision( new int[] { 7, 7, 7, 7, 12, 16, 5, 5, 5} );
    fieldSplitter.setFieldNullIf( new String[] { "hdfgh", "sdfgsdg", "ZZfZDf", "dfhfh", "235gwst", "qreqwre" } );
    fieldSplitter.setFieldIfNull( new String[] { "asdfsaf", "qwreqwr", "afxxvzxvc", "qwreasgf", "zxcgdfg" } );

    fieldSplitter.setFieldTrimType( new int[] { 1, 0, 2 } );
    fieldSplitter.setFieldType( new int[] { 1, 1, 0, 3 } );

    try {
      String badXml = fieldSplitter.getXML();
      Assert.fail( "Before calling afterInjectionSynchronization, should have thrown an ArrayIndexOOB" );
    } catch ( Exception expected ) {
      // Do Nothing
    }
    fieldSplitter.afterInjectionSynchronization();
    //run without a exception
    String ktrXml = fieldSplitter.getXML();

    int targetSz = fieldSplitter.getFieldName().length;
    Assert.assertEquals( targetSz, fieldSplitter.getFieldID().length );
    Assert.assertEquals( targetSz, fieldSplitter.getFieldRemoveID().length );
    Assert.assertEquals( targetSz, fieldSplitter.getFieldFormat().length );
    Assert.assertEquals( targetSz, fieldSplitter.getFieldGroup().length );
    Assert.assertEquals( targetSz, fieldSplitter.getFieldDecimal().length );
    Assert.assertEquals( targetSz, fieldSplitter.getFieldCurrency().length );
    Assert.assertEquals( targetSz, fieldSplitter.getFieldLength().length );
    Assert.assertEquals( targetSz, fieldSplitter.getFieldPrecision().length );
    Assert.assertEquals( targetSz, fieldSplitter.getFieldNullIf().length );
    Assert.assertEquals( targetSz, fieldSplitter.getFieldIfNull().length );
    Assert.assertEquals( targetSz, fieldSplitter.getFieldType().length );
    Assert.assertEquals( targetSz, fieldSplitter.getFieldTrimType().length );

    // Check for null arrays being handled
    fieldSplitter.setFieldType( null ); // null int array
    fieldSplitter.setFieldRemoveID( null ); // null boolean array
    fieldSplitter.setFieldID( null ); // null string array
    fieldSplitter.afterInjectionSynchronization();
    Assert.assertEquals( targetSz, fieldSplitter.getFieldType().length );
    Assert.assertEquals( targetSz, fieldSplitter.getFieldID().length );
    Assert.assertEquals( targetSz, fieldSplitter.getFieldRemoveID().length );

  }

}
