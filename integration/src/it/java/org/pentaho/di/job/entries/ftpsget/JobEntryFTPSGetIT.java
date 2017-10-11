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
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.entry.JobEntryBase;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Andrey Khayrutdinov
 */
public class JobEntryFTPSGetIT {

  private static FtpsServer server;
  private static String ramDir;

  @BeforeClass
  public static void createServer() throws Exception {
    KettleEnvironment.init();

    server = FtpsServer.createDefaultServer();
    server.start();

    ramDir = "ram://" + JobEntryFTPSGetIT.class.getSimpleName();
    KettleVFS.getFileObject( ramDir ).createFolder();
  }

  @AfterClass
  public static void stopServer() throws Exception {
    if ( server != null ) {
      server.stop();
      server = null;
    }

    KettleVFS.getFileObject( ramDir ).delete();
  }


  @Test
  public void downloadFile_WhenDestinationIsSetViaVariable() throws Exception {
    final String myVar = "my-var";
    final String expectedDownloadedFilePath = ramDir + "/" + FtpsServer.SAMPLE_FILE;

    JobEntryFTPSGet job = createCommonJob();
    job.setVariable( myVar, ramDir );
    job.setTargetDirectory( String.format( "${%s}", myVar ) );

    FileObject downloaded = KettleVFS.getFileObject( expectedDownloadedFilePath );
    assertFalse( downloaded.exists() );
    try {
      job.execute( new Result(), 1 );
      downloaded = KettleVFS.getFileObject( expectedDownloadedFilePath );
      assertTrue( downloaded.exists() );
    } finally {
      downloaded.delete();
    }
  }

  @Test
  public void downloadFile_WhenDestinationIsSetDirectly() throws Exception {
    JobEntryFTPSGet job = createCommonJob();
    job.setTargetDirectory( ramDir );

    FileObject downloaded = KettleVFS.getFileObject( ramDir + "/" + FtpsServer.SAMPLE_FILE );
    assertFalse( downloaded.exists() );
    try{
      job.execute( new Result(), 1 );
      downloaded = KettleVFS.getFileObject( ramDir + "/" + FtpsServer.SAMPLE_FILE );
      assertTrue( downloaded.exists() );
    } finally {
      downloaded.delete();
    }
  }


  private static JobEntryFTPSGet createCommonJob() {
    JobEntryFTPSGet job = new JobEntryFTPSGet();
    setMockParent( job );
    setCommonServerProperties( job );
    return job;
  }

  private static void setMockParent( JobEntryBase job ) {
    Job parent = mock( Job.class );
    when( parent.isStopped() ).thenReturn( false );
    job.setParentJob( parent );
    job.setLogLevel( LogLevel.NOTHING );
  }

  private static void setCommonServerProperties( JobEntryFTPSGet job ) {
    job.setConnectionType( FTPSConnection.CONNECTION_TYPE_FTP_IMPLICIT_SSL );
    job.setUserName( FtpsServer.ADMIN );
    job.setPassword( FtpsServer.PASSWORD );
    job.setServerName( "localhost" );
    job.setPort( Integer.toString( FtpsServer.DEFAULT_PORT ) );
    job.setFTPSDirectory( "/" );
  }
}
