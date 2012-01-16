/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.repositoryexplorer.model;

import org.junit.Before;
import org.junit.Test;

public class UISecurityTest {
 /* private List<UserInfo> users = new ArrayList<UserInfo>();
  private List<RoleInfo> roles = new ArrayList<RoleInfo>();
  private UISecurity security = new UISecurity();
  List<UIRepositoryRole> rroles;
  List<UIRepositoryUser> rusers;
  UserInfo joeUser;
  UserInfo patUser;  
  UserInfo suzyUser;
  UserInfo tiffanyUser;
  RoleInfo adminRole = new RoleInfo("Admin","Super User");
  RoleInfo anonymousRole = new RoleInfo("Anonymous","User has not logged in");
  RoleInfo authenticatedRole =  new RoleInfo("Authenticated","User has logged in");
  RoleInfo ceoRole =  new RoleInfo("ceo","Chief Executive Officer");
  RoleInfo ctoRole =  new RoleInfo("cto","Chief Technology Officer");
  RoleInfo devRole =  new RoleInfo("dev","Developer");
  RoleInfo devmgrRole =  new RoleInfo("devmgr","Development Manager");
  RoleInfo isRole =  new RoleInfo("is","Information Services");*/
  @Before
  public void init() {    
  /*    roles.add(adminRole);
      roles.add(anonymousRole);
      roles.add(authenticatedRole);
      roles.add(ceoRole);
      roles.add(ctoRole);
      roles.add(devRole);
      roles.add(devmgrRole);
      roles.add(isRole);
      
      joeUser = new UserInfo("joe", "password", "joe","joe", true);
      patUser = new UserInfo("pat", "password", "pat","pat", true);
      suzyUser = new UserInfo("suzy", "password", "suzy","suzy", true);
      tiffanyUser = new UserInfo("tiffany", "password", "tiffany","tiffany", true);
      
      joeUser.addRole(roles.get(0));
      joeUser.addRole(roles.get(2));
      joeUser.addRole(roles.get(3));

      suzyUser.addRole(roles.get(2));
      suzyUser.addRole(roles.get(4));
      suzyUser.addRole(roles.get(7));

      patUser.addRole(roles.get(2));
      patUser.addRole(roles.get(5));

      tiffanyUser.addRole(roles.get(2));
      tiffanyUser.addRole(roles.get(5));
      tiffanyUser.addRole(roles.get(6));
      
      adminRole.addUser(joeUser);
      adminRole.addUser(patUser);
      
      anonymousRole.addUser(tiffanyUser);
      
      authenticatedRole.addUser(joeUser);
      authenticatedRole.addUser(patUser);
      authenticatedRole.addUser(suzyUser);
      authenticatedRole.addUser(tiffanyUser);
      
      ceoRole.addUser(joeUser);
      
      ctoRole.addUser(patUser);
      
      devmgrRole.addUser(joeUser);
      devmgrRole.addUser(patUser);
      
      isRole.addUser(joeUser);
      isRole.addUser(suzyUser);
      
      users.add(joeUser);
      users.add(patUser);
      users.add(suzyUser);
      users.add(tiffanyUser);
      rroles = new ArrayList<UIRepositoryRole>();
      for(RoleInfo roleInfo:roles) {
        rroles.add(new UIRepositoryRole(roleInfo));
      }
      rusers = new ArrayList<UIRepositoryUser>();
      for(UserInfo userInfo:users) {
        rusers.add(new UIRepositoryUser(userInfo));
      }      
      security.setUserList(rusers);
      security.setRoleList(rroles);
*/
  }
 /* private UIRepositoryUser findUser(String username) {
    for(UIRepositoryUser user:security.getUserList()) {
      if(user.getName().equals(username)) {
        return user;
      }
    }
    return null;
  }
  
  private UIRepositoryRole findRole(String rolename) {
    for(UIRepositoryRole role:security.getRoleList()) {
      if(role.getName().equals(rolename)) {
        return role;
      }
    }
    return null;
  }*/
  @Test
  public void testAddUser()  throws Exception {
  /*  try {
      security.setSelectedDeck(ObjectRecipient.Type.USER);
      UIRepositoryUser userToAdd = new UIRepositoryUser(new UserInfo());
      userToAdd.setName("newuser");
      userToAdd.setPassword("newpassword");
      userToAdd.setDescription("new description");
      Set<RoleInfo> rolesToAssign = new HashSet<RoleInfo>();
      rolesToAssign.add(ctoRole);
      rolesToAssign.add(isRole);
      rolesToAssign.add(adminRole);
      rolesToAssign.add(authenticatedRole);
      userToAdd.setRoles(rolesToAssign);
      security.addUser(userToAdd);
      assertEquals(security.getSelectedUser(), userToAdd);
      assertEquals(security.getSelectedDeck(), ObjectRecipient.Type.USER);
      assertEquals(security.getUserList().size(), 5);
    } catch (Exception e) {
        Assert.fail();
    }*/
  }

