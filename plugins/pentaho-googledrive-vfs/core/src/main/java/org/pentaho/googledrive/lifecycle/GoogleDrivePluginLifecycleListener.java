/*!
* Copyright 2010 - 2017 Pentaho Corporation.  All rights reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.pentaho.googledrive.lifecycle;

import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;

import org.pentaho.capabilities.impl.DefaultCapabilityManager;
import org.pentaho.di.core.annotations.KettleLifecyclePlugin;
import org.pentaho.di.core.lifecycle.KettleLifecycleListener;
import org.pentaho.di.core.lifecycle.LifecycleException;
import org.pentaho.di.core.vfs.KettleVFS;

import org.pentaho.googledrive.vfs.GoogleDriveFileObject;
import org.pentaho.googledrive.vfs.GoogleDriveFileProvider;

import java.io.File;
import java.util.Arrays;

@KettleLifecyclePlugin( id = "GoogleDrivePluginLifecycleListener", name = "GoogleDrivePluginLifecycleListener" )
public class GoogleDrivePluginLifecycleListener implements KettleLifecycleListener {

  public void onEnvironmentInit() throws LifecycleException {
    try {
      if ( !new File( GoogleDriveFileObject.resolveCredentialsPath() + "/client_secret.json" ).exists() ) {
        throw new LifecycleException(
            "The Google Authorization secrets security token file (client_secret.json) is not present in the credentials folder.",
            false );
      }

      if ( !new File( GoogleDriveFileObject.resolveCredentialsPath() + "/StoredCredential" ).exists() ) {
        DefaultCapabilityManager capabilityManager = DefaultCapabilityManager.getInstance();
        if ( capabilityManager.getCapabilityById( "pentaho-server" ) != null ) {
          throw new LifecycleException(
              "The Google Authorization Code Flow security token file (StoredCredential) is not present in the credentials folder.",
              false );
        }
      }

      /**
       * Registers the GoogleDrive VFS File Provider dynamically since it is bundled with our plugin and will not automatically
       * be registered through the normal class path search the default FileSystemManager performs.
       */
      FileSystemManager fsm = KettleVFS.getInstance().getFileSystemManager();
      if ( fsm instanceof DefaultFileSystemManager ) {
        if ( !Arrays.asList( fsm.getSchemes() ).contains( GoogleDriveFileProvider.SCHEME ) ) {
          ( (DefaultFileSystemManager) fsm )
              .addProvider( GoogleDriveFileProvider.SCHEME, new GoogleDriveFileProvider() );
        }
      }

    } catch ( FileSystemException e ) {
      System.out.println( "Failed to load Googledrive driver" );
    }
  }

  public void onEnvironmentShutdown() {
    System.out.println( "GoogleDrivePluginLifecycleListener: Kettle Environment shutting down!" );
  }
}
