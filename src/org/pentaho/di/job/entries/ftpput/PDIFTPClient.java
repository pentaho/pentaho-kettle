/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.job.entries.ftpput;

import java.io.IOException;
import java.text.ParseException;

import org.apache.log4j.Logger;
import org.pentaho.di.i18n.BaseMessages;

import com.enterprisedt.net.ftp.FTPClient;
import com.enterprisedt.net.ftp.FTPException;
import com.enterprisedt.net.ftp.FTPFile;

/**
 * This class should largely be able to be removed if the edtftp project
 * accepts my change to replace dirDetails(".") with dirDetails(null).
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

  private static Class<?> PKG = PDIFTPClient.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$
  private static Logger log4j = Logger.getLogger(PDIFTPClient.class);

  public PDIFTPClient() {
    super();
    log4j.info(BaseMessages.getString(PKG, "PDIFTPClient.DEBUG.Using.Overridden.FTPClient")); //$NON-NLS-1$
  }
  
  /*
   *  (non-Javadoc)
   * @see com.enterprisedt.net.ftp.FTPClientInterface#exists(java.lang.String)
   */
  public boolean exists(String remoteFile) throws IOException, FTPException {
    checkConnection(true);

    // first try the SIZE command
    if (sizeSupported) {
      lastReply = control.sendCommand("SIZE " + remoteFile); //$NON-NLS-1$
      char ch = lastReply.getReplyCode().charAt(0);
      if (ch == '2')
        return true;
      if (ch == '5' && fileNotFoundStrings.matches(lastReply.getReplyText()))
        return false;

      sizeSupported = false;
      log4j.debug("SIZE not supported - trying MDTM"); //$NON-NLS-1$
    }

    // then try the MDTM command
    if (mdtmSupported) {
      lastReply = control.sendCommand("MDTM " + remoteFile); //$NON-NLS-1$
      char ch = lastReply.getReplyCode().charAt(0);
      if (ch == '2')
        return true;
      if (ch == '5' && fileNotFoundStrings.matches(lastReply.getReplyText()))
        return false;

      mdtmSupported = false;
      log4j.debug("MDTM not supported - trying LIST"); //$NON-NLS-1$
    }

    try {
      FTPFile[] files = dirDetails(null); // My fix - replace "." with null in this call for MVS support
      for (int i = 0; i < files.length; i++) {
        if (files[i].getName().equals(remoteFile)) {
          return files[i].isFile();
        }
      }
      return false;
    } catch (ParseException ex) {
      log4j.warn(ex.getMessage());
      return false;
    }
  }

}
