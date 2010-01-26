/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2009 Pentaho Corporation.  All rights reserved.
 */
package org.pentaho.di.ui.repository.repositoryexplorer.controllers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.repository.ObjectRecipient;
import org.pentaho.di.repository.RepositoryUserInterface;
import org.pentaho.di.repository.RoleInfo;
import org.pentaho.di.repository.UserInfo;
import org.pentaho.di.repository.ObjectRecipient.Type;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UIRepositoryRole;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UIRepositoryUser;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UISecurity;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UISecurityRole;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UISecurityUser;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UISecurity.Mode;
import org.pentaho.ui.xul.binding.Binding;
import org.pentaho.ui.xul.binding.BindingConvertor;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.components.XulButton;
import org.pentaho.ui.xul.components.XulRadio;
import org.pentaho.ui.xul.components.XulTextbox;
import org.pentaho.ui.xul.containers.XulDeck;
import org.pentaho.ui.xul.containers.XulDialog;
import org.pentaho.ui.xul.containers.XulListbox;
import org.pentaho.ui.xul.containers.XulTree;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;

/**
 *
 * This is the XulEventHandler for the browse panel of the repository explorer. It sets up the bindings for  
 * browse functionality.
 * 
 */
public class SecurityController extends AbstractXulEventHandler{

  public static final int ROLE_DECK = 0;

  public static final int USER_DECK = 1;

  private XulRadio roleRadioButton;

  private XulRadio userRadioButton;

  private XulTree userDetailTable;

  private XulTree roleDetailTable;

  private XulDialog userDialog;

  private XulDialog roleDialog;

  private XulDialog removeUserConfirmationDialog;

  private XulDialog removeRoleConfirmationDialog;

  private XulDeck userRoleDeck;

  private XulListbox roleListBox;

  private XulListbox userListBox;

  private XulTextbox username;

  private XulTextbox userPassword;

  private XulTextbox userDescription;

  private XulListbox availableRoles;

  private XulListbox assignedRoles;

  private XulTextbox rolename;

  private XulTextbox roleDescription;

  private XulListbox availableUsers;

  private XulListbox assignedUsers;

  private XulButton roleEditButton;

  private XulButton roleRemoveButton;

  private XulButton userEditButton;

  private XulButton userRemoveButton;

  private XulButton addUserToRoleButton;

  private XulButton removeUserFromRoleButton;

  private XulButton addRoleToUserButton;

  private XulButton removeRoleFromUserButton;

  private XulButton assignRoleToUserButton;

  private XulButton unassignRoleFromUserButton;
  // private RepositoryExplorerCallback callback;
  
  private XulButton assignUserToRoleButton;

  private XulButton unassignUserFromRoleButton;

  
  private RepositoryUserInterface rui;


  private UISecurity security;

  BindingFactory bf;

  Binding securityBinding;

  Binding roleDetailBinding;

  Binding userDetailBinding;

  private UISecurityUser securityUser;

  private UISecurityRole securityRole;

  public SecurityController() {
  }

  public void init() {
    // Initialize all security models
    security = new UISecurity(rui);
    securityRole = new UISecurityRole();
    securityUser = new UISecurityUser();
    if (bf != null) {
      createBindings();
    }
  }

