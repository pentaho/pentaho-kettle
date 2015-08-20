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

package org.pentaho.di.repository.pur.model;

import java.util.Date;

import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.repository.RepositoryObject;
import org.pentaho.di.repository.RepositoryObjectType;
import org.pentaho.di.repository.StringObjectId;
import org.pentaho.di.repository.pur.PurRepositoryElementMetaInterface;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileTree;

public class EERepositoryObject extends RepositoryObject implements ILockObject, PurRepositoryElementMetaInterface, java.io.Serializable {

  private static final long serialVersionUID = -566113926064789982L; /* EESOURCE: UPDATE SERIALVERUID */
  private String lockMessage;
  private RepositoryLock lock;
  private Boolean versioningEnabled;
  private Boolean versionCommentEnabled;
  
  public EERepositoryObject() {
    super();
    // TODO Auto-generated constructor stub
  }
  
  /**
   * This is the only constructor that will populate the versioningEnabled and versionCommentEnabled flags.
   * Other constructors will leave the values as null.
   * @param repoTree
   * @param repositoryDirectory
   * @param modifiedUser
   * @param objectType
   * @param description
   * @param lock
   * @param deleted
   */
  public EERepositoryObject( RepositoryFileTree repoTree, RepositoryDirectoryInterface repositoryDirectory,
      String modifiedUser, RepositoryObjectType objectType, String description, RepositoryLock lock, boolean deleted ) {
    this( repoTree.getFile(),  repositoryDirectory, modifiedUser, objectType, description, lock, deleted);
    setVersioningEnabled( repoTree.getVersioningEnabled() );
    setVersionCommentEnabled( repoTree.getVersionCommentEnabled() );
  }
  
  public EERepositoryObject( RepositoryFile file, RepositoryDirectoryInterface repositoryDirectory,
      String modifiedUser, RepositoryObjectType objectType, String description, RepositoryLock lock, boolean deleted ) {
    this( new StringObjectId( file.getId().toString() ), file.getTitle(),
        repositoryDirectory, modifiedUser, file.getLastModifiedDate(), objectType, description, lock,
        deleted );
  }
  
  public EERepositoryObject( RepositoryFile file, RepositoryDirectoryInterface repositoryDirectory,
      String modifiedUser, RepositoryObjectType objectType, String description, RepositoryLock lock, boolean deleted,
      Boolean versioningEnabled, Boolean versionCommentEnabled ) {
    this( new StringObjectId( file.getId().toString() ), file.getTitle(), repositoryDirectory, modifiedUser, file
        .getLastModifiedDate(), objectType, description, lock, deleted );
    setVersioningEnabled( versioningEnabled );
    setVersionCommentEnabled( versionCommentEnabled );
  }
  
  public EERepositoryObject(ObjectId objectId, String name, RepositoryDirectoryInterface repositoryDirectory,
        String modifiedUser, Date modifiedDate, RepositoryObjectType objectType, String description, RepositoryLock lock,
        boolean deleted ) {
    super(objectId, name, repositoryDirectory, modifiedUser, modifiedDate, objectType, description, deleted );
    setLock(lock);
  }

  public boolean isLocked() {
    return lock != null;
  }

  /**
   * @return the lockMessage
   */
  public String getLockMessage() {
    return lockMessage;
  }

  public RepositoryLock getLock() {
    return lock;
  }

  public void setLock(RepositoryLock lock) {
    this.lock = lock;
    lockMessage = lock == null ? null : lock.getMessage() + " (" + lock.getLogin() + " since " //$NON-NLS-1$ //$NON-NLS-2$
        + XMLHandler.date2string(lock.getLockDate()) + ")"; //$NON-NLS-1$
  }

  @Override
  public Boolean getVersioningEnabled() {
    return versioningEnabled;
  }

  public void setVersioningEnabled( Boolean versioningEnabled ) {
    this.versioningEnabled = versioningEnabled;
  }

  @Override
  public Boolean getVersionCommentEnabled() {
    return versionCommentEnabled;
  }

  public void setVersionCommentEnabled( Boolean versionCommentEnabled ) {
    this.versionCommentEnabled = versionCommentEnabled;
  }
  
}
