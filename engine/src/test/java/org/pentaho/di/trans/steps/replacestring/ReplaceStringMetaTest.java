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

package org.pentaho.di.trans.steps.replacestring;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.loadsave.LoadSaveTester;
import org.pentaho.di.trans.steps.loadsave.validator.ArrayLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.BooleanLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.IntLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.PrimitiveBooleanArrayLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.PrimitiveIntegerArrayLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.StringLoadSaveValidator;
import org.pentaho.di.trans.steps.mock.StepMockHelper;
import org.pentaho.metastore.api.IMetaStore;

public class ReplaceStringMetaTest {

  private static final String FIELD_NAME = "test";
  private static final String ENCODING_NAME = "UTF-8";

  @Test
  public void testGetFields() throws KettleStepException {
    ReplaceStringMeta meta = new ReplaceStringMeta();
    meta.setFieldInStream( new String[] { FIELD_NAME } );
    meta.setFieldOutStream( new String[] { FIELD_NAME } );

    ValueMetaInterface inputFieldMeta = mock( ValueMetaInterface.class );
    when( inputFieldMeta.getStringEncoding() ).thenReturn( ENCODING_NAME );

    RowMetaInterface inputRowMeta = mock( RowMetaInterface.class );
    when( inputRowMeta.searchValueMeta( anyString() ) ).thenReturn( inputFieldMeta );

    StepMeta nextStep = mock( StepMeta.class );
    VariableSpace space = mock( VariableSpace.class );
    Repository repository = mock( Repository.class );
    IMetaStore metaStore = mock( IMetaStore.class );
    meta.getFields( inputRowMeta, "test", null, nextStep, space, repository, metaStore );

    ArgumentCaptor<ValueMetaInterface> argument = ArgumentCaptor.forClass( ValueMetaInterface.class );
    verify( inputRowMeta ).addValueMeta( argument.capture() );
    assertEquals( ENCODING_NAME, argument.getValue().getStringEncoding() );
  }

  @Test
  public void testRoundTrips() throws KettleException {
    List<String> attributes = Arrays.asList( "in_stream_name", "out_stream_name", "use_regex", "replace_string",
      "replace_by_string", "set_empty_string", "replace_field_by_string", "whole_word", "case_sensitive", "is_unicode" );

    Map<String, String> getterMap = new HashMap<String, String>();
    getterMap.put( "in_stream_name", "getFieldInStream" );
    getterMap.put( "out_stream_name", "getFieldOutStream" );
    getterMap.put( "use_regex", "getUseRegEx" );
    getterMap.put( "replace_string", "getReplaceString" );
    getterMap.put( "replace_by_string", "getReplaceByString" );
    getterMap.put( "set_empty_string", "isSetEmptyString" );
    getterMap.put( "replace_field_by_string", "getFieldReplaceByString" );
    getterMap.put( "whole_word", "getWholeWord" );
    getterMap.put( "case_sensitive", "getCaseSensitive" );
    getterMap.put( "is_unicode", "isUnicode" );

    Map<String, String> setterMap = new HashMap<String, String>();
    setterMap.put( "in_stream_name", "setFieldInStream" );
    setterMap.put( "out_stream_name", "setFieldOutStream" );
    setterMap.put( "use_regex", "setUseRegEx" );
    setterMap.put( "replace_string", "setReplaceString" );
    setterMap.put( "replace_by_string", "setReplaceByString" );
    setterMap.put( "set_empty_string", "setEmptyString" );
    setterMap.put( "replace_field_by_string", "setFieldReplaceByString" );
    setterMap.put( "whole_word", "setWholeWord" );
    setterMap.put( "case_sensitive", "setCaseSensitive" );
    setterMap.put( "is_unicode", "setIsUnicode" );

    Map<String, FieldLoadSaveValidator<?>> fieldLoadSaveValidatorAttributeMap =
      new HashMap<String, FieldLoadSaveValidator<?>>();
    FieldLoadSaveValidator<String[]> stringArrayLoadSaveValidator =
      new ArrayLoadSaveValidator<String>( new StringLoadSaveValidator(), 25 );
    FieldLoadSaveValidator<boolean[]> booleanArrayLoadSaveValidator =
      new PrimitiveBooleanArrayLoadSaveValidator( new BooleanLoadSaveValidator(), 25 );
    FieldLoadSaveValidator<int[]> useRegExArrayLoadSaveValidator =
      new PrimitiveIntegerArrayLoadSaveValidator(
        new IntLoadSaveValidator( ReplaceStringMeta.useRegExCode.length ), 25 );
    FieldLoadSaveValidator<int[]> wholeWordArrayLoadSaveValidator =
      new PrimitiveIntegerArrayLoadSaveValidator(
        new IntLoadSaveValidator( ReplaceStringMeta.wholeWordCode.length ), 25 );
    FieldLoadSaveValidator<int[]> caseSensitiveArrayLoadSaveValidator =
      new PrimitiveIntegerArrayLoadSaveValidator(
        new IntLoadSaveValidator( ReplaceStringMeta.caseSensitiveCode.length ), 25 );
    FieldLoadSaveValidator<int[]> isUnicodeArrayLoadSaveValidator =
      new PrimitiveIntegerArrayLoadSaveValidator(
        new IntLoadSaveValidator( ReplaceStringMeta.isUnicodeCode.length ), 25 );

    fieldLoadSaveValidatorAttributeMap.put( "in_stream_name", stringArrayLoadSaveValidator );
    fieldLoadSaveValidatorAttributeMap.put( "out_stream_name", stringArrayLoadSaveValidator );
    fieldLoadSaveValidatorAttributeMap.put( "use_regex", useRegExArrayLoadSaveValidator );
    fieldLoadSaveValidatorAttributeMap.put( "replace_string", stringArrayLoadSaveValidator );
    fieldLoadSaveValidatorAttributeMap.put( "replace_by_string", stringArrayLoadSaveValidator );
    fieldLoadSaveValidatorAttributeMap.put( "set_empty_string", booleanArrayLoadSaveValidator );
    fieldLoadSaveValidatorAttributeMap.put( "replace_field_by_string", stringArrayLoadSaveValidator );
    fieldLoadSaveValidatorAttributeMap.put( "whole_word", wholeWordArrayLoadSaveValidator );
    fieldLoadSaveValidatorAttributeMap.put( "case_sensitive", caseSensitiveArrayLoadSaveValidator );
    fieldLoadSaveValidatorAttributeMap.put( "is_unicode", isUnicodeArrayLoadSaveValidator );

    LoadSaveTester loadSaveTester =
      new LoadSaveTester( ReplaceStringMeta.class, attributes, getterMap, setterMap,
        fieldLoadSaveValidatorAttributeMap, new HashMap<String, FieldLoadSaveValidator<?>>() );

    loadSaveTester.testSerialization();
  }

