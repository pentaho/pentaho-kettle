/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2024 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.abort;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.steps.mock.StepMockHelper;

public class AbortTest {
  private StepMockHelper<AbortMeta, StepDataInterface> stepMockHelper;

  @Before
  public void setup() {
    stepMockHelper = new StepMockHelper<>( "ABORT TEST", AbortMeta.class, StepDataInterface.class );
    when( stepMockHelper.logChannelInterfaceFactory.create( any(), any( LoggingObjectInterface.class ) ) )
      .thenReturn( stepMockHelper.logChannelInterface );
    when( stepMockHelper.trans.isRunning() ).thenReturn( true );
  }

  @After
  public void tearDown() {
    stepMockHelper.cleanUp();
  }

  @Test
  public void testAbortDoesntAbortWithoutInputRow() throws KettleException {
    Abort abort =
      new Abort(
        stepMockHelper.stepMeta, stepMockHelper.stepDataInterface, 0, stepMockHelper.transMeta,
        stepMockHelper.trans );
    abort.init( stepMockHelper.initStepMetaInterface, stepMockHelper.initStepDataInterface );
    abort.addRowSetToInputRowSets( stepMockHelper.getMockInputRowSet() );
    assertFalse( abort.isStopped() );
    abort.processRow( stepMockHelper.processRowsStepMetaInterface, stepMockHelper.processRowsStepDataInterface );
    verify( stepMockHelper.trans, never() ).stopAll();
    assertFalse( abort.isStopped() );
  }

  @Test
  public void testAbortAbortsWithInputRow() throws KettleException {
    Abort abort =
      new Abort(
        stepMockHelper.stepMeta, stepMockHelper.stepDataInterface, 0, stepMockHelper.transMeta,
        stepMockHelper.trans );
    abort.init( stepMockHelper.initStepMetaInterface, stepMockHelper.initStepDataInterface );
    abort.addRowSetToInputRowSets( stepMockHelper.getMockInputRowSet( new Object[] {} ) );
    assertFalse( abort.isStopped() );
    abort.processRow( stepMockHelper.processRowsStepMetaInterface, stepMockHelper.processRowsStepDataInterface );
    verify( stepMockHelper.trans, times( 1 ) ).stopAll();
    assertTrue( abort.isStopped() );
  }

  @Test
  public void testSafeStop() throws KettleException {
    Abort abort =
      new Abort(
        stepMockHelper.stepMeta, stepMockHelper.stepDataInterface, 0, stepMockHelper.transMeta,
        stepMockHelper.trans );
    when( stepMockHelper.processRowsStepMetaInterface.isSafeStop() ).thenReturn( true );
    abort.init( stepMockHelper.initStepMetaInterface, stepMockHelper.initStepDataInterface );
    abort.addRowSetToInputRowSets( stepMockHelper.getMockInputRowSet( new Object[] {} ) );
    abort.processRow( stepMockHelper.processRowsStepMetaInterface, stepMockHelper.processRowsStepDataInterface );
    verify( stepMockHelper.trans ).safeStop();
  }

  @Test
  public void testAbortWithError() throws KettleException {
    Abort abort =
      new Abort(
        stepMockHelper.stepMeta, stepMockHelper.stepDataInterface, 0, stepMockHelper.transMeta,
        stepMockHelper.trans );
    when( stepMockHelper.processRowsStepMetaInterface.isSafeStop() ).thenReturn( false );
    when( stepMockHelper.processRowsStepMetaInterface.isAbortWithError() ).thenReturn( true );
    abort.init( stepMockHelper.initStepMetaInterface, stepMockHelper.initStepDataInterface );
    abort.addRowSetToInputRowSets( stepMockHelper.getMockInputRowSet( new Object[] {} ) );
    abort.processRow( stepMockHelper.processRowsStepMetaInterface, stepMockHelper.processRowsStepDataInterface );
    assertEquals( 1L, abort.getErrors() );
    verify( stepMockHelper.trans ).stopAll();
  }
}
