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

package org.pentaho.di.cli.auth;

import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.mockito.ArgumentCaptor;
import org.pentaho.di.cli.auth.store.StoredCredential;
import org.pentaho.di.cli.auth.store.TokenStore;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class BrowserAuthSessionHolderTest {

  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  private static final String SERVER_URL = "http://localhost:8080/pentaho";
  private static final String USERNAME = "alice";
  private static final String BEARER = "Bearer";
  private static final String ACCESS_TOKEN = "access-token";
  private static final String IDP_REGISTRATION_ID = "azure";
  private static final String SESSION_TOKEN = "JSESSIONID";
  private static final String SESSION_COOKIE = "JSESSIONID=abc; Path=/pentaho";

  @Test
  public void constructorRestoresExpiredOAuthSessionWhenRefreshHandleExists() {
    TokenStore tokenStore = mock( TokenStore.class );
    when( tokenStore.load() ).thenReturn( Optional.of( StoredCredential.builder()
      .serverUrl( SERVER_URL )
      .username( USERNAME )
      .oauthAccessToken( "expired-access" )
      .oauthTokenType( BEARER )
      .oauthTokenExpiry( System.currentTimeMillis() - TimeUnit.MINUTES.toMillis( 1 ) )
      .oauthRefreshHandle( "refresh-handle" )
      .oauthBrokerAuthHandle( "broker-auth-handle" )
      .build() ) );

    BrowserAuthSessionHolder holder = new BrowserAuthSessionHolder( tokenStore );

    assertTrue( holder.hasValidSession( SERVER_URL ) );
    assertNull( holder.getOAuthAccessToken( SERVER_URL ) );
    assertTrue( holder.isOAuthTokenExpiredButRefreshable( SERVER_URL ) );
    assertEquals( "refresh-handle", holder.getRefreshHandle( SERVER_URL ) );
    assertEquals( "broker-auth-handle", holder.getBrokerAuthHandle( SERVER_URL ) );
    verify( tokenStore, never() ).delete();
  }

  @Test
  public void storeOAuthTokenPersistsBearerTokenUsingJwtExpiry() {
    TokenStore tokenStore = mock( TokenStore.class );
    when( tokenStore.load() ).thenReturn( Optional.empty() );

    BrowserAuthSessionHolder holder = new BrowserAuthSessionHolder( tokenStore );
    long expEpochSeconds = TimeUnit.MILLISECONDS.toSeconds( System.currentTimeMillis() ) + 600;

    holder.storeOAuthToken( new BrowserAuthSessionHolder.OAuthTokenData(
      SERVER_URL,
      ACCESS_TOKEN,
      "refresh-token",
      null,
      IDP_REGISTRATION_ID,
      60,
      expEpochSeconds,
      USERNAME ) );

    ArgumentCaptor<StoredCredential> credentialCaptor = ArgumentCaptor.forClass( StoredCredential.class );
    verify( tokenStore ).save( credentialCaptor.capture() );

    StoredCredential credential = credentialCaptor.getValue();
    assertEquals( SERVER_URL, credential.serverUrl() );
    assertEquals( ACCESS_TOKEN, credential.oauthAccessToken() );
    assertEquals( "refresh-token", credential.oauthRefreshToken() );
    assertEquals( BEARER, credential.oauthTokenType() );
    assertEquals( IDP_REGISTRATION_ID, credential.oauthIdpRegistrationId() );
    assertEquals( expEpochSeconds * 1000L, credential.oauthTokenExpiry() );
    assertEquals( USERNAME, credential.username() );
    assertEquals( ACCESS_TOKEN, holder.getOAuthAccessToken( SERVER_URL ) );
    assertTrue( holder.hasValidOAuthToken( SERVER_URL ) );
  }

  @Test
  public void storeOAuthTokenIgnoresEmptyAccessToken() {
    TokenStore tokenStore = mock( TokenStore.class );
    when( tokenStore.load() ).thenReturn( Optional.empty() );

    BrowserAuthSessionHolder holder = new BrowserAuthSessionHolder( tokenStore );

    holder.storeOAuthToken( new BrowserAuthSessionHolder.OAuthTokenData(
      SERVER_URL,
      "",
      "refresh-token",
      BEARER,
      IDP_REGISTRATION_ID,
      60,
      0,
      USERNAME ) );

    verify( tokenStore, never() ).save( org.mockito.ArgumentMatchers.any() );
    assertNull( holder.getOAuthAccessToken( SERVER_URL ) );
  }

  @Test
  public void clearOAuthTokenKeepsBrowserSessionWhenCookieFieldsExist() {
    TokenStore tokenStore = mock( TokenStore.class );
    when( tokenStore.load() ).thenReturn( Optional.empty() );

    BrowserAuthSessionHolder holder = new BrowserAuthSessionHolder( tokenStore );
    holder.storeSession( SERVER_URL, SESSION_TOKEN, SESSION_COOKIE, USERNAME );
    holder.storeOAuthToken( new BrowserAuthSessionHolder.OAuthTokenData(
      SERVER_URL,
      ACCESS_TOKEN,
      null,
      BEARER,
      IDP_REGISTRATION_ID,
      300,
      0,
      USERNAME ) );

    reset( tokenStore );

    holder.clearOAuthToken( SERVER_URL );

    ArgumentCaptor<StoredCredential> credentialCaptor = ArgumentCaptor.forClass( StoredCredential.class );
    verify( tokenStore ).save( credentialCaptor.capture() );
    verify( tokenStore, never() ).delete();

    StoredCredential credential = credentialCaptor.getValue();
    assertEquals( SESSION_TOKEN, holder.getSessionToken( SERVER_URL ) );
    assertEquals( SESSION_COOKIE, holder.getSessionCookie( SERVER_URL ) );
    assertNull( holder.getOAuthAccessToken( SERVER_URL ) );
    assertEquals( SESSION_TOKEN, credential.sessionToken() );
    assertNull( credential.oauthAccessToken() );
    assertNull( credential.oauthRefreshHandle() );
  }

  @Test
  public void clearOAuthTokenDeletesPersistedCredentialWhenNoBrowserSessionRemains() {
    TokenStore tokenStore = mock( TokenStore.class );
    when( tokenStore.load() ).thenReturn( Optional.empty() );

    BrowserAuthSessionHolder holder = new BrowserAuthSessionHolder( tokenStore );
    holder.storeOAuthToken( new BrowserAuthSessionHolder.OAuthTokenData(
      SERVER_URL,
      ACCESS_TOKEN,
      null,
      BEARER,
      IDP_REGISTRATION_ID,
      300,
      0,
      USERNAME ) );

    reset( tokenStore );

    holder.clearOAuthToken( SERVER_URL );

    verify( tokenStore ).delete();
    assertFalse( holder.hasValidSession( SERVER_URL ) );
    assertNull( holder.getOAuthAccessToken( SERVER_URL ) );
  }

  @Test
  public void storeSessionTracksCookieOnlySessionWithoutExpiry() {
    TokenStore tokenStore = mock( TokenStore.class );
    when( tokenStore.load() ).thenReturn( Optional.empty() );

    BrowserAuthSessionHolder holder = new BrowserAuthSessionHolder( tokenStore );

    holder.storeSession( SERVER_URL, null, SESSION_COOKIE, USERNAME, null );

    assertTrue( holder.hasValidSession( SERVER_URL + "/" ) );
    assertEquals( SESSION_COOKIE, holder.getSessionCookie( SERVER_URL ) );
    assertNull( holder.getSessionToken( SERVER_URL ) );
    assertEquals( USERNAME, holder.getSessionUsername( SERVER_URL ) );
  }

  @Test
  public void storeSessionTreatsInvalidExpiryAsUnknownInsteadOfExpired() {
    TokenStore tokenStore = mock( TokenStore.class );
    when( tokenStore.load() ).thenReturn( Optional.empty() );

    BrowserAuthSessionHolder holder = new BrowserAuthSessionHolder( tokenStore );

    holder.storeSession( SERVER_URL, SESSION_TOKEN, SESSION_COOKIE, USERNAME, "not-a-number" );

    assertTrue( holder.hasValidSession( SERVER_URL ) );
    assertEquals( SESSION_COOKIE, holder.getSessionCookie( SERVER_URL ) );
  }

  @Test
  public void storeSessionRejectsExpiredServerMismatchAndClearSessionFieldsKeepsOAuth() {
    TokenStore tokenStore = mock( TokenStore.class );
    when( tokenStore.load() ).thenReturn( Optional.empty() );

    BrowserAuthSessionHolder holder = new BrowserAuthSessionHolder( tokenStore );
    holder.storeSession( SERVER_URL, SESSION_TOKEN, SESSION_COOKIE, USERNAME,
      String.valueOf( System.currentTimeMillis() - TimeUnit.MINUTES.toMillis( 1 ) ) );

    assertFalse( holder.hasValidSession( SERVER_URL ) );
    assertNull( holder.getSessionCookie( SERVER_URL ) );

    holder.storeOAuthToken( new BrowserAuthSessionHolder.OAuthTokenData(
      SERVER_URL,
      ACCESS_TOKEN,
      "refresh-token",
      null,
      IDP_REGISTRATION_ID,
      300,
      0,
      USERNAME ) );
    holder.clearSessionFields( SERVER_URL );

    assertTrue( holder.hasValidSession( SERVER_URL ) );
    assertEquals( ACCESS_TOKEN, holder.getOAuthAccessToken( SERVER_URL ) );
    assertNull( holder.getSessionToken( SERVER_URL ) );
    assertNull( holder.getSessionCookie( SERVER_URL ) );
    assertFalse( holder.hasValidSession( "http://other-host/pentaho" ) );
  }

  @Test
  public void updateOAuthTokenPreservesRefreshTokenAndUsesJwtExpiryWhenProvided() {
    TokenStore tokenStore = mock( TokenStore.class );
    when( tokenStore.load() ).thenReturn( Optional.empty() );

    BrowserAuthSessionHolder holder = new BrowserAuthSessionHolder( tokenStore );
    holder.storeOAuthToken( new BrowserAuthSessionHolder.OAuthTokenData(
      SERVER_URL,
      ACCESS_TOKEN,
      "refresh-token",
      BEARER,
      IDP_REGISTRATION_ID,
      60,
      0,
      USERNAME ) );

    long expEpochSeconds = TimeUnit.MILLISECONDS.toSeconds( System.currentTimeMillis() ) + 120;
    holder.updateOAuthToken( SERVER_URL, "new-access-token", null, 30, expEpochSeconds );

    assertEquals( "new-access-token", holder.getOAuthAccessToken( SERVER_URL ) );
    assertEquals( "refresh-token", holder.getOAuthRefreshToken( SERVER_URL ) );
    assertEquals( BEARER, holder.getOAuthTokenType( SERVER_URL ) );
    assertEquals( IDP_REGISTRATION_ID, holder.getOAuthIdpRegistrationId( SERVER_URL ) );
  }

  @Test
  public void updateOAuthTokenWithoutSessionDoesNothing() {
    TokenStore tokenStore = mock( TokenStore.class );
    when( tokenStore.load() ).thenReturn( Optional.empty() );

    BrowserAuthSessionHolder holder = new BrowserAuthSessionHolder( tokenStore );

    holder.updateOAuthToken( SERVER_URL, "new-access-token", "new-refresh-token", 30, 0 );

    verify( tokenStore, never() ).save( org.mockito.ArgumentMatchers.any() );
    assertNull( holder.getOAuthAccessToken( SERVER_URL ) );
  }

  @Test
  public void refreshHandleAndBrokerAuthHandleRoundTripAndClearWithOAuthToken() {
    TokenStore tokenStore = mock( TokenStore.class );
    when( tokenStore.load() ).thenReturn( Optional.empty() );

    BrowserAuthSessionHolder holder = new BrowserAuthSessionHolder( tokenStore );
    long expiredEpochSeconds = TimeUnit.MILLISECONDS.toSeconds( System.currentTimeMillis() ) - 60;
    holder.storeOAuthToken( new BrowserAuthSessionHolder.OAuthTokenData(
      SERVER_URL,
      ACCESS_TOKEN,
      null,
      BEARER,
      IDP_REGISTRATION_ID,
      300,
      expiredEpochSeconds,
      USERNAME ) );

    holder.setRefreshHandle( SERVER_URL, "refresh-handle" );
    holder.setBrokerAuthHandle( SERVER_URL, "broker-auth-handle" );

    assertTrue( holder.isOAuthTokenExpiredButRefreshable( SERVER_URL ) );
    assertEquals( "refresh-handle", holder.getRefreshHandle( SERVER_URL ) );
    assertEquals( "broker-auth-handle", holder.getBrokerAuthHandle( SERVER_URL ) );
    assertFalse( holder.findAccessToken( SERVER_URL ).isPresent() );
    assertFalse( holder.findSessionCookie( SERVER_URL ).isPresent() );

    holder.clearOAuthToken( SERVER_URL );

    assertNull( holder.getRefreshHandle( SERVER_URL ) );
    assertNull( holder.getBrokerAuthHandle( SERVER_URL ) );
  }

  @Test
  public void constructorDeletesPersistedExpiredSessionWithoutBrowserOrRefreshCapability() {
    TokenStore tokenStore = mock( TokenStore.class );
    when( tokenStore.load() ).thenReturn( Optional.of( StoredCredential.builder()
      .serverUrl( SERVER_URL )
      .oauthAccessToken( "expired-access" )
      .oauthTokenType( BEARER )
      .oauthTokenExpiry( System.currentTimeMillis() - TimeUnit.MINUTES.toMillis( 1 ) )
      .build() ) );

    BrowserAuthSessionHolder holder = new BrowserAuthSessionHolder( tokenStore );

    assertFalse( holder.hasValidSession( SERVER_URL ) );
    verify( tokenStore ).delete();
  }

  @Test
  public void clearSessionForSpecificServerOnlyClearsMatchingSession() {
    TokenStore tokenStore = mock( TokenStore.class );
    when( tokenStore.load() ).thenReturn( Optional.empty() );

    BrowserAuthSessionHolder holder = new BrowserAuthSessionHolder( tokenStore );
    holder.storeSession( SERVER_URL, SESSION_TOKEN, SESSION_COOKIE, USERNAME );

    holder.clearSession( "http://other-host/pentaho" );
    assertTrue( holder.hasValidSession( SERVER_URL ) );

    holder.clearSession( SERVER_URL );
    assertFalse( holder.hasValidSession( SERVER_URL ) );
  }

  @Test
  public void constructorRestoresOAuthSessionWhenExpiryIsUnknown() {
    TokenStore tokenStore = mock( TokenStore.class );
    when( tokenStore.load() ).thenReturn( Optional.of( StoredCredential.builder()
      .serverUrl( SERVER_URL )
      .username( USERNAME )
      .oauthAccessToken( ACCESS_TOKEN )
      .oauthTokenType( BEARER )
      .oauthTokenExpiry( -1 )
      .build() ) );

    BrowserAuthSessionHolder holder = new BrowserAuthSessionHolder( tokenStore );

    assertTrue( holder.hasValidSession( SERVER_URL ) );
    assertEquals( ACCESS_TOKEN, holder.getOAuthAccessToken( SERVER_URL ) );
    verify( tokenStore, never() ).delete();
  }

  @Test
  public void constructorRestoresCookieSession() {
    TokenStore tokenStore = mock( TokenStore.class );
    when( tokenStore.load() ).thenReturn( Optional.of( StoredCredential.builder()
      .serverUrl( SERVER_URL )
      .sessionCookie( SESSION_COOKIE )
      .username( USERNAME )
      .build() ) );

    BrowserAuthSessionHolder holder = new BrowserAuthSessionHolder( tokenStore );

    assertTrue( holder.hasValidSession( SERVER_URL ) );
    assertEquals( SESSION_COOKIE, holder.findSessionCookie( SERVER_URL ).orElse( null ) );
    assertNotNull( holder.findSessionCookie( SERVER_URL ) );
  }
}
