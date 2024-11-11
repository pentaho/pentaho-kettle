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
import org.pentaho.di.repository.pur.model.RepositoryLock;

public interface ILockObject {
  public String getLockMessage() throws KettleException;

  public void lock( String lockNote ) throws KettleException;

  public void unlock() throws KettleException;

  public RepositoryLock getRepositoryLock() throws KettleException;

  public boolean isLocked() throws KettleException;
}
