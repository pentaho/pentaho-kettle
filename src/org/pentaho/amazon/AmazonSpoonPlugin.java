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
import org.pentaho.di.core.annotations.LifecyclePlugin;
import org.pentaho.di.core.gui.GUIOption;
import org.pentaho.di.core.lifecycle.LifeEventHandler;
import org.pentaho.di.core.lifecycle.LifecycleException;
import org.pentaho.di.core.lifecycle.LifecycleListener;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.s3.vfs.S3FileProvider;
import org.pentaho.vfs.ui.VfsFileChooserDialog;

import org.pentaho.amazon.s3.S3VfsFileChooserDialog;

@LifecyclePlugin(id = "AmazonSpoonPlugin", name = "Amazon Spoon Plugin EE")
public class AmazonSpoonPlugin implements LifecycleListener, GUIOption {

  private static Class<?> PKG = AmazonSpoonPlugin.class;
  private LogChannelInterface log = new LogChannel(AmazonSpoonPlugin.class.getName());

  public static final String S3_SCHEME = "s3";
  public static final String S3_SCHEME_DISPLAY_TEXT = "S3";

  public AmazonSpoonPlugin() {
  }

  public void onStart(LifeEventHandler arg0) throws LifecycleException {
    try {
      // Register S3 as a file system type with VFS
      FileSystemManager fsm = KettleVFS.getInstance().getFileSystemManager();
      if (fsm instanceof DefaultFileSystemManager) {
        if (!Arrays.asList(fsm.getSchemes()).contains(S3_SCHEME)) {
          ((DefaultFileSystemManager) fsm).addProvider(S3_SCHEME, new S3FileProvider());
        }
      }
    } catch (FileSystemException e) {
      log.logError(BaseMessages.getString(PKG, "AmazonSpoonPlugin.StartupError.FailedToLoadS3Driver"));
    }

    VfsFileChooserDialog dialog = Spoon.getInstance().getVfsFileChooserDialog(null, null);
    Spoon.getInstance().getVfsFileChooserDialog(null, null).addVFSUIPanel(new S3VfsFileChooserDialog(dialog, null, null));
  }

  public void onExit(LifeEventHandler arg0) throws LifecycleException {
  }

  public String getLabelText() {
    return null;
  }

  public Object getLastValue() {
    return null;
  }

  public DisplayType getType() {
    return null;
  }

  public void setValue(Object value) {
  }
}
