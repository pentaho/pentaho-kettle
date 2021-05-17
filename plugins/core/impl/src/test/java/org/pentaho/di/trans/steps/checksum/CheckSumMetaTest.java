/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2021 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.checksum;

import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.trans.steps.loadsave.LoadSaveTester;
import org.pentaho.di.trans.steps.loadsave.initializer.InitializerInterface;
import org.pentaho.di.trans.steps.loadsave.validator.ArrayLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.IntLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.StringLoadSaveValidator;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;

public class CheckSumMetaTest implements InitializerInterface<CheckSumMeta> {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  private static final int A_NEGATIVE_NUMBER = -1234;
  private static final int A_HUGE_POSITIVE_NUMBER = Integer.MAX_VALUE;
  private static final String SOME_RANDOM_STRING = UUID.randomUUID().toString();

  // Call the allocate method on the LoadSaveTester meta class
  @Override
  public void modify( CheckSumMeta someMeta ) {
    someMeta.allocate( 5 );
  }

  @Test
  public void testConstants() {
    assertEquals( "CRC32", CheckSumMeta.TYPE_CRC32 );
    assertEquals( "CRC32", CheckSumMeta.checksumtypeCodes[0] );
    assertEquals( "ADLER32", CheckSumMeta.TYPE_ADLER32 );
    assertEquals( "ADLER32", CheckSumMeta.checksumtypeCodes[1] );
    assertEquals( "MD5", CheckSumMeta.TYPE_MD5 );
    assertEquals( "MD5", CheckSumMeta.checksumtypeCodes[2] );
    assertEquals( "SHA-1", CheckSumMeta.TYPE_SHA1 );
    assertEquals( "SHA-1", CheckSumMeta.checksumtypeCodes[3] );
    assertEquals( "SHA-256", CheckSumMeta.TYPE_SHA256 );
    assertEquals( "SHA-256", CheckSumMeta.checksumtypeCodes[4] );
    assertEquals( CheckSumMeta.checksumtypeCodes.length, CheckSumMeta.checksumtypeDescs.length );
    assertEquals( CheckSumMeta.EVALUATION_METHOD_CODES.length, CheckSumMeta.EVALUATION_METHOD_DESCS.length );
    assertEquals( "BYTES",
      CheckSumMeta.EVALUATION_METHOD_CODES[ CheckSumMeta.EVALUATION_METHOD_BYTES ] );
    assertEquals( "PENTAHO_STRINGS",
      CheckSumMeta.EVALUATION_METHOD_CODES[ CheckSumMeta.EVALUATION_METHOD_PENTAHO_STRINGS ] );
    assertEquals( "NATIVE_STRINGS",
      CheckSumMeta.EVALUATION_METHOD_CODES[ CheckSumMeta.EVALUATION_METHOD_NATIVE_STRINGS ] );
  }

  @Test
  public void testSerialization() throws KettleException {
    List<String> attributes =
      Arrays.asList( "FieldName", "ResultFieldName", "CheckSumType", "CompatibilityMode", "ResultType",
        "oldChecksumBehaviour", "fieldSeparatorString", "evaluationMethod" );

    Map<String, String> getterMap = new HashMap<>();
    Map<String, String> setterMap = new HashMap<>();
    getterMap.put( "CheckSumType", "getTypeByDesc" );
    getterMap.put( "evaluationMethod", "getEvaluationMethod" );

    FieldLoadSaveValidator<String[]> stringArrayLoadSaveValidator =
      new ArrayLoadSaveValidator<>( new StringLoadSaveValidator(), 5 );

    Map<String, FieldLoadSaveValidator<?>> attrValidatorMap = new HashMap<>();
    attrValidatorMap.put( "FieldName", stringArrayLoadSaveValidator );
    attrValidatorMap.put( "CheckSumType", new IntLoadSaveValidator( CheckSumMeta.checksumtypeCodes.length ) );
    attrValidatorMap.put( "ResultType", new IntLoadSaveValidator( CheckSumMeta.resultTypeCode.length ) );
    attrValidatorMap.put( "evaluationMethod", new IntLoadSaveValidator( CheckSumMeta.EVALUATION_METHOD_CODES.length ) );

    Map<String, FieldLoadSaveValidator<?>> typeValidatorMap = new HashMap<>();

    LoadSaveTester<CheckSumMeta> loadSaveTester =
      new LoadSaveTester<>( CheckSumMeta.class, attributes, getterMap, setterMap,
        attrValidatorMap, typeValidatorMap, this );

    loadSaveTester.testSerialization();
  }

