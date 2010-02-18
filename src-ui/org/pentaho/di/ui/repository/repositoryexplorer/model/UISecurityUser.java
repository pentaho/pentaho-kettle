package org.pentaho.di.ui.repository.repositoryexplorer.model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import org.pentaho.di.repository.UserInfo;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UISecurity.Mode;
import org.pentaho.ui.xul.XulEventSourceAdapter;
import org.pentaho.ui.xul.util.AbstractModelList;

public class UISecurityUser extends XulEventSourceAdapter{

  private Mode mode;
  private AbstractModelList<IUIRole> availableRoles;
  private AbstractModelList<IUIRole> assignedRoles;
  private List<IUIRole> availableSelectedRoles = new ArrayList<IUIRole>();
  private List<IUIRole> assignedSelectedRoles = new ArrayList<IUIRole>();  
  private boolean roleAssignmentPossible;
  private boolean roleUnassignmentPossible;
  private String name;
  private String description;
  private String password;
  public UISecurityUser() {
    availableRoles = new AbstractModelList<IUIRole>();
    availableRoles.addPropertyChangeListener("children", new PropertyChangeListener() { //$NON-NLS-1$
      
      public void propertyChange(PropertyChangeEvent evt) {
        UISecurityUser.this.firePropertyChange("availableRoles", null, availableRoles); //$NON-NLS-1$
      }
    });
    
    assignedRoles = new AbstractModelList<IUIRole>();
    assignedRoles.addPropertyChangeListener("children", new PropertyChangeListener() { //$NON-NLS-1$
      
      public void propertyChange(PropertyChangeEvent evt) {
        UISecurityUser.this.firePropertyChange("assignedRoles", null, assignedRoles); //$NON-NLS-1$
      }
    });
    description = null;
    name = null;
    password = null;
    
  }

  public void setUser(UIRepositoryUser user, List<IUIRole> roles) throws Exception{
    setAvailableRoles(roles);
    setDescription(user.getDescription());
    setName(user.getName());
    // Show empty password on the client site
    setPassword("");//$NON-NLS-1$
    for(IUIRole role:user.getRoles()) {
      removeFromAvailableRoles(role.getName());
      addToSelectedRoles(UIObjectRegistery.getInstance().constructUIRepositoryRole(role.getRole()));
    }
    
  }
 
  public List<IUIRole> getAvailableSelectedRoles() {
    return availableSelectedRoles;
  }

  public void setAvailableSelectedRoles(List<Object> availableSelectedRoles) {
    List<Object> previousVal = new ArrayList<Object>();
    previousVal.addAll(this.availableSelectedRoles);
    this.availableSelectedRoles.clear();
    if(availableSelectedRoles != null && availableSelectedRoles.size() > 0) {
      for(Object role:availableSelectedRoles) {
        this.availableSelectedRoles.add((UIRepositoryRole) role);
      }
    }
    this.firePropertyChange("availableSelectedRoles", previousVal, this.availableSelectedRoles); //$NON-NLS-1$
    fireRoleAssignmentPropertyChange();
  }

  public List<IUIRole> getAssignedSelectedRoles() {
    return assignedSelectedRoles;
  }

  public void setAssignedSelectedRoles(List<Object> assignedSelectedRoles) {
    List<Object> previousVal = new ArrayList<Object>();
    previousVal.addAll(this.availableSelectedRoles);
    this.assignedSelectedRoles.clear();
    if(assignedSelectedRoles != null && assignedSelectedRoles.size() > 0) {
      for(Object role:assignedSelectedRoles) {
        this.assignedSelectedRoles.add((UIRepositoryRole) role);
      }
    }
    this.firePropertyChange("assignedSelectedRoles", null, this.assignedSelectedRoles); //$NON-NLS-1$
    fireRoleUnassignmentPropertyChange();
  }


