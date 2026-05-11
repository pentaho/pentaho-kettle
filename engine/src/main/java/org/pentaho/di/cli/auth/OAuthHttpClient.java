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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.i18n.BaseMessages;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;

import static jakarta.ws.rs.HttpMethod.GET;
import static jakarta.ws.rs.HttpMethod.POST;
import static jakarta.ws.rs.core.HttpHeaders.ACCEPT;
import static jakarta.ws.rs.core.HttpHeaders.CONTENT_LENGTH;
import static jakarta.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static jakarta.ws.rs.core.MediaType.APPLICATION_FORM_URLENCODED;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static java.net.HttpURLConnection.HTTP_OK;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Low-level HTTP and JSON utility for OAuth 2.0 CLI flows.
 * <p>
 * Centralises the following concerns that were previously duplicated:
 * <ul>
 * <li><b>HTTP connection opening</b> — handles both {@code http://} and
 * {@code https://} URIs correctly, without the dangerous
 * {@code (HttpsURLConnection) url.openConnection()} cast that crashes
 * on plain-HTTP endpoints (e.g., local Keycloak).</li>
 * <li><b>Form-encoded POST</b> — builds an
 * {@code application/x-www-form-urlencoded} body and sends it to an
 * IdP endpoint, returning the response body as a string.</li>
 * <li><b>Response and error body reading</b> — reads input/error streams
 * safely.</li>
 * <li><b>Lightweight JSON extraction</b> — Gson-backed parsing for robust
 * handling of escaped strings, booleans, numbers, and arrays.</li>
 * <li><b>Token response parsing</b> — converts a raw JSON token endpoint
 * response into a record, including
 * best-effort username extraction from an embedded {@code id_token}
 * JWT.</li>
 * </ul>
 * <p>
 * <b>Testability:</b> connect and read timeouts are constructor parameters.
 *
 */
public class OAuthHttpClient {

  private static final LogChannelInterface DEFAULT_LOG = new LogChannel( "OAuthHttpClient" );
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  private static String message( String key, String... tokens ) {
    return BaseMessages.getString( OAuthHttpClient.class, key, tokens );
  }

  private final int connectTimeoutMs;
  private final int readTimeoutMs;
  private final LogChannelInterface log;

  /**
   * Production constructor with default log channel.
   *
   * @param connectTimeoutMs TCP connect timeout in milliseconds
   * @param readTimeoutMs    socket read timeout in milliseconds
   */
  public OAuthHttpClient( int connectTimeoutMs, int readTimeoutMs ) {
    this( connectTimeoutMs, readTimeoutMs, DEFAULT_LOG );
  }

  OAuthHttpClient( int connectTimeoutMs, int readTimeoutMs, LogChannelInterface log ) {
    this.connectTimeoutMs = connectTimeoutMs;
    this.readTimeoutMs = readTimeoutMs;
    this.log = log;
  }

  /**
   * Open an {@link HttpURLConnection} for the given URI string.
   * <p>
   * Handles both {@code http://} and {@code https://} schemes correctly.
   * Never casts to {@code HttpsURLConnection} — the JDK returns the right
   * subtype through {@code URL.openConnection()} already.
   *
   * @param uri the endpoint URI (must start with {@code http://} or
   *            {@code https://})
   * @return an open {@code HttpURLConnection}
   * @throws IOException if the connection cannot be created
   */
  public HttpURLConnection openConnection( String uri ) throws IOException {
    HttpURLConnection conn = (HttpURLConnection) URI.create( uri ).toURL().openConnection();
    conn.setConnectTimeout( connectTimeoutMs );
    conn.setReadTimeout( readTimeoutMs );
    return conn;
  }

  /**
   * POST an {@code application/x-www-form-urlencoded} body to the given URI
   * and return the response body as a string.
   * <p>
   * On a non-200 response the method returns {@code null} and logs the error.
   *
   * @param uri    target endpoint
   * @param params form parameters (keys and values are URL-encoded internally)
   * @return response body on HTTP 200, {@code null} otherwise
   * @throws IOException on network errors
   */
  public String postForm( String uri, Map<String, String> params ) throws IOException {
    return postForm( uri, params, null );
  }

  /**
   * POST a form to the given URI with optional extra headers.
   *
   * @param uri          target endpoint
   * @param params       form parameters
   * @param extraHeaders optional additional request headers (nullable)
   * @return response body on HTTP 200, {@code null} otherwise
   * @throws IOException on network errors
   */
  public String postForm( String uri, Map<String, String> params,
                          Map<String, String> extraHeaders ) throws IOException {
    HttpURLConnection conn = openConnection( uri );
    try {
      conn.setRequestMethod( POST );
      conn.setDoOutput( true );
      applyFormJsonHeaders( conn, extraHeaders );
      writeRequestBody( conn, buildFormBody( params ) );
      return getSuccessfulBodyOrLog( POST, uri, readPostResult( conn ) );
    } finally {
      conn.disconnect();
    }
  }

  /**
   * GET a JSON endpoint and return the response body as a string.
   * <p>
   * On a non-200 response the method returns {@code null} and logs the error.
   *
   * @param uri target endpoint
   * @return response body on HTTP 200, {@code null} otherwise
   * @throws IOException on network errors
   */
  public String getJson( String uri ) throws IOException {
    HttpURLConnection conn = openConnection( uri );
    try {
      conn.setRequestMethod( GET );
      applyAcceptJsonHeader( conn );
      return getSuccessfulBodyOrLog( GET, uri, readPostResult( conn ) );
    } finally {
      conn.disconnect();
    }
  }

  /**
   * POST a form to the given URI, returning both the response body and the
   * HTTP status code.
   * <p>
   * Unlike {@link #postForm(String, Map)} this method does NOT treat non-200
   * as failure — the caller handles the status code (needed for device-code
   * polling where HTTP 400 is expected).
   *
   * @param uri    target endpoint
   * @param params form parameters
   * @return a two-element result: {@code [status, responseBody]}
   * @throws IOException on network errors
   */
  public PostResult postFormRaw( String uri, Map<String, String> params ) throws IOException {
    return postFormRaw( uri, params, null );
  }

  /**
   * POST a form to the given URI, returning both the response body and the
   * HTTP status code, with optional extra headers.
   *
   * @param uri          target endpoint
   * @param params       form parameters
   * @param extraHeaders optional additional request headers (nullable)
   * @return a two-element result: {@code [status, responseBody]}
   * @throws IOException on network errors
   */
  public PostResult postFormRaw( String uri, Map<String, String> params,
                                 Map<String, String> extraHeaders ) throws IOException {
    HttpURLConnection conn = openConnection( uri );
    try {
      conn.setRequestMethod( POST );
      conn.setDoOutput( true );
      applyFormJsonHeaders( conn, extraHeaders );
      writeRequestBody( conn, buildFormBody( params ) );
      return readPostResult( conn );
    } finally {
      conn.disconnect();
    }
  }

  /**
   * POST to the given URI without a request body, returning both the response
   * body and the HTTP status code.
   *
   * @param uri          target endpoint
   * @param extraHeaders optional additional request headers (nullable)
   * @return a two-element result: {@code [status, responseBody]}
   * @throws IOException on network errors
   */
  public PostResult postEmptyRaw( String uri, Map<String, String> extraHeaders ) throws IOException {
    HttpURLConnection conn = openConnection( uri );
    try {
      conn.setRequestMethod( POST );
      conn.setRequestProperty( CONTENT_LENGTH, "0" );
      applyHeaders( conn, extraHeaders );
      return readPostResult( conn );
    } finally {
      conn.disconnect();
    }
  }

  private void applyFormJsonHeaders( HttpURLConnection conn, Map<String, String> extraHeaders ) {
    conn.setRequestProperty( CONTENT_TYPE, APPLICATION_FORM_URLENCODED );
    applyAcceptJsonHeader( conn );
    applyHeaders( conn, extraHeaders );
  }

  private void applyAcceptJsonHeader( HttpURLConnection conn ) {
    conn.setRequestProperty( ACCEPT, APPLICATION_JSON );
  }

  private void applyHeaders( HttpURLConnection conn, Map<String, String> extraHeaders ) {
    if ( extraHeaders == null ) {
      return;
    }
    for ( Map.Entry<String, String> header : extraHeaders.entrySet() ) {
      conn.setRequestProperty( header.getKey(), header.getValue() );
    }
  }

  private void writeRequestBody( HttpURLConnection conn, String requestBody ) throws IOException {
    try ( OutputStream os = conn.getOutputStream() ) {
      os.write( requestBody.getBytes( UTF_8 ) );
    }
  }

  private String getSuccessfulBodyOrLog( String method, String uri, PostResult result ) {
    if ( result.status() == HTTP_OK ) {
      return result.body();
    }
    if ( log.isDebug() ) {
      log.logDebug( message( "OAuthHttpClient.HttpStatusDebug", method, uri,
        String.valueOf( result.status() ), result.body() ) );
    }
    return null;
  }

  private PostResult readPostResult( HttpURLConnection conn ) throws IOException {
    int status = conn.getResponseCode();
    return new PostResult( status, readResponse( conn, status ) );
  }

  private String readResponse( HttpURLConnection conn, int status ) throws IOException {
    return status == HTTP_OK ? readResponseBody( conn ) : readErrorBody( conn );
  }

  /**
   * Build a URL-encoded form body from a map of key-value pairs.
   *
   * @param params parameters to encode
   * @return the form body string (e.g.
   * {@code "grant_type=device_code&client_id=abc"})
   */
  public String buildFormBody( Map<String, String> params ) {
    StringJoiner joiner = new StringJoiner( "&" );
    for ( Map.Entry<String, String> entry : params.entrySet() ) {
      // URLEncoder encodes space as '+' (application/x-www-form-urlencoded).
      // RFC 6749 §B and most IdPs require '%20' for spaces in OAuth form params
      // (e.g. scope values). Replace '+' → '%20' to be safe with both strict
      // and lenient IdPs while remaining valid form encoding.
      joiner.add(
        encodeQueryParam( entry.getKey() )
          + "="
          + encodeQueryParam( entry.getValue() ) );
    }
    return joiner.toString();
  }

  /**
   * Percent-encode a single query parameter value.
   * {@link URLEncoder} produces {@code application/x-www-form-urlencoded}
   * encoding
   * (space → {@code +}); replacing {@code +} → {@code %20} gives RFC 3986
   * encoding
   * that is accepted by both strict and lenient OAuth 2.0 IdPs.
   */
  protected String encodeQueryParam( String value ) {
    if ( value == null ) {
      return "";
    }
    return URLEncoder.encode( value, UTF_8 ).replace( "+", "%20" );
  }

  /**
   * Read the response body from an HTTP connection's input stream.
   */
  public String readResponseBody( HttpURLConnection conn ) throws IOException {
    return readStream( conn.getInputStream() );
  }

  /**
   * Read the error response body from an HTTP connection's error stream.
   * Returns an empty string if the error stream is null or reading fails.
   */
  public String readErrorBody( HttpURLConnection conn ) {
    try {
      InputStream errorStream = conn.getErrorStream();
      if ( errorStream == null ) {
        return "";
      }
      return readStream( errorStream );
    } catch ( IOException e ) {
      return "";
    }
  }

  private String readStream( InputStream stream ) throws IOException {
    try ( BufferedReader reader = new BufferedReader(
      new InputStreamReader( stream, UTF_8 ) ) ) {
      StringBuilder sb = new StringBuilder();
      String line;
      while ( ( line = reader.readLine() ) != null ) {
        sb.append( line );
      }
      return sb.toString();
    }
  }

  /**
   * Extract a scalar JSON value as a string.
   *
   * @param json the raw JSON string
   * @param key  the key to look up
   * @return the value, or {@code null} if not found
   */
  public String extractJsonString( String json, String key ) {
    JsonNode element = extractJsonElement( json, key );
    if ( element == null || element.isNull() || element.isContainerNode() ) {
      return null;
    }
    return element.asText();
  }

  /**
   * Extract a JSON string array value.
   *
   * @param json the raw JSON string
   * @param key  the key to look up
   * @return the list of string values, or an empty list if not found
   */
  public List<String> extractJsonArray( String json, String key ) {
    JsonNode element = extractJsonElement( json, key );
    if ( element == null || !element.isArray() || element.isEmpty() ) {
      return Collections.emptyList();
    }
    List<String> result = new ArrayList<>();
    for ( JsonNode item : element ) {
      if ( item != null && item.isValueNode() ) {
        result.add( item.asText() );
      }
    }
    return Collections.unmodifiableList( result );
  }

  /**
   * Extract a numeric value from a simple JSON object.
   *
   * @param json the raw JSON string
   * @param key  the key to look up
   * @return the value as a {@code Long}, or {@code null} if not found
   */
  public Long extractJsonNumber( String json, String key ) {
    JsonNode element = extractJsonElement( json, key );
    if ( element == null || element.isNull() || !element.isNumber() ) {
      return null;
    }
    try {
      return element.longValue();
    } catch ( RuntimeException e ) {
      return null;
    }
  }

  public Optional<Boolean> extractJsonBoolean( String json, String key ) {
    JsonNode element = extractJsonElement( json, key );
    if ( element == null || element.isNull() || !element.isBoolean() ) {
      return Optional.empty();
    }
    return Optional.of( element.booleanValue() );
  }

  JsonNode extractJsonElement( String json, String key ) {
    if ( json == null || key == null ) {
      return null;
    }
    try {
      JsonNode root = OBJECT_MAPPER.readTree( json );
      return root != null && root.isObject() ? root.get( key ) : null;
    } catch ( Exception e ) {
      if ( log.isDebug() ) {
        log.logDebug( message( "OAuthHttpClient.JsonFieldParseFailed", key, e.getMessage() ) );
      }
      return null;
    }
  }

  /**
   * Holds the HTTP status code and response body from a raw POST.
   *
   * @param status HTTP status code
   * @param body   response or error body (never null)
   */
  public record PostResult(int status, String body) {
  }
}
