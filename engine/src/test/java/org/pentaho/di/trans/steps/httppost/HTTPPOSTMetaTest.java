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

package org.pentaho.di.trans.steps.httppost;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.trans.steps.loadsave.LoadSaveTester;
import org.pentaho.di.trans.steps.loadsave.validator.ArrayLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.BooleanLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.PrimitiveBooleanArrayLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.StringLoadSaveValidator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class HTTPPOSTMetaTest {
  LoadSaveTester loadSaveTester;
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @Before
  public void testLoadSaveRoundTrip() throws KettleException {
    KettleEnvironment.init();
    PluginRegistry.init( false );
    List<String> attributes =
        Arrays.asList( "postAFile", "encoding", "url", "urlInField", "urlField", "requestEntity", "httpLogin",
            "httpPassword", "proxyHost", "proxyPort", "socketTimeout", "connectionTimeout",
            "closeIdleConnectionsTime", "argumentField", "argumentParameter", "argumentHeader", "queryField",
            "queryParameter", "fieldName", "resultCodeFieldName", "responseTimeFieldName", "responseHeaderFieldName" );

    Map<String, FieldLoadSaveValidator<?>> fieldLoadSaveValidatorAttributeMap =
        new HashMap<String, FieldLoadSaveValidator<?>>();

    //Arrays need to be consistent length
    FieldLoadSaveValidator<String[]> stringArrayLoadSaveValidator =
        new ArrayLoadSaveValidator<String>( new StringLoadSaveValidator(), 25 );
    FieldLoadSaveValidator<boolean[]> booleanArrayLoadSaveValidator =
        new PrimitiveBooleanArrayLoadSaveValidator( new BooleanLoadSaveValidator(), 25 );
    fieldLoadSaveValidatorAttributeMap.put( "argumentField", stringArrayLoadSaveValidator );
    fieldLoadSaveValidatorAttributeMap.put( "argumentParameter", stringArrayLoadSaveValidator );
    fieldLoadSaveValidatorAttributeMap.put( "argumentHeader", booleanArrayLoadSaveValidator );
    fieldLoadSaveValidatorAttributeMap.put( "queryField", stringArrayLoadSaveValidator );
    fieldLoadSaveValidatorAttributeMap.put( "queryParameter", stringArrayLoadSaveValidator );

    loadSaveTester = new LoadSaveTester( HTTPPOSTMeta.class, attributes, new HashMap<String, String>(),
        new HashMap<String, String>(), fieldLoadSaveValidatorAttributeMap, new HashMap<String, FieldLoadSaveValidator<?>>() );
  }

  @Test
  public void testSerialization() throws KettleException {
    loadSaveTester.testSerialization();
  }

  @Test
  public void setDefault() {
    HTTPPOSTMeta meta = new HTTPPOSTMeta();
    assertNull( meta.getEncoding() );

    meta.setDefault();
    assertEquals( "UTF-8", meta.getEncoding() );
  }
}
