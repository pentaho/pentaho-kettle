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

import java.util.Set;

import org.pentaho.di.repository.RoleInfo;
import org.pentaho.di.repository.UserInfo;
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

  public boolean addRole(RoleInfo role) {
    return rui.addRole(role);
  }

  public boolean removeRole(RoleInfo role) {
    return rui.removeRole(role);
  }

  public void clearRoles() {
    rui.clearRoles();
  }

  public void setRoles(Set<RoleInfo> roles) {
    rui.setRoles(roles);
  }

  public Set<RoleInfo> getRoles() {
    return rui.getRoles();
  }
}