  @Test
  public void testGetEvaluationMethodByDesc() throws Exception {
    CheckSumMeta checkSumMeta = new CheckSumMeta();

    // Passing 'null'
    int evaluationMethod = checkSumMeta.getEvaluationMethodByDesc( null );
    assertEquals( CheckSumMeta.DEFAULT_EVALUATION_METHOD, evaluationMethod );

    // Passing an unknown description
    evaluationMethod = checkSumMeta.getEvaluationMethodByDesc( "$#%#&$/(%&%#$%($/)" );
    assertEquals( CheckSumMeta.DEFAULT_EVALUATION_METHOD, evaluationMethod );

    // The descriptions
    evaluationMethod = checkSumMeta
      .getEvaluationMethodByDesc( CheckSumMeta.EVALUATION_METHOD_DESCS[ CheckSumMeta.EVALUATION_METHOD_BYTES ] );
    assertEquals( CheckSumMeta.EVALUATION_METHOD_BYTES, evaluationMethod );

    evaluationMethod =
      checkSumMeta.getEvaluationMethodByDesc( Const.KETTLE_CHECKSUM_EVALUATION_METHOD_PENTAHO_STRINGS );
    assertEquals( CheckSumMeta.EVALUATION_METHOD_PENTAHO_STRINGS, evaluationMethod );

    evaluationMethod = checkSumMeta.getEvaluationMethodByDesc(
      CheckSumMeta.EVALUATION_METHOD_DESCS[ CheckSumMeta.EVALUATION_METHOD_NATIVE_STRINGS ] );
    assertEquals( CheckSumMeta.EVALUATION_METHOD_NATIVE_STRINGS, evaluationMethod );

    // Passing the Code instead of the description
    evaluationMethod = checkSumMeta.getEvaluationMethodByDesc( Const.KETTLE_CHECKSUM_EVALUATION_METHOD_BYTES );
    assertEquals( CheckSumMeta.EVALUATION_METHOD_BYTES, evaluationMethod );

    evaluationMethod =
      checkSumMeta.getEvaluationMethodByDesc( Const.KETTLE_CHECKSUM_EVALUATION_METHOD_PENTAHO_STRINGS );
    assertEquals( CheckSumMeta.EVALUATION_METHOD_PENTAHO_STRINGS, evaluationMethod );

    evaluationMethod = checkSumMeta.getEvaluationMethodByDesc( Const.KETTLE_CHECKSUM_EVALUATION_METHOD_NATIVE_STRINGS );
    assertEquals( CheckSumMeta.EVALUATION_METHOD_NATIVE_STRINGS, evaluationMethod );
  }

  @Test
  public void testGetEvaluationMethodByCode() throws Exception {
    CheckSumMeta checkSumMeta = new CheckSumMeta();

    // Passing 'null'
    int evaluationMethod = checkSumMeta.getEvaluationMethodByCode( null );
    assertEquals( CheckSumMeta.DEFAULT_EVALUATION_METHOD, evaluationMethod );

    // Passing an unknown code
    evaluationMethod = checkSumMeta.getEvaluationMethodByCode( "$#%#&$/(%&%#$%($/)" );
    assertEquals( CheckSumMeta.DEFAULT_EVALUATION_METHOD, evaluationMethod );

    // The Codes
    evaluationMethod = checkSumMeta.getEvaluationMethodByCode( Const.KETTLE_CHECKSUM_EVALUATION_METHOD_BYTES );
    assertEquals( CheckSumMeta.EVALUATION_METHOD_BYTES, evaluationMethod );

    evaluationMethod =
      checkSumMeta.getEvaluationMethodByCode( Const.KETTLE_CHECKSUM_EVALUATION_METHOD_PENTAHO_STRINGS );
    assertEquals( CheckSumMeta.EVALUATION_METHOD_PENTAHO_STRINGS, evaluationMethod );

    evaluationMethod = checkSumMeta.getEvaluationMethodByCode( Const.KETTLE_CHECKSUM_EVALUATION_METHOD_NATIVE_STRINGS );
    assertEquals( CheckSumMeta.EVALUATION_METHOD_NATIVE_STRINGS, evaluationMethod );
  }

  @Test
  public void testGetEvaluationMethodCode() throws Exception {
    CheckSumMeta checkSumMeta = new CheckSumMeta();

    // Passing a negative number
    String evaluationMethodCode = CheckSumMeta.getEvaluationMethodCode( A_NEGATIVE_NUMBER );
    assertEquals( Const.KETTLE_CHECKSUM_EVALUATION_METHOD_DEFAULT, evaluationMethodCode );

    // Passing a huge number
    evaluationMethodCode = CheckSumMeta.getEvaluationMethodCode( A_HUGE_POSITIVE_NUMBER );
    assertEquals( Const.KETTLE_CHECKSUM_EVALUATION_METHOD_DEFAULT, evaluationMethodCode );

    // The Codes
    evaluationMethodCode = CheckSumMeta.getEvaluationMethodCode( CheckSumMeta.EVALUATION_METHOD_BYTES );
    assertEquals( Const.KETTLE_CHECKSUM_EVALUATION_METHOD_BYTES, evaluationMethodCode );

    evaluationMethodCode = CheckSumMeta.getEvaluationMethodCode( CheckSumMeta.EVALUATION_METHOD_PENTAHO_STRINGS );
    assertEquals( Const.KETTLE_CHECKSUM_EVALUATION_METHOD_PENTAHO_STRINGS, evaluationMethodCode );

    evaluationMethodCode = CheckSumMeta.getEvaluationMethodCode( CheckSumMeta.EVALUATION_METHOD_NATIVE_STRINGS );
    assertEquals( Const.KETTLE_CHECKSUM_EVALUATION_METHOD_NATIVE_STRINGS, evaluationMethodCode );
  }

