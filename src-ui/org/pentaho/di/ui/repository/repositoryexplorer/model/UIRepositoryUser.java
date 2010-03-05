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
