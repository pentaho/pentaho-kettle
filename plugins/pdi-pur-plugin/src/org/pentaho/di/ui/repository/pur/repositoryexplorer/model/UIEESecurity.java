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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.pentaho.di.repository.RepositorySecurityManager;
import org.pentaho.di.repository.pur.model.IRole;
import org.pentaho.di.ui.repository.pur.repositoryexplorer.IUIEEUser;
import org.pentaho.di.ui.repository.pur.repositoryexplorer.IUIRole;
import org.pentaho.di.ui.repository.pur.repositoryexplorer.UIEEObjectRegistery;
import org.pentaho.di.ui.repository.pur.services.IRoleSupportSecurityManager;
import org.pentaho.di.ui.repository.repositoryexplorer.model.IUIUser;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UISecurity;

public class UIEESecurity extends UISecurity implements java.io.Serializable {

  private static final long serialVersionUID = -1835729731278502930L; /* EESOURCE: UPDATE SERIALVERUID */

  protected IUIRole selectedRole;

  private int selectedRoleIndex;

  protected List<IUIRole> roleList;

  protected IUIRole selectedSystemRole;

  private int selectedSystemRoleIndex;

  protected List<IUIRole> systemRoleList;

  private static final String AUTHENTICATED = "Authenticated";
  private static final String ANONYMOUS = "Anonymous";

  public UIEESecurity() {
    super();
    roleList = new ArrayList<IUIRole>();
    systemRoleList = new ArrayList<IUIRole>();
  }

  public UIEESecurity( RepositorySecurityManager rsm ) throws Exception {
    super( rsm );
    roleList = new ArrayList<IUIRole>();
    systemRoleList = new ArrayList<IUIRole>();
    if ( rsm instanceof IRoleSupportSecurityManager ) {
      for ( IRole role : ( (IRoleSupportSecurityManager) rsm ).getRoles() ) {
        // For a NON-MANAGED SERVER( I.E LDAP) We don't want to show
        // Authenticated and Anonymous in the Roles List. These will appear in the
        // System Roles Deck
        if ( role.getName().equals( AUTHENTICATED ) || role.getName().equals( ANONYMOUS ) ) {
          continue;
        } else {
          IUIRole newRole = UIEEObjectRegistery.getInstance().constructUIRepositoryRole( role );
          roleList.add( newRole );
        }
      }
    }
    Collections.sort( roleList );
    this.firePropertyChange( "roleList", null, roleList ); //$NON-NLS-1$
    // Hardcoding the System Roles
    IRole authenticatedSystemRole = ( (IRoleSupportSecurityManager) rsm ).constructRole();
    IRole anonymousSystemRole = ( (IRoleSupportSecurityManager) rsm ).constructRole();
    authenticatedSystemRole.setName( AUTHENTICATED );
    authenticatedSystemRole.setDescription( AUTHENTICATED );
    anonymousSystemRole.setName( ANONYMOUS );
    anonymousSystemRole.setDescription( ANONYMOUS );
    systemRoleList.add( UIEEObjectRegistery.getInstance().constructUIRepositoryRole( authenticatedSystemRole ) );
    systemRoleList.add( UIEEObjectRegistery.getInstance().constructUIRepositoryRole( anonymousSystemRole ) );
    Collections.sort( systemRoleList );
    this.firePropertyChange( "systemRoleList", null, systemRoleList ); //$NON-NLS-1$
  }

  public int getSelectedRoleIndex() {
    return selectedRoleIndex;
  }

  public void setSelectedRoleIndex( int selectedRoleIndex ) {
    this.selectedRoleIndex = selectedRoleIndex;
    this.firePropertyChange( "selectedRoleIndex", null, selectedRoleIndex ); //$NON-NLS-1$
  }

  public int getSelectedSystemRoleIndex() {
    return selectedSystemRoleIndex;
  }

