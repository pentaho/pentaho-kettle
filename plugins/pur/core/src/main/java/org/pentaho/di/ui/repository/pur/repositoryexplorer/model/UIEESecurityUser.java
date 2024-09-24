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
import org.pentaho.di.repository.IUser;
import org.pentaho.di.repository.RepositorySecurityManager;
import org.pentaho.di.repository.pur.model.IEEUser;
import org.pentaho.di.ui.repository.pur.repositoryexplorer.IUIEEUser;
import org.pentaho.di.ui.repository.pur.repositoryexplorer.IUIRole;
import org.pentaho.di.ui.repository.pur.repositoryexplorer.UIEEObjectRegistery;
import org.pentaho.di.ui.repository.repositoryexplorer.model.IUIUser;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UISecurityUser;
import org.pentaho.ui.xul.util.AbstractModelList;

public class UIEESecurityUser extends UISecurityUser implements java.io.Serializable {

  private static final long serialVersionUID = 3632015987344690267L; /* EESOURCE: UPDATE SERIALVERUID */

  private AbstractModelList<IUIRole> availableRoles;
  private AbstractModelList<IUIRole> assignedRoles;
  private List<IUIRole> availableSelectedRoles = new ArrayList<IUIRole>();
  private List<IUIRole> assignedSelectedRoles = new ArrayList<IUIRole>();
  private boolean roleAssignmentPossible;
  private boolean roleUnassignmentPossible;

  public UIEESecurityUser( RepositorySecurityManager rsm ) {
    super( rsm );
    availableRoles = new AbstractModelList<IUIRole>();
    availableRoles.addPropertyChangeListener( "children", new PropertyChangeListener() { //$NON-NLS-1$
          public void propertyChange( PropertyChangeEvent evt ) {
            UIEESecurityUser.this.firePropertyChange( "availableRoles", null, availableRoles ); //$NON-NLS-1$
          }
        } );

    assignedRoles = new AbstractModelList<IUIRole>();
    assignedRoles.addPropertyChangeListener( "children", new PropertyChangeListener() { //$NON-NLS-1$
          public void propertyChange( PropertyChangeEvent evt ) {
            UIEESecurityUser.this.firePropertyChange( "assignedRoles", null, assignedRoles ); //$NON-NLS-1$
          }
        } );
  }

  public void setUser( IUIUser user, List<IUIRole> roles ) throws Exception {
    super.setUser( user );
    setAvailableRoles( roles );
    if ( user instanceof IUIEEUser ) {
      for ( IUIRole role : ( (IUIEEUser) user ).getRoles() ) {
        removeFromAvailableRoles( role.getName() );
        addToAssignedRoles( UIEEObjectRegistery.getInstance().constructUIRepositoryRole( role.getRole() ) );
      }
    }
  }

  public List<IUIRole> getAvailableSelectedRoles() {
    return availableSelectedRoles;
  }

  public void setAvailableSelectedRoles( List<Object> availableSelectedRoles ) {
    List<Object> previousVal = new ArrayList<Object>();
    previousVal.addAll( this.availableSelectedRoles );
    this.availableSelectedRoles.clear();
    if ( availableSelectedRoles != null && availableSelectedRoles.size() > 0 ) {
      for ( Object role : availableSelectedRoles ) {
        this.availableSelectedRoles.add( (IUIRole) role );
      }
    }
    this.firePropertyChange( "availableSelectedRoles", previousVal, this.availableSelectedRoles ); //$NON-NLS-1$
    fireRoleAssignmentPropertyChange();
  }

  public List<IUIRole> getAssignedSelectedRoles() {
    return assignedSelectedRoles;
  }

