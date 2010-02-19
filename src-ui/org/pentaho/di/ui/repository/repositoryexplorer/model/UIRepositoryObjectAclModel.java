package org.pentaho.di.ui.repository.repositoryexplorer.model;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.di.repository.ObjectAce;
import org.pentaho.di.repository.ObjectRecipient;
import org.pentaho.di.repository.RepositoryObjectAce;
import org.pentaho.di.repository.RepositoryObjectRecipient;
import org.pentaho.di.repository.ObjectRecipient.Type;
import org.pentaho.ui.xul.XulEventSourceAdapter;

public class UIRepositoryObjectAclModel extends XulEventSourceAdapter {
  private List<String> masterAvailableUserList;

  private List<String> masterAvailableRoleList;

  private UIRepositoryObjectAcls selectedAcls;

  private List<String> selectedAvailableRoles = new ArrayList<String>();

  private List<String> selectedAvailableUsers = new ArrayList<String>();

  private List<UIRepositoryObjectAcl> selectedAssignedRoles = new ArrayList<UIRepositoryObjectAcl>();

  private List<UIRepositoryObjectAcl> selectedAssignedUsers = new ArrayList<UIRepositoryObjectAcl>();

  private List<UIRepositoryObjectAcl> aclsToAdd;

  private List<UIRepositoryObjectAcl> aclsToRemove;

  private List<String> availableUserList;

  private List<String> availableRoleList;

  private boolean userAssignmentPossible;

  private boolean userUnassignmentPossible;

  private boolean roleAssignmentPossible;

  private boolean roleUnassignmentPossible;

  public UIRepositoryObjectAclModel(UIRepositoryObjectAcls acls) {
    availableUserList = new ArrayList<String>();
    availableRoleList = new ArrayList<String>();
    masterAvailableUserList = new ArrayList<String>();
    masterAvailableRoleList = new ArrayList<String>();
    aclsToAdd = new ArrayList<UIRepositoryObjectAcl>();
    aclsToRemove = new ArrayList<UIRepositoryObjectAcl>();
    selectedAcls = acls;
  }

  public List<UIRepositoryObjectAcl> getAclsToAdd() {
    return aclsToAdd;
  }

  public void setAclsToRemove(List<UIRepositoryObjectAcl> aclsToRemove) {
    this.aclsToRemove = aclsToRemove;
  }

  public List<String> getSelectedAvailableRoles() {
    return selectedAvailableRoles;
  }

  public void setSelectedAvailableRoles(List<String> selectedAvailableRoles) {
    List<String> previousVal = new ArrayList<String>();
    previousVal.addAll(this.selectedAvailableRoles);
    this.selectedAvailableRoles.clear();
    for (String role : selectedAvailableRoles) {
      this.selectedAvailableRoles.add(role);
    }
    this.firePropertyChange("selectedAvailableRoles", previousVal, this.selectedAvailableRoles); //$NON-NLS-1$
    fireAssignPropertyChangeEvent(Type.ROLE);
  }

  public void setSelectedAvailableRole(String selectedAvailableRole) {
    this.selectedAvailableRoles.add(selectedAvailableRole);
  }

  public List<String> getSelectedAvailableUsers() {
    return selectedAvailableUsers;
  }

  public void setSelectedAvailableUsers(List<String> selectedAvailableUsers) {
    List<String> previousVal = new ArrayList<String>();
    previousVal.addAll(this.selectedAvailableUsers);
    this.selectedAvailableUsers.clear();
    for (String user : selectedAvailableUsers) {
      this.selectedAvailableUsers.add(user);
    }
    this.firePropertyChange("selectedAvailableUsers", previousVal, this.selectedAvailableUsers); //$NON-NLS-1$
    fireAssignPropertyChangeEvent(Type.USER);
  }

  public void setSelectedAvailableUser(String selectedAvailableUser) {
    this.selectedAvailableUsers.add(selectedAvailableUser);
  }

  public List<UIRepositoryObjectAcl> getSelectedAssignedRoles() {
    return selectedAssignedRoles;
  }

