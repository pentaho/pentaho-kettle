/*
 * *****************************************************************************
 *
 *  Pentaho Data Integration
 *
 *  Copyright (C) 2002-2017 by Pentaho : http://www.pentaho.com
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
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.engine.configuration.api.RunConfiguration;
import org.pentaho.di.engine.configuration.api.RunConfigurationExecutor;
import org.pentaho.di.trans.TransExecutionConfiguration;
import org.pentaho.di.trans.TransMeta;

/**
 * Created by bmorrise on 3/16/17.
 */
public class DefaultRunConfigurationExecutor implements RunConfigurationExecutor {

  @Override public void execute( RunConfiguration runConfiguration, TransExecutionConfiguration configuration,
                                 AbstractMeta meta, VariableSpace variableSpace ) {
    DefaultRunConfiguration defaultRunConfiguration = (DefaultRunConfiguration) runConfiguration;
    configuration.setExecutingLocally( defaultRunConfiguration.isLocal() );
    configuration.setExecutingRemotely( defaultRunConfiguration.isRemote() );
    configuration.setExecutingClustered( defaultRunConfiguration.isClustered() );
    if ( defaultRunConfiguration.isRemote() ) {
      configuration.setRemoteServer( meta.findSlaveServer( defaultRunConfiguration.getServer() ) );
    }
    if ( defaultRunConfiguration.isClustered() ) {
      configuration.setPassingExport( defaultRunConfiguration.isSendResources() );
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
