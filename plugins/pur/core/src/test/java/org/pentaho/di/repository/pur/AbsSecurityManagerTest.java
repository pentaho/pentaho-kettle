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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.xml.ws.WebServiceException;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.repository.IUser;
import org.pentaho.platform.security.policy.rolebased.RoleBindingStruct;
import org.pentaho.platform.security.policy.rolebased.ws.IRoleAuthorizationPolicyRoleBindingDaoWebService;
import org.springframework.test.util.ReflectionTestUtils;

public class AbsSecurityManagerTest {

  private PurRepository repository;
  private PurRepositoryMeta repositoryMeta;
  private IUser userInfo;
  private ServiceManager serviceManager;
  private IRoleAuthorizationPolicyRoleBindingDaoWebService authService;
  private IRoleAuthorizationPolicyRoleBindingDaoWebService freshAuthService;

  @Before
  public void setUp() {
    repository = mock( PurRepository.class );
    repositoryMeta = mock( PurRepositoryMeta.class );
    userInfo = mock( IUser.class );
    when( userInfo.getLogin() ).thenReturn( "admin" );
    when( userInfo.getPassword() ).thenReturn( "password" );

    serviceManager = mock( ServiceManager.class );

    authService = mock( IRoleAuthorizationPolicyRoleBindingDaoWebService.class );
    freshAuthService = mock( IRoleAuthorizationPolicyRoleBindingDaoWebService.class );
  }

  @Test
  public void testInitialize_Success() throws Exception {
    RoleBindingStruct struct = mock( RoleBindingStruct.class );
    when( serviceManager.createService( anyString(), anyString(),
        eq( IRoleAuthorizationPolicyRoleBindingDaoWebService.class ) ) )
        .thenReturn( authService );
    when( authService.getRoleBindingStruct( anyString() ) ).thenReturn( struct );

    AbsSecurityManager manager = new AbsSecurityManager( repository, repositoryMeta, userInfo, serviceManager );
    manager.initialize( "en" );

    verify( authService ).getRoleBindingStruct( "en" );
  }

  @Test
  public void testInitialize_RetriesWithFreshStubOnStaleStubFailure() throws Exception {
    // First call to createService returns the stale stub
    // Second call returns a fresh one
    when( serviceManager.createService( anyString(), anyString(),
        eq( IRoleAuthorizationPolicyRoleBindingDaoWebService.class ) ) )
        .thenReturn( authService )
        .thenReturn( freshAuthService );

    // The stale stub throws "close method has already been invoked"
    when( authService.getRoleBindingStruct( anyString() ) )
        .thenThrow( new WebServiceException( "close method has already been invoked" ) );

    // The fresh stub succeeds
    RoleBindingStruct freshStruct = mock( RoleBindingStruct.class );
    when( freshAuthService.getRoleBindingStruct( anyString() ) ).thenReturn( freshStruct );

    // Set up repository to return fresh user info after reconnection
    IUser freshUser = mock( IUser.class );
    when( freshUser.getLogin() ).thenReturn( "admin" );
    when( freshUser.getPassword() ).thenReturn( "newPassword" );
    when( repository.getUserInfo() ).thenReturn( freshUser );

    AbsSecurityManager manager = new AbsSecurityManager( repository, repositoryMeta, userInfo, serviceManager );
    manager.initialize( "en" );

    // Verify the service was created twice (once in constructor, once in retry)
    verify( serviceManager, times( 2 ) ).createService( anyString(), anyString(),
        eq( IRoleAuthorizationPolicyRoleBindingDaoWebService.class ) );

    // Verify the fresh stub was used
    verify( freshAuthService ).getRoleBindingStruct( "en" );
  }

