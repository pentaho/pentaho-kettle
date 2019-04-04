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

package org.pentaho.di.engine.configuration.impl.spark;

import org.osgi.service.cm.ConfigurationAdmin;
import org.pentaho.capabilities.api.ICapability;
import org.pentaho.capabilities.api.ICapabilityManager;
import org.pentaho.capabilities.impl.DefaultCapabilityManager;
import org.pentaho.di.ExecutionConfiguration;
import org.pentaho.di.base.AbstractMeta;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.engine.configuration.api.RunConfiguration;
import org.pentaho.di.engine.configuration.api.RunConfigurationExecutor;
import org.pentaho.di.repository.Repository;

import java.net.URI;

/**
 * Created by bmorrise on 3/17/17.
 */
public class SparkRunConfigurationExecutor implements RunConfigurationExecutor {

  public static String JAAS_CAPABILITY_ID = "pentaho-kerberos-jaas";
  public static String AEL_SECURITY_CAPABILITY_ID = "ael-security";
  public static String DEFAULT_PROTOCOL = "http";
  public static String DEFAULT_HOST = "127.0.0.1";
  public static String DEFAULT_WEBSOCKET_PORT = "53000";

  private ConfigurationAdmin configurationAdmin;
  private ICapabilityManager capabilityManager = DefaultCapabilityManager.getInstance();

  public SparkRunConfigurationExecutor( ConfigurationAdmin configurationAdmin ) {
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
                                 AbstractMeta meta, VariableSpace variableSpace, Repository repository ) {

    // Check to see if the ael-security feature is installed. If it is, then install the jaas capability if it is
    // not already installed
    ICapability securityCapability = capabilityManager.getCapabilityById( AEL_SECURITY_CAPABILITY_ID );
    ICapability jaasCapability = capabilityManager.getCapabilityById( JAAS_CAPABILITY_ID );
    if ( securityCapability != null && securityCapability.isInstalled() ) {
      if ( jaasCapability != null && !jaasCapability.isInstalled() ) {
        jaasCapability.install();
      }
    }

    SparkRunConfiguration sparkRunConfiguration = (SparkRunConfiguration) runConfiguration;

    String runConfigSchema = Const.NVL( sparkRunConfiguration.getSchema(), "" );
    String runConfigURL = Const.NVL( sparkRunConfiguration.getUrl(), "" );
    URI uri = URI.create( runConfigSchema.trim() + runConfigURL.trim() );
    String protocol = uri.getScheme();
    String host = uri.getHost();
    String port = uri.getPort() == -1 ? null : String.valueOf( uri.getPort() );

    // Variables for Websocket spark engine version
    variableSpace.setVariable( "engine.protocol", Const.NVL( protocol, DEFAULT_PROTOCOL ) );
    variableSpace.setVariable( "engine.host", Const.NVL( host, DEFAULT_HOST ) );
    variableSpace.setVariable( "engine.port", Const.NVL( port, DEFAULT_WEBSOCKET_PORT ) );

    // Sets the appropriate variables on the transformation for the spark engine
    variableSpace.setVariable( "engine", "remote" );
    variableSpace.setVariable( "engine.remote", "spark" );
  }
}
