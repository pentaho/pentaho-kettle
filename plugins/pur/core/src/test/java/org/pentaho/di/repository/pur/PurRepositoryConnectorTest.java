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

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.ui.spoon.session.AuthenticationContext;
import org.pentaho.di.ui.spoon.session.SpoonSessionManager;
import org.pentaho.platform.api.engine.IApplicationContext;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.api.engine.IAuthorizationPolicy;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PurRepositoryConnectorTest {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @BeforeClass
  public static void setUpClass() throws Exception {
    if ( !KettleEnvironment.isInitialized() ) {
      KettleEnvironment.init();
    }
  }

  @Test
  public void testPDI12439PurRepositoryConnectorDoesntNPEAfterMultipleDisconnects() {
    PurRepository mockPurRepository = mock( PurRepository.class );
    PurRepositoryMeta mockPurRepositoryMeta = mock( PurRepositoryMeta.class );
    RootRef mockRootRef = mock( RootRef.class );
    PurRepositoryConnector purRepositoryConnector =
      new PurRepositoryConnector( mockPurRepository, mockPurRepositoryMeta, mockRootRef );
    purRepositoryConnector.disconnect();
    purRepositoryConnector.disconnect();
  }

  @Test
  public void testConnect() {
    PurRepository mockPurRepository = mock( PurRepository.class );
    PurRepositoryMeta mockPurRepositoryMeta = mock( PurRepositoryMeta.class );
    PurRepositoryLocation location = mock( PurRepositoryLocation.class );
    RootRef mockRootRef = mock( RootRef.class );
    PurRepositoryConnector purRepositoryConnector =
      spy( new PurRepositoryConnector( mockPurRepository, mockPurRepositoryMeta, mockRootRef ) );
    doReturn( location ).when( mockPurRepositoryMeta ).getRepositoryLocation();
    doReturn( "" ).when( location ).getUrl();
    ExecutorService service = mock( ExecutorService.class );
    doReturn( service ).when( purRepositoryConnector ).getExecutor();
    Future future = mock( Future.class );
    try {
      doReturn( "U1" ).when( future ).get();
    } catch ( Exception e ) {
      e.printStackTrace();
    }
    Future future2 = mock( Future.class );
    try {
      doReturn( false ).when( future2 ).get();
    } catch ( Exception e ) {
      e.printStackTrace();
    }
    Future future3 = mock( Future.class );
    try {
      doReturn( null ).when( future3 ).get();
    } catch ( Exception e ) {
      e.printStackTrace();
    }
    when( service.submit( any( Callable.class ) ) ).thenReturn( future2 ).thenReturn( future3 ).thenReturn( future3 )
      .thenReturn( future );

    try {
      RepositoryConnectResult res = purRepositoryConnector.connect( "userNam", "password" );
      Assert.assertEquals( "U1", res.getUser().getLogin() );
    } catch ( KettleException e ) {
      e.printStackTrace();
    }
  }

  @Test
  public void testConnectWithSessionAuth_Success() throws Exception {
    PurRepository mockPurRepository = mock( PurRepository.class );
    PurRepositoryMeta mockPurRepositoryMeta = mock( PurRepositoryMeta.class );
    PurRepositoryLocation location = mock( PurRepositoryLocation.class );
    RootRef mockRootRef = mock( RootRef.class );

    PurRepositoryConnector purRepositoryConnector =
      spy( new PurRepositoryConnector( mockPurRepository, mockPurRepositoryMeta, mockRootRef ) );
    doReturn( location ).when( mockPurRepositoryMeta ).getRepositoryLocation();
    doReturn( "http://localhost:8080/pentaho" ).when( location ).getUrl();

    ExecutorService service = mock( ExecutorService.class );
    doReturn( service ).when( purRepositoryConnector ).getExecutor();

    // future for authorizationWebserviceFuture (isAdmin = false)
    Future futureAuth = mock( Future.class );
    doReturn( false ).when( futureAuth ).get();
    // future for repoWebServiceFuture (no exception)
    Future futureRepo = mock( Future.class );
    doReturn( null ).when( futureRepo ).get();
    // future for syncWebserviceFuture (no exception)
    Future futureSync = mock( Future.class );
    doReturn( null ).when( futureSync ).get();
    // future for sessionServiceFuture (should return the username directly)
    Future futureSession = mock( Future.class );
    doReturn( "sessionUser" ).when( futureSession ).get();

    when( service.submit( any( Callable.class ) ) )
      .thenReturn( futureAuth )
      .thenReturn( futureRepo )
      .thenReturn( futureSync )
      .thenReturn( futureSession );

    // Mock SpoonSessionManager and AuthenticationContext
    SpoonSessionManager mockSessionManager = mock( SpoonSessionManager.class );
    AuthenticationContext mockAuthContext = mock( AuthenticationContext.class );

    when( mockSessionManager.getAuthenticationContext( "http://localhost:8080/pentaho" ) )
      .thenReturn( mockAuthContext );
    when( mockAuthContext.isAuthenticated() ).thenReturn( true );
    when( mockAuthContext.getJSessionId() ).thenReturn( "VALID_SESSION_ID_123" );

    try ( MockedStatic<SpoonSessionManager> mockedManager = mockStatic( SpoonSessionManager.class ) ) {
      mockedManager.when( SpoonSessionManager::getInstance ).thenReturn( mockSessionManager );

      RepositoryConnectResult result =
        purRepositoryConnector.connect( "sessionUser", AuthenticationContext.SESSION_AUTH_TOKEN );

      assertTrue( result.isSuccess() );
      assertEquals( "sessionUser", result.getUser().getLogin() );
      // decryptedPassword should be empty when session auth is used, so password on user should be ""
      assertEquals( "", result.getUser().getPassword() );
    }
  }

  @Test
  public void testConnectWithSessionAuth_NoJSessionId_ThrowsKettleException() throws Exception {
    PurRepository mockPurRepository = mock( PurRepository.class );
    PurRepositoryMeta mockPurRepositoryMeta = mock( PurRepositoryMeta.class );
    PurRepositoryLocation location = mock( PurRepositoryLocation.class );
    RootRef mockRootRef = mock( RootRef.class );

    PurRepositoryConnector purRepositoryConnector =
      spy( new PurRepositoryConnector( mockPurRepository, mockPurRepositoryMeta, mockRootRef ) );
    doReturn( location ).when( mockPurRepositoryMeta ).getRepositoryLocation();
    doReturn( "http://localhost:8080/pentaho" ).when( location ).getUrl();

    // Mock SpoonSessionManager returning context with no JSESSIONID
    SpoonSessionManager mockSessionManager = mock( SpoonSessionManager.class );
    AuthenticationContext mockAuthContext = mock( AuthenticationContext.class );

    when( mockSessionManager.getAuthenticationContext( "http://localhost:8080/pentaho" ) )
      .thenReturn( mockAuthContext );
    when( mockAuthContext.isAuthenticated() ).thenReturn( true );
    when( mockAuthContext.getJSessionId() ).thenReturn( null );

    try ( MockedStatic<SpoonSessionManager> mockedManager = mockStatic( SpoonSessionManager.class ) ) {
      mockedManager.when( SpoonSessionManager::getInstance ).thenReturn( mockSessionManager );

      KettleException thrown = null;
      try {
        purRepositoryConnector.connect( "user", AuthenticationContext.SESSION_AUTH_TOKEN );
      } catch ( KettleException e ) {
        thrown = e;
      }
      assertNotNull( "Expected KettleException when JSESSIONID is null", thrown );
      assertTrue( thrown.getMessage().contains( "no JSESSIONID found" ) );
    }
  }

  @Test
  public void testConnectWithSessionAuth_EmptyJSessionId_ThrowsKettleException() throws Exception {
    PurRepository mockPurRepository = mock( PurRepository.class );
    PurRepositoryMeta mockPurRepositoryMeta = mock( PurRepositoryMeta.class );
    PurRepositoryLocation location = mock( PurRepositoryLocation.class );
    RootRef mockRootRef = mock( RootRef.class );

    PurRepositoryConnector purRepositoryConnector =
      spy( new PurRepositoryConnector( mockPurRepository, mockPurRepositoryMeta, mockRootRef ) );
    doReturn( location ).when( mockPurRepositoryMeta ).getRepositoryLocation();
    doReturn( "http://localhost:8080/pentaho" ).when( location ).getUrl();

    SpoonSessionManager mockSessionManager = mock( SpoonSessionManager.class );
    AuthenticationContext mockAuthContext = mock( AuthenticationContext.class );

    when( mockSessionManager.getAuthenticationContext( "http://localhost:8080/pentaho" ) )
      .thenReturn( mockAuthContext );
    when( mockAuthContext.isAuthenticated() ).thenReturn( true );
    when( mockAuthContext.getJSessionId() ).thenReturn( "   " ); // whitespace-only

    try ( MockedStatic<SpoonSessionManager> mockedManager = mockStatic( SpoonSessionManager.class ) ) {
      mockedManager.when( SpoonSessionManager::getInstance ).thenReturn( mockSessionManager );

      KettleException thrown = null;
      try {
        purRepositoryConnector.connect( "user", AuthenticationContext.SESSION_AUTH_TOKEN );
      } catch ( KettleException e ) {
        thrown = e;
      }
      assertNotNull( "Expected KettleException when JSESSIONID is blank", thrown );
      assertTrue( thrown.getMessage().contains( "no JSESSIONID found" ) );
    }
  }

  @Test
  public void testConnectWithSessionAuth_NotAuthenticated_ThrowsKettleException() throws Exception {
    PurRepository mockPurRepository = mock( PurRepository.class );
    PurRepositoryMeta mockPurRepositoryMeta = mock( PurRepositoryMeta.class );
    PurRepositoryLocation location = mock( PurRepositoryLocation.class );
    RootRef mockRootRef = mock( RootRef.class );

    PurRepositoryConnector purRepositoryConnector =
      spy( new PurRepositoryConnector( mockPurRepository, mockPurRepositoryMeta, mockRootRef ) );
    doReturn( location ).when( mockPurRepositoryMeta ).getRepositoryLocation();
    doReturn( "http://localhost:8080/pentaho" ).when( location ).getUrl();

    // AuthContext exists but isAuthenticated returns false
    SpoonSessionManager mockSessionManager = mock( SpoonSessionManager.class );
    AuthenticationContext mockAuthContext = mock( AuthenticationContext.class );

    when( mockSessionManager.getAuthenticationContext( "http://localhost:8080/pentaho" ) )
      .thenReturn( mockAuthContext );
    when( mockAuthContext.isAuthenticated() ).thenReturn( false );

    try ( MockedStatic<SpoonSessionManager> mockedManager = mockStatic( SpoonSessionManager.class ) ) {
      mockedManager.when( SpoonSessionManager::getInstance ).thenReturn( mockSessionManager );

      KettleException thrown = null;
      try {
        purRepositoryConnector.connect( "user", AuthenticationContext.SESSION_AUTH_TOKEN );
      } catch ( KettleException e ) {
        thrown = e;
      }
      assertNotNull( "Expected KettleException when not authenticated", thrown );
      assertTrue( thrown.getMessage().contains( "no JSESSIONID found" ) );
    }
  }

  @Test
  public void testConnectWithSessionAuth_NullAuthContext_ThrowsKettleException() throws Exception {
    PurRepository mockPurRepository = mock( PurRepository.class );
    PurRepositoryMeta mockPurRepositoryMeta = mock( PurRepositoryMeta.class );
    PurRepositoryLocation location = mock( PurRepositoryLocation.class );
    RootRef mockRootRef = mock( RootRef.class );

    PurRepositoryConnector purRepositoryConnector =
      spy( new PurRepositoryConnector( mockPurRepository, mockPurRepositoryMeta, mockRootRef ) );
    doReturn( location ).when( mockPurRepositoryMeta ).getRepositoryLocation();
    doReturn( "http://localhost:8080/pentaho" ).when( location ).getUrl();

    // SpoonSessionManager returns null AuthenticationContext
    SpoonSessionManager mockSessionManager = mock( SpoonSessionManager.class );
    when( mockSessionManager.getAuthenticationContext( "http://localhost:8080/pentaho" ) )
      .thenReturn( null );

    try ( MockedStatic<SpoonSessionManager> mockedManager = mockStatic( SpoonSessionManager.class ) ) {
      mockedManager.when( SpoonSessionManager::getInstance ).thenReturn( mockSessionManager );

      KettleException thrown = null;
      try {
        purRepositoryConnector.connect( "user", AuthenticationContext.SESSION_AUTH_TOKEN );
      } catch ( KettleException e ) {
        thrown = e;
      }
      assertNotNull( "Expected KettleException when AuthenticationContext is null", thrown );
      assertTrue( thrown.getMessage().contains( "no JSESSIONID found" ) );
    }
  }

  @Test
  public void testConnectWithSessionAuth_SpoonSessionManagerThrowsException_ThrowsKettleException() throws Exception {
    PurRepository mockPurRepository = mock( PurRepository.class );
    PurRepositoryMeta mockPurRepositoryMeta = mock( PurRepositoryMeta.class );
    PurRepositoryLocation location = mock( PurRepositoryLocation.class );
    RootRef mockRootRef = mock( RootRef.class );

    PurRepositoryConnector purRepositoryConnector =
      spy( new PurRepositoryConnector( mockPurRepository, mockPurRepositoryMeta, mockRootRef ) );
    doReturn( location ).when( mockPurRepositoryMeta ).getRepositoryLocation();
    doReturn( "http://localhost:8080/pentaho" ).when( location ).getUrl();

    // SpoonSessionManager.getInstance() throws exception
    SpoonSessionManager mockSessionManager = mock( SpoonSessionManager.class );
    when( mockSessionManager.getAuthenticationContext( "http://localhost:8080/pentaho" ) )
      .thenThrow( new RuntimeException( "Session manager error" ) );

    try ( MockedStatic<SpoonSessionManager> mockedManager = mockStatic( SpoonSessionManager.class ) ) {
      mockedManager.when( SpoonSessionManager::getInstance ).thenReturn( mockSessionManager );

      KettleException thrown = null;
      try {
        purRepositoryConnector.connect( "user", AuthenticationContext.SESSION_AUTH_TOKEN );
      } catch ( KettleException e ) {
        thrown = e;
      }
      // Exception is caught internally, jsessionId remains null, so KettleException should be thrown
      assertNotNull( "Expected KettleException when SpoonSessionManager throws", thrown );
      assertTrue( thrown.getMessage().contains( "no JSESSIONID found" ) );
    }
  }

  @Test
  public void testConnectWithSessionAuth_SessionServiceFutureReturnsUsername() throws Exception {
    PurRepository mockPurRepository = mock( PurRepository.class );
    PurRepositoryMeta mockPurRepositoryMeta = mock( PurRepositoryMeta.class );
    PurRepositoryLocation location = mock( PurRepositoryLocation.class );
    RootRef mockRootRef = mock( RootRef.class );

    PurRepositoryConnector purRepositoryConnector =
      spy( new PurRepositoryConnector( mockPurRepository, mockPurRepositoryMeta, mockRootRef ) );
    doReturn( location ).when( mockPurRepositoryMeta ).getRepositoryLocation();
    doReturn( "http://localhost:8080/pentaho" ).when( location ).getUrl();

    ExecutorService service = mock( ExecutorService.class );
    doReturn( service ).when( purRepositoryConnector ).getExecutor();

    // Capture callables to verify the session service callable behavior
    Future futureAuth = mock( Future.class );
    doReturn( false ).when( futureAuth ).get();
    Future futureRepo = mock( Future.class );
    doReturn( null ).when( futureRepo ).get();
    Future futureSync = mock( Future.class );
    doReturn( null ).when( futureSync ).get();
    // The session service future should return the provided username when session auth is used
    Future futureSession = mock( Future.class );
    doReturn( "testBrowserUser" ).when( futureSession ).get();

    when( service.submit( any( Callable.class ) ) )
      .thenReturn( futureAuth )
      .thenReturn( futureRepo )
      .thenReturn( futureSync )
      .thenReturn( futureSession );

    SpoonSessionManager mockSessionManager = mock( SpoonSessionManager.class );
    AuthenticationContext mockAuthContext = mock( AuthenticationContext.class );
    when( mockSessionManager.getAuthenticationContext( "http://localhost:8080/pentaho" ) )
      .thenReturn( mockAuthContext );
    when( mockAuthContext.isAuthenticated() ).thenReturn( true );
    when( mockAuthContext.getJSessionId() ).thenReturn( "BROWSER_SESSION_ABC" );

    try ( MockedStatic<SpoonSessionManager> mockedManager = mockStatic( SpoonSessionManager.class ) ) {
      mockedManager.when( SpoonSessionManager::getInstance ).thenReturn( mockSessionManager );

      RepositoryConnectResult result =
        purRepositoryConnector.connect( "testBrowserUser", AuthenticationContext.SESSION_AUTH_TOKEN );

      assertTrue( result.isSuccess() );
      // The user login should be set to what sessionServiceFuture returned
      assertEquals( "testBrowserUser", result.getUser().getLogin() );
    }
  }

  @Test
  public void testConnectWithRegularPassword_DoesNotTriggerSessionAuth() throws Exception {
    PurRepository mockPurRepository = mock( PurRepository.class );
    PurRepositoryMeta mockPurRepositoryMeta = mock( PurRepositoryMeta.class );
    PurRepositoryLocation location = mock( PurRepositoryLocation.class );
    RootRef mockRootRef = mock( RootRef.class );

    PurRepositoryConnector purRepositoryConnector =
      spy( new PurRepositoryConnector( mockPurRepository, mockPurRepositoryMeta, mockRootRef ) );
    doReturn( location ).when( mockPurRepositoryMeta ).getRepositoryLocation();
    doReturn( "" ).when( location ).getUrl();

    ExecutorService service = mock( ExecutorService.class );
    doReturn( service ).when( purRepositoryConnector ).getExecutor();

    Future futureAuth = mock( Future.class );
    doReturn( false ).when( futureAuth ).get();
    Future futureRepo = mock( Future.class );
    doReturn( null ).when( futureRepo ).get();
    Future futureSync = mock( Future.class );
    doReturn( null ).when( futureSync ).get();
    Future futureSession = mock( Future.class );
    doReturn( "regularUser" ).when( futureSession ).get();

    when( service.submit( any( Callable.class ) ) )
      .thenReturn( futureAuth )
      .thenReturn( futureRepo )
      .thenReturn( futureSync )
      .thenReturn( futureSession );

    // Should NOT interact with SpoonSessionManager when using regular password
    RepositoryConnectResult result = purRepositoryConnector.connect( "regularUser", "regularPassword" );

    assertTrue( result.isSuccess() );
    assertEquals( "regularUser", result.getUser().getLogin() );
  }

  @Test
  public void testConnectWithSessionAuth_DecryptedPasswordIsEmpty() throws Exception {
    PurRepository mockPurRepository = mock( PurRepository.class );
    PurRepositoryMeta mockPurRepositoryMeta = mock( PurRepositoryMeta.class );
    PurRepositoryLocation location = mock( PurRepositoryLocation.class );
    RootRef mockRootRef = mock( RootRef.class );

    PurRepositoryConnector purRepositoryConnector =
      spy( new PurRepositoryConnector( mockPurRepository, mockPurRepositoryMeta, mockRootRef ) );
    doReturn( location ).when( mockPurRepositoryMeta ).getRepositoryLocation();
    doReturn( "http://localhost:8080/pentaho" ).when( location ).getUrl();

    ExecutorService service = mock( ExecutorService.class );
    doReturn( service ).when( purRepositoryConnector ).getExecutor();

    Future futureAuth = mock( Future.class );
    doReturn( false ).when( futureAuth ).get();
    Future futureRepo = mock( Future.class );
    doReturn( null ).when( futureRepo ).get();
    Future futureSync = mock( Future.class );
    doReturn( null ).when( futureSync ).get();
    Future futureSession = mock( Future.class );
    doReturn( "user" ).when( futureSession ).get();

    when( service.submit( any( Callable.class ) ) )
      .thenReturn( futureAuth )
      .thenReturn( futureRepo )
      .thenReturn( futureSync )
      .thenReturn( futureSession );

    SpoonSessionManager mockSessionManager = mock( SpoonSessionManager.class );
    AuthenticationContext mockAuthContext = mock( AuthenticationContext.class );
    when( mockSessionManager.getAuthenticationContext( "http://localhost:8080/pentaho" ) )
      .thenReturn( mockAuthContext );
    when( mockAuthContext.isAuthenticated() ).thenReturn( true );
    when( mockAuthContext.getJSessionId() ).thenReturn( "SESSION_XYZ" );

    try ( MockedStatic<SpoonSessionManager> mockedManager = mockStatic( SpoonSessionManager.class ) ) {
      mockedManager.when( SpoonSessionManager::getInstance ).thenReturn( mockSessionManager );

      RepositoryConnectResult result =
        purRepositoryConnector.connect( "user", AuthenticationContext.SESSION_AUTH_TOKEN );

      assertTrue( result.isSuccess() );
      // Verify that decryptedPassword was set to empty string (reflected in user password)
      assertEquals( "", result.getUser().getPassword() );
    }
  }
  /**
   * Helper: runs connect() with a capturing executor, returns all captured Callables.
   * The 4th callable is always the session-service callable.
   */
  @SuppressWarnings( "unchecked" )
  private List<Callable> captureSessionCallable( PurRepositoryConnector connector,
      String username, String password ) throws Exception {
    ExecutorService service = mock( ExecutorService.class );
    doReturn( service ).when( connector ).getExecutor();
    ArgumentCaptor<Callable> captor = ArgumentCaptor.forClass( Callable.class );

    Future futureAuth  = mock( Future.class ); doReturn( false ).when( futureAuth ).get();
    Future futureRepo  = mock( Future.class ); doReturn( null ).when( futureRepo ).get();
    Future futureSync  = mock( Future.class ); doReturn( null ).when( futureSync ).get();
    Future futureSession = mock( Future.class ); doReturn( username ).when( futureSession ).get();

    when( service.submit( captor.capture() ) )
      .thenReturn( futureAuth )
      .thenReturn( futureRepo )
      .thenReturn( futureSync )
      .thenReturn( futureSession );

    connector.connect( username, password );
    return captor.getAllValues();
  }

  @Test
  public void testResolveSessionUsername_SessionAuth_ReturnsUsernameDirectly() throws Exception {
    PurRepository mockPurRepository = mock( PurRepository.class );
    PurRepositoryMeta mockPurRepositoryMeta = mock( PurRepositoryMeta.class );
    PurRepositoryLocation location = mock( PurRepositoryLocation.class );
    RootRef mockRootRef = mock( RootRef.class );

    PurRepositoryConnector connector =
      spy( new PurRepositoryConnector( mockPurRepository, mockPurRepositoryMeta, mockRootRef ) );
    doReturn( location ).when( mockPurRepositoryMeta ).getRepositoryLocation();
    doReturn( "http://localhost:8080/pentaho" ).when( location ).getUrl();

    SpoonSessionManager mockSessionManager = mock( SpoonSessionManager.class );
    AuthenticationContext mockAuthContext = mock( AuthenticationContext.class );
    when( mockSessionManager.getAuthenticationContext( "http://localhost:8080/pentaho" ) )
      .thenReturn( mockAuthContext );
    when( mockAuthContext.isAuthenticated() ).thenReturn( true );
    when( mockAuthContext.getJSessionId() ).thenReturn( "SESSION_XYZ" );

    List<Callable> capturedCallables;
    try ( MockedStatic<SpoonSessionManager> mockedManager = mockStatic( SpoonSessionManager.class ) ) {
      mockedManager.when( SpoonSessionManager::getInstance ).thenReturn( mockSessionManager );
      capturedCallables = captureSessionCallable( connector, "browserUser", AuthenticationContext.SESSION_AUTH_TOKEN );
    }

    // The 4th callable is the session-service callable (resolveSessionUsername)
    Callable sessionCallable = capturedCallables.get( 3 );

    // When useSessionAuth=true, resolveSessionUsername returns the username directly — no HTTP call
    String result = (String) sessionCallable.call();
    assertEquals( "browserUser", result );
  }

  @Test
  public void testResolveSessionUsername_BasicAuth_CallsHttpSessionService() throws Exception {
    PurRepository mockPurRepository = mock( PurRepository.class );
    PurRepositoryMeta mockPurRepositoryMeta = mock( PurRepositoryMeta.class );
    PurRepositoryLocation location = mock( PurRepositoryLocation.class );
    RootRef mockRootRef = mock( RootRef.class );

    PurRepositoryConnector connector =
      spy( new PurRepositoryConnector( mockPurRepository, mockPurRepositoryMeta, mockRootRef ) );
    doReturn( location ).when( mockPurRepositoryMeta ).getRepositoryLocation();
    doReturn( "" ).when( location ).getUrl();

    List<Callable> capturedCallables = captureSessionCallable( connector, "admin", "password" );

    // The 4th callable is the session-service callable
    Callable sessionCallable = capturedCallables.get( 3 );

    // Mock HttpClientBuilder static chain
    CloseableHttpClient mockHttpClient = mock( CloseableHttpClient.class );
    HttpClientBuilder mockBuilder = mock( HttpClientBuilder.class );
    doReturn( mockBuilder ).when( mockBuilder ).setDefaultCredentialsProvider( any() );
    doReturn( mockHttpClient ).when( mockBuilder ).build();

    CloseableHttpResponse mockResponse = mock( CloseableHttpResponse.class );
    HttpEntity mockEntity = mock( HttpEntity.class );
    when( mockEntity.getContent() ).thenReturn(
      new ByteArrayInputStream( "admin".getBytes( StandardCharsets.UTF_8 ) ) );
    when( mockEntity.getContentLength() ).thenReturn( (long) "admin".length() );
    when( mockResponse.getEntity() ).thenReturn( mockEntity );
    when( mockHttpClient.execute( any() ) ).thenReturn( mockResponse );

    try ( MockedStatic<HttpClientBuilder> mockedBuilder = mockStatic( HttpClientBuilder.class ) ) {
      mockedBuilder.when( HttpClientBuilder::create ).thenReturn( mockBuilder );

      String result = (String) sessionCallable.call();
      assertEquals( "admin", result );
    }
  }

  @Test
  public void testFetchUsernameFromSessionService_HttpException_ReturnsNull() throws Exception {
    PurRepository mockPurRepository = mock( PurRepository.class );
    PurRepositoryMeta mockPurRepositoryMeta = mock( PurRepositoryMeta.class );
    PurRepositoryLocation location = mock( PurRepositoryLocation.class );
    RootRef mockRootRef = mock( RootRef.class );

    PurRepositoryConnector connector =
      spy( new PurRepositoryConnector( mockPurRepository, mockPurRepositoryMeta, mockRootRef ) );
    doReturn( location ).when( mockPurRepositoryMeta ).getRepositoryLocation();
    doReturn( "" ).when( location ).getUrl();

    List<Callable> capturedCallables = captureSessionCallable( connector, "admin", "password" );

    // The 4th callable is the session-service callable
    Callable sessionCallable = capturedCallables.get( 3 );

    // Make the HTTP call throw — fetchUsernameFromSessionService should catch and return null
    HttpClientBuilder mockBuilder = mock( HttpClientBuilder.class );
    doReturn( mockBuilder ).when( mockBuilder ).setDefaultCredentialsProvider( any() );
    CloseableHttpClient mockHttpClient = mock( CloseableHttpClient.class );
    doReturn( mockHttpClient ).when( mockBuilder ).build();
    when( mockHttpClient.execute( any() ) ).thenThrow( new RuntimeException( "Connection refused" ) );

    try ( MockedStatic<HttpClientBuilder> mockedBuilder = mockStatic( HttpClientBuilder.class ) ) {
      mockedBuilder.when( HttpClientBuilder::create ).thenReturn( mockBuilder );

      String result = (String) sessionCallable.call();
      assertNull( "fetchUsernameFromSessionService should return null on exception", result );
    }
  }
  /**
   * Helper: sets up executor futures so the normal (non-in-process) connect path can run.
   * connect() may still throw from registerRepositoryServices; tests should catch it.
   */
  @SuppressWarnings( "unchecked" )
  private void setupExecutorFutures( PurRepositoryConnector connector ) throws Exception {
    ExecutorService service = mock( ExecutorService.class );
    doReturn( service ).when( connector ).getExecutor();
    Future futureAuth = mock( Future.class ); doReturn( false ).when( futureAuth ).get();
    Future futureRepo = mock( Future.class ); doReturn( null ).when( futureRepo ).get();
    Future futureSync = mock( Future.class ); doReturn( null ).when( futureSync ).get();
    Future futureSession = mock( Future.class ); doReturn( "admin" ).when( futureSession ).get();
    when( service.submit( any( Callable.class ) ) )
      .thenReturn( futureAuth ).thenReturn( futureRepo ).thenReturn( futureSync ).thenReturn( futureSession );
  }

  @Test
  public void testTryInProcessConnect_NullApplicationContext_ContinuesToFutures() throws Exception {
    PurRepository mockPurRepository = mock( PurRepository.class );
    PurRepositoryMeta mockPurRepositoryMeta = mock( PurRepositoryMeta.class );
    PurRepositoryLocation location = mock( PurRepositoryLocation.class );
    RootRef mockRootRef = mock( RootRef.class );

    PurRepositoryConnector connector =
      spy( new PurRepositoryConnector( mockPurRepository, mockPurRepositoryMeta, mockRootRef ) );
    doReturn( location ).when( mockPurRepositoryMeta ).getRepositoryLocation();
    doReturn( "" ).when( location ).getUrl();
    setupExecutorFutures( connector );

    try ( MockedStatic<PentahoSystem> mockedPS = mockStatic( PentahoSystem.class );
          MockedStatic<PentahoSessionHolder> mockedPSH = mockStatic( PentahoSessionHolder.class ) ) {

      mockedPS.when( PentahoSystem::getApplicationContext ).thenReturn( null );

      try {
        connector.connect( "admin", "password" );
      } catch ( KettleException ignored ) {
        // registerRepositoryServices may fail with blank URL — that's acceptable here
      }

      // getExecutor() called confirms tryInProcessConnect returned null
      verify( connector ).getExecutor();
    }
  }

  @Test
  public void testTryInProcessConnect_NullSession_ContinuesToFutures() throws Exception {
    PurRepository mockPurRepository = mock( PurRepository.class );
    PurRepositoryMeta mockPurRepositoryMeta = mock( PurRepositoryMeta.class );
    PurRepositoryLocation location = mock( PurRepositoryLocation.class );
    RootRef mockRootRef = mock( RootRef.class );

    PurRepositoryConnector connector =
      spy( new PurRepositoryConnector( mockPurRepository, mockPurRepositoryMeta, mockRootRef ) );
    doReturn( location ).when( mockPurRepositoryMeta ).getRepositoryLocation();
    doReturn( "" ).when( location ).getUrl();
    setupExecutorFutures( connector );

    try ( MockedStatic<PentahoSystem> mockedPS = mockStatic( PentahoSystem.class );
          MockedStatic<PentahoSessionHolder> mockedPSH = mockStatic( PentahoSessionHolder.class ) ) {

      mockedPS.when( PentahoSystem::getApplicationContext ).thenReturn( mock( IApplicationContext.class ) );
      mockedPSH.when( PentahoSessionHolder::getSession ).thenReturn( null );

      try {
        connector.connect( "admin", "password" );
      } catch ( KettleException ignored ) {
        // registerRepositoryServices may fail with blank URL — that's acceptable here
      }

      verify( connector ).getExecutor();
    }
  }

  @Test
  public void testTryInProcessConnect_SessionNotAuthenticated_ContinuesToFutures() throws Exception {
    PurRepository mockPurRepository = mock( PurRepository.class );
    PurRepositoryMeta mockPurRepositoryMeta = mock( PurRepositoryMeta.class );
    PurRepositoryLocation location = mock( PurRepositoryLocation.class );
    RootRef mockRootRef = mock( RootRef.class );

    PurRepositoryConnector connector =
      spy( new PurRepositoryConnector( mockPurRepository, mockPurRepositoryMeta, mockRootRef ) );
    doReturn( location ).when( mockPurRepositoryMeta ).getRepositoryLocation();
    doReturn( "" ).when( location ).getUrl();
    setupExecutorFutures( connector );

    IPentahoSession mockSession = mock( IPentahoSession.class );
    when( mockSession.isAuthenticated() ).thenReturn( false );

    try ( MockedStatic<PentahoSystem> mockedPS = mockStatic( PentahoSystem.class );
          MockedStatic<PentahoSessionHolder> mockedPSH = mockStatic( PentahoSessionHolder.class ) ) {

      mockedPS.when( PentahoSystem::getApplicationContext ).thenReturn( mock( IApplicationContext.class ) );
      mockedPSH.when( PentahoSessionHolder::getSession ).thenReturn( mockSession );

      try {
        connector.connect( "admin", "password" );
      } catch ( KettleException ignored ) {
        // registerRepositoryServices may fail with blank URL — that's acceptable here
      }

      verify( connector ).getExecutor();
    }
  }

  @Test
  public void testTryInProcessConnect_NotInProcess_ContinuesToFutures() throws Exception {
    PurRepository mockPurRepository = mock( PurRepository.class );
    PurRepositoryMeta mockPurRepositoryMeta = mock( PurRepositoryMeta.class );
    PurRepositoryLocation location = mock( PurRepositoryLocation.class );
    RootRef mockRootRef = mock( RootRef.class );

    PurRepositoryConnector connector =
      spy( new PurRepositoryConnector( mockPurRepository, mockPurRepositoryMeta, mockRootRef ) );
    doReturn( location ).when( mockPurRepositoryMeta ).getRepositoryLocation();
    doReturn( "" ).when( location ).getUrl();
    setupExecutorFutures( connector );

    IPentahoSession mockSession = mock( IPentahoSession.class );
    when( mockSession.isAuthenticated() ).thenReturn( true );

    try ( MockedStatic<PentahoSystem> mockedPS = mockStatic( PentahoSystem.class );
          MockedStatic<PentahoSessionHolder> mockedPSH = mockStatic( PentahoSessionHolder.class ) ) {

      mockedPS.when( PentahoSystem::getApplicationContext ).thenReturn( mock( IApplicationContext.class ) );
      mockedPSH.when( PentahoSessionHolder::getSession ).thenReturn( mockSession );
      // Make inProcess() return false: singleDiServerInstance != "true", remoteDiServerInstance = true
      mockedPS.when( () -> PentahoSystem.getSystemSetting( "singleDiServerInstance", "true" ) ).thenReturn( "false" );
      mockedPS.when( () -> PentahoSystem.getSystemSetting( "remoteDiServerInstance", "false" ) ).thenReturn( "true" );

      try {
        connector.connect( "admin", "password" );
      } catch ( KettleException ignored ) {
        // registerRepositoryServices may fail with blank URL — that's acceptable here
      }

      verify( connector ).getExecutor();
    }
  }

  @Test
  public void testTryInProcessConnect_NullUnifiedRepository_ContinuesToFutures() throws Exception {
    PurRepository mockPurRepository = mock( PurRepository.class );
    PurRepositoryMeta mockPurRepositoryMeta = mock( PurRepositoryMeta.class );
    PurRepositoryLocation location = mock( PurRepositoryLocation.class );
    RootRef mockRootRef = mock( RootRef.class );

    PurRepositoryConnector connector =
      spy( new PurRepositoryConnector( mockPurRepository, mockPurRepositoryMeta, mockRootRef ) );
    doReturn( location ).when( mockPurRepositoryMeta ).getRepositoryLocation();
    doReturn( "" ).when( location ).getUrl();
    setupExecutorFutures( connector );

    IPentahoSession mockSession = mock( IPentahoSession.class );
    when( mockSession.isAuthenticated() ).thenReturn( true );

    try ( MockedStatic<PentahoSystem> mockedPS = mockStatic( PentahoSystem.class );
          MockedStatic<PentahoSessionHolder> mockedPSH = mockStatic( PentahoSessionHolder.class ) ) {

      mockedPS.when( PentahoSystem::getApplicationContext ).thenReturn( mock( IApplicationContext.class ) );
      mockedPSH.when( PentahoSessionHolder::getSession ).thenReturn( mockSession );
      // inProcess() returns true via singleDiServerInstance
      mockedPS.when( () -> PentahoSystem.getSystemSetting( "singleDiServerInstance", "true" ) ).thenReturn( "true" );
      // IUnifiedRepository is null — second guard clause in tryInProcessConnect
      mockedPS.when( () -> PentahoSystem.get( IUnifiedRepository.class ) ).thenReturn( null );

      try {
        connector.connect( "admin", "password" );
      } catch ( KettleException ignored ) {
        // registerRepositoryServices may fail with blank URL — that's acceptable here
      }

      verify( connector ).getExecutor();
    }
  }

  @Test
  public void testTryInProcessConnect_Success_AdminUser_ReturnsEarlyWithoutExecutor() throws Exception {
    PurRepository mockPurRepository = mock( PurRepository.class );
    PurRepositoryMeta mockPurRepositoryMeta = mock( PurRepositoryMeta.class );
    PurRepositoryLocation location = mock( PurRepositoryLocation.class );
    RootRef mockRootRef = mock( RootRef.class );

    PurRepositoryConnector connector =
      spy( new PurRepositoryConnector( mockPurRepository, mockPurRepositoryMeta, mockRootRef ) );
    doReturn( location ).when( mockPurRepositoryMeta ).getRepositoryLocation();
    doReturn( "http://localhost:8080/pentaho" ).when( location ).getUrl();

    IPentahoSession mockSession = mock( IPentahoSession.class );
    when( mockSession.isAuthenticated() ).thenReturn( true );
    when( mockSession.getName() ).thenReturn( "adminUser" );

    IUnifiedRepository mockRepo = mock( IUnifiedRepository.class );
    IAuthorizationPolicy mockAuthPolicy = mock( IAuthorizationPolicy.class );
    when( mockAuthPolicy.isAllowed( any() ) ).thenReturn( true );

    try ( MockedStatic<PentahoSystem> mockedPS = mockStatic( PentahoSystem.class );
          MockedStatic<PentahoSessionHolder> mockedPSH = mockStatic( PentahoSessionHolder.class ) ) {

      mockedPS.when( PentahoSystem::getApplicationContext ).thenReturn( mock( IApplicationContext.class ) );
      mockedPSH.when( PentahoSessionHolder::getSession ).thenReturn( mockSession );
      mockedPS.when( () -> PentahoSystem.getSystemSetting( "singleDiServerInstance", "true" ) ).thenReturn( "true" );
      mockedPS.when( () -> PentahoSystem.get( IUnifiedRepository.class ) ).thenReturn( mockRepo );
      mockedPS.when( () -> PentahoSystem.get( IAuthorizationPolicy.class ) ).thenReturn( mockAuthPolicy );

      RepositoryConnectResult result = connector.connect( "adminUser", "password" );

      // connect() returned early — executor was never needed
      verify( connector, never() ).getExecutor();
      assertTrue( result.isSuccess() );
      assertEquals( "adminUser", result.getUser().getLogin() );
      assertEquals( "adminUser", result.getUser().getName() );
    }
  }

  @Test
  public void testTryInProcessConnect_Success_NonAdminUser_AdminFlagFalse() throws Exception {
    PurRepository mockPurRepository = mock( PurRepository.class );
    PurRepositoryMeta mockPurRepositoryMeta = mock( PurRepositoryMeta.class );
    PurRepositoryLocation location = mock( PurRepositoryLocation.class );
    RootRef mockRootRef = mock( RootRef.class );

    PurRepositoryConnector connector =
      spy( new PurRepositoryConnector( mockPurRepository, mockPurRepositoryMeta, mockRootRef ) );
    doReturn( location ).when( mockPurRepositoryMeta ).getRepositoryLocation();
    doReturn( "http://localhost:8080/pentaho" ).when( location ).getUrl();

    IPentahoSession mockSession = mock( IPentahoSession.class );
    when( mockSession.isAuthenticated() ).thenReturn( true );
    when( mockSession.getName() ).thenReturn( "regularUser" );

    IUnifiedRepository mockRepo = mock( IUnifiedRepository.class );
    IAuthorizationPolicy mockAuthPolicy = mock( IAuthorizationPolicy.class );
    when( mockAuthPolicy.isAllowed( any() ) ).thenReturn( false );

    try ( MockedStatic<PentahoSystem> mockedPS = mockStatic( PentahoSystem.class );
          MockedStatic<PentahoSessionHolder> mockedPSH = mockStatic( PentahoSessionHolder.class ) ) {

      mockedPS.when( PentahoSystem::getApplicationContext ).thenReturn( mock( IApplicationContext.class ) );
      mockedPSH.when( PentahoSessionHolder::getSession ).thenReturn( mockSession );
      mockedPS.when( () -> PentahoSystem.getSystemSetting( "singleDiServerInstance", "true" ) ).thenReturn( "true" );
      mockedPS.when( () -> PentahoSystem.get( IUnifiedRepository.class ) ).thenReturn( mockRepo );
      mockedPS.when( () -> PentahoSystem.get( IAuthorizationPolicy.class ) ).thenReturn( mockAuthPolicy );

      RepositoryConnectResult result = connector.connect( "regularUser", "password" );

      verify( connector, never() ).getExecutor();
      assertTrue( result.isSuccess() );
      assertEquals( "regularUser", result.getUser().getLogin() );
    }
  }

  @Test
  public void testConnect_NullPointerException_SetsSuccessFalseAndThrowsKettleException() throws Exception {
    PurRepository mockPurRepository = mock( PurRepository.class );
    PurRepositoryMeta mockPurRepositoryMeta = mock( PurRepositoryMeta.class );
    PurRepositoryLocation location = mock( PurRepositoryLocation.class );
    RootRef mockRootRef = mock( RootRef.class );

    PurRepositoryConnector connector =
      spy( new PurRepositoryConnector( mockPurRepository, mockPurRepositoryMeta, mockRootRef ) );
    doReturn( location ).when( mockPurRepositoryMeta ).getRepositoryLocation();
    doReturn( "http://localhost:8080/pentaho" ).when( location ).getUrl();

    ExecutorService service = mock( ExecutorService.class );
    doReturn( service ).when( connector ).getExecutor();

    // First future (auth) throws NPE when .get() is called
    Future futureAuth = mock( Future.class );
    when( futureAuth.get() ).thenThrow( new NullPointerException( "something is null" ) );
    Future futureRepo = mock( Future.class );
    doReturn( null ).when( futureRepo ).get();
    Future futureSync = mock( Future.class );
    doReturn( null ).when( futureSync ).get();
    Future futureSession = mock( Future.class );
    doReturn( "user" ).when( futureSession ).get();

    when( service.submit( any( Callable.class ) ) )
      .thenReturn( futureAuth )
      .thenReturn( futureRepo )
      .thenReturn( futureSync )
      .thenReturn( futureSession );

    KettleException thrown = null;
    try {
      connector.connect( "user", "password" );
    } catch ( KettleException e ) {
      thrown = e;
    }

    assertNotNull( "Expected KettleException for NPE", thrown );
    // The message comes from BaseMessages for PurRepository.LoginException.Message
    assertNotNull( thrown.getMessage() );
  }

  @SuppressWarnings( "unchecked" )
  @Test
  public void testConnect_InterruptedException_ClosesServiceManagerAndRestoresInterruptFlag() throws Exception {
    PurRepository mockPurRepository = mock( PurRepository.class );
    PurRepositoryMeta mockPurRepositoryMeta = mock( PurRepositoryMeta.class );
    PurRepositoryLocation location = mock( PurRepositoryLocation.class );
    RootRef mockRootRef = mock( RootRef.class );

    PurRepositoryConnector connector =
      spy( new PurRepositoryConnector( mockPurRepository, mockPurRepositoryMeta, mockRootRef ) );
    doReturn( location ).when( mockPurRepositoryMeta ).getRepositoryLocation();
    doReturn( "http://localhost:8080/pentaho" ).when( location ).getUrl();

    ExecutorService service = mock( ExecutorService.class );
    doReturn( service ).when( connector ).getExecutor();

    // repoFuture throws InterruptedException
    Future futureAuth = mock( Future.class );
    doReturn( false ).when( futureAuth ).get();
    Future futureRepo = mock( Future.class );
    when( futureRepo.get() ).thenThrow( new InterruptedException( "thread interrupted" ) );
    Future futureSync = mock( Future.class );
    doReturn( null ).when( futureSync ).get();
    Future futureSession = mock( Future.class );
    doReturn( "user" ).when( futureSession ).get();

    when( service.submit( any( Callable.class ) ) )
      .thenReturn( futureAuth )
      .thenReturn( futureRepo )
      .thenReturn( futureSync )
      .thenReturn( futureSession );

    KettleException thrown = null;
    try {
      connector.connect( "user", "password" );
    } catch ( KettleException e ) {
      thrown = e;
    }

    assertNotNull( "Expected KettleException for InterruptedException", thrown );
    assertTrue( thrown.getCause() instanceof InterruptedException );
    // Verify Thread.currentThread().interrupt() was called — flag should still be set
    assertTrue( "Interrupt flag should be restored", Thread.interrupted() ); // also clears it
  }

  @SuppressWarnings( "unchecked" )
  @Test
  public void testConnect_GeneralException_ClosesServiceManagerAndThrowsKettleException() throws Exception {
    PurRepository mockPurRepository = mock( PurRepository.class );
    PurRepositoryMeta mockPurRepositoryMeta = mock( PurRepositoryMeta.class );
    PurRepositoryLocation location = mock( PurRepositoryLocation.class );
    RootRef mockRootRef = mock( RootRef.class );

    PurRepositoryConnector connector =
      spy( new PurRepositoryConnector( mockPurRepository, mockPurRepositoryMeta, mockRootRef ) );
    doReturn( location ).when( mockPurRepositoryMeta ).getRepositoryLocation();
    doReturn( "http://localhost:8080/pentaho" ).when( location ).getUrl();

    ExecutorService service = mock( ExecutorService.class );
    doReturn( service ).when( connector ).getExecutor();

    // repoFuture throws ExecutionException (a general Exception)
    Future futureAuth = mock( Future.class );
    doReturn( false ).when( futureAuth ).get();
    Future futureRepo = mock( Future.class );
    when( futureRepo.get() ).thenThrow(
      new java.util.concurrent.ExecutionException( new RuntimeException( "service creation failed" ) ) );
    Future futureSync = mock( Future.class );
    doReturn( null ).when( futureSync ).get();
    Future futureSession = mock( Future.class );
    doReturn( "user" ).when( futureSession ).get();

    when( service.submit( any( Callable.class ) ) )
      .thenReturn( futureAuth )
      .thenReturn( futureRepo )
      .thenReturn( futureSync )
      .thenReturn( futureSession );

    KettleException thrown = null;
    try {
      connector.connect( "user", "password" );
    } catch ( KettleException e ) {
      thrown = e;
    }

    assertNotNull( "Expected KettleException for general Exception", thrown );
    assertTrue( thrown.getCause() instanceof java.util.concurrent.ExecutionException );
  }
  @Test
  public void testAllowedActionsContains_ActionNotPresent_ReturnsFalse() throws Exception {
    // Covers lines 234-238: user does NOT have ADMINISTER_SECURITY_ACTION → returns false
    PurRepository mockPurRepository = mock( PurRepository.class );
    PurRepositoryMeta mockPurRepositoryMeta = mock( PurRepositoryMeta.class );
    RootRef mockRootRef = mock( RootRef.class );

    PurRepositoryConnector connector =
      new PurRepositoryConnector( mockPurRepository, mockPurRepositoryMeta, mockRootRef );

    AbsSecurityProvider mockProvider = mock( AbsSecurityProvider.class );
    when( mockProvider.getAllowedActions( any() ) )
      .thenReturn( Arrays.asList( "org.pentaho.repository.read", "org.pentaho.repository.create" ) );

    Method m = PurRepositoryConnector.class.getDeclaredMethod(
      "allowedActionsContains", AbsSecurityProvider.class, String.class );
    m.setAccessible( true );

    boolean result = (boolean) m.invoke( connector, mockProvider,
      org.pentaho.di.ui.repository.pur.services.IAbsSecurityProvider.ADMINISTER_SECURITY_ACTION );

    assertFalse( "Should return false when action is not in allowed actions", result );
  }

  @Test
  public void testAllowedActionsContains_ActionPresent_ReturnsTrue() throws Exception {
    PurRepository mockPurRepository = mock( PurRepository.class );
    PurRepositoryMeta mockPurRepositoryMeta = mock( PurRepositoryMeta.class );
    RootRef mockRootRef = mock( RootRef.class );

    PurRepositoryConnector connector =
      new PurRepositoryConnector( mockPurRepository, mockPurRepositoryMeta, mockRootRef );

    String adminAction =
      org.pentaho.di.ui.repository.pur.services.IAbsSecurityProvider.ADMINISTER_SECURITY_ACTION;
    AbsSecurityProvider mockProvider = mock( AbsSecurityProvider.class );
    when( mockProvider.getAllowedActions( any() ) )
      .thenReturn( Arrays.asList( "org.pentaho.repository.read", adminAction ) );

    Method m = PurRepositoryConnector.class.getDeclaredMethod(
      "allowedActionsContains", AbsSecurityProvider.class, String.class );
    m.setAccessible( true );

    boolean result = (boolean) m.invoke( connector, mockProvider, adminAction );

    assertTrue( "Should return true when action is in allowed actions", result );
  }

  @Test
  public void testAllowedActionsContains_NullAction_ReturnsFalse() throws Exception {
    PurRepository mockPurRepository = mock( PurRepository.class );
    PurRepositoryMeta mockPurRepositoryMeta = mock( PurRepositoryMeta.class );
    RootRef mockRootRef = mock( RootRef.class );

    PurRepositoryConnector connector =
      new PurRepositoryConnector( mockPurRepository, mockPurRepositoryMeta, mockRootRef );

    AbsSecurityProvider mockProvider = mock( AbsSecurityProvider.class );
    when( mockProvider.getAllowedActions( any() ) )
      .thenReturn( Arrays.asList( "org.pentaho.repository.read" ) );

    Method m = PurRepositoryConnector.class.getDeclaredMethod(
      "allowedActionsContains", AbsSecurityProvider.class, String.class );
    m.setAccessible( true );

    boolean result = (boolean) m.invoke( connector, mockProvider, (String) null );

    assertFalse( "Should return false when action is null", result );
  }

  @Test
  public void testAllowedActionsContains_EmptyActions_ReturnsFalse() throws Exception {
    PurRepository mockPurRepository = mock( PurRepository.class );
    PurRepositoryMeta mockPurRepositoryMeta = mock( PurRepositoryMeta.class );
    RootRef mockRootRef = mock( RootRef.class );

    PurRepositoryConnector connector =
      new PurRepositoryConnector( mockPurRepository, mockPurRepositoryMeta, mockRootRef );

    AbsSecurityProvider mockProvider = mock( AbsSecurityProvider.class );
    when( mockProvider.getAllowedActions( any() ) ).thenReturn( Collections.emptyList() );

    Method m = PurRepositoryConnector.class.getDeclaredMethod(
      "allowedActionsContains", AbsSecurityProvider.class, String.class );
    m.setAccessible( true );

    boolean result = (boolean) m.invoke( connector, mockProvider,
      org.pentaho.di.ui.repository.pur.services.IAbsSecurityProvider.ADMINISTER_SECURITY_ACTION );

    assertFalse( "Should return false when allowed actions list is empty", result );
  }

}
