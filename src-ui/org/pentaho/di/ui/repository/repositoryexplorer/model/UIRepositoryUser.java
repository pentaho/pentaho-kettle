package org.pentaho.di.ui.repository.repositoryexplorer.model;

import org.pentaho.di.repository.UserInfo;
import org.pentaho.ui.xul.XulEventSourceAdapter;

public class UIRepositoryUser extends XulEventSourceAdapter{

  private UserInfo rui;
  
  public UIRepositoryUser() {
  }
  
  public UIRepositoryUser(UserInfo rui) {
    this.rui = rui;
  }

  public void setLogin(String name){
    rui.setLogin(name);
  }
  
  public String getLogin(){
    return rui.getLogin();
  }

  public String getDescription(){
    return rui.getDescription();
  }
  
  public void setDescription(String desc){
    rui.setLogin(desc);
  }
  
  public void setPassword(String pass){
    rui.setPassword(pass);
  }
  
  public String getPassword(){
    return rui.getPassword();
  }
  
  public UserInfo getUserInfo(){
    return rui;
  }
}