  public void setAssignedSelectedRoles( List<Object> assignedSelectedRoles ) {
    List<Object> previousVal = new ArrayList<Object>();
    previousVal.addAll( this.assignedSelectedRoles );
    this.assignedSelectedRoles.clear();
    if ( assignedSelectedRoles != null && assignedSelectedRoles.size() > 0 ) {
      for ( Object role : assignedSelectedRoles ) {
        this.assignedSelectedRoles.add( (IUIRole) role );
      }
    }
    this.firePropertyChange( "assignedSelectedRoles", previousVal, this.assignedSelectedRoles ); //$NON-NLS-1$
    fireRoleUnassignmentPropertyChange();
  }

  public UIEESecurityUser getUISecurityUser() {
    return this;
  }

  public List<IUIRole> getAvailableRoles() {
    return availableRoles;
  }

  public void setAvailableRoles( List<IUIRole> availableRoles ) {
    List<IUIRole> previousValue = getPreviousAvailableRoles();
    this.availableRoles.clear();
    if ( availableRoles != null ) {
      this.availableRoles.addAll( availableRoles );
    }
    this.firePropertyChange( "availableRoles", previousValue, this.availableRoles ); //$NON-NLS-1$
  }

  public List<IUIRole> getAssignedRoles() {
    return assignedRoles;
  }

  public void updateAssignedRoles( List<IUIRole> assignedRoles ) {
    for ( IUIRole role : assignedRoles ) {
      assignRole( role );
    }
  }

  public void setAssignedRoles( List<IUIRole> selectedRoles ) {
    List<IUIRole> previousValue = getPreviousAssignedRoles();
    this.assignedRoles.clear();
    if ( selectedRoles != null ) {
      this.assignedRoles.addAll( selectedRoles );
    }
    this.firePropertyChange( "assignedRoles", previousValue, this.assignedRoles ); //$NON-NLS-1$
  }

  @Override
  public void clear() {
    super.clear();
    setAssignedSelectedRoles( null );
    setAvailableSelectedRoles( null );
    setAvailableRoles( null );
    setAssignedRoles( null );
    setRoleAssignmentPossible( false );
    setRoleUnassignmentPossible( false );
  }

  public void assignRoles( List<Object> rolesToAssign ) {
    for ( Object roleToAssign : rolesToAssign ) {
      assignRole( (IUIRole) roleToAssign );
    }
    setAssignedSelectedRoles( rolesToAssign );
    setAvailableSelectedRoles( new ArrayList<Object>() );
    this.firePropertyChange( "roleAssignmentPossible", null, false ); //$NON-NLS-1$
  }

  public void assignRole( IUIRole roleToAssign ) {
    addToAssignedRoles( roleToAssign );
    removeFromAvailableRoles( roleToAssign.getName() );
  }

  public void unassignRoles( List<Object> rolesToUnAssign ) {
    for ( Object roleToUnAssign : rolesToUnAssign ) {
      unassignRole( (IUIRole) roleToUnAssign );
    }
    setAvailableSelectedRoles( rolesToUnAssign );
    setAssignedSelectedRoles( new ArrayList<Object>() );
    this.firePropertyChange( "roleUnassignmentPossible", null, false ); //$NON-NLS-1$
  }

  public void unassignRole( IUIRole roleToUnAssign ) {
    removeFromAssignedRoles( roleToUnAssign.getName() );
    addToAvailableRoles( roleToUnAssign );
  }

  public boolean isRoleAssignmentPossible() {
    return roleAssignmentPossible;
  }

  public void setRoleAssignmentPossible( boolean roleAssignmentPossible ) {
    this.roleAssignmentPossible = roleAssignmentPossible;
    fireRoleAssignmentPropertyChange();
  }

  public boolean isRoleUnassignmentPossible() {
    return roleUnassignmentPossible;
  }

  public void setRoleUnassignmentPossible( boolean roleUnassignmentPossible ) {
    this.roleUnassignmentPossible = roleUnassignmentPossible;
    fireRoleUnassignmentPropertyChange();
  }

