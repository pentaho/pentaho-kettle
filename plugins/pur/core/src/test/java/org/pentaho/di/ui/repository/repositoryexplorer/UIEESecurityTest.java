/*!
 * Copyright 2010 - 2017 Hitachi Vantara.  All rights reserved.
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.repository.IUser;
import org.pentaho.di.repository.ObjectRecipient;
import org.pentaho.di.repository.pur.model.EERoleInfo;
import org.pentaho.di.repository.pur.model.EEUserInfo;
import org.pentaho.di.repository.pur.model.IRole;
import org.pentaho.di.ui.repository.pur.repositoryexplorer.IUIEEUser;
import org.pentaho.di.ui.repository.pur.repositoryexplorer.IUIRole;
import org.pentaho.di.ui.repository.pur.repositoryexplorer.model.UIEERepositoryUser;
import org.pentaho.di.ui.repository.pur.repositoryexplorer.model.UIEESecurity;
import org.pentaho.di.ui.repository.pur.repositoryexplorer.model.UIRepositoryRole;
import org.pentaho.di.ui.repository.repositoryexplorer.model.IUIUser;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UIRepositoryUser;

public class UIEESecurityTest implements java.io.Serializable {

  static final long serialVersionUID = -7889725393698207931L; /* EESOURCE: UPDATE SERIALVERUID */

  private List<IUser> users = new ArrayList<IUser>();
  private List<IRole> roles = new ArrayList<IRole>();
  private UIEESecurity security = new UIEESecurity();
  List<IUIRole> rroles;
  List<IUIUser> rusers;
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

    adminRole.addUser( joeUser );
    adminRole.addUser( patUser );

    anonymousRole.addUser( tiffanyUser );

    authenticatedRole.addUser( joeUser );
    authenticatedRole.addUser( patUser );
    authenticatedRole.addUser( suzyUser );
    authenticatedRole.addUser( tiffanyUser );

    ceoRole.addUser( joeUser );

    ctoRole.addUser( patUser );

    devmgrRole.addUser( joeUser );
    devmgrRole.addUser( patUser );

    isRole.addUser( joeUser );
    isRole.addUser( suzyUser );

    users.add( joeUser );
    users.add( patUser );
    users.add( suzyUser );
    users.add( tiffanyUser );
    rroles = new ArrayList<IUIRole>();
    for ( IRole EERoleInfo : roles ) {
      IUIRole role = new UIRepositoryRole( EERoleInfo );
      rroles.add( role );
    }
    rusers = new ArrayList<IUIUser>();
    for ( IUser eEUserInfo : users ) {
      rusers.add( new UIRepositoryUser( eEUserInfo ) );
    }
    security.setUserList( rusers );
    security.setRoleList( rroles );

  }

  protected IUIUser findUser( String username ) {
    for ( IUIUser user : security.getUserList() ) {
      if ( user.getName().equals( username ) ) {
        return user;
      }
    }
    return null;
  }

  private IUIRole findRole( String rolename ) {
    for ( IUIRole role : security.getRoleList() ) {
      if ( role.getName().equals( rolename ) ) {
        return role;
      }
    }
    return null;
  }

  @Test
  public void testAddUser() throws Exception {
    try {
      security.setSelectedDeck( ObjectRecipient.Type.USER );
      UIEERepositoryUser userToAdd = new UIEERepositoryUser( new EEUserInfo() );
      userToAdd.setName( "newuser" );
      userToAdd.setPassword( "newpassword" );
      userToAdd.setDescription( "new description" );
      Set<IUIRole> rolesToAssign = new HashSet<IUIRole>();
      rolesToAssign.add( new UIRepositoryRole( ctoRole ) );
      rolesToAssign.add( new UIRepositoryRole( isRole ) );
      rolesToAssign.add( new UIRepositoryRole( adminRole ) );
      rolesToAssign.add( new UIRepositoryRole( authenticatedRole ) );
      userToAdd.setRoles( rolesToAssign );
      security.addUser( userToAdd );
      assertEquals( security.getSelectedUser(), userToAdd );
      assertEquals( security.getSelectedDeck(), ObjectRecipient.Type.USER );
      assertEquals( security.getUserList().size(), 5 );
    } catch ( Exception e ) {
      Assert.fail();
    }
  }

  @Test
  public void testUpdateUser() throws Exception {
    try {
      UIEERepositoryUser userToAdd = new UIEERepositoryUser( new EEUserInfo() );
      userToAdd.setName( "newuser" );
      userToAdd.setPassword( "newpassword" );
      userToAdd.setDescription( "new description" );
      Set<IUIRole> rolesToAssign = new HashSet<IUIRole>();
      rolesToAssign.add( new UIRepositoryRole( ctoRole ) );
      rolesToAssign.add( new UIRepositoryRole( isRole ) );
      rolesToAssign.add( new UIRepositoryRole( adminRole ) );
      rolesToAssign.add( new UIRepositoryRole( authenticatedRole ) );
      userToAdd.setRoles( rolesToAssign );
      security.addUser( userToAdd );

      IUIUser selectedUser = security.getSelectedUser();
      selectedUser.setPassword( "newpassword123" );
      selectedUser.setDescription( "new description 123" );
      ( (IUIEEUser) selectedUser ).addRole( new UIRepositoryRole( ctoRole ) );
      ( (IUIEEUser) selectedUser ).addRole( new UIRepositoryRole( isRole ) );
      ( (IUIEEUser) selectedUser ).removeRole( new UIRepositoryRole( adminRole ) );
      ( (IUIEEUser) selectedUser ).removeRole( new UIRepositoryRole( authenticatedRole ) );
      security.updateUser( selectedUser, rolesToAssign );
      assertEquals( selectedUser.getPassword(), "newpassword123" );//$NON-NLS-1$
      assertEquals( selectedUser.getDescription(), "new description 123" ); //$NON-NLS-1$
      assertEquals( security.getSelectedUser(), selectedUser );
      assertEquals( security.getUserList().size(), 5 );
    } catch ( Exception e ) {
      Assert.fail();
    }
  }

  @Test
  public void testRemoveUser() throws Exception {
    try {
      UIEERepositoryUser userToAdd = new UIEERepositoryUser( new EEUserInfo() );
      userToAdd.setName( "newuser" );
      userToAdd.setPassword( "newpassword" );
      userToAdd.setDescription( "new description" );
      Set<IUIRole> rolesToAssign = new HashSet<IUIRole>();
      rolesToAssign.add( new UIRepositoryRole( ctoRole ) );
      rolesToAssign.add( new UIRepositoryRole( isRole ) );
      rolesToAssign.add( new UIRepositoryRole( adminRole ) );
      rolesToAssign.add( new UIRepositoryRole( authenticatedRole ) );
      userToAdd.setRoles( rolesToAssign );
      security.addUser( userToAdd );

      // IUIUser selectedUser = security.getSelectedUser();
      int removeUserIndex = security.getSelectedUserIndex();
      security.removeUser( "newuser" );
      assertEquals( security.getSelectedUserIndex(), removeUserIndex - 1 );
      assertEquals( security.getUserList().size(), 4 );
    } catch ( Exception e ) {
      Assert.fail();
    }
  }

  @Test
  public void testAddRole() throws Exception {
    try {
      security.setSelectedDeck( ObjectRecipient.Type.ROLE );
      UIRepositoryRole roleToAdd = new UIRepositoryRole( new EERoleInfo() );
      roleToAdd.setName( "newrole" );
      roleToAdd.setDescription( "new description" );
      Set<EEUserInfo> usersToAssign = new HashSet<EEUserInfo>();
      usersToAssign.add( suzyUser );
      usersToAssign.add( tiffanyUser );
      usersToAssign.add( joeUser );
      security.addRole( roleToAdd );
      assertEquals( security.getSelectedRole(), roleToAdd );
      assertEquals( security.getSelectedDeck(), ObjectRecipient.Type.ROLE );
      assertEquals( security.getRoleList().size(), 9 );
    } catch ( Exception e ) {
      Assert.fail();
    }
  }

  @Test
  public void testUpdateRole() throws Exception {
    try {
      IUIRole roleToAdd = new UIRepositoryRole( new EERoleInfo() );
      roleToAdd.setName( "newrole" );
      roleToAdd.setDescription( "new description" );
      Set<IUIUser> usersToAssign = new HashSet<IUIUser>();
      usersToAssign.add( new UIRepositoryUser( suzyUser ) );
      usersToAssign.add( new UIRepositoryUser( tiffanyUser ) );
      usersToAssign.add( new UIRepositoryUser( joeUser ) );
      roleToAdd.setUsers( usersToAssign );
      security.addRole( roleToAdd );
      security.setSelectedRole( findRole( "newrole" ) );
      IUIRole selectedRole = security.getSelectedRole();
      selectedRole.setDescription( "new description 123" );
      selectedRole.addUser( new UIRepositoryUser( patUser ) );
      selectedRole.removeUser( new UIRepositoryUser( suzyUser ) );
      selectedRole.removeUser( new UIRepositoryUser( tiffanyUser ) );
      security.updateRole( selectedRole, usersToAssign );
      assertEquals( selectedRole.getDescription(), "new description 123" ); //$NON-NLS-1$
      assertEquals( security.getSelectedRole(), selectedRole );
      assertEquals( security.getRoleList().size(), 9 );
    } catch ( Exception e ) {
      Assert.fail();
    }
  }

  @Test
  public void testRemoveRole() throws Exception {
    try {
      UIRepositoryRole roleToAdd = new UIRepositoryRole( new EERoleInfo() );
      roleToAdd.setName( "newrole" );
      roleToAdd.setDescription( "new description" );
      Set<EEUserInfo> usersToAssign = new HashSet<EEUserInfo>();
      usersToAssign.add( suzyUser );
      usersToAssign.add( tiffanyUser );
      usersToAssign.add( joeUser );
      security.addRole( roleToAdd );

      // IUIRole selectedRole = security.getSelectedRole();
      int removeRoleIndex = security.getSelectedRoleIndex();
      security.removeRole( "newrole" );
      assertEquals( security.getSelectedRoleIndex(), removeRoleIndex - 1 );
      assertEquals( security.getRoleList().size(), 8 );
    } catch ( Exception e ) {
      Assert.fail();
    }
  }
}
