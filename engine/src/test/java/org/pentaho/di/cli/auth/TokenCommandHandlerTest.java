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

import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

public class TokenCommandHandlerTest {

  private static final String SET_TOKEN_COMMAND = "-auth:set-token";
  private static final String CLEAR_TOKEN_COMMAND = "-auth:clear-token";
  private static final String SERVER_ARG = "--server";
  private static final String TOKEN_ARG = "--token";
  private static final String EXPIRES_IN_ARG = "--expires-in";

  private static final String SERVER_URL = "http://localhost:8080/pentaho";
  private static final String JWT_TOKEN = buildJwt(
    "{\"alg\":\"none\"}",
    "{\"preferred_username\":\"alice\",\"exp\":1893456000}" );
  private static final String SUB_ONLY_JWT_TOKEN = buildJwt(
    "{\"alg\":\"none\"}",
    "{\"sub\":\"subject-user\"}" );
  private static final String OPAQUE_TOKEN = "opaque-access-token";

  @Test
  public void hasTokenCommandDetectsAuthPrefixedArguments() {
    assertTrue( TokenCommandHandler.hasTokenCommand( new String[] { SET_TOKEN_COMMAND } ) );
    assertTrue( TokenCommandHandler.hasTokenCommand( new String[] { "--flag", "-AUTH:CLEAR-TOKEN" } ) );
    assertFalse( TokenCommandHandler.hasTokenCommand( null ) );
    assertFalse( TokenCommandHandler.hasTokenCommand( new String[] { SERVER_ARG, SERVER_URL } ) );
  }

  @Test
  public void executeSetTokenStoresJwtClaimsWithoutLifetimeHint() {
    BrowserAuthSessionHolder holder = mock( BrowserAuthSessionHolder.class );
    TokenCommandHandler handler = new TokenCommandHandler( holder );

    int result = handler.execute( new String[] {
      SET_TOKEN_COMMAND, SERVER_ARG, SERVER_URL, TOKEN_ARG, JWT_TOKEN, EXPIRES_IN_ARG, "999"
    } );

    ArgumentCaptor<BrowserAuthSessionHolder.OAuthTokenData> tokenDataCaptor =
      ArgumentCaptor.forClass( BrowserAuthSessionHolder.OAuthTokenData.class );

    assertEquals( 0, result );
    verify( holder ).storeOAuthToken( tokenDataCaptor.capture() );

    BrowserAuthSessionHolder.OAuthTokenData tokenData = tokenDataCaptor.getValue();
    assertEquals( SERVER_URL, tokenData.serverUrl() );
    assertEquals( JWT_TOKEN, tokenData.accessToken() );
    assertEquals( "Bearer", tokenData.tokenType() );
    assertEquals( 0L, tokenData.expiresInSeconds() );
    assertEquals( 1893456000L, tokenData.expEpochSeconds() );
    assertEquals( "alice", tokenData.username() );
  }

  @Test
  public void executeSetTokenUsesExpiresInHintForOpaqueToken() {
    BrowserAuthSessionHolder holder = mock( BrowserAuthSessionHolder.class );
    TokenCommandHandler handler = new TokenCommandHandler( holder );

    int result = handler.execute( new String[] {
      SET_TOKEN_COMMAND, SERVER_ARG, SERVER_URL, TOKEN_ARG, OPAQUE_TOKEN, EXPIRES_IN_ARG, "3600"
    } );

    ArgumentCaptor<BrowserAuthSessionHolder.OAuthTokenData> tokenDataCaptor =
      ArgumentCaptor.forClass( BrowserAuthSessionHolder.OAuthTokenData.class );

    assertEquals( 0, result );
    verify( holder ).storeOAuthToken( tokenDataCaptor.capture() );

    BrowserAuthSessionHolder.OAuthTokenData tokenData = tokenDataCaptor.getValue();
    assertEquals( OPAQUE_TOKEN, tokenData.accessToken() );
    assertEquals( 3600L, tokenData.expiresInSeconds() );
    assertEquals( 0L, tokenData.expEpochSeconds() );
    assertNull( tokenData.username() );
  }

  @Test
  public void executeSetTokenStoresOpaqueTokenWithoutExpiryHint() {
    BrowserAuthSessionHolder holder = mock( BrowserAuthSessionHolder.class );
    TokenCommandHandler handler = new TokenCommandHandler( holder );

    int result = handler.execute( new String[] {
      SET_TOKEN_COMMAND, SERVER_ARG, SERVER_URL, TOKEN_ARG, OPAQUE_TOKEN
    } );

    ArgumentCaptor<BrowserAuthSessionHolder.OAuthTokenData> tokenDataCaptor =
      ArgumentCaptor.forClass( BrowserAuthSessionHolder.OAuthTokenData.class );

    assertEquals( 0, result );
    verify( holder ).storeOAuthToken( tokenDataCaptor.capture() );
    assertEquals( 0L, tokenDataCaptor.getValue().expiresInSeconds() );
    assertEquals( 0L, tokenDataCaptor.getValue().expEpochSeconds() );
    assertNull( tokenDataCaptor.getValue().username() );
  }

  @Test
  public void executeClearTokenWithoutServerClearsAllCredentials() {
    BrowserAuthSessionHolder holder = mock( BrowserAuthSessionHolder.class );
    TokenCommandHandler handler = new TokenCommandHandler( holder );

    int result = handler.execute( new String[] { CLEAR_TOKEN_COMMAND } );

    assertEquals( 0, result );
    verify( holder ).clearSession();
  }