  public UISecurityUser getUISecurityUser() {
    return this;
  }
  public Mode getMode() {
    return mode;
  }
  public void setMode(Mode mode) {
    this.mode = mode;
    this.firePropertyChange("mode", null, mode); //$NON-NLS-1$
  }
  public List<IUIRole> getAvailableRoles() {
    return availableRoles;
  }
  public void setAvailableRoles(List<IUIRole> availableRoles) {
    List<IUIRole> previousValue = getPreviousAvailableRoles();
    this.availableRoles.clear();
    if(availableRoles != null) {
      this.availableRoles.addAll(availableRoles);
    }
    this.firePropertyChange("availableRoles", previousValue, this.availableRoles); //$NON-NLS-1$
  }
  public List<IUIRole> getAssignedRoles() {
    return assignedRoles;
  }
  public void setAssignedRoles(List<IUIRole> selectedRoles) {
    List<IUIRole> previousValue = getPreviousSelectedRoles();
    this.assignedRoles.clear();
    if(selectedRoles != null) {
      this.assignedRoles.addAll(selectedRoles);
    }
    this.firePropertyChange("assignedRoles", previousValue, this.assignedRoles); //$NON-NLS-1$
  }
  public String getName() {
    return name;
  }
  public void setName(String name) {
    String previousValue = this.name;
    this.name = name;
    this.firePropertyChange("name", previousValue, name); //$NON-NLS-1$
  }
  public String getDescription() {
    return description;
  }
  public void setDescription(String description) {
    String previousValue = this.description;
    this.description = description;
    this.firePropertyChange("description", previousValue, description); //$NON-NLS-1$
  }
  public String getPassword() {
    return password;
  }
  public void setPassword(String password) {
    String previousValue = this.password;
    this.password = password;
    this.firePropertyChange("password", previousValue, password); //$NON-NLS-1$
  }
  public void clear() {
    setMode(Mode.ADD);
    setName("");//$NON-NLS-1$
    setDescription("");//$NON-NLS-1$
    setPassword("");//$NON-NLS-1$
    setAssignedSelectedRoles(null);
    setAvailableSelectedRoles(null);
    setAvailableRoles(null);
    setAssignedRoles(null);
    setRoleAssignmentPossible(false);
    setRoleUnassignmentPossible(false);
    
  }
  
  public void assignRoles(List<Object> rolesToAssign) {   
    for(Object roleToAssign:rolesToAssign) {
      assignRole((UIRepositoryRole)roleToAssign);
    }
    setAssignedSelectedRoles(rolesToAssign);
    setAvailableSelectedRoles(new ArrayList<Object>());
    this.firePropertyChange("roleAssignmentPossible", null, false); //$NON-NLS-1$
  }

  public void assignRole(UIRepositoryRole roleToAssign) {
    addToSelectedRoles(roleToAssign);
    removeFromAvailableRoles(roleToAssign.getName());
  }
  
  public void unassignRoles(List<Object> rolesToUnAssign) {
    for(Object roleToUnAssign:rolesToUnAssign) {
      unassignRole((UIRepositoryRole) roleToUnAssign);
    }
    setAvailableSelectedRoles(rolesToUnAssign);
    setAssignedSelectedRoles(new ArrayList<Object>());
    this.firePropertyChange("roleUnassignmentPossible", null, false); //$NON-NLS-1$
  }

  public void unassignRole(UIRepositoryRole roleToUnAssign) {
    removeFromSelectedRoles(roleToUnAssign.getName());
    addToAvailableRoles(roleToUnAssign);
  }
  public boolean isRoleAssignmentPossible() {
    return roleAssignmentPossible;
  }

  public void setRoleAssignmentPossible(boolean roleAssignmentPossible) {
    this.roleAssignmentPossible = roleAssignmentPossible;
    fireRoleAssignmentPropertyChange();
  }

  public boolean isRoleUnassignmentPossible() {
    return roleUnassignmentPossible;
  }

