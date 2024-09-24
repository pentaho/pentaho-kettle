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

package org.pentaho.di.repository;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleSecurityException;

public class BaseRepositorySecurityProvider {

  protected RepositoryMeta repositoryMeta;
  protected IUser userInfo;
  protected RepositoryCapabilities capabilities;

  public BaseRepositorySecurityProvider( RepositoryMeta repositoryMeta, IUser userInfo ) {
    this.repositoryMeta = repositoryMeta;
    this.userInfo = userInfo;
    this.capabilities = repositoryMeta.getRepositoryCapabilities();
  }

  public IUser getUserInfo() {
    // return a copy of the user info, so that external editing cannot effect the database repo behavior
    // this allows the user info to act as immutable.
    return userInfo != null ? new UserInfo( userInfo ) : null;
  }

  /**
   * @return the repositoryMeta
   */
  public RepositoryMeta getRepositoryMeta() {
    return repositoryMeta;
  }

  /**
   * @param repositoryMeta
   *          the repositoryMeta to set
   */
  public void setRepositoryMeta( RepositoryMeta repositoryMeta ) {
    this.repositoryMeta = repositoryMeta;
  }

  public void validateAction( RepositoryOperation... operations ) throws KettleException, KettleSecurityException {

  }
}
