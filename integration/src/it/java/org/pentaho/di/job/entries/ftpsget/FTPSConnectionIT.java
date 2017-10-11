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

package org.pentaho.di.job.entries.ftpsget;

import org.ftp4che.util.ftpfile.FTPFile;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;

import java.io.File;
import java.io.PrintWriter;

import static org.apache.commons.io.FileUtils.contentEquals;
import static org.junit.Assert.assertTrue;
import static org.pentaho.di.job.entries.ftpsget.FTPSConnection.CONNECTION_TYPE_FTP_IMPLICIT_SSL;
import static org.pentaho.di.job.entries.ftpsget.FtpsServer.*;

/**
 * @author Andrey Khayrutdinov
 */
public class FTPSConnectionIT {

  private static FtpsServer server;

  @BeforeClass
  public static void createServer() throws Exception {
    KettleEnvironment.init();

    server = FtpsServer.createDefaultServer();
    server.start();
  }

  @AfterClass
  public static void stopServer() throws Exception {
    if ( server != null ) {
      server.stop();
      server = null;
    }
  }


  private FTPSConnection connection;

  @Before
  public void createConnection() throws Exception {
    connection = new FTPSConnection( CONNECTION_TYPE_FTP_IMPLICIT_SSL, "localhost", DEFAULT_PORT, ADMIN, PASSWORD );
    connection.connect();
  }

  @After
  public void closeConnection() throws Exception {
    if ( connection != null ) {
      connection.disconnect();
      connection = null;
    }
  }


  @Test
  public void download() throws Exception {
    File tmp = File.createTempFile( "FTPSConnectionTest", "download" );

    tmp.deleteOnExit();
    try {
      connection.downloadFile( new FTPFile( "/", SAMPLE_FILE, false ), tmp.getAbsolutePath() );
      assertTrue( contentEquals( new File( FtpsServer.USER_HOME_DIR + "/" + SAMPLE_FILE ), tmp ) );
    } finally {
      tmp.delete();
    }
  }

  @Test
  public void upload() throws Exception {
    File tmp = File.createTempFile( "FTPSConnectionTest", "download" );
    PrintWriter pw = new PrintWriter( tmp );
    try {
      pw.print( "test" );
      pw.flush();
    } finally {
      pw.close();
    }

    tmp.deleteOnExit();
    try {
      connection.uploadFile( tmp.getAbsolutePath(), "uploaded.txt" );

      File uploaded = new File( FtpsServer.USER_HOME_DIR + "/uploaded.txt" );
      assertTrue( uploaded.exists() );
      assertTrue( contentEquals( uploaded, tmp ) );

      uploaded.delete();
    } finally {
      tmp.delete();
    }
  }
}
