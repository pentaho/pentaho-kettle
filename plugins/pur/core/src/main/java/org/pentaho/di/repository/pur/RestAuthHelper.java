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

import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.cli.auth.CredentialProvider;

import java.io.IOException;

/*
 * Utility for handling REST authentication fallback logic (session cookie, then basic auth).
 * Centralizes all dual-auth logic for REST clients.
 */

public class RestAuthHelper {

  private static String message( String key, String... tokens ) {
    return BaseMessages.getString( RestAuthHelper.class, key, tokens );
  }

  public static class RestAuthException extends IOException {
    RestAuthException( String message ) {
      super( message );
    }
  }

  private RestAuthHelper() {
    // noop
  }

  /**
   * Executes a REST request using the highest-priority available credential,
    * retrying lower-priority options when a prior attempt returns HTTP 401 or 403.
   * <p>
   * Priority: (1) OAuth Bearer → (2) session cookie → (3) trusted proxy → (4)
   * basic auth.
   * <p>
   * OAuth bearer tokens and session cookies are read from the injected
   * {@link CredentialProvider} (expiry-checked, URL-matched). Trusted proxy is
   * attempted only when {@code pentaho.repository.client.attemptTrust} is set.
   *
   * @param method             the HTTP request (GET, POST, etc.)
   * @param serverUrl          the Pentaho server URL used for credential lookup
   * @param username           the username for basic auth fallback and trust header
   * @param password           the password for basic auth fallback
   * @param trustUser          the trusted proxy header name
   * @param credentialProvider the credential source
   * @return the response entity body as a String
   * @throws IOException if an I/O error occurs or the final authentication attempt fails
   */
  public static String executeWithAuthFallback( ClassicHttpRequest method, String serverUrl, String username,
                                                String password, String trustUser,
                                                CredentialProvider credentialProvider ) throws IOException {

    // Priority 1: OAuth Bearer token — expiry checked, URL matched
    String oauthAccessToken = credentialProvider.findAccessToken( serverUrl ).orElse( null );
    if ( StringUtils.isNotBlank( oauthAccessToken ) ) {
      method.setHeader( "Authorization", "Bearer " + oauthAccessToken );
      try ( CloseableHttpClient client = HttpClients.createDefault() ) {
        String responseBody = executeWith401Fallback( client, method );
        if ( responseBody != null ) {
          return responseBody;
        }
      }
      method.removeHeaders( "Authorization" );
      // OAuth token rejected — fall through to session cookie.
      // Token refresh is broker-owned; the CLI does not refresh client-side.
    }

    // Priority 2: Session cookie
    String sessionCookie = credentialProvider.findSessionCookie( serverUrl ).orElse( null );
    if ( StringUtils.isNotBlank( sessionCookie ) ) {
      method.setHeader( "Cookie", sessionCookie );
      try ( CloseableHttpClient client = HttpClients.createDefault() ) {
        String responseBody = executeWith401Fallback( client, method );
        if ( responseBody != null ) {
          return responseBody;
        }
      }
      method.removeHeaders( "Cookie" );
    }

    // Priority 3: Trusted proxy header
    if ( StringUtils.isNotBlank( System.getProperty( "pentaho.repository.client.attemptTrust" ) ) ) {
      method.setHeader( trustUser, username );
      try ( CloseableHttpClient client = HttpClients.createDefault() ) {
        String responseBody = executeWith401Fallback( client, method );
        if ( responseBody != null ) {
          return responseBody;
        }
      }
      method.removeHeaders( trustUser );
    }

    // Priority 4: Basic auth
    BasicCredentialsProvider credsProvider = new BasicCredentialsProvider();
    credsProvider.setCredentials(
      new AuthScope( null, -1 ),
      new UsernamePasswordCredentials( username, password.toCharArray() ) );
    try ( CloseableHttpClient client = HttpClientBuilder.create()
      .setDefaultCredentialsProvider( credsProvider ).build() ) {
      return client.execute( method, response -> {
        int code = response.getCode();
        if ( code == 401 || code == 403 ) {
          throw new RestAuthException( message( "RestAuthHelper.AllAuthMethodsFailed",
            serverUrl, String.valueOf( code ) ) );
        }
        return EntityUtils.toString( response.getEntity() );
      } );
    }
  }

  private static String executeWith401Fallback( CloseableHttpClient client, ClassicHttpRequest method )
    throws IOException {
    return client.execute( method,
      response -> {
        int code = response.getCode();
        return code == 401 || code == 403 ? null : EntityUtils.toString( response.getEntity() );
      } );
  }
}
