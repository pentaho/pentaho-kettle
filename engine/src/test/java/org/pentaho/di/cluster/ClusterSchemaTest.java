/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


package org.pentaho.di.cluster;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.www.SlaveServerDetection;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;

public class ClusterSchemaTest {

  private ClusterSchema clusterSchema;

  @Before
  public void before() {
    clusterSchema = spy( ClusterSchema.class );
  }

  @Test
  @SuppressWarnings( "ResultOfMethodCallIgnored" )
  public void testUpdateActivityStatusInGetSlaveServers() throws Exception {
    SlaveServer master = mock( SlaveServer.class );
    SlaveServer activeSlaveServer = mock( SlaveServer.class );
    SlaveServer inactiveSlaveServer = mock( SlaveServer.class );
    SlaveServerDetection masterDetection = mock( SlaveServerDetection.class );
    SlaveServerDetection activeSlaveServerDetection = mock( SlaveServerDetection.class );
    SlaveServerDetection inactiveSlaveServerDetection = mock( SlaveServerDetection.class );

    List<SlaveServer> slaveServers = new ArrayList<>();
    slaveServers.add( master );
    slaveServers.add( activeSlaveServer );
    slaveServers.add( inactiveSlaveServer );

    List<SlaveServerDetection> detections = new ArrayList<>();
    detections.add( masterDetection );
    detections.add( activeSlaveServerDetection );
    detections.add( inactiveSlaveServerDetection );

    doReturn( true ).when( clusterSchema ).isDynamic();
    doReturn( true ).when( master ).isMaster();
    doReturn( false ).when( activeSlaveServer ).isMaster();
    doReturn( false ).when( inactiveSlaveServer ).isMaster();
    doReturn( detections ).when( master ).getSlaveServerDetections();
    doReturn( master ).when( masterDetection ).getSlaveServer();
    doReturn( activeSlaveServer ).when( activeSlaveServerDetection ).getSlaveServer();
    doReturn( inactiveSlaveServer ).when( inactiveSlaveServerDetection ).getSlaveServer();
    doThrow( new Exception() ).when( inactiveSlaveServer ).getStatus();

    clusterSchema.setSlaveServers( slaveServers );
    clusterSchema.getSlaveServersFromMasterOrLocal();

    verify( master ).getStatus();
    verify( masterDetection, never() ).setActive( false );
    verify( masterDetection, never() ).setLastInactiveDate( any() );

    verify( activeSlaveServer ).getStatus();
    verify( activeSlaveServerDetection, never() ).setActive( false );
    verify( activeSlaveServerDetection, never() ).setLastInactiveDate( any() );

    verify( inactiveSlaveServer ).getStatus();
    verify( inactiveSlaveServerDetection ).setActive( false );
    verify( inactiveSlaveServerDetection ).setLastInactiveDate( any() );
  }
}
