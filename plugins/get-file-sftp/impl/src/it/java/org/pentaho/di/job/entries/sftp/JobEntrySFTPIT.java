/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.di.job.entries.sftp;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.Session;
import org.apache.commons.vfs2.FileObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.pentaho.di.core.bowl.DefaultBowl;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.job.Job;
import org.pentaho.di.utils.TestUtils;

import java.io.ByteArrayInputStream;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Andrey Khayrutdinov
 */
public class JobEntrySFTPIT {

  @ClassRule
  public static TemporaryFolder folder = new TemporaryFolder();

  private static SftpServer server;

  @BeforeClass
  public static void prepareEnv() throws Exception {
    KettleEnvironment.init();

    server = SftpServer.createDefaultServer( folder );
    server.start();
  }

  @AfterClass
  public static void stopServer() throws Exception {
    server.stop();
    server = null;
  }

  @Test
  public void getFile_WhenDestinationIsSetViaVariable() throws Exception {
    final String localDir = TestUtils.createTempDir();
    KettleVFS.getInstance( DefaultBowl.getInstance() ).getFileObject( localDir ).createFolder();

    final String myVar = "my-var";

    final String sftpDir = "job-entry-sftp-test";
    final String fileName = "file.txt";

    uploadFile( sftpDir, fileName );

    JobEntrySFTP job = new JobEntrySFTP();
    job.setVariable( myVar, localDir );

    Job parent = mock( Job.class );
    when( parent.isStopped() ).thenReturn( false );
    job.setParentJob( parent );
    job.setLogLevel( LogLevel.NOTHING );

    job.setUserName( server.getUsername() );
    job.setPassword( server.getPassword() );
    job.setServerName( "localhost" );
    job.setServerPort( Integer.toString( server.getPort() ) );
    job.setScpDirectory( sftpDir );
    job.setTargetDirectory( String.format( "${%s}", myVar ) );

    job.execute( new Result(), 1 );

    FileObject downloaded = KettleVFS.getInstance( DefaultBowl.getInstance() )
      .getFileObject( localDir + "/" + fileName );
    assertTrue( downloaded.exists() );
    downloaded.delete();
  }

  private void uploadFile( String dir, String file ) throws Exception {
    Session session = server.createJschSession();
    session.connect();
    try {
      ChannelSftp sftp = (ChannelSftp) session.openChannel( "sftp" );
      sftp.connect();
      try {
        sftp.mkdir( dir );
        sftp.cd( dir );
        sftp.put( new ByteArrayInputStream( "data".getBytes() ), file );
      } finally {
        sftp.disconnect();
      }
    } finally {
      session.disconnect();
    }
  }
}
