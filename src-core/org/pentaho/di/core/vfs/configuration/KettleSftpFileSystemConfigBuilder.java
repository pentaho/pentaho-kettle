/*
 * Copyright (c) 2009 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
*/
package org.pentaho.di.core.vfs.configuration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemOptions;
import org.apache.commons.vfs.provider.FileNameParser;
import org.apache.commons.vfs.provider.URLFileName;
import org.apache.commons.vfs.provider.sftp.SftpFileNameParser;
import org.apache.commons.vfs.provider.sftp.SftpFileSystem;
import org.jfree.util.Log;

import com.jcraft.jsch.UserInfo;

/**
 * An SFTP FileSystemConfigBuilder that uses Kettle variables to build SFTP VFS configuration options.
 * Options can be specified by host by appending the host name (as it will appear in the VFS URL) to the
 * end of the parameter. (e.g.- vfs.sftp.parameter.192.168.1.5)
 * 
 * Overriden parameters are currently:
 * <table style="text-align: left;" border="1">
 *  <tr><th>Parameter</th><th>Description</th></tr>
 *  <tr><td>AuthKeyPassphrase</td><td>The passphrase that unlocks the private key. (Recommended on a per host basis, unless the passphrase is the same for ALL authentication keys)</td></tr>
 *  <tr><td>identity</td><td>Local file path (Not VFS) to the private key for authentication.</td></tr>
 * </table>
 * 
 * @author cboyden
 */
public class KettleSftpFileSystemConfigBuilder extends KettleGenericFileSystemConfigBuilder {

  private final static KettleSftpFileSystemConfigBuilder builder = new KettleSftpFileSystemConfigBuilder();
  
  public static KettleSftpFileSystemConfigBuilder getInstance() {
    return builder;
  }

  protected KettleSftpFileSystemConfigBuilder() {
    super();
  }

  @Override
  protected Class<?> getConfigClass() {
    // Return the VFS driver class that will recognize the parameters processed by this component
    return SftpFileSystem.class;
  }
  
  /**
   * Publicly expose a generic way to set parameters
   */
  @Override
  public void setParameter(FileSystemOptions opts, String name, String value, String fullParameterName, String vfsUrl) throws IOException {
    if(!fullParameterName.startsWith("vfs.sftp")) { //$NON-NLS-1$
      // This is not an SFTP parameter. Delegate to the generic handler
      super.setParameter(opts, name, value, fullParameterName, vfsUrl);
    } else {
      // Check for the presence of a host in the full variable name
      try { 
        // Parse server name from vfsFilename
        FileNameParser sftpFilenameParser = SftpFileNameParser.getInstance();
        URLFileName file = (URLFileName)sftpFilenameParser.parseUri(null, null, vfsUrl);
  
        if(!parameterContainsHost(fullParameterName) || fullParameterName.endsWith(file.getHostName())) {
          // Match special cases for parameter names
          if(name.equalsIgnoreCase("AuthKeyPassphrase")) { //$NON-NLS-1$
            setParam(opts, UserInfo.class.getName(), new PentahoUserInfo((String)value));
          } else if (name.equals("identity")) { //$NON-NLS-1$
            File[] identities = (File[])this.getParam(opts, "identities"); //$NON-NLS-1$
            
            if(identities == null) {
              identities = new File[] {new File((String)value)};
            } else {
              // Copy, in a Java 5 friendly manner, identities into a larger array 
              File[] temp = new File[identities.length + 1];
              System.arraycopy(identities, 0, temp, 0, identities.length);
              identities = temp;
              
              identities[identities.length - 1] = new File((String)value);
            }
            setParam(opts, "identities", identities); //$NON-NLS-1$
          } else {
            setParam(opts, name, value);
          }
        } else {
          // No host match found
          Log.debug("No host match found for: " + fullParameterName); //$NON-NLS-1$
        }
      } catch (FileSystemException e) {
        Log.error("Failed to set VFS parameter: [" + fullParameterName + "] " + value, e); //$NON-NLS-1$ //$NON-NLS-2$
      }
    }
  }
  
  private static boolean parameterContainsHost(String parameter) {
    // Test the number of '.' in the file. If there are more then two, then there is a host associated
    return parameter.matches("^(.*\\..*){3,}") ? true : false; //$NON-NLS-1$
  }
  
  private static class PentahoUserInfo implements UserInfo{
    private String passphrase;
    private String password;
    
    public PentahoUserInfo(String passphrase) {
      this.passphrase = passphrase;
    }
    
    public String getPassphrase() {
      return passphrase; // Passphrase for the authentication key
    }

    public String getPassword() {
      return password; // Appears to be unused in this usage
    }

    public boolean promptPassphrase(String arg0) {
        return true;
    }

    public boolean promptPassword(String arg0) {
      return false;  
    }

    public boolean promptYesNo(String arg0) {
      return false;
    }

    public void showMessage(String arg0) {
    }
  };
}
