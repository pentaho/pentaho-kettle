package org.pentaho.di.repository;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

public class RoleInfo implements IActionPermission{

  public static final String REPOSITORY_ELEMENT_TYPE = "role"; //$NON-NLS-1$

  // ~ Instance fields
  // =================================================================================================

  private String name;

  private String description;

  private Set<UserInfo> users;

  private EnumSet<ActionPermission> actionPermissions;

  // ~ Constructors
  // ====================================================================================================

  public RoleInfo() {
    this.name = null;
    this.description = null;
    users = new HashSet<UserInfo>();
    this.actionPermissions = EnumSet.noneOf(ActionPermission.class);
  }

  public void setName(String name) {
    this.name = name;
  }

  public RoleInfo(String name) {
    this(name, null);
  }

  public RoleInfo(String name, String description) {
    this();
    this.name = name;
    this.description = description;
  }

  public RoleInfo(String name, String description, Set<UserInfo> users) {
    this(name, description);
    this.users = users;
  }

  public RoleInfo(String name, String description, Set<UserInfo> users, EnumSet<ActionPermission> actionPermissions) {
    this(name, description, users);
    this.actionPermissions = actionPermissions;
  }

  // ~ Methods
  // =========================================================================================================

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public void setUsers(Set<UserInfo> users) {
    this.users = users;
  }

  public Set<UserInfo> getUsers() {
    return users;
  }

  public boolean addUser(UserInfo user) {
    return users.add(user);
  }

  public boolean removeUser(UserInfo user) {
    return users.remove(user);
  }

  public void clearUsers() {
    users.clear();
  }

  public void addActionPermission(ActionPermission actionPermission) {
    this.actionPermissions.add(actionPermission);
  }

  public void removeActionPermission(ActionPermission actionPermission) {
    this.actionPermissions.remove(actionPermission);
  }

  public void setActionPermissions(EnumSet<ActionPermission> actionPermissions) {
    this.actionPermissions = actionPermissions;
  }

  public EnumSet<ActionPermission> getActionPermissions() {
    return actionPermissions;
  }
}
