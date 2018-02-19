/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.http;

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
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.StringLoadSaveValidator;

public class HTTPMetaLoadSaveTest {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  LoadSaveTester loadSaveTester;

  @Before
  public void testLoadSaveRoundTrip() throws Exception {
    KettleEnvironment.init();
    PluginRegistry.init( false );
    List<String> attributes =
        Arrays.asList( "url", "urlInField", "urlField", "encoding", "httpLogin", "httpPassword", "proxyHost",
            "proxyPort", "socketTimeout", "connectionTimeout", "closeIdleConnectionsTime", "argumentField",
            "argumentParameter", "headerField", "headerParameter", "fieldName", "resultCodeFieldName",
            "responseTimeFieldName", "responseHeaderFieldName" );
    Map<String, FieldLoadSaveValidator<?>> fieldLoadSaveValidatorAttributeMap =
        new HashMap<String, FieldLoadSaveValidator<?>>();

    //Arrays need to be consistent length
    FieldLoadSaveValidator<String[]> stringArrayLoadSaveValidator =
        new ArrayLoadSaveValidator<String>( new StringLoadSaveValidator(), 25 );
    fieldLoadSaveValidatorAttributeMap.put( "argumentField", stringArrayLoadSaveValidator );
    fieldLoadSaveValidatorAttributeMap.put( "argumentParameter", stringArrayLoadSaveValidator );
    fieldLoadSaveValidatorAttributeMap.put( "headerField", stringArrayLoadSaveValidator );
    fieldLoadSaveValidatorAttributeMap.put( "headerParameter", stringArrayLoadSaveValidator );

    loadSaveTester =
        new LoadSaveTester( HTTPMeta.class, attributes, new HashMap<String, String>(),
            new HashMap<String, String>(), fieldLoadSaveValidatorAttributeMap,
            new HashMap<String, FieldLoadSaveValidator<?>>() );

  }
  @Test
  public void testSerialization() throws KettleException {
    loadSaveTester.testSerialization();
  }
}
