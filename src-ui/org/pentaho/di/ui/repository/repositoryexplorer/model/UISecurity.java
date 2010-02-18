package org.pentaho.di.ui.repository.repositoryexplorer.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.pentaho.di.repository.IRole;
import org.pentaho.di.repository.RepositorySecurityManager;
import org.pentaho.di.repository.UserInfo;
import org.pentaho.di.repository.ObjectRecipient.Type;
import org.pentaho.ui.xul.XulEventSourceAdapter;

public class UISecurity extends XulEventSourceAdapter {

  public static enum Mode {
    ADD, EDIT, EDIT_MEMBER
  };

  private Type selectedDeck;

  protected UIRepositoryUser selectedUser;

  protected IUIRole selectedRole;

  private int selectedUserIndex;

  private int selectedRoleIndex;

  protected List<UIRepositoryUser> userList;

  protected List<IUIRole> roleList;

  public UISecurity() {
    userList = new ArrayList<UIRepositoryUser>();
    roleList = new ArrayList<IUIRole>();
  }

  public UISecurity(RepositorySecurityManager rsm) throws Exception {
    this();
    if (rsm != null && rsm.getUsers() != null) {
      for (UserInfo user : rsm.getUsers()) {
        userList.add(new UIRepositoryUser(user));
      }
      this.firePropertyChange("userList", null, userList); //$NON-NLS-1$
      for (IRole role : rsm.getRoles()) {
        IUIRole newRole = UIObjectRegistery.getInstance().constructUIRepositoryRole(role);
        roleList.add(newRole);
      }
      this.firePropertyChange("roleList", null, roleList); //$NON-NLS-1$
    }
  }

  public Type getSelectedDeck() {
    return selectedDeck;
  }

  public void setSelectedDeck(Type selectedDeck) {
    this.selectedDeck = selectedDeck;
    this.firePropertyChange("selectedDeck", null, selectedDeck); //$NON-NLS-1$
  }

  public int getSelectedUserIndex() {
    return selectedUserIndex;
  }

  public void setSelectedUserIndex(int selectedUserIndex) {
    this.selectedUserIndex = selectedUserIndex;
    this.firePropertyChange("selectedUserIndex", null, selectedUserIndex); //$NON-NLS-1$
  }

  public int getSelectedRoleIndex() {
    return selectedRoleIndex;
  }

  public void setSelectedRoleIndex(int selectedRoleIndex) {
    this.selectedRoleIndex = selectedRoleIndex;
    this.firePropertyChange("selectedRoleIndex", null, selectedRoleIndex); //$NON-NLS-1$
  }

  public UIRepositoryUser getSelectedUser() {
    return selectedUser;
  }

  public void setSelectedUser(UIRepositoryUser selectedUser) {
    this.selectedUser = selectedUser;
    this.firePropertyChange("selectedUser", null, selectedUser); //$NON-NLS-1$
    setSelectedUserIndex(getIndexOfUser(selectedUser));
  }

  public IUIRole getSelectedRole() {
    return selectedRole;
  }

  public void setSelectedRole(IUIRole selectedRole) {
    this.selectedRole = selectedRole;
    this.firePropertyChange("selectedRole", null, selectedRole); //$NON-NLS-1$
    setSelectedRoleIndex(getIndexOfRole(selectedRole));
  }

  public List<UIRepositoryUser> getUserList() {
    return userList;
  }

  public void setUserList(List<UIRepositoryUser> userList) {
    this.userList.clear();
    this.userList.addAll(userList);
    this.firePropertyChange("userList", null, userList); //$NON-NLS-1$
  }

  public List<IUIRole> getRoleList() {
    return roleList;
  }

  public void setRoleList(List<IUIRole> roleList) {
    this.roleList.clear();
    this.roleList.addAll(roleList);
    this.firePropertyChange("roleList", null, roleList); //$NON-NLS-1$		
  }

  public void addRole(IUIRole roleToAdd) {
    roleList.add(roleToAdd);
    this.firePropertyChange("roleList", null, roleList); //$NON-NLS-1$
    // We need to make sure this new role is added to all users as well
    for (UIRepositoryUser userInfo : roleToAdd.getUsers()) {
      assignRoleToUser(userInfo, roleToAdd);
    }
    setSelectedRole(roleToAdd);
  }

