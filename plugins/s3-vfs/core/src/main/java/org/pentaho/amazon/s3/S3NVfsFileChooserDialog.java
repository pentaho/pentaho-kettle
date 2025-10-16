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


package org.pentaho.amazon.s3;

import org.apache.commons.vfs2.FileObject;
import org.pentaho.amazon.AmazonS3NFileSystemBootstrap;
import org.pentaho.s3n.vfs.S3NFileProvider;
import org.pentaho.vfs.ui.VfsFileChooserDialog;

/**
 * The UI for S3 VFS
 */
public class S3NVfsFileChooserDialog extends S3VfsFileChooserBaseDialog {

  public S3NVfsFileChooserDialog( VfsFileChooserDialog vfsFileChooserDialog, FileObject rootFile,
                                  FileObject initialFile ) {
    super( vfsFileChooserDialog, rootFile, initialFile, S3NFileProvider.SCHEME, AmazonS3NFileSystemBootstrap.getS3NFileSystemDisplayText() );
  }

  @Override
  public void activate() {
    vfsFileChooserDialog.openFileCombo.setText( "s3n://s3n/" );
    super.activate();
  }

  /**
   * Build a URL given Url and Port provided by the user.
   *
   * @return
   * @TODO: relocate to a s3 helper class or similar
   */
  public String buildS3FileSystemUrlString() {
    return S3NFileProvider.SCHEME + "://s3n/";
  }
}
