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
package org.pentaho.di.repository.pur;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.repository.IUser;
import org.pentaho.di.repository.pur.model.IEEUser;
import org.pentaho.di.repository.pur.model.IRole;
import org.pentaho.di.ui.repository.pur.services.IRoleSupportSecurityManager;
import org.pentaho.platform.api.engine.security.userroledao.UserRoleInfo;
import org.pentaho.platform.security.userroledao.ws.ProxyPentahoRole;
import org.pentaho.platform.security.userroledao.ws.ProxyPentahoUser;
import org.pentaho.platform.security.userroledao.ws.UserRoleSecurityInfo;

public class UserRoleLookupCache implements java.io.Serializable {

  private static final long serialVersionUID = -5922614423981595812L; /* EESOURCE: UPDATE SERIALVERUID */
  Set<IUser> userInfoSet;

  Set<IRole> roleInfoSet;

  IRoleSupportSecurityManager rsm;

  public UserRoleLookupCache( UserRoleInfo userRoleInfo, IRoleSupportSecurityManager rsm ) {
    this.rsm = rsm;
    userInfoSet = new HashSet<IUser>();
    for ( String user : userRoleInfo.getUsers() ) {
      userInfoSet.add( createUserInfo( user ) );
    }
    roleInfoSet = new HashSet<IRole>();
    for ( String role : userRoleInfo.getRoles() ) {
      roleInfoSet.add( createRoleInfo( role ) );
    }

  }

  public UserRoleLookupCache( UserRoleSecurityInfo userRoleSecurityInfo, IRoleSupportSecurityManager rsm ) {
    this.rsm = rsm;
    userInfoSet = new HashSet<IUser>();
    for ( ProxyPentahoUser user : userRoleSecurityInfo.getUsers() ) {
      userInfoSet.add( createUserInfo( user ) );
    }
    roleInfoSet = new HashSet<IRole>();
    for ( ProxyPentahoRole role : userRoleSecurityInfo.getRoles() ) {
      roleInfoSet.add( createRoleInfo( role ) );
    }
  }

  public IUser lookupUser( ProxyPentahoUser proxyUser ) {
    for ( IUser user : userInfoSet ) {
      if ( user.getLogin().equals( proxyUser.getName() ) ) {
        return user;
      }
    }
    return addUserToLookupSet( proxyUser );
  }

  public IRole lookupRole( ProxyPentahoRole proxyRole ) {
    for ( IRole role : roleInfoSet ) {
      if ( role.getName().equals( proxyRole.getName() ) ) {
        return role;
      }
    }
    return addRoleToLookupSet( proxyRole );
  }

  public void insertUserToLookupSet( IUser user ) {
    userInfoSet.add( user );
  }

  public void insertRoleToLookupSet( IRole role ) {
    roleInfoSet.add( role );
  }

  public void updateUserInLookupSet( IUser user ) {
    IUser userInfoToUpdate = null;
    for ( IUser userInfo : userInfoSet ) {
      if ( userInfo.getLogin().equals( user.getLogin() ) ) {
        userInfoToUpdate = userInfo;
        break;
      }
    }
    userInfoToUpdate.setDescription( user.getDescription() );
    if ( !StringUtils.isEmpty( user.getPassword() ) ) {
      userInfoToUpdate.setPassword( user.getPassword() );
    }
    if ( user instanceof IEEUser ) {
      ( (IEEUser) userInfoToUpdate ).setRoles( ( (IEEUser) user ).getRoles() );
    }
  }

  public void updateRoleInLookupSet( IRole role ) {
    IRole roleInfoToUpdate = null;
    for ( IRole roleInfo : roleInfoSet ) {
      if ( roleInfo.getName().equals( role.getName() ) ) {
        roleInfoToUpdate = roleInfo;
        break;
      }
    }
    roleInfoToUpdate.setDescription( role.getDescription() );
    roleInfoToUpdate.setUsers( role.getUsers() );
  }

  public void removeUsersFromLookupSet( List<IUser> users ) {
    for ( IUser user : users ) {
      removeUserFromLookupSet( user );
    }
  }

  public void removeRolesFromLookupSet( List<IRole> roles ) {
    for ( IRole role : roles ) {
      removeRoleFromLookupSet( role );
    }
  }

  private void removeUserFromLookupSet( IUser user ) {
    IUser userToRemove = null;
    for ( IUser userInfo : userInfoSet ) {
      if ( userInfo.getLogin().equals( user.getLogin() ) ) {
        userToRemove = userInfo;
        break;
      }
      userInfoSet.remove( userToRemove );
    }
  }

  private void removeRoleFromLookupSet( IRole role ) {
    IRole roleToRemove = null;
    for ( IRole roleInfo : roleInfoSet ) {
      if ( roleInfo.getName().equals( role.getName() ) ) {
        roleToRemove = roleInfo;
        break;
      }
      roleInfoSet.remove( roleToRemove );
    }
  }

  private IUser addUserToLookupSet( ProxyPentahoUser user ) {
    IUser userInfo = createUserInfo( user );
    userInfoSet.add( userInfo );
    return userInfo;
  }

  private IRole addRoleToLookupSet( ProxyPentahoRole role ) {
    IRole roleInfo = createRoleInfo( role );
    roleInfoSet.add( roleInfo );
    return roleInfo;
  }

  private IUser createUserInfo( ProxyPentahoUser user ) {
    IUser userInfo = null;
    try {
      userInfo = this.rsm.constructUser();
      userInfo.setDescription( user.getDescription() );
      userInfo.setLogin( user.getName() );
      userInfo.setName( user.getName() );
      userInfo.setPassword( user.getPassword() );
    } catch ( KettleException e ) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return userInfo;
  }

  private IRole createRoleInfo( ProxyPentahoRole role ) {
    IRole roleInfo = null;
    try {
      roleInfo = rsm.constructRole();
    } catch ( KettleException e ) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    roleInfo.setDescription( role.getDescription() );
    roleInfo.setName( role.getName() );
    return roleInfo;
  }

  private IUser createUserInfo( String user ) {
    IUser userInfo = null;
    try {
      userInfo = this.rsm.constructUser();
      userInfo.setName( user );
    } catch ( KettleException e ) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return userInfo;
  }

  private IRole createRoleInfo( String role ) {
    IRole roleInfo = null;
    try {
      roleInfo = rsm.constructRole();
    } catch ( KettleException e ) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    roleInfo.setName( role );
    return roleInfo;
  }
}
