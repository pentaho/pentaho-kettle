package org.pentaho.di.repositoryexplorer.model;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.repository.RoleInfo;
import org.pentaho.di.repository.UserInfo;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UIRepositoryRole;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UIRepositoryUser;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UISecurityUser;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UISecurity.Mode;

public class UISecurityUserTest {
  private List<UserInfo> users = new ArrayList<UserInfo>();
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
  RoleInfo isRole =  new RoleInfo("is","Information Services");
  @Before
  public void init() {    
      roles.add(adminRole);
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
      
      users.add(joeUser);
      users.add(patUser);
      users.add(suzyUser);
      users.add(tiffanyUser);    
  }
  
  private boolean  contains(List<UIRepositoryRole> roles, UIRepositoryRole role) {
    for(UIRepositoryRole rrole:roles) {
      if(rrole.equals(role)) {
        return true;
      }
    }
    return false;
  }
  @Test
  public void testEditUser()  throws Exception {
    try {
      UISecurityUser user = new UISecurityUser();
      List<UIRepositoryRole> rroles = new ArrayList<UIRepositoryRole>();
      for(RoleInfo roleInfo:roles) {
        rroles.add(new UIRepositoryRole(roleInfo));
      }
      user.setUser(new UIRepositoryUser(joeUser),rroles );
      user.setMode(Mode.EDIT);
      user.setPassword("newpassword");
      user.setDescription("new description");
      List<Object> rolesToAssign = new ArrayList<Object>();
      rolesToAssign.add(new UIRepositoryRole(ctoRole));
      rolesToAssign.add(new UIRepositoryRole(isRole));
      List<Object> rolesToUnAssign = new ArrayList<Object>();
      rolesToUnAssign.add(new UIRepositoryRole(adminRole));
      rolesToUnAssign.add(new UIRepositoryRole(authenticatedRole));
      user.assignRoles(rolesToAssign);
      user.unassignRoles(rolesToUnAssign);
      assertEquals(user.getMode(), Mode.EDIT); // Should have exactly 7 roles
      assertEquals(user.getPassword(), "newpassword");//$NON-NLS-1$
      assertEquals(user.getDescription(), "new description"); //$NON-NLS-1$
      Assert.assertFalse(contains(user.getAssignedRoles(),new UIRepositoryRole(adminRole)));
      Assert.assertFalse(contains(user.getAssignedRoles(),new UIRepositoryRole(authenticatedRole)));
      Assert.assertTrue(contains(user.getAssignedRoles(),new UIRepositoryRole(ctoRole)));
      Assert.assertTrue(contains(user.getAssignedRoles(),new UIRepositoryRole(isRole)));
    } catch (Exception e) {
        Assert.fail();
    }
  }
  
  @Test
  public void testAddUser()  throws Exception {
    try {
      UISecurityUser user = new UISecurityUser();
      List<UIRepositoryRole> rroles = new ArrayList<UIRepositoryRole>();
      for(RoleInfo roleInfo:roles) {
        rroles.add(new UIRepositoryRole(roleInfo));
      }
      user.clear();
      user.setAvailableRoles(rroles);
      user.setMode(Mode.ADD);
      user.setName("newuser");
      user.setPassword("newpassword");
      user.setDescription("new description");
      List<Object> rolesToAssign = new ArrayList<Object>();
      rolesToAssign.add(new UIRepositoryRole(ctoRole));
      rolesToAssign.add(new UIRepositoryRole(isRole));
      rolesToAssign.add(new UIRepositoryRole(adminRole));
      rolesToAssign.add(new UIRepositoryRole(authenticatedRole));
      user.assignRoles(rolesToAssign);
      assertEquals(user.getMode(), Mode.ADD); 
      assertEquals(user.getName(), "newuser");//$NON-NLS-1$
      assertEquals(user.getPassword(), "newpassword");//$NON-NLS-1$
      assertEquals(user.getDescription(), "new description"); //$NON-NLS-1$
      Assert.assertTrue(contains(user.getAssignedRoles(),new UIRepositoryRole(adminRole)));
      Assert.assertTrue(contains(user.getAssignedRoles(),new UIRepositoryRole(authenticatedRole)));
      Assert.assertTrue(contains(user.getAssignedRoles(),new UIRepositoryRole(ctoRole)));
      Assert.assertTrue(contains(user.getAssignedRoles(),new UIRepositoryRole(isRole)));
    } catch (Exception e) {
        Assert.fail();
    }
  }

}
