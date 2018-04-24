/*
 * ! ******************************************************************************
 *
 *  Pentaho Data Integration
 *
 *  Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
 *
 * ******************************************************************************
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 * *****************************************************************************
 */
package org.pentaho.di.trans.ael.websocket;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.logging.LogChannelInterfaceFactory;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.engine.api.remote.StopMessage;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMetaDataCombi;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TransWebSocketEngineAdapterTest {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();
  private LogChannelInterfaceFactory logChannelFactory = mock( LogChannelInterfaceFactory.class );
  private LogChannelInterface logChannel = mock( LogChannelInterface.class );

  @BeforeClass
  public static void init() throws Exception {
    KettleClientEnvironment.init();
    PluginRegistry.addPluginType( StepPluginType.getInstance() );
    PluginRegistry.init();
    if ( !Props.isInitialized() ) {
      Props.init( 0 );
    }
  }

  @Before
  public void setUp() throws Exception {
    KettleLogStore.setLogChannelInterfaceFactory( logChannelFactory );
    when( logChannelFactory.create( any(), any() ) ).thenReturn( logChannel );
  }

  @Test
  public void testOpsIncludeSubTrans() throws Exception {
    TransMeta transMeta = new TransMeta( getClass().getResource( "grid-to-subtrans.ktr" ).getPath() );
    TransWebSocketEngineAdapter adapter =
      new TransWebSocketEngineAdapter( transMeta, "", "", false );
    adapter.prepareExecution( new String[]{} );
    List<StepMetaDataCombi> steps = adapter.getSteps();
    steps.sort( Comparator.comparing( s -> s.stepname ) );
    assertEquals( 2, steps.size() );
    assertEquals( 0, steps.get( 0 ).step.subStatuses().size() );
    assertEquals( 2, steps.get( 1 ).step.subStatuses().size() );
  }

  @Test
  public void testSafeStopStaysRunningUntilStopped() throws Exception {
    TransMeta transMeta = new TransMeta( getClass().getResource( "grid-to-subtrans.ktr" ).getPath() );
    DaemonMessagesClientEndpoint daemonEndpoint = mock( DaemonMessagesClientEndpoint.class );
    CountDownLatch latch = new CountDownLatch( 1 );
    TransWebSocketEngineAdapter adapter =
      new TransWebSocketEngineAdapter( transMeta, "", "", false ) {
        @Override DaemonMessagesClientEndpoint getDaemonEndpoint() throws KettleException {
          return daemonEndpoint;
        }

        @Override public void waitUntilFinished() {
          try {
            latch.await( 5, TimeUnit.SECONDS );
          } catch ( InterruptedException e ) {
            fail( e.getMessage() );
          }
        }
      };
    adapter.prepareExecution( new String[]{} );
    adapter.getSteps().stream().map( stepMetaDataCombi -> stepMetaDataCombi.step )
      .forEach( step -> step.setRunning( true ) );
    adapter.safeStop();
    StopMessage.builder()
      .reasonPhrase( "User Request" )
      .safeStop( true )
      .build();
    verify( daemonEndpoint ).sendMessage( argThat( matchesSafeStop() ) );
    List<StepMetaDataCombi> steps = adapter.getSteps();
    steps.stream().map( s -> s.step ).forEach( step -> assertEquals( "Halting", step.getStatus().getDescription() ) );
    latch.countDown();
  }

  private Matcher<StopMessage> matchesSafeStop() {
    return new BaseMatcher<StopMessage>() {
      @Override public boolean matches( Object o ) {
        return ((StopMessage) o).isSafeStop();
      }

      @Override public void describeTo( Description description ) {

      }
    };
  }
}
