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

import java.util.HashMap;
import java.util.Map;

import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.ui.repository.pur.repositoryexplorer.IAclObject;
import org.pentaho.di.ui.repository.pur.services.IAclService;
import org.pentaho.di.ui.repository.repositoryexplorer.AccessDeniedException;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UIDatabaseConnection;
import org.pentaho.platform.api.repository2.unified.RepositoryFilePermission;

/**
 * This UI DB Connection extends the default and allows for ACLs in the view
 * 
 * @author Will Gorman (wgorman@pentaho.com)
 * 
 */
public class UIEEDatabaseConnection extends UIDatabaseConnection implements IAclObject {
  private IAclService aclService;
  private Map<RepositoryFilePermission, Boolean> hasAccess = null;

  public UIEEDatabaseConnection() {
    super();
  }

  public UIEEDatabaseConnection( DatabaseMeta meta, Repository rep ) {
    super( meta, rep );
    initializeService( rep );
  }

  private void initializeService( Repository rep ) {
    try {
      if ( rep.hasService( IAclService.class ) ) {
        aclService = (IAclService) rep.getService( IAclService.class );
      } else {
        throw new IllegalStateException();
      }
    } catch ( KettleException e ) {
      throw new RuntimeException( e );
    }

  }

  public void getAcls( UIRepositoryObjectAcls acls, boolean forceParentInheriting ) throws AccessDeniedException {
    try {
      acls.setObjectAcl( aclService.getAcl( getDatabaseMeta().getObjectId(), forceParentInheriting ) );
    } catch ( KettleException ke ) {
      throw new AccessDeniedException( ke );
    }
  }

  public void getAcls( UIRepositoryObjectAcls acls ) throws AccessDeniedException {
    try {
      acls.setObjectAcl( aclService.getAcl( getDatabaseMeta().getObjectId(), false ) );
    } catch ( KettleException ke ) {
      throw new AccessDeniedException( ke );
    }
  }

  public void setAcls( UIRepositoryObjectAcls security ) throws AccessDeniedException {
    try {
      aclService.setAcl( getDatabaseMeta().getObjectId(), security.getObjectAcl() );
    } catch ( KettleException e ) {
      throw new AccessDeniedException( e );
    }
  }

  @Override
  public void clearAcl() {
    hasAccess = null;
  }

  @Override
  public boolean hasAccess( RepositoryFilePermission perm ) throws KettleException {
    if ( hasAccess == null ) {
      hasAccess = new HashMap<RepositoryFilePermission, Boolean>();
    }
    if ( hasAccess.get( perm ) == null ) {
      hasAccess.put( perm, new Boolean( aclService.hasAccess( getDatabaseMeta().getObjectId(), perm ) ) );
    }
    return hasAccess.get( perm ).booleanValue();
  }
}
