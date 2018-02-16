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

package org.pentaho.di.trans.steps.dummytrans;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.RowSet;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.mock.StepMockHelper;

public class DummyTransTest {
  private StepMockHelper<StepMetaInterface, StepDataInterface> stepMockHelper;

  @Before
  public void setup() {
    stepMockHelper =
      new StepMockHelper<StepMetaInterface, StepDataInterface>(
        "DUMMY TEST", StepMetaInterface.class, StepDataInterface.class );
    when( stepMockHelper.logChannelInterfaceFactory.create( any(), any( LoggingObjectInterface.class ) ) )
      .thenReturn( stepMockHelper.logChannelInterface );
    when( stepMockHelper.trans.isRunning() ).thenReturn( true );
  }

  @After
  public void tearDown() {
    stepMockHelper.cleanUp();
  }

  @Test
  public void testDummyTransDoesntWriteOutputWithoutInputRow() throws KettleException {
    DummyTrans dummy =
      new DummyTrans(
        stepMockHelper.stepMeta, stepMockHelper.stepDataInterface, 0, stepMockHelper.transMeta,
        stepMockHelper.trans );
    dummy.init( stepMockHelper.initStepMetaInterface, stepMockHelper.initStepDataInterface );
    RowSet rowSet = stepMockHelper.getMockInputRowSet();
    RowMetaInterface inputRowMeta = mock( RowMetaInterface.class );
    when( rowSet.getRowMeta() ).thenReturn( inputRowMeta );
    dummy.addRowSetToInputRowSets( rowSet );
    RowSet outputRowSet = mock( RowSet.class );
    dummy.addRowSetToOutputRowSets( outputRowSet );
    dummy.processRow( stepMockHelper.processRowsStepMetaInterface, stepMockHelper.processRowsStepDataInterface );
    verify( inputRowMeta, never() ).cloneRow( any( Object[].class ) );
    verify( outputRowSet, never() ).putRow( any( RowMetaInterface.class ), any( Object[].class ) );
  }

  @Test
  public void testDummyTransWritesOutputWithInputRow() throws KettleException {
    DummyTrans dummy =
      new DummyTrans(
        stepMockHelper.stepMeta, stepMockHelper.stepDataInterface, 0, stepMockHelper.transMeta,
        stepMockHelper.trans );
    dummy.init( stepMockHelper.initStepMetaInterface, stepMockHelper.initStepDataInterface );
    Object[] row = new Object[] { "abcd" };
    RowSet rowSet = stepMockHelper.getMockInputRowSet( row );
    RowMetaInterface inputRowMeta = mock( RowMetaInterface.class );
    when( inputRowMeta.clone() ).thenReturn( inputRowMeta );
    when( rowSet.getRowMeta() ).thenReturn( inputRowMeta );
    dummy.addRowSetToInputRowSets( rowSet );
    RowSet outputRowSet = mock( RowSet.class );
    dummy.addRowSetToOutputRowSets( outputRowSet );
    when( outputRowSet.putRow( inputRowMeta, row ) ).thenReturn( true );
    dummy.processRow( stepMockHelper.processRowsStepMetaInterface, stepMockHelper.processRowsStepDataInterface );
    verify( outputRowSet, times( 1 ) ).putRow( inputRowMeta, row );
  }
}