  public void setRoleUnassignmentPossible(boolean roleUnassignmentPossible) {
    this.roleUnassignmentPossible = roleUnassignmentPossible;
    fireRoleUnassignmentPropertyChange();
  }
  public UserInfo getUserInfo() {
    UserInfo userInfo = new UserInfo();
    userInfo.setDescription(description);
    userInfo.setLogin(name);
    userInfo.setName(name);
    userInfo.setUsername(name);
    userInfo.setPassword(password);
    for (IUIRole role : getAssignedRoles()) {
      userInfo.addRole(role.getRole());
    }
    return userInfo;
  }

  private void addToSelectedRoles(IUIRole roleToAdd) {
    List<IUIRole> previousValue = getPreviousSelectedRoles();
    assignedRoles.add(roleToAdd);
    this.firePropertyChange("assignedRoles", previousValue, assignedRoles); //$NON-NLS-1$
    if(assignedRoles.size() == 1) {
      setRoleUnassignmentPossible(true);      
    }
    fireRoleUnassignmentPropertyChange();
  }

  private void addToAvailableRoles(UIRepositoryRole roleToAdd) {
    List<IUIRole> previousValue = getPreviousAvailableRoles();
    availableRoles.add(roleToAdd);
    if(availableRoles.size() == 1) {
      setRoleAssignmentPossible(true);      
    }
    this.firePropertyChange("availableRoles", previousValue, availableRoles); //$NON-NLS-1$
    fireRoleAssignmentPropertyChange();
  }
  private void removeFromAvailableRoles(String roleName) {
    List<IUIRole> previousValue = getPreviousAvailableRoles();
    availableRoles.remove(getAvailableRole(roleName));
    if(availableRoles.size() == 0) {
      setRoleAssignmentPossible(false);      
    }
    this.firePropertyChange("availableRoles", previousValue, availableRoles); //$NON-NLS-1$
    fireRoleAssignmentPropertyChange();
  }

  private void removeFromSelectedRoles(String roleName) {
    List<IUIRole> previousValue = getPreviousSelectedRoles();
    assignedRoles.remove(getSelectedRole(roleName));
    if(assignedRoles.size() == 0) {
      setRoleUnassignmentPossible(false);      
    }    
    this.firePropertyChange("assignedRoles", previousValue, assignedRoles); //$NON-NLS-1$
    fireRoleUnassignmentPropertyChange();
  }
  private void fireRoleUnassignmentPropertyChange () {
    if(roleUnassignmentPossible && assignedRoles.size() > 0 && assignedSelectedRoles.size() > 0) {
      this.firePropertyChange("roleUnassignmentPossible", null, true); //$NON-NLS-1$
    } else {
      this.firePropertyChange("roleUnassignmentPossible", null, false); //$NON-NLS-1$
    }
  }
  private void fireRoleAssignmentPropertyChange () {
    if(roleAssignmentPossible && availableRoles.size() > 0 && availableSelectedRoles.size() > 0) {
      this.firePropertyChange("roleAssignmentPossible", null, true); //$NON-NLS-1$
    } else {
      this.firePropertyChange("roleAssignmentPossible", null, false); //$NON-NLS-1$
    }
  }
  
  private IUIRole getSelectedRole(String name) {
    for (IUIRole role : assignedRoles) {
      if (role.getName().equals(name)) {
        return role;
      }
    }
    return null;
  }
  
  private IUIRole getAvailableRole(String name) {
    for (IUIRole role : availableRoles) {
      if (role.getName().equals(name)) {
        return role;
      }
    }
    return null;
  }
  
  private List<IUIRole> getPreviousAvailableRoles() {
    List<IUIRole> previousValue = new ArrayList<IUIRole>();
    for (IUIRole ru : availableRoles) {
      previousValue.add(ru);
    }
    return previousValue;
  }

  private List<IUIRole> getPreviousSelectedRoles() {
    List<IUIRole> previousValue = new ArrayList<IUIRole>();
    for (IUIRole ru : assignedRoles) {
      previousValue.add(ru);
    }
    return previousValue;
  }
}
