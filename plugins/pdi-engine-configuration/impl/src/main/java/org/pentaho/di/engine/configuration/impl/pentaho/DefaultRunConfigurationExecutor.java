/*
 * *****************************************************************************
 *
 *  Pentaho Data Integration
 *
 *  Copyright (C) 2002-2018 by Pentaho : http://www.pentaho.com
 *
 *  *******************************************************************************
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use
 *  this file except in compliance with the License. You may obtain a copy of the
 *  License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 * *****************************************************************************
 *
 */

package org.pentaho.di.engine.configuration.impl.pentaho;

import org.pentaho.di.base.AbstractMeta;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.engine.configuration.api.RunConfiguration;
import org.pentaho.di.engine.configuration.api.RunConfigurationExecutor;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransExecutionConfiguration;
import org.pentaho.di.trans.TransMeta;

/**
 * Created by bmorrise on 3/16/17.
 */
public class DefaultRunConfigurationExecutor implements RunConfigurationExecutor {

  private static Class<?> PKG = DefaultRunConfigurationExecutor.class;

  @Override
  public void execute( RunConfiguration runConfiguration, TransExecutionConfiguration configuration, AbstractMeta meta,
                       VariableSpace variableSpace ) throws KettleException {
    DefaultRunConfiguration defaultRunConfiguration = (DefaultRunConfiguration) runConfiguration;
    configuration.setExecutingLocally( defaultRunConfiguration.isLocal() );
    configuration.setExecutingRemotely( defaultRunConfiguration.isRemote() );
    configuration.setExecutingClustered( defaultRunConfiguration.isClustered() );
    if ( defaultRunConfiguration.isRemote() ) {
      SlaveServer slaveServer = meta.findSlaveServer( defaultRunConfiguration.getServer() );
      configuration.setRemoteServer( slaveServer );
      if ( slaveServer == null ) {
        String filename = "";
        if ( variableSpace instanceof TransMeta ) {
          filename = ( (TransMeta) variableSpace ).getFilename();
        }
        throw new KettleException( BaseMessages
          .getString( PKG, "DefaultRunConfigurationExecutor.RemoteNotFound.Error", filename,
            runConfiguration.getName(), "{0}", defaultRunConfiguration.getServer() ) );
      }
    }
    configuration.setPassingExport( defaultRunConfiguration.isSendResources() );
    if ( defaultRunConfiguration.isClustered() ) {
      configuration.setClusterShowingTransformation( defaultRunConfiguration.isShowTransformations() );
      configuration.setClusterPosting( defaultRunConfiguration.isClustered() );
      configuration.setClusterPreparing( defaultRunConfiguration.isClustered() );
      configuration.setClusterStarting( defaultRunConfiguration.isClustered() );
      configuration.setLogRemoteExecutionLocally( defaultRunConfiguration.isLogRemoteExecutionLocally() );
    }

    variableSpace.setVariable( "engine", null );
    variableSpace.setVariable( "engine.remote", null );
  }
}
