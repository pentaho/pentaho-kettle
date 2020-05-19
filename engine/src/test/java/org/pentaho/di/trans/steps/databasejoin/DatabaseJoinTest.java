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

package org.pentaho.di.trans.steps.databasejoin;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.step.StepPartitioningMeta;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;
import static org.powermock.api.mockito.PowerMockito.spy;

public class DatabaseJoinTest {

  DatabaseJoinMeta mockStepMetaInterface;
  DatabaseJoinData mockStepDataInterface;
  DatabaseJoin mockDatabaseJoin;

  @Before
  public void setUp() {

    StepMeta mockStepMeta = mock( StepMeta.class );
    TransMeta mockTransMeta = mock( TransMeta.class );
    Trans mockTrans = mock( Trans.class );
    StepPartitioningMeta mockStepPartitioningMeta = mock( StepPartitioningMeta.class );

    when( mockStepMeta.getName() ).thenReturn( "MockStep" );
    when( mockTransMeta.findStep( anyString() ) ).thenReturn( mockStepMeta );
    when( mockStepMeta.getTargetStepPartitioningMeta() ).thenReturn( mockStepPartitioningMeta );

    mockStepMetaInterface = mock( DatabaseJoinMeta.class, withSettings().extraInterfaces( StepMetaInterface.class ) );
    mockStepDataInterface = mock( DatabaseJoinData.class, withSettings().extraInterfaces( StepMetaInterface.class ) );
    mockStepDataInterface.db = mock( Database.class );
    mockStepDataInterface.pstmt = mock( PreparedStatement.class );
    mockDatabaseJoin = spy( new DatabaseJoin( mockStepMeta, mockStepDataInterface, 1, mockTransMeta, mockTrans ) );
  }

  @Test
  public void testStopRunningWhenStepIsStopped() throws KettleException {
    doReturn( true ).when( mockDatabaseJoin ).isStopped();

    mockDatabaseJoin.stopRunning( mockStepMetaInterface, mockStepDataInterface );

    verify( mockDatabaseJoin, times( 1 ) ).isStopped();
    verify( mockStepDataInterface, times( 0 ) ).isDisposed();
  }

  @Test
  public void testStopRunningWhenStepDataInterfaceIsDisposed() throws KettleException {
    doReturn( false ).when( mockDatabaseJoin ).isStopped();
    doReturn( true ).when( mockStepDataInterface ).isDisposed();

    mockDatabaseJoin.stopRunning( mockStepMetaInterface, mockStepDataInterface );

    verify( mockDatabaseJoin, times( 1 ) ).isStopped();
    verify( mockStepDataInterface, times( 1 ) ).isDisposed();
  }

  @Test
  public void testStopRunningWhenStepIsNotStoppedNorStepDataInterfaceIsDisposedAndDatabaseConnectionIsValid() throws KettleException {
    doReturn( false ).when( mockDatabaseJoin ).isStopped();
    doReturn( false ).when( mockStepDataInterface ).isDisposed();
    when( mockStepDataInterface.db.getConnection() ).thenReturn( mock( Connection.class ) );

    mockDatabaseJoin.stopRunning( mockStepMetaInterface, mockStepDataInterface );

    verify( mockDatabaseJoin, times( 1 ) ).isStopped();
    verify( mockStepDataInterface, times( 1 ) ).isDisposed();
    verify( mockStepDataInterface.db, times( 1 ) ).getConnection();
    verify( mockStepDataInterface.db, times( 1 ) ).cancelStatement( any( PreparedStatement.class ) );
    assertTrue( mockStepDataInterface.isCanceled );

  }

