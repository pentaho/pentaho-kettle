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

import com.sun.xml.ws.client.ClientTransportException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.xml.ws.BindingProvider;
import jakarta.xml.ws.handler.MessageContext;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.di.ui.spoon.session.AuthenticationContext;
import org.pentaho.di.ui.spoon.session.SpoonSessionManager;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith( MockitoJUnitRunner.class )
public class WebServiceManagerTest {

  private static final String BASE_URL = "http://localhost:8080/pentaho";
  private static final String USERNAME = "admin";
  private static final String PASSWORD = "password";
  private static final String JSESSION_ID = "ABCDEF123456";

  private WebServiceManager manager;

  public static class FakeFactory {
    public static class FakeService {
      public static volatile URI lastUri = null;
      public static Client fakeClient = null;
    }

    public static FakeService create( Client client, URI uri ) {
      FakeService.lastUri = uri;
      FakeService.fakeClient = client;

      return new FakeService();
    }
  }

  @Before
  public void setUp() {
    manager = new WebServiceManager( BASE_URL, USERNAME );
  }

  /**
   * Reflectively invokes a private method on {@link #manager}.
   */
  @SuppressWarnings( "unchecked" )
  private <T> T invokePrivate( String methodName, Class<?>[] paramTypes, Object... args ) throws Exception {
    Method method = WebServiceManager.class.getDeclaredMethod( methodName, paramTypes );
    method.setAccessible( true );
    return (T) method.invoke( manager, args );
  }

  /**
   * Creates a mock {@link BindingProvider} whose {@code getRequestContext()} returns a real {@link HashMap}
   * that the test can interrogate after the authentication method has run.
   */
  private Map<String, Object> bindingProviderContext( BindingProvider bp ) {
    Map<String, Object> ctx = new HashMap<>();
    when( bp.getRequestContext() ).thenReturn( ctx );
    return ctx;
  }

  @Test
  public void testIsSessionExpiredException_ClientTransportExceptionWith401_ReturnsTrue() throws Exception {
    ClientTransportException ex = mock( ClientTransportException.class );
    when( ex.getMessage() ).thenReturn( "HTTP transport error: 401 Unauthorized" );

    boolean result = invokePrivate( "isSessionExpiredException",
      new Class<?>[] { Throwable.class }, ex );

    assertTrue( result );
  }

  @Test
  public void testIsSessionExpiredException_ClientTransportExceptionWithout401_ReturnsFalse() throws Exception {
    ClientTransportException ex = mock( ClientTransportException.class );
    when( ex.getMessage() ).thenReturn( "HTTP transport error: 500 Internal Server Error" );

    boolean result = invokePrivate( "isSessionExpiredException",
      new Class<?>[] { Throwable.class }, ex );

    assertFalse( result );
  }

  @Test
  public void testIsSessionExpiredException_ClientTransportExceptionNullMessage_ReturnsFalse() throws Exception {
    ClientTransportException ex = mock( ClientTransportException.class );
    when( ex.getMessage() ).thenReturn( null );

    boolean result = invokePrivate( "isSessionExpiredException",
      new Class<?>[] { Throwable.class }, ex );

    assertFalse( result );
  }

  @Test
  public void testIsSessionExpiredException_NonClientTransportException_ReturnsFalse() throws Exception {
    RuntimeException ex = new RuntimeException( "something went wrong" );

    boolean result = invokePrivate( "isSessionExpiredException",
      new Class<?>[] { Throwable.class }, ex );

    assertFalse( result );
  }

  @Test
  public void testIsSessionExpiredException_NullThrowable_ReturnsFalse() throws Exception {
    boolean result = invokePrivate( "isSessionExpiredException",
      new Class<?>[] { Throwable.class }, (Object) null );

    assertFalse( result );
  }

  @Test
  public void testIsSessionExpiredException_ChainedCause_ClientTransportExceptionWith401_ReturnsTrue()
    throws Exception {
    ClientTransportException cause = mock( ClientTransportException.class );
    when( cause.getMessage() ).thenReturn( "401" );

    RuntimeException wrapper = new RuntimeException( "wrapped", cause );

    boolean result = invokePrivate( "isSessionExpiredException",
      new Class<?>[] { Throwable.class }, wrapper );

    assertTrue( result );
  }

  @Test
  public void testIsSessionExpiredException_ChainedCause_No401Anywhere_ReturnsFalse() throws Exception {
    RuntimeException inner = new RuntimeException( "inner" );
    RuntimeException outer = new RuntimeException( "outer", inner );

    boolean result = invokePrivate( "isSessionExpiredException",
      new Class<?>[] { Throwable.class }, outer );

    assertFalse( result );
  }

  @Test
  public void testGetValidAuthContext_NullAuthContext_ReturnsNull() throws Exception {
    SpoonSessionManager mockMgr = mock( SpoonSessionManager.class );
    when( mockMgr.getAuthenticationContext( BASE_URL ) ).thenReturn( null );

    try ( MockedStatic<SpoonSessionManager> mocked = mockStatic( SpoonSessionManager.class ) ) {
      mocked.when( SpoonSessionManager::getInstance ).thenReturn( mockMgr );

      AuthenticationContext result = invokePrivate( "getValidAuthContext", new Class<?>[ 0 ] );

      assertNull( result );
    }
  }

  @Test
  public void testGetValidAuthContext_NotAuthenticated_ReturnsNull() throws Exception {
    SpoonSessionManager mockMgr = mock( SpoonSessionManager.class );
    AuthenticationContext mockCtx = mock( AuthenticationContext.class );
    when( mockMgr.getAuthenticationContext( BASE_URL ) ).thenReturn( mockCtx );
    when( mockCtx.isAuthenticated() ).thenReturn( false );

    try ( MockedStatic<SpoonSessionManager> mocked = mockStatic( SpoonSessionManager.class ) ) {
      mocked.when( SpoonSessionManager::getInstance ).thenReturn( mockMgr );

      AuthenticationContext result = invokePrivate( "getValidAuthContext", new Class<?>[ 0 ] );

      assertNull( result );
    }
  }

  @Test
  public void testGetValidAuthContext_AuthenticatedButExpired_ReturnsNull() throws Exception {
    SpoonSessionManager mockMgr = mock( SpoonSessionManager.class );
    AuthenticationContext mockCtx = mock( AuthenticationContext.class );
    when( mockMgr.getAuthenticationContext( BASE_URL ) ).thenReturn( mockCtx );
    when( mockCtx.isAuthenticated() ).thenReturn( true );
    when( mockCtx.validateAndClearIfExpired() ).thenReturn( false ); // expired

    try ( MockedStatic<SpoonSessionManager> mocked = mockStatic( SpoonSessionManager.class ) ) {
      mocked.when( SpoonSessionManager::getInstance ).thenReturn( mockMgr );

      AuthenticationContext result = invokePrivate( "getValidAuthContext", new Class<?>[ 0 ] );

      assertNull( result );
    }
  }

