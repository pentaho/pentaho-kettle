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

package org.pentaho.di.pan.auth;

import com.pentaho.oauth.client.BrowserAuthSessionHolder;
import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DefaultCredentialProviderTest {

  private static final String SERVER_URL = "http://localhost:8080/pentaho";

  @Test
  public void findAccessTokenDelegatesToSessionHolder() {
    BrowserAuthSessionHolder holder = mock( BrowserAuthSessionHolder.class );
    when( holder.getOAuthAccessToken( SERVER_URL ) ).thenReturn( "access-token" );

    DefaultCredentialProvider provider = new DefaultCredentialProvider( holder );

    assertEquals( Optional.of( "access-token" ), provider.findAccessToken( SERVER_URL ) );
  }

  @Test
  public void findSessionCookieReturnsEmptyWhenHolderHasNoSessionCookie() {
    BrowserAuthSessionHolder holder = mock( BrowserAuthSessionHolder.class );
    when( holder.getSessionCookie( SERVER_URL ) ).thenReturn( null );

    DefaultCredentialProvider provider = new DefaultCredentialProvider( holder );

    assertFalse( provider.findSessionCookie( SERVER_URL ).isPresent() );
  }
}
