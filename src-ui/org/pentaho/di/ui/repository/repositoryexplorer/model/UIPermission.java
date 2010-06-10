/*
 * Copyright (c) 2010 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
 */
package org.pentaho.di.ui.repository.repositoryexplorer.model;

public enum UIPermission {
  READ("UIPermission.READ_DESC"), //$NON-NLS-1$ 
  CREATE("UIPermission.CREATE_DESC"), //$NON-NLS-1$
  UPDATE("UIPermission.UPDATE_DESC"), //$NON-NLS-1$
  MODIFY_PERMISSION("UIPermission.MODIFY_PERMISSION_DESC"), //$NON-NLS-1$
  DELETE("UIPermission.DELETE_DESC"); //$NON-NLS-1$

  private String description;

  private UIPermission(String description) {
    this.description = description;
  }
  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

}
