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


package com.pentaho.di.purge;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.pentaho.di.ui.spoon.session.AuthenticationContext;
import org.pentaho.di.ui.spoon.session.SpoonSessionManager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.pentaho.test.util.InternalState.setInternalState;

public class RepositoryCleanupUtilTest {

  @Test
  public void authenticateLoginCredentials() throws Exception {
    RepositoryCleanupUtil util = mock( RepositoryCleanupUtil.class );
    doCallRealMethod().when( util ).authenticateLoginCredentials();

    setInternalState( util, "url", "http://localhost:8080/pentaho" );
    setInternalState( util, "username", "admin" );
    setInternalState( util, "password", "Encrypted 2be98afc86aa7f2e4bb18bd63c99dbdde" );

    WebTarget mockTarget = mock( WebTarget.class );
    Invocation.Builder builder = mock( Invocation.Builder.class );
    when( mockTarget.request( MediaType.TEXT_PLAIN ) ).thenReturn( builder );
    when( builder.get( String.class ) ).thenReturn( "true" );

    Client mockClient = mock( Client.class );
    HttpAuthenticationFeature feature = HttpAuthenticationFeature.basic( "admin", "password" );
    mockClient.register( feature );
    when(mockClient.target( anyString() ) ).thenReturn( mockTarget );
    setInternalState( util, "client", mockClient );

    try( MockedStatic<ClientBuilder> mockedClientBuilder = mockStatic( ClientBuilder.class ) ) {
      mockedClientBuilder.when( ClientBuilder::newClient ).thenReturn( mockClient );

      util.authenticateLoginCredentials();

      verify( mockClient, times(1 )).register( any( HttpAuthenticationFeature.class ) );
    }
  }

  @Test
  public void testAuthenticateLoginCredentials_SessionAuth_Authenticated() throws Exception {
    RepositoryCleanupUtil util = mock( RepositoryCleanupUtil.class );
    doCallRealMethod().when( util ).authenticateLoginCredentials();

    String serverUrl = "http://localhost:8080/pentaho";
    setInternalState( util, "url", serverUrl );
    setInternalState( util, "username", "admin" );
    setInternalState( util, "password", AuthenticationContext.SESSION_AUTH_TOKEN );
    setInternalState( util, "client", null );

    // Mock the authorization check
    WebTarget mockTarget = mock( WebTarget.class );
    Invocation.Builder builder = mock( Invocation.Builder.class );
    when( mockTarget.request( MediaType.TEXT_PLAIN ) ).thenReturn( builder );
    when( builder.get( String.class ) ).thenReturn( "true" );

    Client mockClient = mock( Client.class );
    when( mockClient.target( anyString() ) ).thenReturn( mockTarget );

    // Mock SpoonSessionManager and AuthenticationContext
    SpoonSessionManager mockSessionMgr = mock( SpoonSessionManager.class );
    AuthenticationContext mockAuthContext = mock( AuthenticationContext.class );
    when( mockSessionMgr.getAuthenticationContext( serverUrl ) ).thenReturn( mockAuthContext );
    when( mockAuthContext.isAuthenticated() ).thenReturn( true );
    when( mockAuthContext.getJSessionId() ).thenReturn( "ABC123SESSION" );

    try ( MockedStatic<ClientBuilder> mockedClientBuilder = mockStatic( ClientBuilder.class );
          MockedStatic<SpoonSessionManager> mockedSessionMgr = mockStatic( SpoonSessionManager.class ) ) {

      mockedClientBuilder.when( () -> ClientBuilder.newClient( any() ) ).thenReturn( mockClient );
      mockedSessionMgr.when( SpoonSessionManager::getInstance ).thenReturn( mockSessionMgr );

      util.authenticateLoginCredentials();

      // Should register a ClientRequestFilter, NOT HttpAuthenticationFeature
      verify( mockClient, times( 1 ) ).register( any( ClientRequestFilter.class ) );
      verify( mockClient, never() ).register( any( HttpAuthenticationFeature.class ) );
    } finally {
      RepositoryCleanupUtil.client = null;
    }
  }

