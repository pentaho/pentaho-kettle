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

package org.pentaho.di.cli.auth.store;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class StoredCredentialTest {

  @Test
  public void builderUsesBearerAsDefaultTokenType() {
    StoredCredential credential = StoredCredential.builder()
      .serverUrl( "http://localhost:8080/pentaho" )
      .build();

    assertEquals( "Bearer", credential.oauthTokenType() );
  }

  @Test
  public void expiryHelpersHandleExpiredAndNonExpiredValues() {
    long now = System.currentTimeMillis();
    StoredCredential expired = StoredCredential.builder()
      .serverUrl( "http://localhost:8080/pentaho" )
      .sessionExpiry( now - 1 )
      .oauthTokenExpiry( now - 1 )
      .build();
    StoredCredential active = StoredCredential.builder()
      .serverUrl( "http://localhost:8080/pentaho" )
      .sessionExpiry( now + 60_000 )
      .oauthTokenExpiry( now + 60_000 )
      .build();
    StoredCredential unknown = StoredCredential.builder()
      .serverUrl( "http://localhost:8080/pentaho" )
      .sessionExpiry( 0 )
      .oauthTokenExpiry( -1 )
      .build();

    assertTrue( expired.isSessionExpired() );
    assertTrue( expired.isOAuthTokenExpired() );
    assertFalse( active.isSessionExpired() );
    assertFalse( active.isOAuthTokenExpired() );
    assertFalse( unknown.isSessionExpired() );
    assertFalse( unknown.isOAuthTokenExpired() );
  }

  @Test
  public void toBuilderCopiesValuesAndAllowsModification() {
    StoredCredential original = StoredCredential.builder()
      .serverUrl( "http://localhost:8080/pentaho" )
      .sessionToken( "JSESSIONID" )
      .sessionCookie( "JSESSIONID=abc" )
      .username( "alice" )
      .sessionExpiry( 1234L )
      .oauthAccessToken( "access-token" )
      .oauthRefreshToken( "refresh-token" )
      .oauthTokenType( "Bearer" )
      .oauthIdpRegistrationId( "azure" )
      .oauthTokenExpiry( 5678L )
      .oauthRefreshHandle( "refresh-handle" )
      .oauthBrokerAuthHandle( "broker-auth-handle" )
      .build();

    StoredCredential modified = original.toBuilder()
      .username( "bob" )
      .oauthAccessToken( "new-access-token" )
      .build();

    assertEquals( original.serverUrl(), modified.serverUrl() );
    assertEquals( "bob", modified.username() );
    assertEquals( "new-access-token", modified.oauthAccessToken() );
    assertEquals( original.oauthRefreshHandle(), modified.oauthRefreshHandle() );
  }
}

