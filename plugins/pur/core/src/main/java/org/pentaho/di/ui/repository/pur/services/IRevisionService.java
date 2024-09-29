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

package org.pentaho.di.ui.repository.pur.services;

import java.util.List;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.repository.IRepositoryService;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.ObjectRevision;
import org.pentaho.di.repository.RepositoryElementInterface;

/**
 * Repository service which adds a revision feature to the repository. Using this feature, user of this repository can
 * get revisions of the object and restore to a specific version
 * 
 * @author rmansoor
 * 
 */
public interface IRevisionService extends IRepositoryService {

  /**
   * Get the revision history of a repository element.
   * 
   * @param element
   *          the element. If the ID is specified, this will be taken. Otherwise it will be looked up.
   * 
   * @return The revision history, sorted from first to last.
   * @throws KettleException
   *           in case something goes horribly wrong
   */
  public List<ObjectRevision> getRevisions( RepositoryElementInterface element ) throws KettleException;

  /**
   * Get the revision history of a repository element.
   * 
   * @param element
   *          the element. If the ID is specified, this will be taken. Otherwise it will be looked up.
   * 
   * @return The revision history, sorted from first to last.
   * @throws KettleException
   *           in case something goes horribly wrong
   */
  public List<ObjectRevision> getRevisions( ObjectId id ) throws KettleException;

  /**
   * Restore a job from the given revision. The state of the specified revision becomes the current / latest state of
   * the job.
   * 
   * @param id_job
   *          id of the job
   * @param revision
   *          revision to restore
   * @throws KettleException
   */
  public void restoreJob( ObjectId id_job, String revision, String versionComment ) throws KettleException;

  /**
   * Restore a transformation from the given revision. The state of the specified revision becomes the current / latest
   * state of the transformation.
   * 
   * @param id_transformation
   *          id of the transformation
   * @param revision
   *          revision to restore
   * @throws KettleException
   */
  public void restoreTransformation( ObjectId id_transformation, String revision, String versionComment )
    throws KettleException;

}
