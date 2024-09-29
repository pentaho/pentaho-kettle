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

import com.google.common.io.Files;
import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.ssl.SslConfigurationFactory;
import org.apache.ftpserver.usermanager.PropertiesUserManagerFactory;
import org.apache.ftpserver.usermanager.impl.BaseUser;
import org.apache.ftpserver.usermanager.impl.WritePermission;

import java.io.File;
import java.util.Collections;

/**
 * @author Andrey Khayrutdinov
 */
public class FtpsServer {

  public static final String SERVER_BASE_DIR = "src/it/resources/org/pentaho/di/job/entries/ftpput";
  public static final String SERVER_KEYSTORE = SERVER_BASE_DIR + "/ftpserver.jks";
  public static final String SERVER_KEYSTORE_PASSWORD = "password";
  public static final String USER_HOME_DIR = SERVER_BASE_DIR + "/dir";
  public static final String SAMPLE_FILE = "file.txt";

  public static final int DEFAULT_PORT = 8009;


  /**
   * Creates an FTP server on the given port with a user with the given name, password and home directory.
   *
   * @param username    the user's name
   * @param password    the password for the user
   * @param userHomeDir the user home directory
   * @param implicitSsl if it's to use SSL
   * @return an FTP server with the given configuration
   * @throws Exception
   */
  public static FtpsServer createFTPServer( int port, String username, String password, File userHomeDir,
                                            boolean implicitSsl ) throws Exception {
    if ( userHomeDir.exists() && userHomeDir.isDirectory() ) {
      Files.copy( new File( USER_HOME_DIR + "/" + SAMPLE_FILE ),
        new File( userHomeDir.getAbsolutePath() + "/" + SAMPLE_FILE ) );
    }

    return new FtpsServer( port, username, password, userHomeDir.getAbsolutePath(), implicitSsl );
  }

  private final FtpServer server;

  public FtpsServer( int port, String username, String password, String userHomeDir, boolean implicitSsl )
    throws Exception {
    this.server = createServer( port, username, password, userHomeDir, implicitSsl );
  }

  /*
   * Adopted from https://mina.apache.org/ftpserver-project/embedding_ftpserver.html
   */
  private FtpServer createServer( int port, String username, String password, String userHomeDir, boolean implicitSsl )
    throws Exception {
    ListenerFactory factory = new ListenerFactory();
    factory.setPort( port );

    if ( implicitSsl ) {
      SslConfigurationFactory ssl = new SslConfigurationFactory();
      ssl.setKeystoreFile( new File( SERVER_KEYSTORE ) );
      ssl.setKeystorePassword( SERVER_KEYSTORE_PASSWORD );
      // set the SSL configuration for the listener
      factory.setSslConfiguration( ssl.createSslConfiguration() );
      factory.setImplicitSsl( true );
    }

    FtpServerFactory serverFactory = new FtpServerFactory();
    // replace the default listener
    serverFactory.addListener( "default", factory.createListener() );

    PropertiesUserManagerFactory userManagerFactory = new PropertiesUserManagerFactory();

    // As this is to be used in testing and the user file is generated/updated... there's no need to reuse it.
    // Let's use a temp file
    File usersFile = File.createTempFile( getClass().getName() + "Users", ".properties" );
    // And set it to be deleted on exit...
    usersFile.deleteOnExit();

    userManagerFactory.setFile( usersFile );
    UserManager userManager = userManagerFactory.createUserManager();
    if ( !userManager.doesExist( username ) ) {
      BaseUser user = new BaseUser();
      user.setName( username );
      user.setPassword( password );
      user.setEnabled( true );
      user.setHomeDirectory( userHomeDir );
      user.setAuthorities( Collections.singletonList( new WritePermission() ) );
      userManager.save( user );
    }

    serverFactory.setUserManager( userManager );
    return serverFactory.createServer();
  }

  public void start() throws Exception {
    server.start();
  }

  public void stop() {
    if ( server != null ) {
      server.stop();
    }
  }
}
