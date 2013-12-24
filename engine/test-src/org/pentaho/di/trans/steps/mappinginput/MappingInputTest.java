/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2013 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.mappinginput;

import org.junit.Test;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.trans.step.StepErrorMeta;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.steps.mock.StepMockHelper;
import org.pentaho.di.trans.steps.validator.Validator;
import org.pentaho.di.trans.steps.validator.ValidatorData;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * User: Dzmitry Stsiapanau Date: 12/24/13 Time: 12:45 PM
 */
public class MappingInputTest {

  @Test
  public void testSetConnectorSteps() throws Exception {
    String stepName = "MAPPING INPUT";
    StepMockHelper<MappingInputMeta, MappingInputData> stepMockHelper =
        new StepMockHelper<MappingInputMeta, MappingInputData>( stepName, MappingInputMeta.class,
            MappingInputData.class );
    when( stepMockHelper.logChannelInterfaceFactory.create( any(), any( LoggingObjectInterface.class ) ) ).thenReturn(
        stepMockHelper.logChannelInterface );
    when( stepMockHelper.trans.isRunning() ).thenReturn( true );
    when( stepMockHelper.transMeta.getSizeRowset() ).thenReturn( 1 );
    MappingInputData mappingInputData = new MappingInputData();
    MappingInput mappingInput =
        new MappingInput( stepMockHelper.stepMeta, mappingInputData,
                0, stepMockHelper.transMeta, stepMockHelper.trans );
    mappingInput.init( stepMockHelper.initStepMetaInterface, mappingInputData );
    ValidatorData validatorData = new ValidatorData();
    Validator previousStep =
        new Validator( stepMockHelper.stepMeta, validatorData, 0, stepMockHelper.transMeta, stepMockHelper.trans );
    when( stepMockHelper.stepMeta.isDoingErrorHandling() ).thenReturn( true );
    StepErrorMeta stepErrorMeta = mock( StepErrorMeta.class );
    when( stepErrorMeta.getTargetStep() ).thenReturn( stepMockHelper.stepMeta );
    when( stepMockHelper.stepMeta.getName() ).thenReturn( stepName );
    when( stepMockHelper.stepMeta.getStepErrorMeta() ).thenReturn( stepErrorMeta );

    StepInterface[] si = new StepInterface[] { previousStep };
    mappingInput.setConnectorSteps( si, null, stepName );
    assertEquals( previousStep.getOutputRowSets().size(), 0 );

  }
}
