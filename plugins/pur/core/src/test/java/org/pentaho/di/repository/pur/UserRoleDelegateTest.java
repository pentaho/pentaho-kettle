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

import org.apache.commons.logging.Log;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.repository.IUser;
import org.pentaho.di.repository.UserInfo;
import org.pentaho.di.repository.pur.model.EERoleInfo;
import org.pentaho.di.repository.pur.model.IRole;
import org.pentaho.di.ui.repository.pur.services.IRoleSupportSecurityManager;
import org.pentaho.di.ui.spoon.session.AuthenticationContext;
import org.pentaho.di.ui.spoon.session.SpoonSessionManager;
import org.pentaho.platform.security.userrole.ws.IUserRoleListWebService;
import org.pentaho.platform.security.userroledao.ws.IUserRoleWebService;
import org.pentaho.platform.security.userroledao.ws.ProxyPentahoRole;
import org.pentaho.platform.security.userroledao.ws.ProxyPentahoUser;
import org.pentaho.platform.security.userroledao.ws.UserRoleSecurityInfo;

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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.ArgumentCaptor;
import static org.pentaho.di.repository.pur.UserRoleHelper.convertToPentahoProxyRole;
import static org.pentaho.di.repository.pur.UserRoleHelper.convertToPentahoProxyUser;

/**
 * @author Andrey Khayrutdinov
 */
public class UserRoleDelegateTest {

  @Mock
  private Log log;
  @Mock
  private IUserRoleListWebService roleListWebService;
  @Mock
  private IUserRoleWebService roleWebService;

  private UserRoleDelegate delegate;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks( this );

    when( roleWebService.getUserRoleSecurityInfo() ).thenReturn( new UserRoleSecurityInfo() );