  @Test
  public void testPDI16559() throws Exception {
    StepMockHelper<ReplaceStringMeta, ReplaceStringData> mockHelper =
        new StepMockHelper<ReplaceStringMeta, ReplaceStringData>( "replaceString", ReplaceStringMeta.class, ReplaceStringData.class );

    ReplaceStringMeta replaceString = new ReplaceStringMeta();

    // String Arrays
    replaceString.setFieldInStream( new String[] { "field1", "field2", "field3", "field4", "field5" } );
    replaceString.setFieldOutStream( new String[] { "outField1", "outField2", "outField3" } );
    replaceString.setReplaceString( new String[] { "rep1", "rep 2", "rep 3" }  );
    replaceString.setReplaceByString( new String[] { "by1", "by 2" }  );
    replaceString.setFieldReplaceByString( new String[] { "fieldby1", "fieldby2", "fieldby3", "fieldby4" } );

    // Other arrays
    replaceString.setUseRegEx( new int[] { 0, 1, 0 } );
    replaceString.setWholeWord( new int[] { 1, 1, 0, 0, 1 } );
    replaceString.setCaseSensitive( new int[] { 1, 0, 0, 1 } );
    replaceString.setEmptyString( new boolean[] { true, false } );
    replaceString.setIsUnicode( new int[] { 1, 0, 0, 1 } );

    try {
      String badXml = replaceString.getXML();
      Assert.fail( "Before calling afterInjectionSynchronization, should have thrown an ArrayIndexOOB" );
    } catch ( Exception expected ) {
      // Do Nothing
    }
    replaceString.afterInjectionSynchronization();
    //run without a exception
    String ktrXml = replaceString.getXML();

    int targetSz = replaceString.getFieldInStream().length;
    Assert.assertEquals( targetSz, replaceString.getFieldOutStream().length );
    Assert.assertEquals( targetSz, replaceString.getUseRegEx().length );
    Assert.assertEquals( targetSz, replaceString.getReplaceString().length );
    Assert.assertEquals( targetSz, replaceString.getReplaceByString().length );
    Assert.assertEquals( targetSz, replaceString.isSetEmptyString().length );
    Assert.assertEquals( targetSz, replaceString.getFieldReplaceByString().length );
    Assert.assertEquals( targetSz, replaceString.getWholeWord().length );
    Assert.assertEquals( targetSz, replaceString.getCaseSensitive().length );
    Assert.assertEquals( targetSz, replaceString.isUnicode().length );

    Assert.assertEquals( "", replaceString.getFieldOutStream()[ 3 ] );
    Assert.assertEquals( "", replaceString.getReplaceString()[ 3 ] );
    Assert.assertEquals( "", replaceString.getReplaceByString()[ 3 ] );
    Assert.assertEquals( "", replaceString.getFieldReplaceByString()[ 4 ] );

    Assert.assertEquals( "outField1", replaceString.getFieldOutStream()[0] );
    Assert.assertEquals( 1, replaceString.getWholeWord()[0] );
    Assert.assertEquals( true, replaceString.isSetEmptyString()[0] );
  }

}
