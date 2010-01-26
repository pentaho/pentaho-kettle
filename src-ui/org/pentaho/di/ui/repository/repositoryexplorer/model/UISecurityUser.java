package org.pentaho.di.ui.repository.repositoryexplorer.model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import org.pentaho.di.repository.RoleInfo;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UISecurity.Mode;
import org.pentaho.ui.xul.XulEventSourceAdapter;
import org.pentaho.ui.xul.util.AbstractModelList;

public class UISecurityUser extends XulEventSourceAdapter{

  private Mode mode;
  private AbstractModelList<UIRepositoryRole> availableRoles;
  private AbstractModelList<UIRepositoryRole> assignedRoles;
  private List<UIRepositoryRole> availableSelectedRoles = new ArrayList<UIRepositoryRole>();
  private List<UIRepositoryRole> assignedSelectedRoles = new ArrayList<UIRepositoryRole>();  
  private boolean roleAssignmentPossible;
  private boolean roleUnassignmentPossible;
  private String name;
  private String description;
  private String password;
  public UISecurityUser() {
    availableRoles = new AbstractModelList<UIRepositoryRole>();
    availableRoles.addPropertyChangeListener("children", new PropertyChangeListener() {
      
      public void propertyChange(PropertyChangeEvent evt) {
        UISecurityUser.this.firePropertyChange("availableRoles", null, availableRoles);
      }
    });
    
    assignedRoles = new AbstractModelList<UIRepositoryRole>();
    assignedRoles.addPropertyChangeListener("children", new PropertyChangeListener() {
      
      public void propertyChange(PropertyChangeEvent evt) {
        UISecurityUser.this.firePropertyChange("assignedRoles", null, assignedRoles);
      }
    });
    description = null;
    name = null;
    password = null;
    
  }

  public void setUISecurityUser(UIRepositoryUser user, List<UIRepositoryRole> roles) {
    setAvailableRoles(roles);
    setDescription(user.getDescription());
    setName(user.getName());
    setPassword(user.getPassword());
    for(RoleInfo role:user.getRoles()) {
      removeFromAvailableRoles(role.getName());
      addToSelectedRoles(new UIRepositoryRole(role));
    }
    
  }
 
  public List<UIRepositoryRole> getAvailableSelectedRoles() {
    return availableSelectedRoles;
  }

  public void setAvailableSelectedRoles(List<Object> availableSelectedRoles) {
    this.availableSelectedRoles.clear();
    for(Object role:availableSelectedRoles) {
      this.availableSelectedRoles.add((UIRepositoryRole) role);
    }
    this.firePropertyChange("availableSelectedRoles", null, this.availableSelectedRoles); //$NON-NLS-1$
  }

  public List<UIRepositoryRole> getAssignedSelectedRoles() {
    return assignedSelectedRoles;
  }

  public void setAssignedSelectedRoles(List<Object> assignedSelectedRoles) {
    this.assignedSelectedRoles.clear();
    for(Object role:assignedSelectedRoles) {
      this.assignedSelectedRoles.add((UIRepositoryRole) role);
    }
    this.firePropertyChange("assignedSelectedRoles", null, this.assignedSelectedRoles); //$NON-NLS-1$
    
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
  public List<UIRepositoryRole> getAvailableRoles() {
    return availableRoles;
  }
  public void setAvailableRoles(List<UIRepositoryRole> availableRoles) {
    List<UIRepositoryRole> previousValue = getPreviousAvailableRoles();
    this.availableRoles.clear();
    if(availableRoles != null) {
      this.availableRoles.addAll(availableRoles);
    }
    this.firePropertyChange("availableRoles", previousValue, this.availableRoles); //$NON-NLS-1$
  }
  public List<UIRepositoryRole> getAssignedRoles() {
    return assignedRoles;
  }
  public void setAssignedRoles(List<UIRepositoryRole> selectedRoles) {
    List<UIRepositoryRole> previousValue = getPreviousSelectedRoles();
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
    setName("");
    setDescription("");
    setPassword("");
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


  private void addToSelectedRoles(UIRepositoryRole roleToAdd) {
    List<UIRepositoryRole> previousValue = getPreviousSelectedRoles();
    assignedRoles.add(roleToAdd);
    this.firePropertyChange("assignedRoles", previousValue, assignedRoles); //$NON-NLS-1$
    if(assignedRoles.size() == 1) {
      setRoleUnassignmentPossible(true);      
    }
    fireRoleUnassignmentPropertyChange();
  }

  private void addToAvailableRoles(UIRepositoryRole roleToAdd) {
    List<UIRepositoryRole> previousValue = getPreviousAvailableRoles();
    availableRoles.add(roleToAdd);
    if(availableRoles.size() == 1) {
      setRoleAssignmentPossible(true);      
    }
    this.firePropertyChange("availableRoles", previousValue, availableRoles); //$NON-NLS-1$
    fireRoleAssignmentPropertyChange();
  }
  private void removeFromAvailableRoles(String roleName) {
    List<UIRepositoryRole> previousValue = getPreviousAvailableRoles();
    availableRoles.remove(getAvailableRole(roleName));
    if(availableRoles.size() == 0) {
      setRoleAssignmentPossible(false);      
    }
    this.firePropertyChange("availableRoles", previousValue, availableRoles); //$NON-NLS-1$
    fireRoleAssignmentPropertyChange();
  }

  private void removeFromSelectedRoles(String roleName) {
    List<UIRepositoryRole> previousValue = getPreviousSelectedRoles();
    assignedRoles.remove(getSelectedRole(roleName));
    if(assignedRoles.size() == 0) {
      setRoleUnassignmentPossible(false);      
    }    
    this.firePropertyChange("assignedRoles", previousValue, assignedRoles); //$NON-NLS-1$
    fireRoleUnassignmentPropertyChange();
  }
  private void fireRoleUnassignmentPropertyChange () {
    if(roleUnassignmentPossible && assignedRoles.size() > 0) {
      this.firePropertyChange("roleUnassignmentPossible", null, true); //$NON-NLS-1$
    } else {
      this.firePropertyChange("roleUnassignmentPossible", null, false); //$NON-NLS-1$
    }
  }
  private void fireRoleAssignmentPropertyChange () {
    if(roleAssignmentPossible && availableRoles.size() > 0) {
      this.firePropertyChange("roleAssignmentPossible", null, true); //$NON-NLS-1$
    } else {
      this.firePropertyChange("roleAssignmentPossible", null, false); //$NON-NLS-1$
    }
  }
  
  private UIRepositoryRole getSelectedRole(String name) {
    for (UIRepositoryRole role : assignedRoles) {
      if (role.getName().equals(name)) {
        return role;
      }
    }
    return null;
  }
  
  private UIRepositoryRole getAvailableRole(String name) {
    for (UIRepositoryRole role : availableRoles) {
      if (role.getName().equals(name)) {
        return role;
      }
    }
    return null;
  }
  
  private List<UIRepositoryRole> getPreviousAvailableRoles() {
    List<UIRepositoryRole> previousValue = new ArrayList<UIRepositoryRole>();
    for (UIRepositoryRole ru : availableRoles) {
      previousValue.add(ru);
    }
    return previousValue;
  }

  private List<UIRepositoryRole> getPreviousSelectedRoles() {
    List<UIRepositoryRole> previousValue = new ArrayList<UIRepositoryRole>();
    for (UIRepositoryRole ru : assignedRoles) {
      previousValue.add(ru);
    }
    return previousValue;
  }
}
