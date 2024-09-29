/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


package org.pentaho.di.trans.steps.dummytrans;

import static org.mockito.ArgumentMatchers.any;
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
