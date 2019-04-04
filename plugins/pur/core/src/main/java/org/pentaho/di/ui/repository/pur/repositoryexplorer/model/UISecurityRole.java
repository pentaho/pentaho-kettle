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
package org.pentaho.di.ui.repository.pur.repositoryexplorer.model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.repository.pur.model.IRole;
import org.pentaho.di.ui.repository.pur.repositoryexplorer.IUIRole;
import org.pentaho.di.ui.repository.pur.services.IRoleSupportSecurityManager;
import org.pentaho.di.ui.repository.repositoryexplorer.model.IUIUser;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UISecurity.Mode;
import org.pentaho.ui.xul.XulEventSourceAdapter;
import org.pentaho.ui.xul.util.AbstractModelList;

public class UISecurityRole extends XulEventSourceAdapter implements java.io.Serializable {

  private static final long serialVersionUID = -2525642001107996480L; /* EESOURCE: UPDATE SERIALVERUID */

  private Mode mode;
  private List<IUIUser> availableUsers;
  private AbstractModelList<IUIUser> assignedUsers;
  private List<IUIUser> availableSelectedUsers = new ArrayList<IUIUser>();
  private List<IUIUser> assignedSelectedUsers = new ArrayList<IUIUser>();
  private boolean userAssignmentPossible;
  private boolean userUnassignmentPossible;
  private String name;

  private String description;

  public UISecurityRole() {
    availableUsers = new ArrayList<IUIUser>();
    assignedUsers = new AbstractModelList<IUIUser>();

    assignedUsers.addPropertyChangeListener( "children", //$NON-NLS-1$
        new PropertyChangeListener() {

          public void propertyChange( PropertyChangeEvent evt ) {
            List<IUIUser> previousValue = getPreviousSelectedUsers();
            UISecurityRole.this.firePropertyChange( "selectedUsers", //$NON-NLS-1$
                previousValue, assignedUsers );
          }
        } );

    description = null;
    name = null;
  }

  public List<IUIUser> getAvailableSelectedUsers() {
    return availableSelectedUsers;
  }

  public void setAvailableSelectedUsers( List<Object> availableSelectedUsers ) {
    List<Object> previousVal = new ArrayList<Object>();
    previousVal.addAll( this.availableSelectedUsers );
    this.availableSelectedUsers.clear();
    if ( availableSelectedUsers != null && availableSelectedUsers.size() > 0 ) {
      for ( Object user : availableSelectedUsers ) {
        this.availableSelectedUsers.add( (IUIUser) user );
      }
    }
    this.firePropertyChange( "availableSelectedUsers", previousVal, this.availableSelectedUsers ); //$NON-NLS-1$
    fireUserAssignmentPropertyChange();
  }

  public List<IUIUser> getAssignedSelectedUsers() {
    return assignedSelectedUsers;
  }

  public void setAssignedSelectedUsers( List<Object> assignedSelectedUsers ) {
    List<Object> previousVal = new ArrayList<Object>();
    previousVal.addAll( this.assignedSelectedUsers );
    this.assignedSelectedUsers.clear();
    if ( assignedSelectedUsers != null && assignedSelectedUsers.size() > 0 ) {
      for ( Object user : assignedSelectedUsers ) {
        this.assignedSelectedUsers.add( (IUIUser) user );
      }
    }
    this.firePropertyChange( "assignedSelectedUsers", previousVal, this.assignedSelectedUsers ); //$NON-NLS-1$
    fireUserUnassignmentPropertyChange();
  }

  public UISecurityRole getUISecurityRole() {
    return this;
  }

  public void setRole( IUIRole role, List<IUIUser> users ) {
    setAvailableUsers( users );
    setDescription( role.getDescription() );
    setName( role.getName() );
    for ( IUIUser user : role.getUsers() ) {
      removeFromAvailableUsers( user.getName() );
      addToAssignedUsers( user );
    }
  }

  public Mode getMode() {
    return mode;
  }

  public void setMode( Mode mode ) {
    this.mode = mode;
    this.firePropertyChange( "mode", null, mode ); //$NON-NLS-1$
  }

  public List<IUIUser> getAvailableUsers() {
    return availableUsers;
  }

  public void setAvailableUsers( List<IUIUser> availableUsers ) {
    List<IUIUser> previousValue = getPreviousAvailableUsers();
    this.availableUsers.clear();
    if ( availableUsers != null ) {
      this.availableUsers.addAll( availableUsers );
    }
    this.firePropertyChange( "availableUsers", previousValue, this.availableUsers ); //$NON-NLS-1$
  }

  public List<IUIUser> getAssignedUsers() {
    return assignedUsers;
  }

  public void setAssignedUsers( AbstractModelList<IUIUser> selectedUsers ) {
    List<IUIUser> previousValue = getPreviousSelectedUsers();
    this.assignedUsers.clear();
    if ( selectedUsers != null ) {
      this.assignedUsers.addAll( selectedUsers );
    }
    this.firePropertyChange( "assignedUsers", previousValue, this.assignedUsers ); //$NON-NLS-1$
  }

  public String getName() {
    return name;
  }

  public void setName( String name ) {
    String previousVal = this.name;
    this.name = name;
    this.firePropertyChange( "name", previousVal, name ); //$NON-NLS-1$
  }

  public String getDescription() {
    return description;
  }

  public void setDescription( String description ) {
    String previousVal = this.description;
    this.description = description;
    this.firePropertyChange( "description", previousVal, description ); //$NON-NLS-1$
  }

  public void clear() {
    setMode( Mode.ADD );
    setName( "" );//$NON-NLS-1$
    setDescription( "" ); //$NON-NLS-1$
    setAvailableUsers( null );
    setAssignedSelectedUsers( null );
    setAvailableSelectedUsers( null );
    setAssignedUsers( null );
    setUserAssignmentPossible( false );
    setUserUnassignmentPossible( false );
  }