  @Test
  public void testAuthenticateLoginCredentials_SessionAuth_NotAuthenticated_ThrowsException() throws Exception {
    RepositoryCleanupUtil util = mock( RepositoryCleanupUtil.class );
    doCallRealMethod().when( util ).authenticateLoginCredentials();

    String serverUrl = "http://localhost:8080/pentaho";
    setInternalState( util, "url", serverUrl );
    setInternalState( util, "username", "admin" );
    setInternalState( util, "password", AuthenticationContext.SESSION_AUTH_TOKEN );
    setInternalState( util, "client", null );

    // Mock SpoonSessionManager and AuthenticationContext
    SpoonSessionManager mockSessionMgr = mock( SpoonSessionManager.class );
    AuthenticationContext mockAuthContext = mock( AuthenticationContext.class );
    when( mockSessionMgr.getAuthenticationContext( serverUrl ) ).thenReturn( mockAuthContext );
    when( mockAuthContext.isAuthenticated() ).thenReturn( false );

    Client mockClient = mock( Client.class );

    try ( MockedStatic<ClientBuilder> mockedClientBuilder = mockStatic( ClientBuilder.class );
          MockedStatic<SpoonSessionManager> mockedSessionMgr = mockStatic( SpoonSessionManager.class ) ) {

      mockedClientBuilder.when( () -> ClientBuilder.newClient( any() ) ).thenReturn( mockClient );
      mockedSessionMgr.when( SpoonSessionManager::getInstance ).thenReturn( mockSessionMgr );

      try {
        util.authenticateLoginCredentials();
        fail( "Expected Exception for unauthenticated session" );
      } catch ( Exception e ) {
        assertTrue( e.getMessage().contains( "no valid session found" ) );
      }
    } finally {
      RepositoryCleanupUtil.client = null;
    }
  }

  @Test
  public void testAuthenticateLoginCredentials_BasicAuth_RegistersHttpAuthFeature() throws Exception {
    RepositoryCleanupUtil util = mock( RepositoryCleanupUtil.class );
    doCallRealMethod().when( util ).authenticateLoginCredentials();

    setInternalState( util, "url", "http://localhost:8080/pentaho" );
    setInternalState( util, "username", "admin" );
    setInternalState( util, "password", "password" );
    setInternalState( util, "client", null );

    WebTarget mockTarget = mock( WebTarget.class );
    Invocation.Builder builder = mock( Invocation.Builder.class );
    when( mockTarget.request( MediaType.TEXT_PLAIN ) ).thenReturn( builder );
    when( builder.get( String.class ) ).thenReturn( "true" );

    Client mockClient = mock( Client.class );
    when( mockClient.target( anyString() ) ).thenReturn( mockTarget );

    try ( MockedStatic<ClientBuilder> mockedClientBuilder = mockStatic( ClientBuilder.class ) ) {
      mockedClientBuilder.when( () -> ClientBuilder.newClient( any() ) ).thenReturn( mockClient );

      util.authenticateLoginCredentials();

      verify( mockClient, times( 1 ) ).register( any( HttpAuthenticationFeature.class ) );
      verify( mockClient, never() ).register( any( ClientRequestFilter.class ) );
    } finally {
      RepositoryCleanupUtil.client = null;
    }
  }

