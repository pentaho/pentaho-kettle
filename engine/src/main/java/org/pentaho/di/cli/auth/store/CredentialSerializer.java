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

import java.util.Properties;

/**
 * Package-private helper that converts a {@link StoredCredential} to/from a
 * {@link Properties} map. Centralises the property-key names so that
 * {@link FileTokenStore} and {@link EncryptedFileTokenStore} share the same
 * on-disk format
 * and neither can accidentally drift independently.
 */
final class CredentialSerializer {

  static final String KEY_SERVER_URL = "serverUrl";
  static final String KEY_SESSION_TOKEN = "sessionToken";
  static final String KEY_SESSION_COOKIE = "sessionCookie";
  static final String KEY_USERNAME = "username";
  static final String KEY_SESSION_EXPIRY = "sessionExpiry";
  static final String KEY_OAUTH_ACCESS_TOKEN = "oauthAccessToken";
  static final String KEY_OAUTH_REFRESH_TOKEN = "oauthRefreshToken";
  static final String KEY_OAUTH_TOKEN_TYPE = "oauthTokenType";
  static final String KEY_OAUTH_IDP_ID = "oauthIdpRegistrationId";
  static final String KEY_OAUTH_TOKEN_EXPIRY = "oauthTokenExpiry";
  static final String KEY_OAUTH_REFRESH_HANDLE = "oauthRefreshHandle";
  static final String KEY_OAUTH_BROKER_AUTH_HANDLE = "oauthBrokerAuthHandle";

  private CredentialSerializer() {
    // utility class — no instances
  }

  /**
   * Serialises {@code credential} into a {@link Properties} instance.
   * Only non-null / non-zero fields are written to keep the file clean.
   */
  static Properties toProperties( StoredCredential credential ) {
    Properties props = new Properties();
    setIfPresent( props, KEY_SERVER_URL, credential.serverUrl() );
    setIfPresent( props, KEY_SESSION_TOKEN, credential.sessionToken() );
    setIfPresent( props, KEY_SESSION_COOKIE, credential.sessionCookie() );
    setIfPresent( props, KEY_USERNAME, credential.username() );
    if ( credential.sessionExpiry() > 0 ) {
      props.setProperty( KEY_SESSION_EXPIRY, String.valueOf( credential.sessionExpiry() ) );
    }
    setIfPresent( props, KEY_OAUTH_ACCESS_TOKEN, credential.oauthAccessToken() );
    setIfPresent( props, KEY_OAUTH_REFRESH_TOKEN, credential.oauthRefreshToken() );
    setIfPresent( props, KEY_OAUTH_TOKEN_TYPE, credential.oauthTokenType() );
    setIfPresent( props, KEY_OAUTH_IDP_ID, credential.oauthIdpRegistrationId() );
    if ( credential.oauthTokenExpiry() > 0 ) {
      props.setProperty( KEY_OAUTH_TOKEN_EXPIRY, String.valueOf( credential.oauthTokenExpiry() ) );
    }
    setIfPresent( props, KEY_OAUTH_REFRESH_HANDLE, credential.oauthRefreshHandle() );
    setIfPresent( props, KEY_OAUTH_BROKER_AUTH_HANDLE, credential.oauthBrokerAuthHandle() );
    return props;
  }

  /**
   * Deserialises a {@link StoredCredential} from a {@link Properties} instance.
   * Missing keys produce {@code null} string fields and {@code 0} long fields —
   * callers must treat those as "not set".
   */
  static StoredCredential fromProperties( Properties props ) {
    return StoredCredential.builder()
      .serverUrl( props.getProperty( KEY_SERVER_URL ) )
      .sessionToken( props.getProperty( KEY_SESSION_TOKEN ) )
      .sessionCookie( props.getProperty( KEY_SESSION_COOKIE ) )
      .username( props.getProperty( KEY_USERNAME ) )
      .sessionExpiry( parseLong( props.getProperty( KEY_SESSION_EXPIRY ) ) )
      .oauthAccessToken( props.getProperty( KEY_OAUTH_ACCESS_TOKEN ) )
      .oauthRefreshToken( props.getProperty( KEY_OAUTH_REFRESH_TOKEN ) )
      .oauthTokenType( props.getProperty( KEY_OAUTH_TOKEN_TYPE ) )
      .oauthIdpRegistrationId( props.getProperty( KEY_OAUTH_IDP_ID ) )
      .oauthTokenExpiry( parseLong( props.getProperty( KEY_OAUTH_TOKEN_EXPIRY ) ) )
      .oauthRefreshHandle( props.getProperty( KEY_OAUTH_REFRESH_HANDLE ) )
      .oauthBrokerAuthHandle( props.getProperty( KEY_OAUTH_BROKER_AUTH_HANDLE ) )
      .build();
  }

  private static void setIfPresent( Properties props, String key, String value ) {
    if ( value != null && !value.isEmpty() ) {
      props.setProperty( key, value );
    }
  }

  private static long parseLong( String value ) {
    if ( value == null || value.isBlank() ) {
      return 0L;
    }
    try {
      return Long.parseLong( value );
    } catch ( NumberFormatException ignored ) {
      return 0L;
    }
  }
}
