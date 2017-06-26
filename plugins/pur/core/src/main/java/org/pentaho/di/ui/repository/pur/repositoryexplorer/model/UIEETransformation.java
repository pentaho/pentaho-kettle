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
package org.pentaho.di.ui.repository.pur.repositoryexplorer.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.ObjectRevision;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectory;
import org.pentaho.di.repository.RepositoryElementMetaInterface;
import org.pentaho.di.repository.pur.model.EERepositoryObject;
import org.pentaho.di.repository.pur.model.ObjectAcl;
import org.pentaho.di.repository.pur.model.RepositoryLock;
import org.pentaho.di.ui.repository.pur.repositoryexplorer.IAclObject;
import org.pentaho.di.ui.repository.pur.repositoryexplorer.ILockObject;
import org.pentaho.di.ui.repository.pur.repositoryexplorer.IRevisionObject;
import org.pentaho.di.ui.repository.pur.services.IAclService;
import org.pentaho.di.ui.repository.pur.services.ILockService;
import org.pentaho.di.ui.repository.pur.services.IRevisionService;
import org.pentaho.di.ui.repository.repositoryexplorer.AccessDeniedException;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UIRepositoryDirectory;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UITransformation;
import org.pentaho.platform.api.repository2.unified.RepositoryFilePermission;

public class UIEETransformation extends UITransformation implements ILockObject, IRevisionObject, IAclObject,
    java.io.Serializable {

  private static final long serialVersionUID = 3460651955586659084L; /* EESOURCE: UPDATE SERIALVERUID */
  private ILockService lockService;
  private IAclService aclService;
  private IRevisionService revisionService;
  private UIRepositoryObjectRevisions revisions;
  private EERepositoryObject repObj;
  private ObjectAcl acl;
  private Map<RepositoryFilePermission, Boolean> hasAccess = null;

  public UIEETransformation( RepositoryElementMetaInterface rc, UIRepositoryDirectory parent, Repository rep ) {
    super( rc, parent, rep );
    if ( !( rc instanceof EERepositoryObject ) ) {
      throw new IllegalArgumentException();
    }
    repObj = (EERepositoryObject) rc;
    try {
      if ( rep.hasService( ILockService.class ) ) {
        lockService = (ILockService) rep.getService( ILockService.class );
      } else {
        throw new IllegalStateException();
      }
      if ( rep.hasService( IRevisionService.class ) ) {
        revisionService = (IRevisionService) rep.getService( IRevisionService.class );
      } else {
        throw new IllegalStateException();
      }
      if ( rep.hasService( IAclService.class ) ) {
        aclService = (IAclService) rep.getService( IAclService.class );
      } else {
        throw new IllegalStateException();
      }
    } catch ( KettleException e ) {
      throw new RuntimeException( e );
    }
  }

  @Override
  public String getImage() {
    try {
      if ( isLocked() ) {
        return "ui/images/lock.svg"; //$NON-NLS-1$
      }
    } catch ( KettleException e ) {
      throw new RuntimeException( e );
    }
    return "ui/images/transrepo.svg"; //$NON-NLS-1$
  }

  public String getLockMessage() throws KettleException {
    return repObj.getLockMessage();
  }

  public void lock( String lockNote ) throws KettleException {
    RepositoryLock lock = lockService.lockTransformation( getObjectId(), lockNote );
    repObj.setLock( lock );
    uiParent.fireCollectionChanged();
  }

  public void unlock() throws KettleException {
    lockService.unlockTransformation( getObjectId() );
    repObj.setLock( null );
    uiParent.fireCollectionChanged();
  }

  public boolean isLocked() throws KettleException {
    return repObj.isLocked();
  }

  public RepositoryLock getRepositoryLock() throws KettleException {
    return repObj.getLock();
  }

  public UIRepositoryObjectRevisions getRevisions() throws KettleException {
    if ( revisions != null ) {
      return revisions;
    }

    revisions = new UIRepositoryObjectRevisions();

    List<ObjectRevision> or = revisionService.getRevisions( getObjectId() );

    for ( ObjectRevision rev : or ) {
      revisions.add( new UIRepositoryObjectRevision( rev ) );
    }
    return revisions;
  }

  protected void refreshRevisions() throws KettleException {
    revisions = null;
    getRevisions();
  }

  public void restoreRevision( UIRepositoryObjectRevision revision, String commitMessage ) throws KettleException {
    if ( revisionService != null ) {
      revisionService.restoreTransformation( this.getObjectId(), revision.getName(), commitMessage );
      refreshRevisions();
      uiParent.fireCollectionChanged();
    }
  }

  public void getAcls( UIRepositoryObjectAcls acls, boolean forceParentInheriting ) throws AccessDeniedException {
    if ( acl == null ) {
      try {
        acl = aclService.getAcl( getObjectId(), forceParentInheriting );
      } catch ( KettleException ke ) {
        throw new AccessDeniedException( ke );
      }
    }
    acls.setObjectAcl( acl );
  }

  public void getAcls( UIRepositoryObjectAcls acls ) throws AccessDeniedException {
    getAcls( acls, false );
  }

  public void setAcls( UIRepositoryObjectAcls security ) throws AccessDeniedException {
    try {
      aclService.setAcl( getObjectId(), security.getObjectAcl() );
    } catch ( KettleException e ) {
      throw new AccessDeniedException( e );
    }
  }

  @Override
  public void clearAcl() {
    acl = null;
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

  @Override
  protected ObjectId renameTransformation( ObjectId objectId, RepositoryDirectory directory, String name )
    throws Exception {
    ObjectId id = super.renameTransformation( objectId, directory, name );
    refreshRevisions();
    return id;
  }

  @Override
  public Boolean getVersioningEnabled() {
    return repObj.getVersioningEnabled();
  }

  @Override
  public Boolean getVersionCommentEnabled() {
    return repObj.getVersionCommentEnabled();
  }
}