  @Test
  public void executeClearTokenWithBlankServerClearsAllCredentials() {
    BrowserAuthSessionHolder holder = mock( BrowserAuthSessionHolder.class );
    TokenCommandHandler handler = new TokenCommandHandler( holder );

    int result = handler.execute( new String[] { CLEAR_TOKEN_COMMAND, SERVER_ARG, "   " } );

    assertEquals( 0, result );
    verify( holder ).clearSession();
    verify( holder, never() ).clearOAuthToken( SERVER_URL );
  }

  @Test
  public void executeClearTokenWithServerClearsOnlyThatServer() {
    BrowserAuthSessionHolder holder = mock( BrowserAuthSessionHolder.class );
    TokenCommandHandler handler = new TokenCommandHandler( holder );

    int result = handler.execute( new String[] { CLEAR_TOKEN_COMMAND, SERVER_ARG, SERVER_URL } );

    assertEquals( 0, result );
    verify( holder ).clearOAuthToken( SERVER_URL );
  }

  @Test
  public void executeSetTokenFailsWhenServerIsMissing() {
    TokenCommandHandler handler = new TokenCommandHandler();

    int result = handler.execute( new String[] { SET_TOKEN_COMMAND, TOKEN_ARG, JWT_TOKEN } );

    assertEquals( 1, result );
  }

  @Test
  public void executeSetTokenPromptsWhenTokenArgumentIsMissing() {
    BrowserAuthSessionHolder holder = mock( BrowserAuthSessionHolder.class );
    TokenCommandHandler handler = spy( new TokenCommandHandler( holder ) );
    doReturn( JWT_TOKEN ).when( handler ).promptForToken();

    int result = handler.execute( new String[] { SET_TOKEN_COMMAND, SERVER_ARG, SERVER_URL } );

    assertEquals( 0, result );
    verify( holder ).storeOAuthToken( org.mockito.ArgumentMatchers.any() );
  }

  @Test
  public void executeSetTokenFailsWhenPromptReturnsBlank() {
    BrowserAuthSessionHolder holder = mock( BrowserAuthSessionHolder.class );
    TokenCommandHandler handler = spy( new TokenCommandHandler( holder ) );
    doReturn( "   " ).when( handler ).promptForToken();

    int result = handler.execute( new String[] { SET_TOKEN_COMMAND, SERVER_ARG, SERVER_URL } );

    assertEquals( 1, result );
    verify( holder, never() ).storeOAuthToken( org.mockito.ArgumentMatchers.any() );
  }

  @Test
  public void executeFailsWhenNoTokenCommandIsPresent() {
    TokenCommandHandler handler = new TokenCommandHandler();

    int result = handler.execute( new String[] { SERVER_ARG, SERVER_URL } );

    assertEquals( 1, result );
  }

  @Test
  public void executeFailsForUnknownTokenCommand() {
    TokenCommandHandler handler = new TokenCommandHandler();

    int result = handler.execute( new String[] { "-auth:oops" } );

    assertEquals( 1, result );
  }

  @Test
  public void executeSetTokenFailsWhenExpiresInIsNegative() {
    BrowserAuthSessionHolder holder = mock( BrowserAuthSessionHolder.class );
    TokenCommandHandler handler = new TokenCommandHandler( holder );

    int result = handler.execute( new String[] {
      SET_TOKEN_COMMAND, SERVER_ARG, SERVER_URL, TOKEN_ARG, OPAQUE_TOKEN, EXPIRES_IN_ARG, "-1"
    } );

    assertEquals( 1, result );
    verify( holder, never() ).storeOAuthToken( org.mockito.ArgumentMatchers.any() );
  }

  @Test
  public void executeSetTokenFailsWhenExpiresInIsNotNumeric() {
    BrowserAuthSessionHolder holder = mock( BrowserAuthSessionHolder.class );
    TokenCommandHandler handler = new TokenCommandHandler( holder );

    int result = handler.execute( new String[] {
      SET_TOKEN_COMMAND, SERVER_ARG, SERVER_URL, TOKEN_ARG, OPAQUE_TOKEN, EXPIRES_IN_ARG, "abc"
    } );

    assertEquals( 1, result );
    verify( holder, never() ).storeOAuthToken( org.mockito.ArgumentMatchers.any() );
  }

  @Test
  public void executeSetTokenFallsBackToSubClaimWhenPreferredUsernameIsMissing() {
    BrowserAuthSessionHolder holder = mock( BrowserAuthSessionHolder.class );
    TokenCommandHandler handler = new TokenCommandHandler( holder );

    int result = handler.execute( new String[] {
      SET_TOKEN_COMMAND, SERVER_ARG, SERVER_URL, TOKEN_ARG, SUB_ONLY_JWT_TOKEN
    } );

    ArgumentCaptor<BrowserAuthSessionHolder.OAuthTokenData> tokenDataCaptor =
      ArgumentCaptor.forClass( BrowserAuthSessionHolder.OAuthTokenData.class );

    assertEquals( 0, result );
    verify( holder ).storeOAuthToken( tokenDataCaptor.capture() );
    assertEquals( "subject-user", tokenDataCaptor.getValue().username() );
  }

  private static String buildJwt( String header, String payload ) {
    Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();
    return encoder.encodeToString( header.getBytes( StandardCharsets.UTF_8 ) )
      + "."
      + encoder.encodeToString( payload.getBytes( StandardCharsets.UTF_8 ) )
      + ".signature";
  }

}
