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

package org.pentaho.di.trans;

import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs2.FileObject;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.ProgressMonitorListener;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.logging.LogStatus;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.logging.LoggingObjectLifecycleInterface;
import org.pentaho.di.core.logging.StepLogTable;
import org.pentaho.di.core.logging.TransLogTable;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInitThread;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaDataCombi;
import org.pentaho.di.trans.step.StepMetaInterface;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

import static com.google.common.collect.ImmutableList.of;
import static java.util.Collections.emptyList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.pentaho.test.util.InternalState.setInternalState;

public class TransTest {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  private final int count = 10000;
  private Trans trans;
  private TransMeta meta;


  @BeforeClass
  public static void beforeClass() throws Exception {
    //KettleEnvironment.init();
  }

  @Before
  public void beforeTest() throws Exception {
    meta = spy( new TransMeta() );
    trans = spy( new Trans( meta ) );
    trans.setLog( mock( LogChannelInterface.class ) );
  }

  /**
   * PDI-14948 - Execution of trans with no steps never ends
   */
  @Test ( timeout = 1000 )
  public void transWithNoStepsIsNotEndless() throws Exception {
    trans.prepareExecution( new String[] {} );
    trans.startThreads();

    while ( trans.isRunning() ) {
      Thread.sleep( 1 );
    }

    // check trans lifecycle is not corrupted
    verify( trans ).fireTransStartedListeners();
    verify( trans ).fireTransFinishedListeners();
  }

  @Test
  public void testFindDatabaseWithEncodedConnectionName() {
    DatabaseMeta dbMeta1 =
      new DatabaseMeta( "encoded_DBConnection", "Oracle", "localhost", "access", "test", "111", "test", "test" );
    dbMeta1.setDisplayName( "encoded.DBConnection" );
    meta.addDatabase( dbMeta1 );

    DatabaseMeta dbMeta2 =
      new DatabaseMeta( "normalDBConnection", "Oracle", "localhost", "access", "test", "111", "test", "test" );
    dbMeta2.setDisplayName( "normalDBConnection" );
    meta.addDatabase( dbMeta2 );

    DatabaseMeta databaseMeta = meta.findDatabase( dbMeta1.getDisplayName() );
    assertNotNull( databaseMeta );
    assertEquals( "encoded_DBConnection", databaseMeta.getName() );
    assertEquals( "encoded.DBConnection", databaseMeta.getDisplayName() );
  }

  /**
   * PDI-10762 - Trans and TransMeta leak
   */
  @Test
  public void testLoggingObjectIsNotLeakInMeta() {
    String expected = meta.log.getLogChannelId();
    meta.clear();

    assertEquals( "Use same logChannel for empty constructors, or assign General level for clear() calls",
      expected, meta.log.getLogChannelId() );
  }

  /**
   * PDI-10762 - Trans and TransMeta leak
   */
  // TODO: fix this!
  // This cannot be tested without some changes to our logging code and/or our RestorePDIEngineEnvironment util.
  // LogChannel creates the General channel once upon class initialization, and if the LoggingRegistry gets
  // reset, which it does often in our unit tests, that registration is lost for good and this test never works.
  // I do not know how it worked before.
  @Ignore
  @Test
  public void testLoggingObjectIsNotLeakInTrans() throws Exception {
    Repository rep = mock( Repository.class );
    RepositoryDirectoryInterface repInt = Mockito.mock( RepositoryDirectoryInterface.class );
    when(
      rep.loadTransformation( anyString(), any( RepositoryDirectoryInterface.class ),
        nullable( ProgressMonitorListener.class ), anyBoolean(), nullable( String.class ) ) ).thenReturn( meta );
    when( rep.findDirectory( anyString() ) ).thenReturn( repInt );

    Trans trans = new Trans( meta, rep, "junit", "junitDir", "fileName" );
    assertEquals( "Log channel General assigned", LogChannel.GENERAL.getLogChannelId(), trans.log
      .getLogChannelId() );
  }

  /**
   * PDI-5229 - ConcurrentModificationException when restarting transformation Test that listeners can be accessed
   * concurrently during transformation finish
   */
  @Test
  public void testTransFinishListenersConcurrentModification() throws Exception {
    CountDownLatch start = new CountDownLatch( 1 );
    TransFinishListenerAdder add = new TransFinishListenerAdder( trans, start );
    TransFinishListenerFirer firer = new TransFinishListenerFirer( trans, start );
    startThreads( add, firer, start );

    assertEquals( "All listeners are added: no ConcurrentModificationException", count, add.c );
    assertEquals( "All Finish listeners are iterated over: no ConcurrentModificationException", count, firer.c );
  }

  /**
   * Test that listeners can be accessed concurrently during transformation start
   */
  @Test
  public void testTransStartListenersConcurrentModification() throws Exception {
    CountDownLatch start = new CountDownLatch( 1 );
    TransFinishListenerAdder add = new TransFinishListenerAdder( trans, start );
    TransStartListenerFirer starter = new TransStartListenerFirer( trans, start );
    startThreads( add, starter, start );

    assertEquals( "All listeners are added: no ConcurrentModificationException", count, add.c );
    assertEquals( "All Start listeners are iterated over: no ConcurrentModificationException", count, starter.c );
  }