  public void assignUsers( List<Object> usersToAssign ) {
    for ( Object userToAssign : usersToAssign ) {

      assignUser( (IUIUser) userToAssign );
    }
    setAssignedSelectedUsers( usersToAssign );
    setAvailableSelectedUsers( new ArrayList<Object>() );
    this.firePropertyChange( "userAssignmentPossible", null, false ); //$NON-NLS-1$
  }

  public void assignUser( IUIUser userToAssign ) {
    addToAssignedUsers( userToAssign );
    removeFromAvailableUsers( userToAssign.getName() );
  }

  public void unassignUsers( List<Object> usersToUnAssign ) {
    for ( Object userToUnAssign : usersToUnAssign ) {
      unassignUser( (IUIUser) userToUnAssign );
    }
    setAvailableSelectedUsers( usersToUnAssign );
    setAssignedSelectedUsers( new ArrayList<Object>() );
    this.firePropertyChange( "userUnassignmentPossible", null, false ); //$NON-NLS-1$
  }

  public void unassignUser( IUIUser userToUnAssign ) {
    removeFromAssignedUsers( userToUnAssign.getName() );
    addToAvailableUsers( userToUnAssign );
  }

  public boolean isUserAssignmentPossible() {
    return userAssignmentPossible;
  }

  public void setUserAssignmentPossible( boolean userAssignmentPossible ) {
    this.userAssignmentPossible = userAssignmentPossible;
    fireUserAssignmentPropertyChange();
  }

  public boolean isUserUnassignmentPossible() {
    return userUnassignmentPossible;
  }

  public void setUserUnassignmentPossible( boolean userUnassignmentPossible ) {
    this.userUnassignmentPossible = userUnassignmentPossible;
    fireUserUnassignmentPropertyChange();
  }

  public IRole getRole( IRoleSupportSecurityManager rsm ) {
    IRole roleInfo = null;
    try {
      roleInfo = rsm.constructRole();
    } catch ( KettleException e ) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    roleInfo.setDescription( description );
    roleInfo.setName( name );
    for ( IUIUser user : getAssignedUsers() ) {
      roleInfo.addUser( user.getUserInfo() );
    }
    return roleInfo;
  }

  private void addToAssignedUsers( IUIUser userToAdd ) {
    List<IUIUser> previousValue = getPreviousSelectedUsers();
    assignedUsers.add( userToAdd );
    if ( assignedUsers.size() == 1 ) {
      setUserUnassignmentPossible( true );
    }
    this.firePropertyChange( "assignedUsers", previousValue, assignedUsers ); //$NON-NLS-1$
  }

  private void addToAvailableUsers( IUIUser userToAdd ) {
    List<IUIUser> previousValue = getPreviousAvailableUsers();
    availableUsers.add( userToAdd );
    if ( availableUsers.size() == 1 ) {
      setUserAssignmentPossible( true );
    }
    this.firePropertyChange( "availableUsers", previousValue, availableUsers ); //$NON-NLS-1$
  }

  private void removeFromAvailableUsers( String userName ) {
    List<IUIUser> previousValue = getPreviousAvailableUsers();
    availableUsers.remove( getAvailableUser( userName ) );
    if ( availableUsers.size() == 0 ) {
      setUserAssignmentPossible( false );
    }
    this.firePropertyChange( "availableUsers", previousValue, availableUsers ); //$NON-NLS-1$
    fireUserAssignmentPropertyChange();
  }

  private void removeFromAssignedUsers( String userName ) {
    List<IUIUser> previousValue = getPreviousSelectedUsers();
    assignedUsers.remove( getSelectedUser( userName ) );
    if ( assignedUsers.size() == 0 ) {
      setUserUnassignmentPossible( false );
    }

    this.firePropertyChange( "assignedUsers", previousValue, assignedUsers ); //$NON-NLS-1$
    fireUserUnassignmentPropertyChange();
  }

  private IUIUser getSelectedUser( String userName ) {
    for ( IUIUser user : assignedUsers ) {
      if ( user.getName().equals( userName ) ) {
        return user;
      }
    }
    return null;
  }

  private void fireUserUnassignmentPropertyChange() {
    if ( userUnassignmentPossible && assignedUsers.size() > 0 && assignedSelectedUsers.size() > 0 ) {
      this.firePropertyChange( "userUnassignmentPossible", null, true ); //$NON-NLS-1$
    } else {
      this.firePropertyChange( "userUnassignmentPossible", null, false ); //$NON-NLS-1$
    }
  }

  private void fireUserAssignmentPropertyChange() {
    if ( userAssignmentPossible && availableUsers.size() > 0 && availableSelectedUsers.size() > 0 ) {
      this.firePropertyChange( "userAssignmentPossible", null, true ); //$NON-NLS-1$
    } else {
      this.firePropertyChange( "userAssignmentPossible", null, false ); //$NON-NLS-1$
    }
  }

  private IUIUser getAvailableUser( String userName ) {
    for ( IUIUser user : availableUsers ) {
      if ( user.getName().equals( userName ) ) {
        return user;
      }
    }
    return null;
  }

  private List<IUIUser> getPreviousAvailableUsers() {
    List<IUIUser> previousValue = new ArrayList<IUIUser>();
    for ( IUIUser ru : availableUsers ) {
      previousValue.add( ru );
    }
    return previousValue;
  }

  private List<IUIUser> getPreviousSelectedUsers() {
    List<IUIUser> previousValue = new ArrayList<IUIUser>();
    for ( IUIUser ru : assignedUsers ) {
      previousValue.add( ru );
    }
    return previousValue;
  }
}
