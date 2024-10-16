/*!
 * Copyright 2010 - 2024 Hitachi Vantara.  All rights reserved.
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
    when( builder.get( String.class ) ).thenReturn("true" );

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