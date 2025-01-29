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

package org.pentaho.di.ui.repository.pur.repositoryexplorer.abs.model;

import java.util.List;

import org.pentaho.di.repository.RepositorySecurityManager;
import org.pentaho.di.ui.repository.pur.repositoryexplorer.IUIRole;
import org.pentaho.di.ui.repository.pur.repositoryexplorer.abs.IUIAbsRole;
import org.pentaho.di.ui.repository.pur.repositoryexplorer.model.UIEESecurity;
import org.pentaho.di.ui.repository.pur.services.IAbsSecurityManager;

public class UIAbsSecurity extends UIEESecurity implements java.io.Serializable {

  private static final long serialVersionUID = -8131064658827613758L; /* EESOURCE: UPDATE SERIALVERUID */

  public UIAbsSecurity() {
    super();
  }

  public UIAbsSecurity( RepositorySecurityManager rsm ) throws Exception {
    super( rsm );
    for ( IUIRole systemRole : systemRoleList ) {
      if ( rsm instanceof IAbsSecurityManager ) {
        IAbsSecurityManager asm = (IAbsSecurityManager) rsm;
        List<String> logicalRoles = asm.getLogicalRoles( systemRole.getName() );
        if ( systemRole instanceof IUIAbsRole ) {
          ( (IUIAbsRole) systemRole ).setLogicalRoles( logicalRoles );
        } else {
          throw new IllegalStateException();
        }
      } else {
        throw new IllegalStateException();
      }
    }
  }

  public void addLogicalRole( String logicalRole ) {
    IUIRole role = getSelectedRole();
    if ( role != null ) {
      if ( role instanceof IUIAbsRole ) {
        ( (IUIAbsRole) role ).addLogicalRole( logicalRole );
      } else {
        throw new IllegalStateException();
      }
    } else {
      role = getSelectedSystemRole();
      if ( role instanceof IUIAbsRole ) {
        ( (IUIAbsRole) role ).addLogicalRole( logicalRole );
      } else {
        throw new IllegalStateException();
      }
    }
  }

  public void removeLogicalRole( String logicalRole ) {
    IUIRole role = getSelectedRole();
    if ( role != null ) {
      if ( role instanceof IUIAbsRole ) {
        ( (IUIAbsRole) role ).removeLogicalRole( logicalRole );
      } else {
        throw new IllegalStateException();
      }
    } else {
      role = getSelectedSystemRole();
      if ( role instanceof IUIAbsRole ) {
        ( (IUIAbsRole) role ).removeLogicalRole( logicalRole );
      } else {
        throw new IllegalStateException();
      }
    }
  }
}