  /**
   * Test that transformation stop listeners can be accessed concurrently
   */
  @Test
  public void testTransStoppedListenersConcurrentModification() throws Exception {
    CountDownLatch start = new CountDownLatch( 1 );
    TransStoppedCaller stopper = new TransStoppedCaller( trans, start );
    TransStopListenerAdder adder = new TransStopListenerAdder( trans, start );
    startThreads( stopper, adder, start );

    assertEquals( "All transformation stop listeners is added", count, adder.c );
    assertEquals( "All stop call success", count, stopper.c );
  }

  @Test
  public void testPDI12424ParametersFromMetaAreCopiedToTrans() throws Exception {
    String testParam = "testParam";
    String testParamValue = "testParamValue";
    when( meta.listVariables() ).thenReturn( new String[] {} );
    when( meta.listParameters() ).thenReturn( new String[] { testParam } );
    when( meta.getParameterValue( testParam ) ).thenReturn( testParamValue );
    FileObject ktr = KettleVFS.createTempFile( "parameters", ".ktr", KettleVFS.TEMP_DIR );
    try ( OutputStream outputStream = ktr.getContent().getOutputStream() ) {
      InputStream inputStream = new ByteArrayInputStream( "<transformation></transformation>".getBytes() );
      IOUtils.copy( inputStream, outputStream );
    }

    Trans trans = new Trans( meta, null, null, null, ktr.getURL().toURI().toString() );

    assertEquals( testParamValue, trans.getParameterValue( testParam ) );
  }

  @Test
  public void testRecordsCleanUpMethodIsCalled() throws Exception {
    Database mockedDataBase = mock( Database.class );
    StepLogTable stepLogTable =
      StepLogTable.getDefault( mock( VariableSpace.class ), mock( HasDatabasesInterface.class ) );
    stepLogTable.setConnectionName( "connection" );

    meta.setStepLogTable( stepLogTable );
    doReturn( mockedDataBase ).when( trans ).createDataBase( nullable( DatabaseMeta.class ) );
    trans.setSteps( new ArrayList<>() );

    trans.writeStepLogInformation();

    verify( mockedDataBase ).cleanupLogRecords( eq(stepLogTable), nullable( String.class ) );
  }

  @Test
  public void testFireTransFinishedListeners() throws Exception {
    TransListener mockListener = mock( TransListener.class );
    trans.setTransListeners( Collections.singletonList( mockListener ) );

    trans.fireTransFinishedListeners();

    verify( mockListener ).transFinished( trans );
  }

  @Test ( expected = KettleException.class )
  public void testFireTransFinishedListenersExceptionOnTransFinished() throws Exception {
    TransListener mockListener = mock( TransListener.class );
    doThrow( KettleException.class ).when( mockListener ).transFinished( trans );
    trans.setTransListeners( Collections.singletonList( mockListener ) );

    trans.fireTransFinishedListeners();
  }

  @Test ( timeout = 1000 )
  public void testFinishStatus() throws Exception {
    trans.prepareExecution( null );
    trans.startThreads();

    while ( trans.isRunning() ) {
      Thread.sleep( 1 );
    }

    assertEquals( Trans.STRING_FINISHED, trans.getStatus() );
  }

  @Test
  public void testSafeStop_WithoutSteps_null() {
    trans.setSteps( null );
    assertNull( trans.getSteps() );

    trans.safeStop();

    verify( trans, times( 0 ) ).notifyStoppedListeners();
  }

  @Test
  public void testSafeStop() {
    StepInterface stepMock1 = mock( StepInterface.class );
    StepDataInterface stepDataMock1 = mock( StepDataInterface.class );
    StepMeta stepMetaMock1 = mock( StepMeta.class );

    when( stepMock1.getStepname() ).thenReturn( "stepName" );
    trans.setSteps( of( combi( stepMock1, stepDataMock1, stepMetaMock1 ) ) );

    // Scenario: step not stopped
    when( stepMock1.isSafeStopped() ).thenReturn( false );
    Result result = trans.getResult();
    assertFalse( result.isSafeStop() );

    // Scenario: step stopped
    when( stepMock1.isSafeStopped() ).thenReturn( true );
    result = trans.getResult();
    assertTrue( result.isSafeStop() );
  }

  @Test
  public void safeStopStopsInputStepsRightAway() throws Exception {
    StepInterface stepMock1 = mock( StepInterface.class );
    StepDataInterface stepDataMock1 = mock( StepDataInterface.class );
    StepMeta stepMetaMock1 = mock( StepMeta.class );

    trans.setSteps( of( combi( stepMock1, stepDataMock1, stepMetaMock1 ) ) );

    trans.safeStop();

    verifyStopped( stepMock1, 1 );
    verify( trans ).notifyStoppedListeners();
  }

