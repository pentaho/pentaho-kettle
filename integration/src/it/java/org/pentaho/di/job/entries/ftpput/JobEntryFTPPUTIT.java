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

package org.pentaho.di.job.entries.ftpput;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.Result;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entries.ftpsget.FTPSConnection;
import org.pentaho.di.job.entries.ftpsget.FtpsServer;

import java.io.File;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.pentaho.di.core.Const.INTERNAL_VARIABLE_JOB_FILENAME_DIRECTORY;
import static org.pentaho.di.job.entries.ftpsget.FTPSConnection.CONNECTION_TYPE_FTP;
import static org.pentaho.di.job.entries.ftpsget.FtpsServer.ADMIN;
import static org.pentaho.di.job.entries.ftpsget.FtpsServer.DEFAULT_PORT;
import static org.pentaho.di.job.entries.ftpsget.FtpsServer.PASSWORD;

/**
 * @author Andrey Khayrutdinov
 */
public class JobEntryFTPPUTIT {

  private static FtpsServer server;
  private static TemporaryFolder folder;

  @BeforeClass
  public static void prepareEnv() throws Exception {
    KettleEnvironment.init();

    folder = new TemporaryFolder();
    folder.create();

    server = FtpsServer.createFtpServer();
    server.start();
  }

  @AfterClass
  public static void stopServer() throws Exception {
    if ( server != null ) {
      server.stop();
      server = null;
    }

    folder.delete();
    folder = null;
  }


  private FTPSConnection connection;
  private File tempFile;
  private JobEntryFTPPUT entry;

  @Before
  public void prepareResources() throws Exception {
    connection = new FTPSConnection( CONNECTION_TYPE_FTP, "localhost", DEFAULT_PORT, ADMIN, PASSWORD );
    connection.connect();

    tempFile = new File( folder.getRoot(), UUID.randomUUID().toString() );
    tempFile.createNewFile();

    entry = new JobEntryFTPPUT();
    entry.setUserName( ADMIN );
    entry.setPassword( PASSWORD );
    entry.setServerName( "localhost" );
    entry.setServerPort( Integer.toString( DEFAULT_PORT ) );
    entry.setActiveConnection( true );
    entry.setControlEncoding( "UTF-8" );
    entry.setBinaryMode( true );
    // tempFile is a UUID ==> it is a valid wildcard to itself (no need to escape)
    entry.setWildcard( tempFile.getName() );
  }

  @After
  public void releaseResources() throws Exception {
    entry = null;

    if ( connection != null ) {
      connection.disconnect();
      connection = null;
    }
    // no need to delete tempFile - it will be removed by the folder rule
  }


  @Test
  public void reportsError_WhenLocalDirectoryIsNotSet() throws Exception {
    entry.setLocalDirectory( null );
    Result result = executeStep( entry );
    assertEquals( 1, result.getNrErrors() );
  }

  @Test
  public void uploadsFile_WhenSourceFolderIsSet_WithOutProtocolPrefix() throws Exception {
    entry.setLocalDirectory( folder.getRoot().getAbsolutePath() );

    doTestUploadsFile( entry, tempFile.getName() );
  }

  @Test
  public void uploadsFile_WhenSourceFolderIsSet_WithProtocolPrefix() throws Exception {
    entry.setLocalDirectory( folder.getRoot().toURI().toString() );

    doTestUploadsFile( entry, tempFile.getName() );
  }

  @Test
  public void uploadsFile_WhenSourceFolderIsSet_ViaVariable() throws Exception {
    String absolutePath = folder.getRoot().getAbsolutePath();
    entry.setLocalDirectory( "${" + INTERNAL_VARIABLE_JOB_FILENAME_DIRECTORY + "}" );
    entry.setVariable( INTERNAL_VARIABLE_JOB_FILENAME_DIRECTORY, absolutePath );

    doTestUploadsFile( entry, tempFile.getName() );
  }

  private void doTestUploadsFile( JobEntryFTPPUT entry, String fileName ) throws Exception {
    assertFalse( connection.isFileExists( fileName ) );
    executeStep( entry );
    assertTrue( connection.isFileExists( fileName ) );
  }

  private Result executeStep( JobEntryFTPPUT entry ) throws Exception {
    Job job = new Job( null, new JobMeta() );
    job.setStopped( false );

    entry.setParentJob( job );
    return entry.execute( new Result(), 0 );
  }

}