  @Test( expected = KettleException.class )
  public void testInitialize_ThrowsIfRetryAlsoFails() throws Exception {
    // Both stubs throw exceptions
    when( serviceManager.createService( anyString(), anyString(),
        eq( IRoleAuthorizationPolicyRoleBindingDaoWebService.class ) ) )
        .thenReturn( authService )
        .thenReturn( freshAuthService );

    when( authService.getRoleBindingStruct( anyString() ) )
        .thenThrow( new WebServiceException( "close method has already been invoked" ) );
    when( freshAuthService.getRoleBindingStruct( anyString() ) )
        .thenThrow( new WebServiceException( "still failing" ) );

    IUser freshUser = mock( IUser.class );
    when( freshUser.getLogin() ).thenReturn( "admin" );
    when( freshUser.getPassword() ).thenReturn( "password" );
    when( repository.getUserInfo() ).thenReturn( freshUser );

    AbsSecurityManager manager = new AbsSecurityManager( repository, repositoryMeta, userInfo, serviceManager );
    manager.initialize( "en" );
  }

  @Test( expected = KettleException.class )
  public void testInitialize_ThrowsIfServiceIsNull() throws Exception {
    when( serviceManager.createService( anyString(), anyString(),
        eq( IRoleAuthorizationPolicyRoleBindingDaoWebService.class ) ) )
        .thenReturn( null );

    AbsSecurityManager manager = new AbsSecurityManager( repository, repositoryMeta, userInfo, serviceManager );
    manager.initialize( "en" );
  }

  @Test
  public void testInitialize_UsesRepositoryUserInfoForRetry() throws Exception {
    when( serviceManager.createService( anyString(), anyString(),
        eq( IRoleAuthorizationPolicyRoleBindingDaoWebService.class ) ) )
        .thenReturn( authService )
        .thenReturn( freshAuthService );

    when( authService.getRoleBindingStruct( anyString() ) )
        .thenThrow( new WebServiceException( "close method has already been invoked" ) );

    RoleBindingStruct freshStruct = mock( RoleBindingStruct.class );
    when( freshAuthService.getRoleBindingStruct( anyString() ) ).thenReturn( freshStruct );

    // Repository returns different user info after reconnection
    IUser reconnectedUser = mock( IUser.class );
    when( reconnectedUser.getLogin() ).thenReturn( "reconnectedAdmin" );
    when( reconnectedUser.getPassword() ).thenReturn( "reconnectedPassword" );
    when( repository.getUserInfo() ).thenReturn( reconnectedUser );

    AbsSecurityManager manager = new AbsSecurityManager( repository, repositoryMeta, userInfo, serviceManager );
    manager.initialize( "en" );

    // Verify that the retry used the reconnected user's credentials
    verify( serviceManager ).createService( "reconnectedAdmin", "reconnectedPassword",
        IRoleAuthorizationPolicyRoleBindingDaoWebService.class );
  }

  @Test( expected = KettleException.class )
  public void testInitialize_ThrowsWhenCurrentUserIsNullDuringRetry() throws Exception {
    // Constructor creates service successfully
    when( serviceManager.createService( anyString(), anyString(),
        eq( IRoleAuthorizationPolicyRoleBindingDaoWebService.class ) ) )
        .thenReturn( authService );

    // The stub throws on first call, triggering retry
    when( authService.getRoleBindingStruct( anyString() ) )
        .thenThrow( new WebServiceException( "close method has already been invoked" ) );

    // Repository returns null user info — the if-condition at line 104 evaluates to false
    when( repository.getUserInfo() ).thenReturn( null );

    AbsSecurityManager manager = new AbsSecurityManager( repository, repositoryMeta, userInfo, serviceManager );
    manager.initialize( "en" );
  }

  @Test
  public void testInitialize_ThrowsWithOriginalExceptionWhenCurrentUserIsNull() throws Exception {
    when( serviceManager.createService( anyString(), anyString(),
        eq( IRoleAuthorizationPolicyRoleBindingDaoWebService.class ) ) )
        .thenReturn( authService );

    WebServiceException originalCause = new WebServiceException( "close method has already been invoked" );
    when( authService.getRoleBindingStruct( anyString() ) )
        .thenThrow( originalCause );

    // currentUser is null — retry cannot proceed, should wrap the original exception
    when( repository.getUserInfo() ).thenReturn( null );

    AbsSecurityManager manager = new AbsSecurityManager( repository, repositoryMeta, userInfo, serviceManager );
    try {
      manager.initialize( "en" );
      fail( "Expected KettleException" );
    } catch ( KettleException e ) {
      // Verify original exception is preserved as the cause
      assertNotNull( e.getCause() );
      assertTrue( e.getCause() instanceof WebServiceException );
      // Service should NOT have been recreated (createService called only once in constructor)
      verify( serviceManager, times( 1 ) ).createService( anyString(), anyString(),
          eq( IRoleAuthorizationPolicyRoleBindingDaoWebService.class ) );
    }
  }