  private void createBindings() {
    //User Role Binding

    removeUserConfirmationDialog = (XulDialog) document.getElementById("remove-user-confirmation-dialog");
    removeRoleConfirmationDialog = (XulDialog) document.getElementById("remove-role-confirmation-dialog");

    roleRadioButton = (XulRadio) document.getElementById("role-radio-button");
    userRadioButton = (XulRadio) document.getElementById("user-radio-button");

    roleEditButton = (XulButton) document.getElementById("role-edit");
    roleRemoveButton = (XulButton) document.getElementById("role-remove");
    userEditButton = (XulButton) document.getElementById("user-edit");
    userRemoveButton = (XulButton) document.getElementById("user-remove");

    addUserToRoleButton = (XulButton) document.getElementById("add-user-to-role");
    removeUserFromRoleButton = (XulButton) document.getElementById("remove-user-from-role");
    addRoleToUserButton = (XulButton) document.getElementById("add-role-to-user");
    removeRoleFromUserButton = (XulButton) document.getElementById("remove-role-from-user");

    userDialog = (XulDialog) document.getElementById("add-user-dialog");
    roleDialog = (XulDialog) document.getElementById("add-role-dialog");
    userRoleDeck = (XulDeck) document.getElementById("user-role-deck");
    roleListBox = (XulListbox) document.getElementById("roles-list");
    userListBox = (XulListbox) document.getElementById("users-list");
    roleDetailTable = (XulTree) document.getElementById("role-detail-table");
    userDetailTable = (XulTree) document.getElementById("user-detail-table");
    // Add User Binding

    username = (XulTextbox) document.getElementById("user-name");
    userPassword = (XulTextbox) document.getElementById("user-password");
    userDescription = (XulTextbox) document.getElementById("user-description");
    availableRoles = (XulListbox) document.getElementById("available-roles-list");
    assignedRoles = (XulListbox) document.getElementById("selected-roles-list");
    assignRoleToUserButton = (XulButton) document.getElementById("assign-role-to-user");
    unassignRoleFromUserButton = (XulButton) document.getElementById("unassign-role-from-user");

    bf.setBindingType(Binding.Type.BI_DIRECTIONAL);
    bf.createBinding(securityUser, "name", username, "value");
    bf.createBinding(securityUser, "password", userPassword, "value");
    bf.createBinding(securityUser, "description", userDescription, "value");
    bf.createBinding(securityUser, "assignedRoles", assignedRoles, "elements");
    bf.createBinding(securityUser, "availableRoles", availableRoles, "elements");

    BindingConvertor<List<UIRepositoryRole>, Object[]> arrayToListRoleConverter = new BindingConvertor<List<UIRepositoryRole>, Object[]>() {

      @Override
      public Object[] sourceToTarget(List<UIRepositoryRole> roles) {
        if(roles != null) {
          Object[] retVal = new Object[roles.size()];
          int i=0;
          for(UIRepositoryRole role:roles) {
            retVal[i++] = role;
          }
          return retVal;
        }
        return null;
      }

      @Override
      public List<UIRepositoryRole> targetToSource(Object[] roles) {
        if(roles != null) {
          List<UIRepositoryRole> retVal = new ArrayList<UIRepositoryRole>();
          for(int i=0;i<roles.length;i++) {
            retVal.add((UIRepositoryRole)roles[i]);
          }
          return retVal;
        }
        return null;
      }
      
    };

    BindingConvertor<List<UIRepositoryUser>, Object[]> arrayToListUserConverter = new BindingConvertor<List<UIRepositoryUser>, Object[]>() {

      @Override
      public Object[] sourceToTarget(List<UIRepositoryUser> users) {
        if(users != null) {
          Object[] retVal = new Object[users.size()];
          int i=0;
          for(UIRepositoryUser user:users) {
            retVal[i++] = user;
          }
          return retVal;
        }
        return null;
      }

      @Override
      public List<UIRepositoryUser> targetToSource(Object[] users) {
        if(users != null) {
          List<UIRepositoryUser> retVal = new ArrayList<UIRepositoryUser>();
          for(int i=0;i<users.length;i++) {
            retVal.add((UIRepositoryUser)users[i]);
          }
          return retVal;
        }
        return null;
      }
      
    };

    bf.createBinding(securityUser, "availableSelectedRoles", availableRoles, "selectedItems", arrayToListRoleConverter);
    bf.createBinding(securityUser, "assignedSelectedRoles", assignedRoles, "selectedItems", arrayToListRoleConverter);
    
    bf.createBinding(security, "selectedUserIndex", userListBox, "selectedIndex");

    BindingConvertor<Integer, Boolean> accumulatorButtonConverter = new BindingConvertor<Integer, Boolean>() {

      @Override
      public Boolean sourceToTarget(Integer value) {
        if (value != null && value >= 0) {
          return true;
        }
        return false;
      }

      @Override
      public Integer targetToSource(Boolean value) {
        // TODO Auto-generated method stub
        return null;
      }
    };
    
    bf.setBindingType(Binding.Type.ONE_WAY);
    bf.createBinding(assignedRoles,  "selectedIndex", securityUser, "roleUnassignmentPossible", accumulatorButtonConverter);
    bf.createBinding(availableRoles, "selectedIndex", securityUser, "roleAssignmentPossible", accumulatorButtonConverter);
    
    bf.createBinding(securityUser, "roleUnassignmentPossible", unassignRoleFromUserButton, "!disabled");
    bf.createBinding(securityUser, "roleAssignmentPossible", assignRoleToUserButton, "!disabled");

    bf.setBindingType(Binding.Type.BI_DIRECTIONAL);
    
    // Add Role Binding

    rolename = (XulTextbox) document.getElementById("role-name");
    roleDescription = (XulTextbox) document.getElementById("role-description");
    availableUsers = (XulListbox) document.getElementById("available-users-list");
    assignedUsers = (XulListbox) document.getElementById("selected-users-list");
    assignUserToRoleButton = (XulButton) document.getElementById("assign-user-to-role");
    unassignUserFromRoleButton = (XulButton) document.getElementById("unassign-user-from-role");

    bf.setBindingType(Binding.Type.BI_DIRECTIONAL);
    bf.createBinding(securityRole, "name", rolename, "value");
    bf.createBinding(securityRole, "description", roleDescription, "value");
    bf.createBinding(securityRole, "assignedUsers", assignedUsers, "elements");
    bf.createBinding(securityRole, "availableUsers", availableUsers, "elements");
    
    bf.createBinding(securityRole, "availableSelectedUsers", availableUsers, "selectedItems", arrayToListUserConverter);
    bf.createBinding(securityRole, "assignedSelectedUsers", assignedUsers, "selectedItems", arrayToListUserConverter);

    bf.createBinding(security, "selectedRoleIndex", roleListBox, "selectedIndex");

    bf.setBindingType(Binding.Type.ONE_WAY);
    
    bf.createBinding(assignedUsers,  "selectedIndex", securityRole, "userUnassignmentPossible", accumulatorButtonConverter);
    bf.createBinding(availableUsers, "selectedIndex", securityRole, "userAssignmentPossible", accumulatorButtonConverter);
    
    bf.createBinding(securityRole, "userUnassignmentPossible", unassignUserFromRoleButton, "!disabled");
    bf.createBinding(securityRole, "userAssignmentPossible", assignUserToRoleButton, "!disabled");

    try {
      bf.setBindingType(Binding.Type.ONE_WAY);

      BindingConvertor<Integer, Boolean> buttonConverter = new BindingConvertor<Integer, Boolean>() {

        @Override
        public Boolean sourceToTarget(Integer value) {
          if (value != null && value >= 0) {
            return false;
          }
          return true;
        }

        @Override
        public Integer targetToSource(Boolean value) {
          // TODO Auto-generated method stub
          return null;
        }
      };
      bf.createBinding(roleListBox, "selectedIndex", roleEditButton, "disabled", buttonConverter);
      bf.createBinding(roleListBox, "selectedIndex", roleRemoveButton, "disabled", buttonConverter);
      bf.createBinding(userListBox, "selectedIndex", userEditButton, "disabled", buttonConverter);
      bf.createBinding(userListBox, "selectedIndex", userRemoveButton, "disabled", buttonConverter);

      bf.setBindingType(Binding.Type.ONE_WAY);
      bf.createBinding(userListBox, "selectedItem", security, "selectedUser");
      bf.createBinding(roleListBox, "selectedItem", security, "selectedRole");

      bf.createBinding(roleListBox, "selectedIndex", addUserToRoleButton, "disabled", buttonConverter);
      bf.createBinding(roleListBox, "selectedIndex", removeUserFromRoleButton, "disabled", buttonConverter);
      bf.createBinding(userListBox, "selectedIndex", addRoleToUserButton, "disabled", buttonConverter);
      bf.createBinding(userListBox, "selectedIndex", removeRoleFromUserButton, "disabled", buttonConverter);

      bf.createBinding(security, "roleList", roleListBox, "elements").fireSourceChanged();
      bf.createBinding(security, "userList", userListBox, "elements").fireSourceChanged();

      bf.createBinding(roleListBox, "selectedItem", security, "selectedRole");
      bf.createBinding(userListBox, "selectedItem", security, "selectedUser");
      
     
      roleDetailBinding = bf.createBinding(security, "selectedRole", roleDetailTable, "elements",
          new BindingConvertor<UIRepositoryRole, List<UIRepositoryUser>>() {

            @Override
            public List<UIRepositoryUser> sourceToTarget(UIRepositoryRole rr) {
              List<UIRepositoryUser> rusers = new ArrayList<UIRepositoryUser>();
              if (rr != null && rr.getUsers() != null) {
                List<UserInfo> users = new ArrayList<UserInfo>(rr.getUsers());
                for (UserInfo user : users) {
                  rusers.add(new UIRepositoryUser(user));
                }
              }
              return rusers;
            }

            @Override
            public UIRepositoryRole targetToSource(List<UIRepositoryUser> arg0) {
              // TODO Auto-generated method stub
              return null;
            }

          });
      userDetailBinding = bf.createBinding(security, "selectedUser", userDetailTable, "elements",
          new BindingConvertor<UIRepositoryUser, List<UIRepositoryRole>>() {

            @Override
            public List<UIRepositoryRole> sourceToTarget(UIRepositoryUser ru) {
              List<UIRepositoryRole> rroles = new ArrayList<UIRepositoryRole>();
              if (ru != null && ru.getRoles() != null) {
                List<RoleInfo> roles = new ArrayList<RoleInfo>(ru.getRoles());
                for (RoleInfo role : roles) {
                  rroles.add(new UIRepositoryRole(role));
                }
              }
              return rroles;
            }

            @Override
            public UIRepositoryUser targetToSource(List<UIRepositoryRole> arg0) {
              // TODO Auto-generated method stub
              return null;
            }

          });
      bf.createBinding(security, "selectedDeck", userRoleDeck, "selectedIndex",
          new BindingConvertor<ObjectRecipient.Type, Integer>() {

            @Override
            public Integer sourceToTarget(Type arg0) {
              if (arg0 == Type.ROLE) {
                roleRadioButton.setSelected(true);
                userRadioButton.setSelected(false);
                return 0;
              } else if (arg0 == Type.USER) {
                roleRadioButton.setSelected(false);
                userRadioButton.setSelected(true);
                return 1;
              } else
                return -1;
            }

            @Override
            public Type targetToSource(Integer arg0) {
              return null;
            }

          });
      BindingConvertor<Mode, Boolean> modeBindingConverter = new BindingConvertor<Mode, Boolean>() {

        @Override
        public Boolean sourceToTarget(Mode arg0) {
          if (arg0.equals(Mode.ADD)) {
            return false;
          }
          return true;
        }

        @Override
        public Mode targetToSource(Boolean arg0) {
          // TODO Auto-generated method stub
          return null;
        }

      };
      BindingConvertor<Mode, Boolean> anotherModeBindingConverter = new BindingConvertor<Mode, Boolean>() {

        @Override
        public Boolean sourceToTarget(Mode arg0) {
          if (arg0.equals(Mode.EDIT_MEMBER)) {
            return true;
          } else
            return false;
        }

        @Override
        public Mode targetToSource(Boolean arg0) {
          // TODO Auto-generated method stub
          return null;
        }

      };
      bf.createBinding(securityRole, "mode", rolename, "disabled", modeBindingConverter);
      bf.createBinding(securityRole, "mode", roleDescription, "disabled", anotherModeBindingConverter);

      bf.createBinding(securityUser, "mode", username, "disabled", modeBindingConverter);
      bf.createBinding(securityUser, "mode", userPassword, "disabled", anotherModeBindingConverter);
      bf.createBinding(securityUser, "mode", userDescription, "disabled", anotherModeBindingConverter);
    } catch (Exception e) {
      System.out.println(e.getMessage());
      e.printStackTrace();
    }
    changeToRoleDeck();
  }

