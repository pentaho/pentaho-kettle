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

import org.pentaho.di.core.Const;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.extension.ExtensionPointHandler;
import org.pentaho.di.core.extension.KettleExtensionPoint;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.metastore.MetaStoreConst;
import org.pentaho.di.pan.executors.ClusteredTransExecutorService;
import org.pentaho.di.pan.executors.LocalTransExecutorService;
import org.pentaho.di.pan.executors.RemoteTransExecutorService;
import org.pentaho.di.pan.executors.TransExecutorService;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransExecutionConfiguration;
import org.pentaho.di.trans.TransMeta;

import java.util.HashMap;
import java.util.Map;

/**
 * Delegate class for handling transformation execution in command-line contexts (Pan).
 * This class centralizes the execution logic similar to SpoonTransformationDelegate
 * but is designed for non-UI execution environments.
 */
public class PanTransformationDelegate {

  private static Class<?> pkg = PanTransformationDelegate.class;

  private LogChannelInterface log;
  private Repository repository;

  private static final String LOCAL = "LOCAL";
  private static final String REMOTE = "REMOTE";
  private static final String CLUSTERED = "CLUSTERED";

  private Map<String, TransExecutorService> transformationExecutorServiceMap;

  public PanTransformationDelegate( LogChannelInterface log ) {
    this( log, null );
  }

  public PanTransformationDelegate( LogChannelInterface log, Repository repository ) {
    this( log, repository, Map.of(
      LOCAL, new LocalTransExecutorService(),
      REMOTE, new RemoteTransExecutorService(),
      CLUSTERED, new ClusteredTransExecutorService()
    ) );
  }

  public PanTransformationDelegate( LogChannelInterface log, Repository repository, Map<String,
    TransExecutorService> map ) {
    this.log = log;
    this.repository = repository;
    this.transformationExecutorServiceMap = map;
  }

  /**
   * Execute a transformation with the specified configuration.
   *
   * @param trans              the transformation
   * @param executionConfiguration the execution configuration
   * @param arguments              command line arguments
   * @return the execution result
   * @throws KettleException if execution fails
   */
  public Result executeTransformation( final Trans trans,
                                       final TransExecutionConfiguration executionConfiguration,
                                       final String[] arguments ) throws KettleException {

    TransMeta transMeta = trans.getTransMeta();
    if ( transMeta == null ) {
      throw new KettleException( BaseMessages.getString( pkg, "PanTransformationDelegate.Error.TransMetaNull" ) );
    }

    // Set repository and metastore information in both the exec config and the metadata
    transMeta.setRepository( repository );
    transMeta.setMetaStore( MetaStoreConst.getDefaultMetastore() );
    executionConfiguration.setRepository( repository );

    // Set the run options
    transMeta.setClearingLog( executionConfiguration.isClearingLog() );
    transMeta.setSafeModeEnabled( executionConfiguration.isSafeModeEnabled() );
    transMeta.setGatheringMetrics( executionConfiguration.isGatheringMetrics() );

    // Call extension points
    ExtensionPointHandler.callExtensionPoint( log, KettleExtensionPoint.SpoonTransMetaExecutionStart.id, transMeta );
    ExtensionPointHandler.callExtensionPoint( log, KettleExtensionPoint.SpoonTransExecutionConfiguration.id, executionConfiguration );

    try {
      ExtensionPointHandler.callExtensionPoint( log, KettleExtensionPoint.SpoonTransBeforeStart.id, new Object[] {
        executionConfiguration, transMeta, transMeta, repository
      } );
    } catch ( KettleException e ) {
      log.logError( e.getMessage(), transMeta.getFilename() );
      throw e;
    }

    // Apply parameters
    Map<String, String> paramMap = executionConfiguration.getParams();
    for ( Map.Entry<String, String> entry : paramMap.entrySet() ) {
      transMeta.setParameterValue( entry.getKey(), Const.NVL( entry.getValue(), "" ) );
    }
    transMeta.activateParameters();

    // Set the log level
    if ( executionConfiguration.getLogLevel() != null ) {
      transMeta.setLogLevel( executionConfiguration.getLogLevel() );
    }

    // Determine execution type and execute accordingly
    return executeBasedOnConfiguration( transMeta, executionConfiguration, arguments );
  }

  /**
   * Execute transformation based on the execution configuration type.
   */
  private Result executeBasedOnConfiguration( TransMeta transMeta,
                                              TransExecutionConfiguration executionConfiguration,
                                              String[] arguments ) throws KettleException {

    if ( executionConfiguration.isExecutingLocally() ) {
      return transformationExecutorServiceMap.get( LOCAL )
        .execute( log, transMeta, repository, executionConfiguration, arguments );
    } else if ( executionConfiguration.isExecutingRemotely() ) {
      return transformationExecutorServiceMap.get( REMOTE )
        .execute( log, transMeta, repository, executionConfiguration, arguments );
    } else if ( executionConfiguration.isExecutingClustered() ) {
      return transformationExecutorServiceMap.get( CLUSTERED )
        .execute( log, transMeta, repository, executionConfiguration, arguments );

    } else {
      throw new KettleException( BaseMessages.getString( pkg, "PanTransformationDelegate.Error.NoExecutionTypeSpecified" ) );
    }
  }

  /**
   * Create a default execution configuration for command-line execution.
   */
  public TransExecutionConfiguration createDefaultExecutionConfiguration() {
    TransExecutionConfiguration config = new TransExecutionConfiguration();

    // Set defaults for command-line execution
    config.setExecutingLocally( true );
    config.setExecutingRemotely( false );
    config.setExecutingClustered( false );
    config.setClearingLog( true );
    config.setSafeModeEnabled( false );
    config.setGatheringMetrics( false );
    config.setLogLevel( LogLevel.BASIC );

    // Initialize empty collections
    config.setVariables( new HashMap<>() );
    config.setParams( new HashMap<>() );

    return config;
  }

  // Getters and setters
  public LogChannelInterface getLog() {
    return log;
  }

  public void setLog( LogChannelInterface log ) {
    this.log = log;
  }

  public Repository getRepository() {
    return repository;
  }

  public void setRepository( Repository repository ) {
    this.repository = repository;
  }
}