  public void updateUser(UIRepositoryUser userToUpdate, Set<IUIRole> previousRoleList) {
    UIRepositoryUser user = getUser(userToUpdate.getName());
    user.setDescription(userToUpdate.getDescription());
    user.setRoles(userToUpdate.getRoles());
    this.firePropertyChange("userList", null, userList); //$NON-NLS-1$
    /*
     *  Now update this current user to all the roles. So if any roles were added
     *  to this user then add the user to the roles as well. Similarly if some roles
     *  were remove from this user, remove the user from those roles.  
     */
    updateUserInRoles(userToUpdate, previousRoleList, userToUpdate.getRoles());
    setSelectedUser(user);
  }

  public void updateRole(IUIRole roleToUpdate, Set<UIRepositoryUser> previousUserList) {
    IUIRole role = getRole(roleToUpdate.getName());
    role.setDescription(roleToUpdate.getDescription());
    role.setUsers(roleToUpdate.getUsers());
    this.firePropertyChange("roleList", null, roleList); //$NON-NLS-1$
    /*
     *  Now update this current role to all the users. So if any users were added
     *  to this role then add the role to the users as well. Similarly if some users
     *  were remove from this role, remove the role from those users.  
     */
    updateRoleInUsers(roleToUpdate, previousUserList, roleToUpdate.getUsers());
    setSelectedRole(role);
  }

  public void removeRole(String name) {
    removeRole(getRole(name));
  }

  public void removeRole(IUIRole roleToRemove) {
    int index = getIndexOfRole(roleToRemove);
    roleList.remove(roleToRemove);
    this.firePropertyChange("roleList", null, roleList); //$NON-NLS-1$
    // We need to make sure this new role is added to all users as well
    for (UIRepositoryUser userInfo : roleToRemove.getUsers()) {
      unassignRoleFromUser(userInfo, roleToRemove);
    }
    if (index - 1 >= 0) {
      setSelectedRole(getRoleAtIndex(index - 1));
    }

  }

  public void addUser(UIRepositoryUser userToAdd) {
    userList.add(userToAdd);
    this.firePropertyChange("userList", null, userList); //$NON-NLS-1$
    // We need to make sure this recently removed user is removed from all roles as well
    for (IUIRole role : userToAdd.getRoles()) {
      assignUserToRole(role, userToAdd);
    }
    setSelectedUser(userToAdd);
  }

  public void removeUser(String name) {
    removeUser(getUser(name));
  }

  public void removeUser(UIRepositoryUser userToRemove) {
    int index = getIndexOfUser(userToRemove);
    userList.remove(userToRemove);
    this.firePropertyChange("userList", null, userList); //$NON-NLS-1$
    // We need to make sure this recently removed user is removed to all roles as well
    for (IUIRole role : userToRemove.getRoles()) {
      unassignUserFromRole(role, userToRemove);
    }
    if (index - 1 >= 0) {
      setSelectedUser(getUserAtIndex(index - 1));
    }
  }

  private void updateUserInRoles(UIRepositoryUser user, Set<IUIRole> userRolesBeforeUpdate, Set<IUIRole> userRolesAfterUpdate) {
    // unassign user from the roles which were unassigned 
    for (IUIRole role : userRolesBeforeUpdate) {
      if (!exist(role, userRolesAfterUpdate)) {
        unassignUserFromRole(role, user);
      }
    }
    // assign user to the roles which were assigned   
    for (IUIRole role : userRolesAfterUpdate) {
      if (!exist(role, userRolesBeforeUpdate)) {
        assignUserToRole(role, user);
      }
    }
  }

  private boolean exist(IUIRole role, Set<IUIRole> roleSet) {
    for (IUIRole roleInfo : roleSet) {
      if (role.getName().equals(roleInfo.getName())) {
        return true;
      }
    }
    return false;
  }

  private void updateRoleInUsers(IUIRole role, Set<UIRepositoryUser> roleUsersBeforeUpdate, Set<UIRepositoryUser> roleUsersAfterUpdate) {
    // unassign user from the roles which were unassigned 
    for (UIRepositoryUser userInfo : roleUsersBeforeUpdate) {
      if (!exist(userInfo, roleUsersAfterUpdate)) {
        unassignRoleFromUser(userInfo, role);
      }
    }
    // assign user to the roles which were assigned   
    for (UIRepositoryUser userInfo : roleUsersAfterUpdate) {
      if (!exist(userInfo, roleUsersBeforeUpdate)) {
        assignRoleToUser(userInfo, role);
      }
    }
  }

