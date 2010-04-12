/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2009 Pentaho Corporation.  All rights reserved.
 */
package org.pentaho.di.ui.repository.repositoryexplorer.model;

import java.util.List;

import org.pentaho.di.repository.RepositorySecurityManager;
import org.pentaho.di.repository.RepositorySecurityProvider;
import org.pentaho.ui.xul.util.AbstractModelNode;


public class UIRepositoryUsers extends AbstractModelNode<IUIUser>{

    
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
