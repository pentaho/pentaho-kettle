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

package org.pentaho.googledrive.vfs.ui;

import org.apache.commons.vfs2.*;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.googledrive.vfs.GoogleDriveFileProvider;
import org.pentaho.vfs.ui.CustomVfsUiPanel;
import org.pentaho.vfs.ui.VfsFileChooserDialog;

public class GoogleDriveFileChooserDialog extends CustomVfsUiPanel {

  public GoogleDriveFileChooserDialog( String vfsScheme, String vfsSchemeDisplayText,
      VfsFileChooserDialog vfsFileChooserDialog, int flags ) {
    super( vfsScheme, vfsSchemeDisplayText, vfsFileChooserDialog, flags );
  }

  public void activate() {
    try {
      getVfsFileChooserDialog().setRootFile( null );
      getVfsFileChooserDialog().setInitialFile( null );
      getVfsFileChooserDialog().openFileCombo.setText( GoogleDriveFileProvider.SCHEME + "://" );
      getVfsFileChooserDialog().vfsBrowser.fileSystemTree.removeAll();
      super.activate();

      FileObject newRoot = resolveFile( getVfsFileChooserDialog().openFileCombo.getText() );
      getVfsFileChooserDialog().vfsBrowser.resetVfsRoot( newRoot );
    } catch ( FileSystemException e ) {
      System.out.println( e );
    }
  }

  public FileObject resolveFile( String fileUri ) throws FileSystemException {
    try {
      return KettleVFS.getFileObject( fileUri, getVariableSpace(), getFileSystemOptions() );
    } catch ( KettleFileException e ) {
      throw new FileSystemException( e );
    }
  }

  protected FileSystemOptions getFileSystemOptions() throws FileSystemException {
    FileSystemOptions opts = new FileSystemOptions();
    return opts;
  }

  private VariableSpace getVariableSpace() {
    if ( Spoon.getInstance().getActiveTransformation() != null ) {
      return Spoon.getInstance().getActiveTransformation();
    } else if ( Spoon.getInstance().getActiveJob() != null ) {
      return Spoon.getInstance().getActiveJob();
    } else {
      return new Variables();
    }
  }
}
