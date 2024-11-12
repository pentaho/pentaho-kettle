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

package org.pentaho.di.ui.repository.pur.repositoryexplorer;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.ui.repository.pur.repositoryexplorer.model.UIRepositoryObjectAcls;
import org.pentaho.di.ui.repository.repositoryexplorer.AccessDeniedException;
import org.pentaho.platform.api.repository2.unified.RepositoryFilePermission;

public interface IAclObject {

  public void getAcls( UIRepositoryObjectAcls acls ) throws AccessDeniedException;

  public void getAcls( UIRepositoryObjectAcls acls, boolean forceParentInheriting ) throws AccessDeniedException;

  public void setAcls( UIRepositoryObjectAcls security ) throws AccessDeniedException;

  /**
   * Clear the cached ACL so it is refreshed upon next request.
   */
  public void clearAcl();

  public boolean hasAccess( RepositoryFilePermission perm ) throws KettleException;
}
