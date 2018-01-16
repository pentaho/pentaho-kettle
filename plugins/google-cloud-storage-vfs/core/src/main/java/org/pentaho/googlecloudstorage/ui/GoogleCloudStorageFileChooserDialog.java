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

package org.pentaho.googlecloudstorage.ui;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.googlecloudstorage.vfs.GoogleCloudStorageFileProvider;
import org.pentaho.vfs.ui.CustomVfsUiPanel;
import org.pentaho.vfs.ui.VfsFileChooserDialog;

/**
 * The UI for Google Cloud Storage VFS
 */
public class GoogleCloudStorageFileChooserDialog extends CustomVfsUiPanel {

  private static Class<?> PKG = GoogleCloudStorageFileChooserDialog.class;
  private LogChannel log = new LogChannel( this );

  private FileObject rootFile;
  private FileObject initialFile;
  private VfsFileChooserDialog vfsFileChooserDialog;

  public GoogleCloudStorageFileChooserDialog( VfsFileChooserDialog vfsFileChooserDialog, FileObject rootFile,
                                              FileObject initialFile ) {
    super( GoogleCloudStorageFileProvider.SCHEME, GoogleCloudStorageFileProvider.SCHEME_NAME, vfsFileChooserDialog,
      SWT.NONE );

    this.vfsFileChooserDialog = vfsFileChooserDialog;
    this.rootFile = rootFile;
    this.initialFile = initialFile;

    setLayout( new GridLayout() );
  }

  public void activate() {
    vfsFileChooserDialog.setRootFile( rootFile );
    vfsFileChooserDialog.setInitialFile( initialFile );
    vfsFileChooserDialog.openFileCombo.setText( "gs://" );
    vfsFileChooserDialog.vfsBrowser.fileSystemTree.removeAll();
    super.activate();

    try {
      FileObject newRoot = resolveFile( vfsFileChooserDialog.openFileCombo.getText() );
      vfsFileChooserDialog.vfsBrowser.resetVfsRoot( newRoot );
    } catch ( FileSystemException ignored ) {
      //ignored
    }
  }

  public FileObject resolveFile( String fileUri ) throws FileSystemException {
    try {
      return KettleVFS.getFileObject( fileUri, getVariableSpace(), getFileSystemOptions() );
    } catch ( KettleFileException e ) {
      throw new FileSystemException( e );
    }
  }

  public FileObject resolveFile( String fileUri, FileSystemOptions opts ) throws FileSystemException {
    try {
      return KettleVFS.getFileObject( fileUri, getVariableSpace(), opts );
    } catch ( KettleFileException e ) {
      throw new FileSystemException( e );
    }
  }

  private FileSystemOptions getFileSystemOptions() throws FileSystemException {
    return new FileSystemOptions();
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