  @Test
  public void safeStopLetsNonInputStepsKeepRunning() throws Exception {
    StepInterface stepMock1 = mock( StepInterface.class );
    StepInterface stepMock2 = mock( StepInterface.class );
    StepDataInterface stepDataMock1 = mock( StepDataInterface.class );
    StepDataInterface stepDataMock2 = mock( StepDataInterface.class );
    StepMeta stepMetaMock1 = mock( StepMeta.class );
    StepMeta stepMetaMock2 = mock( StepMeta.class );

    trans.setSteps( of(
      combi( stepMock1, stepDataMock1, stepMetaMock1 ),
      combi( stepMock2, stepDataMock2, stepMetaMock2 ) ) );

    doReturn( emptyList() ).when( meta ).findPreviousSteps( stepMetaMock1, true );
    // stepMeta2 will have stepMeta as previous, so is not an input step
    doReturn( of( stepMetaMock1 ) ).when( meta ).findPreviousSteps( stepMetaMock2, true );

    trans.safeStop();

    verifyStopped( stepMock1, 1 );
    // non input step shouldn't have stop called
    verifyStopped( stepMock2, 0 );

    verify( trans ).notifyStoppedListeners();
  }

  private void verifyStopped( StepInterface step, int numberTimesCalled ) throws Exception {
    verify( step, times( numberTimesCalled ) ).setStopped( true );
    verify( step, times( numberTimesCalled ) ).setSafeStopped( true );
    verify( step, times( numberTimesCalled ) ).resumeRunning();
    verify( step, times( numberTimesCalled ) ).stopRunning( any(), any() );
  }

  private StepMetaDataCombi combi( StepInterface step, StepDataInterface data, StepMeta stepMeta ) {
    StepMetaDataCombi stepMetaDataCombi = new StepMetaDataCombi();
    stepMetaDataCombi.step = step;
    stepMetaDataCombi.data = data;
    stepMetaDataCombi.stepMeta = stepMeta;
    return stepMetaDataCombi;
  }

  private void startThreads( Runnable one, Runnable two, CountDownLatch start ) throws InterruptedException {
    Thread th = new Thread( one );
    Thread tt = new Thread( two );
    th.start();
    tt.start();
    start.countDown();
    th.join();
    tt.join();
  }

  private abstract class TransKicker implements Runnable {
    protected Trans tr;
    protected int c = 0;
    protected CountDownLatch start;
    protected int max = count;

    TransKicker( Trans tr, CountDownLatch start ) {
      this.tr = tr;
      this.start = start;
    }

    protected boolean isStopped() {
      c++;
      return c >= max;
    }
  }

  private class TransStoppedCaller extends TransKicker {
    TransStoppedCaller( Trans tr, CountDownLatch start ) {
      super( tr, start );
    }

    @Override
    public void run() {
      try {
        start.await();
      } catch ( InterruptedException e ) {
        throw new RuntimeException();
      }
      while ( !isStopped() ) {
        trans.stopAll();
      }
    }
  }

  private class TransStopListenerAdder extends TransKicker {
    TransStopListenerAdder( Trans tr, CountDownLatch start ) {
      super( tr, start );
    }

    @Override
    public void run() {
      try {
        start.await();
      } catch ( InterruptedException e ) {
        throw new RuntimeException();
      }
      while ( !isStopped() ) {
        trans.addTransStoppedListener( transStoppedListener );
      }
    }
  }

  private class TransFinishListenerAdder extends TransKicker {
    TransFinishListenerAdder( Trans tr, CountDownLatch start ) {
      super( tr, start );
    }

    @Override
    public void run() {
      try {
        start.await();
      } catch ( InterruptedException e ) {
        throw new RuntimeException();
      }
      // run
      while ( !isStopped() ) {
        tr.addTransListener( listener );
      }
    }
  }

  private class TransFinishListenerFirer extends TransKicker {
    TransFinishListenerFirer( Trans tr, CountDownLatch start ) {
      super( tr, start );
    }

    @Override
    public void run() {
      try {
        start.await();
      } catch ( InterruptedException e ) {
        throw new RuntimeException();
      }
      // run
      while ( !isStopped() ) {
        try {
          tr.fireTransFinishedListeners();
          // clean array blocking queue
          tr.waitUntilFinished();
        } catch ( KettleException e ) {
          throw new RuntimeException();
        }
      }
    }
  }

  private class TransStartListenerFirer extends TransKicker {
    TransStartListenerFirer( Trans tr, CountDownLatch start ) {
      super( tr, start );
    }

    @Override
    public void run() {
      try {
        start.await();
      } catch ( InterruptedException e ) {
        throw new RuntimeException();
      }
      // run
      while ( !isStopped() ) {
        try {
          tr.fireTransStartedListeners();
        } catch ( KettleException e ) {
          throw new RuntimeException();
        }
      }
    }
  }

  private final TransListener listener = new TransListener() {
    @Override
    public void transStarted( Trans trans ) throws KettleException {
    }

    @Override
    public void transActive( Trans trans ) {
    }

    @Override
    public void transFinished( Trans trans ) throws KettleException {
    }
  };

  private final TransStoppedListener transStoppedListener = new TransStoppedListener() {
    @Override
    public void transStopped( Trans trans ) {
    }
  };

  @Test
  public void testNewTransformationsWithContainerObjectId() throws Exception {
    String carteId = UUID.randomUUID().toString();
    meta.setCarteObjectId( carteId );

    Trans trans = new Trans( meta );

    assertEquals( carteId, trans.getContainerObjectId() );
  }

