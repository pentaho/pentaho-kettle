package org.pentaho.di.ui.repository.repositoryexplorer.model;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.di.repository.ObjectAce;
import org.pentaho.di.repository.ObjectRecipient;
import org.pentaho.di.repository.RepositoryObjectAce;
import org.pentaho.di.repository.RepositoryObjectRecipient;
import org.pentaho.di.repository.RoleInfo;
import org.pentaho.di.repository.UserInfo;
import org.pentaho.di.repository.ObjectRecipient.Type;
import org.pentaho.ui.xul.XulEventSourceAdapter;

public class UIRepositoryObjectAclModel extends XulEventSourceAdapter{
  private List<UIRepositoryUser> masterAvailableUserList;
  private List<UIRepositoryRole> masterAvailableRoleList;
  private UIRepositoryObjectAcls selectedAcls;
  private List<UIRepositoryRole> selectedAvailableRoles = new ArrayList<UIRepositoryRole>();
  private List<UIRepositoryUser> selectedAvailableUsers = new ArrayList<UIRepositoryUser>();
  private List<UIRepositoryObjectAcl> selectedAssignedRoles = new ArrayList<UIRepositoryObjectAcl>();
  private List<UIRepositoryObjectAcl> selectedAssignedUsers= new ArrayList<UIRepositoryObjectAcl>();
  private List<UIRepositoryObjectAcl> aclsToAdd;
  private List<UIRepositoryObjectAcl> aclsToRemove;
  private List<UIRepositoryUser> availableUserList;
  private List<UIRepositoryRole> availableRoleList;
  private boolean userAssignmentPossible;
  private boolean userUnassignmentPossible;
  private boolean roleAssignmentPossible;
  private boolean roleUnassignmentPossible;

  public UIRepositoryObjectAclModel() {
    availableUserList = new ArrayList<UIRepositoryUser>();
    availableRoleList = new ArrayList<UIRepositoryRole>();
    masterAvailableUserList = new ArrayList<UIRepositoryUser>();
    masterAvailableRoleList = new ArrayList<UIRepositoryRole>();
    aclsToAdd = new ArrayList<UIRepositoryObjectAcl>(); 
    aclsToRemove = new ArrayList<UIRepositoryObjectAcl>();
    selectedAcls = new UIRepositoryObjectAcls(); 
  }

  public List<UIRepositoryObjectAcl> getAclsToAdd() {
    return aclsToAdd;
  }

  public void setAclsToRemove(List<UIRepositoryObjectAcl> aclsToRemove) {
    this.aclsToRemove = aclsToRemove;
  }

  public List<UIRepositoryRole> getSelectedAvailableRoles() {
    return selectedAvailableRoles;
  }
  public void setSelectedAvailableRoles(List<Object> selectedAvailableRoles) {
    List<UIRepositoryRole> previousVal = new ArrayList<UIRepositoryRole>();
    previousVal.addAll(this.selectedAvailableRoles);
    this.selectedAvailableRoles.clear();
    for(Object role:selectedAvailableRoles) {
      if(role instanceof UIRepositoryRole) {
        this.selectedAvailableRoles.add((UIRepositoryRole) role);
      } else {
        UIRepositoryObjectAcl acl = (UIRepositoryObjectAcl) role;
        this.selectedAvailableRoles.add(getRole(acl.getRecipientName()));
      }
    }
    this.firePropertyChange("selectedAvailableRoles", previousVal, this.selectedAvailableRoles); //$NON-NLS-1$
  }
  public void setSelectedAvailableRole(UIRepositoryRole selectedAvailableRole) {
    this.selectedAvailableRoles.add(selectedAvailableRole);
  }

