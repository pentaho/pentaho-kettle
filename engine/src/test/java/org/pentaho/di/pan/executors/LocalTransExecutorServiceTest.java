/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/


package org.pentaho.di.pan.executors;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.pan.delegates.PanTransformationDelegate;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransExecutionConfiguration;

import java.util.Map;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.openMocks;

public class LocalTransExecutorServiceTest {

  @Mock
  private Repository repository;
  private LocalTransExecutorService localTransExecutorService;

  @Mock
  private LogChannelInterface log;
  private PanTransformationDelegate delegate;

  @Before
  public void setUp() {
    openMocks( this );
    localTransExecutorService = new LocalTransExecutorService();
    delegate = new PanTransformationDelegate( log, repository );
  }

  @Test
  public void testExecuteTransformationLocally() throws KettleException {
    Trans mockTrans = mock( Trans.class );
    TransExecutionConfiguration config = delegate.createDefaultExecutionConfiguration();
    config.setVariables( Map.of( "param", "value" ) );
    String[] params = new String[0];
    localTransExecutorService.execute( mockTrans, repository, config, params );
    verify( mockTrans ).setRepository( repository );
    verify( mockTrans ).setLogLevel( config.getLogLevel() );
    verify( mockTrans ).setSafeModeEnabled( config.isSafeModeEnabled() );
    verify( mockTrans ).setGatheringMetrics( config.isGatheringMetrics() );

    verify( mockTrans ).prepareExecution( params );
    verify( mockTrans ).startThreads();
    verify( mockTrans ).waitUntilFinished();
    assertSame( mockTrans, localTransExecutorService.getTransToLog() );
  }

  @Test
  public void testPreparationFailureRetainsCause() throws KettleException {
    Trans trans = mock( Trans.class );
    KettleException failure = new KettleException( "prepare failed" );
    doThrow( failure ).when( trans ).prepareExecution( any( String[].class ) );
    try {
      localTransExecutorService.execute( trans, repository, delegate.createDefaultExecutionConfiguration(), new String[0] );
      fail( "Expected initialization failure" );
    } catch ( LocalTransExecutorService.KettleTransInitializationException e ) {
      assertSame( failure, e.getCause() );
    }
    verify( trans, never() ).startThreads();
  }

  @Test
  public void testStartFailureRetainsCause() throws KettleException {
    Trans trans = mock( Trans.class );
    KettleException failure = new KettleException( "start failed" );
    doThrow( failure ).when( trans ).startThreads();
    try {
      localTransExecutorService.execute( trans, repository, delegate.createDefaultExecutionConfiguration(), new String[0] );
      fail( "Expected initialization failure" );
    } catch ( LocalTransExecutorService.KettleTransInitializationException e ) {
      assertSame( failure, e.getCause() );
    }
    verify( trans, never() ).waitUntilFinished();
  }

}