  @Test
  public void testGetValidAuthContext_ValidSession_ReturnsAuthContext() throws Exception {
    SpoonSessionManager mockMgr = mock( SpoonSessionManager.class );
    AuthenticationContext mockCtx = mock( AuthenticationContext.class );
    when( mockMgr.getAuthenticationContext( BASE_URL ) ).thenReturn( mockCtx );
    when( mockCtx.isAuthenticated() ).thenReturn( true );
    when( mockCtx.validateAndClearIfExpired() ).thenReturn( true );

    try ( MockedStatic<SpoonSessionManager> mocked = mockStatic( SpoonSessionManager.class ) ) {
      mocked.when( SpoonSessionManager::getInstance ).thenReturn( mockMgr );

      AuthenticationContext result = invokePrivate( "getValidAuthContext", new Class<?>[ 0 ] );

      assertNotNull( result );
      assertEquals( mockCtx, result );
    }
  }

  @Test
  public void testGetValidAuthContext_SpoonSessionManagerThrows_ReturnsNull() throws Exception {
    try ( MockedStatic<SpoonSessionManager> mocked = mockStatic( SpoonSessionManager.class ) ) {
      mocked.when( SpoonSessionManager::getInstance ).thenThrow( new RuntimeException( "headless" ) );

      AuthenticationContext result = invokePrivate( "getValidAuthContext", new Class<?>[ 0 ] );

      assertNull( result );
    }
  }

  @Test
  @SuppressWarnings( "unchecked" )
  public void testConfigureJaxWsAuthentication_SessionAuth_ValidJSessionId_SetsCookieHeader() throws Exception {
    SpoonSessionManager mockMgr = mock( SpoonSessionManager.class );
    AuthenticationContext mockCtx = mock( AuthenticationContext.class );
    when( mockMgr.getAuthenticationContext( BASE_URL ) ).thenReturn( mockCtx );
    when( mockCtx.isAuthenticated() ).thenReturn( true );
    when( mockCtx.validateAndClearIfExpired() ).thenReturn( true );
    when( mockCtx.getJSessionId() ).thenReturn( JSESSION_ID );

    BindingProvider bp = mock( BindingProvider.class );
    Map<String, Object> ctx = bindingProviderContext( bp );

    try ( MockedStatic<SpoonSessionManager> mocked = mockStatic( SpoonSessionManager.class ) ) {
      mocked.when( SpoonSessionManager::getInstance ).thenReturn( mockMgr );

      invokePrivate( "configureJaxWsAuthentication",
        new Class<?>[] { BindingProvider.class, String.class, String.class },
        bp, USERNAME, PASSWORD );
    }

    Map<String, List<String>> headers =
      (Map<String, List<String>>) ctx.get( MessageContext.HTTP_REQUEST_HEADERS );
    assertNotNull( "Cookie header map must be set", headers );
    assertEquals( 1, headers.get( "Cookie" ).size() );
    assertEquals( "JSESSIONID=" + JSESSION_ID, headers.get( "Cookie" ).get( 0 ) );
    assertEquals( Boolean.TRUE, ctx.get( BindingProvider.SESSION_MAINTAIN_PROPERTY ) );
    assertFalse( "USERNAME_PROPERTY must NOT be set", ctx.containsKey( BindingProvider.USERNAME_PROPERTY ) );
  }

  @Test
  public void testConfigureJaxWsAuthentication_TrustUserProperty_SetsTrustUserHeader() throws Exception {
    // Ensure session auth is disabled (null auth context)
    SpoonSessionManager mockMgr = mock( SpoonSessionManager.class );
    when( mockMgr.getAuthenticationContext( BASE_URL ) ).thenReturn( null );

    BindingProvider bp = mock( BindingProvider.class );
    Map<String, Object> ctx = bindingProviderContext( bp );

    String originalProp = System.getProperty( "pentaho.repository.client.attemptTrust" );
    System.setProperty( "pentaho.repository.client.attemptTrust", "true" );
    try ( MockedStatic<SpoonSessionManager> mocked = mockStatic( SpoonSessionManager.class ) ) {
      mocked.when( SpoonSessionManager::getInstance ).thenReturn( mockMgr );

      invokePrivate( "configureJaxWsAuthentication",
        new Class<?>[] { BindingProvider.class, String.class, String.class },
        bp, USERNAME, PASSWORD );
    } finally {
      if ( originalProp == null ) {
        System.clearProperty( "pentaho.repository.client.attemptTrust" );
      } else {
        System.setProperty( "pentaho.repository.client.attemptTrust", originalProp );
      }
    }

    // The TRUST_USER header must be present; USERNAME_PROPERTY must NOT
    @SuppressWarnings( "unchecked" )
    Map<String, List<String>> headers =
      (Map<String, List<String>>) ctx.get( MessageContext.HTTP_REQUEST_HEADERS );
    assertNotNull( headers );
    assertTrue( "TRUST_USER header must be present", headers.containsKey( "_trust_user_" ) );
    assertEquals( USERNAME, headers.get( "_trust_user_" ).get( 0 ) );
    assertFalse( ctx.containsKey( BindingProvider.USERNAME_PROPERTY ) );
  }

  @Test
  public void testConfigureJaxWsAuthentication_BasicAuth_SetsUsernameAndPassword() throws Exception {
    SpoonSessionManager mockMgr = mock( SpoonSessionManager.class );
    when( mockMgr.getAuthenticationContext( BASE_URL ) ).thenReturn( null );

    BindingProvider bp = mock( BindingProvider.class );
    Map<String, Object> ctx = bindingProviderContext( bp );

    // Ensure the system property is NOT set for this test
    String originalProp = System.getProperty( "pentaho.repository.client.attemptTrust" );
    System.clearProperty( "pentaho.repository.client.attemptTrust" );
    try ( MockedStatic<SpoonSessionManager> mocked = mockStatic( SpoonSessionManager.class ) ) {
      mocked.when( SpoonSessionManager::getInstance ).thenReturn( mockMgr );

      invokePrivate( "configureJaxWsAuthentication",
        new Class<?>[] { BindingProvider.class, String.class, String.class },
        bp, USERNAME, PASSWORD );
    } finally {
      if ( originalProp != null ) {
        System.setProperty( "pentaho.repository.client.attemptTrust", originalProp );
      }
    }

    assertEquals( USERNAME, ctx.get( BindingProvider.USERNAME_PROPERTY ) );
    assertEquals( PASSWORD, ctx.get( BindingProvider.PASSWORD_PROPERTY ) );
    assertFalse( ctx.containsKey( MessageContext.HTTP_REQUEST_HEADERS ) );
  }

