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

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.repository.IUser;
import org.pentaho.di.repository.RepositorySecurityManager;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UISecurity.Mode;
import org.pentaho.ui.xul.XulEventSourceAdapter;

public class UISecurityUser extends XulEventSourceAdapter {

  private Mode mode;
  private String name;
  private String description;
  private String password;
  protected RepositorySecurityManager rsm;

  public UISecurityUser( RepositorySecurityManager rsm ) {
    this.description = null;
    this.name = null;
    this.password = null;
    this.rsm = rsm;
  }

  public void setUser( IUIUser user ) throws Exception {
    setDescription( user.getDescription() );
    setName( user.getName() );
    // Show empty password on the client site
    setPassword( "" );
  }

  public UISecurityUser getUISecurityUser() {
    return this;
  }

  public Mode getMode() {
    return mode;
  }

  public void setMode( Mode mode ) {
    this.mode = mode;
    this.firePropertyChange( "mode", null, mode );
  }

  public String getName() {
    return name;
  }

  public void setName( String name ) {
    String previousValue = this.name;
    this.name = name;
    this.firePropertyChange( "name", previousValue, name );
  }

  public String getDescription() {
    return description;
  }

  public void setDescription( String description ) {
    String previousValue = this.description;
    this.description = description;
    this.firePropertyChange( "description", previousValue, description );
  }

  public String getPassword() {
    return password;
  }

  public void setPassword( String password ) {
    String previousValue = this.password;
    this.password = password;
    this.firePropertyChange( "password", previousValue, password );
  }

  public void clear() {
    setMode( Mode.ADD );
    setName( "" );
    setDescription( "" );
    setPassword( "" );
  }

  public IUser getUserInfo() throws KettleException {
    IUser userInfo = rsm.constructUser();
    userInfo.setDescription( description );
    userInfo.setLogin( name );
    userInfo.setName( name );
    userInfo.setUsername( name );
    userInfo.setPassword( password );
    return userInfo;
  }
}
