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
