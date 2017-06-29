/*!
 * Copyright 2010 - 2016 Pentaho Corporation.  All rights reserved.
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

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.repository.RepositoryExtended;
import org.pentaho.di.ui.repository.pur.repositoryexplorer.IAclObject;
import org.pentaho.di.ui.repository.pur.services.IAclService;
import org.pentaho.di.ui.repository.repositoryexplorer.AccessDeniedException;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UIRepositoryDirectory;
import org.pentaho.platform.api.repository2.unified.RepositoryFilePermission;

public class UIEERepositoryDirectory extends UIRepositoryDirectory implements IAclObject, java.io.Serializable {

  private static final long serialVersionUID = -6273975748634580673L; /* EESOURCE: UPDATE SERIALVERUID */

  private IAclService aclService;
  private Map<RepositoryFilePermission, Boolean> hasAccess = null;

  public UIEERepositoryDirectory() {
    super();
  }

  public UIEERepositoryDirectory( RepositoryDirectoryInterface rd, UIRepositoryDirectory uiParent, Repository rep ) {
    super( rd, uiParent, rep );
    initializeService( rep );
  }

  public void getAcls( UIRepositoryObjectAcls acls, boolean forceParentInheriting ) throws AccessDeniedException {
    try {
      acls.setObjectAcl( aclService.getAcl( getObjectId(), forceParentInheriting ) );
    } catch ( KettleException ke ) {
      throw new AccessDeniedException( ke );
    }
  }

  public void getAcls( UIRepositoryObjectAcls acls ) throws AccessDeniedException {
    try {
      acls.setObjectAcl( aclService.getAcl( getObjectId(), false ) );
    } catch ( KettleException ke ) {
      throw new AccessDeniedException( ke );
    }
  }

  public void setAcls( UIRepositoryObjectAcls security ) throws AccessDeniedException {
    try {
      aclService.setAcl( getObjectId(), security.getObjectAcl() );
    } catch ( KettleException e ) {
      throw new AccessDeniedException( e );
    }
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

  public void delete( boolean deleteHomeDirectories ) throws Exception {
    if ( rep instanceof RepositoryExtended ) {
      ( (RepositoryExtended) rep ).deleteRepositoryDirectory( getDirectory(), deleteHomeDirectories );
    } else {
      rep.deleteRepositoryDirectory( getDirectory() );
    }
    if ( getParent().getChildren().contains( this ) ) {
      getParent().getChildren().remove( this );
    }
    if ( getParent().getRepositoryObjects().contains( this ) ) {
      getParent().getRepositoryObjects().remove( this );
    }
    getParent().refresh();
  }

  public void setName( String name, boolean renameHomeDirectories ) throws Exception {
    if ( getDirectory().getName().equalsIgnoreCase( name ) ) {
      return;
    }

    if ( rep instanceof RepositoryExtended ) {
      ( (RepositoryExtended) rep ).renameRepositoryDirectory( getDirectory().getObjectId(), null, name,
          renameHomeDirectories );
    } else {
      rep.renameRepositoryDirectory( getDirectory().getObjectId(), null, name );
    }

    // Update the object reference so the new name is displayed
    obj = rep.getObjectInformation( getObjectId(), getRepositoryElementType() );
    refresh();
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
      hasAccess.put( perm, new Boolean( aclService.hasAccess( getObjectId(), perm ) ) );
    }
    return hasAccess.get( perm ).booleanValue();
  }
}
