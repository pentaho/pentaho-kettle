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

package org.pentaho.di.repository.pur.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.pentaho.di.repository.IUser;

public class AbsRoleInfo extends EERoleInfo implements IAbsRole, java.io.Serializable {

  private static final long serialVersionUID = -4260995958866269607L; /* EESOURCE: UPDATE SERIALVERUID */

  // logical roles bound to a given runtime role
  private List<String> logicalRoles;

  public AbsRoleInfo() {
    super();
    this.logicalRoles = new ArrayList<String>();
  }

  public AbsRoleInfo( String name, String description ) {
    super( name, description );
    this.logicalRoles = new ArrayList<String>();
  }

  public AbsRoleInfo( String name, String description, Set<IUser> users, List<String> logicalRoles ) {
    super( name, description, users );
    this.logicalRoles = logicalRoles;
  }

  public void addLogicalRole( String logicalRole ) {
    if ( logicalRoles == null ) {
      this.logicalRoles = new ArrayList<String>();
    }
    if ( !containsLogicalRole( logicalRole ) ) {
      this.logicalRoles.add( logicalRole );
    }
  }

  public void removeLogicalRole( String logicalRole ) {
    if ( containsLogicalRole( logicalRole ) ) {
      this.logicalRoles.remove( logicalRole );
    }
  }

  public List<String> getLogicalRoles() {
    return logicalRoles;
  }

  public void setLogicalRoles( List<String> logicalRoles ) {
    this.logicalRoles = logicalRoles;
  }

  public boolean containsLogicalRole( String logicalRole ) {
    if ( logicalRoles != null ) {
      for ( String role : logicalRoles ) {
        if ( role.equals( logicalRole ) ) {
          return true;
        }
      }
    }
    return false;
  }
}