  public void setSelectedAssignedRoles(List<UIRepositoryObjectAcl> selectedAssignedRoles) {
    List<UIRepositoryObjectAcl> previousVal = new ArrayList<UIRepositoryObjectAcl>();
    previousVal.addAll(this.selectedAssignedRoles);
    this.selectedAssignedRoles.clear();
    for (UIRepositoryObjectAcl role : selectedAssignedRoles) {
      this.selectedAssignedRoles.add(role);
    }
    this.firePropertyChange("selectedAssignedRoles", previousVal, this.selectedAssignedRoles); //$NON-NLS-1$    
    fireUnassignPropertyChangeEvent(Type.ROLE);
  }

  public List<UIRepositoryObjectAcl> getSelectedAssignedUsers() {
    return selectedAssignedUsers;
  }

  public void setSelectedAssignedUsers(List<UIRepositoryObjectAcl> selectedAssignedUsers) {
    List<UIRepositoryObjectAcl> previousVal = new ArrayList<UIRepositoryObjectAcl>();
    previousVal.addAll(this.selectedAssignedUsers);
    this.selectedAssignedUsers.clear();
    for (UIRepositoryObjectAcl user : selectedAssignedUsers) {
      this.selectedAssignedUsers.add(user);
    }
    this.firePropertyChange("selectedAssignedUsers", previousVal, this.selectedAssignedUsers); //$NON-NLS-1$
    fireUnassignPropertyChangeEvent(Type.USER);
  }

  public void setSelectedAssignedUser(UIRepositoryObjectAcl selectedAssignedUser) {
    this.selectedAssignedUsers.add(selectedAssignedUser);
  }

  public List<String> getAvailableUserList() {
    return availableUserList;
  }

  public boolean isUserAssignmentPossible() {
    return userAssignmentPossible;
  }

  public void setUserAssignmentPossible(boolean userAssignmentPossible) {
    this.userAssignmentPossible = userAssignmentPossible;
    fireAssignPropertyChangeEvent(Type.USER);
  }

  public boolean isUserUnassignmentPossible() {
    return userUnassignmentPossible;
  }

  public void setUserUnassignmentPossible(boolean userUnassignmentPossible) {
    this.userUnassignmentPossible = userUnassignmentPossible;
    fireUnassignPropertyChangeEvent(Type.USER);
  }

  public boolean isRoleAssignmentPossible() {
    return roleAssignmentPossible;
  }

  public void setRoleAssignmentPossible(boolean roleAssignmentPossible) {
    this.roleAssignmentPossible = roleAssignmentPossible;
    fireAssignPropertyChangeEvent(Type.ROLE);
  }

  public boolean isRoleUnassignmentPossible() {
    return roleUnassignmentPossible;
  }

  public void setRoleUnassignmentPossible(boolean roleUnassignmentPossible) {
    this.roleUnassignmentPossible = roleUnassignmentPossible;
    fireUnassignPropertyChangeEvent(Type.ROLE);
  }

  // Set the initial acl list placing availble acl in the available column
  // and selected acls in selected column
  public void setAclsList(List<String> userList, List<String> roleList) {
    if (userList != null && userList.size() > 0) {
      setAvailableUserList(userList);
      setSelectedAvailableUser(userList.get(0));
      masterAvailableUserList.addAll(availableUserList);
    }
    if (roleList != null && roleList.size() > 0) {
      setAvailableRoleList(roleList);
      setSelectedAvailableRole(roleList.get(0));
      masterAvailableRoleList.addAll(availableRoleList);
    }
    setSelectedAcls(selectedAcls);
  }

  public void setAvailableUserList(List<String> userList) {
    List<String> previousVal = new ArrayList<String>();
    previousVal.addAll(this.availableUserList);
    this.availableUserList.clear();
    this.availableUserList.addAll(userList);
    this.firePropertyChange("availableUserList", previousVal, availableUserList); //$NON-NLS-1$
  }

  public List<String> getAvailableRoleList() {
    return availableRoleList;
  }

  public void setAvailableRoleList(List<String> roleList) {
    List<String> previousVal = new ArrayList<String>();
    previousVal.addAll(this.availableRoleList);
    this.availableRoleList.clear();
    this.availableRoleList.addAll(roleList);
    this.firePropertyChange("availableRoleList", previousVal, availableRoleList); //$NON-NLS-1$
  }

