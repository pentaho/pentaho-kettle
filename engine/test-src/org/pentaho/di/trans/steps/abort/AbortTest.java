package org.pentaho.di.trans.steps.abort;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
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
    stepMockHelper =
      new StepMockHelper<AbortMeta, StepDataInterface>( "ABORT TEST", AbortMeta.class, StepDataInterface.class );
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
    abort.getInputRowSets().add( stepMockHelper.getMockInputRowSet() );
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
    abort.getInputRowSets().add( stepMockHelper.getMockInputRowSet( new Object[] {} ) );
    assertFalse( abort.isStopped() );
    abort.processRow( stepMockHelper.processRowsStepMetaInterface, stepMockHelper.processRowsStepDataInterface );
    verify( stepMockHelper.trans, times( 1 ) ).stopAll();
    assertTrue( abort.isStopped() );
  }
}
