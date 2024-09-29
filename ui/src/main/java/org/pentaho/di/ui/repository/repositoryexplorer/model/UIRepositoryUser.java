/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


package org.pentaho.di.ui.repository.repositoryexplorer.model;

import org.pentaho.di.repository.IUser;
import org.pentaho.ui.xul.XulEventSourceAdapter;

public class UIRepositoryUser extends XulEventSourceAdapter implements IUIUser {

  protected IUser user;

  public UIRepositoryUser() {
  }

  public UIRepositoryUser( IUser user ) {
    this.user = user;
  }

  public void setName( String name ) {
    user.setLogin( name );
  }

  public String getName() {
    return user.getLogin();
  }

  public String getDescription() {
    return user.getDescription();
  }

  public void setDescription( String desc ) {
    user.setDescription( desc );
  }

  public void setPassword( String pass ) {
    user.setPassword( pass );
  }

  public String getPassword() {
    return user.getPassword();
  }

  public IUser getUserInfo() {
    return user;
  }

  public boolean equals( Object o ) {
    return ( ( o instanceof UIRepositoryUser ) ? getName().equals( ( (UIRepositoryUser) o ).getName() ) : false );
  }

  @Override
  public String toString() {
    return getName();
  }

  @Override
  public int compareTo( IUIUser o ) {
    return user.getLogin().compareTo( o.getUserInfo().getLogin() );
  }
}
