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

package org.pentaho.di.engine.configuration.impl.spark;

import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.pentaho.capabilities.api.ICapability;
import org.pentaho.capabilities.api.ICapabilityManager;
import org.pentaho.capabilities.impl.DefaultCapabilityManager;
import org.pentaho.di.base.AbstractMeta;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.engine.configuration.api.RunConfiguration;
import org.pentaho.di.engine.configuration.api.RunConfigurationExecutor;
import org.pentaho.di.trans.TransExecutionConfiguration;

import java.io.IOException;
import java.util.Dictionary;

/**
 * Created by bmorrise on 3/17/17.
 */
public class SparkRunConfigurationExecutor implements RunConfigurationExecutor {

  public static String ZOOKEEPER_CAPABILITY_ID = "aries-rsa-discovery-zookeeper";
  public static String PENTAHO_SERVER_CAPABILITY_ID = "pentaho-server";
  public static String CONFIG_KEY = "org.apache.aries.rsa.discovery.zookeeper";
  public static String JAAS_CAPABILITY_ID = "pentaho-kerberos-jaas";
  public static String AEL_SECURITY_CAPABILITY_ID = "ael-security";
  public static String DEFAULT_HOST = "127.0.0.1";
  public static String DEFAULT_PORT = "2181";

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
   * @param configuration The configuration for executing a transformation
   * @param meta Unused in this implementation
   * @param variableSpace The variableSpace used to set the engine runtime values
   */
  @Override public void execute( RunConfiguration runConfiguration, TransExecutionConfiguration configuration,
                                 AbstractMeta meta, VariableSpace variableSpace ) {

    // Check to see if the ael-security feature is installed. If it is, then install the jaas capability if it is
    // not already installed
    ICapability securityCapability = capabilityManager.getCapabilityById( AEL_SECURITY_CAPABILITY_ID );
    ICapability jaasCapability = capabilityManager.getCapabilityById( JAAS_CAPABILITY_ID  );
    if ( securityCapability != null && securityCapability.isInstalled() ) {
      if ( jaasCapability != null && !jaasCapability.isInstalled() ) {
        jaasCapability.install();
      }
    }

    // Check to see if the aries-rsa-discovery-zookeeper feature is installed and install if not
    ICapability capability = capabilityManager.getCapabilityById( ZOOKEEPER_CAPABILITY_ID );
    if ( capability != null && !capability.isInstalled() ) {
      capability.install();
    }

    // Check to verify this is running on the server or not
    if ( capabilityManager.getCapabilityById( PENTAHO_SERVER_CAPABILITY_ID ) == null ) {
      SparkRunConfiguration sparkRunConfiguration = (SparkRunConfiguration) runConfiguration;

      String[] parts = Const.NVL( sparkRunConfiguration.getUrl(), "" ).split( ":" );
      String host = parts[ 0 ];
      String port = parts.length > 1 ? parts[ 1 ] : DEFAULT_PORT;

      try {
        // Set the configuration properties for zookeepr
        Configuration zookeeperConfiguration = configurationAdmin.getConfiguration( CONFIG_KEY );
        Dictionary<String, Object> properties = zookeeperConfiguration.getProperties();
        if ( properties != null ) {
          properties.put( "zookeeper.host", Const.NVL( host, DEFAULT_HOST ) );
          properties.put( "zookeeper.port", Const.NVL( port, DEFAULT_PORT ) );
          zookeeperConfiguration.update( properties );
        }
      } catch ( IOException ioe ) {
        System.out.println( "Error occurred accessing configuration" );
      }
    }

    // Sets the appropriate variables on the transformation for the spark engine
    variableSpace.setVariable( "engine", "remote" );
    variableSpace.setVariable( "engine.remote", "spark" );
  }
}
