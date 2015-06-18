/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2015 by Pentaho : http://www.pentaho.com
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CountDownLatch;

import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs.FileObject;
import org.apache.tools.ant.filters.StringInputStream;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.ProgressMonitorListener;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaChangeListenerInterface;
import org.pentaho.di.trans.steps.metainject.MetaInjectMeta;
import org.pentaho.di.trans.steps.metainject.SourceStepField;
import org.pentaho.di.trans.steps.metainject.TargetStepAttribute;

public class TransTest {

  int count = 10000;
  Trans trans;
  TransMeta meta;

  @BeforeClass
  public static void beforeClass() throws KettleException {
    KettleEnvironment.init();
  }

  @Before
  public void beforeTest() throws KettleException {
    meta = new TransMeta();
    trans = new Trans( meta );
    trans.setLog( Mockito.mock( LogChannelInterface.class ) );
    trans.prepareExecution( null );
    trans.startThreads();
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
    assertEquals( databaseMeta.getName(), "encoded_DBConnection" );
    assertEquals( databaseMeta.getDisplayName(), "encoded.DBConnection" );
  }

  /**
   * PDI-10762 - Trans and TransMeta leak
   */
  @Test
  public void testLoggingObjectIsNotLeakInMeta() {
    String expected = meta.log.getLogChannelId();
    meta.clear();
    String actual = meta.log.getLogChannelId();
    assertEquals( "Use same logChannel for empty constructors, or assign General level for clear() calls",
        expected, actual );
  }

  /**
   * PDI-10762 - Trans and TransMeta leak
   */
  @Test
  public void testLoggingObjectIsNotLeakInTrans() throws KettleException {
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
   * 
   * @throws KettleException
   * @throws InterruptedException
   */
  @Test
  public void testTransFinishListenersConcurrentModification() throws KettleException, InterruptedException {
    CountDownLatch start = new CountDownLatch( 1 );
    TransFinishListenerAdder add = new TransFinishListenerAdder( trans, start );
    TransFinishListenerFirer firer = new TransFinishListenerFirer( trans, start );
    startThreads( add, firer, start );
    assertEquals( "All listeners are added: no ConcurrentModificationException", count, add.c );
    assertEquals( "All Finish listeners are iterated over: no ConcurrentModificationException", count, firer.c );
  }

  /**
   * Test that listeners can be accessed concurrently during transformation start
   * 
   * @throws InterruptedException
   */
  @Test
  public void testTransStartListenersConcurrentModification() throws InterruptedException {
    CountDownLatch start = new CountDownLatch( 1 );
    TransFinishListenerAdder add = new TransFinishListenerAdder( trans, start );
    TransStartListenerFirer starter = new TransStartListenerFirer( trans, start );
    startThreads( add, starter, start );
    assertEquals( "All listeners are added: no ConcurrentModificationException", count, add.c );
    assertEquals( "All Start listeners are iterated over: no ConcurrentModificationException", count, starter.c );
  }

  /**
   * Test that transformation stop listeners can be accessed concurrently
   * 
   * @throws InterruptedException
   */
  @Test
  public void testTransStoppedListenersConcurrentModification() throws InterruptedException {
    CountDownLatch start = new CountDownLatch( 1 );
    TransStoppedCaller stopper = new TransStoppedCaller( trans, start );
    TransStopListenerAdder adder = new TransStopListenerAdder( trans, start );
    startThreads( stopper, adder, start );
    assertEquals( "All transformation stop listeners is added", count, adder.c );
    assertEquals( "All stop call success", count, stopper.c );
  }

  @Test
  public void testPDI12424ParametersFromMetaAreCopiedToTrans() throws KettleException, URISyntaxException, IOException {
    String testParam = "testParam";
    String testParamValue = "testParamValue";
    TransMeta mockTransMeta = mock( TransMeta.class );
    when( mockTransMeta.listVariables() ).thenReturn( new String[] {} );
    when( mockTransMeta.listParameters() ).thenReturn( new String[] { testParam } );
    when( mockTransMeta.getParameterValue( testParam ) ).thenReturn( testParamValue );
    FileObject ktr = KettleVFS.createTempFile( "parameters", ".ktr", "ram://" );
    OutputStream outputStream = ktr.getContent().getOutputStream( true );
    StringInputStream stringInputStream = new StringInputStream( "<transformation></transformation>" );
    IOUtils.copy( stringInputStream, outputStream );
    outputStream.close();
    Trans trans = new Trans( mockTransMeta, null, null, null, ktr.getURL().toURI().toString() );
    assertEquals( testParamValue, trans.getParameterValue( testParam ) );
  }

  @Test
  public void testTransListeners() {
    TransMeta TransMeta = new TransMeta();

    StepMeta oldFormStep = new StepMeta();
    oldFormStep.setName( "Generate_1" );

    StepMeta newFormStep = new StepMeta();
    newFormStep.setName( "Generate_2" );

    StepMeta toStep = new StepMeta();
    toStep.setStepMetaInterface( new MetaInjectMeta() );
    toStep.setName( "ETL Inject Metadata" );

    StepMeta deletedStep = new StepMeta();
    deletedStep.setStepMetaInterface( new MetaInjectMeta() );
    deletedStep.setName( "ETL Inject Metadata for delete" );

    // Verify add & remove listeners

    TransMeta.addStep( oldFormStep );
    TransMeta.addStep( toStep );
    TransMeta.addStep( deletedStep );

    assertEquals( TransMeta.nrStepChangeListeners(), 2 );
    TransMeta.removeStepChangeListener( (StepMetaChangeListenerInterface) deletedStep.getStepMetaInterface() );
    assertEquals( TransMeta.nrStepChangeListeners(), 1 );
    TransMeta.removeStep( 2 );

    TransHopMeta hi = new TransHopMeta( oldFormStep, toStep );
    TransMeta.addTransHop( hi );

    // Verify MetaInjectMeta.onStepChange()

    // add new TargetStepAttribute
    MetaInjectMeta toMeta = (MetaInjectMeta) toStep.getStepMetaInterface();

    Map<TargetStepAttribute, SourceStepField> sourceMapping = new HashMap<TargetStepAttribute, SourceStepField>();
    TargetStepAttribute keyTest = new TargetStepAttribute( "File", "key", true );
    SourceStepField valueTest = new SourceStepField( oldFormStep.getName(), oldFormStep.getName() );
    sourceMapping.put( keyTest, valueTest );

    toMeta.setTargetSourceMapping( sourceMapping );

    // Run all listeners
    TransMeta.notifyAllListeners( oldFormStep, newFormStep );

    // Verify changes, which listeners makes
    sourceMapping = toMeta.getTargetSourceMapping();
    for ( Entry<TargetStepAttribute, SourceStepField> entry : sourceMapping.entrySet() ) {
      SourceStepField value = entry.getValue();
      if ( !value.getStepname().equals( newFormStep.getName() ) ) {
        fail();
      }
    }

    // verify another functions
    TransMeta.addStep( 1, deletedStep );
    assertEquals( TransMeta.nrSteps(), 3 );
    assertEquals( TransMeta.nrStepChangeListeners(), 2 );

    TransMeta.removeStep( 0 );
    assertEquals( TransMeta.nrSteps(), 2 );

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
}
