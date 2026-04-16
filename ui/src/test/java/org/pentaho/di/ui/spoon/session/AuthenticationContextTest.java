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

package org.pentaho.di.ui.spoon.session;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link AuthenticationContext}.
 */
public class AuthenticationContextTest {

  private static final String SERVER_URL = "http://localhost:8080/pentaho";
  private URI serverUri;
  private AuthenticationStrategy mockStrategy;

  @BeforeClass
  public static void setUpClass() throws KettleException {
    KettleEnvironment.init();
  }

  @Before
  public void setUp() throws Exception {
    serverUri = new URI( SERVER_URL );
    mockStrategy = mock( AuthenticationStrategy.class );
    when( mockStrategy.getAuthType() ).thenReturn( "SESSION" );
  }

  // ===== Constructor Tests =====

  @Test
  public void testConstructorWithDefaultStrategy() {
    AuthenticationContext context = new AuthenticationContext( serverUri );
    assertNotNull( context );
    assertEquals( "SESSION", context.getAuthType() );
  }

  @Test
  public void testConstructorWithExplicitStrategy() {
    AuthenticationContext context = new AuthenticationContext( serverUri, mockStrategy );
    assertNotNull( context );
    assertEquals( "SESSION", context.getAuthType() );
  }

  @Test( expected = IllegalArgumentException.class )
  public void testConstructorNullUri() {
    new AuthenticationContext( null, mockStrategy );
  }

  @Test( expected = IllegalArgumentException.class )
  public void testConstructorNullStrategy() {
    new AuthenticationContext( serverUri, null );
  }

  @Test( expected = IllegalArgumentException.class )
  public void testConstructorBothNull() {
    new AuthenticationContext( null, null );
  }

  // ===== getAuthType Tests =====

  @Test
  public void testGetAuthType() {
    when( mockStrategy.getAuthType() ).thenReturn( "OAUTH" );
    AuthenticationContext context = new AuthenticationContext( serverUri, mockStrategy );
    assertEquals( "OAUTH", context.getAuthType() );
  }

  // ===== getCredentials Tests =====

  @Test
  public void testGetCredentials() {
    Map<String, Object> expectedCreds = new HashMap<>();
    expectedCreds.put( "jsessionid", "ABC123" );
    when( mockStrategy.getCredentials( serverUri ) ).thenReturn( expectedCreds );

    AuthenticationContext context = new AuthenticationContext( serverUri, mockStrategy );
    Map<String, Object> creds = context.getCredentials();

    assertNotNull( creds );
    assertEquals( "ABC123", creds.get( "jsessionid" ) );
    verify( mockStrategy ).getCredentials( serverUri );
  }

  @Test
  public void testGetCredentialsReturnsNullWhenNoneStored() {
    when( mockStrategy.getCredentials( serverUri ) ).thenReturn( null );

    AuthenticationContext context = new AuthenticationContext( serverUri, mockStrategy );
    assertNull( context.getCredentials() );
  }

  // ===== isAuthenticated Tests =====

  @Test
  public void testIsAuthenticatedTrue() {
    when( mockStrategy.isAuthenticated( serverUri ) ).thenReturn( true );

    AuthenticationContext context = new AuthenticationContext( serverUri, mockStrategy );
    assertTrue( context.isAuthenticated() );
    verify( mockStrategy ).isAuthenticated( serverUri );
  }

  @Test
  public void testIsAuthenticatedFalse() {
    when( mockStrategy.isAuthenticated( serverUri ) ).thenReturn( false );

    AuthenticationContext context = new AuthenticationContext( serverUri, mockStrategy );
    assertFalse( context.isAuthenticated() );
  }

  // ===== clearCredentials Tests =====

  @Test
  public void testClearCredentials() {
    AuthenticationContext context = new AuthenticationContext( serverUri, mockStrategy );
    context.clearCredentials();
    verify( mockStrategy ).clearCredentials( serverUri );
  }

