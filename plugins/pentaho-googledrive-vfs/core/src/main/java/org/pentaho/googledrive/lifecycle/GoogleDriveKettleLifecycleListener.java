/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


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