  public List<UIRepositoryUser> getSelectedAvailableUsers() {
    return selectedAvailableUsers;
  }
  public void setSelectedAvailableUsers(List<Object> selectedAvailableUsers) {
    List<UIRepositoryUser> previousVal = new ArrayList<UIRepositoryUser>();
    previousVal.addAll(this.selectedAvailableUsers);
    this.selectedAvailableUsers.clear();
    for(Object user:selectedAvailableUsers) {
      if(user instanceof UIRepositoryUser) {
        this.selectedAvailableUsers.add((UIRepositoryUser) user);
      } else {
        UIRepositoryObjectAcl acl = (UIRepositoryObjectAcl) user;
        this.selectedAvailableUsers.add(getUser(acl.getRecipientName()));
      }
    }
    this.firePropertyChange("selectedAvailableUsers", previousVal, this.selectedAvailableUsers); //$NON-NLS-1$
  }
  public void setSelectedAvailableUser(UIRepositoryUser selectedAvailableUser) {
    this.selectedAvailableUsers.add(selectedAvailableUser);
  }
  
  public List<UIRepositoryObjectAcl> getSelectedAssignedRoles() {
    return selectedAssignedRoles;
  }
  public void setSelectedAssignedRoles(List<Object> selectedAssignedRoles) {
    List<UIRepositoryObjectAcl> previousVal = new ArrayList<UIRepositoryObjectAcl>();
    previousVal.addAll(this.selectedAssignedRoles);
    this.selectedAssignedRoles.clear();
    for(Object role:selectedAssignedRoles) {
      if(role instanceof UIRepositoryRole) {
        this.selectedAssignedRoles.add(createAclFromRole((UIRepositoryRole) role));  
      } else {
        this.selectedAssignedRoles.add( (UIRepositoryObjectAcl) role);
      }      
    }
    this.firePropertyChange("selectedAssignedRoles", previousVal, this.selectedAssignedRoles); //$NON-NLS-1$    
  }
  public List<UIRepositoryObjectAcl> getSelectedAssignedUsers() {
    return selectedAssignedUsers;
  }
  public void setSelectedAssignedUsers(List<Object> selectedAssignedUsers) {
    List<UIRepositoryObjectAcl> previousVal = new ArrayList<UIRepositoryObjectAcl>();
    previousVal.addAll(this.selectedAssignedUsers);
    this.selectedAssignedUsers.clear();
    for(Object user:selectedAssignedUsers) {
      if(user instanceof UIRepositoryUser) {
        this.selectedAssignedUsers.add(createAclFromUser((UIRepositoryUser) user));
      } else {
        this.selectedAssignedUsers.add( (UIRepositoryObjectAcl) user);
      }
    }
    this.firePropertyChange("selectedAssignedUsers", previousVal, this.selectedAssignedUsers); //$NON-NLS-1$    
  }
  public void setSelectedAssignedUser(UIRepositoryObjectAcl selectedAssignedUser) {
    this.selectedAssignedUsers.add(selectedAssignedUser);
  }

  public List<UIRepositoryUser> getAvailableUserList() {
    return availableUserList;
  }

  public boolean isUserAssignmentPossible() {
    return userAssignmentPossible;
  }

  public void setUserAssignmentPossible(boolean userAssignmentPossible) {
    this.userAssignmentPossible = userAssignmentPossible;
    if(userAssignmentPossible && availableUserList.size() > 0) {
      this.firePropertyChange("userAssignmentPossible", null, true); //$NON-NLS-1$
    } else {
      this.firePropertyChange("userAssignmentPossible", null, false); //$NON-NLS-1$
    }
  }

  public boolean isUserUnassignmentPossible() {
    return userUnassignmentPossible;
  }

  public void setUserUnassignmentPossible(boolean userUnassignmentPossible) {
    this.userUnassignmentPossible = userUnassignmentPossible;
    if(userUnassignmentPossible && getSelectedUserList().size() > 0) {
      this.firePropertyChange("userUnassignmentPossible", null, true); //$NON-NLS-1$
    } else {
      this.firePropertyChange("userUnassignmentPossible", null, false); //$NON-NLS-1$
    }
  }

  public boolean isRoleAssignmentPossible() {
    return roleAssignmentPossible;
  }

