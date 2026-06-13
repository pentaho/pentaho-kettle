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

import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SecretsManagementClientTest {

  private static final String REF = "11111111-2222-3333-4444-555555555555";
  private static final String BASE_URL = "http://secrets.test.local:8085";

  private CloseableHttpClient httpClient;
  private SecretsManagementClient client;

  @Before
  public void setUp() {
    System.setProperty( "SECRETS_MANAGEMENT_URL", BASE_URL );
    httpClient = mock( CloseableHttpClient.class );
    client = new SecretsManagementClient( httpClient );
  }

  @After
  public void tearDown() {
    System.clearProperty( "SECRETS_MANAGEMENT_URL" );
  }

  @Test
  public void getSecrets_returnsParsedMap_on200() throws Exception {
    stubResponse( 200, "{\"username\":\"alice\",\"password\":\"s3cret\"}" );

    Map<String, String> secrets = client.getSecrets( REF );

    assertEquals( "alice", secrets.get( "username" ) );
    assertEquals( "s3cret", secrets.get( "password" ) );
  }

  @Test
  public void getSecrets_buildsCorrectUrlAndAcceptHeader() throws Exception {
    // Build the response mock fully before entering the outer when(...) — calling
    // okResponse() inside thenReturn() triggers UnfinishedStubbing.
    CloseableHttpResponse fake = okResponse( "{}" );
    ArgumentCaptor<HttpUriRequest> captor = ArgumentCaptor.forClass( HttpUriRequest.class );
    when( httpClient.execute( captor.capture() ) ).thenReturn( fake );

    client.getSecrets( REF );

    HttpUriRequest sent = captor.getValue();
    assertEquals( BASE_URL + "/api/v1/secrets/" + REF, sent.getURI().toString() );
    assertEquals( "application/json", sent.getFirstHeader( "Accept" ).getValue() );
  }

  @Test
  public void getSecrets_stripsTrailingSlashFromBaseUrl() throws Exception {
    System.setProperty( "SECRETS_MANAGEMENT_URL", BASE_URL + "/" );
    CloseableHttpResponse fake = okResponse( "{}" );
    ArgumentCaptor<HttpUriRequest> captor = ArgumentCaptor.forClass( HttpUriRequest.class );
    when( httpClient.execute( captor.capture() ) ).thenReturn( fake );

    client.getSecrets( REF );

    assertEquals( BASE_URL + "/api/v1/secrets/" + REF, captor.getValue().getURI().toString() );
  }

  @Test
  public void getSecrets_mapsUnauthorizedTo401Message() throws Exception {
    stubResponse( 401, "{\"error\":\"some backend detail with maybe sensitive info\"}" );

    try {
      client.getSecrets( REF );
      fail( "expected SecretsManagementException" );
    } catch ( SecretsManagementException e ) {
      assertEquals( SecretsManagementException.Reason.UNAUTHORIZED, e.getReason() );
      // KettleException.getMessage() wraps with leading/trailing newlines; use contains.
      assertTrue( e.getMessage().contains( "Secret unauthorized or expired" ) );
      // The user-facing message must not leak the backend body.
      assertTrue( !e.getMessage().contains( "sensitive" ) );
    }
  }

  @Test
  public void getSecrets_mapsForbiddenTo401Message() throws Exception {
    stubResponse( 403, "denied" );

    try {
      client.getSecrets( REF );
      fail( "expected SecretsManagementException" );
    } catch ( SecretsManagementException e ) {
      assertEquals( SecretsManagementException.Reason.UNAUTHORIZED, e.getReason() );
      assertTrue( e.getMessage().contains( "Secret unauthorized or expired" ) );
    }
  }

  @Test
  public void getSecrets_mapsNotFoundTo404Message() throws Exception {
    stubResponse( 404, "{}" );

    try {
      client.getSecrets( REF );
      fail( "expected SecretsManagementException" );
    } catch ( SecretsManagementException e ) {
      assertEquals( SecretsManagementException.Reason.NOT_FOUND, e.getReason() );
      assertTrue( e.getMessage().contains( "Secret not found" ) );
      assertTrue( e.getMessage().contains( REF ) );
    }
  }

  @Test
  public void getSecrets_maps5xxToUnavailable() throws Exception {
    stubResponse( 503, "{}" );

    try {
      client.getSecrets( REF );
      fail( "expected SecretsManagementException" );
    } catch ( SecretsManagementException e ) {
      assertEquals( SecretsManagementException.Reason.UNAVAILABLE, e.getReason() );
      assertTrue( e.getMessage().contains( "Secret store unavailable" ) );
    }
  }

  @Test
  public void getSecrets_mapsSocketTimeoutToUnavailable() throws Exception {
    when( httpClient.execute( any( HttpUriRequest.class ) ) )
      .thenThrow( new SocketTimeoutException( "read timed out" ) );

    try {
      client.getSecrets( REF );
      fail( "expected SecretsManagementException" );
    } catch ( SecretsManagementException e ) {
      assertEquals( SecretsManagementException.Reason.UNAVAILABLE, e.getReason() );
      // KettleException.getMessage() concatenates the cause message; assert the user-facing
      // prefix without depending on cause formatting.
      assertTrue( e.getMessage().contains( "Secret store unavailable" ) );
      assertNotNull( "cause preserved for server-side debugging", e.getCause() );
    }
  }

  @Test
  public void getSecrets_mapsGenericIOExceptionToUnavailable() throws Exception {
    when( httpClient.execute( any( HttpUriRequest.class ) ) )
      .thenThrow( new IOException( "connection refused" ) );

    try {
      client.getSecrets( REF );
      fail( "expected SecretsManagementException" );
    } catch ( SecretsManagementException e ) {
      assertEquals( SecretsManagementException.Reason.UNAVAILABLE, e.getReason() );
      assertTrue( e.getMessage().contains( "Secret store unavailable" ) );
    }
  }

  @Test
  public void getSecrets_emptyBodyIsInvalidResponse() throws Exception {
    stubResponse( 200, "" );

    try {
      client.getSecrets( REF );
      fail( "expected SecretsManagementException" );
    } catch ( SecretsManagementException e ) {
      assertEquals( SecretsManagementException.Reason.INVALID_RESPONSE, e.getReason() );
    }
  }

  @Test
  public void getSecrets_malformedJsonIsInvalidResponse() throws Exception {
    stubResponse( 200, "not-json" );

    try {
      client.getSecrets( REF );
      fail( "expected SecretsManagementException" );
    } catch ( SecretsManagementException e ) {
      assertEquals( SecretsManagementException.Reason.INVALID_RESPONSE, e.getReason() );
      // Body content must not be echoed back in the user-facing message.
      assertTrue( !e.getMessage().contains( "not-json" ) );
    }
  }

  @Test
  public void getSecrets_blankRefThrowsIllegalState() {
    try {
      client.getSecrets( "  " );
      fail( "expected IllegalStateException" );
    } catch ( IllegalStateException expected ) {
      // ok
    } catch ( SecretsManagementException e ) {
      fail( "blank ref should not reach the HTTP layer" );
    }
  }

  @Test
  public void getSecrets_missingBaseUrlThrowsIllegalState() {
    System.clearProperty( "SECRETS_MANAGEMENT_URL" );
    try {
      client.getSecrets( REF );
      fail( "expected IllegalStateException" );
    } catch ( IllegalStateException expected ) {
      assertTrue( expected.getMessage().contains( "SECRETS_MANAGEMENT_URL" ) );
    } catch ( SecretsManagementException e ) {
      fail( "missing config should fail fast before the HTTP layer" );
    }
  }

  // ---------- helpers ----------

  private void stubResponse( int status, String body ) throws IOException {
    // Build the mock fully BEFORE entering when(...) so we don't nest stubbings,
    // which Mockito flags as UnfinishedStubbing.
    CloseableHttpResponse response = buildResponse( status, body );
    when( httpClient.execute( any( HttpUriRequest.class ) ) ).thenReturn( response );
  }

  private CloseableHttpResponse okResponse( String body ) {
    return buildResponse( 200, body );
  }

  private CloseableHttpResponse buildResponse( int status, String body ) {
    CloseableHttpResponse response = mock( CloseableHttpResponse.class );
    StatusLine line = mock( StatusLine.class );
    when( line.getStatusCode() ).thenReturn( status );
    when( response.getStatusLine() ).thenReturn( line );
    HttpEntity entity = new ByteArrayEntity( body.getBytes( StandardCharsets.UTF_8 ) );
    when( response.getEntity() ).thenReturn( entity );
    return response;
  }
}
