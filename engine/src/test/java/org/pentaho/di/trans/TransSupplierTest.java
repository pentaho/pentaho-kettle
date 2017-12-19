/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.trans.ael.websocket.TransWebSocketEngineAdapter;

import java.util.Properties;
import java.util.function.Supplier;

import static org.junit.Assert.assertEquals;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith( MockitoJUnitRunner.class )
public class TransSupplierTest {

  @Mock private Trans trans;
  @Mock private TransMeta meta;
  @Mock private Supplier<Trans> fallbackSupplier;
  @Mock private LogChannelInterface log;
  @Mock private TransHopMeta transHopMeta;
  private TransSupplier transSupplier;
  private Properties props = null;

  @Before
  public void before() throws KettleException {
    props = System.getProperties();
  }

  @Test
  public void testFallback() throws KettleException {
    when( fallbackSupplier.get() ).thenReturn( trans );

    transSupplier = new TransSupplier( meta, log, fallbackSupplier );
    Trans transRet = transSupplier.get();

    verify( fallbackSupplier ).get();
    assertEquals( transRet, trans );
  }

  @Test
  public void testWebsocketVersion() throws KettleException {
    props.setProperty( "KETTLE_AEL_PDI_DAEMON_VERSION", "2.0" );
    when( meta.getVariable( "engine" ) ).thenReturn( "spark" );
    when( meta.getVariable( "engine.host" ) ).thenReturn( "hostname" );
    when( meta.getVariable( "engine.port" ) ).thenReturn( "8080" );
    when( meta.nrTransHops() ).thenReturn( 0 );
    when( meta.getTransHop( 0 ) ).thenReturn( transHopMeta );
    when( meta.realClone( false ) ).thenReturn( meta );
    when( transHopMeta.isEnabled() ).thenReturn( false );

    transSupplier = new TransSupplier( meta, log, fallbackSupplier );
    Trans transRet = transSupplier.get();

    assertTrue( transRet instanceof TransWebSocketEngineAdapter );
  }

  @Test( expected = RuntimeException.class )
  public void testInvalidEngine() throws KettleException {
    props.setProperty( "KETTLE_AEL_PDI_DAEMON_VERSION", "1.0" );
    when( meta.getVariable( "engine" ) ).thenReturn( "invalidEngine" );

    transSupplier = new TransSupplier( meta, log, fallbackSupplier );
    transSupplier.get();
  }
}
