/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/



package org.pentaho.amazon.s3.fileoutput;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.vfs.ui.CustomVfsUiPanel;
import org.pentaho.vfs.ui.VfsFileChooserDialog;
import org.pentaho.di.core.Props;

/**
 * The UI for S3 VFS
 */
public abstract class S3VfsFileChooserBaseDialog extends CustomVfsUiPanel {

  protected FileObject rootFile;
  protected FileObject initialFile;
  protected VfsFileChooserDialog vfsFileChooserDialog;

  protected S3VfsFileChooserBaseDialog( VfsFileChooserDialog vfsFileChooserDialog, FileObject rootFile,
                                  FileObject initialFile, String schema, String fileSystemDisplayText ) {
    super( schema, fileSystemDisplayText, vfsFileChooserDialog, SWT.NONE );

    this.vfsFileChooserDialog = vfsFileChooserDialog;
    this.rootFile = rootFile;
    this.initialFile = initialFile;

    setLayout( new GridLayout() );
  }

  /**
   * Build a URL given Url and Port provided by the user.
   */
  public String buildS3FileSystemUrlString() {
    return "s3a://";
  }

  @Override
  public void activate() {
    vfsFileChooserDialog.setRootFile( rootFile );
    vfsFileChooserDialog.setInitialFile( initialFile );
    vfsFileChooserDialog.vfsBrowser.fileSystemTree.removeAll();
    super.activate();

    try {
      String filename = Props.getInstance().getCustomParameter( "S3VfsFileChooserDialog.Filename", buildS3FileSystemUrlString() );
      FileObject newRoot = resolveFile( filename );
      vfsFileChooserDialog.vfsBrowser.resetVfsRoot( newRoot );
    } catch ( FileSystemException ignored ) {
      //ignored
    }
  }

  /** Credentials come from the system properties set by {@link S3FileOutputDialog}, which the driver reads. */
  @Override
  public FileObject resolveFile( String fileUri ) throws FileSystemException {
    return resolveFile( fileUri, new FileSystemOptions() );
  }

  @Override
  public FileObject resolveFile( String fileUri, FileSystemOptions opts ) throws FileSystemException {
    Spoon spoon = Spoon.getInstance();
    try {
      return KettleVFS.getInstance( spoon.getExecutionBowl() ).getFileObject( fileUri, getVariableSpace(), opts );
    } catch ( KettleFileException e ) {
      throw new FileSystemException( e );
    }
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