  @Override
  public IUser getUserInfo() throws KettleException {
    IUser userInfo = rsm.constructUser();
    userInfo.setDescription( getDescription() );
    userInfo.setLogin( getName() );
    userInfo.setName( getName() );
    userInfo.setUsername( getName() );
    userInfo.setPassword( getPassword() );
    if ( userInfo instanceof IEEUser ) {
      for ( IUIRole role : getAssignedRoles() ) {
        ( (IEEUser) userInfo ).addRole( role.getRole() );
      }
    }
    return userInfo;
  }

  private void addToAssignedRoles( IUIRole roleToAdd ) {
    List<IUIRole> previousValue = getPreviousAssignedRoles();
    assignedRoles.add( roleToAdd );
    this.firePropertyChange( "assignedRoles", previousValue, assignedRoles ); //$NON-NLS-1$
    if ( assignedRoles.size() == 1 ) {
      setRoleUnassignmentPossible( true );
    }
    fireRoleUnassignmentPropertyChange();
  }

  private void addToAvailableRoles( IUIRole roleToAdd ) {
    List<IUIRole> previousValue = getPreviousAvailableRoles();
    availableRoles.add( roleToAdd );
    if ( availableRoles.size() == 1 ) {
      setRoleAssignmentPossible( true );
    }
    this.firePropertyChange( "availableRoles", previousValue, availableRoles ); //$NON-NLS-1$
    fireRoleAssignmentPropertyChange();
  }

  private void removeFromAvailableRoles( String roleName ) {
    List<IUIRole> previousValue = getPreviousAvailableRoles();
    availableRoles.remove( getAvailableRole( roleName ) );
    if ( availableRoles.size() == 0 ) {
      setRoleAssignmentPossible( false );
    }
    this.firePropertyChange( "availableRoles", previousValue, availableRoles ); //$NON-NLS-1$
    fireRoleAssignmentPropertyChange();
  }

  private void removeFromAssignedRoles( String roleName ) {
    List<IUIRole> previousValue = getPreviousAssignedRoles();
    assignedRoles.remove( getSelectedRole( roleName ) );
    if ( assignedRoles.size() == 0 ) {
      setRoleUnassignmentPossible( false );
    }
    this.firePropertyChange( "assignedRoles", previousValue, assignedRoles ); //$NON-NLS-1$
    fireRoleUnassignmentPropertyChange();
  }

  private void fireRoleUnassignmentPropertyChange() {
    if ( roleUnassignmentPossible && assignedRoles.size() > 0 && assignedSelectedRoles.size() > 0 ) {
      this.firePropertyChange( "roleUnassignmentPossible", null, true ); //$NON-NLS-1$
    } else {
      this.firePropertyChange( "roleUnassignmentPossible", null, false ); //$NON-NLS-1$
    }
  }

  private void fireRoleAssignmentPropertyChange() {
    if ( roleAssignmentPossible && availableRoles.size() > 0 && availableSelectedRoles.size() > 0 ) {
      this.firePropertyChange( "roleAssignmentPossible", null, true ); //$NON-NLS-1$
    } else {
      this.firePropertyChange( "roleAssignmentPossible", null, false ); //$NON-NLS-1$
    }
  }

  private IUIRole getSelectedRole( String name ) {
    for ( IUIRole role : assignedRoles ) {
      if ( role.getName().equals( name ) ) {
        return role;
      }
    }
    return null;
  }

  private IUIRole getAvailableRole( String name ) {
    for ( IUIRole role : availableRoles ) {
      if ( role.getName().equals( name ) ) {
        return role;
      }
    }
    return null;
  }

  private List<IUIRole> getPreviousAvailableRoles() {
    List<IUIRole> previousValue = new ArrayList<IUIRole>();
    for ( IUIRole ru : availableRoles ) {
      previousValue.add( ru );
    }
    return previousValue;
  }

  private List<IUIRole> getPreviousAssignedRoles() {
    List<IUIRole> previousValue = new ArrayList<IUIRole>();
    for ( IUIRole ru : assignedRoles ) {
      previousValue.add( ru );
    }
    return previousValue;
  }
}
