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

package org.pentaho.di.pan;

import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.parameters.NamedParams;
import org.pentaho.di.core.parameters.NamedParamsDefault;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;

import static org.junit.Assert.assertEquals;

public class PanTest {

  private static final String TEST_PARAM_NAME = "testParam";
  private static final String DEFAULT_PARAM_VALUE = "default value";
  private static final String NOT_DEFAULT_PARAM_VALUE = "not the default value";

  @BeforeClass
  public static void setUp() throws Exception {
    KettleEnvironment.init();
  }

  @Test
  public void testConfigureParameters() throws Exception {
    TransMeta transMeta = new TransMeta();
    transMeta.addParameterDefinition( TEST_PARAM_NAME, DEFAULT_PARAM_VALUE, "This tests a default parameter" );

    assertEquals( "Default parameter was not set correctly on TransMeta",
      DEFAULT_PARAM_VALUE, transMeta.getParameterDefault( TEST_PARAM_NAME ) );

    assertEquals( "Parameter value should be blank in TransMeta", "", transMeta.getParameterValue( TEST_PARAM_NAME ) );

    Trans trans = new Trans( transMeta );

    assertEquals( "Default parameter was not set correctly on Trans",
      DEFAULT_PARAM_VALUE, trans.getParameterDefault( TEST_PARAM_NAME ) );

    assertEquals( "Parameter value should be blank in Trans", "", trans.getParameterValue( TEST_PARAM_NAME ) );

    NamedParams params = new NamedParamsDefault();
    params.addParameterDefinition( TEST_PARAM_NAME, NOT_DEFAULT_PARAM_VALUE, "This tests a non-default parameter" );
    params.setParameterValue( TEST_PARAM_NAME, NOT_DEFAULT_PARAM_VALUE );
    Pan.configureParameters( trans, params, transMeta );
    assertEquals( "Parameter was not set correctly in Trans",
      NOT_DEFAULT_PARAM_VALUE, trans.getParameterValue( TEST_PARAM_NAME ) );
    assertEquals( "Parameter was not set correctly in TransMeta",
      NOT_DEFAULT_PARAM_VALUE, transMeta.getParameterValue( TEST_PARAM_NAME ) );
  }
}
