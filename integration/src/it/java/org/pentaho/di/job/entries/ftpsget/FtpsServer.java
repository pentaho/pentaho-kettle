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

package org.pentaho.di.job.entries.ftpsget;

import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.Authority;
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

  public static final String SERVER_BASE_DIR = "testfiles/org/pentaho/di/job/entries/ftpsget";
  public static final String SERVER_KEYSTORE = SERVER_BASE_DIR + "/ftpserver.jks";
  public static final String SERVER_USERS = SERVER_BASE_DIR + "/users.properties";
  public static final String USER_HOME_DIR = SERVER_BASE_DIR + "/dir";
  public static final String SAMPLE_FILE = "file.txt";

  public static final String ADMIN = "admin";
  public static final String PASSWORD = "password";
  public static final int DEFAULT_PORT = 8009;


  public static FtpsServer createDefaultServer() throws Exception {
    return new FtpsServer( DEFAULT_PORT, ADMIN, PASSWORD, true );
  }

  public static FtpsServer createFtpServer() throws Exception {
    return new FtpsServer( DEFAULT_PORT, ADMIN, PASSWORD, false );
  }

  private final FtpServer server;

  public FtpsServer( int port, String username, String password, boolean implicitSsl ) throws Exception {
    this.server = createServer( port, username, password, implicitSsl );
  }

  /*
   * Adopted from https://mina.apache.org/ftpserver-project/embedding_ftpserver.html
   */
  private FtpServer createServer( int port, String username, String password, boolean implicitSsl ) throws Exception {
    ListenerFactory factory = new ListenerFactory();
    factory.setPort( port );

    if ( implicitSsl ) {
      SslConfigurationFactory ssl = new SslConfigurationFactory();
      ssl.setKeystoreFile( new File( SERVER_KEYSTORE ) );
      ssl.setKeystorePassword( PASSWORD );
      // set the SSL configuration for the listener
      factory.setSslConfiguration( ssl.createSslConfiguration() );
      factory.setImplicitSsl( true );
    }

    FtpServerFactory serverFactory = new FtpServerFactory();
    // replace the default listener
    serverFactory.addListener( "default", factory.createListener() );

    PropertiesUserManagerFactory userManagerFactory = new PropertiesUserManagerFactory();

    userManagerFactory.setFile( new File( SERVER_USERS ) );
    UserManager userManager = userManagerFactory.createUserManager();
    if ( !userManager.doesExist( username ) ) {
      BaseUser user = new BaseUser();
      user.setName( username );
      user.setPassword( password );
      user.setEnabled( true );
      user.setHomeDirectory( USER_HOME_DIR );
      user.setAuthorities( Collections.<Authority>singletonList( new WritePermission() ) );
      userManager.save( user );
    }

    serverFactory.setUserManager( userManager );
    return serverFactory.createServer();
  }

  public void start() throws Exception {
    server.start();
  }

  public void stop() throws Exception {
    if ( server != null ) {
      server.stop();
    }
  }
}
