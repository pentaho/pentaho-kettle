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
