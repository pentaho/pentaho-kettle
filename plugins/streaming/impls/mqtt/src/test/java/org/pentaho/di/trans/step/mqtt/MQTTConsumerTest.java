/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2019-2021 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/
package org.pentaho.di.trans.step.mqtt;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.logging.LogChannelInterfaceFactory;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith ( MockitoJUnitRunner.class )
public class MQTTConsumerTest {
  @Mock LogChannelInterfaceFactory logChannelFactory;
  @Mock LogChannelInterface logChannel;

  private Trans trans;

  @BeforeClass
  public static void setupClass() throws Exception {
    KettleClientEnvironment.init();
    PluginRegistry.addPluginType( StepPluginType.getInstance() );
    PluginRegistry.init();
    if ( !Props.isInitialized() ) {
      Props.init( 0 );
    }
    StepPluginType.getInstance().handlePluginAnnotation(
      MQTTConsumerMeta.class,
      MQTTConsumerMeta.class.getAnnotation( org.pentaho.di.core.annotations.Step.class ),
      Collections.emptyList(), false, null );
  }

  @Before
  public void setup() throws Exception {
    MockitoAnnotations.initMocks( this );
    KettleLogStore.setLogChannelInterfaceFactory( logChannelFactory );
    when( logChannelFactory.create( any(), any() ) ).thenReturn( logChannel );

    TransMeta transMeta = new TransMeta( getClass().getResource( "/ConsumeRows.ktr" ).getPath() );
    trans = new Trans( transMeta );
    trans.setVariable( "mqttServer", "127.0.0.1:1883" );
    trans.setVariable( "topic", "TestWinning" );
    trans.setVariable( "subtrans", "ConsumeRowsSubtrans.ktr" );
    trans.setVariable( "qos", "0" );
    trans.setVariable( "username", "testuser" );
  }

  @Test
  public void testHappyPath() {
    try {
      trans.prepareExecution( new String[] {} );
      assertEquals( 1, trans.getSteps().size() );
      assertEquals( "MQTT Consumer", trans.getSteps().get( 0 ).stepname );
    } catch ( KettleException e ) {
      throw new AssertionError( "Failed to initialize successfully", e );
    }
  }


}
