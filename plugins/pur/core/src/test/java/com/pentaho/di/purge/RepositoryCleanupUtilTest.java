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

package com.pentaho.di.purge;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
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

    WebResource resource = mock( WebResource.class );
    doReturn( "true" ).when( resource ).get( String.class );

    Client client = mock( Client.class );
    doCallRealMethod().when( client ).addFilter( any( HTTPBasicAuthFilter.class ) );
    doCallRealMethod().when( client ).getHeadHandler();
    doReturn( resource ).when( client ).resource( anyString() );

    try( MockedStatic<Client> mockedClient = mockStatic( Client.class) ) {
      mockedClient.when( () -> Client.create( any( ClientConfig.class ) ) ).thenReturn( client );

      when( Client.create( any( ClientConfig.class ) ) ).thenReturn( client );
      util.authenticateLoginCredentials();

      // the expected value is: "Basic <base64 encoded username:password>"
      assertEquals( "Basic " + new String( Base64.getEncoder().encode( "admin:password".getBytes( "utf-8" ) ) ),
      getInternalState( client.getHeadHandler(), "authentication" ) );
    }
  }
}
