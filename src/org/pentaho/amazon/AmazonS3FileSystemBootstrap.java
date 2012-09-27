/*******************************************************************************
 *
 * Pentaho Big Data
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
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

package org.pentaho.amazon;

import java.util.Arrays;

import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.impl.DefaultFileSystemManager;
import org.pentaho.di.core.annotations.KettleLifecyclePlugin;
import org.pentaho.di.core.lifecycle.KettleLifecycleListener;
import org.pentaho.di.core.lifecycle.LifecycleException;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.s3.vfs.S3FileProvider;

/**
 * Registers the Amazon S3 VFS File Provider dynamically since it is bundled with
 * our plugin and will not automatically be registered through the normal
 * class path search the default FileSystemManager performs.
 */
@KettleLifecyclePlugin(id = "AmazonS3FileSystemBootstrap", name = "Amazon S3 FileSystem Bootstrap")
public class AmazonS3FileSystemBootstrap implements KettleLifecycleListener {

  private static Class<?> PKG = AmazonS3FileSystemBootstrap.class;
  private LogChannelInterface log = new LogChannel(AmazonS3FileSystemBootstrap.class.getName());
  
  /**
   * @return the i18n display text for the S3 file system
   */
  public static String getS3FileSystemDisplayText() {
    return BaseMessages.getString(PKG, "S3VfsFileChooserDialog.FileSystemChoice.S3.Label");
  }
  
  @Override
  public void onEnvironmentInit() throws LifecycleException {
    try {
      // Register S3 as a file system type with VFS
      FileSystemManager fsm = KettleVFS.getInstance().getFileSystemManager();
      if (fsm instanceof DefaultFileSystemManager) {
        if (!Arrays.asList(fsm.getSchemes()).contains(S3FileProvider.SCHEME)) {
          ((DefaultFileSystemManager) fsm).addProvider(S3FileProvider.SCHEME, new S3FileProvider());
        }
      }
    } catch (FileSystemException e) {
      log.logError(BaseMessages.getString(PKG, "AmazonSpoonPlugin.StartupError.FailedToLoadS3Driver"));
    }
  }

  @Override
  public void onEnvironmentShutdown() {
    // noop
  }
}
