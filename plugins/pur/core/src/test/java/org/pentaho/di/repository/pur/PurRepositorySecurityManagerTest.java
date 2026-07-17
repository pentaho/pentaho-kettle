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

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.pentaho.di.cli.auth.CredentialProvider;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.repository.IUser;
import org.pentaho.di.repository.UserInfo;
import org.pentaho.di.repository.pur.model.EERoleInfo;
import org.pentaho.di.repository.pur.model.IRole;
import org.pentaho.platform.security.userrole.ws.IUserRoleListWebService;
import org.pentaho.platform.security.userroledao.ws.IUserRoleWebService;
import org.pentaho.platform.security.userroledao.ws.UserRoleSecurityInfo;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Andrey Khayrutdinov
 */
public class PurRepositorySecurityManagerTest {

  private PurRepositorySecurityManager manager;
  private UserRoleDelegate roleDelegate;

  @Before
  public void setUp() throws Exception {
    manager = mock( PurRepositorySecurityManager.class );
    doCallRealMethod().when( manager ).saveUserInfo( any( IUser.class ) );
    doCallRealMethod().when( manager ).validateUserInfo( any( IUser.class ) );
    doCallRealMethod().when( manager ).normalizeUserInfo( any( IUser.class ) );

    doCallRealMethod().when( manager ).createRole( any( IRole.class ) );
    doCallRealMethod().when( manager ).validateRoleInfo( any( IRole.class ) );
    doCallRealMethod().when( manager ).normalizeRoleInfo( any( IRole.class ) );

    roleDelegate = mock( UserRoleDelegate.class );
    doCallRealMethod().when( manager ).setUserRoleDelegate( any( UserRoleDelegate.class ) );
    manager.setUserRoleDelegate( roleDelegate );
  }

  @Test
  public void createRole_NormalizesInfo_PassesIfNoViolations() throws Exception {
    IRole info = new EERoleInfo( "role  ", "" );

    ArgumentCaptor<IRole> captor = ArgumentCaptor.forClass( IRole.class );
    manager.createRole( info );
    verify( roleDelegate ).createRole( captor.capture() );

    info = captor.getValue();
    assertEquals( "Spaces should be trimmed", "role", info.getName() );
  }

  @Test( expected = KettleException.class )
  public void createRole_NormalizesInfo_FailsIfStillBreaches() throws Exception {
    IRole info = new EERoleInfo( "    ", "" );
    manager.createRole( info );
  }

  @Test
  public void saveUserInfo_NormalizesInfo_PassesIfNoViolations() throws Exception {
    IUser info = new UserInfo( "login    " );

    ArgumentCaptor<IUser> captor = ArgumentCaptor.forClass( IUser.class );
    manager.saveUserInfo( info );
    verify( roleDelegate ).createUser( captor.capture() );

    info = captor.getValue();
    assertEquals( "Spaces should be trimmed", "login", info.getLogin() );
  }

  @Test( expected = KettleException.class )
  public void saveUserInfo_NormalizesInfo_FailsIfStillBreaches() throws Exception {
    UserInfo info = new UserInfo( "    " );
    manager.saveUserInfo( info );
  }

  @Test
  public void constructor_UsesInjectedCredentialProviderForDelegateInitialization() throws Exception {
    PurRepository repository = mock( PurRepository.class );
    PurRepositoryMeta repositoryMeta = mock( PurRepositoryMeta.class );
    PurRepositoryLocation location = mock( PurRepositoryLocation.class );
    IUser user = mock( IUser.class );
    ServiceManager serviceManager = mock( ServiceManager.class );
    CredentialProvider credentialProvider = mock( CredentialProvider.class );
    IUserRoleListWebService roleListWebService = mock( IUserRoleListWebService.class );
    IUserRoleWebService roleWebService = mock( IUserRoleWebService.class );

    when( repositoryMeta.getRepositoryLocation() ).thenReturn( location );
    when( location.getUrl() ).thenReturn( "http://localhost:8080/pentaho" );
    when( user.getLogin() ).thenReturn( "admin" );
    when( user.getPassword() ).thenReturn( "password" );
    when( serviceManager.createService( "admin", "password", IUserRoleListWebService.class ) )
      .thenReturn( roleListWebService );
    when( serviceManager.createService( "admin", "password", IUserRoleWebService.class ) )
      .thenReturn( roleWebService );
    when( roleWebService.getUserRoleSecurityInfo() ).thenReturn( new UserRoleSecurityInfo() );

    try ( MockedStatic<RestAuthHelper> mockedRest = mockStatic( RestAuthHelper.class ) ) {
      mockedRest.when( () -> RestAuthHelper.executeWithAuthFallback(
        any( HttpGet.class ), eq( "http://localhost:8080/pentaho" ), eq( "admin" ), eq( "password" ),
        eq( "_trust_user_" ), same( credentialProvider ) ) ).thenReturn( "{\"authenticationType\":\"jackrabbit\"}" );

      PurRepositorySecurityManager securityManager = new PurRepositorySecurityManager( repository, repositoryMeta,
        user, serviceManager, credentialProvider );

      assertNotNull( securityManager.getUserRoleDelegate() );
      mockedRest.verify( () -> RestAuthHelper.executeWithAuthFallback(
        any( HttpGet.class ), eq( "http://localhost:8080/pentaho" ), eq( "admin" ), eq( "password" ),
        eq( "_trust_user_" ), same( credentialProvider ) ) );
    }
  }
}
