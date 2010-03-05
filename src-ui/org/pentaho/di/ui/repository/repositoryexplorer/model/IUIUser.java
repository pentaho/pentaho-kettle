package org.pentaho.di.ui.repository.repositoryexplorer.model;

import org.pentaho.di.repository.IUser;

public interface IUIUser {
  
  public void setName(String name);
  public String getName();
  public String getDescription();
  public void setDescription(String desc);
  public void setPassword(String pass);
  public String getPassword();
  public IUser getUserInfo();
}