  public void setSelectedSystemRoleIndex( int selectedSystemRoleIndex ) {
    this.selectedSystemRoleIndex = selectedSystemRoleIndex;
    this.firePropertyChange( "selectedSystemRoleIndex", null, selectedSystemRoleIndex ); //$NON-NLS-1$
  }

  public IUIRole getSelectedRole() {
    return selectedRole;
  }

  public void setSelectedRole( IUIRole selectedRole ) {
    this.selectedRole = selectedRole;
    this.firePropertyChange( "selectedRole", null, selectedRole ); //$NON-NLS-1$
    setSelectedRoleIndex( getIndexOfRole( selectedRole ) );
  }

  public IUIRole getSelectedSystemRole() {
    return selectedSystemRole;
  }

  public void setSelectedSystemRole( IUIRole selectedSystemRole ) {
    this.selectedSystemRole = selectedSystemRole;
    this.firePropertyChange( "selectedSystemRole", null, selectedSystemRole ); //$NON-NLS-1$
    setSelectedSystemRoleIndex( getIndexOfSystemRole( selectedSystemRole ) );
  }

  public List<IUIRole> getRoleList() {
    return roleList;
  }

  public void setRoleList( List<IUIRole> roleList ) {
    this.roleList.clear();
    this.roleList.addAll( roleList );
    this.firePropertyChange( "roleList", null, roleList ); //$NON-NLS-1$    
  }

  public List<IUIRole> getSystemRoleList() {
    return systemRoleList;
  }

  public void setSystemRoleList( List<IUIRole> systemRoleList ) {
    this.systemRoleList.clear();
    this.systemRoleList.addAll( systemRoleList );
    this.firePropertyChange( "systemRoleList", null, systemRoleList ); //$NON-NLS-1$    
  }

  public void addRole( IUIRole roleToAdd ) {
    roleList.add( roleToAdd );
    Collections.sort( roleList );
    this.firePropertyChange( "roleList", null, roleList ); //$NON-NLS-1$
    // We need to make sure this new role is added to all users as well
    for ( IUIUser userInfo : roleToAdd.getUsers() ) {
      assignRoleToUser( userInfo, roleToAdd );
    }
    setSelectedRole( roleToAdd );
  }

  public void updateUser( IUIUser userToUpdate, Set<IUIRole> previousRoleList ) {
    IUIUser user = getUser( userToUpdate.getName() );
    user.setDescription( userToUpdate.getDescription() );
    if ( userToUpdate instanceof IUIEEUser ) {
      ( (IUIEEUser) user ).setRoles( ( (IUIEEUser) userToUpdate ).getRoles() );
    }
    this.firePropertyChange( "userList", null, userList ); //$NON-NLS-1$
    /*
     * Now update this current user to all the roles. So if any roles were added to this user then add the user to the
     * roles as well. Similarly if some roles were remove from this user, remove the user from those roles.
     */
    updateUserInRoles( userToUpdate, previousRoleList, ( (IUIEEUser) userToUpdate ).getRoles() );
    setSelectedUser( user );
  }

  public void updateRole( IUIRole roleToUpdate, Set<IUIUser> previousUserList ) {
    IUIRole role = getRole( roleToUpdate.getName() );
    role.setDescription( roleToUpdate.getDescription() );
    role.setUsers( roleToUpdate.getUsers() );
    this.firePropertyChange( "roleList", null, roleList ); //$NON-NLS-1$
    /*
     * Now update this current role to all the users. So if any users were added to this role then add the role to the
     * users as well. Similarly if some users were remove from this role, remove the role from those users.
     */
    updateRoleInUsers( roleToUpdate, previousUserList, roleToUpdate.getUsers() );
    setSelectedRole( role );
  }

  public void removeRole( String name ) {
    removeRole( getRole( name ) );
  }

