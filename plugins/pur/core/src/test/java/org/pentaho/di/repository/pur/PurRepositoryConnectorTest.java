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

package org.pentaho.di.repository.pur;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.util.ExecutorUtil;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;

import javax.xml.ws.WebServiceException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class PurRepositoryConnectorTest {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @BeforeClass
  public static void setUpClass() throws Exception {
    if ( !KettleEnvironment.isInitialized() ) {
      KettleEnvironment.init();
    }
  }

  @Test
  public void testPDI12439PurRepositoryConnectorDoesntNPEAfterMultipleDisconnects() {
    PurRepository mockPurRepository = mock( PurRepository.class );
    PurRepositoryMeta mockPurRepositoryMeta = mock( PurRepositoryMeta.class );
    RootRef mockRootRef = mock( RootRef.class );
    PurRepositoryConnector purRepositoryConnector =
        new PurRepositoryConnector( mockPurRepository, mockPurRepositoryMeta, mockRootRef );
    purRepositoryConnector.disconnect();
    purRepositoryConnector.disconnect();
  }

  @Test
  public void testConnect() {
    PurRepository mockPurRepository = mock( PurRepository.class );
    PurRepositoryMeta mockPurRepositoryMeta = mock( PurRepositoryMeta.class );
    PurRepositoryLocation location = mock( PurRepositoryLocation.class );
    RootRef mockRootRef = mock( RootRef.class );
    PurRepositoryConnector purRepositoryConnector =
      spy( new PurRepositoryConnector( mockPurRepository, mockPurRepositoryMeta, mockRootRef ) );
    doReturn( location ).when( mockPurRepositoryMeta ).getRepositoryLocation();
    doReturn( "" ).when( location ).getUrl();
    ExecutorService service = mock( ExecutorService.class );
    doReturn( service ).when( purRepositoryConnector ).getExecutor();
    Future future = mock( Future.class );
    try {
      doReturn( "U1" ).when( future ).get();
    } catch ( Exception e ) {
      e.printStackTrace();
    }
    Future future2 = mock( Future.class );
    try {
      doReturn( false ).when( future2 ).get();
    } catch ( Exception e ) {
      e.printStackTrace();
    }
    Future future3 = mock( Future.class );
    try {
      doReturn( null ).when( future3 ).get();
    } catch ( Exception e ) {
      e.printStackTrace();
    }
    when( service.submit( any( Callable.class ) ) ).thenReturn( future2 ).thenReturn( future3 ).thenReturn( future3 ).thenReturn( future );

    try {
      RepositoryConnectResult res = purRepositoryConnector.connect( "userNam", "password" );
      Assert.assertEquals( "U1", res.getUser().getLogin() );
    } catch ( KettleException e ) {
      e.printStackTrace();
    }
  }
}
