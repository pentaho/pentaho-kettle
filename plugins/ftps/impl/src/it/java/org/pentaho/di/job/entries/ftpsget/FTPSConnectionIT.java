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


package org.pentaho.di.job.entries.ftpsget;

import org.ftp4che.util.ftpfile.FTPFile;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.variables.Variables;

import java.io.File;
import java.io.PrintWriter;

import static org.apache.commons.io.FileUtils.contentEquals;
import static org.junit.Assert.assertTrue;
import static org.pentaho.di.job.entries.ftpsget.FTPSConnection.CONNECTION_TYPE_FTP_IMPLICIT_SSL;

/**
 * @author Andrey Khayrutdinov
 */
public class FTPSConnectionIT {


  private static final String FTP_USER = "FTP_USER";
  private static final String FTP_USER_PASSWORD = "FTP_USER_PASSWORD";

  @ClassRule
  public static TemporaryFolder ftpFolder = new TemporaryFolder();
  private static FtpsServer server;

  private FTPSConnection connection;

  @BeforeClass
  public static void createServer() throws Exception {
    KettleEnvironment.init();
    server =
      FtpsServer.createFTPServer( FtpsServer.DEFAULT_PORT, FTP_USER, FTP_USER_PASSWORD, ftpFolder.getRoot(), true );
    server.start();
  }

  @AfterClass
  public static void stopServer() throws Exception {
    if ( server != null ) {
      server.stop();
      server = null;
    }
  }

  @Before
  public void createConnection() throws Exception {
    connection = new FTPSConnection( CONNECTION_TYPE_FTP_IMPLICIT_SSL, "localhost", FtpsServer.DEFAULT_PORT, FTP_USER,
      FTP_USER_PASSWORD, new Variables() );
    connection.connect();
  }

  @After
  public void closeConnection() {
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
      connection.downloadFile( new FTPFile( "/", FtpsServer.SAMPLE_FILE, false ), tmp.getAbsolutePath() );
      assertTrue( contentEquals( new File( FtpsServer.USER_HOME_DIR + "/" + FtpsServer.SAMPLE_FILE ), tmp ) );
    } finally {
      tmp.delete();
    }
  }

  @Test
  public void upload() throws Exception {
    File tmp = File.createTempFile( "FTPSConnectionTest", "download" );
    tmp.deleteOnExit();

    try ( PrintWriter pw = new PrintWriter( tmp ) ) {
      pw.print( "test" );
      pw.flush();
    }

    try {
      connection.uploadFile( tmp.getAbsolutePath(), "uploaded.txt" );

      File uploaded = new File( ftpFolder.getRoot().getAbsolutePath() + "/uploaded.txt" );
      assertTrue( uploaded.exists() );
      assertTrue( contentEquals( uploaded, tmp ) );

      uploaded.delete();
    } finally {
      tmp.delete();
    }
  }
}
