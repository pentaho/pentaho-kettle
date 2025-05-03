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

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.apache.sshd.common.file.virtualfs.VirtualFileSystemFactory;
import org.apache.sshd.scp.server.ScpCommandFactory;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.auth.password.PasswordAuthenticator;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.session.ServerSession;
import org.apache.sshd.sftp.server.SftpSubsystemFactory;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Properties;
import java.util.Random;
import java.util.UUID;

/**
 * @author Andrey Khayrutdinov
 */
public class SftpServer implements PasswordAuthenticator {

  private final String username;
  private final String password;
  private final SshServer server;

  /**
   * Creates a server's instance for <tt>localhost</tt>, using supplied values for username, password, and port.
   *
   * @param username    The username that will be allowed for authentication
   * @param password    The password that will be allowed for authentication
   * @param port        The port number that the SSH Server should listen on
   * @param homeDir     The local directory that should be the SSH Server's root directory
   * @param hostKeyPath The file that should be used to store the SSH Host Key
   * @return An SftpServer instance
   * @throws IOException
   */
  public SftpServer( String username, String password, int port, String homeDir, String hostKeyPath ) {
    this.username = username;
    this.password = password;
    this.server = createSshServer( port, homeDir, hostKeyPath );
  }

  /**
   * Creates a server's instance for <tt>localhost</tt> using random values for username, password, and port.
   *
   * @param folder temporary folder
   * @return server's instance
   * @throws IOException
   */
  public static SftpServer createDefaultServer( TemporaryFolder folder ) throws IOException {
    return new SftpServer( "fakeuser", UUID.randomUUID().toString(), new Random().nextInt( 20000 ) + 1024,
      folder.getRoot().getAbsolutePath(), folder.newFile( "server.key" ).getAbsolutePath() );
  }

  private org.apache.sshd.server.SshServer createSshServer( int port, String homeDir, String hostKeyPath ) {
    SshServer server = SshServer.setUpDefaultServer();
    server.setHost( "localhost" );
    server.setPort( port );
    server.setFileSystemFactory( new VirtualFileSystemFactory( Paths.get( homeDir ) ) );
    server.setSubsystemFactories( Collections.singletonList( new SftpSubsystemFactory() ) );
    server.setCommandFactory( new ScpCommandFactory() );

    SimpleGeneratorHostKeyProvider keyProvider = new SimpleGeneratorHostKeyProvider( Paths.get( hostKeyPath ) );
    keyProvider.setAlgorithm( "RSA" );
    server.setKeyPairProvider( keyProvider );

    server.setPasswordAuthenticator( this );
    return server;
  }

  public String getUsername() {
    return username;
  }

  public String getPassword() {
    return password;
  }

  public int getPort() {
    return server.getPort();
  }

  public void start() throws IOException {
    server.start();
  }

  public void stop() throws Exception {
    server.stop();
  }

  public Session createJschSession() throws JSchException {
    JSch jsch = new JSch();
    com.jcraft.jsch.Session session = jsch.getSession( username, server.getHost(), server.getPort() );
    session.setPassword( password );

    Properties config = new java.util.Properties();
    config.put( "StrictHostKeyChecking", "no" );
    session.setConfig( config );

    return session;
  }

  @Override
  public boolean authenticate( String username, String password, ServerSession session ) {
    return this.username.equals( username ) && this.password.equals( password );
  }
}