  @Test
  public void testConfigureJaxWsAuthentication_SessionAuth_BlankJSessionId_FallsBackToBasicAuth()
    throws Exception {
    SpoonSessionManager mockMgr = mock( SpoonSessionManager.class );
    AuthenticationContext mockCtx = mock( AuthenticationContext.class );
    when( mockMgr.getAuthenticationContext( BASE_URL ) ).thenReturn( mockCtx );
    when( mockCtx.isAuthenticated() ).thenReturn( true );
    when( mockCtx.validateAndClearIfExpired() ).thenReturn( true );
    // Session auth is "enabled" (getValidAuthContext returns non-null)
    // but getJSessionId returns blank — the isSessionAuthEnabled+getJSessionId check blocks it
    when( mockCtx.getJSessionId() ).thenReturn( "   " );

    BindingProvider bp = mock( BindingProvider.class );
    Map<String, Object> ctx = bindingProviderContext( bp );

    String originalProp = System.getProperty( "pentaho.repository.client.attemptTrust" );
    System.clearProperty( "pentaho.repository.client.attemptTrust" );
    try ( MockedStatic<SpoonSessionManager> mocked = mockStatic( SpoonSessionManager.class ) ) {
      mocked.when( SpoonSessionManager::getInstance ).thenReturn( mockMgr );

      invokePrivate( "configureJaxWsAuthentication",
        new Class<?>[] { BindingProvider.class, String.class, String.class },
        bp, USERNAME, PASSWORD );
    } finally {
      if ( originalProp != null ) {
        System.setProperty( "pentaho.repository.client.attemptTrust", originalProp );
      }
    }

    // blank sessionId → falls through to basic auth branch
    assertEquals( USERNAME, ctx.get( BindingProvider.USERNAME_PROPERTY ) );
    assertEquals( PASSWORD, ctx.get( BindingProvider.PASSWORD_PROPERTY ) );
  }

  @Test
  public void testConfigureJaxWsAuthentication_SessionAuth_NullJSessionId_FallsBackToBasicAuth()
    throws Exception {
    // Covers: isSessionAuthEnabled() returns true but getJSessionId() returns null → falls through
    SpoonSessionManager mockMgr = mock( SpoonSessionManager.class );
    AuthenticationContext mockCtx = mock( AuthenticationContext.class );
    when( mockMgr.getAuthenticationContext( BASE_URL ) ).thenReturn( mockCtx );
    when( mockCtx.isAuthenticated() ).thenReturn( true );
    when( mockCtx.validateAndClearIfExpired() ).thenReturn( true );
    when( mockCtx.getJSessionId() ).thenReturn( null );

    BindingProvider bp = mock( BindingProvider.class );
    Map<String, Object> ctx = bindingProviderContext( bp );

    String originalProp = System.getProperty( "pentaho.repository.client.attemptTrust" );
    System.clearProperty( "pentaho.repository.client.attemptTrust" );
    try ( MockedStatic<SpoonSessionManager> mocked = mockStatic( SpoonSessionManager.class ) ) {
      mocked.when( SpoonSessionManager::getInstance ).thenReturn( mockMgr );

      invokePrivate( "configureJaxWsAuthentication",
        new Class<?>[] { BindingProvider.class, String.class, String.class },
        bp, USERNAME, PASSWORD );
    } finally {
      if ( originalProp != null ) {
        System.setProperty( "pentaho.repository.client.attemptTrust", originalProp );
      }
    }

    // null sessionId → falls through to basic auth
    assertEquals( USERNAME, ctx.get( BindingProvider.USERNAME_PROPERTY ) );
    assertEquals( PASSWORD, ctx.get( BindingProvider.PASSWORD_PROPERTY ) );
    assertFalse( "Cookie header must NOT be set", ctx.containsKey( MessageContext.HTTP_REQUEST_HEADERS ) );
  }

  @Test
  public void testConfigureJaxWsAuthentication_SessionAuth_NullJSessionId_TrustUserProperty_SetsTrustUserHeader()
    throws Exception {
    // Covers: sessionId is null, but trust property is set → goes to trust user branch
    SpoonSessionManager mockMgr = mock( SpoonSessionManager.class );
    AuthenticationContext mockCtx = mock( AuthenticationContext.class );
    when( mockMgr.getAuthenticationContext( BASE_URL ) ).thenReturn( mockCtx );
    when( mockCtx.isAuthenticated() ).thenReturn( true );
    when( mockCtx.validateAndClearIfExpired() ).thenReturn( true );
    when( mockCtx.getJSessionId() ).thenReturn( null );

    BindingProvider bp = mock( BindingProvider.class );
    Map<String, Object> ctx = bindingProviderContext( bp );

    String originalProp = System.getProperty( "pentaho.repository.client.attemptTrust" );
    System.setProperty( "pentaho.repository.client.attemptTrust", "true" );
    try ( MockedStatic<SpoonSessionManager> mocked = mockStatic( SpoonSessionManager.class ) ) {
      mocked.when( SpoonSessionManager::getInstance ).thenReturn( mockMgr );

      invokePrivate( "configureJaxWsAuthentication",
        new Class<?>[] { BindingProvider.class, String.class, String.class },
        bp, USERNAME, PASSWORD );
    } finally {
      if ( originalProp == null ) {
        System.clearProperty( "pentaho.repository.client.attemptTrust" );
      } else {
        System.setProperty( "pentaho.repository.client.attemptTrust", originalProp );
      }
    }

    @SuppressWarnings( "unchecked" )
    Map<String, List<String>> headers =
      (Map<String, List<String>>) ctx.get( MessageContext.HTTP_REQUEST_HEADERS );
    assertNotNull( headers );
    assertTrue( "TRUST_USER header must be present", headers.containsKey( "_trust_user_" ) );
    assertEquals( USERNAME, headers.get( "_trust_user_" ).get( 0 ) );
    assertFalse( ctx.containsKey( BindingProvider.USERNAME_PROPERTY ) );
  }

  @Test
  public void testConfigureJaxWsAuthentication_SessionAuth_BlankJSessionId_TrustUserProperty_SetsTrustUserHeader()
    throws Exception {
    // Covers: authContext is non-null but sessionId is blank AND trust property is set
    SpoonSessionManager mockMgr = mock( SpoonSessionManager.class );
    AuthenticationContext mockCtx = mock( AuthenticationContext.class );
    when( mockMgr.getAuthenticationContext( BASE_URL ) ).thenReturn( mockCtx );
    when( mockCtx.isAuthenticated() ).thenReturn( true );
    when( mockCtx.validateAndClearIfExpired() ).thenReturn( true );
    when( mockCtx.getJSessionId() ).thenReturn( "   " ); // blank

    BindingProvider bp = mock( BindingProvider.class );
    Map<String, Object> ctx = bindingProviderContext( bp );

    String originalProp = System.getProperty( "pentaho.repository.client.attemptTrust" );
    System.setProperty( "pentaho.repository.client.attemptTrust", "true" );
    try ( MockedStatic<SpoonSessionManager> mocked = mockStatic( SpoonSessionManager.class ) ) {
      mocked.when( SpoonSessionManager::getInstance ).thenReturn( mockMgr );

      invokePrivate( "configureJaxWsAuthentication",
        new Class<?>[] { BindingProvider.class, String.class, String.class },
        bp, USERNAME, PASSWORD );
    } finally {
      if ( originalProp == null ) {
        System.clearProperty( "pentaho.repository.client.attemptTrust" );
      } else {
        System.setProperty( "pentaho.repository.client.attemptTrust", originalProp );
      }
    }

    @SuppressWarnings( "unchecked" )
    Map<String, List<String>> headers =
      (Map<String, List<String>>) ctx.get( MessageContext.HTTP_REQUEST_HEADERS );
    assertNotNull( "HTTP_REQUEST_HEADERS must be set", headers );
    assertTrue( "TRUST_USER header must be present", headers.containsKey( "_trust_user_" ) );
    assertEquals( USERNAME, headers.get( "_trust_user_" ).get( 0 ) );
    assertEquals( Boolean.TRUE, ctx.get( BindingProvider.SESSION_MAINTAIN_PROPERTY ) );
    assertFalse( "USERNAME_PROPERTY must NOT be set", ctx.containsKey( BindingProvider.USERNAME_PROPERTY ) );
  }