  @Test
  public void testAuthenticateLoginCredentials_SessionAuth_AccessDenied_ThrowsException() throws Exception {
    RepositoryCleanupUtil util = mock( RepositoryCleanupUtil.class );
    doCallRealMethod().when( util ).authenticateLoginCredentials();

    String serverUrl = "http://localhost:8080/pentaho";
    setInternalState( util, "url", serverUrl );
    setInternalState( util, "username", "admin" );
    setInternalState( util, "password", AuthenticationContext.SESSION_AUTH_TOKEN );
    setInternalState( util, "client", null );

    // Mock authorization check to deny access
    WebTarget mockTarget = mock( WebTarget.class );
    Invocation.Builder builder = mock( Invocation.Builder.class );
    when( mockTarget.request( MediaType.TEXT_PLAIN ) ).thenReturn( builder );
    when( builder.get( String.class ) ).thenReturn( "false" );

    Client mockClient = mock( Client.class );
    when( mockClient.target( anyString() ) ).thenReturn( mockTarget );

    SpoonSessionManager mockSessionMgr = mock( SpoonSessionManager.class );
    AuthenticationContext mockAuthContext = mock( AuthenticationContext.class );
    when( mockSessionMgr.getAuthenticationContext( serverUrl ) ).thenReturn( mockAuthContext );
    when( mockAuthContext.isAuthenticated() ).thenReturn( true );
    when( mockAuthContext.getJSessionId() ).thenReturn( "VALID_SESSION" );

    try ( MockedStatic<ClientBuilder> mockedClientBuilder = mockStatic( ClientBuilder.class );
          MockedStatic<SpoonSessionManager> mockedSessionMgr = mockStatic( SpoonSessionManager.class ) ) {

      mockedClientBuilder.when( () -> ClientBuilder.newClient( any() ) ).thenReturn( mockClient );
      mockedSessionMgr.when( SpoonSessionManager::getInstance ).thenReturn( mockSessionMgr );

      try {
        util.authenticateLoginCredentials();
        fail( "Expected Exception for access denied" );
      } catch ( Exception e ) {
        // The access denied exception is thrown after the auth method is set up
        // Verify the ClientRequestFilter was registered before the access check failed
        verify( mockClient, times( 1 ) ).register( any( ClientRequestFilter.class ) );
      }
    } finally {
      RepositoryCleanupUtil.client = null;
    }
  }

  @Test
  public void testAuthenticateLoginCredentials_ClientAlreadySet_SkipsAuthSetup() throws Exception {
    RepositoryCleanupUtil util = mock( RepositoryCleanupUtil.class );
    doCallRealMethod().when( util ).authenticateLoginCredentials();

    setInternalState( util, "url", "http://localhost:8080/pentaho" );
    setInternalState( util, "username", "admin" );
    setInternalState( util, "password", AuthenticationContext.SESSION_AUTH_TOKEN );

    WebTarget mockTarget = mock( WebTarget.class );
    Invocation.Builder builder = mock( Invocation.Builder.class );
    when( mockTarget.request( MediaType.TEXT_PLAIN ) ).thenReturn( builder );
    when( builder.get( String.class ) ).thenReturn( "true" );

    Client mockClient = mock( Client.class );
    when( mockClient.target( anyString() ) ).thenReturn( mockTarget );
    setInternalState( util, "client", mockClient );

    util.authenticateLoginCredentials();
    verify( mockClient, never() ).register( any( ClientRequestFilter.class ) );
    verify( mockClient, never() ).register( any( HttpAuthenticationFeature.class ) );

    // But the authorization check should still run
    verify( mockClient, times( 1 ) ).target( anyString() );
  }

  @Test
  public void testSessionAuthTokenConstant() {
    assertEquals( "_SESSION_AUTH_", AuthenticationContext.SESSION_AUTH_TOKEN );
  }

