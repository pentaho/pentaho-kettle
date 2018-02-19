/*
 * *****************************************************************************
 *
 *  Pentaho Data Integration
 *
 *  Copyright (C) 2017-2018 by Pentaho : http://www.pentaho.com
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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.pentaho.di.base.AbstractMeta;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.trans.TransExecutionConfiguration;

import static org.junit.Assert.*;
import static org.mockito.Mockito.doReturn;

/**
 * Created by bmorrise on 3/22/17.
 */
@RunWith( MockitoJUnitRunner.class )
public class DefaultRunConfigurationExecutorTest {

  private DefaultRunConfigurationExecutor defaultRunConfigurationExecutor;

  @Mock
  private AbstractMeta abstractMeta;

  @Mock
  private VariableSpace variableSpace;

  @Mock
  private SlaveServer slaveServer;

  @Before
  public void setup() {
    defaultRunConfigurationExecutor = new DefaultRunConfigurationExecutor();
  }

  @Test
  public void testExecuteLocal() throws Exception {
    DefaultRunConfiguration defaultRunConfiguration = new DefaultRunConfiguration();
    defaultRunConfiguration.setName( "Default Configuration" );
    defaultRunConfiguration.setLocal( true );

    TransExecutionConfiguration transExecutionConfiguration = new TransExecutionConfiguration();

    defaultRunConfigurationExecutor
      .execute( defaultRunConfiguration, transExecutionConfiguration, abstractMeta, variableSpace );

    assertTrue( transExecutionConfiguration.isExecutingLocally() );
  }

  @Test
  public void testExecuteRemote() throws Exception {
    DefaultRunConfiguration defaultRunConfiguration = new DefaultRunConfiguration();
    defaultRunConfiguration.setName( "Default Configuration" );
    defaultRunConfiguration.setLocal( false );
    defaultRunConfiguration.setRemote( true );
    defaultRunConfiguration.setServer( "Test Server" );

    TransExecutionConfiguration transExecutionConfiguration = new TransExecutionConfiguration();
    doReturn( slaveServer ).when( abstractMeta ).findSlaveServer( "Test Server" );

    defaultRunConfigurationExecutor
      .execute( defaultRunConfiguration, transExecutionConfiguration, abstractMeta, variableSpace );

    assertFalse( transExecutionConfiguration.isExecutingLocally() );
    assertTrue( transExecutionConfiguration.isExecutingRemotely() );
    assertEquals( transExecutionConfiguration.getRemoteServer(), slaveServer );
  }

  @Test
  public void testSendResources() throws Exception {
    DefaultRunConfiguration defaultRunConfiguration = new DefaultRunConfiguration();
    defaultRunConfiguration.setSendResources( true );

    TransExecutionConfiguration transExecutionConfiguration = new TransExecutionConfiguration();

    defaultRunConfigurationExecutor
      .execute( defaultRunConfiguration, transExecutionConfiguration, abstractMeta, variableSpace );
    assertTrue( transExecutionConfiguration.isPassingExport() );
  }

  @Test
  public void testExecuteClustered() throws Exception {
    DefaultRunConfiguration defaultRunConfiguration = new DefaultRunConfiguration();
    defaultRunConfiguration.setName( "Default Configuration" );
    defaultRunConfiguration.setLocal( false );
    defaultRunConfiguration.setRemote( false );
    defaultRunConfiguration.setClustered( true );

    TransExecutionConfiguration transExecutionConfiguration = new TransExecutionConfiguration();

    defaultRunConfigurationExecutor
      .execute( defaultRunConfiguration, transExecutionConfiguration, abstractMeta, variableSpace );

    assertTrue( transExecutionConfiguration.isExecutingClustered() );
    assertFalse( transExecutionConfiguration.isExecutingRemotely() );
    assertFalse( transExecutionConfiguration.isExecutingLocally() );
  }

  @Test
  public void testExecuteRemoteNotFound() throws Exception {
    DefaultRunConfiguration defaultRunConfiguration = new DefaultRunConfiguration();
    defaultRunConfiguration.setName( "Default Configuration" );
    defaultRunConfiguration.setLocal( false );
    defaultRunConfiguration.setRemote( true );
    defaultRunConfiguration.setServer( "Test Server" );

    TransExecutionConfiguration transExecutionConfiguration = new TransExecutionConfiguration();
    doReturn( slaveServer ).when( abstractMeta ).findSlaveServer( null );

    try {
      defaultRunConfigurationExecutor
        .execute( defaultRunConfiguration, transExecutionConfiguration, abstractMeta, variableSpace );
      fail();
    } catch ( KettleException e ) {
      // expected
    }
  }

}