  @Test
  public void testConfigureJaxRsAuthentication_SessionAuth_ValidJSessionId_RegistersCookieFilter()
    throws Exception {
    SpoonSessionManager mockMgr = mock( SpoonSessionManager.class );
    AuthenticationContext mockCtx = mock( AuthenticationContext.class );
    when( mockMgr.getAuthenticationContext( BASE_URL ) ).thenReturn( mockCtx );
    when( mockCtx.isAuthenticated() ).thenReturn( true );
    when( mockCtx.validateAndClearIfExpired() ).thenReturn( true );
    when( mockCtx.getJSessionId() ).thenReturn( JSESSION_ID );

    Client mockClient = mock( Client.class );

    try ( MockedStatic<SpoonSessionManager> mocked = mockStatic( SpoonSessionManager.class ) ) {
      mocked.when( SpoonSessionManager::getInstance ).thenReturn( mockMgr );

      invokePrivate( "configureJaxRsAuthentication",
        new Class<?>[] { Client.class, String.class, String.class },
        mockClient, USERNAME, PASSWORD );
    }

    verify( mockClient, times( 1 ) ).register( any( ClientRequestFilter.class ) );
    verify( mockClient, never() ).register( any( HttpAuthenticationFeature.class ) );
  }

  @Test
  public void testConfigureJaxRsAuthentication_SessionAuth_CookieFilterAddsCookieHeader() throws Exception {
    SpoonSessionManager mockMgr = mock( SpoonSessionManager.class );
    AuthenticationContext mockCtx = mock( AuthenticationContext.class );
    when( mockMgr.getAuthenticationContext( BASE_URL ) ).thenReturn( mockCtx );
    when( mockCtx.isAuthenticated() ).thenReturn( true );
    when( mockCtx.validateAndClearIfExpired() ).thenReturn( true );
    when( mockCtx.getJSessionId() ).thenReturn( JSESSION_ID );

    Client mockClient = mock( Client.class );
    ArgumentCaptor<ClientRequestFilter> filterCaptor = ArgumentCaptor.forClass( ClientRequestFilter.class );

    try ( MockedStatic<SpoonSessionManager> mocked = mockStatic( SpoonSessionManager.class ) ) {
      mocked.when( SpoonSessionManager::getInstance ).thenReturn( mockMgr );

      invokePrivate( "configureJaxRsAuthentication",
        new Class<?>[] { Client.class, String.class, String.class },
        mockClient, USERNAME, PASSWORD );
    }

    verify( mockClient ).register( filterCaptor.capture() );

    // Invoke the captured filter with a mock ClientRequestContext and verify the Cookie header
    ClientRequestContext mockRequestContext = mock( ClientRequestContext.class );
    MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
    when( mockRequestContext.getHeaders() ).thenReturn( headers );

    filterCaptor.getValue().filter( mockRequestContext );

    assertTrue( "Cookie header must be present", headers.containsKey( "Cookie" ) );
    assertEquals( 1, headers.get( "Cookie" ).size() );
    assertEquals( "JSESSIONID=" + JSESSION_ID, headers.get( "Cookie" ).get( 0 ) );
  }

  @Test
  public void testConfigureJaxRsAuthentication_NoSession_RegistersHttpAuthFeature() throws Exception {
    SpoonSessionManager mockMgr = mock( SpoonSessionManager.class );
    when( mockMgr.getAuthenticationContext( BASE_URL ) ).thenReturn( null );

    Client mockClient = mock( Client.class );

    try ( MockedStatic<SpoonSessionManager> mocked = mockStatic( SpoonSessionManager.class ) ) {
      mocked.when( SpoonSessionManager::getInstance ).thenReturn( mockMgr );

      invokePrivate( "configureJaxRsAuthentication",
        new Class<?>[] { Client.class, String.class, String.class },
        mockClient, USERNAME, PASSWORD );
    }

    verify( mockClient, times( 1 ) ).register( any( HttpAuthenticationFeature.class ) );
    verify( mockClient, never() ).register( any( ClientRequestFilter.class ) );
  }

  @Test
  public void testConfigureJaxRsAuthentication_SessionAuth_BlankJSessionId_RegistersHttpAuthFeature()
    throws Exception {
    SpoonSessionManager mockMgr = mock( SpoonSessionManager.class );
    AuthenticationContext mockCtx = mock( AuthenticationContext.class );
    when( mockMgr.getAuthenticationContext( BASE_URL ) ).thenReturn( mockCtx );
    when( mockCtx.isAuthenticated() ).thenReturn( true );
    when( mockCtx.validateAndClearIfExpired() ).thenReturn( true );
    when( mockCtx.getJSessionId() ).thenReturn( "" ); // blank

    Client mockClient = mock( Client.class );

    try ( MockedStatic<SpoonSessionManager> mocked = mockStatic( SpoonSessionManager.class ) ) {
      mocked.when( SpoonSessionManager::getInstance ).thenReturn( mockMgr );

      invokePrivate( "configureJaxRsAuthentication",
        new Class<?>[] { Client.class, String.class, String.class },
        mockClient, USERNAME, PASSWORD );
    }

    verify( mockClient, times( 1 ) ).register( any( HttpAuthenticationFeature.class ) );
    verify( mockClient, never() ).register( any( ClientRequestFilter.class ) );
  }

  @Test
  public void testConfigureJaxRsAuthentication_SessionExpired_RegistersHttpAuthFeature() throws Exception {
    SpoonSessionManager mockMgr = mock( SpoonSessionManager.class );
    AuthenticationContext mockCtx = mock( AuthenticationContext.class );
    when( mockMgr.getAuthenticationContext( BASE_URL ) ).thenReturn( mockCtx );
    when( mockCtx.isAuthenticated() ).thenReturn( true );
    when( mockCtx.validateAndClearIfExpired() ).thenReturn( false ); // expired

    Client mockClient = mock( Client.class );

    try ( MockedStatic<SpoonSessionManager> mocked = mockStatic( SpoonSessionManager.class ) ) {
      mocked.when( SpoonSessionManager::getInstance ).thenReturn( mockMgr );

      invokePrivate( "configureJaxRsAuthentication",
        new Class<?>[] { Client.class, String.class, String.class },
        mockClient, USERNAME, PASSWORD );
    }

    verify( mockClient, times( 1 ) ).register( any( HttpAuthenticationFeature.class ) );
    verify( mockClient, never() ).register( any( ClientRequestFilter.class ) );
  }

