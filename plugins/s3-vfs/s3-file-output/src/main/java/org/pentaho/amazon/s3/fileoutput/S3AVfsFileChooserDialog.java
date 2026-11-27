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
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.s3.vfs.S3FileProvider;
import org.pentaho.vfs.ui.VfsFileChooserDialog;

/**
 * The UI for S3 VFS
 */
public class S3AVfsFileChooserDialog extends S3VfsFileChooserBaseDialog {

  private static final Class<?> PKG = S3AVfsFileChooserDialog.class;

  public S3AVfsFileChooserDialog( VfsFileChooserDialog vfsFileChooserDialog, FileObject rootFile,
                                  FileObject initialFile ) {
    super( vfsFileChooserDialog, rootFile, initialFile, S3FileProvider.S3A_SCHEME,
      BaseMessages.getString( PKG, "S3AVfsFileChooserDialog.FileSystemChoice.S3.Label" ) );
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
    return "s3a://";
  }
}