  /**
   * This test demonstrates the issue fixed in PDI-17436.
   * When a job is scheduled twice, it gets the same log channel Id and both logs get merged
   */
  @Test
  public void testTwoTransformationsGetSameLogChannelId() throws Exception {
    Trans trans1 = new Trans( meta );
    Trans trans2 = new Trans( meta );

    assertEquals( trans1.getLogChannelId(), trans2.getLogChannelId() );
  }

  /**
   * This test demonstrates the fix for PDI-17436.
   * Two schedules -> two Carte object Ids -> two log channel Ids
   */
  @Test
  public void testTwoTransformationsGetDifferentLogChannelIdWithDifferentCarteId() throws Exception {
    TransMeta meta1 = new TransMeta();
    TransMeta meta2 = new TransMeta();

    String carteId1 = UUID.randomUUID().toString();
    String carteId2 = UUID.randomUUID().toString();

    meta1.setCarteObjectId( carteId1 );
    meta2.setCarteObjectId( carteId2 );

    Trans trans1 = new Trans( meta1 );
    Trans trans2 = new Trans( meta2 );

    assertNotEquals( trans1.getContainerObjectId(), trans2.getContainerObjectId() );
    assertNotEquals( trans1.getLogChannelId(), trans2.getLogChannelId() );
  }

  @Test
  public void testSetInternalEntryCurrentDirectoryWithFilename( ) {
    setInternalEntryCurrentDirectory();

    trans.setInternalEntryCurrentDirectory( true, false );

    assertEquals( "file:///C:/SomeFilenameDirectory", trans.getVariable( Const.INTERNAL_VARIABLE_ENTRY_CURRENT_DIRECTORY ) );
  }

  @Test
  public void testSetInternalEntryCurrentDirectoryWithRepository( ) {
    setInternalEntryCurrentDirectory();

    trans.setInternalEntryCurrentDirectory( false, true );

    assertEquals( "/SomeRepDirectory", trans.getVariable( Const.INTERNAL_VARIABLE_ENTRY_CURRENT_DIRECTORY ) );
  }

  @Test
  public void testSetInternalEntryCurrentDirectoryWithoutFilenameOrRepository( ) {
    setInternalEntryCurrentDirectory();

    trans.setInternalEntryCurrentDirectory( false, false );

    assertEquals( "Original value defined at run execution", trans.getVariable( Const.INTERNAL_VARIABLE_ENTRY_CURRENT_DIRECTORY )  );
  }

  /**
   * Common base for testSetInternalEntryCurrentDirectory's' tests.
   */
  private void setInternalEntryCurrentDirectory() {
    trans.copyVariablesFrom( null );
    trans.setVariable( Const.INTERNAL_VARIABLE_ENTRY_CURRENT_DIRECTORY, "Original value defined at run execution" );
    trans.setVariable( Const.INTERNAL_VARIABLE_TRANSFORMATION_FILENAME_DIRECTORY, "file:///C:/SomeFilenameDirectory" );
    trans.setVariable( Const.INTERNAL_VARIABLE_TRANSFORMATION_REPOSITORY_DIRECTORY, "/SomeRepDirectory" );
  }

  @Test
  public void testCleanup_WithoutSteps_null() throws Exception {
    trans.setSteps( null );
    assertNull( trans.getSteps() );

    // this should work (no exception thrown)
    trans.cleanup();
  }

  @Test
  public void testCleanup_WithoutSteps_emptyList() throws Exception {
    trans.setSteps( new ArrayList<>() );
    List<StepMetaDataCombi> steps = trans.getSteps();
    assertNotNull( steps );
    assertTrue( steps.isEmpty() );

    // this should also work (no exception thrown)
    trans.cleanup();
  }

  @Test
  public void testCleanup_WithSteps() throws Exception {
    StepInterface stepMock1 = mock( StepInterface.class );
    StepInterface stepMock2 = mock( StepInterface.class );
    StepDataInterface stepDataMock1 = mock( StepDataInterface.class );
    StepDataInterface stepDataMock2 = mock( StepDataInterface.class );
    StepMeta stepMetaMock1 = mock( StepMeta.class );
    StepMeta stepMetaMock2 = mock( StepMeta.class );

    // A step that is already disposed
    when( stepDataMock1.isDisposed() ).thenReturn( true );

    // A step not yet disposed
    when( stepDataMock2.isDisposed() ).thenReturn( false );

    trans.setSteps( of(
      combi( stepMock1, stepDataMock1, stepMetaMock1 ),
      combi( stepMock2, stepDataMock2, stepMetaMock2 ) ) );

    // this should work (no exception thrown)
    trans.cleanup();

    // The isDisposed method is always invoked  
    verify( stepDataMock1 ).isDisposed();
    verify( stepDataMock2 ).isDisposed();
    // Only 'stepDataMock2' is to be disposed
    verify( stepMock1, times( 0 ) ).dispose( any( StepMetaInterface.class ), any( StepDataInterface.class ) );
   // The cleanup method is always invoked
    verify( stepMock1 ).cleanup();
    verify( stepMock2 ).cleanup();
  }