  @Test
  public void testGetCredentialValue() {
    when( mockStrategy.getCredentialValue( serverUri, "myKey" ) ).thenReturn( "myValue" );

    AuthenticationContext context = new AuthenticationContext( serverUri, mockStrategy );
    assertEquals( "myValue", context.getCredentialValue( "myKey" ) );
    verify( mockStrategy ).getCredentialValue( serverUri, "myKey" );
  }

  @Test
  public void testGetCredentialValueReturnsNull() {
    when( mockStrategy.getCredentialValue( serverUri, "nonexistent" ) ).thenReturn( null );

    AuthenticationContext context = new AuthenticationContext( serverUri, mockStrategy );
    assertNull( context.getCredentialValue( "nonexistent" ) );
  }

  @Test
  public void testStoreCredentialValue() {
    AuthenticationContext context = new AuthenticationContext( serverUri, mockStrategy );
    context.storeCredentialValue( "key1", "value1" );
    verify( mockStrategy ).storeCredentialValue( serverUri, "key1", "value1" );
  }

  @Test
  public void testGetJSessionIdWithSessionStrategy() {
    SessionBasedAuthStrategy sessionStrategy = mock( SessionBasedAuthStrategy.class );
    when( sessionStrategy.getAuthType() ).thenReturn( "SESSION" );
    when( sessionStrategy.getJSessionId( serverUri ) ).thenReturn( "SESSION123" );

    AuthenticationContext context = new AuthenticationContext( serverUri, sessionStrategy );
    assertEquals( "SESSION123", context.getJSessionId() );
    verify( sessionStrategy ).getJSessionId( serverUri );
  }

  @Test
  public void testGetJSessionIdWithNonSessionStrategy() {
    when( mockStrategy.getCredentialValue( serverUri, "jsessionid" ) ).thenReturn( "GENERIC123" );

    AuthenticationContext context = new AuthenticationContext( serverUri, mockStrategy );
    assertEquals( "GENERIC123", context.getJSessionId() );
    verify( mockStrategy ).getCredentialValue( serverUri, "jsessionid" );
  }

  @Test
  public void testGetJSessionIdReturnsNullWhenNotSet() {
    when( mockStrategy.getCredentialValue( serverUri, "jsessionid" ) ).thenReturn( null );

    AuthenticationContext context = new AuthenticationContext( serverUri, mockStrategy );
    assertNull( context.getJSessionId() );
  }

  @Test
  public void testStoreJSessionIdWithSessionStrategy() {
    SessionBasedAuthStrategy sessionStrategy = mock( SessionBasedAuthStrategy.class );
    when( sessionStrategy.getAuthType() ).thenReturn( "SESSION" );

    AuthenticationContext context = new AuthenticationContext( serverUri, sessionStrategy );
    context.storeJSessionId( "NEW_SESSION_ID" );
    verify( sessionStrategy ).storeJSessionId( serverUri, "NEW_SESSION_ID" );
  }

  @Test
  public void testStoreJSessionIdWithNonSessionStrategy() {
    AuthenticationContext context = new AuthenticationContext( serverUri, mockStrategy );
    context.storeJSessionId( "FALLBACK_ID" );
    verify( mockStrategy ).storeCredentialValue( serverUri, "jsessionid", "FALLBACK_ID" );
  }

  @Test
  public void testIsSessionValidReturnsFalseWhenNotAuthenticated() {
    when( mockStrategy.isAuthenticated( serverUri ) ).thenReturn( false );

    AuthenticationContext context = new AuthenticationContext( serverUri, mockStrategy );
    assertFalse( context.isSessionValid() );
  }

  @Test
  public void testIsSessionValidReturnsFalseWhenNoJSessionId() {
    when( mockStrategy.isAuthenticated( serverUri ) ).thenReturn( true );
    when( mockStrategy.getCredentialValue( serverUri, "jsessionid" ) ).thenReturn( null );

    AuthenticationContext context = new AuthenticationContext( serverUri, mockStrategy );
    assertFalse( context.isSessionValid() );
  }

