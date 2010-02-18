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

import java.util.HashSet;
import java.util.Set;

import org.pentaho.di.repository.IRole;
import org.pentaho.di.repository.UserInfo;
import org.pentaho.di.ui.repository.repositoryexplorer.UIObjectCreationException;
import org.pentaho.ui.xul.XulEventSourceAdapter;

public class UIRepositoryUser extends XulEventSourceAdapter {

  private UserInfo rui;

  public UIRepositoryUser() {
  }

  public UIRepositoryUser(UserInfo rui) {
    this.rui = rui;
  }

  public void setName(String name) {
    rui.setLogin(name);
  }

  public String getName() {
    return rui.getLogin();
  }

  public String getDescription() {
    return rui.getDescription();
  }

  public void setDescription(String desc) {
    rui.setDescription(desc);
  }

  public void setPassword(String pass) {
    rui.setPassword(pass);
  }

  public String getPassword() {
    return rui.getPassword();
  }

  public UserInfo getUserInfo() {
    return rui;
  }

  public boolean addRole(IUIRole role) {
    return rui.addRole(role.getRole());
  }

  public boolean removeRole(IUIRole role) {
    return removeRole(role.getRole().getName());
  }

  public void clearRoles() {
    rui.clearRoles();
  }

  public void setRoles(Set<IUIRole> roles) {
    Set<IRole> roleSet = new HashSet<IRole>();
    for(IUIRole role:roles) {
      roleSet.add(role.getRole());
    }
    rui.setRoles(roleSet);
    
  }

  public Set<IUIRole> getRoles() {
    Set<IUIRole> rroles = new HashSet<IUIRole>();
    for(IRole role:rui.getRoles()) {
      try {
        rroles.add(UIObjectRegistery.getInstance().constructUIRepositoryRole(role));
      } catch(UIObjectCreationException uex) {
        
      }
    }
    return rroles;
  }
  
  public boolean equals(Object o) {
    return ((o instanceof UIRepositoryUser) ? getName().equals(((UIRepositoryUser) o).getName()) : false);
  }

  public int hashCode() {
    return getName().hashCode();
  }
  
  private boolean removeRole(String roleName) {
    IRole roleInfo = null;
    for(IRole role:rui.getRoles()) {
      if(role.getName().equals(roleName)) {
        roleInfo = role;
        break;
      }
    }
    if(roleInfo != null) {
      return rui.removeRole(roleInfo);
    } else {
      return false;
    }
  }
}
