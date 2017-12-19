/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.job.entries.ftp;

import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.text.ParseException;
import java.util.Date;
import java.util.Random;

import com.enterprisedt.net.ftp.DirectoryListCallback;
import com.enterprisedt.net.ftp.FTPClient;
import com.enterprisedt.net.ftp.FTPException;
import com.enterprisedt.net.ftp.FTPFile;
import com.enterprisedt.net.ftp.FTPTransferType;

public class MockedFTPClient extends FTPClient {

  private boolean connected = false;

  @Override
  public void connect() throws IOException, FTPException {
    connected = true;
  }

  @Override
  public boolean connected() {
    return connected;
  }

  @Override
  public void delete( String remoteFile ) throws IOException, FTPException {
  }

  @Override
  public void get( String arg0, String arg1 ) throws IOException, FTPException {
    FileWriter fw = null;
    try {
      Random r = new Random();
      fw = new FileWriter( arg0 );
      for ( int i = 0; i < 100; i++ ) {
        fw.append( (char) ( r.nextInt( 83 ) + 32 ) );
      }
      fw.flush();
      fw.close();
    } finally {
      if ( fw != null ) {
        fw.close();
      }
    }
  }

  @Override
  public void login( String user, String password, String accountInfo ) throws IOException, FTPException {
  }

  @Override
  public void login( String user, String password ) throws IOException, FTPException {
  }

  @Override
  public void mkdir( String dir ) throws IOException, FTPException {
  }

  @Override
  public void password( String password ) throws IOException, FTPException {
  }

  @Override
  public void quit() throws IOException, FTPException {
  }

  @Override
  public void setRemoteHost( String remoteHost ) throws IOException, FTPException {
  }

  @Override
  public void setRemoteAddr( InetAddress remoteAddr ) throws FTPException {
  }

  @Override
  public String system() throws IOException, FTPException {
    return "Wondows";
  }

  @Override
  public void chdir( String dir ) throws IOException, FTPException {
  }

  @Override
  public void dirDetails( String dirname, DirectoryListCallback lister ) throws IOException, FTPException,
    ParseException {
  }

  @Override
  public FTPFile[] dirDetails( String arg0 ) throws IOException, FTPException, ParseException {
    FTPFile[] files = new FTPFile[10];
    for ( int i = 0; i < files.length - 1; i++ ) {
      files[i] = new FTPFile( "file_" + i, "file_" + i, 100, false, new Date() );

    }
    files[files.length - 1] = new FTPFile( "robots.txt", "robots.txt", 100, false, new Date() );
    return files;
  }

  @Override
  public void setType( FTPTransferType type ) throws IOException, FTPException {
  }

}
