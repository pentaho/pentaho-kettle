/*
 * *****************************************************************************
 *
 *  Pentaho Data Integration
 s *
 *  Copyright (C) 2002-2019 by Hitachi Vantara : http://www.pentaho.com
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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.pentaho.capabilities.api.ICapability;
import org.pentaho.capabilities.api.ICapabilityProvider;
import org.pentaho.capabilities.impl.DefaultCapabilityManager;
import org.pentaho.di.base.AbstractMeta;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.trans.TransExecutionConfiguration;

import java.util.Dictionary;

import static org.mockito.Mockito.*;

/**
 * Created by bmorrise on 3/22/17.
 */
@RunWith( MockitoJUnitRunner.class )
public class SparkRunConfigurationExecutorTest {

  private SparkRunConfigurationExecutor sparkRunConfigurationExecutor;

  @Mock
  private Dictionary<String, Object> properties;

  @Mock
  private AbstractMeta abstractMeta;

  @Mock
  private VariableSpace variableSpace;

  private DefaultCapabilityManager capabilityManager;
  private ICapabilityProvider capabilityProvider;

  @Before
  public void setup() throws Exception {
    Configuration configuration = mock( Configuration.class );
    ConfigurationAdmin configurationAdmin = mock( ConfigurationAdmin.class );

    doReturn( properties ).when( configuration ).getProperties();

    sparkRunConfigurationExecutor = new SparkRunConfigurationExecutor( configurationAdmin );
    capabilityProvider = mock( ICapabilityProvider.class );

    capabilityManager = DefaultCapabilityManager.getInstance();
    capabilityManager.registerCapabilityProvider( capabilityProvider );

  }

  @Test
  public void testWebSocketVersionExecute() {
    SparkRunConfiguration sparkRunConfiguration = new SparkRunConfiguration();
    sparkRunConfiguration.setName( "Spark Configuration" );
    sparkRunConfiguration.setSchema( "http://" );
    sparkRunConfiguration.setUrl( "127.0.0.2:8121" );

    TransExecutionConfiguration transExecutionConfiguration = new TransExecutionConfiguration();

    sparkRunConfigurationExecutor
      .execute( sparkRunConfiguration, transExecutionConfiguration, abstractMeta, variableSpace, null );

    verify( variableSpace ).setVariable( "engine", "remote" );
    verify( variableSpace ).setVariable( "engine.remote", "spark" );
    verify( variableSpace ).setVariable( "engine.url", "127.0.0.2:8121" );
  }

  @Test
  public void testWebSocketVersionExecuteNoPort() {
    SparkRunConfiguration sparkRunConfiguration = new SparkRunConfiguration();
    sparkRunConfiguration.setName( "Spark Configuration" );
    doReturn( "2.0" ).when( variableSpace ).getVariable( "KETTLE_AEL_PDI_DAEMON_VERSION", "2.0" );

    TransExecutionConfiguration transExecutionConfiguration = new TransExecutionConfiguration();

    sparkRunConfigurationExecutor
      .execute( sparkRunConfiguration, transExecutionConfiguration, abstractMeta, variableSpace, null );

    verify( variableSpace ).setVariable( "engine", "remote" );
    verify( variableSpace ).setVariable( "engine.remote", "spark" );
    verify( variableSpace ).setVariable( "engine.scheme", "http://" );
    verify( variableSpace ).setVariable( "engine.url", SparkRunConfigurationExecutor.DEFAULT_URL );
  }

  @Test
  public void testWssWebSocketVersionExecute() {
    SparkRunConfiguration sparkRunConfiguration = new SparkRunConfiguration();
    sparkRunConfiguration.setName( "Spark Configuration" );
    sparkRunConfiguration.setSchema( "https://" );
    sparkRunConfiguration.setUrl( "127.0.0.2:8121" );

    TransExecutionConfiguration transExecutionConfiguration = new TransExecutionConfiguration();

    sparkRunConfigurationExecutor
      .execute( sparkRunConfiguration, transExecutionConfiguration, abstractMeta, variableSpace, null );

    verify( variableSpace ).setVariable( "engine.scheme", "https://" );
    verify( variableSpace ).setVariable( "engine.url", "127.0.0.2:8121" );
  }

  @Test
  public void testUrlWssWebSocketVersionExecute() {
    SparkRunConfiguration sparkRunConfiguration = new SparkRunConfiguration();
    sparkRunConfiguration.setName( "Spark Configuration" );
    sparkRunConfiguration.setSchema( "http://" );
    sparkRunConfiguration.setUrl( "  127.0.0.2:8121  " );


    TransExecutionConfiguration transExecutionConfiguration = new TransExecutionConfiguration();

    sparkRunConfigurationExecutor
      .execute( sparkRunConfiguration, transExecutionConfiguration, abstractMeta, variableSpace, null );

    verify( variableSpace ).setVariable( "engine.scheme", "http://" );
    verify( variableSpace ).setVariable( "engine.url", "127.0.0.2:8121" );
  }

  @Test
  public void testExecuteWithAelSecurityInstalled() {
    ICapability aelSecurityCapability = mock( ICapability.class );
    setCapability( aelSecurityCapability, SparkRunConfigurationExecutor.AEL_SECURITY_CAPABILITY_ID, true );

    ICapability jaasCapability = mock( ICapability.class );
    setCapability( jaasCapability, SparkRunConfigurationExecutor.JAAS_CAPABILITY_ID, false );

    SparkRunConfiguration sparkRunConfiguration = new SparkRunConfiguration();
    sparkRunConfiguration.setName( "Spark Configuration" );

    TransExecutionConfiguration transExecutionConfiguration = new TransExecutionConfiguration();

    sparkRunConfigurationExecutor
      .execute( sparkRunConfiguration, transExecutionConfiguration, abstractMeta, variableSpace, null );

    verify( jaasCapability ).isInstalled();
    verify( jaasCapability ).install();

  }

  @Test
  public void testExecuteWithNoAelSecurityInstalled() {
    ICapability aelSecurityCapability = mock( ICapability.class );
    setCapability( aelSecurityCapability, SparkRunConfigurationExecutor.AEL_SECURITY_CAPABILITY_ID, false );

    ICapability jaasCapability = mock( ICapability.class );
    setCapability( jaasCapability, SparkRunConfigurationExecutor.JAAS_CAPABILITY_ID, false );

    SparkRunConfiguration sparkRunConfiguration = new SparkRunConfiguration();
    sparkRunConfiguration.setName( "Spark Configuration" );

    TransExecutionConfiguration transExecutionConfiguration = new TransExecutionConfiguration();

    sparkRunConfigurationExecutor
      .execute( sparkRunConfiguration, transExecutionConfiguration, abstractMeta, variableSpace, null );

    verify( jaasCapability, never() ).isInstalled();

  }

  private void setCapability( ICapability capability, String capabilityId, Object isInstalled ) {
    doReturn( capability ).when( capabilityProvider ).getCapabilityById( capabilityId );
    doReturn( isInstalled ).when( capability ).isInstalled();
  }
}