  @Test
  public void testAuthenticateLoginCredentials_SessionAuth_NullAuthContext_ThrowsException() throws Exception {
    RepositoryCleanupUtil util = mock( RepositoryCleanupUtil.class );
    doCallRealMethod().when( util ).authenticateLoginCredentials();

    String serverUrl = "http://localhost:8080/pentaho";
    setInternalState( util, "url", serverUrl );
    setInternalState( util, "username", "admin" );
    setInternalState( util, "password", AuthenticationContext.SESSION_AUTH_TOKEN );
    setInternalState( util, "client", null );

    SpoonSessionManager mockSessionMgr = mock( SpoonSessionManager.class );
    when( mockSessionMgr.getAuthenticationContext( serverUrl ) ).thenReturn( null );

    Client mockClient = mock( Client.class );

    try ( MockedStatic<ClientBuilder> mockedClientBuilder = mockStatic( ClientBuilder.class );
          MockedStatic<SpoonSessionManager> mockedSessionMgr = mockStatic( SpoonSessionManager.class ) ) {

      mockedClientBuilder.when( () -> ClientBuilder.newClient( any() ) ).thenReturn( mockClient );
      mockedSessionMgr.when( SpoonSessionManager::getInstance ).thenReturn( mockSessionMgr );

      try {
        util.authenticateLoginCredentials();
        fail( "Expected SessionAuthenticationException for null authContext" );
      } catch ( RepositoryCleanupUtil.SessionAuthenticationException e ) {
        assertTrue( e.getMessage().contains( "Unable to obtain authentication context for URL" ) );
        assertTrue( e.getMessage().contains( serverUrl ) );
      }
    } finally {
      RepositoryCleanupUtil.client = null;
    }
  }

  @Test
  public void testSessionAuth_FilterAddsJsessionIdCookieHeader() throws Exception {
    RepositoryCleanupUtil util = mock( RepositoryCleanupUtil.class );
    doCallRealMethod().when( util ).authenticateLoginCredentials();

    String serverUrl = "http://localhost:8080/pentaho";
    setInternalState( util, "url", serverUrl );
    setInternalState( util, "username", "admin" );
    setInternalState( util, "password", AuthenticationContext.SESSION_AUTH_TOKEN );
    setInternalState( util, "client", null );

    WebTarget mockTarget = mock( WebTarget.class );
    Invocation.Builder builder = mock( Invocation.Builder.class );
    when( mockTarget.request( MediaType.TEXT_PLAIN ) ).thenReturn( builder );
    when( builder.get( String.class ) ).thenReturn( "true" );

    Client mockClient = mock( Client.class );
    when( mockClient.target( anyString() ) ).thenReturn( mockTarget );

    SpoonSessionManager mockSessionMgr = mock( SpoonSessionManager.class );
    AuthenticationContext mockAuthContext = mock( AuthenticationContext.class );
    when( mockSessionMgr.getAuthenticationContext( serverUrl ) ).thenReturn( mockAuthContext );
    when( mockAuthContext.isAuthenticated() ).thenReturn( true );
    when( mockAuthContext.getJSessionId() ).thenReturn( "MY_SESSION_ID" );

    try ( MockedStatic<ClientBuilder> mockedClientBuilder = mockStatic( ClientBuilder.class );
          MockedStatic<SpoonSessionManager> mockedSessionMgr = mockStatic( SpoonSessionManager.class ) ) {

      mockedClientBuilder.when( () -> ClientBuilder.newClient( any() ) ).thenReturn( mockClient );
      mockedSessionMgr.when( SpoonSessionManager::getInstance ).thenReturn( mockSessionMgr );

      util.authenticateLoginCredentials();

      // Capture the ClientRequestFilter that was registered
      ArgumentCaptor<ClientRequestFilter> filterCaptor = ArgumentCaptor.forClass( ClientRequestFilter.class );
      verify( mockClient ).register( filterCaptor.capture() );
      ClientRequestFilter capturedFilter = filterCaptor.getValue();

      // Invoke the filter to cover the lambda body (lines 377-379)
      ClientRequestContext requestContext = mock( ClientRequestContext.class );
      MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
      when( requestContext.getHeaders() ).thenReturn( headers );

      capturedFilter.filter( requestContext );

      // Verify the Cookie header was added with the correct JSESSIONID
      assertTrue( headers.containsKey( "Cookie" ) );
      assertEquals( "JSESSIONID=MY_SESSION_ID", headers.getFirst( "Cookie" ) );
    } finally {
      RepositoryCleanupUtil.client = null;
    }
  }
}