  public void removeRole( IUIRole roleToRemove ) {
    int index = getIndexOfRole( roleToRemove );
    roleList.remove( roleToRemove );
    this.firePropertyChange( "roleList", null, roleList ); //$NON-NLS-1$
    // We need to make sure this new role is added to all users as well
    for ( IUIUser userInfo : roleToRemove.getUsers() ) {
      unassignRoleFromUser( userInfo, roleToRemove );
    }
    if ( index - 1 >= 0 ) {
      setSelectedRole( getRoleAtIndex( index - 1 ) );
    }

  }

  @Override
  public void addUser( IUIUser userToAdd ) {
    userList.add( userToAdd );
    Collections.sort( userList );
    this.firePropertyChange( "userList", null, userList ); //$NON-NLS-1$
    // We need to make sure this recently removed user is removed from all roles as well
    if ( userToAdd instanceof IUIEEUser ) {
      for ( IUIRole role : ( (IUIEEUser) userToAdd ).getRoles() ) {
        assignUserToRole( role, userToAdd );
      }
    }
    setSelectedUser( userToAdd );
  }

  @Override
  public void removeUser( IUIUser userToRemove ) {
    int index = getIndexOfUser( userToRemove );
    userList.remove( userToRemove );
    this.firePropertyChange( "userList", null, userList ); //$NON-NLS-1$
    // We need to make sure this recently removed user is removed to all roles as well
    if ( userToRemove instanceof IUIEEUser ) {
      for ( IUIRole role : ( (IUIEEUser) userToRemove ).getRoles() ) {
        unassignUserFromRole( role, userToRemove );
      }
    }
    if ( index - 1 >= 0 ) {
      setSelectedUser( getUserAtIndex( index - 1 ) );
    }
  }

  private void updateUserInRoles( IUIUser user, Set<IUIRole> userRolesBeforeUpdate, Set<IUIRole> userRolesAfterUpdate ) {
    // unassign user from the roles which were unassigned
    for ( IUIRole role : userRolesBeforeUpdate ) {
      if ( !exist( role, userRolesAfterUpdate ) ) {
        unassignUserFromRole( role, user );
      }
    }
    // assign user to the roles which were assigned
    for ( IUIRole role : userRolesAfterUpdate ) {
      if ( !exist( role, userRolesBeforeUpdate ) ) {
        assignUserToRole( role, user );
      }
    }
  }

  private boolean exist( IUIRole role, Set<IUIRole> roleSet ) {
    for ( IUIRole roleInfo : roleSet ) {
      if ( role.getName().equals( roleInfo.getName() ) ) {
        return true;
      }
    }
    return false;
  }

  private void updateRoleInUsers( IUIRole role, Set<IUIUser> roleUsersBeforeUpdate, Set<IUIUser> roleUsersAfterUpdate ) {
    // unassign user from the roles which were unassigned
    for ( IUIUser userInfo : roleUsersBeforeUpdate ) {
      if ( !exist( userInfo, roleUsersAfterUpdate ) ) {
        unassignRoleFromUser( userInfo, role );
      }
    }
    // assign user to the roles which were assigned
    for ( IUIUser userInfo : roleUsersAfterUpdate ) {
      if ( !exist( userInfo, roleUsersBeforeUpdate ) ) {
        assignRoleToUser( userInfo, role );
      }
    }
  }

  private boolean exist( IUIUser ruser, Set<IUIUser> users ) {
    for ( IUIUser user : users ) {
      if ( user.getName().equals( ruser.getName() ) ) {
        return true;
      }
    }
    return false;
  }

  private IUIRole getRole( String name ) {
    for ( IUIRole role : roleList ) {
      if ( role.getName().equals( name ) ) {
        return role;
      }
    }
    return null;
  }

  public void removeRolesFromSelectedUser( Collection<Object> roles ) {
    for ( Object o : roles ) {
      UIRepositoryRole role = (UIRepositoryRole) o;
      removeRoleFromSelectedUser( role.getName() );
    }
    this.firePropertyChange( "selectedUser", null, selectedUser ); //$NON-NLS-1$
  }

