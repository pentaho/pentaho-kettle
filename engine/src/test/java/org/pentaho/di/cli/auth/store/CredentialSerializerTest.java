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

import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

public class CredentialSerializerTest {

  @Test
  public void toPropertiesWritesOnlyPresentFields() {
    StoredCredential credential = StoredCredential.builder()
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

    Properties properties = CredentialSerializer.toProperties( credential );

    assertEquals( "http://localhost:8080/pentaho", properties.getProperty( CredentialSerializer.KEY_SERVER_URL ) );
    assertEquals( "JSESSIONID", properties.getProperty( CredentialSerializer.KEY_SESSION_TOKEN ) );
    assertEquals( "JSESSIONID=abc", properties.getProperty( CredentialSerializer.KEY_SESSION_COOKIE ) );
    assertEquals( "alice", properties.getProperty( CredentialSerializer.KEY_USERNAME ) );
    assertEquals( "1234", properties.getProperty( CredentialSerializer.KEY_SESSION_EXPIRY ) );
    assertEquals( "access-token", properties.getProperty( CredentialSerializer.KEY_OAUTH_ACCESS_TOKEN ) );
    assertEquals( "refresh-token", properties.getProperty( CredentialSerializer.KEY_OAUTH_REFRESH_TOKEN ) );
    assertEquals( "Bearer", properties.getProperty( CredentialSerializer.KEY_OAUTH_TOKEN_TYPE ) );
    assertEquals( "azure", properties.getProperty( CredentialSerializer.KEY_OAUTH_IDP_ID ) );
    assertEquals( "5678", properties.getProperty( CredentialSerializer.KEY_OAUTH_TOKEN_EXPIRY ) );
    assertEquals( "refresh-handle", properties.getProperty( CredentialSerializer.KEY_OAUTH_REFRESH_HANDLE ) );
    assertEquals( "broker-auth-handle", properties.getProperty( CredentialSerializer.KEY_OAUTH_BROKER_AUTH_HANDLE ) );
  }

  @Test
  public void toPropertiesSkipsEmptyStringsAndZeroExpiries() {
    StoredCredential credential = StoredCredential.builder()
      .serverUrl( "http://localhost:8080/pentaho" )
      .sessionToken( "" )
      .sessionCookie( null )
      .username( "" )
      .sessionExpiry( 0L )
      .oauthAccessToken( "" )
      .oauthRefreshToken( null )
      .oauthTokenExpiry( 0L )
      .oauthRefreshHandle( "" )
      .oauthBrokerAuthHandle( null )
      .build();

    Properties properties = CredentialSerializer.toProperties( credential );

    assertEquals( "http://localhost:8080/pentaho", properties.getProperty( CredentialSerializer.KEY_SERVER_URL ) );
    assertFalse( properties.containsKey( CredentialSerializer.KEY_SESSION_TOKEN ) );
    assertFalse( properties.containsKey( CredentialSerializer.KEY_SESSION_COOKIE ) );
    assertFalse( properties.containsKey( CredentialSerializer.KEY_USERNAME ) );
    assertFalse( properties.containsKey( CredentialSerializer.KEY_SESSION_EXPIRY ) );
    assertFalse( properties.containsKey( CredentialSerializer.KEY_OAUTH_ACCESS_TOKEN ) );
    assertFalse( properties.containsKey( CredentialSerializer.KEY_OAUTH_REFRESH_TOKEN ) );
    assertFalse( properties.containsKey( CredentialSerializer.KEY_OAUTH_TOKEN_EXPIRY ) );
    assertFalse( properties.containsKey( CredentialSerializer.KEY_OAUTH_REFRESH_HANDLE ) );
    assertFalse( properties.containsKey( CredentialSerializer.KEY_OAUTH_BROKER_AUTH_HANDLE ) );
  }

  @Test
  public void fromPropertiesReturnsDefaultsForMissingOrInvalidFields() {
    Properties properties = new Properties();
    properties.setProperty( CredentialSerializer.KEY_SERVER_URL, "http://localhost:8080/pentaho" );
    properties.setProperty( CredentialSerializer.KEY_SESSION_EXPIRY, "not-a-long" );
    properties.setProperty( CredentialSerializer.KEY_OAUTH_TOKEN_EXPIRY, "" );

    StoredCredential credential = CredentialSerializer.fromProperties( properties );

    assertEquals( "http://localhost:8080/pentaho", credential.serverUrl() );
    assertNull( credential.sessionToken() );
    assertNull( credential.sessionCookie() );
    assertNull( credential.username() );
    assertEquals( 0L, credential.sessionExpiry() );
    assertNull( credential.oauthAccessToken() );
    assertNull( credential.oauthRefreshToken() );
    assertNull( credential.oauthIdpRegistrationId() );
    assertEquals( 0L, credential.oauthTokenExpiry() );
    assertNull( credential.oauthRefreshHandle() );
    assertNull( credential.oauthBrokerAuthHandle() );
  }
}
