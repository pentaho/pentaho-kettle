/*
 * *****************************************************************************
 *
 *  Pentaho Data Integration
 *
 *  Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.engine.configuration.impl.beam;

import org.osgi.service.cm.ConfigurationAdmin;
import org.pentaho.capabilities.api.ICapabilityManager;
import org.pentaho.capabilities.impl.DefaultCapabilityManager;
import org.pentaho.di.ExecutionConfiguration;
import org.pentaho.di.base.AbstractMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.extension.ExtensionPointHandler;
import org.pentaho.di.core.extension.KettleExtensionPoint;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.engine.configuration.api.RunConfiguration;
import org.pentaho.di.engine.configuration.api.RunConfigurationExecutor;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransExecutionConfiguration;
import org.pentaho.di.trans.TransMeta;

/**
 * Created by bmorrise on 3/17/17.
 */
public class BeamRunConfigurationExecutor implements RunConfigurationExecutor {


  private ConfigurationAdmin configurationAdmin;
  private ICapabilityManager capabilityManager = DefaultCapabilityManager.getInstance();

  public BeamRunConfigurationExecutor( ConfigurationAdmin configurationAdmin ) {
    this.configurationAdmin = configurationAdmin;
  }

  /**
   * Installs the aries-rsa-discovery-zookeeper feature if not installed and sets the host and port for zookeeper.
   * Sets the appropriate variables on the transMeta for the spark engine
   *
   * @param runConfiguration The configuration for running on Spark
   * @param configuration    The configuration for executing a transformation
   * @param meta             Unused in this implementation
   * @param variableSpace    The variableSpace used to set the engine runtime values
   */
  @Override public void execute( RunConfiguration runConfiguration, ExecutionConfiguration configuration,
                                 AbstractMeta meta, VariableSpace variableSpace, Repository repository ) throws KettleException {

    BeamRunConfiguration beamRunConfiguration = (BeamRunConfiguration) runConfiguration;

    variableSpace.setVariable( "beam.job.config", beamRunConfiguration.getBeamJobConfig());

    // Disable running in Spoon...
    //
    TransExecutionConfiguration executionConfiguration = (TransExecutionConfiguration) configuration;
    executionConfiguration.setExecutingClustered( false );
    executionConfiguration.setExecutingLocally( false );
    executionConfiguration.setExecutingRemotely( false );


    // Now allow our Beam plugin to call this point...
    //
    ExtensionPointHandler.callExtensionPoint( LogChannel.GENERAL, "BeamRunConfigurationExecution", new Object[] {
      beamRunConfiguration.getBeamJobConfig(),
      executionConfiguration,
      meta,
      variableSpace,
      repository
    });

  }
}
