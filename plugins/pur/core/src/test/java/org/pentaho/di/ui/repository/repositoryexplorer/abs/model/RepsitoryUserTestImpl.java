/*!
 * Copyright 2010 - 2017 Hitachi Vantara.  All rights reserved.
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
package org.pentaho.di.ui.repository.repositoryexplorer.abs.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.repository.IUser;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.RepositorySecurityManager;
import org.pentaho.di.repository.pur.model.AbsRoleInfo;
import org.pentaho.di.repository.pur.model.EEUserInfo;
import org.pentaho.di.repository.pur.model.IRole;
import org.pentaho.di.ui.repository.pur.repositoryexplorer.IUIRole;
import org.pentaho.di.ui.repository.pur.repositoryexplorer.abs.IUIAbsRole;
import org.pentaho.di.ui.repository.pur.repositoryexplorer.abs.model.UIAbsRepositoryRole;
import org.pentaho.di.ui.repository.pur.repositoryexplorer.model.UIEESecurity;
import org.pentaho.di.ui.repository.pur.services.IAbsSecurityManager;
import org.pentaho.di.ui.repository.pur.services.IRoleSupportSecurityManager;
import org.pentaho.di.ui.repository.repositoryexplorer.model.IUIUser;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UIRepositoryUser;

public class RepsitoryUserTestImpl implements RepositorySecurityManager, IRoleSupportSecurityManager,
    IAbsSecurityManager, java.io.Serializable {

  static final long serialVersionUID = -3031163963776739401L; /* EESOURCE: UPDATE SERIALVERUID */

  private List<IUser> users;
  private List<IRole> roles;
  private List<IRole> systemRoles;
  private UIEESecurity security;

  public RepsitoryUserTestImpl() {
    users = new ArrayList<IUser>();
    roles = new ArrayList<IRole>();
    systemRoles = new ArrayList<IRole>();
    security = new UIEESecurity();
    List<IUIRole> rroles;
    List<IUIUser> rusers;
    EEUserInfo joeUser;
    EEUserInfo patUser;
    EEUserInfo suzyUser;
    EEUserInfo tiffanyUser;
    IRole adminRole = new AbsRoleInfo( "Admin", "Super User" );
    IRole anonymousRole = new AbsRoleInfo( "Anonymous", "User has not logged in" );
    IRole authenticatedRole = new AbsRoleInfo( "Authenticated", "User has logged in" );
    IRole ceoRole = new AbsRoleInfo( "ceo", "Chief Executive Officer" );
    IRole ctoRole = new AbsRoleInfo( "cto", "Chief Technology Officer" );
    IRole devRole = new AbsRoleInfo( "dev", "Developer" );
    IRole devmgrRole = new AbsRoleInfo( "devmgr", "Development Manager" );
    IRole isRole = new AbsRoleInfo( "is", "Information Services" );
    roles.add( adminRole );
    roles.add( ceoRole );
    roles.add( ctoRole );
    roles.add( devRole );
    roles.add( devmgrRole );
    roles.add( isRole );

    systemRoles.add( anonymousRole );
    systemRoles.add( authenticatedRole );

    joeUser = new EEUserInfo( "joe", "password", "joe", "joe", true );
    patUser = new EEUserInfo( "pat", "password", "pat", "pat", true );
    suzyUser = new EEUserInfo( "suzy", "password", "suzy", "suzy", true );
    tiffanyUser = new EEUserInfo( "tiffany", "password", "tiffany", "tiffany", true );

    joeUser.addRole( roles.get( 0 ) );
    joeUser.addRole( roles.get( 2 ) );
    joeUser.addRole( roles.get( 3 ) );

    suzyUser.addRole( roles.get( 2 ) );
    suzyUser.addRole( roles.get( 4 ) );
    suzyUser.addRole( roles.get( 1 ) );

    patUser.addRole( roles.get( 2 ) );
    patUser.addRole( roles.get( 4 ) );

    tiffanyUser.addRole( roles.get( 1 ) );
    tiffanyUser.addRole( roles.get( 2 ) );
    tiffanyUser.addRole( roles.get( 4 ) );

    adminRole.addUser( joeUser );
    adminRole.addUser( patUser );

    anonymousRole.addUser( tiffanyUser );

    authenticatedRole.addUser( joeUser );
    authenticatedRole.addUser( patUser );
    authenticatedRole.addUser( suzyUser );
    authenticatedRole.addUser( tiffanyUser );

    ceoRole.addUser( joeUser );

    ctoRole.addUser( patUser );

    devmgrRole.addUser( joeUser );
    devmgrRole.addUser( patUser );

    isRole.addUser( joeUser );
    isRole.addUser( suzyUser );

    users.add( joeUser );
    users.add( patUser );
    users.add( suzyUser );
    users.add( tiffanyUser );
    rroles = new ArrayList<IUIRole>();
    for ( IRole roleInfo : roles ) {
      IUIAbsRole role = new UIAbsRepositoryRole( roleInfo );
      rroles.add( role );
    }
    rusers = new ArrayList<IUIUser>();
    for ( IUser eEUserInfo : users ) {
      rusers.add( new UIRepositoryUser( eEUserInfo ) );
    }
    security.setUserList( rusers );
    security.setRoleList( rroles );
  }

  public IRole constructRole() throws KettleException {

    return new AbsRoleInfo();
  }

  public void createRole( IRole arg0 ) throws KettleException {
    // TODO Auto-generated method stub

  }

  public void delUser( ObjectId arg0 ) throws KettleException {
    // TODO Auto-generated method stub

  }

  public void delUser( String arg0 ) throws KettleException {
    // TODO Auto-generated method stub

  }

  public void deleteRole( String arg0 ) throws KettleException {
    // TODO Auto-generated method stub

  }

  public void deleteRoles( List<IRole> arg0 ) throws KettleException {
    // TODO Auto-generated method stub

  }

  public void deleteUsers( List<IUser> arg0 ) throws KettleException {
    // TODO Auto-generated method stub

  }

  public List<String> getAllRoles() throws KettleException {
    // TODO Auto-generated method stub
    return null;
  }

  public List<String> getAllUsers() throws KettleException {
    // TODO Auto-generated method stub
    return null;
  }

  public IRole getRole( String arg0 ) throws KettleException {
    // TODO Auto-generated method stub
    return null;
  }

  public List<IRole> getRoles() throws KettleException {
    // TODO Auto-generated method stub
    return roles;
  }

  public ObjectId getUserID( String arg0 ) throws KettleException {
    // TODO Auto-generated method stub
    return null;
  }

  public ObjectId[] getUserIDs() throws KettleException {
    // TODO Auto-generated method stub
    return null;
  }

  public IUser getUserInfo() {
    // TODO Auto-generated method stub
    return null;
  }

  public String[] getUserLogins() throws KettleException {
    // TODO Auto-generated method stub
    return null;
  }

  public List<IUser> getUsers() throws KettleException {
    // TODO Auto-generated method stub
    return users;
  }

  public IUser loadUserInfo( String arg0 ) throws KettleException {
    // TODO Auto-generated method stub
    return null;
  }

  public IUser loadUserInfo( String arg0, String arg1 ) throws KettleException {
    // TODO Auto-generated method stub
    return null;
  }

  public void renameUser( ObjectId arg0, String arg1 ) throws KettleException {
    // TODO Auto-generated method stub

  }

  public void saveUserInfo( IUser arg0 ) throws KettleException {
    // TODO Auto-generated method stub

  }

  public void setRoles( List<IRole> arg0 ) throws KettleException {
    // TODO Auto-generated method stub

  }

  public void setUserInfo( IUser arg0 ) {
    // TODO Auto-generated method stub

  }

  public void setUsers( List<IUser> arg0 ) throws KettleException {
    // TODO Auto-generated method stub

  }

  public void updateRole( IRole arg0 ) throws KettleException {
    // TODO Auto-generated method stub

  }

  public void updateUser( IUser arg0 ) throws KettleException {
    // TODO Auto-generated method stub

  }

  public IUser constructUser() throws KettleException {
    // TODO Auto-generated method stub
    return new EEUserInfo();
  }

  public List<IRole> getDefaultRoles() throws KettleException {
    // TODO Auto-generated method stub
    return null;
  }

  public boolean isManaged() throws KettleException {
    // TODO Auto-generated method stub
    return false;
  }

  public Map<String, String> getAllLogicalRoles( String locale ) throws KettleException {
    // TODO Auto-generated method stub
    return null;
  }

  public List<String> getLogicalRoles( String runtimeRole ) throws KettleException {
    for ( IRole role : roles ) {
      if ( role.getName().equals( runtimeRole ) ) {
        return ( (AbsRoleInfo) role ).getLogicalRoles();
      }
    }
    for ( IRole role : systemRoles ) {
      if ( role.getName().equals( runtimeRole ) ) {
        return ( (AbsRoleInfo) role ).getLogicalRoles();
      }
    }
    return null;
  }

  public void initialize( String locale ) throws KettleException {
    // TODO Auto-generated method stub

  }

  public void setLogicalRoles( String rolename, List<String> logicalRoles ) throws KettleException {
    boolean done = false;
    for ( IRole role : roles ) {
      if ( role.getName().equals( rolename ) ) {
        ( (AbsRoleInfo) role ).setLogicalRoles( logicalRoles );
        done = true;
      }
    }
    if ( !done ) {
      for ( IRole role : systemRoles ) {
        if ( role.getName().equals( rolename ) ) {
          ( (AbsRoleInfo) role ).setLogicalRoles( logicalRoles );
          done = true;
        }
      }
    }
  }

}
