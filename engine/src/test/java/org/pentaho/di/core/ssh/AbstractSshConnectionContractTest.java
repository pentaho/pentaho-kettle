package org.pentaho.di.core.ssh;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.test.util.TestCleanupUtil;

public abstract class AbstractSshConnectionContractTest {

  protected abstract SshImplementation getImplementation();

  protected String getUsername() {
    return "test";
  }

  protected String getPassword() {
    return "test";
  }

  private static TestSshServer server;
  protected SshConnection connection;
  protected static int port;

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
    SshConfig cfg = SshConfig.create()
      .host( "127.0.0.1" )
      .port( port )
      .username( getUsername() )
      .password( getPassword() )
      .connectTimeoutMillis( 10000 ) // 10 second connect timeout
      .commandTimeoutMillis( 30000 ) // 30 second command timeout
      .implementation( getImplementation() );
    connection = SshConnectionFactory.defaultFactory().open( cfg );
    connection.connect();
  }

  @After
  public void tearDown() {
    if ( connection != null ) {
      connection.close();
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
      sftp.upload( new ByteArrayInputStream( content.getBytes( StandardCharsets.UTF_8 ) ), "upload.txt", true );
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      sftp.download( "upload.txt", bos );
      assertEquals( content, bos.toString( StandardCharsets.UTF_8 ) );
    } finally {
      sftp.close();
    }
  }
}