  @Test
  public void testConfigureJaxRsAuthentication_SessionAuth_NullJSessionId_RegistersHttpAuthFeature()
    throws Exception {
    // Covers: isSessionAuthEnabled() true but getJSessionId() returns null → falls to basic auth
    SpoonSessionManager mockMgr = mock( SpoonSessionManager.class );
    AuthenticationContext mockCtx = mock( AuthenticationContext.class );
    when( mockMgr.getAuthenticationContext( BASE_URL ) ).thenReturn( mockCtx );
    when( mockCtx.isAuthenticated() ).thenReturn( true );
    when( mockCtx.validateAndClearIfExpired() ).thenReturn( true );
    when( mockCtx.getJSessionId() ).thenReturn( null );

    Client mockClient = mock( Client.class );

    try ( MockedStatic<SpoonSessionManager> mocked = mockStatic( SpoonSessionManager.class ) ) {
      mocked.when( SpoonSessionManager::getInstance ).thenReturn( mockMgr );

      invokePrivate( "configureJaxRsAuthentication",
        new Class<?>[] { Client.class, String.class, String.class },
        mockClient, USERNAME, PASSWORD );
    }

    verify( mockClient, times( 1 ) ).register( any( HttpAuthenticationFeature.class ) );
    verify( mockClient, never() ).register( any( ClientRequestFilter.class ) );
  }

  @Test
  public void testCreateJaxRsPort_BasicAuth_ClientBuiltAndFactoryInvoked() throws Exception {
    SpoonSessionManager mockMgr = mock( SpoonSessionManager.class );
    when( mockMgr.getAuthenticationContext( BASE_URL ) ).thenReturn( null ); // no session

    Client mockClient = mock( Client.class );
    WebServiceSpecification spec =
      WebServiceSpecification.getRestServiceSpecification( FakeFactory.FakeService.class, "create" );

    try ( MockedStatic<SpoonSessionManager> mockedSM = mockStatic( SpoonSessionManager.class );
          MockedStatic<ClientBuilder> mockedCB = mockStatic( ClientBuilder.class ) ) {

      mockedSM.when( SpoonSessionManager::getInstance ).thenReturn( mockMgr );
      mockedCB.when( () -> ClientBuilder.newClient( any( ClientConfig.class ) ) ).thenReturn( mockClient );

      Object result = invokePrivate( "createJaxRsPort",
        new Class<?>[] { String.class, String.class, WebServiceSpecification.class },
        USERNAME, PASSWORD, spec );

      assertNotNull( result );
      assertTrue( "Result must be a FakeService", result instanceof FakeFactory.FakeService );
    }

    // basic-auth path — HttpAuthenticationFeature registered, NOT a cookie filter
    verify( mockClient, times( 1 ) ).register( any( HttpAuthenticationFeature.class ) );
    verify( mockClient, never() ).register( any( ClientRequestFilter.class ) );
  }


  @Test
  public void testCreateJaxRsPort_SessionAuth_ClientBuiltWithCookieFilter() throws Exception {
    SpoonSessionManager mockMgr = mock( SpoonSessionManager.class );
    AuthenticationContext mockCtx = mock( AuthenticationContext.class );
    when( mockMgr.getAuthenticationContext( BASE_URL ) ).thenReturn( mockCtx );
    when( mockCtx.isAuthenticated() ).thenReturn( true );
    when( mockCtx.validateAndClearIfExpired() ).thenReturn( true );
    when( mockCtx.getJSessionId() ).thenReturn( JSESSION_ID );

    Client mockClient = mock( Client.class );
    WebServiceSpecification spec =
      WebServiceSpecification.getRestServiceSpecification( FakeFactory.FakeService.class, "create" );

    try ( MockedStatic<SpoonSessionManager> mockedSM = mockStatic( SpoonSessionManager.class );
          MockedStatic<ClientBuilder> mockedCB = mockStatic( ClientBuilder.class ) ) {

      mockedSM.when( SpoonSessionManager::getInstance ).thenReturn( mockMgr );
      mockedCB.when( () -> ClientBuilder.newClient( any( ClientConfig.class ) ) ).thenReturn( mockClient );

      Object result = invokePrivate( "createJaxRsPort",
        new Class<?>[] { String.class, String.class, WebServiceSpecification.class },
        USERNAME, PASSWORD, spec );

      assertNotNull( result );
      assertTrue( "Result must be a FakeService", result instanceof FakeFactory.FakeService );
    }

    // session-auth path — ClientRequestFilter registered, NOT HttpAuthenticationFeature
    verify( mockClient, times( 1 ) ).register( any( ClientRequestFilter.class ) );
    verify( mockClient, never() ).register( any( HttpAuthenticationFeature.class ) );
  }

  @Test
  public void testCreateJaxRsPort_FactoryReceivesCorrectPluginUri() throws Exception {
    SpoonSessionManager mockMgr = mock( SpoonSessionManager.class );
    when( mockMgr.getAuthenticationContext( BASE_URL ) ).thenReturn( null );

    Client mockClient = mock( Client.class );

    // Use a spec that points to a factory which records the URI it received.
    WebServiceSpecification spec =
      WebServiceSpecification.getRestServiceSpecification( UriCapturingFactory.FakeService.class, "create" );
    UriCapturingFactory.lastUri = null;

    try ( MockedStatic<SpoonSessionManager> mockedSM = mockStatic( SpoonSessionManager.class );
          MockedStatic<ClientBuilder> mockedCB = mockStatic( ClientBuilder.class ) ) {

      mockedSM.when( SpoonSessionManager::getInstance ).thenReturn( mockMgr );
      mockedCB.when( () -> ClientBuilder.newClient( any( ClientConfig.class ) ) ).thenReturn( mockClient );

      invokePrivate( "createJaxRsPort",
        new Class<?>[] { String.class, String.class, WebServiceSpecification.class },
        USERNAME, PASSWORD, spec );
    }

    assertNotNull( "Factory must have been invoked", UriCapturingFactory.lastUri );
    assertEquals( BASE_URL + "/plugin", UriCapturingFactory.lastUri.toString() );
  }

  @Test
  public void testCreateJaxRsPort_NoSuchMethod_ThrowsNoSuchMethodException() throws Exception {
    SpoonSessionManager mockMgr = mock( SpoonSessionManager.class );
    when( mockMgr.getAuthenticationContext( BASE_URL ) ).thenReturn( null );

    Client mockClient = mock( Client.class );
    // "nonExistentMethod" does not exist on FakeFactory
    WebServiceSpecification spec =
      WebServiceSpecification.getRestServiceSpecification( FakeFactory.FakeService.class, "nonExistentMethod" );

    try ( MockedStatic<SpoonSessionManager> mockedSM = mockStatic( SpoonSessionManager.class );
          MockedStatic<ClientBuilder> mockedCB = mockStatic( ClientBuilder.class ) ) {

      mockedSM.when( SpoonSessionManager::getInstance ).thenReturn( mockMgr );
      mockedCB.when( () -> ClientBuilder.newClient( any( ClientConfig.class ) ) ).thenReturn( mockClient );

      try {
        invokePrivate( "createJaxRsPort",
          new Class<?>[] { String.class, String.class, WebServiceSpecification.class },
          USERNAME, PASSWORD, spec );
        fail( "Expected NoSuchMethodException" );
      } catch ( InvocationTargetException e ) {
        assertTrue( "Cause must be NoSuchMethodException",
          e.getCause() instanceof NoSuchMethodException );
      }
    }
  }