  /**
   * <p>PDI-18458: A Stopped transformation would be logged as 'Running'.</p>
   *
   * @see #testEndProcessing_StatusCalculation_Base()
   * @see #testEndProcessing_StatusCalculation_Finished()
   * @see #testEndProcessing_StatusCalculation_Paused()
   * @see #testEndProcessing_StatusCalculation_Running()
   */
  @Test
  public void testEndProcessing_StatusCalculation_Stopped() throws Exception {
    try ( MockedConstruction<Database> databaseMockedConstruction =
            mockConstruction( Database.class, ( m,c ) -> doNothing().when( m ).connect() ) ) {
      testEndProcessing_StatusCalculation_Base();
      trans.calculateBatchIdAndDateRange();
      trans.beginProcessing();

      // Set 'Stopped'
      trans.setStopped( true );

      int allCount = 0;

      for ( boolean finished : new boolean[] { false, true } ) {
        for ( boolean initializing : new boolean[] { false, true } ) {
          for ( boolean paused : new boolean[] { false, true } ) {
            for ( boolean preparing : new boolean[] { false, true } ) {
              for ( boolean running : new boolean[] { false, true } ) {
                trans.setFinished( finished );
                trans.setInitializing( initializing );
                trans.setPaused( paused );
                trans.setPreparing( preparing );
                trans.setRunning( running );

                trans.fireTransFinishedListeners();

                ++allCount;
              }
            }
          }
        }
      }

      // All cases should result in status being 'Stopped'.
      verify( databaseMockedConstruction.constructed().get( 0 ), times( 1 ) ).writeLogRecord(
        meta.getTransLogTable(), LogStatus.START, trans, null );
      for ( int i = 1; i < 15; i++ ) {
        verify( databaseMockedConstruction.constructed().get( i ), times( 1 ) ).writeLogRecord( meta.getTransLogTable(),
          LogStatus.STOP, trans, null );
      }
    }
  }

  /**
   * <p>PDI-18458: A Stopped transformation would be logged as 'Running'.</p>
   *
   * @see #testEndProcessing_StatusCalculation_Base()
   * @see #testEndProcessing_StatusCalculation_Paused()
   * @see #testEndProcessing_StatusCalculation_Running()
   * @see #testEndProcessing_StatusCalculation_Stopped()
   */
  @Test
  public void testEndProcessing_StatusCalculation_Finished() throws Exception {
    doCallRealMethod().when( trans ).setFinished( anyBoolean() );
    try ( MockedConstruction<Database> databaseMockedConstruction =
            mockConstruction( Database.class, ( m,c ) -> doNothing().when( m ).connect() ) ) {
      testEndProcessing_StatusCalculation_Base();
      trans.calculateBatchIdAndDateRange();
      trans.beginProcessing();

      verify( databaseMockedConstruction.constructed().get( 0 ), times( 1 ) ).writeLogRecord(
        meta.getTransLogTable(), LogStatus.START, trans, null );
      // Set 'Finished'
      trans.setFinished( true );

      int stopCount = 0;
      int allCount = 0;

      for ( boolean initializing : new boolean[] { false, true } ) {
        for ( boolean paused : new boolean[] { false, true } ) {
          for ( boolean preparing : new boolean[] { false, true } ) {
            for ( boolean running : new boolean[] { false, true } ) {
              for ( boolean stopped : new boolean[] { false, true } ) {
                trans.setInitializing( initializing );
                trans.setPaused( paused );
                trans.setPreparing( preparing );
                trans.setRunning( running );
                trans.setStopped( stopped );

                trans.fireTransFinishedListeners();

                ++allCount;
                if ( stopped ) {
                  verify( databaseMockedConstruction.constructed().get( allCount ), times( 1 ) ).writeLogRecord( meta.getTransLogTable(),
                    LogStatus.STOP, trans, null );
                  ++stopCount;
                } else {
                  verify( databaseMockedConstruction.constructed().get( allCount ), times( 1 ) ).writeLogRecord( meta.getTransLogTable(),
                    LogStatus.END, trans, null );
                }
              }
            }
          }
        }
      }
    }
  }

  /**
   * <p>PDI-18458: A Stopped transformation would be logged as 'Running'.</p>
   *
   * @see #testEndProcessing_StatusCalculation_Base()
   * @see #testEndProcessing_StatusCalculation_Finished()
   * @see #testEndProcessing_StatusCalculation_Running()
   * @see #testEndProcessing_StatusCalculation_Stopped()
   */
  @Test
  public void testEndProcessing_StatusCalculation_Paused() throws Exception {
    try ( MockedConstruction<Database> databaseMockedConstruction =
            mockConstruction( Database.class, ( m,c ) -> doNothing().when( m ).connect() ) ) {
      testEndProcessing_StatusCalculation_Base();
      trans.calculateBatchIdAndDateRange();
      trans.beginProcessing();

      // Set 'Paused'
      trans.setPaused( true );

      // It can't be 'Finished' nor 'Stopped'
      trans.setFinished( false );
      trans.setStopped( false );

      int allCount = 0;

      for ( boolean initializing : new boolean[] { false, true } ) {
        for ( boolean preparing : new boolean[] { false, true } ) {
          for ( boolean running : new boolean[] { false, true } ) {
            trans.setInitializing( initializing );
            trans.setPreparing( preparing );
            trans.setRunning( running );

            trans.fireTransFinishedListeners();

            ++allCount;
          }
        }
      }

      // All cases should result in status being 'End'.
      verify( databaseMockedConstruction.constructed().get( 0 ), times( 1 ) ).writeLogRecord(
        meta.getTransLogTable(), LogStatus.START, trans, null );
      for ( int i = 1; i < 9; i++ ) {
        verify( databaseMockedConstruction.constructed().get( i ), times( 1 ) ).writeLogRecord( meta.getTransLogTable(),
          LogStatus.PAUSED, trans, null );
      }
    }
  }

