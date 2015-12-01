/*!
* Copyright 2010 - 2015 Pentaho Corporation.  All rights reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*
*/

package org.pentaho.di.repository.pur;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.exception.KettleRepositoryStatusException;
import org.pentaho.platform.api.engine.IServerStatusProvider;

public class PurRepositoryConnectorTest {
  private PurRepositoryConnector purRepositoryConnector;
  private PurRepositoryMeta mockPurRepositoryMeta;

  @Before
  public void setUp() throws Exception {
    PurRepository mockPurRepository = mock( PurRepository.class );
    mockPurRepositoryMeta = mock( PurRepositoryMeta.class );
    RootRef mockRootRef = mock( RootRef.class );
    purRepositoryConnector = spy( new PurRepositoryConnector( mockPurRepository, mockPurRepositoryMeta, mockRootRef ) );
  }

  @Test
  public void testPDI12439PurRepositoryConnectorDoesntNPEAfterMultipleDisconnects() {
    purRepositoryConnector.disconnect();
    purRepositoryConnector.disconnect();
  }

  @Test
  public void testCheckServerStatus() throws Exception {
    final PurRepositoryLocation purRepositoryLocation = mock( PurRepositoryLocation.class );
    when( mockPurRepositoryMeta.getRepositoryLocation() ).thenReturn( purRepositoryLocation );
    final String testRepoName = "testRepoName";
    when( mockPurRepositoryMeta.getName() ).thenReturn( testRepoName );
    final String testBaseUrl = "testBaseUrl";
    when( purRepositoryLocation.getUrl() ).thenReturn( testBaseUrl );

    final WebResource resource = mock( WebResource.class );

    final Client client = mock( Client.class );
    when( purRepositoryConnector.getClient() ).thenReturn( client );
    when( client.resource( testBaseUrl + PurRepositoryConnector.SERVER_STATUS_ENDPOINT ) ).thenReturn( resource );

    doCallRealMethod().when( purRepositoryConnector ).checkServerStatus();

    // test starting
    when( resource.get( String.class ) ).thenReturn( IServerStatusProvider.ServerStatus.STARTING.toString() );
    try {
      purRepositoryConnector.checkServerStatus();
      fail();
    } catch ( KettleRepositoryStatusException e ) {
      assertEquals( testRepoName, e.getRepositoryName() );
    }

    // test started
    when( resource.get( String.class ) ).thenReturn( IServerStatusProvider.ServerStatus.STARTED.toString() );
    try {
      purRepositoryConnector.checkServerStatus();
    } catch ( KettleRepositoryStatusException e ) {
      fail();
    }
  }
}
