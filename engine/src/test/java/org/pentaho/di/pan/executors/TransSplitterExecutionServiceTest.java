/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/

package org.pentaho.di.pan.executors;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.pan.delegates.PanTransformationDelegate;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransExecutionConfiguration;
import org.pentaho.di.trans.cluster.TransSplitter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

@RunWith( MockitoJUnitRunner.class )
public class TransSplitterExecutionServiceTest {
  private TransSplitterExecutionService transSplitterExecutionService;

  @Mock
  private LogChannelInterface log;
  private TransExecutionConfiguration executionConfiguration;
  PanTransformationDelegate delegate;

  @Before
  public void setUp() {
    MockitoAnnotations.openMocks( this );
    transSplitterExecutionService = new TransSplitterExecutionService();
    delegate = new PanTransformationDelegate( log, null );
    executionConfiguration = createClusteredExecutionConfiguration();
  }

  @Test
  public void executeClusteredExecutesSuccessfully() throws KettleException {
    TransSplitter mockTransSplitter = mock( TransSplitter.class );
    TransExecutionConfiguration mockExecutionConfiguration = mock( TransExecutionConfiguration.class );

    try ( MockedStatic<Trans> mockedTrans = mockStatic( Trans.class ) ) {
      transSplitterExecutionService.executeClustered( log, mockTransSplitter, null, mockExecutionConfiguration );

      mockedTrans.verify( () -> Trans.executeClustered( mockTransSplitter, mockExecutionConfiguration ) );
    }
  }

  @Test
  public void executeClusteredThrowsKettleExceptionOnExecutionFailure() throws KettleException {
    TransSplitter mockTransSplitter = mock( TransSplitter.class );
    TransExecutionConfiguration mockExecutionConfiguration = mock( TransExecutionConfiguration.class );

    try ( MockedStatic<Trans> mockedTrans = mockStatic( Trans.class ) ) {
      mockedTrans.when( () -> Trans.executeClustered( any( TransSplitter.class ), any( TransExecutionConfiguration.class ) ) )
        .thenThrow( new KettleException( "Execution failed" ) );

      transSplitterExecutionService.executeClustered( log, mockTransSplitter, null, mockExecutionConfiguration );
      mockedTrans.verify( () -> Trans.cleanupCluster( log, mockTransSplitter ) );
    }
  }

  @Test
  public void cleanupClusterAfterErrorCleansUpSuccessfully() throws KettleException {
    TransSplitter mockTransSplitter = mock( TransSplitter.class );
    Exception mockException = mock( Exception.class );

    try ( MockedStatic<Trans> mockedTrans = mockStatic( Trans.class ) ) {
      transSplitterExecutionService.cleanupClusterAfterError( log, mockTransSplitter, mockException );

      mockedTrans.verify( () -> Trans.cleanupCluster( log, mockTransSplitter ) );
    }
  }

  @Test( expected = KettleException.class )
  public void cleanupClusterAfterErrorThrowsKettleExceptionOnCleanupFailure() throws KettleException {
    TransSplitter mockTransSplitter = mock( TransSplitter.class );
    Exception mockException = mock( Exception.class );

    try ( MockedStatic<Trans> mockedTrans = mockStatic( Trans.class ) ) {
      mockedTrans.when( () -> Trans.cleanupCluster( any( LogChannelInterface.class ), any( TransSplitter.class ) ) )
        .thenThrow( new RuntimeException( "Cleanup failed" ) );

      transSplitterExecutionService.cleanupClusterAfterError( log, mockTransSplitter, mockException );
    }
  }

  @Test
  public void executeClusteredReturnsClusteredTransformationResult() throws KettleException {
    TransSplitter mockTransSplitter = mock( TransSplitter.class );
    Result mockResult = mock( Result.class );

    try ( MockedStatic<Trans> mockedTrans = mockStatic( Trans.class ) ) {
      mockedTrans.when( () -> Trans.getClusteredTransformationResult( log, mockTransSplitter, null ) )
        .thenReturn( mockResult );

      Result result = transSplitterExecutionService.executeClustered( log, mockTransSplitter, null, executionConfiguration );

      Assert.assertEquals( mockResult, result );
    }
  }

  /**
   * Create an execution configuration for clustered execution.
   */
  public TransExecutionConfiguration createClusteredExecutionConfiguration() {
    TransExecutionConfiguration config = delegate.createDefaultExecutionConfiguration();

    config.setExecutingLocally( false );
    config.setExecutingClustered( true );
    config.setClusterPosting( true );
    config.setClusterPreparing( true );
    config.setClusterStarting( true );
    config.setClusterShowingTransformation( false );

    return config;
  }
}
