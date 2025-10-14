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

import org.apache.commons.vfs2.FileSystemOptions;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.s3n.vfs.S3NFileProvider;
import org.pentaho.vfs.ui.VfsFileChooserDialog;

/**
 * created by: rfellows date: 5/24/12
 */
public class S3NVfsFileChooserHelper extends VfsFileChooserHelper {

  public S3NVfsFileChooserHelper( Shell shell, VfsFileChooserDialog fileChooserDialog, VariableSpace variableSpace ) {
    super( shell, fileChooserDialog, variableSpace );
    setDefaultScheme( S3NFileProvider.SCHEME );
    setSchemeRestriction( S3NFileProvider.SCHEME );
  }

  public S3NVfsFileChooserHelper( Shell shell, VfsFileChooserDialog fileChooserDialog, VariableSpace variableSpace,
      FileSystemOptions fileSystemOptions ) {
    super( shell, fileChooserDialog, variableSpace, fileSystemOptions );
    setDefaultScheme( S3NFileProvider.SCHEME );
    setSchemeRestriction( S3NFileProvider.SCHEME );
  }

  @Override
  protected boolean returnsUserAuthenticatedFileObjects() {
    return true;
  }
}
