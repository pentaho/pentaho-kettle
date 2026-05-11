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

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientRequestFilter;
import jakarta.xml.ws.BindingProvider;
import jakarta.xml.ws.handler.MessageContext;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.pentaho.di.cli.auth.CredentialProvider;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Builds auth headers from a {@link CredentialProvider} for PUR transport.
 * <p>
 * Centralises the auth-header priority cascade that was previously duplicated
 * inline in {@link WebServiceManager} (SOAP + REST) and
 * {@link UserRoleDelegate}. Both callers now delegate to this factory.
 * <p>
 * Priority: OAuth Bearer &gt; Session Cookie &gt; Trust header &gt; Basic auth.
 * <p>
 * PUR transport is credential-consumer only — it never acquires credentials.
 * This class reads from the injected {@link CredentialProvider}; the provider
 * is populated by {@code AbstractBaseCommandExecutor} / broker flows.
 * <p>
 */
public final class CredentialHeaderFactory {

  /**
   * Trust header name matching {@code ProxyTrustingFilter} on the server.
   */
  private static final String TRUST_USER = "_trust_user_";

  private final CredentialProvider credentialProvider;

  public CredentialHeaderFactory( CredentialProvider credentialProvider ) {
    this.credentialProvider = Objects.requireNonNull( credentialProvider, "credentialProvider" );
  }

  // ---------- SOAP (JAX-WS BindingProvider) ----------

  /**
   * Returns HTTP headers for the highest-priority available credential.
   * The returned map is suitable for setting on
   * {@link MessageContext#HTTP_REQUEST_HEADERS} of a JAX-WS
   * {@link BindingProvider}.
   * <p>
   * Returns an empty map when no alternative auth is available — the caller
   * should fall back to username/password on the {@link BindingProvider}
   * directly.
   *
   * @param baseUrl  the Pentaho server base URL (holder lookup key)
   * @param username username for trust header fallback
   * @return headers map; empty if basic auth should be used instead
   */
  public Map<String, List<String>> forSoapRequest( String baseUrl, String username ) {
    String oauthToken = credentialProvider.findAccessToken( baseUrl ).orElse( null );
    if ( StringUtils.isNotBlank( oauthToken ) ) {
      Map<String, List<String>> headers = new HashMap<>();
      headers.put( "Authorization", Collections.singletonList( "Bearer " + oauthToken ) );
      return headers;
    }

    String sessionCookie = credentialProvider.findSessionCookie( baseUrl ).orElse( null );
    if ( StringUtils.isNotBlank( sessionCookie ) ) {
      Map<String, List<String>> headers = new HashMap<>();
      headers.put( "Cookie", Collections.singletonList( sessionCookie ) );
      return headers;
    }

    if ( StringUtils.isNotBlank( System.getProperty( "pentaho.repository.client.attemptTrust" ) ) ) {
      return Collections.singletonMap( TRUST_USER, Collections.singletonList( username ) );
    }

    return Collections.emptyMap();
  }

  /**
   * Returns {@code true} when a non-basic credential (OAuth or session) is
   * available for the given base URL. When true, the SOAP port should NOT be
   * cached because the credential can change mid-session.
   */
  public boolean hasNonBasicCredential( String baseUrl ) {
    return credentialProvider.findAccessToken( baseUrl ).filter( StringUtils::isNotBlank ).isPresent()
      || credentialProvider.findSessionCookie( baseUrl ).filter( StringUtils::isNotBlank ).isPresent();
  }

  // ---------- REST (JAX-RS ClientRequestFilter) ----------

  /**
   * Registers auth filters on a JAX-RS {@link Client} using the same priority:
   * OAuth Bearer &gt; Session Cookie &gt; Trust header &gt; Basic auth.
   * <p>
   * For OAuth and session-cookie, filters read <b>fresh</b> from the holder on
   * every request — picks up any mid-session token refresh automatically.
   *
   * @param client   the JAX-RS client to register filters on
   * @param baseUrl  the Pentaho server base URL
   * @param username username for trust-header/basic-auth fallback
   * @param password password for basic auth fallback
   */
  public void registerJaxRsAuth( Client client, String baseUrl,
                                 String username, String password ) {
    String oauthToken = credentialProvider.findAccessToken( baseUrl ).orElse( null );

    if ( StringUtils.isNotBlank( oauthToken ) ) {
      // Register a filter that reads the Bearer token fresh from the holder on
      // every request — picks up any mid-session token refresh automatically.
      client.register( (ClientRequestFilter) ctx -> {
        String token = credentialProvider.findAccessToken( baseUrl ).orElse( null );
        if ( StringUtils.isNotBlank( token ) ) {
          ctx.getHeaders().putSingle( "Authorization", "Bearer " + token );
        }
      } );
      return;
    }

    String sessionCookie = credentialProvider.findSessionCookie( baseUrl ).orElse( null );
    if ( StringUtils.isNotBlank( sessionCookie ) ) {
      client.register( (ClientRequestFilter) ctx -> {
        String cookie = credentialProvider.findSessionCookie( baseUrl ).orElse( null );
        if ( StringUtils.isNotBlank( cookie ) ) {
          ctx.getHeaders().putSingle( "Cookie", cookie );
        }
      } );
      return;
    }

    if ( StringUtils.isNotBlank( System.getProperty( "pentaho.repository.client.attemptTrust" ) ) ) {
      client.register( (ClientRequestFilter) ctx -> ctx.getHeaders().putSingle( TRUST_USER, username ) );
      return;
    }

    // Neither OAuth, session cookie, nor trust header — fall back to basic auth.
    HttpAuthenticationFeature feature = HttpAuthenticationFeature.basic( username, password );
    client.register( feature );
  }
}
