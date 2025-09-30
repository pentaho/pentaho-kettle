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

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.pan.delegates.PanTransformationDelegate;
import org.pentaho.di.trans.TransExecutionConfiguration;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.cluster.TransSplitter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ClusteredTransExecutorServiceTest {
  @Mock
  private LogChannelInterface log;

  private static final String DASHES = "-----------------------------------------------------";

  private ClusteredTransExecutorService clusteredTransExecutorService;
  @Mock
  private TransMeta transMeta;
  private TransExecutionConfiguration executionConfiguration;

  @Mock
  private TransSplitterExecutionService transSplitterExecutionService;
  private PanTransformationDelegate delegate;


  @Before
  public void setUp() {
    MockitoAnnotations.openMocks( this );
    clusteredTransExecutorService = new ClusteredTransExecutorService( transSplitterExecutionService );
    delegate = new PanTransformationDelegate( log, null );
    executionConfiguration = createClusteredExecutionConfiguration();
  }

  @Test
  public void logClusteredResultsLogsAllMetrics() {
    TransMeta mockTransMeta = mock( TransMeta.class );
    Result mockResult = mock( Result.class );

    when( mockTransMeta.toString() ).thenReturn( "MockTransformation" );
    when( mockResult.getNrErrors() ).thenReturn( 2L );
    when( mockResult.getNrLinesInput() ).thenReturn( 10L );
    when( mockResult.getNrLinesOutput() ).thenReturn( 8L );
    when( mockResult.getNrLinesUpdated() ).thenReturn( 1L );
    when( mockResult.getNrLinesRead() ).thenReturn( 12L );
    when( mockResult.getNrLinesWritten() ).thenReturn( 7L );
    when( mockResult.getNrLinesRejected() ).thenReturn( 3L );
    LogChannelInterface logMock = mock( LogChannelInterface.class );
    clusteredTransExecutorService.logClusteredResults( logMock, mockTransMeta, mockResult );

    verify( logMock ).logBasic( DASHES );
    verify( logMock ).logBasic( "Got result back from clustered transformation:" );
    verify( logMock, atLeastOnce() ).logBasic( "MockTransformation" + DASHES );
    verify( logMock ).logBasic( "MockTransformation Errors : 2" );
    verify( logMock ).logBasic( "MockTransformation Input : 10" );
    verify( logMock ).logBasic( "MockTransformation Output : 8" );
    verify( logMock ).logBasic( "MockTransformation Updated : 1" );
    verify( logMock ).logBasic( "MockTransformation Read : 12" );
    verify( logMock ).logBasic( "MockTransformation Written : 7" );
    verify( logMock ).logBasic( "MockTransformation Rejected : 3" );
  }


  @Test
  public void executeClusteredCallsTransSplitterExecutionService() throws KettleException {
    TransSplitter mockTransSplitter = mock( TransSplitter.class );
    TransMeta mockOriginalTransformation = mock( TransMeta.class );
    TransSplitterExecutionService mockExecutionService = mock( TransSplitterExecutionService.class );
    when( mockTransSplitter.getOriginalTransformation() ).thenReturn( mockOriginalTransformation );
    when( mockOriginalTransformation.listParameters() ).thenReturn( new String[] {"param1", "param2"} );
    when( mockOriginalTransformation.getParameterValue( "param1" ) ).thenReturn( "value1" );
    when( mockOriginalTransformation.getParameterDefault( "param2" ) ).thenReturn( "default2" );


    clusteredTransExecutorService = new ClusteredTransExecutorService( mockExecutionService );

    clusteredTransExecutorService.executeClustered( log, transMeta, mockTransSplitter, executionConfiguration );

    verify( mockExecutionService ).executeClustered( log, mockTransSplitter, null, executionConfiguration );
  }

  @Test( expected = KettleException.class )
  public void executeClusteredThrowsKettleExceptionOnError() throws KettleException {
    TransSplitter mockTransSplitter = mock( TransSplitter.class );
    TransMeta mockOriginalTransformation = mock( TransMeta.class );
    TransSplitterExecutionService mockExecutionService = mock( TransSplitterExecutionService.class );
    when( mockTransSplitter.getOriginalTransformation() ).thenReturn( mockOriginalTransformation );
    when( mockOriginalTransformation.listParameters() ).thenReturn( new String[] {"param1", "param2"} );
    clusteredTransExecutorService = new ClusteredTransExecutorService( mockExecutionService );
    when( mockExecutionService.executeClustered( any(), any(), any(), any() ) ).thenThrow( new KettleException() );

    clusteredTransExecutorService.executeClustered( log, transMeta, mockTransSplitter, executionConfiguration );
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
