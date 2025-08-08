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

/**
 * Utility class that demonstrates various usage patterns for the PanTransformationDelegate.
 * This class shows how to execute transformations in different modes using the delegate pattern.
 */
public class TransformationExecutionHelper {

  private static final LogChannelInterface log = new LogChannel("TransformationExecutionHelper");

  /**
   * Execute a transformation locally with basic configuration.
   */
  public static Result executeTransformationLocally(TransMeta transMeta, String[] arguments) throws KettleException {
    
    PanTransformationDelegate delegate = new PanTransformationDelegate(log);
    TransExecutionConfiguration config = PanTransformationDelegate.createDefaultExecutionConfiguration();
    
    return delegate.executeTransformation(transMeta, config, arguments);
  }

  /**
   * Execute a transformation locally with custom variables and parameters.
   */
  public static Result executeTransformationLocallyWithVariables(TransMeta transMeta, 
                                                                Map<String, String> variables,
                                                                Map<String, String> parameters,
                                                                String[] arguments) throws KettleException {
    
    PanTransformationDelegate delegate = new PanTransformationDelegate(log);
    TransExecutionConfiguration config = PanTransformationDelegate.createDefaultExecutionConfiguration();
    
    // Set custom variables
    if (variables != null) {
      config.getVariables().putAll(variables);
    }
    
    // Set custom parameters
    if (parameters != null) {
      config.getParams().putAll(parameters);
    }
    
    return delegate.executeTransformation(transMeta, config, arguments);
  }

  /**
   * Execute a transformation remotely on a specified slave server.
   */
  public static Result executeTransformationRemotely(TransMeta transMeta, 
                                                    SlaveServer slaveServer,
                                                    String[] arguments) throws KettleException {
    
    PanTransformationDelegate delegate = new PanTransformationDelegate(log);
    TransExecutionConfiguration config = PanTransformationDelegate.createRemoteExecutionConfiguration(slaveServer);
    
    return delegate.executeTransformation(transMeta, config, arguments);
  }

  /**
   * Execute a transformation in clustered mode.
   */
  public static Result executeTransformationClustered(TransMeta transMeta, 
                                                     String[] arguments) throws KettleException {
    
    PanTransformationDelegate delegate = new PanTransformationDelegate(log);
    TransExecutionConfiguration config = PanTransformationDelegate.createClusteredExecutionConfiguration();
    
    return delegate.executeTransformation(transMeta, config, arguments);
  }

  /**
   * Execute a transformation with repository context.
   */
  public static Result executeTransformationWithRepository(TransMeta transMeta,
                                                          Repository repository,
                                                          String[] arguments) throws KettleException {
    
    PanTransformationDelegate delegate = new PanTransformationDelegate(log, repository);
    TransExecutionConfiguration config = PanTransformationDelegate.createDefaultExecutionConfiguration();
    
    return delegate.executeTransformation(transMeta, config, arguments);
  }

  /**
   * Execute a transformation with custom log level and safe mode.
   */
  public static Result executeTransformationWithCustomSettings(TransMeta transMeta,
                                                              LogLevel logLevel,
                                                              boolean safeMode,
                                                              boolean gatherMetrics,
                                                              String[] arguments) throws KettleException {
    
    PanTransformationDelegate delegate = new PanTransformationDelegate(log);
    TransExecutionConfiguration config = PanTransformationDelegate.createDefaultExecutionConfiguration();
    
    // Set custom settings
    config.setLogLevel(logLevel);
    config.setSafeModeEnabled(safeMode);
    config.setGatheringMetrics(gatherMetrics);
    
    return delegate.executeTransformation(transMeta, config, arguments);
  }

  /**
   * Example of a comprehensive transformation execution with all options.
   */
  public static Result executeTransformationComprehensive(TransMeta transMeta,
                                                         Repository repository,
                                                         Map<String, String> variables,
                                                         Map<String, String> parameters,
                                                         LogLevel logLevel,
                                                         boolean safeMode,
                                                         boolean gatherMetrics,
                                                         String executionType, // "local", "remote", "clustered"
                                                         SlaveServer slaveServer, // for remote execution
                                                         String[] arguments) throws KettleException {
    
    PanTransformationDelegate delegate = new PanTransformationDelegate(log, repository);
    
    // Create appropriate execution configuration based on type
    TransExecutionConfiguration config;
    switch (executionType.toLowerCase()) {
      case "remote":
        if (slaveServer == null) {
          throw new KettleException("Slave server must be specified for remote execution");
        }
        config = PanTransformationDelegate.createRemoteExecutionConfiguration(slaveServer);
        break;
      case "clustered":
        config = PanTransformationDelegate.createClusteredExecutionConfiguration();
        break;
      case "local":
      default:
        config = PanTransformationDelegate.createDefaultExecutionConfiguration();
        break;
    }
    
    // Apply custom settings
    config.setLogLevel(logLevel);
    config.setSafeModeEnabled(safeMode);
    config.setGatheringMetrics(gatherMetrics);
    
    // Set variables and parameters
    if (variables != null) {
      config.getVariables().putAll(variables);
    }
    if (parameters != null) {
      config.getParams().putAll(parameters);
    }
    
    return delegate.executeTransformation(transMeta, config, arguments);
  }

  /**
   * Example usage method showing different execution patterns.
   */
  public static void demonstrateUsagePatterns(TransMeta transMeta) {
    
    try {
      log.logBasic("=== Demonstrating PanTransformationDelegate Usage Patterns ===");
      
      // 1. Simple local execution
      log.logBasic("1. Simple local execution");
      Result result1 = executeTransformationLocally(transMeta, new String[0]);
      log.logBasic("Local execution completed with " + result1.getNrErrors() + " errors");
      
      // 2. Local execution with variables
      log.logBasic("2. Local execution with custom variables");
      Map<String, String> variables = new HashMap<>();
      variables.put("CUSTOM_VAR", "custom_value");
      variables.put("ENVIRONMENT", "development");
      
      Map<String, String> parameters = new HashMap<>();
      parameters.put("PARAM1", "value1");
      
      Result result2 = executeTransformationLocallyWithVariables(transMeta, variables, parameters, new String[0]);
      log.logBasic("Local execution with variables completed with " + result2.getNrErrors() + " errors");
      
      // 3. Custom settings execution
      log.logBasic("3. Execution with custom settings");
      Result result3 = executeTransformationWithCustomSettings(
        transMeta, LogLevel.DEBUG, true, true, new String[0]);
      log.logBasic("Custom settings execution completed with " + result3.getNrErrors() + " errors");
      
      log.logBasic("=== All demonstrations completed ===");
      
    } catch (KettleException e) {
      log.logError("Error during demonstration", e);
    }
  }
}
