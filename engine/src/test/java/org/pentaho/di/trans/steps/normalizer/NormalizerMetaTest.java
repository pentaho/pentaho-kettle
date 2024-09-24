/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.di.trans.steps.normalizer;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.trans.steps.loadsave.LoadSaveTester;
import org.pentaho.di.trans.steps.loadsave.validator.ArrayLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;
import org.pentaho.di.trans.steps.normaliser.NormaliserMeta;
import org.pentaho.di.trans.steps.normaliser.NormaliserMeta.NormaliserField;

public class NormalizerMetaTest {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

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
