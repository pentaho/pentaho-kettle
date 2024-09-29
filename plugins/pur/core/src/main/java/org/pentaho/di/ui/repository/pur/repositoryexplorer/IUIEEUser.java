/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.di.ui.repository.pur.repositoryexplorer;

import java.util.Set;

import org.pentaho.di.ui.repository.repositoryexplorer.model.IUIUser;

public interface IUIEEUser extends IUIUser {

  public boolean addRole( IUIRole role );

  public boolean removeRole( IUIRole role );

  public void clearRoles();

  public void setRoles( Set<IUIRole> roles );

  public Set<IUIRole> getRoles();
}
