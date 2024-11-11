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


package org.pentaho.di.repository.pur.model;

import org.pentaho.di.job.JobMeta;

public class EEJobMeta extends JobMeta implements ILockable, java.io.Serializable {

  private static final long serialVersionUID = -8474422291164154884L; /* EESOURCE: UPDATE SERIALVERUID */
  private RepositoryLock repositoryLock;

  /**
   * @return the repositoryLock
   */
  public RepositoryLock getRepositoryLock() {
    return repositoryLock;
  }

  /**
   * @param repositoryLock
   *          the repositoryLock to set
   */
  public void setRepositoryLock( RepositoryLock repositoryLock ) {
    this.repositoryLock = repositoryLock;
  }
}