  @Test
  public void testIsSessionValidReturnsFalseWhenEmptyJSessionId() {
    when( mockStrategy.isAuthenticated( serverUri ) ).thenReturn( true );
    when( mockStrategy.getCredentialValue( serverUri, "jsessionid" ) ).thenReturn( "  " );

    AuthenticationContext context = new AuthenticationContext( serverUri, mockStrategy );
    assertFalse( context.isSessionValid() );
  }

  @Test
  public void testValidateAndClearIfExpiredReturnsFalseWhenNotAuthenticated() {
    when( mockStrategy.isAuthenticated( serverUri ) ).thenReturn( false );

    AuthenticationContext context = new AuthenticationContext( serverUri, mockStrategy );
    assertFalse( context.validateAndClearIfExpired() );
    // Should not attempt to clear credentials
    verify( mockStrategy, never() ).clearCredentials( any() );
  }

  @Test
  public void testWasPreviouslyAuthenticatedWithSessionStrategy() {
    SessionBasedAuthStrategy sessionStrategy = mock( SessionBasedAuthStrategy.class );
    when( sessionStrategy.getAuthType() ).thenReturn( "SESSION" );
    when( sessionStrategy.hasBrowserAuthMarker( serverUri ) ).thenReturn( true );

    AuthenticationContext context = new AuthenticationContext( serverUri, sessionStrategy );
    assertTrue( context.wasPreviouslyAuthenticated() );
    verify( sessionStrategy ).hasBrowserAuthMarker( serverUri );
  }

  @Test
  public void testWasPreviouslyAuthenticatedReturnsFalseWithNoBrowserMarker() {
    SessionBasedAuthStrategy sessionStrategy = mock( SessionBasedAuthStrategy.class );
    when( sessionStrategy.getAuthType() ).thenReturn( "SESSION" );
    when( sessionStrategy.hasBrowserAuthMarker( serverUri ) ).thenReturn( false );

    AuthenticationContext context = new AuthenticationContext( serverUri, sessionStrategy );
    assertFalse( context.wasPreviouslyAuthenticated() );
  }

  @Test
  public void testWasPreviouslyAuthenticatedReturnsFalseForNonSessionStrategy() {
    AuthenticationContext context = new AuthenticationContext( serverUri, mockStrategy );
    assertFalse( context.wasPreviouslyAuthenticated() );
  }

  @Test
  public void testToStringContainsRelevantInfo() {
    when( mockStrategy.isAuthenticated( serverUri ) ).thenReturn( true );

    AuthenticationContext context = new AuthenticationContext( serverUri, mockStrategy );
    String result = context.toString();

    assertTrue( result.contains( "serverUri=" ) );
    assertTrue( result.contains( "localhost" ) );
    assertTrue( result.contains( "strategyType=SESSION" ) );
    assertTrue( result.contains( "authenticated=true" ) );
  }

  @Test
  public void testToStringNotAuthenticated() {
    when( mockStrategy.isAuthenticated( serverUri ) ).thenReturn( false );

    AuthenticationContext context = new AuthenticationContext( serverUri, mockStrategy );
    String result = context.toString();

    assertTrue( result.contains( "authenticated=false" ) );
  }

  private AuthenticationContext createContextWithMockedClient( URI uri, AuthenticationStrategy strategy,
                                                               int httpStatus ) {
    Client mockClient = mock( Client.class );
    WebTarget mockTarget = mock( WebTarget.class );
    Invocation.Builder mockBuilder = mock( Invocation.Builder.class );
    Response mockResponse = mock( Response.class );

    when( mockClient.target( anyString() ) ).thenReturn( mockTarget );
    when( mockTarget.request() ).thenReturn( mockBuilder );
    when( mockBuilder.header( anyString(), any() ) ).thenReturn( mockBuilder );
    when( mockBuilder.get() ).thenReturn( mockResponse );
    when( mockResponse.getStatus() ).thenReturn( httpStatus );

    return new AuthenticationContext( uri, strategy ) {
      @Override Client createClient() {
        return mockClient;
      }
    };
  }

