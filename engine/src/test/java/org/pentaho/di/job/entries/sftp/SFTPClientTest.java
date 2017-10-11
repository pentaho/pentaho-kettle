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

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.InetAddress;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SFTPClientTest {

  private int serverPort;
  private String userName;
  private Session session;
  private InetAddress server;
  private JSch jSch;

  @Before
  public void setUp() throws JSchException {
    System.clearProperty( SFTPClient.ENV_PARAM_USERAUTH_GSSAPI );

    String serverIp = "serverIp";
    serverPort = 1;
    userName = "userName";
    session = mock( Session.class );
    server = mock( InetAddress.class );
    when( server.getHostAddress() ).thenReturn( serverIp );
    jSch = mock( JSch.class );
    when( jSch.getSession( userName, serverIp, serverPort ) ).thenReturn( session );
  }

  @After
  public void tearDown() {
    System.clearProperty( SFTPClient.ENV_PARAM_USERAUTH_GSSAPI );
  }

  /**
   * Given SFTP connection configuration, and -Duserauth.gssapi.enabled param was NOT passed on application start.
   * <br/>
   * When SFTP Client is instantiated, then preferred authentications list should not contain
   * GSS API Authentication.
   */
  @Test
  public void shouldExcludeGssapiFromPreferredAuthenticationsByDefault() throws Exception {
    new SFTPClient( server, serverPort, userName ) {
      @Override
      JSch createJSch() {
        return jSch;
      }
    };

    verify( session )
      .setConfig( "PreferredAuthentications", "publickey,keyboard-interactive,password" );
  }

  /**
   * Given SFTP connection configuration, and -Duserauth.gssapi.enabled param
   * was passed on application start with correct value.
   * <br/>
   * When SFTP Client is instantiated, then preferred authentications list should contain
   * GSS API Authentication as the last one.
   */
  @Test
  public void shouldIncludeGssapiToPreferredAuthenticationsIfSpecified() throws Exception {
    System.setProperty( SFTPClient.ENV_PARAM_USERAUTH_GSSAPI, "true" );

    new SFTPClient( server, serverPort, userName ) {
      @Override
      JSch createJSch() {
        return jSch;
      }
    };

    verify( session )
      .setConfig( "PreferredAuthentications", "publickey,keyboard-interactive,password,gssapi-with-mic" );
  }

  /**
   * Given SFTP connection configuration, and -Duserauth.gssapi.enabled param
   * was passed on application start with incorrect value.
   * <br/>
   * When SFTP Client is instantiated, then preferred authentications list should not contain
   * GSS API Authentication.
   */
  @Test
  public void shouldIncludeGssapiToPreferredAuthenticationsIfOnlySpecifiedCorrectly() throws Exception {
    System.setProperty( SFTPClient.ENV_PARAM_USERAUTH_GSSAPI, "yes" );

    new SFTPClient( server, serverPort, userName ) {
      @Override
      JSch createJSch() {
        return jSch;
      }
    };

    verify( session )
      .setConfig( "PreferredAuthentications", "publickey,keyboard-interactive,password" );
  }
}
