/*!
 * Copyright 2010 - 2015 Pentaho Corporation.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.pentaho.di.ui.repository.repositoryexplorer;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.repository.RepositorySecurityManager;
import org.pentaho.di.repository.pur.model.EERoleInfo;
import org.pentaho.di.repository.pur.model.EEUserInfo;
import org.pentaho.di.repository.pur.model.IRole;
import org.pentaho.di.ui.repository.pur.repositoryexplorer.IUIRole;
import org.pentaho.di.ui.repository.pur.repositoryexplorer.model.UIEESecurityUser;
import org.pentaho.di.ui.repository.pur.repositoryexplorer.model.UIRepositoryRole;
import org.pentaho.di.ui.repository.repositoryexplorer.abs.model.RepsitoryUserTestImpl;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UIRepositoryUser;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UISecurity.Mode;

public class UIEESecurityUserTest implements java.io.Serializable {

  static final long serialVersionUID = -7328513894400990825L; /* EESOURCE: UPDATE SERIALVERUID */

  RepositorySecurityManager sm;
  private List<EEUserInfo> users = new ArrayList<EEUserInfo>();
  private List<IRole> roles = new ArrayList<IRole>();
  EEUserInfo joeUser;
  EEUserInfo patUser;
  EEUserInfo suzyUser;
  EEUserInfo tiffanyUser;
  EERoleInfo adminRole = new EERoleInfo( "Admin", "Super User" );
  EERoleInfo anonymousRole = new EERoleInfo( "Anonymous", "User has not logged in" );
  EERoleInfo authenticatedRole = new EERoleInfo( "Authenticated", "User has logged in" );
  EERoleInfo ceoRole = new EERoleInfo( "ceo", "Chief Executive Officer" );
  EERoleInfo ctoRole = new EERoleInfo( "cto", "Chief Technology Officer" );
  EERoleInfo devRole = new EERoleInfo( "dev", "Developer" );
  EERoleInfo devmgrRole = new EERoleInfo( "devmgr", "Development Manager" );
  EERoleInfo isRole = new EERoleInfo( "is", "Information Services" );

  @Before
  public void init() {
    roles.add( adminRole );
    roles.add( anonymousRole );
    roles.add( authenticatedRole );
    roles.add( ceoRole );
    roles.add( ctoRole );
    roles.add( devRole );
    roles.add( devmgrRole );
    roles.add( isRole );

    joeUser = new EEUserInfo( "joe", "password", "joe", "joe", true );
    patUser = new EEUserInfo( "pat", "password", "pat", "pat", true );
    suzyUser = new EEUserInfo( "suzy", "password", "suzy", "suzy", true );
    tiffanyUser = new EEUserInfo( "tiffany", "password", "tiffany", "tiffany", true );

    joeUser.addRole( roles.get( 0 ) );
    joeUser.addRole( roles.get( 2 ) );
    joeUser.addRole( roles.get( 3 ) );

    suzyUser.addRole( roles.get( 2 ) );
    suzyUser.addRole( roles.get( 4 ) );
    suzyUser.addRole( roles.get( 7 ) );

    patUser.addRole( roles.get( 2 ) );
    patUser.addRole( roles.get( 5 ) );

    tiffanyUser.addRole( roles.get( 2 ) );
    tiffanyUser.addRole( roles.get( 5 ) );
    tiffanyUser.addRole( roles.get( 6 ) );

    users.add( joeUser );
    users.add( patUser );
    users.add( suzyUser );
    users.add( tiffanyUser );
    sm = new RepsitoryUserTestImpl();
  }

  private boolean contains( List<IUIRole> roles, IUIRole role ) {
    for ( IUIRole rrole : roles ) {
      if ( rrole.getName().equals( role.getName() ) ) {
        return true;
      }
    }
    return false;
  }

  @Test
  public void testEditUser() throws Exception {
    try {
      UIEESecurityUser user = new UIEESecurityUser( sm );
      List<IUIRole> rroles = new ArrayList<IUIRole>();
      for ( IRole EERoleInfo : roles ) {
        rroles.add( new UIRepositoryRole( EERoleInfo ) );
      }
      user.setUser( new UIRepositoryUser( joeUser ), rroles );
      user.setMode( Mode.EDIT );
      user.setPassword( "newpassword" );
      user.setDescription( "new description" );
      List<Object> rolesToAssign1 = new ArrayList<Object>();
      rolesToAssign1.add( new UIRepositoryRole( adminRole ) );
      rolesToAssign1.add( new UIRepositoryRole( authenticatedRole ) );
      user.assignRoles( rolesToAssign1 );
      List<Object> rolesToAssign = new ArrayList<Object>();
      rolesToAssign.add( new UIRepositoryRole( ctoRole ) );
      rolesToAssign.add( new UIRepositoryRole( isRole ) );
      List<Object> rolesToUnAssign = new ArrayList<Object>();
      rolesToUnAssign.add( new UIRepositoryRole( adminRole ) );
      rolesToUnAssign.add( new UIRepositoryRole( authenticatedRole ) );
      user.assignRoles( rolesToAssign );
      user.unassignRoles( rolesToUnAssign );
      assertEquals( user.getMode(), Mode.EDIT ); // Should have exactly 7 roles
      assertEquals( user.getPassword(), "newpassword" );//$NON-NLS-1$
      assertEquals( user.getDescription(), "new description" ); //$NON-NLS-1$
      Assert.assertFalse( contains( user.getAssignedRoles(), new UIRepositoryRole( adminRole ) ) );
      Assert.assertFalse( contains( user.getAssignedRoles(), new UIRepositoryRole( authenticatedRole ) ) );
      Assert.assertTrue( contains( user.getAssignedRoles(), new UIRepositoryRole( ctoRole ) ) );
      Assert.assertTrue( contains( user.getAssignedRoles(), new UIRepositoryRole( isRole ) ) );
    } catch ( Exception e ) {
      Assert.fail();
    }
  }

  @Test
  public void testAddUser() throws Exception {
    try {
      UIEESecurityUser user = new UIEESecurityUser( sm );
      List<IUIRole> rroles = new ArrayList<IUIRole>();
      for ( IRole EERoleInfo : roles ) {
        rroles.add( new UIRepositoryRole( EERoleInfo ) );
      }
      user.clear();
      user.setAvailableRoles( rroles );
      user.setMode( Mode.ADD );
      user.setName( "newuser" );
      user.setPassword( "newpassword" );
      user.setDescription( "new description" );
      List<Object> rolesToAssign = new ArrayList<Object>();
      rolesToAssign.add( new UIRepositoryRole( ctoRole ) );
      rolesToAssign.add( new UIRepositoryRole( isRole ) );
      rolesToAssign.add( new UIRepositoryRole( adminRole ) );
      rolesToAssign.add( new UIRepositoryRole( authenticatedRole ) );
      user.assignRoles( rolesToAssign );
      assertEquals( user.getMode(), Mode.ADD );
      assertEquals( user.getName(), "newuser" );//$NON-NLS-1$
      assertEquals( user.getPassword(), "newpassword" );//$NON-NLS-1$
      assertEquals( user.getDescription(), "new description" ); //$NON-NLS-1$
      Assert.assertTrue( contains( user.getAssignedRoles(), new UIRepositoryRole( adminRole ) ) );
      Assert.assertTrue( contains( user.getAssignedRoles(), new UIRepositoryRole( authenticatedRole ) ) );
      Assert.assertTrue( contains( user.getAssignedRoles(), new UIRepositoryRole( ctoRole ) ) );
      Assert.assertTrue( contains( user.getAssignedRoles(), new UIRepositoryRole( isRole ) ) );
    } catch ( Exception e ) {
      Assert.fail();
    }
  }

}
