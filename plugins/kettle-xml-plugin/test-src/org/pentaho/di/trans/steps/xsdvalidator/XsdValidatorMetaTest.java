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

package org.pentaho.di.trans.steps.xsdvalidator;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.trans.steps.loadsave.LoadSaveTester;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;

public class XsdValidatorMetaTest {

  @Test
  public void testRoundTrip() throws KettleException {
    List<String> attributes =
        Arrays.asList( "xdsfilename", "xmlstream", "resultfieldname", "addvalidationmsg", "validationmsgfield",
            "ifxmlunvalid", "ifxmlvalid", "outputstringfield", "xmlsourcefile", "xsddefinedfield", "xsdsource" );

    Map<String, String> getterMap = new HashMap<String, String>();
    getterMap.put( "xdsfilename", "getXSDFilename" );
    getterMap.put( "xmlstream", "getXMLStream" );
    getterMap.put( "resultfieldname", "getResultfieldname" );
    getterMap.put( "addvalidationmsg", "useAddValidationMessage" );
    getterMap.put( "validationmsgfield", "getValidationMessageField" );
    getterMap.put( "ifxmlunvalid", "getIfXmlInvalid" );
    getterMap.put( "ifxmlvalid", "getIfXmlValid" );
    getterMap.put( "outputstringfield", "getOutputStringField" );
    getterMap.put( "xmlsourcefile", "getXMLSourceFile" );
    getterMap.put( "xsddefinedfield", "getXSDDefinedField" );
    getterMap.put( "xsdsource", "getXSDSource" );

    Map<String, String> setterMap = new HashMap<String, String>();
    setterMap.put( "xdsfilename", "setXSDfilename" );
    setterMap.put( "xmlstream", "setXMLStream" );
    setterMap.put( "resultfieldname", "setResultfieldname" );
    setterMap.put( "addvalidationmsg", "setAddValidationMessage" );
    setterMap.put( "validationmsgfield", "setValidationMessageField" );
    setterMap.put( "ifxmlunvalid", "setIfXmlInvalid" );
    setterMap.put( "ifxmlvalid", "setIfXMLValid" );
    setterMap.put( "outputstringfield", "setOutputStringField" );
    setterMap.put( "xmlsourcefile", "setXMLSourceFile" );
    setterMap.put( "xsddefinedfield", "setXSDDefinedField" );
    setterMap.put( "xsdsource", "setXSDSource" );

    LoadSaveTester loadSaveTester =
        new LoadSaveTester( XsdValidatorMeta.class, attributes, getterMap, setterMap,
            new HashMap<String, FieldLoadSaveValidator<?>>(), new HashMap<String, FieldLoadSaveValidator<?>>() );

    loadSaveTester.testRepoRoundTrip();
    loadSaveTester.testXmlRoundTrip();
  }
}