  /**
   * <p>PDI-18458: A Stopped transformation would be logged as 'Running'.</p>
   *
   * @see #testEndProcessing_StatusCalculation_Base()
   * @see #testEndProcessing_StatusCalculation_Finished()
   * @see #testEndProcessing_StatusCalculation_Paused()
   * @see #testEndProcessing_StatusCalculation_Stopped()
   */
  @Test
  public void testEndProcessing_StatusCalculation_Running() throws Exception {
    try ( MockedConstruction<Database> databaseMockedConstruction =
            mockConstruction( Database.class, ( m,c ) -> doNothing().when( m ).connect() ) ) {
      testEndProcessing_StatusCalculation_Base();
      trans.calculateBatchIdAndDateRange();
      trans.beginProcessing();

      // It can't be 'Finished', 'Paused' nor 'Stopped'
      trans.setFinished( false );
      trans.setPaused( false );
      trans.setStopped( false );

      int allCount = 0;

      for ( boolean initializing : new boolean[] { false, true } ) {
        for ( boolean preparing : new boolean[] { false, true } ) {
          for ( boolean running : new boolean[] { false, true } ) {
            trans.setInitializing( initializing );
            trans.setPreparing( preparing );
            trans.setRunning( running );

            trans.fireTransFinishedListeners();

            ++allCount;
          }
        }
      }

      // All cases should result in status being 'Running' except the first
      verify( databaseMockedConstruction.constructed().get( 0 ), times( 1 ) ).writeLogRecord( meta.getTransLogTable(),
        LogStatus.START, trans, null );
      for ( int i = 1; i < 9; i++ ) {
        verify( databaseMockedConstruction.constructed().get( i ), times( 1 ) ).writeLogRecord( meta.getTransLogTable(),
          LogStatus.RUNNING, trans, null );
      }
    }
  }

  /**
   * <p>PDI-18458: A Stopped transformation would be logged as 'Running'.</p>
   * <p>Base for the testEndProcessing_StatusCalculation's tests.</p>
   *
   * @return the mocked {@link Database} object used on the test
   *
   * @see #testEndProcessing_StatusCalculation_Finished()
   * @see #testEndProcessing_StatusCalculation_Paused()
   * @see #testEndProcessing_StatusCalculation_Running()
   * @see #testEndProcessing_StatusCalculation_Stopped()
   */
  private void testEndProcessing_StatusCalculation_Base() throws Exception {
    TransLogTable transLogTable = spy( new TransLogTable(null, null, null) );
    doReturn("AnActualTableNametransLogTable").when(transLogTable).getActualTableName();
    doReturn("AnActualConnectionName").when(transLogTable).getActualConnectionName();
    doReturn("AnActualSchemaName").when(transLogTable).getActualSchemaName();
    doReturn( true ).when( transLogTable ).isDefined();
    //doReturn( "1" ).when( transLogTable ).getLogInterval();
    DatabaseMeta databaseMeta = mock( DatabaseMeta.class );
    doReturn( databaseMeta).when( transLogTable ).getDatabaseMeta();
    doReturn( transLogTable).when( meta ).getTransLogTable();
    doReturn( false ).when( transLogTable ).isBatchIdUsed();
    doReturn( "MetaName" ).when( meta ).getName();
  }

