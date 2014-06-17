package org.pentaho.di.trans.step;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.trans.steps.mock.StepMockHelper;

public class BaseStepTest {
  private StepMockHelper<StepMetaInterface, StepDataInterface> mockHelper;

  @Before
  public void setup() {
    mockHelper =
      new StepMockHelper<StepMetaInterface, StepDataInterface>(
        "BASE STEP", StepMetaInterface.class, StepDataInterface.class );
  }

  @After
  public void tearDown() {
    mockHelper.cleanUp();
  }

  @Test
  public void testBaseStepGetLogLevelWontThrowNPEWithNullLog() {
    when( mockHelper.logChannelInterfaceFactory.create( any(), any( LoggingObjectInterface.class ) ) ).thenAnswer(
      new Answer<LogChannelInterface>() {

        @Override
        public LogChannelInterface answer( InvocationOnMock invocation ) throws Throwable {
          ( (BaseStep) invocation.getArguments()[0] ).getLogLevel();
          return mockHelper.logChannelInterface;
        }
      } );
    new BaseStep( mockHelper.stepMeta, mockHelper.stepDataInterface, 0, mockHelper.transMeta, mockHelper.trans )
      .getLogLevel();
  }

  @Test
  public void testStepListenersConcurrentModification() throws InterruptedException {
    //Create a base step
    when( mockHelper.logChannelInterfaceFactory.create( any(), any( LoggingObjectInterface.class ) ) ).thenReturn( mockHelper.logChannelInterface );
    final BaseStep baseStep = new BaseStep( mockHelper.stepMeta, mockHelper.stepDataInterface, 0, mockHelper.transMeta, mockHelper.trans );

    //Create thread to dynamically add listeners
    final AtomicBoolean done = new AtomicBoolean( false );
    Thread addListeners = new Thread() {
      @Override
      public void run() {
        while ( !done.get() ) {
          baseStep.addStepListener( mock( StepListener.class ) );
          synchronized ( done ) {
            done.notify();
          }
        }
      }
    };

    //Mark start and stop while listeners are being added
    try {
      addListeners.start();

      //Allow a few listeners to be added
      synchronized ( done ) {
        while ( baseStep.getStepListeners().size() < 20 ) {
          done.wait();
        }
      }

      baseStep.markStart();

      //Allow more listeners to be added
      synchronized ( done ) {
        while ( baseStep.getStepListeners().size() < 100 ) {
          done.wait();
        }
      }

      baseStep.markStop();

    } finally {
      //Close addListeners thread
      done.set( true );
      addListeners.join();
    }
  }
}
