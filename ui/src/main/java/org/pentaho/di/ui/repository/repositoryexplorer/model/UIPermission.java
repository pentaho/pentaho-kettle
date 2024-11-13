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


package org.pentaho.di.ui.repository.repositoryexplorer.model;

public enum UIPermission {
  READ( "UIPermission.READ_DESC" ), CREATE( "UIPermission.CREATE_DESC" ), UPDATE( "UIPermission.UPDATE_DESC" ),
    MODIFY_PERMISSION( "UIPermission.MODIFY_PERMISSION_DESC" ), DELETE( "UIPermission.DELETE_DESC" );

  private String description;

  private UIPermission( String description ) {
    this.description = description;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription( String description ) {
    this.description = description;
  }

}
