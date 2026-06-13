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
import org.apache.hc.client5.http.classic.methods.HttpGet;
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
import org.pentaho.platform.security.userrole.ws.IUserRoleListWebService;
import org.pentaho.platform.security.userroledao.ws.IUserRoleWebService;
import org.pentaho.platform.security.userroledao.ws.ProxyPentahoRole;
import org.pentaho.platform.security.userroledao.ws.ProxyPentahoUser;
import org.pentaho.platform.security.userroledao.ws.UserRoleSecurityInfo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.pentaho.di.repository.pur.UserRoleHelper.convertToPentahoProxyRole;
import static org.pentaho.di.repository.pur.UserRoleHelper.convertToPentahoProxyUser;

/**
 * @author Andrey Khayrutdinov
 */
public class UserRoleDelegateTest {

  private static final String ADMIN_USER = "admin";
  private static final String ADMIN_PASSWORD = "password";
  private static final String SERVER_URL = "http://localhost:8080/pentaho";
  private static final String TRUST_USER = "_trust_user_";

  @Mock
  private Log log;
  @Mock
  private IUserRoleListWebService roleListWebService;
  @Mock
  private IUserRoleWebService roleWebService;

  private UserRoleDelegate delegate;
  private AutoCloseable mocks;

  @Before
  public void setUp() throws Exception {
    mocks = MockitoAnnotations.openMocks( this );

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
    roleWebService = null;
    if ( mocks != null ) {
      try {
        mocks.close();
      } catch ( Exception ignored ) {
        // nothing to clean up for this test if Mockito close fails
      }
      mocks = null;
    }
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
  public void createRole_ProhibitsToCreate_WhenNameDiffersOnlyInCase() throws Exception {
    final String name = "role";
    final String upperCased = name.toUpperCase();

    IRole existing = new EERoleInfo( upperCased );
    when( roleWebService.getRoles() ).thenReturn( new ProxyPentahoRole[] { convertToPentahoProxyRole( existing ) } );

    delegate.createRole( new EERoleInfo( name ) );
  }

  private ServiceManager buildServiceManager( IUserRoleListWebService roleListService,
                                              IUserRoleWebService roleWebService ) throws Exception {
    ServiceManager serviceManager = mock( ServiceManager.class );
    when( serviceManager.createService( ADMIN_USER, ADMIN_PASSWORD, IUserRoleListWebService.class ) )
      .thenReturn( roleListService );
    when( serviceManager.createService( ADMIN_USER, ADMIN_PASSWORD, IUserRoleWebService.class ) )
      .thenReturn( roleWebService );
    return serviceManager;
  }

  @Test
  public void testInitManaged_JackrabbitProvider_SetsManagedTrue() throws Exception {
    String serverUrl = SERVER_URL;

    PurRepositoryMeta mockMeta = mock( PurRepositoryMeta.class );
    PurRepositoryLocation mockLocation = mock( PurRepositoryLocation.class );
    when( mockMeta.getRepositoryLocation() ).thenReturn( mockLocation );
    when( mockLocation.getUrl() ).thenReturn( serverUrl );

    IUser mockUser = mock( IUser.class );
    when( mockUser.getLogin() ).thenReturn( ADMIN_USER );
    when( mockUser.getPassword() ).thenReturn( ADMIN_PASSWORD );

    IUserRoleWebService mockRoleWebSvc = mock( IUserRoleWebService.class );
    when( mockRoleWebSvc.getUserRoleSecurityInfo() ).thenReturn( new UserRoleSecurityInfo() );
    IUserRoleListWebService mockRoleListSvc = mock( IUserRoleListWebService.class );
    ServiceManager mockServiceManager = buildServiceManager( mockRoleListSvc, mockRoleWebSvc );
    IRoleSupportSecurityManager mockRsm = mock( IRoleSupportSecurityManager.class );

    try ( MockedStatic<RestAuthHelper> mockedRest = mockStatic( RestAuthHelper.class ) ) {
      mockedRest.when( () -> RestAuthHelper.executeWithAuthFallback(
        any( HttpGet.class ), eq( serverUrl ), eq( ADMIN_USER ), eq( ADMIN_PASSWORD ), eq( TRUST_USER ), any() ) )
        .thenAnswer( invocation -> {
          HttpGet request = invocation.getArgument( 0 );
          assertEquals( serverUrl + "/api/system/authentication-provider", request.getUri().toString() );
          assertEquals( "application/json", request.getFirstHeader( "Accept" ).getValue() );
          return "{\"authenticationType\":\"jackrabbit\"}";
        } );

      UserRoleDelegate initializedDelegate =
        new UserRoleDelegate( mockRsm, mockMeta, mockUser, mock( Log.class ), mockServiceManager );

      assertTrue( initializedDelegate.isManaged() );
      verify( mockRoleWebSvc ).getUserRoleSecurityInfo();
      verify( mockRoleListSvc, never() ).getUserRoleInfo();
    }
  }

  @Test
  public void testInitManaged_NonJackrabbitProvider_SetsManagedFalse() throws Exception {
    String serverUrl = SERVER_URL;

    PurRepositoryMeta mockMeta = mock( PurRepositoryMeta.class );
    PurRepositoryLocation mockLocation = mock( PurRepositoryLocation.class );
    when( mockMeta.getRepositoryLocation() ).thenReturn( mockLocation );
    when( mockLocation.getUrl() ).thenReturn( serverUrl );

    IUser mockUser = mock( IUser.class );
    when( mockUser.getLogin() ).thenReturn( ADMIN_USER );
    when( mockUser.getPassword() ).thenReturn( ADMIN_PASSWORD );

    IUserRoleWebService mockRoleWebSvc = mock( IUserRoleWebService.class );
    when( mockRoleWebSvc.getUserRoleSecurityInfo() ).thenReturn( new UserRoleSecurityInfo() );
    IUserRoleListWebService mockRoleListSvc = mock( IUserRoleListWebService.class );
    when( mockRoleListSvc.getUserRoleInfo() ).thenReturn( mock( org.pentaho.platform.api.engine.security.userroledao.UserRoleInfo.class ) );
    ServiceManager mockServiceManager = buildServiceManager( mockRoleListSvc, mockRoleWebSvc );
    IRoleSupportSecurityManager mockRsm = mock( IRoleSupportSecurityManager.class );

    try ( MockedStatic<RestAuthHelper> mockedRest = mockStatic( RestAuthHelper.class ) ) {
      mockedRest.when( () -> RestAuthHelper.executeWithAuthFallback(
        any( HttpGet.class ), eq( serverUrl ), eq( ADMIN_USER ), eq( ADMIN_PASSWORD ), eq( TRUST_USER ), any() ) )
        .thenReturn( "{\"authenticationType\":\"ldap\"}" );

      UserRoleDelegate initializedDelegate =
        new UserRoleDelegate( mockRsm, mockMeta, mockUser, mock( Log.class ), mockServiceManager );

      assertFalse( initializedDelegate.isManaged() );
      verify( mockRoleListSvc ).getUserRoleInfo();
      verify( mockRoleWebSvc, never() ).getUserRoleSecurityInfo();
    }
  }

  @Test
  public void testInitManaged_RestAuthFailure_IsSwallowedByConstructor() throws Exception {
    String serverUrl = SERVER_URL;

    PurRepositoryMeta mockMeta = mock( PurRepositoryMeta.class );
    PurRepositoryLocation mockLocation = mock( PurRepositoryLocation.class );
    when( mockMeta.getRepositoryLocation() ).thenReturn( mockLocation );
    when( mockLocation.getUrl() ).thenReturn( serverUrl );

    IUser mockUser = mock( IUser.class );
    when( mockUser.getLogin() ).thenReturn( ADMIN_USER );
    when( mockUser.getPassword() ).thenReturn( ADMIN_PASSWORD );

    IUserRoleWebService mockRoleWebSvc = mock( IUserRoleWebService.class );
    when( mockRoleWebSvc.getUserRoleSecurityInfo() ).thenReturn( new UserRoleSecurityInfo() );
    IUserRoleListWebService mockRoleListSvc = mock( IUserRoleListWebService.class );
    ServiceManager mockServiceManager = buildServiceManager( mockRoleListSvc, mockRoleWebSvc );
    IRoleSupportSecurityManager mockRsm = mock( IRoleSupportSecurityManager.class );
    Log mockLogger = mock( Log.class );

    try ( MockedStatic<RestAuthHelper> mockedRest = mockStatic( RestAuthHelper.class ) ) {
      mockedRest.when( () -> RestAuthHelper.executeWithAuthFallback(
        any( HttpGet.class ), eq( serverUrl ), eq( ADMIN_USER ), eq( ADMIN_PASSWORD ), eq( TRUST_USER ), any() ) )
        .thenThrow( new java.io.IOException( "Connection refused" ) );

      UserRoleDelegate initializedDelegate =
        new UserRoleDelegate( mockRsm, mockMeta, mockUser, mockLogger, mockServiceManager );

      assertNotNull( initializedDelegate );
      verify( mockLogger ).error( any(), any( Exception.class ) );
    }
  }
}