  @Test( expected = KettleException.class )
  public void testInitialize_ThrowsWhenRecreatedServiceIsNullDuringRetry() throws Exception {
    // First call in constructor returns a valid service; second call in retry returns null
    when( serviceManager.createService( anyString(), anyString(),
        eq( IRoleAuthorizationPolicyRoleBindingDaoWebService.class ) ) )
        .thenReturn( authService )
        .thenReturn( null );

    // The stub throws on first call, triggering retry
    when( authService.getRoleBindingStruct( anyString() ) )
        .thenThrow( new WebServiceException( "close method has already been invoked" ) );

    // Repository returns a valid user, but the recreated service will be null
    IUser freshUser = mock( IUser.class );
    when( freshUser.getLogin() ).thenReturn( "admin" );
    when( freshUser.getPassword() ).thenReturn( "password" );
    when( repository.getUserInfo() ).thenReturn( freshUser );

    AbsSecurityManager manager = new AbsSecurityManager( repository, repositoryMeta, userInfo, serviceManager );
    manager.initialize( "en" );
  }

  @Test
  public void testInitialize_ThrowsWhenRecreatedServiceIsNull_VerifiesOriginalException() throws Exception {
    when( serviceManager.createService( anyString(), anyString(),
        eq( IRoleAuthorizationPolicyRoleBindingDaoWebService.class ) ) )
        .thenReturn( authService )
        .thenReturn( null );

    WebServiceException originalCause = new WebServiceException( "close method has already been invoked" );
    when( authService.getRoleBindingStruct( anyString() ) )
        .thenThrow( originalCause );

    IUser freshUser = mock( IUser.class );
    when( freshUser.getLogin() ).thenReturn( "admin" );
    when( freshUser.getPassword() ).thenReturn( "password" );
    when( repository.getUserInfo() ).thenReturn( freshUser );

    AbsSecurityManager manager = new AbsSecurityManager( repository, repositoryMeta, userInfo, serviceManager );
    try {
      manager.initialize( "en" );
      fail( "Expected KettleException" );
    } catch ( KettleException e ) {
      // Verify original exception is preserved as the cause
      assertNotNull( e.getCause() );
      assertTrue( e.getCause() instanceof WebServiceException );
      // Verify service recreation was attempted (createService called twice)
      verify( serviceManager, times( 2 ) ).createService( anyString(), anyString(),
          eq( IRoleAuthorizationPolicyRoleBindingDaoWebService.class ) );
      // Verify no retry call was made on freshAuthService since the recreated service was null
      verify( freshAuthService, never() ).getRoleBindingStruct( anyString() );
    }
  }

  @Test( expected = KettleException.class )
  public void testInitialize_ThrowsWhenServiceManagerIsNullDuringRetry() throws Exception {
    // Constructor creates service successfully with a valid serviceManager
    when( serviceManager.createService( anyString(), anyString(),
        eq( IRoleAuthorizationPolicyRoleBindingDaoWebService.class ) ) )
        .thenReturn( authService );

    // The stub throws on first call, triggering retry
    when( authService.getRoleBindingStruct( anyString() ) )
        .thenThrow( new WebServiceException( "close method has already been invoked" ) );

    // Repository returns a valid user
    IUser freshUser = mock( IUser.class );
    when( freshUser.getLogin() ).thenReturn( "admin" );
    when( freshUser.getPassword() ).thenReturn( "password" );
    when( repository.getUserInfo() ).thenReturn( freshUser );

    AbsSecurityManager manager = new AbsSecurityManager( repository, repositoryMeta, userInfo, serviceManager );

    // Simulate serviceManager being null (e.g. after deserialization of the transient field)
    ReflectionTestUtils.setField( manager, "serviceManager", null );

    // Now initialize — the retry path will see serviceManager == null at line 104
    manager.initialize( "en" );
  }
}
