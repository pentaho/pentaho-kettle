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

package org.pentaho.di.job.entries.sftp;

import java.io.IOException;
import java.util.Collections;
import java.util.Properties;
import java.util.Random;
import java.util.UUID;

import org.apache.sshd.SshServer;
import org.apache.sshd.common.NamedFactory;
import org.apache.sshd.common.file.virtualfs.VirtualFileSystemFactory;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.PasswordAuthenticator;
import org.apache.sshd.server.command.ScpCommandFactory;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.session.ServerSession;
import org.apache.sshd.sftp.subsystem.SftpSubsystem;
import org.junit.rules.TemporaryFolder;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

/**
 * @author Andrey Khayrutdinov
 */
public class SftpServer implements PasswordAuthenticator {

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

  private final String username;
  private final String password;

  private final SshServer server;

  /**
   * Creates a server's instance for <tt>localhost</tt>, using supplied values for username, password, and port.
   * 
   * @param username
   *   The username that will be allowed for authentication
   * @param password
   *   The password that will be allowed for authentication
   * @param port
   *   The port number that the SSH Server should listen on
   * @param homeDir
   *   The local directory that should be the SSH Server's root directory
   * @param hostKeyPath
   *   The file that should be used to store the SSH Host Key
   * @return
   *   An SftpServer instance
   * @throws IOException
   */
  public SftpServer( String username, String password, int port, String homeDir, String hostKeyPath ) {
    this.username = username;
    this.password = password;
    this.server = createSshServer( port, homeDir, hostKeyPath );
  }

  private SshServer createSshServer( int port, String homeDir, String hostKeyPath ) {
    SshServer server = SshServer.setUpDefaultServer();
    server.setHost( "localhost" );
    server.setPort( port );
    server.setFileSystemFactory( new VirtualFileSystemFactory( homeDir ) );
    server.setSubsystemFactories( Collections.<NamedFactory<Command>>singletonList( new SftpSubsystem.Factory() ) );
    server.setCommandFactory( new ScpCommandFactory() );
    server.setKeyPairProvider( new SimpleGeneratorHostKeyProvider( hostKeyPath ) );
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

  public void stop() throws InterruptedException {
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
