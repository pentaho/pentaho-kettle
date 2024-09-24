/*!
* Copyright (C) 2017 by Hitachi Vantara : http://www.pentaho.com
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

import org.eclipse.swt.SWT;
import org.pentaho.di.core.annotations.LifecyclePlugin;
import org.pentaho.di.core.lifecycle.LifeEventHandler;
import org.pentaho.di.core.lifecycle.LifecycleException;
import org.pentaho.di.core.lifecycle.LifecycleListener;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.googledrive.vfs.GoogleDriveFileObject;
import org.pentaho.googledrive.vfs.GoogleDriveFileProvider;
import org.pentaho.googledrive.vfs.ui.GoogleDriveFileChooserDialog;
import org.pentaho.vfs.ui.VfsFileChooserDialog;

import java.io.File;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

@LifecyclePlugin( id = "GoogleDriveKettleLifecycleListener", name = "GoogleDriveKettleLifecycleListener" )
public class GoogleDriveKettleLifecycleListener implements LifecycleListener {

  private ResourceBundle resourceBundle = PropertyResourceBundle.getBundle( "plugin" );

  public void onStart( LifeEventHandler handler ) throws LifecycleException {

    if ( new File( GoogleDriveFileObject.resolveCredentialsPath() + "/" + resourceBundle.getString( "client.secrets" ) )
        .exists() ) {
      /*
      * Registers the UI for the VFS Browser
      * */
      final Spoon spoon = Spoon.getInstance();
      spoon.getDisplay().asyncExec( new Runnable() {
        public void run() {
          VfsFileChooserDialog dialog = spoon.getVfsFileChooserDialog( null, null );
          GoogleDriveFileChooserDialog
              hadoopVfsFileChooserDialog =
              new GoogleDriveFileChooserDialog( GoogleDriveFileProvider.SCHEME, GoogleDriveFileProvider.DISPLAY_NAME,
                  dialog, SWT.NONE );
          dialog.addVFSUIPanel( hadoopVfsFileChooserDialog );
        }
      } );
    }
  }

  public void onExit( LifeEventHandler handler ) throws LifecycleException {
  }
}

