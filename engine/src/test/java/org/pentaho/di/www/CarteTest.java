/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2016 - 2017 by Hitachi Vantara : http://www.pentaho.com
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
package org.pentaho.di.www;

import org.junit.Ignore;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.Const;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.RETURNS_MOCKS;
import static org.mockito.Mockito.when;

/**
 * Created by ccaspanello on 5/31/2016.
 */
public class CarteTest {

  // this test isn't consistent/doesn't work.
  @Ignore
  @Test
  public void test() throws Exception {

    CountDownLatch latch = new CountDownLatch( 1 );

    System.setProperty( Const.KETTLE_SLAVE_DETECTION_TIMER, "100" );

    SlaveServer master = new SlaveServer();
    master.setHostname( "127.0.0.1" );
    master.setPort( "9000" );
    master.setUsername( "cluster" );
    master.setPassword( "cluster" );
    master.setMaster( true );

    SlaveServerConfig config = new SlaveServerConfig();
    config.setSlaveServer( master );

    Carte carte = new Carte( config );

    SlaveServerDetection slaveServerDetection = mock( SlaveServerDetection.class );
    carte.getWebServer().getDetections().add( slaveServerDetection );

    SlaveServer slaveServer = mock( SlaveServer.class, RETURNS_MOCKS );
    when( slaveServerDetection.getSlaveServer() ).thenReturn( slaveServer );
    when( slaveServer.getStatus() ).thenAnswer( new Answer<SlaveServerStatus>() {
      @Override public SlaveServerStatus answer( InvocationOnMock invocation ) throws Throwable {
        SlaveServerDetection anotherDetection = mock( SlaveServerDetection.class );
        carte.getWebServer().getDetections().add( anotherDetection );
        latch.countDown();
        return new SlaveServerStatus();
      }
    } );

    latch.await( 10, TimeUnit.SECONDS );
    assertEquals( carte.getWebServer().getDetections().size(), 2 );
    carte.getWebServer().stopServer();
  }
}
