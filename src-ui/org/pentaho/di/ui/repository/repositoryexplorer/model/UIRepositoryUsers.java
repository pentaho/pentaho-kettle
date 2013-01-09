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

import java.util.List;

import org.pentaho.di.repository.RepositorySecurityManager;
import org.pentaho.di.repository.RepositorySecurityProvider;
import org.pentaho.ui.xul.util.AbstractModelNode;


public class UIRepositoryUsers extends AbstractModelNode<IUIUser>{

  private static final long serialVersionUID = -8505587648560630174L;

  public UIRepositoryUsers(){
  }
  
  public UIRepositoryUsers(List<IUIUser> users){
    super(users);
  }

  public UIRepositoryUsers(RepositorySecurityProvider rsp, RepositorySecurityManager rsm) {

    String[] logins; 
    try {
      logins = rsp.getUserLogins();
      for (String login : logins) {
        this.add(UIObjectRegistry.getInstance().constructUIRepositoryUser(rsm.loadUserInfo(login)));
      }
    } catch (Exception e) {
      // TODO: handle exception; can't get users???
    }
  }
  
  @Override
  protected void fireCollectionChanged() {
    this.changeSupport.firePropertyChange("children", null, this);
  }
  
}