  @Test
  public void test_setAndGetEvaluationMethod() {
    CheckSumMeta checkSumMeta = new CheckSumMeta();

    // Passing a negative number
    checkSumMeta.setEvaluationMethod( A_NEGATIVE_NUMBER );
    assertEquals( A_NEGATIVE_NUMBER, checkSumMeta.getEvaluationMethod() );

    // Passing a huge number
    checkSumMeta.setEvaluationMethod( A_HUGE_POSITIVE_NUMBER );
    assertEquals( A_HUGE_POSITIVE_NUMBER, checkSumMeta.getEvaluationMethod() );

    // Known valid values
    checkSumMeta.setEvaluationMethod( CheckSumMeta.EVALUATION_METHOD_BYTES );
    assertEquals( CheckSumMeta.EVALUATION_METHOD_BYTES, checkSumMeta.getEvaluationMethod() );
    checkSumMeta.setEvaluationMethod( CheckSumMeta.EVALUATION_METHOD_PENTAHO_STRINGS );
    assertEquals( CheckSumMeta.EVALUATION_METHOD_PENTAHO_STRINGS, checkSumMeta.getEvaluationMethod() );
    checkSumMeta.setEvaluationMethod( CheckSumMeta.EVALUATION_METHOD_NATIVE_STRINGS );
    assertEquals( CheckSumMeta.EVALUATION_METHOD_NATIVE_STRINGS, checkSumMeta.getEvaluationMethod() );
    checkSumMeta.setEvaluationMethod( CheckSumMeta.DEFAULT_EVALUATION_METHOD );
    assertEquals( CheckSumMeta.DEFAULT_EVALUATION_METHOD, checkSumMeta.getEvaluationMethod() );
  }

  @Test
  public void test_setAndGetResultType() {
    CheckSumMeta checkSumMeta = new CheckSumMeta();

    // Passing a negative number
    checkSumMeta.setResultType( A_NEGATIVE_NUMBER );
    assertEquals( A_NEGATIVE_NUMBER, checkSumMeta.getResultType() );

    // Passing a huge number
    checkSumMeta.setResultType( A_HUGE_POSITIVE_NUMBER );
    assertEquals( A_HUGE_POSITIVE_NUMBER, checkSumMeta.getResultType() );

    // Known valid values
    checkSumMeta.setResultType( CheckSumMeta.result_TYPE_STRING );
    assertEquals( CheckSumMeta.result_TYPE_STRING, checkSumMeta.getResultType() );
    checkSumMeta.setResultType( CheckSumMeta.result_TYPE_HEXADECIMAL );
    assertEquals( CheckSumMeta.result_TYPE_HEXADECIMAL, checkSumMeta.getResultType() );
    checkSumMeta.setResultType( CheckSumMeta.result_TYPE_BINARY );
    assertEquals( CheckSumMeta.result_TYPE_BINARY, checkSumMeta.getResultType() );
  }

  @Test
  public void test_setAndGetFieldSeparatorString() {
    CheckSumMeta checkSumMeta = new CheckSumMeta();

    // Passing 'null'
    checkSumMeta.setFieldSeparatorString( null );
    assertNull( checkSumMeta.getFieldSeparatorString() );

    // Passing a huge number
    checkSumMeta.setFieldSeparatorString( SOME_RANDOM_STRING );
    assertEquals( SOME_RANDOM_STRING, checkSumMeta.getFieldSeparatorString() );
  }

  @Test
  public void testAllocate() {
    CheckSumMeta checkSumMeta = new CheckSumMeta();
    Random random = new Random();
    int maxAllocation = 50;

    // Initially the array should exist but be empty
    String[] fieldNames = checkSumMeta.getFieldName();
    assertNotNull( fieldNames );
    assertEquals( 0, fieldNames.length );

    // Some random numbers
    for ( int i = 0; i < 10; ++i ) {
      int n = random.nextInt( maxAllocation );
      checkSumMeta.allocate( n );
      fieldNames = checkSumMeta.getFieldName();
      assertNotNull( fieldNames );
      assertEquals( n, fieldNames.length );
    }

    // Zero
    checkSumMeta.allocate( 0 );
    fieldNames = checkSumMeta.getFieldName();
    assertNotNull( fieldNames );
    assertEquals( 0, fieldNames.length );
  }
}