  @Test
  public void testStopRunningWhenStepIsNotStoppedNorStepDataInterfaceIsDisposedAndDatabaseConnectionIsNotValid() throws KettleException {
    doReturn( false ).when( mockDatabaseJoin ).isStopped();
    doReturn( false ).when( mockStepDataInterface ).isDisposed();
    when( mockStepDataInterface.db.getConnection() ).thenReturn( null );

    mockDatabaseJoin.stopRunning( mockStepMetaInterface, mockStepDataInterface );

    verify( mockDatabaseJoin, times( 1 ) ).isStopped();
    verify( mockStepDataInterface, times( 1 ) ).isDisposed();
    verify( mockStepDataInterface.db, times( 1 ) ).getConnection();
    verify( mockStepDataInterface.db, times( 0 ) ).cancelStatement( any( PreparedStatement.class ) );
    assertFalse( mockStepDataInterface.isCanceled );
  }

  @Test
  public void testLookupStopRunningPossibleDeadlock() throws Exception {

    //Prepare mock results
    RowMeta mockRowMeta = mock( RowMeta.class );
    DatabaseJoinMeta mockStepMetaInterfaceCausesException = mock( DatabaseJoinMeta.class );
    doReturn( new Object[] { null, null, null } ).when( mockDatabaseJoin ).getRow();
    doReturn( mockRowMeta ).when( mockDatabaseJoin ).getInputRowMeta();
    doReturn( new String[]{} ).when( mockStepMetaInterface ).getParameterField();
    doReturn( new String[]{"", ""} ).when( mockStepMetaInterfaceCausesException ).getParameterField();
    doReturn( -1 ).when( mockRowMeta ).indexOfValue( anyString() );
    doNothing().when( mockDatabaseJoin ).logError( anyString(), (Throwable) any() );
    doNothing().when( mockDatabaseJoin ).setOutputDone();

    //When stopAll is invoked call stopRunning. Only called when lookup has an exception.
    doAnswer( invocationOnMock -> {
      //Force context switch
      Thread.sleep( 1 );
      mockDatabaseJoin.stopRunning( mockStepMetaInterfaceCausesException, mockStepDataInterface );
      return 1;
    } ).when( mockDatabaseJoin ).stopAll();

    //When incrementLinesInput is invoked call stopRunning. Only called when lookup terminates ok.
    doAnswer( invocationOnMock -> {
      //Force context switch
      Thread.sleep( 1 );
      mockDatabaseJoin.stopRunning( mockStepMetaInterface, mockStepDataInterface );
      return 1;
    } ).when( mockDatabaseJoin ).incrementLinesInput();

    /* This simulates calling several processRows, the first ones will end up ok, calling stopRunning in the end.
    The second ones will fail in the process which will call stopRunning.
    This tests if we end up with a deadlock like the one caused in (PDI-18406), because lookup and stopRunning had the same lock
    and called each other in two different situations (failing and processing until the end)*/

    int threadPoolSize = 3;
    int tasks = 5000;
    int timeout = 80000;
    final AtomicBoolean hasFailed = new AtomicBoolean( false );
    ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(
        threadPoolSize, new ThreadFactory() {
          public Thread newThread( Runnable r ) {
            final Thread t = new Thread( r );
            t.setUncaughtExceptionHandler( new Thread.UncaughtExceptionHandler() {
              public void uncaughtException( Thread t, Throwable e ) {
                e.printStackTrace();
                hasFailed.set( true );
              }
            } );
            return t;
          }
        } );

    for ( int i = 0; i < tasks; i++ ) {
      executor.execute( () -> {
        try {
          mockDatabaseJoin.processRow( mockStepMetaInterface, mockStepDataInterface );
        } catch ( KettleException e ) {
          e.printStackTrace();
        }
      } );
    }
    for ( int i = 0; i < tasks; i++ ) {
      executor.execute( () -> {
        try {
          mockDatabaseJoin.processRow( mockStepMetaInterfaceCausesException, mockStepDataInterface );
        } catch ( KettleException e ) {
          e.printStackTrace();
        }
      } );
    }
    executor.shutdown();
    if ( !executor.awaitTermination( timeout, TimeUnit.MILLISECONDS ) ) {
      Assert.fail( "Deadlock detected" );
    }
    assertFalse( "Errors encountered.", hasFailed.get() );
  }
}
