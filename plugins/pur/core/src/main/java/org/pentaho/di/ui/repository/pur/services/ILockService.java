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

package org.pentaho.di.ui.repository.pur.services;

import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.repository.IRepositoryService;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.pur.model.RepositoryLock;

/**
 * Repository service which adds a locking service to the repository. Using this feature, the user of the repository can
 * lock, unlock a particular object in the repository
 * 
 * @author rmansoor
 * 
 */
public interface ILockService extends IRepositoryService {

  /**
   * Locks this job for exclusive use by the current user of the repository
   * 
   * @param id_job
   *          the id of the job to lock
   * @param message
   *          the lock message
   * 
   * @return Repository lock object
   * @throws KettleException
   *           in case something goes wrong or the job is already locked by someone else.
   */
  public RepositoryLock lockJob( ObjectId id_job, String message ) throws KettleException;

  /**
   * Unlocks a job, allowing other people to modify it again.
   * 
   * @param id_job
   *          the id of the transformation to unlock
   * @throws KettleException
   *           in case something goes wrong with the database or connection
   */
  public void unlockJob( ObjectId id_job ) throws KettleException;

  /**
   * Return the lock object for this job. Returns null if there is no lock present.
   * 
   * @param id_job
   * @return the lock object for this job, null if no lock is present.
   * @throws KettleDatabaseException
   */
  public RepositoryLock getJobLock( ObjectId id_job ) throws KettleException;

  /**
   * Locks this transformation for exclusive use by the current user of the repository
   * 
   * @param id_transformation
   *          the id of the transformation to lock
   * @param isSessionScoped
   *          If isSessionScoped is true then this lock will expire upon the expiration of the current session (either
   *          through an automatic or explicit Session.logout); if false, this lock does not expire until explicitly
   *          unlocked or automatically unlocked due to a implementation-specific limitation, such as a timeout.
   * @param message
   *          the lock message
   * 
   * @return Transformation lock
   * @throws KettleException
   *           in case something goes wrong or the transformation is already locked by someone else.
   */
  public RepositoryLock lockTransformation( ObjectId id_transformation, String message ) throws KettleException;

  /**
   * Unlocks a transformation, allowing other people to modify it again.
   * 
   * @param id_transformation
   *          the id of the transformation to unlock
   * @throws KettleException
   *           in case something goes wrong with the database or connection
   */
  public void unlockTransformation( ObjectId id_transformation ) throws KettleException;

  /**
   * Return the lock object for this transformation. Returns null if there is no lock present.
   * 
   * @param id_transformation
   * @return the lock object for this transformation, null if no lock is present.
   * @throws KettleException
   *           in case something goes wrong with the database or connection
   */
  public RepositoryLock getTransformationLock( ObjectId id_transformation ) throws KettleException;

  /**
   * Return true if the file can be unlocked by the logged in user
   * 
   * @param id
   * @return
   * @throws KettleException
   */
  public boolean canUnlockFileById( final ObjectId id ) throws KettleException;
}
