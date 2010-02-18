package org.pentaho.di.ui.repository.repositoryexplorer.model;

import java.util.Set;

import org.pentaho.di.repository.IRole;

public interface IUIRole {

  public void setName(String name);
  public String getName();
  public String getDescription();
  public void setDescription(String description);
  public void setUsers(Set<UIRepositoryUser> users);
  public Set<UIRepositoryUser> getUsers();
  public boolean addUser(UIRepositoryUser user);
  public boolean removeUser(UIRepositoryUser user);
  public void clearUsers();
  public IRole getRole();
}
