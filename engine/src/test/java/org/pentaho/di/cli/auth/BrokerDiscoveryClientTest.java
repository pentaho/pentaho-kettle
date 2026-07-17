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

import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class BrokerDiscoveryClientTest {

  @ClassRule public static final RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  private static final String SERVER_URL = "http://localhost:8080/pentaho";
  private static final String DISCOVERY_URL = SERVER_URL + "/api/pentaho-oauth/discovery";
  private static final String KEYCLOAK_DISCOVERY_URL = DISCOVERY_URL + "?registration_id=keycloak";
  private static final String AZURE = "azure";
  private static final String KEYCLOAK = "keycloak";
  private static final String VERSION = "version";

  @Test
  public void fetchDiscoveryParsesResponseAndCachesIt() throws Exception {
    OAuthHttpClient httpClient = spy( new OAuthHttpClient( 1000, 1000 ) );
    doReturn( discoveryJson( true, true, AZURE, KEYCLOAK ) )
      .when( httpClient ).getJson( DISCOVERY_URL );

    BrokerDiscoveryClient client = new BrokerDiscoveryClient( httpClient, TimeUnit.MINUTES.toMillis( 5 ) );

    Map<String, Object> discovery = client.fetchDiscovery( SERVER_URL + "/" );

    assertEquals( "1", discovery.get( VERSION ) );
    assertTrue( client.isBrokerOAuthAvailable( SERVER_URL ) );
    assertTrue( client.isClientCredentialsAvailable( SERVER_URL ) );
    assertTrue( client.isDpopEnabled( SERVER_URL ) );
    assertEquals( Arrays.asList( AZURE, KEYCLOAK ), client.getAvailableIdps( SERVER_URL ) );
    assertEquals( AZURE, discovery.get( "registrationId" ) );

    try {
      discovery.put( VERSION, "other" );
      fail( "Discovery map should be unmodifiable" );
    } catch ( UnsupportedOperationException expected ) {
      // expected
    }

    verify( httpClient, times( 1 ) ).getJson( DISCOVERY_URL );
  }

  @Test
  public void fetchDiscoveryRefetchesAfterTtlExpires() throws Exception {
    OAuthHttpClient httpClient = spy( new OAuthHttpClient( 1000, 1000 ) );
    doReturn( discoveryJson( false, false, AZURE ), discoveryJson( false, false, KEYCLOAK ) )
      .when( httpClient ).getJson( DISCOVERY_URL );

    BrokerDiscoveryClient client = new BrokerDiscoveryClient( httpClient, 0L );

    assertEquals( List.of( AZURE ), client.getAvailableIdps( SERVER_URL ) );

    assertEquals( List.of( KEYCLOAK ), client.getAvailableIdps( SERVER_URL ) );
    verify( httpClient, times( 2 ) ).getJson( DISCOVERY_URL );
  }

  @Test
  public void invalidateCacheForcesFreshFetch() throws Exception {
    OAuthHttpClient httpClient = spy( new OAuthHttpClient( 1000, 1000 ) );
    doReturn( discoveryJson( false, false, AZURE ), discoveryJson( true, true, KEYCLOAK ) )
      .when( httpClient ).getJson( DISCOVERY_URL );

    BrokerDiscoveryClient client = new BrokerDiscoveryClient( httpClient, TimeUnit.MINUTES.toMillis( 5 ) );

    assertEquals( List.of( AZURE ), client.getAvailableIdps( SERVER_URL ) );
    client.invalidateCache();

    assertEquals( List.of( KEYCLOAK ), client.getAvailableIdps( SERVER_URL ) );
    assertTrue( client.isDpopEnabled( SERVER_URL ) );
    verify( httpClient, times( 2 ) ).getJson( DISCOVERY_URL );
  }

  @Test
  public void fetchDiscoveryReturnsEmptyMapWhenBodyMissingOrRequestFails() throws Exception {
    OAuthHttpClient nullBodyClient = spy( new OAuthHttpClient( 1000, 1000 ) );
    doReturn( null ).when( nullBodyClient ).getJson( DISCOVERY_URL );

    BrokerDiscoveryClient nullBodyDiscoveryClient =
      new BrokerDiscoveryClient( nullBodyClient, TimeUnit.MINUTES.toMillis( 5 ) );

    assertEquals( Collections.emptyMap(), nullBodyDiscoveryClient.fetchDiscovery( SERVER_URL ) );
    assertFalse( nullBodyDiscoveryClient.isBrokerOAuthAvailable( SERVER_URL ) );
    assertEquals( Collections.<String>emptyList(), nullBodyDiscoveryClient.getAvailableIdps( SERVER_URL ) );

    OAuthHttpClient failingClient = spy( new OAuthHttpClient( 1000, 1000 ) );
    doThrow( new IOException( "boom" ) ).when( failingClient ).getJson( DISCOVERY_URL );

    BrokerDiscoveryClient failingDiscoveryClient =
      new BrokerDiscoveryClient( failingClient, TimeUnit.MINUTES.toMillis( 5 ) );

    assertEquals( Collections.emptyMap(), failingDiscoveryClient.fetchDiscovery( SERVER_URL ) );
    assertFalse( failingDiscoveryClient.isDpopEnabled( SERVER_URL ) );
  }

  @Test
  public void fetchDiscoveryWithPreferredIdpUsesQueryParamAndParsesSelectedIdpMetadata() throws Exception {
    OAuthHttpClient httpClient = spy( new OAuthHttpClient( 1000, 1000 ) );
    doReturn( discoveryJson( true, true, KEYCLOAK ) )
      .when( httpClient ).getJson( KEYCLOAK_DISCOVERY_URL );

    BrokerDiscoveryClient client = new BrokerDiscoveryClient( httpClient, TimeUnit.MINUTES.toMillis( 5 ) );

    Map<String, Object> discovery = client.fetchDiscovery( SERVER_URL, KEYCLOAK );

    assertEquals( KEYCLOAK, discovery.get( "registrationId" ) );
    assertEquals( "https://idp.example/auth/" + KEYCLOAK, discovery.get( "authorizationUri" ) );
    assertEquals( true, discovery.get( "requiresClientSecret" ) );
    assertEquals( "/pentaho/login/oauth2/code", discovery.get( "localRedirectPath" ) );
    verify( httpClient ).getJson( KEYCLOAK_DISCOVERY_URL );
  }

  private String discoveryJson( boolean dpopEnabled, boolean supportsClientCredentials, String... availableIdps ) {
    return "{"
      + "\"version\":\"1\","
      + "\"dpopEnabled\":" + dpopEnabled + ','
      + "\"supportsClientCredentials\":" + supportsClientCredentials + ','
      + "\"registrationId\":\"" + availableIdps[ 0 ] + "\","
      + "\"authorizationUri\":\"https://idp.example/auth/" + availableIdps[ 0 ] + "\","
      + "\"tokenUri\":\"https://idp.example/token/" + availableIdps[ 0 ] + "\","
      + "\"deviceCodeUri\":\"https://idp.example/device/" + availableIdps[ 0 ] + "\","
      + "\"clientId\":\"client-" + availableIdps[ 0 ] + "\","
      + "\"deviceCodeClientId\":\"device-" + availableIdps[ 0 ] + "\","
      + "\"serviceClientId\":\"service-" + availableIdps[ 0 ] + "\","
      + "\"scope\":\"openid profile email\","
      + "\"deviceCodeScope\":\"openid profile offline_access\","
      + "\"clientCredentialsScope\":\"api://default/.default\","
      + "\"effectiveClientAuthMethod\":\"client_secret_basic\","
      + "\"requiresClientSecret\":true,"
      + "\"serviceClientRequiresSecret\":true,"
      + "\"authorizationGrantType\":\"authorization_code\","
      + "\"clientAuthenticationMethod\":\"client_secret_basic\","
      + "\"localRedirectPath\":\"/pentaho/login/oauth2/code\","
      + "\"availableIdps\":" + toJsonArray( availableIdps )
      + "}";
  }

  private String toJsonArray( String[] values ) {
    StringBuilder json = new StringBuilder( "[" );
    for ( int i = 0; i < values.length; i++ ) {
      if ( i > 0 ) {
        json.append( ',' );
      }
      json.append( '"' ).append( values[ i ] ).append( '"' );
    }
    json.append( ']' );
    return json.toString();
  }
}
