/*
 * ! ******************************************************************************
 *  *
 *  * Pentaho Data Integration
 *  *
 *  * Copyright (C) 2002-2013 by Pentaho : http://www.pentaho.com
 *  *
 *  *******************************************************************************
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with
 *  * the License. You may obtain a copy of the License at
 *  *
 *  *    http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *  *
 *  *****************************************************************************
 */

package org.pentaho.di.trans.steps.validator;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.trans.steps.mock.StepMockHelper;

public class ValidatorTest {

  private Validator validator;
  private StepMockHelper<ValidatorMeta, ValidatorData> mockHelper;

  @Before
  public void setUp() throws Exception {
    mockHelper =
      new StepMockHelper<ValidatorMeta, ValidatorData>( "Validator", ValidatorMeta.class, ValidatorData.class );
    when( mockHelper.logChannelInterfaceFactory.create( any(), any( LoggingObjectInterface.class ) ) ).thenReturn(
      mockHelper.logChannelInterface );
    when( mockHelper.trans.isRunning() ).thenReturn( true );
  }

  @Test
  public void testPatternExpectedCompile() throws KettlePluginException {
    validator =
      spy( new Validator(
        mockHelper.stepMeta, mockHelper.stepDataInterface, 0, mockHelper.transMeta, mockHelper.trans ) );

    // ValidatorData data = (ValidatorData) mockHelper.initStepDataInterface;
    // ValidatorMeta meta = (ValidatorMeta) mockHelper.initStepMetaInterface;

    ValidatorData data = new ValidatorData();
    ValidatorMeta meta = new ValidatorMeta();
    data.regularExpression = new String[1];
    data.regularExpressionNotAllowed = new String[1];
    data.patternExpected = new Pattern[1];
    data.patternDisallowed = new Pattern[1];

    List<Validation> validations = new ArrayList<Validation>( 1 );
    Validation v = new Validation();
    v.setFieldName( "field" );
    v.setDataType( 1 );
    v.setRegularExpression( "${param}" );
    v.setRegularExpressionNotAllowed( "${param}" );
    validations.add( v );
    meta.setValidations( validations );

    validator.setVariable( "param", "^(((0[1-9]|[12]\\d|3[01])\\/(0[13578]|1[02])\\/((1[6-9]|[2-9]\\d)\\d{2}))|("
      + "(0[1-9]|[12]\\d|30)\\/(0[13456789]|1[012])\\/((1[6-9]|[2-9]\\d)\\d{2}))|((0[1-9]|1\\d|2[0-8])\\/02\\/("
      + "(1[6-9]|[2-9]\\d)\\d{2}))|(29\\/02\\/((1[6-9]|[2-9]\\d)(0[48]|[2468][048]|[13579][26])|("
      + "(16|[2468][048]|[3579][26])00))))$" );

    doReturn( new ValueMetaString( "field" ) ).when( validator ).createValueMeta( anyString(), anyInt() );
    doReturn( new ValueMetaString( "field" ) ).when( validator ).cloneValueMeta(
      (ValueMetaInterface) anyObject(), anyInt() );

    validator.init( meta, data );

  }

}
