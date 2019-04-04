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

/**
 * Allows to connect to repository multiple times (in case of timeout and etc).
 *
 */
public interface ReconnectableRepository extends Repository {

  /**
   * Connect to the repository. This repository type allows to connect more than once to the same repository
   * object.
   *
   * @param username
   *          the username of the user connecting to the repository.
   * @param password
   *          the password of the user connecting to the repository.
   * @throws KettleSecurityException
   *           in case the supplied user or password is incorrect.
   * @throws KettleException
   *           in case there is a general unexpected error.
   */
  public void connect( String username, String password ) throws KettleException, KettleSecurityException;

}
