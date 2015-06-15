/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2015 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.stringoperations;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.trans.steps.loadsave.LoadSaveTester;
import org.pentaho.di.trans.steps.loadsave.validator.ArrayLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.IntLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.PrimitiveIntegerArrayLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.StringLoadSaveValidator;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

/**
 * User: Dzmitry Stsiapanau Date: 2/3/14 Time: 5:41 PM
 */
public class StringOperationsMetaTest {
  @Test
  public void testGetFields() throws Exception {
    StringOperationsMeta meta = new StringOperationsMeta();
    meta.allocate( 1 );
    meta.setFieldInStream( new String[] { "field1" } );

    RowMetaInterface rowMetaInterface = new RowMeta();
    ValueMetaInterface valueMeta = new ValueMeta( "field1", ValueMeta.TYPE_STRING );
    valueMeta.setStorageMetadata( new ValueMeta( "field1", ValueMeta.TYPE_STRING ) );
    valueMeta.setStorageType( ValueMetaInterface.STORAGE_TYPE_BINARY_STRING );
    rowMetaInterface.addValueMeta( valueMeta );

    VariableSpace space = mock( VariableSpace.class );
    meta.getFields( rowMetaInterface, "STRING_OPERATIONS", null, null, space, null, null );
    RowMetaInterface expectedRowMeta = new RowMeta();
    expectedRowMeta.addValueMeta( new ValueMeta( "field1", ValueMeta.TYPE_STRING ) );
    assertEquals( expectedRowMeta.toString(), rowMetaInterface.toString() );
  }

  @Test
  public void testRoundTrip() throws KettleException {
    List<String> attributes =
        Arrays.asList( "in_stream_name", "out_stream_name", "trim_type", "lower_upper", "padding_type", "pad_char",
          "pad_len", "init_cap", "mask_xml", "digits", "remove_special_characters", "reverseString" );

      Map<String, String> getterMap = new HashMap<String, String>();
      getterMap.put( "in_stream_name", "getFieldInStream" );
      getterMap.put( "out_stream_name", "getFieldOutStream" );
      getterMap.put( "trim_type", "getTrimType" );
      getterMap.put( "lower_upper", "getLowerUpper" );
      getterMap.put( "padding_type", "getPaddingType" );
      getterMap.put( "pad_char", "getPadChar" );
      getterMap.put( "pad_len", "getPadLen" );
      getterMap.put( "init_cap", "getInitCap" );
      getterMap.put( "mask_xml", "getMaskXML" );
      getterMap.put( "digits", "getDigits" );
      getterMap.put( "remove_special_characters", "getRemoveSpecialCharacters" );
      getterMap.put( "reverseString", "getReverseString" );

      Map<String, String> setterMap = new HashMap<String, String>();
      setterMap.put( "in_stream_name", "setFieldInStream" );
      setterMap.put( "out_stream_name", "setFieldOutStream" );
      setterMap.put( "trim_type", "setTrimType" );
      setterMap.put( "lower_upper", "setLowerUpper" );
      setterMap.put( "padding_type", "setPaddingType" );
      setterMap.put( "pad_char", "setPadChar" );
      setterMap.put( "pad_len", "setPadLen" );
      setterMap.put( "init_cap", "setInitCap" );
      setterMap.put( "mask_xml", "setMaskXML" );
      setterMap.put( "digits", "setDigits" );
      setterMap.put( "remove_special_characters", "setRemoveSpecialCharacters" );
      setterMap.put( "reverseString", "setReverseString" );

      Map<String, FieldLoadSaveValidator<?>> fieldLoadSaveValidatorAttributeMap =
        new HashMap<String, FieldLoadSaveValidator<?>>();

      fieldLoadSaveValidatorAttributeMap.put( "in_stream_name",
        new ArrayLoadSaveValidator<String>( new StringLoadSaveValidator(), 25 ) );
      fieldLoadSaveValidatorAttributeMap.put( "out_stream_name",
        new ArrayLoadSaveValidator<String>( new StringLoadSaveValidator(), 25 ) );
      fieldLoadSaveValidatorAttributeMap.put( "trim_type", new PrimitiveIntegerArrayLoadSaveValidator(
        new IntLoadSaveValidator( StringOperationsMeta.trimTypeCode.length - 1 ), 25 ) );
      fieldLoadSaveValidatorAttributeMap.put( "lower_upper", new PrimitiveIntegerArrayLoadSaveValidator(
        new IntLoadSaveValidator( StringOperationsMeta.lowerUpperCode.length - 1 ), 25 ) );
      fieldLoadSaveValidatorAttributeMap.put( "padding_type", new PrimitiveIntegerArrayLoadSaveValidator(
        new IntLoadSaveValidator( StringOperationsMeta.paddingCode.length - 1 ), 25 ) );
      fieldLoadSaveValidatorAttributeMap.put( "pad_char",
        new ArrayLoadSaveValidator<String>( new StringLoadSaveValidator(), 25 ) );
      fieldLoadSaveValidatorAttributeMap.put( "pad_len",
        new ArrayLoadSaveValidator<String>( new StringLoadSaveValidator(), 25 ) );
      fieldLoadSaveValidatorAttributeMap.put( "init_cap", new PrimitiveIntegerArrayLoadSaveValidator(
        new IntLoadSaveValidator( StringOperationsMeta.initCapCode.length - 1 ), 25 ) );
      fieldLoadSaveValidatorAttributeMap.put( "mask_xml", new PrimitiveIntegerArrayLoadSaveValidator(
        new IntLoadSaveValidator( StringOperationsMeta.maskXMLCode.length - 1 ), 25 ) );
      fieldLoadSaveValidatorAttributeMap.put( "digits", new PrimitiveIntegerArrayLoadSaveValidator(
        new IntLoadSaveValidator( StringOperationsMeta.digitsCode.length - 1 ), 25 ) );
      fieldLoadSaveValidatorAttributeMap.put( "remove_special_characters", new PrimitiveIntegerArrayLoadSaveValidator(
        new IntLoadSaveValidator( StringOperationsMeta.removeSpecialCharactersCode.length - 1 ), 25 ) );
      fieldLoadSaveValidatorAttributeMap.put( "reverseString", new PrimitiveIntegerArrayLoadSaveValidator(
        new IntLoadSaveValidator( StringOperationsMeta.reverseStringCode.length - 1 ), 25 ) );

      LoadSaveTester loadSaveTester =
        new LoadSaveTester( StringOperationsMeta.class, attributes, getterMap, setterMap,
          fieldLoadSaveValidatorAttributeMap, new HashMap<String, FieldLoadSaveValidator<?>>()  );

      loadSaveTester.testXmlRoundTrip();
      loadSaveTester.testRepoRoundTrip();
  }
}
