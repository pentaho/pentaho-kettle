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

package org.pentaho.di.trans.steps.symmetriccrypto.symmetriccryptotrans;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.trans.steps.loadsave.LoadSaveTester;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.IntLoadSaveValidator;

public class SymmetricCryptoTransMetaTest {

  @Test
  public void testRoundTrip() throws KettleException {
    KettleEnvironment.init();

    List<String> attributes = Arrays.asList( "operation_type", "algorithm", "schema", "secretKeyField", "messageField",
      "resultfieldname", "secretKey", "secretKeyInField", "readKeyAsBinary", "outputResultAsBinary" );

    Map<String, String> getterMap = new HashMap<String, String>();
    getterMap.put( "operation_type", "getOperationType" );
    getterMap.put( "algorithm", "getAlgorithm" );
    getterMap.put( "schema", "getSchema" );
    getterMap.put( "secretKeyField", "getSecretKeyField" );
    getterMap.put( "messageField", "getMessageField" );
    getterMap.put( "resultfieldname", "getResultfieldname" );
    getterMap.put( "secretKey", "getSecretKey" );
    getterMap.put( "secretKeyInField", "isSecretKeyInField" );
    getterMap.put( "readKeyAsBinary", "isReadKeyAsBinary" );
    getterMap.put( "outputResultAsBinary", "isOutputResultAsBinary" );

    Map<String, String> setterMap = new HashMap<String, String>();
    setterMap.put( "operation_type", "setOperationType" );
    setterMap.put( "algorithm", "setAlgorithm" );
    setterMap.put( "schema", "setSchema" );
    setterMap.put( "secretKeyField", "setsecretKeyField" );
    setterMap.put( "messageField", "setMessageField" );
    setterMap.put( "resultfieldname", "setResultfieldname" );
    setterMap.put( "secretKey", "setSecretKey" );
    setterMap.put( "secretKeyInField", "setSecretKeyInField" );
    setterMap.put( "readKeyAsBinary", "setReadKeyAsBinary" );
    setterMap.put( "outputResultAsBinary", "setOutputResultAsBinary" );

    Map<String, FieldLoadSaveValidator<?>> fieldLoadSaveValidator = new HashMap<String, FieldLoadSaveValidator<?>>();
    fieldLoadSaveValidator.put( "operation_type",
      new IntLoadSaveValidator( SymmetricCryptoTransMeta.operationTypeCode.length ) );

    LoadSaveTester loadSaveTester = new LoadSaveTester( SymmetricCryptoTransMeta.class, attributes,
      getterMap, setterMap, fieldLoadSaveValidator, new HashMap<String, FieldLoadSaveValidator<?>>() );

    loadSaveTester.testSerialization();
  }
}
