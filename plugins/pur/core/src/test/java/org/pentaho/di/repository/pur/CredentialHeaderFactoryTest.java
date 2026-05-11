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
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.junit.After;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.pentaho.di.cli.auth.CredentialProvider;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CredentialHeaderFactoryTest {

  private static final String BASE_URL = "http://localhost:8080/pentaho";
  private static final String USERNAME = "alice";
  private static final String PASSWORD = "secret";
  private static final String ACCESS_TOKEN = "access-token";
  private static final String SESSION_COOKIE = "JSESSIONID=abc123";
  private static final String TRUST_PROPERTY = "pentaho.repository.client.attemptTrust";
  private static final String TRUST_HEADER = "_trust_user_";

  @After
  public void tearDown() {
    System.clearProperty( TRUST_PROPERTY );
  }

  @Test
  public void forSoapRequestPrefersBearerOverCookieAndTrust() {
    CredentialProvider provider = mock( CredentialProvider.class );
    when( provider.findAccessToken( BASE_URL ) ).thenReturn( Optional.of( ACCESS_TOKEN ) );
    when( provider.findSessionCookie( BASE_URL ) ).thenReturn( Optional.of( SESSION_COOKIE ) );
    System.setProperty( TRUST_PROPERTY, "true" );

    CredentialHeaderFactory factory = new CredentialHeaderFactory( provider );

    Map<String, List<String>> headers = factory.forSoapRequest( BASE_URL, USERNAME );

    assertEquals( List.of( "Bearer " + ACCESS_TOKEN ), headers.get( "Authorization" ) );
    assertFalse( headers.containsKey( "Cookie" ) );
    assertFalse( headers.containsKey( TRUST_HEADER ) );
  }

  @Test
  public void forSoapRequestReturnsCookieWhenBearerMissing() {
    CredentialProvider provider = mock( CredentialProvider.class );
    when( provider.findAccessToken( BASE_URL ) ).thenReturn( Optional.empty() );
    when( provider.findSessionCookie( BASE_URL ) ).thenReturn( Optional.of( SESSION_COOKIE ) );
    System.setProperty( TRUST_PROPERTY, "true" );

    CredentialHeaderFactory factory = new CredentialHeaderFactory( provider );

    Map<String, List<String>> headers = factory.forSoapRequest( BASE_URL, USERNAME );

    assertEquals( List.of( SESSION_COOKIE ), headers.get( "Cookie" ) );
    assertFalse( headers.containsKey( "Authorization" ) );
    assertFalse( headers.containsKey( TRUST_HEADER ) );
  }

  @Test
  public void forSoapRequestReturnsTrustHeaderWhenNoNonBasicCredentialExists() {
    CredentialProvider provider = mock( CredentialProvider.class );
    when( provider.findAccessToken( BASE_URL ) ).thenReturn( Optional.empty() );
    when( provider.findSessionCookie( BASE_URL ) ).thenReturn( Optional.empty() );
    System.setProperty( TRUST_PROPERTY, "true" );

    CredentialHeaderFactory factory = new CredentialHeaderFactory( provider );

    Map<String, List<String>> headers = factory.forSoapRequest( BASE_URL, USERNAME );

    assertEquals( List.of( USERNAME ), headers.get( TRUST_HEADER ) );
    assertEquals( 1, headers.size() );
  }

  @Test
  public void forSoapRequestReturnsEmptyMapForBasicFallback() {
    CredentialProvider provider = mock( CredentialProvider.class );
    when( provider.findAccessToken( BASE_URL ) ).thenReturn( Optional.empty() );
    when( provider.findSessionCookie( BASE_URL ) ).thenReturn( Optional.empty() );

    CredentialHeaderFactory factory = new CredentialHeaderFactory( provider );

    assertTrue( factory.forSoapRequest( BASE_URL, USERNAME ).isEmpty() );
    assertFalse( factory.hasNonBasicCredential( BASE_URL ) );
  }

  @Test
  public void registerJaxRsAuthAddsBearerHeaderFromProvider() throws Exception {
    CredentialProvider provider = mock( CredentialProvider.class );
    when( provider.findAccessToken( BASE_URL ) ).thenReturn( Optional.of( ACCESS_TOKEN ) );
    when( provider.findSessionCookie( BASE_URL ) ).thenReturn( Optional.of( SESSION_COOKIE ) );
    Client client = mock( Client.class );

    CredentialHeaderFactory factory = new CredentialHeaderFactory( provider );
    factory.registerJaxRsAuth( client, BASE_URL, USERNAME, PASSWORD );

    ArgumentCaptor<ClientRequestFilter> filterCaptor = ArgumentCaptor.forClass( ClientRequestFilter.class );
    verify( client ).register( filterCaptor.capture() );
    verify( client, never() ).register( any( HttpAuthenticationFeature.class ) );

    ClientRequestContext requestContext = mock( ClientRequestContext.class );
    MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
    when( requestContext.getHeaders() ).thenReturn( headers );

    filterCaptor.getValue().filter( requestContext );

    assertEquals( "Bearer " + ACCESS_TOKEN, headers.getFirst( "Authorization" ) );
  }

  @Test
  public void registerJaxRsAuthAddsSessionCookieWhenBearerMissing() throws Exception {
    CredentialProvider provider = mock( CredentialProvider.class );
    when( provider.findAccessToken( BASE_URL ) ).thenReturn( Optional.empty() );
    when( provider.findSessionCookie( BASE_URL ) ).thenReturn( Optional.of( SESSION_COOKIE ) );
    Client client = mock( Client.class );

    CredentialHeaderFactory factory = new CredentialHeaderFactory( provider );
    factory.registerJaxRsAuth( client, BASE_URL, USERNAME, PASSWORD );

    ArgumentCaptor<ClientRequestFilter> filterCaptor = ArgumentCaptor.forClass( ClientRequestFilter.class );
    verify( client ).register( filterCaptor.capture() );

    ClientRequestContext requestContext = mock( ClientRequestContext.class );
    MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
    when( requestContext.getHeaders() ).thenReturn( headers );

    filterCaptor.getValue().filter( requestContext );

    assertEquals( SESSION_COOKIE, headers.getFirst( "Cookie" ) );
  }

  @Test
  public void registerJaxRsAuthFallsBackToBasicFeatureWithoutNonBasicCredential() {
    CredentialProvider provider = mock( CredentialProvider.class );
    when( provider.findAccessToken( BASE_URL ) ).thenReturn( Optional.empty() );
    when( provider.findSessionCookie( BASE_URL ) ).thenReturn( Optional.empty() );
    Client client = mock( Client.class );

    CredentialHeaderFactory factory = new CredentialHeaderFactory( provider );
    factory.registerJaxRsAuth( client, BASE_URL, USERNAME, PASSWORD );

    verify( client ).register( any( HttpAuthenticationFeature.class ) );
    verify( client, never() ).register( any( ClientRequestFilter.class ) );
  }
}
