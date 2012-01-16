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

public class UISecurityRoleTest {
 /* private List<UserInfo> users = new ArrayList<UserInfo>();
  private List<RoleInfo> roles = new ArrayList<RoleInfo>();
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
   /*   roles.add(adminRole);
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
      users.add(tiffanyUser);    */
  }
  
 /* private boolean  contains(List<UIRepositoryUser> users, UIRepositoryUser user) {
    for(UIRepositoryUser ruser:users) {
      if(ruser.equals(user)) {
        return true;
      }
    }
    return false;
  }*/
  @Test
  public void testAddRole()  throws Exception {
   /* try {
      UISecurityRole role = new UISecurityRole();
      List<UIRepositoryUser> rusers = new ArrayList<UIRepositoryUser>();
      for(UserInfo userInfo:users) {
        rusers.add(new UIRepositoryUser(userInfo));
      }
      role.setAvailableUsers(rusers);
      role.setMode(Mode.ADD);
      role.setName("newrole");
      role.setDescription("new description");
      List<Object> usersToAssign = new ArrayList<Object>();
      usersToAssign.add(new UIRepositoryUser(suzyUser));
      usersToAssign.add(new UIRepositoryUser(tiffanyUser));
      usersToAssign.add(new UIRepositoryUser(joeUser));
      role.assignUsers(usersToAssign);
      assertEquals(role.getMode(), Mode.ADD); // Should have exactly 7 roles
      assertEquals(role.getName(), "newrole"); //$NON-NLS-1$
      assertEquals(role.getDescription(), "new description"); //$NON-NLS-1$
      Assert.assertTrue(contains(role.getAssignedUsers(),new UIRepositoryUser(joeUser)));
      Assert.assertTrue(contains(role.getAssignedUsers(),new UIRepositoryUser(tiffanyUser)));
      Assert.assertTrue(contains(role.getAssignedUsers(),new UIRepositoryUser(suzyUser)));
    } catch (Exception e) {
        Assert.fail();
    }*/
  }
  
  @Test
  public void testEditRole()  throws Exception {
   /* try {
      UISecurityRole role = new UISecurityRole();
      List<UIRepositoryUser> rusers = new ArrayList<UIRepositoryUser>();
      for(UserInfo userInfo:users) {
        rusers.add(new UIRepositoryUser(userInfo));
      }
      role.setRole(new UIRepositoryRole(adminRole),rusers );
      role.setMode(Mode.EDIT);
      role.setDescription("new description");
      List<Object> usersToAssign = new ArrayList<Object>();
      usersToAssign.add(new UIRepositoryUser(suzyUser));
      usersToAssign.add(new UIRepositoryUser(tiffanyUser));
      List<Object> usersToUnAssign = new ArrayList<Object>();
      usersToUnAssign.add(new UIRepositoryUser(patUser));
      usersToUnAssign.add(new UIRepositoryUser(joeUser));
      role.assignUsers(usersToAssign);
      role.unassignUsers(usersToUnAssign);
      assertEquals(role.getMode(), Mode.EDIT); // Should have exactly 7 roles
      assertEquals(role.getDescription(), "new description"); //$NON-NLS-1$
      Assert.assertFalse(contains(role.getAssignedUsers(),new UIRepositoryUser(joeUser)));
      Assert.assertFalse(contains(role.getAssignedUsers(),new UIRepositoryUser(patUser)));
      Assert.assertTrue(contains(role.getAssignedUsers(),new UIRepositoryUser(tiffanyUser)));
      Assert.assertTrue(contains(role.getAssignedUsers(),new UIRepositoryUser(suzyUser)));
    } catch (Exception e) {
        Assert.fail();
    }*/
  }
}
