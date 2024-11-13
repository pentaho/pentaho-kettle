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

import org.pentaho.di.repository.pur.model.IAbsRole;
import org.pentaho.di.repository.pur.model.IRole;
import org.pentaho.di.ui.repository.pur.repositoryexplorer.abs.IUIAbsRole;
import org.pentaho.di.ui.repository.pur.repositoryexplorer.model.UIRepositoryRole;

public class UIAbsRepositoryRole extends UIRepositoryRole implements IUIAbsRole, java.io.Serializable {

  private static final long serialVersionUID = -2985868210333283734L; /* EESOURCE: UPDATE SERIALVERUID */
  IAbsRole absRole;

  public UIAbsRepositoryRole() {
    super();
  }

  public UIAbsRepositoryRole( IRole role ) {
    super( role );
    if ( role instanceof IAbsRole ) {
      absRole = (IAbsRole) role;
    } else {
      throw new IllegalStateException();
    }
  }

  public List<String> getLogicalRoles() {
    return absRole.getLogicalRoles();
  }

  public void setLogicalRoles( List<String> logicalRoles ) {
    absRole.setLogicalRoles( logicalRoles );
  }

  public void addLogicalRole( String logicalRole ) {
    absRole.addLogicalRole( logicalRole );
  }

  public void removeLogicalRole( String logicalRole ) {
    absRole.removeLogicalRole( logicalRole );
  }

  public boolean containsLogicalRole( String logicalRole ) {
    return absRole.containsLogicalRole( logicalRole );
  }

  @Override
  public String toString() {
    return absRole.getName();
  }
}