  public void setBindingFactory(BindingFactory bf) {
    this.bf = bf;
  }

  public String getName() {
    return "securityController";
  }

  /*
  public void setCallback(RepositoryExplorerCallback callback) {
    this.callback = callback;
  }
  */

  public RepositoryUserInterface getRepositoryUserInterface() {
    return rui;
  }

  public void setRepositoryUserInterface(RepositoryUserInterface rui) {
    this.rui = rui;
  }

  public void assignUsersToRole() {
    securityRole.assignUsers(Arrays.asList(availableUsers.getSelectedItems()));
  }

  public void unassignUsersFromRole() {
    securityRole.unassignUsers(Arrays.asList(assignedUsers.getSelectedItems()));
  }

  public void assignRolesToUser() {
    securityUser.assignRoles(Arrays.asList(availableRoles.getSelectedItems()));
  }

  public void unassignRolesFromUser() {
    securityUser.unassignRoles(Arrays.asList(assignedRoles.getSelectedItems()));
  }

  public void showAddUserDialog() throws Exception {
    if (rui != null && rui.getRoles() != null) {
      securityUser.clear();
      securityUser.setAvailableRoles(convertToUIRoleModel(rui.getRoles()));
    }
    securityUser.setMode(Mode.ADD);
    userDialog.setTitle("Add User");
    userDialog.show();
  }

