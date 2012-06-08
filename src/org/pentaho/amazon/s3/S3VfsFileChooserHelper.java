/*******************************************************************************
 *
 * Pentaho Big Data
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
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

package org.pentaho.amazon.s3;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemOptions;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.amazon.AmazonSpoonPlugin;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.vfs.VfsFileChooserHelper;
import org.pentaho.vfs.ui.VfsFileChooserDialog;

/**
 * created by: rfellows
 * date:       5/24/12
 */
public class S3VfsFileChooserHelper extends VfsFileChooserHelper {

  public S3VfsFileChooserHelper(Shell shell, VfsFileChooserDialog fileChooserDialog, VariableSpace variableSpace) {
    super(shell, fileChooserDialog, variableSpace);
    setDefaultScheme(AmazonSpoonPlugin.S3_SCHEME);
    setSchemeRestriction(AmazonSpoonPlugin.S3_SCHEME);
  }

  public S3VfsFileChooserHelper(Shell shell, VfsFileChooserDialog fileChooserDialog, VariableSpace variableSpace, FileSystemOptions fileSystemOptions) {
    super(shell, fileChooserDialog, variableSpace, fileSystemOptions);
    setDefaultScheme(AmazonSpoonPlugin.S3_SCHEME);
    setSchemeRestriction(AmazonSpoonPlugin.S3_SCHEME);
  }

  @Override
  protected boolean returnsUserAuthenticatedFileObjects() {
    return true;
  }

  @Override
  public boolean showFileScheme() {
    return false;
  }

}
