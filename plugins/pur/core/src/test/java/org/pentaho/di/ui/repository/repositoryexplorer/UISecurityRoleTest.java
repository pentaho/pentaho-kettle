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
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.repository.UserInfo;
import org.pentaho.di.repository.pur.model.EERoleInfo;
import org.pentaho.di.ui.repository.pur.repositoryexplorer.model.UIEERepositoryUser;
import org.pentaho.di.ui.repository.pur.repositoryexplorer.model.UIRepositoryRole;
import org.pentaho.di.ui.repository.pur.repositoryexplorer.model.UISecurityRole;
import org.pentaho.di.ui.repository.repositoryexplorer.model.IUIUser;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UIRepositoryUser;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UISecurity.Mode;

public class UISecurityRoleTest implements java.io.Serializable {

  static final long serialVersionUID = -5870525772742725819L; /* EESOURCE: UPDATE SERIALVERUID */

  private List<UserInfo> users = new ArrayList<UserInfo>();
  private List<EERoleInfo> roles = new ArrayList<EERoleInfo>();
  UserInfo joeUser;
  UserInfo patUser;
  UserInfo suzyUser;
  UserInfo tiffanyUser;
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

    joeUser = new UserInfo( "joe", "password", "joe", "joe", true );
    patUser = new UserInfo( "pat", "password", "pat", "pat", true );
    suzyUser = new UserInfo( "suzy", "password", "suzy", "suzy", true );
    tiffanyUser = new UserInfo( "tiffany", "password", "tiffany", "tiffany", true );

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
  }

  private boolean contains( List<IUIUser> users, IUIUser user ) {
    for ( IUIUser ruser : users ) {
      if ( ruser.equals( user ) ) {
        return true;
      }
    }
    return false;
  }

  @Test
  public void testAddRole() throws Exception {
    try {
      UISecurityRole role = new UISecurityRole();
      List<IUIUser> rusers = new ArrayList<IUIUser>();
      for ( UserInfo userInfo : users ) {
        rusers.add( new UIEERepositoryUser( userInfo ) );
      }
      role.setAvailableUsers( rusers );
      role.setMode( Mode.ADD );
      role.setName( "newrole" );
      role.setDescription( "new description" );
      List<Object> usersToAssign = new ArrayList<Object>();
      usersToAssign.add( new UIRepositoryUser( suzyUser ) );
      usersToAssign.add( new UIRepositoryUser( tiffanyUser ) );
      usersToAssign.add( new UIRepositoryUser( joeUser ) );
      role.assignUsers( usersToAssign );
      assertEquals( role.getMode(), Mode.ADD ); // Should have exactly 7 roles
      assertEquals( role.getName(), "newrole" ); //$NON-NLS-1$
      assertEquals( role.getDescription(), "new description" ); //$NON-NLS-1$
      Assert.assertTrue( contains( role.getAssignedUsers(), new UIRepositoryUser( joeUser ) ) );
      Assert.assertTrue( contains( role.getAssignedUsers(), new UIRepositoryUser( tiffanyUser ) ) );
      Assert.assertTrue( contains( role.getAssignedUsers(), new UIRepositoryUser( suzyUser ) ) );
    } catch ( Exception e ) {
      Assert.fail();
    }
  }

  @Test
  public void testEditRole() throws Exception {
    try {
      UISecurityRole role = new UISecurityRole();
      List<IUIUser> rusers = new ArrayList<IUIUser>();
      for ( UserInfo userInfo : users ) {
        rusers.add( new UIEERepositoryUser( userInfo ) );
      }
      role.setRole( new UIRepositoryRole( adminRole ), rusers );
      role.setMode( Mode.EDIT );
      role.setDescription( "new description" );
      List<Object> usersToAssign = new ArrayList<Object>();
      usersToAssign.add( new UIRepositoryUser( suzyUser ) );
      usersToAssign.add( new UIRepositoryUser( tiffanyUser ) );
      List<Object> usersToUnAssign = new ArrayList<Object>();
      usersToUnAssign.add( new UIRepositoryUser( patUser ) );
      usersToUnAssign.add( new UIRepositoryUser( joeUser ) );
      role.assignUsers( usersToAssign );
      role.unassignUsers( usersToUnAssign );
      assertEquals( role.getMode(), Mode.EDIT ); // Should have exactly 7 roles
      assertEquals( role.getDescription(), "new description" ); //$NON-NLS-1$
      Assert.assertFalse( contains( role.getAssignedUsers(), new UIRepositoryUser( joeUser ) ) );
      Assert.assertFalse( contains( role.getAssignedUsers(), new UIRepositoryUser( patUser ) ) );
      Assert.assertTrue( contains( role.getAssignedUsers(), new UIRepositoryUser( tiffanyUser ) ) );
      Assert.assertTrue( contains( role.getAssignedUsers(), new UIRepositoryUser( suzyUser ) ) );
    } catch ( Exception e ) {
      Assert.fail();
    }
  }
}