  @Test
  public void testResolveServiceFuture_JaxWsSpec_ReturnsFuture() throws Exception {
    WebServiceSpecification spec = mock( WebServiceSpecification.class );
    when( spec.getServiceType() ).thenReturn( WebServiceSpecification.ServiceType.JAX_WS );

    Future<?> future = invokePrivate( "resolveServiceFuture",
      new Class<?>[] { String.class, String.class, Class.class,
        WebServiceSpecification.class, String.class },
      USERNAME, PASSWORD, String.class, spec, "userRoleListService" );

    assertNotNull( "Future must not be null for JAX_WS spec", future );
  }

  @Test
  public void testResolveServiceFuture_JaxRsSpec_ReturnsFuture() throws Exception {
    WebServiceSpecification spec = mock( WebServiceSpecification.class );
    when( spec.getServiceType() ).thenReturn( WebServiceSpecification.ServiceType.JAX_RS );

    Future<?> future = invokePrivate( "resolveServiceFuture",
      new Class<?>[] { String.class, String.class, Class.class,
        WebServiceSpecification.class, String.class },
      USERNAME, PASSWORD, String.class, spec, "purRepositoryPluginApiRevision" );

    assertNotNull( "Future must not be null for JAX_RS spec", future );
  }

  @Test
  public void testGetOrCreateJaxWsFuture_NoTrailingSlash_UrlFormedCorrectly() throws Exception {
    // BASE_URL = "http://localhost:8080/pentaho" — no trailing slash
    invokePrivate( "getOrCreateJaxWsFuture",
      new Class<?>[] { String.class, String.class, Class.class, String.class },
      USERNAME, PASSWORD, String.class, "myService" );

    Map<String, ?> cache = getServiceCache();
    String expectedUrlPrefix = BASE_URL + "/webservices/myService?wsdl";
    assertTrue( "Cache must contain key based on correct WSDL URL",
      cache.keySet().stream().anyMatch( k -> k.startsWith( expectedUrlPrefix ) ) );
    // Must NOT contain a double slash after the host
    assertTrue( "URL must not contain double slash in path",
      cache.keySet().stream().anyMatch( k -> !k.contains( "//webservices" ) ) );
  }

  @Test
  public void testGetOrCreateJaxWsFuture_TrailingSlash_NoDoubleSlashInUrl() throws Exception {
    WebServiceManager managerWithSlash =
      new WebServiceManager( BASE_URL + "/", USERNAME );

    Method method = WebServiceManager.class.getDeclaredMethod(
      "getOrCreateJaxWsFuture", String.class, String.class, Class.class, String.class );
    method.setAccessible( true );
    method.invoke( managerWithSlash, USERNAME, PASSWORD, String.class, "myService" );

    Map<String, ?> cache = getServiceCacheOf( managerWithSlash );
    assertTrue( "No double-slash must appear in any cache key",
      cache.keySet().stream().noneMatch( k -> k.contains( "//webservices" ) ) );
    String expectedPrefix = BASE_URL + "/webservices/myService?wsdl";
    assertTrue( "Cache key must start with the correct WSDL URL",
      cache.keySet().stream().anyMatch( k -> k.startsWith( expectedPrefix ) ) );
  }

  @Test
  public void testGetOrCreateJaxWsFuture_SameKey_ReturnsSameFuture() throws Exception {
    Future<?> first = invokePrivate( "getOrCreateJaxWsFuture",
      new Class<?>[] { String.class, String.class, Class.class, String.class },
      USERNAME, PASSWORD, String.class, "cachedService" );

    Future<?> second = invokePrivate( "getOrCreateJaxWsFuture",
      new Class<?>[] { String.class, String.class, Class.class, String.class },
      USERNAME, PASSWORD, String.class, "cachedService" );

    assertSame( "Second call must return the cached Future", first, second );
    assertEquals( "Cache must contain exactly one entry for this key", 1,
      getServiceCache().keySet().stream()
        .filter( k -> k.contains( "cachedService" ) ).count() );
  }

  @Test
  public void testGetOrCreateJaxWsFuture_DifferentServiceName_GeneratesDifferentFutures()
    throws Exception {
    Future<?> first = invokePrivate( "getOrCreateJaxWsFuture",
      new Class<?>[] { String.class, String.class, Class.class, String.class },
      USERNAME, PASSWORD, String.class, "serviceAlpha" );

    Future<?> second = invokePrivate( "getOrCreateJaxWsFuture",
      new Class<?>[] { String.class, String.class, Class.class, String.class },
      USERNAME, PASSWORD, String.class, "serviceBeta" );

    assertNotSame( "Different service names must produce different Future instances", first, second );
    Map<String, ?> cache = getServiceCache();
    assertTrue( cache.keySet().stream().anyMatch( k -> k.contains( "serviceAlpha" ) ) );
    assertTrue( cache.keySet().stream().anyMatch( k -> k.contains( "serviceBeta" ) ) );
  }

  @Test
  public void testGetOrCreateJaxWsFuture_DifferentClass_SameServiceName_GeneratesDifferentFutures()
    throws Exception {
    // Covers line 153: key = url + '_' + serviceName + '_' + clazz.getName()
    // Same serviceName but different clazz must produce distinct cache entries
    Future<?> first = invokePrivate( "getOrCreateJaxWsFuture",
      new Class<?>[] { String.class, String.class, Class.class, String.class },
      USERNAME, PASSWORD, String.class, "sharedWsService" );

    Future<?> second = invokePrivate( "getOrCreateJaxWsFuture",
      new Class<?>[] { String.class, String.class, Class.class, String.class },
      USERNAME, PASSWORD, Integer.class, "sharedWsService" );

    assertNotSame( "Different class types must produce different Future instances", first, second );
    Map<String, ?> cache = getServiceCache();
    // Both keys contain the service name but differ by class name suffix
    assertTrue( "Cache must contain entry for String class",
      cache.keySet().stream().anyMatch( k -> k.contains( "sharedWsService" )
        && k.endsWith( String.class.getName() ) ) );
    assertTrue( "Cache must contain entry for Integer class",
      cache.keySet().stream().anyMatch( k -> k.contains( "sharedWsService" )
        && k.endsWith( Integer.class.getName() ) ) );
  }

  @Test
  public void testGetOrCreateJaxRsFuture_ReturnsNonNullFutureAndPopulatesCache() throws Exception {
    WebServiceSpecification spec = mock( WebServiceSpecification.class );

    Future<?> future = invokePrivate( "getOrCreateJaxRsFuture",
      new Class<?>[] { String.class, String.class, Class.class,
        WebServiceSpecification.class, String.class },
      USERNAME, PASSWORD, String.class, spec, "restService" );

    assertNotNull( future );
    String expectedKey = BASE_URL + "_restService_" + String.class.getName();
    assertTrue( "Cache must contain JAX_RS key", getServiceCache().containsKey( expectedKey ) );
  }

