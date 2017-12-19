/********************************************************************************
 *  Portions Copyright (C) 2000-2003  Enterprise Distributed Technologies Ltd
 *  Portions Copyright (c) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
 *
 *  This file is dual licensed under the terms of the Apache Software License
 *  Version 2.0, and the GNU Lesser GPL Version 2.1 as provided for by the
 *  edtFTPj project (http://www.enterprisedt.com/products/edtftpj/readme.html#Licensing).
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
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

package org.pentaho.di.job.entries.ftpput;

import java.io.IOException;
import java.text.ParseException;

import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.i18n.BaseMessages;

import com.enterprisedt.net.ftp.FTPClient;
import com.enterprisedt.net.ftp.FTPException;
import com.enterprisedt.net.ftp.FTPFile;

/**
 * This class should largely be able to be removed if the edtftp project accepts my change to replace dirDetails(".")
 * with dirDetails(null).
 *
 * @author mbatchelor
 *
 */

public class PDIFTPClient extends FTPClient {

  /**
   * MDTM supported flag
   */
  private boolean mdtmSupported = true;

  /**
   * SIZE supported flag
   */
  private boolean sizeSupported = true;

  private static Class<?> PKG = PDIFTPClient.class; // for i18n purposes, needed by Translator2!!
  private LogChannelInterface log;

  public PDIFTPClient( LogChannelInterface log ) {
    super();
    this.log = log;
    log.logBasic( BaseMessages.getString( PKG, "PDIFTPClient.DEBUG.Using.Overridden.FTPClient" ) );
  }

  /*
   * (non-Javadoc)
   *
   * @see com.enterprisedt.net.ftp.FTPClientInterface#exists(java.lang.String)
   */
  public boolean exists( String remoteFile ) throws IOException, FTPException {
    checkConnection( true );

    // first try the SIZE command
    if ( sizeSupported ) {
      lastReply = control.sendCommand( "SIZE " + remoteFile );
      char ch = lastReply.getReplyCode().charAt( 0 );
      if ( ch == '2' ) {
        return true;
      }
      if ( ch == '5' && fileNotFoundStrings.matches( lastReply.getReplyText() ) ) {
        return false;
      }

      sizeSupported = false;
      log.logDebug( "SIZE not supported - trying MDTM" );
    }

    // then try the MDTM command
    if ( mdtmSupported ) {
      lastReply = control.sendCommand( "MDTM " + remoteFile );
      char ch = lastReply.getReplyCode().charAt( 0 );
      if ( ch == '2' ) {
        return true;
      }
      if ( ch == '5' && fileNotFoundStrings.matches( lastReply.getReplyText() ) ) {
        return false;
      }

      mdtmSupported = false;
      log.logDebug( "MDTM not supported - trying LIST" );
    }

    try {
      FTPFile[] files = dirDetails( null ); // My fix - replace "." with null in this call for MVS support
      for ( int i = 0; i < files.length; i++ ) {
        if ( files[i].getName().equals( remoteFile ) ) {
          return files[i].isFile();
        }
      }
      return false;
    } catch ( ParseException ex ) {
      log.logBasic( ex.getMessage() );
      return false;
    }
  }

}
