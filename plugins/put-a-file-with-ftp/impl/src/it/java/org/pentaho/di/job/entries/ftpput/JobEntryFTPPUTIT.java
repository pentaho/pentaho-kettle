/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2023 by Hitachi Vantara : http://www.pentaho.com
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
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entries.ftpsget.FTPSConnection;
import org.pentaho.di.job.entry.JobEntryBase;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.pentaho.di.job.entries.ftpsget.FTPSConnection.CONNECTION_TYPE_FTP;

/**
 * @author Andrey Khayrutdinov
 */
public class JobEntryFTPPUTIT {

  private static FtpsServer server;
  @ClassRule
  public static TemporaryFolder ftpFolder = new TemporaryFolder();
  @Rule
  public TemporaryFolder localFolder = new TemporaryFolder();

  private FTPSConnection connection;
  private File tempFile;
  private JobEntryFTPPUT jobEntry;

  private static final int FTP_PORT = 8123;
  private static final String FTP_USER = "FTP_USER";
  private static final String FTP_USER_PASSWORD = "FTP_USER_PASSWORD";

  @BeforeClass
  public static void prepareEnv() throws Exception {
    KettleEnvironment.init();
    server =
      FtpsServer.createFTPServer( FTP_PORT, FTP_USER, FTP_USER_PASSWORD, ftpFolder.getRoot(), false );
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
  public void prepareResources() throws Exception {
    tempFile = new File( localFolder.getRoot(), UUID.randomUUID().toString() );
    tempFile.createNewFile();

    jobEntry = new JobEntryFTPPUT();
    jobEntry.setUserName( FTP_USER );
    jobEntry.setPassword( FTP_USER_PASSWORD );
    jobEntry.setServerName( "localhost" );
    jobEntry.setServerPort( Integer.toString( FTP_PORT ) );
    jobEntry.setActiveConnection( true );
    jobEntry.setControlEncoding( StandardCharsets.UTF_8.name() );
    jobEntry.setBinaryMode( true );
    // tempFile is a UUID ==> it is a valid wildcard to itself (no need to escape)
    jobEntry.setWildcard( tempFile.getName() );
    setMockParent( jobEntry );

    connection =
      new FTPSConnection( CONNECTION_TYPE_FTP, "localhost", FTP_PORT, FTP_USER, FTP_USER_PASSWORD, new Variables() );
    connection.connect();
  }

  @After
  public void releaseResources() throws Exception {
    jobEntry = null;

    if ( connection != null ) {
      connection.disconnect();
      connection = null;
    }
  }

  @Test
  public void reportsError_WhenLocalDirectoryIsNotSet() throws Exception {
    jobEntry.setLocalDirectory( null );

    Result result = jobEntry.execute( new Result(), 0 );
    assertEquals( 1, result.getNrErrors() );
  }

  @Test
  public void uploadsFile_WhenSourceFolderIsSet_WithOutProtocolPrefix() throws Exception {
    jobEntry.setLocalDirectory( localFolder.getRoot().getAbsolutePath() );

    doTestUploadsFile( jobEntry, tempFile.getName() );
  }

  @Test
  public void uploadsFile_WhenSourceFolderIsSet_WithProtocolPrefix() throws Exception {
    jobEntry.setLocalDirectory( localFolder.getRoot().toURI().toString() );

    doTestUploadsFile( jobEntry, tempFile.getName() );
  }

  @Test
  public void uploadsFile_WhenSourceFolderIsSet_ViaVariable() throws Exception {
    String absolutePath = localFolder.getRoot().getAbsolutePath();
    jobEntry.setLocalDirectory( "${" + Const.INTERNAL_VARIABLE_JOB_FILENAME_DIRECTORY + "}" );
    jobEntry.setVariable( Const.INTERNAL_VARIABLE_JOB_FILENAME_DIRECTORY, absolutePath );

    doTestUploadsFile( jobEntry, tempFile.getName() );
  }

  private void doTestUploadsFile( JobEntryFTPPUT entry, String fileName ) throws Exception {
    assertFalse( connection.isFileExists( fileName ) );
    entry.execute( new Result(), 0 );
    assertTrue( connection.isFileExists( fileName ) );
  }

  private static void setMockParent( JobEntryBase job ) {
    Job parentJob = mock( Job.class );
    when( parentJob.isStopped() ).thenReturn( false );
    when( parentJob.getLogLevel() ).thenReturn( LogLevel.BASIC );
    when( parentJob.getContainerObjectId() ).thenReturn( UUID.randomUUID().toString() );
    job.setParentJob( parentJob );
    JobMeta parentJobMeta = mock( JobMeta.class );
    when( parentJobMeta.getNamedClusterEmbedManager() ).thenReturn( null );
    job.setParentJobMeta( parentJobMeta );
    job.setLogLevel( LogLevel.ROWLEVEL );
  }
}
