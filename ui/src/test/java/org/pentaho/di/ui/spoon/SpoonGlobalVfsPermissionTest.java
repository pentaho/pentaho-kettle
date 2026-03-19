package org.pentaho.di.ui.spoon;

import org.pentaho.di.repository.IUser;
import org.pentaho.di.repository.Repository;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class SpoonGlobalVfsPermissionTest {

  private Spoon spoon;

  @Before
  public void setUp() {
    spoon = mock( Spoon.class );
    doCallRealMethod().when( spoon ).isAllowedManageGlobalVFS();
  }

  @Test
  public void isAllowedManageGlobalVFS_AdminUser() {
    Repository repository = mock( Repository.class );
    IUser user = mock( IUser.class );

    doReturn( user ).when( repository ).getUserInfo();
    doReturn( true ).when( user ).isAdmin();
    spoon.rep = repository;

    assertTrue( spoon.isAllowedManageGlobalVFS() );
  }

  @Test
  public void isAllowedManageGlobalVFS_NonAdminUser() {
    Repository repository = mock( Repository.class );
    IUser user = mock( IUser.class );

    doReturn( user ).when( repository ).getUserInfo();
    doReturn( false ).when( user ).isAdmin();
    spoon.rep = repository;

    assertFalse( spoon.isAllowedManageGlobalVFS() );
  }

  @Test
  public void isAllowedManageGlobalVFS_NullRepository() {
    spoon.rep = null;

    assertTrue( spoon.isAllowedManageGlobalVFS() );
  }

  @Test
  public void isAllowedManageGlobalVFS_NullAdminFlag() {
    Repository repository = mock( Repository.class );
    IUser user = mock( IUser.class );

    doReturn( user ).when( repository ).getUserInfo();
    doReturn( null ).when( user ).isAdmin();
    spoon.rep = repository;

    assertTrue( spoon.isAllowedManageGlobalVFS() );
  }

  @Test
  public void isAllowedManageGlobalVFS_NullUser() {
    Repository repository = mock( Repository.class );

    doReturn( null ).when( repository ).getUserInfo();
    spoon.rep = repository;

    assertTrue( spoon.isAllowedManageGlobalVFS() );
  }
}
