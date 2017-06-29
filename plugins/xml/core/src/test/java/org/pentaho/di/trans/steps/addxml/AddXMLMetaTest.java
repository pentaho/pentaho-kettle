/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Pentaho : http://www.pentaho.com
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
