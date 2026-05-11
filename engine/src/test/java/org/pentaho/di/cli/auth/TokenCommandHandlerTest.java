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
import org.mockito.ArgumentCaptor;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class TokenCommandHandlerTest {

  private static final String SET_TOKEN_COMMAND = "-auth:set-token";
  private static final String CLEAR_TOKEN_COMMAND = "-auth:clear-token";
  private static final String SERVER_ARG = "--server";
  private static final String TOKEN_ARG = "--token";
  private static final String EXPIRES_IN_ARG = "--expires-in";

  @ClassRule public static final RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  private static final String SERVER_URL = "http://localhost:8080/pentaho";
  private static final String JWT_TOKEN = buildJwt(
    "{\"alg\":\"none\"}",
    "{\"preferred_username\":\"alice\",\"exp\":1893456000}" );
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
  public void executeClearTokenWithoutServerClearsAllCredentials() {
    BrowserAuthSessionHolder holder = mock( BrowserAuthSessionHolder.class );
    TokenCommandHandler handler = new TokenCommandHandler( holder );

    int result = handler.execute( new String[] { CLEAR_TOKEN_COMMAND } );

    assertEquals( 0, result );
    verify( holder ).clearSession();
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

  private static String buildJwt( String header, String payload ) {
    Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();
    return encoder.encodeToString( header.getBytes( StandardCharsets.UTF_8 ) )
      + "."
      + encoder.encodeToString( payload.getBytes( StandardCharsets.UTF_8 ) )
      + ".signature";
  }
}