  public void setRoleAssignmentPossible(boolean roleAssignmentPossible) {
    this.roleAssignmentPossible = roleAssignmentPossible;
    if(roleAssignmentPossible && availableRoleList.size() > 0) {
      this.firePropertyChange("roleAssignmentPossible", null, true); //$NON-NLS-1$
    } else {
      this.firePropertyChange("roleAssignmentPossible", null, false); //$NON-NLS-1$
    }
  }

  public boolean isRoleUnassignmentPossible() {
    return roleUnassignmentPossible;
  }

  public void setRoleUnassignmentPossible(boolean roleUnassignmentPossible) {
    this.roleUnassignmentPossible = roleUnassignmentPossible;
    if(roleUnassignmentPossible && getSelectedRoleList().size() > 0) {
      this.firePropertyChange("roleUnassignmentPossible", null, true); //$NON-NLS-1$
    } else {
      this.firePropertyChange("roleUnassignmentPossible", null, false); //$NON-NLS-1$
    }
  }

  public void setUserList(List<UserInfo> userList,List<RoleInfo> roleList) {
    List<UIRepositoryUser> users = new ArrayList<UIRepositoryUser>();
    List<UIRepositoryRole> roles = new ArrayList<UIRepositoryRole>();
    if(userList != null && userList.size() > 0) {
      for(UserInfo userInfo:userList) {
        users.add(new UIRepositoryUser(userInfo));
      }
      setAvailableUserList(users);
      setSelectedAvailableUser( new UIRepositoryUser(userList.get(0)));
      masterAvailableUserList.addAll(availableUserList);

    }
    if(roleList != null && roleList.size() > 0) {   
      for(RoleInfo roleInfo:roleList) {
        roles.add(new UIRepositoryRole(roleInfo));
      }
      setAvailableRoleList(roles);
      setSelectedAvailableRole(new UIRepositoryRole(roleList.get(0)));
      masterAvailableRoleList.addAll(availableRoleList);
    }
    setSelectedAcls(selectedAcls);
  }
  public void setAvailableUserList(List<UIRepositoryUser> userList) {
    List<UIRepositoryUser> previousVal = new ArrayList<UIRepositoryUser>();
    previousVal.addAll(this.availableUserList);
    this.availableUserList.clear();
    this.availableUserList.addAll(userList);
    this.firePropertyChange("availableUserList", previousVal, availableUserList); //$NON-NLS-1$
  }

  public List<UIRepositoryRole> getAvailableRoleList() {
    return availableRoleList;
  }

  public void setAvailableRoleList(List<UIRepositoryRole> roleList) {
    List<UIRepositoryRole> previousVal = new ArrayList<UIRepositoryRole>();
    previousVal.addAll(this.availableRoleList);
    this.availableRoleList.clear();
    this.availableRoleList.addAll(roleList);
    this.firePropertyChange("availableRoleList", previousVal, availableRoleList); //$NON-NLS-1$
  }
  public void assignRoles(List<Object> rolesToAssign) {
    for(Object role:rolesToAssign) {
      UIRepositoryRole roleToAssign = (UIRepositoryRole) role;
      assignRole(roleToAssign);
    }
    this.firePropertyChange("selectedRoleList", null, getSelectedRoleList()); //$NON-NLS-1$
    setSelectedAssignedRoles(rolesToAssign);
    setSelectedAvailableRoles(new ArrayList<Object>());
  }
  public void assignRole(UIRepositoryRole roleToAssign) {
    aclsToAdd.add(createAclFromRole(roleToAssign));
    removeFromAvailableRoles(roleToAssign.getName());
  }

