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

package org.pentaho.di.core.ssh;

import org.pentaho.di.core.ssh.exceptions.SshConnectionException;
import org.pentaho.di.core.ssh.mina.MinaSshConnection;

public interface SshConnectionFactory {
  SshConnection open( SshConfig config ) throws SshConnectionException;

  static SshConnectionFactory defaultFactory() {
    return new SshConnectionFactory() {
      @Override
      public SshConnection open( SshConfig config ) throws SshConnectionException {
        return new MinaSshConnection( config );
      }
    };
  }
}
