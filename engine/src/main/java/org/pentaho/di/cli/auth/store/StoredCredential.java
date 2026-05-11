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

import org.pentaho.di.cli.auth.BrowserAuthSessionHolder;

/**
 * Immutable snapshot of all credential data that needs to be persisted across
 * Pan invocations for a given server.
 *
 * <p>
 * Holds both browser-session fields (JSESSIONID-based) and OAuth token
 * fields (Device Code / Auth Code flows). The persistence backend
 * ({@link TokenStore})
 * stores and retrieves this record as a single unit;
 * {@link BrowserAuthSessionHolder}
 * owns the in-memory state and uses this type only at the serialization
 * boundary.
 *
 * <p>
 * All string fields may be {@code null} when the corresponding sub-flow
 * has not run. Expiry values of {@code 0} or negative indicate "no expiry set".
 *
 * @param serverUrl              Normalized server URL this credential is bound
 *                               to
 * @param sessionToken           JSESSIONID value obtained from browser auth
 *                               callback
 * @param sessionCookie          Full {@code Set-Cookie} value (may include
 *                               path/flags)
 * @param username               Authenticated principal name
 * @param sessionExpiry          Session expiry as epoch milliseconds
 * @param oauthAccessToken       OAuth2 Bearer access token
 * @param oauthRefreshToken      OAuth2 refresh token (may be null for
 *                               short-lived flows)
 * @param oauthTokenType         Token type — typically {@code "Bearer"}
 * @param oauthIdpRegistrationId IdP registration ID as configured in server
 *                               OAuth properties
 * @param oauthTokenExpiry       Access token expiry as epoch milliseconds
 * @param oauthRefreshHandle     Opaque handle for server-side token refresh
 *                               ({@code POST /api/oauth/token-refresh}); only
 *                               present
 *                               when the IdP issued a refresh token during the
 *                               auth flow
 * @param oauthBrokerAuthHandle  Broker auth handle bound to the refresh record.
 *                               It must be replayed alongside refresh_handle on
 *                               later Pan invocations so restart-time silent
 *                               refresh can satisfy TokenRefresh's binding check.
 */
public record StoredCredential(
  String serverUrl,
  String sessionToken,
  String sessionCookie,
  String username,
  long sessionExpiry,
  String oauthAccessToken,
  String oauthRefreshToken,
  String oauthTokenType,
  String oauthIdpRegistrationId,
  long oauthTokenExpiry,
  String oauthRefreshHandle,
  String oauthBrokerAuthHandle) {

  /**
   * Fluent builder so callers can construct instances without repeating all
   * fields
   * when updating only a subset (e.g. token refresh).
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Returns true when the session (JSESSIONID) portion has expired.
   */
  public boolean isSessionExpired() {
    return sessionExpiry > 0 && System.currentTimeMillis() > sessionExpiry;
  }

  /**
   * Returns true when the OAuth access token has expired.
   */
  public boolean isOAuthTokenExpired() {
    return oauthTokenExpiry > 0 && System.currentTimeMillis() > oauthTokenExpiry;
  }

  /**
   * Returns a builder pre-populated with this record's values.
   */
  public Builder toBuilder() {
    return new Builder()
      .serverUrl( serverUrl )
      .sessionToken( sessionToken )
      .sessionCookie( sessionCookie )
      .username( username )
      .sessionExpiry( sessionExpiry )
      .oauthAccessToken( oauthAccessToken )
      .oauthRefreshToken( oauthRefreshToken )
      .oauthTokenType( oauthTokenType )
      .oauthIdpRegistrationId( oauthIdpRegistrationId )
      .oauthTokenExpiry( oauthTokenExpiry )
      .oauthRefreshHandle( oauthRefreshHandle )
      .oauthBrokerAuthHandle( oauthBrokerAuthHandle );
  }

  /**
   * Builder for {@link StoredCredential}.
   */
  public static final class Builder {

    private String serverUrl;
    private String sessionToken;
    private String sessionCookie;
    private String username;
    private long sessionExpiry;
    private String oauthAccessToken;
    private String oauthRefreshToken;
    private String oauthTokenType = "Bearer";
    private String oauthIdpRegistrationId;
    private long oauthTokenExpiry;
    private String oauthRefreshHandle;
    private String oauthBrokerAuthHandle;

    private Builder() {
    }

    public Builder serverUrl( String serverUrl ) {
      this.serverUrl = serverUrl;
      return this;
    }

    public Builder sessionToken( String sessionToken ) {
      this.sessionToken = sessionToken;
      return this;
    }

    public Builder sessionCookie( String sessionCookie ) {
      this.sessionCookie = sessionCookie;
      return this;
    }

    public Builder username( String username ) {
      this.username = username;
      return this;
    }

    public Builder sessionExpiry( long sessionExpiry ) {
      this.sessionExpiry = sessionExpiry;
      return this;
    }

    public Builder oauthAccessToken( String oauthAccessToken ) {
      this.oauthAccessToken = oauthAccessToken;
      return this;
    }

    public Builder oauthRefreshToken( String oauthRefreshToken ) {
      this.oauthRefreshToken = oauthRefreshToken;
      return this;
    }

    public Builder oauthTokenType( String oauthTokenType ) {
      this.oauthTokenType = oauthTokenType;
      return this;
    }

    public Builder oauthIdpRegistrationId( String oauthIdpRegistrationId ) {
      this.oauthIdpRegistrationId = oauthIdpRegistrationId;
      return this;
    }

    public Builder oauthTokenExpiry( long oauthTokenExpiry ) {
      this.oauthTokenExpiry = oauthTokenExpiry;
      return this;
    }

    public Builder oauthRefreshHandle( String oauthRefreshHandle ) {
      this.oauthRefreshHandle = oauthRefreshHandle;
      return this;
    }

    public Builder oauthBrokerAuthHandle( String oauthBrokerAuthHandle ) {
      this.oauthBrokerAuthHandle = oauthBrokerAuthHandle;
      return this;
    }

    public StoredCredential build() {
      return new StoredCredential(
        serverUrl, sessionToken, sessionCookie, username, sessionExpiry,
        oauthAccessToken, oauthRefreshToken, oauthTokenType,
        oauthIdpRegistrationId, oauthTokenExpiry, oauthRefreshHandle,
        oauthBrokerAuthHandle );
    }
  }
}
