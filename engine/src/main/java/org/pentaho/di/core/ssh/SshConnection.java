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

import java.io.IOException;

public interface SshConnection extends AutoCloseable {
  void connect() throws Exception;

  ExecResult exec( String command, long timeoutMs ) throws IOException, Exception;

  SftpSession openSftp() throws IOException, Exception;

  @Override
  void close();
}
