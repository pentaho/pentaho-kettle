/*
 * *****************************************************************************
 *
 *  Pentaho Data Integration
 *
 *  Copyright (C) 2002-2022 by Hitachi Vantara : http://www.pentaho.com
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

import org.pentaho.di.ExecutionConfiguration;
import org.pentaho.di.base.AbstractMeta;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.engine.configuration.api.RunConfiguration;
import org.pentaho.di.engine.configuration.api.RunConfigurationExecutor;
import org.pentaho.di.repository.Repository;

/**
 * Created by bmorrise on 3/17/17.
 */
public class SparkRunConfigurationExecutor implements RunConfigurationExecutor {

  public static String DEFAULT_SCHEMA = "http";
  public static String DEFAULT_URL = "127.0.0.1:53000";

  private static SparkRunConfigurationExecutor instance;

  public static SparkRunConfigurationExecutor getInstance() {
    if ( null == instance ) {
      instance = new SparkRunConfigurationExecutor();
    }
    return instance;
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

    SparkRunConfiguration sparkRunConfiguration = (SparkRunConfiguration) runConfiguration;

    String runConfigSchema = Const.NVL( sparkRunConfiguration.getSchema(), "" );
    String runConfigURL = Const.NVL( sparkRunConfiguration.getUrl(), "" );


    // Variables for Websocket spark engine version
    String engineScheme = Const.NVL( runConfigSchema, DEFAULT_SCHEMA ).trim();
    String engineUrl = Const.NVL( runConfigURL, DEFAULT_URL ).trim();

    variableSpace.setVariable( "engine.scheme", engineScheme );
    variableSpace.setVariable( "engine.url", engineUrl );

    // Sets the appropriate variables on the transformation for the spark engine
    variableSpace.setVariable( "engine", "remote" );
    variableSpace.setVariable( "engine.remote", "spark" );
  }
}
