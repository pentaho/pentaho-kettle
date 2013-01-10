/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.ui.repository.repositoryexplorer.model;

import org.pentaho.di.repository.IUser;
import org.pentaho.ui.xul.XulEventSourceAdapter;

public class UIRepositoryUser extends XulEventSourceAdapter implements IUIUser{

  protected IUser user;

  public UIRepositoryUser() {
  }

  public UIRepositoryUser(IUser user) {
    this.user = user;
  }

  public void setName(String name) {
    user.setLogin(name);
  }

  public String getName() {
    return user.getLogin();
  }

  public String getDescription() {
    return user.getDescription();
  }

  public void setDescription(String desc) {
    user.setDescription(desc);
  }

  public void setPassword(String pass) {
    user.setPassword(pass);
  }

  public String getPassword() {
    return user.getPassword();
  }

  public IUser getUserInfo() {
    return user;
  }
  
  public boolean equals(Object o) {
    return ((o instanceof UIRepositoryUser) ? getName().equals(((UIRepositoryUser) o).getName()) : false);
  }

}