  public void assignRoles(List<Object> rolesToAssign) {
    List<UIRepositoryObjectAcl> acls = new ArrayList<UIRepositoryObjectAcl>();
    for (Object role : rolesToAssign) {
      if (role instanceof String) {
        String roleToAssign = (String) role;
        acls.add(assignRole(roleToAssign));
      }
    }
    this.firePropertyChange("selectedRoleList", null, getSelectedRoleList()); //$NON-NLS-1$
    setSelectedAssignedRoles(acls);
    setSelectedAvailableRoles(new ArrayList<String>());
  }

  public UIRepositoryObjectAcl assignRole(String roleToAssign) {
    UIRepositoryObjectAcl acl = createAclFromRole(roleToAssign);
    aclsToAdd.add(acl);
    removeFromAvailableRoles(roleToAssign);
    return acl;
  }

  public UIRepositoryObjectAcl getAcl(String aclName) {
    for (UIRepositoryObjectAcl selected : this.selectedAcls.getAcls()) {
      if (selected.getRecipientName().equals(aclName)) {
        return selected;
      }
    }
    for (UIRepositoryObjectAcl selected : aclsToAdd) {
      if (selected.getRecipientName().equals(aclName)) {
        return selected;
      }
    }
    return null;
  }

  public void unassign(List<Object> toUnassign) {
    Type type = null;
    List<String> unassignList = new ArrayList<String>();
    for (Object obj : toUnassign) {
      if (obj instanceof UIRepositoryObjectAcl) {
        UIRepositoryObjectAcl acl = (UIRepositoryObjectAcl) obj;
        type = acl.getRecipientType();
        unassign(acl.getRecipientName());
        unassignList.add(acl.getRecipientName());
      }
    }
    if (type == Type.ROLE) {
      setSelectedAvailableRoles(unassignList);
      setSelectedAssignedRoles(new ArrayList<UIRepositoryObjectAcl>());
    } else {
      setSelectedAvailableUsers(unassignList);
      setSelectedAssignedUsers(new ArrayList<UIRepositoryObjectAcl>());
    }

  }

  private void unassign(String toUnassign) {
    UIRepositoryObjectAcl selectedAcl = getAcl(toUnassign);
    if (selectedAcl != null) {
      unassign(selectedAcl);
      if (selectedAcl.getRecipientType() == ObjectRecipient.Type.USER) {
        addToAvailableUsers(selectedAcl.getRecipientName());
      } else if (selectedAcl.getRecipientType() == ObjectRecipient.Type.ROLE) {
        addToAvailableRoles(selectedAcl.getRecipientName());
      }
    }
  }

  private void unassign(UIRepositoryObjectAcl toUnassign) {
    if (aclsToAdd.contains(toUnassign)) {
      aclsToAdd.remove(toUnassign);
    } else {
      aclsToRemove.add(toUnassign);
    }
    if (toUnassign.getRecipientType() == ObjectRecipient.Type.ROLE) {
      this.firePropertyChange("selectedRoleList", null, getSelectedRoleList()); //$NON-NLS-1$
    } else if (toUnassign.getRecipientType() == ObjectRecipient.Type.USER) {
      this.firePropertyChange("selectedUserList", null, getSelectedUserList()); //$NON-NLS-1$
    }

  }

  public void assignUsers(List<Object> usersToAssign) {
    List<UIRepositoryObjectAcl> previousVal = new ArrayList<UIRepositoryObjectAcl>();
    previousVal.addAll(getSelectedUserList());
    List<UIRepositoryObjectAcl> assignList = new ArrayList<UIRepositoryObjectAcl>();
    for (Object user : usersToAssign) {
      if (user instanceof String)
        assignList.add(assignUser((String) user));
    }
    this.firePropertyChange("selectedUserList", null, getSelectedUserList()); //$NON-NLS-1$
    setSelectedAssignedUsers(assignList);
    setSelectedAvailableUsers(new ArrayList<String>());
  }

  public UIRepositoryObjectAcl assignUser(String userToAssign) {
    UIRepositoryObjectAcl acl = createAclFromUser(userToAssign);
    aclsToAdd.add(acl);
    removeFromAvailableUsers(userToAssign);
    return acl;
  }

