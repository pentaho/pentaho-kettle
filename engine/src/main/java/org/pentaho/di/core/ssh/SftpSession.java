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

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.pentaho.di.core.ssh.exceptions.SftpException;

/**
 * SFTP session interface providing secure file transfer operations.
 * All methods throw specific SFTP exceptions instead of generic IOException for better error handling.
 */
public interface SftpSession extends AutoCloseable {

  /**
   * Lists files and directories in the specified remote path.
   *
   * @param path the remote path to list
   * @return list of files and directories
   * @throws SftpException if the operation fails
   */
  List<SftpFile> list( String path ) throws SftpException;

  /**
   * Checks if a remote path exists.
   *
   * @param path the remote path to check
   * @return true if the path exists, false otherwise
   * @throws SftpException if the operation fails
   */
  boolean exists( String path ) throws SftpException;

  /**
   * Checks if a remote path is a directory.
   *
   * @param path the remote path to check
   * @return true if the path is a directory, false otherwise
   * @throws SftpException if the operation fails
   */
  boolean isDirectory( String path ) throws SftpException;

  /**
   * Gets the size of a remote file.
   *
   * @param path the remote file path
   * @return the file size in bytes
   * @throws SftpException if the operation fails
   */
  long size( String path ) throws SftpException;

  /**
   * Downloads a remote file to the provided output stream.
   *
   * @param remote the remote file path
   * @param target the output stream to write to
   * @throws SftpException if the download fails
   */
  void download( String remote, OutputStream target ) throws SftpException;

  /**
   * Uploads data from an input stream to a remote file.
   *
   * @param source the input stream to read from
   * @param remote the remote file path
   * @param overwrite whether to overwrite existing files
   * @throws SftpException if the upload fails
   */
  void upload( InputStream source, String remote, boolean overwrite ) throws SftpException;

  /**
   * Creates a directory on the remote server.
   *
   * @param path the remote directory path to create
   * @throws SftpException if the operation fails
   */
  void mkdir( String path ) throws SftpException;

  /**
   * Deletes a file or directory on the remote server.
   *
   * @param path the remote path to delete
   * @throws SftpException if the operation fails
   */
  void delete( String path ) throws SftpException;

  /**
   * Renames or moves a file or directory on the remote server.
   *
   * @param oldPath the current remote path
   * @param newPath the new remote path
   * @throws SftpException if the operation fails
   */
  void rename( String oldPath, String newPath ) throws SftpException;

  /**
   * Closes the SFTP session and releases resources.
   */
  @Override
  void close();
}
