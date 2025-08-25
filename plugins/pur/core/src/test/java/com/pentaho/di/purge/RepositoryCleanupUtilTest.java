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


package com.pentaho.di.purge;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.junit.Test;
import org.mockito.MockedStatic;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.any;
import static org.pentaho.test.util.InternalState.setInternalState;

public class RepositoryCleanupUtilTest {

  @Test
  public void authenticateLoginCredentials() throws Exception {
    RepositoryCleanupUtil util = mock( RepositoryCleanupUtil.class );
    doCallRealMethod().when( util ).authenticateLoginCredentials();

    setInternalState( util, "url", "http://localhost:8080/pentaho" );
    setInternalState( util, "username", "admin" );
    setInternalState( util, "password", "Encrypted 2be98afc86aa7f2e4bb18bd63c99dbdde" );

    WebTarget mockTarget = mock( WebTarget.class );
    Invocation.Builder builder = mock( Invocation.Builder.class );
    when( mockTarget.request( MediaType.TEXT_PLAIN ) ).thenReturn( builder );
    when( builder.get( String.class ) ).thenReturn( "true" );

    Client mockClient = mock( Client.class );
    HttpAuthenticationFeature feature = HttpAuthenticationFeature.basic( "admin", "password" );
    mockClient.register( feature );
    when(mockClient.target( anyString() ) ).thenReturn( mockTarget );
    setInternalState( util, "client", mockClient );

    try( MockedStatic<ClientBuilder> mockedClientBuilder = mockStatic( ClientBuilder.class ) ) {
      mockedClientBuilder.when( ClientBuilder::newClient ).thenReturn( mockClient );

      util.authenticateLoginCredentials();

      verify( mockClient, times(1 )).register( any( HttpAuthenticationFeature.class ) );
    }
  }
}