  public UIRepositoryObjectAcl getAcl(String aclName) {
    for(UIRepositoryObjectAcl selected:this.selectedAcls.getAcls()) {
      if(selected.getRecipientName().equals(aclName)){
        return selected;
      }
    }
    for(UIRepositoryObjectAcl selected:aclsToAdd) {
      if(selected.getRecipientName().equals(aclName)){
        return selected;
      }
    }
    return null;
  }
  public void unassign(List<Object> toUnassign) {
    for(Object o:toUnassign) {
      UIRepositoryObjectAcl acl = (UIRepositoryObjectAcl) o;
      unassign(acl.getRecipientName());
    }
    setSelectedAvailableRoles(toUnassign);
    setSelectedAssignedRoles(new ArrayList<Object>());
  }
  public void unassign(String toUnassign) {
    UIRepositoryObjectAcl selectedAcl = getAcl(toUnassign);
    if(selectedAcl != null) {
      unassign(selectedAcl);      
      if(selectedAcl.getRecipientType() == ObjectRecipient.Type.USER) {
        addToAvailableUsers(selectedAcl.getRecipientName());
      } else if(selectedAcl.getRecipientType() == ObjectRecipient.Type.ROLE) {
        addToAvailableRoles(selectedAcl.getRecipientName());
      }
    }
  }

  public void unassign(UIRepositoryObjectAcl toUnassign) {    
    if(aclsToAdd.contains(toUnassign)) {
      aclsToAdd.remove(toUnassign);
    } else {
      aclsToRemove.add(toUnassign);
    }
    if(toUnassign.getRecipientType() == ObjectRecipient.Type.ROLE) {
      this.firePropertyChange("selectedRoleList", null, getSelectedRoleList()); //$NON-NLS-1$
    } else if(toUnassign.getRecipientType() == ObjectRecipient.Type.USER) {
      this.firePropertyChange("selectedUserList", null, getSelectedUserList()); //$NON-NLS-1$
    }
    
  }

  public void assignUsers(List<Object> usersToAssign) {
    List<UIRepositoryObjectAcl> previousVal = new ArrayList<UIRepositoryObjectAcl>();
    previousVal.addAll(getSelectedUserList());

    for(Object user:usersToAssign) {
      UIRepositoryUser userToAssign = (UIRepositoryUser) user;
      assignUser(userToAssign);
    }
    this.firePropertyChange("selectedUserList", null, getSelectedUserList()); //$NON-NLS-1$
    setSelectedAssignedUsers(usersToAssign);
    setSelectedAvailableUsers(new ArrayList<Object>());
  }

  public void assignUser(UIRepositoryUser userToAssign) {
    ObjectAce ace = new RepositoryObjectAce(
        new RepositoryObjectRecipient(userToAssign.getName(), ObjectRecipient.Type.USER));
    aclsToAdd.add(new UIRepositoryObjectAcl(ace));    
    removeFromAvailableUsers(userToAssign.getName());   
  }

  private void removeFromAvailableAcls(UIRepositoryObjectAcl acl) {
    if(acl.getRecipientType().equals(Type.ROLE)) {
      removeFromAvailableRoles(acl.getRecipientName());
    } else if(acl.getRecipientType().equals(Type.USER)) {
      removeFromAvailableUsers(acl.getRecipientName());
    }
  }
  public UIRepositoryObjectAcls getSelectedAcls() {
    return selectedAcls;
  }
  
  public void setSelectedAcls(UIRepositoryObjectAcls selectedAcls) {
    this.selectedAcls = selectedAcls;
    for(UIRepositoryObjectAcl acl:this.selectedAcls.getAcls()) {
      removeFromAvailableAcls(acl); 
    }
    this.firePropertyChange("selectedUserList", null, getSelectedUserList()); //$NON-NLS-1$   
    this.firePropertyChange("selectedRoleList", null, getSelectedRoleList()); //$NON-NLS-1$   
  }
  
  public List<UIRepositoryObjectAcl> getSelectedUserList() {
    if(selectedAcls != null && selectedAcls.getAcls() != null) {
      List<UIRepositoryObjectAcl> selectedUserList = new ArrayList<UIRepositoryObjectAcl>();
      // Add all the acls from the initial list and remove the one that are marked for removal
      for(UIRepositoryObjectAcl selectedAcl:selectedAcls.getAcls()) {
        if(!findByRecipientName(selectedAcl.getRecipientName())) {
          if( selectedAcl.getRecipientType().equals(ObjectRecipient.Type.USER)) {
          selectedUserList.add(selectedAcl);
          }
        }
      }
      // Now add the acls that are marked for addition
      for(UIRepositoryObjectAcl acl:aclsToAdd) {
        if( acl.getRecipientType().equals(ObjectRecipient.Type.USER)) {
          selectedUserList.add(acl);
        }
      }
        return selectedUserList;
    } else {
      return null;
    }
    
  }
  