  private AuthenticationContext createContextWithThrowingClient( URI uri, AuthenticationStrategy strategy ) {
    Client mockClient = mock( Client.class );
    when( mockClient.target( anyString() ) ).thenThrow( new RuntimeException( "connection refused" ) );

    return new AuthenticationContext( uri, strategy ) {
      @Override Client createClient() {
        return mockClient;
      }
    };
  }

  @Test
  public void isSessionValidReturnsFalseOnNonSuccessHttpStatuses() {
    for ( int status : new int[] { 100, 401, 403, 500 } ) {
      when( mockStrategy.isAuthenticated( serverUri ) ).thenReturn( true );
      when( mockStrategy.getCredentialValue( serverUri, "jsessionid" ) ).thenReturn( "VALID_ID" );

      AuthenticationContext context = createContextWithMockedClient( serverUri, mockStrategy, status );
      assertFalse( "Expected isSessionValid() to return false for HTTP " + status,
        context.isSessionValid() );
    }
  }

  @Test
  public void isSessionValidReturnsTrueOnHttp200() {
    when( mockStrategy.isAuthenticated( serverUri ) ).thenReturn( true );
    when( mockStrategy.getCredentialValue( serverUri, "jsessionid" ) ).thenReturn( "VALID_ID" );

    AuthenticationContext context = createContextWithMockedClient( serverUri, mockStrategy, 200 );
    assertTrue( context.isSessionValid() );
  }

  @Test
  public void isSessionValidReturnsTrueOnHttp302() {
    when( mockStrategy.isAuthenticated( serverUri ) ).thenReturn( true );
    when( mockStrategy.getCredentialValue( serverUri, "jsessionid" ) ).thenReturn( "VALID_ID" );

    AuthenticationContext context = createContextWithMockedClient( serverUri, mockStrategy, 302 );
    assertTrue( context.isSessionValid() );
  }

  @Test
  public void isSessionValidReturnsFalseOnException() {
    when( mockStrategy.isAuthenticated( serverUri ) ).thenReturn( true );
    when( mockStrategy.getCredentialValue( serverUri, "jsessionid" ) ).thenReturn( "VALID_ID" );

    AuthenticationContext context = createContextWithThrowingClient( serverUri, mockStrategy );
    assertFalse( context.isSessionValid() );
  }

  @Test
  public void isSessionValidReturnsFalseWhenResponseThrowsException() {
    when( mockStrategy.isAuthenticated( serverUri ) ).thenReturn( true );
    when( mockStrategy.getCredentialValue( serverUri, "jsessionid" ) ).thenReturn( "VALID_ID" );

    Client mockClient = mock( Client.class );
    WebTarget mockTarget = mock( WebTarget.class );
    Invocation.Builder mockBuilder = mock( Invocation.Builder.class );
    Response mockResponse = mock( Response.class );

    when( mockClient.target( anyString() ) ).thenReturn( mockTarget );
    when( mockTarget.request() ).thenReturn( mockBuilder );
    when( mockBuilder.header( anyString(), any() ) ).thenReturn( mockBuilder );
    when( mockBuilder.get() ).thenReturn( mockResponse );
    when( mockResponse.getStatus() ).thenThrow( new RuntimeException( "response processing failed" ) );

    AuthenticationContext context = new AuthenticationContext( serverUri, mockStrategy ) {
      @Override Client createClient() {
        return mockClient;
      }
    };

    assertFalse( context.isSessionValid() );
  }

