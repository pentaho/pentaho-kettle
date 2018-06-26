/*
 * *****************************************************************************
 *
 *  Pentaho Data Integration
 *
 *  Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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
package org.pentaho.di.engine.configuration.impl.dataflow;

import org.osgi.service.cm.ConfigurationAdmin;
import org.pentaho.di.ExecutionConfiguration;
import org.pentaho.di.base.AbstractMeta;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.engine.configuration.api.RunConfiguration;
import org.pentaho.di.engine.configuration.api.RunConfigurationExecutor;
import org.pentaho.di.repository.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by ccaspanello on 6/13/18.
 */
public class DataflowRunConfigurationExecutor implements RunConfigurationExecutor {

  private static final Logger LOG = LoggerFactory.getLogger( DataflowRunConfigurationExecutor.class );
  private ConfigurationAdmin configurationAdmin;

  public DataflowRunConfigurationExecutor( ConfigurationAdmin configurationAdmin ) {
    this.configurationAdmin = configurationAdmin;
  }

  @Override
  public void execute( RunConfiguration runConfiguration, ExecutionConfiguration configuration,
                       AbstractMeta meta, VariableSpace variableSpace, Repository repository ) {

    LOG.error( "Executing on Dataflow" );

    DataflowRunConfiguration config = (DataflowRunConfiguration) runConfiguration;

    variableSpace.setVariable( "engine", "remote" );
    variableSpace.setVariable( "engine.remote", "dataflow" );

    variableSpace.setVariable( "engine.application.jar", config.getApplicationJar() );
    variableSpace.setVariable( "engine.runner", config.getRunner() );
  }
}
