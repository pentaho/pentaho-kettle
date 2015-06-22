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

package org.pentaho.di.trans.steps.replacestring;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

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
import org.pentaho.di.trans.steps.loadsave.validator.PrimitiveBooleanArrayLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.PrimitiveIntegerArrayLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.StringLoadSaveValidator;
import org.pentaho.metastore.api.IMetaStore;

public class ReplaceStringMetaTest {

  private static final String FIELD_NAME = "test";
  private static final String ENCODING_NAME = "UTF-8";

  public class UseRegExLoadSaveValidator implements FieldLoadSaveValidator<Integer> {
    public Integer getTestObject() {
      return new Random().nextInt( ReplaceStringMeta.useRegExCode.length );
    }

    public boolean validateTestObject( Integer testObject, Object actual ) {
      return testObject.equals( actual );
    }
  }
  public class WholeWordLoadSaveValidator implements FieldLoadSaveValidator<Integer> {
    public Integer getTestObject() {
      return new Random().nextInt( ReplaceStringMeta.wholeWordCode.length );
    }

    public boolean validateTestObject( Integer testObject, Object actual ) {
      return testObject.equals( actual );
    }
  }

  public class CaseSensitiveLoadSaveValidator implements FieldLoadSaveValidator<Integer> {
    public Integer getTestObject() {
      return new Random().nextInt( ReplaceStringMeta.caseSensitiveCode.length );
    }

    public boolean validateTestObject( Integer testObject, Object actual ) {
      return testObject.equals( actual );
    }
  }

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
      "replace_by_string", "set_empty_string", "replace_field_by_string", "whole_word", "case_sensitive" );

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

    Map<String, FieldLoadSaveValidator<?>> fieldLoadSaveValidatorAttributeMap =
      new HashMap<String, FieldLoadSaveValidator<?>>();
    FieldLoadSaveValidator<String[]> stringArrayLoadSaveValidator =
      new ArrayLoadSaveValidator<String>( new StringLoadSaveValidator(), 25 );
    FieldLoadSaveValidator<boolean[]> booleanArrayLoadSaveValidator =
      new PrimitiveBooleanArrayLoadSaveValidator( new BooleanLoadSaveValidator(), 25 );
    FieldLoadSaveValidator<int[]> useRegExArrayLoadSaveValidator =
      new PrimitiveIntegerArrayLoadSaveValidator( new UseRegExLoadSaveValidator(), 25 );
    FieldLoadSaveValidator<int[]> wholeWordArrayLoadSaveValidator =
      new PrimitiveIntegerArrayLoadSaveValidator( new WholeWordLoadSaveValidator(), 25 );
    FieldLoadSaveValidator<int[]> caseSensitiveArrayLoadSaveValidator =
      new PrimitiveIntegerArrayLoadSaveValidator( new CaseSensitiveLoadSaveValidator(), 25 );

    fieldLoadSaveValidatorAttributeMap.put( "in_stream_name", stringArrayLoadSaveValidator );
    fieldLoadSaveValidatorAttributeMap.put( "out_stream_name", stringArrayLoadSaveValidator );
    fieldLoadSaveValidatorAttributeMap.put( "use_regex", useRegExArrayLoadSaveValidator );
    fieldLoadSaveValidatorAttributeMap.put( "replace_string", stringArrayLoadSaveValidator );
    fieldLoadSaveValidatorAttributeMap.put( "replace_by_string", stringArrayLoadSaveValidator );
    fieldLoadSaveValidatorAttributeMap.put( "set_empty_string", booleanArrayLoadSaveValidator );
    fieldLoadSaveValidatorAttributeMap.put( "replace_field_by_string", stringArrayLoadSaveValidator );
    fieldLoadSaveValidatorAttributeMap.put( "whole_word", wholeWordArrayLoadSaveValidator );
    fieldLoadSaveValidatorAttributeMap.put( "case_sensitive", caseSensitiveArrayLoadSaveValidator );

    LoadSaveTester loadSaveTester =
      new LoadSaveTester( ReplaceStringMeta.class, attributes, getterMap, setterMap,
        fieldLoadSaveValidatorAttributeMap, new HashMap<String, FieldLoadSaveValidator<?>>() );

    loadSaveTester.testRepoRoundTrip();
    loadSaveTester.testXmlRoundTrip();
  }
}
