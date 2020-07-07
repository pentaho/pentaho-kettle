/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2020 by Hitachi Vantara : http://www.pentaho.com
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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.ProgressMonitorListener;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.logging.StepLogTable;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.trans.step.StepDataInterface;
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

import static com.google.common.collect.ImmutableList.of;
import static java.util.Collections.emptyList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith ( MockitoJUnitRunner.class )
public class TransTest {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  private int count = 10000;
  private Trans trans;
  private TransMeta meta;


  @BeforeClass
  public static void beforeClass() throws Exception {
    KettleEnvironment.init();
  }

  @Before
  public void beforeTest() throws Exception {
    meta = spy( new TransMeta() );
    trans = spy( new Trans( meta ) );
    trans.setLog( Mockito.mock( LogChannelInterface.class ) );
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
  @Test
  public void testLoggingObjectIsNotLeakInTrans() throws Exception {
    Repository rep = Mockito.mock( Repository.class );
    RepositoryDirectoryInterface repInt = Mockito.mock( RepositoryDirectoryInterface.class );
    Mockito.when(
      rep.loadTransformation( Mockito.anyString(), Mockito.any( RepositoryDirectoryInterface.class ), Mockito
        .any( ProgressMonitorListener.class ), Mockito.anyBoolean(), Mockito.anyString() ) ).thenReturn( meta );
    Mockito.when( rep.findDirectory( Mockito.anyString() ) ).thenReturn( repInt );

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
    FileObject ktr = KettleVFS.createTempFile( "parameters", ".ktr", "ram://" );
    try ( OutputStream outputStream = ktr.getContent().getOutputStream( true ) ) {
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
    doReturn( mockedDataBase ).when( trans ).createDataBase( any( DatabaseMeta.class ) );
    trans.setSteps( new ArrayList<>() );

    trans.writeStepLogInformation();

    verify( mockedDataBase ).cleanupLogRecords( stepLogTable );
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
  }

  @Test
  public void safeLetsNonInputStepsKeepRunning() throws Exception {
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
    assertNotNull( trans.getSteps() );

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
    verify( stepMock2, times( 1 ) ).dispose( any( StepMetaInterface.class ), any( StepDataInterface.class ) );
    // The cleanup method is always invoked
    verify( stepMock1 ).cleanup();
    verify( stepMock2 ).cleanup();
  }

}