  @Test
  public void testGetOrCreateJaxRsFuture_SameKey_ReturnsSameFuture() throws Exception {
    WebServiceSpecification spec = mock( WebServiceSpecification.class );

    Future<?> first = invokePrivate( "getOrCreateJaxRsFuture",
      new Class<?>[] { String.class, String.class, Class.class,
        WebServiceSpecification.class, String.class },
      USERNAME, PASSWORD, Integer.class, spec, "cachedRest" );

    Future<?> second = invokePrivate( "getOrCreateJaxRsFuture",
      new Class<?>[] { String.class, String.class, Class.class,
        WebServiceSpecification.class, String.class },
      USERNAME, PASSWORD, Integer.class, spec, "cachedRest" );

    assertSame( "Second call must return the cached Future", first, second );
  }

  @Test
  public void testGetOrCreateJaxRsFuture_DifferentClass_GeneratesDifferentFutures() throws Exception {
    WebServiceSpecification spec = mock( WebServiceSpecification.class );

    Future<?> first = invokePrivate( "getOrCreateJaxRsFuture",
      new Class<?>[] { String.class, String.class, Class.class,
        WebServiceSpecification.class, String.class },
      USERNAME, PASSWORD, String.class, spec, "sharedService" );

    Future<?> second = invokePrivate( "getOrCreateJaxRsFuture",
      new Class<?>[] { String.class, String.class, Class.class,
        WebServiceSpecification.class, String.class },
      USERNAME, PASSWORD, Integer.class, spec, "sharedService" );

    assertNotSame( "Different classes must produce different Future instances", first, second );
    Map<String, ?> cache = getServiceCache();
    assertTrue( cache.containsKey( BASE_URL + "_sharedService_" + String.class.getName() ) );
    assertTrue( cache.containsKey( BASE_URL + "_sharedService_" + Integer.class.getName() ) );
  }

  @SuppressWarnings( "unchecked" )
  private Map<String, Future<Object>> getServiceCache() throws Exception {
    return getServiceCacheOf( manager );
  }

  @SuppressWarnings( "unchecked" )
  private Map<String, Future<Object>> getServiceCacheOf( WebServiceManager target ) throws Exception {
    Field field = WebServiceManager.class.getDeclaredField( "serviceCache" );
    field.setAccessible( true );
    return (Map<String, Future<Object>>) field.get( target );
  }

  public static class UriCapturingFactory {
    public static class FakeService {
    }

    public static volatile URI lastUri = null;
    public static Client fakeClient = null;

    public static FakeService create( Client client, URI uri ) {
      fakeClient = client;
      lastUri = uri;
      return new FakeService();
    }
  }
  @Test
  public void testValidateRequest_DifferentUsername_ThrowsIllegalStateException() throws Exception {
    try {
      invokePrivate( "validateRequest", new Class<?>[] { String.class }, "differentUser" );
      fail( "Expected IllegalStateException" );
    } catch ( InvocationTargetException e ) {
      assertTrue( e.getCause() instanceof IllegalStateException );
    }
  }

  @Test
  public void testResolveServiceFuture_UnknownServiceType_ThrowsException() throws Exception {
    WebServiceSpecification spec = mock( WebServiceSpecification.class );
    when( spec.getServiceType() ).thenReturn( null ); // null causes NPE on .equals() in source

    try {
      invokePrivate( "resolveServiceFuture",
        new Class<?>[] { String.class, String.class, Class.class,
          WebServiceSpecification.class, String.class },
        USERNAME, PASSWORD, String.class, spec, "unknownService" );
      fail( "Expected exception for null service type" );
    } catch ( InvocationTargetException e ) {
      assertTrue( "Cause should be NullPointerException",
        e.getCause() instanceof NullPointerException );
    }
  }

  @Test
  public void testResolveServiceFuture_UnrecognizedServiceType_ThrowsIllegalStateException() throws Exception {
    WebServiceSpecification spec = mock( WebServiceSpecification.class );
    // Mock a ServiceType instance that is neither JAX_WS nor JAX_RS so both .equals() checks fail
    WebServiceSpecification.ServiceType unknownType = mock( WebServiceSpecification.ServiceType.class );
    when( spec.getServiceType() ).thenReturn( unknownType );

    try {
      invokePrivate( "resolveServiceFuture",
        new Class<?>[] { String.class, String.class, Class.class,
          WebServiceSpecification.class, String.class },
        USERNAME, PASSWORD, String.class, spec, "unknownService" );
      fail( "Expected IllegalStateException for unrecognized service type" );
    } catch ( InvocationTargetException e ) {
      assertTrue( "Cause should be IllegalStateException",
        e.getCause() instanceof IllegalStateException );
      assertTrue( "Message should mention unknown service type",
        e.getCause().getMessage().contains( "Unknown service type" ) );
    }
  }

  @Test
  public void testCreateService_NullServiceName_ThrowsIllegalStateException() throws Exception {
    // Inject a serviceNameMap entry with null serviceName
    WebServiceSpecification spec = mock( WebServiceSpecification.class );
    when( spec.getServiceName() ).thenReturn( null );

    Map<Class<?>, WebServiceSpecification> customMap = new HashMap<>();
    customMap.put( Runnable.class, spec );

    Field serviceNameMapField = WebServiceManager.class.getDeclaredField( "serviceNameMap" );
    serviceNameMapField.setAccessible( true );
    serviceNameMapField.set( manager, customMap );

    try {
      manager.createService( USERNAME, PASSWORD, Runnable.class );
      fail( "Expected IllegalStateException" );
    } catch ( IllegalStateException e ) {
      // expected
    }
  }

  @Test
  public void testCreateService_DifferentUsername_ThrowsIllegalStateException() throws Exception {
    // validateRequest fires before serviceName lookup
    try {
      manager.createService( "wrongUser", PASSWORD,
        org.pentaho.platform.security.userrole.ws.IUserRoleListWebService.class );
      fail( "Expected IllegalStateException" );
    } catch ( IllegalStateException e ) {
      // expected
    }
  }

  @SuppressWarnings( "unchecked" )
  @Test
  public void testUnwrapFuture_SuccessfulGet_NonInterface_ReturnsServiceDirectly() throws Exception {
    Future<Object> future = mock( Future.class );
    String service = "plainService";
    when( future.get() ).thenReturn( service );

    // String.class is NOT an interface, so no proxy wrapping
    Object result = invokePrivate( "unwrapFuture",
      new Class<?>[] { Future.class, Class.class }, future, String.class );

    assertSame( service, result );
  }

  @SuppressWarnings( "unchecked" )
  @Test
  public void testUnwrapFuture_SuccessfulGet_Interface_ReturnsProxy() throws Exception {
    Future<Object> future = mock( Future.class );
    Runnable service = mock( Runnable.class );
    when( future.get() ).thenReturn( service );

    // Runnable.class IS an interface, so UnifiedRepositoryInvocationHandler wraps it
    Object result = invokePrivate( "unwrapFuture",
      new Class<?>[] { Future.class, Class.class }, future, Runnable.class );

    assertNotNull( result );
    assertTrue( result instanceof Runnable );
    assertNotSame( service, result );
  }

  @SuppressWarnings( "unchecked" )
  private Throwable invokeUnwrapFutureExpectingFailure( Future<Object> future, Class<?> clazz ) {
    try {
      invokePrivate( "unwrapFuture",
        new Class<?>[] { Future.class, Class.class }, future, clazz );
      fail( "Expected exception from unwrapFuture" );
      return null; // unreachable
    } catch ( InvocationTargetException e ) {
      return e.getCause();
    } catch ( Exception e ) {
      fail( "Unexpected exception type: " + e.getClass().getName() );
      return null; // unreachable
    }
  }