  private void removeRoleFromSelectedUser( String roleName ) {
    IUIRole role = findRoleInSelectedUser( roleName );
    if ( selectedUser instanceof IUIEEUser ) {
      ( (IUIEEUser) selectedUser ).removeRole( role );
    }
    unassignUserFromRole( role, selectedUser );
  }

  public void removeUsersFromSelectedRole( Collection<Object> users ) {
    for ( Object o : users ) {
      IUIUser user = (IUIUser) o;
      removeUserFromSelectedRole( user.getName() );
    }
    this.firePropertyChange( "selectedRole", null, selectedRole ); //$NON-NLS-1$
  }

  private void removeUserFromSelectedRole( String userName ) {
    IUIUser user = findUserInSelectedRole( userName );
    selectedRole.removeUser( user );
    unassignRoleFromUser( user, selectedRole );
  }

  private IUIRole findRoleInSelectedUser( String roleName ) {
    if ( selectedUser instanceof IUIEEUser ) {
      Set<IUIRole> roles = ( (IUIEEUser) selectedUser ).getRoles();
      for ( IUIRole role : roles ) {
        if ( role.getName().equals( roleName ) ) {
          return role;
        }
      }
    }
    return null;
  }

  private IUIUser findUserInSelectedRole( String userName ) {
    Set<IUIUser> users = selectedRole.getUsers();
    for ( IUIUser user : users ) {
      if ( user.getName().equals( userName ) ) {
        return user;
      }
    }
    return null;
  }

  private IUIUser getUserAtIndex( int index ) {
    return this.userList.get( index );
  }

  private int getIndexOfUser( IUIUser ru ) {
    for ( int i = 0; i < this.userList.size(); i++ ) {
      IUIUser user = this.userList.get( i );
      if ( ru.getName().equals( user.getName() ) ) {
        return i;
      }
    }
    return -1;
  }

  private IUIRole getRoleAtIndex( int index ) {
    return this.roleList.get( index );
  }

  protected int getIndexOfRole( IUIRole rr ) {
    for ( int i = 0; i < this.roleList.size(); i++ ) {
      IUIRole role = this.roleList.get( i );
      if ( rr.getName().equals( role.getName() ) ) {
        return i;
      }
    }
    return -1;
  }

  protected int getIndexOfSystemRole( IUIRole rr ) {
    for ( int i = 0; i < this.systemRoleList.size(); i++ ) {
      IUIRole role = this.systemRoleList.get( i );
      if ( rr.getName().equals( role.getName() ) ) {
        return i;
      }
    }
    return -1;
  }

  private void assignRoleToUser( IUIUser userInfo2, IUIRole role ) {
    IUIEEUser userInfo = findEEUser( userInfo2 );
    if ( userInfo != null ) {
      userInfo.addRole( role );
    }
  }

  private void unassignRoleFromUser( IUIUser user, IUIRole role ) {
    IUIEEUser userInfo = findEEUser( user );
    if ( userInfo != null ) {
      userInfo.removeRole( role );
    }
  }

  private void assignUserToRole( IUIRole role, IUIUser user ) {
    IUIRole roleInfo = findRole( role );
    if ( roleInfo != null ) {
      roleInfo.addUser( user );
    }
  }

  private void unassignUserFromRole( IUIRole role, IUIUser user ) {
    IUIRole roleInfo = findRole( role );
    if ( roleInfo != null ) {
      roleInfo.removeUser( user );
    }
  }

  private IUIEEUser findEEUser( IUIUser userInfo ) {
    for ( IUIUser user : userList ) {
      if ( user.getName().equals( userInfo.getName() ) && user instanceof IUIEEUser ) {
        return (IUIEEUser) user;
      }
    }
    return null;
  }

  private IUIRole findRole( IUIRole role ) {
    for ( IUIRole roleInfo : roleList ) {
      if ( roleInfo.getName().equals( role.getName() ) ) {
        return roleInfo;
      }
    }
    return null;
  }
}
