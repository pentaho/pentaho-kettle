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

package org.pentaho.di.trans.steps.normalizer;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.trans.steps.loadsave.LoadSaveTester;
import org.pentaho.di.trans.steps.loadsave.validator.ArrayLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;
import org.pentaho.di.trans.steps.normaliser.NormaliserMeta;
import org.pentaho.di.trans.steps.normaliser.NormaliserMeta.NormaliserField;

public class NormalizerMetaTest {

  @Test
  public void loadSaveTest() throws KettleException {
    List<String> attributes = Arrays.asList( "normaliserFields" );

    NormaliserField testField = new NormaliserField();
    testField.setName( "TEST_NAME" );
    testField.setValue( "TEST_VALUE" );
    testField.setNorm( "TEST" );

    Map<String, FieldLoadSaveValidator<?>> fieldLoadSaveValidatorTypeMap =
        new HashMap<String, FieldLoadSaveValidator<?>>();
    fieldLoadSaveValidatorTypeMap.put( NormaliserField[].class.getCanonicalName(),
        new ArrayLoadSaveValidator<NormaliserField>( new NormaliserFieldLoadSaveValidator( testField ), 50 ) );

    LoadSaveTester<NormaliserMeta> tester =
        new LoadSaveTester<NormaliserMeta>( NormaliserMeta.class, attributes, new HashMap<String, String>(),
            new HashMap<String, String>(), new HashMap<String, FieldLoadSaveValidator<?>>(),
            fieldLoadSaveValidatorTypeMap );

    tester.testSerialization();
  }

  public static class NormaliserFieldLoadSaveValidator implements FieldLoadSaveValidator<NormaliserField> {

    private final NormaliserField defaultValue;

    public NormaliserFieldLoadSaveValidator( NormaliserField defaultValue ) {
      this.defaultValue = defaultValue;
    }

    @Override
    public NormaliserField getTestObject() {
      return defaultValue;
    }

    @Override
    public boolean validateTestObject( NormaliserField testObject, Object actual ) {
      return testObject.equals( actual );
    }

  }

}
