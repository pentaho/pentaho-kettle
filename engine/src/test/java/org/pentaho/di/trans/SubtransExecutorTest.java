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

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterfaceFactory;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.core.logging.LoggingObject;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.trans.step.StepStatus;
import org.pentaho.di.trans.steps.transexecutor.TransExecutorParameters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
//import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

@RunWith( MockitoJUnitRunner.class )
public class SubtransExecutorTest {
  @Mock private LogChannelInterfaceFactory logChannelFactory;
  @Mock private LogChannel logChannel;
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @Before
  public void setUp() throws Exception {
    KettleLogStore.setLogChannelInterfaceFactory( this.logChannelFactory );
    doReturn( LogLevel.BASIC ).when( logChannel ).getLogLevel();
    Mockito.when( this.logChannelFactory.create( any(), any() ) ).thenReturn( this.logChannel );
  }

  @BeforeClass
  public static void init() throws Exception {
    KettleClientEnvironment.init();
    PluginRegistry.addPluginType( StepPluginType.getInstance() );
    PluginRegistry.init();
    if ( !Props.isInitialized() ) {
      Props.init( 0 );
    }
  }

  @Test
  public void testRunningZeroRowsIsEmptyOptional() throws Exception {
    SubtransExecutor subtransExecutor = new SubtransExecutor( "subtransname", null, null, false, null, "", 0 );
    Optional<Result> execute = subtransExecutor.execute( Collections.emptyList() );
    assertFalse( execute.isPresent() );
  }

  @Test
  public void testRunsATrans() throws Exception {
    TransMeta parentMeta =
      new TransMeta( this.getClass().getResource( "subtrans-executor-parent.ktr" ).getPath(), new Variables() );
    TransMeta subMeta =
      new TransMeta( this.getClass().getResource( "subtrans-executor-sub.ktr" ).getPath(), new Variables() );
    LoggingObjectInterface loggingObject = new LoggingObject( "anything" );
    Trans parentTrans = spy( new Trans( parentMeta, loggingObject ) );
    SubtransExecutor subtransExecutor =
      new SubtransExecutor( "subtransname", parentTrans, subMeta, true, new TransExecutorParameters(), "Group By",
        1001 );
    RowMetaInterface rowMeta = parentMeta.getStepFields( "Data Grid" );
    List<RowMetaAndData> rows = Arrays.asList(
      new RowMetaAndData( rowMeta, "Pentaho", 1L ),
      new RowMetaAndData( rowMeta, "Pentaho", 2L ),
      new RowMetaAndData( rowMeta, "Pentaho", 3L ),
      new RowMetaAndData( rowMeta, "Pentaho", 4L ) );
    Optional<Result> optionalResult = subtransExecutor.execute( rows );
    assertEquals( 1, optionalResult.orElseThrow( AssertionError::new ).getRows().size() );
    verify( this.logChannel )
      .logBasic(
        Const.CR
          + "------------> Linenr 1------------------------------"
          + Const.CR
          + "name = Pentaho"
          + Const.CR
          + "sum = 10"
          + Const.CR
          + Const.CR
          + "===================="
      );

    Map<String, StepStatus> statuses = subtransExecutor.getStatuses();
    assertEquals( 3, statuses.size() );
    List<StepStatus> statusList = new ArrayList<>( statuses.values() );
    assertEquals( "Get rows from result", statusList.get( 0 ).getStepname() );
    assertEquals( "Group by", statusList.get( 1 ).getStepname() );
    assertEquals( "Write to log", statusList.get( 2 ).getStepname() );
    for ( Map.Entry<String, StepStatus> entry : statuses.entrySet() ) {
      StepStatus statusSpy = spy( entry.getValue() );
      statuses.put( entry.getKey(), statusSpy );
    }

    subtransExecutor.execute( rows );
    for ( Map.Entry<String, StepStatus> entry : statuses.entrySet() ) {
      verify( entry.getValue() ).updateAll( any() );
    }

    verify( parentTrans, atLeastOnce() ).addActiveSubTransformation( eq( "subtransname" ), any( Trans.class ) );
  }

  @Test
  public void stopsAll() throws KettleException {
    TransMeta parentMeta =
      new TransMeta( this.getClass().getResource( "subtrans-executor-parent.ktr" ).getPath(), new Variables() );
    TransMeta subMeta =
      new TransMeta( this.getClass().getResource( "subtrans-executor-sub.ktr" ).getPath(), new Variables() );
    LoggingObjectInterface loggingObject = new LoggingObject( "anything" );
    Trans parentTrans = new Trans( parentMeta, loggingObject );
    SubtransExecutor subtransExecutor =
      new SubtransExecutor( "subtransname", parentTrans, subMeta, true, new TransExecutorParameters(), "", 1001 );
    subtransExecutor.running = Mockito.spy( subtransExecutor.running );
    RowMetaInterface rowMeta = parentMeta.getStepFields( "Data Grid" );
    List<RowMetaAndData> rows = Arrays.asList(
      new RowMetaAndData( rowMeta, "Pentaho", 1L ),
      new RowMetaAndData( rowMeta, "Pentaho", 2L ),
      new RowMetaAndData( rowMeta, "Pentaho", 3L ),
      new RowMetaAndData( rowMeta, "Pentaho", 4L ) );
    subtransExecutor.execute( rows );
    verify( subtransExecutor.running ).add( any() );
    subtransExecutor.stop();
    assertTrue( subtransExecutor.running.isEmpty() );
  }