  private void removeFromAvailableAcls(UIRepositoryObjectAcl acl) {
    if (acl.getRecipientType().equals(Type.ROLE)) {
      removeFromAvailableRoles(acl.getRecipientName());
    } else if (acl.getRecipientType().equals(Type.USER)) {
      removeFromAvailableUsers(acl.getRecipientName());
    }
  }

  public UIRepositoryObjectAcls getSelectedAcls() {
    return selectedAcls;
  }

  public void setSelectedAcls(UIRepositoryObjectAcls selectedAcls) {
    this.selectedAcls = selectedAcls;
    for (UIRepositoryObjectAcl acl : this.selectedAcls.getAcls()) {
      removeFromAvailableAcls(acl);
    }
    this.firePropertyChange("selectedUserList", null, getSelectedUserList()); //$NON-NLS-1$   
    this.firePropertyChange("selectedRoleList", null, getSelectedRoleList()); //$NON-NLS-1$   
  }

  public List<UIRepositoryObjectAcl> getSelectedUserList() {
    if (selectedAcls != null && selectedAcls.getAcls() != null) {
      List<UIRepositoryObjectAcl> selectedUserList = new ArrayList<UIRepositoryObjectAcl>();
      // Add all the acls from the initial list and remove the one that are marked for removal
      for (UIRepositoryObjectAcl selectedAcl : selectedAcls.getAcls()) {
        if (!findByRecipientName(selectedAcl.getRecipientName())) {
          if (selectedAcl.getRecipientType().equals(ObjectRecipient.Type.USER)) {
            selectedUserList.add(selectedAcl);
          }
        }
      }
      // Now add the acls that are marked for addition
      for (UIRepositoryObjectAcl acl : aclsToAdd) {
        if (acl.getRecipientType().equals(ObjectRecipient.Type.USER)) {
          selectedUserList.add(acl);
        }
      }
      return selectedUserList;
    } else {
      return null;
    }

  }

  public List<UIRepositoryObjectAcl> getSelectedRoleList() {
    if (selectedAcls != null && selectedAcls.getAcls() != null) {
      List<UIRepositoryObjectAcl> selectedRoleList = new ArrayList<UIRepositoryObjectAcl>();
      // Add all the acls from the initial list and remove the one that are marked for removal      
      for (UIRepositoryObjectAcl selectedAcl : this.selectedAcls.getAcls()) {
        if (!findByRecipientName(selectedAcl.getRecipientName())) {
          if (selectedAcl.getRecipientType().equals(ObjectRecipient.Type.ROLE)) {
            selectedRoleList.add(selectedAcl);
          }
        }
      }
      // Now add the acls that are marked for addition
      for (UIRepositoryObjectAcl acl : aclsToAdd) {
        if (acl.getRecipientType().equals(ObjectRecipient.Type.ROLE)) {
          selectedRoleList.add(acl);
        }
      }
      return selectedRoleList;
    } else {
      return null;
    }
  }

  public String getAvailableUser(int index) {
    return availableUserList.get(index);
  }

  public int getAvailableUserIndex(String user) {
    for (int i = 0; i < availableUserList.size(); i++) {
      String u = availableUserList.get(i);
      if (u.equals(user)) {
        return i;
      }
    }
    return -1;
  }

  public String getAvailableRole(int index) {
    return availableRoleList.get(index);
  }

  public int getAvailableRoleIndex(String role) {
    for (int i = 0; i < availableRoleList.size(); i++) {
      String r = availableRoleList.get(i);
      if (r.equals(role)) {
        return i;
      }
    }
    return -1;
  }

  private UIRepositoryObjectAcl createAclFromRole(String role) {
    ObjectAce ace = new RepositoryObjectAce(new RepositoryObjectRecipient(role, ObjectRecipient.Type.ROLE));
    return new UIRepositoryObjectAcl(ace);
  }

  private UIRepositoryObjectAcl createAclFromUser(String user) {
    ObjectAce ace = new RepositoryObjectAce(new RepositoryObjectRecipient(user, ObjectRecipient.Type.USER));
    return new UIRepositoryObjectAcl(ace);
  }

