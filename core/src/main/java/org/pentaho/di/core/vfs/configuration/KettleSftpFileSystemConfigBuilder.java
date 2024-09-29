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

package org.pentaho.di.core.vfs.configuration;

import java.io.File;
import java.io.IOException;

import org.apache.commons.vfs2.FileSystem;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.provider.FileNameParser;
import org.apache.commons.vfs2.provider.URLFileName;
import org.apache.commons.vfs2.provider.sftp.IdentityInfo;
import org.apache.commons.vfs2.provider.sftp.SftpFileNameParser;
import org.apache.commons.vfs2.provider.sftp.SftpFileSystem;
import org.apache.commons.vfs2.provider.sftp.SftpFileSystemConfigBuilder;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;

import com.jcraft.jsch.UserInfo;

/**
 * An SFTP FileSystemConfigBuilder that uses Kettle variables to build SFTP VFS configuration options. Options can be
 * specified by host by appending the host name (as it will appear in the VFS URL) to the end of the parameter. (e.g.-
 * vfs.sftp.parameter.192.168.1.5)
 *
 * Overriden parameters are currently:
 * <table style="text-align: left;" border="1">
 * <tr>
 * <th>Parameter</th>
 * <th>Description</th>
 * </tr>
 * <tr>
 * <td>AuthKeyPassphrase</td>
 * <td>The passphrase that unlocks the private key. (Recommended on a per host basis, unless the passphrase is the same
 * for ALL authentication keys)</td>
 * </tr>
 * <tr>
 * <td>identity</td>
 * <td>Local file path (Not VFS) to the private key for authentication.</td>
 * </tr>
 * </table>
 *
 * @author cboyden
 */
public class KettleSftpFileSystemConfigBuilder extends KettleGenericFileSystemConfigBuilder {

  private static final KettleSftpFileSystemConfigBuilder builder = new KettleSftpFileSystemConfigBuilder();
  private static final LogChannelInterface log = new LogChannel( "cfgbuilder" );
  private static final String IDENTITY_KEY = SftpFileSystemConfigBuilder.class.getName() + ".IDENTITIES";

  public static KettleSftpFileSystemConfigBuilder getInstance() {
    return builder;
  }

  protected KettleSftpFileSystemConfigBuilder() {
    super();
  }

  @Override
  protected Class<? extends FileSystem> getConfigClass() {
    // Return the VFS driver class that will recognize the parameters processed by this component
    return SftpFileSystem.class;
  }

  /**
   * Publicly expose a generic way to set parameters
   */
  @Override
  public void setParameter( FileSystemOptions opts, String name, String value, String fullParameterName,
    String vfsUrl ) throws IOException {
    if ( !fullParameterName.startsWith( "vfs.sftp" ) ) {
      // This is not an SFTP parameter. Delegate to the generic handler
      super.setParameter( opts, name, value, fullParameterName, vfsUrl );
    } else {
      // Check for the presence of a host in the full variable name
      try {
        // Parse server name from vfsFilename
        FileNameParser sftpFilenameParser = SftpFileNameParser.getInstance();
        URLFileName file = (URLFileName) sftpFilenameParser.parseUri( null, null, vfsUrl );

        if ( !parameterContainsHost( fullParameterName ) || fullParameterName.endsWith( file.getHostName() ) ) {
          // Match special cases for parameter names
          if ( name.equalsIgnoreCase( "AuthKeyPassphrase" ) ) {
            setParam( opts, UserInfo.class.getName(), new PentahoUserInfo( value ) );
          } else if ( name.equals( "identity" ) ) {

            IdentityInfo[] identities = (IdentityInfo[]) this.getParam( opts, IDENTITY_KEY );

            if ( identities == null ) {
              identities = new IdentityInfo[] { new IdentityInfo( new File( value ) ) };
            } else {
              // Copy, in a Java 5 friendly manner, identities into a larger array
              IdentityInfo[] temp = new IdentityInfo[identities.length + 1];
              System.arraycopy( identities, 0, temp, 0, identities.length );
              identities = temp;

              identities[identities.length - 1] = new IdentityInfo( new File( value ) );
            }
            setParam( opts, IDENTITY_KEY, identities );
          } else {
            super.setParameter( opts, name, value, fullParameterName, vfsUrl );
          }
        } else {
          // No host match found
          log.logDebug( "No host match found for: " + fullParameterName );
        }
      } catch ( IOException e ) {
        log.logError( "Failed to set VFS parameter: [" + fullParameterName + "] " + value, e );
      }
    }
  }

  private static boolean parameterContainsHost( String parameter ) {
    // Test the number of '.' in the file. If there are more then two, then there is a host associated
    return parameter.matches( "^(.*\\..*){3,}" ) ? true : false;
  }

  private static class PentahoUserInfo implements UserInfo {
    private String passphrase;
    private String password;

    public PentahoUserInfo( String passphrase ) {
      this.passphrase = passphrase;
    }

    @Override
    public String getPassphrase() {
      return passphrase; // Passphrase for the authentication key
    }

    @Override
    public String getPassword() {
      return password; // Appears to be unused in this usage
    }

    @Override
    public boolean promptPassphrase( String arg0 ) {
      return true;
    }

    @Override
    public boolean promptPassword( String arg0 ) {
      return false;
    }

    @Override
    public boolean promptYesNo( String arg0 ) {
      return false;
    }

    @Override
    public void showMessage( String arg0 ) {
    }
  }
}