  /**
   * <p>PDI-18459 - When any step fails on initialization, the transformation should set status to Stopped.</p>
   */
  @Test ( expected = KettleException.class )
  public void testFailOnInitialization() throws Exception {
    StepInterface stepMock1 = mock( StepInterface.class );
    StepInterface stepMock2 = mock( StepInterface.class );
    StepDataInterface stepDataMock1 = mock( StepDataInterface.class );
    StepDataInterface stepDataMock2 = mock( StepDataInterface.class );
    StepMeta stepMetaMock1 = mock( StepMeta.class );
    StepMeta stepMetaMock2 = mock( StepMeta.class );
    StepMetaInterface stepMetaInterfaceMock1 = mock( StepMetaInterface.class );
    StepMetaInterface stepMetaInterfaceMock2 = mock( StepMetaInterface.class );

    doReturn( stepDataMock1 ).when( stepMetaInterfaceMock1 ).getStepData();
    doReturn( stepDataMock2 ).when( stepMetaInterfaceMock2 ).getStepData();
    doReturn( stepMock1 ).when( stepMetaInterfaceMock1 ).getStep( any( StepMeta.class ), any( StepDataInterface.class ), anyInt(), any( TransMeta.class ), any( Trans.class ) );
    doReturn( stepMock2 ).when( stepMetaInterfaceMock2 ).getStep( any( StepMeta.class ), any( StepDataInterface.class ), anyInt(), any( TransMeta.class ), any( Trans.class ) );
    doReturn( stepMetaMock1 ).when( stepMock1 ).getStepMeta();
    doReturn( stepMetaMock2 ).when( stepMock2 ).getStepMeta();
    doReturn( stepMetaInterfaceMock1 ).when( stepMetaMock1 ).getStepMetaInterface();
    doReturn( stepMetaInterfaceMock2 ).when( stepMetaMock2 ).getStepMetaInterface();
    StepInitThread stepInitThreadMock = mock( StepInitThread.class );
    doNothing().when( stepInitThreadMock ).run();
    // Mocking the initialization results: the first step will initialize correctly, the second will fail.
    // There're four entries because
    when( stepInitThreadMock.isOk() ).thenReturn( true, false, true, false );
    StepMetaDataCombi dummyCombi = new StepMetaDataCombi();
    dummyCombi.stepname = "dummy";
    dummyCombi.copy = 1;
    dummyCombi.data = mock( StepDataInterface.class );
    dummyCombi.step = mock( StepInterface.class );
    doReturn( dummyCombi ).when( stepInitThreadMock ).getCombi();

    // A step that failed initialization
    doReturn( false ).when( stepMock1 ).init( any(), any() );

    // A step that passed initialization
    doReturn( false ).when( stepMock1 ).init( any(), any() );

    trans.setSteps( of(
            combi( stepMock1, stepDataMock1, stepMetaMock1 ),
            combi( stepMock2, stepDataMock2, stepMetaMock2 ) ) );

    List<StepMeta> hopsteps = of( stepMetaMock1, stepMetaMock2 );
    doReturn( true ).when( stepMetaMock1 ).isMapping();
    doReturn( 1 ).when( stepMetaMock1 ).getCopies();
    doReturn( 1 ).when( stepMetaMock2 ).getCopies();
    doReturn( true ).when( stepMetaMock2 ).isMapping();
    doReturn( hopsteps ).when( meta ).getTransHopSteps( anyBoolean() );

    try {
      trans.prepareExecution(new String[]{});
    } catch ( KettleException ke ) {
      verify( trans, times( 1 ) ).setPreparing( true );
      verify( trans, times( 1 ) ).setPreparing( false );
      verify( trans, times( 1 ) ).setInitializing( true );
      verify( trans, times( 1 ) ).setInitializing( false );

      throw ke;
    }

    fail( "Should not have reached here: an exception should have been thrown!");
  }

  @Test
  public void testShutdownHeartbeat_null() throws Exception {
    doCallRealMethod().when( trans ).shutdownHeartbeat( any( ExecutorService.class ) );

    trans.shutdownHeartbeat( null );
  }

  @Test
  public void testShutdownHeartbeat_exception() throws Exception {
    doCallRealMethod().when( trans ).shutdownHeartbeat( any( ExecutorService.class ) );
    ExecutorService executorService = mock( ExecutorService.class );
    doThrow( new SecurityException() ).when( executorService ).shutdownNow();

    trans.shutdownHeartbeat( executorService );

    verify( executorService, times ( 1 ) ).shutdownNow();
  }

  @Test
  public void testShutdownHeartbeat_success() throws Exception {
    doCallRealMethod().when( trans ).shutdownHeartbeat( any( ExecutorService.class ) );
    ExecutorService executorService = mock( ExecutorService.class );
    doReturn( null ).when( executorService ).shutdownNow();

    trans.shutdownHeartbeat( executorService );

    verify( executorService, times ( 1 ) ).shutdownNow();
  }

  @Test
  public void testSetAndGetLog() {
    LogChannelInterface logMock = mock( LogChannelInterface.class );
    trans.setLog( logMock );
    assertEquals( logMock, trans.getLogChannel() );
  }

  @Test
  public void testGetName() {
    String str = "dummyName";

    doReturn( str ).when( meta ).getName();

    assertEquals( str, trans.getName() );
  }

  @Test
  public void testGetErrors_nullSteps() {
    trans.setSteps( null );

    assertEquals( 0, trans.getErrors() );
  }

  @Test
  public void testGetErrors_withSteps() {
    StepInterface stepMock1 = mock( StepInterface.class );
    StepInterface stepMock2 = mock( StepInterface.class );
    StepInterface stepMock3 = mock( StepInterface.class );
    StepInterface stepMock4 = mock( StepInterface.class );
    StepDataInterface stepDataMock1 = mock( StepDataInterface.class );
    StepDataInterface stepDataMock2 = mock( StepDataInterface.class );
    StepDataInterface stepDataMock3 = mock( StepDataInterface.class );
    StepDataInterface stepDataMock4 = mock( StepDataInterface.class );
    StepMeta stepMetaMock1 = mock( StepMeta.class );
    StepMeta stepMetaMock2 = mock( StepMeta.class );
    StepMeta stepMetaMock3 = mock( StepMeta.class );
    StepMeta stepMetaMock4 = mock( StepMeta.class );

    // Step with no errors
    doReturn( 0L ).when( stepMock1 ).getErrors();
    // Step with three (3) errors
    doReturn( 3L ).when( stepMock2 ).getErrors();
    // Another step with no errors
    doReturn( 0L ).when( stepMock3 ).getErrors();
    // Step with two (2) errors
    doReturn( 2L ).when( stepMock4 ).getErrors();

    // A total of 5 (0 + 3 + 0 + 2) errors

    trans.setSteps( of(
            combi( stepMock1, stepDataMock1, stepMetaMock1 ),
            combi( stepMock2, stepDataMock2, stepMetaMock2 ),
            combi( stepMock3, stepDataMock3, stepMetaMock3 ),
            combi( stepMock4, stepDataMock4, stepMetaMock4 ) ) );

    assertEquals( 5, trans.getErrors() );
  }

