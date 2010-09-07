/*
 * Copyright 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the Mozilla Public License, Version 1.1, or any later version. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.mozilla.org/MPL/MPL-1.1.txt. The Original Code is the Pentaho 
 * BI Platform.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
 *
 * @author Michael D'Amour
 */

package org.pentaho.di.core;

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
