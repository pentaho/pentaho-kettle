/*!
 * Copyright 2010-2020 Hitachi Vantara.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.pentaho.di.repository.pur;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleSecurityException;
import org.pentaho.di.core.exception.KettleUnsupportedOperationException;
import org.pentaho.di.core.logging.LogChannelInterface;

public interface IRepositoryConnector {

  /**
   * <p>Connect to the repository. Make sure you don't connect more than once to the same repository with this
   * repository object.</p>
   * <p><b>Note:</b> in certain scenarios, when a valid Pentaho session exists, the given credentials may be ignored
   * and the whole authentication process may be skipped.</p>
   *
   * @param username
   *          the username of the user connecting to the repository.
   * @param password
   *          the password of the user connecting to the repository.
   * @throws KettleSecurityException
   *           in case the supplied user or password is incorrect.
   * @throws KettleException
   *           in case there is a general unexpected error OR if we're already connected to the repository.
   * @see #connect(String, String, boolean)
   */
  public RepositoryConnectResult connect( final String username, final String password ) throws KettleException,
    KettleSecurityException;

  /**
   * <p>Connect to the repository. Make sure you don't connect more than once to the same repository with this
   * repository object.</p>
   * <p><b>Note:</b> in certain scenarios, when a valid Pentaho session exists, the given credentials may be ignored
   * and the whole authentication process may be skipped.</p>
   * <p>This behaviour may be disallowed by passing {@code false} in the proper parameter.</p>
   *
   * @param username
   *          the username of the user connecting to the repository.
   * @param password
   *          the password of the user connecting to the repository.
   * @param allowSkipAuthentication
   *          if it's allowed to skip the authentication.
   * @throws KettleSecurityException
   *           in case the supplied user or password is incorrect.
   * @throws KettleException
   *           in case there is a general unexpected error OR if we're already connected to the repository.
   * @see #connect(String, String)
   */
  default RepositoryConnectResult connect( final String username, final String password,
    boolean allowSkipAuthentication ) throws KettleException {
    if ( allowSkipAuthentication ) {
      return connect( username, password );
    } else {
      throw new KettleUnsupportedOperationException();
    }
  }

  public void disconnect();

  public LogChannelInterface getLog();

  public ServiceManager getServiceManager();
}
