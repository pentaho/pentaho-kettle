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

public interface ILockObject {

  /**
   * @return is this object locked?
   */
  public boolean isLocked();

  /**
   * @return the lockMessage
   */
  public String getLockMessage();

  /**
   * @return the repository lock for this object
   */
  public RepositoryLock getLock();

  /**
   * Set the lock for this object
   */
  public void setLock( RepositoryLock lock );
}
