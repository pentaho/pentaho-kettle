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

import org.junit.Test;
import org.mockito.MockedStatic;
import org.pentaho.di.ui.spoon.session.AuthenticationContext;
import org.pentaho.di.ui.spoon.session.SpoonSessionManager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

public class SpoonCredentialProviderTest {

  @Test
  public void findAccessTokenAlwaysReturnsEmpty() {
    SpoonCredentialProvider provider = new SpoonCredentialProvider( "http://localhost:8080/pentaho" );

    assertFalse( provider.findAccessToken( "ignored" ).isPresent() );
  }

  @Test
  public void findSessionCookieReturnsJsessionIdWhenSpoonSessionIsAuthenticated() {
    SpoonCredentialProvider provider = new SpoonCredentialProvider( "http://localhost:8080/pentaho" );
    SpoonSessionManager sessionManager = mock( SpoonSessionManager.class );
    AuthenticationContext authenticationContext = mock( AuthenticationContext.class );
    when( sessionManager.getAuthenticationContext( "http://localhost:8080/pentaho" ) )
      .thenReturn( authenticationContext );
    when( authenticationContext.isAuthenticated() ).thenReturn( true );
    when( authenticationContext.getJSessionId() ).thenReturn( "ABC123" );

    try ( MockedStatic<SpoonSessionManager> mockedManager = mockStatic( SpoonSessionManager.class ) ) {
      mockedManager.when( SpoonSessionManager::getInstance ).thenReturn( sessionManager );

      assertTrue( provider.findSessionCookie( "ignored" ).isPresent() );
      assertEquals( "JSESSIONID=ABC123", provider.findSessionCookie( "ignored" ).get() );
    }
  }

  @Test
  public void findSessionCookieReturnsEmptyWhenSessionIsMissingOrBlank() {
    SpoonCredentialProvider provider = new SpoonCredentialProvider( "http://localhost:8080/pentaho" );
    SpoonSessionManager sessionManager = mock( SpoonSessionManager.class );
    AuthenticationContext authenticationContext = mock( AuthenticationContext.class );
    when( sessionManager.getAuthenticationContext( "http://localhost:8080/pentaho" ) )
      .thenReturn( authenticationContext );
    when( authenticationContext.isAuthenticated() ).thenReturn( true );
    when( authenticationContext.getJSessionId() ).thenReturn( "   " );

    try ( MockedStatic<SpoonSessionManager> mockedManager = mockStatic( SpoonSessionManager.class ) ) {
      mockedManager.when( SpoonSessionManager::getInstance ).thenReturn( sessionManager );

      assertFalse( provider.findSessionCookie( "ignored" ).isPresent() );
    }
  }

  @Test
  public void findSessionCookieReturnsEmptyWhenSessionIsNotAuthenticated() {
    SpoonCredentialProvider provider = new SpoonCredentialProvider( "http://localhost:8080/pentaho" );
    SpoonSessionManager sessionManager = mock( SpoonSessionManager.class );
    AuthenticationContext authenticationContext = mock( AuthenticationContext.class );
    when( sessionManager.getAuthenticationContext( "http://localhost:8080/pentaho" ) )
      .thenReturn( authenticationContext );
    when( authenticationContext.isAuthenticated() ).thenReturn( false );

    try ( MockedStatic<SpoonSessionManager> mockedManager = mockStatic( SpoonSessionManager.class ) ) {
      mockedManager.when( SpoonSessionManager::getInstance ).thenReturn( sessionManager );

      assertFalse( provider.findSessionCookie( "ignored" ).isPresent() );
    }
  }

  @Test
  public void findSessionCookieSwallowsSessionLookupFailures() {
    SpoonCredentialProvider provider = new SpoonCredentialProvider( "http://localhost:8080/pentaho" );
    SpoonSessionManager sessionManager = mock( SpoonSessionManager.class );
    when( sessionManager.getAuthenticationContext( "http://localhost:8080/pentaho" ) )
      .thenThrow( new RuntimeException( "boom" ) );

    try ( MockedStatic<SpoonSessionManager> mockedManager = mockStatic( SpoonSessionManager.class ) ) {
      mockedManager.when( SpoonSessionManager::getInstance ).thenReturn( sessionManager );

      assertFalse( provider.findSessionCookie( "ignored" ).isPresent() );
    }
  }
}
