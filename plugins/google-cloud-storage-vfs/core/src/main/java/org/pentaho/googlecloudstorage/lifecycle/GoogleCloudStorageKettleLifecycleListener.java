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

import org.pentaho.di.core.annotations.LifecyclePlugin;
import org.pentaho.di.core.lifecycle.LifeEventHandler;
import org.pentaho.di.core.lifecycle.LifecycleException;
import org.pentaho.di.core.lifecycle.LifecycleListener;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.googlecloudstorage.ui.GoogleCloudStorageFileChooserDialog;
import org.pentaho.vfs.ui.VfsFileChooserDialog;

/**
 * The LifecycleListener for the Google Cloud Storage VFS plugin
 */
@LifecyclePlugin( id = "GoogleCloudStorageKettleLifecycleListener", name = "GoogleCloudStorageKettleLifecycleListener" )
public class GoogleCloudStorageKettleLifecycleListener implements LifecycleListener {

  public void onStart( LifeEventHandler handler ) throws LifecycleException {

    final Spoon spoon = Spoon.getInstance();
    spoon.getDisplay().asyncExec( new Runnable() {
      public void run() {
        VfsFileChooserDialog dialog = spoon.getVfsFileChooserDialog( null, null );
        GoogleCloudStorageFileChooserDialog
          googleCloudStorageVfsFileChooserDialog =
          new GoogleCloudStorageFileChooserDialog( dialog, null, null );
        dialog.addVFSUIPanel( googleCloudStorageVfsFileChooserDialog );
      }
    } );
  }

  public void onExit( LifeEventHandler handler ) throws LifecycleException {
  }
}