    delegate = new UserRoleDelegate( log, roleListWebService, roleWebService );
    delegate.managed = true;
    delegate.updateUserRoleInfo();
  }

  @After
  public void tearDown() {
    delegate = null;
    log = null;
    roleListWebService = null;
    roleListWebService = null;
  }

  @Test( expected = KettleException.class )
  public void createUser_ProhibitsToCreate_WhenNameCollides() throws Exception {
    final String name = "user";

    IUser existing = new UserInfo( name );
    when( roleWebService.getUsers() ).thenReturn( new ProxyPentahoUser[] { convertToPentahoProxyUser( existing ) } );

    delegate.createUser( new UserInfo( name ) );
  }

  @Test
  public void createUser_CreatesSuccessfully_WhenNameIsUnique() throws Exception {
    final String name = "user";
    delegate.createUser( new UserInfo( name ) );
    verify( roleWebService ).createUser( any( ProxyPentahoUser.class ) );
  }

  @Test
  public void createUser_CreatesSuccessfully_WhenNameDiffersInCase() throws Exception {
    final String name = "user";
    final String upperCased = name.toUpperCase();

    IUser existing = new UserInfo( upperCased );
    when( roleWebService.getUsers() ).thenReturn( new ProxyPentahoUser[] { convertToPentahoProxyUser( existing ) } );

    delegate.createUser( new UserInfo( name ) );
    verify( roleWebService ).createUser( any( ProxyPentahoUser.class ) );
  }

  @Test( expected = KettleException.class )
  public void createRole_ProhibitsToCreate_WhenNameCollides() throws Exception {
    final String name = "role";

    IRole existing = new EERoleInfo( name );
    when( roleWebService.getRoles() ).thenReturn( new ProxyPentahoRole[] { convertToPentahoProxyRole( existing ) } );

    delegate.createRole( new EERoleInfo( name ) );
  }

  @Test
  public void createRole_CreatesSuccessfully_WhenNameIsUnique() throws Exception {
    final String name = "role";
    delegate.createRole( new EERoleInfo( name ) );
    verify( roleWebService ).createRole( any( ProxyPentahoRole.class ) );
  }

  @Test( expected = KettleException.class )
  public void createRole_CreatesSuccessfully_WhenNameDiffersInCase() throws Exception {
    final String name = "role";
    final String upperCased = name.toUpperCase();

    IRole existing = new EERoleInfo( upperCased );
    when( roleWebService.getRoles() ).thenReturn( new ProxyPentahoRole[] { convertToPentahoProxyRole( existing ) } );

    delegate.createRole( new EERoleInfo( name ) );
    verify( roleWebService ).createRole( any( ProxyPentahoRole.class ) );
  }

  private Client buildMockClient( String authProviderJson ) {
    Client mockClient = mock( Client.class );
    WebTarget mockWebTarget = mock( WebTarget.class );
    Invocation.Builder mockBuilder = mock( Invocation.Builder.class );

    when( mockClient.target( anyString() ) ).thenReturn( mockWebTarget );
    when( mockWebTarget.request( any( MediaType.class ) ) ).thenReturn( mockBuilder );
    when( mockBuilder.get( String.class ) ).thenReturn( authProviderJson );
    when( mockBuilder.header( anyString(), any() ) ).thenReturn( mockBuilder );

    return mockClient;
  }

  @Test
  public void testInitManaged_SessionAuth_ValidSession_RegistersCookieFilter() throws Exception {
    String serverUrl = "http://localhost:8080/pentaho";
    String jsessionId = "VALID_SESSION_123";

    SpoonSessionManager mockSessionMgr = mock( SpoonSessionManager.class );
    AuthenticationContext mockAuthCtx = mock( AuthenticationContext.class );
    when( mockSessionMgr.getAuthenticationContext( serverUrl ) ).thenReturn( mockAuthCtx );
    when( mockAuthCtx.isAuthenticated() ).thenReturn( true );
    when( mockAuthCtx.validateAndClearIfExpired() ).thenReturn( true );
    when( mockAuthCtx.getJSessionId() ).thenReturn( jsessionId );

    Client mockClient = buildMockClient( "{\"authenticationType\":\"jackrabbit\"}" );

    PurRepositoryMeta mockMeta = mock( PurRepositoryMeta.class );
    PurRepositoryLocation mockLocation = mock( PurRepositoryLocation.class );
    when( mockMeta.getRepositoryLocation() ).thenReturn( mockLocation );
    when( mockLocation.getUrl() ).thenReturn( serverUrl );

    IUser mockUser = mock( IUser.class );
    when( mockUser.getLogin() ).thenReturn( "admin" );
    when( mockUser.getPassword() ).thenReturn( "password" );

    IUserRoleWebService mockRoleWebSvc = mock( IUserRoleWebService.class );
    when( mockRoleWebSvc.getUserRoleSecurityInfo() ).thenReturn( new UserRoleSecurityInfo() );
    IUserRoleListWebService mockRoleListSvc = mock( IUserRoleListWebService.class );
    ServiceManager mockServiceManager = mock( ServiceManager.class );
    when( mockServiceManager.createService( anyString(), anyString(), eq( IUserRoleListWebService.class ) ) )
      .thenReturn( mockRoleListSvc );
    when( mockServiceManager.createService( anyString(), anyString(), eq( IUserRoleWebService.class ) ) )
      .thenReturn( mockRoleWebSvc );
    IRoleSupportSecurityManager mockRsm = mock( IRoleSupportSecurityManager.class );

    try ( MockedStatic<SpoonSessionManager> mockedSM = mockStatic( SpoonSessionManager.class );
          MockedStatic<ClientBuilder> mockedCB = mockStatic( ClientBuilder.class ) ) {

      mockedSM.when( SpoonSessionManager::getInstance ).thenReturn( mockSessionMgr );
      mockedCB.when( ClientBuilder::newClient ).thenReturn( mockClient );

      new UserRoleDelegate( mockRsm, mockMeta, mockUser, mock( Log.class ), mockServiceManager );

      // A ClientRequestFilter (cookie filter) must be registered, NOT HttpAuthenticationFeature
      verify( mockClient, times( 1 ) ).register( any( ClientRequestFilter.class ) );
      verify( mockClient, never() ).register( any( HttpAuthenticationFeature.class ) );
    }
  }

  @Test
  public void testInitManaged_BasicAuth_NoSession_RegistersHttpAuthFeature() throws Exception {
    String serverUrl = "http://localhost:8080/pentaho";

    SpoonSessionManager mockSessionMgr = mock( SpoonSessionManager.class );
    AuthenticationContext mockAuthCtx = mock( AuthenticationContext.class );
    when( mockSessionMgr.getAuthenticationContext( serverUrl ) ).thenReturn( mockAuthCtx );
    when( mockAuthCtx.isAuthenticated() ).thenReturn( false ); // not authenticated

    Client mockClient = buildMockClient( "{\"authenticationType\":\"jackrabbit\"}" );

    PurRepositoryMeta mockMeta = mock( PurRepositoryMeta.class );
    PurRepositoryLocation mockLocation = mock( PurRepositoryLocation.class );
    when( mockMeta.getRepositoryLocation() ).thenReturn( mockLocation );
    when( mockLocation.getUrl() ).thenReturn( serverUrl );

    IUser mockUser = mock( IUser.class );
    when( mockUser.getLogin() ).thenReturn( "admin" );
    when( mockUser.getPassword() ).thenReturn( "password" );

    IUserRoleWebService mockRoleWebSvc = mock( IUserRoleWebService.class );
    when( mockRoleWebSvc.getUserRoleSecurityInfo() ).thenReturn( new UserRoleSecurityInfo() );
    IUserRoleListWebService mockRoleListSvc = mock( IUserRoleListWebService.class );
    ServiceManager mockServiceManager = mock( ServiceManager.class );
    when( mockServiceManager.createService( anyString(), anyString(), eq( IUserRoleListWebService.class ) ) )
      .thenReturn( mockRoleListSvc );
    when( mockServiceManager.createService( anyString(), anyString(), eq( IUserRoleWebService.class ) ) )
      .thenReturn( mockRoleWebSvc );
    IRoleSupportSecurityManager mockRsm = mock( IRoleSupportSecurityManager.class );

    try ( MockedStatic<SpoonSessionManager> mockedSM = mockStatic( SpoonSessionManager.class );
          MockedStatic<ClientBuilder> mockedCB = mockStatic( ClientBuilder.class ) ) {

      mockedSM.when( SpoonSessionManager::getInstance ).thenReturn( mockSessionMgr );
      mockedCB.when( ClientBuilder::newClient ).thenReturn( mockClient );

      new UserRoleDelegate( mockRsm, mockMeta, mockUser, mock( Log.class ), mockServiceManager );

      // HttpAuthenticationFeature must be registered, NOT a ClientRequestFilter
      verify( mockClient, times( 1 ) ).register( any( HttpAuthenticationFeature.class ) );
      verify( mockClient, never() ).register( any( ClientRequestFilter.class ) );
    }
  }

  @Test
  public void testInitManaged_BasicAuth_NullAuthContext_RegistersHttpAuthFeature() throws Exception {
    String serverUrl = "http://localhost:8080/pentaho";

    SpoonSessionManager mockSessionMgr = mock( SpoonSessionManager.class );
    // AuthenticationContext is null
    when( mockSessionMgr.getAuthenticationContext( serverUrl ) ).thenReturn( null );

    Client mockClient = buildMockClient( "{\"authenticationType\":\"jackrabbit\"}" );

    PurRepositoryMeta mockMeta = mock( PurRepositoryMeta.class );
    PurRepositoryLocation mockLocation = mock( PurRepositoryLocation.class );
    when( mockMeta.getRepositoryLocation() ).thenReturn( mockLocation );
    when( mockLocation.getUrl() ).thenReturn( serverUrl );

    IUser mockUser = mock( IUser.class );
    when( mockUser.getLogin() ).thenReturn( "admin" );
    when( mockUser.getPassword() ).thenReturn( "password" );

    IUserRoleWebService mockRoleWebSvc = mock( IUserRoleWebService.class );
    when( mockRoleWebSvc.getUserRoleSecurityInfo() ).thenReturn( new UserRoleSecurityInfo() );
    IUserRoleListWebService mockRoleListSvc = mock( IUserRoleListWebService.class );
    ServiceManager mockServiceManager = mock( ServiceManager.class );
    when( mockServiceManager.createService( anyString(), anyString(), eq( IUserRoleListWebService.class ) ) )
      .thenReturn( mockRoleListSvc );
    when( mockServiceManager.createService( anyString(), anyString(), eq( IUserRoleWebService.class ) ) )
      .thenReturn( mockRoleWebSvc );
    IRoleSupportSecurityManager mockRsm = mock( IRoleSupportSecurityManager.class );

    try ( MockedStatic<SpoonSessionManager> mockedSM = mockStatic( SpoonSessionManager.class );
          MockedStatic<ClientBuilder> mockedCB = mockStatic( ClientBuilder.class ) ) {

      mockedSM.when( SpoonSessionManager::getInstance ).thenReturn( mockSessionMgr );
      mockedCB.when( ClientBuilder::newClient ).thenReturn( mockClient );

      new UserRoleDelegate( mockRsm, mockMeta, mockUser, mock( Log.class ), mockServiceManager );

      verify( mockClient, times( 1 ) ).register( any( HttpAuthenticationFeature.class ) );
      verify( mockClient, never() ).register( any( ClientRequestFilter.class ) );
    }
  }

  @Test
  public void testInitManaged_BasicAuth_SessionExpired_ValidateReturnsFalse_RegistersHttpAuthFeature()
      throws Exception {
    String serverUrl = "http://localhost:8080/pentaho";

    SpoonSessionManager mockSessionMgr = mock( SpoonSessionManager.class );
    AuthenticationContext mockAuthCtx = mock( AuthenticationContext.class );
    when( mockSessionMgr.getAuthenticationContext( serverUrl ) ).thenReturn( mockAuthCtx );
    when( mockAuthCtx.isAuthenticated() ).thenReturn( true );
    when( mockAuthCtx.validateAndClearIfExpired() ).thenReturn( false ); // expired

    Client mockClient = buildMockClient( "{\"authenticationType\":\"jackrabbit\"}" );

    PurRepositoryMeta mockMeta = mock( PurRepositoryMeta.class );
    PurRepositoryLocation mockLocation = mock( PurRepositoryLocation.class );
    when( mockMeta.getRepositoryLocation() ).thenReturn( mockLocation );
    when( mockLocation.getUrl() ).thenReturn( serverUrl );

    IUser mockUser = mock( IUser.class );
    when( mockUser.getLogin() ).thenReturn( "admin" );
    when( mockUser.getPassword() ).thenReturn( "password" );

    IUserRoleWebService mockRoleWebSvc = mock( IUserRoleWebService.class );
    when( mockRoleWebSvc.getUserRoleSecurityInfo() ).thenReturn( new UserRoleSecurityInfo() );
    IUserRoleListWebService mockRoleListSvc = mock( IUserRoleListWebService.class );
    ServiceManager mockServiceManager = mock( ServiceManager.class );
    when( mockServiceManager.createService( anyString(), anyString(), eq( IUserRoleListWebService.class ) ) )
      .thenReturn( mockRoleListSvc );
    when( mockServiceManager.createService( anyString(), anyString(), eq( IUserRoleWebService.class ) ) )
      .thenReturn( mockRoleWebSvc );
    IRoleSupportSecurityManager mockRsm = mock( IRoleSupportSecurityManager.class );

    try ( MockedStatic<SpoonSessionManager> mockedSM = mockStatic( SpoonSessionManager.class );
          MockedStatic<ClientBuilder> mockedCB = mockStatic( ClientBuilder.class ) ) {

      mockedSM.when( SpoonSessionManager::getInstance ).thenReturn( mockSessionMgr );
      mockedCB.when( ClientBuilder::newClient ).thenReturn( mockClient );

      new UserRoleDelegate( mockRsm, mockMeta, mockUser, mock( Log.class ), mockServiceManager );

      verify( mockClient, times( 1 ) ).register( any( HttpAuthenticationFeature.class ) );
      verify( mockClient, never() ).register( any( ClientRequestFilter.class ) );
    }
  }

  @Test
  public void testInitManaged_BasicAuth_SessionAuth_BlankJSessionId_RegistersHttpAuthFeature()
      throws Exception {
    String serverUrl = "http://localhost:8080/pentaho";

    SpoonSessionManager mockSessionMgr = mock( SpoonSessionManager.class );
    AuthenticationContext mockAuthCtx = mock( AuthenticationContext.class );
    when( mockSessionMgr.getAuthenticationContext( serverUrl ) ).thenReturn( mockAuthCtx );
    when( mockAuthCtx.isAuthenticated() ).thenReturn( true );
    when( mockAuthCtx.validateAndClearIfExpired() ).thenReturn( true );
    when( mockAuthCtx.getJSessionId() ).thenReturn( "   " ); // blank

    Client mockClient = buildMockClient( "{\"authenticationType\":\"jackrabbit\"}" );

    PurRepositoryMeta mockMeta = mock( PurRepositoryMeta.class );
    PurRepositoryLocation mockLocation = mock( PurRepositoryLocation.class );
    when( mockMeta.getRepositoryLocation() ).thenReturn( mockLocation );
    when( mockLocation.getUrl() ).thenReturn( serverUrl );

    IUser mockUser = mock( IUser.class );
    when( mockUser.getLogin() ).thenReturn( "admin" );
    when( mockUser.getPassword() ).thenReturn( "password" );

    IUserRoleWebService mockRoleWebSvc = mock( IUserRoleWebService.class );
    when( mockRoleWebSvc.getUserRoleSecurityInfo() ).thenReturn( new UserRoleSecurityInfo() );
    IUserRoleListWebService mockRoleListSvc = mock( IUserRoleListWebService.class );
    ServiceManager mockServiceManager = mock( ServiceManager.class );
    when( mockServiceManager.createService( anyString(), anyString(), eq( IUserRoleListWebService.class ) ) )
      .thenReturn( mockRoleListSvc );
    when( mockServiceManager.createService( anyString(), anyString(), eq( IUserRoleWebService.class ) ) )
      .thenReturn( mockRoleWebSvc );
    IRoleSupportSecurityManager mockRsm = mock( IRoleSupportSecurityManager.class );

    try ( MockedStatic<SpoonSessionManager> mockedSM = mockStatic( SpoonSessionManager.class );
          MockedStatic<ClientBuilder> mockedCB = mockStatic( ClientBuilder.class ) ) {

      mockedSM.when( SpoonSessionManager::getInstance ).thenReturn( mockSessionMgr );
      mockedCB.when( ClientBuilder::newClient ).thenReturn( mockClient );

      new UserRoleDelegate( mockRsm, mockMeta, mockUser, mock( Log.class ), mockServiceManager );

      verify( mockClient, times( 1 ) ).register( any( HttpAuthenticationFeature.class ) );
      verify( mockClient, never() ).register( any( ClientRequestFilter.class ) );
    }
  }

  @Test
  public void testInitManaged_BasicAuth_SpoonSessionManagerThrows_FallsBackToBasicAuth()
      throws Exception {
    String serverUrl = "http://localhost:8080/pentaho";

    SpoonSessionManager mockSessionMgr = mock( SpoonSessionManager.class );
    when( mockSessionMgr.getAuthenticationContext( serverUrl ) )
      .thenThrow( new RuntimeException( "headless mode" ) );

    Client mockClient = buildMockClient( "{\"authenticationType\":\"jackrabbit\"}" );

    PurRepositoryMeta mockMeta = mock( PurRepositoryMeta.class );
    PurRepositoryLocation mockLocation = mock( PurRepositoryLocation.class );
    when( mockMeta.getRepositoryLocation() ).thenReturn( mockLocation );
    when( mockLocation.getUrl() ).thenReturn( serverUrl );

    IUser mockUser = mock( IUser.class );
    when( mockUser.getLogin() ).thenReturn( "admin" );
    when( mockUser.getPassword() ).thenReturn( "password" );

    IUserRoleWebService mockRoleWebSvc = mock( IUserRoleWebService.class );
    when( mockRoleWebSvc.getUserRoleSecurityInfo() ).thenReturn( new UserRoleSecurityInfo() );
    IUserRoleListWebService mockRoleListSvc = mock( IUserRoleListWebService.class );
    ServiceManager mockServiceManager = mock( ServiceManager.class );
    when( mockServiceManager.createService( anyString(), anyString(), eq( IUserRoleListWebService.class ) ) )
      .thenReturn( mockRoleListSvc );
    when( mockServiceManager.createService( anyString(), anyString(), eq( IUserRoleWebService.class ) ) )
      .thenReturn( mockRoleWebSvc );
    IRoleSupportSecurityManager mockRsm = mock( IRoleSupportSecurityManager.class );

    try ( MockedStatic<SpoonSessionManager> mockedSM = mockStatic( SpoonSessionManager.class );
          MockedStatic<ClientBuilder> mockedCB = mockStatic( ClientBuilder.class ) ) {

      mockedSM.when( SpoonSessionManager::getInstance ).thenReturn( mockSessionMgr );
      mockedCB.when( ClientBuilder::newClient ).thenReturn( mockClient );

      // Exception swallowed — constructor should complete normally
      new UserRoleDelegate( mockRsm, mockMeta, mockUser, mock( Log.class ), mockServiceManager );

      verify( mockClient, times( 1 ) ).register( any( HttpAuthenticationFeature.class ) );
      verify( mockClient, never() ).register( any( ClientRequestFilter.class ) );
    }
  }

  @Test
  public void testInitManaged_SessionAuth_FilterAddsJsessionIdCookieHeader() throws Exception {
    String serverUrl = "http://localhost:8080/pentaho";
    String jsessionId = "TEST_SESSION_ABC";

    SpoonSessionManager mockSessionMgr = mock( SpoonSessionManager.class );
    AuthenticationContext mockAuthCtx = mock( AuthenticationContext.class );
    when( mockSessionMgr.getAuthenticationContext( serverUrl ) ).thenReturn( mockAuthCtx );
    when( mockAuthCtx.isAuthenticated() ).thenReturn( true );
    when( mockAuthCtx.validateAndClearIfExpired() ).thenReturn( true );
    when( mockAuthCtx.getJSessionId() ).thenReturn( jsessionId );

    Client mockClient = buildMockClient( "{\"authenticationType\":\"jackrabbit\"}" );

    PurRepositoryMeta mockMeta = mock( PurRepositoryMeta.class );
    PurRepositoryLocation mockLocation = mock( PurRepositoryLocation.class );
    when( mockMeta.getRepositoryLocation() ).thenReturn( mockLocation );
    when( mockLocation.getUrl() ).thenReturn( serverUrl );

    IUser mockUser = mock( IUser.class );
    when( mockUser.getLogin() ).thenReturn( "admin" );
    when( mockUser.getPassword() ).thenReturn( "password" );

    IUserRoleWebService mockRoleWebSvc = mock( IUserRoleWebService.class );
    when( mockRoleWebSvc.getUserRoleSecurityInfo() ).thenReturn( new UserRoleSecurityInfo() );
    IUserRoleListWebService mockRoleListSvc = mock( IUserRoleListWebService.class );
    ServiceManager mockServiceManager = mock( ServiceManager.class );
    when( mockServiceManager.createService( anyString(), anyString(), eq( IUserRoleListWebService.class ) ) )
      .thenReturn( mockRoleListSvc );
    when( mockServiceManager.createService( anyString(), anyString(), eq( IUserRoleWebService.class ) ) )
      .thenReturn( mockRoleWebSvc );
    IRoleSupportSecurityManager mockRsm = mock( IRoleSupportSecurityManager.class );

    try ( MockedStatic<SpoonSessionManager> mockedSM = mockStatic( SpoonSessionManager.class );
          MockedStatic<ClientBuilder> mockedCB = mockStatic( ClientBuilder.class ) ) {

      mockedSM.when( SpoonSessionManager::getInstance ).thenReturn( mockSessionMgr );
      mockedCB.when( ClientBuilder::newClient ).thenReturn( mockClient );

      new UserRoleDelegate( mockRsm, mockMeta, mockUser, mock( Log.class ), mockServiceManager );

      // Capture the registered ClientRequestFilter
      ArgumentCaptor<ClientRequestFilter> filterCaptor = ArgumentCaptor.forClass( ClientRequestFilter.class );
      verify( mockClient ).register( filterCaptor.capture() );
      ClientRequestFilter capturedFilter = filterCaptor.getValue();

      // Invoke the filter to execute the lambda body (lines 121-123)
      ClientRequestContext requestContext = mock( ClientRequestContext.class );
      MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
      when( requestContext.getHeaders() ).thenReturn( headers );

      capturedFilter.filter( requestContext );

      // Verify the Cookie header was added with the correct JSESSIONID
      assertTrue( headers.containsKey( "Cookie" ) );
      assertEquals( "JSESSIONID=TEST_SESSION_ABC", headers.getFirst( "Cookie" ) );
    }
  }

  @Test
  public void testInitManaged_SessionAuth_NullJSessionId_FallsBackToBasicAuth() throws Exception {
    String serverUrl = "http://localhost:8080/pentaho";

    SpoonSessionManager mockSessionMgr = mock( SpoonSessionManager.class );
    AuthenticationContext mockAuthCtx = mock( AuthenticationContext.class );
    when( mockSessionMgr.getAuthenticationContext( serverUrl ) ).thenReturn( mockAuthCtx );
    when( mockAuthCtx.isAuthenticated() ).thenReturn( true );
    when( mockAuthCtx.validateAndClearIfExpired() ).thenReturn( true );
    when( mockAuthCtx.getJSessionId() ).thenReturn( null ); // null sessionId

    Client mockClient = buildMockClient( "{\"authenticationType\":\"jackrabbit\"}" );

    PurRepositoryMeta mockMeta = mock( PurRepositoryMeta.class );
    PurRepositoryLocation mockLocation = mock( PurRepositoryLocation.class );
    when( mockMeta.getRepositoryLocation() ).thenReturn( mockLocation );
    when( mockLocation.getUrl() ).thenReturn( serverUrl );

    IUser mockUser = mock( IUser.class );
    when( mockUser.getLogin() ).thenReturn( "admin" );
    when( mockUser.getPassword() ).thenReturn( "password" );

    IUserRoleWebService mockRoleWebSvc = mock( IUserRoleWebService.class );
    when( mockRoleWebSvc.getUserRoleSecurityInfo() ).thenReturn( new UserRoleSecurityInfo() );
    IUserRoleListWebService mockRoleListSvc = mock( IUserRoleListWebService.class );
    ServiceManager mockServiceManager = mock( ServiceManager.class );
    when( mockServiceManager.createService( anyString(), anyString(), eq( IUserRoleListWebService.class ) ) )
      .thenReturn( mockRoleListSvc );
    when( mockServiceManager.createService( anyString(), anyString(), eq( IUserRoleWebService.class ) ) )
      .thenReturn( mockRoleWebSvc );
    IRoleSupportSecurityManager mockRsm = mock( IRoleSupportSecurityManager.class );

    try ( MockedStatic<SpoonSessionManager> mockedSM = mockStatic( SpoonSessionManager.class );
          MockedStatic<ClientBuilder> mockedCB = mockStatic( ClientBuilder.class ) ) {

      mockedSM.when( SpoonSessionManager::getInstance ).thenReturn( mockSessionMgr );
      mockedCB.when( ClientBuilder::newClient ).thenReturn( mockClient );

      new UserRoleDelegate( mockRsm, mockMeta, mockUser, mock( Log.class ), mockServiceManager );

      // sessionId is null so useSessionAuth is false — falls back to basic auth
      verify( mockClient, times( 1 ) ).register( any( HttpAuthenticationFeature.class ) );
      verify( mockClient, never() ).register( any( ClientRequestFilter.class ) );
    }
  }
}
