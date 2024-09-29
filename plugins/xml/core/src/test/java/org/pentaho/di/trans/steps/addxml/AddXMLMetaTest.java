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


package org.pentaho.di.trans.steps.addxml;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.junit.Test;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.trans.steps.loadsave.LoadSaveTester;
import org.pentaho.di.trans.steps.loadsave.validator.ArrayLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;

public class AddXMLMetaTest {

  @Test
  public void loadSaveTest() throws KettleException {
    List<String> attributes =
        Arrays.asList( "omitXMLheader", "omitNullValues", "encoding", "valueName", "rootNode", "outputFields" );

    XMLField xmlField = new XMLField();
    xmlField.setFieldName( "TEST_FIELD" );
    xmlField.setType( 0 );

    Map<String, FieldLoadSaveValidator<?>> fieldLoadSaveValidatorTypeMap =
        new HashMap<String, FieldLoadSaveValidator<?>>();
    fieldLoadSaveValidatorTypeMap.put( XMLField[].class.getCanonicalName(), new ArrayLoadSaveValidator<>(
        new XMLFieldLoadSaveValidator( xmlField ), 1 ) );

    LoadSaveTester tester =
        new LoadSaveTester( AddXMLMeta.class, attributes, new HashMap<String, String>(), new HashMap<String, String>(),
            new HashMap<String, FieldLoadSaveValidator<?>>(), fieldLoadSaveValidatorTypeMap );

    tester.testRepoRoundTrip();
    tester.testXmlRoundTrip();
  }

  public static class XMLFieldLoadSaveValidator implements FieldLoadSaveValidator<XMLField> {

    private final XMLField defaultValue;

    public XMLFieldLoadSaveValidator( XMLField defaultValue ) {
      this.defaultValue = defaultValue;
    }

    @Override
    public XMLField getTestObject() {
      return defaultValue;
    }

    @Override
    public boolean validateTestObject( XMLField testObject, Object actual ) {
      return EqualsBuilder.reflectionEquals( testObject, actual );
    }
  }
}
