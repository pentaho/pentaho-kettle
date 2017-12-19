/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
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
