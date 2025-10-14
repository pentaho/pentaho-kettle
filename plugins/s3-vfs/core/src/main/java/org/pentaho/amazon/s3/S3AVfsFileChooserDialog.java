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
import org.pentaho.amazon.AmazonS3AFileSystemBootstrap;
import org.pentaho.s3a.vfs.S3AFileProvider;
import org.pentaho.vfs.ui.VfsFileChooserDialog;

/**
 * The UI for S3 VFS
 */
public class S3AVfsFileChooserDialog extends S3VfsFileChooserBaseDialog {

  public S3AVfsFileChooserDialog( VfsFileChooserDialog vfsFileChooserDialog, FileObject rootFile,
                                  FileObject initialFile ) {
    super( vfsFileChooserDialog, rootFile, initialFile, S3AFileProvider.SCHEME, AmazonS3AFileSystemBootstrap.getS3AFileSystemDisplayText() );
  }

  @Override
  public void activate() {
    vfsFileChooserDialog.openFileCombo.setText( "s3a://" );
    super.activate();
  }

  /**
   * Build a URL given Url and Port provided by the user.
   *
   * @return
   */
  @Override
  public String buildS3FileSystemUrlString() {
    return S3AFileProvider.SCHEME + "://";
  }
}
