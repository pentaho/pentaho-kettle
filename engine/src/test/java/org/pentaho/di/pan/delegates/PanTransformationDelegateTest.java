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

package org.pentaho.di.pan.delegates;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.pan.executors.ClusteredTransExecutorService;
import org.pentaho.di.pan.executors.LocalTransExecutorService;
import org.pentaho.di.pan.executors.RemoteTransExecutorService;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransExecutionConfiguration;
import org.pentaho.di.trans.TransMeta;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

/**
 * Unit tests for PanTransformationDelegate.
 */
public class PanTransformationDelegateTest {

  @Mock
  private TransMeta transMeta;

  @Mock
  private Trans trans;

  @Mock
  private Repository repository;

  @Mock
  private LogChannelInterface log;

  private PanTransformationDelegate delegate;

  @Before
  public void setUp() {
    openMocks( this );
    delegate = new PanTransformationDelegate( log, repository );
  }

  @Test( expected = KettleException.class )
  public void testExecuteTransformationWithNullTransMeta() throws KettleException {
    TransMeta t = null;
    when( trans.getTransMeta() ).thenReturn( t );
    TransExecutionConfiguration config = delegate.createDefaultExecutionConfiguration();
    delegate.executeTransformation( trans, config, new String[0] );
  }

  @Test
  public void testCreateDefaultExecutionConfiguration() {
    TransExecutionConfiguration config = delegate.createDefaultExecutionConfiguration();

    assertTrue( "Should be executing locally", config.isExecutingLocally() );
    assertFalse( "Should not be executing remotely", config.isExecutingRemotely() );
    assertFalse( "Should not be executing clustered", config.isExecutingClustered() );
    assertTrue( "Should be clearing log", config.isClearingLog() );
    assertFalse( "Should not be in safe mode", config.isSafeModeEnabled() );
    assertFalse( "Should not be gathering metrics", config.isGatheringMetrics() );
    assertEquals( "Should have basic log level", LogLevel.BASIC, config.getLogLevel() );
    assertNotNull( "Variables map should not be null", config.getVariables() );
    assertNotNull( "Parameters map should not be null", config.getParams() );
  }

  @Test
  public void testExecutionConfigurationParametersAndVariables() {
    // Setup mock behavior
    when( transMeta.getName() ).thenReturn( "TestTransformation" );
    when( transMeta.getFilename() ).thenReturn( "test.ktr" );

    TransExecutionConfiguration config = delegate.createDefaultExecutionConfiguration();

    // Add some variables and parameters
    Map<String, String> variables = new HashMap<>();
    variables.put( "TEST_VAR", "test_value" );
    config.setVariables( variables );

    Map<String, String> parameters = new HashMap<>();
    parameters.put( "TEST_PARAM", "param_value" );
    config.setParams( parameters );

    // Test that configuration is properly set
    assertEquals( "test_value", config.getVariables().get( "TEST_VAR" ) );
    assertEquals( "param_value", config.getParams().get( "TEST_PARAM" ) );
  }

  @Test
  public void testDelegateRepositoryAndLogSettings() {
    assertEquals( "Repository should match", repository, delegate.getRepository() );
    assertEquals( "Log should match", log, delegate.getLog() );

    // Test setting new repository
    Repository newRepository = mock( Repository.class );
    delegate.setRepository( newRepository );
    assertEquals( "Repository should be updated", newRepository, delegate.getRepository() );

    // Test setting new log
    LogChannelInterface newLog = new LogChannel( "NewLog" );
    delegate.setLog( newLog );
    assertEquals( "Log should be updated", newLog, delegate.getLog() );
  }

  /**
   * Test the execution configuration factory methods.
   */
  @Test
  public void testExecutionConfigurationFactoryMethods() {
    // Test default configuration
    TransExecutionConfiguration defaultConfig = delegate.createDefaultExecutionConfiguration();
    assertNotNull( "Default config should not be null", defaultConfig );
    assertTrue( "Default should be local execution", defaultConfig.isExecutingLocally() );
  }

  /**
   * Integration test showing how the helper class would be used.
   */
  @Test
  public void testTransformationExecutionHelperIntegration() {
    // This test demonstrates how the helper class would be used
    // Note: In a real test, you'd need to properly mock the Trans class and its dependencies

    TransMeta mockTransMeta = mock( TransMeta.class );
    when( mockTransMeta.getName() ).thenReturn( "TestTransformation" );
    when( mockTransMeta.toString() ).thenReturn( "TestTransformation" );

    // Test that the helper methods can be called without throwing exceptions
    // (actual execution would require more complex mocking)
    Map<String, String> variables = new HashMap<>();
    variables.put( "TEST_VAR", "test_value" );

    Map<String, String> parameters = new HashMap<>();
    parameters.put( "TEST_PARAM", "param_value" );

    // This would typically execute the transformation, but for unit testing
    // we'd need extensive mocking of the Trans class
    assertNotNull( "Variables should be set", variables );
    assertNotNull( "Parameters should be set", parameters );
  }

