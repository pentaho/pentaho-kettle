package org.pentaho.di.core.ssh;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.UUID;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.test.util.TestCleanupUtil;

public abstract class AbstractSshConnectionContractTest {

  protected String getUsername() {
    return "test";
  }

  protected String getPassword() {
    return "test";
  }

  private static TestSshServer server;
  protected SshConnection connection;
  protected static int port;
  private String tempFileName;

  @BeforeClass
  public static void startServer() throws Exception {
    KettleEnvironment.init();
    server = new TestSshServer();
    server.start( 0 ); // random port
    port = server.getAssignedPort();
  }

  @AfterClass
  public static void stopServer() throws Exception {
    KettleEnvironment.shutdown();
    TestCleanupUtil.cleanUpLogsDir();
    if ( server != null ) {
      server.stop();
    }
  }

  @Before
  public void setUp() throws Exception {
    // Generate unique filename to avoid conflicts between test runs
    tempFileName = "upload-" + UUID.randomUUID().toString() + ".txt";

    SshConfig cfg = SshConfig.create()
      .host( "127.0.0.1" )
      .port( port )
      .username( getUsername() )
      .password( getPassword() )
      .connectTimeoutMillis( 10000 ) // 10 second connect timeout
      .commandTimeoutMillis( 30000 ); // 30 second command timeout
    connection = SshConnectionFactory.defaultFactory().open( cfg );
    connection.connect();
  }

  @After
  public void tearDown() {
    if ( connection != null ) {
      connection.close();
    }

    // Clean up any files that might have been created in the current working directory
    // (the SFTP server operates in the current directory)
    try {
      Files.deleteIfExists( java.nio.file.Paths.get( "upload.txt" ) );
      if ( tempFileName != null ) {
        Files.deleteIfExists( java.nio.file.Paths.get( tempFileName ) );
      }
    } catch ( IOException e ) {
      // Ignore cleanup errors
    }
  }

  @Test
  public void testExecEcho() throws Exception {
    ExecResult res = connection.exec( "echo test123", 5000 );
    assertTrue( res.getStdout().contains( "test123" ) );
    assertEquals( 0, res.getExitCode() );
    assertFalse( res.isError() );
  }

  @Test
  public void testSftpUploadDownload() throws Exception {
    SftpSession sftp = connection.openSftp();
    try {
      String content = "hello world";

      // Use temporary filename to avoid conflicts and pollution
      sftp.upload( new ByteArrayInputStream( content.getBytes( StandardCharsets.UTF_8 ) ), tempFileName, true );

      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      sftp.download( tempFileName, bos );
      assertEquals( content, bos.toString( StandardCharsets.UTF_8 ) );

      // Clean up the uploaded file immediately after test
      sftp.delete( tempFileName );

    } finally {
      sftp.close();
    }
  }
}
