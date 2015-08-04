/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2015 by Pentaho : http://www.pentaho.com
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

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.InetAddress;

import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs2.FileObject;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.vfs.KettleVFS;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.Session;

/**
 * @author Andrey Khayrutdinov
 */
public class SFTPClientTest {

  // @ClassRule
  public static TemporaryFolder folder;

  private static SftpServer server;

  @BeforeClass
  public static void startServer() throws Exception {
    KettleEnvironment.init();

    folder = new TemporaryFolder();
    folder.create();

    server = SftpServer.createDefaultServer( folder );
    server.start();
  }

  @AfterClass
  public static void stopServer() throws Exception {
    server.stop();
    server = null;

    folder.delete();
    folder = null;
  }


  private SFTPClient client;
  private Session session;
  private ChannelSftp channel;

  @Before
  public void setUp() throws Exception {
    session = server.createJschSession();
    session.connect();

    client = new SFTPClient( InetAddress.getByName( "localhost" ), server.getPort(), server.getUsername() );
    client.login( server.getPassword() );

    channel = (ChannelSftp) session.openChannel( "sftp" );
  }

  @After
  public void tearDown() throws Exception {
    if ( channel != null && channel.isConnected() ) {
      channel.disconnect();
    }
    channel = null;

    if ( session.isConnected() ) {
      session.disconnect();
    }
    session = null;
  }


  @Test
  public void putFile() throws Exception {
    final byte[] data = "putFile()".getBytes();

    client.put( new ByteArrayInputStream( data ), "uploaded.txt" );

    ByteArrayOutputStream uploaded = new ByteArrayOutputStream();
    channel.connect();
    InputStream inputStream = channel.get( "uploaded.txt" );
    try {
      IOUtils.copy( inputStream, uploaded );
    } finally {
      inputStream.close();
    }

    assertTrue(
      IOUtils.contentEquals( new ByteArrayInputStream( data ), new ByteArrayInputStream( uploaded.toByteArray() ) ) );
  }

  @Test
  public void getFile() throws Exception {
    final byte[] data = "getFile()".getBytes();

    channel.connect();
    channel.put( new ByteArrayInputStream( data ), "downloaded.txt" );

    client.get( KettleVFS.getFileObject( "ram://downloaded.txt" ), "downloaded.txt" );

    FileObject downloaded = KettleVFS.getFileObject( "ram://downloaded.txt" );
    assertTrue( downloaded.exists() );
    assertTrue( IOUtils.contentEquals( downloaded.getContent().getInputStream(), new ByteArrayInputStream( data ) ) );
  }

}