  public List<UIRepositoryObjectAcl> getSelectedRoleList() {
    if(selectedAcls != null && selectedAcls.getAcls() != null) {
      List<UIRepositoryObjectAcl> selectedRoleList = new ArrayList<UIRepositoryObjectAcl>();
      // Add all the acls from the initial list and remove the one that are marked for removal      
      for(UIRepositoryObjectAcl selectedAcl:this.selectedAcls.getAcls()) {
        if(!findByRecipientName(selectedAcl.getRecipientName())) {        
          if( selectedAcl.getRecipientType().equals(ObjectRecipient.Type.ROLE)) {
            selectedRoleList.add(selectedAcl);
          }
        }
      }
      // Now add the acls that are marked for addition
      for(UIRepositoryObjectAcl acl:aclsToAdd) {
        if( acl.getRecipientType().equals(ObjectRecipient.Type.ROLE)) {
          selectedRoleList.add(acl);
        }
      }
      return selectedRoleList;
    } else {
      return null;
    }   
  }
  
  public UIRepositoryUser getAvailableUser(int index) {
    return availableUserList.get(index);
  }

  public int getAvailableUserIndex(UIRepositoryUser user) {
    for(int i=0;i<availableUserList.size();i++) {
      UIRepositoryUser u = availableUserList.get(i);
      if(u.getName().equals(user.getName())) {
        return i;
      }
    }
    return -1; 
  }

  public UIRepositoryRole getAvailableRole(int index) {
    return availableRoleList.get(index);
  }

  public int getAvailableRoleIndex(UIRepositoryRole role) {
    for(int i=0;i<availableRoleList.size();i++) {
      UIRepositoryRole r = availableRoleList.get(i);
      if(r.getName().equals(role.getName())) {
        return i;
      }
    }
    return -1; 
  }

  private UIRepositoryObjectAcl createAclFromRole(UIRepositoryRole role) {
    ObjectAce ace = new RepositoryObjectAce(
      new RepositoryObjectRecipient(role.getName(), ObjectRecipient.Type.ROLE));
    return new UIRepositoryObjectAcl(ace);
  }

  private UIRepositoryObjectAcl createAclFromUser(UIRepositoryUser user) {
    ObjectAce ace = new RepositoryObjectAce(
      new RepositoryObjectRecipient(user.getName(), ObjectRecipient.Type.USER));
    return new UIRepositoryObjectAcl(ace);
  }

  private void removeFromAvailableUsers(String name) {
    for(UIRepositoryUser user:availableUserList) {
      if(user.getName().equals(name)) {
        availableUserList.remove(user);
        break;
      }
    }
    this.firePropertyChange("availableUserList", null, availableUserList); //$NON-NLS-1$
    if(userAssignmentPossible && availableUserList.size() > 0) {
      this.firePropertyChange("userAssignmentPossible", null, true); //$NON-NLS-1$
    } else {
      this.firePropertyChange("userAssignmentPossible", null, false); //$NON-NLS-1$
    }
    
  }

  private void removeFromAvailableRoles(String name) {
    for(UIRepositoryRole role:availableRoleList) {
      if(role.getName().equals(name)) {
        availableRoleList.remove(role);
        break;
      }
    }
    this.firePropertyChange("availableRoleList", null, availableRoleList); //$NON-NLS-1$
    if(roleAssignmentPossible && availableRoleList.size() > 0) {
      this.firePropertyChange("roleAssignmentPossible", null, true); //$NON-NLS-1$
    } else {
      this.firePropertyChange("roleAssignmentPossible", null, false); //$NON-NLS-1$
    }

  }

