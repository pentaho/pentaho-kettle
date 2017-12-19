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

package org.pentaho.di.trans.steps.missing;

import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.util.AbstractStepMeta;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.StepMockUtil;
import org.pentaho.di.trans.steps.datagrid.DataGridMeta;
import org.pentaho.di.trans.steps.mock.StepMockHelper;

import static org.junit.Assert.assertFalse;

public class MissingTransStepTest {

  @Test
  public void testInit() {
    StepMetaInterface stepMetaInterface = new AbstractStepMeta() {

      @Override
      public void setDefault() { }

      @Override
      public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr,
                                    TransMeta transMeta,
                                    Trans trans ) {
        return null;
      }
    };

    StepMeta stepMeta = new StepMeta();

    stepMeta.setName( "TestMetaStep" );
    StepDataInterface stepDataInterface = mock( StepDataInterface.class );
    Trans trans = new Trans();
    LogChannel log = mock( LogChannel.class );
    doAnswer( new Answer<Void>() {
      public Void answer( InvocationOnMock invocation ) {

        return null;
      }
    } ).when( log ).logError( anyString() );
    trans.setLog( log );
    TransMeta transMeta = new TransMeta();
    transMeta.addStep( stepMeta );

    MissingTransStep step = createAndInitStep( stepMetaInterface, stepDataInterface );

    assertFalse( step.init( stepMetaInterface, stepDataInterface ) );
  }

  private MissingTransStep createAndInitStep( StepMetaInterface meta, StepDataInterface data ) {
    StepMockHelper<DataGridMeta, StepDataInterface> helper =
      StepMockUtil.getStepMockHelper( DataGridMeta.class, "DataGrid_EmptyStringVsNull_Test" );
    when( helper.stepMeta.getStepMetaInterface() ).thenReturn( meta );

    MissingTransStep step = new MissingTransStep( helper.stepMeta, data, 0, helper.transMeta, helper.trans );
    step.init( meta, data );
    return step;
  }

}