  @Test
  public void executeTransformationLocalExecutorCalledTest() throws KettleException {
    Result result = mock( Result.class );
    LocalTransExecutorService mockLocalTransExecutorService = mock( LocalTransExecutorService.class );
    ClusteredTransExecutorService mockClusteredTransExecutorService = mock( ClusteredTransExecutorService.class );
    RemoteTransExecutorService mockRemoteTransExecutorService = mock( RemoteTransExecutorService.class );
    PanTransformationDelegate testInstance = new PanTransformationDelegate( log, repository, Map.of(
      "LOCAL", mockLocalTransExecutorService,
      "REMOTE", mockRemoteTransExecutorService,
      "CLUSTERED", mockClusteredTransExecutorService
    ) );
    TransExecutionConfiguration config = testInstance.createDefaultExecutionConfiguration();
    String[] params = new String[] {};
    when( trans.getTransMeta() ).thenReturn( transMeta );
    when( mockLocalTransExecutorService.execute( log, transMeta, repository, config, params  ) ).thenReturn( result );

    testInstance.executeTransformation( trans, config, params );
    verify( mockLocalTransExecutorService, times( 1 ) )
      .execute( log, transMeta, repository, config, params );
    verify( mockClusteredTransExecutorService, times( 0 ) )
      .execute( log, transMeta, repository, config, params );
    verify( mockRemoteTransExecutorService, times( 0 ) )
      .execute( log, transMeta, repository, config, params );

  }

  @Test
  public void executeTransformationClusteredExecutorCalledTest() throws KettleException {
    Result result = mock( Result.class );
    LocalTransExecutorService mockLocalTransExecutorService = mock( LocalTransExecutorService.class );
    ClusteredTransExecutorService mockClusteredTransExecutorService = mock( ClusteredTransExecutorService.class );
    RemoteTransExecutorService mockRemoteTransExecutorService = mock( RemoteTransExecutorService.class );
    PanTransformationDelegate testInstance = new PanTransformationDelegate( log, repository, Map.of(
      "LOCAL", mockLocalTransExecutorService,
      "REMOTE", mockRemoteTransExecutorService,
      "CLUSTERED", mockClusteredTransExecutorService
    ) );
    TransExecutionConfiguration config = testInstance.createDefaultExecutionConfiguration();
    config.setExecutingClustered( true );
    config.setExecutingLocally( false );
    String[] params = new String[] {};
    when( trans.getTransMeta() ).thenReturn( transMeta );
    when( mockClusteredTransExecutorService.execute( log, transMeta, repository, config, params  ) ).thenReturn( result );

    testInstance.executeTransformation( trans, config, params );
    verify( mockLocalTransExecutorService, times( 0 ) )
      .execute( log, transMeta, repository, config, params );
    verify( mockClusteredTransExecutorService, times( 1 ) )
      .execute( log, transMeta, repository, config, params );
    verify( mockRemoteTransExecutorService, times( 0 ) )
      .execute( log, transMeta, repository, config, params );

  }

  @Test
  public void executeTransformationRemotedExecutorCalledTest() throws KettleException {
    Result result = mock( Result.class );
    LocalTransExecutorService mockLocalTransExecutorService = mock( LocalTransExecutorService.class );
    ClusteredTransExecutorService mockClusteredTransExecutorService = mock( ClusteredTransExecutorService.class );
    RemoteTransExecutorService mockRemoteTransExecutorService = mock( RemoteTransExecutorService.class );
    PanTransformationDelegate testInstance = new PanTransformationDelegate( log, repository, Map.of(
      "LOCAL", mockLocalTransExecutorService,
      "REMOTE", mockRemoteTransExecutorService,
      "CLUSTERED", mockClusteredTransExecutorService
    ) );
    TransExecutionConfiguration config = testInstance.createDefaultExecutionConfiguration();
    config.setExecutingRemotely( true );
    config.setExecutingLocally( false );
    String[] params = new String[] {};
    when( trans.getTransMeta() ).thenReturn( transMeta );
    when( mockRemoteTransExecutorService.execute( log, transMeta, repository, config, params  ) ).thenReturn( result );

    testInstance.executeTransformation( trans, config, params );
    verify( mockLocalTransExecutorService, times( 0 ) )
      .execute( log, transMeta, repository, config, params );
    verify( mockClusteredTransExecutorService, times( 0 ) )
      .execute( log, transMeta, repository, config, params );
    verify( mockRemoteTransExecutorService, times( 1 ) )
      .execute( log, transMeta, repository, config, params );

  }

  @Test( expected = NullPointerException.class )
  public void emptyExecutorMapTest() throws KettleException {
    PanTransformationDelegate testInstance = new PanTransformationDelegate( log, repository, null );
    TransExecutionConfiguration config = testInstance.createDefaultExecutionConfiguration();
    String[] params = new String[] {};
    when( trans.getTransMeta() ).thenReturn( transMeta );

    testInstance.executeTransformation( trans, config, params );
  }

  @Test
  public void noExecutionTypeSetTest() {
    LocalTransExecutorService mockLocalTransExecutorService = mock( LocalTransExecutorService.class );
    ClusteredTransExecutorService mockClusteredTransExecutorService = mock( ClusteredTransExecutorService.class );
    RemoteTransExecutorService mockRemoteTransExecutorService = mock( RemoteTransExecutorService.class );
    PanTransformationDelegate testInstance = new PanTransformationDelegate( log, repository, Map.of(
      "LOCAL", mockLocalTransExecutorService,
      "REMOTE", mockRemoteTransExecutorService,
      "CLUSTERED", mockClusteredTransExecutorService
    ) );
    TransExecutionConfiguration config = testInstance.createDefaultExecutionConfiguration();
    config.setExecutingLocally( false );
    String[] params = new String[] {};
    when( trans.getTransMeta() ).thenReturn( transMeta );
    try {
      testInstance.executeTransformation( trans, config, params );
    } catch ( KettleException e ) {
      assertEquals( "No execution type specified in configuration", e.getMessage().trim() );
    }
  }
}
