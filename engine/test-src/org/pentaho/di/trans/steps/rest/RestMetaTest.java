/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2016 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.rest;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.encryption.TwoWayPasswordEncoderPluginType;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.util.EnvUtil;
import org.pentaho.di.trans.steps.loadsave.LoadSaveTester;
import org.pentaho.di.trans.steps.loadsave.validator.ArrayLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.StringLoadSaveValidator;

public class RestMetaTest {

  @BeforeClass
  public static void beforeClass() throws KettleException {
    PluginRegistry.addPluginType( TwoWayPasswordEncoderPluginType.getInstance() );
    PluginRegistry.init();
    String passwordEncoderPluginID =
      Const.NVL( EnvUtil.getSystemProperty( Const.KETTLE_PASSWORD_ENCODER_PLUGIN ), "Kettle" );
    Encr.init( passwordEncoderPluginID );
  }

  @Test
  public void testLoadSaveRoundTrip() throws KettleException {
    List<String> attributes =
      Arrays.asList( "applicationType", "method", "url", "urlInField", "dynamicMethod", "methodFieldName",
        "urlField", "bodyField", "httpLogin", "httpPassword", "proxyHost", "proxyPort", "preemptive",
        "trustStoreFile", "trustStorePassword", "headerField", "headerName", "parameterField", "parameterName",
        "matrixParameterField", "matrixParameterName", "fieldName", "resultCodeFieldName", "responseTimeFieldName",
        "responseHeaderFieldName" );

    Map<String, FieldLoadSaveValidator<?>> fieldLoadSaveValidatorAttributeMap =
      new HashMap<String, FieldLoadSaveValidator<?>>();

    // Arrays need to be consistent length
    FieldLoadSaveValidator<String[]> stringArrayLoadSaveValidator =
      new ArrayLoadSaveValidator<String>( new StringLoadSaveValidator(), 25 );
    fieldLoadSaveValidatorAttributeMap.put( "headerField", stringArrayLoadSaveValidator );
    fieldLoadSaveValidatorAttributeMap.put( "headerName", stringArrayLoadSaveValidator );
    fieldLoadSaveValidatorAttributeMap.put( "parameterField", stringArrayLoadSaveValidator );
    fieldLoadSaveValidatorAttributeMap.put( "parameterName", stringArrayLoadSaveValidator );
    fieldLoadSaveValidatorAttributeMap.put( "matrixParameterField", stringArrayLoadSaveValidator );
    fieldLoadSaveValidatorAttributeMap.put( "matrixParameterName", stringArrayLoadSaveValidator );

    LoadSaveTester loadSaveTester =
      new LoadSaveTester( RestMeta.class, attributes, new HashMap<String, String>(), new HashMap<String, String>(),
        fieldLoadSaveValidatorAttributeMap, new HashMap<String, FieldLoadSaveValidator<?>>() );

    loadSaveTester.testSerialization();
  }
}
