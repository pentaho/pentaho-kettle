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

package org.pentaho.di.engine.configuration.impl.pentaho;

import org.pentaho.di.ExecutionConfiguration;
import org.pentaho.di.base.AbstractMeta;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.engine.configuration.api.RunConfiguration;
import org.pentaho.di.engine.configuration.api.RunConfigurationExecutor;
import org.pentaho.di.engine.configuration.impl.pentaho.scheduler.SchedulerRequest;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.JobExecutionConfiguration;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.DefaultTransSupplier;
import org.pentaho.di.trans.TransExecutionConfiguration;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.ui.spoon.Spoon;

/**
 * Created by bmorrise on 3/16/17.
 */
public class DefaultRunConfigurationExecutor implements RunConfigurationExecutor {

  private static Class<?> PKG = DefaultRunConfigurationExecutor.class;
  private static DefaultRunConfigurationExecutor instance;

  public static DefaultRunConfigurationExecutor getInstance() {
    if ( null == instance ) {
      instance = new DefaultRunConfigurationExecutor();
    }
    return instance;
  }

  @Override
  public void execute( RunConfiguration runConfiguration, ExecutionConfiguration executionConfiguration,
                       AbstractMeta meta, VariableSpace variableSpace, Repository repository ) throws KettleException {
    DefaultRunConfiguration defaultRunConfiguration = (DefaultRunConfiguration) runConfiguration;
    if ( executionConfiguration instanceof TransExecutionConfiguration ) {
      configureTransExecution( (TransExecutionConfiguration) executionConfiguration, defaultRunConfiguration,
        variableSpace, meta, repository );
    }

    if ( executionConfiguration instanceof JobExecutionConfiguration ) {
      configureJobExecution( (JobExecutionConfiguration) executionConfiguration, defaultRunConfiguration, variableSpace,
        meta, repository );
    }

    variableSpace.setVariable( "engine", null );
    variableSpace.setVariable( "engine.remote", null );
    variableSpace.setVariable( "engine.scheme", null );
    variableSpace.setVariable( "engine.url", null );
    meta.setTransSupplier( new DefaultTransSupplier() );
  }

  private void configureTransExecution( TransExecutionConfiguration transExecutionConfiguration,
                                        DefaultRunConfiguration defaultRunConfiguration, VariableSpace variableSpace,
                                        AbstractMeta meta, Repository repository ) throws KettleException {
    transExecutionConfiguration.setExecutingLocally( defaultRunConfiguration.isLocal() );
    transExecutionConfiguration.setExecutingRemotely( defaultRunConfiguration.isRemote() );
    transExecutionConfiguration.setExecutingClustered( defaultRunConfiguration.isClustered() );
    if ( defaultRunConfiguration.isRemote() ) {
      setSlaveServer( transExecutionConfiguration, meta, defaultRunConfiguration, variableSpace );
    }
    transExecutionConfiguration.setPassingExport( defaultRunConfiguration.isSendResources() );
    if ( defaultRunConfiguration.isClustered() ) {
      transExecutionConfiguration.setClusterShowingTransformation( defaultRunConfiguration.isShowTransformations() );
      transExecutionConfiguration.setClusterPosting( defaultRunConfiguration.isClustered() );
      transExecutionConfiguration.setClusterPreparing( defaultRunConfiguration.isClustered() );
      transExecutionConfiguration.setClusterStarting( defaultRunConfiguration.isClustered() );
      transExecutionConfiguration.setLogRemoteExecutionLocally( defaultRunConfiguration.isLogRemoteExecutionLocally() );
    }
    if ( defaultRunConfiguration.isPentaho() && repository != null ) {
      sendNow( repository, (AbstractMeta) variableSpace );
    }
  }

  private void configureJobExecution( JobExecutionConfiguration jobExecutionConfiguration,
                                      DefaultRunConfiguration defaultRunConfiguration, VariableSpace variableSpace,
                                      AbstractMeta meta, Repository repository ) throws KettleException {
    jobExecutionConfiguration.setExecutingLocally( defaultRunConfiguration.isLocal() );
    jobExecutionConfiguration.setExecutingRemotely( defaultRunConfiguration.isRemote() );
    if ( defaultRunConfiguration.isRemote() ) {
      setSlaveServer( jobExecutionConfiguration, meta, defaultRunConfiguration, variableSpace );
    }
    jobExecutionConfiguration.setPassingExport( defaultRunConfiguration.isSendResources() );
    if ( defaultRunConfiguration.isPentaho() && repository != null ) {
      sendNow( repository, (AbstractMeta) variableSpace );
    }
  }

  private void setSlaveServer( ExecutionConfiguration executionConfiguration, AbstractMeta meta,
                               DefaultRunConfiguration defaultRunConfiguration,
                               VariableSpace variableSpace ) throws KettleException {
    SlaveServer slaveServer = meta.findSlaveServer( defaultRunConfiguration.getServer() );
    executionConfiguration.setRemoteServer( slaveServer );
    if ( slaveServer == null ) {
      String filename = "";
      if ( variableSpace instanceof AbstractMeta ) {
        filename = ( (AbstractMeta) variableSpace ).getFilename();
      }
      throw new KettleException( BaseMessages
        .getString( PKG, "DefaultRunConfigurationExecutor.RemoteNotFound.Error", filename,
          defaultRunConfiguration.getName(), "{0}", defaultRunConfiguration.getServer() ) );
    }
  }

  private void sendNow( Repository repository, AbstractMeta meta ) {
    try {
      if ( meta instanceof TransMeta ) {
        Spoon.getInstance().getActiveTransGraph().handleTransMetaChanges( (TransMeta) meta );
      } else {
        Spoon.getInstance().getActiveJobGraph().handleJobMetaChanges( (JobMeta) meta );
      }
    } catch ( Exception e ) {
      // Ignore an exception if occurs
    }

    if ( !meta.hasChanged() ) {
      SchedulerRequest.Builder builder = new SchedulerRequest.Builder();
      builder.repository( repository );
      SchedulerRequest schedulerRequest = builder.build();
      schedulerRequest.submit( meta );
    }
  }
}
