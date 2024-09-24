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

public class EERepositoryObject extends RepositoryObject implements ILockObject, PurRepositoryElementMetaInterface,
    java.io.Serializable {

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
   * This is the only constructor that will populate the versioningEnabled and versionCommentEnabled flags. Other
   * constructors will leave the values as null.
   * 
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
    this( repoTree.getFile(), repositoryDirectory, modifiedUser, objectType, description, lock, deleted );
    setVersioningEnabled( repoTree.getVersioningEnabled() );
    setVersionCommentEnabled( repoTree.getVersionCommentEnabled() );
  }

  public EERepositoryObject( RepositoryFile file, RepositoryDirectoryInterface repositoryDirectory,
      String modifiedUser, RepositoryObjectType objectType, String description, RepositoryLock lock, boolean deleted ) {
    this( new StringObjectId( file.getId().toString() ), file.getTitle(), repositoryDirectory, modifiedUser, file
        .getLastModifiedDate(), objectType, description, lock, deleted );
  }

  public EERepositoryObject( RepositoryFile file, RepositoryDirectoryInterface repositoryDirectory,
      String modifiedUser, RepositoryObjectType objectType, String description, RepositoryLock lock, boolean deleted,
      Boolean versioningEnabled, Boolean versionCommentEnabled ) {
    this( new StringObjectId( file.getId().toString() ), file.getTitle(), repositoryDirectory, modifiedUser, file
        .getLastModifiedDate(), objectType, description, lock, deleted );
    setVersioningEnabled( versioningEnabled );
    setVersionCommentEnabled( versionCommentEnabled );
  }

  public EERepositoryObject( ObjectId objectId, String name, RepositoryDirectoryInterface repositoryDirectory,
      String modifiedUser, Date modifiedDate, RepositoryObjectType objectType, String description, RepositoryLock lock,
      boolean deleted ) {
    super( objectId, name, repositoryDirectory, modifiedUser, modifiedDate, objectType, description, deleted );
    setLock( lock );
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

  public void setLock( RepositoryLock lock ) {
    this.lock = lock;
    lockMessage = lock == null ? null : lock.getMessage() + " (" + lock.getLogin() + " since " //$NON-NLS-1$ //$NON-NLS-2$
        + XMLHandler.date2string( lock.getLockDate() ) + ")"; //$NON-NLS-1$
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
