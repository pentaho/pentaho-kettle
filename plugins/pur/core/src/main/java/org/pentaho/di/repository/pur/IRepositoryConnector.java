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

package org.pentaho.di.repository.pur;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannelInterface;

public interface IRepositoryConnector {
  RepositoryConnectResult connect( final String username, final String password ) throws KettleException;

  /**
   * Connects using only the given username, without a password. Requires that
   * an OAuth access token or a session cookie is already present in the
   * credential holder — otherwise the connection will fail.
   *
   * <p>
   * Default implementation delegates to {@link #connect(String, String)}
   * with an empty-string password sentinel.
   *
   * @param username the authenticated principal name
   * @return connection result
   * @throws KettleException if no non-basic credential is available or the
   *                         connection fails
   */
  default RepositoryConnectResult connect( final String username ) throws KettleException {
    return connect( username, "" );
  }

  void disconnect();

  LogChannelInterface getLog();

  ServiceManager getServiceManager();
}
