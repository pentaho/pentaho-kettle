/*
 * *****************************************************************************
 *
 *  Pentaho Data Integration
 *
 *  Copyright (C) 2017-2024 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.engine.configuration.impl.extension;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.di.base.AbstractMeta;
import org.pentaho.di.core.attributes.metastore.EmbeddedMetaStore;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.engine.configuration.api.RunConfiguration;
import org.pentaho.di.engine.configuration.api.RunConfigurationExecutor;
import org.pentaho.di.engine.configuration.impl.RunConfigurationManager;
import org.pentaho.di.trans.TransExecutionConfiguration;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by bmorrise on 5/4/17.
 */
@RunWith( MockitoJUnitRunner.StrictStubs.class )
public class RunConfigurationRunExtensionPointTest {

  RunConfigurationRunExtensionPoint runConfigurationRunExtensionPoint;

  @Mock private RunConfigurationManager runConfigurationManager;
  @Mock private TransExecutionConfiguration transExecutionConfiguration;
  @Mock private AbstractMeta abstractMeta;
  @Mock private VariableSpace variableSpace;
  @Mock private LogChannelInterface log;
  @Mock private EmbeddedMetaStore embeddedMetaStore;
  @Mock private RunConfiguration runConfiguration;
  @Mock private RunConfigurationExecutor runConfigurationExecutor;

  @Before
  public void setup() {
    runConfigurationRunExtensionPoint = new RunConfigurationRunExtensionPoint();
    runConfigurationRunExtensionPoint.setRunConfigurationManagerProvider ( f -> runConfigurationManager );

    when( abstractMeta.getEmbeddedMetaStore() ).thenReturn( embeddedMetaStore );
    when( transExecutionConfiguration.getRunConfiguration() ).thenReturn( "RUN_CONF" );
    when( runConfigurationManager.load( "RUN_CONF" ) ).thenReturn( runConfiguration );
    when( runConfiguration.getType() ).thenReturn( "RUN_CONF_TYPE" );
    when( runConfigurationManager.getExecutor( "RUN_CONF_TYPE" ) ).thenReturn( runConfigurationExecutor );
  }

  @Test
  public void testCallExtensionPoint() throws Exception {

    runConfigurationRunExtensionPoint.callExtensionPoint( log, new Object[] {
      transExecutionConfiguration, abstractMeta, variableSpace, null
    } );

    verify( runConfigurationExecutor )
      .execute( runConfiguration, transExecutionConfiguration, abstractMeta, variableSpace, null );
  }

  @Test
  public void testCallExtensionPointEmbedded() throws Exception {
    try {
      runConfigurationRunExtensionPoint.callExtensionPoint( log, new Object[] {
        transExecutionConfiguration, abstractMeta, variableSpace
      } );
      fail();
    } catch ( Exception e ) {
      // Should go here
    }
  }

}