  @Test
  public void testGetEnded_nullSteps() {
    trans.setSteps( null );

    assertEquals( 0, trans.getEnded() );
  }

  @Test
  public void testGetEnded_withSteps() {
    List<StepMetaDataCombi> steps = new ArrayList<>();
    int ended = 0;

    // Steps with all possible status (and still running).
    // Finished, Halted, or Stopped (three in total) should be counted.
    ended += 3;
    for( BaseStepData.StepExecutionStatus status: BaseStepData.StepExecutionStatus.values() ) {
      StepInterface stepMock = mock( StepInterface.class );
      StepDataInterface stepDataMock = mock( StepDataInterface.class );
      StepMeta stepMetaMock = mock( StepMeta.class );

      doReturn( true ).when( stepMock ).isRunning();
      doReturn( status ).when( stepDataMock ).getStatus();

      steps.add( combi( stepMock, stepDataMock, stepMetaMock ) );
    }

    // Step not running (also to be counted)
    ++ended;
    StepInterface stepMock = mock( StepInterface.class );

    doReturn( false ).when( stepMock ).isRunning();

    steps.add( combi( stepMock, mock( StepDataInterface.class ), mock( StepMeta.class )) );

    // Add created steps
    trans.setSteps( steps );

    // A total of four (4) steps should be counted as ended.
    assertEquals( ended, trans.getEnded() );
  }

  @Test
  public void testNrSteps_nullSteps() {
    trans.setSteps( null );

    assertEquals( 0, trans.nrSteps() );
  }

  @Test
  public void testNrSteps_withSteps() {
    List<StepMetaDataCombi> steps = new ArrayList<>();
    int nrSteps = 3;

    for ( int i = 0; i < nrSteps; ++i ) {
      steps.add( combi(mock( StepInterface.class ), mock( StepDataInterface.class ), mock( StepMeta.class ) ) );
    }

    // Add created steps
    trans.setSteps( steps );

    assertEquals( nrSteps, trans.nrSteps() );
  }

  @Test
  public void testNrActiveSteps_nullSteps() {
    trans.setSteps( null );

    assertEquals( 0, trans.nrActiveSteps() );
  }

  @Test
  public void testNrActiveSteps_withSteps() {
    List<StepMetaDataCombi> steps = new ArrayList<>();
    int nrActiveSteps = 0;

    // Steps with all possible status (and still running).
    // All should be counted.
    nrActiveSteps += BaseStepData.StepExecutionStatus.values().length;
    for( BaseStepData.StepExecutionStatus status: BaseStepData.StepExecutionStatus.values() ) {
      StepInterface stepMock = mock( StepInterface.class );
      StepDataInterface stepDataMock = mock( StepDataInterface.class );
      StepMeta stepMetaMock = mock( StepMeta.class );

      doReturn( true ).when( stepMock ).isRunning();
      doReturn( status ).when( stepMock ).getStatus();

      steps.add( combi( stepMock, stepDataMock, stepMetaMock ) );
    }

    // Steps with all possible status (and not running).
    // All except Finished should be counted.
    nrActiveSteps += ( BaseStepData.StepExecutionStatus.values().length - 1 );
    for( BaseStepData.StepExecutionStatus status: BaseStepData.StepExecutionStatus.values() ) {
      StepInterface stepMock = mock( StepInterface.class );
      StepDataInterface stepDataMock = mock( StepDataInterface.class );
      StepMeta stepMetaMock = mock( StepMeta.class );

      doReturn( false ).when( stepMock ).isRunning();
      doReturn( status ).when( stepMock ).getStatus();

      steps.add( combi( stepMock, stepDataMock, stepMetaMock ) );
    }

    // Add created steps
    trans.setSteps( steps );

    assertEquals( nrActiveSteps, trans.nrActiveSteps() );
  }

  @Test
  public void testTransLoggingObjectLifecycleInterface() {
    Trans trans = new Trans();

    assertTrue( trans instanceof LoggingObjectLifecycleInterface );
//    assertEquals( 2, getMethods( Trans.class, "callBeforeLog", "callAfterLog" ).length );
  }

  @Test
  public void testJobCallBeforeLog() {
    Trans trans = new Trans();
    LoggingObjectInterface parent = mock( LoggingObjectInterface.class );
    setInternalState( trans, "parent", parent );

    trans.callBeforeLog();
    verify( parent, times( 1 ) ).callBeforeLog();
  }

  @Test
  public void testJobCallAfterLog() {
    Trans trans = new Trans();
    LoggingObjectInterface parent = mock( LoggingObjectInterface.class );
    setInternalState( trans, "parent", parent );

    trans.callAfterLog();
    verify( parent, times( 1 ) ).callAfterLog();
  }
}
