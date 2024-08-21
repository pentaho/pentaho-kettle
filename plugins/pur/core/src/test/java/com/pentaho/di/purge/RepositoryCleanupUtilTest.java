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

import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.junit.Test;
import org.mockito.MockedStatic;

import java.util.Base64;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.pentaho.test.util.InternalState.setInternalState;
import static org.pentaho.test.util.InternalState.getInternalState;

public class RepositoryCleanupUtilTest {

  @Test
  public void authenticateLoginCredentials() throws Exception {
    RepositoryCleanupUtil util = mock( RepositoryCleanupUtil.class );
    doCallRealMethod().when( util ).authenticateLoginCredentials();

    setInternalState( util, "url", "http://localhost:8080/pentaho" );
    setInternalState( util, "username", "admin" );
    setInternalState( util, "password", "Encrypted 2be98afc86aa7f2e4bb18bd63c99dbdde" );

    WebTarget resource = mock( WebTarget.class );
    doReturn( Response.ok("true").build() ).when( resource.request(MediaType.TEXT_PLAIN) ).get();

      jakarta.ws.rs.client.Client client = mock(jakarta.ws.rs.client.Client.class);
      HttpAuthenticationFeature feature = HttpAuthenticationFeature.basic("admin", "password");
      client.register(feature);
    doReturn( resource ).when( client ).target( anyString() );

    try( MockedStatic<ClientBuilder> mockedClient = mockStatic( ClientBuilder.class) ) {
      mockedClient.when( () -> ClientBuilder.newBuilder().build() ).thenReturn( client );
      util.authenticateLoginCredentials();

      // the expected value is: "Basic <base64 encoded username:password>"
      assertEquals( "Basic " + new String( Base64.getEncoder().encode( "admin:password".getBytes( "utf-8" ) ) ),
              getInternalState( client.getConfiguration().getProperties().get(HttpAuthenticationFeature.HTTP_AUTHENTICATION_BASIC_USERNAME), "authentication" ) );
    }
  }
}
