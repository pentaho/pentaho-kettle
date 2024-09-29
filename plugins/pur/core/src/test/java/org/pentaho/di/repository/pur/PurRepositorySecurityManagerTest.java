/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.di.repository.pur;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.repository.IUser;
import org.pentaho.di.repository.UserInfo;
import org.pentaho.di.repository.pur.model.EERoleInfo;
import org.pentaho.di.repository.pur.model.IRole;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

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
}
