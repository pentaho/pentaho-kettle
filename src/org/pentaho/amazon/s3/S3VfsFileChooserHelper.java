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
import org.pentaho.vfs.ui.VfsFileChooserDialog;

/**
 * created by: rfellows
 * date:       5/24/12
 */
public class S3VfsFileChooserHelper {

  private VfsFileChooserDialog fileChooserDialog = null;
  private Shell shell = null;
  private VariableSpace variableSpace = null;
  private FileSystemOptions fileSystemOptions = null;

  public S3VfsFileChooserHelper(Shell shell, VfsFileChooserDialog fileChooserDialog, VariableSpace variableSpace) {
    this(shell, fileChooserDialog, variableSpace, new FileSystemOptions());
  }

  public S3VfsFileChooserHelper(Shell shell, VfsFileChooserDialog fileChooserDialog, VariableSpace variableSpace, FileSystemOptions fileSystemOptions) {
    this.fileChooserDialog = fileChooserDialog;
    this.shell = shell;
    this.variableSpace = variableSpace;
    this.fileSystemOptions = fileSystemOptions;
  }

  public FileObject browse(String[] fileFilters, String[] fileFilterNames, String fileUri) throws KettleException, FileSystemException {
    return browse(fileFilters, fileFilterNames, fileUri, VfsFileChooserDialog.VFS_DIALOG_OPEN_DIRECTORY);
  }

  public FileObject browse(String[] fileFilters, String[] fileFilterNames, String fileUri, int fileDialogMode) throws KettleException, FileSystemException {
    return browse(fileFilters, fileFilterNames, fileUri, fileSystemOptions, fileDialogMode);
  }

  public FileObject browse(String[] fileFilters, String[] fileFilterNames, String fileUri, FileSystemOptions opts) throws KettleException, FileSystemException {
    return browse(fileFilters, fileFilterNames, fileUri, opts, VfsFileChooserDialog.VFS_DIALOG_OPEN_DIRECTORY);
  }

  public FileObject browse(String[] fileFilters, String[] fileFilterNames, String fileUri, FileSystemOptions opts, int fileDialogMode) throws KettleException, FileSystemException {
    // Get current file
    FileObject rootFile = null;
    FileObject initialFile = null;
    FileObject defaultInitialFile = KettleVFS.getFileObject("file:///c:/");

    if (fileUri != null) {
      initialFile = KettleVFS.getFileObject(fileUri, variableSpace, opts);
    } else {
      initialFile = KettleVFS.getFileObject(Spoon.getInstance().getLastFileOpened());
    }
    rootFile = initialFile.getFileSystem().getRoot();
    fileChooserDialog.setRootFile(rootFile);
    fileChooserDialog.setInitialFile(initialFile);

    fileChooserDialog.defaultInitialFile = rootFile;

    FileObject selectedFile = null;
    if(initialFile != null) {
      selectedFile = fileChooserDialog.open(shell, initialFile, initialFile.getName().getPath(), fileFilters, fileFilterNames,
          fileDialogMode, true);
    } else {
      selectedFile = fileChooserDialog.open(shell, null, AmazonSpoonPlugin.S3_SCHEME, false, null, fileFilters, fileFilterNames,
          fileDialogMode, true);
    }

    return selectedFile;
  }

  public VariableSpace getVariableSpace() {
    return variableSpace;
  }

  public void setVariableSpace(VariableSpace variableSpace) {
    this.variableSpace = variableSpace;
  }

  public FileSystemOptions getFileSystemOptions() {
    return fileSystemOptions;
  }

  public void setFileSystemOptions(FileSystemOptions fileSystemOptions) {
    this.fileSystemOptions = fileSystemOptions;
  }
}