  @SuppressWarnings( "unchecked" )
  @Test
  public void testUnwrapFuture_InterruptedException_ThrowsRuntimeException() throws Exception {
    Future<Object> future = mock( Future.class );
    when( future.get() ).thenThrow( new InterruptedException( "interrupted" ) );

    Throwable thrown = invokeUnwrapFutureExpectingFailure( future, String.class );

    assertTrue( thrown instanceof RuntimeException );
    assertTrue( thrown.getCause() instanceof InterruptedException );
  }

  @SuppressWarnings( "unchecked" )
  @Test
  public void testUnwrapFuture_ExecutionException_RuntimeCause_RethrowsCause() throws Exception {
    Future<Object> future = mock( Future.class );
    RuntimeException cause = new RuntimeException( "original runtime error" );
    when( future.get() ).thenThrow( new java.util.concurrent.ExecutionException( cause ) );

    Throwable thrown = invokeUnwrapFutureExpectingFailure( future, String.class );

    assertTrue( thrown instanceof RuntimeException );
    assertEquals( "original runtime error", thrown.getMessage() );
  }

  @SuppressWarnings( "unchecked" )
  @Test
  public void testUnwrapFuture_ExecutionException_MalformedURLCause_RethrowsCause() throws Exception {
    Future<Object> future = mock( Future.class );
    java.net.MalformedURLException cause = new java.net.MalformedURLException( "bad url" );
    when( future.get() ).thenThrow( new java.util.concurrent.ExecutionException( cause ) );

    Throwable thrown = invokeUnwrapFutureExpectingFailure( future, String.class );

    assertTrue( thrown instanceof java.net.MalformedURLException );
    assertEquals( "bad url", thrown.getMessage() );
  }

  @SuppressWarnings( "unchecked" )
  @Test
  public void testUnwrapFuture_ExecutionException_OtherCause_WrapsInRuntimeException() throws Exception {
    Future<Object> future = mock( Future.class );
    Exception cause = new java.io.IOException( "io error" );
    when( future.get() ).thenThrow( new java.util.concurrent.ExecutionException( cause ) );

    Throwable thrown = invokeUnwrapFutureExpectingFailure( future, String.class );

    assertTrue( thrown instanceof RuntimeException );
    assertTrue( thrown.getCause() instanceof java.util.concurrent.ExecutionException );
  }

  /**
   * Covers the main {@code createService} flow (lines 120-123): a valid service spec is looked up,
   * {@code resolveServiceFuture} produces a {@link Future}, and {@code unwrapFuture} resolves it
   * into the actual service instance returned to the caller.
   *
   * We pre-populate the serviceCache with a completed future so that the executor thread is not
   * spawned (MockedStatic is thread-local and would not be visible on the executor thread).
   */
  @Test
  public void testCreateService_ValidJaxRsSpec_ResolvesAndUnwrapsFutureSuccessfully() throws Exception {
    // 1. Register a JAX_RS spec for FakeFactory.FakeService in the serviceNameMap
    WebServiceSpecification fakeSpec =
      WebServiceSpecification.getRestServiceSpecification( FakeFactory.FakeService.class, "create" );

    Map<Class<?>, WebServiceSpecification> customMap = new HashMap<>();
    customMap.put( FakeFactory.FakeService.class, fakeSpec );

    Field serviceNameMapField = WebServiceManager.class.getDeclaredField( "serviceNameMap" );
    serviceNameMapField.setAccessible( true );
    serviceNameMapField.set( manager, customMap );

    // 2. Pre-populate the serviceCache with a completed future holding the service instance.
    //    This matches the key that getOrCreateJaxRsFuture would generate, so computeIfAbsent
    //    returns the existing entry and no async task is spawned.
    FakeFactory.FakeService expectedService = new FakeFactory.FakeService();
    String cacheKey = BASE_URL + "_create_" + FakeFactory.FakeService.class.getName();
    java.util.concurrent.CompletableFuture<Object> completedFuture =
      java.util.concurrent.CompletableFuture.completedFuture( expectedService );

    Map<String, Future<Object>> cache = getServiceCache();
    cache.put( cacheKey, completedFuture );

    // 3. Call createService — it will find the cached future and unwrap it directly
    FakeFactory.FakeService result =
      manager.createService( USERNAME, PASSWORD, FakeFactory.FakeService.class );

    assertNotNull( "createService must return a non-null service", result );
    // FakeService is a concrete class (not an interface) → unwrapFuture returns it directly
    assertSame( "unwrapFuture should return the exact service for non-interface classes",
      expectedService, result );
  }

  /**
   * Helper class that has a {@code logout()} method which throws a configurable exception.
   * Used by the {@code close()} tests to exercise the InvocationTargetException catch block.
   */
  public static class LogoutThrowingService {
    public static volatile RuntimeException exceptionToThrow = null;

    public void logout() {
      if ( exceptionToThrow != null ) {
        throw exceptionToThrow;
      }
    }
  }

  @Test
  public void testClose_LogoutThrowsNonSessionExpiredException_LogsDebug() throws Exception {
    // Set the logout() method to throw a non-401 RuntimeException.
    // Method.invoke() wraps it in InvocationTargetException.
    // isSessionExpiredException returns false → logDebug is called (lines 280-281).
    LogoutThrowingService.exceptionToThrow = new RuntimeException( "unexpected error" );

    LogoutThrowingService service = new LogoutThrowingService();
    String cacheKey = BASE_URL + "_someService_" + LogoutThrowingService.class.getName();
    java.util.concurrent.CompletableFuture<Object> completedFuture =
      java.util.concurrent.CompletableFuture.completedFuture( service );

    Map<String, Future<Object>> cache = getServiceCache();
    cache.put( cacheKey, completedFuture );

    // close() should not throw — the InvocationTargetException is caught and logged
    manager.close();

    // After close, serviceCache must be cleared
    assertTrue( "serviceCache must be empty after close()", cache.isEmpty() );
  }

  @Test
  public void testClose_LogoutThrowsSessionExpiredException_SilentlyIgnored() throws Exception {
    // Set the logout() method to throw a ClientTransportException with 401.
    // Method.invoke() wraps it in InvocationTargetException.
    // isSessionExpiredException returns true → silently ignored (line 280 condition is false).
    ClientTransportException cause = mock( ClientTransportException.class );
    when( cause.getMessage() ).thenReturn( "HTTP transport error: 401 Unauthorized" );
    LogoutThrowingService.exceptionToThrow = cause;

    LogoutThrowingService service = new LogoutThrowingService();
    String cacheKey = BASE_URL + "_someService_" + LogoutThrowingService.class.getName();
    java.util.concurrent.CompletableFuture<Object> completedFuture =
      java.util.concurrent.CompletableFuture.completedFuture( service );

    Map<String, Future<Object>> cache = getServiceCache();
    cache.put( cacheKey, completedFuture );

    // close() should not throw — the session expired exception is silently ignored
    manager.close();

    assertTrue( "serviceCache must be empty after close()", cache.isEmpty() );
  }
}