  private void removeFromAvailableUsers(String name) {
    for (String user : availableUserList) {
      if (user.equals(name)) {
        availableUserList.remove(user);
        break;
      }
    }
    this.firePropertyChange("availableUserList", null, availableUserList); //$NON-NLS-1$
  }

  private void removeFromAvailableRoles(String name) {
    for (String role : availableRoleList) {
      if (role.equals(name)) {
        availableRoleList.remove(role);
        break;
      }
    }
    this.firePropertyChange("availableRoleList", null, availableRoleList); //$NON-NLS-1$
  }

  private void addToAvailableUsers(String name) {
    for (String user : masterAvailableUserList) {
      if (user.equals(name)) {
        availableUserList.add(user);
        break;
      }
    }
    this.firePropertyChange("availableUserList", null, availableUserList); //$NON-NLS-1$
    fireAssignPropertyChangeEvent(Type.USER);
  }

  private void addToAvailableRoles(String name) {
    for (String role : masterAvailableRoleList) {
      if (role.equals(name)) {
        availableRoleList.add(role);
        break;
      }
    }
    this.firePropertyChange("availableRoleList", null, availableRoleList); //$NON-NLS-1$
    fireAssignPropertyChangeEvent(Type.ROLE);
  }

  public boolean findByRecipientName(String recipientName) {
    for (UIRepositoryObjectAcl acl : aclsToRemove) {
      if (acl.getRecipientName().equals(recipientName)) {
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
    selectedAcls.addAcls(aclsToAdd);
    selectedAcls.removeAcls(aclsToRemove);
  }

  public UIRepositoryObjectAcl getSelectedUser(int index) {
    return getSelectedUserList().get(index);
  }

  public int getSelectedUserIndex(UIRepositoryObjectAcl user) {
    List<UIRepositoryObjectAcl> userList = getSelectedUserList();
    for (int i = 0; i < userList.size(); i++) {
      UIRepositoryObjectAcl u = userList.get(i);
      if (u.getRecipientName().equals(user.getRecipientName())) {
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
    for (int i = 0; i < roleList.size(); i++) {
      UIRepositoryObjectAcl r = roleList.get(i);
      if (r.getRecipientName().equals(role.getRecipientName())) {
        return i;
      }
    }
    return -1;
  }

  @SuppressWarnings("unchecked")
  private void fireUnassignPropertyChangeEvent(Type type) {
    if (type == Type.USER) {
      if (isUnAssignmentPossible(type)) {
        this.firePropertyChange("userUnassignmentPossible", null, true); //$NON-NLS-1$
      } else {
        this.firePropertyChange("userUnassignmentPossible", null, false); //$NON-NLS-1$
      }
    } else {
      if (isUnAssignmentPossible(type)) {
        this.firePropertyChange("roleUnassignmentPossible", null, true); //$NON-NLS-1$
      } else {
        this.firePropertyChange("roleUnassignmentPossible", null, false); //$NON-NLS-1$
      }
    }
  }

  @SuppressWarnings("unchecked")
  private void fireAssignPropertyChangeEvent(Type type) {
    if (type == Type.USER) {
      if (isAssignmentPossible(type)) {
        this.firePropertyChange("userAssignmentPossible", null, true); //$NON-NLS-1$
      } else {
        this.firePropertyChange("userAssignmentPossible", null, false); //$NON-NLS-1$
      }
    } else {
      if (isAssignmentPossible(type)) {
        this.firePropertyChange("roleAssignmentPossible", null, true); //$NON-NLS-1$
      } else {
        this.firePropertyChange("roleAssignmentPossible", null, false); //$NON-NLS-1$
      }

    }
  }
  
  private boolean isUnAssignmentPossible(Type type) {
   if(type == Type.USER) {
     return (selectedAssignedUsers != null && selectedAssignedUsers.size() > 0);
   } else {
     return (selectedAssignedRoles != null && selectedAssignedRoles.size() > 0);
   }
  }
  

  private boolean isAssignmentPossible(Type type) {
    if(type == Type.USER) {
      return (selectedAvailableUsers != null && selectedAvailableUsers.size() > 0);
    } else {
      return (selectedAvailableRoles != null && selectedAvailableRoles.size() > 0);
    }
   }

}
