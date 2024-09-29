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


package org.pentaho.di.job.entries.ftp;

import java.net.InetAddress;
import java.net.UnknownHostException;

import com.enterprisedt.net.ftp.FTPClient;

public class MockedJobEntryFTP extends JobEntryFTP {

  @Override
  protected FTPClient initFTPClient() {
    return new MockedFTPClient();
  }

  @Override
  protected InetAddress getInetAddress( String realServername ) throws UnknownHostException {
    return null;
  }

}
