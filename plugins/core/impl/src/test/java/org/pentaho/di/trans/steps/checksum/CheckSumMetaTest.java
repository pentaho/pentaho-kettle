/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2019 by Hitachi Vantara : http://www.pentaho.com
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

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.trans.steps.loadsave.LoadSaveTester;
import org.pentaho.di.trans.steps.loadsave.initializer.InitializerInterface;
import org.pentaho.di.trans.steps.loadsave.validator.ArrayLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.IntLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.StringLoadSaveValidator;

public class CheckSumMetaTest implements InitializerInterface<CheckSumMeta> {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

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
  }

  @Test
  public void testSerialization() throws KettleException {
    List<String> attributes =
      Arrays.asList( "FieldName", "ResultFieldName", "CheckSumType", "CompatibilityMode", "ResultType", "oldChecksumBehaviour", "fieldSeparatorString" );

    Map<String, String> getterMap = new HashMap<String, String>();
    Map<String, String> setterMap = new HashMap<String, String>();
    getterMap.put( "CheckSumType", "getTypeByDesc" );

    FieldLoadSaveValidator<String[]> stringArrayLoadSaveValidator =
      new ArrayLoadSaveValidator<String>( new StringLoadSaveValidator(), 5 );

    Map<String, FieldLoadSaveValidator<?>> attrValidatorMap = new HashMap<String, FieldLoadSaveValidator<?>>();
    attrValidatorMap.put( "FieldName", stringArrayLoadSaveValidator );
    attrValidatorMap.put( "CheckSumType", new IntLoadSaveValidator( CheckSumMeta.checksumtypeCodes.length ) );
    attrValidatorMap.put( "ResultType", new IntLoadSaveValidator( CheckSumMeta.resultTypeCode.length ) );

    Map<String, FieldLoadSaveValidator<?>> typeValidatorMap = new HashMap<String, FieldLoadSaveValidator<?>>();

    LoadSaveTester<CheckSumMeta> loadSaveTester =
      new LoadSaveTester<>( CheckSumMeta.class, attributes, getterMap, setterMap,
        attrValidatorMap, typeValidatorMap, this );

    loadSaveTester.testSerialization();
  }
}