  @Test
  public void isSessionValidReturnsFalseWhenClientCloseThrowsException() {
    when( mockStrategy.isAuthenticated( serverUri ) ).thenReturn( true );
    when( mockStrategy.getCredentialValue( serverUri, "jsessionid" ) ).thenReturn( "VALID_ID" );

    Client mockClient = mock( Client.class );
    when( mockClient.target( anyString() ) ).thenThrow( new RuntimeException( "target failed" ) );
    org.mockito.Mockito.doThrow( new RuntimeException( "close failed" ) ).when( mockClient ).close();

    AuthenticationContext context = new AuthenticationContext( serverUri, mockStrategy ) {
      @Override Client createClient() {
        return mockClient;
      }
    };

    assertFalse( context.isSessionValid() );
  }

  @Test
  public void isSessionValidReturnsFalseWhenBothResponseCloseAndClientCloseThrow() {
    when( mockStrategy.isAuthenticated( serverUri ) ).thenReturn( true );
    when( mockStrategy.getCredentialValue( serverUri, "jsessionid" ) ).thenReturn( "VALID_ID" );

    Client mockClient = mock( Client.class );
    WebTarget mockTarget = mock( WebTarget.class );
    Invocation.Builder mockBuilder = mock( Invocation.Builder.class );
    Response mockResponse = mock( Response.class );

    when( mockClient.target( anyString() ) ).thenReturn( mockTarget );
    when( mockTarget.request() ).thenReturn( mockBuilder );
    when( mockBuilder.header( anyString(), any() ) ).thenReturn( mockBuilder );
    when( mockBuilder.get() ).thenReturn( mockResponse );
    when( mockResponse.getStatus() ).thenReturn( 200 );
    org.mockito.Mockito.doThrow( new RuntimeException( "response close failed" ) ).when( mockResponse ).close();
    org.mockito.Mockito.doThrow( new RuntimeException( "client close failed" ) ).when( mockClient ).close();

    AuthenticationContext context = new AuthenticationContext( serverUri, mockStrategy ) {
      @Override Client createClient() {
        return mockClient;
      }
    };

    assertFalse( context.isSessionValid() );
  }

  @Test
  public void isSessionValidReturnsFalseWhenGetReturnsNull() {
    when( mockStrategy.isAuthenticated( serverUri ) ).thenReturn( true );
    when( mockStrategy.getCredentialValue( serverUri, "jsessionid" ) ).thenReturn( "VALID_ID" );

    Client mockClient = mock( Client.class );
    WebTarget mockTarget = mock( WebTarget.class );
    Invocation.Builder mockBuilder = mock( Invocation.Builder.class );

    when( mockClient.target( anyString() ) ).thenReturn( mockTarget );
    when( mockTarget.request() ).thenReturn( mockBuilder );
    when( mockBuilder.header( anyString(), any() ) ).thenReturn( mockBuilder );
    when( mockBuilder.get() ).thenReturn( null );

    AuthenticationContext context = new AuthenticationContext( serverUri, mockStrategy ) {
      @Override Client createClient() {
        return mockClient;
      }
    };

    assertFalse( context.isSessionValid() );
  }

  @Test
  public void isSessionValidReturnsFalseWhenResponseBodyAndCloseThrowExceptions() {
    when( mockStrategy.isAuthenticated( serverUri ) ).thenReturn( true );
    when( mockStrategy.getCredentialValue( serverUri, "jsessionid" ) ).thenReturn( "VALID_ID" );

    Client mockClient = mock( Client.class );
    WebTarget mockTarget = mock( WebTarget.class );
    Invocation.Builder mockBuilder = mock( Invocation.Builder.class );
    Response mockResponse = mock( Response.class );

    when( mockClient.target( anyString() ) ).thenReturn( mockTarget );
    when( mockTarget.request() ).thenReturn( mockBuilder );
    when( mockBuilder.header( anyString(), any() ) ).thenReturn( mockBuilder );
    when( mockBuilder.get() ).thenReturn( mockResponse );
    when( mockResponse.getStatus() ).thenThrow( new RuntimeException( "status failed" ) );
    org.mockito.Mockito.doThrow( new RuntimeException( "response close failed" ) ).when( mockResponse ).close();

    AuthenticationContext context = new AuthenticationContext( serverUri, mockStrategy ) {
      @Override Client createClient() {
        return mockClient;
      }
    };

    assertFalse( context.isSessionValid() );
  }