  @Test
  public void doesNotExecuteWhenStopped() throws KettleException {

    TransMeta parentMeta =
      new TransMeta( this.getClass().getResource( "subtrans-executor-parent.ktr" ).getPath(), new Variables() );
    TransMeta subMeta =
      new TransMeta( this.getClass().getResource( "subtrans-executor-sub.ktr" ).getPath(), new Variables() );
    LoggingObjectInterface loggingObject = new LoggingObject( "anything" );
    Trans parentTrans = new Trans( parentMeta, loggingObject );
    SubtransExecutor subtransExecutor =
      new SubtransExecutor( "subtransname", parentTrans, subMeta, true, new TransExecutorParameters(), "", 1001 );
    RowMetaInterface rowMeta = parentMeta.getStepFields( "Data Grid" );
    List<RowMetaAndData> rows = Arrays.asList(
      new RowMetaAndData( rowMeta, "Pentaho", 1L ),
      new RowMetaAndData( rowMeta, "Pentaho", 2L ),
      new RowMetaAndData( rowMeta, "Pentaho", 3L ),
      new RowMetaAndData( rowMeta, "Pentaho", 4L ) );
    subtransExecutor.stop();
    subtransExecutor.execute( rows );

    verify( this.logChannel, never() )
      .logBasic(
        "\n"
          + "------------> Linenr 1------------------------------\n"
          + "name = Pentaho\n"
          + "sum = 10\n"
          + "\n"
          + "===================="
      );
  }

  /**
   * When we acquire the last permit, the thread should block until we timeout the test
   */
  @Test
  public void blockOnAcquiringLastPermit() throws InterruptedException, ExecutionException, TimeoutException {
    final ExecutorService executorService = Executors.newFixedThreadPool( 1 );

    SubtransExecutor subtransExecutor = new SubtransExecutor( "subtransname", null, null, false, null, "", 0 );

    CompletableFuture<Boolean> acquireThreadRunning = new CompletableFuture();

    //Prefetch count is set to 0, so blocking occurs when that single permit is acquired
    Future future = executorService.submit( () -> safeAcquirePermits( subtransExecutor, 1, acquireThreadRunning ) );

    assertTrue( acquireThreadRunning.get( 5, TimeUnit.SECONDS ) );

    final boolean timedOut = !safeWaitForCompletion( future, 2 );
    assertTrue( timedOut );
  }

  @Test
  public void blockAndUnblockTwice() throws KettleException, InterruptedException, ExecutionException,
    TimeoutException {
    final ExecutorService executorService = Executors.newFixedThreadPool( 1 );

    TransMeta parentMeta =
      new TransMeta( this.getClass().getResource( "subtrans-executor-parent.ktr" ).getPath(), new Variables() );
    TransMeta subMeta =
      new TransMeta( this.getClass().getResource( "subtrans-executor-sub.ktr" ).getPath(), new Variables() );
    LoggingObjectInterface loggingObject = new LoggingObject( "anything" );
    Trans parentTrans = new Trans( parentMeta, loggingObject );
    SubtransExecutor subtransExecutor =
      new SubtransExecutor( "subtransname", parentTrans, subMeta, true, new TransExecutorParameters(), "", 1 );

    RowMetaInterface rowMeta = parentMeta.getStepFields( "Data Grid" );
    List<RowMetaAndData> rows = Arrays.asList(
      new RowMetaAndData( rowMeta, "Pentaho", 1L ),
      new RowMetaAndData( rowMeta, "Pentaho", 2L ),
      new RowMetaAndData( rowMeta, "Pentaho", 3L ),
      new RowMetaAndData( rowMeta, "Pentaho", 4L ) );

    CompletableFuture<Boolean> acquireThreadRunning = new CompletableFuture();

    Future future = executorService.submit( () -> safeAcquirePermits( subtransExecutor, 8, acquireThreadRunning ) );

    //Acquire the permits before releasing them by waiting for the acquire thread to spin up.
    assertTrue( acquireThreadRunning.get( 5, TimeUnit.SECONDS ) );

    //Release 2*4=8 permits by calling execute twice with 4 rows
    subtransExecutor.execute( rows );
    subtransExecutor.execute( rows );

    final boolean timedOut = !safeWaitForCompletion( future, 5 );
    assertFalse( timedOut );
  }

  private void safeAcquirePermits( SubtransExecutor subtransExecutor, int permitCount,
                                   CompletableFuture<Boolean> threadRunning ) {
    try {
      for ( int i = 0; i < permitCount; i++ ) {
        threadRunning.complete( true );
        subtransExecutor.acquireBufferPermit();
      }
    } catch ( InterruptedException e ) {
      fail();
    }
  }

  /**
   * @param future
   * @return false if timed out, otherwise true
   */
  private boolean safeWaitForCompletion( Future future, long timeoutSeconds ) {
    try {
      future.get( timeoutSeconds, TimeUnit.SECONDS );
    } catch ( TimeoutException te ) {
      return false;
    } catch ( InterruptedException | ExecutionException e ) {
      fail();
    }
    return true;
  }
}
