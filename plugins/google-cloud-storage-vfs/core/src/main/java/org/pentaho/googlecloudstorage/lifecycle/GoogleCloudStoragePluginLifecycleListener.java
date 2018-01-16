/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2018 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.googlecloudstorage.lifecycle;

import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.pentaho.di.core.annotations.KettleLifecyclePlugin;
import org.pentaho.di.core.lifecycle.KettleLifecycleListener;
import org.pentaho.di.core.lifecycle.LifecycleException;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.googlecloudstorage.vfs.GoogleCloudStorageFileProvider;

import java.util.Arrays;

/**
 * The KettleLifecycleListener for the Google Cloud Storage VFS plugin
 */
@KettleLifecyclePlugin( id = "GoogleCloudStoragePluginLifecycleListener", name =
  "GoogleCloudStoragePluginLifecycleListener" )
public class GoogleCloudStoragePluginLifecycleListener implements KettleLifecycleListener {

  public void onEnvironmentInit() throws LifecycleException {
    try {
      FileSystemManager fsm = KettleVFS.getInstance().getFileSystemManager();
      if ( fsm instanceof DefaultFileSystemManager ) {
        if ( !Arrays.asList( fsm.getSchemes() ).contains( GoogleCloudStorageFileProvider.SCHEME ) ) {
          ( (DefaultFileSystemManager) fsm )
            .addProvider( GoogleCloudStorageFileProvider.SCHEME, new GoogleCloudStorageFileProvider() );
        }
      }
    } catch ( FileSystemException e ) {
      throw new LifecycleException( e.getMessage(), false );
    }
  }

  public void onEnvironmentShutdown() {
  }
}