  @Test
  public void isSessionValidReturnsFalseWhenResponseCloseThrowsException() {
    when( mockStrategy.isAuthenticated( serverUri ) ).thenReturn( true );
    when( mockStrategy.getCredentialValue( serverUri, "jsessionid" ) ).thenReturn( "VALID_ID" );

    Client mockClient = mock( Client.class );
    WebTarget mockTarget = mock( WebTarget.class );
    Invocation.Builder mockBuilder = mock( Invocation.Builder.class );
    Response mockResponse = mock( Response.class );

    when( mockClient.target( anyString() ) ).thenReturn( mockTarget );
    when( mockTarget.request() ).thenReturn( mockBuilder );
    when( mockBuilder.header( anyString(), any() ) ).thenReturn( mockBuilder );
    when( mockBuilder.get() ).thenReturn( mockResponse );
    when( mockResponse.getStatus() ).thenReturn( 200 );
    org.mockito.Mockito.doThrow( new RuntimeException( "response close failed" ) ).when( mockResponse ).close();

    AuthenticationContext context = new AuthenticationContext( serverUri, mockStrategy ) {
      @Override Client createClient() {
        return mockClient;
      }
    };

    assertFalse( context.isSessionValid() );
  }

  @Test
  public void isSessionValidBuildsUrlWithTrailingSlash() throws Exception {
    URI uriWithSlash = new URI( "http://localhost:8080/pentaho/" );
    when( mockStrategy.isAuthenticated( uriWithSlash ) ).thenReturn( true );
    when( mockStrategy.getCredentialValue( uriWithSlash, "jsessionid" ) ).thenReturn( "ID123" );

    Client mockClient = mock( Client.class );
    WebTarget mockTarget = mock( WebTarget.class );
    Invocation.Builder mockBuilder = mock( Invocation.Builder.class );
    Response mockResponse = mock( Response.class );

    when( mockClient.target( anyString() ) ).thenReturn( mockTarget );
    when( mockTarget.request() ).thenReturn( mockBuilder );
    when( mockBuilder.header( anyString(), any() ) ).thenReturn( mockBuilder );
    when( mockBuilder.get() ).thenReturn( mockResponse );
    when( mockResponse.getStatus() ).thenReturn( 200 );

    AuthenticationContext context = new AuthenticationContext( uriWithSlash, mockStrategy ) {
      @Override Client createClient() {
        return mockClient;
      }
    };

    assertTrue( context.isSessionValid() );
    // URL already ends with "/" so should not double it
    verify( mockClient ).target( "http://localhost:8080/pentaho/api/system/authentication-provider" );
  }

  @Test
  public void isSessionValidBuildsUrlWithoutTrailingSlash() throws Exception {
    URI uriNoSlash = new URI( "http://localhost:8080/pentaho" );
    when( mockStrategy.isAuthenticated( uriNoSlash ) ).thenReturn( true );
    when( mockStrategy.getCredentialValue( uriNoSlash, "jsessionid" ) ).thenReturn( "ID123" );

    Client mockClient = mock( Client.class );
    WebTarget mockTarget = mock( WebTarget.class );
    Invocation.Builder mockBuilder = mock( Invocation.Builder.class );
    Response mockResponse = mock( Response.class );

    when( mockClient.target( anyString() ) ).thenReturn( mockTarget );
    when( mockTarget.request() ).thenReturn( mockBuilder );
    when( mockBuilder.header( anyString(), any() ) ).thenReturn( mockBuilder );
    when( mockBuilder.get() ).thenReturn( mockResponse );
    when( mockResponse.getStatus() ).thenReturn( 200 );

    AuthenticationContext context = new AuthenticationContext( uriNoSlash, mockStrategy ) {
      @Override Client createClient() {
        return mockClient;
      }
    };

    assertTrue( context.isSessionValid() );
    // URL does not end with "/" so it should be appended
    verify( mockClient ).target( "http://localhost:8080/pentaho/api/system/authentication-provider" );
  }

