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

package org.pentaho.di.ui.repo.util;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class SsoProviderServiceTest {

  private static final int PORT = 18282;
  private static final String BASE_URL = "http://localhost:" + PORT;

  private HttpServer server;
  private SsoProviderService service;

  @Before
  public void setUp() throws IOException {
    service = new SsoProviderService();
    server = HttpServer.create( new InetSocketAddress( PORT ), 0 );
    server.setExecutor( null );
    server.start();
  }

  @After
  public void tearDown() {
    if ( server != null ) {
      server.stop( 0 );
    }
  }

  @Test
  public void testBuildProvidersUrl_appendsEndpoint() {
    assertEquals(
      "http://localhost:8080/pentaho/plugin/login/api/v0/oauth-providers",
      service.buildProvidersUrl( "http://localhost:8080/pentaho" ) );
  }

  @Test
  public void testBuildProvidersUrl_stripsTrailingSlash() {
    assertEquals(
      "http://localhost:8080/pentaho/plugin/login/api/v0/oauth-providers",
      service.buildProvidersUrl( "http://localhost:8080/pentaho/" ) );
  }

  @Test( expected = IllegalArgumentException.class )
  public void testBuildProvidersUrl_blankUrl_throws() {
    service.buildProvidersUrl( "   " );
  }

  @Test( expected = IllegalArgumentException.class )
  public void testBuildProvidersUrl_nullUrl_throws() {
    service.buildProvidersUrl( null );
  }

  @Test
  public void testFetchProviders_404_returnsEmptyList() throws Exception {
    registerHandler( "/pentaho/plugin/login/api/v0/oauth-providers", 404, "" );

    List<SsoProviderService.SsoProvider> result = service.fetchProviders( BASE_URL + "/pentaho" );
    assertNotNull( result );
    assertTrue( result.isEmpty() );
  }

  @Test
  public void testFetchProviders_500_throwsIOException() {
    registerHandler( "/pentaho/plugin/login/api/v0/oauth-providers", 500, "Internal Server Error" );

    try {
      service.fetchProviders( BASE_URL + "/pentaho" );
      fail( "Expected IOException" );
    } catch ( IOException e ) {
      assertTrue( e.getMessage().contains( "500" ) );
    }
  }

  @Test
  public void testFetchProviders_403_throwsIOException() {
    registerHandler( "/pentaho/plugin/login/api/v0/oauth-providers", 403, "Forbidden" );

    try {
      service.fetchProviders( BASE_URL + "/pentaho" );
      fail( "Expected IOException" );
    } catch ( IOException e ) {
      assertTrue( e.getMessage().contains( "403" ) );
    }
  }

  @Test
  public void testFetchProviders_notJsonArray_returnsEmptyList() throws Exception {
    registerHandler( "/pentaho/plugin/login/api/v0/oauth-providers", 200, "{\"key\":\"value\"}" );

    List<SsoProviderService.SsoProvider> result = service.fetchProviders( BASE_URL + "/pentaho" );
    assertNotNull( result );
    assertTrue( result.isEmpty() );
  }

  @Test
  public void testFetchProviders_malformedJson_throwsIOException() {
    registerHandler( "/pentaho/plugin/login/api/v0/oauth-providers", 200, "NOT_JSON{{{{" );

    try {
      service.fetchProviders( BASE_URL + "/pentaho" );
      fail( "Expected IOException" );
    } catch ( IOException e ) {
      assertTrue( e.getMessage().contains( "parse" ) );
    }
  }

  @Test
  public void testFetchProviders_invalidItems_skipped() throws Exception {
    String path = "/pentaho/plugin/login/api/v0/oauth-providers";
    Object[][] cases = {
      { "arrayItemNotObject", "[\"not-an-object\", 42]" },
      { "blankAuthorizationUri", "[{\"enabled\":true,\"clientName\":\"Google\","
        + "\"authorizationUri\":\"\",\"registrationId\":\"google\"}]" },
      { "nullClientName", "[{\"enabled\":true,"
        + "\"authorizationUri\":\"https://accounts.google.com/o/oauth2/auth\","
        + "\"registrationId\":\"google\"}]" }
    };

    for ( Object[] tc : cases ) {
      String label = (String) tc[0];
      String json = (String) tc[1];

      // Remove any previously registered context before re-registering
      try {
        server.removeContext( path );
      } catch ( IllegalArgumentException ignored ) {
        // Context doesn't exist yet on the first iteration
      }
      registerHandler( path, 200, json );

      List<SsoProviderService.SsoProvider> result = service.fetchProviders( BASE_URL + "/pentaho" );
      assertTrue( "Expected empty result for case: " + label, result.isEmpty() );
    }
  }

  @Test
  public void testFetchProviders_validProvider_booleanEnabled() throws Exception {
    String json = "[{\"enabled\":true,\"clientName\":\"Google\","
      + "\"authorizationUri\":\"https://accounts.google.com/o/oauth2/auth\","
      + "\"registrationId\":\"google\"}]";
    registerHandler( "/pentaho/plugin/login/api/v0/oauth-providers", 200, json );

    List<SsoProviderService.SsoProvider> result = service.fetchProviders( BASE_URL + "/pentaho" );
    assertEquals( 1, result.size() );
    SsoProviderService.SsoProvider p = result.get( 0 );
    assertEquals( "Google", p.clientName() );
    assertEquals( "https://accounts.google.com/o/oauth2/auth", p.authorizationUri() );
    assertEquals( "google", p.registrationId() );
  }

  @Test
  public void testFetchProviders_validProvider_stringEnabled() throws Exception {
    String json = "[{\"enabled\":\"true\",\"clientName\":\"Okta\","
      + "\"authorizationUri\":\"https://dev.okta.com/oauth2/v1/authorize\","
      + "\"registrationId\":\"okta\"}]";
    registerHandler( "/pentaho/plugin/login/api/v0/oauth-providers", 200, json );

    List<SsoProviderService.SsoProvider> result = service.fetchProviders( BASE_URL + "/pentaho" );
    assertEquals( 1, result.size() );
    assertEquals( "Okta", result.get( 0 ).clientName() );
  }

  @Test
  public void testFetchProviders_stringEnabledFalse_skipped() throws Exception {
    // "enabled" is the String "false" — getBoolean enters the instanceof String branch
    // and Boolean.parseBoolean("false") returns false, so the provider is skipped
    String json = "[{\"enabled\":\"false\",\"clientName\":\"Skipped\","
      + "\"authorizationUri\":\"https://example.com/auth\","
      + "\"registrationId\":\"skip\"}]";
    registerHandler( "/pentaho/plugin/login/api/v0/oauth-providers", 200, json );

    List<SsoProviderService.SsoProvider> result = service.fetchProviders( BASE_URL + "/pentaho" );
    assertTrue( result.isEmpty() );
  }

  @Test
  public void testFetchProviders_nullRegistrationId() throws Exception {
    String json = "[{\"enabled\":true,\"clientName\":\"Azure\","
      + "\"authorizationUri\":\"https://login.microsoftonline.com/common/oauth2/authorize\"}]";
    registerHandler( "/pentaho/plugin/login/api/v0/oauth-providers", 200, json );

    List<SsoProviderService.SsoProvider> result = service.fetchProviders( BASE_URL + "/pentaho" );
    assertEquals( 1, result.size() );
    assertNull( result.get( 0 ).registrationId() );
  }

  @Test
  public void testFetchProviders_multipleProviders() throws Exception {
    String json = "["
      + "{\"enabled\":true,\"clientName\":\"Google\","
      + "\"authorizationUri\":\"https://accounts.google.com/o/oauth2/auth\","
      + "\"registrationId\":\"google\"},"
      + "{\"enabled\":true,\"clientName\":\"Okta\","
      + "\"authorizationUri\":\"https://dev.okta.com/oauth2/v1/authorize\","
      + "\"registrationId\":\"okta\"}"
      + "]";
    registerHandler( "/pentaho/plugin/login/api/v0/oauth-providers", 200, json );

    List<SsoProviderService.SsoProvider> result = service.fetchProviders( BASE_URL + "/pentaho" );
    assertEquals( 2, result.size() );
  }

  @Test
  public void testFetchProviders_mixedItems_onlyValidReturned() throws Exception {
    String json = "["
      + "{\"enabled\":true,\"clientName\":\"Google\","
      + "\"authorizationUri\":\"https://accounts.google.com/o/oauth2/auth\","
      + "\"registrationId\":\"google\"},"
      + "{\"enabled\":false,\"clientName\":\"Disabled\","
      + "\"authorizationUri\":\"https://disabled.example.com\","
      + "\"registrationId\":\"disabled\"},"
      + "\"not-an-object\""
      + "]";
    registerHandler( "/pentaho/plugin/login/api/v0/oauth-providers", 200, json );

    List<SsoProviderService.SsoProvider> result = service.fetchProviders( BASE_URL + "/pentaho" );
    assertEquals( 1, result.size() );
    assertEquals( "Google", result.get( 0 ).clientName() );
  }

  @Test
  public void testFetchProviders_emptyArray_returnsEmptyList() throws Exception {
    registerHandler( "/pentaho/plugin/login/api/v0/oauth-providers", 200, "[]" );

    List<SsoProviderService.SsoProvider> result = service.fetchProviders( BASE_URL + "/pentaho" );
    assertTrue( result.isEmpty() );
  }

  @Test
  public void testSsoProvider_toString_returnsClientName() {
    SsoProviderService.SsoProvider provider =
      new SsoProviderService.SsoProvider( "Google", "https://example.com/auth", "google" );
    assertEquals( "Google", provider.toString() );
  }

  @Test
  public void testFetchProviders_malformedUrl_connectionIsNull_finallyHandlesGracefully() {
    // A URL with spaces causes MalformedURLException before connection is assigned,
    // so connection is null when the finally block runs (covers line 77-78)
    try {
      service.fetchProviders( "ht tp://invalid url" );
      fail( "Expected IOException" );
    } catch ( IOException e ) {
      assertNotNull( e );
    }
  }

  private void registerHandler( String path, int statusCode, String responseBody ) {
    server.createContext( path, ( HttpExchange exchange ) -> {
      byte[] bytes = responseBody.getBytes( java.nio.charset.StandardCharsets.UTF_8 );
      exchange.getResponseHeaders().set( "Content-Type", "application/json" );
      exchange.sendResponseHeaders( statusCode, bytes.length );
      try ( OutputStream out = exchange.getResponseBody() ) {
        out.write( bytes );
      }
    } );
  }
}
