/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/

package org.pentaho.di.trans.step.mqtt;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;

import java.util.Collections;

import static org.junit.Assert.assertEquals;

@RunWith ( MockitoJUnitRunner.class )
public class MQTTConsumerTest {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();
  private Trans trans;

  @BeforeClass
  public static void setupClass() throws Exception {
    PluginRegistry.addPluginType( StepPluginType.getInstance() );
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