  @Test
  public void isSessionValidSendsCookieHeader() {
    when( mockStrategy.isAuthenticated( serverUri ) ).thenReturn( true );
    when( mockStrategy.getCredentialValue( serverUri, "jsessionid" ) ).thenReturn( "MY_SESSION" );

    Client mockClient = mock( Client.class );
    WebTarget mockTarget = mock( WebTarget.class );
    Invocation.Builder mockBuilder = mock( Invocation.Builder.class );
    Response mockResponse = mock( Response.class );

    when( mockClient.target( anyString() ) ).thenReturn( mockTarget );
    when( mockTarget.request() ).thenReturn( mockBuilder );
    when( mockBuilder.header( anyString(), any() ) ).thenReturn( mockBuilder );
    when( mockBuilder.get() ).thenReturn( mockResponse );
    when( mockResponse.getStatus() ).thenReturn( 200 );

    AuthenticationContext context = new AuthenticationContext( serverUri, mockStrategy ) {
      @Override Client createClient() {
        return mockClient;
      }
    };

    context.isSessionValid();
    verify( mockBuilder ).header( "Cookie", "JSESSIONID=MY_SESSION" );
  }

  @Test
  public void validateAndClearIfExpiredReturnsTrueWhenSessionIsValid() {
    when( mockStrategy.isAuthenticated( serverUri ) ).thenReturn( true );
    when( mockStrategy.getCredentialValue( serverUri, "jsessionid" ) ).thenReturn( "VALID" );

    AuthenticationContext context = createContextWithMockedClient( serverUri, mockStrategy, 200 );
    assertTrue( context.validateAndClearIfExpired() );
    verify( mockStrategy, never() ).clearCredentials( any() );
    verify( mockStrategy, never() ).clearCredentialValue( any(), any() );
    verify( mockStrategy, never() ).storeCredentialValue( any(), any(), any() );
  }

  @Test
  public void validateAndClearIfExpiredClearsOnlyJSessionIdForSessionStrategy() {
    SessionBasedAuthStrategy sessionStrategy = mock( SessionBasedAuthStrategy.class );
    when( sessionStrategy.getAuthType() ).thenReturn( "SESSION" );
    when( sessionStrategy.isAuthenticated( serverUri ) ).thenReturn( true );
    when( sessionStrategy.getCredentialValue( serverUri, "jsessionid" ) ).thenReturn( "EXPIRED" );

    AuthenticationContext context = createContextWithMockedClient( serverUri, sessionStrategy, 401 );
    assertFalse( context.validateAndClearIfExpired() );
    verify( sessionStrategy ).clearCredentialValue( serverUri, "jsessionid" );
    verify( sessionStrategy, never() ).clearCredentials( serverUri );
  }

  @Test
  public void validateAndClearIfExpiredClearsAllCredentialsForNonSessionStrategy() {
    when( mockStrategy.isAuthenticated( serverUri ) ).thenReturn( true );
    when( mockStrategy.getCredentialValue( serverUri, "jsessionid" ) ).thenReturn( "EXPIRED" );

    AuthenticationContext context = createContextWithMockedClient( serverUri, mockStrategy, 401 );
    assertFalse( context.validateAndClearIfExpired() );
    verify( mockStrategy ).clearCredentials( serverUri );
  }

