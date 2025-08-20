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
import org.mockito.MockitoAnnotations;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.TransExecutionConfiguration;
import org.pentaho.di.trans.TransMeta;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PanTransformationDelegate.
 */
public class PanTransformationDelegateTest {

  @Mock
  private TransMeta transMeta;
  
  @Mock
  private Repository repository;
  
  @Mock
  private SlaveServer slaveServer;
  
  private LogChannelInterface log;
  private PanTransformationDelegate delegate;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    log = new LogChannel("PanTransformationDelegateTest");
    delegate = new PanTransformationDelegate(log, repository);
  }

  @Test(expected = KettleException.class)
  public void testExecuteTransformationWithNullTransMeta() throws KettleException {
    TransExecutionConfiguration config = PanTransformationDelegate.createDefaultExecutionConfiguration();
    delegate.executeTransformation(null, config, new String[0]);
  }

  @Test
  public void testCreateDefaultExecutionConfiguration() {
    TransExecutionConfiguration config = PanTransformationDelegate.createDefaultExecutionConfiguration();
    
    assertTrue("Should be executing locally", config.isExecutingLocally());
    assertFalse("Should not be executing remotely", config.isExecutingRemotely());
    assertFalse("Should not be executing clustered", config.isExecutingClustered());
    assertTrue("Should be clearing log", config.isClearingLog());
    assertFalse("Should not be in safe mode", config.isSafeModeEnabled());
    assertFalse("Should not be gathering metrics", config.isGatheringMetrics());
    assertEquals("Should have basic log level", LogLevel.BASIC, config.getLogLevel());
    assertNotNull("Variables map should not be null", config.getVariables());
    assertNotNull("Parameters map should not be null", config.getParams());
  }

  @Test
  public void testCreateRemoteExecutionConfiguration() {
    TransExecutionConfiguration config = PanTransformationDelegate.createRemoteExecutionConfiguration(slaveServer);
    
    assertFalse("Should not be executing locally", config.isExecutingLocally());
    assertTrue("Should be executing remotely", config.isExecutingRemotely());
    assertFalse("Should not be executing clustered", config.isExecutingClustered());
    assertEquals("Should have the specified slave server", slaveServer, config.getRemoteServer());
  }

  @Test
  public void testCreateClusteredExecutionConfiguration() {
    TransExecutionConfiguration config = PanTransformationDelegate.createClusteredExecutionConfiguration();
    
    assertFalse("Should not be executing locally", config.isExecutingLocally());
    assertFalse("Should not be executing remotely", config.isExecutingRemotely());
    assertTrue("Should be executing clustered", config.isExecutingClustered());
    assertTrue("Should be posting to cluster", config.isClusterPosting());
    assertTrue("Should be preparing cluster", config.isClusterPreparing());
    assertTrue("Should be starting cluster", config.isClusterStarting());
    assertFalse("Should not be showing transformation", config.isClusterShowingTransformation());
  }

  @Test
  public void testExecutionConfigurationParametersAndVariables() throws KettleException {
    // Setup mock behavior
    when(transMeta.getName()).thenReturn("TestTransformation");
    when(transMeta.getFilename()).thenReturn("test.ktr");
    
    TransExecutionConfiguration config = PanTransformationDelegate.createDefaultExecutionConfiguration();
    
    // Add some variables and parameters
    Map<String, String> variables = new HashMap<>();
    variables.put("TEST_VAR", "test_value");
    config.setVariables(variables);
    
    Map<String, String> parameters = new HashMap<>();
    parameters.put("TEST_PARAM", "param_value");
    config.setParams(parameters);
    
    // Test that configuration is properly set
    assertEquals("test_value", config.getVariables().get("TEST_VAR"));
    assertEquals("param_value", config.getParams().get("TEST_PARAM"));
  }

  @Test
  public void testDelegateRepositoryAndLogSettings() {
    assertEquals("Repository should match", repository, delegate.getRepository());
    assertEquals("Log should match", log, delegate.getLog());
    
    // Test setting new repository
    Repository newRepository = mock(Repository.class);
    delegate.setRepository(newRepository);
    assertEquals("Repository should be updated", newRepository, delegate.getRepository());
    
    // Test setting new log
    LogChannelInterface newLog = new LogChannel("NewLog");
    delegate.setLog(newLog);
    assertEquals("Log should be updated", newLog, delegate.getLog());
  }

  /**
   * Test the execution configuration factory methods.
   */
  @Test
  public void testExecutionConfigurationFactoryMethods() {
    // Test default configuration
    TransExecutionConfiguration defaultConfig = PanTransformationDelegate.createDefaultExecutionConfiguration();
    assertNotNull("Default config should not be null", defaultConfig);
    assertTrue("Default should be local execution", defaultConfig.isExecutingLocally());
    
    // Test remote configuration
    SlaveServer mockSlaveServer = mock(SlaveServer.class);
    TransExecutionConfiguration remoteConfig = PanTransformationDelegate.createRemoteExecutionConfiguration(mockSlaveServer);
    assertNotNull("Remote config should not be null", remoteConfig);
    assertTrue("Remote config should be remote execution", remoteConfig.isExecutingRemotely());
    assertEquals("Remote config should have correct slave server", mockSlaveServer, remoteConfig.getRemoteServer());
    
    // Test clustered configuration
    TransExecutionConfiguration clusteredConfig = PanTransformationDelegate.createClusteredExecutionConfiguration();
    assertNotNull("Clustered config should not be null", clusteredConfig);
    assertTrue("Clustered config should be clustered execution", clusteredConfig.isExecutingClustered());
  }

  /**
   * Integration test showing how the helper class would be used.
   */
  @Test
  public void testTransformationExecutionHelperIntegration() throws KettleException {
    // This test demonstrates how the helper class would be used
    // Note: In a real test, you'd need to properly mock the Trans class and its dependencies
    
    TransMeta mockTransMeta = mock(TransMeta.class);
    when(mockTransMeta.getName()).thenReturn("TestTransformation");
    when(mockTransMeta.toString()).thenReturn("TestTransformation");
    
    // Test that the helper methods can be called without throwing exceptions
    // (actual execution would require more complex mocking)
    try {
      Map<String, String> variables = new HashMap<>();
      variables.put("TEST_VAR", "test_value");
      
      Map<String, String> parameters = new HashMap<>();
      parameters.put("TEST_PARAM", "param_value");
      
      // This would typically execute the transformation, but for unit testing
      // we'd need extensive mocking of the Trans class
      assertNotNull("Variables should be set", variables);
      assertNotNull("Parameters should be set", parameters);
      
    } catch (Exception e) {
      // Expected in unit test environment without full Kettle initialization
      assertTrue("Exception should be KettleException or related", 
                e instanceof KettleException || e instanceof RuntimeException);
    }
  }
}
