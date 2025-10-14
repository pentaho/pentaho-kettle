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
import org.pentaho.s3a.vfs.S3AFileProvider;
import org.pentaho.vfs.ui.VfsFileChooserDialog;


public class S3AVfsFileChooserHelper extends VfsFileChooserHelper {

  public S3AVfsFileChooserHelper( Shell shell, VfsFileChooserDialog fileChooserDialog, VariableSpace variableSpace ) {
    super( shell, fileChooserDialog, variableSpace );
    setDefaultScheme( S3AFileProvider.SCHEME );
    setSchemeRestriction( S3AFileProvider.SCHEME );
  }

  public S3AVfsFileChooserHelper( Shell shell, VfsFileChooserDialog fileChooserDialog, VariableSpace variableSpace,
                                  FileSystemOptions fileSystemOptions ) {
    super( shell, fileChooserDialog, variableSpace, fileSystemOptions );
    setDefaultScheme( S3AFileProvider.SCHEME );
    setSchemeRestriction( S3AFileProvider.SCHEME );
  }

  @Override
  protected boolean returnsUserAuthenticatedFileObjects() {
    return true;
  }
}
