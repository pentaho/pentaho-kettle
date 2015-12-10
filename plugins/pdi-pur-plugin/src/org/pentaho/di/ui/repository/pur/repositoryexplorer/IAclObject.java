/*!
 * Copyright 2010 - 2015 Pentaho Corporation.  All rights reserved.
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
