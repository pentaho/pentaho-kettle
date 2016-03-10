/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2016 by Pentaho : http://www.pentaho.com
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
import com.jcraft.jsch.Session;
import org.junit.Test;

import java.net.InetAddress;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SFTPClientTest {

  /**
   * Given SFTP connection configuration.
   * <br/>
   * When SFTP Client is instantiated, then default preferred authentications list should be reordered,
   * particularly, GSS API Authentication should be the last one.
   */
  @Test
  public void shouldReorderDefaultPreferredAuthenticationsList() throws Exception {
    String serverIp = "serverIp";
    int serverPort = 1;
    String userName = "userName";
    Session session = mock( Session.class );
    InetAddress server = mock( InetAddress.class );
    final JSch jSch = mock( JSch.class );
    when( server.getHostAddress() ).thenReturn( serverIp );
    when( jSch.getSession( userName, serverIp, serverPort ) ).thenReturn( session );

    new SFTPClient( server, serverPort, userName ) {
      @Override
      JSch createJSch() {
        return jSch;
      }
    };

    verify( session )
      .setConfig( "PreferredAuthentications", "publickey,keyboard-interactive,password,gssapi-with-mic" );
  }
}
