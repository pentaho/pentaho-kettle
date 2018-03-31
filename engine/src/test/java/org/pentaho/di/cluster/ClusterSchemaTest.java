/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.cluster;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.www.SlaveServerDetection;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.anyObject;
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
    verify( masterDetection, never() ).setLastInactiveDate( anyObject() );

    verify( activeSlaveServer ).getStatus();
    verify( activeSlaveServerDetection, never() ).setActive( false );
    verify( activeSlaveServerDetection, never() ).setLastInactiveDate( anyObject() );

    verify( inactiveSlaveServer ).getStatus();
    verify( inactiveSlaveServerDetection ).setActive( false );
    verify( inactiveSlaveServerDetection ).setLastInactiveDate( anyObject() );
  }
}
