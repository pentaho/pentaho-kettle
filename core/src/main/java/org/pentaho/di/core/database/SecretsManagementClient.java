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

package org.pentaho.di.core.database;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.util.HttpClientManager;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;

/**
 * JVM-wide client for the Fusion secrets-management service.
 *
 * <p>Fetches plaintext key/value pairs (e.g. {@code {"username": "...", "password": "..."}})
 * for a given {@code secretsRef} by calling
 * {@code GET <SECRETS_MANAGEMENT_URL>/api/v1/secrets/{secretsRef}} with a bearer token from
 * {@link CmsTokenProvider}. The plaintext is returned to the caller and is never written to
 * logs, cached on disk, or stored on long-lived objects by this class.
 *
 * <h3>Configuration</h3>
 * Resolution is active when {@link Const#getSecretsManagementUrl()} is non-null. Callers
 * should check that flag (and {@link Const#isFusionConnectionManagementEnabled()}) before
 * invoking this client. If the URL is not configured, {@link #getSecrets(String)} throws
 * {@link IllegalStateException}.
 *
 * <h3>Error handling</h3>
 * All transport-level and HTTP-level failures are mapped to {@link SecretsManagementException}
 * with one of {@link SecretsManagementException.Reason}. The user-facing message is short
 * (e.g. {@code "Secret unauthorized or expired"}) and never includes a backend stack trace,
 * but the original exception is preserved as the cause for server-side debugging.
 *
 * <h3>Thread safety</h3>
 * Instances are stateless apart from the underlying {@link CloseableHttpClient}, which is
 * already thread-safe. Safe to use as a singleton across the JVM.
 */
public class SecretsManagementClient {

  private static final String SECRETS_PATH = "/api/v1/secrets";

  /** Timeouts kept conservative — secrets retrieval blocks JDBC connect, so we want fast failures. */
  private static final int CONNECT_TIMEOUT_MS = 5_000;
  private static final int SOCKET_TIMEOUT_MS = 10_000;

  private static final SecretsManagementClient INSTANCE =
    new SecretsManagementClient( HttpClientManager.getInstance().createDefaultClient() );

  private static final LogChannelInterface log = LogChannel.GENERAL;

  private final CloseableHttpClient httpClient;

  /**
   * Returns the JVM-wide singleton.
   */
  public static SecretsManagementClient getInstance() {
    return INSTANCE;
  }

  /**
   * Package-private constructor for tests; production code uses {@link #getInstance()}.
   */
  SecretsManagementClient( CloseableHttpClient httpClient ) {
    this.httpClient = httpClient;
  }

  /**
   * Fetches the secrets bundle for {@code secretsRef} from the secrets-management service.
   *
   * <p>The returned map contains plaintext values. Callers MUST scope the references narrowly
   * (e.g. local variables consumed immediately by the JDBC driver) and MUST NOT log the map,
   * write it to disk, or attach it to long-lived objects.
   *
   * @param secretsRef the secret reference (typically a UUID returned by CMS at connection-creation time)
   * @return a map of plaintext key/value pairs; never {@code null}, may be empty
   * @throws SecretsManagementException if the secret cannot be retrieved or is invalid
   * @throws IllegalStateException if {@code SECRETS_MANAGEMENT_URL} is not configured or {@code secretsRef} is blank
   */
  public Map<String, String> getSecrets( String secretsRef ) throws SecretsManagementException {
    if ( secretsRef == null || secretsRef.trim().isEmpty() ) {
      throw new IllegalStateException( "secretsRef must be a non-blank reference" );
    }
    String baseUrl = Const.getSecretsManagementUrl();
    if ( baseUrl == null || baseUrl.trim().isEmpty() ) {
      throw new IllegalStateException(
        "SECRETS_MANAGEMENT_URL is not configured — cannot resolve secret '" + secretsRef + "'" );
    }

    String url = stripTrailingSlash( baseUrl ) + SECRETS_PATH + "/" + secretsRef;
    HttpGet request = new HttpGet( url );
    request.setConfig( RequestConfig.custom()
      .setConnectTimeout( CONNECT_TIMEOUT_MS )
      .setSocketTimeout( SOCKET_TIMEOUT_MS )
      .build() );
    request.setHeader( "Accept", "application/json" );

    String bearerToken;
    try {
      bearerToken = CmsTokenProvider.getInstance().getToken();
    } catch ( Exception e ) {
      // Token-endpoint failure: treat as unauthorized so the caller gets the standard message.
      throw new SecretsManagementException( SecretsManagementException.Reason.UNAUTHORIZED,
        "Secret unauthorized or expired", e );
    }
    if ( bearerToken != null ) {
      request.setHeader( "Authorization", "Bearer " + bearerToken );
    }

    log.logDebug( "SecretsManagementClient: GET " + url );

    try ( CloseableHttpResponse response = httpClient.execute( request ) ) {
      int status = response.getStatusLine().getStatusCode();
      if ( status == 200 ) {
        return parseBody( response.getEntity(), secretsRef );
      }
      throw mapStatus( status, secretsRef );
    } catch ( SecretsManagementException e ) {
      throw e;
    } catch ( SocketTimeoutException e ) {
      throw new SecretsManagementException( SecretsManagementException.Reason.UNAVAILABLE,
        "Secret store unavailable", e );
    } catch ( IOException e ) {
      throw new SecretsManagementException( SecretsManagementException.Reason.UNAVAILABLE,
        "Secret store unavailable", e );
    }
  }

  private Map<String, String> parseBody( HttpEntity entity, String secretsRef ) throws SecretsManagementException {
    if ( entity == null ) {
      throw new SecretsManagementException( SecretsManagementException.Reason.INVALID_RESPONSE,
        "Secret response was empty for '" + secretsRef + "'" );
    }
    try {
      byte[] body = EntityUtils.toByteArray( entity );
      if ( body.length == 0 ) {
        throw new SecretsManagementException( SecretsManagementException.Reason.INVALID_RESPONSE,
          "Secret response was empty for '" + secretsRef + "'" );
      }
      Map<String, String> parsed = new ObjectMapper().readValue( body,
        new TypeReference<Map<String, String>>() { } );
      return parsed == null ? Collections.emptyMap() : parsed;
    } catch ( SecretsManagementException e ) {
      throw e;
    } catch ( IOException e ) {
      // Do not include the body in the message — it may contain plaintext secret material.
      throw new SecretsManagementException( SecretsManagementException.Reason.INVALID_RESPONSE,
        "Secret response could not be parsed for '" + secretsRef + "'", e );
    }
  }

  private SecretsManagementException mapStatus( int status, String secretsRef ) {
    switch ( status ) {
      case 401:
      case 403:
        return new SecretsManagementException( SecretsManagementException.Reason.UNAUTHORIZED,
          "Secret unauthorized or expired" );
      case 404:
        return new SecretsManagementException( SecretsManagementException.Reason.NOT_FOUND,
          "Secret not found: " + secretsRef );
      default:
        if ( status >= 500 ) {
          return new SecretsManagementException( SecretsManagementException.Reason.UNAVAILABLE,
            "Secret store unavailable" );
        }
        return new SecretsManagementException( SecretsManagementException.Reason.UNAVAILABLE,
          "Secret store returned unexpected HTTP " + status );
    }
  }

  private static String stripTrailingSlash( String s ) {
    return s.endsWith( "/" ) ? s.substring( 0, s.length() - 1 ) : s;
  }

  /** Test-only helper to avoid pulling in StandardCharsets from callers. */
  static byte[] toBytes( String s ) {
    return s.getBytes( StandardCharsets.UTF_8 );
  }
}
