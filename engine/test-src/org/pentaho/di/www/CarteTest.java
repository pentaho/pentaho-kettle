package org.pentaho.di.www;

import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.pentaho.di.cluster.SlaveServer;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

/**
 * Created by ccaspanello on 5/31/2016.
 */
public class CarteTest {

  @Test
  public void test() throws Exception {

    System.setProperty( "pentaho.crate.detectionTimer", "100" );

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
        return new SlaveServerStatus();
      }
    } );

    Thread.sleep( 100 );
    assertEquals( carte.getWebServer().getDetections().size(), 2 );
    carte.getWebServer().stopServer();
  }
}
