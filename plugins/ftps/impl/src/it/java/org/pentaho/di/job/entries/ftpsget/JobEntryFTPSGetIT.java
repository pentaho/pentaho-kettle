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

import org.apache.commons.vfs2.FileObject;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryBase;

import java.util.UUID;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Andrey Khayrutdinov
 */
public class JobEntryFTPSGetIT {

  private static FtpsServer server;

  private static final String FTP_USER = "FTP_USER";
  private static final String FTP_USER_PASSWORD = "FTP_USER_PASSWORD";

  @ClassRule
  public static TemporaryFolder ftpFolder = new TemporaryFolder();
  @Rule
  public TemporaryFolder outputFolder = new TemporaryFolder();

  private JobEntryFTPSGet jobEntry;

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
  public void createJobEntry() {
    jobEntry = new JobEntryFTPSGet();
    setMockParent( jobEntry );
    setServerProperties( jobEntry );
  }

  @Test
  public void downloadFile_WhenDestinationIsSetViaVariable() throws Exception {
    final String myVar = "my-var";
    final String expectedDownloadedFilePath = outputFolder.getRoot().getAbsolutePath() + "/" + FtpsServer.SAMPLE_FILE;

    jobEntry.setVariable( myVar, outputFolder.getRoot().getAbsolutePath() );
    jobEntry.setTargetDirectory( String.format( "${%s}", myVar ) );

    FileObject downloaded = KettleVFS.getFileObject( expectedDownloadedFilePath );
    assertFalse( downloaded.exists() );
    try {
      jobEntry.execute( new Result(), 1 );
      downloaded = KettleVFS.getFileObject( expectedDownloadedFilePath );
      assertTrue( downloaded.exists() );
    } finally {
      downloaded.delete();
    }
  }

  @Test
  public void downloadFile_WhenDestinationIsSetDirectly() throws Exception {
    final String expectedDownloadedFilePath = outputFolder.getRoot().getAbsolutePath() + "/" + FtpsServer.SAMPLE_FILE;

    jobEntry.setTargetDirectory( outputFolder.getRoot().getAbsolutePath() );

    FileObject downloaded = KettleVFS.getFileObject( expectedDownloadedFilePath );
    assertFalse( downloaded.exists() );
    try {
      jobEntry.execute( new Result(), 1 );
      downloaded = KettleVFS.getFileObject( expectedDownloadedFilePath );
      assertTrue( downloaded.exists() );
    } finally {
      downloaded.delete();
    }
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
    job.setLogLevel( LogLevel.NOTHING );
  }

  private static void setServerProperties( JobEntryFTPSGet job ) {
    job.setConnectionType( FTPSConnection.CONNECTION_TYPE_FTP_IMPLICIT_SSL );
    job.setUserName( FTP_USER );
    job.setPassword( FTP_USER_PASSWORD );
    job.setServerName( "localhost" );
    job.setPort( Integer.toString( FtpsServer.DEFAULT_PORT ) );
    job.setFTPSDirectory( "/" );
  }
}
