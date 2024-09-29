/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2018 by Hitachi Vantara : http://www.pentaho.com
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
