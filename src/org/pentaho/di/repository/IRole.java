package org.pentaho.di.repository;

import java.util.Set;

public interface IRole {

  public void setName(String name);
  public String getName();
  public String getDescription();
  public void setDescription(String description);
  public void setUsers(Set<UserInfo> users);
  public Set<UserInfo> getUsers();
  public boolean addUser(UserInfo user);
  public boolean removeUser(UserInfo user);
  public void clearUsers();
  public IRole getRole();
}
