package org.pentaho.di.ui.repository.repositoryexplorer.model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.repository.IRole;
import org.pentaho.di.repository.RepositorySecurityManager;
import org.pentaho.di.repository.UserInfo;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UISecurity.Mode;
import org.pentaho.ui.xul.XulEventSourceAdapter;
import org.pentaho.ui.xul.util.AbstractModelList;

public class UISecurityRole extends XulEventSourceAdapter {

  private Mode mode;
  private List<UIRepositoryUser> availableUsers;
  private AbstractModelList<UIRepositoryUser> assignedUsers;
  private List<UIRepositoryUser> availableSelectedUsers = new ArrayList<UIRepositoryUser>();
  private List<UIRepositoryUser> assignedSelectedUsers  = new ArrayList<UIRepositoryUser>();
  private boolean userAssignmentPossible;
  private boolean userUnassignmentPossible;
  private String name;

  private String description;

  public UISecurityRole() {
    availableUsers = new ArrayList<UIRepositoryUser>();
    assignedUsers = new AbstractModelList<UIRepositoryUser>();

    assignedUsers.addPropertyChangeListener("children", //$NON-NLS-1$
        new PropertyChangeListener() {

          public void propertyChange(PropertyChangeEvent evt) {
            List<UIRepositoryUser> previousValue = getPreviousSelectedUsers();
            UISecurityRole.this.firePropertyChange("selectedUsers", //$NON-NLS-1$
                previousValue, assignedUsers);
          }
        });

    description = null;
    name = null;
  }

  public List<UIRepositoryUser> getAvailableSelectedUsers() {
    return availableSelectedUsers;
  }

  public void setAvailableSelectedUsers(List<Object> availableSelectedUsers) {
    List<Object> previousVal = new ArrayList<Object>();
    previousVal.addAll(this.availableSelectedUsers);
    this.availableSelectedUsers.clear();
    if(availableSelectedUsers != null  && availableSelectedUsers.size() > 0) {
      for(Object user:availableSelectedUsers) {
        this.availableSelectedUsers.add((UIRepositoryUser) user);
      }
    }
    this.firePropertyChange("availableSelectedUsers",  previousVal, this.availableSelectedUsers); //$NON-NLS-1$
    fireUserAssignmentPropertyChange();
  }

  public List<UIRepositoryUser> getAssignedSelectedUsers() {
    return assignedSelectedUsers;
  }

  public void setAssignedSelectedUsers(List<Object> assignedSelectedUsers) {
    List<Object> previousVal = new ArrayList<Object>();
    previousVal.addAll(this.assignedSelectedUsers);
    this.assignedSelectedUsers.clear();
    if(assignedSelectedUsers != null && assignedSelectedUsers.size() > 0) {
      for(Object user:assignedSelectedUsers) {
        this.assignedSelectedUsers.add((UIRepositoryUser) user);
      }
    }
    this.firePropertyChange("assignedSelectedUsers",  previousVal, this.assignedSelectedUsers); //$NON-NLS-1$
    fireUserUnassignmentPropertyChange();
  }


  public UISecurityRole getUISecurityRole() {
    return this;
  }

  public void setRole(IUIRole role,List<UIRepositoryUser> users) {
    setAvailableUsers(users);
    setDescription(role.getDescription());
    setName(role.getName());
    for (UIRepositoryUser user : role.getUsers()) {
      removeFromAvailableUsers(user.getName());
      addToAssignedUsers(user);
    }
  }

  public Mode getMode() {
    return mode;
  }

  public void setMode(Mode mode) {
    this.mode = mode;
    this.firePropertyChange("mode", null, mode); //$NON-NLS-1$
  }

  public List<UIRepositoryUser> getAvailableUsers() {
    return availableUsers;
  }

  public void setAvailableUsers(List<UIRepositoryUser> availableUsers) {
    List<UIRepositoryUser> previousValue = getPreviousAvailableUsers();
    this.availableUsers.clear();
    if (availableUsers != null) {
      this.availableUsers.addAll(availableUsers);
    }
    this.firePropertyChange("availableUsers", previousValue, this.availableUsers); //$NON-NLS-1$
  }

  public List<UIRepositoryUser> getAssignedUsers() {
    return assignedUsers;
  }
  public void setAssignedUsers(AbstractModelList<UIRepositoryUser> selectedUsers) {
      List<UIRepositoryUser> previousValue = getPreviousSelectedUsers();
    this.assignedUsers.clear();
    if (selectedUsers != null) {
      this.assignedUsers.addAll(selectedUsers);
    }
    this.firePropertyChange("assignedUsers", previousValue, this.assignedUsers); //$NON-NLS-1$
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    String previousVal = this.name;
    this.name = name;
    this.firePropertyChange("name", previousVal, name); //$NON-NLS-1$
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    String previousVal = this.description;
    this.description = description;
    this.firePropertyChange("description", previousVal, description); //$NON-NLS-1$
  }

  public void clear() {
    setMode(Mode.ADD);
    setName("");//$NON-NLS-1$
    setDescription(""); //$NON-NLS-1$
    setAvailableUsers(null);
    setAssignedSelectedUsers(null);
    setAvailableSelectedUsers(null);
    setAssignedUsers(null);
    setUserAssignmentPossible(false);
    setUserUnassignmentPossible(false);
  }

