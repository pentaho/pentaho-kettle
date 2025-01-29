/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/

package org.pentaho.di.ui.repository.pur.repositoryexplorer.model;

import java.util.HashSet;
import java.util.Set;

import org.pentaho.di.repository.IUser;
import org.pentaho.di.repository.pur.model.IRole;
import org.pentaho.di.ui.repository.pur.repositoryexplorer.IUIRole;
import org.pentaho.di.ui.repository.repositoryexplorer.model.IUIUser;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UIObjectCreationException;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UIObjectRegistry;
import org.pentaho.ui.xul.XulEventSourceAdapter;

public class UIRepositoryRole extends XulEventSourceAdapter implements IUIRole, java.io.Serializable {

  private static final long serialVersionUID = 6397782451758458788L; /* EESOURCE: UPDATE SERIALVERUID */

  private IRole rri;

  public UIRepositoryRole() {
  }

  public UIRepositoryRole( IRole rri ) {
    this.rri = rri;
  }

  public String getName() {
    return rri.getName();
  }

  public void setName( String name ) {
    rri.setName( name );
  }

  public String getDescription() {
    return rri.getDescription();
  }

  public void setDescription( String description ) {
    rri.setDescription( description );
  }

  public void setUsers( Set<IUIUser> users ) {
    Set<IUser> rusers = new HashSet<IUser>();
    for ( IUIUser user : users ) {
      rusers.add( user.getUserInfo() );
    }
    rri.setUsers( rusers );
  }

  public Set<IUIUser> getUsers() {
    Set<IUIUser> rusers = new HashSet<IUIUser>();
    for ( IUser userInfo : rri.getUsers() ) {
      try {
        rusers.add( UIObjectRegistry.getInstance().constructUIRepositoryUser( userInfo ) );
      } catch ( UIObjectCreationException e ) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
    return rusers;
  }

  public boolean addUser( IUIUser user ) {
    return rri.addUser( user.getUserInfo() );
  }

  public boolean removeUser( IUIUser user ) {
    return removeUser( user.getUserInfo().getLogin() );
  }

  public void clearUsers() {
    rri.clearUsers();
  }

  public IRole getRole() {
    return rri;
  }

  private boolean removeUser( String userName ) {
    IUser userInfo = null;
    for ( IUser user : rri.getUsers() ) {
      if ( user.getLogin().equals( userName ) ) {
        userInfo = user;
        break;
      }
    }
    if ( userInfo != null ) {
      return rri.removeUser( userInfo );
    } else {
      return false;
    }
  }

  @Override
  public int compareTo( IUIRole o ) {
    return rri.getName().compareTo( o.getName() );
  }
}
