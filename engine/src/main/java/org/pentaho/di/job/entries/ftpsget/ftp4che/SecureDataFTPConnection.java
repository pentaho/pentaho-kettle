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

package org.pentaho.di.job.entries.ftpsget.ftp4che;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.ftp4che.FTPConnection;
import org.ftp4che.commands.Command;
import org.ftp4che.exception.ConfigurationException;
import org.ftp4che.exception.FtpIOException;
import org.ftp4che.exception.FtpWorkflowException;
import org.ftp4che.impl.SecureFTPConnection;
import org.ftp4che.util.ftpfile.FTPFile;

/**
 * SecureFTPConnection changed to force data channel encryption for implicit TLS/SSL connections with crypted data.
 */
public class SecureDataFTPConnection extends SecureFTPConnection {

  public SecureDataFTPConnection( FTPConnection conn, String password, int timeout ) throws ConfigurationException {
    setConnectionType( conn.getConnectionType() );
    setAddress( conn.getAddress() );
    setUser( conn.getUser() );
    setPassword( password );
    setAccount( conn.getAccount() );
    setPassiveMode( conn.isPassiveMode() );
    setTryResume( conn.isTryResume() );
    setProxy( conn.getProxy() );
  }

  /**
   * Also sends a PROT P command for the implicit modes with crypted data.<br>
   * {@inheritDoc}
   */
  public List<FTPFile> getDirectoryListing( String directory )
      throws IOException, FtpWorkflowException, FtpIOException {
    setDataProtIfImplicit();
    return super.getDirectoryListing( directory );
  }

  /**
   * Also sends a PROT P command for the implicit modes with crypted data.<br>
   * {@inheritDoc}
   */
  @Override
  public void uploadFile( FTPFile fromFile, FTPFile toFile ) throws IOException, FtpWorkflowException, FtpIOException {
    setDataProtIfImplicit();
    super.uploadFile( fromFile, toFile );
  }

  /**
   * Also sends a PROT P command for the implicit modes with crypted data.<br>
   * {@inheritDoc}
   */
  @Override
  public void uploadStream( InputStream upStream, FTPFile toFile )
    throws IOException, FtpWorkflowException, FtpIOException {
    setDataProtIfImplicit();
    super.uploadStream( upStream, toFile );
  }

  /**
   * Send a PROT P command if on implicit modes with crypted data.<br>
   * @throws IOException
   */
  protected void setDataProtIfImplicit() throws IOException {
    // enable PROT P for cases missing in super
    int connectionType = getConnectionType();
    if ( connectionType == FTPConnection.IMPLICIT_SSL_WITH_CRYPTED_DATA_FTP_CONNECTION
        || connectionType == FTPConnection.IMPLICIT_TLS_WITH_CRYPTED_DATA_FTP_CONNECTION ) {
      setConnectionStatusLock( CSL_INDIRECT_CALL );
      setConnectionStatus( FTPConnection.BUSY );
      Command pbsz = new Command( Command.PBSZ, "0" );
      sendCommand( pbsz ).dumpReply();
      Command prot = new Command( Command.PROT, "P" );
      sendCommand( prot ).dumpReply();
      setConnectionStatus( FTPConnection.IDLE );
      setConnectionStatusLock( CSL_DIRECT_CALL );
    }
  }
}