  @Test
  public void validateAndClearIfExpiredClearsOnExceptionForNonSessionStrategy() {
    when( mockStrategy.isAuthenticated( serverUri ) ).thenReturn( true );
    when( mockStrategy.getCredentialValue( serverUri, "jsessionid" ) ).thenReturn( "VALID" );

    AuthenticationContext context = createContextWithThrowingClient( serverUri, mockStrategy );
    assertFalse( context.validateAndClearIfExpired() );
    verify( mockStrategy ).clearCredentials( serverUri );
  }

  @Test
  public void sessionAuthPasswordConstantIsNotNullOrEmpty() {
    assertNotNull( AuthenticationContext.SESSION_AUTH_TOKEN );
    assertFalse( AuthenticationContext.SESSION_AUTH_TOKEN.isEmpty() );
  }

  @Test
  public void testFullFlowWithRealSessionStrategy() {
    SessionBasedAuthStrategy realStrategy = new SessionBasedAuthStrategy();
    AuthenticationContext context = new AuthenticationContext( serverUri, realStrategy );

    // Initially not authenticated
    assertFalse( context.isAuthenticated() );
    assertNull( context.getJSessionId() );
    assertFalse( context.wasPreviouslyAuthenticated() );

    // Store a session ID
    context.storeJSessionId( "REAL_SESSION_123" );

    // Now should be authenticated
    assertTrue( context.isAuthenticated() );
    assertEquals( "REAL_SESSION_123", context.getJSessionId() );
    assertTrue( context.wasPreviouslyAuthenticated() );

    // Clear credentials
    context.clearCredentials();
    assertFalse( context.isAuthenticated() );
    assertNull( context.getJSessionId() );
  }

  @Test
  public void testMultipleServersWithRealStrategy() throws Exception {
    SessionBasedAuthStrategy realStrategy = new SessionBasedAuthStrategy();
    URI server1 = new URI( "http://server1:8080/pentaho" );
    URI server2 = new URI( "http://server2:9090/pentaho" );

    AuthenticationContext context1 = new AuthenticationContext( server1, realStrategy );
    AuthenticationContext context2 = new AuthenticationContext( server2, realStrategy );

    context1.storeJSessionId( "SESSION_SERVER1" );
    context2.storeJSessionId( "SESSION_SERVER2" );

    assertEquals( "SESSION_SERVER1", context1.getJSessionId() );
    assertEquals( "SESSION_SERVER2", context2.getJSessionId() );

    // Clearing one doesn't affect the other
    context1.clearCredentials();
    assertFalse( context1.isAuthenticated() );
    assertTrue( context2.isAuthenticated() );
    assertEquals( "SESSION_SERVER2", context2.getJSessionId() );
  }

  @Test
  public void testStoreCredentialValueWithRealStrategy() {
    SessionBasedAuthStrategy realStrategy = new SessionBasedAuthStrategy();
    AuthenticationContext context = new AuthenticationContext( serverUri, realStrategy );

    context.storeCredentialValue( "custom_key", "custom_value" );
    assertEquals( "custom_value", context.getCredentialValue( "custom_key" ) );
  }

  @Test
  public void testGetCredentialsMapWithRealStrategy() {
    SessionBasedAuthStrategy realStrategy = new SessionBasedAuthStrategy();
    AuthenticationContext context = new AuthenticationContext( serverUri, realStrategy );

    context.storeJSessionId( "MAP_TEST_SESSION" );
    Map<String, Object> creds = context.getCredentials();

    assertNotNull( creds );
    assertEquals( "MAP_TEST_SESSION", creds.get( "jsessionid" ) );
  }

  @Test
  public void testCreateClientReturnsNotNullClient() {
    AuthenticationContext context = new AuthenticationContext( serverUri, mockStrategy );
    Client client = context.createClient();
    assertNotNull( client );
  }
}