  @Test
  public void testUpdateUser()  throws Exception {
  /*  try {
      UIRepositoryUser userToAdd = new UIRepositoryUser(new UserInfo());
      userToAdd.setName("newuser");
      userToAdd.setPassword("newpassword");
      userToAdd.setDescription("new description");
      Set<RoleInfo> rolesToAssign = new HashSet<RoleInfo>();
      rolesToAssign.add(ctoRole);
      rolesToAssign.add(isRole);
      rolesToAssign.add(adminRole);
      rolesToAssign.add(authenticatedRole);
      userToAdd.setRoles(rolesToAssign);
      security.addUser(userToAdd);
      
      UIRepositoryUser selectedUser = security.getSelectedUser();
      selectedUser.setPassword("newpassword123");
      selectedUser.setDescription("new description 123");
      selectedUser.addRole(ctoRole);
      selectedUser.addRole(isRole);
      selectedUser.removeRole(adminRole);
      selectedUser.removeRole(authenticatedRole);
      security.updateUser(selectedUser);
      assertEquals(selectedUser.getPassword(), "newpassword123");//$NON-NLS-1$
      assertEquals(selectedUser.getDescription(), "new description 123"); //$NON-NLS-1$
      assertEquals(security.getSelectedUser(), selectedUser);
      assertEquals(security.getUserList().size(), 5);
    } catch (Exception e) {
        Assert.fail();
    }*/
  }
  
  @Test
  public void testRemoveUser()  throws Exception {
  /*  try {
      UIRepositoryUser userToAdd = new UIRepositoryUser(new UserInfo());
      userToAdd.setName("newuser");
      userToAdd.setPassword("newpassword");
      userToAdd.setDescription("new description");
      Set<RoleInfo> rolesToAssign = new HashSet<RoleInfo>();
      rolesToAssign.add(ctoRole);
      rolesToAssign.add(isRole);
      rolesToAssign.add(adminRole);
      rolesToAssign.add(authenticatedRole);
      userToAdd.setRoles(rolesToAssign);
      security.addUser(userToAdd);      

      UIRepositoryUser selectedUser = security.getSelectedUser();
      int removeUserIndex = security.getSelectedUserIndex();
      security.removeUser("newuser");
      assertEquals(security.getSelectedUserIndex(), removeUserIndex-1);
      assertEquals(security.getUserList().size(), 4);
    } catch (Exception e) {
        Assert.fail();
    }*/
  }

  
  @Test
  public void testAddRole()  throws Exception {
  /*  try {
      security.setSelectedDeck(ObjectRecipient.Type.ROLE);
      UIRepositoryRole roleToAdd = new UIRepositoryRole(new RoleInfo());
      roleToAdd.setName("newrole");
      roleToAdd.setDescription("new description");
      Set<UserInfo> usersToAssign = new HashSet<UserInfo>();
      usersToAssign.add(suzyUser);
      usersToAssign.add(tiffanyUser);
      usersToAssign.add(joeUser);
      security.addRole(roleToAdd);
      assertEquals(security.getSelectedRole(), roleToAdd);
      assertEquals(security.getSelectedDeck(), ObjectRecipient.Type.ROLE);
      assertEquals(security.getRoleList().size(), 9);
    } catch (Exception e) {
        Assert.fail();
    }*/
  }
  
  @Test
  public void testUpdateRole()  throws Exception {
   /* try {
      UIRepositoryRole roleToAdd = new UIRepositoryRole(new RoleInfo());
      roleToAdd.setName("newrole");
      roleToAdd.setDescription("new description");
      Set<UserInfo> usersToAssign = new HashSet<UserInfo>();
      usersToAssign.add(suzyUser);
      usersToAssign.add(tiffanyUser);
      usersToAssign.add(joeUser);
      security.addRole(roleToAdd);

      security.setSelectedRole(findRole("newrole"));
      UIRepositoryRole selectedRole = security.getSelectedRole();
      selectedRole.setDescription("new description 123");
      selectedRole.addUser(patUser);
      selectedRole.removeUser(suzyUser);
      selectedRole.removeUser(tiffanyUser);
      security.updateRole(selectedRole);
      assertEquals(selectedRole.getDescription(), "new description 123"); //$NON-NLS-1$
      assertEquals(security.getSelectedRole(), selectedRole);
      assertEquals(security.getRoleList().size(), 9);
    } catch (Exception e) {
        Assert.fail();
    }*/
  }
  
  @Test
  public void testRemoveRole()  throws Exception {
 /*   try {
      UIRepositoryRole roleToAdd = new UIRepositoryRole(new RoleInfo());
      roleToAdd.setName("newrole");
      roleToAdd.setDescription("new description");
      Set<UserInfo> usersToAssign = new HashSet<UserInfo>();
      usersToAssign.add(suzyUser);
      usersToAssign.add(tiffanyUser);
      usersToAssign.add(joeUser);
      security.addRole(roleToAdd);

      UIRepositoryRole selectedRole = security.getSelectedRole();
      int removeRoleIndex = security.getSelectedRoleIndex();
      security.removeRole("newrole");
      assertEquals(security.getSelectedRoleIndex(), removeRoleIndex-1);
      assertEquals(security.getRoleList().size(), 8);    } catch (Exception e) {
        Assert.fail();
    }*/
  }
}
