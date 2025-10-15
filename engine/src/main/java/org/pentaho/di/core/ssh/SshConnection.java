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

/**
 * SSH connection interface providing secure shell connectivity and operations.
 * All methods throw specific SSH exceptions instead of generic exceptions for better error handling.
 */
public interface SshConnection extends AutoCloseable {

  /**
   * Establishes the SSH connection.
   *
   * @throws SshAuthenticationException if authentication fails
   * @throws SshTimeoutException if connection times out
   * @throws SshConnectionException for other connection errors
   */
  void connect() throws SshConnectionException;

  /**
   * Executes a command on the remote SSH server.
   *
   * @param command the command to execute
   * @param timeoutMs timeout in milliseconds
   * @return the execution result
   * @throws SshTimeoutException if the command times out
   * @throws SshConnectionException for other execution errors
   */
  ExecResult exec( String command, long timeoutMs ) throws SshConnectionException;

  /**
   * Opens an SFTP session for file operations.
   *
   * @return the SFTP session
   * @throws SftpException if SFTP session creation fails
   * @throws SshConnectionException for other connection errors
   */
  SftpSession openSftp() throws SshConnectionException;

  /**
   * Closes the SSH connection and releases resources.
   */
  @Override
  void close();
}