  public void cancelAddUserDialog() throws Exception {
    userDialog.hide();
  }

  public void cancelAddRoleDialog() throws Exception {
    roleDialog.hide();
  }


  private void addUser() throws Exception {
    if (rui != null) {
      UserInfo userInfo = new UserInfo();
      userInfo.setDescription(securityUser.getDescription());
      userInfo.setLogin(securityUser.getName());
      userInfo.setName(securityUser.getName());
      userInfo.setUsername(securityUser.getName());
      for (UIRepositoryRole role : securityUser.getAssignedRoles()) {
        userInfo.addRole(role.getRoleInfo());
      }
      try {
        rui.saveUserInfo(userInfo);
        security.addUser(new UIRepositoryUser(userInfo));
      } catch (KettleException ke) {
        ke.printStackTrace();
        // TODO Open the message dialog and display the error to the user
        // Make the decision to either close the dialog or leave the dialog open for the user for correct the error
      }
    }
    userDialog.hide();
  }

  public void showEditUserDialog() throws Exception {
    if (rui != null && rui.getRoles() != null) {
      securityUser.clear();
      securityUser.setUISecurityUser(security.getSelectedUser(), convertToUIRoleModel(rui.getRoles()));
      securityUser.setMode(Mode.EDIT);
      userDialog.setTitle("Edit User");
      userDialog.show();
    }
  }