  public void assignUsers(List<Object> usersToAssign) {
    for(Object userToAssign:usersToAssign) {
      
      assignUser((UIRepositoryUser)userToAssign);
    }
    setAssignedSelectedUsers(usersToAssign);
    setAvailableSelectedUsers(new ArrayList<Object>());
    this.firePropertyChange("userAssignmentPossible", null, false); //$NON-NLS-1$
  }
  public void assignUser(UIRepositoryUser userToAssign) {
    addToAssignedUsers(userToAssign);
    removeFromAvailableUsers(userToAssign.getName());
  }

  public void unassignUsers(List<Object> usersToUnAssign) {
    for(Object userToUnAssign:usersToUnAssign) {
      unassignUser((UIRepositoryUser)userToUnAssign);
    }
    setAvailableSelectedUsers(usersToUnAssign);
    setAssignedSelectedUsers(new ArrayList<Object>());    
    this.firePropertyChange("userUnassignmentPossible", null, false); //$NON-NLS-1$
  }

  public void unassignUser(UIRepositoryUser userToUnAssign) {
    removeFromAssignedUsers(userToUnAssign.getName());
    addToAvailableUsers(userToUnAssign);
  }
  
  public boolean isUserAssignmentPossible() {
    return userAssignmentPossible;
  }

  public void setUserAssignmentPossible(boolean userAssignmentPossible) {
    this.userAssignmentPossible = userAssignmentPossible;
    fireUserAssignmentPropertyChange ();
  }

  public boolean isUserUnassignmentPossible() {
    return userUnassignmentPossible;
  }

  public void setUserUnassignmentPossible(boolean userUnassignmentPossible) {
    this.userUnassignmentPossible = userUnassignmentPossible;
    fireUserUnassignmentPropertyChange ();
  }

  public IRole getRole(RepositorySecurityManager rsm) {
    IRole roleInfo = null;
    try {
      roleInfo = rsm.constructRole();
    } catch (KettleException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    roleInfo.setDescription(description);
    roleInfo.setName(name);
    for (UIRepositoryUser user : getAssignedUsers()) {
      roleInfo.addUser(user.getUserInfo());
    }
    return roleInfo;
  }
  private void addToAssignedUsers(UIRepositoryUser userToAdd) {
    List<UIRepositoryUser> previousValue = getPreviousSelectedUsers();
    assignedUsers.add(userToAdd);
    if(assignedUsers.size() == 1) {
      setUserUnassignmentPossible(true);      
    }
    this.firePropertyChange("assignedUsers", previousValue, assignedUsers); //$NON-NLS-1$
  }
  
  private void addToAvailableUsers(UIRepositoryUser userToAdd) {
    List<UIRepositoryUser> previousValue = getPreviousAvailableUsers();
    availableUsers.add(userToAdd);
    if(availableUsers.size() == 1) {
      setUserAssignmentPossible(true);      
    }
    this.firePropertyChange("availableUsers", previousValue, availableUsers); //$NON-NLS-1$
  }

  private void removeFromAvailableUsers(String userName) {
    List<UIRepositoryUser> previousValue = getPreviousAvailableUsers();
    availableUsers.remove(getAvailableUser(userName));
    if(availableUsers.size() == 0) {
      setUserAssignmentPossible(false);      
    }    
    this.firePropertyChange("availableUsers", previousValue, availableUsers); //$NON-NLS-1$
    fireUserAssignmentPropertyChange ();
  }

  private void removeFromAssignedUsers(String userName) {
    List<UIRepositoryUser> previousValue = getPreviousSelectedUsers();
    assignedUsers.remove(getSelectedUser(userName));
    if(assignedUsers.size() == 0) {
      setUserUnassignmentPossible(false);      
    }    
    
    this.firePropertyChange("assignedUsers", previousValue, assignedUsers); //$NON-NLS-1$
    fireUserUnassignmentPropertyChange ();
  }

  private UIRepositoryUser getSelectedUser(String userName) {
    for (UIRepositoryUser user : assignedUsers) {
      if (user.getName().equals(userName)) {
        return user;
      }
    }
    return null;
  }
  
  private void fireUserUnassignmentPropertyChange () {
    if(userUnassignmentPossible && assignedUsers.size() > 0 && assignedSelectedUsers.size() > 0) {
      this.firePropertyChange("userUnassignmentPossible", null, true); //$NON-NLS-1$
    } else {
      this.firePropertyChange("userUnassignmentPossible", null, false); //$NON-NLS-1$
    }
  }
  private void fireUserAssignmentPropertyChange () {
    if(userAssignmentPossible && availableUsers.size() > 0 && availableSelectedUsers.size() > 0) {
      this.firePropertyChange("userAssignmentPossible", null, true); //$NON-NLS-1$
    } else {
      this.firePropertyChange("userAssignmentPossible", null, false); //$NON-NLS-1$
    }
  }

  
  private UIRepositoryUser getAvailableUser(String userName) {
    for (UIRepositoryUser user : availableUsers) {
      if (user.getName().equals(userName)) {
        return user;
      }
    }
    return null;
  }
  private List<UIRepositoryUser> getPreviousAvailableUsers() {
    List<UIRepositoryUser> previousValue = new ArrayList<UIRepositoryUser>();
    for (UIRepositoryUser ru : availableUsers) {
      previousValue.add(ru);
    }
    return previousValue;
  }

  private List<UIRepositoryUser> getPreviousSelectedUsers() {
    List<UIRepositoryUser> previousValue = new ArrayList<UIRepositoryUser>();
    for (UIRepositoryUser ru : assignedUsers) {
      previousValue.add(ru);
    }
    return previousValue;
  }
}
