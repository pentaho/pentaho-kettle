package org.pentaho.di.ui.repository.repositoryexplorer.model;

import java.util.EnumSet;
import java.util.Set;

import org.pentaho.di.repository.ActionPermission;
import org.pentaho.di.repository.IActionPermission;
import org.pentaho.di.repository.RoleInfo;
import org.pentaho.di.repository.UserInfo;
import org.pentaho.ui.xul.XulEventSourceAdapter;

public class UIRepositoryRole extends XulEventSourceAdapter implements IActionPermission{

  private RoleInfo rri;

  public UIRepositoryRole() {
   rri = new RoleInfo();
  }

  public UIRepositoryRole(RoleInfo rri) {
    this.rri = rri;
  }
  
  public UIRepositoryRole(String name) {
    this(name, null);
  }

  public UIRepositoryRole(String name, String description) {
    this();
    setName(name);
    setDescription(description);
  }

  public UIRepositoryRole(String name, String description, Set<UserInfo> users) {
    this(name, description);
    setUsers(users);
  }

  public UIRepositoryRole(String name, String description, Set<UserInfo> users, EnumSet<ActionPermission> actionPermissions) {
    this(name, description, users);
    setActionPermissions(actionPermissions);
  }
  public String getName() {
    return rri.getName();
  }

  public void setName(String name) {
    rri.setName(name);
  }

  public String getDescription() {
    return rri.getDescription();
  }

  public void setDescription(String description) {
    rri.setDescription(description);
  }

  public void setUsers(Set<UserInfo> users) {
    rri.setUsers(users);
  }

  public Set<UserInfo> getUsers() {
    return rri.getUsers();
  }

  public boolean addUser(UserInfo user) {
    return rri.addUser(user);
  }

  public boolean removeUser(UserInfo user) {
    return rri.removeUser(user);
  }

  public void clearUsers() {
    rri.clearUsers();
  }

  public EnumSet<ActionPermission> getActionPermissions() {
    return rri.getActionPermissions();
  }

  public void setActionPermissions(EnumSet<ActionPermission> actionPermissions) {
    rri.setActionPermissions(actionPermissions);
  }

  public void addActionPermission(ActionPermission permission) {
    rri.addActionPermission(permission);
  }

  public void removeActionPermission(ActionPermission permission) {
    rri.removeActionPermission(permission);
  }

  public RoleInfo getRoleInfo() {
    return rri;
  }

  public boolean equals(Object o) {
    return ((o instanceof UIRepositoryRole) ? getName().equals(((UIRepositoryRole) o).getName()) : false);
  }

  public int hashCode() {
    return getName().hashCode();
  }
}