  private void updateUser() throws Exception {
    if (rui != null) {
      UserInfo userInfo = new UserInfo();
      userInfo.setDescription(securityUser.getDescription());
      userInfo.setName(securityUser.getName());
      userInfo.setLogin(securityUser.getName());
      userInfo.setPassword(securityUser.getPassword());
      userInfo.setRoles(convertToDomainRoleModel(securityUser.getAssignedRoles()));
      try {
        rui.updateUser(userInfo); // TODO Change the call to update user
        security.updateUser(new UIRepositoryUser(userInfo));
        userDetailTable.update();
      } catch (KettleException ke) {
        ke.printStackTrace();
        // TODO Open the message dialog and display the error to the user
        // Make the decision to either close the dialog or leave the dialog open for the user for correct the error
      }
    }
    userDialog.hide();
  }

  public void showAddRoleDialog() throws Exception {
    if (rui != null && rui.getUsers() != null) {
      securityRole.clear();
      securityRole.setAvailableUsers(convertToUIUserModel(rui.getUsers()));
    }
    roleDialog.setTitle("Add Role");
    roleDialog.show();
  }

  public void showAddUserToRoleDialog() throws Exception {
    if (rui != null && rui.getUsers() != null) {
      securityRole.clear();
      securityRole.setUISecurityRole(security.getSelectedRole(), convertToUIUserModel(rui.getUsers()));
      securityRole.setMode(Mode.EDIT_MEMBER);
    }
    roleDialog.setTitle("Add User To Role");
    roleDialog.show();
  }

  public void showRemoveUserConfirmationDialog() {
    removeUserConfirmationDialog.show();
  }

  public void showRemoveRoleConfirmationDialog() {
    removeRoleConfirmationDialog.show();
  }

  public void closeRemoveUserConfirmationDialog() {
    removeUserConfirmationDialog.hide();
  }

  public void closeRemoveRoleConfirmationDialog() {
    removeRoleConfirmationDialog.hide();
  }

  public void showAddRoleToUserDialog() throws Exception {
    if (rui != null && rui.getRoles() != null) {
      securityUser.clear();
      securityUser.setUISecurityUser(security.getSelectedUser(), convertToUIRoleModel(rui.getRoles()));
      securityUser.setMode(Mode.EDIT_MEMBER);
      userDialog.setTitle("Add Role To User");
      userDialog.show();
    }
  }

  public void removeRolesFromUser() throws Exception {
    security.removeRolesFromSelectedUser(userDetailTable.getSelectedItems());
    rui.updateUser(security.getSelectedUser().getUserInfo());
  }

  public void removeUsersFromRole() throws Exception {
    security.removeUsersFromSelectedRole(roleDetailTable.getSelectedItems());
    rui.updateRole(security.getSelectedRole().getRoleInfo());
  }

