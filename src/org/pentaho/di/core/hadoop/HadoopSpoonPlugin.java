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

package org.pentaho.di.core.hadoop;

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
import org.pentaho.di.ui.vfs.hadoopvfsfilechooserdialog.HadoopVfsFileChooserDialog;
import org.pentaho.hdfs.vfs.HDFSFileProvider;
import org.pentaho.vfs.ui.VfsFileChooserDialog;

@LifecyclePlugin(id = "HadoopSpoonPlugin", name = "Hadoop Spoon Plugin")
public class HadoopSpoonPlugin implements LifecycleListener, GUIOption {
  private static Class<?> PKG = HadoopSpoonPlugin.class;
  private LogChannelInterface log = new LogChannel(HadoopSpoonPlugin.class.getName());

  public static final String HDFS_SCHEME = "hdfs";
  public static final String HDFS_SCHEME_DISPLAY_NAME = "HDFS";

  public void onStart(LifeEventHandler arg0) throws LifecycleException {
    try {
      // Register HDFS as a file system type with VFS
      FileSystemManager fsm = KettleVFS.getInstance().getFileSystemManager();
      if (fsm instanceof DefaultFileSystemManager) {
        if (!Arrays.asList(fsm.getSchemes()).contains(HDFS_SCHEME)) {
          ((DefaultFileSystemManager) fsm).addProvider(HDFS_SCHEME, new HDFSFileProvider());
        }
      }
    } catch (FileSystemException e) {
      log.logError(BaseMessages.getString(PKG, "HadoopSpoonPlugin.StartupError.FailedToLoadHdfsDriver"));
    }

    VfsFileChooserDialog dialog = Spoon.getInstance().getVfsFileChooserDialog(null, null);
    dialog.addVFSUIPanel(new HadoopVfsFileChooserDialog(dialog, null, null));
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
