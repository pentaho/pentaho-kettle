/*!
 * Copyright 2010 - 2015 Pentaho Corporation.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.pentaho.di.ui.repository.pur.repositoryexplorer.model;

import java.util.HashSet;
import java.util.Set;

import org.pentaho.di.repository.IUser;
import org.pentaho.di.repository.pur.model.IEEUser;
import org.pentaho.di.repository.pur.model.IRole;
import org.pentaho.di.ui.repository.pur.repositoryexplorer.IUIEEUser;
import org.pentaho.di.ui.repository.pur.repositoryexplorer.IUIRole;
import org.pentaho.di.ui.repository.pur.repositoryexplorer.UIEEObjectRegistery;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UIObjectCreationException;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UIRepositoryUser;

public class UIEERepositoryUser extends UIRepositoryUser implements IUIEEUser, java.io.Serializable {

  private static final long serialVersionUID = -4653578043082025692L; /* EESOURCE: UPDATE SERIALVERUID */
  private IEEUser eeUser;

  public UIEERepositoryUser() {
    super();
    // TODO Auto-generated constructor stub
  }

  public UIEERepositoryUser( IUser user ) {
    super( user );
    if ( user instanceof IEEUser ) {
      eeUser = (IEEUser) user;
    }
  }

  public boolean addRole( IUIRole role ) {
    return eeUser.addRole( role.getRole() );
  }

  public boolean removeRole( IUIRole role ) {
    return removeRole( role.getRole().getName() );
  }

  public void clearRoles() {
    eeUser.clearRoles();
  }

  public void setRoles( Set<IUIRole> roles ) {
    Set<IRole> roleSet = new HashSet<IRole>();
    for ( IUIRole role : roles ) {
      roleSet.add( role.getRole() );
    }
    eeUser.setRoles( roleSet );
  }

  public Set<IUIRole> getRoles() {
    Set<IUIRole> rroles = new HashSet<IUIRole>();
    for ( IRole role : eeUser.getRoles() ) {
      try {
        rroles.add( UIEEObjectRegistery.getInstance().constructUIRepositoryRole( role ) );
      } catch ( UIObjectCreationException uex ) {

      }
    }
    return rroles;
  }

  private boolean removeRole( String roleName ) {
    IRole roleInfo = null;
    for ( IRole role : eeUser.getRoles() ) {
      if ( role.getName().equals( roleName ) ) {
        roleInfo = role;
        break;
      }
    }
    if ( roleInfo != null ) {
      return eeUser.removeRole( roleInfo );
    } else {
      return false;
    }
  }

  @Override
  public String toString() {
    return eeUser.getLogin();
  }
}