  private void addToAvailableUsers(String name) {
    for(UIRepositoryUser user:masterAvailableUserList) {
      if(user.getName().equals(name)) {
        availableUserList.add(user);
        break;
      }
    }   
    this.firePropertyChange("availableUserList", null, availableUserList); //$NON-NLS-1$
    if(userUnassignmentPossible && getSelectedUserList().size() > 0) {
      this.firePropertyChange("userUnassignmentPossible", null, true); //$NON-NLS-1$
    } else {
      this.firePropertyChange("userUnassignmentPossible", null, false); //$NON-NLS-1$
    }
  }

  private void addToAvailableRoles(String name) {
    for(UIRepositoryRole role:masterAvailableRoleList) {
      if(role.getName().equals(name)) {
        availableRoleList.add(role);
        break;
      }
    }   
    this.firePropertyChange("availableRoleList", null, availableRoleList); //$NON-NLS-1$
    if(roleUnassignmentPossible && getSelectedRoleList().size() > 0) {
      this.firePropertyChange("roleUnassignmentPossible", null, true); //$NON-NLS-1$
    } else {
      this.firePropertyChange("roleUnassignmentPossible", null, false); //$NON-NLS-1$
    }
  }

  public boolean findByRecipientName(String recipientName) {
      for(UIRepositoryObjectAcl acl:aclsToRemove) {
        if(acl.getRecipientName().equals(recipientName)) {
          return true;
        }
    }
    return false;
  }
  
  public void clear() {
    aclsToAdd.clear();
    aclsToRemove.clear();
    masterAvailableRoleList.clear();
    masterAvailableUserList.clear();
    availableRoleList.clear();
    availableUserList.clear();
    selectedAvailableRoles.clear();
    selectedAvailableUsers.clear();
    selectedAssignedRoles.clear();
    selectedAssignedUsers.clear();      
    setRoleAssignmentPossible(false);
    setRoleUnassignmentPossible(false);
    setUserAssignmentPossible(false);
    setUserUnassignmentPossible(false);
  }
  
  public void updateSelectedAcls() {
    // Add the acls to be added to the selected acls
    for(UIRepositoryObjectAcl acl:aclsToAdd) {
      selectedAcls.addAcl(acl);
    }
    // Add the acls to be removed to the selected acls    
    for(UIRepositoryObjectAcl acl:aclsToRemove) {
      selectedAcls.removeAcl(acl.getRecipientName());
    }
  }
  
  private UIRepositoryRole getRole(String recipientName) {
    for(UIRepositoryRole role:masterAvailableRoleList) {
      if(role.getName().equals(recipientName)) {
        return role;
      }
    }
    return null;
  }
  private UIRepositoryUser getUser(String recipientName) {
    for(UIRepositoryUser user:masterAvailableUserList) {
      if(user.getName().equals(recipientName)) {
        return user;
      }
    }
    return null;
  }
  
  public UIRepositoryObjectAcl getSelectedUser(int index) {
    return getSelectedUserList().get(index);
  }

  public int getSelectedUserIndex(UIRepositoryObjectAcl user) {
    List<UIRepositoryObjectAcl> userList = getSelectedUserList();
    for(int i=0;i<userList.size();i++) {
      UIRepositoryObjectAcl u = userList.get(i);
      if(u.getRecipientName().equals(user.getRecipientName())) {
        return i;
      }
    }
    return -1; 
  }

  public UIRepositoryObjectAcl getSelectedRole(int index) {
    return getSelectedRoleList().get(index);
  }

  public int getSelectedRoleIndex(UIRepositoryObjectAcl role) {
    List<UIRepositoryObjectAcl> roleList = getSelectedRoleList();
    for(int i=0;i<roleList.size();i++) {
      UIRepositoryObjectAcl r = roleList.get(i);
      if(r.getRecipientName().equals(role.getRecipientName())) {
        return i;
      }
    }
    return -1; 
  }
}
