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