  private void addRole() throws Exception {
    if (rui != null) {
      RoleInfo roleInfo = new RoleInfo();
      roleInfo.setDescription(securityRole.getDescription());
      roleInfo.setName(securityRole.getName());
      for (UIRepositoryUser user : securityRole.getAssignedUsers()) {
        roleInfo.addUser(user.getUserInfo());
      }
      try {
        rui.createRole(roleInfo);
        security.addRole(new UIRepositoryRole(roleInfo));
      } catch (KettleException ke) {
        ke.printStackTrace();
        // TODO Open the message dialog and display the error to the user
        // Make the decision to either close the dialog or leave the dialog open for the user for correct the error
      }
    }
    roleDialog.hide();
  }

  private void updateRole() throws Exception {
    if (rui != null) {
      RoleInfo roleInfo = new RoleInfo();
      roleInfo.setDescription(securityRole.getDescription());
      roleInfo.setName(securityRole.getName());
      roleInfo.setUsers(convertToDomainUserModel(securityRole.getAssignedUsers()));
      try {
        rui.updateRole(roleInfo);
        security.updateRole(new UIRepositoryRole(roleInfo));
        roleDetailTable.update();
      } catch (KettleException ke) {
        ke.printStackTrace();
        // TODO Open the message dialog and display the error to the user
        // Make the decision to either close the dialog or leave the dialog open for the user for correct the error
      }

    }
    roleDialog.hide();
  }

  public void removeRole() throws Exception {
    if (rui != null) {
      if (security != null && security.getSelectedRole() != null) {
        try {
          rui.deleteRole(security.getSelectedRole().getName());
          security.removeRole(security.getSelectedRole().getName());
          removeRoleConfirmationDialog.hide();
        } catch (KettleException ke) {
          ke.printStackTrace();
          // TODO Open the message dialog and display the error to the user
          // Make the decision to either close the dialog or leave the dialog open for the user for correct the error
        }

      }
      // TODO Throw exception of display error to the user that there was not selected role to be removed
    }
  }

  public void showEditRoleDialog() throws Exception {
    if (rui != null && rui.getUsers() != null) {
      securityRole.clear();
      securityRole.setUISecurityRole(security.getSelectedRole(), convertToUIUserModel(rui.getUsers()));
      securityRole.setMode(Mode.EDIT);
      roleDialog.setTitle("Edit Role");
      roleDialog.show();
    }
  }

  public void removeUser() throws Exception {
    if (rui != null) {
      try {
        rui.delUser(security.getSelectedUser().getName());
        security.removeUser(security.getSelectedUser().getName());
        removeUserConfirmationDialog.hide();
      } catch (KettleException ke) {
        ke.printStackTrace();
        // TODO Open the message dialog and display the error to the user
        // Make the decision to either close the dialog or leave the dialog open for the user for correct the error
      }
    }
  }
  public void changeToRoleDeck() {
    security.setSelectedDeck(ObjectRecipient.Type.ROLE);
  }

  public void changeToUserDeck() {
    security.setSelectedDeck(ObjectRecipient.Type.USER);
  }

  private List<UIRepositoryRole> convertToUIRoleModel(List<RoleInfo> roles) {
    List<UIRepositoryRole> rroles = new ArrayList<UIRepositoryRole>();
    for (RoleInfo role : roles) {
      rroles.add(new UIRepositoryRole(role));
    }
    return rroles;
  }

  private List<UIRepositoryUser> convertToUIUserModel(List<UserInfo> users) {
    List<UIRepositoryUser> rusers = new ArrayList<UIRepositoryUser>();
    for (UserInfo user : users) {
      rusers.add(new UIRepositoryUser(user));
    }
    return rusers;
  }

  private Set<UserInfo> convertToDomainUserModel(List<UIRepositoryUser> rusers) {
    Set<UserInfo> userSet = new HashSet<UserInfo>();
    for (UIRepositoryUser ru : rusers) {
      userSet.add(ru.getUserInfo());
    }
    return userSet;
  }

  private Set<RoleInfo> convertToDomainRoleModel(List<UIRepositoryRole> rrole) {
    Set<RoleInfo> roleSet = new HashSet<RoleInfo>();
    for (UIRepositoryRole rr : rrole) {
      roleSet.add(rr.getRoleInfo());
    }
    return roleSet;
  }

  public void saveUser() throws Exception {
    if(securityUser.getMode().equals(Mode.ADD)) {
      addUser();
    } else {
      updateUser();
    }
  }

  public void saveRole() throws Exception{
    if(securityRole.getMode().equals(Mode.ADD)) {
      addRole();
    } else {
      updateRole();
    } 
  }
}
