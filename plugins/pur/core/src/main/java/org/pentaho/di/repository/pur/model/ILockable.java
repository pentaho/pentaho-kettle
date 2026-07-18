/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/



package org.pentaho.di.repository.pur.model;

import org.pentaho.di.core.exception.KettleException;

public interface ILockable {
  public RepositoryLock getRepositoryLock() throws KettleException;

  public void setRepositoryLock( RepositoryLock lock ) throws KettleException;
}