  private boolean exist(UIRepositoryUser ruser, Set<UIRepositoryUser> users) {
    for (UIRepositoryUser user : users) {
      if (user.getName().equals(ruser.getName())) {
        return true;
      }
    }
    return false;
  }

  private IUIRole getRole(String name) {
    for (IUIRole role : roleList) {
      if (role.getName().equals(name)) {
        return role;
      }
    }
    return null;
  }

  private UIRepositoryUser getUser(String name) {
    for (UIRepositoryUser user : userList) {
      if (user.getName().equals(name)) {
        return user;
      }
    }
    return null;
  }

  public void removeRolesFromSelectedUser(Collection<Object> roles) {
    for (Object o : roles) {
      UIRepositoryRole role = (UIRepositoryRole) o;
      removeRoleFromSelectedUser(role.getName());
    }
    this.firePropertyChange("selectedUser", null, selectedUser); //$NON-NLS-1$
  }

  private void removeRoleFromSelectedUser(String roleName) {
    IUIRole role = findRoleInSelectedUser(roleName);
    selectedUser.removeRole(role);
    unassignUserFromRole(role, selectedUser);
  }

  public void removeUsersFromSelectedRole(Collection<Object> users) {
    for (Object o : users) {
      UIRepositoryUser user = (UIRepositoryUser) o;
      removeUserFromSelectedRole(user.getName());
    }
    this.firePropertyChange("selectedRole", null, selectedRole); //$NON-NLS-1$
  }

  private void removeUserFromSelectedRole(String userName) {
    UIRepositoryUser user = findUserInSelectedRole(userName);
    selectedRole.removeUser(user);
    unassignRoleFromUser(user, selectedRole);
  }

  private IUIRole findRoleInSelectedUser(String roleName) {
    Set<IUIRole> roles = selectedUser.getRoles();
    for (IUIRole role : roles) {
      if (role.getName().equals(roleName)) {
        return role;
      }
    }
    return null;
  }

  private UIRepositoryUser findUserInSelectedRole(String userName) {
    Set<UIRepositoryUser> users = selectedRole.getUsers();
    for (UIRepositoryUser user : users) {
      if (user.getName().equals(userName)) {
        return user;
      }
    }
    return null;
  }

  private UIRepositoryUser getUserAtIndex(int index) {
    return this.userList.get(index);
  }

  private int getIndexOfUser(UIRepositoryUser ru) {
    for (int i = 0; i < this.userList.size(); i++) {
      UIRepositoryUser user = this.userList.get(i);
      if (ru.getName().equals(user.getName())) {
        return i;
      }
    }
    return -1;
  }

  private IUIRole getRoleAtIndex(int index) {
    return this.roleList.get(index);
  }

  protected int getIndexOfRole(IUIRole rr) {
    for (int i = 0; i < this.roleList.size(); i++) {
      IUIRole role = this.roleList.get(i);
      if (rr.getName().equals(role.getName())) {
        return i;
      }
    }
    return -1;
  }

  private void assignRoleToUser(UIRepositoryUser userInfo2, IUIRole role) {
    UIRepositoryUser userInfo = findUser(userInfo2);
    if(userInfo != null) {
      userInfo.addRole(role);
    }
  }

  private void unassignRoleFromUser(UIRepositoryUser user, IUIRole role) {
    UIRepositoryUser userInfo = findUser(user);
    if(userInfo != null) {
      userInfo.removeRole(role);
    }
  }

  private void assignUserToRole(IUIRole role, UIRepositoryUser user) {
    IUIRole roleInfo = findRole(role);
    if(roleInfo != null) {
      roleInfo.addUser(user);
    }
  }

  private void unassignUserFromRole(IUIRole role, UIRepositoryUser user) {
    IUIRole roleInfo = findRole(role);
    if(roleInfo != null) {
      roleInfo.removeUser(user);  
    }
  }
 private UIRepositoryUser findUser(UIRepositoryUser userInfo) {
   for(UIRepositoryUser user:userList) {
     if(user.getName().equals(userInfo.getName())) {
       return user;
     }
   }
   return null;
 }
 
 private IUIRole findRole(IUIRole role) {
   for(IUIRole roleInfo:roleList) {
     if(roleInfo.getName().equals(role.getName())) {
       return roleInfo;
     }
   }
   return null;   
 }
}
